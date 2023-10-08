package com.aki.modfix.WorldRender.chunk.openGL;

import com.aki.mcutils.APICore.Utils.cache.IntIntInt2ObjFunction;
import com.aki.mcutils.APICore.Utils.cache.ObjIntIntIntConsumer;
import com.aki.mcutils.APICore.Utils.cache.ObjObjObjObjConsumer;
import com.aki.mcutils.APICore.Utils.render.MathUtil;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;

public class ChunkRenderProvider<T extends ChunkRender> {
    private int GridSizeX = 0;
    private int GridSizeY = 0;
    private int GridSizeZ = 0;

    private int CameraChunkX = 0;
    private int CameraChunkY = 0;
    private int CameraChunkZ = 0;

    private ChunkRender[] chunkRenders = null;

    public void init(int RenderDistX, int RenderDistY, int RenderDistZ) {
        GridSizeX = RenderDistX * 2 + 1;
        GridSizeY = RenderDistY * 2 + 1;
        GridSizeZ = RenderDistZ * 2 + 1;
        CameraChunkX = RenderDistX;
        CameraChunkY = RenderDistY;
        CameraChunkZ = RenderDistZ;

        this.chunkRenders = new ChunkRender[GridSizeX * GridSizeY * GridSizeZ];

        for (int x = 0; x < this.GridSizeX; x++) {
            for (int z = 0; z < this.GridSizeZ; z++) {
                for (int y = 0; y < this.GridSizeY; y++) {
                    ChunkRender chunkRender = new ChunkRender(x, y, z);
                    this.chunkRenders[getChunkIndex(x, y, z)] = chunkRender;

                    if(x > 0) {
                        T neighbor = this.getRenderChunkAtUnchecked(x - 1, y, z);
                        chunkRender.setNeighbor(EnumFacing.WEST, neighbor);
                        neighbor.setNeighbor(EnumFacing.EAST, chunkRender);
                    }

                    if(y > 0) {
                        T neighbor = this.getRenderChunkAtUnchecked(x, y - 1, z);
                        chunkRender.setNeighbor(EnumFacing.DOWN, neighbor);
                        neighbor.setNeighbor(EnumFacing.UP, chunkRender);
                    }

                    if(z > 0) {
                        T neighbor = this.getRenderChunkAtUnchecked(x, y, z - 1);
                        chunkRender.setNeighbor(EnumFacing.NORTH, neighbor);
                        neighbor.setNeighbor(EnumFacing.SOUTH, chunkRender);
                    }
                }
            }
        }
    }

    private int getChunkIndex(int chunkX, int chunkY, int chunkZ) {
        return (chunkZ * this.GridSizeY + chunkY) * this.GridSizeX + chunkX;
    }

    public void setDirty(int chunkX, int chunkY, int chunkZ) {
        ChunkRender renderChunk = this.getRenderChunkAt(chunkX, chunkY, chunkZ);
        if (renderChunk != null) {
            renderChunk.markDirty();
        }
    }

