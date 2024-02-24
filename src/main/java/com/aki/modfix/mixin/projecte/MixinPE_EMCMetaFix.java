package com.aki.modfix.mixin.projecte;

import com.aki.modfix.util.fix.FixDefaultImpl;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = moze_intel.projecte.impl.KnowledgeImpl.class, remap = false)
public class MixinPE_EMCMetaFix {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true, remap = false)
    private static void init1(CallbackInfo ci) {
        CapabilityManager.INSTANCE.register(moze_intel.projecte.api.capabilities.IKnowledgeProvider.class, new Capability.IStorage<moze_intel.projecte.api.capabilities.IKnowledgeProvider>() {
            public NBTTagCompound writeNBT(Capability<moze_intel.projecte.api.capabilities.IKnowledgeProvider> capability, moze_intel.projecte.api.capabilities.IKnowledgeProvider instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            public void readNBT(Capability<moze_intel.projecte.api.capabilities.IKnowledgeProvider> capability, moze_intel.projecte.api.capabilities.IKnowledgeProvider instance, EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    instance.deserializeNBT((NBTTagCompound) nbt);
                }

            }
        }, () -> {
            return new FixDefaultImpl(null);
        });
        ci.cancel();
    }
}
