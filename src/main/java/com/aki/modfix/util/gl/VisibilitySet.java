package com.aki.modfix.util.gl;

import net.minecraft.util.EnumFacing;

public class VisibilitySet {

    private long visibilities;

    public void setVisible(EnumFacing origin, EnumFacing dir) {
        visibilities |= 1L << (origin.ordinal() * 6) << dir.ordinal();
        visibilities |= 1L << (dir.ordinal() * 6) << origin.ordinal();
    }

    public boolean isVisible(EnumFacing origin, EnumFacing dir) {
        return ((int) (visibilities >>> (origin.ordinal() * 6)) & (1 << dir.ordinal())) != 0;
    }

    public boolean allVisible() {
        return visibilities == 0xF_FFFF_FFFFL;
    }

    public int allVisibleFrom(EnumFacing origin) {
        return (int) (visibilities >>> (origin.ordinal() * 6)) & 0x3F;
    }

    public void setAllVisible() {
        this.visibilities = 0xF_FFFF_FFFFL;
    }

}
