package com.redandwhitefox.personalbeacon.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import com.redandwhitefox.personalbeacon.PersonalBeacon;
import com.redandwhitefox.personalbeacon.client.gui.BeaconTrinketScreen;
import com.redandwhitefox.personalbeacon.network.OpenBeaconTrinketPayload;

public class PersonalBeaconClient implements ClientModInitializer {

	public static KeyBinding openTrinketGuiKey;

	@Override
	public void onInitializeClient() {
		HandledScreens.register(PersonalBeacon.BEACON_TRINKET_SCREEN_HANDLER, BeaconTrinketScreen::new);

		openTrinketGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.personalbeacon.open_gui", // Translation key
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G, // Default key
				"category.personalbeacon" // Category in controls menu
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openTrinketGuiKey.wasPressed()) {
				// Use the record that is actually registered on the server
				ClientPlayNetworking.send(new OpenBeaconTrinketPayload());
			}
		});
	}


}