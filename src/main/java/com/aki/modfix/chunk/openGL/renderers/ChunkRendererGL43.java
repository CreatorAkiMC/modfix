package com.aki.modfix.chunk.openGL.renderers;

//VertexBuffer を置き換える感じ
//texturedump が使えるかも

import com.aki.mcutils.APICore.Utils.matrixutil.Matrix4f;
import com.aki.mcutils.APICore.Utils.render.Frustum;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.modfix.chunk.GLSytem.GlCommandBuffer;
import com.aki.modfix.chunk.GLSytem.GlDynamicVBO;
import com.aki.modfix.chunk.GLSytem.GlVertexOffsetBuffer;
import com.aki.modfix.chunk.openGL.ChunkRender;
import com.aki.modfix.chunk.openGL.ChunkRenderProvider;
import com.aki.modfix.chunk.openGL.RenderEngineType;
import com.aki.modfix.util.gl.ChunkRenderPass;
import com.aki.modfix.util.gl.GLFogUtils;
import com.aki.modfix.util.gl.ListUtil;
import com.aki.modfix.util.gl.MapCreateHelper;
import org.lwjgl.opengl.*;

import java.util.Arrays;
import java.util.Objects;

/**
 * BlockRenderDispatcher で取得した BufferBuilder をもとに描画する。
 * */
public class ChunkRendererGL43 extends ChunkRendererBase<ChunkRender> {
    /**
     * Bufferのデータマッピング
     *
     *         BLOCK.addElement(POSITION_3F);
     *         BLOCK.addElement(COLOR_4UB); -> Color
     *         BLOCK.addElement(TEX_2F); -> TEXCOORD
     *         BLOCK.addElement(TEX_2S); -> LIGHTCOORD
     * */

    /**Bufferの構造 (VboRenderList - setupArrayPointers 参照)
     *          //Index 0 ~ 11 (Vec3 4頂点) までが
     *          GL20.glVertexAttribPointer(shader.getAttribute(A_POS), 3, GL11.GL_FLOAT, false, 28, 0);
     *          //Index 12 ~ 15 (Vec4 色 1個)
     * 			GL20.glVertexAttribPointer(shader.getAttribute(A_COLOR), 4, GL11.GL_UNSIGNED_BYTE, true, 28, 12);
     * 		    //Index 16 ~ 23 (Vec2 テクスチャの座標 2個)
     * 			GL20.glVertexAttribPointer(shader.getAttribute(A_TEXCOORD), 2, GL11.GL_FLOAT, false, 28, 16);
     * 		    //Index 24 ~ 27 (Vec2 光の座標 2個)
     * 			GL20.glVertexAttribPointer(shader.getAttribute(A_LIGHTCOORD), 2, GL11.GL_SHORT, false, 28, 24);
     * */
    public ChunkRendererGL43() {
        super();
    }

    @Override
    public RenderEngineType getRenderEngine() {
        return RenderEngineType.GL43;
    }

