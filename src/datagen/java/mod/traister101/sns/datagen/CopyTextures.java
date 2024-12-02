package mod.traister101.sns.datagen;

import com.google.common.collect.Lists;
import com.google.common.hash.*;

import net.minecraft.Util;
import net.minecraft.data.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CopyTextures implements DataProvider {

	private final PackOutput packOutput;
	private final Iterable<Path> inputFolders;

	public CopyTextures(final PackOutput packOutput, final Iterable<Path> inputFolders) {
		this.packOutput = packOutput;
		this.inputFolders = inputFolders;
	}

	private static String getName(final Path folderPath, final Path filePath) {
		final String s = folderPath.relativize(filePath).toString().replaceAll("\\\\", "/");
		return s.substring(0, s.length() - ".png".length());
	}

	private static TaskResult readImage(final Path imagePath, final String name) {
		try {
			final byte[] bytes = Files.readAllBytes(imagePath);
			final var hashCode = Hashing.murmur3_128().hashBytes(bytes);
			return new TaskResult(name, bytes, hashCode);
		} catch (final Throwable throwable) {
			throw new ImageCopyException(imagePath, throwable);
		}
	}

	@Override
	public CompletableFuture<?> run(final CachedOutput cachedOutput) {
		final Path ouputPath = packOutput.getOutputFolder();
		final List<CompletableFuture<?>> list = Lists.newArrayList();

		for (final var folderPath : inputFolders) {
			list.add(CompletableFuture.supplyAsync(() -> {
				try (final var stream = Files.walk(folderPath).filter(p -> p.toString().endsWith(".png"))) {
					return CompletableFuture.allOf(stream.map(imagePath -> CompletableFuture.runAsync(() -> {
						final TaskResult taskResult = readImage(imagePath, getName(folderPath, imagePath));
						storeImageIfChanged(cachedOutput, taskResult, ouputPath);
					}, Util.backgroundExecutor())).toArray(CompletableFuture[]::new));
				} catch (final Exception exception) {
					throw new RuntimeException("Failed to read texture input directory, aborting", exception);
				}
			}, Util.backgroundExecutor()).thenCompose(Function.identity()));
		}

		return Util.sequenceFailFast(list);
	}

	@Override
	public String getName() {
		return "Copy Textures";
	}

	private void storeImageIfChanged(final CachedOutput cachedOutput, final TaskResult taskResult, final Path directoryPath) {
		final Path outputPath = directoryPath.resolve(taskResult.name + ".png");

		try {
			cachedOutput.writeIfNeeded(outputPath, taskResult.payload, taskResult.hash);
		} catch (final IOException e) {
			LOGGER.error("Couldn't write structure {} at {}", taskResult.name, outputPath, e);
		}
	}

	@SuppressWarnings("serial")
	private static class ImageCopyException extends RuntimeException {

		public ImageCopyException(final Path path, final Throwable cause) {
			super(path.toAbsolutePath().toString(), cause);
		}
	}

	record TaskResult(String name, byte[] payload, HashCode hash) {

	}
}