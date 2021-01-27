package com.huawei.agpengine.resources;

import com.huawei.agpengine.util.BoundingBox;

public class MeshDesc {
    private BoundingBox mBounds;
    private PrimitiveDesc[] mPrimitives;

    public static class PrimitiveDesc {
        private ResourceHandle mMaterial;

        public ResourceHandle getMaterial() {
            return this.mMaterial;
        }

        public void setMaterial(ResourceHandle material) {
            this.mMaterial = material;
        }
    }

    public PrimitiveDesc[] getPrimitives() {
        return this.mPrimitives;
    }

    public void setPrimitives(PrimitiveDesc[] primitives) {
        this.mPrimitives = primitives;
    }

    public BoundingBox getBounds() {
        return this.mBounds;
    }

    public void setBounds(BoundingBox bounds) {
        this.mBounds = bounds;
    }
}
