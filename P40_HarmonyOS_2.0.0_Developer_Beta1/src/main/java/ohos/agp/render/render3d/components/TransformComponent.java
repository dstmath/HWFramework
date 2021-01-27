package ohos.agp.render.render3d.components;

import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.math.Quaternion;
import ohos.agp.render.render3d.math.Vector3;

public class TransformComponent implements Component {
    private Vector3 mPosition;
    private Quaternion mRotation;
    private Vector3 mScale;

    public Vector3 getPosition() {
        return this.mPosition;
    }

    public void setPosition(Vector3 vector3) {
        this.mPosition = vector3;
    }

    public Quaternion getRotation() {
        return this.mRotation;
    }

    public void setRotation(Quaternion quaternion) {
        this.mRotation = quaternion;
    }

    public Vector3 getScale() {
        return this.mScale;
    }

    public void setScale(Vector3 vector3) {
        this.mScale = vector3;
    }
}
