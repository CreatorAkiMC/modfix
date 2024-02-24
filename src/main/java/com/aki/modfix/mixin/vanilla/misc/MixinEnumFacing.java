package com.aki.modfix.mixin.vanilla.misc;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnumFacing.class)
public class MixinEnumFacing {
    @Shadow
    @Final
    public static EnumFacing[] VALUES;

    @Shadow
    @Final
    private int opposite;

    private int offsetX, offsetY, offsetZ;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void Reinit(String p_i46016_1_, int p_i46016_2_, int p_i46016_3_, int p_i46016_4_, int p_i46016_5_, String p_i46016_6_, EnumFacing.AxisDirection p_i46016_7_, EnumFacing.Axis p_i46016_8_, Vec3i vec, CallbackInfo ci) {
        this.offsetX = vec.getX();
        this.offsetY = vec.getY();
        this.offsetZ = vec.getZ();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public EnumFacing getOpposite() {
        return VALUES[this.opposite];
    }

    /**
     * @reason
     * @author
     */
    @Overwrite
    public int getXOffset() {
        return this.offsetX;
    }

    /**
     * @reason
     * @author
     */
    @Overwrite
    public int getYOffset() {
        return this.offsetY;
    }

    /**
     * @reason
     * @author
     */
    @Overwrite
    public int getZOffset() {
        return this.offsetZ;
    }
}
