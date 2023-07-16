#version 110

//attribute vec3 vertexPos;
attribute vec3 a_pos;
attribute vec3 a_offset;
attribute vec4 a_color;//Only use varying

uniform mat4 u_ModelViewProjectionMatrix;

varying vec4 v_color;

void main() {
    v_color = a_color;

    vec3 pos = a_pos + a_offset;
    gl_Position = u_ModelViewProjectionMatrix * vec4(pos, 1.0);
}