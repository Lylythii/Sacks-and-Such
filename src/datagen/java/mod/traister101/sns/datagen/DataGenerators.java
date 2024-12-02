package mod.traister101.sns.datagen;

import mod.traister101.sns.SacksNSuch;
import mod.traister101.sns.datagen.providers.*;
import mod.traister101.sns.datagen.providers.tags.*;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import lombok.experimental.UtilityClass;

@UtilityClass
@Mod.EventBusSubscriber(modid = SacksNSuch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {

	@SubscribeEvent
	public static void gatherData(final GatherDataEvent event) {
		final var generator = event.getGenerator();
		final var lookupProvider = event.getLookupProvider();
		final var existingFileHelper = event.getExistingFileHelper();
		final var packOutput = generator.getPackOutput();

		final var blockTags = generator.addProvider(event.includeServer(), new BuiltInBlockTags(packOutput, lookupProvider, existingFileHelper));
		generator.addProvider(event.includeServer(), new BuiltInItemTags(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
		generator.addProvider(event.includeServer(), new BuiltInEntityTags(packOutput, lookupProvider, existingFileHelper));
		generator.addProvider(event.includeServer(), new BuiltInRecipes(packOutput));
		generator.addProvider(event.includeServer(), new BuiltInCurios(packOutput, existingFileHelper, lookupProvider));
		generator.addProvider(event.includeServer(), new BuiltInItemSizes(packOutput));
		generator.addProvider(event.includeServer(), new BuiltInItemHeats(packOutput));
		final var advancementProvider = BuiltInAvdancements.create(packOutput, lookupProvider, existingFileHelper);
		generator.addProvider(event.includeServer(), advancementProvider);

		generator.addProvider(event.includeClient(), new BuiltInLanguage(packOutput, advancementProvider));
		generator.addProvider(event.includeClient(), new BuiltInItemModels(packOutput, existingFileHelper));

		generator.addProvider(event.includeClient() || event.includeDev(), new CopyTextures(packOutput, event.getInputs()));
	}
}