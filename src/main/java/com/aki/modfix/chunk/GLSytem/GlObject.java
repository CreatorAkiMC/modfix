package com.aki.modfix.chunk.GLSytem;

public class GlObject {
    private static final int INVALID_HANDLE = Integer.MIN_VALUE;

    private int handle = INVALID_HANDLE;

    protected final void setHandle(int handle) {
        this.handle = handle;
    }

    public final int handle() {
        this.checkHandle();

        return this.handle;
    }

    public final int GetDebugHandle() {
        return this.handle;
    }

    protected final void checkHandle() {
        if (!this.isHandleValid()) {
            throw new IllegalStateException("Handle is not valid");
        }
    }

    protected final boolean isHandleValid() {
        return this.handle != INVALID_HANDLE;
    }

    protected final void invalidateHandle() {
        this.handle = INVALID_HANDLE;
    }
}