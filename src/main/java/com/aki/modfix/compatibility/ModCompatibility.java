package com.aki.modfix.compatibility;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class ModCompatibility {
    public static int getHash(IBlockState state, @Nullable TileEntity tile) {
        int tileHash = tile != null ? getModHash(tile) : 0;
        System.out.println("TileHash: " + tileHash);
        int h;
        return ((h = state.hashCode() + Block.getStateId(state) + state.getBlock().getMetaFromState(state) + tileHash) ^ (h >>> 16));
    }

    private static int getModHash(TileEntity tile) {
        if (ModCompatibilityTileRegistry.tileChangeRegistry.containsKey(tile.getClass())) {
            return ModCompatibilityTileRegistry.tileChangeRegistry.get(tile.getClass()).apply(tile);
        } else {
            return 0;
        }
    }
}
