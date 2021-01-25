package com.huawei.agpengine.util;

import com.huawei.agpengine.resources.ResourceHandle;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
        private int mInstanceCount = 1;
        private boolean mIsColorDataDefined = false;
        private boolean mIsJointDataDefined = false;
        private boolean mIsTangentDataDefined = false;
        private ResourceHandle mMaterial;
        private int mVertexCount;

        public int getVertexCount() {
            return this.mVertexCount;
        }

        public void setVertexCount(int vertexCount) {
            this.mVertexCount = vertexCount;
        }

        public int getIndexCount() {
            return this.mIndexCount;
        }

        public void setIndexCount(int indexCount) {
            this.mIndexCount = indexCount;
        }

        public int getInstanceCount() {
            return this.mInstanceCount;
        }

        public void setInstanceCount(int instanceCount) {
            this.mInstanceCount = instanceCount;
        }

        public ResourceHandle getMaterial() {
            return this.mMaterial;
        }

        public void setMaterial(ResourceHandle material) {
            this.mMaterial = material;
        }

        public boolean isTangentDataDefined() {
            return this.mIsTangentDataDefined;
        }

        public void setTangentDataDefined(boolean isTangentDataDefined) {
            this.mIsTangentDataDefined = isTangentDataDefined;
        }

        public boolean isColorDataDefined() {
            return this.mIsColorDataDefined;
        }

        public void setColorDataDefined(boolean isColorDataDefined) {
            this.mIsColorDataDefined = isColorDataDefined;
        }

        public boolean isJointDataDefined() {
            return this.mIsJointDataDefined;
        }

        public void setJointDataDefined(boolean isJointDataDefined) {
            this.mIsJointDataDefined = isJointDataDefined;
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

        public void setPositions(FloatBuffer positions) {
            this.mPositions = positions;
        }

        public FloatBuffer getNormals() {
            return this.mNormals;
        }

        public void setNormals(FloatBuffer normals) {
            this.mNormals = normals;
        }

        public FloatBuffer getTexCoords() {
            return this.mTexCoords;
        }

        public void setTexCoords(FloatBuffer texCoords) {
            this.mTexCoords = texCoords;
        }

        public FloatBuffer getTangents() {
            return this.mTangents;
        }

        public void setTangents(FloatBuffer tangents) {
            this.mTangents = tangents;
        }

        public FloatBuffer getColors() {
            return this.mColors;
        }

        public void setColors(FloatBuffer colors) {
            this.mColors = colors;
        }
    }
}
