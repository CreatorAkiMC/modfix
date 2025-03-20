package com.aki.modfix.WorldRender.chunk;

import com.aki.mcutils.APICore.Utils.render.LightUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nullable;

//16^3の立方体
public class ChunkSectorCache implements IBlockAccess {
    private final World world;
    private final int sectorX, sectorY, sectorZ;


    //キャッシュする隣接するチャンクの範囲(正方形)
    private int NeighborLoadChunks = 1;

    //X-Z の順番で保存
    //    X ->
    //　Z 他他他
    //　| 他自他
    //　v 他他他
    //
    //隣接チャンクにアクセスするレンダーに対応
    // 例：チェスト、マルチブロック...
    private final Chunk[][] chunks;

    //X-Z-Y の順番で保存
    private final ExtendedBlockStorage[][][] blockStorages;

    private final Long2ObjectOpenHashMap<IBlockState> blockCache = new Long2ObjectOpenHashMap<>(256);
    private final Long2ObjectOpenHashMap<Integer> lightCache = new Long2ObjectOpenHashMap<>(256);
    private final Long2ObjectOpenHashMap<Biome> biomeCache = new Long2ObjectOpenHashMap<>(256);

    public ChunkSectorCache(World world, int SectorX, int SectorY, int SectorZ) {
        this.world = world;
        this.sectorX = SectorX;
        this.sectorY = SectorY;
        this.sectorZ = SectorZ;

        int size = 2 * this.NeighborLoadChunks + 1;
        this.chunks = new Chunk[size][size];
        this.blockStorages = new ExtendedBlockStorage[size][size][size];

        for(int x = 0; x <= this.chunks.length; x++) {
            for(int z = 0; z <= this.chunks[x].length; z++) {
                this.chunks[x][z] = this.world.getChunk((this.sectorX + (x - this.NeighborLoadChunks)), (this.sectorZ + (z - this.NeighborLoadChunks)));

                for(int y = 0; y <= size; y++) {
                    //x z y
                    this.blockStorages[x][z][y] = WorldUtil.getStorageOfSection((this.sectorX + (x - this.NeighborLoadChunks)), (this.sectorY + (y - this.NeighborLoadChunks)), (this.sectorZ + (z - this.NeighborLoadChunks)));
                }
            }
        }
    }

    @Nullable
    public Chunk getChunk(BlockPos pos) {
        int indexX = ((pos.getX() >> 4) - this.sectorX) + this.NeighborLoadChunks;
        int indexZ = ((pos.getZ() >> 4) - this.sectorZ) + this.NeighborLoadChunks;

        if(0 <= indexX && indexX < (2 * this.chunks.length) && 0 <= indexZ && indexZ < (2 * this.chunks[indexX].length)) {
            return this.chunks[indexX][indexZ];
        }

        return null;
    }

    @Nullable
    public ExtendedBlockStorage getBlockStorage(BlockPos pos) {
        if (this.inBounds(pos)) {
            int indexX = ((pos.getX() >> 4) - this.sectorX) + this.NeighborLoadChunks;
            int indexZ = ((pos.getZ() >> 4) - this.sectorZ) + this.NeighborLoadChunks;
            int indexY = ((pos.getY() >> 4) - this.sectorY) + this.NeighborLoadChunks;

            return this.blockStorages[indexX][indexZ][indexY];
        }
        return null;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        Chunk chunk = this.getChunk(pos);
        return chunk == null ? null : chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
    }

    @Override
    public IBlockState getBlockState(final BlockPos pos) {
        return this.blockCache.computeIfAbsent(pos.toLong(), (posL) -> {
            ExtendedBlockStorage storage = this.getBlockStorage(pos);
            return storage == null ? Blocks.AIR.getDefaultState() : storage.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        });
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState state = this.getBlockState(pos);
        return state.getBlock().isAir(state, this.world, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.biomeCache.computeIfAbsent(pos.toLong(), (posL) -> {
            Chunk chunk = this.getChunk(pos);
            return chunk == null ? Biomes.PLAINS : chunk.getBiome(pos, this.world.getBiomeProvider());
        });
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return this.getBlockState(pos).getStrongPower(this.world, pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return this.world.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if(this.inBounds(pos)) {
            return this.getBlockState(pos).isSideSolid(this.world, pos, side);
        }
        return _default;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int minBlockLight) {
        return this.lightCache.compute(pos.toLong(), (key, value) -> value == null ? this.calculateCombinedLight(pos, minBlockLight) : value);
    }

    private int calculateCombinedLight(BlockPos pos, int minBlockLight) {
        int light = LightUtil.pack(0, minBlockLight);

        IBlockState state = this.getBlockState(pos);

        if (state.useNeighborBrightness()) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            light = this.getLight(mutable.setPos(pos.getX(), pos.getY() - 1, pos.getZ()), light);
            light = this.getLight(mutable.setPos(pos.getX(), pos.getY() + 1, pos.getZ()), light);
            light = this.getLight(mutable.setPos(pos.getX(), pos.getY(), pos.getZ() - 1), light);
            light = this.getLight(mutable.setPos(pos.getX(), pos.getY(), pos.getZ() + 1), light);
            light = this.getLight(mutable.setPos(pos.getX() - 1, pos.getY(), pos.getZ()), light);
            light = this.getLight(mutable.setPos(pos.getX() + 1, pos.getY(), pos.getZ()), light);
        } else {
            light = this.getLight(pos, light);
        }

        /*if (GLOptifine.OPTIFINE_INSIDE && GLOptifine.IS_DYNAMIC_LIGHTS.invoke(null) && !state.isOpaqueCube()) {
            light = GLOptifine.GET_COMBINED_LIGHT.invoke(null, pos, light);
        }*/

        return light;
    }

    private int getLight(BlockPos pos, int skyBlock) {
        return this.getLight(pos, LightUtil.sky(skyBlock), LightUtil.block(skyBlock));
    }

    private int getLight(BlockPos pos, int sky, int block) {
        ExtendedBlockStorage section = this.getBlockStorage(pos);
        if (section != null) {
            if (this.world.provider.hasSkyLight() && sky < 15) {
                sky = Math.max(sky, LightUtil.getSkyLight(section, pos));
            }
            if (block < 15) {
                block = Math.max(block, LightUtil.getBlockLight(section, pos));
            }
        } else if (this.world.provider.hasSkyLight() && sky < EnumSkyBlock.SKY.defaultLightValue) {
            Chunk chunk = this.getChunk(pos);
            if (chunk != null && chunk.canSeeSky(pos)) {
                sky = EnumSkyBlock.SKY.defaultLightValue;
            }
        }
        return LightUtil.pack(sky, block);
    }

    private boolean inBounds(BlockPos pos) {
        int indexX = ((pos.getX() >> 4) - this.sectorX) + this.NeighborLoadChunks;
        int indexZ = ((pos.getZ() >> 4) - this.sectorZ) + this.NeighborLoadChunks;
        return 0 <= indexX && indexX < (2 * this.chunks.length) && 0 <= indexZ && indexZ < (2 * this.chunks[indexX].length);
    }
}