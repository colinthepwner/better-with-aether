package teamport.aether.util;

import net.minecraft.core.enums.HumanArmorShape;
import net.minecraft.core.enums.IArmorShape;

/**
 * An {@link IArmorShape} for an arbitrary armor slot index.
 *
 * <p>BTA 7.3 addressed armor slots by raw {@code int} ({@code armorItemInSlot(int)}). 8.0 replaced
 * that with {@link IArmorShape}, which covers the four vanilla slots — but the Aether's accessory
 * system adds slots past those (cape, quiver, pendant, gloves...), and those have no vanilla shape.
 *
 * <p>{@link #of(int)} keeps the old index-addressed behaviour working: vanilla indices resolve to
 * the real {@link HumanArmorShape} so protection and durability stay correct, and accessory indices
 * fall back to this wrapper. Accessory protection is applied by the accessory mixins rather than by
 * the shape, so zero is the right value here.
 */
public record AetherArmorSlot(int slotIndex) implements IArmorShape {
	@Override
	public int getSlotIndex() {
		return this.slotIndex;
	}

	@Override
	public float getDurabilityModifier() {
		return 1.0F;
	}

	@Override
	public int getProtectionValue() {
		return 0;
	}

	/** The shape for a raw slot index, preferring the vanilla shape when one matches. */
	public static IArmorShape of(int slotIndex) {
		for (HumanArmorShape shape : HumanArmorShape.values()) {
			if (shape.getSlotIndex() == slotIndex) return shape;
		}
		return new AetherArmorSlot(slotIndex);
	}
}
