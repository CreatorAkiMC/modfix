package com.aki.modfix.chunk.GLSytem;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class GLMutableArrayBuffer extends GlObject {
    public GLMutableArrayBuffer() {
        this.setHandle(GL30.glGenVertexArrays());
    }

    public void bind() {
        GL30.glBindVertexArray(this.handle());
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void delete() {
        unbind();
        GL30.glDeleteVertexArrays(this.handle());
        this.invalidateHandle();
    }
}
