package com.aki.modfix.chunk.openGL.test;

import com.aki.mcutils.APICore.Utils.matrixutil.Matrix4f;
import com.aki.mcutils.APICore.Utils.render.GLUtils;
import com.aki.mcutils.APICore.program.shader.ShaderHelper;
import com.aki.mcutils.APICore.program.shader.ShaderObject;
import com.aki.mcutils.APICore.program.shader.ShaderProgram;
import com.aki.modfix.chunk.GLSytem.GLMutableArrayBuffer;
import com.aki.modfix.chunk.GLSytem.GlCommandBuffer;
import com.aki.modfix.chunk.GLSytem.GlMutableBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GLRenderTest {
    public GlMutableBuffer VertexBuffer;
    public GlMutableBuffer PosBuffer;
    public GlMutableBuffer ColorBuffer;
    public GLMutableArrayBuffer VaoBuffer;
    public GlCommandBuffer commandBuffer;
    public ShaderProgram program;
    public float Aspect = 0.0f;//アスペクト比

    public GLRenderTest() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int width = dim.width;//幅
        int height = dim.height;//高さ

        int gcd = this.gcd(width, height);

        this.Aspect = (float)(width / gcd) / (float)(height / gcd);

        /**
         * GL_STATIC_DRAWは、一度だけアップロードしますが、アップロードしたデータは利用制限なしで再利用できます
         * */
        this.VertexBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.PosBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.ColorBuffer = new GlMutableBuffer(GL15.GL_STATIC_DRAW);
        this.VaoBuffer = new GLMutableArrayBuffer();
    }

    public void init() {
        if(this.commandBuffer != null)
            this.commandBuffer.delete();
        /**
         * (12Chunk Render * 2 + 1)^3 * 16(...12)
         * */
        this.commandBuffer = new GlCommandBuffer(250000, GL30.GL_MAP_WRITE_BIT, GL15.GL_STREAM_DRAW, GL30.GL_MAP_WRITE_BIT);

        try {
            program = new ShaderProgram();
            program.attachShader(new ShaderObject(ShaderObject.ShaderType.VERTEX, ShaderHelper.readShader(ShaderHelper.getStream("/assets/modfix/shaders/gltest_v.glsl"))));
            program.attachShader(new ShaderObject(ShaderObject.ShaderType.FRAGMENT, ShaderHelper.readShader(ShaderHelper.getStream("/assets/modfix/shaders/gltest_f.glsl"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * BlocKPos などを使ったほうがいい
     *
     * GL43などが原因？ <- サポートしていない？
     * */
    public void Render() {
        GL11.glPushMatrix();

        /**
         * プログラム開始
         * */
        program.useShader();

        //三角形を作る
        //これに位置座標を積分することで、自由に動かせる。
        float[] vertices = new float[] {
                0.5f, 0.5f, 0.0f,
                -0.5f, 0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f

                /*-0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,
                -0.5f, 0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f*/
        };
        FloatBuffer bufferData = BufferUtils.createFloatBuffer(vertices.length).put(vertices);

        // 頂点数 * 表示数 = 3 (頂点) * 10 (表示数)
        FloatBuffer PosData = BufferUtils.createFloatBuffer(3 * 10);
        FloatBuffer ColorData = BufferUtils.createFloatBuffer(4 * 10);
        for(int i = 0; i < 10; i++) {
            PosData.put(new float[] {
                    //とりあえず4倍
                    (float)Math.random() * 4.0f, (float)Math.random() * 4.0f, (float)Math.random() * 4.0f
            });

            ColorData.put(new float[] {
                    1.0f, 1.0f, 0.0f, 0.5f
            });
        }

        /**
         * これが原因？
         * */
        int bufferMode = GL15.GL_ARRAY_BUFFER;//GL40.GL_DRAW_INDIRECT_BUFFER;

        /**
         * 自動で flip() されるものを作ったほうがいいかも
         * uploadに付け加えるとか
         * */
        bufferData.flip();
        PosData.flip();
        ColorData.flip();


        this.VertexBuffer.bind(bufferMode);//PosBuffer をレンダリングにバインド -> 後の処理のバッファーになる
        this.VertexBuffer.upload(bufferMode, bufferData);
        this.VertexBuffer.unbind(bufferMode);

        this.PosBuffer.bind(bufferMode);
        this.PosBuffer.upload(bufferMode, PosData);
        this.PosBuffer.unbind(bufferMode);

        this.ColorBuffer.bind(bufferMode);
        this.ColorBuffer.upload(bufferMode, ColorData);
        this.ColorBuffer.unbind(bufferMode);

        //VBO -> VAO化
        this.VaoBuffer.bind();


        int V_pos = program.getAttributeLocation("vertex_test");//vec3 頂点座標
        this.VertexBuffer.bind(bufferMode);
        //3個ずつ割り当て
        GL20.glVertexAttribPointer(V_pos, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL33.glVertexAttribDivisor(V_pos, 0);//頂点なので 0
        GL20.glEnableVertexAttribArray(V_pos);//VAO内で、Index を固定化
        this.VertexBuffer.unbind(bufferMode);


        int A_pos = program.getAttributeLocation("a_pos");//vec3 位置
        this.PosBuffer.bind(bufferMode);
        //3個ずつ割り当て
        GL20.glVertexAttribPointer(A_pos, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL33.glVertexAttribDivisor(A_pos, 1);//1頂点で分割
        GL20.glEnableVertexAttribArray(A_pos);//VAO内で、Index を固定化
        this.PosBuffer.unbind(bufferMode);


        int T_color = program.getAttributeLocation("test_color");//vec4 色
        this.ColorBuffer.bind(bufferMode);
        //4個ずつ割り当て
        GL20.glVertexAttribPointer(T_color, 4, GL11.GL_FLOAT, false, 0, 0L);
        GL33.glVertexAttribDivisor(T_color, 1);//1頂点で分割
        GL20.glEnableVertexAttribArray(T_color);//VAO内で、Index を固定化
        this.ColorBuffer.unbind(bufferMode);

        this.VaoBuffer.unbind();


        int projectionMatrixIndex = program.getUniformLocation("u_ModelViewProjectionMatrix");
        Matrix4f mat4f = GLUtils.getProjectionModelViewMatrix().copy();
        //Matrix指定
        GLUtils.setMatrix(projectionMatrixIndex, mat4f);



        this.VaoBuffer.bind();/*
        this.VertexBuffer.bind(RenderBufferMode);
        this.PosBuffer.bind(RenderBufferMode);
        this.ColorBuffer.bind(RenderBufferMode);*/


        this.commandBuffer.begin();
        for(int i = 0; i < 10; i++) {
            //first (初期) 0, 頂点(四角) 4, BaseInstance i, instanceCount 1
            this.commandBuffer.addIndirectDrawCall(0, 4, i, 1);
        }
        this.commandBuffer.end();


        /**
         * レンダーリング
         * primecount レンダリングする物の数?
         * */
        int RenderBufferMode = GL40.GL_DRAW_INDIRECT_BUFFER;
        GL15.glBindBuffer(RenderBufferMode, this.commandBuffer.getBufferIndex());

        //GL11.glMultiDrawArrays にするべき？
        //https://litasa.github.io/blog/2017/09/04/OpenGL-MultiDrawIndirect-with-Individual-Textures
        GL43.glMultiDrawArraysIndirect(GL11.GL_QUADS, 0, 10,0); //<- GL43にサポートしていない？
        //GL11.glDrawArrays(GL11.GL_QUADS, 0, 10);// <-謎
        //ARBMultiDrawIndirect.glMultiDrawArraysIndirect(GL11.GL_QUADS, 0, 10, 0);
        //GL31.glDrawArraysInstanced(GL11.GL_QUADS, 0, 10, 10);

        this.commandBuffer.unbind(RenderBufferMode);


        this.VaoBuffer.unbind();

        //消去
        this.VertexBuffer.delete();
        this.PosBuffer.delete();
        this.ColorBuffer.delete();
        this.VaoBuffer.delete();
        this.commandBuffer.delete();

        program.releaseShader();
    }

    public void deleteDatas() {
        this.VertexBuffer.delete();
        this.PosBuffer.delete();
        this.ColorBuffer.delete();
        this.VaoBuffer.delete();

        if(this.commandBuffer != null) {
            this.commandBuffer.delete();
            program.releaseShader();
            program.Delete();
        }
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
