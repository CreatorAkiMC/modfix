package com.aki.modfix.util.gl;

import com.aki.mcutils.APICore.Utils.list.MapCreateHelper;
import com.aki.mcutils.APICore.Utils.list.Pair;
import com.aki.modfix.WorldRender.chunk.openGL.ChunkRender;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// 頂点座標データを保存
public class BlockVertexDatas {
    public ChunkRender chunkRender;
    private int vertexes_id = 0;
    private final HashMap<BakedModelEnumFacing, List<Pair<Integer, Integer>>> VertexData_Indexes = MapCreateHelper.CreateHashMap(BakedModelEnumFacing.values(), i -> new ArrayList<>());
    public BlockVertexDatas(ChunkRender chunk) {
        this.chunkRender = chunk;
    }

    public void addVertexIndex(BakedModelEnumFacing facing, int index) {
        this.VertexData_Indexes.get(facing).add(new Pair<>(vertexes_id++, index));
    }

    //Vertex_id, VertexArraysId
    /*public List<Pair<Integer, Integer>> getIds(BakedModelEnumFacing facing) {
        return this.VertexData_Indexes.get(facing);
    }*/

    public int getSize(BakedModelEnumFacing facing) {
        return this.VertexData_Indexes.get(facing).size();
    }

    //頂点インデックス(このブロック内 0 <= x) と 頂点座標
    //頂点座標の配列に変換。
    public Pair<Integer, Vector3f> getVertex(BakedModelEnumFacing facing, int InIndex) {
        Pair<Integer, Integer> pair = this.VertexData_Indexes.get(facing).get(InIndex);
        return new Pair<>(pair.getKey(), this.chunkRender.getVertexes().get(pair.getValue()));
    }

    @Override
    public String toString() {
        return "BlockVertexDatas{" +
                "vertexes_ids=" + vertexes_id +
                ", VertexData_Indexes=" + VertexData_Indexes +
                '}';
    }
}
