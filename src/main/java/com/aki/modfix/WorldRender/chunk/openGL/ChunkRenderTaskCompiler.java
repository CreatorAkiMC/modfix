/**
 * Thankyou Nothirium
 */

package com.aki.modfix.WorldRender.chunk.openGL;

import com.aki.mcutils.APICore.Utils.memory.NIOBufferUtil;
import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.mcutils.APICore.Utils.render.GraphVisibility;
import com.aki.mcutils.APICore.Utils.render.SortVertexUtil;
import com.aki.mcutils.APICore.Utils.render.VisibilitySet;
import com.aki.modfix.GLSytem.GlDynamicVBO;
import com.aki.modfix.Modfix;
import com.aki.modfix.WorldRender.chunk.ChunkRenderManager;
import com.aki.modfix.WorldRender.chunk.openGL.integreate.BetterFoliage;
import com.aki.modfix.WorldRender.chunk.openGL.integreate.FluidLoggedAPI;
import com.aki.modfix.WorldRender.chunk.openGL.renderers.ChunkRendererBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

public class ChunkRenderTaskCompiler<T extends ChunkRender> extends ChunkRenderTaskBase<T> {
    private static final BlockingQueue<RegionRenderCacheBuilder> BUFFERBUILDER_QUEUE = new LinkedBlockingQueue<>();


    static {
        IntStream.range(0, (Runtime.getRuntime().availableProcessors() - 2) * 2).mapToObj(i -> new RegionRenderCacheBuilder()).forEach(BUFFERBUILDER_QUEUE::add);
    }

    private final IBlockAccess access;

    public ChunkRenderTaskCompiler(ChunkRendererBase<T> renderer, ChunkGLDispatcher dispatcher, T chunkRender, IBlockAccess access) {
        super(renderer, dispatcher, chunkRender);
        this.access = access;
    }

    @Override
    public ChunkRenderTaskResult run() {
        if (access instanceof GLChunkRenderCache) {
            ((GLChunkRenderCache) access).initCaches();
        }
        try {
            return compileSection();
        } finally {
            if (access instanceof GLChunkRenderCache) {
                ((GLChunkRenderCache) access).freeCaches();
            }
        }
    }

