package ohos.agp.render.render3d.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import ohos.agp.render.render3d.resources.ResourceHandle;

public interface MeshBuilder {
    void addPrimitive(Primitive primitive);

    void allocate();

    int getIndexCount();

    int getVertexCount();

    void release();

    void setAabb(int i, BoundingBox boundingBox);

    void setIndexData(int i, ByteBuffer byteBuffer);

    void setVertexData(int i, VertexData vertexData);

    public static class Primitive {
        private int mIndexCount;
        private boolean mIsColorDataDefined = false;
        private boolean mIsJointDataDefined = false;
        private boolean mIsTangentDataDefined = false;
        private ResourceHandle mMaterial;
        private int mVertexCount;

        public int getVertexCount() {
            return this.mVertexCount;
        }

        public void setVertexCount(int i) {
            this.mVertexCount = i;
        }

        public int getIndexCount() {
            return this.mIndexCount;
        }

        public void setIndexCount(int i) {
            this.mIndexCount = i;
        }

        public ResourceHandle getMaterial() {
            return this.mMaterial;
        }

        public void setMaterial(ResourceHandle resourceHandle) {
            this.mMaterial = resourceHandle;
        }

        public boolean isTangentDataDefined() {
            return this.mIsTangentDataDefined;
        }

        public void setTangentDataDefined(boolean z) {
            this.mIsTangentDataDefined = z;
        }

        public boolean isColorDataDefined() {
            return this.mIsColorDataDefined;
        }

        public void setColorDataDefined(boolean z) {
            this.mIsColorDataDefined = z;
        }

        public boolean isJointDataDefined() {
            return this.mIsJointDataDefined;
        }

        public void setJointDataDefined(boolean z) {
            this.mIsJointDataDefined = z;
        }
    }

    public static class VertexData {
        private FloatBuffer mColors;
        private FloatBuffer mNormals;
        private FloatBuffer mPositions;
        private FloatBuffer mTangents;
        private FloatBuffer mTexCoords;

        public FloatBuffer getPositions() {
            return this.mPositions;
        }

        public void setPositions(FloatBuffer floatBuffer) {
            this.mPositions = floatBuffer;
        }

        public FloatBuffer getNormals() {
            return this.mNormals;
        }

        public void setNormals(FloatBuffer floatBuffer) {
            this.mNormals = floatBuffer;
        }

        public FloatBuffer getTexCoords() {
            return this.mTexCoords;
        }

        public void setTexCoords(FloatBuffer floatBuffer) {
            this.mTexCoords = floatBuffer;
        }

        public FloatBuffer getTangents() {
            return this.mTangents;
        }

        public void setTangents(FloatBuffer floatBuffer) {
            this.mTangents = floatBuffer;
        }

        public FloatBuffer getColors() {
            return this.mColors;
        }

        public void setColors(FloatBuffer floatBuffer) {
            this.mColors = floatBuffer;
        }
    }
}
