package com.android.server.display;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.display.BrightnessConfiguration;
import android.util.MathUtils;
import android.util.Pair;
import android.util.Slog;
import android.util.Spline;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.display.utils.Plog;
import com.android.server.os.HwBootFail;
import java.io.PrintWriter;
import java.util.Arrays;

public abstract class BrightnessMappingStrategy {
    private static final boolean DEBUG = false;
    private static final float LUX_GRAD_SMOOTHING = 0.25f;
    private static final float MAX_GRAD = 1.0f;
    private static final Plog PLOG = Plog.createSystemPlog(TAG);
    private static final String TAG = "BrightnessMappingStrategy";

    @VisibleForTesting
    static class PhysicalMappingStrategy extends BrightnessMappingStrategy {
        private float mAutoBrightnessAdjustment;
        private Spline mBacklightToNitsSpline;
        private Spline mBrightnessSpline;
        private BrightnessConfiguration mConfig;
        private final BrightnessConfiguration mDefaultConfig;
        private float mMaxGamma;
        private final Spline mNitsToBacklightSpline;
        private float mUserBrightness;
        private float mUserLux;

        public PhysicalMappingStrategy(BrightnessConfiguration config, float[] nits, int[] backlight, float maxGamma) {
            boolean z = true;
            Preconditions.checkArgument((nits.length == 0 || backlight.length == 0) ? false : true, "Nits and backlight arrays must not be empty!");
            Preconditions.checkArgument(nits.length != backlight.length ? false : z, "Nits and backlight arrays must be the same length!");
            Preconditions.checkNotNull(config);
            Preconditions.checkArrayElementsInRange(nits, 0.0f, Float.MAX_VALUE, "nits");
            Preconditions.checkArrayElementsInRange(backlight, 0, 255, "backlight");
            this.mMaxGamma = maxGamma;
            this.mAutoBrightnessAdjustment = 0.0f;
            this.mUserLux = -1.0f;
            this.mUserBrightness = -1.0f;
            int N = nits.length;
            float[] normalizedBacklight = new float[N];
            for (int i = 0; i < N; i++) {
                normalizedBacklight[i] = BrightnessMappingStrategy.normalizeAbsoluteBrightness(backlight[i]);
            }
            this.mNitsToBacklightSpline = Spline.createSpline(nits, normalizedBacklight);
            this.mBacklightToNitsSpline = Spline.createSpline(normalizedBacklight, nits);
            this.mDefaultConfig = config;
            this.mConfig = config;
            computeSpline();
        }

        public boolean setBrightnessConfiguration(BrightnessConfiguration config) {
            if (config == null) {
                config = this.mDefaultConfig;
            }
            if (config.equals(this.mConfig)) {
                return false;
            }
            this.mConfig = config;
            computeSpline();
            return true;
        }

        public float getBrightness(float lux) {
            return this.mNitsToBacklightSpline.interpolate(this.mBrightnessSpline.interpolate(lux));
        }

        public float getAutoBrightnessAdjustment() {
            return this.mAutoBrightnessAdjustment;
        }

        public boolean setAutoBrightnessAdjustment(float adjustment) {
            float adjustment2 = MathUtils.constrain(adjustment, -1.0f, 1.0f);
            if (adjustment2 == this.mAutoBrightnessAdjustment) {
                return false;
            }
            this.mAutoBrightnessAdjustment = adjustment2;
            computeSpline();
            return true;
        }

        public float convertToNits(int backlight) {
            return this.mBacklightToNitsSpline.interpolate(BrightnessMappingStrategy.normalizeAbsoluteBrightness(backlight));
        }

        public void addUserDataPoint(float lux, float brightness) {
            this.mAutoBrightnessAdjustment = BrightnessMappingStrategy.inferAutoBrightnessAdjustment(this.mMaxGamma, brightness, getUnadjustedBrightness(lux));
            this.mUserLux = lux;
            this.mUserBrightness = brightness;
            computeSpline();
        }

