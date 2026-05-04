package name.betterbeacons.item;

import dev.emi.trinkets.api.TrinketItem;
import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
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
    public void onUnequip(ItemStack stack, dev.emi.trinkets.api.SlotReference slot, net.minecraft.entity.LivingEntity entity) {
        // 1. Always check if the entity is a player
        if (entity instanceof PlayerEntity player) {
            // 2. Only run this on the server side
            if (!player.getWorld().isClient) {
                removeTrinketAttributes(player);
            }
        }
        super.onUnequip(stack, slot, entity);
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

    public static void applySpecialAttributes(PlayerEntity player, ItemStack stack, int duration) {
        // 1. Reach Scaling: +1.0 block per level (Total +4.0 at Level 4)
        int reachLvl = getEffectLevel(stack, "reach");
        if (reachLvl > 0) {
            double boost = reachLvl * 1.0;
            applyTempModifier(player, EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, "trinket_block_reach", boost);
            applyTempModifier(player, EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, "trinket_entity_reach", boost);
        }

        // 2. Knockback Resistance: +0.25 per level (Level 4 = 1.0, 100% resistance)
        int kbLvl = getEffectLevel(stack, "knockback_res");
        if (kbLvl > 0) {
            double boost = kbLvl * 0.25;
            applyTempModifier(player, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "trinket_kb", boost);
        }
    }

    private static void applyTempModifier(PlayerEntity player, RegistryEntry<EntityAttribute> attr, String id, double value) {
        var instance = player.getAttributeInstance(attr);
        if (instance != null) {
            Identifier modifierId = Identifier.of("betterbeacons", id);
            if (instance.getModifier(modifierId) == null) {
                instance.addTemporaryModifier(new EntityAttributeModifier(
                        modifierId, value, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public static boolean hasEffect(ItemStack stack, String effectName) {
        return getEffectLevel(stack, effectName) > 0;
    }

    // Ensure removeTrinketAttributes stays clean
    public static void removeTrinketAttributes(PlayerEntity player) {
        removeModifier(player, EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, "trinket_block_reach");
        removeModifier(player, EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, "trinket_entity_reach");
        removeModifier(player, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "trinket_kb");
    }

    private static void removeModifier(PlayerEntity player, RegistryEntry<EntityAttribute> attr, String id) {
        var instance = player.getAttributeInstance(attr);
        if (instance != null) {
            instance.removeModifier(Identifier.of("betterbeacons", id));
        }
    }
}