package com.aki.modfix.mixin.vanilla.misc;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockRedstoneWire.class)
public abstract class MixinBlockRedstoneWire {

    @Shadow private boolean canProvidePower;

    @Shadow @Final public static PropertyInteger POWER;

    @Shadow protected abstract boolean isPowerSourceAt(IBlockAccess worldIn, BlockPos pos, EnumFacing side);

    /**
     * @author Aki
     * @reason Replace WeakPower
     */
    @Overwrite
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (!this.canProvidePower) {
            return 0;
        } else {
            int i = blockState.getValue(POWER);

            if (i == 0) {
                return 0;
            } else if (side == EnumFacing.UP) {
                return i;
            } else {
                //EnumSet<EnumFacing> enumset = EnumSet.<EnumFacing>noneOf(EnumFacing.class);

                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                    if(enumfacing == side && enumfacing != side.rotateYCCW() && enumfacing != side.rotateY() && this.isPowerSourceAt(blockAccess, pos, enumfacing)) {
                        return i;
                    }
                    /*if (this.isPowerSourceAt(blockAccess, pos, enumfacing)) {
                        enumset.add(enumfacing);
                    }*/
                }

                if (side.getAxis().isHorizontal()) {
                    return i;
                } else {
                    return 0;
                }
            }
        }
    }
}
