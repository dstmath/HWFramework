package ohos.agp.render.render3d.components;

import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.math.Matrix4x4;

public class LocalMatrixComponent implements Component {
    private Matrix4x4 mLocalMatrix;

    public Matrix4x4 getLocalMatrix() {
        return this.mLocalMatrix;
    }

    public void setLocalMatrix(Matrix4x4 matrix4x4) {
        this.mLocalMatrix = matrix4x4;
    }
}
