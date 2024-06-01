/**
 * Thankyou Nothirium
 */

package com.aki.modfix.WorldRender.chunk.openGL;

import com.aki.mcutils.APICore.Utils.list.MapCreateHelper;
import com.aki.mcutils.APICore.Utils.list.Pair;
import com.aki.mcutils.APICore.Utils.memory.NIOBufferUtil;
import com.aki.mcutils.APICore.Utils.render.ChunkRenderPass;
import com.aki.mcutils.APICore.Utils.render.GraphVisibility;
import com.aki.mcutils.APICore.Utils.render.SortVertexUtil;
import com.aki.mcutils.APICore.Utils.render.VisibilitySet;
import com.aki.modfix.GLSytem.GLDynamicIBO;
import com.aki.modfix.GLSytem.GLDynamicVBO;
import com.aki.modfix.Modfix;
import com.aki.modfix.WorldRender.chunk.ChunkRenderManager;
import com.aki.modfix.WorldRender.chunk.openGL.integreate.BetterFoliage;
import com.aki.modfix.WorldRender.chunk.openGL.integreate.FluidLoggedAPI;
import com.aki.modfix.WorldRender.chunk.openGL.renderers.ChunkRendererBase;
import com.aki.modfix.util.gl.BakedModelEnumFacing;
import com.aki.modfix.util.gl.BlockVertexDatas;
import com.aki.modfix.util.gl.ChunkModelMeshUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

        HashMap<ChunkRenderPass, IntArrayList> indexLists = MapCreateHelper.CreateHashMap(ChunkRenderPass.values(), i -> new IntArrayList());
        int index_offset = 0;

        //頂点インデックスの準備 -> VAO用
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    pos.setPos(this.chunkRender.getX() + x, this.chunkRender.getY() + y, this.chunkRender.getZ() + z);

                    IBlockState blockState = this.access.getBlockState(pos);
                    blockState = ChunkModelMeshUtils.getActualState(blockState, pos, this.access);
                    blockState = ChunkModelMeshUtils.getExtendState(blockState, pos, this.access);

                    ChunkRenderPass pass = ChunkRenderPass.ConvVanillaRenderPass(blockState.getBlock().getRenderLayer());

                    BlockVertexDatas vertexDatas = this.chunkRender.GetVertexDatasFromState(pass, blockState);
                    if (vertexDatas != null) {
                        for (BakedModelEnumFacing baked_facing : BakedModelEnumFacing.values()) {
                            int size = vertexDatas.getSize(baked_facing);
                            EnumFacing facing = baked_facing.getFacing();
                            if (facing == null || blockState.shouldSideBeRendered(this.access, pos, facing)) {
                                for (int i = 0; i < size; i++) {
                                    Pair<Integer, Vector3f> index_vec = vertexDatas.getVertex(baked_facing, i);
                                    indexLists.get(pass).add(index_offset + index_vec.getKey());
                                }
                            }
                        }
                        index_offset += vertexDatas.getVertexCount();
                    }
                }
            }
            if (this.getCancel()) {
                return ChunkRenderTaskResult.CANCELLED;
            }
        }

        //System.out.println("Out: " + indexLists);

        for(ChunkRenderPass pass : ChunkRenderPass.values()) {
            this.chunkRender.CreateIndexesBuffer(pass, indexLists.get(pass).toIntArray());
            System.out.print("VertexIndexes: " + Arrays.toString(indexLists.get(pass).toIntArray()) + ", Pass: " + pass);
        }

        this.dispatcher.runOnRenderThread(() -> {
            if (!this.getCancel()) {
                try {
                    for (ChunkRenderPass pass : ChunkRenderPass.values()) {
                        /*
                         * sizeチェック必要 -> 増加している
                         * sectorに分けるのが一番よさそう
                         * すでにデータがある場合、置き換えるみたいな
                         *
                         *　ChunkRender ごとに部屋(Sector)を割り当てる...
                         * */
                        IntBuffer buffer = this.chunkRender.getIndexesBuffer(pass);
                        GLDynamicIBO.IBOPart iboPart = this.chunkRender.getIBO(pass);
                        if(iboPart != null) {
                            iboPart.free();
                        }

                        if(buffer != null) {
                            iboPart = this.renderer.buffer(pass, buffer);
                        } else {
                            iboPart = null;
                        }

                        this.chunkRender.setIBO(pass, iboPart);
                        /*buffer.bind(GL15.GL_ELEMENT_ARRAY_BUFFER);

                        System.out.print("___Size: " + GL15.glGetBufferParameteri(GL15.GL_ELEMENT_ARRAY_BUFFER, 34660));

                        buffer.bufferSubData(0, this.chunkRender.getIndexesBuffer(pass));
                        buffer.unbind();*/
                    }
                } catch (Exception e) {
                    Minecraft.getMinecraft().crashed(new CrashReport("ModFix ChunkRenderTaskCompiler MultiThread", e));
                }
            }
        });

        indexLists.clear();

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
                            GLDynamicVBO.VBOPart vboPart = this.chunkRender.getVBO(pass);
                            //VBO のずれを減らす
                            if (vboPart != null)
                                vboPart.free();
                            vboPart = this.renderer.buffer(pass, bufferBuilder.getByteBuffer());
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
        long rand = MathHelper.getCoordinateRandom(pos.getX(), pos.getY(), pos.getZ());

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

            /*
             * データの準備
             * BlockState(と頂点) がすでに保存されていれば処理しない。
             * 頂点のデータを使いまわす(リストの座標も) -> メモリ削減・軽量化
             * */
            EnumBlockRenderType enumblockrendertype = blockState.getRenderType();
            if (enumblockrendertype == EnumBlockRenderType.MODEL) {

                blockState = ChunkModelMeshUtils.getActualState(blockState, pos, this.access);

                IBakedModel model = ChunkModelMeshUtils.getBakedModelFromState(blockState);
                blockState = ChunkModelMeshUtils.getExtendState(blockState, pos, this.access);
                ChunkRenderPass pass = ChunkRenderPass.ConvVanillaRenderPass(layer);
                if(!this.chunkRender.IsStateVertexContains(pass, blockState)) {

                    BlockVertexDatas vertexDatas = new BlockVertexDatas(this.chunkRender, pass);
                    List<Vector3f> vertexes = this.chunkRender.getVertexes();

                    for (EnumFacing enumfacing : EnumFacing.values()) {
                        List<BakedQuad> list = model.getQuads(blockState, enumfacing, rand);
                        if (!list.isEmpty()) {
                            for (BakedQuad quad : list) {
                                int[] vertex = quad.getVertexData();
                                for (int index = 0; index < 4; index++) {
                                    int pos_head = index * 7;
                                    Vector3f vec3f = new Vector3f(Float.intBitsToFloat(vertex[pos_head]), Float.intBitsToFloat(vertex[pos_head + 1]), Float.intBitsToFloat(vertex[pos_head + 2]));
                                    int vertex_index = vertexes.indexOf(vec3f);
                                    if (vertex_index == -1) {
                                        vertexes.add(vec3f);// after size - 1 -> index
                                        vertex_index = vertexes.size() - 1;
                                    }

                                    //隠れている(見えない面)も含む
                                    //if (blockState.shouldSideBeRendered(this.access, pos, enumfacing))
                                    vertexDatas.addVertexIndex(BakedModelEnumFacing.getBakedFacing(enumfacing), vertex_index);
                                }
                            }
                        }
                    }

                    List<BakedQuad> list = model.getQuads(blockState, null, rand);

                    if (!list.isEmpty()) {
                        for (BakedQuad quad : list) {
                            int[] vertex = quad.getVertexData();
                            for (int index = 0; index < 4; index++) {
                                int pos_head = index * 7;
                                Vector3f vec3f = new Vector3f(Float.intBitsToFloat(vertex[pos_head]), Float.intBitsToFloat(vertex[pos_head + 1]), Float.intBitsToFloat(vertex[pos_head + 2]));
                                int vertex_index = vertexes.indexOf(vec3f);
                                if (vertex_index == -1) {
                                    vertexes.add(vec3f);// after size - 1 -> index
                                    vertex_index = vertexes.size() - 1;
                                }
                                vertexDatas.addVertexIndex(BakedModelEnumFacing._NULL, vertex_index);
                            }
                        }
                    }

                    this.chunkRender.addStateVertexes(pass, blockState, vertexDatas);
                }
            }

            //VanillaFix の修正
            ChunkRenderManager.CurrentChunkRender = null;

            ForgeHooksClient.setRenderLayer(null);
        }
    }


}
