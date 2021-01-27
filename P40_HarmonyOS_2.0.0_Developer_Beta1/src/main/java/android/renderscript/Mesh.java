package android.renderscript;

import android.annotation.UnsupportedAppUsage;
import android.provider.BrowserContract;
import android.renderscript.Element;
import android.renderscript.Type;
import java.util.Vector;

public class Mesh extends BaseObj {
    Allocation[] mIndexBuffers;
    Primitive[] mPrimitives;
    Allocation[] mVertexBuffers;

    public enum Primitive {
        POINT(0),
        LINE(1),
        LINE_STRIP(2),
        TRIANGLE(3),
        TRIANGLE_STRIP(4),
        TRIANGLE_FAN(5);
        
        int mID;

        private Primitive(int id) {
            this.mID = id;
        }
    }

    Mesh(long id, RenderScript rs) {
        super(id, rs);
        this.guard.open("destroy");
    }

    public int getVertexAllocationCount() {
        Allocation[] allocationArr = this.mVertexBuffers;
        if (allocationArr == null) {
            return 0;
        }
        return allocationArr.length;
    }

    @UnsupportedAppUsage
    public Allocation getVertexAllocation(int slot) {
        return this.mVertexBuffers[slot];
    }

    public int getPrimitiveCount() {
        Allocation[] allocationArr = this.mIndexBuffers;
        if (allocationArr == null) {
            return 0;
        }
        return allocationArr.length;
    }

    public Allocation getIndexSetAllocation(int slot) {
        return this.mIndexBuffers[slot];
    }

    public Primitive getPrimitive(int slot) {
        return this.mPrimitives[slot];
    }

    /* access modifiers changed from: package-private */
    @Override // android.renderscript.BaseObj
    public void updateFromNative() {
        super.updateFromNative();
        int vtxCount = this.mRS.nMeshGetVertexBufferCount(getID(this.mRS));
        int idxCount = this.mRS.nMeshGetIndexCount(getID(this.mRS));
        long[] vtxIDs = new long[vtxCount];
        long[] idxIDs = new long[idxCount];
        int[] primitives = new int[idxCount];
        this.mRS.nMeshGetVertices(getID(this.mRS), vtxIDs, vtxCount);
        this.mRS.nMeshGetIndices(getID(this.mRS), idxIDs, primitives, idxCount);
        this.mVertexBuffers = new Allocation[vtxCount];
        this.mIndexBuffers = new Allocation[idxCount];
        this.mPrimitives = new Primitive[idxCount];
        for (int i = 0; i < vtxCount; i++) {
            if (vtxIDs[i] != 0) {
                this.mVertexBuffers[i] = new Allocation(vtxIDs[i], this.mRS, null, 1);
                this.mVertexBuffers[i].updateFromNative();
            }
        }
        for (int i2 = 0; i2 < idxCount; i2++) {
            if (idxIDs[i2] != 0) {
                this.mIndexBuffers[i2] = new Allocation(idxIDs[i2], this.mRS, null, 1);
                this.mIndexBuffers[i2].updateFromNative();
            }
            this.mPrimitives[i2] = Primitive.values()[primitives[i2]];
        }
    }

    public static class Builder {
        Vector mIndexTypes = new Vector();
        RenderScript mRS;
        int mUsage;
        int mVertexTypeCount = 0;
        Entry[] mVertexTypes = new Entry[16];

        /* access modifiers changed from: package-private */
        public class Entry {
            Element e;
            Primitive prim;
            int size;
            Type t;
            int usage;

            Entry() {
            }
        }

        public Builder(RenderScript rs, int usage) {
            this.mRS = rs;
            this.mUsage = usage;
        }

        public int getCurrentVertexTypeIndex() {
            return this.mVertexTypeCount - 1;
        }

        public int getCurrentIndexSetIndex() {
            return this.mIndexTypes.size() - 1;
        }

