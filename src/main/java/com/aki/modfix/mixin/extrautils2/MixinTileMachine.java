package com.aki.modfix.mixin.extrautils2;

import com.aki.modfix.Modfix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(value = com.rwtema.extrautils2.machine.TileMachine.class, priority = Modfix.ModPriority)
public interface MixinTileMachine {
    @Accessor(value = "type", remap = false)
    String getType();
}
