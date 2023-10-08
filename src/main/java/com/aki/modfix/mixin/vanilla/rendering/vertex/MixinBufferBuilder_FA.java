package com.aki.modfix.mixin.vanilla.rendering.vertex;

import com.aki.mcutils.APICore.Utils.matrixutil.MemoryUtil;
import com.aki.mcutils.APICore.Utils.render.vertex.ExtendedBufferBuilder;
import com.aki.mcutils.APICore.Utils.render.vertex.ExtendedVertexFormatElement;
import com.aki.modfix.Modfix;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Mixin(value = BufferBuilder.class, priority = Modfix.ModPriority)
public abstract class MixinBufferBuilder_FA implements ExtendedBufferBuilder {
    @Shadow private VertexFormat vertexFormat;

    @Shadow private int vertexCount;

    @Shadow private VertexFormatElement vertexFormatElement;

    @Shadow private int vertexFormatIndex;

    @Shadow private boolean noColor;

    @Shadow private IntBuffer rawIntBuffer;

    @Shadow protected abstract int getBufferSize();

    @Shadow private FloatBuffer rawFloatBuffer;

    @Shadow private double xOffset;

    @Shadow private double yOffset;

    @Shadow private double zOffset;

    @Shadow private ByteBuffer byteBuffer;
    @Unique
    private long address;

    /**
     * @author Aki
     * @reason Replaced to next VertexFormatIndex (Fix Speed)
     */
    @Overwrite
    public void nextVertexFormatIndex() {
        if (++vertexFormatIndex == vertexFormat.getElementCount()) {
            vertexFormatIndex = 0;
        }
        if ((vertexFormatElement = ((ExtendedVertexFormatElement) vertexFormatElement).getNext())
                .getUsage() == VertexFormatElement.EnumUsage.PADDING) {
            nextVertexFormatIndex();
        }
    }

    @ModifyVariable(method = "<init>", at = @At("RETURN"), index = 1, ordinal = 0, name = "bufferSizeIn", argsOnly = true)
    private int init(int bufferSizeIn) {
        address = MemoryUtil.getAddress(byteBuffer);
        return bufferSizeIn;
    }

    @ModifyVariable(method = "growBuffer", at = @At(value = "INVOKE", target = "Ljava/nio/ShortBuffer;position(I)Ljava/nio/Buffer;", shift = At.Shift.AFTER), index = 1, ordinal = 0, name = "increaseAmount", argsOnly = true)
    private int growBuffer(int increaseAmount) {
        address = MemoryUtil.getAddress(byteBuffer);
        return increaseAmount;
    }

    /**
     * @author AKi
     * @reason Replace Fix
     */
    @Overwrite
    public BufferBuilder pos(double x, double y, double z) {
        ((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().pos(this, x, y, z);
        this.nextVertexFormatIndex();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @author AKi
     * @reason Replace Fix
     */
    @Overwrite
    public BufferBuilder color(int red, int green, int blue, int alpha) {
        if (this.noColor) {
            return (BufferBuilder) (Object) this;
        }

        ((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().color(this, red, green, blue, alpha);
        this.nextVertexFormatIndex();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @author AKi
     * @reason Replace Fix
     */
    @Overwrite
    public BufferBuilder tex(double u, double v) {
        ((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().tex(this, u, v);
        this.nextVertexFormatIndex();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @author AKi
     * @reason Replace Fix
     */
    @Overwrite
    public BufferBuilder lightmap(int skyLight, int blockLight) {
        ((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().lightmap(this, skyLight, blockLight);
        this.nextVertexFormatIndex();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @author AKi
     * @reason Replace Fix
     */
    @Overwrite
    public BufferBuilder normal(float x, float y, float z) {
        ((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().normal(this, x, y, z);
        this.nextVertexFormatIndex();
        return (BufferBuilder) (Object) this;
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public int getOffset() {
        return vertexCount * vertexFormat.getSize() + ((ExtendedVertexFormatElement) vertexFormatElement).getOffset();
    }

    @Override
    public double xOffset() {
        return xOffset;
    }

    @Override
    public double yOffset() {
        return yOffset;
    }

    @Override
    public double zOffset() {
        return zOffset;
    }
}
