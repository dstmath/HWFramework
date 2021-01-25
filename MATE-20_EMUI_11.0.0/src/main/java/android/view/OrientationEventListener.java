package android.view;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Log;

public abstract class OrientationEventListener {
    private static final boolean DEBUG = false;
    public static final int ORIENTATION_UNKNOWN = -1;
    private static final String TAG = "OrientationEventListener";
    private static final boolean localLOGV = false;
    private Context mContext;
    private boolean mEnabled;
    private OrientationListener mOldListener;
    private int mOrientation;
    private int mRate;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;

    public abstract void onOrientationChanged(int i);

    public OrientationEventListener(Context context) {
        this(context, 3);
    }

    public OrientationEventListener(Context context, int rate) {
        this.mOrientation = -1;
        this.mEnabled = false;
        this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.mRate = rate;
        this.mSensor = this.mSensorManager.getDefaultSensor(1);
        if (this.mSensor != null) {
            this.mSensorEventListener = new SensorEventListenerImpl();
        }
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public void registerListener(OrientationListener lis) {
        this.mOldListener = lis;
    }

    public void enable() {
        Sensor sensor = this.mSensor;
        if (sensor == null) {
            Log.w(TAG, "Cannot detect sensors. Not enabled");
        } else if (!this.mEnabled) {
            this.mSensorManager.registerListener(this.mSensorEventListener, sensor, this.mRate);
            this.mEnabled = true;
        }
    }

    public void disable() {
        if (this.mSensor == null) {
            Log.w(TAG, "Cannot detect sensors. Invalid disable");
        } else if (this.mEnabled) {
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mEnabled = false;
        }
    }

    class SensorEventListenerImpl implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        SensorEventListenerImpl() {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (!HwPCUtils.isValidExtDisplayId(OrientationEventListener.this.mContext)) {
                float[] values = event.values;
                int orientation = -1;
                float X = -values[0];
                float Y = -values[1];
                float Z = -values[2];
                float magnitude = (X * X) + (Y * Y);
                if (HwMwUtils.IS_FOLD_SCREEN_DEVICE || !HwMwUtils.ENABLED || !HwMwUtils.performPolicy(82, false).getBoolean(HwMwUtils.RESULT_IS_DISABLE_SENSOR, false)) {
                    if (4.0f * magnitude >= Z * Z) {
                        orientation = 90 - Math.round(((float) Math.atan2((double) (-Y), (double) X)) * 57.29578f);
                        while (orientation >= 360) {
                            orientation -= 360;
                        }
                        while (orientation < 0) {
                            orientation += 360;
                        }
                    }
                    if (OrientationEventListener.this.mOldListener != null) {
                        OrientationEventListener.this.mOldListener.onSensorChanged(1, event.values);
                    }
                    if (orientation != OrientationEventListener.this.mOrientation) {
                        OrientationEventListener.this.mOrientation = orientation;
                        OrientationEventListener.this.onOrientationChanged(orientation);
                        return;
                    }
                    return;
                }
                OrientationEventListener.this.onOrientationChanged(0);
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public boolean canDetectOrientation() {
        return this.mSensor != null;
    }
}
