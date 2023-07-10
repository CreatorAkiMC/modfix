#version 110

attribute vec3 vertexPos;
attribute vec3 a_pos;
attribute vec3 a_offset;

uniform mat4 u_ModelViewProjectionMatrix;

void main() {
    vec3 pos = a_pos + a_offset;
    mat4 m4 = mat4(1.0, 0.0, 0.0, 0.0,
                   0.0, 1.0, 0.0, 0.0,
                   0.0, 0.0, 1.0, 0.0,
                   pos.x, pos.y, pos.z, 1.0
    );
    gl_Position = u_ModelViewProjectionMatrix * m4 * vec4(vertexPos, 1.0);
}