package ohos.agp.render.render3d.resources;

import java.util.Arrays;
import ohos.agp.render.render3d.util.BoundingBox;

public class MeshDesc {
    private BoundingBox mBounds;
    private PrimitiveDesc[] mPrimitives;

    public static class PrimitiveDesc {
        private ResourceHandle mMaterial;

        public ResourceHandle getMaterial() {
            return this.mMaterial;
        }

        public void setMaterial(ResourceHandle resourceHandle) {
            this.mMaterial = resourceHandle;
        }
    }

    public PrimitiveDesc[] getPrimitives() {
        PrimitiveDesc[] primitiveDescArr = this.mPrimitives;
        if (primitiveDescArr == null) {
            return new PrimitiveDesc[0];
        }
        return (PrimitiveDesc[]) Arrays.copyOf(primitiveDescArr, primitiveDescArr.length);
    }

    public void setPrimitives(PrimitiveDesc[] primitiveDescArr) {
        this.mPrimitives = (PrimitiveDesc[]) Arrays.copyOf(primitiveDescArr, primitiveDescArr.length);
    }

    public BoundingBox getBounds() {
        return this.mBounds;
    }

    public void setBounds(BoundingBox boundingBox) {
        this.mBounds = boundingBox;
    }
}
