package com.aki.modfix.mixin.vanilla.keybind;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.GameSettingsExtended;
import com.aki.modfix.util.fix.KeyBindingRegister;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.client.settings.KeyModifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Mixin(value = GameSettings.class, priority = Modfix.ModPriority)
public abstract class MixinKeyBindingGameSettings implements GameSettingsExtended {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public boolean invertMouse;

    @Shadow
    public float mouseSensitivity;

    @Shadow
    public float fovSetting;

    @Shadow
    public float gammaSetting;

    @Shadow
    public float saturation;

    @Shadow
    public int renderDistanceChunks;

    @Shadow
    public int guiScale;

    @Shadow
    public int particleSetting;

    @Shadow
    public boolean viewBobbing;

    @Shadow
    public boolean anaglyph;

    @Shadow
    public int limitFramerate;

    @Shadow
    public boolean fboEnable;

    @Shadow
    public EnumDifficulty difficulty;

    @Shadow
    public boolean fancyGraphics;

    @Shadow
    public int ambientOcclusion;

    @Shadow
    public int clouds;

    @Shadow
    @Final
    private static Gson GSON;

    @Shadow
    public List<String> resourcePacks;

    @Shadow
    public List<String> incompatibleResourcePacks;

    @Shadow
    public String lastServer;

    @Shadow
    public String language;

    @Shadow
    public EntityPlayer.EnumChatVisibility chatVisibility;

    @Shadow
    public boolean chatColours;

    @Shadow
    public boolean chatLinks;

    @Shadow
    public boolean chatLinksPrompt;

    @Shadow
    public float chatOpacity;

    @Shadow
    public boolean snooperEnabled;

    @Shadow
    public boolean fullScreen;

    @Shadow
    public boolean enableVsync;

    @Shadow
    public boolean useVbo;

    @Shadow
    public boolean hideServerAddress;

    @Shadow
    public boolean advancedItemTooltips;

    @Shadow
    public boolean pauseOnLostFocus;

    @Shadow
    public boolean touchscreen;

    @Shadow
    public int overrideWidth;

    @Shadow
    public int overrideHeight;

    @Shadow
    public boolean heldItemTooltips;

    @Shadow
    public float chatHeightFocused;

    @Shadow
    public float chatHeightUnfocused;

    @Shadow
    public float chatScale;

    @Shadow
    public float chatWidth;

    @Shadow
    public int mipmapLevels;

    @Shadow
    public boolean forceUnicodeFont;

    @Shadow
    public boolean reducedDebugInfo;

    @Shadow
    public boolean useNativeTransport;

    @Shadow
    public boolean entityShadows;

    @Shadow
    public EnumHandSide mainHand;

    @Shadow
    public int attackIndicator;

    @Shadow
    public boolean showSubtitles;

    @Shadow
    public boolean realmsNotifications;

    @Shadow
    public boolean enableWeakAttacks;

    @Shadow
    public boolean autoJump;

    @Shadow
    public int narrator;

    @Shadow
    public TutorialSteps tutorialStep;

    @Shadow
    public abstract float getSoundLevel(SoundCategory category);

    @Shadow
    @Final
    private Set<EnumPlayerModelParts> setModelParts;

    @Shadow
    public abstract void sendSettingsToServer();

    @Shadow
    private File optionsFile;

    @Shadow
    @Final
    private Map<SoundCategory, Float> soundLevels;

    @Shadow
    protected abstract NBTTagCompound dataFix(NBTTagCompound p_189988_1_);

    @Shadow
    protected abstract float parseFloat(String str);

    @Shadow
    @Final
    private static Type TYPE_LIST_STRING;

    @Shadow
    public abstract void setModelPartEnabled(EnumPlayerModelParts modelPart, boolean enable);


    @Shadow
    public KeyBinding[] keyBindings;
    @Shadow
    @Final
    public static Splitter COLON_SPLITTER;
    @Unique
    public KeyBinding[] VanillaKeyBinding;

    //[0 ~ 8] [Keys]

    @Unique
    public KeyBinding[][] ModKeyBinding = new KeyBinding[9][];
    @Unique
    public KeyBindingRegister[][] ModKeyBindingRegister = new KeyBindingRegister[9][];

    @Unique
    public int Pattern = 0;//表示は +1 する

    @Unique
    public File ModsKeySettingFile = null;

