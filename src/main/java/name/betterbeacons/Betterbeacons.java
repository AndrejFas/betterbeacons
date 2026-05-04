package name.betterbeacons;

import config.BetterBeaconsConfig;
import dev.emi.trinkets.api.TrinketsApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import name.betterbeacons.item.BeaconMedaillon;
import name.betterbeacons.item.BeaconTrinketItem;
import name.betterbeacons.component.ModDataComponents;
import name.betterbeacons.item.DragonLeather;
import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.loot.condition.RandomChanceWithEnchantedBonusLootCondition;

public class Betterbeacons implements ModInitializer {
	public static final String MOD_ID = "betterbeacons";
	public static BetterBeaconsConfig CONFIG;

	public static final Identifier OPEN_GUI_PACKET_ID = Identifier.of("betterbeacons", "open_gui");
	// 1. ADD THIS FIELD (Ensure it is public and static)
	public static final ScreenHandlerType<BeaconTrinketScreenHandler> BEACON_TRINKET_SCREEN_HANDLER =
			Registry.register(
					Registries.SCREEN_HANDLER,
					Identifier.of(MOD_ID, "beacon_trinket"),
					new ExtendedScreenHandlerType<>(BeaconTrinketScreenHandler::new, ItemStack.PACKET_CODEC)
			);

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Create the item instance
	public static final BeaconTrinketItem BEACON_TRINKET = new BeaconTrinketItem(new Item.Settings().maxCount(1));
	public static final DragonLeather DRAGON_LEATHER = new DragonLeather(new Item.Settings().rarity(Rarity.EPIC));
	public static final BeaconMedaillon BEACON_MEDAILLON = new BeaconMedaillon(new Item.Settings().maxCount(1));

	@Override
	public void onInitialize() {

		AutoConfig.register(BetterBeaconsConfig.class, (config, clazz) -> new GsonConfigSerializer<>(config, clazz));
		//AutoConfig.register(BetterBeaconsConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(BetterBeaconsConfig.class).getConfig();


		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// 1. Register Data Components first
		ModDataComponents.register();

		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "dragon_leather"), DRAGON_LEATHER);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "beacon_medaillon"), BEACON_MEDAILLON);
		// 2. Register the Item
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "beacon_trinket"), BEACON_TRINKET);

		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (EntityType.ENDER_DRAGON.getLootTableId().equals(key)) {

				var enchantmentRegistry = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
				var lootingEnchant = enchantmentRegistry.getOrThrow(Enchantments.LOOTING);

				LootPool.Builder poolBuilder = LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))

						// MODERN 1.21 LOOTING CONDITION
						// Arg 1: The Enchantment (Looting)
						// Arg 2: Base chance without enchantment (0.30f = 30%)
						// Arg 3: The bonus per level (0.14 = 14%) -> Lvl 5 = 30% + (5*14)% = 100%
						.conditionally(RandomChanceWithEnchantedBonusLootCondition.builder(
								registries,
								0.30f,
								0.14f // (If your IDE complains here, wrap this in EnchantmentLevelBasedValue.linear(0.30f, 0.14f))
						))

						// Your weighted items
						.with(ItemEntry.builder(DRAGON_LEATHER).weight(60)
								.apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f))))
						.with(ItemEntry.builder(DRAGON_LEATHER).weight(30)
								.apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f))))
						.with(ItemEntry.builder(DRAGON_LEATHER).weight(10)
								.apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(3.0f))));

				tableBuilder.pool(poolBuilder);
			}
		});

		PayloadTypeRegistry.playC2S().register(OpenBeaconTrinketPayload.ID, OpenBeaconTrinketPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(OpenBeaconTrinketPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ItemStack stack = ItemStack.EMPTY;

				// 1. Check Trinket Slot
				var trinketComp = TrinketsApi.getTrinketComponent(player);
				if (trinketComp.isPresent()) {
					var equipped = trinketComp.get().getEquipped(s -> s.getItem() instanceof BeaconTrinketItem);
					if (!equipped.isEmpty()) stack = equipped.get(0).getRight();
				}

				// 2. If not in Trinket slot, check Hands (Important for Right-Click logic!)
				if (stack.isEmpty()) {
					if (player.getMainHandStack().getItem() instanceof BeaconTrinketItem) stack = player.getMainHandStack();
					else if (player.getOffHandStack().getItem() instanceof BeaconTrinketItem) stack = player.getOffHandStack();
				}

				if (!stack.isEmpty()) {
					final ItemStack finalStack = stack; // Must be final for the anonymous class
					player.openHandledScreen(new ExtendedScreenHandlerFactory<ItemStack>() {
						@Override public ItemStack getScreenOpeningData(ServerPlayerEntity player) { return finalStack; }
						@Override public Text getDisplayName() { return Text.literal("Beacon Necklace"); }
						@Override public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
							return new BeaconTrinketScreenHandler(syncId, inv, finalStack);
						}
					});
				}

			});
		});

		AutoConfig.getConfigHolder(BetterBeaconsConfig.class).registerSaveListener((holder, config) -> {
			// This code runs whenever the config is saved via Mod Menu
			// You can put logic here to update things immediately
			return ActionResult.SUCCESS;
		});
	}
}