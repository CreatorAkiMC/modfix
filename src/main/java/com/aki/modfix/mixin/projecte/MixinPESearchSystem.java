package com.aki.modfix.mixin.projecte;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Mixin(value = TransmutationInventory.class, remap = false)
public class MixinPESearchSystem {

    @Shadow(remap = false) public String filter;

    @Shadow(remap = false) @Final public EntityPlayer player;

    /**
     * @author Aki
     * @reason Replace SearchEngine
     */
    @Overwrite(remap = false)
    private boolean doesItemMatchFilter(ItemStack stack) {
        String displayName;
        String registryName;
        String ModId;
        try {
            displayName = stack.getDisplayName().toLowerCase(Locale.ROOT);
            registryName = stack.getItem().getRegistryName().getPath().toLowerCase(Locale.ROOT);
            ModId = stack.getItem().getRegistryName().getNamespace().toLowerCase(Locale.ROOT);
            List<String> tooltips = new ArrayList<>(stack.getTooltip(this.player, ITooltipFlag.TooltipFlags.ADVANCED));

            if(displayName != null && registryName != null && ModId != null) {
                String low = this.filter.toLowerCase(Locale.ROOT);
                if (low.contains("@")) {
                    return ModId.contains(low.replace("@", ""));
                } else {
                    if((this.filter.length() <= 0 || displayName.contains(low) || registryName.contains(low)))
                        return true;

                    for (String s : tooltips) {
                        if (s.toLowerCase(Locale.ROOT).contains(low)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception var4) {
            var4.printStackTrace();
            return true;
        }
    }
}
