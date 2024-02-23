package com.aki.modfix.mixin.vanilla.misc.network.security;

import com.aki.modfix.util.fix.extensions.IClientConnectionEncryptionExtension;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.server.network.NetHandlerLoginServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

@Mixin(NetHandlerLoginServer.class)
public class MixinServerLoginHandler {
    @Shadow @Final public NetworkManager networkManager;

    @Shadow private SecretKey secretKey;

    @Inject(method = "processEncryptionResponse", at = @At("RETURN"))
    public void GetSecretKey(CPacketEncryptionResponse p_147315_1_, CallbackInfo ci) throws GeneralSecurityException {
        ((IClientConnectionEncryptionExtension)this.networkManager).setupEncryption(this.secretKey);
    }
}
