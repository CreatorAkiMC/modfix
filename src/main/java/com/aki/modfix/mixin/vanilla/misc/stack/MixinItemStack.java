package com.aki.modfix.mixin.vanilla.misc.stack;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow
    @Final
    private Item item;

    @Shadow
    @Final
    public static ItemStack EMPTY;

    @Shadow
    private int stackSize;

    @Shadow
    public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

    @Shadow
    private CapabilityDispatcher capabilities;
    @Shadow
    private NBTTagCompound stackTagCompound;

    @Shadow
    public abstract boolean hasTagCompound();

    @Unique
    private static Cache<ItemStack, EntityItemFrame> itemFrames;
    @Unique
    private static Cache<ItemStack, Pair<Block, Boolean>> canPlaceCache;
    @Unique
    private static Cache<ItemStack, Pair<Block, Boolean>> canDestroyCache;

    private NBTTagCompound oldStackTagCompound;

    public boolean oldCache = false;

    public boolean first = true;

    @Inject(method = "areCapsCompatible", at = @At("HEAD"), cancellable = true, remap = false)
    public void AreCapsCompatibleFix(ItemStack p_areCapsCompatible_1_, CallbackInfoReturnable<Boolean> cir) {
        if (this.oldStackTagCompound != this.stackTagCompound || first) {
            this.oldStackTagCompound = this.stackTagCompound;
            this.oldCache = areCapsCompatible2(p_areCapsCompatible_1_);
            this.first = false;
        }

        cir.setReturnValue(oldCache);
    }

    public boolean areCapsCompatible2(ItemStack other) {
        if (this.capabilities == null) {
            if (other.capabilities == null) {
                return true;
            } else {
                return other.capabilities.areCompatible(null);
            }
        } else {
            return this.capabilities.areCompatible(other.capabilities);
        }
    }

    /**
     * @author Aki
     * @reason Replace
     */
    @Overwrite
    public boolean isEmpty() {
        return (this.item == null || this.item == Items.AIR || this.stackSize <= 0);
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     */
    @Overwrite
    public boolean isOnItemFrame() {
        return itemFrames != null && itemFrames.getIfPresent((ItemStack) (Object) this) != null;
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     */
    @Overwrite
    public void setItemFrame(EntityItemFrame frame) {
        if (itemFrames == null) {
            itemFrames = CacheBuilder.newBuilder().weakKeys().weakValues().build();
        }
        if (frame == null) {
            itemFrames.invalidate((ItemStack) (Object) this);
        } else {
            itemFrames.put((ItemStack) (Object) this, frame);
        }
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     */
    @Nullable
    @Overwrite
    public EntityItemFrame getItemFrame() {
        return itemFrames == null ? null : itemFrames.getIfPresent((ItemStack) (Object) this);
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     */
    @Overwrite
    public boolean canPlaceOn(Block blockIn) {
        if (canPlaceCache == null) {
            canPlaceCache = CacheBuilder.newBuilder().weakKeys().build();
        }
        Pair<Block, Boolean> placeInfo = canPlaceCache.getIfPresent((ItemStack) (Object) this);
        if (placeInfo != null && placeInfo.getLeft() == blockIn) {
            return placeInfo.getRight();
        }
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9)) {
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanPlaceOn", 8);
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));
                if (block == blockIn) {
                    canPlaceCache.put((ItemStack) (Object) this, Pair.of(blockIn, true));
                    return true;
                }
            }
        }
        canPlaceCache.put((ItemStack) (Object) this, Pair.of(blockIn, false));
        return false;
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     */
    @Overwrite
    public boolean canDestroy(Block blockIn) {
        if (canDestroyCache == null) {
            canDestroyCache = CacheBuilder.newBuilder().weakKeys().build();
        }
        Pair<Block, Boolean> destroyInfo = canDestroyCache.getIfPresent((ItemStack) (Object) this);
        if (destroyInfo != null && destroyInfo.getLeft() == blockIn) {
            return destroyInfo.getRight();
        }
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9)) {
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanDestroy", 8);
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));
                if (block == blockIn) {
                    canDestroyCache.put((ItemStack) (Object) this, Pair.of(blockIn, true));
                    return true;
                }
            }
        }
        canDestroyCache.put((ItemStack) (Object) this, Pair.of(blockIn, false));
        return false;
    }
}
