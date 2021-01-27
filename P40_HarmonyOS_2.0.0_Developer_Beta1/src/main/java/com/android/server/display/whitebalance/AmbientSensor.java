package com.android.server.display.whitebalance;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.display.utils.History;
import java.io.PrintWriter;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public abstract class AmbientSensor {
    private static final int HISTORY_SIZE = 50;
    private boolean mEnabled;
    private int mEventsCount;
    private History mEventsHistory;
    private final Handler mHandler;
    private SensorEventListener mListener = new SensorEventListener() {
        /* class com.android.server.display.whitebalance.AmbientSensor.AnonymousClass1 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            AmbientSensor.this.handleNewEvent(event.values[0]);
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    protected boolean mLoggingEnabled;
    private int mRate;
    protected Sensor mSensor;
    protected final SensorManager mSensorManager;
    protected String mTag;

    /* access modifiers changed from: protected */
    public abstract void update(float f);

    AmbientSensor(String tag, Handler handler, SensorManager sensorManager, int rate) {
        validateArguments(handler, sensorManager, rate);
        this.mTag = tag;
        this.mLoggingEnabled = false;
        this.mHandler = handler;
        this.mSensorManager = sensorManager;
        this.mEnabled = false;
        this.mRate = rate;
        this.mEventsCount = 0;
        this.mEventsHistory = new History(50);
    }

    public boolean setEnabled(boolean enabled) {
        if (enabled) {
            return enable();
        }
        return disable();
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
        writer.println("    mHandler=" + this.mHandler);
        writer.println("    mSensorManager=" + this.mSensorManager);
        writer.println("    mSensor=" + this.mSensor);
        writer.println("    mEnabled=" + this.mEnabled);
        writer.println("    mRate=" + this.mRate);
        writer.println("    mEventsCount=" + this.mEventsCount);
        writer.println("    mEventsHistory=" + this.mEventsHistory);
    }

    private static void validateArguments(Handler handler, SensorManager sensorManager, int rate) {
        Preconditions.checkNotNull(handler, "handler cannot be null");
        Preconditions.checkNotNull(sensorManager, "sensorManager cannot be null");
        if (rate <= 0) {
            throw new IllegalArgumentException("rate must be positive");
        }
    }

    private boolean enable() {
        if (this.mEnabled) {
            return false;
        }
        if (this.mLoggingEnabled) {
            Slog.d(this.mTag, "enabling");
        }
        this.mEnabled = true;
        startListening();
        return true;
    }

    private boolean disable() {
        if (!this.mEnabled) {
            return false;
        }
        if (this.mLoggingEnabled) {
            Slog.d(this.mTag, "disabling");
        }
        this.mEnabled = false;
        this.mEventsCount = 0;
        stopListening();
        return true;
    }

    private void startListening() {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager != null) {
            sensorManager.registerListener(this.mListener, this.mSensor, this.mRate * 1000, this.mHandler);
        }
    }

    private void stopListening() {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this.mListener);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNewEvent(float value) {
        if (this.mEnabled) {
            if (this.mLoggingEnabled) {
                Slog.d(this.mTag, "handle new event: " + value);
            }
            this.mEventsCount++;
            this.mEventsHistory.add(value);
            update(value);
        }
    }

    /* access modifiers changed from: package-private */
    public static class AmbientBrightnessSensor extends AmbientSensor {
        private static final String TAG = "AmbientBrightnessSensor";
        private Callbacks mCallbacks;

        /* access modifiers changed from: package-private */
        public interface Callbacks {
            void onAmbientBrightnessChanged(float f);
        }

        AmbientBrightnessSensor(Handler handler, SensorManager sensorManager, int rate) {
            super(TAG, handler, sensorManager, rate);
            this.mSensor = this.mSensorManager.getDefaultSensor(5);
            if (this.mSensor != null) {
                this.mCallbacks = null;
                return;
            }
            throw new IllegalStateException("cannot find light sensor");
        }

        public boolean setCallbacks(Callbacks callbacks) {
            if (this.mCallbacks == callbacks) {
                return false;
            }
            this.mCallbacks = callbacks;
            return true;
        }

        @Override // com.android.server.display.whitebalance.AmbientSensor
        public void dump(PrintWriter writer) {
            AmbientSensor.super.dump(writer);
            writer.println("    mCallbacks=" + this.mCallbacks);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.whitebalance.AmbientSensor
        public void update(float value) {
            Callbacks callbacks = this.mCallbacks;
            if (callbacks != null) {
                callbacks.onAmbientBrightnessChanged(value);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class AmbientColorTemperatureSensor extends AmbientSensor {
        private static final String TAG = "AmbientColorTemperatureSensor";
        private Callbacks mCallbacks;

        /* access modifiers changed from: package-private */
        public interface Callbacks {
            void onAmbientColorTemperatureChanged(float f);
        }

        AmbientColorTemperatureSensor(Handler handler, SensorManager sensorManager, String name, int rate) {
            super(TAG, handler, sensorManager, rate);
            this.mSensor = null;
            Iterator<Sensor> it = this.mSensorManager.getSensorList(-1).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Sensor sensor = it.next();
                if (sensor.getStringType().equals(name)) {
                    this.mSensor = sensor;
                    break;
                }
            }
            if (this.mSensor != null) {
                this.mCallbacks = null;
                return;
            }
            throw new IllegalStateException("cannot find sensor " + name);
        }

        public boolean setCallbacks(Callbacks callbacks) {
            if (this.mCallbacks == callbacks) {
                return false;
            }
            this.mCallbacks = callbacks;
            return true;
        }

        @Override // com.android.server.display.whitebalance.AmbientSensor
        public void dump(PrintWriter writer) {
            AmbientSensor.super.dump(writer);
            writer.println("    mCallbacks=" + this.mCallbacks);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.display.whitebalance.AmbientSensor
        public void update(float value) {
            Callbacks callbacks = this.mCallbacks;
            if (callbacks != null) {
                callbacks.onAmbientColorTemperatureChanged(value);
            }
        }
    }
}
