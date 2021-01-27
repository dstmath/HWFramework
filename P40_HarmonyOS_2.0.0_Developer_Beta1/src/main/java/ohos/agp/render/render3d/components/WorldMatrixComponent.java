package ohos.agp.render.render3d.components;

import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.math.Matrix4x4;

public class WorldMatrixComponent implements Component {
    private Matrix4x4 mWorldMatrix;

    public Matrix4x4 getWorldMatrix() {
        return this.mWorldMatrix;
    }

    public void setWorldMatrix(Matrix4x4 matrix4x4) {
        this.mWorldMatrix = matrix4x4;
    }
}
