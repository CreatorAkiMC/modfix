package com.aki.modfix.util.gl;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.Objects;

public class VertexData {
    // Pos
    private final Vector3f vec3;
    // Texture UV
    private final Vector2f vec2;
    public VertexData(Vector3f vec3f, Vector2f vec2f) {
        this.vec3 = vec3f;
        this.vec2 = vec2f;
    }

    public Vector3f getVec3() {
        return vec3;
    }

    public Vector2f getVec2() {
        return vec2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VertexData)) return false;
        VertexData that = (VertexData) o;
        return Objects.equals(getVec3(), that.getVec3()) && Objects.equals(getVec2(), that.getVec2());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getVec3(), this.getVec2());
    }

    @Override
    public String toString() {
        return "VertexData{" +
                "vec3=" + vec3 +
                ", vec2=" + vec2 +
                '}';
    }
}
