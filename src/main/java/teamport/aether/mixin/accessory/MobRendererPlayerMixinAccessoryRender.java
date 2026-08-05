package teamport.aether.mixin.accessory;

import teamport.aether.util.AetherArmorSlot;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.container.ScreenInventory;
import net.minecraft.client.gui.container.ScreenInventoryCreative;
import net.minecraft.client.render.EntityRendererDispatcher;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import teamport.aether.compat.model.ModelBase;
import teamport.aether.compat.model.ModelBiped;
import net.minecraft.client.render.tessellator.TessellatorGeneral;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.player.gamemode.Gamemode;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teamport.aether.AetherMod;
import teamport.aether.entity.animal.aerbunny.MobAerbunny;
import teamport.aether.entity.player.PlayerUtil;
import teamport.aether.helper.GLManager;
import teamport.aether.helper.MixinHelper;
import teamport.aether.item.AetherItemTags;
import teamport.aether.item.AetherRepulsion;
import teamport.aether.item.accessory.IAccessory;
import teamport.aether.item.accessory.ItemGloves;
import teamport.aether.item.accessory.pendant.ItemPendant;
import teamport.aether.item.accessory.trinket.ItemGoldenFeather;
import teamport.aether.item.accessory.trinket.ItemIronBubble;
import teamport.aether.item.accessory.trinket.ItemRegenStone;
import teamport.aether.item.accessory.trinket.ItemRepulsionShield;

import static teamport.aether.AetherMod.MOD_ID;
import static teamport.aether.item.accessory.SlotAccessory.*;

@Environment(EnvType.CLIENT)
@Mixin(value = MobRendererPlayer.class)
public abstract class MobRendererPlayerMixinAccessoryRender extends net.minecraft.client.render.entity.MobRendererBipedArmored<Player> {
    public MobRendererPlayerMixinAccessoryRender() {
        super(0f);
    }
        @Unique
    private final ModelBiped modelArmor = new ModelBiped(1.0F);
    @Unique
    private final ModelBiped modelArmorChestplate = new ModelBiped(1.0F);
    @Unique
    private final ModelBiped modelAccessories = new ModelBiped(1.1F);
    @Unique
    private final ModelBiped modelHeart = new ModelBiped(1.0F);
    @Unique
    private final ModelBiped modelBubble = new ModelBiped(1.0F);
    @Unique
    private final ModelBiped modelFeather = new ModelBiped(1.0F);
    @Unique
    private final ModelBiped shield = new ModelBiped(1.5F);
    @Unique
    private boolean shield6 = false;