        public Builder addVertexType(Type t) throws IllegalStateException {
            int i = this.mVertexTypeCount;
            Entry[] entryArr = this.mVertexTypes;
            if (i < entryArr.length) {
                entryArr[i] = new Entry();
                Entry[] entryArr2 = this.mVertexTypes;
                int i2 = this.mVertexTypeCount;
                entryArr2[i2].t = t;
                entryArr2[i2].e = null;
                this.mVertexTypeCount = i2 + 1;
                return this;
            }
            throw new IllegalStateException("Max vertex types exceeded.");
        }

        public Builder addVertexType(Element e, int size) throws IllegalStateException {
            int i = this.mVertexTypeCount;
            Entry[] entryArr = this.mVertexTypes;
            if (i < entryArr.length) {
                entryArr[i] = new Entry();
                Entry[] entryArr2 = this.mVertexTypes;
                int i2 = this.mVertexTypeCount;
                entryArr2[i2].t = null;
                entryArr2[i2].e = e;
                entryArr2[i2].size = size;
                this.mVertexTypeCount = i2 + 1;
                return this;
            }
            throw new IllegalStateException("Max vertex types exceeded.");
        }

        public Builder addIndexSetType(Type t, Primitive p) {
            Entry indexType = new Entry();
            indexType.t = t;
            indexType.e = null;
            indexType.size = 0;
            indexType.prim = p;
            this.mIndexTypes.addElement(indexType);
            return this;
        }

        public Builder addIndexSetType(Primitive p) {
            Entry indexType = new Entry();
            indexType.t = null;
            indexType.e = null;
            indexType.size = 0;
            indexType.prim = p;
            this.mIndexTypes.addElement(indexType);
            return this;
        }

        public Builder addIndexSetType(Element e, int size, Primitive p) {
            Entry indexType = new Entry();
            indexType.t = null;
            indexType.e = e;
            indexType.size = size;
            indexType.prim = p;
            this.mIndexTypes.addElement(indexType);
            return this;
        }

        /* access modifiers changed from: package-private */
        public Type newType(Element e, int size) {
            Type.Builder tb = new Type.Builder(this.mRS, e);
            tb.setX(size);
            return tb.create();
        }

        public Mesh create() {
            Allocation alloc;
            Allocation alloc2;
            this.mRS.validate();
            long[] vtx = new long[this.mVertexTypeCount];
            long[] idx = new long[this.mIndexTypes.size()];
            int[] prim = new int[this.mIndexTypes.size()];
            Allocation[] vertexBuffers = new Allocation[this.mVertexTypeCount];
            Allocation[] indexBuffers = new Allocation[this.mIndexTypes.size()];
            Primitive[] primitives = new Primitive[this.mIndexTypes.size()];
            for (int ct = 0; ct < this.mVertexTypeCount; ct++) {
                Entry entry = this.mVertexTypes[ct];
                if (entry.t != null) {
                    alloc2 = Allocation.createTyped(this.mRS, entry.t, this.mUsage);
                } else if (entry.e != null) {
                    alloc2 = Allocation.createSized(this.mRS, entry.e, entry.size, this.mUsage);
                } else {
                    throw new IllegalStateException("Builder corrupt, no valid element in entry.");
                }
                vertexBuffers[ct] = alloc2;
                vtx[ct] = alloc2.getID(this.mRS);
            }
            for (int ct2 = 0; ct2 < this.mIndexTypes.size(); ct2++) {
                Entry entry2 = (Entry) this.mIndexTypes.elementAt(ct2);
                if (entry2.t != null) {
                    alloc = Allocation.createTyped(this.mRS, entry2.t, this.mUsage);
                } else if (entry2.e != null) {
                    alloc = Allocation.createSized(this.mRS, entry2.e, entry2.size, this.mUsage);
                } else {
                    throw new IllegalStateException("Builder corrupt, no valid element in entry.");
                }
                long allocID = alloc == null ? 0 : alloc.getID(this.mRS);
                indexBuffers[ct2] = alloc;
                primitives[ct2] = entry2.prim;
                idx[ct2] = allocID;
                prim[ct2] = entry2.prim.mID;
            }
            Mesh newMesh = new Mesh(this.mRS.nMeshCreate(vtx, idx, prim), this.mRS);
            newMesh.mVertexBuffers = vertexBuffers;
            newMesh.mIndexBuffers = indexBuffers;
            newMesh.mPrimitives = primitives;
            return newMesh;
        }
    }

