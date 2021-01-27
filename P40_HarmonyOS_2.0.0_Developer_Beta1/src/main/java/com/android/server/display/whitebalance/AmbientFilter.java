package com.android.server.display.whitebalance;

import android.util.Slog;
import com.android.server.display.utils.RollingBuffer;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public abstract class AmbientFilter {
    protected static final boolean DEBUG = false;
    private final RollingBuffer mBuffer;
    private final int mHorizon;
    protected boolean mLoggingEnabled = false;
    protected final String mTag;

    /* access modifiers changed from: protected */
    public abstract float filter(long j, RollingBuffer rollingBuffer);

    AmbientFilter(String tag, int horizon) {
        validateArguments(horizon);
        this.mTag = tag;
        this.mHorizon = horizon;
        this.mBuffer = new RollingBuffer();
    }

    public boolean addValue(long time, float value) {
        if (value < 0.0f) {
            return false;
        }
        truncateOldValues(time);
        if (this.mLoggingEnabled) {
            String str = this.mTag;
            Slog.d(str, "add value: " + value + " @ " + time);
        }
        this.mBuffer.add(time, value);
        return true;
    }

    public float getEstimate(long time) {
        truncateOldValues(time);
        float value = filter(time, this.mBuffer);
        if (this.mLoggingEnabled) {
            String str = this.mTag;
            Slog.d(str, "get estimate: " + value + " @ " + time);
        }
        return value;
    }

    public void clear() {
        this.mBuffer.clear();
    }

    public boolean setLoggingEnabled(boolean loggingEnabled) {
        if (this.mLoggingEnabled == loggingEnabled) {
            return false;
        }
        this.mLoggingEnabled = loggingEnabled;
        return true;
    }

    public void dump(PrintWriter writer) {
        writer.println("  " + this.mTag);
        writer.println("    mLoggingEnabled=" + this.mLoggingEnabled);
        writer.println("    mHorizon=" + this.mHorizon);
        writer.println("    mBuffer=" + this.mBuffer);
    }

    private void validateArguments(int horizon) {
        if (horizon <= 0) {
            throw new IllegalArgumentException("horizon must be positive");
        }
    }

    private void truncateOldValues(long time) {
        this.mBuffer.truncate(time - ((long) this.mHorizon));
    }

    /* access modifiers changed from: package-private */
    public static class WeightedMovingAverageAmbientFilter extends AmbientFilter {
        private static final int PREDICTION_TIME = 100;
        private final float mIntercept;

        WeightedMovingAverageAmbientFilter(String tag, int horizon, float intercept) {
            super(tag, horizon);
            validateArguments(intercept);
            this.mIntercept = intercept;
        }

        @Override // com.android.server.display.whitebalance.AmbientFilter
        public void dump(PrintWriter writer) {
            AmbientFilter.super.dump(writer);
            writer.println("    mIntercept=" + this.mIntercept);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.whitebalance.AmbientFilter
        public float filter(long time, RollingBuffer buffer) {
            if (buffer.isEmpty()) {
                return -1.0f;
            }
            float total = 0.0f;
            float totalWeight = 0.0f;
            float[] weights = getWeights(time, buffer);
            for (int i = 0; i < weights.length; i++) {
                float value = buffer.getValue(i);
                float weight = weights[i];
                total += weight * value;
                totalWeight += weight;
            }
            if (totalWeight == 0.0f) {
                return buffer.getValue(buffer.size() - 1);
            }
            return total / totalWeight;
        }

        private void validateArguments(float intercept) {
            if (Float.isNaN(intercept) || intercept < 0.0f) {
                throw new IllegalArgumentException("intercept must be a non-negative number");
            }
        }

        private float[] getWeights(long time, RollingBuffer buffer) {
            float[] weights = new float[buffer.size()];
            long startTime = buffer.getTime(0);
            float previousTime = 0.0f;
            for (int i = 1; i < weights.length; i++) {
                float currentTime = ((float) (buffer.getTime(i) - startTime)) / 1000.0f;
                weights[i - 1] = calculateIntegral(previousTime, currentTime);
                previousTime = currentTime;
            }
            weights[weights.length - 1] = calculateIntegral(previousTime, ((float) ((100 + time) - startTime)) / 1000.0f);
            return weights;
        }

        private float calculateIntegral(float from, float to) {
            return antiderivative(to) - antiderivative(from);
        }

        private float antiderivative(float x) {
            return (0.5f * x * x) + (this.mIntercept * x);
        }
    }
}
