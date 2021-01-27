package ohos.agp.colors;

public class ColorConverter {
    private static native CmykColor nativeToCmyk(int i);

    private static native HslColor nativeToHsl(int i);

    private static native HsvColor nativeToHsv(int i);

    private static native RgbColor nativeToRgb(Color color);

    private ColorConverter() {
    }

    public static CmykColor toCmyk(Color color) {
        RgbColor nativeToRgb = nativeToRgb(color);
        if (nativeToRgb == null) {
            return null;
        }
        return nativeToCmyk(nativeToRgb.asRgbaInt());
    }

    public static HslColor toHsl(Color color) {
        RgbColor nativeToRgb = nativeToRgb(color);
        if (nativeToRgb == null) {
            return null;
        }
        return nativeToHsl(nativeToRgb.asRgbaInt());
    }

    public static HsvColor toHsv(Color color) {
        RgbColor nativeToRgb = nativeToRgb(color);
        if (nativeToRgb == null) {
            return null;
        }
        return nativeToHsv(nativeToRgb.asRgbaInt());
    }

    public static RgbColor toRgb(Color color) {
        return color instanceof UserDefinedColor ? ((UserDefinedColor) color).toRgb() : nativeToRgb(color);
    }
}
