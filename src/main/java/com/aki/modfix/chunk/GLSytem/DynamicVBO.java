package com.aki.modfix.chunk.GLSytem;

import org.lwjgl.opengl.GL15;

public class DynamicVBO extends GlObject {
    public DynamicVBO() {
        GL15.glGenBuffers();
    }
}
