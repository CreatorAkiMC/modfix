package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import net.minecraft.client.renderer.BlockFluidRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BlockFluidRenderer.class, priority = Modfix.ModPriority)
public class MixinBlockFluidRenderer {
    /**
     * @reason Adds liquid textures to the set of visible textures in the compiled chunk. Note
     * that this is necessary only for liquid textures, since Forge liquids are rendered by the
     * normal block rendering code.
     */
    /*@ModifyVariable(method = "renderFluid", at = @At(value = "CONSTANT", args = "floatValue=0.001", ordinal = 1), ordinal = 0)
    private TextureAtlasSprite afterTextureDetermined(TextureAtlasSprite texture) {
        ChunkRender chunk = ChunkRenderManager.CurrentChunkRender;
        if (chunk != null) {
            chunk.getVisibleTextures().add(texture);
        } else {
            // Called from non-chunk render thread. Unfortunately, the best we can do
            // is assume it's only going to be used once:
            ((IPatchedTextureAtlasSpriteModFix) texture).modfix$markNeedsAnimationUpdate();
        }

        return texture;
    }*/
}
