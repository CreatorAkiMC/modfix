package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.modfix.Modfix;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Block.class, priority = Modfix.ModPriority)
public class MixinBlock {

    /**
     * @author Aki
     * @reason Fix Texture Mapping
     */
    @Overwrite
    @Deprecated
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        return source.getCombinedLight(pos, state.getLightValue(source, pos));
    }

}
