package com.aki.modfix.chunk.GLSytem;

import com.aki.modfix.chunk.openGL.ChunkRender;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class GlDynamicVBO extends GlObject {
    /**
     * free と used で分けて、使っていないもの(読み込まれていないChunkRender)は、freeに入れればよさそう
     * (読み込まれているものしか forループ処理 されないから。)
     */
    //private int SectorMax = 0;
    private final int PerSectionBlock = 4096;
    private ChainSectors BaseSector = null;

    private final Queue<Runnable> Listeners = new ArrayDeque<>();//IniVBOS を実行 -> レンダーを更新

    public GlDynamicVBO() {
        this.setHandle(GL15.glGenBuffers());

        BaseSector = new ChainSectors(integer -> {
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
     * */

    public VBOPart Buf_Upload(ChunkRender render, ByteBuffer buffer) {//ここを変えるべき？ ChunkRenderにIDでもふっておくべきかも
        int SectionCheck = render.getID();

        /*
         * getChainSectorFromIndex にすると重複する可能性あり
         * */
        ChainSectors sector = BaseSector.getChainSector(SectionCheck);
        sector.setUsed(true);
        sector.BufferUpload(this.handle(), buffer);

        return new VBOPart(sector);
    }

    public void FreeSector(ChainSectors sector) {
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
        private final ChainSectors sector;

        public VBOPart(ChainSectors sector) {
            this.sector = sector;
        }

        public int getVBO() {
            return GlDynamicVBO.this.handle();
        }

        //return =  dataLim / DefaultVertexFormats.BLOCK.getSize()
        public int getVertexCount() {
            return this.sector.GetVertexCount();
        }

        //バグってる？
        public int getVBOFirst() {
            return (int)sector.GetRenderFirst();
        }

        public int getSectorIndex() {
            return sector.getIndex();
        }

        public long getSectorByteBufferOffset() {
            return sector.GetIndexBufferOffset();
        }

        public void free() {
            if(valid) {
                GlDynamicVBO.this.FreeSector(this.sector);
                valid = false;
            }
        }

        public boolean isValid() {
            return valid;
        }
    }
}
