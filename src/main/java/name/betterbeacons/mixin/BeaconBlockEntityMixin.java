package name.betterbeacons.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import name.betterbeacons.item.BeaconTrinketItem;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {

    @Inject(method = "applyPlayerEffects", at = @At("TAIL"))
    private static void onApplyPlayerEffects(World world, BlockPos pos, int beaconLevel, RegistryEntry<StatusEffect> primary, RegistryEntry<StatusEffect> secondary, CallbackInfo ci) {
        if (world.isClient) return;

        // 1. Calculate the duration exactly like Minecraft does
        // Formula: (9 + level * 2) * 20 ticks
        int duration = (9 + beaconLevel * 2) * 20;

        // 2. Replicate the beacon's range logic
        double range = (double)beaconLevel * 10.0 + 10.0;
        Box box = (new Box(pos)).expand(range).stretch(0.0, (double)world.getHeight(), 0.0);

        List<PlayerEntity> players = world.getNonSpectatingEntities(PlayerEntity.class, box);

        for (PlayerEntity player : players) {
            TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
                var necklace = component.getEquipped(itemStack -> itemStack.getItem() instanceof BeaconTrinketItem);

                if (!necklace.isEmpty()) {
                    ItemStack stack = necklace.get(0).getRight();

                    // 3. Apply custom effects using the calculated duration
                    applyNecklaceEffects(player, stack, duration);
                }
            });
        }
    }

    private static void applyNecklaceEffects(PlayerEntity player, ItemStack stack, int duration) {
        // Ensure these methods are public and static in your BeaconTrinketItem class!
        BeaconTrinketItem.applyEffect(player, stack, "haste", StatusEffects.HASTE, duration);
        BeaconTrinketItem.applyEffect(player, stack, "speed", StatusEffects.SPEED, duration);
        BeaconTrinketItem.applyEffect(player, stack, "strength", StatusEffects.STRENGTH, duration);
        BeaconTrinketItem.applyEffect(player, stack, "resistance", StatusEffects.RESISTANCE, duration);
        BeaconTrinketItem.applyEffect(player, stack, "jump_boost", StatusEffects.JUMP_BOOST, duration);
        BeaconTrinketItem.applyEffect(player, stack, "regeneration", StatusEffects.REGENERATION, duration);
    }
}