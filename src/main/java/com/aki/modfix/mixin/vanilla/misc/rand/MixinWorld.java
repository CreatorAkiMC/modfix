package com.aki.modfix.mixin.vanilla.misc.rand;

import com.aki.mcutils.APICore.Utils.rand.mersenne.MersenneTwister;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(World.class)
public class MixinWorld {
    @Mutable
    @Shadow
    @Final
    public Random rand;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void ChangeRandom(ISaveHandler p_i45749_1_, WorldInfo p_i45749_2_, WorldProvider p_i45749_3_, Profiler p_i45749_4_, boolean p_i45749_5_, CallbackInfo ci) {
        this.rand = new MersenneTwister();
    }
}
