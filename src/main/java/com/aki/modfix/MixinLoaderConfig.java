package com.aki.modfix;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
        return true;//SettingMixins.getOrDefault(mixinClassName, true);
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
