package com.aki.modfix.mixin.gvc;

import gvcr2.block.tile.TileEntity_GunCrafter;
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

@Pseudo
@Mixin(value = TileEntity_GunCrafter.class, remap = false)
public abstract class MixinGvcGunCrafterFix extends gvcr2.block.tile.TileEntityCrafterBase implements ITickable {
    @Shadow public boolean do_sell;

    @Shadow public int id;

    @Shadow public int facing;

    public gvcr2.mod_GVCR2 mod_GVCR2 = gvcr2.mod_GVCR2.INSTANCE;

    @Inject(method = "update", at = @At("HEAD"), remap = false, cancellable = true, require = 1)
    public void updateFix(CallbackInfo ci) {
        if (!world.isRemote) {
            if (this.world.getBlockState(pos).getBlock() instanceof gvcr2.block.Block_GunCrafter) {
                gvcr2.block.Block_GunCrafter gubcrafter = (gvcr2.block.Block_GunCrafter) this.world.getBlockState(pos).getBlock();
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

            if (this.world.getBlockState(pos).getBlock() == mod_GVCR2.gvcr2_block_guncrafter_1) {
                id = 0;
                ItemStack eme = (ItemStack) this.furnaceItemStacks.get(0);
                ItemStack iron = (ItemStack) this.furnaceItemStacks.get(1);
                ItemStack red = (ItemStack) this.furnaceItemStacks.get(2);
                ItemStack output = (ItemStack) this.furnaceItemStacks.get(3);

                if (mod_GVCR2.gun_item_1[sell_id] != null) {
                    if (!eme.isEmpty() && eme.getItem() == Items.EMERALD
                            && !iron.isEmpty() && iron.getItem() == Items.IRON_INGOT
                            && !red.isEmpty() && red.getItem() == Items.REDSTONE) {
                        if (eme.getCount() >= mod_GVCR2.gun_eme_1[sell_id]
                                && iron.getCount() >= mod_GVCR2.gun_iron_1[sell_id]
                                && red.getCount() >= mod_GVCR2.gun_red_1[sell_id]) {
                            if (do_sell) {
                                if (output.isEmpty()) {
                                    this.furnaceItemStacks.set(3, new ItemStack(mod_GVCR2.gun_item_1[sell_id], 1));
                                    eme.shrink(mod_GVCR2.gun_eme_1[sell_id]);
                                    iron.shrink(mod_GVCR2.gun_iron_1[sell_id]);
                                    red.shrink(mod_GVCR2.gun_red_1[sell_id]);
                                }
                                do_sell = false;
                            }
                        }
                    }
                }
            }

            if (this.world.getBlockState(pos).getBlock() == mod_GVCR2.gvcr2_block_guncrafter_2) {
                id = 1;
                ItemStack eme = (ItemStack) this.furnaceItemStacks.get(0);
                ItemStack iron = (ItemStack) this.furnaceItemStacks.get(1);
                ItemStack red = (ItemStack) this.furnaceItemStacks.get(2);
                ItemStack output = (ItemStack) this.furnaceItemStacks.get(3);

                if (mod_GVCR2.gun_item_2[sell_id] != null) {
                    if (!eme.isEmpty() && eme.getItem() == Items.EMERALD
                            && !iron.isEmpty() && iron.getItem() == Items.IRON_INGOT
                            && !red.isEmpty() && red.getItem() == Items.REDSTONE) {
                        if (eme.getCount() >= mod_GVCR2.gun_eme_2[sell_id]
                                && iron.getCount() >= mod_GVCR2.gun_iron_2[sell_id]
                                && red.getCount() >= mod_GVCR2.gun_red_2[sell_id]) {
                            if (do_sell) {
                                if (output.isEmpty()) {
                                    this.furnaceItemStacks.set(3, new ItemStack(mod_GVCR2.gun_item_2[sell_id], 1));
                                    eme.shrink(mod_GVCR2.gun_eme_2[sell_id]);
                                    iron.shrink(mod_GVCR2.gun_iron_2[sell_id]);
                                    red.shrink(mod_GVCR2.gun_red_2[sell_id]);
                                }
                                do_sell = false;
                            }
                        }
                    }
                }
            }

            if (this.world.getBlockState(pos).getBlock() == mod_GVCR2.gvcr2_block_guncrafter_3) {
                id = 2;
                ItemStack eme = (ItemStack) this.furnaceItemStacks.get(0);
                ItemStack iron = (ItemStack) this.furnaceItemStacks.get(1);
                ItemStack red = (ItemStack) this.furnaceItemStacks.get(2);
                ItemStack output = (ItemStack) this.furnaceItemStacks.get(3);

                if (mod_GVCR2.gun_item_3[sell_id] != null) {
                    if (!eme.isEmpty() && eme.getItem() == Items.EMERALD
                            && !iron.isEmpty() && iron.getItem() == Items.IRON_INGOT
                            && !red.isEmpty() && red.getItem() == Items.REDSTONE) {
                        if (eme.getCount() >= mod_GVCR2.gun_eme_3[sell_id]
                                && iron.getCount() >= mod_GVCR2.gun_iron_3[sell_id]
                                && red.getCount() >= mod_GVCR2.gun_red_3[sell_id]) {
                            if (do_sell) {
                                if (output.isEmpty()) {
                                    this.furnaceItemStacks.set(3, new ItemStack(mod_GVCR2.gun_item_3[sell_id], 1));
                                    eme.shrink(mod_GVCR2.gun_eme_3[sell_id]);
                                    iron.shrink(mod_GVCR2.gun_iron_3[sell_id]);
                                    red.shrink(mod_GVCR2.gun_red_3[sell_id]);
                                }
                                do_sell = false;
                            }
                        }
                    }
                }
            }
        }

        ci.cancel();
    }
}
