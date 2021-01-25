package com.android.server.display.whitebalance;

import android.util.Slog;
import android.util.Spline;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.display.color.ColorDisplayService;
import com.android.server.display.utils.History;
import com.android.server.display.whitebalance.AmbientSensor;
import java.io.PrintWriter;

public class DisplayWhiteBalanceController implements AmbientSensor.AmbientBrightnessSensor.Callbacks, AmbientSensor.AmbientColorTemperatureSensor.Callbacks {
    private static final int HISTORY_SIZE = 50;
    protected static final String TAG = "DisplayWhiteBalanceController";
    private float mAmbientColorTemperature;
    private History mAmbientColorTemperatureHistory;
    private float mAmbientColorTemperatureOverride;
    private Spline.LinearSpline mAmbientToDisplayColorTemperatureSpline;
    private AmbientFilter mBrightnessFilter;
    private AmbientSensor.AmbientBrightnessSensor mBrightnessSensor;
    private Callbacks mCallbacks = null;
    private ColorDisplayService.ColorDisplayServiceInternal mColorDisplayServiceInternal;
    private AmbientFilter mColorTemperatureFilter;
    private AmbientSensor.AmbientColorTemperatureSensor mColorTemperatureSensor;
    private boolean mEnabled = false;
    private float mLastAmbientColorTemperature;
    protected boolean mLoggingEnabled = false;
    private final float mLowLightAmbientBrightnessThreshold;
    private final float mLowLightAmbientColorTemperature;
    private float mPendingAmbientColorTemperature;
    private DisplayWhiteBalanceThrottler mThrottler;

    public interface Callbacks {
        void updateWhiteBalance();
    }

    public DisplayWhiteBalanceController(AmbientSensor.AmbientBrightnessSensor brightnessSensor, AmbientFilter brightnessFilter, AmbientSensor.AmbientColorTemperatureSensor colorTemperatureSensor, AmbientFilter colorTemperatureFilter, DisplayWhiteBalanceThrottler throttler, float lowLightAmbientBrightnessThreshold, float lowLightAmbientColorTemperature, float[] ambientColorTemperatures, float[] displayColorTemperatures) {
        validateArguments(brightnessSensor, brightnessFilter, colorTemperatureSensor, colorTemperatureFilter, throttler);
        this.mBrightnessSensor = brightnessSensor;
        this.mBrightnessFilter = brightnessFilter;
        this.mColorTemperatureSensor = colorTemperatureSensor;
        this.mColorTemperatureFilter = colorTemperatureFilter;
        this.mThrottler = throttler;
        this.mLowLightAmbientBrightnessThreshold = lowLightAmbientBrightnessThreshold;
        this.mLowLightAmbientColorTemperature = lowLightAmbientColorTemperature;
        this.mAmbientColorTemperature = -1.0f;
        this.mPendingAmbientColorTemperature = -1.0f;
        this.mLastAmbientColorTemperature = -1.0f;
        this.mAmbientColorTemperatureHistory = new History(50);
        this.mAmbientColorTemperatureOverride = -1.0f;
        try {
            this.mAmbientToDisplayColorTemperatureSpline = new Spline.LinearSpline(ambientColorTemperatures, displayColorTemperatures);
        } catch (Exception e) {
            this.mAmbientToDisplayColorTemperatureSpline = null;
        }
        this.mColorDisplayServiceInternal = (ColorDisplayService.ColorDisplayServiceInternal) LocalServices.getService(ColorDisplayService.ColorDisplayServiceInternal.class);
    }

    public boolean setEnabled(boolean enabled) {
        if (enabled) {
            return enable();
        }
        return disable();
    }

    public boolean setCallbacks(Callbacks callbacks) {
        if (this.mCallbacks == callbacks) {
            return false;
        }
        this.mCallbacks = callbacks;
        return true;
    }

    public boolean setLoggingEnabled(boolean loggingEnabled) {
        if (this.mLoggingEnabled == loggingEnabled) {
            return false;
        }
        this.mLoggingEnabled = loggingEnabled;
        this.mBrightnessSensor.setLoggingEnabled(loggingEnabled);
        this.mBrightnessFilter.setLoggingEnabled(loggingEnabled);
        this.mColorTemperatureSensor.setLoggingEnabled(loggingEnabled);
        this.mColorTemperatureFilter.setLoggingEnabled(loggingEnabled);
        this.mThrottler.setLoggingEnabled(loggingEnabled);
        return true;
    }

    public boolean setAmbientColorTemperatureOverride(float ambientColorTemperatureOverride) {
        if (this.mAmbientColorTemperatureOverride == ambientColorTemperatureOverride) {
            return false;
        }
        this.mAmbientColorTemperatureOverride = ambientColorTemperatureOverride;
        return true;
    }

    public void dump(PrintWriter writer) {
        writer.println(TAG);
        writer.println("  mLoggingEnabled=" + this.mLoggingEnabled);
        writer.println("  mEnabled=" + this.mEnabled);
        writer.println("  mCallbacks=" + this.mCallbacks);
        this.mBrightnessSensor.dump(writer);
        this.mBrightnessFilter.dump(writer);
        this.mColorTemperatureSensor.dump(writer);
        this.mColorTemperatureFilter.dump(writer);
        this.mThrottler.dump(writer);
        writer.println("  mLowLightAmbientBrightnessThreshold=" + this.mLowLightAmbientBrightnessThreshold);
        writer.println("  mLowLightAmbientColorTemperature=" + this.mLowLightAmbientColorTemperature);
        writer.println("  mAmbientColorTemperature=" + this.mAmbientColorTemperature);
        writer.println("  mPendingAmbientColorTemperature=" + this.mPendingAmbientColorTemperature);
        writer.println("  mLastAmbientColorTemperature=" + this.mLastAmbientColorTemperature);
        writer.println("  mAmbientColorTemperatureHistory=" + this.mAmbientColorTemperatureHistory);
        writer.println("  mAmbientColorTemperatureOverride=" + this.mAmbientColorTemperatureOverride);
        writer.println("  mAmbientToDisplayColorTemperatureSpline=" + this.mAmbientToDisplayColorTemperatureSpline);
    }