    @Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", at = @At("RETURN"))
    public void NewCreateFile(Minecraft mcIn, File mcDataDir, CallbackInfo ci) {
        this.ModsKeySettingFile = new File(mcDataDir, "ModsKeySettings.txt");
        this.VanillaKeyBinding = this.keyBindings;
    }

    @Inject(method = "<init>()V", at = @At("RETURN"))
    public void ChangeToCustomKeys(CallbackInfo ci) {
        this.VanillaKeyBinding = this.keyBindings;
    }

    @Inject(method = "loadOptions", at = @At("RETURN"))
    public void LoadOptionsFixReturn(CallbackInfo ci) {
        this.VanillaKeyBinding = this.keyBindings;
        try {
            //なかった時にデフォルトのものを適用。
            Path path = Paths.get(this.ModsKeySettingFile.getPath());
            if (!Files.exists(path) || (Files.exists(path) && Files.size(path) < 1)) {
                for(KeyBinding vanillaKey : this.VanillaKeyBinding) {
                    for (int i = 0; i < this.ModKeyBinding[0].length; i++) {//念のためコピー
                        KeyBinding ModKey = this.ModKeyBinding[0][i];
                        if(vanillaKey.getKeyDescription().equals(ModKey.getKeyDescription())) {
                            ModKey.setKeyModifierAndCode(vanillaKey.getKeyModifier(), vanillaKey.getKeyCode());
                            this.ModKeyBindingRegister[0][i] = new KeyBindingRegister(ModKey.getKeyDescription(), vanillaKey.getKeyCode(), ModKey.getKeyCategory(), vanillaKey.getKeyModifier());
                        }
                    }
                }

                for (int i = 1; i < 9; i++) {
                    this.ModKeyBinding[i] = this.ModKeyBinding[0];//別の配列に同じものをコピー
                    this.ModKeyBindingRegister[i] = this.ModKeyBindingRegister[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.ReloadKeyBindingSettings();
    }

    @Unique
    @Override
    public void ReloadKeyBindingSettings() {
        /**
         *
         * 保存した設定の読み込み
         *
         * */
        if (this.ModsKeySettingFile != null) {
            Path path = this.ModsKeySettingFile.toPath();
            try {
                //ファイルがある場合に読み込む
                if (Files.exists(path) || !(Files.exists(path) && Files.size(path) < 1)) {
                    List<String> list = IOUtils.readLines(Files.newInputStream(this.ModsKeySettingFile.toPath()), StandardCharsets.UTF_8); // Forge: fix MC-117449, MC-151173
                    int idx = 0;
                    for (String s : list) {
                        try {
                            Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(s).iterator();
                            String s1 = iterator.next();
                            String s2 = iterator.next();
                            if (!s1.equals("Select Pattern ")) {
                                if (s1.contains("[idx ")) {
                                    idx = Integer.parseInt(s2.toLowerCase(Locale.ROOT).replace(" ]", "").replace(" ", "")) - 1;
                                } else {
                                    KeyBinding[] keyBindings = this.ModKeyBinding[idx];
                                    KeyBindingRegister[] registers = this.ModKeyBindingRegister[idx];

                                    for (int i = 0; i < keyBindings.length; i++) {
                                        KeyBinding keybinding = keyBindings[i];
                                        KeyBindingRegister register = registers[i];
                                        if (s1.equals("key_" + keybinding.getKeyDescription())) {
                                            if (s2.indexOf(':') != -1) {
                                                String[] t = s2.split(":");
                                                keybinding.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.valueFromString(t[1]), Integer.parseInt(t[0]));
                                                register.SetData(keybinding.getKeyDescription(), keybinding.getKeyCode(), keybinding.getKeyCategory(), net.minecraftforge.client.settings.KeyModifier.valueFromString(t[1]));
                                            } else {
                                                keybinding.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.NONE, Integer.parseInt(s2));
                                                register.SetData(keybinding.getKeyDescription(), keybinding.getKeyCode(), keybinding.getKeyCategory(), KeyModifier.NONE);
                                            }
                                        }

                                        keyBindings[i] = keybinding;
                                        registers[i] = register;
                                    }
                                    this.ModKeyBinding[idx] = keyBindings;
                                    this.ModKeyBindingRegister[idx] = registers;
                                }
                            }
                        } catch (Exception ignored) {

                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to File Check KeyBinds by (ModFix mod) ", e);
            }
        }
    }

    @Inject(method = "saveOptions", at = @At("RETURN"))
    public void SaveOptionsReturn(CallbackInfo ci) {
        PrintWriter printwriter = null;
        try {
            printwriter = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(this.ModsKeySettingFile.toPath()), StandardCharsets.UTF_8));
            printwriter.println("Create by ModFix mod (Creator Aki)");
            printwriter.println("Select Pattern : " + this.Pattern);
            for (int i = 0; i < 9; i++) {//[1] ~ [9] (0 ~ 8)
                printwriter.println("[idx : " + (i + 1) + " ]");
                for (int i2 = 0; i2 < this.ModKeyBinding[i].length; i2++) {
                    KeyBinding keybinding = this.ModKeyBinding[i][i2];
                    KeyBindingRegister register = this.ModKeyBindingRegister[i][i2];

                    String keyString = "key_" + keybinding.getKeyDescription() + ":" + register.keycode;
                    printwriter.println(register.modifier != net.minecraftforge.client.settings.KeyModifier.NONE ? keyString + ":" + register.modifier : keyString);
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to save mod key options", exception);
        } finally {
            IOUtils.closeQuietly(printwriter);
        }
    }

    @Override
    public KeyBinding[] MCKeyBinding() {
        return this.VanillaKeyBinding;
    }

    @Override
    public KeyBinding[] ModRegisteredBinding() {
        return this.ModKeyBinding[this.Pattern];
    }

    @Override
    public KeyBindingRegister[] KeyBindingRegister() {
        return this.ModKeyBindingRegister[this.Pattern];
    }

    @Override
    public void SetModRegisteredBinding(KeyBinding[] ModCustomKeyBinding) {
        this.ModKeyBinding[this.Pattern] = ModCustomKeyBinding;
        //NullPointerError 回避
        this.ModKeyBindingRegister[this.Pattern] = new KeyBindingRegister[ModCustomKeyBinding.length];

        for (int i = 0; i < ModCustomKeyBinding.length; i++) {
            this.ModKeyBindingRegister[this.Pattern][i] = new KeyBindingRegister(ModCustomKeyBinding[i].getKeyDescription(), ModCustomKeyBinding[i].getKeyCode(), ModCustomKeyBinding[i].getKeyCategory(), ModCustomKeyBinding[i].getKeyModifier());
        }

        Path path = Paths.get(this.ModsKeySettingFile.getPath());
        try {
            //ファイルがない場合やファイルがあっても中身がない場合などの処理
            if (this.Pattern == 0 || (Files.exists(path) && Files.size(path) < 1)) {
                //すべてのパターンにチェック
                for (int i1 = 1; i1 < 9; i1++) {
                    this.ModKeyBinding[i1] = ModCustomKeyBinding;
                    this.ModKeyBindingRegister[i1] = new KeyBindingRegister[ModCustomKeyBinding.length];
                    for (int i = 0; i < ModCustomKeyBinding.length; i++) {
                        this.ModKeyBindingRegister[i1][i] = new KeyBindingRegister(ModCustomKeyBinding[i].getKeyDescription(), ModCustomKeyBinding[i].getKeyCode(), ModCustomKeyBinding[i].getKeyCategory(), ModCustomKeyBinding[i].getKeyModifier());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to File Check KeyBinds by (ModFix mod) ", e);
        }
    }

    @Override
    public int getPatternID() {
        return this.Pattern;
    }

    // Set range 0 ~ 8
    @Override
    public void setPatternID(int Id) {
        this.Pattern = Id;
    }

    @Override
    public void SetKeyBindingRegister(KeyBindingRegister register, int index) {
        this.ModKeyBindingRegister[this.Pattern][index] = register;
    }

    /**
     * Patternを設定してから
     * ModKeyBindingを更新 KeyBindの[共通Hash]をすべて変更
     */
    @Override
    public void ChangeKeyPatternEvent() {
        for (int i = 0; i < this.ModKeyBindingRegister[this.Pattern].length; i++) {
            KeyBindingRegister register = this.ModKeyBindingRegister[this.Pattern].clone()[i];
            this.ModKeyBinding[this.Pattern][i].setKeyModifierAndCode(register.modifier, register.keycode);
        }
    }

    @Override
    public KeyBinding[] getPatternKeyBindings(int id) {
        return this.ModKeyBinding[id];
    }
}
