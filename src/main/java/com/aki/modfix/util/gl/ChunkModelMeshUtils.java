package com.aki.modfix.util.gl;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ChunkModelMeshUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static IBakedModel getBakedModelFromState(IBlockState state) {
        return mc.getBlockRendererDispatcher().getModelForState(state);
    }

    public static IBlockState getExtendState(IBlockState state, BlockPos pos, IBlockAccess access) {
        return state.getBlock().getExtendedState(state, access, pos);
    }
}
