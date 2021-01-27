package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.math.Matrix4x4;

public class LocalMatrixComponent implements Component {
    private Matrix4x4 mLocalMatrix;

    public Matrix4x4 getLocalMatrix() {
        return this.mLocalMatrix;
    }

    public void setLocalMatrix(Matrix4x4 localMatrix) {
        this.mLocalMatrix = localMatrix;
    }
}
