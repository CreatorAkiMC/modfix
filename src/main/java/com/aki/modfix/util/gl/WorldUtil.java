package com.aki.modfix.util.gl;

import com.aki.modfix.Modfix;
import com.aki.modfix.chunk.openGL.integreate.CubicChunks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class WorldUtil {
    public static World getWorld() {
        return Minecraft.getMinecraft().world;
    }

    public static Chunk getChunk(int chunkX, int chunkZ) {
        World world = getWorld();
        if (world == null) {
            return null;
        }
        return world.getChunk(chunkX, chunkZ);
    }

    public static boolean isSectionLoaded(int sectionX, int sectionY, int sectionZ) {
        if (Modfix.isCubicChunksInstalled && CubicChunks.isCubicWorld(getWorld())) {
            return CubicChunks.isSectionLoaded(getWorld(), sectionX, sectionY, sectionZ);
        }
        return isChunkLoaded(sectionX, sectionZ);
    }

    public static ExtendedBlockStorage getSection(int sectionX, int sectionY, int sectionZ) {
        if (Modfix.isCubicChunksInstalled && CubicChunks.isCubicWorld(getWorld())) {
            return CubicChunks.getSection(getWorld(), sectionX, sectionY, sectionZ);
        }
        if (sectionY < 0 || sectionY >= 16) {
            return null;
        }
        Chunk chunk = getChunk(sectionX, sectionZ);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockStorageArray()[sectionY];
    }


    public static boolean isChunkLoaded(int chunkX, int chunkZ) {
        return getWorld().getChunkProvider().getLoadedChunk(chunkX, chunkZ) != null;
    }
}
