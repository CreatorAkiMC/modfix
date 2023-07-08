package com.aki.modfix.mixin.gvc;

import gvcr2.block.tile.TileEntity_GVC_DecoFacingBlock;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = TileEntity_GVC_DecoFacingBlock.class, remap = false)
public class MixinGvcDecoFacing extends TileEntity {
    @Shadow(remap = false) public int facing;

    @Shadow(remap = false) public int id;

    @Inject(method = "update", at = @At("HEAD"), remap = false, cancellable = true, require = 1)
    public void updateFix(CallbackInfo ci) {
        if (!world.isRemote) {
            Block block = this.world.getBlockState(this.pos).getBlock();

            int x = this.pos.getX();
            int y= this.pos.getY();
            int z = this.pos.getZ();
            if(this.world.getBlockState(pos).getBlock() instanceof gvcr2.block.Block_GVC_DecoFacingBlock) {
                gvcr2.block.Block_GVC_DecoFacingBlock gubcrafter = (gvcr2.block.Block_GVC_DecoFacingBlock) this.world.getBlockState(pos).getBlock();
                EnumFacing enumfacing = (EnumFacing)this.world.getBlockState(pos).getValue(gubcrafter.FACING);
                if(enumfacing == EnumFacing.SOUTH) {
                    facing = 1;
                }else if(enumfacing == EnumFacing.WEST) {
                    facing = 2;
                }else if(enumfacing == EnumFacing.EAST) {
                    facing = 3;
                }else {
                    facing = 0;
                }
                this.id = gubcrafter.id;
            }
        }

        ci.cancel();
    }
}
