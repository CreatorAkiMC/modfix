package com.aki.modfix.chunk.openGL.renderers;

import com.aki.mcutils.APICore.Utils.matrixutil.Matrix4f;
import com.aki.mcutils.APICore.Utils.render.Frustum;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.chunk.GLSytem.GLMutableArrayBuffer;
import com.aki.modfix.chunk.GLSytem.GlDynamicVBO;
import com.aki.modfix.chunk.GLSytem.GlVertexOffsetBuffer;
import com.aki.modfix.chunk.openGL.ChunkRender;
import com.aki.modfix.chunk.openGL.ChunkRenderProvider;
import com.aki.modfix.chunk.openGL.RenderEngineType;
import com.aki.modfix.util.gl.*;
import org.lwjgl.opengl.*;

import java.util.Arrays;

public class ChunkRendererGL42 extends ChunkRendererBase<ChunkRender> {
    public ChunkRendererGL42() {
        super();
    }

    @Override
    public RenderEngineType getRenderEngine() {
        return RenderEngineType.GL42;
    }

    @Override
    public void Init(int renderDist) {
        int PD = renderDist * 2 + 1;
        int dist3 = (int)Math.pow(PD, 3);

        //this.CommandBuffers = MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i -> new GlCommandBuffer(dist3 * 16L, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, GL30.GL_MAP_WRITE_BIT));
        this.OffsetBuffers = new RTList<>(2, 0, i -> MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i2 -> new GlVertexOffsetBuffer(dist3 * 12L, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, GL30.GL_MAP_WRITE_BIT)));

        Arrays.stream(ChunkRenderPass.ALL).forEach(this::InitVAOs);
    }

    private void InitVAOs(ChunkRenderPass pass) {
        try {
            this.program.useShader();

            this.VaoBuffers.forEach((index, VAOMap) -> {
                GLMutableArrayBuffer VAO = VAOMap.get(pass);

                VAO.ChangeNewVAO();
                VAO.bind();

                int A_Pos = this.program.getAttributeLocation(A_POS);
                int A_Color = this.program.getAttributeLocation(A_COLOR);
                int a_texCoord = this.program.getAttributeLocation(A_TEXCOORD);
                int a_lightCoord = this.program.getAttributeLocation(A_LIGHTCOORD);
                int Offset = program.getAttributeLocation(A_OFFSET);

                //読み込み
                this.DynamicBuffers.get(pass).bind(GL15.GL_ARRAY_BUFFER);

                //Size は分割する量
                GL20.glVertexAttribPointer(A_Pos, 3, GL11.GL_FLOAT, false, 28, 0L);
                GL20.glVertexAttribPointer(A_Color, 4, GL11.GL_UNSIGNED_BYTE, true, 28, 12L);
                GL20.glVertexAttribPointer(a_texCoord, 2, GL11.GL_FLOAT, false, 28, 16L);
                GL20.glVertexAttribPointer(a_lightCoord, 2, GL11.GL_SHORT, false, 28, 24L);
                GL20.glEnableVertexAttribArray(A_Pos);
                GL20.glEnableVertexAttribArray(A_Color);
                GL20.glEnableVertexAttribArray(a_texCoord);
                GL20.glEnableVertexAttribArray(a_lightCoord);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.OffsetBuffers.get(index).get(pass).getBufferIndex());
                GL20.glVertexAttribPointer(Offset, 3, GL11.GL_FLOAT, false, 0, 0L);
                GL20.glEnableVertexAttribArray(Offset);//VAO内で、Index を固定化
                GL33.glVertexAttribDivisor(Offset, 1);//1頂点で分割
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

                this.DynamicBuffers.get(pass).unbind(GL15.GL_ARRAY_BUFFER);

                VAO.unbind();
                VAOMap.replace(pass, VAO);
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.program.releaseShader();
        }
    }

    @Override
    public void SetUP(ChunkRenderProvider<ChunkRender> provider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int Frame) {
        super.SetUP(provider, cameraX, cameraY, cameraZ, frustum, Frame);
        try {
            this.SyncList.ToNext();
            this.OffsetBuffers.ToNext();
            this.VaoBuffers.ToNext();

            if (this.SyncList.getSelect() != -1) {
                GL33.glGetQueryObjecti64(this.SyncList.getSelect(), GL15.GL_QUERY_RESULT);
                GL15.glDeleteQueries(this.SyncList.getSelect());
                this.SyncList.setSelect(-1);
            }

            Arrays.stream(ChunkRenderPass.ALL).forEach(pass -> this.OffsetBuffers.getSelect().get(pass).begin());

            this.RenderChunks.forEach((pass, list) -> {
                this.OffsetBuffers.getSelect().get(pass).ResetWriter();
                ListUtil.forEach(list, pass == ChunkRenderPass.TRANSLUCENT,(chunkRender, index) ->{
                    this.OffsetBuffers.getSelect().get(pass).addIndirectDrawOffsetCall((float) (chunkRender.getX() - cameraX), (float) (chunkRender.getY() - cameraY), (float) (chunkRender.getZ() - cameraZ));
                });
            });

            Arrays.stream(ChunkRenderPass.ALL).forEach(pass -> this.OffsetBuffers.getSelect().get(pass).end());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void RenderChunks(ChunkRenderPass pass) {
        int projectionMatrixIndex = program.getUniformLocation("u_ModelViewProjectionMatrix");
        Matrix4f mat4f = GLUtils.getProjectionModelViewMatrix().copy();
        mat4f.translate((float) GLUtils.getCameraOffsetX(), (float) GLUtils.getCameraOffsetY(), (float) GLUtils.getCameraOffsetZ());
        GLUtils.setMatrix(projectionMatrixIndex, mat4f);
        GLFogUtils.setupFogFromGL(program);

        this.VaoBuffers.getSelect().get(pass).bind();
        //this.CommandBuffers.get(pass).getCount() == this.RenderChunks.get(pass).size()
        ListUtil.forEach(RenderChunks.get(pass), pass == ChunkRenderPass.TRANSLUCENT, (renderChunk, i) -> {
            GlDynamicVBO.VBOPart vboPart = renderChunk.getVBO(pass);
            GL42.glDrawArraysInstancedBaseInstance(GL11.GL_QUADS, vboPart.getVBOFirst(), vboPart.getVertexCount(), 1, i);
        });
        //GL43.glMultiDrawArraysIndirect(GL11.GL_QUADS, 0, this.CommandBuffers.get(pass).getCount(), 0);

        if (pass == ChunkRenderPass.TRANSLUCENT) {//同期
            if (this.SyncList.getSelect() != -1)
                GL15.glDeleteQueries(this.SyncList.getSelect());
            int query = GL15.glGenQueries();
            GL33.glQueryCounter(query, GL33.GL_TIMESTAMP);
            this.SyncList.setSelect(query);
        }

        this.VaoBuffers.getSelect().get(pass).unbind();
    }
}
