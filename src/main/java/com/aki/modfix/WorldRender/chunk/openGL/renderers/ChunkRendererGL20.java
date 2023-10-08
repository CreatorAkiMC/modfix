package com.aki.modfix.WorldRender.chunk.openGL.renderers;

import com.aki.mcutils.APICore.Utils.matrixutil.Matrix4f;
import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.mcutils.APICore.Utils.render.GLFogUtils;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.mcutils.APICore.Utils.render.ListUtil;
import com.aki.modfix.GLSytem.GlDynamicVBO;
import com.aki.modfix.WorldRender.chunk.openGL.ChunkRender;
import com.aki.modfix.WorldRender.chunk.openGL.RenderEngineType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class ChunkRendererGL20 extends ChunkRendererBase<ChunkRender> {
    public ChunkRendererGL20() {
        super();
    }

    @Override
    public RenderEngineType getRenderEngine() {
        return RenderEngineType.GL20;
    }

    @Override
    public void Init(int renderDist) {

    }

    @Override
    public void RenderChunks(ChunkRenderPass pass) {
        Matrix4f matrix = GLUtils.getProjectionModelViewMatrix().copy();
        matrix.translate((float) GLUtils.getCameraOffsetX(), (float) GLUtils.getCameraOffsetY(), (float) GLUtils.getCameraOffsetZ());
        GLUtils.setMatrix(program.getUniformLocation("u_ModelViewProjectionMatrix"), matrix);
        GLFogUtils.setupFogFromGL(program);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, DynamicBuffers.get(pass).handle());

        setupClientState(pass);
        setupAttributePointers(pass);

        double cameraX = GLUtils.getCameraEntityX();
        double cameraY = GLUtils.getCameraEntityY();
        double cameraZ = GLUtils.getCameraEntityZ();
        ListUtil.forEach(RenderChunks.get(pass), pass == ChunkRenderPass.TRANSLUCENT, (renderChunk, i) -> {
            this.draw(renderChunk, pass, cameraX, cameraY, cameraZ);
        });

        resetClientState(pass);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    protected void setupClientState(ChunkRenderPass pass) {
        GL20.glEnableVertexAttribArray(program.getAttributeLocation(A_POS));
        GL20.glEnableVertexAttribArray(program.getAttributeLocation(A_COLOR));
        GL20.glEnableVertexAttribArray(program.getAttributeLocation(A_TEXCOORD));
        GL20.glEnableVertexAttribArray(program.getAttributeLocation(A_LIGHTCOORD));
    }

    protected void setupAttributePointers(ChunkRenderPass pass) {
        GL20.glVertexAttribPointer(program.getAttributeLocation(A_POS), 3, GL11.GL_FLOAT, false, 28, 0);
        GL20.glVertexAttribPointer(program.getAttributeLocation(A_COLOR), 4, GL11.GL_UNSIGNED_BYTE, true, 28, 12);
        GL20.glVertexAttribPointer(program.getAttributeLocation(A_TEXCOORD), 2, GL11.GL_FLOAT, false, 28, 16);
        GL20.glVertexAttribPointer(program.getAttributeLocation(A_LIGHTCOORD), 2, GL11.GL_SHORT, false, 28, 24);
    }

    protected void draw(ChunkRender chunkRender, ChunkRenderPass pass, double cameraX, double cameraY, double cameraZ) {
        GL20.glVertexAttrib3f(program.getAttributeLocation(A_OFFSET), (float) (chunkRender.getX() - cameraX), (float) (chunkRender.getY() - cameraY), (float) (chunkRender.getZ() - cameraZ));
        GlDynamicVBO.VBOPart vboPart = chunkRender.getVBO(pass);
        GL11.glDrawArrays(GL11.GL_QUADS, vboPart.getVBOFirst(), vboPart.getVertexCount());
    }

    protected void resetClientState(ChunkRenderPass pass) {
        GL20.glDisableVertexAttribArray(program.getAttributeLocation(A_POS));
        GL20.glDisableVertexAttribArray(program.getAttributeLocation(A_COLOR));
        GL20.glDisableVertexAttribArray(program.getAttributeLocation(A_TEXCOORD));
        GL20.glDisableVertexAttribArray(program.getAttributeLocation(A_LIGHTCOORD));
    }
}
