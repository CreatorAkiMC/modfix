package com.aki.modfix.util.gl;

import com.aki.mcutils.APICore.Utils.list.MapCreateHelper;
import com.aki.mcutils.APICore.Utils.list.Pair;
import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.modfix.WorldRender.chunk.openGL.ChunkRender;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// 頂点座標データを保存
public class BlockVertexDatas {
    public final ChunkRender chunkRender;
    private final ChunkRenderPass renderPass;
    private int vertexes_id = 0;

    //重複を 1 とした頂点の数 -> 頂点インデックスを 1 から始めたものと等しい
    //private int BaseVertex = 0;
    // Integer 頂点インデックス、Integer 頂点の座標配列インデックス。
    private final HashMap<BakedModelEnumFacing, List<Pair<Integer, Integer>>> VertexData_Indexes = MapCreateHelper.CreateHashMap(BakedModelEnumFacing.values(), i -> new ArrayList<>());
    public BlockVertexDatas(ChunkRender chunk, ChunkRenderPass pass) {
        this.chunkRender = chunk;
        this.renderPass = pass;
    }

    //重複する頂点は小さいほうの vertexes_id を合わせる。-> 頂点をスキップするようなもの
    public void addVertexIndex(BakedModelEnumFacing facing, int index) {
        //boolean contained = false;
        int insertId = this.vertexes_id;
        end:for(BakedModelEnumFacing model_facing : BakedModelEnumFacing.values()) {
            List<Pair<Integer, Integer>> vertexes = this.VertexData_Indexes.get(model_facing);
            for(Pair<Integer, Integer> pair : vertexes) {
                if(pair.getValue() == index) {
                    //先にあるほうに合わせる。
                    insertId = pair.getKey();
                    //contained = true;
                    break end;
                }
            }
        }
        if(insertId == this.vertexes_id) {
            //重複を 1 とした頂点の数 -> 頂点インデックスを 1 から始めたものと等しい
            //this.BaseVertex += 1;
            this.chunkRender.addBaseVertex(this.renderPass, 1);
        }
        this.VertexData_Indexes.get(facing).add(new Pair<>(insertId, index));
        this.vertexes_id++;
    }

    //vertexes_id == 頂点の数といえる
    public int getVertexCount() {
        return this.vertexes_id;
    }

    //Vertex_id, VertexArraysId
    /*public List<Pair<Integer, Integer>> getIds(BakedModelEnumFacing facing) {
        return this.VertexData_Indexes.get(facing);
    }*/

    public int getSize(BakedModelEnumFacing facing) {
        return this.VertexData_Indexes.get(facing).size();
    }

    /*public int getBaseVertex() {
        return this.BaseVertex;
    }*/

    //頂点インデックス(このブロック内 0 <= x) と 頂点座標
    //頂点座標の配列に変換。
    public Pair<Integer, Vector3f> getVertex(BakedModelEnumFacing facing, int InIndex) {
        Pair<Integer, Integer> pair = this.VertexData_Indexes.get(facing).get(InIndex);
        return new Pair<>(pair.getKey(), this.chunkRender.getVertexes().get(pair.getValue()));
    }

    @Override
    public String toString() {
        return "BlockVertexDatas{" +
                "vertexes_ids=" + this.vertexes_id +
                ", VertexData_Indexes=" + this.VertexData_Indexes +
                '}';
    }
}
