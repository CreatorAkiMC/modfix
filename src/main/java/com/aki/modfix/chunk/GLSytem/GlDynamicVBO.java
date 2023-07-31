package com.aki.modfix.chunk.GLSytem;

import com.aki.modfix.util.gl.ChunkSector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class GlDynamicVBO extends GlObject {
    /**
     * free と used で分けて、使っていないもの(読み込まれていないChunkRender)は、freeに入れればよさそう
     * (読み込まれているものしか forループ処理 されないから。)
     */
    //どうやって、全チャンクのすべての16立方体をずれなく設定するか...
    private LinkedList<ChunkSector> ChunkSectors = new LinkedList<>();
    private LinkedList<ChunkSector> FreeChunkSectors = new LinkedList<>();

    private int SectorMax = 0;
    private int PerSectionBlock = 4096;

    public GlDynamicVBO() {
        this.setHandle(GL15.glGenBuffers());

        this.SectorMax = 16 * (int)Math.pow(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 2 + 1, 2.0d);

        IntStream.range(0, this.SectorMax).forEach(i -> {
            this.ChunkSectors.add(null);
            this.FreeChunkSectors.add(new ChunkSector(i));
        });

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.handle());
        //使わない領域がないほうが、メモリを使わない
        //4096(Block 16 * 16 * 16) * DefaultVertexFormats.BLOCK.getSize() で分割して入れていく
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, PerSectionBlock * 16 * (long)Math.pow(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 2 + 1, 2.0d) * (long)DefaultVertexFormats.BLOCK.getSize(), GL15.GL_STREAM_DRAW);//領域の確保 4096(16 * 16 * 16) * 16(Y 16*16=256) (Chunk) * (DistMax * 2 + 1) * (DistMax * 2 + 1) 正方形の一辺
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    /**
     * 下から上に読み込み 0<<<16...
     *  順番はSetup で円形状に増加するのでOK
     * */
    public VBOPart Buf_Upload(ByteBuffer buffer) {
        int dataLim = buffer.limit();
        int datas = dataLim / DefaultVertexFormats.BLOCK.getSize();


        int SectionCheck = datas >= PerSectionBlock ? ((datas - Math.floorMod(datas, PerSectionBlock)) / PerSectionBlock) : 0;
        ChunkSector sector = new ChunkSector(SectionCheck);
        int Index = this.FreeChunkSectors.indexOf(sector);
        if(Index != -1) {
            sector = this.FreeChunkSectors.set(Index, null);
            ChunkSector Dup = this.ChunkSectors.set(Index, sector);
            int NextIndex;
            if(Dup != null) {
                NextIndex = Dup.getChunkOffsetIndex();
                while (this.ChunkSectors.get(NextIndex) != Dup) {
                    Dup = this.ChunkSectors.set(NextIndex, Dup);
                    if(Dup != null) {
                        NextIndex = Dup.getChunkOffsetIndex();
                    }
                }
            }
        } else {
            sector = this.ChunkSectors.get(Index);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.handle());
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, sector.getVBOFirst(), buffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        return new VBOPart(sector, datas);
    }

    public void unbind(int target) {
        GL15.glBindBuffer(target, 0);
    }

    public void bind(int target) {
        GL15.glBindBuffer(target, this.handle());
    }

    public void FreeSector(ChunkSector sector) {
        ChunkSector chunkSector = this.ChunkSectors.set(sector.getChunkOffsetIndex(), null);
        if (chunkSector != null) {
            if(chunkSector == sector) {
                int NewIndex = sector.getChunkOffsetIndex();
                ChunkSector OldSector = this.FreeChunkSectors.set(NewIndex, sector);

                int OldIndex = 0;
                while (OldSector != null) {
                    OldIndex = OldSector.getChunkOffsetIndex();
                    if (OldIndex != NewIndex) {
                        NewIndex = OldIndex;
                        OldSector = this.FreeChunkSectors.set(OldIndex, sector);
                    } else {
                        break;
                    }
                }
            } else {
                int OldChunkIndex = chunkSector.getChunkOffsetIndex();
                while (this.ChunkSectors.get(OldChunkIndex) != chunkSector) {
                    ChunkSector chunkSector1 = this.ChunkSectors.set(OldChunkIndex, chunkSector);
                    if(chunkSector1 != null) {
                        chunkSector = chunkSector1;
                        OldChunkIndex = chunkSector1.getChunkOffsetIndex();
                    } else break;
                }
            }
        }
    }

    public void Delete() {
        this.ChunkSectors.clear();
        this.FreeChunkSectors.clear();
        GL15.glDeleteBuffers(this.handle());
        this.invalidateHandle();
    }

    public class VBOPart {
        private boolean valid = true;//free = false
        private ChunkSector sector = null;
        private int VertexCount = 0;
        private int VBOFirst = 0;

        public VBOPart(ChunkSector sector, int VertexCount) {
            this.sector = sector;
            this.VertexCount = VertexCount;
            this.VBOFirst = sector.getVBOFirst();
        }

        public int getVBO() {
            return GlDynamicVBO.this.handle();
        }

        //return =  dataLim / DefaultVertexFormats.BLOCK.getSize()
        public int getVertexCount() {
            return VertexCount;
        }

        public int getVBOFirst() {
            return VBOFirst;
        }

        public int getSectorIndexOffset() {
            return sector.getChunkOffsetIndex();
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
