package com.aki.modfix.mixin.vanilla.misc.rand;

import com.aki.mcutils.APICore.Utils.rand.XoRoShiRoFastRandomW;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.datafix.DataFixer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.net.Proxy;
import java.util.Random;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Mutable
    @Shadow
    @Final
    private Random random;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void ChangeRandomSystem(File p_i47054_1_, Proxy p_i47054_2_, DataFixer p_i47054_3_, YggdrasilAuthenticationService p_i47054_4_, MinecraftSessionService p_i47054_5_, GameProfileRepository p_i47054_6_, PlayerProfileCache p_i47054_7_, CallbackInfo ci) {
        this.random = new XoRoShiRoFastRandomW();
    }
}
