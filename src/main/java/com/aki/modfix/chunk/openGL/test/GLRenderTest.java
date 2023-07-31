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
import com.aki.modfix.util.gl.GLFogUtils;
import com.aki.modfix.util.gl.RTList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Random;

public class GLRenderTest {
    public GlMutableBuffer PosBuffer;
    public GlMutableBuffer ColorBuffer;
    public GlMutableBuffer TexCoordBuffer;
    public GlMutableBuffer LightCoordBuffer;

    public GlMutableBuffer DynamicVBO = null;

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
        this.PosBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.ColorBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.TexCoordBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.LightCoordBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);

        this.DynamicVBO = new GlMutableBuffer(GL15.GL_STATIC_DRAW);

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

    public BufferBuilder bufferBuilder = new BufferBuilder(100000);

    //init で実行
    //BlockRenderLayers分やったほうがいい？
    private void InitBuffer() {
        try {

            bufferBuilder.reset();
            //bufferBuilder.finishDrawing();
            if(!bufferBuilder.isDrawing) {
                bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                bufferBuilder.setTranslation(-0, -0, -0);//RenderChunk の中心座標を引く
            }

            Block[] RenderArray = new Block[] {Blocks.TNT, Blocks.WATER, Blocks.PLANKS, Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK, Blocks.IRON_ORE, Blocks.IRON_BLOCK, Blocks.GOLD_ORE, Blocks.GOLD_BLOCK, Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK, Blocks.FARMLAND, Blocks.END_STONE, Blocks.END_ROD, Blocks.TORCH};

            int r = 1 + (int)(Math.random() * 10);
            int Range = 10;
            for(int PosX = -Range; PosX <= Range; PosX++)
                 for(int PosZ = -Range; PosZ <= Range; PosZ++) {
                     Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(RenderArray[Math.abs(PosX + PosZ) % RenderArray.length].getDefaultState(), new BlockPos(PosX * r, 10, PosZ * r), FMLClientHandler.instance().getWorldClient(), bufferBuilder);
                 }

            Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(Blocks.TNT.getDefaultState(), new BlockPos(0, 10, 0), FMLClientHandler.instance().getWorldClient(), bufferBuilder);
            Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(Blocks.FARMLAND.getDefaultState(), new BlockPos(1, 10, 0), FMLClientHandler.instance().getWorldClient(), bufferBuilder);
            Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(Blocks.DIAMOND_ORE.getDefaultState(), new BlockPos(-1, 10, 0), FMLClientHandler.instance().getWorldClient(), bufferBuilder);
            Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(Blocks.END_ROD.getDefaultState(), new BlockPos(0, 10, 1), FMLClientHandler.instance().getWorldClient(), bufferBuilder);
            //ガラスのテクスチャは、ソートして、最後のほうで描画しないと透過しない。
            Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(Blocks.GLASS.getDefaultState(), new BlockPos(0, 10, -1), FMLClientHandler.instance().getWorldClient(), bufferBuilder);

            /**
             * Pos は正しくは VertexPos(形を作る)
             * Pos(VertexPos) は面ごと設定しなおし、レンダリングするのが一番いい
             * (同じ面の形状のコピーはいくらでも取れる)
             * */
            /*float[] Pos = new float[]{
                    0.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f
            };

            float DC_Offset = 0.8F;

            float[] Color = new float[]{
                    (float)Math.random() * DC_Offset + 0.2f, (float)Math.random() * DC_Offset + 0.2f, (float)Math.random() * DC_Offset + 0.2f, 1.0F,
                    (float)Math.random() * DC_Offset + 0.2f, (float)Math.random() * DC_Offset + 0.2f, (float)Math.random() * DC_Offset + 0.2f, 1.0F,
                    (float)Math.random() * DC_Offset + 0.2f, (float)Math.random() * DC_Offset + 0.2f, (float)Math.random() * DC_Offset + 0.2f, 1.0F,
                    (float)Math.random() * DC_Offset + 0.2f, (float)Math.random() * DC_Offset + 0.2f, (float)Math.random() * DC_Offset + 0.2f, 1.0F
            };*/

            /*FloatBuffer PosData = BufferUtils.createFloatBuffer(Pos.length).put(Pos);
            FloatBuffer ColorData = BufferUtils.createFloatBuffer(Color.length).put(Color);
            PosData.flip();
            ColorData.flip();*/

            //位置座標 -> これを頂点にすればよい。
            /*PosBuffer.bind(GL15.GL_ARRAY_BUFFER);
            PosBuffer.upload(GL15.GL_ARRAY_BUFFER, PosData);
            PosBuffer.unbind(GL15.GL_ARRAY_BUFFER);

            ColorBuffer.bind(GL15.GL_ARRAY_BUFFER);
            ColorBuffer.upload(GL15.GL_ARRAY_BUFFER, ColorData);
            ColorBuffer.unbind(GL15.GL_ARRAY_BUFFER);

            TexCoordBuffer.bind(GL15.GL_ARRAY_BUFFER);
            TexCoordBuffer.upload(GL15.GL_ARRAY_BUFFER, );
            TexCoordBuffer.unbind(GL15.GL_ARRAY_BUFFER);

            LightCoordBuffer.bind(GL15.GL_ARRAY_BUFFER);
            LightCoordBuffer.upload(GL15.GL_ARRAY_BUFFER, );
            LightCoordBuffer.unbind(GL15.GL_ARRAY_BUFFER);*/

            /**
             * すべてのデータを一つの VBO にまとめる。
             * */
            this.DynamicVBO.bind(GL15.GL_ARRAY_BUFFER);
            this.DynamicVBO.upload(GL15.GL_ARRAY_BUFFER, bufferBuilder.getByteBuffer());
            this.DynamicVBO.unbind(GL15.GL_ARRAY_BUFFER);

            this.program.useShader();

            this.VaoBuffer.bind();

            int A_Pos = this.program.getAttributeLocation("a_pos");
            int A_Color = this.program.getAttributeLocation("a_color");
            int a_texCoord = this.program.getAttributeLocation("a_TexCoord");
            int a_lightCoord = this.program.getAttributeLocation("a_LightCoord");
            int Offset = program.getAttributeLocation("a_offset");

            //読み込み
            this.DynamicVBO.bind(GL15.GL_ARRAY_BUFFER);


            /**
             * 一応、謎の黒い四角い物体は表示される。
             *  色がない(黒だからある？)
             *  平面が1つ
             *
             *  addIndirectDrawCall が関係あるかも？ (first や count) 26にすればいける？ count は 36 ?
             * */
            //Size は分割する量
            GL20.glVertexAttribPointer(A_Pos, 3, GL11.GL_FLOAT, false, 28, 0L);
            GL20.glVertexAttribPointer(A_Color, 4, GL11.GL_UNSIGNED_BYTE, true, 28, 12L);
            GL20.glVertexAttribPointer(a_texCoord, 2, GL11.GL_FLOAT, false, 28, 16L);
            GL20.glVertexAttribPointer(a_lightCoord, 2, GL11.GL_SHORT, false, 28, 24L);
            GL20.glEnableVertexAttribArray(A_Pos);
            GL20.glEnableVertexAttribArray(A_Color);
            GL20.glEnableVertexAttribArray(a_texCoord);
            GL20.glEnableVertexAttribArray(a_lightCoord);

            //this.offsetBufferがここで登録されても、内部ではメモリのアドレスが共有されているため後から変更されても
            //ちゃんと変更が適用される
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.offsetBuffer.getBufferIndex());
            GL20.glVertexAttribPointer(Offset, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL20.glEnableVertexAttribArray(Offset);//VAO内で、Index を固定化
            GL33.glVertexAttribDivisor(Offset, 1);//1頂点で分割
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            this.DynamicVBO.unbind(GL15.GL_ARRAY_BUFFER);//念のため

            /*PosBuffer.bind(GL15.GL_ARRAY_BUFFER);
            int A_Pos = this.program.getAttributeLocation("a_pos");
            //Size は分割する量
            GL20.glVertexAttribPointer(A_Pos, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL20.glEnableVertexAttribArray(A_Pos);
            GL33.glVertexAttribDivisor(A_Pos, 0);//0にすると共有されて、メモリを削減できる
            PosBuffer.unbind(GL15.GL_ARRAY_BUFFER);

            ColorBuffer.bind(GL15.GL_ARRAY_BUFFER);
            int A_Color = this.program.getAttributeLocation("a_color");
            //Size は分割する量
            GL20.glVertexAttribPointer(A_Color, 4, GL11.GL_FLOAT, false, 0, 0L);
            GL20.glEnableVertexAttribArray(A_Color);
            //消すと滑らかになる？
            // https://shizenkarasuzon.hatenablog.com/entry/2020/08/08/134830#%E3%83%87%E3%83%BC%E3%82%BF%E5%85%B1%E6%9C%89%E3%81%AB%E3%82%88%E3%82%8B%E3%83%A1%E3%83%A2%E3%83%AA%E5%89%8A%E6%B8%9BglVertexAttribDivisor
            // を参照
            //デバッグ用にすべてに共有
            GL33.glVertexAttribDivisor(A_Color, 0);//0にすると共有されて、メモリを削減できる
            ColorBuffer.unbind(GL15.GL_ARRAY_BUFFER);

            //this.offsetBufferがここで登録されても、内部ではメモリのアドレスが共有されているため後から変更されても
            //ちゃんと変更が適用される
            int Offset = program.getAttributeLocation("a_offset");
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.offsetBuffer.getBufferIndex());
            GL20.glVertexAttribPointer(Offset, 3, GL11.GL_FLOAT, false, 0, 0L);
            GL20.glEnableVertexAttribArray(Offset);//VAO内で、Index を固定化
            GL33.glVertexAttribDivisor(Offset, 1);//1頂点で分割

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);*/

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

            int DebugCount = 1;

            //System.out.println("Render Debug Count: " + DebugCount);

            Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

            double factor = 0.75;

            if(entity != null) {
                for (Vec3d vec3d : new Vec3d[]{new Vec3d(0, 0, 0)}) {//なんとなく Vec3d( 0 0 0 )
                    /**
                     * 位置座標を決める
                     * チャンクの中心でいい？
                     * */
                    this.offsetBuffer.addIndirectDrawOffsetCall((float) (0 - GLUtils.getCameraX() - entity.motionX * factor), (float) (0 - GLUtils.getCameraY() - entity.motionY * factor), (float) (0 - GLUtils.getCameraZ() - entity.motionZ * factor));
                }
            }

            this.offsetBuffer.end();

            this.commandBuffer.begin();

            /**
             * count の値は本当に大事 -> 形にかかわる
             * 色を付けたほうが見やすいかも？
             * */
            for (int i = 0; i < DebugCount/*2*/; i++) {// i < 表示するものの量
                //first (初期) 0, 頂点(四角 = 4 or 三角形 * 2 = 6？ 立方体は 3 * 2 * 6 -> 36 ?) 4, BaseInstance i, instanceCount 1

                //first オフセット スキップする頂点の数を入れる <- renderBlock で取得した Buffer を DefaultVertexFormats.BLOCK.getSize() で割るとよさそう (合計)。
                //だから、連続する見えないブロックではそれぞれのブロックのBuffer を合計して割るとよさそう。
                //(ただし、連続しないブロックの場合、addIndirectDrawCall を分割して実行するとよさそう (配列で))

                //DefaultVertexFormats.BLOCK.getSize() は 11
                //...スキップするブロックの数は引いておいたほうが軽くなる
                this.commandBuffer.addIndirectDrawCall(0, this.bufferBuilder.getByteBuffer().limit() / DefaultVertexFormats.BLOCK.getSize(), i, 1);
            }
            this.commandBuffer.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Render(BlockRenderLayer layer) {
        try {

            program.useShader(cache -> {
                //マッピング用
                cache.glUniform1I("u_BlockTex", 0);
                cache.glUniform1I("u_LightTex", 1);
            });

            int projectionMatrixIndex = program.getUniformLocation("u_ModelViewProjectionMatrix");
            Matrix4f mat4f = GLUtils.getProjectionModelViewMatrix().copy();
            //座標移動
            mat4f.translate((float) GLUtils.getCameraOffsetX(), (float) GLUtils.getCameraOffsetY(), (float) GLUtils.getCameraOffsetZ());
            //Matrix指定
            GLUtils.setMatrix(projectionMatrixIndex, mat4f);

            GLFogUtils.setupFogFromGL(program);

            this.VaoBuffer.bind();

            int RenderBufferMode = GL40.GL_DRAW_INDIRECT_BUFFER;
            this.commandBuffer.bind(RenderBufferMode);

            //for(int i = 0; i < Count; i++) {
                //Indirect_Buffer_Offset は CommandBuffer の オフセット -> いろいろできる
            GL43.glMultiDrawArraysIndirect(GL11.GL_QUADS, 0, this.commandBuffer.getCount(), 0);
            //}

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
        this.PosBuffer.delete();
        this.ColorBuffer.delete();
        this.TexCoordBuffer.delete();
        this.LightCoordBuffer.delete();

        this.DynamicVBO.delete();

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
