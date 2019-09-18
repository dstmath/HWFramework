package com.huawei.zxing.common;

public final class PerspectiveTransform {
    private final float a11;
    private final float a12;
    private final float a13;
    private final float a21;
    private final float a22;
    private final float a23;
    private final float a31;
    private final float a32;
    private final float a33;

    private PerspectiveTransform(float a112, float a212, float a312, float a122, float a222, float a322, float a132, float a232, float a332) {
        this.a11 = a112;
        this.a12 = a122;
        this.a13 = a132;
        this.a21 = a212;
        this.a22 = a222;
        this.a23 = a232;
        this.a31 = a312;
        this.a32 = a322;
        this.a33 = a332;
    }

    public static PerspectiveTransform quadrilateralToQuadrilateral(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, float x0p, float y0p, float x1p, float y1p, float x2p, float y2p, float x3p, float y3p) {
        return squareToQuadrilateral(x0p, y0p, x1p, y1p, x2p, y2p, x3p, y3p).times(quadrilateralToSquare(x0, y0, x1, y1, x2, y2, x3, y3));
    }

    public void transformPoints(float[] points) {
        float[] fArr = points;
        int max = fArr.length;
        float a112 = this.a11;
        float a122 = this.a12;
        float a132 = this.a13;
        float a212 = this.a21;
        float a222 = this.a22;
        float a232 = this.a23;
        float a312 = this.a31;
        float a322 = this.a32;
        float a332 = this.a33;
        for (int i = 0; i < max; i += 2) {
            float x = fArr[i];
            float y = fArr[i + 1];
            float denominator = (a132 * x) + (a232 * y) + a332;
            fArr[i] = (((a112 * x) + (a212 * y)) + a312) / denominator;
            fArr[i + 1] = (((a122 * x) + (a222 * y)) + a322) / denominator;
        }
    }

    public void transformPoints(float[] xValues, float[] yValues) {
        int n = xValues.length;
        for (int i = 0; i < n; i++) {
            float x = xValues[i];
            float y = yValues[i];
            float denominator = (this.a13 * x) + (this.a23 * y) + this.a33;
            xValues[i] = (((this.a11 * x) + (this.a21 * y)) + this.a31) / denominator;
            yValues[i] = (((this.a12 * x) + (this.a22 * y)) + this.a32) / denominator;
        }
    }

    public static PerspectiveTransform squareToQuadrilateral(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        float dx3 = ((x0 - x1) + x2) - x3;
        float dy3 = ((y0 - y1) + y2) - y3;
        if (dx3 == 0.0f && dy3 == 0.0f) {
            PerspectiveTransform perspectiveTransform = new PerspectiveTransform(x1 - x0, x2 - x1, x0, y1 - y0, y2 - y1, y0, 0.0f, 0.0f, 1.0f);
            return perspectiveTransform;
        }
        float dx1 = x1 - x2;
        float dx2 = x3 - x2;
        float dy1 = y1 - y2;
        float dy2 = y3 - y2;
        float denominator = (dx1 * dy2) - (dx2 * dy1);
        float a132 = ((dx3 * dy2) - (dx2 * dy3)) / denominator;
        float a232 = ((dx1 * dy3) - (dx3 * dy1)) / denominator;
        PerspectiveTransform perspectiveTransform2 = new PerspectiveTransform((a132 * x1) + (x1 - x0), (a232 * x3) + (x3 - x0), x0, (y1 - y0) + (a132 * y1), (y3 - y0) + (a232 * y3), y0, a132, a232, 1.0f);
        return perspectiveTransform2;
    }

    public static PerspectiveTransform quadrilateralToSquare(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        return squareToQuadrilateral(x0, y0, x1, y1, x2, y2, x3, y3).buildAdjoint();
    }

    /* access modifiers changed from: package-private */
    public PerspectiveTransform buildAdjoint() {
        PerspectiveTransform perspectiveTransform = new PerspectiveTransform((this.a22 * this.a33) - (this.a23 * this.a32), (this.a23 * this.a31) - (this.a21 * this.a33), (this.a21 * this.a32) - (this.a22 * this.a31), (this.a13 * this.a32) - (this.a12 * this.a33), (this.a11 * this.a33) - (this.a13 * this.a31), (this.a12 * this.a31) - (this.a11 * this.a32), (this.a12 * this.a23) - (this.a13 * this.a22), (this.a13 * this.a21) - (this.a11 * this.a23), (this.a11 * this.a22) - (this.a12 * this.a21));
        return perspectiveTransform;
    }

    /* access modifiers changed from: package-private */
    public PerspectiveTransform times(PerspectiveTransform other) {
        PerspectiveTransform perspectiveTransform = new PerspectiveTransform((this.a31 * other.a13) + (this.a11 * other.a11) + (this.a21 * other.a12), (this.a31 * other.a23) + (this.a11 * other.a21) + (this.a21 * other.a22), (this.a31 * other.a33) + (this.a11 * other.a31) + (this.a21 * other.a32), (this.a32 * other.a13) + (this.a12 * other.a11) + (this.a22 * other.a12), (this.a32 * other.a23) + (this.a12 * other.a21) + (this.a22 * other.a22), (this.a32 * other.a33) + (this.a12 * other.a31) + (this.a22 * other.a32), (this.a33 * other.a13) + (this.a13 * other.a11) + (this.a23 * other.a12), (this.a33 * other.a23) + (this.a13 * other.a21) + (this.a23 * other.a22), (this.a33 * other.a33) + (this.a13 * other.a31) + (this.a23 * other.a32));
        return perspectiveTransform;
    }
}
