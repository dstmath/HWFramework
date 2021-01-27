package com.android.server.display.whitebalance;

import android.util.Slog;
import java.io.PrintWriter;
import java.util.Arrays;

/* access modifiers changed from: package-private */
public class DisplayWhiteBalanceThrottler {
    protected static final String TAG = "DisplayWhiteBalanceThrottler";
    private float[] mBaseThresholds;
    private int mDecreaseDebounce;
    private float mDecreaseThreshold;
    private float[] mDecreaseThresholds;
    private int mIncreaseDebounce;
    private float mIncreaseThreshold;
    private float[] mIncreaseThresholds;
    private long mLastTime;
    private float mLastValue;
    protected boolean mLoggingEnabled = false;

    DisplayWhiteBalanceThrottler(int increaseDebounce, int decreaseDebounce, float[] baseThresholds, float[] increaseThresholds, float[] decreaseThresholds) {
        validateArguments((float) increaseDebounce, (float) decreaseDebounce, baseThresholds, increaseThresholds, decreaseThresholds);
        this.mIncreaseDebounce = increaseDebounce;
        this.mDecreaseDebounce = decreaseDebounce;
        this.mBaseThresholds = baseThresholds;
        this.mIncreaseThresholds = increaseThresholds;
        this.mDecreaseThresholds = decreaseThresholds;
        clear();
    }

    public boolean throttle(float value) {
        if (this.mLastTime != -1 && (tooSoon(value) || tooClose(value))) {
            return true;
        }
        computeThresholds(value);
        this.mLastTime = System.currentTimeMillis();
        this.mLastValue = value;
        return false;
    }

    public void clear() {
        this.mLastTime = -1;
        this.mIncreaseThreshold = -1.0f;
        this.mDecreaseThreshold = -1.0f;
        this.mLastValue = -1.0f;
    }

    public boolean setLoggingEnabled(boolean loggingEnabled) {
        if (this.mLoggingEnabled == loggingEnabled) {
            return false;
        }
        this.mLoggingEnabled = loggingEnabled;
        return true;
    }

    public void dump(PrintWriter writer) {
        writer.println("  DisplayWhiteBalanceThrottler");
        writer.println("    mLoggingEnabled=" + this.mLoggingEnabled);
        writer.println("    mIncreaseDebounce=" + this.mIncreaseDebounce);
        writer.println("    mDecreaseDebounce=" + this.mDecreaseDebounce);
        writer.println("    mLastTime=" + this.mLastTime);
        writer.println("    mBaseThresholds=" + Arrays.toString(this.mBaseThresholds));
        writer.println("    mIncreaseThresholds=" + Arrays.toString(this.mIncreaseThresholds));
        writer.println("    mDecreaseThresholds=" + Arrays.toString(this.mDecreaseThresholds));
        writer.println("    mIncreaseThreshold=" + this.mIncreaseThreshold);
        writer.println("    mDecreaseThreshold=" + this.mDecreaseThreshold);
        writer.println("    mLastValue=" + this.mLastValue);
    }

    private void validateArguments(float increaseDebounce, float decreaseDebounce, float[] baseThresholds, float[] increaseThresholds, float[] decreaseThresholds) {
        if (Float.isNaN(increaseDebounce) || increaseDebounce < 0.0f) {
            throw new IllegalArgumentException("increaseDebounce must be a non-negative number.");
        } else if (Float.isNaN(decreaseDebounce) || decreaseDebounce < 0.0f) {
            throw new IllegalArgumentException("decreaseDebounce must be a non-negative number.");
        } else if (!isValidMapping(baseThresholds, increaseThresholds)) {
            throw new IllegalArgumentException("baseThresholds to increaseThresholds is not a valid mapping.");
        } else if (!isValidMapping(baseThresholds, decreaseThresholds)) {
            throw new IllegalArgumentException("baseThresholds to decreaseThresholds is not a valid mapping.");
        }
    }

    private static boolean isValidMapping(float[] x, float[] y) {
        if (x == null || y == null || x.length == 0 || y.length == 0 || x.length != y.length) {
            return false;
        }
        float prevX = -1.0f;
        for (int i = 0; i < x.length; i++) {
            if (Float.isNaN(x[i]) || Float.isNaN(y[i]) || x[i] < 0.0f || prevX >= x[i]) {
                return false;
            }
            prevX = x[i];
        }
        return true;
    }

    private boolean tooSoon(float value) {
        long earliestTime;
        long time = System.currentTimeMillis();
        if (value > this.mLastValue) {
            earliestTime = this.mLastTime + ((long) this.mIncreaseDebounce);
        } else {
            earliestTime = this.mLastTime + ((long) this.mDecreaseDebounce);
        }
        boolean tooSoon = time < earliestTime;
        if (this.mLoggingEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append(tooSoon ? "too soon: " : "late enough: ");
            sb.append(time);
            sb.append(tooSoon ? " < " : " > ");
            sb.append(earliestTime);
            Slog.d(TAG, sb.toString());
        }
        return tooSoon;
    }

    private boolean tooClose(float value) {
        float threshold;
        boolean tooClose = true;
        if (value > this.mLastValue) {
            threshold = this.mIncreaseThreshold;
            if (value >= threshold) {
                tooClose = false;
            }
        } else {
            threshold = this.mDecreaseThreshold;
            if (value <= threshold) {
                tooClose = false;
            }
        }
        if (this.mLoggingEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append(tooClose ? "too close: " : "far enough: ");
            sb.append(value);
            sb.append(value > threshold ? " > " : " < ");
            sb.append(threshold);
            Slog.d(TAG, sb.toString());
        }
        return tooClose;
    }

    private void computeThresholds(float value) {
        int index = getHighestIndexBefore(value, this.mBaseThresholds);
        this.mIncreaseThreshold = (this.mIncreaseThresholds[index] + 1.0f) * value;
        this.mDecreaseThreshold = (1.0f - this.mDecreaseThresholds[index]) * value;
    }

    private int getHighestIndexBefore(float value, float[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= value) {
                return i;
            }
        }
        return values.length - 1;
    }
}
