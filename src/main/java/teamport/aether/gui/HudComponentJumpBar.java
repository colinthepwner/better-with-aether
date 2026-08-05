package teamport.aether.gui;

import net.minecraft.client.option.GameSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.hud.HudIngame;
import net.minecraft.client.gui.hud.component.HudComponentMovable;
import net.minecraft.client.gui.hud.component.layout.Layout;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import org.lwjgl.opengl.GL11;
import teamport.aether.entity.AetherJumpAmount;

@Environment(EnvType.CLIENT)
public class HudComponentJumpBar extends HudComponentMovable {
    private static final IconCoordinate jump_full = TextureRegistry.getTexture("aether:gui/hud/jump_full");
    private static final IconCoordinate jump_empty = TextureRegistry.getTexture("aether:gui/hud/jump_empty");

    public HudComponentJumpBar(String key, Layout layout) {
        super(key, 81, 10, layout);
    }

    @Override
    public boolean isVisible() {
        return GameSettings.IMMERSIVE_MODE.drawHotbar() && mc.thePlayer.vehicle instanceof AetherJumpAmount;
    }

    @Override
    public void render(HudIngame hud, int xSizeScreen, int ySizeScreen, float partialTick) {
        if (!(mc.thePlayer.vehicle instanceof AetherJumpAmount)) return;

        int maxJumps = ((AetherJumpAmount) mc.thePlayer.vehicle).getJumpMaxAmount();
        int currentJumps = ((AetherJumpAmount) mc.thePlayer.vehicle).getJumpAmount();

        int baseX = this.getLayout().getComponentX(this, xSizeScreen);
        int baseY = this.getLayout().getComponentY(this, ySizeScreen);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);

        for (int i = 0; i < maxJumps; i++) {
            int row = i / 10;
            int column = i % 10;

            int x = baseX + column * 8;
            int y = baseY - row * 10;

            IconCoordinate icon = (i < currentJumps) ? jump_full : jump_empty;
            hud.drawGuiIcon(x, y, 9, 9, icon);
        }
    }

    @Override
    public void renderPreview(Gui gui, Layout layout, int screenWidth, int screenHeight) {
        int x = layout.getComponentX(this, screenWidth);
        int y = layout.getComponentY(this, screenHeight);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);

        int previewFilled = 5;

        for (int i = 0; i < 10; i++) {
            IconCoordinate icon = (i < previewFilled) ? jump_full : jump_empty;
            gui.drawGuiIcon(x + i * 8, y, 9, 9, icon);
        }
    }
}
