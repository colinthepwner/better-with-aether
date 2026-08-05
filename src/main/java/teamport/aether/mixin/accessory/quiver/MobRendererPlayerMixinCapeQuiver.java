package teamport.aether.mixin.accessory.quiver;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import teamport.aether.compat.model.ModelBase;
import teamport.aether.compat.model.ModelBiped;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemQuiver;
import net.minecraft.core.item.ItemQuiverEndless;
import net.minecraft.core.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class)
public abstract class MobRendererPlayerMixinCapeQuiver extends net.minecraft.client.render.entity.MobRendererBipedArmored<Player> {
    public MobRendererPlayerMixinCapeQuiver() {
        super(0f);
    }

        @Unique
    private final ModelBiped quiver = new ModelBiped(1.05F);
    @Unique
    private void renderQuiverModel(net.minecraft.client.render.tessellator.TessellatorGeneral tessellator, ModelBiped model, Player entity, float partialTick) {
        float limbSwing = this.getLimbSwing(entity, partialTick);
        float limbYaw = this.getLimbYaw(entity, partialTick);
        float limbPitch = this.getLimbPitch(entity, partialTick);
        float headYaw = this.getHeadYaw(entity, partialTick);
        float headPitch = this.getHeadPitch(entity, partialTick);
        
        ModelBase.bind(tessellator);
        model.render(limbSwing, limbYaw, limbPitch, headYaw, headPitch, 0.0625F);
        ModelBase.bind(null);
    }

    @Inject(method = "renderAdditional", at = @At("TAIL"))
    private void renderQuiverAdditional(TessellatorGeneral tessellator, Player player, float partialTick, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        quiver.holdingLarge = false;
        quiver.holdingRightHand = player.inventory.getCurrentItem() != null;
        quiver.holdingLeftHand = false;
        quiver.sneaking = player.isSneaking();
        quiver.isRiding = player.isPassenger();
        quiver.onGround = player.getSwingProgress(partialTick);

        if (player.inventory.armorInventory.length <= 5) return;
        ItemStack armorStack = player.inventory.armorInventory[5]; // cape slot is 5
        if (armorStack == null) return;
        
        Item item = armorStack.getItem();
        ItemStack chestplate = player.inventory.armorInventory[2];
        
        if (item instanceof ItemQuiver) {
            String path = "/assets/minecraft/textures/armor/quiver.png";
            if (chestplate != null && (chestplate.getItem() instanceof ItemQuiver || chestplate.getItem() instanceof ItemQuiverEndless)) {
                path = "/assets/aether/textures/armor/quiver_flipped.png";
            }
            renderDispatcher.textureManager.loadTexture(path).bind();
            ModelBiped modelBiped = this.quiver;
            modelBiped.body.visible = true;
            renderQuiverModel(tessellator, modelBiped, player, partialTick);
            return;
        }
        if (item instanceof ItemQuiverEndless) {
            String path = "/assets/minecraft/textures/armor/quiver_golden.png";
            if (chestplate != null && (chestplate.getItem() instanceof ItemQuiver || chestplate.getItem() instanceof ItemQuiverEndless)) {
                path = "/assets/aether/textures/armor/quiver_golden_flipped.png";
            }
            renderDispatcher.textureManager.loadTexture(path).bind();
            ModelBiped modelBiped = this.quiver;
            modelBiped.body.visible = true;
            renderQuiverModel(tessellator, modelBiped, player, partialTick);
        }
    }
}
