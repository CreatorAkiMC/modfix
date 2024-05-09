package com.aki.modfix.util.gl;

import com.aki.mcutils.APICore.Utils.render.MapCreateHelper;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// 頂点座標データを保存
public class BlockVertexDatas {
    private final HashMap<BakedModelEnumFacing, List<Vector3f>> VertexPosMap = MapCreateHelper.CreateHashMap(BakedModelEnumFacing.values(), (i) -> new ArrayList<>());

    public BlockVertexDatas() {}

    public void AddVertexPosFromBitInt(BakedModelEnumFacing enumFacing, int x, int y, int z) {
        this.AddVertexPosFromFloat(enumFacing, Float.intBitsToFloat(x), Float.intBitsToFloat(y), Float.intBitsToFloat(z));
    }

    public void AddVertexPosFromFloat(BakedModelEnumFacing enumFacing, float x, float y, float z) {
        this.AddVertexPosFromFloatVec(enumFacing, new Vector3f(x, y, z));
    }

    public void AddVertexPosFromFloatVec(BakedModelEnumFacing enumFacing, Vector3f vector3f) {
        this.VertexPosMap.get(enumFacing).add(vector3f);
    }

    public HashMap<BakedModelEnumFacing, List<Vector3f>> getVertexPosMap() {
        return VertexPosMap;
    }

    @Override
    public String toString() {
        return "BlockVertexDatas{" +
                "VertexPosMap=" + VertexPosMap +
                '}';
    }
}
