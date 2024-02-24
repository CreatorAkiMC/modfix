package com.aki.modfix.mixin.gvc;

import gvcr2.block.Block_GunRack;
import gvcr2.block.tile.TileEntity_GunRack;
import gvcr2.mod_GVCR2;
import gvcr2.network.GVCR2ClientMessageKeyPressed;
import gvcr2.network.GVCR2PacketHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = TileEntity_GunRack.class, remap = false)
public abstract class MixinGvcGunRack extends TileEntityLockable implements ITickable {

    @Shadow
    public boolean clientgetter;

    @Shadow
    public NonNullList<ItemStack> furnaceItemStacks;

    @Shadow
    public int id;

    @Shadow
    public int facing;

    @Inject(method = "update", at = @At("HEAD"), remap = false, cancellable = true, require = 1)
    public void updateFix(CallbackInfo ci) {
        if (!world.isRemote) {
            if (this.world.getBlockState(pos).getBlock() instanceof Block_GunRack) {
                Block_GunRack gubcrafter = (Block_GunRack) this.world.getBlockState(pos).getBlock();
                EnumFacing enumfacing = this.world.getBlockState(pos).getValue(Block_GunRack.FACING);
                if (enumfacing == EnumFacing.SOUTH) {
                    facing = 1;
                } else if (enumfacing == EnumFacing.WEST) {
                    facing = 2;
                } else if (enumfacing == EnumFacing.EAST) {
                    facing = 3;
                } else {
                    facing = 0;
                }
            }


            if (this.world.getBlockState(this.pos).getBlock() == mod_GVCR2.gvcr2_block_gunrack_rsin) {
                id = 1;
            } else if (this.world.getBlockState(this.pos).getBlock() == mod_GVCR2.gvcr2_block_gunrack_rswn) {
                id = 2;
            } else if (this.world.getBlockState(this.pos).getBlock() == mod_GVCR2.gvcr2_block_gunrack_hti) {
                id = 3;
            } else if (this.world.getBlockState(this.pos).getBlock() == mod_GVCR2.gvcr2_block_gunrack_rti1) {
                id = 4;
            } else if (this.world.getBlockState(this.pos).getBlock() == mod_GVCR2.gvcr2_block_gunrack_rti2) {
                id = 5;
            } else {
                id = 0;
            }

            if (!this.world.isRemote) {
                if (!this.furnaceItemStacks.get(0).isEmpty()) {
                    Item item = this.furnaceItemStacks.get(0).getItem();
                    if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
                        for (EntityPlayerMP playermp : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                            GVCR2PacketHandler.INSTANCE2.sendTo(new GVCR2ClientMessageKeyPressed(3,
                                    this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()
                                    , Item.getIdFromItem(item), true), playermp);
                        }
                    }
                }
                if (!this.furnaceItemStacks.get(1).isEmpty()) {
                    Item item = this.furnaceItemStacks.get(1).getItem();
                    if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
                        for (EntityPlayerMP playermp : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                            GVCR2PacketHandler.INSTANCE2.sendTo(new GVCR2ClientMessageKeyPressed(4,
                                    this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()
                                    , Item.getIdFromItem(item), true), playermp);
                        }
                    }
                }
                if (!this.furnaceItemStacks.get(2).isEmpty()) {
                    Item item = this.furnaceItemStacks.get(2).getItem();
                    if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
                        for (EntityPlayerMP playermp : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                            GVCR2PacketHandler.INSTANCE2.sendTo(new GVCR2ClientMessageKeyPressed(5,
                                    this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()
                                    , Item.getIdFromItem(item), true), playermp);
                        }
                    }
                }
                clientgetter = true;
            }
        }
        ci.cancel();
    }
}
