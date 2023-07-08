package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.chunk.ChunkRenderManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remap = false にしない
 * */
@Mixin(value = RenderGlobal.class)
public abstract class MixinRenderGlobal {
    @Inject(method = "stopChunkUpdates", cancellable = true, at = @At("HEAD"))
    public void stopChunkUpdates(CallbackInfo info) {
        info.cancel();
    }

    /*@Inject(method = "getRenderedChunks", cancellable = true, at = @At("HEAD"))
    public void getRenderedChunks(CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(ChunkRenderManager.renderedSections());
    }*/

    @Inject(method = "setupTerrain", at = @At("HEAD"), cancellable = true)
    public void SetUP(Entity p_174970_1_, double p_174970_2_, ICamera p_174970_4_, int p_174970_5_, boolean p_174970_6_, CallbackInfo ci) {
        GLUtils.updateCamera();
        ChunkRenderManager.SetUPTerrain(p_174970_1_, p_174970_2_, p_174970_4_, p_174970_5_, p_174970_6_);
        ci.cancel();
    }

    /*@Unique
    public long Time = 0L;

    @Inject(method = "setupTerrain", at = @At("HEAD"))
    public void CheckTimeStart(Entity p_174970_1_, double p_174970_2_, ICamera p_174970_4_, int p_174970_5_, boolean p_174970_6_, CallbackInfo ci) {
        Time = System.currentTimeMillis();
    }

    @Inject(method = "setupTerrain", at = @At("RETURN"))
    public void CheckTimeEnd(Entity p_174970_1_, double p_174970_2_, ICamera p_174970_4_, int p_174970_5_, boolean p_174970_6_, CallbackInfo ci) {
        System.out.println("RenderAllTime: " + (System.currentTimeMillis() - Time));
    }*/
}
