package com.aki.modfix.mixin.vanilla.misc.rand;

import com.aki.mcutils.APICore.Utils.rand.XoRoShiRoRandom;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(Random.class)
public abstract class MixinRandomFix {
    @Shadow
    @Final
    private AtomicLong seed;

    @Shadow
    @Final
    private static long multiplier;
    @Shadow
    @Final
    private static long mask;
    public XoRoShiRoRandom roShiRoRandom = new XoRoShiRoRandom();

    @Inject(method = "<init>(J)V", at = @At("RETURN"), remap = false)
    public void SeedInsertInit(long par1, CallbackInfo ci) {
        roShiRoRandom = new XoRoShiRoRandom(this.seed.get());
    }

    @Inject(method = "seedUniquifier", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ChangeSeedUniquifier(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(XoRoShiRoRandom.randomSeed());
    }

    @Inject(method = "setSeed", at = @At("HEAD"), remap = false)
    public void setSeedXoRoshiRo(long par1, CallbackInfo ci) {
        this.roShiRoRandom.setSeed((par1 ^ multiplier) & mask);
    }

    @Inject(method = "nextInt()I", at = @At("HEAD"), cancellable = true, remap = false)
    public void ChangeNextInt1(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.roShiRoRandom.nextInt());
    }

    @Inject(method = "nextInt(I)I", at = @At("HEAD"), cancellable = true, remap = false)
    public void ChangeNextInt2(int bound, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(roShiRoRandom.nextInt(bound));
    }

    @Inject(method = "nextDouble", at = @At("HEAD"), cancellable = true, remap = false)
    public void ChangeDouble(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(this.roShiRoRandom.nextDouble());
    }

    @Inject(method = "nextBoolean", at = @At("HEAD"), cancellable = true, remap = false)
    public void ChangeBoolean(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.roShiRoRandom.nextBoolean());
    }

    @Inject(method = "nextFloat", at = @At("HEAD"), cancellable = true, remap = false)
    public void ChangeFloat(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(this.roShiRoRandom.nextFloat());
    }

    @Inject(method = "nextBytes", at = @At("HEAD"), remap = false)
    public void ChangeBytes(byte[] par1, CallbackInfo ci) {
        this.roShiRoRandom.nextBytes(par1);
    }

    @Inject(method = "nextLong", at = @At("HEAD"), cancellable = true, remap = false)
    public void ChangeLong(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(this.roShiRoRandom.nextLong());
    }
    /*@Shadow public abstract int nextInt();

    @Inject(method = "nextInt(I)I", at = @At("HEAD"), cancellable = true, remap = false)
    public void nextInt(int bound, CallbackInfoReturnable<Integer> cir) {

        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }

        long x = ((long) nextInt()) & 0xffff_ffffL;
        long m = x * bound;
        long l = m & 0xffff_ffffL;

        if (l < bound) {
            for (long t = 0x1_0000_0000L % bound; l < t; ) {
                x = ((long) nextInt()) & 0xffff_ffffL;
                m = x * bound;
                l = m & 0xffff_ffffL;
            }
        }
        cir.setReturnValue((int) (m >>> 32));
    }*/
}
