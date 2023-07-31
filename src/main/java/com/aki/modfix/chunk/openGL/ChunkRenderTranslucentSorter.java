package com.aki.modfix.chunk.openGL;

import com.aki.mcutils.APICore.Utils.memory.UnsafeByteBuffer;
import com.aki.modfix.chunk.GLSytem.GlDynamicVBO;
import com.aki.modfix.util.gl.SortVertexUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL15;


/**
 * Translucent(透過)とSolid(不透過)の順番を入れ替える。
 * (透過が最後に来るように位置で入れ替える)
 * */
public class ChunkRenderTranslucentSorter<T extends ChunkRender> extends ChunkRenderTaskBase<T> {

    private final GlDynamicVBO.VBOPart vboPart;
    private final UnsafeByteBuffer vertexData;

    public ChunkRenderTranslucentSorter(ChunkRendererBase<T> renderer, ChunkGLDispatcher dispatcher, T chunkRender, GlDynamicVBO.VBOPart vboPart, UnsafeByteBuffer translucent) {
        super(renderer, dispatcher, chunkRender);
        this.vboPart = vboPart;
        this.vertexData = translucent;
    }

    public ChunkRenderTaskResult run() {
        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if(this.getCancel() || !this.vboPart.isValid() || renderViewEntity == null)
            return ChunkRenderTaskResult.CANCELLED;

        Vec3d camera = renderViewEntity.getPositionEyes(1.0f);
        SortVertexUtil.sortVertexData(vertexData, vboPart.getVertexCount(), DefaultVertexFormats.BLOCK.getSize(), 4, (float)(chunkRender.getX() - camera.x), (float)(chunkRender.getY() - camera.y), (float)(chunkRender.getZ() - camera.z));

        dispatcher.runOnRenderThread(() -> {
            if (!this.getCancel() && vboPart.isValid()) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPart.getVBO());
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, vboPart.getVBOFirst(),//Target, Offset, Data
                        vertexData.getBuffer());
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            }
        });

        return ChunkRenderTaskResult.SUCCESSFUL;
    }
}
