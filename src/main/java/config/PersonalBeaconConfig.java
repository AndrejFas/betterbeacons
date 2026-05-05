package config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

@Config(name = "personal_beacon")
public class PersonalBeaconConfig implements ConfigData {

    // --- VANILLA BEACON SECTION ---
    @ConfigEntry.Gui.CollapsibleObject
    public VanillaSettings vanillaBeacon = new VanillaSettings();

    // --- TRINKET BEACON SECTION ---
    @ConfigEntry.Gui.CollapsibleObject
    public TrinketSettings trinketBeacon = new TrinketSettings();

    // Sub-class for Vanilla
    public static class VanillaSettings {
        @ConfigEntry.Gui.Tooltip
        public List<Double> beaconRanges = Arrays.asList(20.0, 30.0, 40.0, 50.0, 65.0, 80.0, 100.0, 150.0);
    }

    // Sub-class for Trinket
    public static class TrinketSettings {
        public double copperValue = 5.0;
        public double ironValue = 6.0;
        public double goldValue = 7.0;
        public double emeraldValue = 8.0;
        public double diamondValue = 10.0;

        @ConfigEntry.Gui.Tooltip
        public double netheriteMultiplier = 0.5;
        @ConfigEntry.Gui.Tooltip
        public double uniqueMultiplier = 0.25;
    }
}