        public void clearUserDataPoints() {
            if (this.mUserLux != -1.0f) {
                this.mAutoBrightnessAdjustment = 0.0f;
                this.mUserLux = -1.0f;
                this.mUserBrightness = -1.0f;
                computeSpline();
            }
        }

        public boolean hasUserDataPoints() {
            return this.mUserLux != -1.0f;
        }

        public boolean isDefaultConfig() {
            return this.mDefaultConfig.equals(this.mConfig);
        }

        public BrightnessConfiguration getDefaultConfig() {
            return this.mDefaultConfig;
        }

        public void dump(PrintWriter pw) {
            pw.println("PhysicalMappingStrategy");
            pw.println("  mConfig=" + this.mConfig);
            pw.println("  mBrightnessSpline=" + this.mBrightnessSpline);
            pw.println("  mNitsToBacklightSpline=" + this.mNitsToBacklightSpline);
            pw.println("  mMaxGamma=" + this.mMaxGamma);
            pw.println("  mAutoBrightnessAdjustment=" + this.mAutoBrightnessAdjustment);
            pw.println("  mUserLux=" + this.mUserLux);
            pw.println("  mUserBrightness=" + this.mUserBrightness);
        }

        private void computeSpline() {
            Pair<float[], float[]> defaultCurve = this.mConfig.getCurve();
            float[] defaultLux = (float[]) defaultCurve.first;
            float[] defaultNits = (float[]) defaultCurve.second;
            float[] defaultBacklight = new float[defaultNits.length];
            int i = 0;
            for (int i2 = 0; i2 < defaultBacklight.length; i2++) {
                defaultBacklight[i2] = this.mNitsToBacklightSpline.interpolate(defaultNits[i2]);
            }
            Pair<float[], float[]> curve = BrightnessMappingStrategy.getAdjustedCurve(defaultLux, defaultBacklight, this.mUserLux, this.mUserBrightness, this.mAutoBrightnessAdjustment, this.mMaxGamma);
            float[] lux = (float[]) curve.first;
            float[] backlight = (float[]) curve.second;
            float[] nits = new float[backlight.length];
            while (true) {
                int i3 = i;
                if (i3 < nits.length) {
                    nits[i3] = this.mBacklightToNitsSpline.interpolate(backlight[i3]);
                    i = i3 + 1;
                } else {
                    this.mBrightnessSpline = Spline.createSpline(lux, nits);
                    return;
                }
            }
        }

        private float getUnadjustedBrightness(float lux) {
            Pair<float[], float[]> curve = this.mConfig.getCurve();
            return this.mNitsToBacklightSpline.interpolate(Spline.createSpline((float[]) curve.first, (float[]) curve.second).interpolate(lux));
        }
    }

    private static class SimpleMappingStrategy extends BrightnessMappingStrategy {
        private float mAutoBrightnessAdjustment;
        private final float[] mBrightness;
        private final float[] mLux;
        private float mMaxGamma;
        private Spline mSpline;
        private float mUserBrightness;
        private float mUserLux;

        public SimpleMappingStrategy(float[] lux, int[] brightness, float maxGamma) {
            boolean z = true;
            int i = 0;
            Preconditions.checkArgument((lux.length == 0 || brightness.length == 0) ? false : true, "Lux and brightness arrays must not be empty!");
            Preconditions.checkArgument(lux.length != brightness.length ? false : z, "Lux and brightness arrays must be the same length!");
            Preconditions.checkArrayElementsInRange(lux, 0.0f, Float.MAX_VALUE, "lux");
            Preconditions.checkArrayElementsInRange(brightness, 0, HwBootFail.STAGE_BOOT_SUCCESS, "brightness");
            int N = brightness.length;
            this.mLux = new float[N];
            this.mBrightness = new float[N];
            while (true) {
                int i2 = i;
                if (i2 < N) {
                    this.mLux[i2] = lux[i2];
                    this.mBrightness[i2] = BrightnessMappingStrategy.normalizeAbsoluteBrightness(brightness[i2]);
                    i = i2 + 1;
                } else {
                    this.mMaxGamma = maxGamma;
                    this.mAutoBrightnessAdjustment = 0.0f;
                    this.mUserLux = -1.0f;
                    this.mUserBrightness = -1.0f;
                    computeSpline();
                    return;
                }
            }
        }

