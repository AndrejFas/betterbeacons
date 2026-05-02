package name.betterbeacons.client.gui;

import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BeaconTrinketScreen extends HandledScreen<BeaconTrinketScreenHandler> {
    // OLD: private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/beacon.png");

    // NEW:
    private static final Identifier TEXTURE = Identifier.of("betterbeacons", "textures/gui/container/pocket_beacon.png");

    public BeaconTrinketScreen(BeaconTrinketScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 256;
        this.backgroundHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Add 12 buttons for effects (3 columns x 4 rows)
        for (int row = 0; row < 4; row++) { // Changed from 3 to 4
            for (int col = 0; col < 3; col++) { // Changed from 4 to 3
                int effectIndex = row * 3 + col; // Now calculates based on 3 columns

                this.addDrawableChild(ButtonWidget.builder(Text.literal(""), button -> {
                            // Logic to select effect send packet to server
                            selectEffect(effectIndex);
                        })
                        // Adjusted spacing: col * 22 for X, row * 22 for Y
                        .dimensions(x + 125 + (col * 22), y + 11 + (row * 22), 20, 20)
                        .build());
            }
        }

        // 2. The "Clear All" Button
        // Positioned below the last row (row 3).
        // Since the last row is at y + 11 + (3 * 22) = 77, we put this at y + 100.
        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
                    // Logic to clear all effects
                    //clearAllEffects();
                })
                // x + 70 aligns it with the left of the grid.
                // 62 width spans roughly the width of all 3 columns.
                .dimensions(x + 125, y + 100, 64, 15)
                .build());
    }

    private void selectEffect(int effectIndex) {
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(
                TEXTURE,
                x,                    // Screen X
                y,                    // Screen Y
                this.backgroundWidth, // Width on screen (256)
                this.backgroundHeight,// Height on screen (220)
                0.0f,                 // u: Start X in PNG
                0.0f,                 // v: Start Y in PNG
                1024,                 // regionWidth: Take all 1024 horizontal pixels
                880,                  // regionHeight: Take all 880 vertical pixels
                1024,                 // textureWidth: The actual width of your file
                880                   // textureHeight: The actual height of your file
        );
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // These coordinates are relative to the top-left of the GUI

        // Draw the Point Counter (Under the blue triangle)
        context.drawText(this.textRenderer, "0450", 20, 42, 0xFFFFFF, true);

        // Draw the "Available" text
        context.drawText(this.textRenderer, "Available", 15, 60, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "2x", 25, 70, 0x00FF00, false); // Green for count
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }


}
