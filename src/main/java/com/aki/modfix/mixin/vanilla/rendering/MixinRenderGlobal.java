package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.chunk.ChunkRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

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

    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I", at = @At("HEAD"), cancellable = true)
    public void RenderChunkBlockLayer(BlockRenderLayer p_174977_1_, double p_174977_2_, int p_174977_4_, Entity p_174977_5_, CallbackInfoReturnable<Integer> cir) {
        ChunkRenderManager.Render(p_174977_1_, p_174977_2_, p_174977_4_, p_174977_5_);
        cir.cancel();
    }

    @Inject(method = "markBlocksForUpdate", cancellable = true, at = @At("HEAD"))
    public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately, CallbackInfo info) {
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkY = minY >> 4; chunkY <= maxY >> 4; chunkY++) {
                for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                    //ChunkRenderManager.getProvider().setDirty(chunkX, chunkY, chunkZ);
                }
            }
        }

        info.cancel();
    }

    @Inject(method = "getRenderChunkOffset", remap = false, cancellable = true, at = @At("HEAD"))
    public void getRenderChunkOffset(CallbackInfoReturnable<RenderChunk> info) {
        info.setReturnValue(null);
    }

    @Redirect(method = "updateClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;hasNoFreeRenderBuilders()Z"))
    public boolean hasNoFreeRenderBuilders(ChunkRenderDispatcher chunkRenderDispatcher) {
        return false;
    }

    @Inject(method = "updateChunks", cancellable = true, at = @At("HEAD"))
    public void updateChunks(long finishTimeNano, CallbackInfo info) {
        info.cancel();
    }

    @Inject(method = "hasNoChunkUpdates", cancellable = true, at = @At("HEAD"))
    public void hasNoChunkUpdates(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(true);
    }
}
