package com.aki.modfix.util.gl.quadList;

import com.aki.modfix.util.gl.BlockVertexData;
import net.minecraft.client.renderer.block.model.IBakedModel;

import java.util.Objects;

public class BlockVertexValue implements IBakedQuadValue {
    private final BlockVertexData vertexData;

    public BlockVertexValue(BlockVertexData vertexData) {
        this.vertexData = vertexData;
    }

    @Override
    public BlockVertexData getBlockVertexData(IBakedModel model) {
        return this.vertexData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockVertexValue)) return false;
        BlockVertexValue that = (BlockVertexValue) o;
        return Objects.equals(this.vertexData, that.vertexData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.vertexData);
    }
}
