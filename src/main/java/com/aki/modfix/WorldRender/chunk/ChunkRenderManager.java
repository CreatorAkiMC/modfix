package com.aki.modfix.WorldRender.chunk;

import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;

public class ChunkRenderManager {
    //private static ChunkRendererBase<ChunkRender> ChunkRender = null;
    private static ChunkRendererDispatcher RendererDispatcher = null;
    /*private static ChunkRenderProvider<ChunkRender> renderProvider = null;

    //VanillaFixの修正用
    public static ChunkRender CurrentChunkRender = null;

    public static ChunkRendererBase<ChunkRender> getChunkRenderer() {
        return ChunkRender;
    }*/

    public static ChunkRendererDispatcher getRenderDispatcher() {
        return RendererDispatcher;
    }

    /*public static ChunkRenderProvider<ChunkRender> getRenderProvider() {
        return renderProvider;
    }*/

    public static void SetUPTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        RendererDispatcher.update();
        //renderProvider.repositionCamera(GLUtils.getCameraX(), GLUtils.getCameraY(), GLUtils.getCameraZ());
        //ChunkRender.SetUP(renderProvider, GLUtils.getCameraX(), GLUtils.getCameraY(), GLUtils.getCameraZ(), GLUtils.getFrustum(), GLUtils.getFrame());
    }

    public static void loadRender() {
        dispose();

        int renderDist = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;

        RendererDispatcher = new ChunkRendererDispatcher();

        /*renderProvider = new ChunkRenderProvider<>();
        renderProvider.init(renderDist, renderDist, renderDist);

        ChunkRender = getGLChunkRenderer();
        ChunkRender.Init(renderDist);*/
    }

    /**
     * Config で変えれるようにしてもいいかも
     */
    /*private static ChunkRendererBase<ChunkRender> getGLChunkRenderer() {
        if (GLOptifine.OPTIFINE_INSIDE)
            return GLOptifine.createChunkRenderer(ChunkRender);
        int i = Math.min(ModfixConfig.DefaultUseGLIndex, 3);
        ContextCapabilities context = GLUtils.CAPS;

        if(i == 3 && context.OpenGL43) {
            return new ChunkRendererGL43();
        } else if(i == 2 && context.OpenGL42) {
            return new ChunkRendererGL42();
        } else if(i == 1 && context.OpenGL20) {
            return new ChunkRendererGL20();
        } else if(context.OpenGL15) {
            return new ChunkRendererGL15();
        }
        throw new UnsupportedOperationException("Your PC Don`t Supported OpenGL");
    }*/

    /*public static RenderEngineType getBestRenderEngineType() {
        ContextCapabilities context = GLUtils.CAPS;
        int i = Math.min(ModfixConfig.DefaultUseGLIndex, 3);

        if(i == 3 && context.OpenGL43) {
            return RenderEngineType.GL43;
        } else if(i == 2 && context.OpenGL42) {
            return RenderEngineType.GL42;
        } else if(i == 1 && context.OpenGL20) {
            return RenderEngineType.GL20;
        } else if(context.OpenGL15) {
            return RenderEngineType.GL15;
        }

        throw new UnsupportedOperationException("????? Your PC Don`t Supported OpenGL ?????");
    }*/

    public static void Render(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        /*if (ChunkRender != null)
            ChunkRender.Render(ChunkRenderPass.ConvVanillaRenderPass(blockLayerIn));*/
    }

    public static void dispose() {
        /*if (renderProvider != null)
            renderProvider.Delete();
        if (ChunkRender != null)
            ChunkRender.deleteDatas();*/
        if (RendererDispatcher != null)
            RendererDispatcher.Remove_ShutDown();

        /*ChunkRender = null;
        renderProvider = null;*/
        RendererDispatcher = null;
    }

    public static int RenderSections(ChunkRenderPass pass) {
        return 0;//ChunkRender.getRenderedChunks(pass);
    }

    public static int totalRenderedSections() {
        return 0;//ChunkRender.getRenderedChunks();
    }

    public static int AllPassRenderSize() {
        return 0;//ChunkRender.getAllPassRenderChunks();
    }

    public static void LoadChunk(int chunkX, int chunkZ) {

    }

    public static void UnLoadChunk(int chunkX, int chunkZ) {

    }
}
