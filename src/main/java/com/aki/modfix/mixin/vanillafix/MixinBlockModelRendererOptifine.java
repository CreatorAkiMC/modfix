package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import net.minecraft.client.renderer.BlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BlockModelRenderer.class, priority = Modfix.ModPriority)
public class MixinBlockModelRendererOptifine {
    /*@Inject(method = "renderQuadsSmooth", remap = false, at = @At("HEAD"))
    private void onRenderQuadsSmooth(IBlockAccess blockAccess, IBlockState state, BlockPos pos, BufferBuilder buffer, List<BakedQuad> quads, RenderEnv renderEnv, CallbackInfo ci) {
        markQuads(quads);
    }

    @Inject(method = "renderQuadsFlat", remap = false, at = @At("HEAD"))
    private void onRenderQuadsFlat(IBlockAccess blockAccess, IBlockState state, BlockPos pos, int brightness, boolean ownBrightness, BufferBuilder buffer, List<BakedQuad> quads, RenderEnv renderEnv, CallbackInfo ci) {
        markQuads(quads);
    }

    private static void markQuads(List<BakedQuad> quads) {
        ChunkRender chunk = ChunkRenderManager.CurrentChunkRender;
        if (chunk != null) {
            Set<TextureAtlasSprite> visibleTextures = chunk.getVisibleTextures();

            for (BakedQuad quad : quads) {
                if (quad.getSprite() != null) {
                    visibleTextures.add(quad.getSprite());
                }
            }
        } else {
            // Called from non-chunk render thread. Unfortunately, the best we can do
            // is assume it's only going to be used once:
            for (BakedQuad quad : quads) {
                if (quad.getSprite() != null) {
                    ((IPatchedTextureAtlasSpriteModFix) quad.getSprite()).modfix$markNeedsAnimationUpdate();
                }
            }
        }
    }*/
}
