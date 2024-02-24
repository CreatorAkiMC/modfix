package com.aki.modfix.util.fix;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.KnowledgeSyncPKT;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class FixDefaultImpl implements IKnowledgeProvider {
    @Nullable
    public final EntityPlayer player;
    public List<ItemStack> knowledge;
    public final IItemHandlerModifiable inputLocks;
    public long emc;
    public boolean fullKnowledge;

    public FixDefaultImpl(EntityPlayer player) {
        this.knowledge = new ArrayList<>();
        this.inputLocks = new ItemStackHandler(9);
        this.emc = 0L;
        this.fullKnowledge = false;
        this.player = player;
    }

    private void fireChangedEvent() {
        if (this.player != null && !this.player.world.isRemote) {
            MinecraftForge.EVENT_BUS.post(new PlayerKnowledgeChangeEvent(this.player));
        }

    }

    public boolean hasFullKnowledge() {
        return this.fullKnowledge;
    }

    public void setFullKnowledge(boolean fullKnowledge) {
        boolean changed = this.fullKnowledge != fullKnowledge;
        this.fullKnowledge = fullKnowledge;
        if (changed) {
            this.fireChangedEvent();
        }

    }

    public void clearKnowledge() {
        this.knowledge.clear();
        this.fullKnowledge = false;
        this.fireChangedEvent();
    }

    public boolean hasKnowledge(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else if (this.fullKnowledge) {
            return true;
        } else {
            /*Iterator<ItemStack> var2 = this.knowledge.iterator();

            ItemStack s;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                s = (ItemStack)var2.next();
            } while(!ItemHelper.basicAreStacksEqual(s, stack));*/

            //int oldSize = this.knowledge.size();

            Stream<ItemStack> stream = this.knowledge.stream().parallel();
            int CompSize = (int) stream.filter((stack1) -> ItemHelper.basicAreStacksEqual(stack1, stack)).count();

            return CompSize > 0;
        }
    }

    public boolean addKnowledge(@Nonnull ItemStack stack) {
        if (this.fullKnowledge) {
            return false;
        } else if (stack.getItem() == ObjHandler.tome) {
            if (!this.hasKnowledge(stack)) {
                this.knowledge.add(stack);
            }

            this.fullKnowledge = true;
            this.fireChangedEvent();
            return true;
        } else if (!this.hasKnowledge(stack)) {
            this.knowledge.add(stack);
            try {

                long time = System.currentTimeMillis();

                if (Objects.requireNonNull(stack.getItem().getRegistryName()).getNamespace().toLowerCase(Locale.ROOT).equals("extrautils2") && Loader.isModLoaded("extrautils2")) {
                    (new ExtraUtils2AddKnowledge()).addKnowledge(this, stack);
                } else {

                    NonNullList<ItemStack> subs = NonNullList.create();
                    stack.getItem().getSubItems(CreativeTabs.SEARCH, subs);

                    System.out.println("KnowIMPL Size: " + subs.size() + ", Time: " + (System.currentTimeMillis() - time));

                    subs.stream().parallel().filter(s -> !this.hasKnowledge(s)).forEach(this.knowledge::add);
                }

                /*for (ItemStack sub : subs) {
                    if (!this.hasKnowledge(sub)) {
                        this.knowledge.add(sub);
                        //this.addKnowledge(sub);
                    }
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.fireChangedEvent();
            return true;
        } else {
            return false;
        }
    }

    public boolean removeKnowledge(@Nonnull ItemStack stack) {
        boolean removed = false;
        if (stack.getItem() == ObjHandler.tome) {
            this.fullKnowledge = false;
            removed = true;
        }

        if (this.fullKnowledge) {
            return false;
        } else {
            Iterator<ItemStack> iter = this.knowledge.iterator();

            while (iter.hasNext()) {
                if (ItemHelper.basicAreStacksEqual(stack, iter.next())) {
                    iter.remove();
                    removed = true;
                }
            }

            if (removed) {
                this.fireChangedEvent();
            }

            return removed;
        }
    }

    @Nonnull
    public List<ItemStack> getKnowledge() {
        return this.fullKnowledge ? Transmutation.getCachedTomeKnowledge() : Collections.unmodifiableList(this.knowledge);
    }

    @Nonnull
    public IItemHandlerModifiable getInputAndLocks() {
        return this.inputLocks;
    }

    public long getEmc() {
        return this.emc;
    }

    public void setEmc(long emc) {
        this.emc = emc;
    }

    public void sync(@Nonnull EntityPlayerMP player) {
        PacketHandler.sendTo(new KnowledgeSyncPKT(this.serializeNBT()), player);
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound properties = new NBTTagCompound();
        properties.setLong("transmutationEmc", this.emc);
        NBTTagList knowledgeWrite = new NBTTagList();

        for (ItemStack i : this.knowledge) {
            NBTTagCompound tag = i.writeToNBT(new NBTTagCompound());
            knowledgeWrite.appendTag(tag);
        }

        properties.setTag("knowledge", knowledgeWrite);
        properties.setTag("inputlock", Objects.requireNonNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(this.inputLocks, null)));
        properties.setBoolean("fullknowledge", this.fullKnowledge);
        return properties;
    }

    public void deserializeNBT(NBTTagCompound properties) {
        this.emc = properties.getLong("transmutationEmc");
        NBTTagList list = properties.getTagList("knowledge", 10);

        int i;
        for (i = 0; i < list.tagCount(); ++i) {
            ItemStack item = new ItemStack(list.getCompoundTagAt(i));
            if (!item.isEmpty()) {
                this.knowledge.add(item);
            }
        }

        this.pruneStaleKnowledge();
        this.pruneDuplicateKnowledge();

        for (i = 0; i < this.inputLocks.getSlots(); ++i) {
            this.inputLocks.setStackInSlot(i, ItemStack.EMPTY);
        }

        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(this.inputLocks, null, properties.getTagList("inputlock", 10));
        this.fullKnowledge = properties.getBoolean("fullknowledge");
    }

    private void pruneDuplicateKnowledge() {
        ItemHelper.removeEmptyTags(this.knowledge);
        ItemHelper.compactItemListNoStacksize(this.knowledge);

        for (ItemStack s : this.knowledge) {
            if (s.getCount() > 1) {
                s.setCount(1);
            }
        }

    }

    private void pruneStaleKnowledge() {
        this.knowledge.removeIf((stack) -> !EMCHelper.doesItemHaveEmc(stack));
    }
}
