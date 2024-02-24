package com.aki.modfix.mixin.vanilla.misc.network.process_size;

import com.aki.modfix.util.fix.network.PacketSizeUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SPacketCustomPayload.class)
public class MixinSPacketCustomPayload {
    @Shadow
    private PacketBuffer data;

    @Shadow
    private String channel;

    @ModifyConstant(method = {"readPacketData", "<init>(Ljava/lang/String;Lnet/minecraft/network/PacketBuffer;)V"}, constant = @Constant(intValue = 1048576))
    public int ChangeSizeInit(int constant) {
        return PacketSizeUtil.PacketSize;
    }

    @ModifyConstant(method = {"<init>(Ljava/lang/String;Lnet/minecraft/network/PacketBuffer;)V", "readPacketData"}, constant = @Constant(stringValue = "Payload may not be larger than 1048576 bytes"))
    public String ChangeStringInit(String constant) {
        return "Payload may not be larger than " + PacketSizeUtil.PacketSize + " bytes";
    }
}
