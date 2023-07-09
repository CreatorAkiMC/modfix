package com.aki.modfix.chunk;

import com.aki.modfix.chunk.openGL.test.GLRenderTest;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

public class ChunkRenderManager {
    public static GLRenderTest renderTest = null;

    public static void SetUPTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        while (renderTest != null) {
             renderTest.Render();
        }
    }

    public static void loadRenderers() {
        if (renderTest != null) {
            renderTest.deleteDatas();
        }
        renderTest = new GLRenderTest();
        renderTest.init();
    }

    public static void LoadChunk(int chunkX, int chunkZ) {

    }

    public static void UnLoadChunk(int chunkX, int chunkZ) {

    }
}
