package com.aki.modfix.mixin.vanilla.fast_load;

import com.aki.modfix.Modfix;
import net.minecraftforge.fml.common.discovery.DirectoryDiscoverer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = DirectoryDiscoverer.class, priority = Modfix.ModPriority)
public class MixinDirectoryDiscoverer {
}
