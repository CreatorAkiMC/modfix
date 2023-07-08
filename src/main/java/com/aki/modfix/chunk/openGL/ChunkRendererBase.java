package com.aki.modfix.chunk.openGL;

import com.aki.mcutils.APICore.program.shader.ShaderHelper;
import com.aki.mcutils.APICore.program.shader.ShaderObject;
import com.aki.mcutils.APICore.program.shader.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

import java.io.IOException;

public class ChunkRendererBase extends ShaderProgram {

    /*
    * ChunkRendererBase (継承した物も)を新しく作成した場合必ず呼ぶ。
    * */
    public void CreateShaders() {
        this.attachShader(this.CreateVertex());
        this.attachShader(this.CreateFragment());
    }

    public ShaderObject CreateVertex() {
        try {
            return new ShaderObject(ShaderObject.ShaderType.VERTEX, ShaderHelper.readShader(ShaderHelper.getStream("assets/modfix/shaders/chunk_gl20.v.glsl")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new NullPointerException("Don`t create ShaderObject Vertex");
    }

    public ShaderObject CreateFragment() {
        try {
            return new ShaderObject(ShaderObject.ShaderType.FRAGMENT, ShaderHelper.readShader(ShaderHelper.getStream("assets/modfix/shaders/chunk_gl20.f.glsl")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new NullPointerException("Don`t create ShaderObject Fragment");
    }
}
