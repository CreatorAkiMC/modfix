package com.aki.modfix.chunk.openGL.integreate.optifine;

import com.aki.mcutils.APICore.Utils.render.Frustum;
import com.aki.modfix.chunk.openGL.ChunkRender;
import com.aki.modfix.chunk.openGL.ChunkRenderProvider;
import com.aki.modfix.chunk.openGL.RenderEngineType;
import com.aki.modfix.chunk.openGL.renderers.ChunkRendererGL42;
import com.aki.modfix.util.gl.ChunkRenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ChunkRendererGL42Optifine extends ChunkRendererGL42 {
    @Override
    public RenderEngineType getRenderEngine() {
        return RenderEngineType.O_GL42;
    }

    @Override
    public void SetUP(ChunkRenderProvider<ChunkRender> provider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int Frame) {
        if (GLOptifine.IS_DYNAMIC_LIGHTS.invoke(null)) {
            GLOptifine.DYNAMIC_LIGHTS_UPDATE.invoke(null, Minecraft.getMinecraft().renderGlobal);
        }
        super.SetUP(provider, cameraX, cameraY, cameraZ, frustum, Frame);
    }

    @Override
    public void RenderChunks(ChunkRenderPass pass) {
        if (GLOptifine.IS_FOG_OFF.invoke(null) && GLOptifine.FOG_STANDARD.getBoolean(Minecraft.getMinecraft().entityRenderer)) {
            GlStateManager.disableFog();
        }
        super.RenderChunks(pass);
    }
}
