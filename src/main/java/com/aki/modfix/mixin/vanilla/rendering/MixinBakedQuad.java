package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.gl.extensions.IBakedQuadExtension;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(value = BakedQuad.class, priority = Modfix.ModPriority)
public class MixinBakedQuad implements IBakedQuadExtension {
    @Unique
    private BakedQuad originalBakedQuad = null;


    @Override
    public void setOriginalBakedQuad(BakedQuad quad) {
        this.originalBakedQuad = quad;
    }

    @Nullable
    @Override
    public BakedQuad getOriginalBakedQuad() {
        return this.originalBakedQuad;
    }
}
