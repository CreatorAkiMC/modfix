package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.mcutils.APICore.Utils.GuiDebugHelper;
import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.Modfix;
import com.aki.modfix.WorldRender.chunk.ChunkRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedList;
import java.util.List;

/**
 * Remap = false にしない
 * */
@Mixin(value = RenderGlobal.class, priority = Modfix.ModPriority)
public abstract class MixinRenderGlobal {
    @Shadow @Final private Minecraft mc;
    @Shadow private int renderDistanceChunks;

    @Shadow public abstract void loadRenderers();

    @Unique
    public int modFix$StringListIndex = -1;

    @Unique
    public List<String> modfix$OldstringList = new LinkedList<>();

    @Inject(method = "getRenderedChunks", cancellable = true, at = @At("HEAD"))
    public void getRenderedChunks(CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(ChunkRenderManager.getChunkRenderer().getRenderedChunks());
    }

    @Inject(method = "stopChunkUpdates", cancellable = true, at = @At("HEAD"))
    public void stopChunkUpdates(CallbackInfo info) {
        info.cancel();
    }

    /**
     * renderEntitiesはChunkRendererが完成してから,
     * --renderInfos だったり、world.getChunk など使っているから。<- 置き換える
     * */

    @Inject(method = "setupTerrain", at = @At("HEAD"), cancellable = true)
    public void SetUP(Entity p_174970_1_, double p_174970_2_, ICamera p_174970_4_, int p_174970_5_, boolean p_174970_6_, CallbackInfo ci) {
        if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks)
        {
            this.loadRenderers();
        }
        GLUtils.updateCamera();
        ChunkRenderManager.SetUPTerrain(p_174970_1_, p_174970_2_, p_174970_4_, p_174970_5_, p_174970_6_);
        ci.cancel();
    }

    @Inject(method = "getDebugInfoRenders", cancellable = true, at = @At("HEAD"))
    public void getDebugInfoRenders(CallbackInfoReturnable<String> info) {
        LinkedList<String> strings = new LinkedList<>();
        strings.add("ModFix ChunkRenderer: " + ChunkRenderManager.getChunkRenderer().getRenderEngine().getGLName());
        strings.add("ModFix ChunkRenderSection Info: ");
        strings.add("  Solid: " + ChunkRenderManager.RenderSections(ChunkRenderPass.SOLID) + ", CutOut: " + ChunkRenderManager.RenderSections(ChunkRenderPass.CUTOUT));
        strings.add("  CutOut_Mipped: " + ChunkRenderManager.RenderSections(ChunkRenderPass.CUTOUT_MIPPED) + ", Translucent: " + ChunkRenderManager.RenderSections(ChunkRenderPass.TRANSLUCENT));
        strings.add("  Total: " + ChunkRenderManager.AllPassRenderSize());
        strings.add("  RenderChunks: " + ChunkRenderManager.totalRenderedSections());
        this.modFix$StringListIndex = GuiDebugHelper.ReplaceDebugStringList(this.modFix$StringListIndex >= 0 ? modfix$OldstringList : strings, strings);
        modfix$OldstringList = new LinkedList<>(strings);
        info.setReturnValue("C: 0/0 (s) D: 0, L: 0, pC: 0, pU: 0, aB: 0");
    }


    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I", at = @At("HEAD"), cancellable = true)
    public void RenderChunkBlockLayer(BlockRenderLayer p_174977_1_, double p_174977_2_, int p_174977_4_, Entity p_174977_5_, CallbackInfoReturnable<Integer> cir) {
        ChunkRenderManager.Render(p_174977_1_, p_174977_2_, p_174977_4_, p_174977_5_);
        cir.setReturnValue(0);
    }

    @Inject(method = "markBlocksForUpdate", cancellable = true, at = @At("HEAD"))
    public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately, CallbackInfo info) {
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkY = minY >> 4; chunkY <= maxY >> 4; chunkY++) {
                for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                    ChunkRenderManager.getRenderProvider().setDirty(chunkX, chunkY, chunkZ);
                }
            }
        }

        info.cancel();
    }

    /**
     * remap = false をつけると Optifine を抜いた時に動かない？
     * */
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
