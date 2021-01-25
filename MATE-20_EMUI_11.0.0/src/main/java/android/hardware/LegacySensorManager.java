package android.hardware;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import java.util.HashMap;

/* access modifiers changed from: package-private */
public final class LegacySensorManager {
    private static boolean sInitialized;
    private static int sRotation = 0;
    private static IWindowManager sWindowManager;
    private final HashMap<SensorListener, LegacyListener> mLegacyListenersMap = new HashMap<>();
    private final SensorManager mSensorManager;

    public LegacySensorManager(SensorManager sensorManager) {
        this.mSensorManager = sensorManager;
        synchronized (SensorManager.class) {
            if (!sInitialized) {
                sWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
                if (sWindowManager != null) {
                    try {
                        sRotation = sWindowManager.watchRotation(new IRotationWatcher.Stub() {
                            /* class android.hardware.LegacySensorManager.AnonymousClass1 */

                            @Override // android.view.IRotationWatcher
                            public void onRotationChanged(int rotation) {
                                LegacySensorManager.onRotationChanged(rotation);
                            }
                        }, 0);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    public int getSensors() {
        int result = 0;
        for (Sensor i : this.mSensorManager.getFullSensorList()) {
            int type = i.getType();
            if (type == 1) {
                result |= 2;
            } else if (type == 2) {
                result |= 8;
            } else if (type == 3) {
                result |= 129;
            }
        }
        return result;
    }

    public boolean registerListener(SensorListener listener, int sensors, int rate) {
        if (listener == null) {
            return false;
        }
        boolean result = registerLegacyListener(1, 3, listener, sensors, rate) || (registerLegacyListener(128, 3, listener, sensors, rate) || (registerLegacyListener(8, 2, listener, sensors, rate) || (registerLegacyListener(2, 1, listener, sensors, rate) || 0 != 0)));
        if (registerLegacyListener(4, 7, listener, sensors, rate) || result) {
            return true;
        }
        return false;
    }

    private boolean registerLegacyListener(int legacyType, int type, SensorListener listener, int sensors, int rate) {
        Sensor sensor;
        boolean result = false;
        if (!((sensors & legacyType) == 0 || (sensor = this.mSensorManager.getDefaultSensor(type)) == null)) {
            synchronized (this.mLegacyListenersMap) {
                LegacyListener legacyListener = this.mLegacyListenersMap.get(listener);
                if (legacyListener == null) {
                    legacyListener = new LegacyListener(listener);
                    this.mLegacyListenersMap.put(listener, legacyListener);
                }
                if (legacyListener.registerSensor(legacyType)) {
                    result = this.mSensorManager.registerListener(legacyListener, sensor, rate);
                } else {
                    result = true;
                }
            }
        }
        return result;
    }

    public void unregisterListener(SensorListener listener, int sensors) {
        if (listener != null) {
            unregisterLegacyListener(2, 1, listener, sensors);
            unregisterLegacyListener(8, 2, listener, sensors);
            unregisterLegacyListener(128, 3, listener, sensors);
            unregisterLegacyListener(1, 3, listener, sensors);
            unregisterLegacyListener(4, 7, listener, sensors);
        }
    }

    private void unregisterLegacyListener(int legacyType, int type, SensorListener listener, int sensors) {
        Sensor sensor;
        if ((sensors & legacyType) != 0 && (sensor = this.mSensorManager.getDefaultSensor(type)) != null) {
            synchronized (this.mLegacyListenersMap) {
                LegacyListener legacyListener = this.mLegacyListenersMap.get(listener);
                if (legacyListener != null && legacyListener.unregisterSensor(legacyType)) {
                    this.mSensorManager.unregisterListener(legacyListener, sensor);
                    if (!legacyListener.hasSensors()) {
                        this.mLegacyListenersMap.remove(listener);
                    }
                }
            }
        }
    }

    static void onRotationChanged(int rotation) {
        synchronized (SensorManager.class) {
            sRotation = rotation;
        }
    }

    static int getRotation() {
        int i;
        synchronized (SensorManager.class) {
            i = sRotation;
        }
        return i;
    }

    /* access modifiers changed from: private */
    public static final class LegacyListener implements SensorEventListener {
        private int mSensors;
        private SensorListener mTarget;
        private float[] mValues = new float[6];
        private final LmsFilter mYawfilter = new LmsFilter();

        LegacyListener(SensorListener target) {
            this.mTarget = target;
            this.mSensors = 0;
        }

        /* access modifiers changed from: package-private */
        public boolean registerSensor(int legacyType) {
            int i = this.mSensors;
            if ((i & legacyType) != 0) {
                return false;
            }
            boolean alreadyHasOrientationSensor = hasOrientationSensor(i);
            this.mSensors |= legacyType;
            if (!alreadyHasOrientationSensor || !hasOrientationSensor(legacyType)) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean unregisterSensor(int legacyType) {
            int i = this.mSensors;
            if ((i & legacyType) == 0) {
                return false;
            }
            this.mSensors = i & (~legacyType);
            if (!hasOrientationSensor(legacyType) || !hasOrientationSensor(this.mSensors)) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean hasSensors() {
            return this.mSensors != 0;
        }

        private static boolean hasOrientationSensor(int sensors) {
            return (sensors & 129) != 0;
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            try {
                this.mTarget.onAccuracyChanged(getLegacySensorType(sensor.getType()), accuracy);
            } catch (AbstractMethodError e) {
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            float[] v = this.mValues;
            v[0] = event.values[0];
            v[1] = event.values[1];
            v[2] = event.values[2];
            int type = event.sensor.getType();
            int legacyType = getLegacySensorType(type);
            mapSensorDataToWindow(legacyType, v, LegacySensorManager.getRotation());
            if (type == 3) {
                if ((this.mSensors & 128) != 0) {
                    this.mTarget.onSensorChanged(128, v);
                }
                if ((this.mSensors & 1) != 0) {
                    v[0] = this.mYawfilter.filter(event.timestamp, v[0]);
                    this.mTarget.onSensorChanged(1, v);
                    return;
                }
                return;
            }
            this.mTarget.onSensorChanged(legacyType, v);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
            if (r10 != 128) goto L_0x0056;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:4:0x0013, code lost:
            if (r10 != 128) goto L_0x001f;
         */
        /* JADX WARNING: Removed duplicated region for block: B:10:0x0032  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x005a  */
        /* JADX WARNING: Removed duplicated region for block: B:34:? A[RETURN, SYNTHETIC] */
        private void mapSensorDataToWindow(int sensor, float[] values, int orientation) {
            float x = values[0];
            float y = values[1];
            float z = values[2];
            if (sensor != 1) {
                if (sensor == 2) {
                    x = -x;
                    y = -y;
                    z = -z;
                } else if (sensor == 8) {
                    x = -x;
                    y = -y;
                }
                values[0] = x;
                values[1] = y;
                values[2] = z;
                values[3] = x;
                values[4] = y;
                values[5] = z;
                if ((orientation & 1) != 0) {
                    if (sensor != 1) {
                        if (sensor == 2 || sensor == 8) {
                            values[0] = -y;
                            values[1] = x;
                            values[2] = z;
                        }
                    }
                    values[0] = ((float) (x < 270.0f ? 90 : -270)) + x;
                    values[1] = z;
                    values[2] = y;
                }
                if ((orientation & 2) == 0) {
                    float x2 = values[0];
                    float y2 = values[1];
                    float z2 = values[2];
                    if (sensor != 1) {
                        if (sensor == 2 || sensor == 8) {
                            values[0] = -x2;
                            values[1] = -y2;
                            values[2] = z2;
                            return;
                        } else if (sensor != 128) {
                            return;
                        }
                    }
                    values[0] = x2 >= 180.0f ? x2 - 180.0f : 180.0f + x2;
                    values[1] = -y2;
                    values[2] = -z2;
                    return;
                }
                return;
            }
            z = -z;
            values[0] = x;
            values[1] = y;
            values[2] = z;
            values[3] = x;
            values[4] = y;
            values[5] = z;
            if ((orientation & 1) != 0) {
            }
            if ((orientation & 2) == 0) {
            }
        }

        private static int getLegacySensorType(int type) {
            if (type == 1) {
                return 2;
            }
            if (type == 2) {
                return 8;
            }
            if (type == 3) {
                return 128;
            }
            if (type != 7) {
                return 0;
            }
            return 4;
        }
    }

    private static final class LmsFilter {
        private static final int COUNT = 12;
        private static final float PREDICTION_RATIO = 0.33333334f;
        private static final float PREDICTION_TIME = 0.08f;
        private static final int SENSORS_RATE_MS = 20;
        private int mIndex = 12;
        private long[] mT = new long[24];
        private float[] mV = new float[24];

        public float filter(long time, float in) {
            float v = in;
            float v1 = this.mV[this.mIndex];
            if (v - v1 > 180.0f) {
                v -= 360.0f;
            } else if (v1 - v > 180.0f) {
                v += 360.0f;
            }
            this.mIndex++;
            if (this.mIndex >= 24) {
                this.mIndex = 12;
            }
            float[] fArr = this.mV;
            int i = this.mIndex;
            fArr[i] = v;
            long[] jArr = this.mT;
            jArr[i] = time;
            fArr[i - 12] = v;
            jArr[i - 12] = time;
            float E = 0.0f;
            float D = 0.0f;
            float C = 0.0f;
            float B = 0.0f;
            float A = 0.0f;
            for (int i2 = 0; i2 < 11; i2++) {
                int j = (this.mIndex - 1) - i2;
                float Z = this.mV[j];
                long[] jArr2 = this.mT;
                float T = ((float) (((jArr2[j] / 2) + (jArr2[j + 1] / 2)) - time)) * 1.0E-9f;
                float dT = ((float) (jArr2[j] - jArr2[j + 1])) * 1.0E-9f;
                float dT2 = dT * dT;
                A += Z * dT2;
                B += T * dT2 * T;
                C += T * dT2;
                D += T * dT2 * Z;
                E += dT2;
            }
            float b = ((A * B) + (C * D)) / ((E * B) + (C * C));
            float f = ((PREDICTION_TIME * (((E * b) - A) / C)) + b) * 0.0027777778f;
            if ((f >= 0.0f ? f : -f) >= 0.5f) {
                f = (f - ((float) Math.ceil((double) (0.5f + f)))) + 1.0f;
            }
            if (f < 0.0f) {
                f += 1.0f;
            }
            return f * 360.0f;
        }
    }
}
