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
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!entity.getWorld().isClient() && entity instanceof PlayerEntity player) {
            // Run every 4 seconds
            if (entity.getWorld().getTime() % 80 == 0) {
                applyEffect(player, stack, "haste", StatusEffects.HASTE);
                applyEffect(player, stack, "speed", StatusEffects.SPEED);
                applyEffect(player, stack, "strength", StatusEffects.STRENGTH);
                // Add more here to match your switch-case...
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.betterbeacons.beacon_trinket").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }

    private void applyEffect(PlayerEntity player, ItemStack stack, String key, RegistryEntry<StatusEffect> effect) {
        int level = getEffectLevel(stack, key);
        if (level > 0) {
            player.addStatusEffect(new StatusEffectInstance(effect, 160, level - 1, true, false, true));
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

    public static void saveInventory(ItemStack stack, DefaultedList<ItemStack> items, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
        // Added registries as the third argument
        net.minecraft.inventory.Inventories.writeNbt(nbt, items, registries);

        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(nbt));
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


}