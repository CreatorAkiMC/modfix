package com.aki.modfix.util.gl;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public enum BakedModelEnumFacing {
    _DOWN(EnumFacing.DOWN),
    _UP(EnumFacing.UP),
    _NORTH(EnumFacing.NORTH),
    _SOUTH(EnumFacing.SOUTH),
    _WEST(EnumFacing.WEST),
    _EAST(EnumFacing.EAST),
    _NULL(null);

    final EnumFacing facing_null;

    BakedModelEnumFacing(EnumFacing facing) {
        facing_null = facing;
    }

    @Nullable
    public EnumFacing getFacing() {
        return facing_null;
    }

    public static BakedModelEnumFacing getBakedFacing(@Nullable EnumFacing facing) {
        if (facing == null)
            return _NULL;
        switch (facing) {
            case DOWN:
                return _DOWN;
            case UP:
                return _UP;
            case NORTH:
                return _NORTH;
            case SOUTH:
                return _SOUTH;
            case WEST:
                return _WEST;
            case EAST:
                return _EAST;
        }
        return _NULL;
    }
}
