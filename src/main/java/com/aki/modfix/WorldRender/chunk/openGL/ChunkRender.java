package com.aki.modfix.WorldRender.chunk.openGL;

import com.aki.mcutils.APICore.Utils.memory.UnsafeByteBuffer;
import com.aki.mcutils.APICore.Utils.render.*;
import com.aki.modfix.GLSytem.GlDynamicVBO;
import com.aki.modfix.Modfix;
import com.aki.modfix.WorldRender.chunk.openGL.integreate.CubicChunks;
import com.aki.modfix.WorldRender.chunk.openGL.renderers.ChunkRendererBase;
import com.aki.modfix.util.gl.WorldUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * BufferBuilder など
 * 透過タスクの実行も
 */
public class ChunkRender {
    //private RegionRenderCacheBuilder VBOs = null;
    private final LinkedHashMap<ChunkRenderPass, GlDynamicVBO.VBOPart> VBOs = MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i -> null);
    private boolean dirty;

    private final ChunkRender[] neighbors = new ChunkRender[EnumFacing.values().length];

    public int lastEnqueuedTime = -1;
    public int lastRecordedTime = -1;

    private VisibilitySet Visibilityset = new VisibilitySet();
    public int VisibleDirections;

    private ChunkRenderTaskBase<ChunkRender> LastChunkRenderCompileTask;
    private CompletableFuture<ChunkRenderTaskResult> lastChunkRenderCompileTaskResult;

    private UnsafeByteBuffer translucentVertexData;

    private SectionPos pos;

    private int EmptyCount = 0;//空の場合は加算

    private int ID = -1;

    public ChunkRender(int X, int Y, int Z) {
        this.pos = SectionPos.of(X, Y, Z);
        this.markDirty();
    }

    public boolean setCoord(int SectionX, int SectionY, int SectionZ) {
        if (pos.getX() != SectionX || pos.getY() != SectionY || pos.getZ() != SectionZ) {
            this.pos = SectionPos.of(SectionX, SectionY, SectionZ);
            this.Delete();
            this.markDirty();
            /*if (Modfix.isChunkAnimatorInstalled) {
                ChunkAnimator.onSetCoords(this);
            }*/
            return true;
        }

        return false;
    }

    public ChunkRender setID(int id) {
        this.ID = id;
        return this;
    }

    public int getID() {
        return this.ID;
    }

    //BlockX
    public int getX() {
        return this.pos.getBlockX();
    }

    //BlockY
    public int getY() {
        return this.pos.getBlockY();
    }

    //BlockZ
    public int getZ() {
        return this.pos.getBlockZ();
    }

    public int getSectionX() {
        return this.pos.getX();
    }

    public int getSectionY() {
        return this.pos.getY();
    }

    public int getSectionZ() {
        return this.pos.getZ();
    }

    public VisibilitySet getVisibility() {
        return Visibilityset;
    }

    public void setVisibility(VisibilitySet visibilitySet) {
        this.Visibilityset = visibilitySet;
    }

    public boolean isVisibleFromAnyOrigin(EnumFacing direction) {
        return (VisibleDirections & (1 << direction.ordinal())) != 0;
    }

    public void setOrigin(EnumFacing origin) {
        VisibleDirections |= getVisibility().allVisibleFrom(origin);
    }

    public void resetOrigins() {
        this.VisibleDirections = 0;
    }

    @Nullable
    ChunkRender getNeighbor(EnumFacing direction) {
        return this.neighbors[direction.ordinal()];
    }

    void setNeighbor(EnumFacing direction, @Nullable ChunkRender neighbor) {
        this.neighbors[direction.ordinal()] = neighbor;
    }

    public boolean isFogCulled(double cameraX, double cameraY, double cameraZ, double fogEndSqr) {
        // TODO support other fog shapes
        double x = clamp(cameraX, this.getX(), this.getX() + 16) - cameraX;
        double y = clamp(cameraY, this.getY(), this.getY() + 16) - cameraY;
        double z = clamp(cameraZ, this.getZ(), this.getZ() + 16) - cameraZ;
        return Math.max(x * x + z * z, y * y) > fogEndSqr;
    }

    public double clamp(double a, double min, double max) {
        return a <= min ? min : (Math.min(a, max));
    }

    public boolean isFrustumCulled(Frustum frustum) {
        return !frustum.isAABBInFrustum(this.getX(), this.getY(), this.getZ(), this.getX() + 16, this.getY() + 16, this.getZ() + 16);
    }

    public LinkedHashMap<ChunkRenderPass, GlDynamicVBO.VBOPart> getVBOArray() {
        return this.VBOs;
    }

    @Nullable
    public GlDynamicVBO.VBOPart getVBO(ChunkRenderPass pass) {
        return this.VBOs.get(pass);
    }

    public void setVBO(ChunkRenderPass pass, @Nullable GlDynamicVBO.VBOPart SetVBO) {
        GlDynamicVBO.VBOPart vboPart = this.VBOs.get(pass);
        if (vboPart != null)
            vboPart.free();
        this.VBOs.replace(pass, SetVBO);

        if (SetVBO != null)
            EmptyCount |= 1 << pass.ordinal();
        else EmptyCount &= ~(1 << pass.ordinal());

        if (pass == ChunkRenderPass.TRANSLUCENT)
            this.setTranslucentVertexData(null);
    }

    public boolean IsVBOEmpty() {
        return this.EmptyCount == 0;
    }

    public int EmptyCount() {
        return this.EmptyCount;
    }

    public boolean canCompile() {
        if (Modfix.isCubicChunksInstalled && CubicChunks.isCubicWorld()) {
            return CubicChunks.canCompile(this);
        }

        for (int x = this.pos.getX() - 1; x <= this.pos.getX() + 1; x++) {
            for (int z = this.pos.getZ() - 1; z <= this.pos.getZ() + 1; z++) {
                if (!WorldUtil.isChunkLoaded(x, z))
                    return false;
            }
        }
        return true;
    }

    public void markDirty() {
        if ((!Modfix.isCubicChunksInstalled || !CubicChunks.isCubicWorld())
                && (this.pos.getY() < 0 || this.pos.getY() >= 16)) {
            this.getVisibility().setAllVisible();
            return;
        }
        this.dirty = true;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    /**
     * タスクの .cancel(); = null; などを実行
     */
    public void Delete() {
        this.deleteTask();
        Arrays.stream(ChunkRenderPass.ALL).forEach(i -> {
            if (this.VBOs.get(i) != null) {
                this.VBOs.get(i).free();
            }
            this.VBOs.replace(i, null);
        });//this.VBOs.forEach((key, value) -> this.VBOs.replace(key, null));
        this.setTranslucentVertexData(null);
    }

    private void deleteTask() {
        if (this.LastChunkRenderCompileTask != null) {
            this.LastChunkRenderCompileTask.SetCancelState(true);
            this.LastChunkRenderCompileTask = null;
            this.lastChunkRenderCompileTaskResult = null;
        }
    }

    public void ChunkRenderCompileAsync(ChunkRendererBase<ChunkRender> chunkRenderer, ChunkGLDispatcher taskDispatcher) {
        if (!this.isDirty() || !this.canCompile()) {
            return;
        }
        this.deleteTask();
        this.dirty = false;

        ExtendedBlockStorage blockStorage = WorldUtil.getSection(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        if (blockStorage == null || blockStorage.isEmpty()) {
            this.LastChunkRenderCompileTask = null;
            Arrays.stream(ChunkRenderPass.ALL).forEach(pass -> this.setVBO(pass, null));
            this.Visibilityset.setAllVisible();
        } else {
            /**
             * ほかのチャンクが表示されないのもここに原因がありそう。
             * */
            this.LastChunkRenderCompileTask = new ChunkRenderTaskCompiler<>(chunkRenderer, taskDispatcher, this, new GLChunkRenderCache(WorldUtil.getWorld(), this.pos));
            this.lastChunkRenderCompileTaskResult = taskDispatcher.runAsync(this.LastChunkRenderCompileTask);
        }
    }

    public void ChunkRenderResortTransparency(ChunkRendererBase<ChunkRender> chunkRenderer, ChunkGLDispatcher taskDispatcher) {
        if (this.isDirty())
            return;
        if (this.lastChunkRenderCompileTaskResult != null && !this.lastChunkRenderCompileTaskResult.isDone())
            return;

        GlDynamicVBO.VBOPart vboPart = this.getVBO(ChunkRenderPass.TRANSLUCENT);
        if (vboPart != null) {
            this.LastChunkRenderCompileTask = new ChunkRenderTranslucentSorter<>(chunkRenderer, taskDispatcher, this, vboPart, this.getTranslucentVertexData());

            this.lastChunkRenderCompileTaskResult = taskDispatcher.runAsync(this.LastChunkRenderCompileTask);
        } else {
            this.LastChunkRenderCompileTask = null;
        }
    }

    @Nullable
    public UnsafeByteBuffer getTranslucentVertexData() {
        return translucentVertexData;
    }

    public void setTranslucentVertexData(@Nullable UnsafeByteBuffer translucentVertexData) {
        if (this.translucentVertexData != null)
            this.translucentVertexData.close();
        this.translucentVertexData = translucentVertexData;
    }
}
