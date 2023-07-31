package com.aki.modfix.util.gl;

import net.minecraft.util.BlockRenderLayer;

public enum ChunkRenderPass {
    SOLID,
    CUTOUT,
    CUTOUT_MIPPED,
    TRANSLUCENT;

    public static final ChunkRenderPass[] ALL = ChunkRenderPass.values();

    public static ChunkRenderPass ConvVanillaRenderPass(BlockRenderLayer layer) {
        return ALL[layer.ordinal()];
    }
}
