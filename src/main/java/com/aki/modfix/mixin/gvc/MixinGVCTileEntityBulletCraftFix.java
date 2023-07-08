package com.aki.modfix.mixin.gvc;

import gvcr2.block.tile.TileEntity_BulletCrafter;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TileEntityのupdateが( !world.isRemote )で行われていないのが原因 ( 状態が二重で重なってしまうから )
 * */
@Pseudo
@Mixin(value = TileEntity_BulletCrafter.class, remap = false)
public abstract class MixinGVCTileEntityBulletCraftFix extends gvcr2.block.tile.TileEntityCrafterBase implements ITickable {

    @Shadow public int facing;

    @Shadow public int id;

    @Shadow public boolean do_sell;

    public gvcr2.mod_GVCR2 mod_GVCR2 = gvcr2.mod_GVCR2.INSTANCE;

    @Inject(method = "update", at = @At("HEAD"), remap = false, cancellable = true, require = 1)
    public void updateFix(CallbackInfo ci)
    {
        if(!world.isRemote) {
            if (this.world.getBlockState(pos).getBlock() instanceof gvcr2.block.Block_BulletCrafter) {
                gvcr2.block.Block_BulletCrafter gubcrafter = (gvcr2.block.Block_BulletCrafter) this.world.getBlockState(pos).getBlock();
                EnumFacing enumfacing = (EnumFacing) this.world.getBlockState(pos).getValue(gubcrafter.FACING);
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


            if (this.world.getBlockState(pos).getBlock() == mod_GVCR2.gvcr2_block_bulletcrafter_1) {
                id = 0;
                ItemStack eme = (ItemStack) this.furnaceItemStacks.get(0);
                ItemStack iron = (ItemStack) this.furnaceItemStacks.get(1);
                ItemStack output = (ItemStack) this.furnaceItemStacks.get(3);


                if (mod_GVCR2.bullet_item_1[sell_id] != null) {
                    if (!eme.isEmpty() && eme.getItem() == Items.GUNPOWDER
                            && !iron.isEmpty() && iron.getItem() == Items.IRON_INGOT) {
                        if (eme.getCount() >= mod_GVCR2.bullet_eme_1[sell_id]
                                && iron.getCount() >= mod_GVCR2.bullet_iron_1[sell_id]) {
                            if (do_sell) {
                                if (!output.isEmpty() && output.getItem() == mod_GVCR2.bullet_item_1[sell_id]) {
                                    if (output.getCount() <= 64 - mod_GVCR2.bullet_kazu_1[sell_id]) {
                                        this.furnaceItemStacks.set(3, new ItemStack(mod_GVCR2.bullet_item_1[sell_id], mod_GVCR2.bullet_kazu_1[sell_id] + output.getCount()));
                                        eme.shrink(mod_GVCR2.bullet_eme_1[sell_id]);
                                        iron.shrink(mod_GVCR2.bullet_iron_1[sell_id]);
                                    }
                                } else {
                                    this.furnaceItemStacks.set(3, new ItemStack(mod_GVCR2.bullet_item_1[sell_id], mod_GVCR2.bullet_kazu_1[sell_id]));
                                    eme.shrink(mod_GVCR2.bullet_eme_1[sell_id]);
                                    iron.shrink(mod_GVCR2.bullet_iron_1[sell_id]);
                                }
                                do_sell = false;
                            }
                        } else {
                            //this.furnaceItemStacks.set(3, output.EMPTY);
                        }
                    } else {
                        //this.furnaceItemStacks.set(3, output.EMPTY);
                        //cansell = false;
                    }
                }
            }
        }

        ci.cancel();
    }
}
