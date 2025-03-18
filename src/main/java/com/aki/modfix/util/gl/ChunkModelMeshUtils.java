package com.aki.modfix.util.gl;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;

import javax.annotation.Nonnull;

public class ChunkModelMeshUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static IBakedModel getBakedModelFromState(IBlockState state) {
        return mc.getBlockRendererDispatcher().getModelForState(state);
    }

    public static IBlockState getExtendState(IBlockState state, BlockPos pos, IBlockAccess access) {
        return state.getBlock().getExtendedState(state, access, pos);
    }

    @Nonnull
    public static IBlockState getActualState(IBlockState blockState, BlockPos pos, IBlockAccess access) {
        if (access.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES)
        {
            try
            {
                return blockState.getBlock().getActualState(blockState, access, pos);
            } catch (Exception ignored) {
                try
                {
                    return blockState.getActualState(access, pos);
                } catch (Exception ignored2) {}
            }
        }
        return blockState;
    }

    private static double getDistanceFromCam(Vec3d vec3f1, double camX, double camY, double camZ) {
        return Math.pow((vec3f1.x - camX), 2.0d) + Math.pow((vec3f1.y - camY), 2.0d) + Math.pow((vec3f1.z - camZ), 2.0d);
    }

    /*private static Vec3d getCenterVec3(List<ChunkRenderTaskCompiler.Index2VertexVec> vecList, int primitiveSize) {
        Vec3d vec3d = Vec3d.ZERO;
        for(ChunkRenderTaskCompiler.Index2VertexVec vertexVec : vecList)
            vec3d.add(vertexVec.getVec());
        return vec3d.scale(1.0d / (double)primitiveSize);
    }*/

    //camX~Z プレイヤーの座標と ChunkRender.getX~Z() の差
    /*
    * それぞれの頂点をバラバラに移動するのではなく、まとまりとしてソートしなければいけない。
    * (OpenGLの GL_Quads は4つの頂点で面を作るため)
    * */
    /*public static List<ChunkRenderTaskCompiler.Index2VertexVec> SortIndex2VertexVec(List<ChunkRenderTaskCompiler.Index2VertexVec> data, int primitiveSize, double camX, double camY, double camZ) {
        int quadSize = data.size() / primitiveSize;
        List<List<ChunkRenderTaskCompiler.Index2VertexVec>> splitDataStream = IntStream.range(0, quadSize).boxed().map((i) -> data.subList((i * primitiveSize), ((i + 1) * primitiveSize))).collect(Collectors.toList());
        splitDataStream.sort((i2v1, i2v2) -> {
            double dist1 = getDistanceFromCam(getCenterVec3(i2v1, primitiveSize), camX, camY, camZ);
            double dist2 = getDistanceFromCam(getCenterVec3(i2v2, primitiveSize), camX, camY, camZ);
            return Double.compare(dist1, dist2);
        });

        return splitDataStream.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }*/
}
