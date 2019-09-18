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

public abstract class ColorSpace {
    public static final float[] ILLUMINANT_A = {0.44757f, 0.40745f};
    public static final float[] ILLUMINANT_B = {0.34842f, 0.35161f};
    public static final float[] ILLUMINANT_C = {0.31006f, 0.31616f};
    public static final float[] ILLUMINANT_D50 = {0.34567f, 0.3585f};
    /* access modifiers changed from: private */
    public static final float[] ILLUMINANT_D50_XYZ = {0.964212f, 1.0f, 0.825188f};
    public static final float[] ILLUMINANT_D55 = {0.33242f, 0.34743f};
    public static final float[] ILLUMINANT_D60 = {0.32168f, 0.33767f};
    public static final float[] ILLUMINANT_D65 = {0.31271f, 0.32902f};
    public static final float[] ILLUMINANT_D75 = {0.29902f, 0.31485f};
    public static final float[] ILLUMINANT_E = {0.33333f, 0.33333f};
    public static final int MAX_ID = 63;
    public static final int MIN_ID = -1;
    /* access modifiers changed from: private */
    public static final float[] NTSC_1953_PRIMARIES = {0.67f, 0.33f, 0.21f, 0.71f, 0.14f, 0.08f};
    /* access modifiers changed from: private */
    public static final float[] SRGB_PRIMARIES = {0.64f, 0.33f, 0.3f, 0.6f, 0.15f, 0.06f};
    private static final ColorSpace[] sNamedColorSpaces = new ColorSpace[Named.values().length];
    private final int mId;
    private final Model mModel;
    private final String mName;

    public enum Adaptation {
        BRADFORD(new float[]{0.8951f, -0.7502f, 0.0389f, 0.2664f, 1.7135f, -0.0685f, -0.1614f, 0.0367f, 1.0296f}),
        VON_KRIES(new float[]{0.40024f, -0.2263f, 0.0f, 0.7076f, 1.16532f, 0.0f, -0.08081f, 0.0457f, 0.91822f}),
        CIECAT02(new float[]{0.7328f, -0.7036f, 0.003f, 0.4296f, 1.6975f, 0.0136f, -0.1624f, 0.0061f, 0.9834f});
        
        final float[] mTransform;

        private Adaptation(float[] transform) {
            this.mTransform = transform;
        }
    }

    public static class Connector {
        private final ColorSpace mDestination;
        private final RenderIntent mIntent;
        private final ColorSpace mSource;
        private final float[] mTransform;
        private final ColorSpace mTransformDestination;
        private final ColorSpace mTransformSource;

        private static class Rgb extends Connector {
            private final Rgb mDestination;
            private final Rgb mSource;
            private final float[] mTransform;

            Rgb(Rgb source, Rgb destination, RenderIntent intent) {
                super(destination, source, destination, intent, null);
                this.mSource = source;
                this.mDestination = destination;
                this.mTransform = computeTransform(source, destination, intent);
            }

