package teamport.aether.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.components.OptionsCategory;
import net.minecraft.client.gui.options.components.ToggleableOptionComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionRange;
import net.minecraft.core.item.ItemStack;
import teamport.aether.block.AetherBlocks;

@Environment(EnvType.CLIENT)
public class AetherGameSettings {

    private AetherGameSettings(){}

    private static boolean hasInit = false;

    public static final OptionRange flickAccessorySpeed = GameSettings.register(
        new OptionRange("aether.flickAccessorySpeed", 5, 0, 60)
            .withDisplayStringProvider((mc, i18n, option) -> {
                int speed = (Integer) option.value;
                if (speed == 0) return "OFF";
                return speed + " seconds";
            })
    );

    public static void init() {
        if (!hasInit) {
            hasInit = true;
            registerSettings();
        }
    }

    public static void registerSettings() {
        OptionsPage AETHER = new OptionsPage("gui.options.page.aether.title", new ItemStack(AetherBlocks.CARVED_STONE_LIGHT))
            .withComponent(new OptionsCategory("gui.options.page.aether.category.user_interface")
                .withComponent(new ToggleableOptionComponent<>(flickAccessorySpeed))
            );
        OptionsPages.register(AETHER);
    }
}
