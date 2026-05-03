package name.betterbeacons.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public class BeaconTrinketItem extends TrinketItem {

    public BeaconTrinketItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            user.openHandledScreen(new ExtendedScreenHandlerFactory<ItemStack>() {
                @Override
                public ItemStack getScreenOpeningData(ServerPlayerEntity player) {
                    return stack; // This is the payload sent to the client constructor
                }

                @Override
                public Text getDisplayName() {
                    return Text.literal("Beacon Trinket");
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player) {
                    return new BeaconTrinketScreenHandler(syncId, playerInv, stack);
                }
            });
        }
        return TypedActionResult.success(stack);
    }



    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.betterbeacons.beacon_trinket").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }

    public static void applyEffect(PlayerEntity player, ItemStack stack, String nbtKey, RegistryEntry<StatusEffect> effect, int duration) {
        int level = getEffectLevel(stack, nbtKey);
        if (level > 0) {
            player.addStatusEffect(new StatusEffectInstance(effect, duration, level - 1, true, true));
        }
    }

    // 2. Update the helper methods
    public static DefaultedList<ItemStack> getInventory(ItemStack stack, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        DefaultedList<ItemStack> items = DefaultedList.ofSize(12, ItemStack.EMPTY);

        net.minecraft.component.type.NbtComponent nbtComponent = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);

        if (nbtComponent != null) {
            net.minecraft.nbt.NbtCompound nbt = nbtComponent.copyNbt();
            if (nbt.contains("Items", 9)) {
                // Note the third argument: registries
                net.minecraft.inventory.Inventories.readNbt(nbt, items, registries);
            }
        }
        return items;
    }

    public static void saveInventory(ItemStack stack, DefaultedList<ItemStack> items, RegistryWrapper.WrapperLookup registries) {
        if (stack.isEmpty()) return;

        NbtCompound nbt = new NbtCompound();
        // This helper from net.minecraft.inventory.Inventories writes the list to NBT
        Inventories.writeNbt(nbt, items, registries);

        // Update the item's component data
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static int getEffectLevel(ItemStack stack, String effectName) {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent != null) {
            NbtCompound nbt = nbtComponent.copyNbt();
            return nbt.getInt(effectName + "_level");
        }
        return 0;
    }

    public static void setEffectLevel(ItemStack stack, String effectName, int level) {
        NbtCompound nbt = new NbtCompound();
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (nbtComponent != null) {
            nbt = nbtComponent.copyNbt();
        }

        nbt.putInt(effectName + "_level", level);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private boolean isReceivingBeaconPower(PlayerEntity player) {
        // We check the player's current status effects
        return player.getStatusEffects().stream().anyMatch(effect ->
                // 1. Ambient particles = From a beacon (usually)
                effect.isAmbient() &&
                        // 2. Short duration = Currently being refreshed by a nearby source
                        effect.getDuration() <= 320 && // 320 ticks = 16 seconds
                        // 3. Match against standard beacon effect types
                        isStandardBeaconEffect(effect.getEffectType())
        );
    }

    private boolean isStandardBeaconEffect(RegistryEntry<StatusEffect> effect) {
        return effect.equals(StatusEffects.SPEED) ||
                effect.equals(StatusEffects.HASTE) ||
                effect.equals(StatusEffects.RESISTANCE) ||
                effect.equals(StatusEffects.JUMP_BOOST) ||
                effect.equals(StatusEffects.STRENGTH) ||
                effect.equals(StatusEffects.REGENERATION);
    }

}