package com.aki.modfix.chunk.GLSytem;

import com.aki.modfix.util.gl.GLHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

//The ChainSectors0 in` ChainSectors1 in` ChainSectors2 in` ChainSectors3 in` ChainSectors4 in`.... ChainSectors[N].
//無限に続けることができ、高速
//Infinity and Very Fastest.
public class ChainSectors {
    private ChainSectors PrevChain = null;
    private ChainSectors NextChain = null;

    private int ID_Index = 0;//これを基に生成やforeachを行うので大事

    private boolean IsUsed = false;

    private int NextChainCounts = 0;

    private int PrevChainCounts = 0;

    private ByteBuffer buffer = null;

    private int BufferFirst = 0;

    //BufferData の自分の位置を表す。
    private int BufferOffset = 0;

    //もし、後ろに新しく要素が追加された場合 [this.BufferOffset + this.FromOffset] で [0 + x より後ろのOffset + 自分より後ろのOffset]にするため。
    private int FromOffset = 0;

    private Consumer<Integer> VBOUpdate = null;

    public ChainSectors(Consumer<Integer> VBOUpdateConsumer) {
        this.VBOUpdate = VBOUpdateConsumer;
        this.ID_Index = 0;
        this.PrevChain = null;
        this.NextChain = null;
    }

    private ChainSectors(Consumer<Integer> VBOUpdateConsumer, ChainSectors prevChain, ChainSectors nextChain, int Offset, int id_index) {
        this.VBOUpdate = VBOUpdateConsumer;
        this.PrevChain = prevChain;
        this.NextChain = nextChain;
        this.ID_Index = id_index;
        this.BufferOffset = Offset;
    }

    private synchronized ChainSectors CallAndCreate(int ID) {
        if(ID < 0)
            throw new IllegalArgumentException("ChainSectors Under 0 Error, ID: " + ID);
        if(this.ID_Index == ID)
            return this;

        if(ID > this.ID_Index) {//next
            if(this.NextChain == null)
                this.NextChain = new ChainSectors(this.VBOUpdate, this, null, this.BufferOffset + this.FromOffset, ID_Index + 1);
            this.NextChainCounts = Math.max((ID - this.ID_Index), this.NextChainCounts);
            return this.NextChain.CallAndCreate(ID);
        } else {//Prev
            if(this.PrevChain == null)
                this.PrevChain = new ChainSectors(this.VBOUpdate, null, this, 0, ID_Index - 1);
            this.PrevChainCounts = Math.max((this.ID_Index - ID), this.PrevChainCounts);
            return this.PrevChain.CallAndCreate(ID);
        }
    }

    private int GetToIDBufferSize(int TargetIndex) {
        if(TargetIndex < 0)
            throw new IllegalArgumentException("ChainSectors Under 0 Error, ID: " + TargetIndex);

        int Sizes = this.buffer != null ? this.buffer.limit() : 0;

        System.out.println(" BufferSizeIndex: " + this.ID_Index + ", Size: " + Sizes);

        if(this.ID_Index == TargetIndex)
            return Sizes;



        if(TargetIndex > this.ID_Index) {//next
            if(this.NextChain != null) {
                return Sizes + this.CallAndCreate(this.ID_Index + 1).GetToIDBufferSize(TargetIndex);
            } else return Sizes;
        } else {//Prev
            if(this.PrevChain != null) {
                return Sizes + this.CallAndCreate(this.ID_Index - 1).GetToIDBufferSize(TargetIndex);
            } else return Sizes;
        }
    }

    public ChainSectors getChainSector(int RequireSectorIndex) {
        if(RequireSectorIndex == this.ID_Index && !this.IsUsed)
            return this;
        ChainSectors chainSectors = null;
        boolean MinusError = false;
        while (chainSectors == null || chainSectors.IsUsed) {
            int Index = (chainSectors == null) ? RequireSectorIndex : (this.getIndex() < RequireSectorIndex ? chainSectors.ID_Index + 1 : chainSectors.ID_Index - 1);
            if(Index < 0)
                MinusError = true;
            if(chainSectors != null && MinusError)
                Index = chainSectors.ID_Index + 1;
            chainSectors = CallAndCreate(Index);
        }

        return chainSectors;
    }

    public void ExecuteToIndexChainSector(Consumer<ChainSectors> consumer, int TargetIndex) {
        if(TargetIndex < 0)
            throw new IllegalArgumentException("ChainSectors Under 0 Error, ID: " + TargetIndex);
        if(this.ID_Index == TargetIndex) {
            consumer.accept(this);
            return;
        }
        if(TargetIndex > this.ID_Index) {
            ChainSectors NextSector = this.CallAndCreate(this.ID_Index + 1);
            consumer.accept(NextSector);
            if(TargetIndex != (this.ID_Index + 1))
                NextSector.ExecuteToIndexChainSector(consumer, TargetIndex);
        } else {
            ChainSectors PrevSector = this.CallAndCreate(this.ID_Index - 1);
            consumer.accept(PrevSector);
            if(TargetIndex != (this.ID_Index - 1))
                PrevSector.ExecuteToIndexChainSector(consumer, TargetIndex);
        }
    }

    public ChainSectors getChainSector() {
        if(!this.IsUsed)
            return this;
        ChainSectors chainSectors = null;
        while (chainSectors == null || chainSectors.IsUsed) {
            chainSectors = CallAndCreate(chainSectors == null ? 0 : chainSectors.ID_Index + 1);
        }
        return chainSectors;
    }

    public ChainSectors getChainSectorFromIndex(int RequireSectorIndex) {
        if(RequireSectorIndex == this.ID_Index)
            return this;
        return CallAndCreate(RequireSectorIndex);
    }

