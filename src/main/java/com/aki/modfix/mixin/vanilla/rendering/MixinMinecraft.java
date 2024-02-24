package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.Modfix;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = Modfix.ModPriority)
public abstract class MixinMinecraft {
    @Shadow
    public abstract boolean isGamePaused();

    @Shadow
    private boolean isGamePaused;

    @Shadow
    private float renderPartialTicksPaused;

    @Shadow
    @Final
    private Timer timer;

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=gameRenderer"))
    public void runGameLoop(CallbackInfo info) {
        GLUtils.update(isGamePaused ? renderPartialTicksPaused : timer.renderPartialTicks);
    }
}
