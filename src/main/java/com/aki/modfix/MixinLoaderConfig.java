package com.aki.modfix;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MixinLoaderConfig implements IMixinConfigPlugin {
    /*public Map<String, Boolean> SettingMixins = new ManyObjectsMap<String, Boolean>().addE(
            "mixins.projecte.json", Loader.isModLoaded("projecte")
    );*/

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "modfix.refmap.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        System.out.println("S-ModFix TargetClass: " + targetClassName);
        System.out.println("------ MixinClassName: " + mixinClassName);

        if (mixinClassName.equals("com.aki.modfix.mixin.vanillafix.MixinBlockModelRenderer")) {
            return !classExists("optifine.OptiFineForgeTweaker");
        }

        if (mixinClassName.equals("com.aki.modfix.mixin.vanillafix.MixinBlockModelRendererOptifine")) {
            return classExists("optifine.OptiFineForgeTweaker");
        }

        return true;//SettingMixins.getOrDefault(mixinClassName, true);
    }

    public boolean classExists(String name) {
        try {
            return Launch.classLoader.getClassBytes(name) != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        System.out.println("Pre-ModFix TargetClass: " + targetClassName);
        System.out.println("------ MixinClassName: " + mixinClassName);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        System.out.println("Post-ModFix TargetClass: " + targetClassName);
        System.out.println("------ MixinClassName: " + mixinClassName);
    }
}
