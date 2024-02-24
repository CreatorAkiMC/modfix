package com.aki.modfix.mixin.projecte;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.NBTWhitelist;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;

@Mixin(value = TransmutationInventory.class, remap = false)
public abstract class MixinPEFixNBTMod {


    @Shadow
    @Final
    public EntityPlayer player;

    @Shadow
    @Final
    public IKnowledgeProvider provider;

    @Shadow
    public int learnFlag;

    @Shadow
    public int unlearnFlag;

    @Shadow
    public abstract void updateClientTargets();

    /**
     * @author Aki
     * @reason Fix Item NBT
     */
    @Overwrite(remap = false)
    public void handleKnowledge(ItemStack stack) {
        if (stack.getCount() > 1) {
            stack.setCount(1);
        }

        if (ItemHelper.isDamageable(stack)) {
            stack.setItemDamage(0);
        }

        if (!this.provider.hasKnowledge(stack)) {
            if (stack.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(stack)) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                stack.writeToNBT(tagCompound);
                if (tagCompound.hasKey("Type") && tagCompound.getTag("Type") instanceof NBTTagString && stack.getItem().getRegistryName().getNamespace().toLowerCase(Locale.ROOT).equals("extrautils2")) {
                    NBTTagString type = (NBTTagString) tagCompound.getTag("Type");
                    tagCompound = new NBTTagCompound();

                    tagCompound.setTag("Type", type);

                    stack.setTagCompound(tagCompound);
                } else stack.setTagCompound(null);
            }

            if (!MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(this.player, stack))) {
                this.learnFlag = 300;
                this.unlearnFlag = 0;
                this.provider.addKnowledge(stack);
            }

            if (!this.player.world.isRemote) {
                this.provider.sync((EntityPlayerMP) this.player);
            }
        }

        this.updateClientTargets();
    }

    /**
     * @author Aki
     * @reason Fix Item NBT
     */
    @Overwrite(remap = false)
    public void handleUnlearn(ItemStack stack) {
        if (stack.getCount() > 1) {
            stack.setCount(1);
        }

        if (ItemHelper.isDamageable(stack)) {
            stack.setItemDamage(0);
        }

        if (this.provider.hasKnowledge(stack)) {
            this.unlearnFlag = 300;
            this.learnFlag = 0;
            if (stack.hasTagCompound() && !NBTWhitelist.shouldDupeWithNBT(stack)) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                stack.writeToNBT(tagCompound);
                if (tagCompound.hasKey("Type") && tagCompound.getTag("Type") instanceof NBTTagString && stack.getItem().getRegistryName().getNamespace().toLowerCase(Locale.ROOT).equals("extrautils2")) {
                    NBTTagString type = (NBTTagString) tagCompound.getTag("Type");
                    tagCompound = new NBTTagCompound();

                    tagCompound.setTag("Type", type);

                    stack.setTagCompound(tagCompound);
                } else stack.setTagCompound(null);
            }

            this.provider.removeKnowledge(stack);
            if (!this.player.world.isRemote) {
                this.provider.sync((EntityPlayerMP) this.player);
            }
        }

        this.updateClientTargets();
    }
}
