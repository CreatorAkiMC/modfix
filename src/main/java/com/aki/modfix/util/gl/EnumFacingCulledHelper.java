package com.aki.modfix.util.gl;

public class EnumFacingCulledHelper {
    /*public static boolean isFaceCulled(EnumFacing facing, ChunkRender chunkRender, double cameraX, double cameraY, double cameraZ) {
        switch (facing) {
            case DOWN:
                if ((!Modfix.isCubicChunksInstalled || !CubicChunks.isCubicWorld()) && chunkRender.getSectionY() >= 16)
                    return true;
                return cameraY > chunkRender.getY();
            case UP:
                if ((!Modfix.isCubicChunksInstalled || !CubicChunks.isCubicWorld()) && chunkRender.getSectionY() < 0)
                    return true;
                return cameraY < chunkRender.getY() + 16;
            case NORTH:
                return cameraZ > chunkRender.getZ();
            case SOUTH:
                return cameraZ < chunkRender.getZ() + 16;
            case WEST:
                return cameraX > chunkRender.getX();
            case EAST:
                return cameraX < chunkRender.getX() + 16;
        }
        return false;
    }*/
}
