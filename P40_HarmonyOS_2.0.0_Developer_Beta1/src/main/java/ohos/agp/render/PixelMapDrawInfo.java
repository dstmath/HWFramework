package ohos.agp.render;

public class PixelMapDrawInfo {
    private static final int DRAW_ARRAY_SIZE = 2;
    private int colorOffset = 0;
    private int[] colors = new int[0];
    private int[] divideInfo = {0, 0};
    private int vertOffset = 0;
    private float[] vertices = new float[0];

    public PixelMapDrawInfo(int[] iArr, float[] fArr, int i, int[] iArr2, int i2) {
        if (iArr != null && iArr.length == 2) {
            this.divideInfo = (int[]) iArr.clone();
            if (fArr.length >= ((iArr[0] + 1) * (iArr[1] + 1) * 2) + i) {
                this.vertices = (float[]) fArr.clone();
                this.vertOffset = i;
                if (iArr2 != null && iArr2.length < ((iArr[0] + 1) * (iArr[1] + 1)) + i2) {
                    this.colors = null;
                } else if (iArr2 != null) {
                    this.colors = (int[]) iArr2.clone();
                    this.colorOffset = i2;
                } else {
                    this.colors = null;
                }
            }
        }
    }

    public int getVertOffset() {
        return this.vertOffset;
    }

    public int getColorOffset() {
        return this.colorOffset;
    }

    public int getWidth() {
        return this.divideInfo[0];
    }

    public int getHeight() {
        return this.divideInfo[1];
    }

    public float[] getVertices() {
        float[] fArr = this.vertices;
        if (fArr == null) {
            return new float[0];
        }
        return (float[]) fArr.clone();
    }

    public int[] getColors() {
        int[] iArr = this.colors;
        return iArr != null ? (int[]) iArr.clone() : new int[0];
    }
}