    @Override // com.android.server.display.whitebalance.AmbientSensor.AmbientBrightnessSensor.Callbacks
    public void onAmbientBrightnessChanged(float value) {
        this.mBrightnessFilter.addValue(System.currentTimeMillis(), value);
        updateAmbientColorTemperature();
    }

    @Override // com.android.server.display.whitebalance.AmbientSensor.AmbientColorTemperatureSensor.Callbacks
    public void onAmbientColorTemperatureChanged(float value) {
        this.mColorTemperatureFilter.addValue(System.currentTimeMillis(), value);
        updateAmbientColorTemperature();
    }

    public void updateAmbientColorTemperature() {
        long time = System.currentTimeMillis();
        float ambientColorTemperature = this.mColorTemperatureFilter.getEstimate(time);
        Spline.LinearSpline linearSpline = this.mAmbientToDisplayColorTemperatureSpline;
        if (!(linearSpline == null || ambientColorTemperature == -1.0f)) {
            ambientColorTemperature = linearSpline.interpolate(ambientColorTemperature);
        }
        float ambientBrightness = this.mBrightnessFilter.getEstimate(time);
        if (ambientBrightness < this.mLowLightAmbientBrightnessThreshold) {
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "low light ambient brightness: " + ambientBrightness + " < " + this.mLowLightAmbientBrightnessThreshold + ", falling back to fixed ambient color temperature: " + ambientColorTemperature + " => " + this.mLowLightAmbientColorTemperature);
            }
            ambientColorTemperature = this.mLowLightAmbientColorTemperature;
        }
        if (this.mAmbientColorTemperatureOverride != -1.0f) {
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "override ambient color temperature: " + ambientColorTemperature + " => " + this.mAmbientColorTemperatureOverride);
            }
            ambientColorTemperature = this.mAmbientColorTemperatureOverride;
        }
        if (ambientColorTemperature != -1.0f && !this.mThrottler.throttle(ambientColorTemperature)) {
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "pending ambient color temperature: " + ambientColorTemperature);
            }
            this.mPendingAmbientColorTemperature = ambientColorTemperature;
            Callbacks callbacks = this.mCallbacks;
            if (callbacks != null) {
                callbacks.updateWhiteBalance();
            }
        }
    }

    public void updateDisplayColorTemperature() {
        float ambientColorTemperature = -1.0f;
        if (this.mAmbientColorTemperature == -1.0f && this.mPendingAmbientColorTemperature == -1.0f) {
            ambientColorTemperature = this.mLastAmbientColorTemperature;
        }
        float f = this.mPendingAmbientColorTemperature;
        if (!(f == -1.0f || f == this.mAmbientColorTemperature)) {
            ambientColorTemperature = this.mPendingAmbientColorTemperature;
        }
        if (ambientColorTemperature != -1.0f) {
            this.mAmbientColorTemperature = ambientColorTemperature;
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "ambient color temperature: " + this.mAmbientColorTemperature);
            }
            this.mPendingAmbientColorTemperature = -1.0f;
            this.mAmbientColorTemperatureHistory.add(this.mAmbientColorTemperature);
            this.mColorDisplayServiceInternal.setDisplayWhiteBalanceColorTemperature((int) this.mAmbientColorTemperature);
            this.mLastAmbientColorTemperature = this.mAmbientColorTemperature;
        }
    }

    private void validateArguments(AmbientSensor.AmbientBrightnessSensor brightnessSensor, AmbientFilter brightnessFilter, AmbientSensor.AmbientColorTemperatureSensor colorTemperatureSensor, AmbientFilter colorTemperatureFilter, DisplayWhiteBalanceThrottler throttler) {
        Preconditions.checkNotNull(brightnessSensor, "brightnessSensor must not be null");
        Preconditions.checkNotNull(brightnessFilter, "brightnessFilter must not be null");
        Preconditions.checkNotNull(colorTemperatureSensor, "colorTemperatureSensor must not be null");
        Preconditions.checkNotNull(colorTemperatureFilter, "colorTemperatureFilter must not be null");
        Preconditions.checkNotNull(throttler, "throttler cannot be null");
    }

    private boolean enable() {
        if (this.mEnabled) {
            return false;
        }
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "enabling");
        }
        this.mEnabled = true;
        this.mBrightnessSensor.setEnabled(true);
        this.mColorTemperatureSensor.setEnabled(true);
        return true;
    }

    private boolean disable() {
        if (!this.mEnabled) {
            return false;
        }
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "disabling");
        }
        this.mEnabled = false;
        this.mBrightnessSensor.setEnabled(false);
        this.mBrightnessFilter.clear();
        this.mColorTemperatureSensor.setEnabled(false);
        this.mColorTemperatureFilter.clear();
        this.mThrottler.clear();
        this.mAmbientColorTemperature = -1.0f;
        this.mPendingAmbientColorTemperature = -1.0f;
        this.mColorDisplayServiceInternal.resetDisplayWhiteBalanceColorTemperature();
        return true;
    }
}
