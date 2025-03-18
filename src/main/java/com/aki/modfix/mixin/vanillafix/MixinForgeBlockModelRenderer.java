package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ForgeBlockModelRenderer.class, priority = Modfix.ModPriority)
public class MixinForgeBlockModelRenderer {
    /**
     * @reason Adds the textures used to render this block to the set of textures in
     * the CompiledChunk.
     */
    /*@Inject(method = "render", at = @At("HEAD"), remap = false)
    private static void onRender(VertexLighterFlat lighter, IBlockAccess world, IBakedModel model, IBlockState state, BlockPos pos, BufferBuilder wr, boolean checkSides, long rand, CallbackInfoReturnable<Boolean> cir) {
        ChunkRender chunk = ChunkRenderManager.CurrentChunkRender;
        if (chunk != null) {
            Set<TextureAtlasSprite> visibleTextures = chunk.getVisibleTextures();

            for (BakedQuad quad : model.getQuads(state, null, rand)) {
                if (quad.getSprite() != null) {
                    visibleTextures.add(quad.getSprite());
                }
            }

            for (EnumFacing side : EnumFacing.values()) {
                List<BakedQuad> quads = model.getQuads(state, side, rand);
                if (!quads.isEmpty() && !checkSides || state.shouldSideBeRendered(world, pos, side)) {
                    for (BakedQuad quad : quads) {
                        if (quad.getSprite() != null) {
                            visibleTextures.add(quad.getSprite());
                        }
                    }
                }
            }
        } else {
            // Called from non-chunk render thread. Unfortunately, the best we can do
            // is assume it's only going to be used once:
            for (BakedQuad quad : model.getQuads(state, null, rand)) {
                if (quad.getSprite() != null) {
                    ((IPatchedTextureAtlasSpriteModFix) quad.getSprite()).modfix$markNeedsAnimationUpdate();
                }
            }

            for (EnumFacing side : EnumFacing.values()) {
                List<BakedQuad> quads = model.getQuads(state, side, rand);
                if (!quads.isEmpty() && !checkSides || state.shouldSideBeRendered(world, pos, side)) {
                    for (BakedQuad quad : quads) {
                        if (quad.getSprite() != null) {
                            ((IPatchedTextureAtlasSpriteModFix) quad.getSprite()).modfix$markNeedsAnimationUpdate();
                        }
                    }
                }
            }
        }
    }*/
}
