package com.aki.modfix.chunk.GLSytem;

import com.aki.mcutils.APICore.Utils.render.GLUtils;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL44;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static com.aki.mcutils.APICore.Utils.matrixutil.UnsafeUtil.UNSAFE;

/**
 * https://github.com/CaffeineMC/sodium-fabric/blob/5af41c180e63590b7797b864393ef584a746eccd/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/multidraw/ChunkDrawCallBatcher.java#L29
 * 参考
 * */
public class GlCommandBuffer extends GlObject {
    private long BaseWriter = 0L;//デフォルト
    private long MainWriter = 0L;
    private int count = 0;//ドローコール数
    private long capacity = 0L;
    public ByteBuffer buffer = null;
    private int bufferIndex = 0;
    private boolean isBuilding = false;
    private int arrayLength;
    private int stride = 0;

    //容量(size), GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW GL30.GL_MAP_WRITE_BIT
    public GlCommandBuffer(long capacity, int flags, int usage, int persistentAccess) {
        this.capacity = capacity;
        this.bufferIndex = GLUtils.createBuffer(capacity, flags, usage);

        this.setHandle(this.bufferIndex);

        int accessRange = persistentAccess | GL44.GL_MAP_PERSISTENT_BIT;

        this.buffer = GLUtils.map(this.bufferIndex, this.capacity, accessRange, 0, null);
        this.MainWriter = this.BaseWriter = MemoryUtil.getAddress(this.buffer);
        this.stride = 16;
        this.arrayLength = 0;
    }

    /**
     * 作用範囲
     * */
    public void bind(int target) {
        GL15.glBindBuffer(target, this.handle());
    }

    /**
     * 作用範囲
     * */
    public void unbind(int target) {
        GL15.glBindBuffer(target, 0);
    }

    public void delete() {
        GL15.glDeleteBuffers(this.handle());//bufferIndex
        this.invalidateHandle();
    }

    /**
     * addIndirectDrawCall 前
     * */
    public void begin() {
        this.isBuilding = true;
        this.count = 0;
        this.arrayLength = 0;

        this.buffer.clear();

        this.MainWriter = this.BaseWriter;
    }

    /**
     * addIndirectDrawCall 後
     * */
    public void end() {
        this.isBuilding = false;

        this.arrayLength = this.count * this.stride;
        this.buffer.limit(this.arrayLength);
    }

    public void addIndirectDrawCall(int first, int count, int baseInstance, int instanceCount) {
        if (this.count++ >= this.capacity) {
            throw new BufferUnderflowException();
        }

        UNSAFE.putInt(this.MainWriter     , count);         // Vertex Count
        UNSAFE.putInt(this.MainWriter +  4, instanceCount); // Instance Count
        UNSAFE.putInt(this.MainWriter +  8, first);         // Vertex Start
        UNSAFE.putInt(this.MainWriter + 12, baseInstance);  // Base Instance

        this.MainWriter += this.stride;//main += 16
    }

    public boolean isBuilding() {
        return this.isBuilding;
    }

    public int getArrayLength() {
        return this.arrayLength;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getCount() {
        return count;
    }

    public int getBufferIndex() {
        return bufferIndex;
    }
}
