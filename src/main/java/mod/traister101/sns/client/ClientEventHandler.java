package mod.traister101.sns.client;

import mod.traister101.sns.client.models.*;
import mod.traister101.sns.client.screen.ContainerItemScreen;
import mod.traister101.sns.common.menu.SNSMenus;
import mod.traister101.sns.compat.curios.CuriosCompat;
import top.theillusivec4.curios.api.CuriosApi;

import net.minecraft.client.gui.screens.MenuScreens;

import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ClientEventHandler {

	public static void init() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(ClientEventHandler::onClientSetup);
		modEventBus.addListener(ClientEventHandler::registerKeyBindings);
		modEventBus.addListener(ClientEventHandler::registerLayers);
		modEventBus.addListener(SacksNSuchGuiOverlay::registerOverlays);
	}

	private static void onClientSetup(final FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(SNSMenus.SACK_MENU.get(), ContainerItemScreen::new);
			if (ModList.get().isLoaded(CuriosApi.MODID)) {
				CuriosCompat.clientSetup();
			}
		});
	}

	private static void registerKeyBindings(final RegisterKeyMappingsEvent event) {
		event.register(SNSKeybinds.TOGGLE_PICKUP);
	}

	private static void registerLayers(final RegisterLayerDefinitions event) {
		event.registerLayerDefinition(FramePackModel.LAYER_LOCATION, FramePackModel::createBodyLayer);
		event.registerLayerDefinition(SmallSackModel.LAYER_LOCATION, SmallSackModel::createBodyLayer);
		event.registerLayerDefinition(LargeSackModel.LAYER_LOCATION, LargeSackModel::createBodyLayer);
	}
}