        public boolean setBrightnessConfiguration(BrightnessConfiguration config) {
            return false;
        }

        public float getBrightness(float lux) {
            return this.mSpline.interpolate(lux);
        }

        public float getAutoBrightnessAdjustment() {
            return this.mAutoBrightnessAdjustment;
        }

        public boolean setAutoBrightnessAdjustment(float adjustment) {
            float adjustment2 = MathUtils.constrain(adjustment, -1.0f, 1.0f);
            if (adjustment2 == this.mAutoBrightnessAdjustment) {
                return false;
            }
            this.mAutoBrightnessAdjustment = adjustment2;
            computeSpline();
            return true;
        }

        public float convertToNits(int backlight) {
            return -1.0f;
        }

        public void addUserDataPoint(float lux, float brightness) {
            this.mAutoBrightnessAdjustment = BrightnessMappingStrategy.inferAutoBrightnessAdjustment(this.mMaxGamma, brightness, getUnadjustedBrightness(lux));
            this.mUserLux = lux;
            this.mUserBrightness = brightness;
            computeSpline();
        }

        public void clearUserDataPoints() {
            if (this.mUserLux != -1.0f) {
                this.mAutoBrightnessAdjustment = 0.0f;
                this.mUserLux = -1.0f;
                this.mUserBrightness = -1.0f;
                computeSpline();
            }
        }

        public boolean hasUserDataPoints() {
            return this.mUserLux != -1.0f;
        }

        public boolean isDefaultConfig() {
            return true;
        }

        public BrightnessConfiguration getDefaultConfig() {
            return null;
        }

        public void dump(PrintWriter pw) {
            pw.println("SimpleMappingStrategy");
            pw.println("  mSpline=" + this.mSpline);
            pw.println("  mMaxGamma=" + this.mMaxGamma);
            pw.println("  mAutoBrightnessAdjustment=" + this.mAutoBrightnessAdjustment);
            pw.println("  mUserLux=" + this.mUserLux);
            pw.println("  mUserBrightness=" + this.mUserBrightness);
        }

        private void computeSpline() {
            Pair<float[], float[]> curve = BrightnessMappingStrategy.getAdjustedCurve(this.mLux, this.mBrightness, this.mUserLux, this.mUserBrightness, this.mAutoBrightnessAdjustment, this.mMaxGamma);
            this.mSpline = Spline.createSpline((float[]) curve.first, (float[]) curve.second);
        }

        private float getUnadjustedBrightness(float lux) {
            return Spline.createSpline(this.mLux, this.mBrightness).interpolate(lux);
        }
    }

    public abstract void addUserDataPoint(float f, float f2);

    public abstract void clearUserDataPoints();

    public abstract float convertToNits(int i);

    public abstract void dump(PrintWriter printWriter);

    public abstract float getAutoBrightnessAdjustment();

    public abstract float getBrightness(float f);

    public abstract BrightnessConfiguration getDefaultConfig();

    public abstract boolean hasUserDataPoints();

    public abstract boolean isDefaultConfig();

    public abstract boolean setAutoBrightnessAdjustment(float f);

    public abstract boolean setBrightnessConfiguration(BrightnessConfiguration brightnessConfiguration);

