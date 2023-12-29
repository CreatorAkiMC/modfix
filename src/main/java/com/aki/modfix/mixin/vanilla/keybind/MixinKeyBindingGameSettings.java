package com.aki.modfix.mixin.vanilla.keybind;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.GameSettingsExtended;
import com.aki.modfix.util.fix.KeyBindingRegister;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.client.settings.KeyModifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Mixin(value = GameSettings.class, priority = Modfix.ModPriority)
public abstract class MixinKeyBindingGameSettings implements GameSettingsExtended {

    @Shadow @Final private static Logger LOGGER;

    @Shadow public boolean invertMouse;

    @Shadow public float mouseSensitivity;

    @Shadow public float fovSetting;

    @Shadow public float gammaSetting;

    @Shadow public float saturation;

    @Shadow public int renderDistanceChunks;

    @Shadow public int guiScale;

    @Shadow public int particleSetting;

    @Shadow public boolean viewBobbing;

    @Shadow public boolean anaglyph;

    @Shadow public int limitFramerate;

    @Shadow public boolean fboEnable;

    @Shadow public EnumDifficulty difficulty;

    @Shadow public boolean fancyGraphics;

    @Shadow public int ambientOcclusion;

    @Shadow public int clouds;

    @Shadow @Final private static Gson GSON;

    @Shadow public List<String> resourcePacks;

    @Shadow public List<String> incompatibleResourcePacks;

    @Shadow public String lastServer;

    @Shadow public String language;

    @Shadow public EntityPlayer.EnumChatVisibility chatVisibility;

    @Shadow public boolean chatColours;

    @Shadow public boolean chatLinks;

    @Shadow public boolean chatLinksPrompt;

    @Shadow public float chatOpacity;

    @Shadow public boolean snooperEnabled;

    @Shadow public boolean fullScreen;

    @Shadow public boolean enableVsync;

    @Shadow public boolean useVbo;

    @Shadow public boolean hideServerAddress;

    @Shadow public boolean advancedItemTooltips;

    @Shadow public boolean pauseOnLostFocus;

    @Shadow public boolean touchscreen;

    @Shadow public int overrideWidth;

    @Shadow public int overrideHeight;

    @Shadow public boolean heldItemTooltips;

    @Shadow public float chatHeightFocused;

    @Shadow public float chatHeightUnfocused;

    @Shadow public float chatScale;

    @Shadow public float chatWidth;

    @Shadow public int mipmapLevels;

    @Shadow public boolean forceUnicodeFont;

    @Shadow public boolean reducedDebugInfo;

    @Shadow public boolean useNativeTransport;

    @Shadow public boolean entityShadows;

    @Shadow public EnumHandSide mainHand;

    @Shadow public int attackIndicator;

    @Shadow public boolean showSubtitles;

    @Shadow public boolean realmsNotifications;

    @Shadow public boolean enableWeakAttacks;

    @Shadow public boolean autoJump;

    @Shadow public int narrator;

    @Shadow public TutorialSteps tutorialStep;

    @Shadow public abstract float getSoundLevel(SoundCategory category);

    @Shadow @Final private Set<EnumPlayerModelParts> setModelParts;

    @Shadow public abstract void sendSettingsToServer();

    @Shadow private File optionsFile;

    @Shadow @Final private Map<SoundCategory, Float> soundLevels;

    @Shadow @Final public static Splitter COLON_SPLITTER;

    @Shadow protected abstract NBTTagCompound dataFix(NBTTagCompound p_189988_1_);

    @Shadow protected abstract float parseFloat(String str);

    @Shadow @Final private static Type TYPE_LIST_STRING;

    @Shadow public abstract void setModelPartEnabled(EnumPlayerModelParts modelPart, boolean enable);