            public float[] transform(float[] rgb) {
                rgb[0] = (float) this.mSource.mClampedEotf.applyAsDouble((double) rgb[0]);
                rgb[1] = (float) this.mSource.mClampedEotf.applyAsDouble((double) rgb[1]);
                rgb[2] = (float) this.mSource.mClampedEotf.applyAsDouble((double) rgb[2]);
                float[] unused = ColorSpace.mul3x3Float3(this.mTransform, rgb);
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

        /* JADX WARNING: Illegal instructions before constructor call */
        Connector(ColorSpace source, ColorSpace destination, RenderIntent intent) {
            this(source, destination, r4, r5, intent, computeTransform(source, destination, intent));
            ColorSpace colorSpace;
            ColorSpace colorSpace2;
            if (source.getModel() == Model.RGB) {
                colorSpace = ColorSpace.adapt(source, ColorSpace.ILLUMINANT_D50_XYZ);
            } else {
                colorSpace = source;
            }
            if (destination.getModel() == Model.RGB) {
                colorSpace2 = ColorSpace.adapt(destination, ColorSpace.ILLUMINANT_D50_XYZ);
            } else {
                colorSpace2 = destination;
            }
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
            if (this.mTransform != null) {
                xyz[0] = xyz[0] * this.mTransform[0];
                xyz[1] = xyz[1] * this.mTransform[1];
                xyz[2] = xyz[2] * this.mTransform[2];
            }
            return this.mTransformDestination.fromXyz(xyz);
        }

        static Connector identity(ColorSpace source) {
            return new Connector(source, source, RenderIntent.RELATIVE) {
                public float[] transform(float[] v) {
                    return v;
                }
            };
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

        public boolean isWideGamut() {
            return true;
        }

        public float getMinValue(int component) {
            return component == 0 ? 0.0f : -128.0f;
        }

        public float getMaxValue(int component) {
            return component == 0 ? 100.0f : 128.0f;
        }

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

        public float[] fromXyz(float[] v) {
            float X = v[0] / ColorSpace.ILLUMINANT_D50_XYZ[0];
            float Y = v[1] / ColorSpace.ILLUMINANT_D50_XYZ[1];
            float Z = v[2] / ColorSpace.ILLUMINANT_D50_XYZ[2];
            float fx = X > A ? (float) Math.pow((double) X, 0.3333333333333333d) : (B * X) + C;
            float fy = Y > A ? (float) Math.pow((double) Y, 0.3333333333333333d) : (B * Y) + C;
            float fz = Z > A ? (float) Math.pow((double) Z, 0.3333333333333333d) : (B * Z) + C;
            v[0] = clamp((116.0f * fy) - 16.0f, 0.0f, 100.0f);
            v[1] = clamp(500.0f * (fx - fy), -128.0f, 128.0f);
            v[2] = clamp(200.0f * (fy - fz), -128.0f, 128.0f);
            return v;
        }

        private static float clamp(float x, float min, float max) {
            if (x < min) {
                return min;
            }
            return x > max ? max : x;
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

        private static class Point {
            final int mColor;
            final ColorSpace mColorSpace;
            final float[] mRgb;

            Point(ColorSpace colorSpace, float[] rgb, int color) {
                this.mColorSpace = colorSpace;
                this.mRgb = rgb;
                this.mColor = color;
            }
        }

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
            this.mColorSpaces.add(new Pair(colorSpace, Integer.valueOf(color)));
            return this;
        }

        public Renderer add(ColorSpace colorSpace, float r, float g, float b, int pointColor) {
            this.mPoints.add(new Point(colorSpace, new float[]{r, g, b}, pointColor));
            return this;
        }

        public Bitmap render() {
            Paint paint = new Paint(1);
            Bitmap bitmap = Bitmap.createBitmap(this.mSize, this.mSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            float[] primaries = new float[6];
            Path path = new Path();
            setTransform(canvas, NATIVE_SIZE, NATIVE_SIZE, primaries);
            drawBox(canvas, NATIVE_SIZE, NATIVE_SIZE, paint, path);
            setUcsTransform(canvas, NATIVE_SIZE);
            Canvas canvas2 = canvas;
            int height = NATIVE_SIZE;
            int height2 = NATIVE_SIZE;
            int width = NATIVE_SIZE;
            int width2 = height;
            Paint paint2 = paint;
            float[] primaries2 = primaries;
            Path path2 = path;
            Canvas canvas3 = canvas;
            float[] fArr = primaries2;
            drawLocus(canvas2, height2, width2, paint2, path2, fArr);
            Bitmap bitmap2 = bitmap;
            drawGamuts(canvas3, width, width2, paint2, path2, fArr, new float[2]);
            drawPoints(canvas3, width, height, paint);
            return bitmap2;
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
            Canvas canvas2 = canvas;
            int i = width;
            int i2 = height;
            Paint paint2 = paint;
            Path path2 = path;
            float[] fArr = primaries;
            float[] fArr2 = whitePoint;
            float radius = 4.0f / (this.mUcs ? UCS_SCALE : 1.0f);
            Iterator<Pair<ColorSpace, Integer>> it = this.mColorSpaces.iterator();
            while (it.hasNext()) {
                Pair<ColorSpace, Integer> item = it.next();
                ColorSpace colorSpace = (ColorSpace) item.first;
                int color = ((Integer) item.second).intValue();
                if (colorSpace.getModel() == Model.RGB) {
                    Rgb rgb = (Rgb) colorSpace;
                    getPrimaries(rgb, fArr, this.mUcs);
                    path.rewind();
                    Iterator<Pair<ColorSpace, Integer>> it2 = it;
                    path2.moveTo(((float) i) * fArr[0], ((float) i2) - (((float) i2) * fArr[1]));
                    path2.lineTo(((float) i) * fArr[2], ((float) i2) - (((float) i2) * fArr[3]));
                    path2.lineTo(((float) i) * fArr[4], ((float) i2) - (((float) i2) * fArr[5]));
                    path.close();
                    paint2.setStyle(Paint.Style.STROKE);
                    paint2.setColor(color);
                    canvas2.drawPath(path2, paint2);
                    if (this.mShowWhitePoint) {
                        rgb.getWhitePoint(fArr2);
                        if (this.mUcs) {
                            ColorSpace.xyYToUv(whitePoint);
                        }
                        paint2.setStyle(Paint.Style.FILL);
                        paint2.setColor(color);
                        canvas2.drawCircle(((float) i) * fArr2[0], ((float) i2) - (((float) i2) * fArr2[1]), radius, paint2);
                    }
                    it = it2;
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
            Canvas canvas2 = canvas;
            int i = width;
            int i2 = height;
            Paint paint2 = paint;
            Path path2 = path;
            float[] fArr = primaries;
            float[] vertices2 = new float[(SPECTRUM_LOCUS_X.length * 32 * 6 * 2)];
            int[] colors = new int[vertices2.length];
            computeChromaticityMesh(vertices2, colors);
            if (this.mUcs) {
                ColorSpace.xyYToUv(vertices2);
            }
            for (int i3 = 0; i3 < vertices2.length; i3 += 2) {
                vertices2[i3] = vertices2[i3] * ((float) i);
                vertices2[i3 + 1] = ((float) i2) - (vertices2[i3 + 1] * ((float) i2));
            }
            int x = 2;
            if (this.mClip == 0 || this.mColorSpaces.size() <= 0) {
                float[] vertices3 = vertices2;
                vertices = vertices3;
                canvas2.drawVertices(Canvas.VertexMode.TRIANGLES, vertices3.length, vertices3, 0, null, 0, colors, 0, null, 0, 0, paint);
            } else {
                Iterator<Pair<ColorSpace, Integer>> it = this.mColorSpaces.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ColorSpace colorSpace = (ColorSpace) it.next().first;
                    if (colorSpace.getModel() == Model.RGB) {
                        getPrimaries((Rgb) colorSpace, fArr, this.mUcs);
                        break;
                    }
                }
                path.rewind();
                path2.moveTo(((float) i) * fArr[0], ((float) i2) - (((float) i2) * fArr[1]));
                path2.lineTo(((float) i) * fArr[2], ((float) i2) - (((float) i2) * fArr[3]));
                path2.lineTo(((float) i) * fArr[4], ((float) i2) - (((float) i2) * fArr[5]));
                path.close();
                int[] solid = new int[colors.length];
                Arrays.fill(solid, -9671572);
                Canvas canvas3 = canvas2;
                canvas3.drawVertices(Canvas.VertexMode.TRIANGLES, vertices2.length, vertices2, 0, null, 0, solid, 0, null, 0, 0, paint);
                canvas.save();
                canvas2.clipPath(path);
                float[] vertices4 = vertices2;
                canvas3.drawVertices(Canvas.VertexMode.TRIANGLES, vertices4.length, vertices4, 0, null, 0, colors, 0, null, 0, 0, paint);
                canvas.restore();
                vertices = vertices4;
            }
            int index = 372;
            path.reset();
            Path path3 = path;
            path3.moveTo(vertices[372], vertices[372 + 1]);
            while (true) {
                int x2 = x;
                if (x2 >= SPECTRUM_LOCUS_X.length) {
                    break;
                }
                index += 384;
                path3.lineTo(vertices[index], vertices[index + 1]);
                x = x2 + 1;
            }
            path.close();
            Paint paint3 = paint;
            paint3.setStrokeWidth(4.0f / (this.mUcs ? UCS_SCALE : 1.0f));
            paint3.setStyle(Paint.Style.STROKE);
            paint3.setColor(-16777216);
            canvas2.drawPath(path3, paint3);
        }

        private void drawBox(Canvas canvas, int width, int height, Paint paint, Path path) {
            Canvas canvas2 = canvas;
            int i = width;
            int i2 = height;
            Paint paint2 = paint;
            Path path2 = path;
            int lineCount = 10;
            float scale = 1.0f;
            if (this.mUcs) {
                lineCount = 7;
                scale = UCS_SCALE;
            }
            float scale2 = scale;
            paint2.setStyle(Paint.Style.STROKE);
            paint2.setStrokeWidth(2.0f);
            paint2.setColor(-4144960);
            int i3 = 1;
            int i4 = 1;
            while (true) {
                int i5 = i4;
                if (i5 >= lineCount - 1) {
                    break;
                }
                float v = ((float) i5) / 10.0f;
                float x = ((float) i) * v * scale2;
                float y = ((float) i2) - ((((float) i2) * v) * scale2);
                int i6 = i5;
                Paint paint3 = paint2;
                canvas2.drawLine(0.0f, y, 0.9f * ((float) i), y, paint3);
                canvas2.drawLine(x, (float) i2, x, 0.1f * ((float) i2), paint3);
                i4 = i6 + 1;
            }
            paint2.setStrokeWidth(4.0f);
            paint2.setColor(-16777216);
            int i7 = 1;
            while (true) {
                int i8 = i7;
                if (i8 >= lineCount - 1) {
                    break;
                }
                float v2 = ((float) i8) / 10.0f;
                float x2 = ((float) i) * v2 * scale2;
                float y2 = ((float) i2) - ((((float) i2) * v2) * scale2);
                int i9 = i8;
                Paint paint4 = paint2;
                canvas2.drawLine(0.0f, y2, ((float) i) / 100.0f, y2, paint4);
                canvas2.drawLine(x2, (float) i2, x2, ((float) i2) - (((float) i2) / 100.0f), paint4);
                i7 = i9 + 1;
            }
            paint2.setStyle(Paint.Style.FILL);
            paint2.setTextSize(36.0f);
            int i10 = 0;
            paint2.setTypeface(Typeface.create("sans-serif-light", 0));
            Rect bounds = new Rect();
            while (true) {
                int i11 = i3;
                if (i11 < lineCount - 1) {
                    String text = "0." + i11;
                    paint2.getTextBounds(text, i10, text.length(), bounds);
                    float v3 = ((float) i11) / 10.0f;
                    canvas2.drawText(text, (-0.05f * ((float) i)) + 10.0f, (((float) bounds.height()) / 2.0f) + (((float) i2) - ((((float) i2) * v3) * scale2)), paint2);
                    canvas2.drawText(text, ((((float) i) * v3) * scale2) - (((float) bounds.width()) / 2.0f), (float) (bounds.height() + i2 + 16), paint2);
                    i3 = i11 + 1;
                    i10 = 0;
                } else {
                    paint2.setStyle(Paint.Style.STROKE);
                    path2.moveTo(0.0f, (float) i2);
                    path2.lineTo(((float) i) * 0.9f, (float) i2);
                    path2.lineTo(((float) i) * 0.9f, ((float) i2) * 0.1f);
                    path2.lineTo(0.0f, ((float) i2) * 0.1f);
                    path.close();
                    canvas2.drawPath(path2, paint2);
                    return;
                }
            }
        }

        private void setTransform(Canvas canvas, int width, int height, float[] primaries) {
            RectF primariesBounds = new RectF();
            for (Pair<ColorSpace, Integer> item : this.mColorSpaces) {
                ColorSpace colorSpace = (ColorSpace) item.first;
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
            canvas.scale(((float) this.mSize) / 1440.0f, ((float) this.mSize) / 1440.0f);
            canvas.scale(scale, scale);
            canvas.translate(((primariesBounds.width() - max) * ((float) width)) / 2.0f, ((primariesBounds.height() - max) * ((float) height)) / 2.0f);
            canvas.translate(0.05f * ((float) width), -0.05f * ((float) height));
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
            while (x < SPECTRUM_LOCUS_X.length) {
                int nextX = (x % (SPECTRUM_LOCUS_X.length - 1)) + 1;
                float a1 = (float) Math.atan2(((double) SPECTRUM_LOCUS_Y[x]) - ONE_THIRD, ((double) SPECTRUM_LOCUS_X[x]) - ONE_THIRD);
                float a2 = (float) Math.atan2(((double) SPECTRUM_LOCUS_Y[nextX]) - ONE_THIRD, ((double) SPECTRUM_LOCUS_X[nextX]) - ONE_THIRD);
                float radius1 = (float) Math.pow(sqr(((double) SPECTRUM_LOCUS_X[x]) - ONE_THIRD) + sqr(((double) SPECTRUM_LOCUS_Y[x]) - ONE_THIRD), 0.5d);
                int vertexIndex2 = vertexIndex;
                float radius2 = (float) Math.pow(sqr(((double) SPECTRUM_LOCUS_X[nextX]) - ONE_THIRD) + sqr(((double) SPECTRUM_LOCUS_Y[nextX]) - ONE_THIRD), 0.5d);
                colorIndex = colorIndex;
                int c = 1;
                while (true) {
                    int c2 = c;
                    if (c2 > 32) {
                        break;
                    }
                    float f1 = ((float) c2) / 32.0f;
                    float f2 = ((float) (c2 - 1)) / 32.0f;
                    int x2 = x;
                    int nextX2 = nextX;
                    float a12 = a1;
                    double cr1 = ((double) radius1) * Math.cos((double) a12);
                    float radius12 = radius1;
                    double sr1 = ((double) radius1) * Math.sin((double) a12);
                    double cr2 = ((double) radius2) * Math.cos((double) a2);
                    float radius22 = radius2;
                    int colorIndex2 = colorIndex;
                    double sr2 = ((double) radius2) * Math.sin((double) a2);
                    float v1x = (float) (ONE_THIRD + (((double) f1) * cr1));
                    ColorSpace colorSpace2 = colorSpace;
                    float[] color2 = color;
                    float v1y = (float) (ONE_THIRD + (((double) f1) * sr1));
                    float v1x2 = v1x;
                    float v2x = (float) (ONE_THIRD + (((double) f2) * cr1));
                    double d = cr1;
                    float v2y = (float) (ONE_THIRD + (((double) f2) * sr1));
                    double d2 = sr1;
                    float v3x = (float) (ONE_THIRD + (((double) f2) * cr2));
                    float a22 = a2;
                    float v3y = (float) (ONE_THIRD + (((double) f2) * sr2));
                    float a13 = a12;
                    float f = f2;
                    float v4x = (float) (ONE_THIRD + (((double) f1) * cr2));
                    float v4y = (float) (ONE_THIRD + (((double) f1) * sr2));
                    double d3 = sr2;
                    double d4 = cr2;
                    ColorSpace colorSpace3 = colorSpace2;
                    float[] color3 = color2;
                    float v1x3 = v1x2;
                    colors[colorIndex2] = computeColor(color3, v1x3, v1y, (1.0f - v1x) - v1y, colorSpace3);
                    colors[colorIndex2 + 1] = computeColor(color3, v2x, v2y, (1.0f - v2x) - v2y, colorSpace3);
                    colors[colorIndex2 + 2] = computeColor(color3, v3x, v3y, (1.0f - v3x) - v3y, colorSpace3);
                    colors[colorIndex2 + 3] = colors[colorIndex2];
                    colors[colorIndex2 + 4] = colors[colorIndex2 + 2];
                    colors[colorIndex2 + 5] = computeColor(color3, v4x, v4y, (1.0f - v4x) - v4y, colorSpace3);
                    int vertexIndex3 = vertexIndex2 + 1;
                    vertices[vertexIndex2] = v1x3;
                    int vertexIndex4 = vertexIndex3 + 1;
                    vertices[vertexIndex3] = v1y;
                    int vertexIndex5 = vertexIndex4 + 1;
                    vertices[vertexIndex4] = v2x;
                    int vertexIndex6 = vertexIndex5 + 1;
                    vertices[vertexIndex5] = v2y;
                    int vertexIndex7 = vertexIndex6 + 1;
                    vertices[vertexIndex6] = v3x;
                    int vertexIndex8 = vertexIndex7 + 1;
                    vertices[vertexIndex7] = v3y;
                    int vertexIndex9 = vertexIndex8 + 1;
                    vertices[vertexIndex8] = v1x3;
                    int vertexIndex10 = vertexIndex9 + 1;
                    vertices[vertexIndex9] = v1y;
                    int vertexIndex11 = vertexIndex10 + 1;
                    vertices[vertexIndex10] = v3x;
                    int vertexIndex12 = vertexIndex11 + 1;
                    vertices[vertexIndex11] = v3y;
                    int vertexIndex13 = vertexIndex12 + 1;
                    vertices[vertexIndex12] = v4x;
                    vertexIndex2 = vertexIndex13 + 1;
                    vertices[vertexIndex13] = v4y;
                    c = c2 + 1;
                    colorSpace = colorSpace3;
                    color = color3;
                    colorIndex = colorIndex2 + 6;
                    x = x2;
                    nextX = nextX2;
                    radius1 = radius12;
                    radius2 = radius22;
                    a2 = a22;
                    a1 = a13;
                }
                float[] fArr = color;
                int i = colorIndex;
                x++;
                vertexIndex = vertexIndex2;
            }
            float[] fArr2 = color;
            int i2 = vertexIndex;
            int i3 = colorIndex;
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
    }

    public static class Rgb extends ColorSpace {
        /* access modifiers changed from: private */
        public final DoubleUnaryOperator mClampedEotf;
        /* access modifiers changed from: private */
        public final DoubleUnaryOperator mClampedOetf;
        private final DoubleUnaryOperator mEotf;
        /* access modifiers changed from: private */
        public final float[] mInverseTransform;
        private final boolean mIsSrgb;
        private final boolean mIsWideGamut;
        private final float mMax;
        private final float mMin;
        private final DoubleUnaryOperator mOetf;
        private final float[] mPrimaries;
        /* access modifiers changed from: private */
        public TransferParameters mTransferParameters;
        /* access modifiers changed from: private */
        public final float[] mTransform;
        /* access modifiers changed from: private */
        public final float[] mWhitePoint;

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
                double d3 = a2;
                double d4 = c2;
                double d5 = d2;
                double d6 = g2;
                if (Double.isNaN(a2) || Double.isNaN(b2) || Double.isNaN(c2) || Double.isNaN(d2) || Double.isNaN(e2) || Double.isNaN(f2) || Double.isNaN(g2)) {
                    double d7 = b2;
                    double d8 = e2;
                    double d9 = f2;
                    throw new IllegalArgumentException("Parameters cannot be NaN");
                } else if (d5 < 0.0d || d5 > ((double) (1.0f + Math.ulp(1.0f)))) {
                    double d10 = b2;
                    double d11 = e2;
                    double d12 = f2;
                    throw new IllegalArgumentException("Parameter d must be in the range [0..1], was " + d5);
                } else if (d5 == 0.0d && (d3 == 0.0d || d6 == 0.0d)) {
                    throw new IllegalArgumentException("Parameter a or g is zero, the transfer function is constant");
                } else if (d5 >= 1.0d && d4 == 0.0d) {
                    throw new IllegalArgumentException("Parameter c is zero, the transfer function is constant");
                } else if ((d3 == 0.0d || d6 == 0.0d) && d4 == 0.0d) {
                    double d13 = b2;
                    double d14 = e2;
                    double d15 = f2;
                    throw new IllegalArgumentException("Parameter a or g is zero, and c is zero, the transfer function is constant");
                } else if (d4 < 0.0d) {
                    double d16 = b2;
                    double d17 = e2;
                    double d18 = f2;
                    throw new IllegalArgumentException("The transfer function must be increasing");
                } else if (d3 < 0.0d || d6 < 0.0d) {
                    double d19 = b2;
                    double d20 = e2;
                    double d21 = f2;
                    throw new IllegalArgumentException("The transfer function must be positive or increasing");
                } else {
                    this.a = d3;
                    this.b = b2;
                    this.c = d4;
                    this.d = d5;
                    this.e = e2;
                    this.f = f2;
                    this.g = d6;
                }
            }

            public boolean equals(Object o) {
                boolean z = true;
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
                if (Double.compare(that.g, this.g) != 0) {
                    z = false;
                }
                return z;
            }

            public int hashCode() {
                long temp = Double.doubleToLongBits(this.a);
                long temp2 = Double.doubleToLongBits(this.b);
                int result = (31 * ((int) ((temp >>> 32) ^ temp))) + ((int) ((temp2 >>> 32) ^ temp2));
                long temp3 = Double.doubleToLongBits(this.c);
                long temp4 = Double.doubleToLongBits(this.d);
                int result2 = (31 * ((31 * result) + ((int) ((temp3 >>> 32) ^ temp3)))) + ((int) ((temp4 >>> 32) ^ temp4));
                long temp5 = Double.doubleToLongBits(this.e);
                long temp6 = Double.doubleToLongBits(this.f);
                int result3 = (31 * ((31 * result2) + ((int) ((temp5 >>> 32) ^ temp5)))) + ((int) ((temp6 >>> 32) ^ temp6));
                long temp7 = Double.doubleToLongBits(this.g);
                return (31 * result3) + ((int) ((temp7 >>> 32) ^ temp7));
            }
        }

        public Rgb(String name, float[] toXYZ, DoubleUnaryOperator oetf, DoubleUnaryOperator eotf) {
            this(name, computePrimaries(toXYZ), computeWhitePoint(toXYZ), oetf, eotf, 0.0f, 1.0f, -1);
        }

        public Rgb(String name, float[] primaries, float[] whitePoint, DoubleUnaryOperator oetf, DoubleUnaryOperator eotf, float min, float max) {
            this(name, primaries, whitePoint, oetf, eotf, min, max, -1);
        }

        public Rgb(String name, float[] toXYZ, TransferParameters function) {
            this(name, computePrimaries(toXYZ), computeWhitePoint(toXYZ), function, -1);
        }

        public Rgb(String name, float[] primaries, float[] whitePoint, TransferParameters function) {
            this(name, primaries, whitePoint, function, -1);
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        private Rgb(String name, float[] primaries, float[] whitePoint, TransferParameters function, int id) {
            this(name, primaries, whitePoint, r9, r1, 0.0f, 1.0f, id);
            DoubleUnaryOperator doubleUnaryOperator;
            DoubleUnaryOperator doubleUnaryOperator2;
            TransferParameters transferParameters = function;
            if (transferParameters.e == 0.0d && transferParameters.f == 0.0d) {
                doubleUnaryOperator = new DoubleUnaryOperator() {
                    public final double applyAsDouble(double d) {
                        return ColorSpace.rcpResponse(d, ColorSpace.Rgb.TransferParameters.this.a, ColorSpace.Rgb.TransferParameters.this.b, ColorSpace.Rgb.TransferParameters.this.c, ColorSpace.Rgb.TransferParameters.this.d, ColorSpace.Rgb.TransferParameters.this.g);
                    }
                };
            } else {
                doubleUnaryOperator = new DoubleUnaryOperator() {
                    public final double applyAsDouble(double d) {
                        return ColorSpace.Rgb.lambda$new$1(ColorSpace.Rgb.TransferParameters.this, d);
                    }
                };
            }
            DoubleUnaryOperator doubleUnaryOperator3 = doubleUnaryOperator;
            if (transferParameters.e == 0.0d && transferParameters.f == 0.0d) {
                doubleUnaryOperator2 = new DoubleUnaryOperator() {
                    public final double applyAsDouble(double d) {
                        return ColorSpace.response(d, ColorSpace.Rgb.TransferParameters.this.a, ColorSpace.Rgb.TransferParameters.this.b, ColorSpace.Rgb.TransferParameters.this.c, ColorSpace.Rgb.TransferParameters.this.d, ColorSpace.Rgb.TransferParameters.this.g);
                    }
                };
            } else {
                doubleUnaryOperator2 = new DoubleUnaryOperator() {
                    public final double applyAsDouble(double d) {
                        return ColorSpace.Rgb.lambda$new$3(ColorSpace.Rgb.TransferParameters.this, d);
                    }
                };
            }
            this.mTransferParameters = transferParameters;
        }

        static /* synthetic */ double lambda$new$1(TransferParameters function, double x) {
            TransferParameters transferParameters = function;
            return ColorSpace.rcpResponse(x, transferParameters.a, transferParameters.b, transferParameters.c, transferParameters.d, transferParameters.e, transferParameters.f, transferParameters.g);
        }

        static /* synthetic */ double lambda$new$3(TransferParameters function, double x) {
            TransferParameters transferParameters = function;
            return ColorSpace.response(x, transferParameters.a, transferParameters.b, transferParameters.c, transferParameters.d, transferParameters.e, transferParameters.f, transferParameters.g);
        }

        public Rgb(String name, float[] toXYZ, double gamma) {
            this(name, computePrimaries(toXYZ), computeWhitePoint(toXYZ), gamma, 0.0f, 1.0f, -1);
        }

        public Rgb(String name, float[] primaries, float[] whitePoint, double gamma) {
            this(name, primaries, whitePoint, gamma, 0.0f, 1.0f, -1);
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        private Rgb(String name, float[] primaries, float[] whitePoint, double gamma, float min, float max, int id) {
            this(name, primaries, whitePoint, r17, r2, min, max, id);
            DoubleUnaryOperator r2;
            DoubleUnaryOperator r22;
            TransferParameters transferParameters;
            double d = gamma;
            if (d == 1.0d) {
                r2 = DoubleUnaryOperator.identity();
            } else {
                r2 = new DoubleUnaryOperator(d) {
                    private final /* synthetic */ double f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final double applyAsDouble(double d) {
                        return ColorSpace.Rgb.lambda$new$4(this.f$0, d);
                    }
                };
            }
            DoubleUnaryOperator doubleUnaryOperator = r2;
            if (d == 1.0d) {
                r22 = DoubleUnaryOperator.identity();
            } else {
                r22 = new DoubleUnaryOperator(d) {
                    private final /* synthetic */ double f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final double applyAsDouble(double d) {
                        return ColorSpace.Rgb.lambda$new$5(this.f$0, d);
                    }
                };
            }
            if (d == 1.0d) {
                transferParameters = new TransferParameters(0.0d, 0.0d, 1.0d, ((double) Math.ulp(1.0f)) + 1.0d, d);
            } else {
                transferParameters = new TransferParameters(1.0d, 0.0d, 0.0d, 0.0d, d);
            }
            this.mTransferParameters = transferParameters;
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

        /* JADX WARNING: Illegal instructions before constructor call */
        private Rgb(String name, float[] primaries, float[] whitePoint, DoubleUnaryOperator oetf, DoubleUnaryOperator eotf, float min, float max, int id) {
            super(name, Model.RGB, r15);
            float[] fArr = primaries;
            float[] fArr2 = whitePoint;
            DoubleUnaryOperator doubleUnaryOperator = oetf;
            DoubleUnaryOperator doubleUnaryOperator2 = eotf;
            float f = min;
            float f2 = max;
            int i = id;
            if (fArr == null || !(fArr.length == 6 || fArr.length == 9)) {
                throw new IllegalArgumentException("The color space's primaries must be defined as an array of 6 floats in xyY or 9 floats in XYZ");
            } else if (fArr2 == null || !(fArr2.length == 2 || fArr2.length == 3)) {
                throw new IllegalArgumentException("The color space's white point must be defined as an array of 2 floats in xyY or 3 float in XYZ");
            } else if (doubleUnaryOperator == null || doubleUnaryOperator2 == null) {
                throw new IllegalArgumentException("The transfer functions of a color space cannot be null");
            } else if (f < f2) {
                this.mWhitePoint = xyWhitePoint(whitePoint);
                this.mPrimaries = xyPrimaries(primaries);
                this.mTransform = computeXYZMatrix(this.mPrimaries, this.mWhitePoint);
                this.mInverseTransform = ColorSpace.inverse3x3(this.mTransform);
                this.mOetf = doubleUnaryOperator;
                this.mEotf = doubleUnaryOperator2;
                this.mMin = f;
                this.mMax = f2;
                $$Lambda$ColorSpace$Rgb$8EkhO2jIf14tuA3BvrmYJMa7YXM r9 = new DoubleUnaryOperator() {
                    public final double applyAsDouble(double d) {
                        return ColorSpace.Rgb.this.clamp(d);
                    }
                };
                this.mClampedOetf = doubleUnaryOperator.andThen(r9);
                this.mClampedEotf = r9.andThen(doubleUnaryOperator2);
                this.mIsWideGamut = isWideGamut(this.mPrimaries, f, f2);
                $$Lambda$ColorSpace$Rgb$8EkhO2jIf14tuA3BvrmYJMa7YXM r16 = r9;
                this.mIsSrgb = isSrgb(this.mPrimaries, this.mWhitePoint, doubleUnaryOperator, doubleUnaryOperator2, f, f2, i);
            } else {
                throw new IllegalArgumentException("Invalid range: min=" + f + ", max=" + f2 + "; min must be strictly < max");
            }
        }

        private Rgb(Rgb colorSpace, float[] transform, float[] whitePoint) {
            super(colorSpace.getName(), Model.RGB, -1);
            this.mWhitePoint = xyWhitePoint(whitePoint);
            this.mPrimaries = colorSpace.mPrimaries;
            this.mTransform = transform;
            this.mInverseTransform = ColorSpace.inverse3x3(transform);
            this.mMin = colorSpace.mMin;
            this.mMax = colorSpace.mMax;
            this.mOetf = colorSpace.mOetf;
            this.mEotf = colorSpace.mEotf;
            this.mClampedOetf = colorSpace.mClampedOetf;
            this.mClampedEotf = colorSpace.mClampedEotf;
            this.mIsWideGamut = colorSpace.mIsWideGamut;
            this.mIsSrgb = colorSpace.mIsSrgb;
            this.mTransferParameters = colorSpace.mTransferParameters;
        }

        public float[] getWhitePoint(float[] whitePoint) {
            whitePoint[0] = this.mWhitePoint[0];
            whitePoint[1] = this.mWhitePoint[1];
            return whitePoint;
        }

        public float[] getWhitePoint() {
            return Arrays.copyOf(this.mWhitePoint, this.mWhitePoint.length);
        }

        public float[] getPrimaries(float[] primaries) {
            System.arraycopy(this.mPrimaries, 0, primaries, 0, this.mPrimaries.length);
            return primaries;
        }

        public float[] getPrimaries() {
            return Arrays.copyOf(this.mPrimaries, this.mPrimaries.length);
        }

        public float[] getTransform(float[] transform) {
            System.arraycopy(this.mTransform, 0, transform, 0, this.mTransform.length);
            return transform;
        }

        public float[] getTransform() {
            return Arrays.copyOf(this.mTransform, this.mTransform.length);
        }

        public float[] getInverseTransform(float[] inverseTransform) {
            System.arraycopy(this.mInverseTransform, 0, inverseTransform, 0, this.mInverseTransform.length);
            return inverseTransform;
        }

        public float[] getInverseTransform() {
            return Arrays.copyOf(this.mInverseTransform, this.mInverseTransform.length);
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

        public boolean isSrgb() {
            return this.mIsSrgb;
        }

        public boolean isWideGamut() {
            return this.mIsWideGamut;
        }

        public float getMinValue(int component) {
            return this.mMin;
        }

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

        public float[] toXyz(float[] v) {
            v[0] = (float) this.mClampedEotf.applyAsDouble((double) v[0]);
            v[1] = (float) this.mClampedEotf.applyAsDouble((double) v[1]);
            v[2] = (float) this.mClampedEotf.applyAsDouble((double) v[2]);
            return ColorSpace.mul3x3Float3(this.mTransform, v);
        }

        public float[] fromXyz(float[] v) {
            float[] unused = ColorSpace.mul3x3Float3(this.mInverseTransform, v);
            v[0] = (float) this.mClampedOetf.applyAsDouble((double) v[0]);
            v[1] = (float) this.mClampedOetf.applyAsDouble((double) v[1]);
            v[2] = (float) this.mClampedOetf.applyAsDouble((double) v[2]);
            return v;
        }

        /* access modifiers changed from: private */
        public double clamp(double x) {
            float f;
            if (x < ((double) this.mMin)) {
                f = this.mMin;
            } else if (x <= ((double) this.mMax)) {
                return x;
            } else {
                f = this.mMax;
            }
            return (double) f;
        }

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
            if (this.mTransferParameters != null) {
                return this.mTransferParameters.equals(rgb.mTransferParameters);
            }
            if (rgb.mTransferParameters == null) {
                return true;
            }
            if (!this.mOetf.equals(rgb.mOetf)) {
                return false;
            }
            return this.mEotf.equals(rgb.mEotf);
        }

        public int hashCode() {
            int i = 0;
            int result = 31 * ((31 * ((31 * ((31 * ((31 * ColorSpace.super.hashCode()) + Arrays.hashCode(this.mWhitePoint))) + Arrays.hashCode(this.mPrimaries))) + (this.mMin != 0.0f ? Float.floatToIntBits(this.mMin) : 0))) + (this.mMax != 0.0f ? Float.floatToIntBits(this.mMax) : 0));
            if (this.mTransferParameters != null) {
                i = this.mTransferParameters.hashCode();
            }
            int result2 = result + i;
            if (this.mTransferParameters != null) {
                return result2;
            }
            return this.mEotf.hashCode() + (31 * ((31 * result2) + this.mOetf.hashCode()));
        }

        private static boolean isSrgb(float[] primaries, float[] whitePoint, DoubleUnaryOperator OETF, DoubleUnaryOperator EOTF, float min, float max, int id) {
            if (id == 0) {
                return true;
            }
            if (ColorSpace.compare(primaries, ColorSpace.SRGB_PRIMARIES) && ColorSpace.compare(whitePoint, ILLUMINANT_D65) && OETF.applyAsDouble(0.5d) >= 0.5001d && EOTF.applyAsDouble(0.5d) <= 0.5001d && min == 0.0f && max == 1.0f) {
                return true;
            }
            return false;
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

    private static final class Xyz extends ColorSpace {
        private Xyz(String name, int id) {
            super(name, Model.XYZ, id);
        }

        public boolean isWideGamut() {
            return true;
        }

        public float getMinValue(int component) {
            return -2.0f;
        }

        public float getMaxValue(int component) {
            return 2.0f;
        }

        public float[] toXyz(float[] v) {
            v[0] = clamp(v[0]);
            v[1] = clamp(v[1]);
            v[2] = clamp(v[2]);
            return v;
        }

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

    public abstract float[] fromXyz(float[] fArr);

    public abstract float getMaxValue(int i);

    public abstract float getMinValue(int i);

    public abstract boolean isWideGamut();

    public abstract float[] toXyz(float[] fArr);

    static {
        ColorSpace[] colorSpaceArr = sNamedColorSpaces;
        int ordinal = Named.SRGB.ordinal();
        float[] fArr = SRGB_PRIMARIES;
        float[] fArr2 = ILLUMINANT_D65;
        Rgb.TransferParameters transferParameters = new Rgb.TransferParameters(0.9478672985781991d, 0.05213270142180095d, 0.07739938080495357d, 0.04045d, 2.4d);
        Rgb rgb = new Rgb("sRGB IEC61966-2.1", fArr, fArr2, transferParameters, Named.SRGB.ordinal());
        colorSpaceArr[ordinal] = rgb;
        ColorSpace[] colorSpaceArr2 = sNamedColorSpaces;
        int ordinal2 = Named.LINEAR_SRGB.ordinal();
        Rgb rgb2 = new Rgb("sRGB IEC61966-2.1 (Linear)", SRGB_PRIMARIES, ILLUMINANT_D65, 1.0d, 0.0f, 1.0f, Named.LINEAR_SRGB.ordinal());
        colorSpaceArr2[ordinal2] = rgb2;
        ColorSpace[] colorSpaceArr3 = sNamedColorSpaces;
        int ordinal3 = Named.EXTENDED_SRGB.ordinal();
        Rgb rgb3 = new Rgb("scRGB-nl IEC 61966-2-2:2003", SRGB_PRIMARIES, ILLUMINANT_D65, $$Lambda$ColorSpace$BNp1CyCzsQzfEAds9uc4rJDfw.INSTANCE, $$Lambda$ColorSpace$S2rlqJvkXGTpUF6mZhvkElds8JE.INSTANCE, -0.799f, 2.399f, Named.EXTENDED_SRGB.ordinal());
        colorSpaceArr3[ordinal3] = rgb3;
        ColorSpace[] colorSpaceArr4 = sNamedColorSpaces;
        int ordinal4 = Named.LINEAR_EXTENDED_SRGB.ordinal();
        Rgb rgb4 = new Rgb("scRGB IEC 61966-2-2:2003", SRGB_PRIMARIES, ILLUMINANT_D65, 1.0d, -0.5f, 7.499f, Named.LINEAR_EXTENDED_SRGB.ordinal());
        colorSpaceArr4[ordinal4] = rgb4;
        ColorSpace[] colorSpaceArr5 = sNamedColorSpaces;
        int ordinal5 = Named.BT709.ordinal();
        float[] fArr3 = ILLUMINANT_D65;
        Rgb.TransferParameters transferParameters2 = new Rgb.TransferParameters(0.9099181073703367d, 0.09008189262966333d, 0.2222222222222222d, 0.081d, 2.2222222222222223d);
        Rgb rgb5 = new Rgb("Rec. ITU-R BT.709-5", new float[]{0.64f, 0.33f, 0.3f, 0.6f, 0.15f, 0.06f}, fArr3, transferParameters2, Named.BT709.ordinal());
        colorSpaceArr5[ordinal5] = rgb5;
        ColorSpace[] colorSpaceArr6 = sNamedColorSpaces;
        int ordinal6 = Named.BT2020.ordinal();
        float[] fArr4 = ILLUMINANT_D65;
        Rgb.TransferParameters transferParameters3 = new Rgb.TransferParameters(0.9096697898662786d, 0.09033021013372146d, 0.2222222222222222d, 0.08145d, 2.2222222222222223d);
        Rgb rgb6 = new Rgb("Rec. ITU-R BT.2020-1", new float[]{0.708f, 0.292f, 0.17f, 0.797f, 0.131f, 0.046f}, fArr4, transferParameters3, Named.BT2020.ordinal());
        colorSpaceArr6[ordinal6] = rgb6;
        ColorSpace[] colorSpaceArr7 = sNamedColorSpaces;
        int ordinal7 = Named.DCI_P3.ordinal();
        Rgb rgb7 = new Rgb("SMPTE RP 431-2-2007 DCI (P3)", new float[]{0.68f, 0.32f, 0.265f, 0.69f, 0.15f, 0.06f}, new float[]{0.314f, 0.351f}, 2.6d, 0.0f, 1.0f, Named.DCI_P3.ordinal());
        colorSpaceArr7[ordinal7] = rgb7;
        ColorSpace[] colorSpaceArr8 = sNamedColorSpaces;
        int ordinal8 = Named.DISPLAY_P3.ordinal();
        float[] fArr5 = ILLUMINANT_D65;
        Rgb.TransferParameters transferParameters4 = new Rgb.TransferParameters(0.9478672985781991d, 0.05213270142180095d, 0.07739938080495357d, 0.039d, 2.4d);
        Rgb rgb8 = new Rgb("Display P3", new float[]{0.68f, 0.32f, 0.265f, 0.69f, 0.15f, 0.06f}, fArr5, transferParameters4, Named.DISPLAY_P3.ordinal());
        colorSpaceArr8[ordinal8] = rgb8;
        ColorSpace[] colorSpaceArr9 = sNamedColorSpaces;
        int ordinal9 = Named.NTSC_1953.ordinal();
        float[] fArr6 = NTSC_1953_PRIMARIES;
        float[] fArr7 = ILLUMINANT_C;
        Rgb.TransferParameters transferParameters5 = new Rgb.TransferParameters(0.9099181073703367d, 0.09008189262966333d, 0.2222222222222222d, 0.081d, 2.2222222222222223d);
        Rgb rgb9 = new Rgb("NTSC (1953)", fArr6, fArr7, transferParameters5, Named.NTSC_1953.ordinal());
        colorSpaceArr9[ordinal9] = rgb9;
        ColorSpace[] colorSpaceArr10 = sNamedColorSpaces;
        int ordinal10 = Named.SMPTE_C.ordinal();
        float[] fArr8 = ILLUMINANT_D65;
        Rgb.TransferParameters transferParameters6 = new Rgb.TransferParameters(0.9099181073703367d, 0.09008189262966333d, 0.2222222222222222d, 0.081d, 2.2222222222222223d);
        Rgb rgb10 = new Rgb("SMPTE-C RGB", new float[]{0.63f, 0.34f, 0.31f, 0.595f, 0.155f, 0.07f}, fArr8, transferParameters6, Named.SMPTE_C.ordinal());
        colorSpaceArr10[ordinal10] = rgb10;
        ColorSpace[] colorSpaceArr11 = sNamedColorSpaces;
        int ordinal11 = Named.ADOBE_RGB.ordinal();
        Rgb rgb11 = new Rgb("Adobe RGB (1998)", new float[]{0.64f, 0.33f, 0.21f, 0.71f, 0.15f, 0.06f}, ILLUMINANT_D65, 2.2d, 0.0f, 1.0f, Named.ADOBE_RGB.ordinal());
        colorSpaceArr11[ordinal11] = rgb11;
        ColorSpace[] colorSpaceArr12 = sNamedColorSpaces;
        int ordinal12 = Named.PRO_PHOTO_RGB.ordinal();
        float[] fArr9 = ILLUMINANT_D50;
        Rgb.TransferParameters transferParameters7 = new Rgb.TransferParameters(1.0d, 0.0d, 0.0625d, 0.031248d, 1.8d);
        Rgb rgb12 = new Rgb("ROMM RGB ISO 22028-2:2013", new float[]{0.7347f, 0.2653f, 0.1596f, 0.8404f, 0.0366f, 1.0E-4f}, fArr9, transferParameters7, Named.PRO_PHOTO_RGB.ordinal());
        colorSpaceArr12[ordinal12] = rgb12;
        ColorSpace[] colorSpaceArr13 = sNamedColorSpaces;
        int ordinal13 = Named.ACES.ordinal();
        Rgb rgb13 = new Rgb("SMPTE ST 2065-1:2012 ACES", new float[]{0.7347f, 0.2653f, 0.0f, 1.0f, 1.0E-4f, -0.077f}, ILLUMINANT_D60, 1.0d, -65504.0f, 65504.0f, Named.ACES.ordinal());
        colorSpaceArr13[ordinal13] = rgb13;
        ColorSpace[] colorSpaceArr14 = sNamedColorSpaces;
        int ordinal14 = Named.ACESCG.ordinal();
        Rgb rgb14 = new Rgb("Academy S-2014-004 ACEScg", new float[]{0.713f, 0.293f, 0.165f, 0.83f, 0.128f, 0.044f}, ILLUMINANT_D60, 1.0d, -65504.0f, 65504.0f, Named.ACESCG.ordinal());
        colorSpaceArr14[ordinal14] = rgb14;
        sNamedColorSpaces[Named.CIE_XYZ.ordinal()] = new Xyz("Generic XYZ", Named.CIE_XYZ.ordinal());
        sNamedColorSpaces[Named.CIE_LAB.ordinal()] = new Lab("Generic L*a*b*", Named.CIE_LAB.ordinal());
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
        boolean z = true;
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
        if (this.mModel != that.mModel) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((31 * this.mName.hashCode()) + this.mModel.hashCode())) + this.mId;
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

    static ColorSpace get(int index) {
        if (index >= 0 && index <= Named.values().length) {
            return sNamedColorSpaces[index];
        }
        throw new IllegalArgumentException("Invalid ID, must be in the range [0.." + Named.values().length + "]");
    }

    public static ColorSpace get(Named name) {
        return sNamedColorSpaces[name.ordinal()];
    }

    public static ColorSpace match(float[] toXYZD50, Rgb.TransferParameters function) {
        for (ColorSpace colorSpace : sNamedColorSpaces) {
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
        if (x >= d) {
            return Math.pow((a * x) + b, g) + e;
        }
        double d2 = g;
        return (c * x) + f;
    }

    /* access modifiers changed from: private */
    public static double absRcpResponse(double x, double a, double b, double c, double d, double g) {
        double d2 = x;
        return Math.copySign(rcpResponse(d2 < 0.0d ? -d2 : d2, a, b, c, d, g), d2);
    }

    /* access modifiers changed from: private */
    public static double absResponse(double x, double a, double b, double c, double d, double g) {
        double d2 = x;
        return Math.copySign(response(d2 < 0.0d ? -d2 : d2, a, b, c, d, g), d2);
    }

    private static boolean compare(Rgb.TransferParameters a, Rgb.TransferParameters b) {
        boolean z = true;
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null || Math.abs(a.a - b.a) >= 0.001d || Math.abs(a.b - b.b) >= 0.001d || Math.abs(a.c - b.c) >= 0.001d || Math.abs(a.d - b.d) >= 0.002d || Math.abs(a.e - b.e) >= 0.001d || Math.abs(a.f - b.f) >= 0.001d || Math.abs(a.g - b.g) >= 0.001d) {
            z = false;
        }
        return z;
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
        float[] fArr = m;
        float a = fArr[0];
        float b = fArr[3];
        float c = fArr[6];
        float d = fArr[1];
        float e = fArr[4];
        float f = fArr[7];
        float g = fArr[2];
        float h = fArr[5];
        float i = fArr[8];
        float A = (e * i) - (f * h);
        float B = (f * g) - (d * i);
        float C = (d * h) - (e * g);
        float det = (a * A) + (b * B) + (c * C);
        float[] inverted = new float[fArr.length];
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

    /* access modifiers changed from: private */
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
}
