package com.aki.modfix.mixin.vanilla.misc.network.process_size;

import com.aki.modfix.util.fix.network.PacketSizeUtil;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NBTTagCompound.class)
public abstract class MixinNBTTagCompound {
    @ModifyConstant(method = "read", constant = @Constant(intValue = 512))
    public int ChangeSize(int constant) {
        return PacketSizeUtil.ReadNBTSize;
    }
}
