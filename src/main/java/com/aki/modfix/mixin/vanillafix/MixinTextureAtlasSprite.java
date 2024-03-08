package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.extensions.IPatchedTextureAtlasSpriteModFix;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TextureAtlasSprite.class, priority = Modfix.ModPriority)
public class MixinTextureAtlasSprite implements IPatchedTextureAtlasSpriteModFix {
    @Unique
    private boolean modfix$needsAnimationUpdate = false;

    @Override
    public void modfix$markNeedsAnimationUpdate() {
        modfix$needsAnimationUpdate = true;
    }

    @Override
    public boolean modfix$needsAnimationUpdate() {
        return modfix$needsAnimationUpdate;
    }

    @Override
    public void modfix$unmarkNeedsAnimationUpdate() {
        modfix$needsAnimationUpdate = false;
    }
}
