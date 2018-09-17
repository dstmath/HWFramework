package android.graphics;

public class TableMaskFilter extends MaskFilter {
    private static native long nativeNewClip(int i, int i2);

    private static native long nativeNewGamma(float f);

    private static native long nativeNewTable(byte[] bArr);

    public TableMaskFilter(byte[] table) {
        if (table.length < 256) {
            throw new RuntimeException("table.length must be >= 256");
        }
        this.native_instance = nativeNewTable(table);
    }

    private TableMaskFilter(long ni) {
        this.native_instance = ni;
    }

    public static TableMaskFilter CreateClipTable(int min, int max) {
        return new TableMaskFilter(nativeNewClip(min, max));
    }

    public static TableMaskFilter CreateGammaTable(float gamma) {
        return new TableMaskFilter(nativeNewGamma(gamma));
    }
}
