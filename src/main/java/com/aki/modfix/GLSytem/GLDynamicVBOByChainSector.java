package com.aki.modfix.GLSytem;

/*
 * Thank you Meldexum
 * */

import com.aki.mcutils.APICore.Utils.render.GlObject;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * うまく動きません
 * */
public class GLDynamicVBOByChainSector extends GlObject {
    private GLChainSectors BaseSector = null;

    /*
    * 4096 -> 1chunkのブロック
    * 28 -> 1つの頂点のデータ数
    *
    * */

    //1つの頂点の大きさ
    //28
    private final int vertexSize = DefaultVertexFormats.BLOCK.getSize();

    //
    //private final int vertexCountPerSector = 128;
    //private final int sectorSize = vertexCountPerSector * vertexSize;
    //private final SectorizedList sectors;

    private final Queue<Runnable> Listeners = new ArrayDeque<>();//IniVBOS を実行 -> レンダーを更新

    public GLDynamicVBOByChainSector() {
        this.setHandle(GL15.glGenBuffers());
        /*this.sectors = new SectorizedList(4096) {
            @Override
            protected void grow(int minContinousSector) {
                int oldSectorCount = this.getSectorCount();
                super.grow(minContinousSector);

                int newVbo = GLHelper.growBuffer(GL15.GL_ARRAY_BUFFER, handle(), (long) sectorSize * oldSectorCount,
                        (long) sectorSize * this.getSectorCount());
                if (newVbo != handle()) {
                    setHandle(newVbo);
                    Listeners.forEach(Runnable::run);
                }
            }
        };*/
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.handle());
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 0, GL15.GL_STREAM_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        BaseSector = new GLChainSectors(GL15.GL_ARRAY_BUFFER, integer -> {
            if(integer != this.handle()) {
                this.setHandle(integer);
                this.Listeners.forEach(Runnable::run);
            }
        });
    }

    public void AddListener(Runnable runnable) {
        this.Listeners.add(runnable);
    }

    /**
     * Free -> 抜けると Backを +1して index を補完(詰めて, サイズが減少)
     * Use    ->  新しく入るとIndexに格納して、後のものはIndexをずらして Back を -1 (サイズ増加)
     * (0)0, (2)1, (3)2, (4)3, (5)4.... (消滅)消滅-1 * SectorMax
     * (0)0, (2)1, (4)2, (5)3, (6)4.... (消滅)消滅-1 * SectorMax
     * オリジナル(X)と補完用Index()Xなどで位置を特定
     */

    public VBOPart Buf_Upload(ByteBuffer buffer) {//ここを変えるべき？ ChunkRenderにIDでもふっておくべきかも
        /*int size = buffer.limit();
        int requiredSectors = MathUtil.ceilDiv(size, this.sectorSize);
        if (requiredSectors <= 0) {
            throw new IllegalArgumentException();
        }
        SectorizedList.Sector sector = this.sectors.claim(requiredSectors);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, handle());
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) sectorSize * sector.getFirstSector(), buffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return new VBOPart(sector, size / this.vertexSize);*/
        GLChainSectors sector = this.BaseSector.getChainSector();
        sector.setUsed(true);
        sector.BufferUpload(this.handle(), buffer);
        return new VBOPart(sector);
    }

    public void FreeSector(GLChainSectors sector) {
        sector.Free(this.handle());
    }


    public void unbind(int target) {
        GL15.glBindBuffer(target, 0);
    }

    public void bind(int target) {
        GL15.glBindBuffer(target, this.handle());
    }

    public void Delete() {
        GL15.glDeleteBuffers(this.handle());
        this.invalidateHandle();
    }

    public class VBOPart {
        private boolean valid = true;//free = false
        private final GLChainSectors sector;

        public VBOPart(GLChainSectors sector) {
            this.sector = sector;
        }

        public int getVBO() {
            return GLDynamicVBOByChainSector.this.handle();
        }

        //return =  dataLim / DefaultVertexFormats.BLOCK.getSize()
        public int getVertexCount() {
            return this.sector.GetByteSize() / GLDynamicVBOByChainSector.this.vertexSize;
        }

        //バグ？
        public int getVBOFirst() {
            return sector.GetRenderFirst();
        }

        public void free() {
            if (valid) {
                GLDynamicVBOByChainSector.this.FreeSector(this.sector);
                valid = false;
            }
        }

        public boolean isValid() {
            return valid;
        }
    }
}
