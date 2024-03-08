package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.extensions.IPatchedTextureAtlasSpriteModFix;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RenderItem.class, priority = Modfix.ModPriority)
public class MixinRenderItem {
    /**
     * @reason Keep a list of textures that have been used this frame to render an item
     * such that their animation can be updated next tick.
     */
    @Inject(method = "renderQuads", at = @At("HEAD"))
    public void beforeRenderItem(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack, CallbackInfo ci) {
        for (BakedQuad quad : quads) {
            if (quad.getSprite() == null) continue;
            ((IPatchedTextureAtlasSpriteModFix) quad.getSprite()).modfix$markNeedsAnimationUpdate();
        }
    }
}
