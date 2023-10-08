package com.aki.modfix.mixin.vanilla.rendering.vertex;

import com.aki.mcutils.APICore.Utils.render.vertex.ExtendedVertexFormatElement;
import com.aki.modfix.Modfix;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(value = VertexFormat.class, priority = Modfix.ModPriority)
public class MixinVertexFormat {

    @Shadow
    @Final
    private List<VertexFormatElement> elements;
    @Shadow
    @Final
    private List<Integer> offsets;
    @Shadow
    private int vertexSize;

    @ModifyVariable(method = "addElement", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.BY, by = -2), index = 1, ordinal = 0, name = "element", argsOnly = true)
    private VertexFormatElement pre_addElement(VertexFormatElement element) {
        element = new VertexFormatElement(element.getIndex(), element.getType(), element.getUsage(),
                element.getElementCount());
        ((ExtendedVertexFormatElement) element).setVertexFormat((VertexFormat) (Object) this);
        ((ExtendedVertexFormatElement) element).setOffset(vertexSize);
        return element;
    }

    @ModifyVariable(method = "addElement", at = @At(value = "RETURN", ordinal = 1), index = 1, ordinal = 0, name = "element", argsOnly = true)
    private VertexFormatElement post_addElement(VertexFormatElement element) {
        if (elements.size() >= 2) {
            ((ExtendedVertexFormatElement) elements.get(elements.size() - 2))
                    .setNext(elements.get(elements.size() - 1));
        }
        ((ExtendedVertexFormatElement) elements.get(elements.size() - 1)).setNext(elements.get(0));
        return element;
    }

}
