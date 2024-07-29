package com.aki.modfix.util.gl.quadList;

import com.aki.modfix.util.gl.BlockVertexData;
import net.minecraft.client.renderer.block.model.IBakedModel;

import java.util.HashMap;
import java.util.Objects;

public class WeightedBakedQuadValue implements IBakedQuadValue {
    private final HashMap<IBakedModel, BlockVertexData> modelToVertexData = new HashMap<>();

    public WeightedBakedQuadValue() {}

    @Override
    public BlockVertexData getBlockVertexData(IBakedModel model) {
        return this.modelToVertexData.get(model);
    }

    public void AddModelToVertexData(IBakedModel model, BlockVertexData data) {
        this.modelToVertexData.put(model, data);
    }

    public boolean IsBakedModel(IBakedModel model) {
        return this.modelToVertexData.containsKey(model);
    }

    //this.model だけで判断
    @Override
    public int hashCode() {
        return Objects.hashCode(this.modelToVertexData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeightedBakedQuadValue)) return false;
        WeightedBakedQuadValue that = (WeightedBakedQuadValue) o;
        return Objects.equals(this.modelToVertexData, that.modelToVertexData);
    }
}
