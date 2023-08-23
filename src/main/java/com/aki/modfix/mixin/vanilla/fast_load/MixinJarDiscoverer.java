package com.aki.modfix.mixin.vanilla.fast_load;

import com.aki.modfix.Modfix;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = JarDiscoverer.class, priority = Modfix.ModPriority)
public class MixinJarDiscoverer {
}
