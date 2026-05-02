package name.betterbeacons.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class BeaconTrinketItem extends Item implements Trinket {

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInv, player) -> {
                return new BeaconTrinketScreenHandler(syncId, playerInv);
            }, Text.translatable("item.betterbeacons.beacon_trinket")));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    public BeaconTrinketItem(Settings settings) {
        super(settings);
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        // This logic runs every tick while the item is equipped!
        if (!entity.getWorld().isClient()) {
            // We will add the effect application logic here soon
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.betterbeacons.beacon_trinket").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