    public void repositionCamera(double cameraX, double cameraY, double cameraZ) {
        int newCameraChunkX = MathUtil.floor(cameraX) >> 4;
        int newCameraChunkY = MathUtil.floor(cameraY) >> 4;
        int newCameraChunkZ = MathUtil.floor(cameraZ) >> 4;

        if (MathUtil.floorMod(newCameraChunkX, this.GridSizeX) != MathUtil.floorMod(this.CameraChunkX, this.GridSizeX)) {
            updateNeighborRelations(newCameraChunkX, this.CameraChunkX, this.GridSizeX, this.GridSizeY, this.GridSizeZ, this::getXYZ, this::updateNeighborX);
        }
        if (MathUtil.floorMod(newCameraChunkY, this.GridSizeY) != MathUtil.floorMod(this.CameraChunkY, this.GridSizeY)) {
            updateNeighborRelations(newCameraChunkY, this.CameraChunkY, this.GridSizeY, this.GridSizeX, this.GridSizeZ, this::getYXZ, this::updateNeighborY);
        }
        if (MathUtil.floorMod(newCameraChunkZ, this.GridSizeZ) != MathUtil.floorMod(this.CameraChunkZ, this.GridSizeZ)) {
            updateNeighborRelations(newCameraChunkZ, this.CameraChunkZ, this.GridSizeZ, this.GridSizeX, this.GridSizeY, this::getZXY, this::updateNeighborZ);
        }

        int threshold = this.GridSizeX * this.GridSizeY * this.GridSizeZ;
        int offX = Math.abs(newCameraChunkX - this.CameraChunkX);
        int offY = Math.abs(newCameraChunkY - this.CameraChunkY);
        int offZ = Math.abs(newCameraChunkZ - this.CameraChunkZ);
        long updX = (long) offX * this.GridSizeY * this.GridSizeZ;
        long updY = (long) this.GridSizeX * offY * this.GridSizeZ;
        long updZ = (long) this.GridSizeX * this.GridSizeY * offZ;
        if (updX + updY + updZ >= threshold) {
            // update all
            updateRenderChunkPositions(newCameraChunkY, newCameraChunkX, newCameraChunkZ, this.GridSizeY, this.GridSizeX, this.GridSizeZ, this::getYXZ, this::updatePositionYXZ);
        } else {
            if (newCameraChunkX != this.CameraChunkX) {
                updateRenderChunkPositions(newCameraChunkX, newCameraChunkY, newCameraChunkZ, this.CameraChunkX, this.GridSizeX, this.GridSizeY, this.GridSizeZ, this::getXYZ, this::updatePositionXYZ);
            }
            if (newCameraChunkY != this.CameraChunkY) {
                updateRenderChunkPositions(newCameraChunkY, newCameraChunkX, newCameraChunkZ, this.CameraChunkY, this.GridSizeY, this.GridSizeX, this.GridSizeZ, this::getYXZ, this::updatePositionYXZ);
            }
            if (newCameraChunkZ != this.CameraChunkZ) {
                updateRenderChunkPositions(newCameraChunkZ, newCameraChunkX, newCameraChunkY, this.CameraChunkZ, this.GridSizeZ, this.GridSizeX, this.GridSizeY, this::getZXY, this::updatePositionZXY);
            }
        }

        this.CameraChunkX = newCameraChunkX;
        this.CameraChunkY = newCameraChunkY;
        this.CameraChunkZ = newCameraChunkZ;
    }

