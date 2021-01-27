package com.android.server.display;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.BrightnessCorrection;
import android.util.MathUtils;
import android.util.Pair;
import android.util.Slog;
import android.util.Spline;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.display.utils.Plog;
import java.io.PrintWriter;
import java.util.Arrays;

public abstract class BrightnessMappingStrategy {
    private static final float LUX_GRAD_SMOOTHING = 0.25f;
    private static final float MAX_GRAD = 1.0f;
    private static final Plog PLOG = Plog.createSystemPlog(TAG);
    private static final String TAG = "BrightnessMappingStrategy";
    protected boolean mLoggingEnabled;

    public abstract void addUserDataPoint(float f, float f2);

    public abstract void clearUserDataPoints();

    public abstract float convertToNits(int i);

    public abstract void dump(PrintWriter printWriter);

    public abstract float getAutoBrightnessAdjustment();

    public abstract float getBrightness(float f, String str, int i);

    public abstract BrightnessConfiguration getDefaultConfig();

    public abstract boolean hasUserDataPoints();

    public abstract boolean isDefaultConfig();

    public abstract boolean setAutoBrightnessAdjustment(float f);

    public abstract boolean setBrightnessConfiguration(BrightnessConfiguration brightnessConfiguration);

    public static BrightnessMappingStrategy create(Resources resources) {
        float[] luxLevels = getLuxLevels(resources.getIntArray(17235989));
        int[] brightnessLevelsBacklight = resources.getIntArray(17235988);
        float[] brightnessLevelsNits = getFloatArray(resources.obtainTypedArray(17235986));
        float autoBrightnessAdjustmentMaxGamma = resources.getFraction(18022400, 1, 1);
        float[] nitsRange = getFloatArray(resources.obtainTypedArray(17236056));
        int[] backlightRange = resources.getIntArray(17236055);
        if (isValidMapping(nitsRange, backlightRange) && isValidMapping(luxLevels, brightnessLevelsNits)) {
            int minimumBacklight = resources.getInteger(17694889);
            int maximumBacklight = resources.getInteger(17694888);
            if (backlightRange[0] > minimumBacklight || backlightRange[backlightRange.length - 1] < maximumBacklight) {
                Slog.w(TAG, "Screen brightness mapping does not cover whole range of available backlight values, autobrightness functionality may be impaired.");
            }
            return new PhysicalMappingStrategy(new BrightnessConfiguration.Builder(luxLevels, brightnessLevelsNits).build(), nitsRange, backlightRange, autoBrightnessAdjustmentMaxGamma);
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
        for (int i = 1; i < N; i++) {
            if (prevX >= x[i] || prevY > y[i] || Float.isNaN(x[i]) || Float.isNaN(y[i])) {
                return false;
            }
            prevX = x[i];
            prevY = y[i];
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
        for (int i = 1; i < N; i++) {
            if (prevX >= x[i] || prevY > y[i] || Float.isNaN(x[i])) {
                return false;
            }
            prevX = x[i];
            prevY = y[i];
        }
        return true;
    }

    public boolean setLoggingEnabled(boolean loggingEnabled) {
        if (this.mLoggingEnabled == loggingEnabled) {
            return false;
        }
        this.mLoggingEnabled = loggingEnabled;
        return true;
    }

    public float getBrightness(float lux) {
        return getBrightness(lux, null, -1);
    }

    /* access modifiers changed from: protected */
    public float normalizeAbsoluteBrightness(int brightness) {
        return ((float) MathUtils.constrain(brightness, 0, 255)) / 255.0f;
    }

    private Pair<float[], float[]> insertControlPoint(float[] luxLevels, float[] brightnessLevels, float lux, float brightness) {
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

    private int findInsertionPoint(float[] arr, float val) {
        for (int i = 0; i < arr.length; i++) {
            if (val <= arr[i]) {
                return i;
            }
        }
        return arr.length;
    }

    private void smoothCurve(float[] lux, float[] brightness, int idx) {
        if (this.mLoggingEnabled) {
            PLOG.logCurve("unsmoothed curve", lux, brightness);
        }
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
        for (int i2 = idx - 1; i2 >= 0; i2--) {
            float currLux2 = lux[i2];
            float currBrightness2 = brightness[i2];
            float newBrightness2 = MathUtils.constrain(currBrightness2, permissibleRatio(currLux2, prevLux2) * prevBrightness2, prevBrightness2);
            if (newBrightness2 == currBrightness2) {
                break;
            }
            prevLux2 = currLux2;
            prevBrightness2 = newBrightness2;
            brightness[i2] = newBrightness2;
        }
        if (this.mLoggingEnabled) {
            PLOG.logCurve("smoothed curve", lux, brightness);
        }
    }

    private float permissibleRatio(float currLux, float prevLux) {
        return MathUtils.exp((MathUtils.log(currLux + LUX_GRAD_SMOOTHING) - MathUtils.log(LUX_GRAD_SMOOTHING + prevLux)) * 1.0f);
    }

    /* access modifiers changed from: protected */
    public float inferAutoBrightnessAdjustment(float maxGamma, float desiredBrightness, float currentBrightness) {
        float adjustment;
        float gamma = Float.NaN;
        if (currentBrightness <= 0.1f || currentBrightness >= 0.9f) {
            adjustment = desiredBrightness - currentBrightness;
        } else if (desiredBrightness == 0.0f) {
            adjustment = -1.0f;
        } else if (desiredBrightness == 1.0f) {
            adjustment = 1.0f;
        } else {
            gamma = MathUtils.log(desiredBrightness) / MathUtils.log(currentBrightness);
            adjustment = (-MathUtils.log(gamma)) / MathUtils.log(maxGamma);
        }
        float adjustment2 = MathUtils.constrain(adjustment, -1.0f, 1.0f);
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "inferAutoBrightnessAdjustment: " + maxGamma + "^" + (-adjustment2) + "=" + MathUtils.pow(maxGamma, -adjustment2) + " == " + gamma);
            Slog.d(TAG, "inferAutoBrightnessAdjustment: " + currentBrightness + "^" + gamma + "=" + MathUtils.pow(currentBrightness, gamma) + " == " + desiredBrightness);
        }
        return adjustment2;
    }

    /* access modifiers changed from: protected */
    public Pair<float[], float[]> getAdjustedCurve(float[] lux, float[] brightness, float userLux, float userBrightness, float adjustment, float maxGamma) {
        float[] newLux = lux;
        float[] newBrightness = Arrays.copyOf(brightness, brightness.length);
        if (this.mLoggingEnabled) {
            PLOG.logCurve("unadjusted curve", newLux, newBrightness);
        }
        float adjustment2 = MathUtils.constrain(adjustment, -1.0f, 1.0f);
        float gamma = MathUtils.pow(maxGamma, -adjustment2);
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "getAdjustedCurve: " + maxGamma + "^" + (-adjustment2) + "=" + MathUtils.pow(maxGamma, -adjustment2) + " == " + gamma);
        }
        if (gamma != 1.0f) {
            for (int i = 0; i < newBrightness.length; i++) {
                newBrightness[i] = MathUtils.pow(newBrightness[i], gamma);
            }
        }
        if (this.mLoggingEnabled) {
            PLOG.logCurve("gamma adjusted curve", newLux, newBrightness);
        }
        if (userLux != -1.0f) {
            Pair<float[], float[]> curve = insertControlPoint(newLux, newBrightness, userLux, userBrightness);
            newLux = (float[]) curve.first;
            newBrightness = (float[]) curve.second;
            if (this.mLoggingEnabled) {
                PLOG.logCurve("gamma and user adjusted curve", newLux, newBrightness);
                Pair<float[], float[]> curve2 = insertControlPoint(lux, brightness, userLux, userBrightness);
                PLOG.logCurve("user adjusted curve", (float[]) curve2.first, (float[]) curve2.second);
            }
        }
        return Pair.create(newLux, newBrightness);
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
            Preconditions.checkArgument((lux.length == 0 || brightness.length == 0) ? false : true, "Lux and brightness arrays must not be empty!");
            Preconditions.checkArgument(lux.length != brightness.length ? false : z, "Lux and brightness arrays must be the same length!");
            Preconditions.checkArrayElementsInRange(lux, 0.0f, Float.MAX_VALUE, "lux");
            Preconditions.checkArrayElementsInRange(brightness, 0, Integer.MAX_VALUE, "brightness");
            int N = brightness.length;
            this.mLux = new float[N];
            this.mBrightness = new float[N];
            for (int i = 0; i < N; i++) {
                this.mLux[i] = lux[i];
                this.mBrightness[i] = normalizeAbsoluteBrightness(brightness[i]);
            }
            this.mMaxGamma = maxGamma;
            this.mAutoBrightnessAdjustment = 0.0f;
            this.mUserLux = -1.0f;
            this.mUserBrightness = -1.0f;
            if (this.mLoggingEnabled) {
                BrightnessMappingStrategy.PLOG.start("simple mapping strategy");
            }
            computeSpline();
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public boolean setBrightnessConfiguration(BrightnessConfiguration config) {
            return false;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public float getBrightness(float lux, String packageName, int category) {
            return this.mSpline.interpolate(lux);
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public float getAutoBrightnessAdjustment() {
            return this.mAutoBrightnessAdjustment;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public boolean setAutoBrightnessAdjustment(float adjustment) {
            float adjustment2 = MathUtils.constrain(adjustment, -1.0f, 1.0f);
            if (adjustment2 == this.mAutoBrightnessAdjustment) {
                return false;
            }
            if (this.mLoggingEnabled) {
                Slog.d(BrightnessMappingStrategy.TAG, "setAutoBrightnessAdjustment: " + this.mAutoBrightnessAdjustment + " => " + adjustment2);
                BrightnessMappingStrategy.PLOG.start("auto-brightness adjustment");
            }
            this.mAutoBrightnessAdjustment = adjustment2;
            computeSpline();
            return true;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public float convertToNits(int backlight) {
            return -1.0f;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public void addUserDataPoint(float lux, float brightness) {
            float unadjustedBrightness = getUnadjustedBrightness(lux);
            if (this.mLoggingEnabled) {
                Slog.d(BrightnessMappingStrategy.TAG, "addUserDataPoint: (" + lux + "," + brightness + ")");
                BrightnessMappingStrategy.PLOG.start("add user data point").logPoint("user data point", lux, brightness).logPoint("current brightness", lux, unadjustedBrightness);
            }
            float adjustment = inferAutoBrightnessAdjustment(this.mMaxGamma, brightness, unadjustedBrightness);
            if (this.mLoggingEnabled) {
                Slog.d(BrightnessMappingStrategy.TAG, "addUserDataPoint: " + this.mAutoBrightnessAdjustment + " => " + adjustment);
            }
            this.mAutoBrightnessAdjustment = adjustment;
            this.mUserLux = lux;
            this.mUserBrightness = brightness;
            computeSpline();
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public void clearUserDataPoints() {
            if (this.mUserLux != -1.0f) {
                if (this.mLoggingEnabled) {
                    Slog.d(BrightnessMappingStrategy.TAG, "clearUserDataPoints: " + this.mAutoBrightnessAdjustment + " => 0");
                    BrightnessMappingStrategy.PLOG.start("clear user data points").logPoint("user data point", this.mUserLux, this.mUserBrightness);
                }
                this.mAutoBrightnessAdjustment = 0.0f;
                this.mUserLux = -1.0f;
                this.mUserBrightness = -1.0f;
                computeSpline();
            }
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public boolean hasUserDataPoints() {
            return this.mUserLux != -1.0f;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public boolean isDefaultConfig() {
            return true;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public BrightnessConfiguration getDefaultConfig() {
            return null;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public void dump(PrintWriter pw) {
            pw.println("SimpleMappingStrategy");
            pw.println("  mSpline=" + this.mSpline);
            pw.println("  mMaxGamma=" + this.mMaxGamma);
            pw.println("  mAutoBrightnessAdjustment=" + this.mAutoBrightnessAdjustment);
            pw.println("  mUserLux=" + this.mUserLux);
            pw.println("  mUserBrightness=" + this.mUserBrightness);
        }

        private void computeSpline() {
            Pair<float[], float[]> curve = getAdjustedCurve(this.mLux, this.mBrightness, this.mUserLux, this.mUserBrightness, this.mAutoBrightnessAdjustment, this.mMaxGamma);
            this.mSpline = Spline.createSpline((float[]) curve.first, (float[]) curve.second);
        }

        private float getUnadjustedBrightness(float lux) {
            return Spline.createSpline(this.mLux, this.mBrightness).interpolate(lux);
        }
    }

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
                normalizedBacklight[i] = normalizeAbsoluteBrightness(backlight[i]);
            }
            this.mNitsToBacklightSpline = Spline.createSpline(nits, normalizedBacklight);
            this.mBacklightToNitsSpline = Spline.createSpline(normalizedBacklight, nits);
            this.mDefaultConfig = config;
            if (this.mLoggingEnabled) {
                BrightnessMappingStrategy.PLOG.start("physical mapping strategy");
            }
            this.mConfig = config;
            computeSpline();
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public boolean setBrightnessConfiguration(BrightnessConfiguration config) {
            if (config == null) {
                config = this.mDefaultConfig;
            }
            if (config.equals(this.mConfig)) {
                return false;
            }
            if (this.mLoggingEnabled) {
                BrightnessMappingStrategy.PLOG.start("brightness configuration");
            }
            this.mConfig = config;
            computeSpline();
            return true;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public float getBrightness(float lux, String packageName, int category) {
            float backlight = this.mNitsToBacklightSpline.interpolate(this.mBrightnessSpline.interpolate(lux));
            if (this.mUserLux == -1.0f) {
                return correctBrightness(backlight, packageName, category);
            }
            if (!this.mLoggingEnabled) {
                return backlight;
            }
            Slog.d(BrightnessMappingStrategy.TAG, "user point set, correction not applied");
            return backlight;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public float getAutoBrightnessAdjustment() {
            return this.mAutoBrightnessAdjustment;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public boolean setAutoBrightnessAdjustment(float adjustment) {
            float adjustment2 = MathUtils.constrain(adjustment, -1.0f, 1.0f);
            if (adjustment2 == this.mAutoBrightnessAdjustment) {
                return false;
            }
            if (this.mLoggingEnabled) {
                Slog.d(BrightnessMappingStrategy.TAG, "setAutoBrightnessAdjustment: " + this.mAutoBrightnessAdjustment + " => " + adjustment2);
                BrightnessMappingStrategy.PLOG.start("auto-brightness adjustment");
            }
            this.mAutoBrightnessAdjustment = adjustment2;
            computeSpline();
            return true;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public float convertToNits(int backlight) {
            return this.mBacklightToNitsSpline.interpolate(normalizeAbsoluteBrightness(backlight));
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public void addUserDataPoint(float lux, float brightness) {
            float unadjustedBrightness = getUnadjustedBrightness(lux);
            if (this.mLoggingEnabled) {
                Slog.d(BrightnessMappingStrategy.TAG, "addUserDataPoint: (" + lux + "," + brightness + ")");
                BrightnessMappingStrategy.PLOG.start("add user data point").logPoint("user data point", lux, brightness).logPoint("current brightness", lux, unadjustedBrightness);
            }
            float adjustment = inferAutoBrightnessAdjustment(this.mMaxGamma, brightness, unadjustedBrightness);
            if (this.mLoggingEnabled) {
                Slog.d(BrightnessMappingStrategy.TAG, "addUserDataPoint: " + this.mAutoBrightnessAdjustment + " => " + adjustment);
            }
            this.mAutoBrightnessAdjustment = adjustment;
            this.mUserLux = lux;
            this.mUserBrightness = brightness;
            computeSpline();
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public void clearUserDataPoints() {
            if (this.mUserLux != -1.0f) {
                if (this.mLoggingEnabled) {
                    Slog.d(BrightnessMappingStrategy.TAG, "clearUserDataPoints: " + this.mAutoBrightnessAdjustment + " => 0");
                    BrightnessMappingStrategy.PLOG.start("clear user data points").logPoint("user data point", this.mUserLux, this.mUserBrightness);
                }
                this.mAutoBrightnessAdjustment = 0.0f;
                this.mUserLux = -1.0f;
                this.mUserBrightness = -1.0f;
                computeSpline();
            }
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public boolean hasUserDataPoints() {
            return this.mUserLux != -1.0f;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public boolean isDefaultConfig() {
            return this.mDefaultConfig.equals(this.mConfig);
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public BrightnessConfiguration getDefaultConfig() {
            return this.mDefaultConfig;
        }

        @Override // com.android.server.display.BrightnessMappingStrategy
        public void dump(PrintWriter pw) {
            pw.println("PhysicalMappingStrategy");
            pw.println("  mConfig=" + this.mConfig);
            pw.println("  mBrightnessSpline=" + this.mBrightnessSpline);
            pw.println("  mNitsToBacklightSpline=" + this.mNitsToBacklightSpline);
            pw.println("  mMaxGamma=" + this.mMaxGamma);
            pw.println("  mAutoBrightnessAdjustment=" + this.mAutoBrightnessAdjustment);
            pw.println("  mUserLux=" + this.mUserLux);
            pw.println("  mUserBrightness=" + this.mUserBrightness);
            pw.println("  mDefaultConfig=" + this.mDefaultConfig);
        }

        private void computeSpline() {
            Pair<float[], float[]> defaultCurve = this.mConfig.getCurve();
            float[] defaultLux = (float[]) defaultCurve.first;
            float[] defaultNits = (float[]) defaultCurve.second;
            float[] defaultBacklight = new float[defaultNits.length];
            for (int i = 0; i < defaultBacklight.length; i++) {
                defaultBacklight[i] = this.mNitsToBacklightSpline.interpolate(defaultNits[i]);
            }
            Pair<float[], float[]> curve = getAdjustedCurve(defaultLux, defaultBacklight, this.mUserLux, this.mUserBrightness, this.mAutoBrightnessAdjustment, this.mMaxGamma);
            float[] lux = (float[]) curve.first;
            float[] backlight = (float[]) curve.second;
            float[] nits = new float[backlight.length];
            for (int i2 = 0; i2 < nits.length; i2++) {
                nits[i2] = this.mBacklightToNitsSpline.interpolate(backlight[i2]);
            }
            this.mBrightnessSpline = Spline.createSpline(lux, nits);
        }

        private float getUnadjustedBrightness(float lux) {
            Pair<float[], float[]> curve = this.mConfig.getCurve();
            return this.mNitsToBacklightSpline.interpolate(Spline.createSpline((float[]) curve.first, (float[]) curve.second).interpolate(lux));
        }

        private float correctBrightness(float brightness, String packageName, int category) {
            BrightnessCorrection correction;
            BrightnessCorrection correction2;
            if (packageName != null && (correction2 = this.mConfig.getCorrectionByPackageName(packageName)) != null) {
                return correction2.apply(brightness);
            }
            if (category == -1 || (correction = this.mConfig.getCorrectionByCategory(category)) == null) {
                return brightness;
            }
            return correction.apply(brightness);
        }
    }
}
