package ohos.agp.render;

public class Vertices {
    private final int mColorOffset;
    private final int[] mColors;
    private final int mIndexCount;
    private final int mIndexOffset;
    private final short[] mIndices;
    private final int mTexOffset;
    private final float[] mTexs;
    private final int mVertOffset;
    private final int mVertexCount;
    private final float[] mVerts;

    public Vertices(int i, float[] fArr, int i2, float[] fArr2, int i3, int[] iArr, int i4, short[] sArr, int i5, int i6) {
        if (fArr != null) {
            this.mVerts = new float[fArr.length];
            System.arraycopy(fArr, 0, this.mVerts, 0, fArr.length);
        } else {
            this.mVerts = null;
        }
        this.mVertexCount = i;
        this.mVertOffset = i2;
        if (fArr2 != null) {
            this.mTexs = new float[fArr2.length];
            System.arraycopy(fArr2, 0, this.mTexs, 0, fArr2.length);
        } else {
            this.mTexs = null;
        }
        this.mTexOffset = i3;
        if (iArr != null) {
            this.mColors = new int[iArr.length];
            System.arraycopy(iArr, 0, this.mColors, 0, iArr.length);
        } else {
            this.mColors = null;
        }
        this.mColorOffset = i4;
        if (sArr != null) {
            this.mIndices = new short[sArr.length];
            System.arraycopy(sArr, 0, this.mIndices, 0, sArr.length);
        } else {
            this.mIndices = null;
        }
        this.mIndexOffset = i5;
        this.mIndexCount = i6;
    }

    public int getVertexCount() {
        return this.mVertexCount;
    }

    public float[] getVerts() {
        float[] fArr = this.mVerts;
        if (fArr == null) {
            return new float[0];
        }
        float[] fArr2 = new float[fArr.length];
        System.arraycopy(fArr, 0, fArr2, 0, fArr.length);
        return fArr2;
    }

    public int getVertOffset() {
        return this.mVertOffset;
    }

    public float[] getTexs() {
        float[] fArr = this.mTexs;
        if (fArr == null) {
            return new float[0];
        }
        float[] fArr2 = new float[fArr.length];
        System.arraycopy(fArr, 0, fArr2, 0, fArr.length);
        return fArr2;
    }

    public int getTexOffset() {
        return this.mTexOffset;
    }

    public int[] getColors() {
        int[] iArr = this.mColors;
        if (iArr == null) {
            return new int[0];
        }
        int[] iArr2 = new int[iArr.length];
        System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
        return iArr2;
    }

    public int getColorOffset() {
        return this.mColorOffset;
    }

    public short[] getIndices() {
        short[] sArr = this.mIndices;
        if (sArr == null) {
            return new short[0];
        }
        short[] sArr2 = new short[sArr.length];
        System.arraycopy(sArr, 0, sArr2, 0, sArr.length);
        return sArr2;
    }

    public int getIndexOffset() {
        return this.mIndexOffset;
    }

    public int getIndexCount() {
        return this.mIndexCount;
    }
}
