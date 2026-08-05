package teamport.aether.mixin.armor.wolf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.entity.animal.MobAnimal;
import net.minecraft.core.entity.animal.MobWolf;
import net.minecraft.core.item.material.ArmorMaterial;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import teamport.aether.item.AetherArmorMaterial;

@Mixin(value = MobWolf.class)
public abstract class MobWolfMixinZaniteResistance extends MobAnimal {
    protected MobWolfMixinZaniteResistance(World world) {
        super(world);
    }
    @SuppressWarnings("java:S1161")
    @Shadow
    public abstract int getMaxHealth();
    @WrapOperation(method = {"damageEntity", "lavaHurt", "fireHurt"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/entity/animal/MobWolf;getTotalProtectionAmount(Lnet/minecraft/core/util/helper/DamageType;)F"))
    private float reduceWolfDamage(MobWolf instance, DamageType damageType, Operation<Float> original) {
        float originalProtection = original.call(instance, damageType);
        net.minecraft.core.item.ItemStack armorStack = instance.getArmorItem();
        if (armorStack != null && armorStack.getItem() instanceof net.minecraft.core.item.IArmorItem) {
            ArmorMaterial armorMaterial = ((net.minecraft.core.item.IArmorItem) armorStack.getItem()).getArmorMaterial();
            if (armorMaterial == AetherArmorMaterial.ZANITE) {
                float healthPercentage = (float) instance.getHealth() / instance.getMaxHealth();
                return MathHelper.lerp(AetherArmorMaterial.ZANITE_BROKEN.getProtection(damageType) * 1.5f, originalProtection, healthPercentage);
            }
        }
        return originalProtection;
    }
}
