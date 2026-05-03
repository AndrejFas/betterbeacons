package name.betterbeacons.client.gui;

import name.betterbeacons.screen.BeaconTrinketScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BeaconTrinketScreen extends HandledScreen<BeaconTrinketScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of("betterbeacons", "textures/gui/container/pocket_beacon.png");
    private static final Identifier ICONS = Identifier.of("betterbeacons", "textures/gui/icons.png");

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

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                int effectIndex = row * 3 + col;
                this.addDrawableChild(new EffectButton(
                        x + 125 + (col * 22), y + 11 + (row * 22), 20, 20,
                        effectIndex, this
                ));
            }
        }

        // Clear All Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
            // Use 'this.client' or 'BeaconTrinketScreen.this.client'
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 100);
            }
        }).dimensions(x + 125, y + 100, 64, 15).build());
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
        int totalPoints = this.handler.getPropertyDelegate().get(0);
        int charges = this.handler.getPropertyDelegate().get(1);
        // These coordinates are relative to the top-left of the GUI

        // Draw the Point Counter (Under the blue triangle)
        context.drawText(this.textRenderer, "Power: " + totalPoints, 15, 40, 0x404040, false);

        context.drawText(this.textRenderer, "Charges: " + charges, 15, 50, 0xFFD700, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }



    // HELPER GETTERS - Move these here, outside the inner class!
    public MinecraftClient getScreenClient() { return this.client; }
    public BeaconTrinketScreenHandler getHandler() { return this.handler; }

    private class EffectButton extends ButtonWidget {
        private final int effectIndex;
        private final BeaconTrinketScreen screen;

        public EffectButton(int x, int y, int width, int height, int index, BeaconTrinketScreen screen) {
            super(x, y, width, height, Text.empty(), b -> {}, DEFAULT_NARRATION_SUPPLIER);
            this.effectIndex = index;
            this.screen = screen;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // 1. Draw the standard button texture (the gray box)
            super.renderWidget(context, mouseX, mouseY, delta);

            // 2. Calculate source coordinates in the PNG (44x44 grid)
            float u = (float) (this.effectIndex % 3) * 44;
            float v = (float) (this.effectIndex / 3) * 44;

            // 3. Draw with scaling
            context.drawTexture(
                    ICONS,
                    this.getX() + 1, this.getY() + 1, // Target X, Y
                    18, 18,                          // Target Width, Height (on screen)
                    u, v,                            // Source U, V (in the file)
                    44, 44,                          // Source Width, Height (how much to take from file)
                    132, 176                         // Total File Width, Height
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.active && this.visible && this.clicked(mouseX, mouseY)) {
                int packetId = (button == 1) ? (this.effectIndex + 50) : this.effectIndex;
                if (screen.getScreenClient().interactionManager != null) {
                    screen.getScreenClient().interactionManager.clickButton(screen.getHandler().syncId, packetId);
                    this.playDownSound(screen.getScreenClient().getSoundManager());
                }
                return true;
            }
            return false;
        }
    }

}
