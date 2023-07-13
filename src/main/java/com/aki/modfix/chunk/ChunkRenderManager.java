package com.aki.modfix.chunk;

import com.aki.modfix.chunk.openGL.test.GLRenderTest;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;

public class ChunkRenderManager {
    private static GLRenderTest renderTest = null;

    public static void SetUPTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        ///while (renderTest != null) {
        //renderTest.Render();
        //}
        if(renderTest != null)
            renderTest.SetUP();
    }

    public static void loadRender() {
        if (renderTest != null) {
            renderTest.deleteDatas();
            renderTest = null;
        }
        renderTest = new GLRenderTest();
        renderTest.init();
    }

    public static void Render(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        if(renderTest != null)
            renderTest.Render(blockLayerIn);
    }

    public static void dispose() {
        if(renderTest != null)
            renderTest.deleteDatas();
        renderTest = null;
    }

    public static void LoadChunk(int chunkX, int chunkZ) {

    }

    public static void UnLoadChunk(int chunkX, int chunkZ) {

    }
}