    // 8.0 threads the tessellator in as the first parameter of drawFirstPersonHand.
    @Inject(method = "drawFirstPersonHand(Lnet/minecraft/client/render/tessellator/TessellatorGeneral;Lnet/minecraft/core/entity/player/Player;Z)V", at = @At("TAIL"))
    private void callDrawFirstPersonHandAfter(TessellatorGeneral tessellator, @NonNull Player player, boolean isLeft, CallbackInfo ci) {
        ItemStack held = player.inventory.getCurrentItem();

        if ((held == null || !held.getItem().equals(Items.MAP)) && isLeft) {
            return;
        }

        ItemStack glovesStack = player.inventory.armorInventory[GLOVES_SLOT];
        if (glovesStack == null || !(glovesStack.getItem() instanceof ItemGloves)) {
            return;
        }

        Item item = glovesStack.getItem();
        String path = String.format("/assets/%s/textures/armor/gloves/%s_gloves.png", item.namespaceID.namespace(), ((IAccessory) item).name()
        );

        TextureManager textureManager = this.renderDispatcher.textureManager;
        if (textureManager == null) {
            return;
        }

        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        textureManager.loadTexture(path).bind();

        GL11.glDisable(GL11.GL_CULL_FACE);

        modelArmorChestplate.onGround = 0.0F;
        modelArmorChestplate.isRiding = false;
        modelArmorChestplate.sneaking = false;

        modelArmorChestplate.armLeft.visible = false;
        modelArmorChestplate.armRight.visible = false;

        modelArmorChestplate.setupAnimation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

        if (isLeft) {
            modelArmorChestplate.armLeft.visible = true;
            modelArmorChestplate.armLeft.render(0.0625F);
        } else {
            modelArmorChestplate.armRight.visible = true;
            modelArmorChestplate.armRight.render(0.0625F);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
    }

    
    
    @Inject(method = "getAndSetupModelForLayer(Lnet/minecraft/core/entity/player/Player;FFI)Lorg/useless/dragonfly/models/entity/StaticEntityModel;", at = @At("HEAD"), cancellable = true)
    private void hideArmorWhenInvisible(Player entity, float a, float b, int layer, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<org.useless.dragonfly.models.entity.StaticEntityModel> cir) {
        if (PlayerUtil.isInvisible(entity)) {
            cir.setReturnValue(null); return;
        }
        
    }







    // The whole-render injectors (alpha-test push/pop and the aerbunny) moved to
    // MobRendererMixinPlayerRender: 8.0 dropped MobRendererPlayer's render() override, leaving only
    // the inherited MobRenderer declaration, which mixin cannot target from here.

    @SuppressWarnings({"java:S6541", "java:S1075", "java:S3776"})
        @Inject(method = "renderAdditional", at = @At("TAIL"))
    private void renderAccessories(TessellatorGeneral tessellator, Player entity, float partialTick, CallbackInfo ci) {
        for (int layer = 4; layer < ((Player)entity).inventory.armorInventory.length; layer++) {
            renderAccessoryLayer(tessellator, entity, layer, partialTick);
        }
    }

    @Unique
    private void renderModelBiped(net.minecraft.client.render.tessellator.TessellatorGeneral tessellator, ModelBiped model, Player entity, float partialTick) {
        float limbSwing = this.getLimbSwing(entity, partialTick);
        float limbYaw = this.getLimbYaw(entity, partialTick);
        float limbPitch = this.getLimbPitch(entity, partialTick);
        float headYaw = this.getHeadYaw(entity, partialTick);
        float headPitch = this.getHeadPitch(entity, partialTick);
        
        ModelBase.bind(tessellator);
        model.render(limbSwing, limbYaw, limbPitch, headYaw, headPitch, 0.0625F);
        ModelBase.bind(null);
    }

    @Unique
    private void renderAccessoryLayer(TessellatorGeneral tessellator, Player entity, int layer, float partialTick) {
        modelAccessories.holdingLarge = shield.holdingLarge = modelFeather.holdingLarge = modelBubble.holdingLarge = modelHeart.holdingLarge = false;
        modelAccessories.holdingRightHand = shield.holdingRightHand = modelFeather.holdingRightHand = modelBubble.holdingRightHand = modelHeart.holdingRightHand = ((Player)entity).inventory.getCurrentItem() != null;
        modelAccessories.holdingLeftHand = shield.holdingLeftHand = modelFeather.holdingLeftHand = modelBubble.holdingLeftHand = modelHeart.holdingLeftHand = false;
        modelAccessories.sneaking = shield.sneaking = modelFeather.sneaking = modelBubble.sneaking = modelHeart.sneaking = ((Player)entity).isSneaking();
        modelAccessories.isRiding = shield.isRiding = modelFeather.isRiding = modelBubble.isRiding = modelHeart.isRiding = ((Player)entity).isPassenger();
        
        float swingProgress = ((Player)entity).getSwingProgress(partialTick);
        modelAccessories.onGround = shield.onGround = modelFeather.onGround = modelBubble.onGround = modelHeart.onGround = modelArmor.onGround = modelArmorChestplate.onGround = swingProgress;

        ItemStack armorStack = ((Player)entity).inventory.armorInventory[layer];
        if (armorStack == null) {
            return;
        }

        if ((armorStack.getItem() instanceof IAccessory || armorStack.getItem().hasTag(AetherItemTags.TRINKET)) && layer >= GLOVES_SLOT) {
            Item item = armorStack.getItem();

            if (item instanceof ItemGloves) {
                String path = String.format("/assets/%s/textures/armor/gloves/%s_gloves.png", item.namespaceID.namespace(), ((IAccessory) item).name());
                modelArmorChestplate.holdingRightHand = ((Player)entity).inventory.getCurrentItem() != null;
                modelArmorChestplate.sneaking = ((Player)entity).isSneaking();
                modelArmorChestplate.isRiding = ((Player)entity).isPassenger();
                modelArmorChestplate.armLeft.visible = layer == GLOVES_SLOT;
                modelArmorChestplate.armRight.visible = layer == GLOVES_SLOT;
                renderDispatcher.textureManager.loadTexture(path).bind();
                renderModelBiped(tessellator, modelArmorChestplate, entity, partialTick);
                return;
            }
            if ((item instanceof ItemRepulsionShield && (layer == TRINKET_2_SLOT || ((Player)entity).inventory.armorInventory[TRINKET_2_SLOT] == null)) || this.shield6) {
                this.shield6 = false;
                String path;
                if (((AetherRepulsion) entity).aether$isRepulse()) {
                    path = "/assets/aether/textures/armor/energyGlow.png";
                } else {
                    path = "/assets/aether/textures/armor/energyNotGlow.png";
                }

                renderDispatcher.textureManager.loadTexture(path).bind();
                GLManager.glEnable(GL11.GL_CULL_FACE);
                GLManager.glEnable(GL11.GL_BLEND);
                if (PlayerUtil.isInvisible(entity)) {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.25F);
                    GL11.glEnable(GL11.GL_BLEND);
                } else {
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                }
                renderModelBiped(tessellator, shield, entity, partialTick);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                return;
            }

            ///  redirect the render to next item
            if (item instanceof ItemRepulsionShield && layer == 6) {
                this.shield6 = true;
                ItemStack nextSlot = ((Player)entity).inventory.armorInventory[layer + 1];
                if (nextSlot != null) {
                    item = nextSlot.getItem();
                    layer += 1;
                }
            }

            ItemStack itemTrinketSlot1 = ((Player)entity).inventory.armorInventory[TRINKET_1_SLOT];
            ItemStack itemTrinketSlot2 = ((Player)entity).inventory.armorInventory[TRINKET_2_SLOT];

            if (item instanceof ItemGoldenFeather) {
                String path;

                if (layer == TRINKET_1_SLOT) {
                    path = "/assets/aether/textures/armor/trinkets/feather_gold_trinket_helmet.png";
                    setUpFeatherOnHelmet();
                } else {
                    path = "/assets/aether/textures/armor/trinkets/feather_gold_trinket_boots.png";
                    setUpFeathersOnBoots();
                }

                modelFeather.body.visible = false;
                modelFeather.armLeft.visible = false;
                modelFeather.armRight.visible = false;
                renderDispatcher.textureManager.loadTexture(path).bind();
                renderModelBiped(tessellator, modelFeather, entity, partialTick);
                return;
            }

            String textureKey = MixinHelper.TRINKET_TEXTURES.get(item);
            if (textureKey != null) {
                if (layer != TRINKET_1_SLOT && layer != TRINKET_2_SLOT) {
                    return;
                }

                boolean leftSlot = (layer == TRINKET_1_SLOT);
                String path = String.format("/assets/%s/textures/armor/trinkets/%s.png", AetherMod.MOD_ID, textureKey);

                modelAccessories.head.visible = false;
                modelAccessories.body.visible = false;
                modelAccessories.armLeft.visible = false;
                modelAccessories.armRight.visible = false;

                modelAccessories.legLeft.visible  = leftSlot;
                modelAccessories.legRight.visible = !leftSlot;

                renderDispatcher.textureManager.loadTexture(path).bind();
                renderModelBiped(tessellator, modelAccessories, entity, partialTick);
                return;
            }

            if (item instanceof ItemPendant) {
                int variant = 0;
                if (layer == TRINKET_2_SLOT && itemTrinketSlot1 != null && itemTrinketSlot1.getItem() instanceof ItemPendant) {
                    variant = 1;
                }
                String path = String.format("/assets/%s/textures/armor/pendants/%s_pendant_%d.png", item.namespaceID.namespace(), ((IAccessory) item).name(), variant);
                modelAccessories.body.visible = true;
                renderDispatcher.textureManager.loadTexture(path).bind();
                renderModelBiped(tessellator, modelAccessories, entity, partialTick);
                return;
            }

            if (item instanceof ItemRegenStone) {
                String variant = "right";
                if (layer == TRINKET_2_SLOT) {
                    variant = "left";
                }
                String path = String.format("/assets/%s/textures/armor/trinkets/%s_%s.png", item.namespaceID.namespace(), ((IAccessory) item).name(), variant);
                modelHeart.head.visible = true;
                renderDispatcher.textureManager.loadTexture(path).bind();
                renderModelBiped(tessellator, modelHeart, entity, partialTick);
                return;
            }

            if (item instanceof ItemIronBubble) {
                boolean isInTrinketSlot1 = itemTrinketSlot1 != null && itemTrinketSlot1.getItem() instanceof ItemIronBubble;
                boolean isInTrinketSlot2 = itemTrinketSlot2 != null && itemTrinketSlot2.getItem() instanceof ItemIronBubble;
                if ((isInTrinketSlot1 && layer == TRINKET_1_SLOT) || (isInTrinketSlot2 && !isInTrinketSlot1 && layer == TRINKET_2_SLOT)) {

                    String path = "/assets/aether/textures/armor/trinkets/bubble_trinket.png";

                    modelBubble.head.visible = true;
                    modelBubble.body.visible = false;
                    modelBubble.armLeft.visible = false;
                    modelBubble.armRight.visible = false;
                    modelBubble.legLeft.visible = false;
                    modelBubble.legRight.visible = false;

                    renderDispatcher.textureManager.loadTexture(path).bind();
                    GLManager.glEnable(GL11.GL_CULL_FACE);
                    GLManager.glEnable(GL11.GL_BLEND);
                    if (PlayerUtil.isInvisible(entity)) {
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.25F);
                        GL11.glEnable(GL11.GL_BLEND);
                    } else {
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    }
                    renderModelBiped(tessellator, modelBubble, entity, partialTick);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                    return;
                }
            }
        }
    }

    @Unique
    private void setUpFeatherOnHelmet() {
        modelFeather.head.visible = true;
        modelFeather.legLeft.visible = false;
        modelFeather.legRight.visible = false;
        modelFeather.head.addBox(-4.0F, -12.0F, -4.0F, 8, 12, 12, 1.1f);
    }

    @Unique
    private void setUpFeathersOnBoots() {
        modelFeather.head.visible = false;
        modelFeather.legLeft.visible = true;
        modelFeather.legRight.visible = true;
        modelFeather.legLeft.addBox(-2.0F, 2.0F, -3.0F, 4, 12, 8, 1.1f);
        modelFeather.legRight.addBox(-2.0F, 2.0F, -3.0F, 4, 12, 8, 1.1f);
    }
}
