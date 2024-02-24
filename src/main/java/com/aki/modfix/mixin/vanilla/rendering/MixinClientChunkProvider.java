package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.modfix.WorldRender.chunk.ChunkRenderManager;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkProviderClient.class)
public class MixinClientChunkProvider {

    @Inject(method = "loadChunk", at = @At("RETURN"))
    public void loadChunk(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> info) {
        ChunkRenderManager.LoadChunk(chunkX, chunkZ);
    }

    /**
     * {@link ChunkProviderClient#unloadChunk(int, int)}
     */
    @Inject(method = "unloadChunk", at = @At("RETURN"))
    public void unloadChunk(int chunkX, int chunkZ, CallbackInfo info) {
        ChunkRenderManager.UnLoadChunk(chunkX, chunkZ);
    }
}
