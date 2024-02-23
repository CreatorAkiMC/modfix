package com.aki.modfix.mixin.vanilla.misc;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontRenderer.class)
public abstract class MixinClearChatShadow {
    @Shadow public abstract int drawString(String text, float x, float y, int color, boolean dropShadow);

    @Inject(method = "drawStringWithShadow", at = @At("HEAD"), cancellable = true)
    public void drawStringWithShadowFix(String text, float x, float y, int color, CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(this.drawString(text, x, y, color, false));
    }
}
