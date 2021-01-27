package ohos.agp.render.render3d.util;

import java.util.Objects;
import ohos.agp.render.render3d.math.Vector3;

public class BoundingBox {
    private Vector3 mMax;
    private Vector3 mMin;

    public BoundingBox(Vector3 vector3, Vector3 vector32) {
        this.mMin = (Vector3) Objects.requireNonNull(vector3);
        this.mMax = (Vector3) Objects.requireNonNull(vector32);
    }

    public BoundingBox() {
        this(Vector3.ZERO, Vector3.ZERO);
    }

    public void setAabbMinMax(Vector3 vector3, Vector3 vector32) {
        if (vector3 == null || vector32 == null) {
            throw new NullPointerException();
        }
        this.mMin = vector3;
        this.mMax = vector32;
    }

    public Vector3 getAabbMin() {
        return this.mMin;
    }

    public Vector3 getAabbMax() {
        return this.mMax;
    }
}
