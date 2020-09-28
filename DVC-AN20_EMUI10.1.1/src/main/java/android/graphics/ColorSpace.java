package android.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import libcore.util.NativeAllocationRegistry;

public abstract class ColorSpace {
    public static final float[] ILLUMINANT_A = {0.44757f, 0.40745f};
    public static final float[] ILLUMINANT_B = {0.34842f, 0.35161f};
    public static final float[] ILLUMINANT_C = {0.31006f, 0.31616f};
    public static final float[] ILLUMINANT_D50 = {0.34567f, 0.3585f};
    private static final float[] ILLUMINANT_D50_XYZ = {0.964212f, 1.0f, 0.825188f};
    public static final float[] ILLUMINANT_D55 = {0.33242f, 0.34743f};
    public static final float[] ILLUMINANT_D60 = {0.32168f, 0.33767f};
    public static final float[] ILLUMINANT_D65 = {0.31271f, 0.32902f};
    public static final float[] ILLUMINANT_D75 = {0.29902f, 0.31485f};
    public static final float[] ILLUMINANT_E = {0.33333f, 0.33333f};
    public static final int MAX_ID = 63;
    public static final int MIN_ID = -1;
    private static final float[] NTSC_1953_PRIMARIES = {0.67f, 0.33f, 0.21f, 0.71f, 0.14f, 0.08f};
    private static final float[] SRGB_PRIMARIES = {0.64f, 0.33f, 0.3f, 0.6f, 0.15f, 0.06f};
    private static final Rgb.TransferParameters SRGB_TRANSFER_PARAMETERS = new Rgb.TransferParameters(0.9478672985781991d, 0.05213270142180095d, 0.07739938080495357d, 0.04045d, 2.4d);
    private static final ColorSpace[] sNamedColorSpaces = new ColorSpace[Named.values().length];
    private final int mId;
    private final Model mModel;
    private final String mName;

    public enum Named {
        SRGB,
        LINEAR_SRGB,
        EXTENDED_SRGB,
        LINEAR_EXTENDED_SRGB,
        BT709,
        BT2020,
        DCI_P3,
        DISPLAY_P3,
        NTSC_1953,
        SMPTE_C,
        ADOBE_RGB,
        PRO_PHOTO_RGB,
        ACES,
        ACESCG,
        CIE_XYZ,
        CIE_LAB
    }

    public enum RenderIntent {
        PERCEPTUAL,
        RELATIVE,
        SATURATION,
        ABSOLUTE
    }

    public abstract float[] fromXyz(float[] fArr);

    public abstract float getMaxValue(int i);

    public abstract float getMinValue(int i);

    public abstract boolean isWideGamut();

    public abstract float[] toXyz(float[] fArr);

    static {
        sNamedColorSpaces[Named.SRGB.ordinal()] = new Rgb("sRGB IEC61966-2.1", SRGB_PRIMARIES, ILLUMINANT_D65, SRGB_TRANSFER_PARAMETERS, Named.SRGB.ordinal());
        sNamedColorSpaces[Named.LINEAR_SRGB.ordinal()] = new Rgb("sRGB IEC61966-2.1 (Linear)", SRGB_PRIMARIES, ILLUMINANT_D65, 1.0d, 0.0f, 1.0f, Named.LINEAR_SRGB.ordinal());
        sNamedColorSpaces[Named.EXTENDED_SRGB.ordinal()] = new Rgb("scRGB-nl IEC 61966-2-2:2003", SRGB_PRIMARIES, ILLUMINANT_D65, null, $$Lambda$ColorSpace$BNp1CyCzsQzfEAds9uc4rJDfw.INSTANCE, $$Lambda$ColorSpace$S2rlqJvkXGTpUF6mZhvkElds8JE.INSTANCE, -0.799f, 2.399f, SRGB_TRANSFER_PARAMETERS, Named.EXTENDED_SRGB.ordinal());
        sNamedColorSpaces[Named.LINEAR_EXTENDED_SRGB.ordinal()] = new Rgb("scRGB IEC 61966-2-2:2003", SRGB_PRIMARIES, ILLUMINANT_D65, 1.0d, -0.5f, 7.499f, Named.LINEAR_EXTENDED_SRGB.ordinal());
        sNamedColorSpaces[Named.BT709.ordinal()] = new Rgb("Rec. ITU-R BT.709-5", new float[]{0.64f, 0.33f, 0.3f, 0.6f, 0.15f, 0.06f}, ILLUMINANT_D65, new Rgb.TransferParameters(0.9099181073703367d, 0.09008189262966333d, 0.2222222222222222d, 0.081d, 2.2222222222222223d), Named.BT709.ordinal());
        sNamedColorSpaces[Named.BT2020.ordinal()] = new Rgb("Rec. ITU-R BT.2020-1", new float[]{0.708f, 0.292f, 0.17f, 0.797f, 0.131f, 0.046f}, ILLUMINANT_D65, new Rgb.TransferParameters(0.9096697898662786d, 0.09033021013372146d, 0.2222222222222222d, 0.08145d, 2.2222222222222223d), Named.BT2020.ordinal());
        sNamedColorSpaces[Named.DCI_P3.ordinal()] = new Rgb("SMPTE RP 431-2-2007 DCI (P3)", new float[]{0.68f, 0.32f, 0.265f, 0.69f, 0.15f, 0.06f}, new float[]{0.314f, 0.351f}, 2.6d, 0.0f, 1.0f, Named.DCI_P3.ordinal());
        sNamedColorSpaces[Named.DISPLAY_P3.ordinal()] = new Rgb("Display P3", new float[]{0.68f, 0.32f, 0.265f, 0.69f, 0.15f, 0.06f}, ILLUMINANT_D65, SRGB_TRANSFER_PARAMETERS, Named.DISPLAY_P3.ordinal());
        sNamedColorSpaces[Named.NTSC_1953.ordinal()] = new Rgb("NTSC (1953)", NTSC_1953_PRIMARIES, ILLUMINANT_C, new Rgb.TransferParameters(0.9099181073703367d, 0.09008189262966333d, 0.2222222222222222d, 0.081d, 2.2222222222222223d), Named.NTSC_1953.ordinal());
        sNamedColorSpaces[Named.SMPTE_C.ordinal()] = new Rgb("SMPTE-C RGB", new float[]{0.63f, 0.34f, 0.31f, 0.595f, 0.155f, 0.07f}, ILLUMINANT_D65, new Rgb.TransferParameters(0.9099181073703367d, 0.09008189262966333d, 0.2222222222222222d, 0.081d, 2.2222222222222223d), Named.SMPTE_C.ordinal());
        sNamedColorSpaces[Named.ADOBE_RGB.ordinal()] = new Rgb("Adobe RGB (1998)", new float[]{0.64f, 0.33f, 0.21f, 0.71f, 0.15f, 0.06f}, ILLUMINANT_D65, 2.2d, 0.0f, 1.0f, Named.ADOBE_RGB.ordinal());
        sNamedColorSpaces[Named.PRO_PHOTO_RGB.ordinal()] = new Rgb("ROMM RGB ISO 22028-2:2013", new float[]{0.7347f, 0.2653f, 0.1596f, 0.8404f, 0.0366f, 1.0E-4f}, ILLUMINANT_D50, new Rgb.TransferParameters(1.0d, 0.0d, 0.0625d, 0.031248d, 1.8d), Named.PRO_PHOTO_RGB.ordinal());
        sNamedColorSpaces[Named.ACES.ordinal()] = new Rgb("SMPTE ST 2065-1:2012 ACES", new float[]{0.7347f, 0.2653f, 0.0f, 1.0f, 1.0E-4f, -0.077f}, ILLUMINANT_D60, 1.0d, -65504.0f, 65504.0f, Named.ACES.ordinal());
        sNamedColorSpaces[Named.ACESCG.ordinal()] = new Rgb("Academy S-2014-004 ACEScg", new float[]{0.713f, 0.293f, 0.165f, 0.83f, 0.128f, 0.044f}, ILLUMINANT_D60, 1.0d, -65504.0f, 65504.0f, Named.ACESCG.ordinal());
        sNamedColorSpaces[Named.CIE_XYZ.ordinal()] = new Xyz("Generic XYZ", Named.CIE_XYZ.ordinal());
        sNamedColorSpaces[Named.CIE_LAB.ordinal()] = new Lab("Generic L*a*b*", Named.CIE_LAB.ordinal());
    }

    public enum Adaptation {
        BRADFORD(new float[]{0.8951f, -0.7502f, 0.0389f, 0.2664f, 1.7135f, -0.0685f, -0.1614f, 0.0367f, 1.0296f}),
        VON_KRIES(new float[]{0.40024f, -0.2263f, 0.0f, 0.7076f, 1.16532f, 0.0f, -0.08081f, 0.0457f, 0.91822f}),
        CIECAT02(new float[]{0.7328f, -0.7036f, 0.003f, 0.4296f, 1.6975f, 0.0136f, -0.1624f, 0.0061f, 0.9834f});
        
        final float[] mTransform;

        private Adaptation(float[] transform) {
            this.mTransform = transform;
        }
    }

    public enum Model {
        RGB(3),
        XYZ(3),
        LAB(3),
        CMYK(4);
        
        private final int mComponentCount;

        private Model(int componentCount) {
            this.mComponentCount = componentCount;
        }

        public int getComponentCount() {
            return this.mComponentCount;
        }
    }

    private ColorSpace(String name, Model model, int id) {
        if (name == null || name.length() < 1) {
            throw new IllegalArgumentException("The name of a color space cannot be null and must contain at least 1 character");
        } else if (model == null) {
            throw new IllegalArgumentException("A color space must have a model");
        } else if (id < -1 || id > 63) {
            throw new IllegalArgumentException("The id must be between -1 and 63");
        } else {
            this.mName = name;
            this.mModel = model;
            this.mId = id;
        }
    }

    public String getName() {
        return this.mName;
    }

    public int getId() {
        return this.mId;
    }

    public Model getModel() {
        return this.mModel;
    }

    public int getComponentCount() {
        return this.mModel.getComponentCount();
    }

