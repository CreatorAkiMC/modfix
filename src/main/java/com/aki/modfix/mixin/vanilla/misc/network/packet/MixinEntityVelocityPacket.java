package com.aki.modfix.mixin.vanilla.misc.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SPacketEntityVelocity.class)
public class MixinEntityVelocityPacket {
    @Shadow
    private int entityID;

    @Shadow
    private int motionX;

    @Shadow
    private int motionY;

    @Shadow
    private int motionZ;

    @Inject(method = "<init>(IDDD)V", at = @At("RETURN"))
    public void InitFix(int id, double mx, double my, double mz, CallbackInfo ci) {
        this.entityID = id;
        this.motionX = (int) (mx * 8000.0D);
        this.motionY = (int) (my * 8000.0D);
        this.motionZ = (int) (mz * 8000.0D);
    }

    @Inject(method = "writePacketData", at = @At("HEAD"), cancellable = true)
    public void FixWriteData(PacketBuffer buffer, CallbackInfo ci) {
        buffer.writeVarInt(this.entityID);
        buffer.writeInt(this.motionX);
        buffer.writeInt(this.motionY);
        buffer.writeInt(this.motionZ);
        ci.cancel();
    }

    @Inject(method = "readPacketData", at = @At("HEAD"), cancellable = true)
    public void FixReadData(PacketBuffer buf, CallbackInfo ci) {
        this.entityID = buf.readVarInt();
        this.motionX = buf.readInt();
        this.motionY = buf.readInt();
        this.motionZ = buf.readInt();
        ci.cancel();
    }
}
