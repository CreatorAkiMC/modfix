package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.chunk.ChunkRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.client.renderer.culling.ICamera;
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
import java.util.Set;

/**
 * Remap = false にしない
 * */
@Mixin(value = RenderGlobal.class)
public abstract class MixinRenderGlobal {
    @Shadow private WorldClient world;

    @Shadow private ChunkRenderDispatcher renderDispatcher;

    @Shadow private boolean displayListEntitiesDirty;

    @Shadow @Final private Minecraft mc;

    @Shadow private int renderDistanceChunks;

    @Shadow private boolean vboEnabled;

    @Shadow private IRenderChunkFactory renderChunkFactory;

    @Shadow private ChunkRenderContainer renderContainer;

    @Shadow protected abstract void generateStars();

    @Shadow protected abstract void generateSky2();

    @Shadow protected abstract void generateSky();

    @Shadow private ViewFrustum viewFrustum;

    @Shadow @Final private Set<TileEntity> setTileEntities;

    @Shadow protected abstract void stopChunkUpdates();

    @Shadow private int renderEntitiesStartupCounter;

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

    @Inject(method = "loadRenderers", at = @At("HEAD"), cancellable = true)
    public void loadRenderers(CallbackInfo ci)
    {
        if (this.world != null)
        {
            if (this.renderDispatcher == null)
            {
                this.renderDispatcher = new ChunkRenderDispatcher();
            }

            ChunkRenderManager.loadRenderers();

            this.displayListEntitiesDirty = true;
            Blocks.LEAVES.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
            Blocks.LEAVES2.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
            this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
            boolean flag = this.vboEnabled;
            this.vboEnabled = OpenGlHelper.useVbo();

            if (flag && !this.vboEnabled)
            {
                this.renderContainer = new RenderList();
                this.renderChunkFactory = new ListChunkFactory();
            }
            else if (!flag && this.vboEnabled)
            {
                this.renderContainer = new VboRenderList();
                this.renderChunkFactory = new VboChunkFactory();
            }

            if (flag != this.vboEnabled)
            {
                this.generateStars();
                this.generateSky();
                this.generateSky2();
            }

            if (this.viewFrustum != null)
            {
                this.viewFrustum.deleteGlResources();
            }

            this.stopChunkUpdates();

            synchronized (this.setTileEntities)
            {
                this.setTileEntities.clear();
            }

            this.viewFrustum = new ViewFrustum(this.world, this.mc.gameSettings.renderDistanceChunks, (RenderGlobal)(Object) this, this.renderChunkFactory);

            if (this.world != null)
            {
                Entity entity = this.mc.getRenderViewEntity();

                if (entity != null)
                {
                    this.viewFrustum.updateChunkPositions(entity.posX, entity.posZ);
                }
            }

            this.renderEntitiesStartupCounter = 2;
        }

        //上書き
        ci.cancel();
    }

    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I", at = @At("HEAD"), cancellable = true)
    public void loadRenderers(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable<Integer> cir)
    {
        ChunkRenderManager.Render(blockLayerIn, partialTicks, pass, entityIn);
        cir.cancel();
    }

    @Inject(method = "getRenderChunkOffset", remap = false, cancellable = true, at = @At("HEAD"))
    public void getRenderChunkOffset(CallbackInfoReturnable<RenderChunk> info) {
        info.setReturnValue(null);
    }

    @Redirect(method = "updateClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;hasNoFreeRenderBuilders()Z"))
    public boolean hasNoFreeRenderBuilders(ChunkRenderDispatcher chunkRenderDispatcher) {
        return false;
    }

    /** {@link RenderGlobal#updateChunks(long)} */
    @Inject(method = "updateChunks", cancellable = true, at = @At("HEAD"))
    public void updateChunks(long finishTimeNano, CallbackInfo info) {
        info.cancel();
    }

    @Inject(method = "hasNoChunkUpdates", cancellable = true, at = @At("HEAD"))
    public void hasNoChunkUpdates(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(true);
    }


    /**
     * renderDispatcher はtransformerのほうがいいかも
     * */
    /*public void setWorldAndLoadRenderers(@Nullable WorldClient worldClientIn)
    {
        if (this.world != null)
        {
            this.world.removeEventListener(this);
        }

        this.frustumUpdatePosX = Double.MIN_VALUE;
        this.frustumUpdatePosY = Double.MIN_VALUE;
        this.frustumUpdatePosZ = Double.MIN_VALUE;
        this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        this.renderManager.setWorld(worldClientIn);
        this.world = worldClientIn;

        if (worldClientIn != null)
        {
            worldClientIn.addEventListener(this);
            this.loadRenderers();
        }
        else
        {
            this.chunksToUpdate.clear();
            this.renderInfos.clear();

            if (this.viewFrustum != null)
            {
                this.viewFrustum.deleteGlResources();
                this.viewFrustum = null;
            }

            if (this.renderDispatcher != null)
            {
                this.renderDispatcher.stopWorkerThreads();
            }

            this.renderDispatcher = null;
        }
    }*/

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
