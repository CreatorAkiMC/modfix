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

        //this.SectorMax = 16 * (int)Math.pow(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 2 + 1, 2.0d);

        BaseSector = new ChainSectors(integer -> {
            if(integer != this.handle()) {
                this.setHandle(integer);
                this.Listeners.forEach(Runnable::run);
            }
        });

        /*IntStream.range(0, this.SectorMax).forEach(i -> {
            this.FreeChunkSectors.add(new ChunkSector(i, 0));
        });*/

        /*GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.handle());
        //使わない領域がないほうが、メモリを使わない
        //4096(Block 16 * 16 * 16) * DefaultVertexFormats.BLOCK.getSize() で分割して入れていく
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, PerSectionBlock * 16 * (long)Math.pow(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 2 + 1, 2.0d) * (long)DefaultVertexFormats.BLOCK.getSize(), GL15.GL_STREAM_DRAW);//領域の確保 4096(16 * 16 * 16) * 16(Y 16*16=256) (Chunk) * (DistMax * 2 + 1) * (DistMax * 2 + 1) 正方形の一辺
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);*/
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
        int dataLim = buffer.limit();

        /**
         * datas >= PerSectionBlock ? ((datas - Math.floorMod(datas, PerSectionBlock)) / PerSectionBlock) : 0;
         * でやるべき？ でも 16 * 16 * 16 = 4096 * Vertex.size チャンク分だから合わない...
         * */
        int SectionCheck = render.getID();//datas >= PerSectionBlock ? ((datas - Math.floorMod(datas, PerSectionBlock)) / PerSectionBlock) : 0;

        ChainSectors sector = BaseSector.getChainSectorFromIndex(SectionCheck);//getChainSector(); <- LowSpeed ?
        sector.setUsed(true);
        sector.BufferUpload(this.handle(), buffer);

        /*
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.handle());
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, sector.GetRenderFirst(), buffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);*/

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
        private final int VertexCount;

        public VBOPart(ChainSectors sector) {
            this.sector = sector;
            this.VertexCount = this.sector.GetVertexCount();
        }

        public int getVBO() {
            return GlDynamicVBO.this.handle();
        }

        //return =  dataLim / DefaultVertexFormats.BLOCK.getSize()
        public int getVertexCount() {
            return VertexCount;
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
