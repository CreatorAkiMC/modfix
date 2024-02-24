package com.aki.modfix.mixin.phosphor;

import com.aki.modfix.Modfix;
import me.jellysquid.mods.phosphor.mixins.plugins.LightingEnginePlugin;
import me.jellysquid.mods.phosphor.mod.PhosphorMod;
import me.jellysquid.mods.phosphor.mod.world.lighting.LightingEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.locks.ReentrantLock;


@Mixin(value = LightingEngine.class, priority = Modfix.ModPriority)
public class MixinLightingEngine {
    @Shadow
    @Final
    private ReentrantLock lock;

    @Shadow
    @Final
    private Thread ownedThread;

    /**
     * @author Aki
     * @reason Add LightingEngine Thread
     */
    @Overwrite(remap = false)
    private void acquireLock() {
        if (!this.lock.tryLock()) {
            if (LightingEnginePlugin.ENABLE_ILLEGAL_THREAD_ACCESS_WARNINGS) {
                Thread current = Thread.currentThread();
                if (current != this.ownedThread) {
                    IllegalAccessException e = new IllegalAccessException(String.format("World is owned by '%s' (ID: %s), but was accessed from thread '%s' (ID: %s)", this.ownedThread.getName(), this.ownedThread.getId(), current.getName(), current.getId()));
                    PhosphorMod.LOGGER.warn("Something (likely another mod) has attempted to modify the world's state from the wrong thread!\nThis is *bad practice* and can cause severe issues in your game. Phosphor has done as best as it can to mitigate this violation, but it may negatively impact performance or introduce stalls.\nIn a future release, this violation may result in a hard crash instead of the current soft warning. You should report this issue to our issue tracker with the following stacktrace information.\n(If you are aware you have misbehaving mods and cannot resolve this issue, you can safely disable this warning by setting `enable_illegal_thread_access_warnings` to `false` in Phosphor's configuration file for the time being.)", e);
                }
            }

            this.lock.lock();
        }

    }
}
