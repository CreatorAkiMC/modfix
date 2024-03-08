package com.aki.modfix.mixin.vanilla.misc;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Mixin(Explosion.class)
public class MixinExplosion {
    @Shadow @Final private double x;
    @Shadow @Final private double z;
    @Shadow @Final private World world;
    @Shadow @Final private Entity exploder;
    @Shadow @Final private double y;
    @Shadow @Final private float size;
    @Shadow @Final private Map<EntityPlayer, Vec3d> playerKnockbackMap;
    @Shadow @Final private List<BlockPos> affectedBlockPositions;
    @Shadow @Final private Random random;
    @Shadow @Final private boolean causesFire;
    public float power = 0.0f;

    private final BlockPos.MutableBlockPos cachedPos = new BlockPos.MutableBlockPos();

    private int prevChunkX = Integer.MIN_VALUE;
    private int prevChunkZ = Integer.MIN_VALUE;

    private Chunk prevChunk;

    private boolean explodeAirBlocks;

    private int minY, maxY;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;DDDFZZ)V", at = @At("RETURN"))
    public void Init(World world, Entity exploder, double x, double y, double z, float size, boolean causesFire, boolean damagesTerrain, CallbackInfo ci) {
        this.minY = 0;
        this.maxY = 255;
        this.power = size;

        boolean explodeAir = this.causesFire; // air blocks are only relevant for the explosion when fire should be created inside them
        if (!explodeAir && exploder instanceof EntityPlayer) {
            for(EntityPlayer player : world.playerEntities) {
                if(player.dimension == 1) {
                    float overestimatedExplosionRange = (8 + (int) (6f * this.power));
                    int endPortalX = 0;
                    int endPortalZ = 0;
                    if (overestimatedExplosionRange > Math.abs(this.x - endPortalX) && overestimatedExplosionRange > Math.abs(this.z - endPortalZ)) {
                        explodeAir = true;
                        // exploding air works around accidentally fixing vanilla bug: an explosion cancelling the dragon fight start can destroy the newly placed end portal
                    }
                    break;
                }
            }
        }
        this.explodeAirBlocks = explodeAir;
    }

    /**
     * @author Aki
     * @reason Avoid Air Block Check and Duplicate Blocks
     */
    @Overwrite
    public void doExplosionA()
    {
        /*Set<BlockPos> set = Sets.<BlockPos>newHashSet();
        int i = 16;

        for (int j = 0; j < 16; ++j)
        {
            for (int k = 0; k < 16; ++k)
            {
                for (int l = 0; l < 16; ++l)
                {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15)
                    {
                        double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d6 = this.y;
                        double d8 = this.z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F)
                        {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            IBlockState iblockstate = this.world.getBlockState(blockpos);

                            if (iblockstate.getMaterial() != Material.AIR)
                            {
                                float f2 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.world, blockpos, iblockstate) : iblockstate.getBlock().getExplosionResistance(world, blockpos, (Entity)null, this);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (this.exploder == null || this.exploder.canExplosionDestroyBlock(this, this.world, blockpos, iblockstate, f)))
                            {
                                set.add(blockpos);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d6 += d1 * 0.30000001192092896D;
                            d8 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }*/

        /**
         *
         * */
        final LongOpenHashSet touched = new LongOpenHashSet(0);

        final Random random = this.random;

        for (int rayX = 0; rayX < 16; ++rayX) {
            boolean xPlane = rayX == 0 || rayX == 15;
            double vecX = (((float) rayX / 15.0F) * 2.0F) - 1.0F;

            for (int rayY = 0; rayY < 16; ++rayY) {
                boolean yPlane = rayY == 0 || rayY == 15;
                double vecY = (((float) rayY / 15.0F) * 2.0F) - 1.0F;

                for (int rayZ = 0; rayZ < 16; ++rayZ) {
                    boolean zPlane = rayZ == 0 || rayZ == 15;

                    // We only fire rays from the surface of our origin volume
                    if (xPlane || yPlane || zPlane) {
                        double vecZ = (((float) rayZ / 15.0F) * 2.0F) - 1.0F;

                        this.performRayCast(random, vecX, vecY, vecZ, touched);
                    }
                }
            }
        }

        LongIterator it = touched.iterator();

        while (it.hasNext()) {
            affectedBlockPositions.add(BlockPos.fromLong(it.nextLong()));
        }

        //this.affectedBlockPositions.addAll(set);
        float f3 = this.size * 2.0F;
        int k1 = MathHelper.floor(this.x - (double)f3 - 1.0D);
        int l1 = MathHelper.floor(this.x + (double)f3 + 1.0D);
        int i2 = MathHelper.floor(this.y - (double)f3 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double)f3 + 1.0D);
        int j2 = MathHelper.floor(this.z - (double)f3 - 1.0D);
        int j1 = MathHelper.floor(this.z + (double)f3 + 1.0D);
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, (Explosion)(Object)this, list, f3);
        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for (int k2 = 0; k2 < list.size(); ++k2)
        {
            Entity entity = list.get(k2);

            if (!entity.isImmuneToExplosions())
            {
                double d12 = entity.getDistance(this.x, this.y, this.z) / (double)f3;

                if (d12 <= 1.0D)
                {
                    double d5 = entity.posX - this.x;
                    double d7 = entity.posY + (double)entity.getEyeHeight() - this.y;
                    double d9 = entity.posZ - this.z;
                    double d13 = (double)MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D)
                    {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = (double)this.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
                        double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(DamageSource.causeExplosionDamage((Explosion)(Object) this), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f3 + 1.0D)));
                        double d11 = d10;

                        if (entity instanceof EntityLivingBase)
                        {
                            d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)entity, d10);
                        }

                        entity.motionX += d5 * d11;
                        entity.motionY += d7 * d11;
                        entity.motionZ += d9 * d11;

                        if (entity instanceof EntityPlayer)
                        {
                            EntityPlayer entityplayer = (EntityPlayer)entity;

                            if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying))
                            {
                                this.playerKnockbackMap.put(entityplayer, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 爆心地から光線 (ray) をいくつか発射し、到達したブロックから爆破耐性に応じて貫通距離を変更。
     * 今までの処理では、16 * 16 * 16 の立方体を作りその中心から、一つずつブロックの爆発耐性を調べて計算していたが、 -> for が二重
     * この処理では、事前に爆破されるブロックを計算し、そこから算出するので、コストが安価になる。
     * */
    private void performRayCast(Random random, double vecX, double vecY, double vecZ, LongOpenHashSet touched) {
        double dist = Math.sqrt((vecX * vecX) + (vecY * vecY) + (vecZ * vecZ));

        double normX = (vecX / dist) * 0.3D;
        double normY = (vecY / dist) * 0.3D;
        double normZ = (vecZ / dist) * 0.3D;

        float strength = this.power * (0.7F + (random.nextFloat() * 0.6F));

        double stepX = this.x;
        double stepY = this.y;
        double stepZ = this.z;

        int prevX = Integer.MIN_VALUE;
        int prevY = Integer.MIN_VALUE;
        int prevZ = Integer.MIN_VALUE;

        float prevResistance = 0.0F;

        int boundMinY = this.minY;
        int boundMaxY = this.maxY;

        while (strength > 0.0F) {
            int blockX = MathHelper.floor(stepX);
            int blockY = MathHelper.floor(stepY);
            int blockZ = MathHelper.floor(stepZ);

            float resistance;

            if (prevX != blockX || prevY != blockY || prevZ != blockZ) {
                if (blockY < boundMinY || blockY >= boundMaxY || blockX < -30000000 || blockZ < -30000000 || blockX >= 30000000 || blockZ >= 30000000) {
                    return;
                }
                resistance = this.traverseBlock(strength, blockX, blockY, blockZ, touched);

                prevX = blockX;
                prevY = blockY;
                prevZ = blockZ;

                prevResistance = resistance;
            } else {
                resistance = prevResistance;
            }

            strength -= resistance;
            // Apply a constant fall-off
            strength -= 0.22500001F;

            stepX += normX;
            stepY += normY;
            stepZ += normZ;
        }
    }

    /**
     * Called for every step made by a ray being cast by an explosion.
     *
     * @param strength The strength of the ray during this step
     * @param blockX   The x-coordinate of the block the ray is inside of
     * @param blockY   The y-coordinate of the block the ray is inside of
     * @param blockZ   The z-coordinate of the block the ray is inside of
     * @return The resistance of the current block space to the ray
     */
    private float traverseBlock(float strength, int blockX, int blockY, int blockZ, LongOpenHashSet touched) {
        BlockPos pos = this.cachedPos.setPos(blockX, blockY, blockZ);

        if (blockY >= 257) {
            return 0.0F;
        }

        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;

        if (this.prevChunkX != chunkX || this.prevChunkZ != chunkZ) {
            this.prevChunk = this.world.getChunk(chunkX, chunkZ);

            this.prevChunkX = chunkX;
            this.prevChunkZ = chunkZ;
        }

        final Chunk chunk = this.prevChunk;

        IBlockState blockState = Blocks.AIR.getDefaultState();
        float totalResistance = 0.0F;
        Optional<Float> blastResistance;

        labelGetBlastResistance:
        {

            if (chunk != null) {

                ExtendedBlockStorage section = chunk.getBlockStorageArray()[blockY >> 4/*Pos.SectionYIndex.fromBlockCoord(chunk, blockY)*/];


                if (section != null && !section.isEmpty()) {

                    blockState = section.get(blockX & 15, blockY & 15, blockZ & 15);

                    if (blockState.getBlock() != Blocks.AIR) {

                        blastResistance = Optional.of(this.exploder != null ? this.exploder.getExplosionResistance((Explosion)(Object)this, this.world, pos, blockState) : blockState.getBlock().getExplosionResistance(world, pos, (Entity)null, (Explosion)(Object)this));

                        break labelGetBlastResistance;
                    }
                }
            }
            blastResistance = Optional.of(this.exploder != null ? this.exploder.getExplosionResistance((Explosion)(Object)this, this.world, pos, Blocks.AIR.getDefaultState()) : Blocks.AIR.getExplosionResistance(world, pos, (Entity)null, (Explosion)(Object)this));//this.behavior.getBlastResistance((Explosion) (Object) this, this.world, pos, Blocks.AIR.getDefaultState(), Fluids.EMPTY.getDefaultState());
        }

        totalResistance = (blastResistance.get() + 0.3F) * 0.3F;

        float reducedStrength = strength - totalResistance;
        if (reducedStrength > 0.0F && (this.explodeAirBlocks || blockState != Blocks.AIR.getDefaultState())) {
            if (this.exploder == null || this.exploder.canExplosionDestroyBlock((Explosion) (Object) this, this.world, pos, blockState, strength)) {//).behavior.canDestroyBlock((Explosion) (Object) this, this.world, pos, blockState, reducedStrength)) {
                touched.add(pos.toLong());
            }
        }

        return totalResistance;
    }
}
