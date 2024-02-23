package com.aki.modfix.mixin.vanilla.misc.network.process_size;

import com.aki.modfix.util.fix.network.PacketSizeUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CPacketCustomPayload.class)
public class MixinCPacketCustomPayload{
    @Shadow private String channel;

    @Shadow private PacketBuffer data;

    @ModifyConstant(method = {"<init>()V", "readPacketData"}, constant = @Constant(intValue = 32767))
    public int ChangeSizeInit(int constant) {
        return PacketSizeUtil.PacketSize;
    }

    @ModifyConstant(method = {"<init>()V", "readPacketData"}, constant = @Constant(stringValue = "Payload may not be larger than 32767 bytes"))
    public String ChangeStringInit(String constant) {
        return "Payload may not be larger than " + PacketSizeUtil.PacketSize + " bytes";
    }
}
