package com.aki.modfix.util.gl;

import com.aki.mcutils.APICore.Utils.render.GLUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL45;

import java.nio.ByteBuffer;

public class GLHelper {
    public static int growBuffer(int vbo, long oldSize, long newSize) {
        if (GLUtils.CAPS.OpenGL45) {
            int newVbo = GL45.glCreateBuffers();
            GL45.glNamedBufferData(newVbo, newSize, GL15.GL_STREAM_DRAW);
            GL45.glCopyNamedBufferSubData(vbo, newVbo, 0L, 0L, oldSize);
            GL15.glDeleteBuffers(vbo);
            return newVbo;
        } else if (GLUtils.CAPS.OpenGL31) {
            int newVbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, newVbo);
            GL15.glBufferData(GL31.GL_COPY_WRITE_BUFFER, newSize, GL15.GL_STREAM_DRAW);
            GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, vbo);
            GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0L, 0L, oldSize);
            GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
            GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
            GL15.glDeleteBuffers(vbo);
            return newVbo;
        } else {
            int temp = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, temp);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, oldSize, GL15.GL_STREAM_COPY);
            ByteBuffer tempBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, oldSize, null);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, tempBuffer);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, newSize, GL15.GL_STREAM_DRAW);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, tempBuffer);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, temp);
            GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL15.glDeleteBuffers(temp);
            return vbo;
        }
    }

    /**
     * Size = 更新前の全体のサイズ
     * (Size - From) - (Size - To) -> To - From
     *----
     * Size 10, From 5, To 7
     * (10 - 5) - (10 - 7) = 5 - 3 = 2(増加)
     *----
     * Size 10, From 5, To 1
     * (10 - 5) - (10 - 1) = 5 - 9 = -4(減少)
     *----
     * Size 10, From 3, To 3
     * (10 - 3) - (10 - 3) = 7 - 7 = 0(変化なし)
     */
    public static int CopyMoveBuffer(int vbo, long Size, long FromOffset, long ToOffset) {
        long AddDataSize = ToOffset - FromOffset;//(Size - FromOffset) - (Size - ToOffset);
        long FromReadSize = Size - FromOffset;
        if (GLUtils.CAPS.OpenGL45) {
            int newVbo = GL45.glCreateBuffers();
            GL45.glNamedBufferData(newVbo, Size + AddDataSize, GL15.GL_DYNAMIC_DRAW);
            if(AddDataSize >= 0) {//増加するときは大きなBufferを作りそこに小さなものを入れる
                GL45.glCopyNamedBufferSubData(vbo, newVbo, 0, 0, Size);//すべてコピー
            } else {
                GL45.glCopyNamedBufferSubData(vbo, newVbo, 0, 0, ToOffset);//ToOffsetより前をコピー
                //GL45.glCopyNamedBufferSubData(vbo, newVbo, FromOffset, ToOffset);
                //GL45.glCopyNamedBufferSubData(vbo, newVbo, 0, 0, Size + AddDataSize);//減らしてコピー
            }
            GL45.glCopyNamedBufferSubData(vbo, newVbo, FromOffset, ToOffset, FromReadSize);
            GL15.glDeleteBuffers(vbo);
            return newVbo;
        } else if (GLUtils.CAPS.OpenGL31) {
            int newVbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, newVbo);
            GL15.glBufferData(GL31.GL_COPY_WRITE_BUFFER, Size + AddDataSize, GL15.GL_DYNAMIC_DRAW);
            GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, vbo);
            if(AddDataSize >= 0) {
                GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0, 0, Size);
                /*if(AddBuffer != null) {
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, newVbo);
                    GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, FromOffset, AddBuffer);
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                }*/
            } else
                GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0, 0, Size + AddDataSize);
            GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, FromOffset, ToOffset, FromReadSize);
            GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
            GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);
            GL15.glDeleteBuffers(vbo);
            return newVbo;
        } else {
            int NewVBO = GL15.glGenBuffers();
            int CopyTemp = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, NewVBO);
            if(AddDataSize >= 0) {
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Size + AddDataSize, GL15.GL_DYNAMIC_COPY);
                ByteBuffer VBOBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, Size, null);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, CopyTemp);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, FromReadSize, GL15.GL_DYNAMIC_COPY);
                ByteBuffer CopyBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, FromReadSize, null);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0, VBOBuffer);//全体をコピー(lengthの分だけ)
                GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, FromOffset, CopyBuffer);//移動もとをコピー

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, NewVBO);
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, VBOBuffer);
                /*if(AddBuffer != null) {
                    GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, FromOffset, AddBuffer);
                }*/
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, Size, CopyBuffer);
            } else {
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Size + AddDataSize, GL15.GL_DYNAMIC_COPY);
                ByteBuffer VBOBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, Size + AddDataSize, null);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, CopyTemp);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, FromReadSize, GL15.GL_DYNAMIC_COPY);
                ByteBuffer CopyBuffer = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_READ_WRITE, FromReadSize, null);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0, VBOBuffer);//全体をコピー(lengthの分だけ)
                GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, FromOffset, CopyBuffer);//移動もとをコピー

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, NewVBO);
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, VBOBuffer);
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, ToOffset, CopyBuffer);
            }

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, CopyTemp);
            GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, NewVBO);
            GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            return NewVBO;
        }
    }
}
