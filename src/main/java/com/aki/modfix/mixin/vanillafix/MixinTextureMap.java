package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import com.aki.modfix.WorldRender.chunk.ChunkRenderManager;
import com.aki.modfix.WorldRender.chunk.openGL.ChunkRender;
import com.aki.modfix.util.fix.extensions.IPatchedTextureAtlasSpriteModFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = TextureMap.class, priority = Modfix.ModPriority)
public abstract class MixinTextureMap extends AbstractTexture {
    @SuppressWarnings("ShadowModifiers" /*(AT)*/) @Shadow @Final private List<TextureAtlasSprite> listAnimatedSprites;

    /**
     * @reason Replaces the updateAnimations method to only tick animated textures
     * that are in one of the loaded RenderChunks. This can lead to an FPS more than
     * three times higher on large modpacks with many textures.
     * <p>
     * Also breaks down the "root.tick.textures" profiler by texture name.
     */
    @Overwrite
    public void updateAnimations() {
        // TODO: Recalculate list after chunk update instead!
        Minecraft.getMinecraft().profiler.startSection("determineVisibleTextures");
        for (ChunkRender chunkRender : ChunkRenderManager.getRenderProvider().getChunkRenders()) {
            for (TextureAtlasSprite texture : chunkRender.getVisibleTextures()) {
                ((IPatchedTextureAtlasSpriteModFix) texture).modfix$markNeedsAnimationUpdate();
            }
        }
        Minecraft.getMinecraft().profiler.endSection();

        GlStateManager.bindTexture(getGlTextureId());
        for (TextureAtlasSprite texture : listAnimatedSprites) {
            if (((IPatchedTextureAtlasSpriteModFix) texture).modfix$needsAnimationUpdate()) {
                Minecraft.getMinecraft().profiler.startSection(texture.getIconName());
                texture.updateAnimation();
                ((IPatchedTextureAtlasSpriteModFix) texture).modfix$unmarkNeedsAnimationUpdate(); // Can't do this from updateAnimation mixin, that method can be overriden
                Minecraft.getMinecraft().profiler.endSection();
            }
        }
    }
}
