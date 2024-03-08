package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.modfix.Modfix;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = World.class, priority = Modfix.ModPriority)
public abstract class MixinWorld {

}
