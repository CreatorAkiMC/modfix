package com.aki.modfix.asm;

import com.aki.mcutils.APICore.asm.ASMUtil;
import com.aki.mcutils.APICore.asm.HashMapClassNodeClassTransformer;
import com.aki.mcutils.APICore.asm.IClassTransformerRegistry;
import net.minecraft.launchwrapper.IClassTransformer;
import org.dimdev.vanillafix.VanillaFixLoadingPlugin;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.Field;

//import com.aki.mcutils.APICore.asm.IClassTransformer; -> net.minecraft.launchwrapper.IClassTransformer
public class ModFixClassTransformer extends HashMapClassNodeClassTransformer implements IClassTransformer {
    @Override
    protected void registerTransformers(IClassTransformerRegistry reg) {
        reg.add("net.minecraft.client.renderer.RenderGlobal", "setWorldAndLoadRenderers", "(Lnet/minecraft/client/multiplayer/WorldClient;)V", "a", "(Lbsb;)V", ClassWriter.COMPUTE_FRAMES, methodNode -> {
            ASMUtil.LOGGER.info("ModFix -> Transform setWorldAndLoadRenderers net/minecraft/client/renderer/RenderGlobal -> setWorldAndLoadRenderers");

            AbstractInsnNode targetNode1 = ASMUtil.first(methodNode).methodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "clear", "()V").find();
            targetNode1 = ASMUtil.prev(targetNode1).type(LabelNode.class).find();

            AbstractInsnNode popNode1 = ASMUtil.last(methodNode).fieldInsn(Opcodes.PUTFIELD, "buy", "N", "Lbxm;", "net/minecraft/client/renderer/RenderGlobal", "renderDispatcher", "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;").find();
            popNode1 = ASMUtil.next(popNode1).type(LabelNode.class).find();

            methodNode.instructions.insert(targetNode1, ASMUtil.listOf(
                    new MethodInsnNode(Opcodes.INVOKESTATIC, "com/aki/modfix/WorldRender/chunk/ChunkRenderManager", "dispose", "()V", false),
                    new JumpInsnNode(Opcodes.GOTO, (LabelNode) popNode1)
            ));
        });

        reg.add("net.minecraft.client.renderer.RenderGlobal", "loadRenderers", "()V", "a", "()V", ClassWriter.COMPUTE_FRAMES, methodNode -> {
            ASMUtil.LOGGER.info("Transforming method: loadRenderers net/minecraft/client/renderer/RenderGlobal");

            AbstractInsnNode targetNode1 = ASMUtil.first(methodNode).methodInsn(Opcodes.INVOKESPECIAL, "bxm", "<init>", "()V", "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher", "<init>", "()V").find();
            targetNode1 = ASMUtil.prev(targetNode1).type(JumpInsnNode.class).find();
            targetNode1 = ASMUtil.prev(targetNode1).type(LabelNode.class).find();
            AbstractInsnNode popNode1 = ASMUtil.next(targetNode1).methodInsn(Opcodes.INVOKESPECIAL, "bxm", "<init>", "()V", "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher", "<init>", "()V").find();
            popNode1 = ASMUtil.next(popNode1).type(LabelNode.class).find();

            AbstractInsnNode targetNode2 = ASMUtil.next(popNode1).methodInsn(Opcodes.INVOKESPECIAL, "buy", "q", "()V", "net/minecraft/client/renderer/RenderGlobal", "generateSky2", "()V").find();
            targetNode2 = ASMUtil.next(targetNode2).type(LabelNode.class).find();
            AbstractInsnNode popNode2 = ASMUtil.last(methodNode).fieldInsn(Opcodes.PUTFIELD, "buy", "Q", "I", "net/minecraft/client/renderer/RenderGlobal", "renderEntitiesStartupCounter", "I").find();
            popNode2 = ASMUtil.next(popNode2).type(LabelNode.class).find();

            methodNode.instructions.insert(targetNode1, ASMUtil.listOf(
                    new JumpInsnNode(Opcodes.GOTO, (LabelNode) popNode1)
            ));

            methodNode.instructions.insert(targetNode2, ASMUtil.listOf(
                    //new MethodInsnNode(Opcodes.INVOKESTATIC, "com/aki/modfix/chunk/ChunkRenderManager", "transtest", "()V", false),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, "com/aki/modfix/WorldRender/chunk/ChunkRenderManager", "loadRender", "()V", false),
                    new JumpInsnNode(Opcodes.GOTO, (LabelNode) popNode2)
            ));
        });

        try {
            Class<?> LoadingConfigClass = VanillaFixLoadingPlugin.config.getClass();
            Object o = VanillaFixLoadingPlugin.config;

            Field textureFixes = LoadingConfigClass.getDeclaredField("textureFixes");
            textureFixes.setAccessible(true);
            textureFixes.setBoolean(o, false);
        } catch (NoClassDefFoundError | IllegalAccessException | NoSuchFieldException ignored) {

        }
    }
}
