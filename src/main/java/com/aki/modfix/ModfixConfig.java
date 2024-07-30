package com.aki.modfix;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModfixConfig {
    public static Configuration cfg;

    public static String category = "mod";

    public static int DefaultUseGLIndex = 0;
    public static boolean UseElementBuffer = true;

    public static void PreInit(FMLPreInitializationEvent event) {
        cfg = new Configuration(event.getSuggestedConfigurationFile());

        SyncConfig();
    }

    public static void SettingConfig() {
        if(cfg != null) {
            cfg.load();
            cfg.addCustomCategoryComment(category, "Mod Settings");
            cfg.setCategoryRequiresMcRestart(category, false);
        }
    }

    public static void SyncConfig() {
        SettingConfig();
        DefaultUseGLIndex = cfg.getInt("DefaultUseRenderingEngine", category, 3, 0, 3, "0:[GL15], 1:[GL20], 2:[GL42], 3:[GL43]");
        UseElementBuffer = cfg.getBoolean("UseElementBuffer", category, true, "ElementBuffer may speed things up a bit...");
        cfg.save();
    }
}
