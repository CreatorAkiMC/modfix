package com.aki.modfix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.Set;

public class ConfigGuiFactory implements IModGuiFactory {
    public static class GuiConfigScreen extends GuiConfig {

        public GuiConfigScreen(GuiScreen parent) {
            super(parent, new ConfigElement(ModfixConfig.cfg.getCategory(ModfixConfig.category)).getChildElements(), Modfix.MOD_ID, false, false, I18n.format("modfix.config.title"));
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new GuiConfigScreen(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
