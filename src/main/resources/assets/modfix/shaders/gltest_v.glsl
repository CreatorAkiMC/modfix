#version 110

attribute vec3 vertex_test;
attribute vec3 a_pos;
attribute vec4 test_color;

varying vec4 v_color;

uniform mat4 u_ModelViewProjectionMatrix;

void main() {
    gl_Position = u_ModelViewProjectionMatrix * vec4(a_pos.xyz, 1.0) * vec4(vertex_test, 1.0);
    v_color = test_color;
}