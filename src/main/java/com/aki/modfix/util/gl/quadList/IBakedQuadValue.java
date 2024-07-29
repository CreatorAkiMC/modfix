package com.aki.modfix.util.gl.quadList;

import com.aki.modfix.util.gl.BlockVertexData;
import net.minecraft.client.renderer.block.model.IBakedModel;

import javax.annotation.Nullable;

public interface IBakedQuadValue {
    @Nullable
    BlockVertexData getBlockVertexData(IBakedModel model);

    int hashCode();

    boolean equals(Object o);
}
