package com.aki.modfix.chunk;

import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.chunk.openGL.*;
import com.aki.modfix.chunk.openGL.renderers.*;
import com.aki.modfix.util.gl.ChunkRenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.opengl.ContextCapabilities;

public class ChunkRenderManager {
    private static ChunkRendererBase<ChunkRender> ChunkRender = null;
    private static ChunkGLDispatcher RenderDispatcher = null;
    private static ChunkRenderProvider<ChunkRender> renderProvider = null;

    public static ChunkRendererBase<ChunkRender> getChunkRenderer() {
        return ChunkRender;
    }

    public static ChunkGLDispatcher getRenderDispatcher() {
        return RenderDispatcher;
    }

    public static ChunkRenderProvider<ChunkRender> getRenderProvider() {
        return renderProvider;
    }

    public static void SetUPTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        if(RenderDispatcher != null)
            RenderDispatcher.update();
        if(renderProvider != null)
            renderProvider.repositionCamera(GLUtils.getCameraX(), GLUtils.getCameraY(), GLUtils.getCameraZ());

        System.out.println("Frustum Is Null: " + (GLUtils.getFrustum() == null));

        if(ChunkRender != null)
            ChunkRender.SetUP(renderProvider, GLUtils.getCameraX(), GLUtils.getCameraY(), GLUtils.getCameraZ(), GLUtils.getFrustum(), GLUtils.getFrame());
    }

    public static void loadRender() {
        dispose();

        int renderDist = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;

        RenderDispatcher = new ChunkGLDispatcher();

        renderProvider = new ChunkRenderProvider<>();
        renderProvider.init(renderDist, renderDist, renderDist);

        ChunkRender = getGLChunkRenderer();
        ChunkRender.Init(renderDist);
    }

    /**
     * Config で変えれるようにしてもいいかも
     * */
    private static ChunkRendererBase<ChunkRender> getGLChunkRenderer() {
        ContextCapabilities context = GLUtils.CAPS;
        if(context.OpenGL43) {
            return new ChunkRendererGL43();
        } else if(context.OpenGL42) {
            return new ChunkRendererGL42();
        } else if(context.OpenGL20) {
            return new ChunkRendererGL20();
        } else if(context.OpenGL15) {
            return new ChunkRendererGL15();
        }

        throw new UnsupportedOperationException("Your PC Don`t Supported OpenGL");
    }

    public static void Render(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        if(ChunkRender != null)
            ChunkRender.Render(ChunkRenderPass.ConvVanillaRenderPass(blockLayerIn));
    }

    public static void dispose() {
        if(ChunkRender != null)
            ChunkRender.deleteDatas();
        if(renderProvider != null)
            renderProvider.Delete();
        if(RenderDispatcher != null)
            RenderDispatcher.Remove_ShutDown();

        ChunkRender = null;
        renderProvider = null;
        RenderDispatcher = null;
    }

    public static void LoadChunk(int chunkX, int chunkZ) {

    }

    public static void UnLoadChunk(int chunkX, int chunkZ) {

    }
}
