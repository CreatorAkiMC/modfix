package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.modfix.Modfix;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = World.class, priority = Modfix.ModPriority)
public abstract class MixinWorld {
    @Shadow public abstract boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos);

    @Shadow public abstract void scheduleUpdate(BlockPos pos, Block blockIn, int delay);

    @Shadow public abstract void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax);

    @Shadow public abstract void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority);

    @Shadow public abstract IBlockState getBlockState(BlockPos pos);

    @Shadow public abstract void neighborChanged(BlockPos pos, Block blockIn, BlockPos fromPos);

    @Shadow public abstract void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean updateObservers);

    @Shadow public abstract boolean checkLight(BlockPos pos);

    @Shadow public abstract void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags);

    @Shadow public abstract void notifyLightSet(BlockPos pos);

    //動かないときはpriorityを確認しよう
    @Inject(method = "updateEntities", at = @At("HEAD"))
    public void UpdateLightingEngine(CallbackInfo ci) {

    }
}