    @Shadow public KeyBinding[] keyBindings;
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
        ModsKeySettingFile = new File(mcDataDir, "ModsKeySettings.txt");
        this.VanillaKeyBinding = this.keyBindings;
    }

    @Inject(method = "<init>()V", at = @At("RETURN"))
    public void ChangeToCustomKeys(CallbackInfo ci) {
        this.VanillaKeyBinding = this.keyBindings;
    }

    /**
     * @author Aki
     * @reason Replace KeyBinding System
     */
    @Overwrite
    public void loadOptions()
    {
        FileInputStream fileInputStream = null; // Forge: fix MC-151173
        try
        {
            if (!this.optionsFile.exists())
            {
                return;
            }

            this.soundLevels.clear();
            List<String> list = IOUtils.readLines(fileInputStream = new FileInputStream(this.optionsFile), StandardCharsets.UTF_8); // Forge: fix MC-117449, MC-151173
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            for (String s : list)
            {
                try
                {
                    Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(s).iterator();
                    nbttagcompound.setString(iterator.next(), iterator.next());
                }
                catch (Exception var10)
                {
                    LOGGER.warn("Skipping bad option: {}", (Object)s);
                }
            }

            nbttagcompound = this.dataFix(nbttagcompound);

            for (String s1 : nbttagcompound.getKeySet())
            {
                String s2 = nbttagcompound.getString(s1);

                try
                {
                    if ("mouseSensitivity".equals(s1))
                    {
                        this.mouseSensitivity = this.parseFloat(s2);
                    }

                    if ("fov".equals(s1))
                    {
                        this.fovSetting = this.parseFloat(s2) * 40.0F + 70.0F;
                    }

                    if ("gamma".equals(s1))
                    {
                        this.gammaSetting = this.parseFloat(s2);
                    }

                    if ("saturation".equals(s1))
                    {
                        this.saturation = this.parseFloat(s2);
                    }

                    if ("invertYMouse".equals(s1))
                    {
                        this.invertMouse = "true".equals(s2);
                    }

                    if ("renderDistance".equals(s1))
                    {
                        this.renderDistanceChunks = Integer.parseInt(s2);
                    }

                    if ("guiScale".equals(s1))
                    {
                        this.guiScale = Integer.parseInt(s2);
                    }

                    if ("particles".equals(s1))
                    {
                        this.particleSetting = Integer.parseInt(s2);
                    }

                    if ("bobView".equals(s1))
                    {
                        this.viewBobbing = "true".equals(s2);
                    }

                    if ("anaglyph3d".equals(s1))
                    {
                        this.anaglyph = "true".equals(s2);
                    }

                    if ("maxFps".equals(s1))
                    {
                        this.limitFramerate = Integer.parseInt(s2);
                    }

                    if ("fboEnable".equals(s1))
                    {
                        this.fboEnable = "true".equals(s2);
                    }

                    if ("difficulty".equals(s1))
                    {
                        this.difficulty = EnumDifficulty.byId(Integer.parseInt(s2));
                    }

                    if ("fancyGraphics".equals(s1))
                    {
                        this.fancyGraphics = "true".equals(s2);
                    }

                    if ("tutorialStep".equals(s1))
                    {
                        this.tutorialStep = TutorialSteps.getTutorial(s2);
                    }

                    if ("ao".equals(s1))
                    {
                        if ("true".equals(s2))
                        {
                            this.ambientOcclusion = 2;
                        }
                        else if ("false".equals(s2))
                        {
                            this.ambientOcclusion = 0;
                        }
                        else
                        {
                            this.ambientOcclusion = Integer.parseInt(s2);
                        }
                    }

                    if ("renderClouds".equals(s1))
                    {
                        if ("true".equals(s2))
                        {
                            this.clouds = 2;
                        }
                        else if ("false".equals(s2))
                        {
                            this.clouds = 0;
                        }
                        else if ("fast".equals(s2))
                        {
                            this.clouds = 1;
                        }
                    }

                    if ("attackIndicator".equals(s1))
                    {
                        if ("0".equals(s2))
                        {
                            this.attackIndicator = 0;
                        }
                        else if ("1".equals(s2))
                        {
                            this.attackIndicator = 1;
                        }
                        else if ("2".equals(s2))
                        {
                            this.attackIndicator = 2;
                        }
                    }

                    if ("resourcePacks".equals(s1))
                    {
                        this.resourcePacks = (List) JsonUtils.gsonDeserialize(GSON, s2, TYPE_LIST_STRING);

                        if (this.resourcePacks == null)
                        {
                            this.resourcePacks = Lists.<String>newArrayList();
                        }
                    }

                    if ("incompatibleResourcePacks".equals(s1))
                    {
                        this.incompatibleResourcePacks = (List)JsonUtils.gsonDeserialize(GSON, s2, TYPE_LIST_STRING);

                        if (this.incompatibleResourcePacks == null)
                        {
                            this.incompatibleResourcePacks = Lists.<String>newArrayList();
                        }
                    }

                    if ("lastServer".equals(s1))
                    {
                        this.lastServer = s2;
                    }

                    if ("lang".equals(s1))
                    {
                        this.language = s2;
                    }

                    if ("chatVisibility".equals(s1))
                    {
                        this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(s2));
                    }

                    if ("chatColors".equals(s1))
                    {
                        this.chatColours = "true".equals(s2);
                    }

                    if ("chatLinks".equals(s1))
                    {
                        this.chatLinks = "true".equals(s2);
                    }

                    if ("chatLinksPrompt".equals(s1))
                    {
                        this.chatLinksPrompt = "true".equals(s2);
                    }

                    if ("chatOpacity".equals(s1))
                    {
                        this.chatOpacity = this.parseFloat(s2);
                    }

                    if ("snooperEnabled".equals(s1))
                    {
                        this.snooperEnabled = "true".equals(s2);
                    }

                    if ("fullscreen".equals(s1))
                    {
                        this.fullScreen = "true".equals(s2);
                    }

                    if ("enableVsync".equals(s1))
                    {
                        this.enableVsync = "true".equals(s2);
                    }

                    if ("useVbo".equals(s1))
                    {
                        this.useVbo = "true".equals(s2);
                    }

                    if ("hideServerAddress".equals(s1))
                    {
                        this.hideServerAddress = "true".equals(s2);
                    }

                    if ("advancedItemTooltips".equals(s1))
                    {
                        this.advancedItemTooltips = "true".equals(s2);
                    }

                    if ("pauseOnLostFocus".equals(s1))
                    {
                        this.pauseOnLostFocus = "true".equals(s2);
                    }

                    if ("touchscreen".equals(s1))
                    {
                        this.touchscreen = "true".equals(s2);
                    }

                    if ("overrideHeight".equals(s1))
                    {
                        this.overrideHeight = Integer.parseInt(s2);
                    }

                    if ("overrideWidth".equals(s1))
                    {
                        this.overrideWidth = Integer.parseInt(s2);
                    }

                    if ("heldItemTooltips".equals(s1))
                    {
                        this.heldItemTooltips = "true".equals(s2);
                    }

                    if ("chatHeightFocused".equals(s1))
                    {
                        this.chatHeightFocused = this.parseFloat(s2);
                    }

                    if ("chatHeightUnfocused".equals(s1))
                    {
                        this.chatHeightUnfocused = this.parseFloat(s2);
                    }

                    if ("chatScale".equals(s1))
                    {
                        this.chatScale = this.parseFloat(s2);
                    }

                    if ("chatWidth".equals(s1))
                    {
                        this.chatWidth = this.parseFloat(s2);
                    }

                    if ("mipmapLevels".equals(s1))
                    {
                        this.mipmapLevels = Integer.parseInt(s2);
                    }

                    if ("forceUnicodeFont".equals(s1))
                    {
                        this.forceUnicodeFont = "true".equals(s2);
                    }

                    if ("reducedDebugInfo".equals(s1))
                    {
                        this.reducedDebugInfo = "true".equals(s2);
                    }

                    if ("useNativeTransport".equals(s1))
                    {
                        this.useNativeTransport = "true".equals(s2);
                    }

                    if ("entityShadows".equals(s1))
                    {
                        this.entityShadows = "true".equals(s2);
                    }

                    if ("mainHand".equals(s1))
                    {
                        this.mainHand = "left".equals(s2) ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
                    }

                    if ("showSubtitles".equals(s1))
                    {
                        this.showSubtitles = "true".equals(s2);
                    }

                    if ("realmsNotifications".equals(s1))
                    {
                        this.realmsNotifications = "true".equals(s2);
                    }

                    if ("enableWeakAttacks".equals(s1))
                    {
                        this.enableWeakAttacks = "true".equals(s2);
                    }

                    if ("autoJump".equals(s1))
                    {
                        this.autoJump = "true".equals(s2);
                    }

                    if ("narrator".equals(s1))
                    {
                        this.narrator = Integer.parseInt(s2);
                    }

                    for (KeyBinding keybinding : this.VanillaKeyBinding) {
                        if (s1.equals("key_" + keybinding.getKeyDescription())) {
                            if (s2.indexOf(':') != -1) {
                                String[] t = s2.split(":");
                                keybinding.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.valueFromString(t[1]), Integer.parseInt(t[0]));
                            } else
                                keybinding.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.NONE, Integer.parseInt(s2));
                        }
                    }

                    //なかった時にデフォルトのものを適用。
                    Path path = Paths.get(this.ModsKeySettingFile.getPath());
                    if (!Files.exists(path) || (Files.exists(path) && Files.size(path) < 1)) {
                        for(int i = 0; i < this.ModKeyBinding[0].length; i++) {//念のためコピー
                            KeyBinding ModKey = this.ModKeyBinding[0][i];
                            if (s1.equals("key_" + ModKey.getKeyDescription())) {
                                if (s2.indexOf(':') != -1) {
                                    String[] t = s2.split(":");
                                    ModKey.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.valueFromString(t[1]), Integer.parseInt(t[0]));
                                    this.ModKeyBindingRegister[0][i] = new KeyBindingRegister(ModKey.getKeyDescription(), ModKey.getKeyCode(), ModKey.getKeyCategory(), net.minecraftforge.client.settings.KeyModifier.valueFromString(t[1]));
                                } else {
                                    ModKey.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.NONE, Integer.parseInt(s2));
                                    this.ModKeyBindingRegister[0][i] = new KeyBindingRegister(ModKey.getKeyDescription(), ModKey.getKeyCode(), ModKey.getKeyCategory(), KeyModifier.NONE);
                                }
                            }
                        }

                        for(int i = 1; i < 9; i++) {
                            this.ModKeyBinding[i] = this.ModKeyBinding[0];//別の配列に同じものをコピー
                            this.ModKeyBindingRegister[i] = this.ModKeyBindingRegister[0];
                        }
                    }

                    for (SoundCategory soundcategory : SoundCategory.values())
                    {
                        if (s1.equals("soundCategory_" + soundcategory.getName()))
                        {
                            this.soundLevels.put(soundcategory, Float.valueOf(this.parseFloat(s2)));
                        }
                    }

                    for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
                    {
                        if (s1.equals("modelPart_" + enumplayermodelparts.getPartName()))
                        {
                            this.setModelPartEnabled(enumplayermodelparts, "true".equals(s2));
                        }
                    }
                }
                catch (Exception var11)
                {
                    LOGGER.warn("Skipping bad option: {}:{}", s1, s2);
                }
            }

            KeyBinding.resetKeyBindingArrayAndHash();



            /**
             *
             * 保存した設定の読み込み
             *
             * */

            Path path = Paths.get(this.ModsKeySettingFile.getPath());
            try {

                //ファイルがある場合に読み込む
                if (Files.exists(path) || !(Files.exists(path) && Files.size(path) < 1)) {
                    list = IOUtils.readLines(fileInputStream = new FileInputStream(this.ModsKeySettingFile), StandardCharsets.UTF_8); // Forge: fix MC-117449, MC-151173
                    nbttagcompound = new NBTTagCompound();

                    for (String s : list)
                    {
                        try
                        {
                            Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(s).iterator();
                            nbttagcompound.setString(iterator.next(), iterator.next());
                        }
                        catch (Exception var10)
                        {
                            LOGGER.warn("Skipping bad option: {}", (Object)s);
                        }
                    }

                    nbttagcompound = this.dataFix(nbttagcompound);

                    Set<String> stringSet = nbttagcompound.getKeySet();

                    String old = "";

                    for (int c = 0; c < stringSet.size(); c++) {
                        String s1 = stringSet.iterator().next();
                        String s2 = nbttagcompound.getString(s1);

                        //"[idx : 1 ]"

                        if (old.equals("[idx : ")) {
                            int idx = Integer.parseInt(old.toLowerCase(Locale.ROOT).replace("[idx : ", "").replace(" ]", "")) - 1;
                            KeyBinding[] keyBindings = this.ModKeyBinding[idx];
                            KeyBindingRegister[] registers = this.ModKeyBindingRegister[idx];

                            for(int i = 0; i < keyBindings.length; i++) {
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
                            }

                            this.ModKeyBinding[idx] = keyBindings;
                            this.ModKeyBindingRegister[idx] = registers;
                        }

                        old = s1;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to File Check KeyBinds by (ModFix mod) ", (Throwable)e);
            }

        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to load options", (Throwable)exception);
        }
        finally { IOUtils.closeQuietly(fileInputStream); } // Forge: fix MC-151173
    }

    @Override
    public void ReloadKeyBindingSettings() {
        /**
         *
         * 保存した設定の読み込み
         *
         * */

        Path path = Paths.get(this.ModsKeySettingFile.getPath());
        try {
            //ファイルがある場合に読み込む
            if (Files.exists(path) || !(Files.exists(path) && Files.size(path) < 1)) {
                List<String> list = IOUtils.readLines(Files.newInputStream(this.ModsKeySettingFile.toPath()), StandardCharsets.UTF_8); // Forge: fix MC-117449, MC-151173
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                for (String s : list)
                {
                    try
                    {
                        Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(s).iterator();
                        nbttagcompound.setString(iterator.next(), iterator.next());
                    }
                    catch (Exception ignored)
                    {

                    }
                }

                nbttagcompound = this.dataFix(nbttagcompound);

                Set<String> stringSet = nbttagcompound.getKeySet();

                String old = "";

                for (int c = 0; c < stringSet.size(); c++) {
                    String s1 = stringSet.iterator().next();
                    String s2 = nbttagcompound.getString(s1);

                    //"[idx : 1 ]"

                    if (old.equals("[idx : ")) {
                        int idx = Integer.parseInt(old.toLowerCase(Locale.ROOT).replace("[idx : ", "").replace(" ]", "")) - 1;
                        KeyBinding[] keyBindings = this.ModKeyBinding[idx];
                        KeyBindingRegister[] registers = this.ModKeyBindingRegister[idx];

                        for(int i = 0; i < keyBindings.length; i++) {
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
                        }

                        this.ModKeyBinding[idx] = keyBindings;
                        this.ModKeyBindingRegister[idx] = registers;
                    }

                    old = s1;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to File Check KeyBinds by (ModFix mod) ", (Throwable)e);
        }
    }

    /**
     * @author Aki
     * @reason Replace KeyBinding System
     */
    @Overwrite
    public void saveOptions()
    {
        if (net.minecraftforge.fml.client.FMLClientHandler.instance().isLoading()) return;
        PrintWriter printwriter = null;

        try
        {
            printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));
            printwriter.println("version:1343");
            printwriter.println("invertYMouse:" + this.invertMouse);
            printwriter.println("mouseSensitivity:" + this.mouseSensitivity);
            printwriter.println("fov:" + (this.fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + this.gammaSetting);
            printwriter.println("saturation:" + this.saturation);
            printwriter.println("renderDistance:" + this.renderDistanceChunks);
            printwriter.println("guiScale:" + this.guiScale);
            printwriter.println("particles:" + this.particleSetting);
            printwriter.println("bobView:" + this.viewBobbing);
            printwriter.println("anaglyph3d:" + this.anaglyph);
            printwriter.println("maxFps:" + this.limitFramerate);
            printwriter.println("fboEnable:" + this.fboEnable);
            printwriter.println("difficulty:" + this.difficulty.getId());
            printwriter.println("fancyGraphics:" + this.fancyGraphics);
            printwriter.println("ao:" + this.ambientOcclusion);

            switch (this.clouds)
            {
                case 0:
                    printwriter.println("renderClouds:false");
                    break;
                case 1:
                    printwriter.println("renderClouds:fast");
                    break;
                case 2:
                    printwriter.println("renderClouds:true");
            }

            printwriter.println("resourcePacks:" + GSON.toJson(this.resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + GSON.toJson(this.incompatibleResourcePacks));
            printwriter.println("lastServer:" + this.lastServer);
            printwriter.println("lang:" + this.language);
            printwriter.println("chatVisibility:" + this.chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + this.chatColours);
            printwriter.println("chatLinks:" + this.chatLinks);
            printwriter.println("chatLinksPrompt:" + this.chatLinksPrompt);
            printwriter.println("chatOpacity:" + this.chatOpacity);
            printwriter.println("snooperEnabled:" + this.snooperEnabled);
            printwriter.println("fullscreen:" + this.fullScreen);
            printwriter.println("enableVsync:" + this.enableVsync);
            printwriter.println("useVbo:" + this.useVbo);
            printwriter.println("hideServerAddress:" + this.hideServerAddress);
            printwriter.println("advancedItemTooltips:" + this.advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            printwriter.println("touchscreen:" + this.touchscreen);
            printwriter.println("overrideWidth:" + this.overrideWidth);
            printwriter.println("overrideHeight:" + this.overrideHeight);
            printwriter.println("heldItemTooltips:" + this.heldItemTooltips);
            printwriter.println("chatHeightFocused:" + this.chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            printwriter.println("chatScale:" + this.chatScale);
            printwriter.println("chatWidth:" + this.chatWidth);
            printwriter.println("mipmapLevels:" + this.mipmapLevels);
            printwriter.println("forceUnicodeFont:" + this.forceUnicodeFont);
            printwriter.println("reducedDebugInfo:" + this.reducedDebugInfo);
            printwriter.println("useNativeTransport:" + this.useNativeTransport);
            printwriter.println("entityShadows:" + this.entityShadows);
            printwriter.println("mainHand:" + (this.mainHand == EnumHandSide.LEFT ? "left" : "right"));
            printwriter.println("attackIndicator:" + this.attackIndicator);
            printwriter.println("showSubtitles:" + this.showSubtitles);
            printwriter.println("realmsNotifications:" + this.realmsNotifications);
            printwriter.println("enableWeakAttacks:" + this.enableWeakAttacks);
            printwriter.println("autoJump:" + this.autoJump);
            printwriter.println("narrator:" + this.narrator);
            printwriter.println("tutorialStep:" + this.tutorialStep.getName());

            for (KeyBinding keybinding : this.VanillaKeyBinding)
            {
                String keyString = "key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode();
                printwriter.println(keybinding.getKeyModifier() != net.minecraftforge.client.settings.KeyModifier.NONE ? keyString + ":" + keybinding.getKeyModifier() : keyString);
            }

            //Modが消えてもいいように元はそのまま
            for (int i = 0; i < ModKeyBinding[0].length; i++) {
                KeyBinding keybinding = ModKeyBinding[0][i];
                String keyString = "key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode();
                printwriter.println(keybinding.getKeyModifier() != net.minecraftforge.client.settings.KeyModifier.NONE ? keyString + ":" + keybinding.getKeyModifier() : keyString);
            }

            for (SoundCategory soundcategory : SoundCategory.values())
            {
                printwriter.println("soundCategory_" + soundcategory.getName() + ":" + this.getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
            {
                printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + this.setModelParts.contains(enumplayermodelparts));
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to save options", (Throwable)exception);
        }
        finally
        {
            IOUtils.closeQuietly((Writer)printwriter);
        }

        //Mod KeyBinding 用のファイルに保存、 Modを抜いても初期化されないように
        //元ファイルには変更を加えない
        try {
            printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.ModsKeySettingFile), StandardCharsets.UTF_8));
            printwriter.println("Create by ModFix mod (Creator Aki)");
            for(int i = 0; i < 9; i++) {//[1] ~ [9] (0 ~ 8)
                printwriter.println("[idx : " + (i + 1) + " ]");
                for(int i2 = 0; i2 < ModKeyBinding[i].length; i2++) {
                    KeyBinding keybinding = ModKeyBinding[i][i2];
                    KeyBindingRegister register = ModKeyBindingRegister[i][i2];

                    String keyString = "key_" + keybinding.getKeyDescription() + ":" + register.keycode;
                    printwriter.println(register.modifier != net.minecraftforge.client.settings.KeyModifier.NONE ? keyString + ":" + register.modifier : keyString);
                }
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to save mod key options", (Throwable)exception);
        }
        finally
        {
            IOUtils.closeQuietly((Writer)printwriter);
        }

        this.sendSettingsToServer();
    }

    @Override
    public KeyBinding[] MCKeyBinding() {
        return VanillaKeyBinding;
    }

    @Override
    public KeyBinding[] ModRegisteredBinding() {
        return ModKeyBinding[Pattern];
    }

    @Override
    public KeyBindingRegister[] KeyBindingRegister() {
        return this.ModKeyBindingRegister[Pattern];
    }

    @Override
    public void SetModRegisteredBinding(KeyBinding[] ModCustomKeyBinding) {
        this.ModKeyBinding[Pattern] = ModCustomKeyBinding;
        //NullPointerError 回避
        this.ModKeyBindingRegister[Pattern] = new KeyBindingRegister[ModCustomKeyBinding.length];

        for (int i = 0; i < ModCustomKeyBinding.length; i++) {
            this.ModKeyBindingRegister[Pattern][i] = new KeyBindingRegister(ModCustomKeyBinding[i].getKeyDescription(), ModCustomKeyBinding[i].getKeyCode(), ModCustomKeyBinding[i].getKeyCategory(), ModCustomKeyBinding[i].getKeyModifier());
        }

        Path path = Paths.get(this.ModsKeySettingFile.getPath());
        try {
            //ファイルがない場合やファイルがあっても中身がない場合などの処理
            if (Pattern == 0 || (Files.exists(path) && Files.size(path) < 1)) {
                //すべてのパターンにチェック
                for(int i1 = 1; i1 < 9; i1++) {
                    this.ModKeyBinding[i1] = ModCustomKeyBinding;
                    this.ModKeyBindingRegister[i1] = new KeyBindingRegister[ModCustomKeyBinding.length];
                    for (int i = 0; i < ModCustomKeyBinding.length; i++) {
                        this.ModKeyBindingRegister[i1][i] = new KeyBindingRegister(ModCustomKeyBinding[i].getKeyDescription(), ModCustomKeyBinding[i].getKeyCode(), ModCustomKeyBinding[i].getKeyCategory(), ModCustomKeyBinding[i].getKeyModifier());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to File Check KeyBinds by (ModFix mod) ", (Throwable)e);
        }
    }

    @Override
    public int getPatternID() {
        return Pattern;
    }

    // Set range 0 ~ 8
    @Override
    public void setPatternID(int Id) {
        Pattern = Id;
    }

    @Override
    public void SetKeyBindingRegister(KeyBindingRegister register, int index) {
        this.ModKeyBindingRegister[Pattern][index] = register;
    }

    /**
     * Patternを設定してから
     * ModKeyBindingを更新 KeyBindの[共通Hash]をすべて変更
     * */
    @Override
    public void ChangeKeyPatternEvent() {
        for(int i = 0; i < this.ModKeyBindingRegister[this.Pattern].length; i++) {
            KeyBindingRegister register = this.ModKeyBindingRegister[this.Pattern].clone()[i];
            this.ModKeyBinding[this.Pattern][i].setKeyModifierAndCode(register.modifier, register.keycode);
        }
    }
}
