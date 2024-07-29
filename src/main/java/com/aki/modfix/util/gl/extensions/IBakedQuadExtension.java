package com.aki.modfix.util.gl.extensions;

import net.minecraft.client.renderer.block.model.BakedQuad;

import javax.annotation.Nullable;

public interface IBakedQuadExtension {
    void setOriginalBakedQuad(BakedQuad quad);

    @Nullable
    BakedQuad getOriginalBakedQuad();
}
