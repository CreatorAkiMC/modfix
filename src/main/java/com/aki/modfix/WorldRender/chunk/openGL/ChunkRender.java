package com.aki.modfix.WorldRender.chunk.openGL;

import com.aki.mcutils.APICore.Utils.list.GFastList;
import com.aki.mcutils.APICore.Utils.list.MapCreateHelper;
import com.aki.mcutils.APICore.Utils.memory.UnsafeByteBuffer;
import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.mcutils.APICore.Utils.render.Frustum;
import com.aki.mcutils.APICore.Utils.render.SectionPos;
import com.aki.mcutils.APICore.Utils.render.VisibilitySet;
import com.aki.modfix.GLSytem.GLDynamicIBO;
import com.aki.modfix.GLSytem.GLDynamicVBO;
import com.aki.modfix.Modfix;
import com.aki.modfix.WorldRender.chunk.openGL.integreate.CubicChunks;
import com.aki.modfix.WorldRender.chunk.openGL.renderers.ChunkRendererBase;
import com.aki.modfix.util.gl.VertexData;
import com.aki.modfix.util.gl.WorldUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * BufferBuilder など
 * 透過タスクの実行も
 */
public class ChunkRender {
    //private RegionRenderCacheBuilder VBOs = null;
    private final LinkedHashMap<ChunkRenderPass, GLDynamicVBO.VBOPart> VBOs = MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i -> null);
    private final LinkedHashMap<ChunkRenderPass, GLDynamicIBO.IBOPart> IBOs = MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i -> null);
    //チャンク全体の頂点
    private final List<VertexData> vertexes = new GFastList<>();
    private final HashMap<ChunkRenderPass, Integer> BaseVertexes = MapCreateHelper.CreateHashMap(ChunkRenderPass.values(), i -> 0);
    private final HashMap<ChunkRenderPass, ByteBuffer> IndexesBuffers = new HashMap<>();
    // ChainSectors の ID はマルチスレッドでの処理速度によって大分前後します。
    // なので一致しません。
    // ほぼ、デバック用です。
    private final int ChunkRenderIndex;
    private int updateCount = 0;

    private boolean dirty;
    private final ChunkRender[] neighbors = new ChunkRender[EnumFacing.values().length];

    public int lastEnqueuedTime = -1;
    public int lastRecordedTime = -1;

    private VisibilitySet Visibilityset = new VisibilitySet();
    public int VisibleDirections;
    private ChunkRenderTaskBase<ChunkRender> LastChunkRenderCompileTask;
    private CompletableFuture<ChunkRenderTaskResult> lastChunkRenderCompileTaskResult;

    private UnsafeByteBuffer translucentVertexData;
    private List<ChunkRenderTaskCompiler.Index2VertexVec> translucentIndexData = new ArrayList<>();

    private SectionPos pos;

    private int EmptyCount = 0;//空の場合は加算

    private final Set<TextureAtlasSprite> visibleTextures = new HashSet<>();

    public ChunkRender(int X, int Y, int Z, int chunkRenderIndex) {
        this.pos = SectionPos.of(X, Y, Z);
        this.ChunkRenderIndex = chunkRenderIndex;
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

    public Set<TextureAtlasSprite> getVisibleTextures() {
        return visibleTextures;
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
        //Fix bugs
        return !frustum.isAABBInFrustum(this.getX() - 1, this.getY() - 1, this.getZ() - 1, this.getX() + 17, this.getY() + 17, this.getZ() + 17);
    }

    @Nullable
    public GLDynamicVBO.VBOPart getVBO(ChunkRenderPass pass) {
        return this.VBOs.get(pass);
    }

    @Nullable
    public GLDynamicIBO.IBOPart getIBO(ChunkRenderPass pass) {
        return this.IBOs.get(pass);
    }

    public void setVBO(ChunkRenderPass pass, @Nullable GLDynamicVBO.VBOPart SetVBO) {
        GLDynamicVBO.VBOPart vboPart = this.VBOs.get(pass);
        if (vboPart != null)
            vboPart.free();
        this.VBOs.replace(pass, SetVBO);

        if (SetVBO != null)
            EmptyCount |= 1 << pass.ordinal();
        else EmptyCount &= ~(1 << pass.ordinal());

        if (pass == ChunkRenderPass.TRANSLUCENT)
            this.setTranslucentVertexData(null);
    }

    public void setIBO(ChunkRenderPass pass, @Nullable GLDynamicIBO.IBOPart SetIBO) {
        GLDynamicIBO.IBOPart vboPart = this.IBOs.get(pass);
        if (vboPart != null)
            vboPart.free();
        this.IBOs.replace(pass, SetIBO);
    }

    public boolean IsVBOEmpty() {
        return this.EmptyCount == 0;
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
        this.updateCount++;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public int getChunkRenderIndex() {
        return this.ChunkRenderIndex;
    }

    // 更新された回数
    // 2^31回、更新することは無いと思います。
    public int getUpdateCount() {
        return this.updateCount;
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
            if(this.IBOs.get(i) != null) {
                this.IBOs.get(i).free();
            }
            this.IBOs.replace(i, null);
        });
        this.setTranslucentVertexData(null);
        this.vertexes.clear();
        this.IndexesBuffers.clear();
    }

    private void deleteTask() {
        if (this.LastChunkRenderCompileTask != null) {
            this.LastChunkRenderCompileTask.SetCancelState(true);
            this.LastChunkRenderCompileTask = null;
            this.lastChunkRenderCompileTaskResult = null;
        }
    }

    // ブロックの更新があった時に、呼び出される。
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
            this.LastChunkRenderCompileTask = new ChunkRenderTaskCompiler<>(chunkRenderer, taskDispatcher, this, new GLChunkRenderCache(WorldUtil.getWorld(), this.pos));
            this.lastChunkRenderCompileTaskResult = taskDispatcher.runAsync(this.LastChunkRenderCompileTask);
        }
    }

    public void ChunkRenderResortTransparency(ChunkRendererBase<ChunkRender> chunkRenderer, ChunkGLDispatcher taskDispatcher) {
        if (this.isDirty())
            return;
        if (this.lastChunkRenderCompileTaskResult != null && !this.lastChunkRenderCompileTaskResult.isDone())
            return;

        GLDynamicVBO.VBOPart vboPart = this.getVBO(ChunkRenderPass.TRANSLUCENT);
        GLDynamicIBO.IBOPart iboPart = this.getIBO(ChunkRenderPass.TRANSLUCENT);
        if (vboPart != null && iboPart != null) {
            this.LastChunkRenderCompileTask = new ChunkRenderTranslucentSorter<>(chunkRenderer, taskDispatcher, this, vboPart, this.getTranslucentVertexData(), iboPart, this.getTranslucentIndexData());

            this.lastChunkRenderCompileTaskResult = taskDispatcher.runAsync(this.LastChunkRenderCompileTask);
        } else {
            this.LastChunkRenderCompileTask = null;
        }
    }

    @Nullable
    public UnsafeByteBuffer getTranslucentVertexData() {
        return this.translucentVertexData;
    }
    public List<ChunkRenderTaskCompiler.Index2VertexVec> getTranslucentIndexData() {
        return this.translucentIndexData;
    }

    public void setTranslucentVertexData(@Nullable UnsafeByteBuffer translucentVertexData) {
        if (this.translucentVertexData != null)
            this.translucentVertexData.close();
        this.translucentVertexData = translucentVertexData;
    }

    public void setTranslucentIndexData(List<ChunkRenderTaskCompiler.Index2VertexVec> indexData) {
        //this.translucentIndexData.clear();
        this.translucentIndexData = indexData;
    }

    public List<VertexData> getVertexData() {
        return this.vertexes;
    }

    public int getBaseVertex(ChunkRenderPass pass) {
        return this.BaseVertexes.get(pass);
    }

    public void addBaseVertex(ChunkRenderPass pass, int add) {
        this.BaseVertexes.replace(pass, this.BaseVertexes.get(pass) + add);
    }

    public void CreateIndexesBuffer(ChunkRenderPass pass, Integer[] datas) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(datas.length * 4);
        for (int index : datas)
            buffer.putInt(index);
        buffer.flip();
        if (this.IndexesBuffers.containsKey(pass)) {
            this.IndexesBuffers.replace(pass, buffer);
        } else {
            this.IndexesBuffers.put(pass, buffer);
        }
    }

    public ByteBuffer getIndexesBuffer(ChunkRenderPass pass) {
        return this.IndexesBuffers.getOrDefault(pass, ByteBuffer.allocate(0));
    }
}
