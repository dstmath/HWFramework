package ohos.agp.render.render3d.impl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import ohos.agp.render.render3d.BuildConfig;
import ohos.agp.render.render3d.impl.CoreMeshBuilder;
import ohos.agp.render.render3d.util.BoundingBox;
import ohos.agp.render.render3d.util.MeshBuilder;

class MeshBuilderImpl implements MeshBuilder {
    private CoreMeshBuilder mNativeMeshBuilder;

    MeshBuilderImpl(EngineImpl engineImpl, int i) {
        this.mNativeMeshBuilder = Core.createMeshBuilder(engineImpl.getAgpContext().getEngine().getShaderManager(), i);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshBuilder getNativeMeshBuilder() {
        return this.mNativeMeshBuilder;
    }

    @Override // ohos.agp.render.render3d.util.MeshBuilder
    public void allocate() {
        this.mNativeMeshBuilder.allocate();
    }

    @Override // ohos.agp.render.render3d.util.MeshBuilder
    public void release() {
        CoreMeshBuilder coreMeshBuilder = this.mNativeMeshBuilder;
        if (coreMeshBuilder != null) {
            coreMeshBuilder.delete();
            this.mNativeMeshBuilder = null;
        }
    }

    @Override // ohos.agp.render.render3d.util.MeshBuilder
    public void addPrimitive(MeshBuilder.Primitive primitive) {
        CoreMeshBuilder.CorePrimitive corePrimitive = new CoreMeshBuilder.CorePrimitive();
        corePrimitive.setVertexCount((long) primitive.getVertexCount());
        corePrimitive.setIndexCount((long) primitive.getIndexCount());
        corePrimitive.setIndexType(CoreIndexType.CORE_INDEX_TYPE_UINT32);
        corePrimitive.setMaterial(ResourceHandleImpl.getNativeHandle(primitive.getMaterial()));
        corePrimitive.setTangents(primitive.isTangentDataDefined());
        corePrimitive.setColors(primitive.isColorDataDefined());
        corePrimitive.setJoints(primitive.isJointDataDefined());
        this.mNativeMeshBuilder.addPrimitive(corePrimitive);
    }

    private CoreFloatArrayView createArrayView(FloatBuffer floatBuffer) {
        if (floatBuffer == null) {
            return new CoreFloatArrayView(ByteBuffer.allocateDirect(0));
        }
        return new CoreFloatArrayView(floatBuffer);
    }

    @Override // ohos.agp.render.render3d.util.MeshBuilder
    public void setVertexData(int i, MeshBuilder.VertexData vertexData) {
        if (vertexData != null) {
            FloatBuffer positions = vertexData.getPositions();
            FloatBuffer normals = vertexData.getNormals();
            FloatBuffer texCoords = vertexData.getTexCoords();
            FloatBuffer tangents = vertexData.getTangents();
            FloatBuffer colors = vertexData.getColors();
            if (BuildConfig.DEBUG) {
                if (positions == null) {
                    throw new NullPointerException();
                } else if (normals == null) {
                    throw new NullPointerException();
                } else if (texCoords == null) {
                    throw new NullPointerException();
                } else if (!positions.isDirect()) {
                    throw new IllegalArgumentException();
                } else if (!normals.isDirect()) {
                    throw new IllegalArgumentException();
                } else if (!texCoords.isDirect()) {
                    throw new IllegalArgumentException();
                } else if (tangents != null && !tangents.isDirect()) {
                    throw new IllegalArgumentException();
                } else if (colors != null && !colors.isDirect()) {
                    throw new IllegalArgumentException();
                }
            }
            this.mNativeMeshBuilder.setVertexData((long) i, createArrayView(positions), createArrayView(normals), createArrayView(texCoords), createArrayView(tangents), createArrayView(colors));
            return;
        }
        throw new NullPointerException();
    }

    @Override // ohos.agp.render.render3d.util.MeshBuilder
    public void setIndexData(int i, ByteBuffer byteBuffer) {
        this.mNativeMeshBuilder.setIndexData((long) i, new CoreByteArrayView(byteBuffer));
    }

    @Override // ohos.agp.render.render3d.util.MeshBuilder
    public void setAabb(int i, BoundingBox boundingBox) {
        this.mNativeMeshBuilder.setAabb((long) i, Swig.set(boundingBox.getAabbMin()), Swig.set(boundingBox.getAabbMax()));
    }

    @Override // ohos.agp.render.render3d.util.MeshBuilder
    public int getVertexCount() {
        return (int) this.mNativeMeshBuilder.getVertexCount();
    }

    @Override // ohos.agp.render.render3d.util.MeshBuilder
    public int getIndexCount() {
        return (int) this.mNativeMeshBuilder.getIndexCount();
    }
}
