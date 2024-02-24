package com.aki.modfix.mixin.vanilla.misc.network.security;

import com.aki.modfix.util.fix.extensions.IClientConnectionEncryptionExtension;
import io.netty.channel.Channel;
import net.minecraft.network.NettyEncryptingDecoder;
import net.minecraft.network.NettyEncryptingEncoder;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.CryptManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

@Mixin(NetworkManager.class)
public class MixinClientConnection implements IClientConnectionEncryptionExtension {

    @Shadow
    private Channel channel;

    @Shadow
    private boolean isEncrypted;

    @Override
    public void setupEncryption(SecretKey key) throws GeneralSecurityException {
        this.isEncrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, key)));
        this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
    }
}