    //増減したときはBufferOffsetが変動
    public void BufferUpload(int VBOID, ByteBuffer UploadBuffer) {
        boolean OldAtBuffer = this.buffer != null;//Bufferが元から存在したか。
        int OldSize = OldAtBuffer ? this.buffer.limit() : 0;
        int NextByteSize = this.GetToIDBufferSize(this.ID_Index + this.NextChainCounts) - (OldAtBuffer ? (this.buffer.limit()) : 0);
        int PrevByteSize = this.GetToIDBufferSize(0);//自分自身を含めた
        this.buffer = UploadBuffer;
        int size = this.buffer.limit();
        int UpdateOffset = (size - OldSize);//後続のChainを更新(足していく)

        this.FromOffset += UpdateOffset;

        //AtomicInteger atomicOldByteSize = new AtomicInteger();
        //atomicOldByteSize.set(PrevByteSize);//Prev`s Size

        if(this.NextChainCounts > 0)
         this.ExecuteToIndexChainSector(chainSectors -> {
             chainSectors.BufferOffset += UpdateOffset;
             chainSectors.BufferFirst = chainSectors.BufferOffset;/* + atomicOldByteSize.getAndAdd(chainSectors.buffer != null ? chainSectors.buffer.limit() : 0);*/

             System.out.println("NextChainUpdate ID: " + chainSectors.ID_Index + ", Offset: " + chainSectors.BufferOffset + ", Update: " + UpdateOffset + ", PrevCounts: " + chainSectors.PrevChainCounts + ", NextCount: " + chainSectors.NextChainCounts);

             }, this.ID_Index + this.NextChainCounts);

        int NewVBO = VBOID;
        /**
         * コピーする位置が間違っているかも.
         *
         * InitVBOs の読み込みタイミングが間違っている？
         * */
        //glMapBuffer と glGetBufferSubDataなどで作った方が安全かも？
        if(UpdateOffset != 0)
            this.VBOUpdate.accept((NewVBO = GLHelper.CopyMoveBuffer(VBOID, NextByteSize + PrevByteSize, PrevByteSize, PrevByteSize + UpdateOffset, this.buffer)));
        //PrevByteSize も、this.BufferOffset も変わらない？
        //コンソールだと、[PrevByteSize < this.BufferOffset]という関係に時間が経つとなる。
        //Index: 0 の時は、PrevBytes だと自分以前保持していたBuffer.limit が加算されて Firstが0なのに674のなったりする。
        this.BufferFirst = this.BufferOffset;// + this.BufferOffset;

        System.out.println("Index: " + this.ID_Index + ", First: " + this.BufferFirst + ", Buffer: " + size + ", OldBuffer: " + OldSize + ", NextByte: " + NextByteSize + ", PrevByte: " + PrevByteSize + ", UpdateOffset: " + UpdateOffset + ", BO: " + this.BufferOffset + ", From Offset: " + this.FromOffset);

        this.UpdateBuffers(NewVBO);
        /*GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, NewVBO);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, this.BufferFirst, UploadBuffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);*/
    }

    public void Free(int VBOID) {
        this.setUsed(false);
        boolean OldAtBuffer = this.buffer != null;//Bufferが元から存在したか。
        if(OldAtBuffer) {
            int NextByteSize = this.GetToIDBufferSize(this.ID_Index + this.NextChainCounts) - (this.buffer.limit());
            int PrevByteSize = this.GetToIDBufferSize(0);//自分自身を含めた
            if (this.NextChainCounts > 0)
                this.ExecuteToIndexChainSector(chainSectors -> {
                    chainSectors.BufferOffset -= this.FromOffset;
                    chainSectors.BufferFirst -= this.FromOffset;

                    System.out.println("NextChainRemoveUpdate ID: " + chainSectors.ID_Index + ", Offset: " + chainSectors.BufferOffset + ", Update: " + FromOffset + ", PrevCounts: " + chainSectors.PrevChainCounts + ", NextCount: " + chainSectors.NextChainCounts);

                }, this.ID_Index + this.NextChainCounts);

            System.out.println("FreeBuffer Index: " + this.ID_Index + ", BufferSize: " + this.buffer.limit() + ", NextBytes: " + NextByteSize + ", PrevBytes: " + PrevByteSize + ", NextCounts: " + this.NextChainCounts + ", PrevCounts: " + this.PrevChainCounts);

            this.VBOUpdate.accept(GLHelper.CopyMoveBuffer(VBOID, NextByteSize + PrevByteSize, PrevByteSize, PrevByteSize - this.buffer.limit(), null));
            this.BufferFirst = -1;
            this.FromOffset = 0;
            this.buffer = null;
        }
    }

    private void UpdateBuffers(int VBOID) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBOID);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, this.BufferFirst, this.buffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public int getSize() {
        return this.NextChainCounts + this.PrevChainCounts + 1;//Next + Prev + Me(1)
    }

    public boolean isUsed() {
        return this.IsUsed;
    }

    public void setUsed(boolean used) {
        this.IsUsed = used;
    }

    public int getIndex() {
        return this.ID_Index;
    }

    public int GetByteSize() {
        return this.buffer != null ? this.buffer.limit() : 0;
    }

    //(ID_Index)(Prev) * 4096(16^3)
    /**0 ~ Index * 4096 */
    public int GetVertexCount() {
        return this.buffer != null ? (this.buffer.limit() / DefaultVertexFormats.BLOCK.getSize()) : 0;//this.getIndex() * 4096;
    }

    public long GetIndexBufferOffset() {
        return this.BufferOffset;
    }

    /**
     * IsUsed = false の時は Return 0
     * */
    public long GetRenderFirst() {
        return this.BufferFirst;//this.GetPMBlocks() * DefaultVertexFormats.BLOCK.getSize();
    }
}
