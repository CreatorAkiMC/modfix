package com.aki.modfix.GLSytem;

import com.aki.mcutils.APICore.Utils.render.GlObject;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class GLDynamicIBO extends GlObject {
    private final ChainSectors BaseSector;

    private final Queue<Runnable> Listeners = new ArrayDeque<>();//IniVBOS を実行 -> レンダーを更新

    public GLDynamicIBO() {
        this.setHandle(GL15.glGenBuffers());
        //Chunkごと
        /*this.sectors = new SectorizedList(4096) {
            @Override
            protected void grow(int minContinousSector) {
                int oldSectorCount = this.getSectorCount();
                super.grow(minContinousSector);

                int newVbo = GLHelper.growBuffer(handle(), (long) sectorSize * oldSectorCount,
                        (long) sectorSize * getSectorCount());
                if (newVbo != handle()) {
                    setHandle(newVbo);
                    Listeners.forEach(Runnable::run);
                }
            }
        };*/


        BaseSector = new ChainSectors(integer -> {
            //iboの更新
            if(integer != this.handle()) {
                this.setHandle(integer);
                this.Listeners.forEach(Runnable::run);
            }
        });

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.handle());
        //16**3 = 4096,  24 -> 6 * 4
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0L/*4096 * 24L*/, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
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

    public GLDynamicIBO.IBOPart Buf_Upload(ByteBuffer buffer) {//ここを変えるべき？ ChunkRenderにIDでもふっておくべきかも
        ChainSectors sector = this.BaseSector.getChainSector();
        sector.setUsed(true);
        sector.BufferUpload(this.handle(), buffer);
        return new GLDynamicIBO.IBOPart(sector);
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

    public class IBOPart {
        private boolean valid = true;//free = false
        private final ChainSectors sector;

        public IBOPart(ChainSectors sector) {
            this.sector = sector;
        }

        public int getVBO() {
            return GLDynamicIBO.this.handle();
        }

        //return =  dataLim / DefaultVertexFormats.BLOCK.getSize()
        /*public int getVertexCount() {
            return this.VertexCount;
        }*/

        //バグ？
        public int getVBOFirst() {
            return sector.GetRenderFirst();
        }

        public void free() {
            if (valid) {
                GLDynamicIBO.this.FreeSector(this.sector);
                valid = false;
            }
        }

        public boolean isValid() {
            return valid;
        }
    }
}