package com.aki.modfix.GLSytem;

import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GlMutableBuffer extends GlBuffer {
    private final int hints;

    public GlMutableBuffer(int hints) {
        this.hints = hints;
    }

    /**
     * データの保存(格納)
     * 座標や色、ライトマッピングなど
     * */
    @Override
    public void upload(int target, ByteBuffer buf) {
        GL15.glBufferData(target, buf, this.hints);
        this.size = buf.capacity();
    }

    @Override
    public void upload(int target, FloatBuffer buf) {
        GL15.glBufferData(target, buf, this.hints);
        this.size = buf.capacity();
    }

    @Override
    public void allocate(int target, int size) {
        GL15.glBufferData(target, size, this.hints);
        this.size = size;
    }

    public void invalidate(int target) {
        this.allocate(target, 0);
    }
}