package com.aki.modfix.chunk;

import com.aki.modfix.chunk.openGL.test.GLRenderTest;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;

public class ChunkRenderManager {
    public static GLRenderTest renderTest = null;

    public static void SetUPTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        ///while (renderTest != null) {
        //renderTest.Render();
        //}

        renderTest.SetUP();
    }

    public static void loadRenderers() {
        if (renderTest != null) {
            renderTest.deleteDatas();
        }
        renderTest = new GLRenderTest();
        renderTest.init();
    }

    public static void Render(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        renderTest.Render();
    }

    public static void LoadChunk(int chunkX, int chunkZ) {

    }

    public static void UnLoadChunk(int chunkX, int chunkZ) {

    }
}
