package com.aki.modfix.WorldRender.chunk.openGL;

public enum RenderEngineType {
    GL15("GL15 Minecraft`s ChunkRenderer"),
    GL20("GL20 ChunkRenderer"),
    GL42("GL42 ChunkRenderer"),
    GL43("GL43 ChunkRenderer"),
    O_GL15("GL15 Minecraft`s ChunkRenderer (Optifine)"),
    O_GL20("GL20 ChunkRenderer (Optifine)"),
    O_GL42("GL42 ChunkRenderer (Optifine)"),
    O_GL43("GL43 ChunkRenderer (Optifine)");

    final String GLName;

    RenderEngineType(String Name) {
        this.GLName = Name;
    }

    public String getGLName() {
        return this.GLName;
    }
}
