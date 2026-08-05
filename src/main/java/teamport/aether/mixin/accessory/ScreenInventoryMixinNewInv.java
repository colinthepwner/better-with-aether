package teamport.aether.mixin.accessory;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.container.ScreenInventory;
import net.minecraft.client.render.font.FontRenderer;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.texture.Texture;
import net.minecraft.core.enums.HumanArmorShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = ScreenInventory.class)
public abstract class ScreenInventoryMixinNewInv {

    /// Keeps `checkForArmor` walking only the four vanilla armour shapes.
    ///
    /// It loops to `armorInventory.length` but looks each index up in `HumanArmorShape.values()`,
    /// which has four entries. `ContainerInventoryMixinAccessory` widens that array to eight to hold
    /// the accessory slots, so the loop runs off the end of the enum the moment the inventory screen
    /// opens. Same shape of clash as the one `MenuInventoryMixinAddSlotAdjSlot` handles in the menu
    /// constructor -- vanilla's own loops have to keep seeing the vanilla count.
    @Definition(id = "armorInventory", field = "Lnet/minecraft/core/player/inventory/container/ContainerInventory;armorInventory:[Lnet/minecraft/core/item/ItemStack;")
    @Expression("?.armorInventory.length")
    @ModifyExpressionValue(method = "checkForArmor", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int onlyVanillaArmorShapes(int original) {
        return HumanArmorShape.values().length;
    }

    // binds new texture
    @WrapOperation(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextureManager;loadTexture(Ljava/lang/String;)Lnet/minecraft/client/render/texture/Texture;"))
    private Texture bindNewInventory(TextureManager instance, String name, Operation<Texture> original) {
        return original.call(instance, "/assets/aether/textures/gui/container/inventory.png");
    }
    // adjust text position
    @WrapOperation(method = "drawGuiContainerForegroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/container/ScreenInventory;drawStringNoShadow(Lnet/minecraft/client/render/font/FontRenderer;Ljava/lang/CharSequence;III)V"))
    private void fixLabelPlacement(ScreenInventory instance, FontRenderer font, CharSequence text, int x, int y, int color, Operation<Void> original) {
        original.call(instance, font, text, 98, y, color);
    }
}