    public boolean isSrgb() {
        return false;
    }

    public float[] toXyz(float r, float g, float b) {
        return toXyz(new float[]{r, g, b});
    }

    public float[] fromXyz(float x, float y, float z) {
        float[] xyz = new float[this.mModel.getComponentCount()];
        xyz[0] = x;
        xyz[1] = y;
        xyz[2] = z;
        return fromXyz(xyz);
    }

    public String toString() {
        return this.mName + " (id=" + this.mId + ", model=" + this.mModel + ")";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ColorSpace that = (ColorSpace) o;
        if (this.mId != that.mId || !this.mName.equals(that.mName)) {
            return false;
        }
        if (this.mModel == that.mModel) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((this.mName.hashCode() * 31) + this.mModel.hashCode()) * 31) + this.mId;
    }

    public static Connector connect(ColorSpace source, ColorSpace destination) {
        return connect(source, destination, RenderIntent.PERCEPTUAL);
    }

    public static Connector connect(ColorSpace source, ColorSpace destination, RenderIntent intent) {
        if (source.equals(destination)) {
            return Connector.identity(source);
        }
        if (source.getModel() == Model.RGB && destination.getModel() == Model.RGB) {
            return new Connector.Rgb((Rgb) source, (Rgb) destination, intent);
        }
        return new Connector(source, destination, intent);
    }

    public static Connector connect(ColorSpace source) {
        return connect(source, RenderIntent.PERCEPTUAL);
    }

    public static Connector connect(ColorSpace source, RenderIntent intent) {
        if (source.isSrgb()) {
            return Connector.identity(source);
        }
        if (source.getModel() == Model.RGB) {
            return new Connector.Rgb((Rgb) source, (Rgb) get(Named.SRGB), intent);
        }
        return new Connector(source, get(Named.SRGB), intent);
    }

    public static ColorSpace adapt(ColorSpace colorSpace, float[] whitePoint) {
        return adapt(colorSpace, whitePoint, Adaptation.BRADFORD);
    }

    public static ColorSpace adapt(ColorSpace colorSpace, float[] whitePoint, Adaptation adaptation) {
        if (colorSpace.getModel() != Model.RGB) {
            return colorSpace;
        }
        Rgb rgb = (Rgb) colorSpace;
        if (compare(rgb.mWhitePoint, whitePoint)) {
            return colorSpace;
        }
        return new Rgb(rgb, mul3x3(chromaticAdaptation(adaptation.mTransform, xyYToXyz(rgb.getWhitePoint()), whitePoint.length == 3 ? Arrays.copyOf(whitePoint, 3) : xyYToXyz(whitePoint)), rgb.mTransform), whitePoint);
    }

    /* access modifiers changed from: private */
    public static float[] adaptToIlluminantD50(float[] origWhitePoint, float[] origTransform) {
        float[] desired = ILLUMINANT_D50;
        if (compare(origWhitePoint, desired)) {
            return origTransform;
        }
        return mul3x3(chromaticAdaptation(Adaptation.BRADFORD.mTransform, xyYToXyz(origWhitePoint), xyYToXyz(desired)), origTransform);
    }

    static ColorSpace get(int index) {
        if (index >= 0 && index < Named.values().length) {
            return sNamedColorSpaces[index];
        }
        throw new IllegalArgumentException("Invalid ID, must be in the range [0.." + Named.values().length + ")");
    }

    public static ColorSpace get(Named name) {
        return sNamedColorSpaces[name.ordinal()];
    }

    public static ColorSpace match(float[] toXYZD50, Rgb.TransferParameters function) {
        ColorSpace[] colorSpaceArr = sNamedColorSpaces;
        for (ColorSpace colorSpace : colorSpaceArr) {
            if (colorSpace.getModel() == Model.RGB) {
                Rgb rgb = (Rgb) adapt(colorSpace, ILLUMINANT_D50_XYZ);
                if (compare(toXYZD50, rgb.mTransform) && compare(function, rgb.mTransferParameters)) {
                    return colorSpace;
                }
            }
        }
        return null;
    }

    public static Renderer createRenderer() {
        return new Renderer();
    }

    /* access modifiers changed from: private */
    public static double rcpResponse(double x, double a, double b, double c, double d, double g) {
        return x >= d * c ? (Math.pow(x, 1.0d / g) - b) / a : x / c;
    }

    /* access modifiers changed from: private */
    public static double response(double x, double a, double b, double c, double d, double g) {
        return x >= d ? Math.pow((a * x) + b, g) : c * x;
    }

    /* access modifiers changed from: private */
    public static double rcpResponse(double x, double a, double b, double c, double d, double e, double f, double g) {
        return x >= d * c ? (Math.pow(x - e, 1.0d / g) - b) / a : (x - f) / c;
    }

    /* access modifiers changed from: private */
    public static double response(double x, double a, double b, double c, double d, double e, double f, double g) {
        return x >= d ? Math.pow((a * x) + b, g) + e : (c * x) + f;
    }

    /* access modifiers changed from: private */
    public static double absRcpResponse(double x, double a, double b, double c, double d, double g) {
        return Math.copySign(rcpResponse(x < 0.0d ? -x : x, a, b, c, d, g), x);
    }

    /* access modifiers changed from: private */
    public static double absResponse(double x, double a, double b, double c, double d, double g) {
        return Math.copySign(response(x < 0.0d ? -x : x, a, b, c, d, g), x);
    }

    private static boolean compare(Rgb.TransferParameters a, Rgb.TransferParameters b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null || Math.abs(a.a - b.a) >= 0.001d || Math.abs(a.b - b.b) >= 0.001d || Math.abs(a.c - b.c) >= 0.001d || Math.abs(a.d - b.d) >= 0.002d || Math.abs(a.e - b.e) >= 0.001d || Math.abs(a.f - b.f) >= 0.001d || Math.abs(a.g - b.g) >= 0.001d) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean compare(float[] a, float[] b) {
        if (a == b) {
            return true;
        }
        for (int i = 0; i < a.length; i++) {
            if (Float.compare(a[i], b[i]) != 0 && Math.abs(a[i] - b[i]) > 0.001f) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static float[] inverse3x3(float[] m) {
        float a = m[0];
        float b = m[3];
        float c = m[6];
        float d = m[1];
        float e = m[4];
        float f = m[7];
        float g = m[2];
        float h = m[5];
        float i = m[8];
        float A = (e * i) - (f * h);
        float B = (f * g) - (d * i);
        float C = (d * h) - (e * g);
        float det = (a * A) + (b * B) + (c * C);
        float[] inverted = new float[m.length];
        inverted[0] = A / det;
        inverted[1] = B / det;
        inverted[2] = C / det;
        inverted[3] = ((c * h) - (b * i)) / det;
        inverted[4] = ((a * i) - (c * g)) / det;
        inverted[5] = ((b * g) - (a * h)) / det;
        inverted[6] = ((b * f) - (c * e)) / det;
        inverted[7] = ((c * d) - (a * f)) / det;
        inverted[8] = ((a * e) - (b * d)) / det;
        return inverted;
    }

    public static float[] mul3x3(float[] lhs, float[] rhs) {
        return new float[]{(lhs[0] * rhs[0]) + (lhs[3] * rhs[1]) + (lhs[6] * rhs[2]), (lhs[1] * rhs[0]) + (lhs[4] * rhs[1]) + (lhs[7] * rhs[2]), (lhs[2] * rhs[0]) + (lhs[5] * rhs[1]) + (lhs[8] * rhs[2]), (lhs[0] * rhs[3]) + (lhs[3] * rhs[4]) + (lhs[6] * rhs[5]), (lhs[1] * rhs[3]) + (lhs[4] * rhs[4]) + (lhs[7] * rhs[5]), (lhs[2] * rhs[3]) + (lhs[5] * rhs[4]) + (lhs[8] * rhs[5]), (lhs[0] * rhs[6]) + (lhs[3] * rhs[7]) + (lhs[6] * rhs[8]), (lhs[1] * rhs[6]) + (lhs[4] * rhs[7]) + (lhs[7] * rhs[8]), (lhs[2] * rhs[6]) + (lhs[5] * rhs[7]) + (lhs[8] * rhs[8])};
    }

    /* access modifiers changed from: private */
    public static float[] mul3x3Float3(float[] lhs, float[] rhs) {
        float r0 = rhs[0];
        float r1 = rhs[1];
        float r2 = rhs[2];
        rhs[0] = (lhs[0] * r0) + (lhs[3] * r1) + (lhs[6] * r2);
        rhs[1] = (lhs[1] * r0) + (lhs[4] * r1) + (lhs[7] * r2);
        rhs[2] = (lhs[2] * r0) + (lhs[5] * r1) + (lhs[8] * r2);
        return rhs;
    }

    /* access modifiers changed from: private */
    public static float[] mul3x3Diag(float[] lhs, float[] rhs) {
        return new float[]{lhs[0] * rhs[0], lhs[1] * rhs[1], lhs[2] * rhs[2], lhs[0] * rhs[3], lhs[1] * rhs[4], lhs[2] * rhs[5], lhs[0] * rhs[6], lhs[1] * rhs[7], lhs[2] * rhs[8]};
    }

    /* access modifiers changed from: private */
    public static float[] xyYToXyz(float[] xyY) {
        return new float[]{xyY[0] / xyY[1], 1.0f, ((1.0f - xyY[0]) - xyY[1]) / xyY[1]};
    }

    /* access modifiers changed from: private */
    public static void xyYToUv(float[] xyY) {
        for (int i = 0; i < xyY.length; i += 2) {
            float x = xyY[i];
            float y = xyY[i + 1];
            float d = (-2.0f * x) + (12.0f * y) + 3.0f;
            xyY[i] = (4.0f * x) / d;
            xyY[i + 1] = (9.0f * y) / d;
        }
    }

    /* access modifiers changed from: private */
    public static float[] chromaticAdaptation(float[] matrix, float[] srcWhitePoint, float[] dstWhitePoint) {
        float[] srcLMS = mul3x3Float3(matrix, srcWhitePoint);
        float[] dstLMS = mul3x3Float3(matrix, dstWhitePoint);
        return mul3x3(inverse3x3(matrix), mul3x3Diag(new float[]{dstLMS[0] / srcLMS[0], dstLMS[1] / srcLMS[1], dstLMS[2] / srcLMS[2]}, matrix));
    }

    public static float[] cctToXyz(int cct) {
        float x;
        float y;
        if (cct >= 1) {
            float icct = 1000.0f / ((float) cct);
            float icct2 = icct * icct;
            if (((float) cct) <= 4000.0f) {
                x = (((0.8776956f * icct) + 0.17991f) - (0.2343589f * icct2)) - ((0.2661239f * icct2) * icct);
            } else {
                x = (((0.2226347f * icct) + 0.24039f) + (2.1070378f * icct2)) - ((3.025847f * icct2) * icct);
            }
            float x2 = x * x;
            if (((float) cct) <= 2222.0f) {
                y = (((2.1855583f * x) - 22.118805f) - (1.3481102f * x2)) - ((1.1063814f * x2) * x);
            } else if (((float) cct) <= 4000.0f) {
                y = (((2.09137f * x) - 26.561451f) - (1.3741859f * x2)) - ((0.9549476f * x2) * x);
            } else {
                y = (((3.7511299f * x) - 12.159526f) - (5.873387f * x2)) + (3.081758f * x2 * x);
            }
            return xyYToXyz(new float[]{x, y});
        }
        throw new IllegalArgumentException("Temperature must be greater than 0");
    }

    public static float[] cctToIlluminantdXyz(int cct) {
        float x;
        if (cct >= 1) {
            float icct = 1.0f / ((float) cct);
            float icct2 = icct * icct;
            if (((float) cct) <= 7000.0f) {
                x = (((99.11f * icct) + 0.244063f) + (2967800.0f * icct2)) - ((4.6070001E9f * icct2) * icct);
            } else {
                x = (((247.48f * icct) + 0.23704f) + (1901800.0f * icct2)) - ((2.0064E9f * icct2) * icct);
            }
            return xyYToXyz(new float[]{x, (((-3.0f * x) * x) + (2.87f * x)) - 0.275f});
        }
        throw new IllegalArgumentException("Temperature must be greater than 0");
    }

    public static float[] chromaticAdaptation(Adaptation adaptation, float[] srcWhitePoint, float[] dstWhitePoint) {
        float[] srcXyz = srcWhitePoint.length == 3 ? Arrays.copyOf(srcWhitePoint, 3) : xyYToXyz(srcWhitePoint);
        float[] dstXyz = dstWhitePoint.length == 3 ? Arrays.copyOf(dstWhitePoint, 3) : xyYToXyz(dstWhitePoint);
        if (compare(srcXyz, dstXyz)) {
            return new float[]{1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f};
        }
        return chromaticAdaptation(adaptation.mTransform, srcXyz, dstXyz);
    }

    private static final class Xyz extends ColorSpace {
        private Xyz(String name, int id) {
            super(name, Model.XYZ, id);
        }

        @Override // android.graphics.ColorSpace
        public boolean isWideGamut() {
            return true;
        }

        @Override // android.graphics.ColorSpace
        public float getMinValue(int component) {
            return -2.0f;
        }

        @Override // android.graphics.ColorSpace
        public float getMaxValue(int component) {
            return 2.0f;
        }

        @Override // android.graphics.ColorSpace
        public float[] toXyz(float[] v) {
            v[0] = clamp(v[0]);
            v[1] = clamp(v[1]);
            v[2] = clamp(v[2]);
            return v;
        }

        @Override // android.graphics.ColorSpace
        public float[] fromXyz(float[] v) {
            v[0] = clamp(v[0]);
            v[1] = clamp(v[1]);
            v[2] = clamp(v[2]);
            return v;
        }

        private static float clamp(float x) {
            if (x < -2.0f) {
                return -2.0f;
            }
            if (x > 2.0f) {
                return 2.0f;
            }
            return x;
        }
    }

    private static final class Lab extends ColorSpace {
        private static final float A = 0.008856452f;
        private static final float B = 7.787037f;
        private static final float C = 0.13793103f;
        private static final float D = 0.20689656f;

        private Lab(String name, int id) {
            super(name, Model.LAB, id);
        }

        @Override // android.graphics.ColorSpace
        public boolean isWideGamut() {
            return true;
        }

        @Override // android.graphics.ColorSpace
        public float getMinValue(int component) {
            return component == 0 ? 0.0f : -128.0f;
        }

        @Override // android.graphics.ColorSpace
        public float getMaxValue(int component) {
            return component == 0 ? 100.0f : 128.0f;
        }

        @Override // android.graphics.ColorSpace
        public float[] toXyz(float[] v) {
            v[0] = clamp(v[0], 0.0f, 100.0f);
            v[1] = clamp(v[1], -128.0f, 128.0f);
            v[2] = clamp(v[2], -128.0f, 128.0f);
            float fy = (v[0] + 16.0f) / 116.0f;
            float fx = (v[1] * 0.002f) + fy;
            float fz = fy - (v[2] * 0.005f);
            float X = fx > D ? fx * fx * fx : (fx - C) * 0.12841855f;
            float Y = fy > D ? fy * fy * fy : (fy - C) * 0.12841855f;
            float Z = fz > D ? fz * fz * fz : (fz - C) * 0.12841855f;
            v[0] = ColorSpace.ILLUMINANT_D50_XYZ[0] * X;
            v[1] = ColorSpace.ILLUMINANT_D50_XYZ[1] * Y;
            v[2] = ColorSpace.ILLUMINANT_D50_XYZ[2] * Z;
            return v;
        }

        @Override // android.graphics.ColorSpace
        public float[] fromXyz(float[] v) {
            float X = v[0] / ColorSpace.ILLUMINANT_D50_XYZ[0];
            float Y = v[1] / ColorSpace.ILLUMINANT_D50_XYZ[1];
            float Z = v[2] / ColorSpace.ILLUMINANT_D50_XYZ[2];
            float fx = X > A ? (float) Math.pow((double) X, 0.3333333333333333d) : (X * B) + C;
            float fy = Y > A ? (float) Math.pow((double) Y, 0.3333333333333333d) : (Y * B) + C;
            float fz = Z > A ? (float) Math.pow((double) Z, 0.3333333333333333d) : (B * Z) + C;
            v[0] = clamp((116.0f * fy) - 16.0f, 0.0f, 100.0f);
            v[1] = clamp((fx - fy) * 500.0f, -128.0f, 128.0f);
            v[2] = clamp((fy - fz) * 200.0f, -128.0f, 128.0f);
            return v;
        }

        private static float clamp(float x, float min, float max) {
            if (x < min) {
                return min;
            }
            return x > max ? max : x;
        }
    }

    /* access modifiers changed from: package-private */
    public long getNativeInstance() {
        throw new IllegalArgumentException("colorSpace must be an RGB color space");
    }

    public long getNativeInstanceEx() {
        return getNativeInstance();
    }

    public static class Rgb extends ColorSpace {
        private final DoubleUnaryOperator mClampedEotf;
        private final DoubleUnaryOperator mClampedOetf;
        private final DoubleUnaryOperator mEotf;
        private final float[] mInverseTransform;
        private final boolean mIsSrgb;
        private final boolean mIsWideGamut;
        private final float mMax;
        private final float mMin;
        private final long mNativePtr;
        private final DoubleUnaryOperator mOetf;
        private final float[] mPrimaries;
        private final TransferParameters mTransferParameters;
        private final float[] mTransform;
        private final float[] mWhitePoint;

        private static native long nativeCreate(float f, float f2, float f3, float f4, float f5, float f6, float f7, float[] fArr);

        /* access modifiers changed from: private */
        public static native long nativeGetNativeFinalizer();

        public static class TransferParameters {
            public final double a;
            public final double b;
            public final double c;
            public final double d;
            public final double e;
            public final double f;
            public final double g;

            public TransferParameters(double a2, double b2, double c2, double d2, double g2) {
                this(a2, b2, c2, d2, 0.0d, 0.0d, g2);
            }

            public TransferParameters(double a2, double b2, double c2, double d2, double e2, double f2, double g2) {
                if (!Double.isNaN(a2) && !Double.isNaN(b2) && !Double.isNaN(c2)) {
                    if (!Double.isNaN(d2) && !Double.isNaN(e2) && !Double.isNaN(f2)) {
                        if (!Double.isNaN(g2)) {
                            if (d2 < 0.0d || d2 > ((double) (Math.ulp(1.0f) + 1.0f))) {
                                throw new IllegalArgumentException("Parameter d must be in the range [0..1], was " + d2);
                            } else if (d2 == 0.0d && (a2 == 0.0d || g2 == 0.0d)) {
                                throw new IllegalArgumentException("Parameter a or g is zero, the transfer function is constant");
                            } else if (d2 >= 1.0d && c2 == 0.0d) {
                                throw new IllegalArgumentException("Parameter c is zero, the transfer function is constant");
                            } else if ((a2 == 0.0d || g2 == 0.0d) && c2 == 0.0d) {
                                throw new IllegalArgumentException("Parameter a or g is zero, and c is zero, the transfer function is constant");
                            } else if (c2 < 0.0d) {
                                throw new IllegalArgumentException("The transfer function must be increasing");
                            } else if (a2 < 0.0d || g2 < 0.0d) {
                                throw new IllegalArgumentException("The transfer function must be positive or increasing");
                            } else {
                                this.a = a2;
                                this.b = b2;
                                this.c = c2;
                                this.d = d2;
                                this.e = e2;
                                this.f = f2;
                                this.g = g2;
                                return;
                            }
                        }
                    }
                }
                throw new IllegalArgumentException("Parameters cannot be NaN");
            }

            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                TransferParameters that = (TransferParameters) o;
                if (Double.compare(that.a, this.a) != 0 || Double.compare(that.b, this.b) != 0 || Double.compare(that.c, this.c) != 0 || Double.compare(that.d, this.d) != 0 || Double.compare(that.e, this.e) != 0 || Double.compare(that.f, this.f) != 0) {
                    return false;
                }
                if (Double.compare(that.g, this.g) == 0) {
                    return true;
                }
                return false;
            }

            public int hashCode() {
                long temp = Double.doubleToLongBits(this.a);
                long temp2 = Double.doubleToLongBits(this.b);
                int result = (((int) ((temp >>> 32) ^ temp)) * 31) + ((int) ((temp2 >>> 32) ^ temp2));
                long temp3 = Double.doubleToLongBits(this.c);
                long temp4 = Double.doubleToLongBits(this.d);
                int result2 = (((result * 31) + ((int) ((temp3 >>> 32) ^ temp3))) * 31) + ((int) ((temp4 >>> 32) ^ temp4));
                long temp5 = Double.doubleToLongBits(this.e);
                long temp6 = Double.doubleToLongBits(this.f);
                int result3 = (((result2 * 31) + ((int) ((temp5 >>> 32) ^ temp5))) * 31) + ((int) ((temp6 >>> 32) ^ temp6));
                long temp7 = Double.doubleToLongBits(this.g);
                return (result3 * 31) + ((int) ((temp7 >>> 32) ^ temp7));
            }
        }

        /* access modifiers changed from: package-private */
        @Override // android.graphics.ColorSpace
        public long getNativeInstance() {
            long j = this.mNativePtr;
            if (j != 0) {
                return j;
            }
            throw new IllegalArgumentException("ColorSpace must use an ICC parametric transfer function! used " + this);
        }

        public Rgb(String name, float[] toXYZ, DoubleUnaryOperator oetf, DoubleUnaryOperator eotf) {
            this(name, computePrimaries(toXYZ), computeWhitePoint(toXYZ), null, oetf, eotf, 0.0f, 1.0f, null, -1);
        }

        public Rgb(String name, float[] primaries, float[] whitePoint, DoubleUnaryOperator oetf, DoubleUnaryOperator eotf, float min, float max) {
            this(name, primaries, whitePoint, null, oetf, eotf, min, max, null, -1);
        }

        public Rgb(String name, float[] toXYZ, TransferParameters function) {
            this(name, computePrimaries(toXYZ), computeWhitePoint(toXYZ), function, -1);
        }

        public Rgb(String name, float[] primaries, float[] whitePoint, TransferParameters function) {
            this(name, primaries, whitePoint, function, -1);
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        private Rgb(String name, float[] primaries, float[] whitePoint, TransferParameters function, int id) {
            this(name, primaries, whitePoint, null, r5, r6, 0.0f, 1.0f, function, id);
            $$Lambda$ColorSpace$Rgb$bWzafC8vMHNuVmRuTUPEFUMlfuY r5;
            $$Lambda$ColorSpace$Rgb$b9VGKuNnse0bbguR9jbOM_wK2Ac r6;
            if (function.e == 0.0d && function.f == 0.0d) {
                r5 = new DoubleUnaryOperator() {
                    /* class android.graphics.$$Lambda$ColorSpace$Rgb$bWzafC8vMHNuVmRuTUPEFUMlfuY */

                    public final double applyAsDouble(double d) {
                        ColorSpace.Rgb.TransferParameters transferParameters;
                        return ColorSpace.rcpResponse(d, transferParameters.a, transferParameters.b, transferParameters.c, transferParameters.d, ColorSpace.Rgb.TransferParameters.this.g);
                    }
                };
            } else {
                r5 = new DoubleUnaryOperator() {
                    /* class android.graphics.$$Lambda$ColorSpace$Rgb$V_0lmM2WEpxGBDV_1G1wvvidn7Y */

                    public final double applyAsDouble(double d) {
                        ColorSpace.Rgb.TransferParameters transferParameters;
                        return ColorSpace.rcpResponse(d, transferParameters.a, transferParameters.b, transferParameters.c, transferParameters.d, transferParameters.e, transferParameters.f, ColorSpace.Rgb.TransferParameters.this.g);
                    }
                };
            }
            if (function.e == 0.0d && function.f == 0.0d) {
                r6 = new DoubleUnaryOperator() {
                    /* class android.graphics.$$Lambda$ColorSpace$Rgb$b9VGKuNnse0bbguR9jbOM_wK2Ac */

                    public final double applyAsDouble(double d) {
                        ColorSpace.Rgb.TransferParameters transferParameters;
                        return ColorSpace.response(d, transferParameters.a, transferParameters.b, transferParameters.c, transferParameters.d, ColorSpace.Rgb.TransferParameters.this.g);
                    }
                };
            } else {
                r6 = new DoubleUnaryOperator() {
                    /* class android.graphics.$$Lambda$ColorSpace$Rgb$iMkODTKa3_8kPZUnZZerD2Lvyo */

                    public final double applyAsDouble(double d) {
                        ColorSpace.Rgb.TransferParameters transferParameters;
                        return ColorSpace.response(d, transferParameters.a, transferParameters.b, transferParameters.c, transferParameters.d, transferParameters.e, transferParameters.f, ColorSpace.Rgb.TransferParameters.this.g);
                    }
                };
            }
        }

        public Rgb(String name, float[] toXYZ, double gamma) {
            this(name, computePrimaries(toXYZ), computeWhitePoint(toXYZ), gamma, 0.0f, 1.0f, -1);
        }

        public Rgb(String name, float[] primaries, float[] whitePoint, double gamma) {
            this(name, primaries, whitePoint, gamma, 0.0f, 1.0f, -1);
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        private Rgb(String name, float[] primaries, float[] whitePoint, double gamma, float min, float max, int id) {
            this(name, primaries, whitePoint, null, r18, r19, min, max, new TransferParameters(1.0d, 0.0d, 0.0d, 0.0d, gamma), id);
            $$Lambda$ColorSpace$Rgb$CqKld6797g7__JnuY0NeFz5q4_E r18;
            $$Lambda$ColorSpace$Rgb$ZvS77aTfobOSa2o9MTqYMph4Rcg r19;
            if (gamma == 1.0d) {
                r18 = DoubleUnaryOperator.identity();
            } else {
                r18 = new DoubleUnaryOperator(gamma) {
                    /* class android.graphics.$$Lambda$ColorSpace$Rgb$CqKld6797g7__JnuY0NeFz5q4_E */
                    private final /* synthetic */ double f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final double applyAsDouble(double d) {
                        return ColorSpace.Rgb.lambda$new$4(this.f$0, d);
                    }
                };
            }
            if (gamma == 1.0d) {
                r19 = DoubleUnaryOperator.identity();
            } else {
                r19 = new DoubleUnaryOperator(gamma) {
                    /* class android.graphics.$$Lambda$ColorSpace$Rgb$ZvS77aTfobOSa2o9MTqYMph4Rcg */
                    private final /* synthetic */ double f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final double applyAsDouble(double d) {
                        return ColorSpace.Rgb.lambda$new$5(this.f$0, d);
                    }
                };
            }
        }

        static /* synthetic */ double lambda$new$4(double gamma, double x) {
            double d = 0.0d;
            if (x >= 0.0d) {
                d = x;
            }
            return Math.pow(d, 1.0d / gamma);
        }

        static /* synthetic */ double lambda$new$5(double gamma, double x) {
            double d = 0.0d;
            if (x >= 0.0d) {
                d = x;
            }
            return Math.pow(d, gamma);
        }

        private Rgb(String name, float[] primaries, float[] whitePoint, float[] transform, DoubleUnaryOperator oetf, DoubleUnaryOperator eotf, float min, float max, TransferParameters transferParameters, int id) {
            super(name, Model.RGB, id);
            float[] fArr;
            if (primaries == null || !(primaries.length == 6 || primaries.length == 9)) {
                throw new IllegalArgumentException("The color space's primaries must be defined as an array of 6 floats in xyY or 9 floats in XYZ");
            } else if (whitePoint == null || !(whitePoint.length == 2 || whitePoint.length == 3)) {
                throw new IllegalArgumentException("The color space's white point must be defined as an array of 2 floats in xyY or 3 float in XYZ");
            } else if (oetf == null || eotf == null) {
                throw new IllegalArgumentException("The transfer functions of a color space cannot be null");
            } else if (min < max) {
                this.mWhitePoint = xyWhitePoint(whitePoint);
                this.mPrimaries = xyPrimaries(primaries);
                if (transform == null) {
                    this.mTransform = computeXYZMatrix(this.mPrimaries, this.mWhitePoint);
                } else if (transform.length == 9) {
                    this.mTransform = transform;
                } else {
                    throw new IllegalArgumentException("Transform must have 9 entries! Has " + transform.length);
                }
                this.mInverseTransform = ColorSpace.inverse3x3(this.mTransform);
                this.mOetf = oetf;
                this.mEotf = eotf;
                this.mMin = min;
                this.mMax = max;
                DoubleUnaryOperator clamp = new DoubleUnaryOperator() {
                    /* class android.graphics.$$Lambda$ColorSpace$Rgb$8EkhO2jIf14tuA3BvrmYJMa7YXM */

                    public final double applyAsDouble(double d) {
                        return ColorSpace.Rgb.this.clamp(d);
                    }
                };
                this.mClampedOetf = oetf.andThen(clamp);
                this.mClampedEotf = clamp.andThen(eotf);
                this.mTransferParameters = transferParameters;
                this.mIsWideGamut = isWideGamut(this.mPrimaries, min, max);
                this.mIsSrgb = isSrgb(this.mPrimaries, this.mWhitePoint, oetf, eotf, min, max, id);
                if (this.mTransferParameters != null) {
                    float[] fArr2 = this.mWhitePoint;
                    if (fArr2 == null || (fArr = this.mTransform) == null) {
                        throw new IllegalStateException("ColorSpace (" + this + ") cannot create native object! mWhitePoint: " + this.mWhitePoint + " mTransform: " + this.mTransform);
                    }
                    this.mNativePtr = nativeCreate((float) this.mTransferParameters.a, (float) this.mTransferParameters.b, (float) this.mTransferParameters.c, (float) this.mTransferParameters.d, (float) this.mTransferParameters.e, (float) this.mTransferParameters.f, (float) this.mTransferParameters.g, ColorSpace.adaptToIlluminantD50(fArr2, fArr));
                    NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativePtr);
                    return;
                }
                this.mNativePtr = 0;
            } else {
                throw new IllegalArgumentException("Invalid range: min=" + min + ", max=" + max + "; min must be strictly < max");
            }
        }

        private static class NoImagePreloadHolder {
            public static final NativeAllocationRegistry sRegistry = new NativeAllocationRegistry(Rgb.class.getClassLoader(), Rgb.nativeGetNativeFinalizer(), 0);

            private NoImagePreloadHolder() {
            }
        }

        private Rgb(Rgb colorSpace, float[] transform, float[] whitePoint) {
            this(colorSpace.getName(), colorSpace.mPrimaries, whitePoint, transform, colorSpace.mOetf, colorSpace.mEotf, colorSpace.mMin, colorSpace.mMax, colorSpace.mTransferParameters, -1);
        }

        public float[] getWhitePoint(float[] whitePoint) {
            float[] fArr = this.mWhitePoint;
            whitePoint[0] = fArr[0];
            whitePoint[1] = fArr[1];
            return whitePoint;
        }

        public float[] getWhitePoint() {
            float[] fArr = this.mWhitePoint;
            return Arrays.copyOf(fArr, fArr.length);
        }

        public float[] getPrimaries(float[] primaries) {
            float[] fArr = this.mPrimaries;
            System.arraycopy(fArr, 0, primaries, 0, fArr.length);
            return primaries;
        }

        public float[] getPrimaries() {
            float[] fArr = this.mPrimaries;
            return Arrays.copyOf(fArr, fArr.length);
        }

        public float[] getTransform(float[] transform) {
            float[] fArr = this.mTransform;
            System.arraycopy(fArr, 0, transform, 0, fArr.length);
            return transform;
        }

        public float[] getTransform() {
            float[] fArr = this.mTransform;
            return Arrays.copyOf(fArr, fArr.length);
        }

        public float[] getInverseTransform(float[] inverseTransform) {
            float[] fArr = this.mInverseTransform;
            System.arraycopy(fArr, 0, inverseTransform, 0, fArr.length);
            return inverseTransform;
        }

        public float[] getInverseTransform() {
            float[] fArr = this.mInverseTransform;
            return Arrays.copyOf(fArr, fArr.length);
        }

        public DoubleUnaryOperator getOetf() {
            return this.mClampedOetf;
        }

        public DoubleUnaryOperator getEotf() {
            return this.mClampedEotf;
        }

        public TransferParameters getTransferParameters() {
            return this.mTransferParameters;
        }

        @Override // android.graphics.ColorSpace
        public boolean isSrgb() {
            return this.mIsSrgb;
        }

        @Override // android.graphics.ColorSpace
        public boolean isWideGamut() {
            return this.mIsWideGamut;
        }

        @Override // android.graphics.ColorSpace
        public float getMinValue(int component) {
            return this.mMin;
        }

        @Override // android.graphics.ColorSpace
        public float getMaxValue(int component) {
            return this.mMax;
        }

        public float[] toLinear(float r, float g, float b) {
            return toLinear(new float[]{r, g, b});
        }

        public float[] toLinear(float[] v) {
            v[0] = (float) this.mClampedEotf.applyAsDouble((double) v[0]);
            v[1] = (float) this.mClampedEotf.applyAsDouble((double) v[1]);
            v[2] = (float) this.mClampedEotf.applyAsDouble((double) v[2]);
            return v;
        }

        public float[] fromLinear(float r, float g, float b) {
            return fromLinear(new float[]{r, g, b});
        }

        public float[] fromLinear(float[] v) {
            v[0] = (float) this.mClampedOetf.applyAsDouble((double) v[0]);
            v[1] = (float) this.mClampedOetf.applyAsDouble((double) v[1]);
            v[2] = (float) this.mClampedOetf.applyAsDouble((double) v[2]);
            return v;
        }

        @Override // android.graphics.ColorSpace
        public float[] toXyz(float[] v) {
            v[0] = (float) this.mClampedEotf.applyAsDouble((double) v[0]);
            v[1] = (float) this.mClampedEotf.applyAsDouble((double) v[1]);
            v[2] = (float) this.mClampedEotf.applyAsDouble((double) v[2]);
            return ColorSpace.mul3x3Float3(this.mTransform, v);
        }

        @Override // android.graphics.ColorSpace
        public float[] fromXyz(float[] v) {
            ColorSpace.mul3x3Float3(this.mInverseTransform, v);
            v[0] = (float) this.mClampedOetf.applyAsDouble((double) v[0]);
            v[1] = (float) this.mClampedOetf.applyAsDouble((double) v[1]);
            v[2] = (float) this.mClampedOetf.applyAsDouble((double) v[2]);
            return v;
        }

        /* access modifiers changed from: private */
        public double clamp(double x) {
            float f = this.mMin;
            if (x >= ((double) f)) {
                f = this.mMax;
                if (x <= ((double) f)) {
                    return x;
                }
            }
            return (double) f;
        }

        @Override // android.graphics.ColorSpace
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass() || !ColorSpace.super.equals(o)) {
                return false;
            }
            Rgb rgb = (Rgb) o;
            if (Float.compare(rgb.mMin, this.mMin) != 0 || Float.compare(rgb.mMax, this.mMax) != 0 || !Arrays.equals(this.mWhitePoint, rgb.mWhitePoint) || !Arrays.equals(this.mPrimaries, rgb.mPrimaries)) {
                return false;
            }
            TransferParameters transferParameters = this.mTransferParameters;
            if (transferParameters != null) {
                return transferParameters.equals(rgb.mTransferParameters);
            }
            if (rgb.mTransferParameters == null) {
                return true;
            }
            if (!this.mOetf.equals(rgb.mOetf)) {
                return false;
            }
            return this.mEotf.equals(rgb.mEotf);
        }

        @Override // android.graphics.ColorSpace
        public int hashCode() {
            int result = ((((ColorSpace.super.hashCode() * 31) + Arrays.hashCode(this.mWhitePoint)) * 31) + Arrays.hashCode(this.mPrimaries)) * 31;
            float f = this.mMin;
            int i = 0;
            int result2 = (result + (f != 0.0f ? Float.floatToIntBits(f) : 0)) * 31;
            float f2 = this.mMax;
            int result3 = (result2 + (f2 != 0.0f ? Float.floatToIntBits(f2) : 0)) * 31;
            TransferParameters transferParameters = this.mTransferParameters;
            if (transferParameters != null) {
                i = transferParameters.hashCode();
            }
            int result4 = result3 + i;
            if (this.mTransferParameters == null) {
                return (((result4 * 31) + this.mOetf.hashCode()) * 31) + this.mEotf.hashCode();
            }
            return result4;
        }

        private static boolean isSrgb(float[] primaries, float[] whitePoint, DoubleUnaryOperator OETF, DoubleUnaryOperator EOTF, float min, float max, int id) {
            if (id == 0) {
                return true;
            }
            if (!(ColorSpace.compare(primaries, ColorSpace.SRGB_PRIMARIES) && ColorSpace.compare(whitePoint, ILLUMINANT_D65) && min == 0.0f && max == 1.0f)) {
                return false;
            }
            Rgb srgb = (Rgb) get(Named.SRGB);
            for (double x = 0.0d; x <= 1.0d; x += 0.00392156862745098d) {
                if (!(compare(x, OETF, srgb.mOetf) && compare(x, EOTF, srgb.mEotf))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean compare(double point, DoubleUnaryOperator a, DoubleUnaryOperator b) {
            return Math.abs(a.applyAsDouble(point) - b.applyAsDouble(point)) <= 0.001d;
        }

        private static boolean isWideGamut(float[] primaries, float min, float max) {
            return (area(primaries) / area(ColorSpace.NTSC_1953_PRIMARIES) > 0.9f && contains(primaries, ColorSpace.SRGB_PRIMARIES)) || (min < 0.0f && max > 1.0f);
        }

        private static float area(float[] primaries) {
            float Rx = primaries[0];
            float Ry = primaries[1];
            float Gx = primaries[2];
            float Gy = primaries[3];
            float Bx = primaries[4];
            float By = primaries[5];
            float r = 0.5f * ((((((Rx * Gy) + (Ry * Bx)) + (Gx * By)) - (Gy * Bx)) - (Ry * Gx)) - (Rx * By));
            return r < 0.0f ? -r : r;
        }

        private static float cross(float ax, float ay, float bx, float by) {
            return (ax * by) - (ay * bx);
        }

        private static boolean contains(float[] p1, float[] p2) {
            float[] p0 = {p1[0] - p2[0], p1[1] - p2[1], p1[2] - p2[2], p1[3] - p2[3], p1[4] - p2[4], p1[5] - p2[5]};
            return cross(p0[0], p0[1], p2[0] - p2[4], p2[1] - p2[5]) >= 0.0f && cross(p2[0] - p2[2], p2[1] - p2[3], p0[0], p0[1]) >= 0.0f && cross(p0[2], p0[3], p2[2] - p2[0], p2[3] - p2[1]) >= 0.0f && cross(p2[2] - p2[4], p2[3] - p2[5], p0[2], p0[3]) >= 0.0f && cross(p0[4], p0[5], p2[4] - p2[2], p2[5] - p2[3]) >= 0.0f && cross(p2[4] - p2[0], p2[5] - p2[1], p0[4], p0[5]) >= 0.0f;
        }

        private static float[] computePrimaries(float[] toXYZ) {
            float[] r = ColorSpace.mul3x3Float3(toXYZ, new float[]{1.0f, 0.0f, 0.0f});
            float[] g = ColorSpace.mul3x3Float3(toXYZ, new float[]{0.0f, 1.0f, 0.0f});
            float[] b = ColorSpace.mul3x3Float3(toXYZ, new float[]{0.0f, 0.0f, 1.0f});
            float rSum = r[0] + r[1] + r[2];
            float gSum = g[0] + g[1] + g[2];
            float bSum = b[0] + b[1] + b[2];
            return new float[]{r[0] / rSum, r[1] / rSum, g[0] / gSum, g[1] / gSum, b[0] / bSum, b[1] / bSum};
        }

        private static float[] computeWhitePoint(float[] toXYZ) {
            float[] w = ColorSpace.mul3x3Float3(toXYZ, new float[]{1.0f, 1.0f, 1.0f});
            float sum = w[0] + w[1] + w[2];
            return new float[]{w[0] / sum, w[1] / sum};
        }

        private static float[] xyPrimaries(float[] primaries) {
            float[] xyPrimaries = new float[6];
            if (primaries.length == 9) {
                float sum = primaries[0] + primaries[1] + primaries[2];
                xyPrimaries[0] = primaries[0] / sum;
                xyPrimaries[1] = primaries[1] / sum;
                float sum2 = primaries[3] + primaries[4] + primaries[5];
                xyPrimaries[2] = primaries[3] / sum2;
                xyPrimaries[3] = primaries[4] / sum2;
                float sum3 = primaries[6] + primaries[7] + primaries[8];
                xyPrimaries[4] = primaries[6] / sum3;
                xyPrimaries[5] = primaries[7] / sum3;
            } else {
                System.arraycopy(primaries, 0, xyPrimaries, 0, 6);
            }
            return xyPrimaries;
        }

        private static float[] xyWhitePoint(float[] whitePoint) {
            float[] xyWhitePoint = new float[2];
            if (whitePoint.length == 3) {
                float sum = whitePoint[0] + whitePoint[1] + whitePoint[2];
                xyWhitePoint[0] = whitePoint[0] / sum;
                xyWhitePoint[1] = whitePoint[1] / sum;
            } else {
                System.arraycopy(whitePoint, 0, xyWhitePoint, 0, 2);
            }
            return xyWhitePoint;
        }

        private static float[] computeXYZMatrix(float[] primaries, float[] whitePoint) {
            float Rx = primaries[0];
            float Ry = primaries[1];
            float Gx = primaries[2];
            float Gy = primaries[3];
            float Bx = primaries[4];
            float By = primaries[5];
            float Wx = whitePoint[0];
            float Wy = whitePoint[1];
            float oneRxRy = (1.0f - Rx) / Ry;
            float oneGxGy = (1.0f - Gx) / Gy;
            float RxRy = Rx / Ry;
            float GxGy = Gx / Gy;
            float BxBy = Bx / By;
            float WxWy = Wx / Wy;
            float BY = (((((1.0f - Wx) / Wy) - oneRxRy) * (GxGy - RxRy)) - ((WxWy - RxRy) * (oneGxGy - oneRxRy))) / (((((1.0f - Bx) / By) - oneRxRy) * (GxGy - RxRy)) - ((BxBy - RxRy) * (oneGxGy - oneRxRy)));
            float GY = ((WxWy - RxRy) - ((BxBy - RxRy) * BY)) / (GxGy - RxRy);
            float RY = (1.0f - GY) - BY;
            float RYRy = RY / Ry;
            float GYGy = GY / Gy;
            float BYBy = BY / By;
            return new float[]{RYRy * Rx, RY, ((1.0f - Rx) - Ry) * RYRy, GYGy * Gx, GY, ((1.0f - Gx) - Gy) * GYGy, BYBy * Bx, BY, ((1.0f - Bx) - By) * BYBy};
        }
    }

    public static class Connector {
        private final ColorSpace mDestination;
        private final RenderIntent mIntent;
        private final ColorSpace mSource;
        private final float[] mTransform;
        private final ColorSpace mTransformDestination;
        private final ColorSpace mTransformSource;

        /* JADX INFO: this call moved to the top of the method (can break code semantics) */
        Connector(ColorSpace source, ColorSpace destination, RenderIntent intent) {
            this(source, destination, source.getModel() == Model.RGB ? ColorSpace.adapt(source, ColorSpace.ILLUMINANT_D50_XYZ) : source, destination.getModel() == Model.RGB ? ColorSpace.adapt(destination, ColorSpace.ILLUMINANT_D50_XYZ) : destination, intent, computeTransform(source, destination, intent));
        }

        private Connector(ColorSpace source, ColorSpace destination, ColorSpace transformSource, ColorSpace transformDestination, RenderIntent intent, float[] transform) {
            this.mSource = source;
            this.mDestination = destination;
            this.mTransformSource = transformSource;
            this.mTransformDestination = transformDestination;
            this.mIntent = intent;
            this.mTransform = transform;
        }

        private static float[] computeTransform(ColorSpace source, ColorSpace destination, RenderIntent intent) {
            if (intent != RenderIntent.ABSOLUTE) {
                return null;
            }
            boolean srcRGB = source.getModel() == Model.RGB;
            boolean dstRGB = destination.getModel() == Model.RGB;
            if (srcRGB && dstRGB) {
                return null;
            }
            if (!srcRGB && !dstRGB) {
                return null;
            }
            Rgb rgb = (Rgb) (srcRGB ? source : destination);
            float[] srcXYZ = srcRGB ? ColorSpace.xyYToXyz(rgb.mWhitePoint) : ColorSpace.ILLUMINANT_D50_XYZ;
            float[] dstXYZ = dstRGB ? ColorSpace.xyYToXyz(rgb.mWhitePoint) : ColorSpace.ILLUMINANT_D50_XYZ;
            return new float[]{srcXYZ[0] / dstXYZ[0], srcXYZ[1] / dstXYZ[1], srcXYZ[2] / dstXYZ[2]};
        }

        public ColorSpace getSource() {
            return this.mSource;
        }

        public ColorSpace getDestination() {
            return this.mDestination;
        }

        public RenderIntent getRenderIntent() {
            return this.mIntent;
        }

        public float[] transform(float r, float g, float b) {
            return transform(new float[]{r, g, b});
        }

        public float[] transform(float[] v) {
            float[] xyz = this.mTransformSource.toXyz(v);
            float[] fArr = this.mTransform;
            if (fArr != null) {
                xyz[0] = xyz[0] * fArr[0];
                xyz[1] = xyz[1] * fArr[1];
                xyz[2] = xyz[2] * fArr[2];
            }
            return this.mTransformDestination.fromXyz(xyz);
        }

        /* access modifiers changed from: private */
        public static class Rgb extends Connector {
            private final Rgb mDestination;
            private final Rgb mSource;
            private final float[] mTransform;

            Rgb(Rgb source, Rgb destination, RenderIntent intent) {
                super(destination, source, destination, intent, null);
                this.mSource = source;
                this.mDestination = destination;
                this.mTransform = computeTransform(source, destination, intent);
            }

            @Override // android.graphics.ColorSpace.Connector
            public float[] transform(float[] rgb) {
                rgb[0] = (float) this.mSource.mClampedEotf.applyAsDouble((double) rgb[0]);
                rgb[1] = (float) this.mSource.mClampedEotf.applyAsDouble((double) rgb[1]);
                rgb[2] = (float) this.mSource.mClampedEotf.applyAsDouble((double) rgb[2]);
                ColorSpace.mul3x3Float3(this.mTransform, rgb);
                rgb[0] = (float) this.mDestination.mClampedOetf.applyAsDouble((double) rgb[0]);
                rgb[1] = (float) this.mDestination.mClampedOetf.applyAsDouble((double) rgb[1]);
                rgb[2] = (float) this.mDestination.mClampedOetf.applyAsDouble((double) rgb[2]);
                return rgb;
            }

            private static float[] computeTransform(Rgb source, Rgb destination, RenderIntent intent) {
                if (ColorSpace.compare(source.mWhitePoint, destination.mWhitePoint)) {
                    return ColorSpace.mul3x3(destination.mInverseTransform, source.mTransform);
                }
                float[] transform = source.mTransform;
                float[] inverseTransform = destination.mInverseTransform;
                float[] srcXYZ = ColorSpace.xyYToXyz(source.mWhitePoint);
                float[] dstXYZ = ColorSpace.xyYToXyz(destination.mWhitePoint);
                if (!ColorSpace.compare(source.mWhitePoint, ColorSpace.ILLUMINANT_D50)) {
                    transform = ColorSpace.mul3x3(ColorSpace.chromaticAdaptation(Adaptation.BRADFORD.mTransform, srcXYZ, Arrays.copyOf(ColorSpace.ILLUMINANT_D50_XYZ, 3)), source.mTransform);
                }
                if (!ColorSpace.compare(destination.mWhitePoint, ColorSpace.ILLUMINANT_D50)) {
                    inverseTransform = ColorSpace.inverse3x3(ColorSpace.mul3x3(ColorSpace.chromaticAdaptation(Adaptation.BRADFORD.mTransform, dstXYZ, Arrays.copyOf(ColorSpace.ILLUMINANT_D50_XYZ, 3)), destination.mTransform));
                }
                if (intent == RenderIntent.ABSOLUTE) {
                    transform = ColorSpace.mul3x3Diag(new float[]{srcXYZ[0] / dstXYZ[0], srcXYZ[1] / dstXYZ[1], srcXYZ[2] / dstXYZ[2]}, transform);
                }
                return ColorSpace.mul3x3(inverseTransform, transform);
            }
        }

        static Connector identity(ColorSpace source) {
            return new Connector(source, source, RenderIntent.RELATIVE) {
                /* class android.graphics.ColorSpace.Connector.AnonymousClass1 */

                @Override // android.graphics.ColorSpace.Connector
                public float[] transform(float[] v) {
                    return v;
                }
            };
        }
    }

    public static class Renderer {
        private static final int CHROMATICITY_RESOLUTION = 32;
        private static final int NATIVE_SIZE = 1440;
        private static final double ONE_THIRD = 0.3333333333333333d;
        private static final float[] SPECTRUM_LOCUS_X = {0.175596f, 0.172787f, 0.170806f, 0.170085f, 0.160343f, 0.146958f, 0.139149f, 0.133536f, 0.126688f, 0.11583f, 0.109616f, 0.099146f, 0.09131f, 0.07813f, 0.068717f, 0.054675f, 0.040763f, 0.027497f, 0.01627f, 0.008169f, 0.004876f, 0.003983f, 0.003859f, 0.004646f, 0.007988f, 0.01387f, 0.022244f, 0.027273f, 0.03282f, 0.038851f, 0.045327f, 0.052175f, 0.059323f, 0.066713f, 0.074299f, 0.089937f, 0.114155f, 0.138695f, 0.154714f, 0.192865f, 0.229607f, 0.26576f, 0.301588f, 0.337346f, 0.373083f, 0.408717f, 0.444043f, 0.478755f, 0.512467f, 0.544767f, 0.575132f, 0.602914f, 0.627018f, 0.648215f, 0.665746f, 0.680061f, 0.691487f, 0.700589f, 0.707901f, 0.714015f, 0.719017f, 0.723016f, 0.734674f, 0.717203f, 0.699732f, 0.68226f, 0.664789f, 0.647318f, 0.629847f, 0.612376f, 0.594905f, 0.577433f, 0.559962f, 0.542491f, 0.52502f, 0.507549f, 0.490077f, 0.472606f, 0.455135f, 0.437664f, 0.420193f, 0.402721f, 0.38525f, 0.367779f, 0.350308f, 0.332837f, 0.315366f, 0.297894f, 0.280423f, 0.262952f, 0.245481f, 0.22801f, 0.210538f, 0.193067f, 0.175596f};
        private static final float[] SPECTRUM_LOCUS_Y = {0.005295f, 0.0048f, 0.005472f, 0.005976f, 0.014496f, 0.026643f, 0.035211f, 0.042704f, 0.053441f, 0.073601f, 0.086866f, 0.112037f, 0.132737f, 0.170464f, 0.200773f, 0.254155f, 0.317049f, 0.387997f, 0.463035f, 0.538504f, 0.587196f, 0.610526f, 0.654897f, 0.67597f, 0.715407f, 0.750246f, 0.779682f, 0.792153f, 0.802971f, 0.812059f, 0.81943f, 0.8252f, 0.82946f, 0.832306f, 0.833833f, 0.833316f, 0.826231f, 0.814796f, 0.805884f, 0.781648f, 0.754347f, 0.724342f, 0.692326f, 0.658867f, 0.62447f, 0.589626f, 0.554734f, 0.520222f, 0.486611f, 0.454454f, 0.424252f, 0.396516f, 0.37251f, 0.351413f, 0.334028f, 0.319765f, 0.308359f, 0.299317f, 0.292044f, 0.285945f, 0.280951f, 0.276964f, 0.265326f, 0.2572f, 0.249074f, 0.240948f, 0.232822f, 0.224696f, 0.21657f, 0.208444f, 0.200318f, 0.192192f, 0.184066f, 0.17594f, 0.167814f, 0.159688f, 0.151562f, 0.143436f, 0.135311f, 0.127185f, 0.119059f, 0.110933f, 0.102807f, 0.094681f, 0.086555f, 0.078429f, 0.070303f, 0.062177f, 0.054051f, 0.045925f, 0.037799f, 0.029673f, 0.021547f, 0.013421f, 0.005295f};
        private static final float UCS_SCALE = 1.5f;
        private boolean mClip;
        private final List<Pair<ColorSpace, Integer>> mColorSpaces;
        private final List<Point> mPoints;
        private boolean mShowWhitePoint;
        private int mSize;
        private boolean mUcs;

        private Renderer() {
            this.mSize = 1024;
            this.mShowWhitePoint = true;
            this.mClip = false;
            this.mUcs = false;
            this.mColorSpaces = new ArrayList(2);
            this.mPoints = new ArrayList(0);
        }

        public Renderer clip(boolean clip) {
            this.mClip = clip;
            return this;
        }

        public Renderer uniformChromaticityScale(boolean ucs) {
            this.mUcs = ucs;
            return this;
        }

        public Renderer size(int size) {
            this.mSize = Math.max(128, size);
            return this;
        }

        public Renderer showWhitePoint(boolean show) {
            this.mShowWhitePoint = show;
            return this;
        }

        public Renderer add(ColorSpace colorSpace, int color) {
            this.mColorSpaces.add(new Pair<>(colorSpace, Integer.valueOf(color)));
            return this;
        }

        public Renderer add(ColorSpace colorSpace, float r, float g, float b, int pointColor) {
            this.mPoints.add(new Point(colorSpace, new float[]{r, g, b}, pointColor));
            return this;
        }

        public Bitmap render() {
            Paint paint = new Paint(1);
            int i = this.mSize;
            Bitmap bitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            float[] primaries = new float[6];
            Path path = new Path();
            setTransform(canvas, 1440, 1440, primaries);
            drawBox(canvas, 1440, 1440, paint, path);
            setUcsTransform(canvas, 1440);
            drawLocus(canvas, 1440, 1440, paint, path, primaries);
            drawGamuts(canvas, 1440, 1440, paint, path, primaries, new float[2]);
            drawPoints(canvas, 1440, 1440, paint);
            return bitmap;
        }

        private void drawPoints(Canvas canvas, int width, int height, Paint paint) {
            paint.setStyle(Paint.Style.FILL);
            float radius = 4.0f / (this.mUcs ? UCS_SCALE : 1.0f);
            float[] v = new float[3];
            float[] xy = new float[2];
            for (Point point : this.mPoints) {
                v[0] = point.mRgb[0];
                v[1] = point.mRgb[1];
                v[2] = point.mRgb[2];
                point.mColorSpace.toXyz(v);
                paint.setColor(point.mColor);
                float sum = v[0] + v[1] + v[2];
                xy[0] = v[0] / sum;
                xy[1] = v[1] / sum;
                if (this.mUcs) {
                    ColorSpace.xyYToUv(xy);
                }
                canvas.drawCircle(((float) width) * xy[0], ((float) height) - (((float) height) * xy[1]), radius, paint);
            }
        }

        private void drawGamuts(Canvas canvas, int width, int height, Paint paint, Path path, float[] primaries, float[] whitePoint) {
            float radius = 4.0f / (this.mUcs ? UCS_SCALE : 1.0f);
            Iterator<Pair<ColorSpace, Integer>> it = this.mColorSpaces.iterator();
            while (it.hasNext()) {
                Pair<ColorSpace, Integer> item = it.next();
                ColorSpace colorSpace = item.first;
                int color = item.second.intValue();
                if (colorSpace.getModel() == Model.RGB) {
                    Rgb rgb = (Rgb) colorSpace;
                    getPrimaries(rgb, primaries, this.mUcs);
                    path.rewind();
                    path.moveTo(((float) width) * primaries[0], ((float) height) - (((float) height) * primaries[1]));
                    path.lineTo(((float) width) * primaries[2], ((float) height) - (((float) height) * primaries[3]));
                    path.lineTo(((float) width) * primaries[4], ((float) height) - (((float) height) * primaries[5]));
                    path.close();
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(color);
                    canvas.drawPath(path, paint);
                    if (this.mShowWhitePoint) {
                        rgb.getWhitePoint(whitePoint);
                        if (this.mUcs) {
                            ColorSpace.xyYToUv(whitePoint);
                        }
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(color);
                        canvas.drawCircle(((float) width) * whitePoint[0], ((float) height) - (((float) height) * whitePoint[1]), radius, paint);
                    }
                    it = it;
                }
            }
        }

        private static void getPrimaries(Rgb rgb, float[] primaries, boolean asUcs) {
            if (rgb.equals(ColorSpace.get(Named.EXTENDED_SRGB)) || rgb.equals(ColorSpace.get(Named.LINEAR_EXTENDED_SRGB))) {
                primaries[0] = 1.41f;
                primaries[1] = 0.33f;
                primaries[2] = 0.27f;
                primaries[3] = 1.24f;
                primaries[4] = -0.23f;
                primaries[5] = -0.57f;
            } else {
                rgb.getPrimaries(primaries);
            }
            if (asUcs) {
                ColorSpace.xyYToUv(primaries);
            }
        }

        private void drawLocus(Canvas canvas, int width, int height, Paint paint, Path path, float[] primaries) {
            float[] vertices;
            Path path2;
            float[] vertices2 = new float[(SPECTRUM_LOCUS_X.length * 32 * 6 * 2)];
            int[] colors = new int[vertices2.length];
            computeChromaticityMesh(vertices2, colors);
            if (this.mUcs) {
                ColorSpace.xyYToUv(vertices2);
            }
            for (int i = 0; i < vertices2.length; i += 2) {
                vertices2[i] = vertices2[i] * ((float) width);
                vertices2[i + 1] = ((float) height) - (vertices2[i + 1] * ((float) height));
            }
            if (!this.mClip || this.mColorSpaces.size() <= 0) {
                path2 = path;
                vertices = vertices2;
                canvas.drawVertices(Canvas.VertexMode.TRIANGLES, vertices2.length, vertices2, 0, null, 0, colors, 0, null, 0, 0, paint);
            } else {
                Iterator<Pair<ColorSpace, Integer>> it = this.mColorSpaces.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ColorSpace colorSpace = it.next().first;
                    if (colorSpace.getModel() == Model.RGB) {
                        getPrimaries((Rgb) colorSpace, primaries, this.mUcs);
                        break;
                    }
                }
                path.rewind();
                path.moveTo(((float) width) * primaries[0], ((float) height) - (((float) height) * primaries[1]));
                path.lineTo(((float) width) * primaries[2], ((float) height) - (((float) height) * primaries[3]));
                path.lineTo(((float) width) * primaries[4], ((float) height) - (((float) height) * primaries[5]));
                path.close();
                int[] solid = new int[colors.length];
                Arrays.fill(solid, -9671572);
                canvas.drawVertices(Canvas.VertexMode.TRIANGLES, vertices2.length, vertices2, 0, null, 0, solid, 0, null, 0, 0, paint);
                canvas.save();
                canvas.clipPath(path);
                path2 = path;
                canvas.drawVertices(Canvas.VertexMode.TRIANGLES, vertices2.length, vertices2, 0, null, 0, colors, 0, null, 0, 0, paint);
                canvas.restore();
                vertices = vertices2;
            }
            int index = 372;
            path.reset();
            path2.moveTo(vertices[372], vertices[372 + 1]);
            for (int x = 2; x < SPECTRUM_LOCUS_X.length; x++) {
                index += 384;
                path2.lineTo(vertices[index], vertices[index + 1]);
            }
            path.close();
            paint.setStrokeWidth(4.0f / (this.mUcs ? UCS_SCALE : 1.0f));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(-16777216);
            canvas.drawPath(path2, paint);
        }

        private void drawBox(Canvas canvas, int width, int height, Paint paint, Path path) {
            float scale;
            int lineCount;
            if (this.mUcs) {
                lineCount = 7;
                scale = 1.5f;
            } else {
                lineCount = 10;
                scale = 1.0f;
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            paint.setColor(-4144960);
            for (int i = 1; i < lineCount - 1; i++) {
                float v = ((float) i) / 10.0f;
                float x = ((float) width) * v * scale;
                float y = ((float) height) - ((((float) height) * v) * scale);
                canvas.drawLine(0.0f, y, ((float) width) * 0.9f, y, paint);
                canvas.drawLine(x, (float) height, x, ((float) height) * 0.1f, paint);
            }
            paint.setStrokeWidth(4.0f);
            paint.setColor(-16777216);
            for (int i2 = 1; i2 < lineCount - 1; i2++) {
                float v2 = ((float) i2) / 10.0f;
                float x2 = ((float) width) * v2 * scale;
                float y2 = ((float) height) - ((((float) height) * v2) * scale);
                canvas.drawLine(0.0f, y2, ((float) width) / 100.0f, y2, paint);
                canvas.drawLine(x2, (float) height, x2, ((float) height) - (((float) height) / 100.0f), paint);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(36.0f);
            int i3 = 0;
            paint.setTypeface(Typeface.create("sans-serif-light", 0));
            Rect bounds = new Rect();
            int i4 = 1;
            while (i4 < lineCount - 1) {
                String text = "0." + i4;
                paint.getTextBounds(text, i3, text.length(), bounds);
                float v3 = ((float) i4) / 10.0f;
                canvas.drawText(text, (((float) width) * -0.05f) + 10.0f, (((float) bounds.height()) / 2.0f) + (((float) height) - ((((float) height) * v3) * scale)), paint);
                canvas.drawText(text, ((((float) width) * v3) * scale) - (((float) bounds.width()) / 2.0f), (float) (bounds.height() + height + 16), paint);
                i4++;
                i3 = 0;
            }
            paint.setStyle(Paint.Style.STROKE);
            path.moveTo(0.0f, (float) height);
            path.lineTo(((float) width) * 0.9f, (float) height);
            path.lineTo(((float) width) * 0.9f, ((float) height) * 0.1f);
            path.lineTo(0.0f, ((float) height) * 0.1f);
            path.close();
            canvas.drawPath(path, paint);
        }

        private void setTransform(Canvas canvas, int width, int height, float[] primaries) {
            RectF primariesBounds = new RectF();
            for (Pair<ColorSpace, Integer> item : this.mColorSpaces) {
                ColorSpace colorSpace = item.first;
                if (colorSpace.getModel() == Model.RGB) {
                    getPrimaries((Rgb) colorSpace, primaries, this.mUcs);
                    primariesBounds.left = Math.min(primariesBounds.left, primaries[4]);
                    primariesBounds.top = Math.min(primariesBounds.top, primaries[5]);
                    primariesBounds.right = Math.max(primariesBounds.right, primaries[0]);
                    primariesBounds.bottom = Math.max(primariesBounds.bottom, primaries[3]);
                }
            }
            float max = this.mUcs ? 0.6f : 0.9f;
            primariesBounds.left = Math.min(0.0f, primariesBounds.left);
            primariesBounds.top = Math.min(0.0f, primariesBounds.top);
            primariesBounds.right = Math.max(max, primariesBounds.right);
            primariesBounds.bottom = Math.max(max, primariesBounds.bottom);
            float scale = Math.min(max / primariesBounds.width(), max / primariesBounds.height());
            int i = this.mSize;
            canvas.scale(((float) i) / 1440.0f, ((float) i) / 1440.0f);
            canvas.scale(scale, scale);
            canvas.translate(((primariesBounds.width() - max) * ((float) width)) / 2.0f, ((primariesBounds.height() - max) * ((float) height)) / 2.0f);
            canvas.translate(((float) width) * 0.05f, ((float) height) * -0.05f);
        }

        private void setUcsTransform(Canvas canvas, int height) {
            if (this.mUcs) {
                canvas.translate(0.0f, ((float) height) - (((float) height) * UCS_SCALE));
                canvas.scale(UCS_SCALE, UCS_SCALE);
            }
        }

        private static void computeChromaticityMesh(float[] vertices, int[] colors) {
            ColorSpace colorSpace = ColorSpace.get(Named.SRGB);
            float[] color = new float[3];
            int vertexIndex = 0;
            int colorIndex = 0;
            int x = 0;
            while (true) {
                float[] fArr = SPECTRUM_LOCUS_X;
                if (x < fArr.length) {
                    int nextX = (x % (fArr.length - 1)) + 1;
                    float a1 = (float) Math.atan2(((double) SPECTRUM_LOCUS_Y[x]) - ONE_THIRD, ((double) fArr[x]) - ONE_THIRD);
                    float a2 = (float) Math.atan2(((double) SPECTRUM_LOCUS_Y[nextX]) - ONE_THIRD, ((double) SPECTRUM_LOCUS_X[nextX]) - ONE_THIRD);
                    float radius1 = (float) Math.pow(sqr(((double) SPECTRUM_LOCUS_X[x]) - ONE_THIRD) + sqr(((double) SPECTRUM_LOCUS_Y[x]) - ONE_THIRD), 0.5d);
                    float radius2 = (float) Math.pow(sqr(((double) SPECTRUM_LOCUS_X[nextX]) - ONE_THIRD) + sqr(((double) SPECTRUM_LOCUS_Y[nextX]) - ONE_THIRD), 0.5d);
                    int c = 1;
                    while (c <= 32) {
                        float f1 = ((float) c) / 32.0f;
                        float f2 = ((float) (c - 1)) / 32.0f;
                        double cr1 = ((double) radius1) * Math.cos((double) a1);
                        double sr1 = ((double) radius1) * Math.sin((double) a1);
                        double cr2 = ((double) radius2) * Math.cos((double) a2);
                        double sr2 = ((double) radius2) * Math.sin((double) a2);
                        float v1x = (float) ((((double) f1) * cr1) + ONE_THIRD);
                        float v1y = (float) ((((double) f1) * sr1) + ONE_THIRD);
                        float v1z = (1.0f - v1x) - v1y;
                        float v2x = (float) ((((double) f2) * cr1) + ONE_THIRD);
                        float v2y = (float) ((((double) f2) * sr1) + ONE_THIRD);
                        float v3x = (float) ((((double) f2) * cr2) + ONE_THIRD);
                        float v3y = (float) ((((double) f2) * sr2) + ONE_THIRD);
                        float v4x = (float) ((((double) f1) * cr2) + ONE_THIRD);
                        float v4y = (float) ((((double) f1) * sr2) + ONE_THIRD);
                        colors[colorIndex] = computeColor(color, v1x, v1y, v1z, colorSpace);
                        colors[colorIndex + 1] = computeColor(color, v2x, v2y, (1.0f - v2x) - v2y, colorSpace);
                        colors[colorIndex + 2] = computeColor(color, v3x, v3y, (1.0f - v3x) - v3y, colorSpace);
                        colors[colorIndex + 3] = colors[colorIndex];
                        colors[colorIndex + 4] = colors[colorIndex + 2];
                        colors[colorIndex + 5] = computeColor(color, v4x, v4y, (1.0f - v4x) - v4y, colorSpace);
                        int vertexIndex2 = vertexIndex + 1;
                        vertices[vertexIndex] = v1x;
                        int vertexIndex3 = vertexIndex2 + 1;
                        vertices[vertexIndex2] = v1y;
                        int vertexIndex4 = vertexIndex3 + 1;
                        vertices[vertexIndex3] = v2x;
                        int vertexIndex5 = vertexIndex4 + 1;
                        vertices[vertexIndex4] = v2y;
                        int vertexIndex6 = vertexIndex5 + 1;
                        vertices[vertexIndex5] = v3x;
                        int vertexIndex7 = vertexIndex6 + 1;
                        vertices[vertexIndex6] = v3y;
                        int vertexIndex8 = vertexIndex7 + 1;
                        vertices[vertexIndex7] = v1x;
                        int vertexIndex9 = vertexIndex8 + 1;
                        vertices[vertexIndex8] = v1y;
                        int vertexIndex10 = vertexIndex9 + 1;
                        vertices[vertexIndex9] = v3x;
                        int vertexIndex11 = vertexIndex10 + 1;
                        vertices[vertexIndex10] = v3y;
                        int vertexIndex12 = vertexIndex11 + 1;
                        vertices[vertexIndex11] = v4x;
                        vertices[vertexIndex12] = v4y;
                        c++;
                        color = color;
                        x = x;
                        colorSpace = colorSpace;
                        colorIndex += 6;
                        nextX = nextX;
                        a1 = a1;
                        radius2 = radius2;
                        radius1 = radius1;
                        a2 = a2;
                        vertexIndex = vertexIndex12 + 1;
                    }
                    x++;
                } else {
                    return;
                }
            }
        }

        private static int computeColor(float[] color, float x, float y, float z, ColorSpace cs) {
            color[0] = x;
            color[1] = y;
            color[2] = z;
            cs.fromXyz(color);
            return ((((int) (color[0] * 255.0f)) & 255) << 16) | -16777216 | ((((int) (color[1] * 255.0f)) & 255) << 8) | (((int) (color[2] * 255.0f)) & 255);
        }

        private static double sqr(double v) {
            return v * v;
        }

        /* access modifiers changed from: private */
        public static class Point {
            final int mColor;
            final ColorSpace mColorSpace;
            final float[] mRgb;

            Point(ColorSpace colorSpace, float[] rgb, int color) {
                this.mColorSpace = colorSpace;
                this.mRgb = rgb;
                this.mColor = color;
            }
        }
    }
}
