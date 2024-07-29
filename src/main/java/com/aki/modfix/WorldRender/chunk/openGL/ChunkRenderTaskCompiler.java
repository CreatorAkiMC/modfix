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
import com.aki.modfix.util.gl.BakedModelEnumFacing;
import com.aki.modfix.util.gl.BlockVertexData;
import com.aki.modfix.util.gl.ChunkModelMeshUtils;
import com.aki.modfix.util.gl.VertexData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
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

import javax.vecmath.Vector2d;
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

        HashMap<ChunkRenderPass, List<Index2VertexVec>> indexLists = MapCreateHelper.CreateHashMap(ChunkRenderPass.values(), i -> new ArrayList<>());
        Integer[] indexOffset = new Integer[] {0, 0, 0, 0};

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    pos.setPos(this.chunkRender.getX() + x, this.chunkRender.getY() + y, this.chunkRender.getZ() + z);

                    IBlockState blockState = this.access.getBlockState(pos);
                    if (blockState.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                        renderBlockState(blockState, pos, visibilityGraph, bufferBuilderPack, indexLists, indexOffset, mc);
                        if (Modfix.isFluidloggedAPIInstalled) {
                            // エラーが出るかもしれません。
                            //Maybe this will be an error.
                            FluidLoggedAPI.renderFluidState(blockState, this.access, pos, fluidState -> renderBlockState(fluidState, pos, visibilityGraph, bufferBuilderPack, indexLists, indexOffset, mc));
                        }
                    }
                }
            }

            if (this.getCancel()) {
                return ChunkRenderTaskResult.CANCELLED;
            }
        }

        if (bufferBuilderPack.getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT).isDrawing) {
            Entity entity = mc.getRenderViewEntity();
            if (entity != null) {
                BufferBuilder bufferBuilder = bufferBuilderPack.getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT);
                Vec3d camera = entity.getPositionEyes(1.0F);
                SortVertexUtil.sortVertexData(NIOBufferUtil.asMemoryAccess(bufferBuilder.getByteBuffer()), bufferBuilder.getVertexCount(), bufferBuilder.getVertexFormat().getSize(), 4,
                        (float) (chunkRender.getX() - camera.x), (float) (chunkRender.getY() - camera.y), (float) (chunkRender.getZ() - camera.z));
                indexLists.replace(ChunkRenderPass.TRANSLUCENT, ChunkModelMeshUtils.SortIndex2VertexVec(indexLists.get(ChunkRenderPass.TRANSLUCENT), 4, (camera.x - chunkRender.getX()), (camera.y - chunkRender.getY()), (camera.z - chunkRender.getZ())));
            }
        }
        for(ChunkRenderPass pass : ChunkRenderPass.values()) {
            this.chunkRender.CreateIndexesBuffer(pass, indexLists.get(pass).stream().map(Index2VertexVec::getIndex).toArray(Integer[]::new));
            if(pass == ChunkRenderPass.TRANSLUCENT)
                this.chunkRender.setTranslucentIndexData(indexLists.get(pass));
        }

        if (this.getCancel()) {
            return ChunkRenderTaskResult.CANCELLED;
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

    /**
     * getBlockVertexData を動かしているので、IsStateVertexContainsの意味はほとんど無いです。
     * なので、renderBlockState内で index まで作ったほうが軽そうです。
     * また、ChunkRender とのスレッド同期が起きず、一回の getBlockState で処理できるので、バグが起きにくくなります。
     * それに、ほぼすべてのmodへの互換性も維持できます。
     * */
    private void renderBlockState(IBlockState blockState, BlockPos.MutableBlockPos pos, GraphVisibility visibilityGraph, RegionRenderCacheBuilder bufferBuilderPack, HashMap<ChunkRenderPass, List<Index2VertexVec>> indexList, Integer[] indexOffset, Minecraft mc) {
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

        blockState = ChunkModelMeshUtils.getActualState(blockState, pos, this.access);
        IBakedModel model = ChunkModelMeshUtils.getBakedModelFromState(blockState);
        blockState = ChunkModelMeshUtils.getExtendState(blockState, pos, this.access);

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
                 * を通すと、参照渡しでblockStateが変わる (多分...)
                 * */
                mc.getBlockRendererDispatcher().renderBlock(blockState, pos, this.access, bufferBuilder);
            }

            EnumBlockRenderType enumblockrendertype = blockState.getRenderType();
            if (enumblockrendertype == EnumBlockRenderType.MODEL) {
                if(Optifine.isOptifineDetected()) {
                    if(Optifine.isShaders())
                        Optifine.pushEntity(blockState, pos, this.access, bufferBuilder);
                    RenderEnv renderEnv = Optifine.getRenderEnv(bufferBuilder, blockState, pos);
                    model = Optifine.getBakedModel(model, blockState, renderEnv);
                }

                ChunkRenderPass pass = ChunkRenderPass.ConvVanillaRenderPass(layer);

                BlockVertexData vertexData = this.getBlockVertexData(this.access, pos, pass, model, blockState, layer, rand, bufferBuilder);

                //int index = 0;
                int vertexCount = 0;
                Set<Integer> updateIndexes = new HashSet<>();
                //面を描画しないとき、その頂点数を全体から引きます。
                int diffCount = 0;

                for (BakedModelEnumFacing bakedFacing : BakedModelEnumFacing.values()) {
                    int size = vertexData.getSize(bakedFacing);
                    EnumFacing facing = bakedFacing.getFacing();

                    if (facing == null || blockState.shouldSideBeRendered(this.access, pos, facing)) {
                        vertexCount += size;
                        for (int i = 0; i < size; i++) {
                            Pair<Pair<Integer, Integer>, VertexData> indexVec = vertexData.getVertex(bakedFacing, i);
                            int vertexIndex = indexVec.getKey().getValue();
                            if (updateIndexes.contains(vertexIndex)) {
                                vertexIndex = indexVec.getKey().getKey();
                            }

                            indexList.get(pass).add(new Index2VertexVec(indexOffset[pass.ordinal()] + (vertexIndex - diffCount), indexVec.getValue().getPosVec3().add(pos.getX(), pos.getY(), pos.getZ())));
                        }
                    } else {
                        for (int i = 0; i < size; i++) {
                            updateIndexes.add(vertexData.getVertex(bakedFacing, i).getKey().getValue());
                        }
                        diffCount += size;
                    }
                }
                indexOffset[pass.ordinal()] += vertexCount;

                /*if (model instanceof WeightedBakedModel) {
                    IBakedModel randomModel = ((WeightedBakedModel)model).getRandomModel(rand);
                    IBakedQuadValue quadValue = this.chunkRender.GetVertexDataFromState(pass, hashCode);

                    if(quadValue == null) {
                        quadValue = new WeightedBakedQuadValue();
                    }

                    if(quadValue instanceof WeightedBakedQuadValue) {
                        if(!((WeightedBakedQuadValue) quadValue).IsBakedModel(randomModel)) {
                            ((WeightedBakedQuadValue) quadValue).AddModelToVertexData(randomModel, vertexData);
                        }
                        this.chunkRender.UpdateStateToVertexData(pass, hashCode, quadValue);
                    }
                    //For Optifine NaturalTexture
                } else if(Modfix.natural_properties.stream().anyMatch(blockState.getBlock().getRegistryName()::equals) && Optifine.isNaturalTextures()) {

                } else {
                    if (!this.chunkRender.IsStateVertexContains(pass, hashCode)) {
                        this.chunkRender.addStateVertexes(pass, hashCode, new BlockVertexValue(vertexData));
                    }
                }*/

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

                        int[] vertex = quad.getVertexData();
                        for (int index = 0; index < 4; index++) {
                            VertexData data = this.getVertexData(quad, index, vertex);
                            int vertexIndex = vertexes.indexOf(data);
                            if (vertexIndex == -1) {
                                vertexes.add(data);// after size - 1 -> index
                                vertexIndex = vertexes.size() - 1;
                            }

                            //隠れている(見えない面)も含む
                            vertexData.addVertexIndex(BakedModelEnumFacing.getBakedFacing(enumfacing), vertexIndex);
                        }
                    }
                }
            }

            List<BakedQuad> list = this.getQuads(model, blockState, null, rand);

            if (!list.isEmpty()) {
                list = Optifine.getNaturalBakedArray(list, access, blockState, pos, (EnumFacing) null, layer, rand, renderEnv);

                for (BakedQuad quad : list) {

                    int[] vertex = quad.getVertexData();
                    for (int index = 0; index < 4; index++) {
                        VertexData data = this.getVertexData(quad, index, vertex);
                        int vertexIndex = vertexes.indexOf(data);
                        if (vertexIndex == -1) {
                            vertexes.add(data);// after size - 1 -> index
                            vertexIndex = vertexes.size() - 1;
                        }
                        vertexData.addVertexIndex(BakedModelEnumFacing._NULL, vertexIndex);
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
                            int vertexIndex = vertexes.indexOf(data);
                            if (vertexIndex == -1) {
                                vertexes.add(data);// after size - 1 -> index
                                vertexIndex = vertexes.size() - 1;
                            }

                            //隠れている(見えない面)も含む
                            vertexData.addVertexIndex(BakedModelEnumFacing.getBakedFacing(enumfacing), vertexIndex);
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
                        int vertexIndex = vertexes.indexOf(data);
                        if (vertexIndex == -1) {
                            vertexes.add(data);// after size - 1 -> index
                            vertexIndex = vertexes.size() - 1;
                        }
                        vertexData.addVertexIndex(BakedModelEnumFacing._NULL, vertexIndex);
                    }
                }
            }
        }

        return vertexData;
    }

    private VertexData getVertexData(BakedQuad quad, int index, int[] vertex) {
        //getFormat が大事かもしれない
        VertexFormat format = quad.getFormat();
        int posHead = index * format.getIntegerSize();
        //int UVIndex = posHead + format.getUvOffsetById(0) / 4;
        //int UVLightIndex = posHead + UVIndex + 2;

        Vec3d PosVec3 = new Vec3d(Float.intBitsToFloat(vertex[posHead]), Float.intBitsToFloat(vertex[posHead + 1]), Float.intBitsToFloat(vertex[posHead + 2]));

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

        // vertex[posHead + 4 (+1)] には sprite[UV] の座標などが含まれています。
        //DefaultVertexFormats.TEX_2F
        Vector2d UVVec2d = new Vector2d(Float.intBitsToFloat(vertex[posHead + 4]), Float.intBitsToFloat(vertex[posHead + 5])); //UV

        //DefaultVertexFormats.TEX_2S
        int UVLight = vertex[posHead + 6];//, (float)vertex[posHead + 7]); //UV

        return new VertexData(PosVec3, UVVec2d, UVLight);
    }

    public static class Index2VertexVec {
        private final int index;
        private final Vec3d vec;

        public Index2VertexVec(int index, Vec3d vec) {
            this.index = index;
            this.vec = vec;
        }

        public int getIndex() {
            return index;
        }

        public Vec3d getVec() {
            return vec;
        }

        @Override
        public String toString() {
            return "Index2VertexVec{" +
                    "index=" + index +
                    ", vec=" + vec +
                    '}';
        }
    }
}
