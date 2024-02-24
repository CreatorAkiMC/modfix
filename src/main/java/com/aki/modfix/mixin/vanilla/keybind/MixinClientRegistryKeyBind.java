package com.aki.modfix.mixin.vanilla.keybind;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.fix.GameSettingsExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ClientRegistry.class, remap = false, priority = Modfix.ModPriority)
public class MixinClientRegistryKeyBind {

    /**
     * @author Aki
     * @reason Replace KeyBinding System
     */
    @Overwrite(remap = false)
    public static void registerKeyBinding(KeyBinding key) {
        ((GameSettingsExtended) Minecraft.getMinecraft().gameSettings).SetModRegisteredBinding(ArrayUtils.add(((GameSettingsExtended) Minecraft.getMinecraft().gameSettings).ModRegisteredBinding(), key));
    }
}
