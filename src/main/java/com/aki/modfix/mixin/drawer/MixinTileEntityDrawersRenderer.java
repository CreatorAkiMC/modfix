package com.aki.modfix.mixin.drawer;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.client.renderer.TileEntityDrawersRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityDrawersRenderer.class)
public class MixinTileEntityDrawersRenderer {
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void render(TileEntityDrawers tile, double x, double y, double z, float partialTickTime, int destroyStage, float par7, CallbackInfo ci)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;
        BlockPos blockPos = tile.getPos();

        if (player != null && player.getPosition().distanceSq(blockPos) > 15 * 15)
        {
            ci.cancel();
        }
    }
}
