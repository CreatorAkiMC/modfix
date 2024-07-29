/*package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.mcutils.asm.Optifine;
import com.aki.modfix.Modfix;
import com.aki.modfix.util.gl.extensions.IBakedQuadExtension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.optifine.render.RenderEnv;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.optifine.model.BlockModelCustomizer", priority = Modfix.ModPriority, remap = false)
public abstract class MixinBlockModelCustomizer {
    @Inject(method = "getRenderQuads(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;JLnet/optifine/render/RenderEnv;)[Lnet/minecraft/client/renderer/block/model/BakedQuad;", remap = false, at = @At("HEAD"), cancellable = true)
    private static void Override_NewNaturalTextureCode(BakedQuad quad, IBlockAccess worldIn, IBlockState stateIn, BlockPos posIn, EnumFacing enumfacing, long rand, RenderEnv renderEnv, CallbackInfoReturnable<BakedQuad[]> cir) {
        if (Optifine.isBreakingAnimation(renderEnv, quad)) {
            cir.setReturnValue(Optifine.getArrayQuadsCtm(renderEnv, quad));

        } else {
            BakedQuad quadOriginal = quad;
            if (Optifine.isConnectedTextures()) {
                BakedQuad[] quads = Optifine.getConnectedTextures(worldIn, stateIn, posIn, quad, renderEnv);
                if (quads.length != 1 || quads[0] != quad) {
                    cir.setReturnValue(quads);
                }
            }

            if (Optifine.isNaturalTextures()) {
                BakedQuad original = quad;
                quad = Optifine.getNaturalTexture(posIn, quad);
                if (quad != quadOriginal) {
                    ((IBakedQuadExtension)quad).setOriginalBakedQuad(original);
                    cir.setReturnValue(Optifine.getArrayQuadsCtm(renderEnv, quad));
                }
            }

            cir.setReturnValue(Optifine.getArrayQuadsCtm(renderEnv, quad));
        }
    }
}
*/