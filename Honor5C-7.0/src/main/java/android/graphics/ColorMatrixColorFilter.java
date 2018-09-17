package android.graphics;

public class ColorMatrixColorFilter extends ColorFilter {
    private final ColorMatrix mMatrix;

    private static native long nativeColorMatrixFilter(float[] fArr);

    public ColorMatrixColorFilter(ColorMatrix matrix) {
        this.mMatrix = new ColorMatrix();
        this.mMatrix.set(matrix);
        update();
    }

    public ColorMatrixColorFilter(float[] array) {
        this.mMatrix = new ColorMatrix();
        if (array.length < 20) {
            throw new ArrayIndexOutOfBoundsException();
        }
        this.mMatrix.set(array);
        update();
    }

    public ColorMatrix getColorMatrix() {
        return this.mMatrix;
    }

    public void setColorMatrix(ColorMatrix matrix) {
        if (matrix == null) {
            this.mMatrix.reset();
        } else if (matrix != this.mMatrix) {
            this.mMatrix.set(matrix);
        }
        update();
    }

    public void setColorMatrix(float[] array) {
        if (array == null) {
            this.mMatrix.reset();
        } else if (array.length < 20) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            this.mMatrix.set(array);
        }
        update();
    }

    private void update() {
        float[] colorMatrix = this.mMatrix.getArray();
        ColorFilter.destroyFilter(this.native_instance);
        this.native_instance = nativeColorMatrixFilter(colorMatrix);
    }
}
