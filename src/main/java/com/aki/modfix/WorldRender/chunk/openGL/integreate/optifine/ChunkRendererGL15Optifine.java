package com.aki.modfix.WorldRender.chunk.openGL.integreate.optifine;

import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.mcutils.APICore.Utils.render.Frustum;
import com.aki.modfix.WorldRender.chunk.openGL.ChunkRender;
import com.aki.modfix.WorldRender.chunk.openGL.ChunkRenderProvider;
import com.aki.modfix.WorldRender.chunk.openGL.RenderEngineType;
import com.aki.modfix.WorldRender.chunk.openGL.renderers.ChunkRendererGL15;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockRenderLayer;

public class ChunkRendererGL15Optifine extends ChunkRendererGL15 {
    @Override
    public RenderEngineType getRenderEngine() {
        return RenderEngineType.O_GL15;
    }

    @Override
    public void SetUP(ChunkRenderProvider<ChunkRender> provider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int Frame) {
        if (GLOptifine.IS_DYNAMIC_LIGHTS.invoke(null)) {
            GLOptifine.DYNAMIC_LIGHTS_UPDATE.invoke(null, Minecraft.getMinecraft().renderGlobal);
        }
        super.SetUP(provider, cameraX, cameraY, cameraZ, frustum, GLOptifine.IS_SHADOW_PASS.getBoolean(null) ? -Frame : Frame);
    }

    @Override
    public void RenderChunks(ChunkRenderPass pass) {
        if (GLOptifine.IS_FOG_OFF.invoke(null) && GLOptifine.FOG_STANDARD.getBoolean(Minecraft.getMinecraft().entityRenderer)) {
            GlStateManager.disableFog();
        }

        super.RenderChunks(pass);
    }

    @Override
    protected void setupClientState(ChunkRenderPass pass) {
        super.setupClientState(pass);

        if (GLOptifine.IS_SHADERS.invoke(null)) {
            GLOptifine.PRE_RENDER_CHUNK_LAYER.invoke(null, BlockRenderLayer.values()[pass.ordinal()]);
        }
    }

    @Override
    protected void setupAttributePointers(ChunkRenderPass pass) {
        if (GLOptifine.IS_SHADERS.invoke(null)) {
            GLOptifine.SETUP_ARRAY_POINTERS_VBO.invoke(null);
        } else {
            super.setupAttributePointers(pass);
        }
    }

    @Override
    protected void resetClientState(ChunkRenderPass pass) {
        if (GLOptifine.IS_SHADERS.invoke(null)) {
            GLOptifine.POST_RENDER_CHUNK_LAYER.invoke(null, BlockRenderLayer.values()[pass.ordinal()]);
        }

        super.resetClientState(pass);
    }
}