    public static class AllocationBuilder {
        Vector mIndexTypes = new Vector();
        RenderScript mRS;
        int mVertexTypeCount = 0;
        Entry[] mVertexTypes = new Entry[16];

        class Entry {
            Allocation a;
            Primitive prim;

            Entry() {
            }
        }

        @UnsupportedAppUsage
        public AllocationBuilder(RenderScript rs) {
            this.mRS = rs;
        }

        public int getCurrentVertexTypeIndex() {
            return this.mVertexTypeCount - 1;
        }

        public int getCurrentIndexSetIndex() {
            return this.mIndexTypes.size() - 1;
        }

        @UnsupportedAppUsage
        public AllocationBuilder addVertexAllocation(Allocation a) throws IllegalStateException {
            int i = this.mVertexTypeCount;
            Entry[] entryArr = this.mVertexTypes;
            if (i < entryArr.length) {
                entryArr[i] = new Entry();
                Entry[] entryArr2 = this.mVertexTypes;
                int i2 = this.mVertexTypeCount;
                entryArr2[i2].a = a;
                this.mVertexTypeCount = i2 + 1;
                return this;
            }
            throw new IllegalStateException("Max vertex types exceeded.");
        }

        @UnsupportedAppUsage
        public AllocationBuilder addIndexSetAllocation(Allocation a, Primitive p) {
            Entry indexType = new Entry();
            indexType.a = a;
            indexType.prim = p;
            this.mIndexTypes.addElement(indexType);
            return this;
        }

        @UnsupportedAppUsage
        public AllocationBuilder addIndexSetType(Primitive p) {
            Entry indexType = new Entry();
            indexType.a = null;
            indexType.prim = p;
            this.mIndexTypes.addElement(indexType);
            return this;
        }

        @UnsupportedAppUsage
        public Mesh create() {
            this.mRS.validate();
            long[] vtx = new long[this.mVertexTypeCount];
            long[] idx = new long[this.mIndexTypes.size()];
            int[] prim = new int[this.mIndexTypes.size()];
            Allocation[] indexBuffers = new Allocation[this.mIndexTypes.size()];
            Primitive[] primitives = new Primitive[this.mIndexTypes.size()];
            Allocation[] vertexBuffers = new Allocation[this.mVertexTypeCount];
            for (int ct = 0; ct < this.mVertexTypeCount; ct++) {
                Entry entry = this.mVertexTypes[ct];
                vertexBuffers[ct] = entry.a;
                vtx[ct] = entry.a.getID(this.mRS);
            }
            for (int ct2 = 0; ct2 < this.mIndexTypes.size(); ct2++) {
                Entry entry2 = (Entry) this.mIndexTypes.elementAt(ct2);
                long allocID = entry2.a == null ? 0 : entry2.a.getID(this.mRS);
                indexBuffers[ct2] = entry2.a;
                primitives[ct2] = entry2.prim;
                idx[ct2] = allocID;
                prim[ct2] = entry2.prim.mID;
            }
            Mesh newMesh = new Mesh(this.mRS.nMeshCreate(vtx, idx, prim), this.mRS);
            newMesh.mVertexBuffers = vertexBuffers;
            newMesh.mIndexBuffers = indexBuffers;
            newMesh.mPrimitives = primitives;
            return newMesh;
        }
    }

