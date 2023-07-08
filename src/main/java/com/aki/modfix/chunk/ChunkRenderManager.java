package com.aki.modfix.chunk;

import com.aki.modfix.chunk.openGL.test.GLRenderTest;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

public class ChunkRenderManager {
    public static void SetUPTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        GLRenderTest renderTest = new GLRenderTest();
        //while (true) {
        renderTest.Render();
        //}
    }

    public static void LoadChunk(int chunkX, int chunkZ) {

    }

    public static void UnLoadChunk(int chunkX, int chunkZ) {

    }
}
