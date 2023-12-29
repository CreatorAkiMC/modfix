package com.aki.modfix.LighSystem;

import net.minecraft.util.math.BlockPos;

@Deprecated
public class LightingData {
    private final BlockPos LightSourcePos;
    private double LightLevel;

    public LightingData(BlockPos Source, double lightLevel) {
        this.LightSourcePos = Source;
        this.LightLevel = lightLevel;
    }

    public BlockPos getLightSourcePos() {
        return LightSourcePos;
    }

    public double getLightLevel() {
        return LightLevel;
    }

    public LightingData addLightLevel(double AddLightLevel) {
        return new LightingData(LightSourcePos, LightLevel + AddLightLevel);
    }
}
