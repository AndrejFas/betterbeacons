package name.betterbeacons.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static final ScreenHandlerType<BeaconTrinketScreenHandler> BEACON_TRINKET_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of("betterbeacons", "beacon_trinket"),
                    new ScreenHandlerType<>(BeaconTrinketScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

//    public static final ScreenHandlerType<BeaconTrinketScreenHandler> BEACON_TRINKET_SCREEN_HANDLER =
//            new ScreenHandlerType<>(BeaconTrinketScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public static void register() {
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of("betterbeacons", "beacon_trinket"),
                BEACON_TRINKET_SCREEN_HANDLER);
    }
}