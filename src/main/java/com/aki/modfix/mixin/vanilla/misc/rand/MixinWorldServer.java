package com.aki.modfix.mixin.vanilla.misc.rand;

import com.aki.mcutils.APICore.Utils.rand.XoRoShiRoFastRandomW;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends World {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract void createBonusChest();

    protected MixinWorldServer(ISaveHandler p_i45749_1_, WorldInfo p_i45749_2_, WorldProvider p_i45749_3_, Profiler p_i45749_4_, boolean p_i45749_5_) {
        super(p_i45749_1_, p_i45749_2_, p_i45749_3_, p_i45749_4_, p_i45749_5_);
    }

    /**
     * @author Aki
     * @reason Replace Random
     */
    @Overwrite
    private void createSpawnPosition(WorldSettings settings) {
        if (!this.provider.canRespawnHere()) {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up(this.provider.getAverageGroundLevel()));
        } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            this.worldInfo.setSpawn(BlockPos.ORIGIN.up());
        } else {
            if (net.minecraftforge.event.ForgeEventFactory.onCreateWorldSpawn(this, settings)) return;
            this.findingSpawnPoint = true;
            BiomeProvider biomeprovider = this.provider.getBiomeProvider();
            List<Biome> list = biomeprovider.getBiomesToSpawnIn();
            Random random = new XoRoShiRoFastRandomW(this.getSeed());
            BlockPos blockpos = biomeprovider.findBiomePosition(0, 0, 256, list, random);
            int i = 8;
            int j = this.provider.getAverageGroundLevel();
            int k = 8;

            if (blockpos != null) {
                i = blockpos.getX();
                k = blockpos.getZ();
            } else {
                LOGGER.warn("Unable to find spawn biome");
            }

            int l = 0;

            while (!this.provider.canCoordinateBeSpawn(i, k)) {
                i += random.nextInt(64) - random.nextInt(64);
                k += random.nextInt(64) - random.nextInt(64);
                ++l;

                if (l == 1000) {
                    break;
                }
            }

            this.worldInfo.setSpawn(new BlockPos(i, j, k));
            this.findingSpawnPoint = false;

            if (settings.isBonusChestEnabled()) {
                this.createBonusChest();
            }
        }
    }
}
