package com.aki.modfix.mixin.vanilla.misc.network.packet;

import com.aki.modfix.util.fix.extensions.PacketBufExtends;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PacketBuffer.class)
public class MixinPacketBuf implements PacketBufExtends {

    @Shadow
    @Final
    private ByteBuf buf;

    @Override
    public ByteBuf getParent() {
        return this.buf;
    }
}
