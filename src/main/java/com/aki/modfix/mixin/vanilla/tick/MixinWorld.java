package com.aki.modfix.mixin.vanilla.tick;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.tick.TickBalanceStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(World.class)
public abstract class MixinWorld implements IBlockAccess {

    @Shadow @Final public Profiler profiler;
    @Shadow @Final public List<Entity> weatherEffects;
    @Shadow public abstract void removeEntity(Entity entityIn);
    @Shadow @Final public List<Entity> loadedEntityList;
    @Shadow @Final protected List<Entity> unloadedEntityList;
    @Shadow protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);
    @Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract void onEntityRemoved(Entity entityIn);
    @Shadow protected abstract void tickPlayers();
    @Shadow public abstract void updateEntity(Entity ent);
    @Shadow private boolean processingLoadedTiles;
    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);
    @Shadow public abstract boolean isBlockLoaded(BlockPos pos, boolean allowEmpty);
    @Shadow @Final private WorldBorder worldBorder;
    @Shadow public abstract boolean tickUpdates(boolean runAllPending);
    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos);
    @Shadow public abstract Chunk getChunk(BlockPos pos);
    @Shadow @Final private List<TileEntity> addedTileEntityList;
    @Shadow public abstract void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags);
    @Shadow public abstract World init();
    @Shadow public abstract boolean spawnEntity(Entity entityIn);
    @Shadow public abstract boolean setBlockToAir(BlockPos pos);
    @Shadow @Final public boolean isRemote;

    @Shadow @Final private List<TileEntity> tileEntitiesToBeRemoved;
    @Shadow @Final public List<TileEntity> tickableTileEntities;
    @Shadow @Final public List<TileEntity> loadedTileEntityList;

    @Shadow public abstract void updateComparatorOutputLevel(BlockPos pos, Block blockIn);

    @Shadow public abstract void tick();

    @Unique
    public Object2ObjectOpenHashMap<BlockPos, TickBalanceStorage> TickTimeHash = new Object2ObjectOpenHashMap<>();

    @Unique
    public long TimeSum = 0L;
    @Unique
    public int RunCount = 0;

    /**
     * @author Aki
     * @reason Replace Method & fix
     */
    @Inject(method = "updateEntities", at = @At("HEAD"), cancellable = true)
    public void updateEntitiesFix(CallbackInfo ci) throws InterruptedException {
        this.profiler.startSection("entities");
        this.profiler.startSection("global");

        for (int i = 0; i < this.weatherEffects.size(); ++i)
        {
            Entity entity = this.weatherEffects.get(i);

            try
            {
                if(entity.updateBlocked) continue;
                ++entity.ticksExisted;
                entity.onUpdate();
            }
            catch (Throwable throwable2)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

                if (entity == null)
                {
                    crashreportcategory.addCrashSection("Entity", "~~NULL~~");
                }
                else
                {
                    entity.addEntityCrashInfo(crashreportcategory);
                }

                if (net.minecraftforge.common.ForgeModContainer.removeErroringEntities)
                {
                    net.minecraftforge.fml.common.FMLLog.log.fatal("{}", crashreport.getCompleteReport());
                    removeEntity(entity);
                }
                else
                    throw new ReportedException(crashreport);
            }

            if (entity.isDead)
            {
                this.weatherEffects.remove(i--);
            }
        }

        this.profiler.endStartSection("remove");
        this.loadedEntityList.removeAll(this.unloadedEntityList);

        for (int k = 0; k < this.unloadedEntityList.size(); ++k)
        {
            Entity entity1 = this.unloadedEntityList.get(k);
            int j = entity1.chunkCoordX;
            int k1 = entity1.chunkCoordZ;

            if (entity1.addedToChunk && this.isChunkLoaded(j, k1, true))
            {
                this.getChunk(j, k1).removeEntity(entity1);
            }
        }

        for (int l = 0; l < this.unloadedEntityList.size(); ++l)
        {
            this.onEntityRemoved(this.unloadedEntityList.get(l));
        }

        this.unloadedEntityList.clear();
        this.tickPlayers();
        this.profiler.endStartSection("regular");

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1)
        {
            Entity entity2 = this.loadedEntityList.get(i1);
            Entity entity3 = entity2.getRidingEntity();

            if (entity3 != null)
            {
                if (!entity3.isDead && entity3.isPassenger(entity2))
                {
                    continue;
                }

                entity2.dismountRidingEntity();
            }

            this.profiler.startSection("tick");

            if (!entity2.isDead && !(entity2 instanceof EntityPlayerMP))
            {
                try
                {
                    net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackStart(entity2);
                    this.updateEntity(entity2);
                    net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackEnd(entity2);
                }
                catch (Throwable throwable1)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Entity being ticked");
                    entity2.addEntityCrashInfo(crashreportcategory1);
                    if (net.minecraftforge.common.ForgeModContainer.removeErroringEntities)
                    {
                        net.minecraftforge.fml.common.FMLLog.log.fatal("{}", crashreport1.getCompleteReport());
                        removeEntity(entity2);
                    }
                    else
                        throw new ReportedException(crashreport1);
                }
            }

            this.profiler.endSection();
            this.profiler.startSection("remove");

            if (entity2.isDead)
            {
                int l1 = entity2.chunkCoordX;
                int i2 = entity2.chunkCoordZ;

                if (entity2.addedToChunk && this.isChunkLoaded(l1, i2, true))
                {
                    this.getChunk(l1, i2).removeEntity(entity2);
                }

                this.loadedEntityList.remove(i1--);
                this.onEntityRemoved(entity2);
            }

            this.profiler.endSection();
        }

        this.profiler.endStartSection("blockEntities");

        this.processingLoadedTiles = true; //FML Move above remove to prevent CMEs

        if (!this.tileEntitiesToBeRemoved.isEmpty())
        {
            for (TileEntity tile : tileEntitiesToBeRemoved)
            {
                tile.onChunkUnload();
            }

            // forge: faster "contains" makes this removal much more efficient
            java.util.Set<TileEntity> remove = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            remove.addAll(tileEntitiesToBeRemoved);
            this.tickableTileEntities.removeAll(remove);
            remove.forEach(this.TickTimeHash::remove);
            this.loadedTileEntityList.removeAll(remove);
            this.tileEntitiesToBeRemoved.clear();
        }

        Iterator<TileEntity> iterator = this.tickableTileEntities.iterator();
        this.TimeSum = 0L;
        this.RunCount = 0;

        while (iterator.hasNext()) {
            TileEntity tileentity = iterator.next();
            BlockPos tilePos = tileentity.getPos();
            TileEntity tile = this.getTileEntity(tilePos);
            TickBalanceStorage tickBalance = this.TickTimeHash.get(tilePos);
            if (tickBalance.getStopTickCycle() == 0) {
                long nanoTime = System.nanoTime();
                if (!tileentity.isInvalid() && tileentity.hasWorld() && tile == tileentity && !tile.isInvalid() && tile.hasWorld()) {
                    BlockPos blockpos = tileentity.getPos();
                    if (this.isBlockLoaded(blockpos, false) && this.worldBorder.contains(blockpos)) //Forge: Fix TE's getting an extra tick on the client side....
                    {
                        try {
                            this.profiler.func_194340_a(() ->
                                    String.valueOf((Object) TileEntity.getKey(tileentity.getClass())));
                            net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
                            if (tileentity instanceof ITickable) {
                                ((ITickable) tileentity).update();
                            }
                            net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
                            this.profiler.endSection();
                        } catch (Throwable throwable) {
                            CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                            CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
                            tileentity.addInfoToCrashReport(crashreportcategory2);
                            if (net.minecraftforge.common.ForgeModContainer.removeErroringTileEntities) {
                                net.minecraftforge.fml.common.FMLLog.log.fatal("{}", crashreport2.getCompleteReport());
                                tileentity.invalidate();
                                this.removeTileEntity(tileentity.getPos());
                            } else {
                                try {

                                    tileentity.invalidate();
                                    setBlockToAir(tileentity.getPos());
                                    this.removeTileEntity(tileentity.getPos());
                                    System.out.println("-----ERROR-----");
                                    System.out.println(" ERROR BlockPos: " + tileentity.getPos().toString());
                                    System.out.println(" Destroy The Block ");
                                    System.out.println("------END------");

                                    ReportedException RE = new ReportedException(crashreport2);
                                    RE.printStackTrace();

                                } catch (Exception e) {
                                    e.printStackTrace();

                                    Thread.sleep(100);
                                    throw new ReportedException(crashreport2);
                                }
                            }
                        }
                    }
                }

                if (tileentity.isInvalid()) {
                    iterator.remove();
                    this.loadedTileEntityList.remove(tileentity);

                    if (this.isBlockLoaded(tileentity.getPos())) {
                        //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
                        Chunk chunk = this.getChunk(tileentity.getPos());
                        if (chunk.getTileEntity(tileentity.getPos(), net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK) == tileentity)
                            chunk.removeTileEntity(tileentity.getPos());
                    }
                }
                long subtract = System.nanoTime() - nanoTime;
                tickBalance.setTime(subtract);

                this.TimeSum += subtract;
                this.RunCount++;
            } else {
                tickBalance.setStopTickCycle(tickBalance.getStopTickCycle() - 1);
            }
            this.TickTimeHash.replace(tilePos, tickBalance);
        }

        //標準の1Tickより、どれだけ遅れているかを算出
        long subtract = this.TimeSum - Modfix.OneTickNanoBase;
        AtomicLong sum = new AtomicLong(0L);
        HashMap<BlockPos, TickBalanceStorage> slowlyTickTile = new HashMap<>();
        this.TickTimeHash.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparing(TickBalanceStorage::getStopTickCycle).reversed()))
                .filter(e -> e.getValue().getStopTickCycle() == 0).forEach(e -> {
            if(subtract > sum.get()) {
                sum.addAndGet(e.getValue().getTime());
                slowlyTickTile.put(e.getKey(), e.getValue());
            }
        });

        this.RunCount -= slowlyTickTile.size();
        if(this.RunCount > 0) {
            long speed = Modfix.OneTickNanoBase / this.RunCount;
            int count = (int)(subtract / speed);

            for(Map.Entry<BlockPos, TickBalanceStorage> entry : slowlyTickTile.entrySet()) {
                TickBalanceStorage balanceStorage = entry.getValue();
                //重いものほど分配を減らす(減算)
                balanceStorage.setStopTickCycle((int)((subtract - balanceStorage.getTime()) / subtract * count));
                this.TickTimeHash.replace(entry.getKey(), balanceStorage);
            }
        }
        slowlyTickTile.clear();

        this.processingLoadedTiles = false;
        this.profiler.endStartSection("pendingBlockEntities");

        if (!this.addedTileEntityList.isEmpty())
        {
            for (int j1 = 0; j1 < this.addedTileEntityList.size(); ++j1)
            {
                TileEntity tileentity1 = this.addedTileEntityList.get(j1);

                if (!tileentity1.isInvalid())
                {
                    if (!this.loadedTileEntityList.contains(tileentity1))
                    {
                        this.addTileEntity(tileentity1);
                    }

                    if (this.isBlockLoaded(tileentity1.getPos()))
                    {
                        Chunk chunk = this.getChunk(tileentity1.getPos());
                        IBlockState iblockstate = chunk.getBlockState(tileentity1.getPos());
                        chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                        this.notifyBlockUpdate(tileentity1.getPos(), iblockstate, iblockstate, 3);
                    }
                }
            }

            this.addedTileEntityList.clear();
        }

        this.profiler.endSection();
        this.profiler.endSection();

        ci.cancel();
    }

    /**
     * @author Aki
     * @reason Fix TickBalance
     */
    @Overwrite
    public boolean addTileEntity(TileEntity tile)
    {
        // Forge - set the world early as vanilla doesn't set it until next tick
        if (tile.getWorld() != (World)(Object)this) tile.setWorld((World)(Object)this);
        // Forge: wait to add new TE if we're currently processing existing ones
        if (processingLoadedTiles) return addedTileEntityList.add(tile);

        boolean flag = this.loadedTileEntityList.add(tile);

        if (flag && tile instanceof ITickable)
        {
            this.tickableTileEntities.add(tile);
            this.TickTimeHash.put(tile.getPos(), new TickBalanceStorage());
        }
        tile.onLoad();

        if (this.isRemote)
        {
            BlockPos blockpos1 = tile.getPos();
            IBlockState iblockstate1 = this.getBlockState(blockpos1);
            this.notifyBlockUpdate(blockpos1, iblockstate1, iblockstate1, 2);
        }

        return flag;
    }

    /**
     * @author Aki
     * @reason FixTickBalance
     */
    @Overwrite
    public void removeTileEntity(BlockPos pos)
    {
        TileEntity tileentity2 = this.getTileEntity(pos);

        if (tileentity2 != null && this.processingLoadedTiles)
        {
            tileentity2.invalidate();
            this.addedTileEntityList.remove(tileentity2);
            if (!(tileentity2 instanceof ITickable)) //Forge: If they are not tickable they wont be removed in the update loop.
                this.loadedTileEntityList.remove(tileentity2);
        }
        else
        {
            if (tileentity2 != null)
            {
                this.addedTileEntityList.remove(tileentity2);
                this.loadedTileEntityList.remove(tileentity2);
                this.tickableTileEntities.remove(tileentity2);
                this.TickTimeHash.remove(tileentity2.getPos());
            }

            this.getChunk(pos).removeTileEntity(pos);
        }
        this.updateComparatorOutputLevel(pos, getBlockState(pos).getBlock()); //Notify neighbors of changes
    }
}
