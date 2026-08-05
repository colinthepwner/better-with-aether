package teamport.aether.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.player.gamemode.Gamemode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.item.item_tool.AetherToolMaterial;

import static teamport.aether.item.item_tool.AetherToolMaterial.VALKYRIE_TOOL_EXTEND_RANGE_BY;

@Environment(EnvType.CLIENT)
@Mixin(value = Gamemode.class)
public abstract class GamemodeValkToolsReachMixin {
    @ModifyReturnValue(method = "getBlockReachDistance", at = @At("RETURN"))
    private float getBlockReachDistance(float original) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            return original + (AetherToolMaterial.isHoldingValkyrieTool(Minecraft.getMinecraft().thePlayer) ? VALKYRIE_TOOL_EXTEND_RANGE_BY : 0);
        }
        return original;
    }
    @ModifyReturnValue(method = "getEntityReachDistance", at = @At("RETURN"))
    private float getEntityReachDistance(float original) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            return original + (AetherToolMaterial.isHoldingValkyrieTool(Minecraft.getMinecraft().thePlayer) ? VALKYRIE_TOOL_EXTEND_RANGE_BY : 0);
        }
        return original;
    }
}
