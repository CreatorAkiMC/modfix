package com.aki.modfix.mixin.vanillafix;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.extensions.IPatchedTextureAtlasSpriteModFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Render.class, priority = Modfix.ModPriority)
public class MixinRender {
    /**
     * @reason Adds the fire texture to the list of textures used this
     * tick such that it can be animated next tick.
     */
    @Inject(method = "renderEntityOnFire", at = @At("HEAD"))
    private void addFireTextureOnRenderEntityOnFire(Entity entity, double x, double y, double z, float partialTicks, CallbackInfo ci) {
        TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
        ((IPatchedTextureAtlasSpriteModFix) textureMapBlocks.getAtlasSprite("minecraft:blocks/fire_layer_0")).modfix$markNeedsAnimationUpdate();
        ((IPatchedTextureAtlasSpriteModFix) textureMapBlocks.getAtlasSprite("minecraft:blocks/fire_layer_1")).modfix$markNeedsAnimationUpdate();
    }
}
