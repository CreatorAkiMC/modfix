package com.aki.modfix.WorldRender.chunk.openGL.renderers;

import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.mcutils.APICore.Utils.render.ListUtil;
import com.aki.modfix.GLSytem.GLDynamicVBO;
import com.aki.modfix.ModfixConfig;
import com.aki.modfix.WorldRender.chunk.openGL.ChunkRender;
import com.aki.modfix.WorldRender.chunk.openGL.RenderEngineType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

public class ChunkRendererGL15 extends ChunkRendererBase<ChunkRender> {
    public ChunkRendererGL15() {
        super();
    }

    @Override
    public RenderEngineType getRenderEngine() {
        return RenderEngineType.GL15;
    }

    @Override
    public void Init(int renderDist) {

    }

    @Override
    public void RenderChunks(ChunkRenderPass pass) {
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
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
    }

    protected void setupAttributePointers(ChunkRenderPass pass) {
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
        GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        GL11.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
    }

    protected void draw(ChunkRender renderChunk, ChunkRenderPass pass, double cameraX, double cameraY, double cameraZ) {
        GLDynamicVBO.VBOPart vboPart = renderChunk.getVBO(pass);
        GL11.glPushMatrix();
        GL11.glTranslated(renderChunk.getX() - cameraX, renderChunk.getY() - cameraY, renderChunk.getZ() - cameraZ);
        //System.out.println(" GLVBOFist: " + vboPart.getVBOFirst() + ", Count: " + vboPart.getVertexCount());
        if(ModfixConfig.UseElementBuffer) {
            GL11.glDrawElements(GL11.GL_QUADS, renderChunk.getIndexesBuffer(pass));
        } else {
            GL11.glDrawArrays(GL11.GL_QUADS, vboPart.getFirst(), vboPart.getCount());
        }
        GL11.glPopMatrix();
    }

    protected void resetClientState(ChunkRenderPass pass) {
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
    }
}
