package teamport.aether.models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerRemote;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.ItemRenderer;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.client.render.renderer.GLRenderer;
import org.lwjgl.opengl.GL11;
import teamport.aether.item.DartInterface;

@Environment(EnvType.CLIENT)
public class ItemModelShooter extends ItemModelStandard {
    public ItemModelShooter(Item item, String namespace) {
        super(item, namespace);
    }

    public void render(net.minecraft.client.render.tessellator.TessellatorGeneral tessellator, net.minecraft.core.entity.Entity entity, net.minecraft.core.item.ItemStack itemstack, String renderType, boolean handheldTransform, int textureIndex, byte lightmapCoord, float brightness, boolean useColor) {
        super.render(tessellator, entity, itemstack, renderType, handheldTransform, textureIndex, lightmapCoord, brightness, useColor);
        
        Item nextDart = null;
        if (entity instanceof Player) {
            Player entityplayer = (Player) entity;
            nextDart = this.getNextDart(entityplayer);
        }

        if (nextDart != null) {
            GLRenderer.modelM4f().rotate(org.joml.Math.toRadians((float) (-90.0F)), 0.0F, 0.0F, 1.0F);
            GLRenderer.modelM4f().translate(-1.2F, 0.3F, 0.0625F);
            ItemModelDispatcher.getInstance().getDispatch(nextDart).renderGui(tessellator, entity, itemstack, 0, 0, (byte)0, brightness);
        }

    }

    public void renderItemOverlayIntoGUI(TessellatorGeneral tessellator, FontRenderer font, TextureManager textureManager, ItemStack itemStack, int x, int y, String text, float alpha) {
        Minecraft mc = Minecraft.getMinecraft();
        Item nextDart = this.getNextDart(mc.thePlayer);
        if (itemStack == mc.thePlayer.getHeldItem() && nextDart != null) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(GL11.GL_CULL_FACE);
            ItemModelStandard dartModel = (ItemModelStandard) ItemModelDispatcher.getInstance().getDispatch(nextDart);
            IconCoordinate textureIndex = dartModel.getIcon(mc.thePlayer, nextDart.getDefaultStack());
            GL11.glDisable(GL11.GL_LIGHTING);
            textureIndex.parentAtlas.bind();
            if (true) {
                int color = this.getColor(itemStack);
                float r = (color >> 16 & 255) / 255.0F;
                float g = (color >> 8 & 255) / 255.0F;
                float b = (color & 255) / 255.0F;
                GL11.glColor4f(r * 1.0F, g * 1.0F, b * 1.0F, alpha);
            } else {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
            }

            this.renderCoordinate(tessellator, textureIndex, (byte)0, 0, false, false);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
        }

        super.renderItemOverlayIntoGUI(tessellator, font, textureManager, itemStack, x, y, text, alpha);
    }

    public Item getNextDart(Player player) {
        DartInterface dartPlayer = (DartInterface) player;
        if (player instanceof PlayerRemote) {
            int id = dartPlayer.better_with_aether$getDartId();
            return id >= 0 && id < Item.itemsList.length ? Item.itemsList[id] : null;
        } else {
            return dartPlayer.better_with_aether$getNextDart();
        }
    }

    public void heldTransformThirdPerson(ItemRenderer renderer, Entity entity, ItemStack itemStack) {
        GLRenderer.modelM4f().translate(0.0F, 0.125F, 0.3125F);
        GLRenderer.modelM4f().rotate(org.joml.Math.toRadians((float) (-20.0F)), 0.0F, 1.0F, 0.0F);
        GLRenderer.modelM4f().scale(0.625F, -0.625F, 0.625F);
        GLRenderer.modelM4f().rotate(org.joml.Math.toRadians((float) (-100.0F)), 1.0F, 0.0F, 0.0F);
        GLRenderer.modelM4f().rotate(org.joml.Math.toRadians((float) (45.0F)), 0.0F, 1.0F, 0.0F);
    }
}
