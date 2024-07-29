package com.aki.modfix.WorldRender.chunk.openGL;

import com.aki.mcutils.APICore.Utils.memory.UnsafeByteBuffer;
import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.mcutils.APICore.Utils.render.SortVertexUtil;
import com.aki.modfix.GLSytem.GLDynamicIBO;
import com.aki.modfix.GLSytem.GLDynamicVBO;
import com.aki.modfix.WorldRender.chunk.openGL.renderers.ChunkRendererBase;
import com.aki.modfix.util.gl.ChunkModelMeshUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL15;

import java.util.List;


/**
 * Translucent(透過)とSolid(不透過)の順番を入れ替える。
 * (透過が最後に来るように位置で入れ替える)
 */
public class ChunkRenderTranslucentSorter<T extends ChunkRender> extends ChunkRenderTaskBase<T> {

    private final GLDynamicVBO.VBOPart vboPart;
    private final UnsafeByteBuffer vertexData;
    private final GLDynamicIBO.IBOPart iboPart;
    private List<ChunkRenderTaskCompiler.Index2VertexVec> index2VertexVecList;

    public ChunkRenderTranslucentSorter(ChunkRendererBase<T> renderer, ChunkGLDispatcher dispatcher, T chunkRender, GLDynamicVBO.VBOPart vboPart, UnsafeByteBuffer translucent, GLDynamicIBO.IBOPart iboPart, List<ChunkRenderTaskCompiler.Index2VertexVec> index2VertexVecList) {
        super(renderer, dispatcher, chunkRender);
        this.vboPart = vboPart;
        this.vertexData = translucent;
        this.iboPart = iboPart;
        this.index2VertexVecList = index2VertexVecList;
    }

    public ChunkRenderTaskResult run() {
        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if (this.getCancel() || !this.vboPart.isValid() || renderViewEntity == null)
            return ChunkRenderTaskResult.CANCELLED;

        //プレイヤーとチャンクとの距離から、チャンクの透過ブロックを降順(距離が遠いものから)に並べ替えます。
        Vec3d camera = renderViewEntity.getPositionEyes(1.0f);
        SortVertexUtil.sortVertexData(vertexData, vboPart.getVertexCount(), DefaultVertexFormats.BLOCK.getSize(), 4, (float) (chunkRender.getX() - camera.x), (float) (chunkRender.getY() - camera.y), (float) (chunkRender.getZ() - camera.z));
        this.index2VertexVecList = ChunkModelMeshUtils.SortIndex2VertexVec(this.index2VertexVecList, 4, (camera.x - chunkRender.getX()), (camera.y - chunkRender.getY()), (camera.z - chunkRender.getZ()));

        dispatcher.runOnRenderThread(() -> {
            if (!this.getCancel()) {
                if(vboPart.isValid()) {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPart.getVBO());
                    GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) vboPart.getVBOFirst() * DefaultVertexFormats.BLOCK.getSize(),//Target, Offset, Data
                            vertexData.getBuffer());
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                }
                if(iboPart.isValid()) {
                    this.chunkRender.CreateIndexesBuffer(ChunkRenderPass.TRANSLUCENT, this.index2VertexVecList.stream().map(ChunkRenderTaskCompiler.Index2VertexVec::getIndex).toArray(Integer[]::new));
                    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboPart.getIBO());
                    GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, iboPart.getIBOFirst(),//Target, Offset, Data
                            this.chunkRender.getIndexesBuffer(ChunkRenderPass.TRANSLUCENT));
                    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
                }
            }
        });

        return ChunkRenderTaskResult.SUCCESSFUL;
    }
}
