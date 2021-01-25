package com.huawei.agpengine.impl;

import com.huawei.agpengine.impl.CoreMeshBuilder;
import com.huawei.agpengine.util.BoundingBox;
import com.huawei.agpengine.util.MeshBuilder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

class MeshBuilderImpl implements MeshBuilder {
    private CoreMeshBuilderPtr mNativeMeshBuilder;

    MeshBuilderImpl(EngineImpl engine, int primitiveCount) {
        this.mNativeMeshBuilder = Core.createMeshBuilder(engine.getAgpContext().getEngine().getShaderManager(), primitiveCount);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshBuilder getNativeMeshBuilder() {
        return this.mNativeMeshBuilder.get();
    }

    @Override // com.huawei.agpengine.util.MeshBuilder
    public void allocate() {
        this.mNativeMeshBuilder.get().allocate();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }

    @Override // com.huawei.agpengine.util.MeshBuilder
    public void release() {
        CoreMeshBuilderPtr coreMeshBuilderPtr = this.mNativeMeshBuilder;
        if (coreMeshBuilderPtr != null) {
            coreMeshBuilderPtr.delete();
            this.mNativeMeshBuilder = null;
        }
    }

    @Override // com.huawei.agpengine.util.MeshBuilder
    public void addPrimitive(MeshBuilder.Primitive primitive) {
        CoreMeshBuilder.CorePrimitive nativePrimitive = new CoreMeshBuilder.CorePrimitive();
        nativePrimitive.setVertexCount((long) primitive.getVertexCount());
        nativePrimitive.setIndexCount((long) primitive.getIndexCount());
        nativePrimitive.setInstanceCount((long) primitive.getInstanceCount());
        nativePrimitive.setIndexType(CoreIndexType.CORE_INDEX_TYPE_UINT32);
        nativePrimitive.setMaterial(ResourceHandleImpl.getNativeHandle(primitive.getMaterial()));
        nativePrimitive.setTangents(primitive.isTangentDataDefined());
        nativePrimitive.setColors(primitive.isColorDataDefined());
        nativePrimitive.setJoints(primitive.isJointDataDefined());
        this.mNativeMeshBuilder.get().addPrimitive(nativePrimitive);
    }

    private CoreFloatArrayView createArrayView(FloatBuffer buffer) {
        if (buffer == null) {
            return new CoreFloatArrayView(ByteBuffer.allocateDirect(0));
        }
        return new CoreFloatArrayView(buffer);
    }

    private void checkBuffer(Buffer buffer, boolean required) {
        if (buffer == null) {
            if (required) {
                throw new NullPointerException("buffer must not be null.");
            }
        } else if (!buffer.isDirect()) {
            throw new IllegalArgumentException("A direct buffer is required.");
        }
    }

    @Override // com.huawei.agpengine.util.MeshBuilder
    public void setVertexData(int primitiveIndex, MeshBuilder.VertexData vertexData) {
        if (vertexData != null) {
            FloatBuffer positions = vertexData.getPositions();
            checkBuffer(positions, true);
            FloatBuffer normals = vertexData.getNormals();
            checkBuffer(normals, true);
            FloatBuffer texCoords = vertexData.getTexCoords();
            checkBuffer(texCoords, true);
            FloatBuffer tangents = vertexData.getTangents();
            checkBuffer(tangents, false);
            FloatBuffer colors = vertexData.getColors();
            checkBuffer(colors, false);
            this.mNativeMeshBuilder.get().setVertexData((long) primitiveIndex, createArrayView(positions), createArrayView(normals), createArrayView(texCoords), createArrayView(tangents), createArrayView(colors));
            return;
        }
        throw new NullPointerException("vertexData must not be null.");
    }

    @Override // com.huawei.agpengine.util.MeshBuilder
    public void setIndexData(int primitiveIndex, ByteBuffer indices) {
        checkBuffer(indices, true);
        this.mNativeMeshBuilder.get().setIndexData((long) primitiveIndex, new CoreByteArrayView(indices));
    }

    @Override // com.huawei.agpengine.util.MeshBuilder
    public void setAabb(int primitiveIndex, BoundingBox bounds) {
        this.mNativeMeshBuilder.get().setAabb((long) primitiveIndex, Swig.set(bounds.getAabbMin()), Swig.set(bounds.getAabbMax()));
    }

    @Override // com.huawei.agpengine.util.MeshBuilder
    public int getVertexCount() {
        return (int) this.mNativeMeshBuilder.get().getVertexCount();
    }

    @Override // com.huawei.agpengine.util.MeshBuilder
    public int getIndexCount() {
        return (int) this.mNativeMeshBuilder.get().getIndexCount();
    }
}
