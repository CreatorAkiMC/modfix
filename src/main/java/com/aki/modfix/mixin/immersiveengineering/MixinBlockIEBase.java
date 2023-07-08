package com.aki.modfix.mixin.immersiveengineering;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockIEBase.class)
public abstract class MixinBlockIEBase {
    @Shadow
    public abstract int getMetaFromState(IBlockState state);

    @Shadow(remap = false) protected EnumPushReaction[] metaMobilityFlags;

    /**
     * @author Aki
     * @reason Fixes ArrayIndexOutOfBoundsException
     */
    @Overwrite(remap = false)
    public EnumPushReaction getPushReaction(IBlockState state) {
        int meta = getMetaFromState(state);
        if (metaMobilityFlags.length <= meta || metaMobilityFlags[meta] == null) {
            return EnumPushReaction.NORMAL;
        }
        return metaMobilityFlags[meta];
    }
}
