package android.renderscript;

import android.provider.BrowserContract.Bookmarks;
import android.renderscript.Element.DataType;
import java.util.Vector;

public class Mesh extends BaseObj {
    Allocation[] mIndexBuffers;
    Primitive[] mPrimitives;
    Allocation[] mVertexBuffers;

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

        public AllocationBuilder(RenderScript rs) {
            this.mRS = rs;
        }

        public int getCurrentVertexTypeIndex() {
            return this.mVertexTypeCount - 1;
        }

        public int getCurrentIndexSetIndex() {
            return this.mIndexTypes.size() - 1;
        }

        public AllocationBuilder addVertexAllocation(Allocation a) throws IllegalStateException {
            if (this.mVertexTypeCount >= this.mVertexTypes.length) {
                throw new IllegalStateException("Max vertex types exceeded.");
            }
            this.mVertexTypes[this.mVertexTypeCount] = new Entry();
            this.mVertexTypes[this.mVertexTypeCount].a = a;
            this.mVertexTypeCount++;
            return this;
        }

        public AllocationBuilder addIndexSetAllocation(Allocation a, Primitive p) {
            Entry indexType = new Entry();
            indexType.a = a;
            indexType.prim = p;
            this.mIndexTypes.addElement(indexType);
            return this;
        }

        public AllocationBuilder addIndexSetType(Primitive p) {
            Entry indexType = new Entry();
            indexType.a = null;
            indexType.prim = p;
            this.mIndexTypes.addElement(indexType);
            return this;
        }

