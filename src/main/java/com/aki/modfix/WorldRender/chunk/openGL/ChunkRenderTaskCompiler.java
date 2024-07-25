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
import com.aki.mcutils.asm.Optifine;
import com.aki.modfix.GLSytem.GLDynamicIBO;
import com.aki.modfix.GLSytem.GLDynamicVBO;
import com.aki.modfix.Modfix;
import com.aki.modfix.WorldRender.chunk.ChunkRenderManager;
import com.aki.modfix.WorldRender.chunk.openGL.integreate.BetterFoliage;
import com.aki.modfix.WorldRender.chunk.openGL.integreate.FluidLoggedAPI;
import com.aki.modfix.WorldRender.chunk.openGL.renderers.ChunkRendererBase;
import com.aki.modfix.compatibility.ModCompatibility;
import com.aki.modfix.util.gl.BakedModelEnumFacing;
import com.aki.modfix.util.gl.BlockVertexData;
import com.aki.modfix.util.gl.ChunkModelMeshUtils;
import com.aki.modfix.util.gl.VertexData;
import com.aki.modfix.util.gl.quad_list.BlockVertexValue;
import com.aki.modfix.util.gl.quad_list.IBakedQuadValue;
import com.aki.modfix.util.gl.quad_list.WeightedBakedQuadValue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.WeightedBakedModel;
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
import net.optifine.render.RenderEnv;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.util.*;
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

        //使用されていないデータを削除して、メモリを節約します。
        this.chunkRender.ContainsCheckStart();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    pos.setPos(this.chunkRender.getX() + x, this.chunkRender.getY() + y, this.chunkRender.getZ() + z);

                    IBlockState blockState = this.access.getBlockState(pos);
                    IBlockState blockState1 = blockState;
                    if (blockState.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                        renderBlockState(blockState, pos, visibilityGraph, bufferBuilderPack, mc);

                        if (Modfix.isFluidloggedAPIInstalled) {
                            FluidLoggedAPI.renderFluidState(blockState, this.access, pos, fluidState -> renderBlockState(fluidState, pos, visibilityGraph, bufferBuilderPack, mc));
                        }

                        blockState1.getBlock().hasTileEntity(blockState1);

                        blockState1 = ChunkModelMeshUtils.getActualState(blockState1, pos, this.access);
                        blockState1 = ChunkModelMeshUtils.getExtendState(blockState1, pos, this.access);
                    }
                }
            }

            if (this.getCancel()) {
                return ChunkRenderTaskResult.CANCELLED;
            }
        }

        this.chunkRender.ContainsCheckFinishAndRemove();


        HashMap<ChunkRenderPass, IntArrayList> indexLists = MapCreateHelper.CreateHashMap(ChunkRenderPass.values(), i -> new IntArrayList());
        Integer[] index_offsets = new Integer[] {0, 0, 0, 0};

        //頂点インデックスの準備 -> VAO用
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    pos.setPos(this.chunkRender.getX() + x, this.chunkRender.getY() + y, this.chunkRender.getZ() + z);

                    IBlockState blockState = this.access.getBlockState(pos);

                    if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
                        continue;
                    }

                    blockState = ChunkModelMeshUtils.getActualState(blockState, pos, this.access);
                    IBakedModel model = ChunkModelMeshUtils.getBakedModelFromState(blockState);
                    blockState = ChunkModelMeshUtils.getExtendState(blockState, pos, this.access);
                    int hash_code = ModCompatibility.getHash(blockState, this.access.getTileEntity(pos));

                    for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                        if (Modfix.isBetterFoliageInstalled ? !BetterFoliage.canRenderBlockInLayer(blockState.getBlock(), blockState, layer) : !blockState.getBlock().canRenderInLayer(blockState, layer)) {
                            continue;
                        }

                        ChunkRenderPass pass = ChunkRenderPass.ConvVanillaRenderPass(layer);



                        long rand = MathHelper.getCoordinateRandom(pos.getX(), pos.getY(), pos.getZ());

                        try {
                            /*
                             * Water などは getRenderLayer() が TRANSLUCENT なので index にも透過処理をしないといけない
                             * Lava == Solid
                             * */
                            if (blockState.getRenderType() == EnumBlockRenderType.LIQUID) {
                                BlockLiquid blockLiquid = (BlockLiquid) blockState.getBlock();
                                int index = 0;
                                /*for(boolean bool : new boolean[] {blockState.shouldSideBeRendered(this.access, pos, EnumFacing.UP), /*blockLiquid.shouldRenderSides(this.access, pos.up()), *///blockState.shouldSideBeRendered(this.access, pos, EnumFacing.DOWN), blockState.shouldSideBeRendered(this.access, pos, EnumFacing.NORTH), blockState.shouldSideBeRendered(this.access, pos, EnumFacing.SOUTH), blockState.shouldSideBeRendered(this.access, pos, EnumFacing.WEST), blockState.shouldSideBeRendered(this.access, pos, EnumFacing.EAST)}) {
                                /*if(bool) {
                                    for (int N = 0; N < 4; N++) {
                                        indexLists.get(pass).add(index++);
                                        }
                                    }
                                }*/

                                //上(上) ->4(+4) 8
                                //下   　->4     12
                                //横(それぞれ4 * 2) * 4面 -> 32
                                for (int i = 0; i < 44; i++) {
                                    indexLists.get(pass).add(i);
                                }
                                this.chunkRender.addBaseVertex(pass, 1);
                                index_offsets[pass.ordinal()] += 44;

                            } else {
                                if (Optifine.isOptifineDetected()) {
                                    RenderEnv renderEnv = Optifine.getRenderEnv(bufferBuilderPack.getWorldRendererByLayer(layer), blockState, pos);
                                    model = Optifine.getBakedModel(model, blockState, renderEnv);
                                    if (!Optifine.isAlternateBlocks()) {
                                        rand = 0L;
                                    }
                                }

                                BlockVertexData vertexData = null;
                                if (Modfix.natural_properties.stream().anyMatch(blockState.getBlock().getRegistryName()::equals) && Optifine.isNaturalTextures()) {
                                    vertexData = this.getBlockVertexData(this.access, pos, pass, model, blockState, layer, rand, bufferBuilderPack.getWorldRendererByLayer(layer));
                                } else {
                                    IBakedQuadValue quadValue = this.chunkRender.GetVertexDataFromState(pass, hash_code);
                                    if (model instanceof WeightedBakedModel) {
                                        vertexData = quadValue.getBlockVertexData(((WeightedBakedModel) model).getRandomModel(rand));
                                    } else {
                                        vertexData = quadValue.getBlockVertexData(model);
                                    }
                                }
                                /*if(model instanceof WeightedBakedModel) {
                                      vertexData = this.chunkRender.GetWeightedVertexDataFromState(pass, blockState, ((WeightedBakedModel)model).getRandomModel(rand));
                                 } else if(Modfix.natural_properties.stream().anyMatch(blockState.getBlock().getRegistryName()::equals) && Optifine.isNaturalTextures()) {
                                        vertexData = this.chunkRender.GetWeightedVertexDataFromState(pass, blockState, model);
                                 } else {
                                        vertexData = this.chunkRender.GetVertexDataFromState(pass, blockState).get;
                                 }*/

                                if (vertexData != null) {
                                    //int index = 0;
                                    int vertex_count = 0;
                                    Set<Integer> update_indexes = new HashSet<>();
                                    //面を描画しないとき、その頂点数を全体から引きます。
                                    int diff_offset = 0;

                                    for (BakedModelEnumFacing baked_facing : BakedModelEnumFacing.values()) {
                                        int size = vertexData.getSize(baked_facing);
                                        EnumFacing facing = baked_facing.getFacing();

                                        if (facing == null || blockState.shouldSideBeRendered(this.access, pos, facing)) {
                                            vertex_count += size;
                                            for (int i = 0; i < size; i++) {
                                                Pair<Pair<Integer, Integer>, VertexData> index_vec = vertexData.getVertex(baked_facing, i);
                                                int vertex_index = index_vec.getKey().getValue();
                                                if (update_indexes.contains(vertex_index)) {
                                                    vertex_index = index_vec.getKey().getKey();
                                                }
                                                indexLists.get(pass).add(index_offsets[pass.ordinal()] + (vertex_index - diff_offset));
                                                //indexLists.get(pass).add(index_offset + (index++));
                                            }
                                        } else {
                                            for (int i = 0; i < size; i++) {
                                                update_indexes.add(vertexData.getVertex(baked_facing, i).getKey().getValue());
                                            }
                                            diff_offset += size;
                                        }
                                    }
                                    index_offsets[pass.ordinal()] += vertex_count;
                                }

                                System.out.println("---Pass: " + pass + ", IndexesList: " + indexLists);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Modfix.logger.error("ChunkRenderTaskCompiler Error: " + blockState.getBlock().getRegistryName());
                            Modfix.logger.error(e.getLocalizedMessage());
                        }
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
                        /**
                         * 使われていない頂点が、干渉している？
                         *  -> 使っている頂点の法線等がそのまま適用されて、描画がバグる。
                         *     頂点の属性を新しく作る必要がある
                         * */
                        ByteBuffer buffer = this.chunkRender.getIndexesBuffer(pass);
                        GLDynamicIBO.IBOPart iboPart = this.chunkRender.getIBO(pass);
                        if(iboPart != null) {
                            iboPart.free();
                            iboPart = null;
                        }

                        if(buffer != null) {
                            iboPart = this.renderer.bufferIBO(pass, buffer);
                        }

                        this.chunkRender.setIBO(pass, iboPart);
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
                            vboPart = this.renderer.bufferVBO(pass, bufferBuilder.getByteBuffer());
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

        if(Optifine.isOptifineDetected() && !Optifine.isAlternateBlocks()) {
            rand = 0L;
        }

        if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
            return;
        }

        for (EnumFacing dir : EnumFacing.VALUES) {
            if (blockState.doesSideBlockRendering(access, pos, dir)) {
                visibilityGraph.setOpaque(pos.getX(), pos.getY(), pos.getZ(), dir);
            }
        }



        blockState.getBlock().hasTileEntity(blockState);
        IBlockState blockState_copy = blockState;

        blockState_copy = ChunkModelMeshUtils.getActualState(blockState_copy, pos, this.access);

        IBakedModel model = ChunkModelMeshUtils.getBakedModelFromState(blockState_copy);

        blockState_copy = ChunkModelMeshUtils.getExtendState(blockState_copy, pos, this.access);

        int hash_code = ModCompatibility.getHash(blockState_copy, this.access.getTileEntity(pos));

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
                /**
                 * mc.getBlockRendererDispatcher().renderBlock(blockState, pos, this.access, bufferBuilder);
                 * を通すと、参照渡しでblockStateが変わる
                 * */
                mc.getBlockRendererDispatcher().renderBlock(blockState, pos, this.access, bufferBuilder);
            }


            /*
             * データの準備
             * BlockState(と頂点) がすでに保存されていれば処理しない。
             * 頂点のデータを使いまわす(リストの座標も) -> メモリ削減・軽量化
             * */

            EnumBlockRenderType enumblockrendertype = blockState_copy.getRenderType();
            if (enumblockrendertype == EnumBlockRenderType.MODEL) {
                if(Optifine.isOptifineDetected()) {
                    if(Optifine.isShaders())
                        Optifine.pushEntity(blockState_copy, pos, this.access, bufferBuilder);
                    RenderEnv renderEnv = Optifine.getRenderEnv(bufferBuilder, blockState_copy, pos);
                    model = Optifine.getBakedModel(model, blockState_copy, renderEnv);
                }

                ChunkRenderPass pass = ChunkRenderPass.ConvVanillaRenderPass(layer);

                BlockVertexData vertexData = this.getBlockVertexData(this.access, pos, pass, model, blockState_copy, layer, rand, bufferBuilder);

                if (model instanceof WeightedBakedModel) {
                    IBakedModel randomModel = ((WeightedBakedModel)model).getRandomModel(rand);
                    IBakedQuadValue quadValue = this.chunkRender.GetVertexDataFromState(pass, hash_code);

                    if(quadValue == null) {
                        quadValue = new WeightedBakedQuadValue();
                    }

                    if(quadValue instanceof WeightedBakedQuadValue) {
                        if(!((WeightedBakedQuadValue) quadValue).IsBakedModel(randomModel)) {
                            ((WeightedBakedQuadValue) quadValue).AddModelToVertexData(randomModel, vertexData);
                        }
                        this.chunkRender.UpdateStateToVertexData(pass, hash_code, quadValue);
                    }
                    //For Optifine NaturalTexture
                } else if(Modfix.natural_properties.stream().anyMatch(blockState_copy.getBlock().getRegistryName()::equals) && Optifine.isNaturalTextures()) {
                    /*
                    * Optifine の NaturalTextures の仕様上、IBlockState やIBakedModel と紐づけてしまうと、処理が重くなってしまいそうです。
                    * */
                    /*if(!this.chunkRender.IsStateWeightedVertexContainsModel(pass, blockState_copy, model)) {
                        this.chunkRender.add(pass, blockState_copy, model, vertexData);
                    }*/
                } else {
                    if (!this.chunkRender.IsStateVertexContains(pass, hash_code)) {
                        this.chunkRender.addStateVertexes(pass, hash_code, new BlockVertexValue(vertexData));
                    }
                }

                if(Optifine.isShaders() && Optifine.isShaders())
                    Optifine.popEntity(bufferBuilder);
            }

            //VanillaFix の修正
            ChunkRenderManager.CurrentChunkRender = null;

            ForgeHooksClient.setRenderLayer(null);
        }
    }

    private List<BakedQuad> getQuads(IBakedModel model, IBlockState state, EnumFacing facing, long rand) {
        return model.getQuads(state, facing, rand);
    }

    private BlockVertexData getBlockVertexData(IBlockAccess access, BlockPos pos, ChunkRenderPass pass, IBakedModel model, IBlockState blockState, BlockRenderLayer layer, long rand, BufferBuilder bufferBuilder) {
        BlockVertexData vertexData = new BlockVertexData(this.chunkRender, pass);
        List<VertexData> vertexes = this.chunkRender.getVertexData();
        //Optifine 対応
        if(Optifine.isOptifineDetected()) {
            RenderEnv renderEnv = Optifine.getRenderEnv(bufferBuilder, blockState, pos);

            for (EnumFacing enumfacing : EnumFacing.values()) {
                List<BakedQuad> list = this.getQuads(model, blockState, enumfacing, rand);
                if (!list.isEmpty()) {

                    list = Optifine.getNaturalBakedArray(list, access, blockState, pos, enumfacing, layer, rand, renderEnv);

                    for (BakedQuad quad : list) {

                        //Don`t use this.
                        /*if (Optifine.isNaturalTextures() && ((IBakedQuadExtension) quad).getOriginalBakedQuad() != null) {
                            quad = ((IBakedQuadExtension) quad).getOriginalBakedQuad();
                        }*/

                        int[] vertex = quad.getVertexData();
                        for (int index = 0; index < 4; index++) {
                            VertexData data = this.getVertexData(quad, index, vertex);
                            int vertex_index = vertexes.indexOf(data);
                            if (vertex_index == -1) {
                                vertexes.add(data);// after size - 1 -> index
                                vertex_index = vertexes.size() - 1;
                            }

                            //隠れている(見えない面)も含む
                            vertexData.addVertexIndex(BakedModelEnumFacing.getBakedFacing(enumfacing), vertex_index);
                        }
                    }
                }
            }

            List<BakedQuad> list = this.getQuads(model, blockState, null, rand);

            if (!list.isEmpty()) {
                list = Optifine.getNaturalBakedArray(list, access, blockState, pos, (EnumFacing) null, layer, rand, renderEnv);

                for (BakedQuad quad : list) {
                    //Don`t use this.
                    /*if (Optifine.isNaturalTextures() && ((IBakedQuadExtension) quad).getOriginalBakedQuad() != null) {
                        quad = ((IBakedQuadExtension) quad).getOriginalBakedQuad();
                    }*/

                    int[] vertex = quad.getVertexData();
                    for (int index = 0; index < 4; index++) {
                        VertexData data = this.getVertexData(quad, index, vertex);
                        int vertex_index = vertexes.indexOf(data);
                        if (vertex_index == -1) {
                            vertexes.add(data);// after size - 1 -> index
                            vertex_index = vertexes.size() - 1;
                        }
                        vertexData.addVertexIndex(BakedModelEnumFacing._NULL, vertex_index);
                    }
                }
            }
        } else {
            for (EnumFacing enumfacing : EnumFacing.values()) {
                List<BakedQuad> list = model.getQuads(blockState, enumfacing, rand);
                if (!list.isEmpty()) {
                    for (BakedQuad quad : list) {
                        int[] vertex = quad.getVertexData();
                        for (int index = 0; index < 4; index++) {
                            VertexData data = this.getVertexData(quad, index, vertex);
                            int vertex_index = vertexes.indexOf(data);
                            if (vertex_index == -1) {
                                vertexes.add(data);// after size - 1 -> index
                                vertex_index = vertexes.size() - 1;
                            }

                            //隠れている(見えない面)も含む
                            vertexData.addVertexIndex(BakedModelEnumFacing.getBakedFacing(enumfacing), vertex_index);
                        }
                    }
                }
            }

            List<BakedQuad> list = model.getQuads(blockState, null, rand);

            if (!list.isEmpty()) {
                for (BakedQuad quad : list) {
                    int[] vertex = quad.getVertexData();
                    for (int index = 0; index < 4; index++) {
                        VertexData data = this.getVertexData(quad, index, vertex);
                        int vertex_index = vertexes.indexOf(data);
                        if (vertex_index == -1) {
                            vertexes.add(data);// after size - 1 -> index
                            vertex_index = vertexes.size() - 1;
                        }
                        vertexData.addVertexIndex(BakedModelEnumFacing._NULL, vertex_index);
                    }
                }
            }
        }

        return vertexData;
    }

    private VertexData getVertexData(BakedQuad quad, int index, int[] vertex) {
        //getFormat が大事かもしれない
        int pos_head = index * quad.getFormat().getIntegerSize();
        int uvIndex = pos_head + quad.getFormat().getUvOffsetById(0) / 4;

        Vector3f vec3f = new Vector3f(Float.intBitsToFloat(vertex[pos_head]), Float.intBitsToFloat(vertex[pos_head + 1]), Float.intBitsToFloat(vertex[pos_head + 2]));

        /**
         *  Optifine の [自然なテクスチャ]　機能でUVごと回転するため、テクスチャがバグります。
         *  --------------------------------------------------------------------------------------
         *  getNaturalTexture に一回通せばよさそうです。
         *  public static BakedQuad gBlockPosNaturalTexture(BlockPos blockPosIn, BakedQuad quad) {
         *         TextureAtlasSprite sprite = quad.getSprite();
         *         if (sprite == null) {
         *             return quad;
         *         } else {
         *             NaturalProperties nps = getNaturalProperties(sprite);
         *             if (nps == null) {
         *                 return quad;
         *             } else {
         *                 int side = ConnectedTextures.getSide(quad.getFace());
         *                 int rand = Config.getRandom(blockPosIn, side);
         *                 int rotate = 0;
         *                 boolean flipU = false;
         *                 if (nps.rotation > 1) {
         *                     rotate = rand & 3;
         *                 }
         *                 if (nps.rotation == 2) {
         *                     rotate = rotate / 2 * 2;
         *                 }
         *                 if (nps.flip) {
         *                     flipU = (rand & 4) != 0;
         *                 }
         *                 return nps.getQuad(quad, rotate, flipU);
         *             }
         *         }
         *     }
         *  --------------------------------------------------------------------------------------
         *  Optifine  NaturalTextures.class 参照
         * */

        // vertex[pos_head + 4] には sprite の座標などが含まれています。
        Vector2f vec2f = new Vector2f(Float.intBitsToFloat(vertex[uvIndex]), Float.intBitsToFloat(vertex[uvIndex + 1])); //UV

        return new VertexData(vec3f, vec2f);
    }
}
