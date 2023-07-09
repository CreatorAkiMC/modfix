#version 110

attribute vec3 vertex_test;//いらない？
attribute vec3 a_pos;
attribute vec3 a_offset;
attribute vec4 test_color;

varying vec4 v_color;

uniform mat4 u_ModelViewProjectionMatrix;

void main() {
    vec3 pos = a_pos + a_offset;
    gl_Position = u_ModelViewProjectionMatrix * vec4(pos.xyz, 1.0);
    v_color = test_color;
}