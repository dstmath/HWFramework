package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.math.Matrix4x4;

public class WorldMatrixComponent implements Component {
    private Matrix4x4 mWorldMatrix;

    public Matrix4x4 getWorldMatrix() {
        return this.mWorldMatrix;
    }

    public void setWorldMatrix(Matrix4x4 worldMatrix) {
        this.mWorldMatrix = worldMatrix;
    }
}