    public static class TriangleMeshBuilder {
        public static final int COLOR = 1;
        public static final int NORMAL = 2;
        public static final int TEXTURE_0 = 256;
        float mA = 1.0f;
        float mB = 1.0f;
        Element mElement;
        int mFlags;
        float mG = 1.0f;
        int mIndexCount;
        short[] mIndexData;
        int mMaxIndex;
        float mNX = 0.0f;
        float mNY = 0.0f;
        float mNZ = -1.0f;
        float mR = 1.0f;
        RenderScript mRS;
        float mS0 = 0.0f;
        float mT0 = 0.0f;
        int mVtxCount;
        float[] mVtxData;
        int mVtxSize;

        @UnsupportedAppUsage
        public TriangleMeshBuilder(RenderScript rs, int vtxSize, int flags) {
            this.mRS = rs;
            this.mVtxCount = 0;
            this.mMaxIndex = 0;
            this.mIndexCount = 0;
            this.mVtxData = new float[128];
            this.mIndexData = new short[128];
            this.mVtxSize = vtxSize;
            this.mFlags = flags;
            if (vtxSize < 2 || vtxSize > 3) {
                throw new IllegalArgumentException("Vertex size out of range.");
            }
        }

        private void makeSpace(int count) {
            int i = this.mVtxCount + count;
            float[] fArr = this.mVtxData;
            if (i >= fArr.length) {
                float[] t = new float[(fArr.length * 2)];
                System.arraycopy(fArr, 0, t, 0, fArr.length);
                this.mVtxData = t;
            }
        }

        private void latch() {
            if ((this.mFlags & 1) != 0) {
                makeSpace(4);
                float[] fArr = this.mVtxData;
                int i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mR;
                int i2 = this.mVtxCount;
                this.mVtxCount = i2 + 1;
                fArr[i2] = this.mG;
                int i3 = this.mVtxCount;
                this.mVtxCount = i3 + 1;
                fArr[i3] = this.mB;
                int i4 = this.mVtxCount;
                this.mVtxCount = i4 + 1;
                fArr[i4] = this.mA;
            }
            if ((this.mFlags & 256) != 0) {
                makeSpace(2);
                float[] fArr2 = this.mVtxData;
                int i5 = this.mVtxCount;
                this.mVtxCount = i5 + 1;
                fArr2[i5] = this.mS0;
                int i6 = this.mVtxCount;
                this.mVtxCount = i6 + 1;
                fArr2[i6] = this.mT0;
            }
            if ((this.mFlags & 2) != 0) {
                makeSpace(4);
                float[] fArr3 = this.mVtxData;
                int i7 = this.mVtxCount;
                this.mVtxCount = i7 + 1;
                fArr3[i7] = this.mNX;
                int i8 = this.mVtxCount;
                this.mVtxCount = i8 + 1;
                fArr3[i8] = this.mNY;
                int i9 = this.mVtxCount;
                this.mVtxCount = i9 + 1;
                fArr3[i9] = this.mNZ;
                int i10 = this.mVtxCount;
                this.mVtxCount = i10 + 1;
                fArr3[i10] = 0.0f;
            }
            this.mMaxIndex++;
        }

        @UnsupportedAppUsage
        public TriangleMeshBuilder addVertex(float x, float y) {
            if (this.mVtxSize == 2) {
                makeSpace(2);
                float[] fArr = this.mVtxData;
                int i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = x;
                int i2 = this.mVtxCount;
                this.mVtxCount = i2 + 1;
                fArr[i2] = y;
                latch();
                return this;
            }
            throw new IllegalStateException("add mistmatch with declared components.");
        }

