package com.aki.modfix.mixin.vanilla.keybind;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.GameSettingsExtended;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.IOException;

@Mixin(value = GuiOptions.class, priority = Modfix.ModPriority)
public class MixinGuiOptions extends GuiScreen {
    /**
     * @author Aki
     * @reason Fix ModKeybinding System
     */
    @Overwrite
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            ((GameSettingsExtended)this.mc.gameSettings).ReloadKeyBindingSettings();//Fix ModKeybind Loader doesn't work
            this.mc.gameSettings.saveOptions();
        }

        super.keyTyped(typedChar, keyCode);
    }
}
