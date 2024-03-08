package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.extensions.IPatchedTextureAtlasSpriteModFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = Modfix.ModPriority)
public class MixinItemRenderer {
    @Shadow @Final private Minecraft mc;

    /**
     * @reason Add the texture of the block the player is holding to the
     * list of visible textures such that it can be animated next tick.
     */
    @Inject(method = "renderSuffocationOverlay", at = @At("HEAD"))
    private void beforeRenderBlockInHand(TextureAtlasSprite texture, CallbackInfo ci) {
        ((IPatchedTextureAtlasSpriteModFix) texture).modfix$markNeedsAnimationUpdate();
    }

    /**
     * @reason Add the fire texture (rendered when player is on fire) to
     * the list of visible textures such that it can be animated next tick
     * if it is being rendered.
     */
    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"))
    private void beforeRenderFireInFirstPerson(CallbackInfo ci) {
        ((IPatchedTextureAtlasSpriteModFix) mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1")).modfix$markNeedsAnimationUpdate();
    }
}
