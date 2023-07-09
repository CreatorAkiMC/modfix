#version 110

attribute vec3 vertexPos;
attribute vec3 a_pos;
attribute vec3 a_offset;

uniform mat4 u_ModelViewProjectionMatrix;

void main() {
    vec3 pos = a_pos + a_offset;
    gl_Position = u_ModelViewProjectionMatrix * vec4(pos.xyz, 1.0) * vec4(vertexPos, 1.0);
}