    private static void freeBufferBuilder(RegionRenderCacheBuilder buffer) {
        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            BufferBuilder bufferBuilder = buffer.getWorldRendererByLayer(layer);
            if (bufferBuilder.isDrawing) {
                bufferBuilder.finishDrawing();
            }
            bufferBuilder.reset();
            bufferBuilder.setTranslation(0, 0, 0);
        }
        BUFFERBUILDER_QUEUE.add(buffer);
    }

    private ChunkRenderTaskResult compileSection() {
        RegionRenderCacheBuilder bufferBuilderPack = null;
        boolean freeBufferBuilderPack = true;

        try {
            bufferBuilderPack = BUFFERBUILDER_QUEUE.take();
            freeBufferBuilderPack = compileSection(bufferBuilderPack) != ChunkRenderTaskResult.SUCCESSFUL;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ChunkRenderTaskResult.CANCELLED;
        } finally {
            if (bufferBuilderPack != null && freeBufferBuilderPack) {
                freeBufferBuilder(bufferBuilderPack);
            }
        }

        return this.getCancel() ? ChunkRenderTaskResult.CANCELLED : ChunkRenderTaskResult.SUCCESSFUL;
    }

    private ChunkRenderTaskResult compileSection(RegionRenderCacheBuilder bufferBuilderPack) {
        if (this.getCancel()) {
            return ChunkRenderTaskResult.CANCELLED;
        }

        Minecraft mc = Minecraft.getMinecraft();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        GraphVisibility visibilityGraph = new GraphVisibility();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    pos.setPos(this.chunkRender.getX() + x, this.chunkRender.getY() + y, this.chunkRender.getZ() + z);
                    IBlockState blockState = this.access.getBlockState(pos);
                    renderBlockState(blockState, pos, visibilityGraph, bufferBuilderPack, mc);

                    if (Modfix.isFluidloggedAPIInstalled) {
                        FluidLoggedAPI.renderFluidState(blockState, this.access, pos, fluidState -> renderBlockState(fluidState, pos, visibilityGraph, bufferBuilderPack, mc));
                    }
                }
            }

            if (this.getCancel()) {
                return ChunkRenderTaskResult.CANCELLED;
            }
        }

        VisibilitySet visibilitySet = visibilityGraph.compute();

        if (bufferBuilderPack.getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT).isDrawing) {
            Entity entity = mc.getRenderViewEntity();
            if (entity != null) {
                BufferBuilder bufferBuilder = bufferBuilderPack.getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT);
                Vec3d camera = entity.getPositionEyes(1.0F);
                SortVertexUtil.sortVertexData(NIOBufferUtil.asMemoryAccess(bufferBuilder.getByteBuffer()), bufferBuilder.getVertexCount(), bufferBuilder.getVertexFormat().getSize(), 4,
                        (float) (chunkRender.getX() - camera.x), (float) (chunkRender.getY() - camera.y), (float) (chunkRender.getZ() - camera.z));
            }
        }

        if (this.getCancel()) {
            return ChunkRenderTaskResult.CANCELLED;
        }

        BufferBuilder[] finishedBufferBuilders = Arrays.stream(BlockRenderLayer.values()).map(bufferBuilderPack::getWorldRendererByLayer).map(bufferBuilder -> {
            if (!bufferBuilder.isDrawing) {
                return null;
            }
            bufferBuilder.finishDrawing();
            if (bufferBuilder.getVertexCount() == 0) {
                return null;
            }
            return bufferBuilder;
        }).toArray(BufferBuilder[]::new);

        this.dispatcher.runOnRenderThread(() -> {
            try {
                if (!this.getCancel()) {
                    this.chunkRender.setVisibility(visibilitySet);
                    for (ChunkRenderPass pass : ChunkRenderPass.ALL) {
                        BufferBuilder bufferBuilder = finishedBufferBuilders[pass.ordinal()];
                        if (bufferBuilder == null) {
                            this.chunkRender.setVBO(pass, null);
                        } else {
                            GlDynamicVBO.VBOPart vboPart = this.chunkRender.getVBO(pass);
                            //VBO のずれを減らす
                            if (vboPart != null)
                                vboPart.free();
                            vboPart = this.renderer.buffer(pass, this.chunkRender, bufferBuilder.getByteBuffer());
                            this.chunkRender.setVBO(pass, vboPart);
                            if (pass == ChunkRenderPass.TRANSLUCENT) {
                                this.chunkRender.setTranslucentVertexData(NIOBufferUtil.copyAsUnsafeBuffer(bufferBuilder.getByteBuffer()));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Minecraft.getMinecraft().crashed(new CrashReport("ModFix ChunkRenderTaskCompiler MultiThread", e));
            } finally {
                freeBufferBuilder(bufferBuilderPack);
            }
        });

        return ChunkRenderTaskResult.SUCCESSFUL;
    }

    private void renderBlockState(IBlockState blockState, BlockPos.MutableBlockPos pos, GraphVisibility visibilityGraph, RegionRenderCacheBuilder bufferBuilderPack, Minecraft mc) {
        if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
            return;
        }

        for (EnumFacing dir : EnumFacing.VALUES) {
            if (blockState.doesSideBlockRendering(access, pos, dir)) {
                visibilityGraph.setOpaque(pos.getX(), pos.getY(), pos.getZ(), dir);
            }
        }

        blockState.getBlock().hasTileEntity(blockState);

        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            if (Modfix.isBetterFoliageInstalled ? !BetterFoliage.canRenderBlockInLayer(blockState.getBlock(), blockState, layer) : !blockState.getBlock().canRenderInLayer(blockState, layer)) {
                continue;
            }
            ForgeHooksClient.setRenderLayer(layer);

            //VanillaFix の修正
            ChunkRenderManager.CurrentChunkRender = this.chunkRender;

            BufferBuilder bufferBuilder = bufferBuilderPack.getWorldRendererByLayer(layer);
            if (!bufferBuilder.isDrawing) {
                bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                bufferBuilder.setTranslation(-this.chunkRender.getX(), -this.chunkRender.getY(), -this.chunkRender.getZ());
            }
            if (Modfix.isBetterFoliageInstalled) {
                BetterFoliage.renderWorldBlock(mc.getBlockRendererDispatcher(), blockState, pos, this.access, bufferBuilder, layer);
            } else {
                mc.getBlockRendererDispatcher().renderBlock(blockState, pos, this.access, bufferBuilder);
            }

            //VanillaFix の修正
            ChunkRenderManager.CurrentChunkRender = null;

            ForgeHooksClient.setRenderLayer(null);
        }
    }
}
