package com.aki.modfix.chunk.openGL;

import java.util.function.Supplier;

public abstract class ChunkRenderTaskBase<T extends ChunkRender> implements Supplier<ChunkRenderTaskResult> {
    public ChunkRendererBase<T> renderer = null;
    public ChunkGLDispatcher dispatcher = null;
    public ChunkRender chunkRender = null;
    private volatile boolean Cancel = false;

    public ChunkRenderTaskBase(ChunkRendererBase<T> renderer, ChunkGLDispatcher dispatcher, T chunkRender) {
        this.renderer = renderer;
        this.dispatcher = dispatcher;
        this.chunkRender = chunkRender;
    }

    @Override
    public ChunkRenderTaskResult get() {
        return this.run();
    }

    public abstract ChunkRenderTaskResult run();

    //End
    public void SetCancelState(boolean Cancel) {
        this.Cancel = Cancel;
    }

    public boolean getCancel() {
        return this.Cancel;
    }
}
