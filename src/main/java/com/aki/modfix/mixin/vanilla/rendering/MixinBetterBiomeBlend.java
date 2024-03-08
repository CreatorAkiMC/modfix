package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.modfix.WorldRender.chunk.openGL.GLChunkRenderCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "fionathemortal.betterbiomeblend.BiomeColor", remap = false)
public class MixinBetterBiomeBlend {

    @Inject(method = "getWorldFromBlockAccess", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
    private static void getWorldFromBlockAccess(IBlockAccess blockAccess, CallbackInfoReturnable<World> info) {
        if (blockAccess instanceof GLChunkRenderCache) {
            info.setReturnValue(((GLChunkRenderCache) blockAccess).getWorld());
        }
    }

}