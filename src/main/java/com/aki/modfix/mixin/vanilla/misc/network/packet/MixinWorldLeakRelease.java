package com.aki.modfix.mixin.vanilla.misc.network.packet;

import com.aki.modfix.util.fix.extensions.PacketBufExtends;
import io.netty.buffer.AbstractReferenceCountedByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldClient.class)
public class MixinWorldLeakRelease {
    public boolean IsRelease(PacketBuffer buf) {
        if(buf instanceof PacketBufExtends && !(((PacketBufExtends)buf).getParent() instanceof AbstractReferenceCountedByteBuf))
            return buf.refCnt() == 0 && buf.release();
        return true;
    }

    /*@Inject(method = "tick", at = @At("RETURN"))
    public void TickRelease(CallbackInfo ci) {
        RenderCacheManager.BUFFERS_CLEAR.removeIf(this::IsRelease);
    }*/
}
