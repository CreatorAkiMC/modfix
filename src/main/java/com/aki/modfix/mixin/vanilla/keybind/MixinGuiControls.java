package com.aki.modfix.mixin.vanilla.keybind;

import com.aki.modfix.util.fix.GameSettingsExtended;
import com.aki.modfix.util.fix.GuiControlsGetter;
import com.aki.modfix.util.fix.GuiKeyBindingListAltSet;
import com.aki.modfix.util.fix.KeyBindingRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.util.List;

/**
 * keyBindingList を独自のものに置き換える
 * KeyBindingList.drawScreen を置き換える
 * */
@Mixin(GuiControls.class)
public class MixinGuiControls extends GuiScreen implements GuiControlsGetter {
    @Shadow private GuiButton buttonReset;

    @Shadow protected String screenTitle;

    @Shadow @Final private static GameSettings.Options[] OPTIONS_ARR;

    @Shadow public KeyBinding buttonId;

    @Shadow @Final private GameSettings options;

    @Shadow @Final private GuiScreen parentScreen;

    @Shadow public long time;
    @Unique
    public GuiKeyBindingListAltSet keyBindingListAltSet = null;

    @Unique
    public int Index = 0;

    public void initGui()
    {
        this.keyBindingListAltSet = new GuiKeyBindingListAltSet((GuiControls) (Object)this, this.mc);
        this.buttonList.add(new GuiButton(200, this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("gui.done")));
        this.buttonReset = this.addButton(new GuiButton(201, this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("controls.resetAll")));
        this.screenTitle = I18n.format("controls.title");
        int i = 0;

        for (GameSettings.Options gamesettings$options : OPTIONS_ARR)
        {
            if (gamesettings$options.isFloat())
            {
                this.buttonList.add(new GuiOptionSlider(gamesettings$options.getOrdinal(), this.width / 2 - 155 + i % 2 * 160, 18 + 24 * (i >> 1), gamesettings$options));
            }
            else
            {
                this.buttonList.add(new GuiOptionButton(gamesettings$options.getOrdinal(), this.width / 2 - 155 + i % 2 * 160, 18 + 24 * (i >> 1), gamesettings$options, this.options.getKeyBinding(gamesettings$options)));
            }

            ++i;
        }
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.keyBindingListAltSet.handleMouseInput();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (this.buttonId != null)
        {
            this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), -100 + mouseButton);
            if(!this.keyBindingListAltSet.getIsVanilla()) {
                int D = ((GameSettingsExtended) this.mc.gameSettings).getPatternID();
                ((GameSettingsExtended) this.mc.gameSettings).setPatternID(this.keyBindingListAltSet.Pattern);
                ((GameSettingsExtended) this.mc.gameSettings).SetKeyBindingRegister(new KeyBindingRegister(this.buttonId.getKeyDescription(), this.buttonId.getKeyCode(), this.buttonId.getKeyCategory(), this.buttonId.getKeyModifier()), Index);
                ((GameSettingsExtended) this.mc.gameSettings).setPatternID(D);
            }
            this.options.setOptionKeyBinding(this.buttonId, -100 + mouseButton);
            this.buttonId = null;
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else if (mouseButton != 0 || !this.keyBindingListAltSet.mouseClicked(mouseX, mouseY, mouseButton))
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (state != 0 || !this.keyBindingListAltSet.mouseReleased(mouseX, mouseY, state)) {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 200)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 201)
        {
            for (KeyBinding keybinding : ((GameSettingsExtended)this.mc.gameSettings).MCKeyBinding())
            {
                keybinding.setToDefault();
            }

            for (int i = 0; i < ((GameSettingsExtended)this.mc.gameSettings).ModRegisteredBinding().length; i++)
            {
                KeyBinding keybinding = ((GameSettingsExtended)this.mc.gameSettings).ModRegisteredBinding()[i];
                keybinding.setToDefault();

                int D = ((GameSettingsExtended) this.mc.gameSettings).getPatternID();
                ((GameSettingsExtended) this.mc.gameSettings).setPatternID(this.keyBindingListAltSet.Pattern);
                ((GameSettingsExtended) this.mc.gameSettings).SetKeyBindingRegister(new KeyBindingRegister(keybinding.getKeyDescription(), keybinding.getKeyCode(), keybinding.getKeyCategory(), keybinding.getKeyModifier()), Index);
                ((GameSettingsExtended) this.mc.gameSettings).setPatternID(D);
            }

            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else if (button.id < 100 && button instanceof GuiOptionButton)
        {
            this.options.setOptionValue(((GuiOptionButton)button).getOption(), 1);
            button.displayString = this.options.getKeyBinding(GameSettings.Options.byOrdinal(button.id));
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.buttonId != null)
        {
            if (keyCode == 1)
            {
                this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.NONE, 0);

                if(!this.keyBindingListAltSet.getIsVanilla())
                    SetKeyLists(this.buttonId, this.Index);

                this.options.setOptionKeyBinding(this.buttonId, 0);
            }
            else if (keyCode != 0)
            {
                if((keyCode != Keyboard.KEY_LMENU && keyCode != Keyboard.KEY_RMENU)) {
                    this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), keyCode);

                    if (!this.keyBindingListAltSet.getIsVanilla())
                        SetKeyLists(this.buttonId, this.Index);

                    this.options.setOptionKeyBinding(this.buttonId, keyCode);
                }
            }
            else if (typedChar > 0 && ((typedChar + 256) != Keyboard.KEY_LMENU && (typedChar + 256) != Keyboard.KEY_RMENU))
            {
                this.buttonId.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), typedChar + 256);

                if(!this.keyBindingListAltSet.getIsVanilla())
                    SetKeyLists(this.buttonId, this.Index);

                this.options.setOptionKeyBinding(this.buttonId, typedChar + 256);
            }

            if (!net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(keyCode))
                this.buttonId = null;
            this.time = Minecraft.getSystemTime();
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    public void SetKeyLists(KeyBinding AfterKeyBinding, int index) {
        int D = ((GameSettingsExtended) this.mc.gameSettings).getPatternID();
        ((GameSettingsExtended) this.mc.gameSettings).setPatternID(this.keyBindingListAltSet.Pattern);
        ((GameSettingsExtended) this.mc.gameSettings).SetKeyBindingRegister(new KeyBindingRegister(AfterKeyBinding.getKeyDescription(), AfterKeyBinding.getKeyCode(), AfterKeyBinding.getKeyCategory(), AfterKeyBinding.getKeyModifier()), index);
        ((GameSettingsExtended) this.mc.gameSettings).setPatternID(D);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.keyBindingListAltSet.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 8, 16777215);
        boolean flag = false;
        for (KeyBinding keybinding : ((GameSettingsExtended)this.mc.gameSettings).MCKeyBinding())
        {
            if (!keybinding.isSetToDefaultValue())
            {
                flag = true;
                break;
            }
        }
        if(!flag)
            for (KeyBinding keybinding : ((GameSettingsExtended)this.mc.gameSettings).ModRegisteredBinding())
            {
                if (!keybinding.isSetToDefaultValue())
                {
                    flag = true;
                    break;
                }
            }

        this.buttonReset.enabled = flag;
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public List<GuiButton> getButton() {
        return this.buttonList;
    }

    @Override
    public void SetIndex(int Index) {
        this.Index = Index;
    }


}
