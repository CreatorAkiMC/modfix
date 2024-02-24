package com.aki.modfix.mixin.vanilla.misc.nbt;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(NBTTagCompound.class)
public abstract class MixinNBTTagCompound {
    @Mutable
    @Shadow
    @Final
    private Map<String, NBTBase> tagMap;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void UseFastMap(CallbackInfo ci) {
        tagMap = new Object2ObjectOpenHashMap<>();
    }
}
