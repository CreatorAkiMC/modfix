package com.aki.modfix.LighSystem.thread;

import com.aki.mcutils.APICore.Utils.list.Pair;
import com.aki.modfix.LighSystem.LightingData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameLightCalculatingThread extends Thread {
    private double sleepOverhead = 0.0D;
    private ConcurrentHashMap<BlockPos, LightingData> LightBlockPosMap = new ConcurrentHashMap<>();

    public GameLightCalculatingThread() {
        super();
        this.setDaemon(true);
        this.setName("ModFix DynamicLight Calculating Thread");
    }

    @Override
    public void run() {
        while (true) {
            long t = System.nanoTime();
            try {
                Minecraft mc = Minecraft.getMinecraft();
                int RenderDist = mc.gameSettings.renderDistanceChunks * 16 + 16;
                EntityPlayer player = mc.player;
                World world = mc.world;
                if(player != null && world != null) {
                    List<Entity> CheckEntities = world.loadedEntityList;
                    //BlockPosLight Selection
                    LightBlockPosMap = new ConcurrentHashMap<>(LightBlockPosMap.entrySet().stream().filter(entry -> Math.sqrt(Math.pow(entry.getKey().getX() - player.posX, 2.0) + Math.pow(entry.getKey().getY() - player.posY, 2.0) + Math.pow(entry.getKey().getZ() - player.posZ, 2.0)) <= RenderDist && entry.getValue().getLightLevel() > 0.0d && entry.getKey() == entry.getValue().getLightSourcePos()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    Minecraft.getMinecraft().addScheduledTask(() -> this.LightBlockPosMap.forEach((key, value) -> world.checkLightFor(EnumSkyBlock.BLOCK, key)));
                    for(Entity entity : CheckEntities) {
                        double dist = Math.sqrt(Math.pow(entity.posX - player.posX, 2.0) + Math.pow(entity.posY - player.posY, 2.0) + Math.pow(entity.posZ - player.posZ, 2.0));
                        if(dist > RenderDist)
                            continue;
                        if(entity.isBurning()) {
                            PutUpdateLightData(entity.getPosition(), 15.0, world);
                        } else {
                            if(entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator()) {
                                continue;
                            } else if(entity instanceof EntityBlaze && ((EntityBlaze)entity).isCharged()) {
                                PutUpdateLightData(entity.getPosition(), 15.0, world);
                            } else if(entity instanceof EntityFireball || entity instanceof EntityTNTPrimed) {
                                PutUpdateLightData(entity.getPosition(), 15.0, world);
                            } else if(entity instanceof EntityMagmaCube) {
                                PutUpdateLightData(entity.getPosition(), ((EntityMagmaCube)entity).squishFactor > 0.6 ? 13.0 : 8.0, world);
                            } else {
                                if(entity instanceof EntityCreeper) {
                                    if(((EntityCreeper)entity).getCreeperFlashIntensity(0.0F) > 0.0001) {
                                        PutUpdateLightData(entity.getPosition(), 15.0, world);
                                    }
                                }
                                if(entity instanceof EntityLivingBase) {
                                    EntityLivingBase livingBase = (EntityLivingBase) entity;
                                    double LightValue = 0.0d;
                                    for(EntityEquipmentSlot equipmentSlot : EntityEquipmentSlot.values()) {
                                        ItemStack EquipmentStack = livingBase.getItemStackFromSlot(equipmentSlot);
                                        LightValue = Math.max(LightValue, this.GetItemStackLight(EquipmentStack));
                                    }
                                    if(LightValue > 0.0d) {
                                        PutUpdateLightData(entity.getPosition(), LightValue, world);
                                    }
                                } else if (entity instanceof EntityItem) {
                                    ItemStack stack = ((EntityItem)entity).getItem().copy();
                                    double LightLevel = this.GetItemStackLight(stack);
                                    if(LightLevel > 0.0d)
                                        PutUpdateLightData(entity.getPosition(), LightLevel, world);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            double d = (System.nanoTime() - t) / 1_000_000.0D + this.sleepOverhead;
            this.sleepOverhead = d % 1.0D;
            long sleepTime = 10 - (long) d;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private double GetItemStackLight(ItemStack stack) {
        if(stack != null) {
            Item item = stack.getItem();
            if(item instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) item;
                Block block = itemBlock.getBlock();
                if(block != null) {
                    return block.getLightValue(block.getDefaultState());
                }
            }

            if(item == Items.LAVA_BUCKET) {
                return Blocks.LAVA.getLightValue(Blocks.LAVA.getDefaultState());
            } else if(item != Items.BLAZE_ROD && item != Items.BLAZE_POWDER) {
                if(item == Items.GLOWSTONE_DUST || item == Items.PRISMARINE_CRYSTALS || item == Items.MAGMA_CREAM) {
                    return 8.0;
                } else if(item == Items.NETHER_STAR) {
                    return Blocks.BEACON.getLightValue(Blocks.BEACON.getDefaultState()) / 2.0;
                } else {
                    return 0.0D;
                }
            } else {
                return 10.0d;
            }
        } else {
            return 0.0D;
        }
    }

    public void PutUpdateLightData(BlockPos pos, double light, World world) {
        Queue<Pair<BlockPos, LightingData>> UpdateQueue = new ArrayDeque<>();
        UpdateQueue.add(new Pair<>(pos, new LightingData(pos, light)));
        this.LightBlockPosMap.put(pos, new LightingData(pos, light));
        while (UpdateQueue.size() > 0) {
            Pair<BlockPos, LightingData> dataPair = UpdateQueue.poll();
            BlockPos blockPos = dataPair.getKey();
            LightingData data = dataPair.getValue();

            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos NextPos = blockPos.add(facing.getDirectionVec());
                LightingData NextData = this.LightBlockPosMap.get(NextPos);
                LightingData NextDataLight = data.addLightLevel(-1.0);
                if((NextData == null || NextData.getLightLevel() < NextDataLight.getLightLevel()) && NextDataLight.getLightLevel() > 0.0 && IsLightTransport(NextPos, world)) {
                    this.LightBlockPosMap.put(NextPos, NextDataLight);
                    UpdateQueue.add(new Pair<>(NextPos, NextDataLight));
                }
            }
        }
    }

    private boolean IsLightTransport(BlockPos NextPos, World world) {
        IBlockState state = world.getBlockState(NextPos);
        return state.getRenderType() == EnumBlockRenderType.INVISIBLE || !state.isOpaqueCube();
    }

    public synchronized LightingData getPosToLightLevel(BlockPos pos) {
        return this.LightBlockPosMap.getOrDefault(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), new LightingData(pos, 0.0));
    }
}
