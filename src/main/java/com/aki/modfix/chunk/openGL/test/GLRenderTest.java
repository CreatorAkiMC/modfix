package com.aki.modfix.chunk.openGL.test;

import com.aki.mcutils.APICore.Utils.matrixutil.Matrix4f;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.mcutils.APICore.program.shader.ShaderHelper;
import com.aki.mcutils.APICore.program.shader.ShaderObject;
import com.aki.mcutils.APICore.program.shader.ShaderProgram;
import com.aki.modfix.chunk.GLSytem.GLMutableArrayBuffer;
import com.aki.modfix.chunk.GLSytem.GlCommandBuffer;
import com.aki.modfix.chunk.GLSytem.GlMutableBuffer;
import com.aki.modfix.chunk.GLSytem.GlVertexOffsetBuffer;
import com.aki.modfix.util.gl.RTList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class GLRenderTest {
    public GlMutableBuffer VertexBuffer;
    public GlMutableBuffer PosBuffer;
    public GlMutableBuffer ColorBuffer;
    public GLMutableArrayBuffer VaoBuffer;

    public GlCommandBuffer commandBuffer;
    public GlVertexOffsetBuffer offsetBuffer;

    public ShaderProgram program;
    public float Aspect = 0.0f;//アスペクト比

    private RTList<Integer> SyncList;

    public GLRenderTest() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int width = dim.width;//幅
        int height = dim.height;//高さ

        int gcd = this.gcd(width, height);

        this.Aspect = (float)(width / gcd) / (float)(height / gcd);

        /**
         * GL_STATIC_DRAWは、一度だけアップロードしますが、アップロードしたデータは利用制限なしで再利用できます
         * */

        /**
         * 一つにまとめたほうが便利
         * Consumerを使って処理するといいかも
         * */
        this.VertexBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.PosBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.ColorBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.VaoBuffer = new GLMutableArrayBuffer();
        this.SyncList = new RTList<>(0, Arrays.asList(-1, -1));
    }

    public void init() {
        if(this.commandBuffer != null)
            this.commandBuffer.delete();
        if(this.offsetBuffer != null)
            this.offsetBuffer.delete();
        /**
         * (12Chunk Render * 2 + 1)^3 * 16 = 250000
         * */
        this.commandBuffer = new GlCommandBuffer(250000, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, GL30.GL_MAP_WRITE_BIT);

        /**
         * (12Chunk Render * 2 + 1)^3 * 12 = 187500
         * */
        this.offsetBuffer = new GlVertexOffsetBuffer(187500, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, GL30.GL_MAP_WRITE_BIT);

        try {
            program = new ShaderProgram();
            program.attachShader(new ShaderObject(ShaderObject.ShaderType.VERTEX, ShaderHelper.readShader(ShaderHelper.getStream("/assets/modfix/shaders/gltest_v.glsl"))));
            program.attachShader(new ShaderObject(ShaderObject.ShaderType.FRAGMENT, ShaderHelper.readShader(ShaderHelper.getStream("/assets/modfix/shaders/gltest_f.glsl"))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.InitBuffer();
    }

    //init で実行
    //BlockRenderLayers分やったほうがいい？
    private void InitBuffer() {
        try {

            //四角形
            /*float[] vertices = new float[]{
                    0.5f, 0.5f, 0.0f,
                    -0.5f, 0.5f, 0.0f,
                    0.5f, -0.5f, 0.0f,
                    -0.5f, -0.5f, 0.0f
            };*/

            float[] Pos = new float[]{
                    1.5f, 1.5f, 0.0f,
                    -1.5f, 1.5f, 0.0f,
                    1.5f, -1.5f, 0.0f//,
                    -1.5f, -1.5f, 0.0f
            };

            float[] Color = new float[]{
                    (float)Math.random() * 10.0F + 0.2f, (float)Math.random() * 10.0F + 0.2f, (float)Math.random() * 10.0F + 0.2f, 1.0F,
                    (float)Math.random() * 10.0F + 0.2f, (float)Math.random() * 10.0F + 0.2f, (float)Math.random() * 10.0F + 0.2f, 1.0F,
                    (float)Math.random() * 10.0F + 0.2f, (float)Math.random() * 10.0F + 0.2f, (float)Math.random() * 10.0F + 0.2f, 1.0F,
                    (float)Math.random() * 10.0F + 0.2f, (float)Math.random() * 10.0F + 0.2f, (float)Math.random() * 10.0F + 0.2f, 1.0F
            };

            //FloatBuffer bufferData = BufferUtils.createFloatBuffer(vertices.length).put(vertices);
            FloatBuffer PosData = BufferUtils.createFloatBuffer(Pos.length).put(Pos);
            FloatBuffer ColorData = BufferUtils.createFloatBuffer(Color.length).put(Color);
            //bufferData.flip();
            PosData.flip();
            //頂点座標
            /*VertexBuffer.bind(GL15.GL_ARRAY_BUFFER);
            VertexBuffer.upload(GL15.GL_ARRAY_BUFFER, bufferData);
            VertexBuffer.unbind(GL15.GL_ARRAY_BUFFER);*/

            //位置座標 -> これを頂点にすればよい。
            PosBuffer.bind(GL15.GL_ARRAY_BUFFER);
            PosBuffer.upload(GL15.GL_ARRAY_BUFFER, PosData);
            PosBuffer.unbind(GL15.GL_ARRAY_BUFFER);

            ColorBuffer.bind(GL15.GL_ARRAY_BUFFER);
            ColorBuffer.upload(GL15.GL_ARRAY_BUFFER, ColorData);
            ColorBuffer.unbind(GL15.GL_ARRAY_BUFFER);

            this.program.useShader();

            this.VaoBuffer.bind();

            /*VertexBuffer.bind(GL15.GL_ARRAY_BUFFER);
            int VPos = this.program.getAttributeLocation("vertexPos");
            //Size は分割する量
            GL20.glVertexAttribPointer(VPos, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL20.glEnableVertexAttribArray(VPos);
            GL33.glVertexAttribDivisor(VPos, 0);//頂点は４つで複数ないので分割しない -> 0
            VertexBuffer.unbind(GL15.GL_ARRAY_BUFFER);*/

            PosBuffer.bind(GL15.GL_ARRAY_BUFFER);
            int A_Pos = this.program.getAttributeLocation("a_pos");
            //Size は分割する量
            GL20.glVertexAttribPointer(A_Pos, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL20.glEnableVertexAttribArray(A_Pos);
            //GL33.glVertexAttribDivisor(A_Pos, 1);//複数図形の座標があるので-> 1  (いらない？) -> Vertexの代わりに使うから行わない
            PosBuffer.unbind(GL15.GL_ARRAY_BUFFER);

            ColorBuffer.bind(GL15.GL_ARRAY_BUFFER);
            int A_Color = this.program.getAttributeLocation("a_color");
            //Size は分割する量
            GL20.glVertexAttribPointer(A_Color, 4, GL11.GL_FLOAT, false, 0, 0L);
            GL20.glEnableVertexAttribArray(A_Color);
            //GL33.glVertexAttribDivisor(A_Pos, 1);//複数図形の座標があるので-> 1  (いらない？) -> Vertexの代わりに使うから行わない
            ColorBuffer.unbind(GL15.GL_ARRAY_BUFFER);


            //this.offsetBufferがここで登録されても、内部ではメモリのアドレスが共有されているため後から変更されても
            //ちゃんと変更が適用される
            int Offset = program.getAttributeLocation("a_offset");
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.offsetBuffer.getBufferIndex());

            GL20.glVertexAttribPointer(Offset, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL20.glEnableVertexAttribArray(Offset);//VAO内で、Index を固定化
            GL33.glVertexAttribDivisor(Offset, 1);//1頂点で分割

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            this.VaoBuffer.unbind();

            this.program.releaseShader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * RenderGlobal SetupTerrain でする
     * */
    public void SetUP () {
        try {
            this.SyncList.ToNext();
            this.offsetBuffer.begin();

            if (this.SyncList.getSelect() != -1) {
                GL33.glGetQueryObjecti64(this.SyncList.getSelect(), GL15.GL_QUERY_RESULT);
                GL15.glDeleteQueries(this.SyncList.getSelect());
                this.SyncList.setSelect(-1);
            }

            double ObjectX = 0.5; //+ Math.random() * 10;
            double ObjectY = 1.5; //+ Math.random() * 10;
            double ObjectZ = 0.5; //+ Math.random() * 10;

            this.offsetBuffer.addIndirectDrawOffsetCall((float) (ObjectX - GLUtils.getCameraX()), (float) (ObjectY - GLUtils.getCameraY()), (float) (ObjectZ - GLUtils.getCameraZ()));

            this.offsetBuffer.end();

            this.commandBuffer.begin();

            /**
             * count の値は本当に大事 -> 形にかかわる
             * 色を付けたほうが見やすいかも？
             * */
            for (int i = 0; i < 1; i++) {// i < 表示するものの量
                //first (初期) 0, 頂点(四角 = 三角形 * 2) 6, BaseInstance i, instanceCount 1
                this.commandBuffer.addIndirectDrawCall(i, 12, i, 1);
            }
            this.commandBuffer.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Render(BlockRenderLayer layer) {
        try {

            program.useShader();

            int projectionMatrixIndex = program.getUniformLocation("u_ModelViewProjectionMatrix");
            Matrix4f mat4f = GLUtils.getProjectionModelViewMatrix().copy();
            //座標移動
            mat4f.translate((float) GLUtils.getCameraOffsetX(), (float) GLUtils.getCameraOffsetY(), (float) GLUtils.getCameraOffsetZ());
            //Matrix指定
            GLUtils.setMatrix(projectionMatrixIndex, mat4f);

            this.VaoBuffer.bind();

            int RenderBufferMode = GL40.GL_DRAW_INDIRECT_BUFFER;
            this.commandBuffer.bind(RenderBufferMode);

            GL43.glMultiDrawArraysIndirect(GL11.GL_QUADS, 0, this.commandBuffer.getCount(), 0); //<- GL43にサポートしていない？
            if (layer == BlockRenderLayer.TRANSLUCENT) {//同期
                if (this.SyncList.getSelect() != -1)
                    GL15.glDeleteQueries(this.SyncList.getSelect());
                int query = GL15.glGenQueries();
                GL33.glQueryCounter(query, GL33.GL_TIMESTAMP);
                this.SyncList.setSelect(query);
            }

            this.commandBuffer.unbind(RenderBufferMode);//GL15.glBindBuffer(RenderBufferMode, 0);

            this.VaoBuffer.unbind();
            program.releaseShader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteDatas() {
        this.VertexBuffer.delete();
        this.PosBuffer.delete();
        this.ColorBuffer.delete();
        this.VaoBuffer.delete();

        if(this.commandBuffer != null) {
            this.commandBuffer.delete();
            this.offsetBuffer.delete();
            program.Delete();
        }

        this.SyncList.getList().stream().filter(i -> i != -1).forEach(GL15::glDeleteQueries);

        /*this.VertexBuffer = null;
        this.PosBuffer = null;
        this.ColorBuffer = null;
        this.VaoBuffer = null;
        this.commandBuffer = null;
        this.offsetBuffer = null;
        this.program = null;*/
    }

    public int gcd(int a, int b) {
        // ユークリッド互除法にて最大公倍数を算出
        while(a!=b) {
            if(a>b) {
                a -= b;
            }else {
                b -= a;
            }

        }

        return a;
    }
}
