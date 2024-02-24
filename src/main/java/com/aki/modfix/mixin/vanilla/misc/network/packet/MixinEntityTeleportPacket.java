package com.aki.modfix.mixin.vanilla.misc.network.packet;

import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SPacketEntityTeleport.class)
public class MixinEntityTeleportPacket {
    @Shadow
    private double posX;

    @Shadow
    private double posY;

    @Shadow
    private double posZ;

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
    public void FixPosInit(Entity p_i46893_1_, CallbackInfo ci) {
        this.posX = p_i46893_1_.prevPosX;
        this.posY = p_i46893_1_.prevPosY;
        this.posZ = p_i46893_1_.prevPosZ;
    }
}
