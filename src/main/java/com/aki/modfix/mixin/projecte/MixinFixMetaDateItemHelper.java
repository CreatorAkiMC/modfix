package com.aki.modfix.mixin.projecte;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = moze_intel.projecte.utils.ItemHelper.class, remap = false)
public class MixinFixMetaDateItemHelper {

    @Inject(method = "basicAreStacksEqual", at = @At("HEAD"), cancellable = true, remap = false)
    private static void basicAreStacksEqualFix(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        boolean b = false;
        NonNullList<ItemStack> stacks = NonNullList.create();
        stack1.getItem().getSubItems(CreativeTabs.SEARCH, stacks);
        //cir.setReturnValue(stack1.getItem() == stack2.getItem() && stack1.getItemDamage() == stack2.getItemDamage());
        if (stack1.getItem() == stack2.getItem() && stack1.getItemDamage() == stack2.getItemDamage() && (stack1.getMetadata() == stack2.getMetadata())) {
            cir.setReturnValue(true);
        } else {
            for (ItemStack stack : stacks) {
                if (stack.getMetadata() == stack2.getMetadata()) {
                    cir.setReturnValue(true);
                    break;
                }
            }
        }
        cir.setReturnValue(false);
    }
}
