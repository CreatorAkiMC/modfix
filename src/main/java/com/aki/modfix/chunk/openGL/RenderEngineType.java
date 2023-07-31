package com.aki.modfix.chunk.openGL;

public enum RenderEngineType {
    GL15("GL15 Minecraft`s ChunkRenderer"),
    GL20("GL20 ChunkRenderer"),
    GL42("GL42 ChunkRenderer"),
    GL43("GL43 ChunkRenderer");

    final String GLName;

    RenderEngineType(String Name) {
        this.GLName = Name;
    }

    public String getGLName() {
        return this.GLName;
    }
}
