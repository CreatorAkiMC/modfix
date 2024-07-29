package com.aki.modfix.util.gl;

import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector2d;
import java.util.Objects;

public class VertexData {
    // Pos
    private final Vec3d posVec3;
    // Texture UV
    private final Vector2d UVVec2;
    //Light UV
    private final int UVLight;

    public VertexData(Vec3d posVec3f, Vector2d UVVec2f, int UVLight) {
        this.posVec3 = posVec3f;
        this.UVVec2 = UVVec2f;
        this.UVLight = UVLight;
    }

    public Vec3d getPosVec3() {
        return posVec3;
    }

    public Vector2d getUVVec2() {
        return UVVec2;
    }

    public int getUVLight() {
        return UVLight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VertexData)) return false;
        VertexData that = (VertexData) o;
        return Objects.equals(getPosVec3(), that.getPosVec3()) && Objects.equals(getUVVec2(), that.getUVVec2()) && Objects.equals(getUVLight(), that.getUVLight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPosVec3(), this.getUVVec2(), this.getUVLight() * 31);
    }

    @Override
    public String toString() {
        return "VertexData{" +
                "posVec3=" + posVec3 +
                ", UVVec2=" + UVVec2 +
                ", UVLight=" + UVLight +
                '}';
    }
}
