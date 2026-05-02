package name.betterbeacons.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Items;

public class BeaconTrinketScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    // CONSTRUCTOR 1: The Client uses this.
    public BeaconTrinketScreenHandler(int syncId, PlayerInventory playerInventory) {
        // We pass a new ArrayPropertyDelegate(1) so the client has a "container" for the points data.
        this(syncId, playerInventory, new SimpleInventory(12), new ArrayPropertyDelegate(1));
    }

    // CONSTRUCTOR 2: Used by the Server.
    public BeaconTrinketScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {
        super(ModScreenHandlers.BEACON_TRINKET_SCREEN_HANDLER, syncId);
        checkSize(inventory, 12);
        checkDataCount(delegate, 1);
        this.inventory = inventory;

        // ADD THIS BLOCK:
        if (inventory instanceof SimpleInventory simpleInventory) {
            // This code runs whenever the inventory changes
            simpleInventory.addListener(this::onContentChanged);
        }

        this.propertyDelegate = delegate;

        inventory.onOpen(playerInventory.player);
        this.addProperties(delegate);

        // 1. Trinket Inventory (6 rows, 2 columns)
        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 2; ++col) {
                int index = col + (row * 2);
                this.addSlot(new Slot(inventory, index, 207 + col * 18, 11 + row * 18));
            }
        }

        // 2. Player Inventory & Hotbar
        int invX = 40;
        int invY = 130;
        addPlayerInventory(playerInventory, invX, invY);
        addPlayerHotbar(playerInventory, invX, invY + 58);

        // Inside the main constructor of BeaconTrinketScreenHandler.java
        this.addProperties(new PropertyDelegate() {
            @Override
            public int get(int index) {
                // This runs on the Server when syncing data to the Client
                if (index == 0) {
                    return (int) calculatePoints();
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                // Usually left empty as points are derived from items, not set manually
            }

            @Override
            public int size() {
                return 1;
            }
        });
    }

    // This is the "Server-side" math logic
    public double calculatePoints() {
        double basePoints = 0;
        double multiplier = 1.0;

        for (int i = 0; i < 12; i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (stack.isEmpty()) continue;

            // Multiply by stack count if you want 64 blocks to be worth more than 1!
            double val = switch (stack.getItem()) {
                case Item item when item == Items.COPPER_BLOCK -> 5.0;
                case Item item when item == Items.IRON_BLOCK -> 7.0;
                case Item item when item == Items.GOLD_BLOCK -> 10.0;
                case Item item when item == Items.EMERALD_BLOCK -> 12.0;
                case Item item when item == Items.DIAMOND_BLOCK -> 15.0;
                default -> 0.0;
            };

            basePoints += (val * stack.getCount());

            if (stack.isOf(Items.NETHERITE_BLOCK)) {
                // Each netherite block adds 0.5 to the multiplier
                multiplier += (0.5 * stack.getCount());
            }
        }
        return basePoints * multiplier;
    }

    // Accessor for the Screen to use
    public int getSyncedPoints() {
        return this.propertyDelegate.get(0);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (index < 12) {
                if (!this.insertItem(originalStack, 12, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, 12, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        System.out.println("Hello!");
        // This forces the server to sync the PropertyDelegate data
        // to the client whenever a slot is updated.
        this.propertyDelegate.set(0, (int) calculatePoints());
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

    // Inside BeaconTrinketScreenHandler class
    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }
}