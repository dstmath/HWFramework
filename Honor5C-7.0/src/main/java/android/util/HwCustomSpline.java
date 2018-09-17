package android.util;

import huawei.com.android.internal.widget.HwFragmentMenuItemView;

public final class HwCustomSpline extends Spline {
    private static final int CONSTANT_COEF = 2;
    private float level;
    private float[][] mSampleData;
    private float[] mX;
    private float[] mY;
    private int sampleHeight;

    private HwCustomSpline(float[] x, float[] y, float[][] sampleData) {
        this.sampleHeight = -1;
        this.level = 128.0f;
        this.mX = x;
        this.mY = y;
        this.mSampleData = sampleData;
        this.sampleHeight = y.length;
    }

    public static HwCustomSpline createHwCustomSpline(float[] x, float[] y, float[][] sampleData) {
        if (x != null && y != null && sampleData != null) {
            return new HwCustomSpline(x, y, sampleData);
        }
        throw new IllegalArgumentException("There must be at least two control points and the arrays must be of equal length.");
    }

    public String toString() {
        return new StringBuilder().toString();
    }

    public float interpolate(float x) {
        return getInterpolatedValue(this.level, x);
    }

    public void updateLevel(float _level) {
        this.level = _level;
    }

    private static float interpolationCompute(float[] x, float[] y, float inputX) {
        float resultY;
        int sampleLength = x.length;
        float[] h = new float[sampleLength];
        float[] f = new float[sampleLength];
        float[] lambda = new float[sampleLength];
        float[] mu = new float[sampleLength];
        float[] g = new float[sampleLength];
        float[] m = new float[sampleLength];
        int index = 0;
        while (index < sampleLength - 1) {
            if (inputX > x[index]) {
                if (inputX > x[index] && inputX < x[index + 1]) {
                    break;
                }
                index++;
            } else {
                return y[index];
            }
        }
        if (index == sampleLength - 1) {
            resultY = y[index];
        } else {
            m[sampleLength - 1] = 0.0f;
            m[0] = 0.0f;
            g[1] = g[1] - (lambda[1] * m[0]);
            g[sampleLength - 2] = g[sampleLength - 2] - (mu[sampleLength - 2] * m[sampleLength - 1]);
            getCoefH(h, x);
            getCoefF(f, y, h);
            getLambda(lambda, h);
            getMu(mu, h);
            getCoefG(g, lambda, mu, f);
            getCoefM(m, lambda, mu, g);
            resultY = (((((((inputX - x[index + 1]) * (inputX - x[index + 1])) * (h[index] + ((inputX - x[index]) * 2.0f))) * y[index]) / ((h[index] * h[index]) * h[index])) + (((((inputX - x[index]) * (inputX - x[index])) * (h[index] + ((x[index + 1] - inputX) * 2.0f))) * y[index + 1]) / ((h[index] * h[index]) * h[index]))) + (((((inputX - x[index + 1]) * (inputX - x[index + 1])) * (inputX - x[index])) * m[index]) / (h[index] * h[index]))) + (((((inputX - x[index]) * (inputX - x[index])) * (inputX - x[index + 1])) * m[index + 1]) / (h[index] * h[index]));
        }
        return resultY;
    }

    public float getInterpolatedValue(float level, float sensorValue) {
        float[] brightAtEnvLight = new float[this.sampleHeight];
        for (int row = 0; row < this.sampleHeight; row++) {
            brightAtEnvLight[row] = interpolationCompute(this.mX, this.mSampleData[row], sensorValue);
        }
        return (((float) ((int) interpolationCompute(this.mY, brightAtEnvLight, level))) * HwFragmentMenuItemView.ALPHA_NORMAL) / 255.0f;
    }

    public float getLevel(float brightness, float sensorValue) {
        float[] brightAtEnvLight = new float[this.sampleHeight];
        for (int row = 0; row < this.sampleHeight; row++) {
            brightAtEnvLight[row] = interpolationCompute(this.mX, this.mSampleData[row], sensorValue);
        }
        return interpolationCompute(brightAtEnvLight, this.mY, brightness);
    }

    private static void getCoefH(float[] h, float[] X) {
        int length = X.length;
        for (int j = 0; j < length - 1; j++) {
            h[j] = X[j + 1] - X[j];
        }
    }

    private static void getCoefF(float[] f, float[] Y, float[] h) {
        int length = Y.length;
        for (int j = 0; j < length - 1; j++) {
            f[j] = (Y[j + 1] - Y[j]) / h[j];
        }
    }

    private static void getLambda(float[] lambda, float[] h) {
        int length = h.length;
        for (int j = 1; j < length - 1; j++) {
            lambda[j] = h[j] / (h[j - 1] + h[j]);
        }
    }

    private static void getMu(float[] mu, float[] h) {
        int length = h.length;
        for (int j = 1; j < length - 1; j++) {
            mu[j] = h[j - 1] / (h[j - 1] + h[j]);
        }
    }

    private static void getCoefG(float[] g, float[] lambda, float[] mu, float[] f) {
        int length = f.length;
        for (int j = 1; j < length - 1; j++) {
            g[j] = ((lambda[j] * f[j - 1]) + (mu[j] * f[j])) * 3.0f;
        }
    }

    private static void getCoefM(float[] coefM, float[] lambda, float[] mu, float[] g) {
        int i;
        int n = g.length - 2;
        float[] beta = new float[(n + 1)];
        float[] y = new float[(n + 1)];
        beta[1] = mu[1] / 2.0f;
        for (i = CONSTANT_COEF; i < n; i++) {
            beta[i] = mu[i] / (2.0f - (lambda[i] * beta[i - 1]));
        }
        y[1] = g[1] / 2.0f;
        for (i = CONSTANT_COEF; i < n + 1; i++) {
            y[i] = (g[i] - (lambda[i] * y[i - 1])) / (2.0f - (lambda[i] * beta[i - 1]));
        }
        coefM[n] = y[n];
        for (i = n - 1; i > 0; i--) {
            coefM[i] = y[i] - (beta[i] * coefM[i + 1]);
        }
    }
}
