package android.view;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.android.internal.telephony.HwModemCapability;

public abstract class OrientationEventListener {
    private static final boolean DEBUG = false;
    public static final int ORIENTATION_UNKNOWN = -1;
    private static final String TAG = "OrientationEventListener";
    private static final boolean localLOGV = false;
    private boolean mEnabled;
    private OrientationListener mOldListener;
    private int mOrientation;
    private int mRate;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;

    class SensorEventListenerImpl implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        SensorEventListenerImpl() {
        }

        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            int orientation = OrientationEventListener.ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];
            if (4.0f * ((X * X) + (Y * Y)) >= Z * Z) {
                orientation = 90 - Math.round(((float) Math.atan2((double) (-Y), (double) X)) * 57.29578f);
                while (orientation >= HwModemCapability.MODEM_CAP_MAX) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += HwModemCapability.MODEM_CAP_MAX;
                }
            }
            if (OrientationEventListener.this.mOldListener != null) {
                OrientationEventListener.this.mOldListener.onSensorChanged(_DATA_Y, event.values);
            }
            if (orientation != OrientationEventListener.this.mOrientation) {
                OrientationEventListener.this.mOrientation = orientation;
                OrientationEventListener.this.onOrientationChanged(orientation);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public abstract void onOrientationChanged(int i);

    public OrientationEventListener(Context context) {
        this(context, 3);
    }

    public OrientationEventListener(Context context, int rate) {
        this.mOrientation = ORIENTATION_UNKNOWN;
        this.mEnabled = DEBUG;
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mRate = rate;
        this.mSensor = this.mSensorManager.getDefaultSensor(1);
        if (this.mSensor != null) {
            this.mSensorEventListener = new SensorEventListenerImpl();
        }
    }

    void registerListener(OrientationListener lis) {
        this.mOldListener = lis;
    }

    public void enable() {
        if (this.mSensor == null) {
            Log.w(TAG, "Cannot detect sensors. Not enabled");
            return;
        }
        if (!this.mEnabled) {
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mSensor, this.mRate);
            this.mEnabled = true;
        }
    }

    public void disable() {
        if (this.mSensor == null) {
            Log.w(TAG, "Cannot detect sensors. Invalid disable");
            return;
        }
        if (this.mEnabled) {
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mEnabled = DEBUG;
        }
    }

    public boolean canDetectOrientation() {
        return this.mSensor != null ? true : DEBUG;
    }
}