    public static BrightnessMappingStrategy create(Resources resources) {
        float[] luxLevels = getLuxLevels(resources.getIntArray(17235985));
        int[] brightnessLevelsBacklight = resources.getIntArray(17235984);
        float[] brightnessLevelsNits = getFloatArray(resources.obtainTypedArray(17235982));
        float autoBrightnessAdjustmentMaxGamma = resources.getFraction(18022400, 1, 1);
        float[] nitsRange = getFloatArray(resources.obtainTypedArray(17236032));
        int[] backlightRange = resources.getIntArray(17236031);
        if (isValidMapping(nitsRange, backlightRange) && isValidMapping(luxLevels, brightnessLevelsNits)) {
            int minimumBacklight = resources.getInteger(17694862);
            int maximumBacklight = resources.getInteger(17694861);
            if (backlightRange[0] > minimumBacklight || backlightRange[backlightRange.length - 1] < maximumBacklight) {
                Slog.w(TAG, "Screen brightness mapping does not cover whole range of available backlight values, autobrightness functionality may be impaired.");
            }
            BrightnessConfiguration.Builder builder = new BrightnessConfiguration.Builder();
            builder.setCurve(luxLevels, brightnessLevelsNits);
            return new PhysicalMappingStrategy(builder.build(), nitsRange, backlightRange, autoBrightnessAdjustmentMaxGamma);
        } else if (isValidMapping(luxLevels, brightnessLevelsBacklight)) {
            return new SimpleMappingStrategy(luxLevels, brightnessLevelsBacklight, autoBrightnessAdjustmentMaxGamma);
        } else {
            return null;
        }
    }

    private static float[] getLuxLevels(int[] lux) {
        float[] levels = new float[(lux.length + 1)];
        for (int i = 0; i < lux.length; i++) {
            levels[i + 1] = (float) lux[i];
        }
        return levels;
    }

    private static float[] getFloatArray(TypedArray array) {
        int N = array.length();
        float[] vals = new float[N];
        for (int i = 0; i < N; i++) {
            vals[i] = array.getFloat(i, -1.0f);
        }
        array.recycle();
        return vals;
    }

    private static boolean isValidMapping(float[] x, float[] y) {
        if (x == null || y == null || x.length == 0 || y.length == 0 || x.length != y.length) {
            return false;
        }
        int N = x.length;
        float prevX = x[0];
        float prevY = y[0];
        if (prevX < 0.0f || prevY < 0.0f || Float.isNaN(prevX) || Float.isNaN(prevY)) {
            return false;
        }
        float prevY2 = prevY;
        float prevX2 = prevX;
        for (int i = 1; i < N; i++) {
            if (prevX2 >= x[i] || prevY2 > y[i] || Float.isNaN(x[i]) || Float.isNaN(y[i])) {
                return false;
            }
            prevX2 = x[i];
            prevY2 = y[i];
        }
        return true;
    }

