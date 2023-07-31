#version 110

//attribute vec3 vertexPos;
attribute vec3 a_pos;
attribute vec4 a_color;
attribute vec2 a_TexCoord;
attribute vec2 a_LightCoord;
attribute vec3 a_offset;

uniform bool u_FogEnabled;
uniform int u_FogShape;
uniform mat4 u_ModelViewProjectionMatrix;

varying vec4 v_color;
varying vec2 v_TexCoord;
varying vec2 v_LightCoord;
varying float v_VertDistance;

float fog_distance(vec3 pos) {
    if (!u_FogEnabled) {
        return 0.0;
    }

    if (u_FogShape == 0) {
        return max(length(pos.xz), abs(pos.y));
    } else if (u_FogShape == 1) {
        return length(pos);
    }

    return 0.0;
}

void main() {
    vec3 pos = a_pos + a_offset;
    gl_Position = u_ModelViewProjectionMatrix * vec4(pos, 1.0);

    v_color = a_color;
    v_TexCoord = a_TexCoord;
    v_LightCoord = vec2((a_LightCoord.x + 8.0) * 0.00390625, (a_LightCoord.y + 8.0) * 0.00390625);
    v_VertDistance = fog_distance(pos);
}