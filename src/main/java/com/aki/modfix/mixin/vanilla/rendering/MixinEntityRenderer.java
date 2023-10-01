package com.aki.modfix.mixin.vanilla.rendering;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Redirect(method = "setupCameraTransform", require = 0, at = @At(value = "INVOKE", target = "LConfig;isFogFancy()Z", remap = false))
    public boolean isFogFancy(float partialTicks, int pass) {
        return false;
    }

    @Redirect(method = "setupCameraTransform", require = 0, at = @At(value = "INVOKE", target = "LConfig;isFogFast()Z", remap = false))
    public boolean isFogFast(float partialTicks, int pass) {
        return false;
    }
}
