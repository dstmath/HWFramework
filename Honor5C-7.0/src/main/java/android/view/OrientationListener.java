package android.view;

import android.content.Context;
import android.hardware.SensorListener;

@Deprecated
public abstract class OrientationListener implements SensorListener {
    public static final int ORIENTATION_UNKNOWN = -1;
    private OrientationEventListener mOrientationEventLis;

    class OrientationEventListenerInternal extends OrientationEventListener {
        OrientationEventListenerInternal(Context context) {
            super(context);
        }

        OrientationEventListenerInternal(Context context, int rate) {
            super(context, rate);
            registerListener(OrientationListener.this);
        }

        public void onOrientationChanged(int orientation) {
            OrientationListener.this.onOrientationChanged(orientation);
        }
    }

    public abstract void onOrientationChanged(int i);

    public OrientationListener(Context context) {
        this.mOrientationEventLis = new OrientationEventListenerInternal(context);
    }

    public OrientationListener(Context context, int rate) {
        this.mOrientationEventLis = new OrientationEventListenerInternal(context, rate);
    }

    public void enable() {
        this.mOrientationEventLis.enable();
    }

    public void disable() {
        this.mOrientationEventLis.disable();
    }

    public void onAccuracyChanged(int sensor, int accuracy) {
    }

    public void onSensorChanged(int sensor, float[] values) {
    }
}
