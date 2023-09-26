package com.aki.modfix.chunk.GLSytem;

import com.aki.mcutils.APICore.Utils.matrixutil.MemoryUtil;
import com.aki.mcutils.APICore.Utils.matrixutil.UnsafeUtil;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;
import sun.misc.Unsafe;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

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
    private int stride = 0;
    private boolean mapped = false;
    private final boolean persistent;

    public GlCommandBuffer(long capacity, int flags, int usage, int persistentAccess) {
        this.capacity = capacity;
        //n | GL44.GL_MAP_PERSISTENT_BIT を忘れない
        if(GLUtils.CAPS.OpenGL44) {
            this.bufferIndex = GLUtils.createBuffer(capacity, flags | GL44.GL_MAP_PERSISTENT_BIT, usage);
            this.mapped = true;
            this.setHandle(this.bufferIndex);
            this.persistent = true;
            this.buffer = GLUtils.map(this.bufferIndex, this.capacity, persistentAccess | GL44.GL_MAP_PERSISTENT_BIT, 0, null);
            this.BaseWriter = MemoryUtil.getAddress(this.buffer);
        } else {
            this.bufferIndex = GLUtils.createBuffer(capacity, flags, usage);
            this.mapped = false;
            this.setHandle(this.bufferIndex);
            this.BaseWriter = 0L;
            this.persistent = false;
        }
        this.stride = 16;
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
        this.count = 0;
        this.ResetWriter();

        /**
         * GL44をサポートしているので使わない
         * https://github.com/Meldexun/RenderLib/blob/v1.12.2-1.3.1/src/main/java/meldexun/renderlib/util/GLBuffer.java
         * */
        this.map(GL30.GL_MAP_WRITE_BIT, GL15.GL_WRITE_ONLY);
        //this.buffer.clear();

    }

    public void ResetWriter() {
        this.MainWriter = this.BaseWriter;
    }

    /**
     * addIndirectDrawCall 後
     * */
    public void end() {
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

        Unsafe Un_Safe = UnsafeUtil.UNSAFE;

        Un_Safe.putInt(this.MainWriter     , count);         // Vertex Count
        Un_Safe.putInt(this.MainWriter +  4, instanceCount); // Instance Count
        Un_Safe.putInt(this.MainWriter +  8, first);         // Vertex Start
        Un_Safe.putInt(this.MainWriter + 12, baseInstance);  // Base Instance

        this.MainWriter += this.stride;//main += 16
    }

    /**
     * 状態変化
     * GL30.GL_MAP_WRITE_BIT, GL15.GL_WRITE_ONLY
     * */
    public void map(int rangeAccess, int access) {
        if (!mapped && !this.persistent) {
            this.buffer = GLUtils.map(this.bufferIndex, this.capacity, rangeAccess, access, this.buffer);//nullになっている
            this.BaseWriter = MemoryUtil.getAddress(this.buffer);
            this.MainWriter = this.BaseWriter;
            mapped = true;
        }
    }

    public boolean isMapped() {
        return mapped;
    }

    public void unmap() {
        if(!this.persistent)
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

    //Don`t Use
    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getCount() {
        System.out.println("Count: " + count);
        return count;
    }

    /**
     * こっちを入れる
     * */
    public int getBufferIndex() {
        return bufferIndex;
    }
}
