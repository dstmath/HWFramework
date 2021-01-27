package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.math.Quaternion;
import com.huawei.agpengine.math.Vector3;

public class TransformComponent implements Component {
    private Vector3 mPosition;
    private Quaternion mRotation;
    private Vector3 mScale;

    public Vector3 getPosition() {
        return this.mPosition;
    }

    public void setPosition(Vector3 position) {
        this.mPosition = position;
    }

    public Quaternion getRotation() {
        return this.mRotation;
    }

    public void setRotation(Quaternion rotation) {
        this.mRotation = rotation;
    }

    public Vector3 getScale() {
        return this.mScale;
    }

    public void setScale(Vector3 scale) {
        this.mScale = scale;
    }
}
