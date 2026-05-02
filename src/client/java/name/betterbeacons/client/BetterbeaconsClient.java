package name.betterbeacons.client;

import name.betterbeacons.client.gui.BeaconTrinketScreen;
import name.betterbeacons.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class BetterbeaconsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		System.out.println("CLIENT INITIALIZED: Registering Beacon Screen...");
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HandledScreens.register(ModScreenHandlers.BEACON_TRINKET_SCREEN_HANDLER, BeaconTrinketScreen::new);
	}
}