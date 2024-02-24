package com.aki.modfix.mixin.vanilla.misc.network.process_size;

import com.aki.modfix.util.fix.network.PacketSizeUtil;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = PacketBuffer.class, remap = false)
public class MixinPacketBuffer {
    @ModifyConstant(method = {"readTextComponent", "readResourceLocation", "writeString"}, constant = @Constant(intValue = 32767), remap = false)
    public int ChangeSize(int constant) {
        return PacketSizeUtil.PacketSize;
    }

    @ModifyConstant(method = "readCompoundTag", constant = @Constant(longValue = 2097152L), remap = false)
    public long ChangeSizeInit(long constant) {
        return PacketSizeUtil.ReadNBTSize;
    }
}
