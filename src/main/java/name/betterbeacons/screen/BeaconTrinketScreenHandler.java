package name.betterbeacons.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class BeaconTrinketScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    // CONSTRUCTOR 1: This is what the Client calls.
    // It MUST provide a 9-slot inventory to match the logic below.
    public BeaconTrinketScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(12)); // Changed 9 to 12
    }

    // CONSTRUCTOR 2: The actual logic used by both Client and Server.
    public BeaconTrinketScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.BEACON_TRINKET_SCREEN_HANDLER, syncId);
        checkSize(inventory, 12);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // --- Slot Placement (Matching beacon_trinket_screen_v01.png) ---

        // 1. Trinket Inventory (6 rows, 2 columns = 12 slots)
        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 2; ++col) {
                // Use * 2 because you have 2 columns!
                int index = col + (row * 2);
                this.addSlot(new Slot(inventory, index, 207 + col * 18, 11 + row * 18));
            }
        }

        // 2. Player Inventory & Hotbar
        int invX = 40;
        int invY = 130;
        addPlayerInventory(playerInventory, invX, invY);
        addPlayerHotbar(playerInventory, invX, invY + 58);
    }

    private void addPlayerInventory(PlayerInventory playerInventory, int x, int y) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory, int x, int y) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, x + col * 18, y));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (index < 12) { // 0-11 are your Trinket slots
                // Move from Trinket to Player Inventory
                if (!this.insertItem(originalStack, 12, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from Player Inventory to Trinket slots
                if (!this.insertItem(originalStack, 0, 12, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // ... rest of the method remains the same
        }
        return newStack;
    }
}