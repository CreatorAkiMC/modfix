package com.aki.modfix.mixin.vanillafix;

import org.dimdev.vanillafix.ModCompatibilityMixinPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(ModCompatibilityMixinPlugin.class)
public class MixinChangeConfig {
    @Inject(method = "shouldApplyMixin", at = @At("RETURN"), cancellable = true, remap = false)
    public void ChangeShouldApplyMixin(String targetClassName, String mixinClassName, CallbackInfoReturnable<Boolean> cir) {
        if(mixinClassName.equals("org.dimdev.vanillafix.textures.mixins.client.MixinTextureMap"))
            cir.setReturnValue(false);
    }
}
