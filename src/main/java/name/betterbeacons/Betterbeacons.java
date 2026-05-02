package name.betterbeacons;

import name.betterbeacons.item.BeaconTrinketItem;
import name.betterbeacons.component.ModDataComponents;

import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import name.betterbeacons.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Betterbeacons implements ModInitializer {
	public static final String MOD_ID = "betterbeacons";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Create the item instance
	public static final BeaconTrinketItem BEACON_TRINKET = new BeaconTrinketItem(new Item.Settings().maxCount(1));

	public static final ScreenHandlerType<BeaconTrinketScreenHandler> BEACON_TRINKET_SCREEN_HANDLER =
			Registry.register(Registries.SCREEN_HANDLER, Identifier.of("betterbeacons", "beacon_trinket"),
					new ScreenHandlerType<>(BeaconTrinketScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// 1. Register Data Components first
		ModDataComponents.register();
		ModScreenHandlers.register();

		// 2. Register the Item
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "beacon_trinket"), BEACON_TRINKET);
	}
}