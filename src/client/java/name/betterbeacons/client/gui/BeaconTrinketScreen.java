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

        // 3 Rows, 4 Columns
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int effectIndex = row * 4 + col;

                // X start moved from 105 to 90
                this.addDrawableChild(new EffectButton(
                        x + 100 + (col * 24), y + 15 + (row * 35), 20, 20,
                        effectIndex, this
                ));
            }
        }

        // Aligned the "X" button to the new grid start (x + 90)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Clear all"), button -> {
            if (this.client != null && this.client.interactionManager != null) {
                this.client.interactionManager.clickButton(this.handler.syncId, 100);
            }
        }).dimensions(x + 15, y + 95, 80, 20).build());
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
            // 1. Draw the button box
            super.renderWidget(context, mouseX, mouseY, delta);

            // 2. Draw the Icon
            // Note: Use % 3 if your PNG is still 3 columns wide, or % 4 if you changed it!
            int u = (this.effectIndex % 3) * 44;
            int v = (this.effectIndex / 3) * 44;

            context.drawTexture(ICONS, this.getX() + 1, this.getY() + 1, 18, 18, u, v, 44, 44, 132, 176);

            // 3. Draw the Level Number
            int level = screen.getHandler().getLevelForIndex(this.effectIndex);
            if (level > 0) {
                String levelText = String.valueOf(level);

                // Access the field directly: screen.textRenderer
                int textWidth = screen.textRenderer.getWidth(levelText);

                context.drawText(
                        screen.textRenderer,
                        levelText,
                        this.getX() + (this.width / 2) - (textWidth / 2),
                        this.getY() + this.height + 2, // 2 pixels below the button
                        0xFFFFFF,
                        true
                );
            }
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
