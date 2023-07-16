package com.aki.modfix.chunk.GLSytem;

import com.aki.mcutils.APICore.Utils.matrixutil.MemoryUtil;
import com.aki.mcutils.APICore.Utils.memory.MemoryAccess;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
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
    private boolean mapped = false;

    //容量(size), GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW GL30.GL_MAP_WRITE_BIT
    //GL44 -> true (OK)
    //System.out.println("Command_GL44: " + GLUtils.CAPS.OpenGL44);
    public GlCommandBuffer(long capacity, int flags, int usage, int persistentAccess) {
        this.capacity = capacity;
        //n | GL44.GL_MAP_PERSISTENT_BIT を忘れない
        if(GLUtils.CAPS.OpenGL44) {
            this.bufferIndex = GLUtils.createBuffer(capacity, flags | GL44.GL_MAP_PERSISTENT_BIT, usage);
            this.mapped = true;
            this.setHandle(this.bufferIndex);
            this.buffer = GLUtils.map(this.bufferIndex, this.capacity, persistentAccess | GL44.GL_MAP_PERSISTENT_BIT, 0, null);
            //System.out.println("MEM_CD_BI: " + this.bufferIndex + ", B: " + this.buffer + ", GL45: " + GLUtils.CAPS.OpenGL45);
            this.BaseWriter = MemoryUtil.getAddress(this.buffer);
        } else {
            this.bufferIndex = GLUtils.createBuffer(capacity, flags, usage);
            this.mapped = false;
            this.setHandle(this.bufferIndex);
            this.BaseWriter = 0L;
        }

        //System.out.println("Mem_CD: 2");

        this.stride = 16;
        this.arrayLength = 0;
    }

    /**
     * 作用範囲(いらない？)
     * */
    public void bind(int target) {
        GL15.glBindBuffer(target, this.bufferIndex);
    }

    public void unbind(int target) {
        GL15.glBindBuffer(target, 0);
    }

    public void delete() {
        this.forceUnmap();
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

        /**
         * GL44をサポートしているので使わない
         * https://github.com/Meldexun/RenderLib/blob/v1.12.2-1.3.1/src/main/java/meldexun/renderlib/util/GLBuffer.java
         * */
        this.map(GL30.GL_MAP_WRITE_BIT, GL15.GL_WRITE_ONLY);
        //this.buffer.clear();

        this.MainWriter = this.BaseWriter;
    }

    /**
     * addIndirectDrawCall 後
     * */
    public void end() {
        this.isBuilding = false;

        this.arrayLength = this.count * this.stride;
        this.buffer.limit(this.arrayLength);

        /**
         * GL44をサポートしているので使わない
         * https://github.com/Meldexun/RenderLib/blob/v1.12.2-1.3.1/src/main/java/meldexun/renderlib/util/GLBuffer.java
         * */
        this.unmap();

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

    /**
     * 状態変化
     * GL30.GL_MAP_WRITE_BIT, GL15.GL_WRITE_ONLY
     * */
    public void map(int rangeAccess, int access) {
        if (!mapped && !GLUtils.CAPS.OpenGL44) {
            this.buffer = GLUtils.map(this.bufferIndex, this.capacity, rangeAccess, access, this.buffer);//nullになっている
            this.BaseWriter = MemoryUtil.getAddress(this.buffer);
            mapped = true;
        }
    }

    public boolean isMapped() {
        return mapped;
    }

    public void unmap() {
        forceUnmap();
    }

    private void forceUnmap() {
        if (mapped && !GLUtils.CAPS.OpenGL44) {
            GLUtils.unmap(this.bufferIndex);
            mapped = false;
            this.buffer = null;
            this.BaseWriter = 0L;
            this.MainWriter = 0L;
        }
    }

    public boolean isBuilding() {
        return this.isBuilding;
    }

    public int getArrayLength() {
        return this.arrayLength;
    }

    //Don`t Use
    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getCount() {
        return count;
    }

    /**
     * こっちを入れる
     * */
    public int getBufferIndex() {
        return bufferIndex;
    }
}