        public TriangleMeshBuilder addVertex(float x, float y, float z) {
            if (this.mVtxSize == 3) {
                makeSpace(4);
                float[] fArr = this.mVtxData;
                int i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = x;
                int i2 = this.mVtxCount;
                this.mVtxCount = i2 + 1;
                fArr[i2] = y;
                int i3 = this.mVtxCount;
                this.mVtxCount = i3 + 1;
                fArr[i3] = z;
                int i4 = this.mVtxCount;
                this.mVtxCount = i4 + 1;
                fArr[i4] = 1.0f;
                latch();
                return this;
            }
            throw new IllegalStateException("add mistmatch with declared components.");
        }

        public TriangleMeshBuilder setTexture(float s, float t) {
            if ((this.mFlags & 256) != 0) {
                this.mS0 = s;
                this.mT0 = t;
                return this;
            }
            throw new IllegalStateException("add mistmatch with declared components.");
        }

        public TriangleMeshBuilder setNormal(float x, float y, float z) {
            if ((this.mFlags & 2) != 0) {
                this.mNX = x;
                this.mNY = y;
                this.mNZ = z;
                return this;
            }
            throw new IllegalStateException("add mistmatch with declared components.");
        }

        public TriangleMeshBuilder setColor(float r, float g, float b, float a) {
            if ((this.mFlags & 1) != 0) {
                this.mR = r;
                this.mG = g;
                this.mB = b;
                this.mA = a;
                return this;
            }
            throw new IllegalStateException("add mistmatch with declared components.");
        }

        @UnsupportedAppUsage
        public TriangleMeshBuilder addTriangle(int idx1, int idx2, int idx3) {
            int i = this.mMaxIndex;
            if (idx1 >= i || idx1 < 0 || idx2 >= i || idx2 < 0 || idx3 >= i || idx3 < 0) {
                throw new IllegalStateException("Index provided greater than vertex count.");
            }
            int i2 = this.mIndexCount + 3;
            short[] sArr = this.mIndexData;
            if (i2 >= sArr.length) {
                short[] t = new short[(sArr.length * 2)];
                System.arraycopy(sArr, 0, t, 0, sArr.length);
                this.mIndexData = t;
            }
            short[] t2 = this.mIndexData;
            int i3 = this.mIndexCount;
            this.mIndexCount = i3 + 1;
            t2[i3] = (short) idx1;
            int i4 = this.mIndexCount;
            this.mIndexCount = i4 + 1;
            t2[i4] = (short) idx2;
            int i5 = this.mIndexCount;
            this.mIndexCount = i5 + 1;
            t2[i5] = (short) idx3;
            return this;
        }

        @UnsupportedAppUsage
        public Mesh create(boolean uploadToBufferObject) {
            Element.Builder b = new Element.Builder(this.mRS);
            b.add(Element.createVector(this.mRS, Element.DataType.FLOAT_32, this.mVtxSize), BrowserContract.Bookmarks.POSITION);
            if ((this.mFlags & 1) != 0) {
                b.add(Element.F32_4(this.mRS), "color");
            }
            if ((this.mFlags & 256) != 0) {
                b.add(Element.F32_2(this.mRS), "texture0");
            }
            if ((this.mFlags & 2) != 0) {
                b.add(Element.F32_3(this.mRS), "normal");
            }
            this.mElement = b.create();
            int usage = 1;
            if (uploadToBufferObject) {
                usage = 1 | 4;
            }
            Builder smb = new Builder(this.mRS, usage);
            smb.addVertexType(this.mElement, this.mMaxIndex);
            smb.addIndexSetType(Element.U16(this.mRS), this.mIndexCount, Primitive.TRIANGLE);
            Mesh sm = smb.create();
            sm.getVertexAllocation(0).copy1DRangeFromUnchecked(0, this.mMaxIndex, this.mVtxData);
            if (uploadToBufferObject) {
                sm.getVertexAllocation(0).syncAll(1);
            }
            sm.getIndexSetAllocation(0).copy1DRangeFromUnchecked(0, this.mIndexCount, this.mIndexData);
            if (uploadToBufferObject) {
                sm.getIndexSetAllocation(0).syncAll(1);
            }
            return sm;
        }
    }
}
