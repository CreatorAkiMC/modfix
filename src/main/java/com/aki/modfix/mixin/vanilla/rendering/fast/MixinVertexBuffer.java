package com.aki.modfix.mixin.vanilla.rendering.fast;

import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.Modfix;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;

@Mixin(value = VertexBuffer.class, priority = Modfix.ModPriority)
public class MixinVertexBuffer {


    @Shadow
    @Final
    private VertexFormat vertexFormat;

    @Shadow private int count;

    @Shadow private int glBufferId;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;glGenBuffers()I"), remap = false)
    public int allocateOptimizableBuffer() {
        return GLUtils.CAPS.OpenGL45 ? GL45.glCreateBuffers() : OpenGlHelper.glGenBuffers();
    }

    @Redirect(method = "bufferData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;glBufferData(ILjava/nio/ByteBuffer;I)V"))
    public void optimizeVertexDataUploading(int target, ByteBuffer data, int p_176071_2_) {
        if (data.remaining() > this.count) {//あってる?
            this.count = data.remaining();
            if (GLUtils.CAPS.OpenGL45) {
                GL45.glNamedBufferData(this.glBufferId, data, GL15.GL_DYNAMIC_DRAW);
            } else {
                OpenGlHelper.glBufferData(target, data, GL15.GL_DYNAMIC_DRAW);
            }
        } else {
            GL15.glBufferSubData(target, 0, data);
        }
    }
}
