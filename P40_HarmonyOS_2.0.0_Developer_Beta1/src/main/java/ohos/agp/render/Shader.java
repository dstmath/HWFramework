package ohos.agp.render;

import ohos.agp.utils.Color;
import ohos.agp.utils.Matrix;

public abstract class Shader {
    private static final int COUPLE_DATA_LEN = 2;
    static final Color[] DEFAULT_COLORS = {Color.BLACK, Color.WHITE};
    static final TileMode[] TILE_MODE_ARRAY = {TileMode.CLAMP_TILEMODE, TileMode.REPEAT_TILEMODE, TileMode.MIRROR_TILEMODE};
    public int[] intColors;
    protected Matrix mMatrix;
    protected long mNativeShaderHandle = 0;
    protected int mTileMode;
    public Color[] shaderColors;

    private native void nativeResetShaderMatrix(long j);

    private native void nativeSetShaderMatrix(long j, long j2);

    public enum TileMode {
        CLAMP_TILEMODE(0),
        REPEAT_TILEMODE(1),
        MIRROR_TILEMODE(2);
        
        final int enumInt;

        private TileMode(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public Shader(Color[] colorArr, TileMode tileMode) {
        if (colorArr != null) {
            this.shaderColors = (Color[]) colorArr.clone();
            int length = colorArr.length;
            if (length >= 2) {
                this.intColors = new int[length];
                for (int i = 0; i < length; i++) {
                    this.intColors[i] = colorArr[i].getValue();
                }
                this.mTileMode = tileMode.value();
            }
        }
    }

    public long getNativeHandle() {
        return this.mNativeShaderHandle;
    }

    public TileMode getTileMode() {
        int i = this.mTileMode;
        if (i > 2 || i < 0) {
            return TileMode.CLAMP_TILEMODE;
        }
        return TILE_MODE_ARRAY[i];
    }

    public Color[] getShaderColors() {
        return (Color[]) this.shaderColors.clone();
    }

    public void setShaderMatrix(Matrix matrix) {
        if (matrix == null) {
            this.mMatrix = null;
            nativeResetShaderMatrix(this.mNativeShaderHandle);
            return;
        }
        Matrix matrix2 = this.mMatrix;
        if (matrix2 == null) {
            this.mMatrix = new Matrix(matrix);
        } else if (!matrix2.equalsMatrixValue(matrix)) {
            this.mMatrix.setMatrix(matrix);
        }
        nativeSetShaderMatrix(this.mNativeShaderHandle, this.mMatrix.getNativeHandle());
    }
}
