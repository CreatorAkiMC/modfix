package com.aki.modfix.mixin.projecte;

import com.aki.modfix.util.fix.FixDefaultImpl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = moze_intel.projecte.impl.KnowledgeImpl.Provider.class, remap = false)
public class MixinPE_EMCMetaFixProvider {
    private FixDefaultImpl knowledgeF;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void ChangeImpl(EntityPlayer player, CallbackInfo ci) {
        this.knowledgeF = new FixDefaultImpl(player);
    }

    @Inject(method = "hasCapability", at = @At("HEAD"), cancellable = true, remap = false)
    public void hasCapability1(Capability<?> capability, EnumFacing facing, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(capability == moze_intel.projecte.api.ProjectEAPI.KNOWLEDGE_CAPABILITY);
    }

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true, remap = false)
    public <T> void getCapability1(Capability<T> capability, EnumFacing facing, CallbackInfoReturnable<T> cir) {
        cir.setReturnValue(capability == moze_intel.projecte.api.ProjectEAPI.KNOWLEDGE_CAPABILITY ? moze_intel.projecte.api.ProjectEAPI.KNOWLEDGE_CAPABILITY.cast(this.knowledgeF) : null);
    }

    @Inject(method = "serializeNBT()Lnet/minecraft/nbt/NBTTagCompound;", at = @At("HEAD"), remap = false, cancellable = true)
    public void serializeNBT1(CallbackInfoReturnable<NBTTagCompound> cir) {
        cir.setReturnValue(this.knowledgeF.serializeNBT());
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("HEAD"), remap = false, cancellable = true)
    public void deserializeNBT1(NBTTagCompound nbt, CallbackInfo ci) {
        this.knowledgeF.deserializeNBT(nbt);
        ci.cancel();
    }
}
