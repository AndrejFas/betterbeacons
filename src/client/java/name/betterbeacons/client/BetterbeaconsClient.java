package name.betterbeacons.client;

import name.betterbeacons.Betterbeacons;
import name.betterbeacons.OpenBeaconTrinketPayload;
import name.betterbeacons.client.gui.BeaconTrinketScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class BetterbeaconsClient implements ClientModInitializer {

	public static KeyBinding openTrinketGuiKey;

	@Override
	public void onInitializeClient() {
		HandledScreens.register(Betterbeacons.BEACON_TRINKET_SCREEN_HANDLER, BeaconTrinketScreen::new);

		openTrinketGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.betterbeacons.open_gui", // Translation key
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G, // Default key
				"category.betterbeacons" // Category in controls menu
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openTrinketGuiKey.wasPressed()) {
				// Use the record that is actually registered on the server
				ClientPlayNetworking.send(new OpenBeaconTrinketPayload());
			}
		});
	}


}