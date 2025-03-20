package com.aki.modfix.WorldRender.chunk;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nullable;

public class WorldUtil {
    //念のため、Nullable で作る。
    @Nullable
    public static World getWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Nullable
    public static Chunk getChunk(int chunkX, int chunkZ) {
        World world = getWorld();
        if(world == null) {
            return null;
        }
        return world.getChunk(chunkX, chunkZ);
    }

    public static boolean isSectionLoaded(int sectionX, int sectionY, int sectionZ) {
        /*
        * CubicChunksと互換性
        * */

        World world = getWorld();
        if(world == null) {
            return false;
        }

        return isChunkLoaded(sectionX, sectionZ);
    }

    @Nullable
    public static ExtendedBlockStorage getStorageOfSection(int sectionX, int sectionY, int sectionZ) {
        if(sectionY < 0 || 16 <= sectionY) {
            return null;
        }

        Chunk chunk = getChunk(sectionX, sectionZ);
        if(chunk == null) {
            return null;
        }

        //sectionY 256を16等分
        return chunk.getBlockStorageArray()[sectionY];
    }

    public static boolean isChunkLoaded(int sectionX, int sectionZ) {
        World world = getWorld();
        if(world == null) {
            return false;
        }

        return world.getChunkProvider().getLoadedChunk(sectionX, sectionZ) != null;
    }
}