package com.aki.modfix.chunk.GLSytem;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public abstract class GlBuffer extends GlObject {
    protected int size;

    protected GlBuffer() {
        //バッファ生成
        this.setHandle(GL15.glGenBuffers());
    }

    public void unbind(int target) {
        GL15.glBindBuffer(target, 0);
    }

    public void bind(int target) {
        GL15.glBindBuffer(target, this.handle());
    }

    public abstract void upload(int target, ByteBuffer buf);

    public abstract void upload(int target, FloatBuffer buf);

    public abstract void allocate(int target, int size);

    /*
    public void upload(int target, VertexData data) {
        this.upload(target, data.buffer);
    }*/

    public void delete() {
        GL15.glDeleteBuffers(this.handle());

        this.invalidateHandle();
        this.size = 0;
    }

    public static void copy(GlBuffer src, GlBuffer dst, int readOffset, int writeOffset, int copyLen, int bufferSize) {
        src.bind(GL31.GL_COPY_READ_BUFFER);

        dst.bind(GL31.GL_COPY_WRITE_BUFFER);
        dst.allocate(GL31.GL_COPY_WRITE_BUFFER, bufferSize);

        GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, copyLen);

        dst.unbind(GL31.GL_COPY_WRITE_BUFFER);
        src.unbind(GL31.GL_COPY_READ_BUFFER);
    }

    public int getSize() {
        return this.size;
    }
}