    @Override
    public void Init(int distance) {
        int PD = distance * 2 + 1;
        int dist3 = (int)Math.pow(PD, 3);

        this.CommandBuffers = MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i -> new GlCommandBuffer(dist3 * 16L, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, GL30.GL_MAP_WRITE_BIT));
        this.OffsetBuffers = MapCreateHelper.CreateLinkedHashMap(ChunkRenderPass.ALL, i -> new GlVertexOffsetBuffer(dist3 * 12L, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, GL30.GL_MAP_WRITE_BIT));
        this.DynamicBuffers.forEach((pass, vbo) -> vbo.AddListener(this::InitVAOs));
        this.InitVAOs();
    }

    private void InitVAOs() {
        try {
            this.program.useShader();

            this.VaoBuffers.forEach((renderPass, VAO) -> {

                VAO.ChangeNewVAO();
                VAO.bind();

                int A_Pos = this.program.getAttributeLocation(A_POS);
                int A_Color = this.program.getAttributeLocation(A_COLOR);
                int a_texCoord = this.program.getAttributeLocation(A_TEXCOORD);
                int a_lightCoord = this.program.getAttributeLocation(A_LIGHTCOORD);
                int Offset = program.getAttributeLocation(A_OFFSET);

                //読み込み
                this.DynamicBuffers.get(renderPass).bind(GL15.GL_ARRAY_BUFFER);

                //Size は分割する量
                GL20.glVertexAttribPointer(A_Pos, 3, GL11.GL_FLOAT, false, 28, 0L);
                GL20.glVertexAttribPointer(A_Color, 4, GL11.GL_UNSIGNED_BYTE, true, 28, 12L);
                GL20.glVertexAttribPointer(a_texCoord, 2, GL11.GL_FLOAT, false, 28, 16L);
                GL20.glVertexAttribPointer(a_lightCoord, 2, GL11.GL_SHORT, false, 28, 24L);
                GL20.glEnableVertexAttribArray(A_Pos);
                GL20.glEnableVertexAttribArray(A_Color);
                GL20.glEnableVertexAttribArray(a_texCoord);
                GL20.glEnableVertexAttribArray(a_lightCoord);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.OffsetBuffers.get(renderPass).getBufferIndex());
                GL20.glVertexAttribPointer(Offset, 3, GL11.GL_FLOAT, false, 0, 0L);
                GL20.glEnableVertexAttribArray(Offset);//VAO内で、Index を固定化
                GL33.glVertexAttribDivisor(Offset, 1);//1頂点で分割
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

                this.DynamicBuffers.get(renderPass).unbind(GL15.GL_ARRAY_BUFFER);

                VAO.unbind();
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.program.releaseShader();
        }
    }

    /**
     * RenderGlobal SetupTerrain でする
     * */
    @Override
    public void SetUP(ChunkRenderProvider<ChunkRender> provider, double cameraX, double cameraY, double cameraZ, Frustum frustum, int Frame) {
        super.SetUP(provider, cameraX, cameraY, cameraZ, frustum, Frame);
        try {
            this.SyncList.ToNext();

            Arrays.stream(ChunkRenderPass.ALL).forEach(pass -> {
                this.OffsetBuffers.get(pass).begin();
                this.CommandBuffers.get(pass).begin();
            });

            if (this.SyncList.getSelect() != -1) {
                GL33.glGetQueryObjecti64(this.SyncList.getSelect(), GL15.GL_QUERY_RESULT);
                GL15.glDeleteQueries(this.SyncList.getSelect());
                this.SyncList.setSelect(-1);
            }

            this.RenderChunks.forEach((pass, list) -> ListUtil.forEach(list, pass == ChunkRenderPass.TRANSLUCENT,(chunkRender, index) ->{

                //this.OffsetBuffers.get(pass).addIndirectDrawOffsetCall((float) (0 - cameraX), (float) (0 - cameraY), (float) (0 - cameraZ));
                this.OffsetBuffers.get(pass).addIndirectDrawOffsetCall((float) (chunkRender.getX() - cameraX), (float) (chunkRender.getY() - cameraY), (float) (chunkRender.getZ() - cameraZ));

                //first (初期) 0, 頂点(四角 = 4 or 三角形 * 2 = 6？ 立方体は 3 * 2 * 6 -> 36 ?) 4, BaseInstance i, instanceCount 1
                //first オフセット スキップする頂点の数を入れる <- renderBlock で取得した Buffer を DefaultVertexFormats.BLOCK.getSize() で割るとよさそう (合計)。
                //だから、連続する見えないブロックではそれぞれのブロックのBuffer を合計して割るとよさそう。
                //(ただし、連続しないブロックの場合、addIndirectDrawCall を分割して実行するとよさそう (配列で))

                //DefaultVertexFormats.BLOCK.getSize() は 11
                //...スキップするブロックの数は引いておいたほうが軽くなる
                GlDynamicVBO.VBOPart part = Objects.requireNonNull(chunkRender.getVBO(pass));
                this.CommandBuffers.get(pass).addIndirectDrawCall(part.getVBOFirst(), part.getVertexCount(), index, 1);
            }));

            Arrays.stream(ChunkRenderPass.ALL).forEach(pass -> {
                this.CommandBuffers.get(pass).end();
                this.OffsetBuffers.get(pass).end();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RenderChunks(ChunkRenderPass pass) {
        int projectionMatrixIndex = program.getUniformLocation("u_ModelViewProjectionMatrix");
        Matrix4f mat4f = GLUtils.getProjectionModelViewMatrix().copy();
        //座標移動
        mat4f.translate((float) GLUtils.getCameraOffsetX(), (float) GLUtils.getCameraOffsetY(), (float) GLUtils.getCameraOffsetZ());
        //Matrix指定
        GLUtils.setMatrix(projectionMatrixIndex, mat4f);

        GLFogUtils.setupFogFromGL(program);

        this.VaoBuffers.get(pass).bind();
        int RenderBufferMode = GL40.GL_DRAW_INDIRECT_BUFFER;
        this.CommandBuffers.get(pass).bind(RenderBufferMode);
        //this.CommandBuffers.get(pass).getCount() == this.RenderChunks.get(pass).size()

        //Test
        GL43.glMultiDrawArraysIndirect(GL11.GL_QUADS, 0, this.CommandBuffers.get(pass).getCount(), 0);
        if (pass == ChunkRenderPass.TRANSLUCENT) {//同期
            if (this.SyncList.getSelect() != -1)
                GL15.glDeleteQueries(this.SyncList.getSelect());
            int query = GL15.glGenQueries();
            GL33.glQueryCounter(query, GL33.GL_TIMESTAMP);
            this.SyncList.setSelect(query);
        }
        this.CommandBuffers.get(pass).unbind(RenderBufferMode);//GL15.glBindBuffer(RenderBufferMode, 0);
        this.VaoBuffers.get(pass).unbind();
    }
}
