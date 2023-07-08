package com.aki.modfix.mixin.vanilla.ray;

import com.aki.mcutils.APICore.Entity.YawPitchMovingUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

/**
 * 不安定
 * Vanillaのほうが早いかも
 *
 * modelLoader の mixinsに入れる。 (使うときは)
 * */
@Deprecated
@Mixin(World.class)
public abstract class MixinWorldFastRay implements IBlockAccess {

    @Shadow public abstract IBlockState getBlockState(BlockPos pos);

    @Shadow public abstract void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters);

    @Shadow public abstract void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters);

    @Shadow protected abstract void spawnParticle(int particleID, boolean ignoreRange, double xCood, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters);

    /**
     * @author Aki
     * @reason Replace Slow ray to The Fastest ray
     */
    @Overwrite
    @Nullable
    public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
    {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z) && !Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z))
        {

            /**
             * 運動方向の計算
             * */
            float yaw = YawPitchMovingUtil.getyaw(vec31.x, vec31.z, vec32.x, vec32.z);
            float pitch = YawPitchMovingUtil.getPitch(vec31.x, vec31.y, vec31.z, vec32.x, vec32.y, vec32.z);
            Vec3d AddVector = YawPitchMovingUtil.getVec3dMoving(yaw, pitch).scale(0.5); // 移動距離は1

            //弾のぶつかった面?
            EnumFacing enumfacing = EnumFacing.NORTH;


            double AX = AddVector.x;
            double AY = AddVector.y;
            double AZ = AddVector.z;

            /*
            //整数に変換 -> 差から面を算出
            int ZX = MathHelper.floor(AX);
            int ZY = MathHelper.floor(AY);
            int ZZ = MathHelper.floor(AZ);*/

            if(AX > AY && AX > AZ) {
                enumfacing = EnumFacing.WEST;
            } else if(AX < AY && AX < AZ) {
                enumfacing = EnumFacing.EAST;
            } else if(AY > AX && AY > AZ) {
                enumfacing = EnumFacing.DOWN;
            } else if(AY < AX && AY < AZ) {
                enumfacing = EnumFacing.UP;
            } else if(AZ > AY && AZ > AX) {
                enumfacing = EnumFacing.NORTH;
            } else if(AZ < AY && AZ < AX) {
                enumfacing = EnumFacing.SOUTH;
            }

            enumfacing = enumfacing.getOpposite();

            /**
             * 軌道チェック用
             * */
            //spawnParticle(EnumParticleTypes.END_ROD, vec32.x, vec32.y, vec32.z, 0, 0, 0);

            Vec3d PVec = new Vec3d(vec31.x, vec31.y, vec31.z);

            BlockPos blockpos = new BlockPos(MathHelper.floor(PVec.x), MathHelper.floor(PVec.y), MathHelper.floor(PVec.z));
            IBlockState iblockstate = this.getBlockState(blockpos);
            Block block = iblockstate.getBlock();

            if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox((World)(Object)this, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid))
            {
                RayTraceResult raytraceresult = iblockstate.collisionRayTrace((World)(Object)this, blockpos, PVec, vec32);

                if (raytraceresult != null)
                {
                    return raytraceresult;
                }
            }

            int Count = 0;

            RayTraceResult result = null;

            double dist = PVec.distanceTo(vec32);
            double PointDist = dist + 0.25d;//開始用

            //中点を利用する。
            Vec3d OldPVec = vec31;
            Vec3d PDist = PVec;

            double range = 1.5d;

            try {
                while (Count <= 50 && dist > range && PointDist > dist && PointDist > range) {


                    blockpos = new BlockPos(MathHelper.floor(PVec.x), MathHelper.floor(PVec.y), MathHelper.floor(PVec.z));

                    //spawnParticle(EnumParticleTypes.CRIT, MathHelper.floor(PVec.x), MathHelper.floor(PVec.y), MathHelper.floor(PVec.z), 0, 0, 0);

                    IBlockState iblockstate1 = this.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();
                    if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(this, blockpos) != Block.NULL_AABB) {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                            RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace((World) (Object) this, blockpos, PVec, vec32);
                            if (raytraceresult1 != null) {
                                //System.out.println("2...: " + block1.getRegistryName().toString());
                                return raytraceresult1;
                            }
                        } else {
                            result = new RayTraceResult(RayTraceResult.Type.MISS, PVec, enumfacing, blockpos);
                        }
                    }

                    OldPVec = new Vec3d(PVec.x, PVec.y, PVec.z);//p4

                    //System.out.println("FastRay -VPos: " + PVec.toString() + ", AddVector: " + AddVector.toString());

                    PVec = PVec.add(AddVector);//p4 + 1 p5

                    //
                    PDist = OldPVec.add(PVec);

                    //System.out.println("PVec: " + PVec.toString() + ", OldPVec: " + OldPVec.toString() + ", PDist: " + PDist.toString());

                    PDist = PDist.scale(0.5);//平均をとる。


                    PointDist = PDist.distanceTo(vec32);
                    //


                    dist = PVec.distanceTo(vec32);
                    //System.out.println("Count: " + Count + ", PD: " + PointDist + ", Dist: " + dist + ", Vec: " + AddVector.toString());
                    Count++;

                    //spawnParticle(EnumParticleTypes.FLAME, MathHelper.floor(OldPVec.x), MathHelper.floor(OldPVec.y), MathHelper.floor(OldPVec.z), 0, 0, 0);
                }
            } finally {
                if (PointDist <= dist || dist <= range || PointDist <= range) {
                    blockpos = new BlockPos(MathHelper.floor(OldPVec.x), MathHelper.floor(OldPVec.y), MathHelper.floor(OldPVec.z));

                    //spawnParticle(EnumParticleTypes.PORTAL, MathHelper.floor(OldPVec.x), MathHelper.floor(OldPVec.y), MathHelper.floor(OldPVec.z), 0, 0, 0);

                    IBlockState iblockstate1 = this.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();
                    if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(this, blockpos) != Block.NULL_AABB) {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                            RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace((World) (Object) this, blockpos, PVec, vec32);
                            if (raytraceresult1 != null) {

                                return raytraceresult1;
                            }
                        } else {
                            result = new RayTraceResult(RayTraceResult.Type.MISS, PVec, enumfacing, blockpos);
                        }
                    }
                }
            }

            return returnLastUncollidableBlock ? result : null;
        } else {
            return null;
        }
    }
}
