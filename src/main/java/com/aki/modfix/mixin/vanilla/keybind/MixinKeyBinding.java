package com.aki.modfix.mixin.vanilla.keybind;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.GameSettingsExtended;
import com.aki.modfix.util.fix.GuiNewChatExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(value = KeyBinding.class, priority = Modfix.ModPriority)
public class MixinKeyBinding {
    @Shadow
    private int keyCode;

    @Shadow @Final private static KeyBindingMap HASH;

    @Inject(method = "<init>(Ljava/lang/String;Lnet/minecraftforge/client/settings/IKeyConflictContext;Lnet/minecraftforge/client/settings/KeyModifier;ILjava/lang/String;)V", at = @At("RETURN"))
    public void InitDisableToAlt(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, int keyCode, String category, CallbackInfo ci) {
        if (keyModifier != KeyModifier.ALT && (keyCode == Keyboard.KEY_LMENU || keyCode == Keyboard.KEY_RMENU)) {
            this.keyCode = 0;
        }
    }

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private static void onTickInject(int keyCode, CallbackInfo ci) {
        Minecraft instance = Minecraft.getMinecraft();
        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            for (int key = 2; key <= 10; key++) {
                if (Keyboard.isKeyDown(key)) {
                    ((GameSettingsExtended) instance.gameSettings).setPatternID(key - 2);//差が、番号になる。
                    ((GameSettingsExtended) instance.gameSettings).ChangeKeyPatternEvent();
                    instance.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    GuiNewChat chat = instance.ingameGUI.getChatGUI();

                    List<ChatLine> lineList = ((GuiNewChatExtended) chat).getChatLines();

                    ((GuiNewChatExtended) chat).SetChatLines(lineList.stream().filter(o -> !o.getChatComponent().getUnformattedText().contains("You chose the key pattern number ")).collect(Collectors.toList()));

                    lineList = ((GuiNewChatExtended) chat).getDrawnChatLines();

                    ((GuiNewChatExtended) chat).SetDrawnChatLines(lineList.stream().filter(o -> !o.getChatComponent().getUnformattedText().contains("You chose the key pattern number ")).collect(Collectors.toList()));

                    //System.out.println("LS: " + lineList.size());

                    chat.printChatMessage(new TextComponentString("You chose the key pattern number " + (key - 1)).setStyle(new Style().setItalic(true).setColor(TextFormatting.AQUA)));
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "setKeyBindState", at = @At("HEAD"), cancellable = true)
    private static void FixSetKeyBindState(int keyCode, boolean pressed, CallbackInfo ci) {
        Minecraft instance = Minecraft.getMinecraft();
        int id = ((GameSettingsExtended) instance.gameSettings).getPatternID();
        KeyBinding[] keyBindings = ((GameSettingsExtended) instance.gameSettings).getPatternKeyBindings(id);
        List<KeyBinding> keyBindingsList = Arrays.asList(keyBindings != null ? keyBindings : new KeyBinding[0]);
        if (keyCode != 0) {
            for (KeyBinding keybinding : HASH.lookupAll(keyCode)) {
                if (keybinding != null) {
                    List<KeyBinding> MatchKeyBindings = keyBindingsList.stream().filter(keyBinding -> keyBinding.getKeyDescription().equals(keybinding.getKeyDescription())).collect(Collectors.toList());
                    if ((MatchKeyBindings.isEmpty() || MatchKeyBindings.size() == 1 && MatchKeyBindings.get(0).getKeyCode() == keyCode)) {
                        keybinding.pressed = pressed;
                    }
                }
            }
        }
        ci.cancel();
    }
}
