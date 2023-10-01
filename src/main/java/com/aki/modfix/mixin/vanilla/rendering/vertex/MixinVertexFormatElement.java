package com.aki.modfix.mixin.vanilla.rendering.vertex;

import com.aki.modfix.Modfix;
import com.aki.modfix.util.gl.vertex.*;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = VertexFormatElement.class, priority = Modfix.ModPriority)
public class MixinVertexFormatElement implements ExtendedVertexFormatElement {

    @Shadow
    @Final
    private VertexFormatElement.EnumType type;
    @Shadow
    @Final
    private VertexFormatElement.EnumUsage usage;
    @Shadow
    @Final
    private int index;

    @Unique
    private VertexFormat vertexFormat;
    @Unique
    private int offset;
    @Unique
    private VertexFormatElement next;
    @Unique
    private VertexConsumer vertexConsumer;

    @ModifyVariable(method = "<init>", at = @At("RETURN"), index = 1, ordinal = 0, name = "indexIn", argsOnly = true)
    public int init(int indexIn) {
        switch (usage) {
            case POSITION:
                vertexConsumer = PositionUploader.fromType(type);
                break;
            case COLOR:
                vertexConsumer = ColorUploader.fromType(type);
                break;
            case UV:
                vertexConsumer = TextureCoordinateUploader.fromType(type);
                break;
            case NORMAL:
                vertexConsumer = NormalUploader.fromType(type);
                break;
            default:
                break;
        }
        return indexIn;
    }

    @Override
    public void setVertexFormat(VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;
    }

    @Override
    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setNext(VertexFormatElement next) {
        this.next = next;
    }

    @Override
    public VertexFormatElement getNext() {
        return next;
    }

    @Override
    public VertexConsumer getVertexConsumer() {
        return vertexConsumer;
    }

}