    private static boolean isValidMapping(float[] x, int[] y) {
        if (x == null || y == null || x.length == 0 || y.length == 0 || x.length != y.length) {
            return false;
        }
        int N = x.length;
        float prevX = x[0];
        int prevY = y[0];
        if (prevX < 0.0f || prevY < 0 || Float.isNaN(prevX)) {
            return false;
        }
        int prevY2 = prevY;
        float prevX2 = prevX;
        for (int i = 1; i < N; i++) {
            if (prevX2 >= x[i] || prevY2 > y[i] || Float.isNaN(x[i])) {
                return false;
            }
            prevX2 = x[i];
            prevY2 = y[i];
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static float normalizeAbsoluteBrightness(int brightness) {
        return ((float) MathUtils.constrain(brightness, 0, 255)) / 255.0f;
    }

    private static Pair<float[], float[]> insertControlPoint(float[] luxLevels, float[] brightnessLevels, float lux, float brightness) {
        float[] newBrightnessLevels;
        float[] newLuxLevels;
        int idx = findInsertionPoint(luxLevels, lux);
        if (idx == luxLevels.length) {
            newLuxLevels = Arrays.copyOf(luxLevels, luxLevels.length + 1);
            newBrightnessLevels = Arrays.copyOf(brightnessLevels, brightnessLevels.length + 1);
            newLuxLevels[idx] = lux;
            newBrightnessLevels[idx] = brightness;
        } else if (luxLevels[idx] == lux) {
            newLuxLevels = Arrays.copyOf(luxLevels, luxLevels.length);
            newBrightnessLevels = Arrays.copyOf(brightnessLevels, brightnessLevels.length);
            newBrightnessLevels[idx] = brightness;
        } else {
            newLuxLevels = Arrays.copyOf(luxLevels, luxLevels.length + 1);
            System.arraycopy(newLuxLevels, idx, newLuxLevels, idx + 1, luxLevels.length - idx);
            newLuxLevels[idx] = lux;
            newBrightnessLevels = Arrays.copyOf(brightnessLevels, brightnessLevels.length + 1);
            System.arraycopy(newBrightnessLevels, idx, newBrightnessLevels, idx + 1, brightnessLevels.length - idx);
            newBrightnessLevels[idx] = brightness;
        }
        smoothCurve(newLuxLevels, newBrightnessLevels, idx);
        return Pair.create(newLuxLevels, newBrightnessLevels);
    }

    private static int findInsertionPoint(float[] arr, float val) {
        for (int i = 0; i < arr.length; i++) {
            if (val <= arr[i]) {
                return i;
            }
        }
        return arr.length;
    }

    private static void smoothCurve(float[] lux, float[] brightness, int idx) {
        float prevLux = lux[idx];
        float prevBrightness = brightness[idx];
        for (int i = idx + 1; i < lux.length; i++) {
            float currLux = lux[i];
            float currBrightness = brightness[i];
            float newBrightness = MathUtils.constrain(currBrightness, prevBrightness, permissibleRatio(currLux, prevLux) * prevBrightness);
            if (newBrightness == currBrightness) {
                break;
            }
            prevLux = currLux;
            prevBrightness = newBrightness;
            brightness[i] = newBrightness;
        }
        float prevLux2 = lux[idx];
        float prevBrightness2 = brightness[idx];
        int i2 = idx - 1;
        while (i2 >= 0) {
            float currLux2 = lux[i2];
            float currBrightness2 = brightness[i2];
            float newBrightness2 = MathUtils.constrain(currBrightness2, permissibleRatio(currLux2, prevLux2) * prevBrightness2, prevBrightness2);
            if (newBrightness2 != currBrightness2) {
                prevLux2 = currLux2;
                prevBrightness2 = newBrightness2;
                brightness[i2] = newBrightness2;
                i2--;
            } else {
                return;
            }
        }
    }

    private static float permissibleRatio(float currLux, float prevLux) {
        return MathUtils.exp(1.0f * (MathUtils.log(currLux + LUX_GRAD_SMOOTHING) - MathUtils.log(LUX_GRAD_SMOOTHING + prevLux)));
    }

    /* access modifiers changed from: private */
    public static float inferAutoBrightnessAdjustment(float maxGamma, float desiredBrightness, float currentBrightness) {
        float adjustment;
        if (currentBrightness <= 0.1f || currentBrightness >= 0.9f) {
            adjustment = desiredBrightness - currentBrightness;
        } else if (desiredBrightness == 0.0f) {
            adjustment = -1.0f;
        } else {
            adjustment = desiredBrightness == 1.0f ? 1.0f : (-MathUtils.log(MathUtils.log(desiredBrightness) / MathUtils.log(currentBrightness))) / MathUtils.log(maxGamma);
        }
        return MathUtils.constrain(adjustment, -1.0f, 1.0f);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: float[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: float[]} */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    public static Pair<float[], float[]> getAdjustedCurve(float[] lux, float[] brightness, float userLux, float userBrightness, float adjustment, float maxGamma) {
        float[] newLux = lux;
        float[] newBrightness = Arrays.copyOf(brightness, brightness.length);
        float gamma = MathUtils.pow(maxGamma, -MathUtils.constrain(adjustment, -1.0f, 1.0f));
        if (gamma != 1.0f) {
            for (int i = 0; i < newBrightness.length; i++) {
                newBrightness[i] = MathUtils.pow(newBrightness[i], gamma);
            }
        }
        if (userLux != -1.0f) {
            Pair<float[], float[]> curve = insertControlPoint(newLux, newBrightness, userLux, userBrightness);
            newLux = curve.first;
            newBrightness = curve.second;
        }
        return Pair.create(newLux, newBrightness);
    }
}
