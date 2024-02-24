package com.aki.modfix.util.fix;

import com.rwtema.extrautils2.blocks.BlockPassiveGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

import java.util.Locale;
import java.util.Objects;

public class ExtraUtils2AddKnowledge {
    public void addKnowledge(FixDefaultImpl impl, ItemStack stack) {
        String RegiName = Objects.requireNonNull(stack.getItem().getRegistryName()).getPath();
        if (RegiName.toLowerCase(Locale.ROOT).equals("machine")) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            stack.writeToNBT(tagCompound);

            BlockPassiveGenerator.GeneratorType[] generatorTypes = BlockPassiveGenerator.GeneratorType.values();

            if (tagCompound.hasKey("Type") && tagCompound.getTag("Type") instanceof NBTTagString) {
                String value = ((NBTTagString) tagCompound.getTag("Type")).getString().replace("extrautils2:", "");

                for (BlockPassiveGenerator.GeneratorType generatorType : generatorTypes) {
                    String rep = generatorType.name();
                    value = value.replace("_" + rep, "");
                }
                if (value.equals("generator")) {
                    for (BlockPassiveGenerator.GeneratorType generatorType : generatorTypes) {
                        ItemStack stack1 = generatorType.newStack(1);
                        if (!impl.hasKnowledge(stack1)) {
                            impl.knowledge.add(stack1);
                        }
                    }
                } else {
                    if (!impl.hasKnowledge(stack)) {
                        impl.knowledge.add(stack);
                    }
                }
            }
        }
    }
}
