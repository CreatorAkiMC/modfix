package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import com.aki.modfix.WorldRender.chunk.ChunkRenderManager;
import com.aki.modfix.WorldRender.chunk.openGL.ChunkRender;
import com.aki.modfix.util.fix.extensions.IPatchedTextureAtlasSpriteModFix;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = BlockFluidRenderer.class, priority = Modfix.ModPriority)
public class MixinBlockFluidRenderer {
    /**
     * @reason Adds liquid textures to the set of visible textures in the compiled chunk. Note
     * that this is necessary only for liquid textures, since Forge liquids are rendered by the
     * normal block rendering code.
     */
    @ModifyVariable(method = "renderFluid", at = @At(value = "CONSTANT", args = "floatValue=0.001", ordinal = 1), ordinal = 0)
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
    }
}