        public Mesh create() {
            int ct;
            Entry entry;
            this.mRS.validate();
            long[] vtx = new long[this.mVertexTypeCount];
            long[] idx = new long[this.mIndexTypes.size()];
            int[] prim = new int[this.mIndexTypes.size()];
            Allocation[] indexBuffers = new Allocation[this.mIndexTypes.size()];
            Primitive[] primitives = new Primitive[this.mIndexTypes.size()];
            Allocation[] vertexBuffers = new Allocation[this.mVertexTypeCount];
            for (ct = 0; ct < this.mVertexTypeCount; ct++) {
                entry = this.mVertexTypes[ct];
                vertexBuffers[ct] = entry.a;
                vtx[ct] = entry.a.getID(this.mRS);
            }
            for (ct = 0; ct < this.mIndexTypes.size(); ct++) {
                entry = (Entry) this.mIndexTypes.elementAt(ct);
                long allocID = entry.a == null ? 0 : entry.a.getID(this.mRS);
                indexBuffers[ct] = entry.a;
                primitives[ct] = entry.prim;
                idx[ct] = allocID;
                prim[ct] = entry.prim.mID;
            }
            Mesh newMesh = new Mesh(this.mRS.nMeshCreate(vtx, idx, prim), this.mRS);
            newMesh.mVertexBuffers = vertexBuffers;
            newMesh.mIndexBuffers = indexBuffers;
            newMesh.mPrimitives = primitives;
            return newMesh;
        }
    }

    public static class Builder {
        Vector mIndexTypes = new Vector();
        RenderScript mRS;
        int mUsage;
        int mVertexTypeCount = 0;
        Entry[] mVertexTypes = new Entry[16];

        class Entry {
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
            if (this.mVertexTypeCount >= this.mVertexTypes.length) {
                throw new IllegalStateException("Max vertex types exceeded.");
            }
            this.mVertexTypes[this.mVertexTypeCount] = new Entry();
            this.mVertexTypes[this.mVertexTypeCount].t = t;
            this.mVertexTypes[this.mVertexTypeCount].e = null;
            this.mVertexTypeCount++;
            return this;
        }

        public Builder addVertexType(Element e, int size) throws IllegalStateException {
            if (this.mVertexTypeCount >= this.mVertexTypes.length) {
                throw new IllegalStateException("Max vertex types exceeded.");
            }
            this.mVertexTypes[this.mVertexTypeCount] = new Entry();
            this.mVertexTypes[this.mVertexTypeCount].t = null;
            this.mVertexTypes[this.mVertexTypeCount].e = e;
            this.mVertexTypes[this.mVertexTypeCount].size = size;
            this.mVertexTypeCount++;
            return this;
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

        Type newType(Element e, int size) {
            android.renderscript.Type.Builder tb = new android.renderscript.Type.Builder(this.mRS, e);
            tb.setX(size);
            return tb.create();
        }

        public Mesh create() {
            int ct;
            Entry entry;
            Allocation alloc;
            this.mRS.validate();
            long[] vtx = new long[this.mVertexTypeCount];
            long[] idx = new long[this.mIndexTypes.size()];
            int[] prim = new int[this.mIndexTypes.size()];
            Allocation[] vertexBuffers = new Allocation[this.mVertexTypeCount];
            Allocation[] indexBuffers = new Allocation[this.mIndexTypes.size()];
            Primitive[] primitives = new Primitive[this.mIndexTypes.size()];
            for (ct = 0; ct < this.mVertexTypeCount; ct++) {
                entry = this.mVertexTypes[ct];
                if (entry.t != null) {
                    alloc = Allocation.createTyped(this.mRS, entry.t, this.mUsage);
                } else if (entry.e != null) {
                    alloc = Allocation.createSized(this.mRS, entry.e, entry.size, this.mUsage);
                } else {
                    throw new IllegalStateException("Builder corrupt, no valid element in entry.");
                }
                vertexBuffers[ct] = alloc;
                vtx[ct] = alloc.getID(this.mRS);
            }
            for (ct = 0; ct < this.mIndexTypes.size(); ct++) {
                entry = (Entry) this.mIndexTypes.elementAt(ct);
                if (entry.t != null) {
                    alloc = Allocation.createTyped(this.mRS, entry.t, this.mUsage);
                } else if (entry.e != null) {
                    alloc = Allocation.createSized(this.mRS, entry.e, entry.size, this.mUsage);
                } else {
                    throw new IllegalStateException("Builder corrupt, no valid element in entry.");
                }
                long allocID = alloc == null ? 0 : alloc.getID(this.mRS);
                indexBuffers[ct] = alloc;
                primitives[ct] = entry.prim;
                idx[ct] = allocID;
                prim[ct] = entry.prim.mID;
            }
            Mesh newMesh = new Mesh(this.mRS.nMeshCreate(vtx, idx, prim), this.mRS);
            newMesh.mVertexBuffers = vertexBuffers;
            newMesh.mIndexBuffers = indexBuffers;
            newMesh.mPrimitives = primitives;
            return newMesh;
        }
    }

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
            if (this.mVtxCount + count >= this.mVtxData.length) {
                float[] t = new float[(this.mVtxData.length * 2)];
                System.arraycopy(this.mVtxData, 0, t, 0, this.mVtxData.length);
                this.mVtxData = t;
            }
        }

        private void latch() {
            float[] fArr;
            int i;
            if ((this.mFlags & 1) != 0) {
                makeSpace(4);
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mR;
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mG;
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mB;
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mA;
            }
            if ((this.mFlags & 256) != 0) {
                makeSpace(2);
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mS0;
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mT0;
            }
            if ((this.mFlags & 2) != 0) {
                makeSpace(4);
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mNX;
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mNY;
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = this.mNZ;
                fArr = this.mVtxData;
                i = this.mVtxCount;
                this.mVtxCount = i + 1;
                fArr[i] = 0.0f;
            }
            this.mMaxIndex++;
        }

        public TriangleMeshBuilder addVertex(float x, float y) {
            if (this.mVtxSize != 2) {
                throw new IllegalStateException("add mistmatch with declared components.");
            }
            makeSpace(2);
            float[] fArr = this.mVtxData;
            int i = this.mVtxCount;
            this.mVtxCount = i + 1;
            fArr[i] = x;
            fArr = this.mVtxData;
            i = this.mVtxCount;
            this.mVtxCount = i + 1;
            fArr[i] = y;
            latch();
            return this;
        }

        public TriangleMeshBuilder addVertex(float x, float y, float z) {
            if (this.mVtxSize != 3) {
                throw new IllegalStateException("add mistmatch with declared components.");
            }
            makeSpace(4);
            float[] fArr = this.mVtxData;
            int i = this.mVtxCount;
            this.mVtxCount = i + 1;
            fArr[i] = x;
            fArr = this.mVtxData;
            i = this.mVtxCount;
            this.mVtxCount = i + 1;
            fArr[i] = y;
            fArr = this.mVtxData;
            i = this.mVtxCount;
            this.mVtxCount = i + 1;
            fArr[i] = z;
            fArr = this.mVtxData;
            i = this.mVtxCount;
            this.mVtxCount = i + 1;
            fArr[i] = 1.0f;
            latch();
            return this;
        }

        public TriangleMeshBuilder setTexture(float s, float t) {
            if ((this.mFlags & 256) == 0) {
                throw new IllegalStateException("add mistmatch with declared components.");
            }
            this.mS0 = s;
            this.mT0 = t;
            return this;
        }

        public TriangleMeshBuilder setNormal(float x, float y, float z) {
            if ((this.mFlags & 2) == 0) {
                throw new IllegalStateException("add mistmatch with declared components.");
            }
            this.mNX = x;
            this.mNY = y;
            this.mNZ = z;
            return this;
        }

        public TriangleMeshBuilder setColor(float r, float g, float b, float a) {
            if ((this.mFlags & 1) == 0) {
                throw new IllegalStateException("add mistmatch with declared components.");
            }
            this.mR = r;
            this.mG = g;
            this.mB = b;
            this.mA = a;
            return this;
        }

        public TriangleMeshBuilder addTriangle(int idx1, int idx2, int idx3) {
            if (idx1 >= this.mMaxIndex || idx1 < 0 || idx2 >= this.mMaxIndex || idx2 < 0 || idx3 >= this.mMaxIndex || idx3 < 0) {
                throw new IllegalStateException("Index provided greater than vertex count.");
            }
            if (this.mIndexCount + 3 >= this.mIndexData.length) {
                short[] t = new short[(this.mIndexData.length * 2)];
                System.arraycopy(this.mIndexData, 0, t, 0, this.mIndexData.length);
                this.mIndexData = t;
            }
            short[] sArr = this.mIndexData;
            int i = this.mIndexCount;
            this.mIndexCount = i + 1;
            sArr[i] = (short) idx1;
            sArr = this.mIndexData;
            i = this.mIndexCount;
            this.mIndexCount = i + 1;
            sArr[i] = (short) idx2;
            sArr = this.mIndexData;
            i = this.mIndexCount;
            this.mIndexCount = i + 1;
            sArr[i] = (short) idx3;
            return this;
        }

        public Mesh create(boolean uploadToBufferObject) {
            android.renderscript.Element.Builder b = new android.renderscript.Element.Builder(this.mRS);
            b.add(Element.createVector(this.mRS, DataType.FLOAT_32, this.mVtxSize), Bookmarks.POSITION);
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
                usage = 5;
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

    Mesh(long id, RenderScript rs) {
        super(id, rs);
        this.guard.open("destroy");
    }

    public int getVertexAllocationCount() {
        if (this.mVertexBuffers == null) {
            return 0;
        }
        return this.mVertexBuffers.length;
    }

    public Allocation getVertexAllocation(int slot) {
        return this.mVertexBuffers[slot];
    }

    public int getPrimitiveCount() {
        if (this.mIndexBuffers == null) {
            return 0;
        }
        return this.mIndexBuffers.length;
    }

    public Allocation getIndexSetAllocation(int slot) {
        return this.mIndexBuffers[slot];
    }

    public Primitive getPrimitive(int slot) {
        return this.mPrimitives[slot];
    }

    void updateFromNative() {
        int i;
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
        for (i = 0; i < vtxCount; i++) {
            if (vtxIDs[i] != 0) {
                this.mVertexBuffers[i] = new Allocation(vtxIDs[i], this.mRS, null, 1);
                this.mVertexBuffers[i].updateFromNative();
            }
        }
        for (i = 0; i < idxCount; i++) {
            if (idxIDs[i] != 0) {
                this.mIndexBuffers[i] = new Allocation(idxIDs[i], this.mRS, null, 1);
                this.mIndexBuffers[i].updateFromNative();
            }
            this.mPrimitives[i] = Primitive.values()[primitives[i]];
        }
    }
}
