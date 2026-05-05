package name.personal_beacon.mixin;

import name.personal_beacon.item.BeaconTrinketItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void personal_beacon$applyVacuumEffect(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // This only runs when the player is equipped with your trinket
        // We look for your item in the trinket slots via your existing logic
        var component = dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player);
        if (component.isPresent()) {
            var equipped = component.get().getEquipped(stack -> stack.getItem() instanceof BeaconTrinketItem);
            if (!equipped.isEmpty()) {
                ItemStack trinketStack = equipped.get(0).getRight();
                int level = BeaconTrinketItem.getEffectLevel(trinketStack, "vacuum");

                if (level > 0) {
                    // Radius: Level 1 = 3 blocks, Level 4 = 9 blocks
                    double radius = 1.0 + (level * 2.0);
                    List<ItemEntity> items = player.getWorld().getEntitiesByClass(
                            ItemEntity.class,
                            player.getBoundingBox().expand(radius),
                            item -> item.isAlive() && !item.cannotPickup()
                    );

                    for (ItemEntity item : items) {
                        // Move item toward player
                        item.onPlayerCollision(player);
                    }
                }
            }
        }
    }
}