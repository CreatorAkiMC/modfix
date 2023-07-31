package com.aki.modfix.util.gl;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class ChunkSector {
    private int ChunkOffsetIndex = 0;

    public ChunkSector(int chunkOffsetIndex) {
        this.ChunkOffsetIndex = chunkOffsetIndex;
    }

    public int getChunkOffsetIndex() {
        return ChunkOffsetIndex;
    }

    public int getChunkBlockOffset() {
        return ChunkOffsetIndex * 4096;
    }

    //Buffer
    public int getVBOFirst() {
        return this.getChunkBlockOffset() * DefaultVertexFormats.BLOCK.getSize();
    }
}