    private static <T> void updateNeighborRelations(int newX, int oldX, int sizeX, int sizeY, int sizeZ, IntIntInt2ObjFunction<T> renderChunkFunc, ObjObjObjObjConsumer<T, T, T, T> neighborUpdateFunc) {
        int r = sizeX >> 1;
        int oldMinX = MathUtil.floorMod(oldX - r, sizeX);
        int oldMaxX = MathUtil.floorMod(oldX + r, sizeX);
        int newMinX = MathUtil.floorMod(newX - r, sizeX);
        int newMaxX = MathUtil.floorMod(newX + r, sizeX);
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                T c1 = renderChunkFunc.apply(oldMinX, y, z);
                T c2 = renderChunkFunc.apply(oldMaxX, y, z);
                T c3 = renderChunkFunc.apply(newMinX, y, z);
                T c4 = renderChunkFunc.apply(newMaxX, y, z);
                neighborUpdateFunc.accept(c1, c2, c3, c4);
            }
        }
    }

    private T getXYZ(int x, int y, int z) {
        return this.getRenderChunkAtUnchecked(x, y, z);
    }

    private T getYXZ(int y, int x, int z) {
        return this.getRenderChunkAtUnchecked(x, y, z);
    }

    private T getZXY(int z, int x, int y) {
        return this.getRenderChunkAtUnchecked(x, y, z);
    }

    private void updateNeighborX(T c1, T c2, T c3, T c4) {
        c1.setNeighbor(EnumFacing.WEST, c2);
        c2.setNeighbor(EnumFacing.EAST, c1);
        c3.setNeighbor(EnumFacing.WEST, null);
        c4.setNeighbor(EnumFacing.EAST, null);
    }

    private void updateNeighborY(T c1, T c2, T c3, T c4) {
        c1.setNeighbor(EnumFacing.DOWN, c2);
        c2.setNeighbor(EnumFacing.UP, c1);
        c3.setNeighbor(EnumFacing.DOWN, null);
        c4.setNeighbor(EnumFacing.UP, null);
    }

    private void updateNeighborZ(T c1, T c2, T c3, T c4) {
        c1.setNeighbor(EnumFacing.NORTH, c2);
        c2.setNeighbor(EnumFacing.SOUTH, c1);
        c3.setNeighbor(EnumFacing.NORTH, null);
        c4.setNeighbor(EnumFacing.SOUTH, null);
    }

    private static <T> void updateRenderChunkPositions(int x0, int x1, int y0, int y1, int z0, int z1, int sizeX, int sizeY, int sizeZ, IntIntInt2ObjFunction<T> renderChunkFunc, ObjIntIntIntConsumer<T> positionUpdateFunc) {
        for (int x = x0; x <= x1; x++) {
            int ix = MathUtil.floorMod(x, sizeX);

            for (int y = y0; y <= y1; y++) {
                int iy = MathUtil.floorMod(y, sizeY);

                for (int z = z0; z <= z1; z++) {
                    int iz = MathUtil.floorMod(z, sizeZ);

                    positionUpdateFunc.accept(renderChunkFunc.apply(ix, iy, iz), x, y, z);
                }
            }
        }
    }

    private static <T> void updateRenderChunkPositions(int newX, int newY, int newZ, int sizeX, int sizeY, int sizeZ, IntIntInt2ObjFunction<T> renderChunkFunc, ObjIntIntIntConsumer<T> positionUpdateFunc) {
        int rx = sizeX >> 1;
        int ry = sizeY >> 1;
        int rz = sizeZ >> 1;
        int x0 = newX - rx;
        int x1 = newX + rx;
        int y0 = newY - ry;
        int y1 = newY + ry;
        int z0 = newZ - rz;
        int z1 = newZ + rz;

        updateRenderChunkPositions(x0, x1, y0, y1, z0, z1, sizeX, sizeY, sizeZ, renderChunkFunc, positionUpdateFunc);
    }

    private static <T> void updateRenderChunkPositions(int newX, int newY, int newZ, int oldX, int sizeX, int sizeY, int sizeZ, IntIntInt2ObjFunction<T> renderChunkFunc, ObjIntIntIntConsumer<T> positionUpdateFunc) {
        int rx = sizeX >> 1;
        int ry = sizeY >> 1;
        int rz = sizeZ >> 1;
        int y0 = newY - ry;
        int y1 = newY + ry;
        int z0 = newZ - rz;
        int z1 = newZ + rz;

        if (oldX < newX) {
            updateRenderChunkPositions(oldX + rx + 1, newX + rx, y0, y1, z0, z1, sizeX, sizeY, sizeZ, renderChunkFunc, positionUpdateFunc);
        } else {
            updateRenderChunkPositions(newX - rx, oldX - rx - 1, y0, y1, z0, z1, sizeX, sizeY, sizeZ, renderChunkFunc, positionUpdateFunc);
        }
    }

    private void updatePositionXYZ(T renderChunk, int x, int y, int z) {
        renderChunk.setCoord(x, y, z);
    }

    private void updatePositionYXZ(T renderChunk, int y, int x, int z) {
        renderChunk.setCoord(x, y, z);
    }

    private void updatePositionZXY(T renderChunk, int z, int x, int y) {
        renderChunk.setCoord(x, y, z);
    }


    public void Delete() {
        Arrays.stream(this.chunkRenders).forEach(ChunkRender::Delete);
    }

    public T getRenderChunkAt(int chunkX, int chunkY, int chunkZ) {
        if (chunkX < this.CameraChunkX - this.GridSizeX / 2) {
            return null;
        }
        if (chunkX > this.CameraChunkX + this.GridSizeX / 2) {
            return null;
        }
        if (chunkY < this.CameraChunkY - this.GridSizeY / 2) {
            return null;
        }
        if (chunkY > this.CameraChunkY + this.GridSizeY / 2) {
            return null;
        }
        if (chunkZ < this.CameraChunkZ - this.GridSizeZ / 2) {
            return null;
        }
        if (chunkZ > this.CameraChunkZ + this.GridSizeZ / 2) {
            return null;
        }
        chunkX = Math.floorMod(chunkX, this.GridSizeX);
        chunkY = Math.floorMod(chunkY, this.GridSizeY);
        chunkZ = Math.floorMod(chunkZ, this.GridSizeZ);
        return this.getRenderChunkAtUnchecked(chunkX, chunkY, chunkZ);
    }

    @SuppressWarnings("unchecked")
    private T getRenderChunkAtUnchecked(int chunkX, int chunkY, int chunkZ) {
        return (T)this.chunkRenders[this.getChunkIndex(chunkX, chunkY, chunkZ)];
    }

    @SuppressWarnings("unchecked")
    public T getNeighbor(T renderChunk, EnumFacing facing) {
        return (T) renderChunk.getNeighbor(facing);
    }
}
