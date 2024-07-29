package com.aki.modfix.mixin.vanilla.rendering;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.gl.extensions.IWeightedBakedModelExtension;
import net.minecraft.client.renderer.block.model.WeightedBakedModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = WeightedBakedModel.class, priority = Modfix.ModPriority)
public class MixinWeightedBakedModel implements IWeightedBakedModelExtension {
    @Shadow @Final private int totalWeight;

    @Shadow @Final private List<WeightedBakedModel.WeightedModel> models;

    @Override
    public int getIndex(long rand) {
        int weight = Math.abs((int)rand >> 16) % this.totalWeight;
        int i = 0;
        while ((weight -= this.models.get(i).itemWeight) > 0) {
            i++;
        }
        return i;
    }
}
