package com.aki.modfix.util.fix.extensions;

public interface IPatchedTextureAtlasSpriteModFix {
    void modfix$markNeedsAnimationUpdate();

    boolean modfix$needsAnimationUpdate();

    void modfix$unmarkNeedsAnimationUpdate();
}
