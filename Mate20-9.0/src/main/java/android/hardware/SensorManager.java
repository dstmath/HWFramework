package android.hardware;

import android.annotation.SystemApi;
import android.media.HwMediaMonitorUtils;
import android.os.Handler;
import android.os.MemoryFile;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SensorManager {
    public static final int AXIS_MINUS_X = 129;
    public static final int AXIS_MINUS_Y = 130;
    public static final int AXIS_MINUS_Z = 131;
    public static final int AXIS_X = 1;
    public static final int AXIS_Y = 2;
    public static final int AXIS_Z = 3;
    @Deprecated
    public static final int DATA_X = 0;
    @Deprecated
    public static final int DATA_Y = 1;
    @Deprecated
    public static final int DATA_Z = 2;
    public static final float GRAVITY_DEATH_STAR_I = 3.5303614E-7f;
    public static final float GRAVITY_EARTH = 9.80665f;
    public static final float GRAVITY_JUPITER = 23.12f;
    public static final float GRAVITY_MARS = 3.71f;
    public static final float GRAVITY_MERCURY = 3.7f;
    public static final float GRAVITY_MOON = 1.6f;
    public static final float GRAVITY_NEPTUNE = 11.0f;
    public static final float GRAVITY_PLUTO = 0.6f;
    public static final float GRAVITY_SATURN = 8.96f;
    public static final float GRAVITY_SUN = 275.0f;
    public static final float GRAVITY_THE_ISLAND = 4.815162f;
    public static final float GRAVITY_URANUS = 8.69f;
    public static final float GRAVITY_VENUS = 8.87f;
    public static final float LIGHT_CLOUDY = 100.0f;
    public static final float LIGHT_FULLMOON = 0.25f;
    public static final float LIGHT_NO_MOON = 0.001f;
    public static final float LIGHT_OVERCAST = 10000.0f;
    public static final float LIGHT_SHADE = 20000.0f;
    public static final float LIGHT_SUNLIGHT = 110000.0f;
    public static final float LIGHT_SUNLIGHT_MAX = 120000.0f;
    public static final float LIGHT_SUNRISE = 400.0f;
    public static final float MAGNETIC_FIELD_EARTH_MAX = 60.0f;
    public static final float MAGNETIC_FIELD_EARTH_MIN = 30.0f;
    public static final float PRESSURE_STANDARD_ATMOSPHERE = 1013.25f;
    @Deprecated
    public static final int RAW_DATA_INDEX = 3;
    @Deprecated
    public static final int RAW_DATA_X = 3;
    @Deprecated
    public static final int RAW_DATA_Y = 4;
    @Deprecated
    public static final int RAW_DATA_Z = 5;
    @Deprecated
    public static final int SENSOR_ACCELEROMETER = 2;
    @Deprecated
    public static final int SENSOR_ALL = 127;
    public static final int SENSOR_DELAY_FASTEST = 0;
    public static final int SENSOR_DELAY_GAME = 1;
    public static final int SENSOR_DELAY_NORMAL = 3;
    public static final int SENSOR_DELAY_UI = 2;
    @Deprecated
    public static final int SENSOR_LIGHT = 16;
    @Deprecated
    public static final int SENSOR_MAGNETIC_FIELD = 8;
    @Deprecated
    public static final int SENSOR_MAX = 64;
    @Deprecated
    public static final int SENSOR_MIN = 1;
    @Deprecated
    public static final int SENSOR_ORIENTATION = 1;
    @Deprecated
    public static final int SENSOR_ORIENTATION_RAW = 128;
    @Deprecated
    public static final int SENSOR_PROXIMITY = 32;
    public static final int SENSOR_STATUS_ACCURACY_HIGH = 3;
    public static final int SENSOR_STATUS_ACCURACY_LOW = 1;
    public static final int SENSOR_STATUS_ACCURACY_MEDIUM = 2;
    public static final int SENSOR_STATUS_NO_CONTACT = -1;
    public static final int SENSOR_STATUS_UNRELIABLE = 0;
    @Deprecated
    public static final int SENSOR_TEMPERATURE = 4;
    @Deprecated
    public static final int SENSOR_TRICORDER = 64;
    public static final float STANDARD_GRAVITY = 9.80665f;
    protected static final String TAG = "SensorManager";
    private static final float[] sTempMatrix = new float[16];
    private LegacySensorManager mLegacySensorManager;
    private final SparseArray<List<Sensor>> mSensorListByType = new SparseArray<>();

    public static abstract class DynamicSensorCallback {
        public void onDynamicSensorConnected(Sensor sensor) {
        }

        public void onDynamicSensorDisconnected(Sensor sensor) {
        }
    }

    /* access modifiers changed from: protected */
    public abstract boolean cancelTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor, boolean z);

    /* access modifiers changed from: protected */
    public abstract int configureDirectChannelImpl(SensorDirectChannel sensorDirectChannel, Sensor sensor, int i);

    /* access modifiers changed from: protected */
    public abstract SensorDirectChannel createDirectChannelImpl(MemoryFile memoryFile, HardwareBuffer hardwareBuffer);

    /* access modifiers changed from: protected */
    public abstract void destroyDirectChannelImpl(SensorDirectChannel sensorDirectChannel);

    /* access modifiers changed from: protected */
    public abstract boolean flushImpl(SensorEventListener sensorEventListener);

    /* access modifiers changed from: protected */
    public abstract List<Sensor> getFullDynamicSensorList();

    /* access modifiers changed from: protected */
    public abstract List<Sensor> getFullSensorList();

    /* access modifiers changed from: protected */
    public abstract int hwSetSensorConfigImpl(String str);

    /* access modifiers changed from: protected */
    public abstract boolean initDataInjectionImpl(boolean z);

    /* access modifiers changed from: protected */
    public abstract boolean injectSensorDataImpl(Sensor sensor, float[] fArr, int i, long j);

    /* access modifiers changed from: protected */
    public abstract void registerDynamicSensorCallbackImpl(DynamicSensorCallback dynamicSensorCallback, Handler handler);

    /* access modifiers changed from: protected */
    public abstract boolean registerListenerImpl(SensorEventListener sensorEventListener, Sensor sensor, int i, Handler handler, int i2, int i3);

    /* access modifiers changed from: protected */
    public abstract boolean requestTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor);

    /* access modifiers changed from: protected */
    public abstract boolean setOperationParameterImpl(SensorAdditionalInfo sensorAdditionalInfo);

    /* access modifiers changed from: protected */
    public abstract boolean supportSensorFeatureImpl(int i);

    /* access modifiers changed from: protected */
    public abstract void unregisterDynamicSensorCallbackImpl(DynamicSensorCallback dynamicSensorCallback);

    /* access modifiers changed from: protected */
    public abstract void unregisterListenerImpl(SensorEventListener sensorEventListener, Sensor sensor);

    @Deprecated
    public int getSensors() {
        return getLegacySensorManager().getSensors();
    }

    public List<Sensor> getSensorList(int type) {
        List<Sensor> list;
        List<Sensor> list2;
        List<Sensor> fullList = getFullSensorList();
        synchronized (this.mSensorListByType) {
            list = this.mSensorListByType.get(type);
            if (list == null) {
                if (type == -1) {
                    list2 = fullList;
                } else {
                    list2 = new ArrayList<>();
                    for (Sensor i : fullList) {
                        if (i.getType() == type) {
                            list2.add(i);
                        }
                    }
                }
                list = Collections.unmodifiableList(list2);
                this.mSensorListByType.append(type, list);
            }
        }
        return list;
    }

    public List<Sensor> getDynamicSensorList(int type) {
        List<Sensor> fullList = getFullDynamicSensorList();
        if (type == -1) {
            return Collections.unmodifiableList(fullList);
        }
        List<Sensor> list = new ArrayList<>();
        for (Sensor i : fullList) {
            if (i.getType() == type) {
                list.add(i);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public Sensor getDefaultSensor(int type) {
        List<Sensor> l = getSensorList(type);
        boolean wakeUpSensor = false;
        if (type == 8 || type == 17 || type == 22 || type == 23 || type == 24 || type == 25 || type == 26 || type == 32 || type == 65544 || type == 33171012) {
            wakeUpSensor = true;
        }
        for (Sensor sensor : l) {
            if (sensor.isWakeUpSensor() == wakeUpSensor) {
                return sensor;
            }
        }
        return null;
    }

    public Sensor getDefaultSensor(int type, boolean wakeUp) {
        for (Sensor sensor : getSensorList(type)) {
            if (sensor.isWakeUpSensor() == wakeUp) {
                return sensor;
            }
        }
        return null;
    }

    @Deprecated
    public boolean registerListener(SensorListener listener, int sensors) {
        return registerListener(listener, sensors, 3);
    }

    @Deprecated
    public boolean registerListener(SensorListener listener, int sensors, int rate) {
        return getLegacySensorManager().registerListener(listener, sensors, rate);
    }

    @Deprecated
    public void unregisterListener(SensorListener listener) {
        unregisterListener(listener, 255);
    }

    @Deprecated
    public void unregisterListener(SensorListener listener, int sensors) {
        getLegacySensorManager().unregisterListener(listener, sensors);
    }

    public void unregisterListener(SensorEventListener listener, Sensor sensor) {
        if (listener != null && sensor != null) {
            unregisterListenerImpl(listener, sensor);
        }
    }

    public void unregisterListener(SensorEventListener listener) {
        if (listener != null) {
            unregisterListenerImpl(listener, null);
        }
    }

    public boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs) {
        return registerListener(listener, sensor, samplingPeriodUs, (Handler) null);
    }

    public boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs) {
        return registerListenerImpl(listener, sensor, getDelay(samplingPeriodUs), null, maxReportLatencyUs, 0);
    }

    public boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, Handler handler) {
        return registerListenerImpl(listener, sensor, getDelay(samplingPeriodUs), handler, 0, 0);
    }

    public boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs, Handler handler) {
        return registerListenerImpl(listener, sensor, getDelay(samplingPeriodUs), handler, maxReportLatencyUs, 0);
    }

    public boolean flush(SensorEventListener listener) {
        return flushImpl(listener);
    }

    public SensorDirectChannel createDirectChannel(MemoryFile mem) {
        return createDirectChannelImpl(mem, null);
    }

    public SensorDirectChannel createDirectChannel(HardwareBuffer mem) {
        return createDirectChannelImpl(null, mem);
    }

    /* access modifiers changed from: package-private */
    public void destroyDirectChannel(SensorDirectChannel channel) {
        destroyDirectChannelImpl(channel);
    }

    @Deprecated
    public int configureDirectChannel(SensorDirectChannel channel, Sensor sensor, int rateLevel) {
        return configureDirectChannelImpl(channel, sensor, rateLevel);
    }

    public void registerDynamicSensorCallback(DynamicSensorCallback callback) {
        registerDynamicSensorCallback(callback, null);
    }

    public void registerDynamicSensorCallback(DynamicSensorCallback callback, Handler handler) {
        registerDynamicSensorCallbackImpl(callback, handler);
    }

    public void unregisterDynamicSensorCallback(DynamicSensorCallback callback) {
        unregisterDynamicSensorCallbackImpl(callback);
    }

    public boolean isDynamicSensorDiscoverySupported() {
        return getSensorList(32).size() > 0;
    }

    public static boolean getRotationMatrix(float[] R, float[] I, float[] gravity, float[] geomagnetic) {
        float[] fArr = R;
        float[] fArr2 = I;
        float Ax = gravity[0];
        float Ay = gravity[1];
        float Az = gravity[2];
        if ((Ax * Ax) + (Ay * Ay) + (Az * Az) < 0.96236104f) {
            return false;
        }
        float Ex = geomagnetic[0];
        float Ey = geomagnetic[1];
        float Ez = geomagnetic[2];
        float Hx = (Ey * Az) - (Ez * Ay);
        float Hy = (Ez * Ax) - (Ex * Az);
        float Hz = (Ex * Ay) - (Ey * Ax);
        float Ay2 = Ay;
        float normH = (float) Math.sqrt((double) ((Hx * Hx) + (Hy * Hy) + (Hz * Hz)));
        if (normH < 0.1f) {
            return false;
        }
        float invH = 1.0f / normH;
        float Hx2 = Hx * invH;
        float Hy2 = Hy * invH;
        float Hz2 = Hz * invH;
        float f = invH;
        float invA = 1.0f / ((float) Math.sqrt((double) (((Ax * Ax) + (Ay2 * Ay2)) + (Az * Az))));
        float Ax2 = Ax * invA;
        float Ay3 = Ay2 * invA;
        float Az2 = Az * invA;
        float Mx = (Ay3 * Hz2) - (Az2 * Hy2);
        float My = (Az2 * Hx2) - (Ax2 * Hz2);
        float Mz = (Ax2 * Hy2) - (Ay3 * Hx2);
        if (fArr != null) {
            if (fArr.length == 9) {
                fArr[0] = Hx2;
                fArr[1] = Hy2;
                fArr[2] = Hz2;
                fArr[3] = Mx;
                fArr[4] = My;
                fArr[5] = Mz;
                fArr[6] = Ax2;
                fArr[7] = Ay3;
                fArr[8] = Az2;
            } else if (fArr.length == 16) {
                fArr[0] = Hx2;
                fArr[1] = Hy2;
                fArr[2] = Hz2;
                fArr[3] = 0.0f;
                fArr[4] = Mx;
                fArr[5] = My;
                fArr[6] = Mz;
                fArr[7] = 0.0f;
                fArr[8] = Ax2;
                fArr[9] = Ay3;
                fArr[10] = Az2;
                fArr[11] = 0.0f;
                fArr[12] = 0.0f;
                fArr[13] = 0.0f;
                fArr[14] = 0.0f;
                fArr[15] = 1.0f;
            }
        }
        if (fArr2 != null) {
            float invE = 1.0f / ((float) Math.sqrt((double) (((Ex * Ex) + (Ey * Ey)) + (Ez * Ez))));
            float c = ((Ex * Mx) + (Ey * My) + (Ez * Mz)) * invE;
            float s = ((Ex * Ax2) + (Ey * Ay3) + (Ez * Az2)) * invE;
            float f2 = invE;
            float f3 = invA;
            if (fArr2.length == 9) {
                fArr2[0] = 1.0f;
                fArr2[1] = 0.0f;
                fArr2[2] = 0.0f;
                fArr2[3] = 0.0f;
                fArr2[4] = c;
                fArr2[5] = s;
                fArr2[6] = 0.0f;
                fArr2[7] = -s;
                fArr2[8] = c;
            } else if (fArr2.length == 16) {
                fArr2[0] = 1.0f;
                fArr2[1] = 0.0f;
                fArr2[2] = 0.0f;
                fArr2[4] = 0.0f;
                fArr2[5] = c;
                fArr2[6] = s;
                fArr2[8] = 0.0f;
                fArr2[9] = -s;
                fArr2[10] = c;
                fArr2[14] = 0.0f;
                fArr2[13] = 0.0f;
                fArr2[12] = 0.0f;
                fArr2[11] = 0.0f;
                fArr2[7] = 0.0f;
                fArr2[3] = 0.0f;
                fArr2[15] = 1.0f;
            }
        }
        return true;
    }

    public static float getInclination(float[] I) {
        if (I.length == 9) {
            return (float) Math.atan2((double) I[5], (double) I[4]);
        }
        return (float) Math.atan2((double) I[6], (double) I[5]);
    }

    public static boolean remapCoordinateSystem(float[] inR, int X, int Y, float[] outR) {
        if (inR == outR) {
            float[] temp = sTempMatrix;
            synchronized (temp) {
                if (remapCoordinateSystemImpl(inR, X, Y, temp)) {
                    int size = outR.length;
                    for (int i = 0; i < size; i++) {
                        outR[i] = temp[i];
                    }
                    return true;
                }
            }
        }
        return remapCoordinateSystemImpl(inR, X, Y, outR);
    }

    private static boolean remapCoordinateSystemImpl(float[] inR, int X, int Y, float[] outR) {
        float[] fArr = inR;
        int i = X;
        int i2 = Y;
        float[] fArr2 = outR;
        int length = fArr2.length;
        if (fArr.length != length) {
            return false;
        }
        if ((i & 124) != 0 || (i2 & 124) != 0) {
            return false;
        }
        if ((i & 3) == 0 || (i2 & 3) == 0) {
            return false;
        }
        if ((i & 3) == (i2 & 3)) {
            return false;
        }
        int Z = i ^ i2;
        int x = (i & 3) - 1;
        int y = (i2 & 3) - 1;
        int z = (Z & 3) - 1;
        int i3 = 3;
        if (((x ^ ((z + 1) % 3)) | (y ^ ((z + 2) % 3))) != 0) {
            Z ^= 128;
        }
        boolean sx = i >= 128;
        boolean sy = i2 >= 128;
        boolean sz = Z >= 128;
        int rowLength = length == 16 ? 4 : 3;
        int i4 = 0;
        while (true) {
            int j = i4;
            if (j >= i3) {
                break;
            }
            int offset = j * rowLength;
            int i5 = 0;
            while (true) {
                int i6 = i5;
                if (i6 >= i3) {
                    break;
                }
                if (x == i6) {
                    fArr2[offset + i6] = sx ? -fArr[offset + 0] : fArr[offset + 0];
                }
                if (y == i6) {
                    fArr2[offset + i6] = sy ? -fArr[offset + 1] : fArr[offset + 1];
                }
                if (z == i6) {
                    fArr2[offset + i6] = sz ? -fArr[offset + 2] : fArr[offset + 2];
                }
                i5 = i6 + 1;
                i3 = 3;
            }
            i4 = j + 1;
            i3 = 3;
        }
        if (length == 16) {
            fArr2[14] = 0.0f;
            fArr2[13] = 0.0f;
            fArr2[12] = 0.0f;
            fArr2[11] = 0.0f;
            fArr2[7] = 0.0f;
            fArr2[3] = 0.0f;
            fArr2[15] = 1.0f;
        }
        return true;
    }

    public static float[] getOrientation(float[] R, float[] values) {
        if (R.length == 9) {
            values[0] = (float) Math.atan2((double) R[1], (double) R[4]);
            values[1] = (float) Math.asin((double) (-R[7]));
            values[2] = (float) Math.atan2((double) (-R[6]), (double) R[8]);
        } else {
            values[0] = (float) Math.atan2((double) R[1], (double) R[5]);
            values[1] = (float) Math.asin((double) (-R[9]));
            values[2] = (float) Math.atan2((double) (-R[8]), (double) R[10]);
        }
        return values;
    }

    public static float getAltitude(float p0, float p) {
        return 44330.0f * (1.0f - ((float) Math.pow((double) (p / p0), 0.19029495120048523d)));
    }

    public static void getAngleChange(float[] angleChange, float[] R, float[] prevR) {
        int i;
        float[] fArr = R;
        float[] fArr2 = prevR;
        float ri0 = 0.0f;
        float ri1 = 0.0f;
        float ri2 = 0.0f;
        float ri3 = 0.0f;
        float ri4 = 0.0f;
        float ri5 = 0.0f;
        float ri6 = 0.0f;
        float ri7 = 0.0f;
        float ri8 = 0.0f;
        float pri0 = 0.0f;
        float pri1 = 0.0f;
        float pri2 = 0.0f;
        float pri3 = 0.0f;
        float pri4 = 0.0f;
        float pri5 = 0.0f;
        float pri6 = 0.0f;
        float pri7 = 0.0f;
        float pri8 = 0.0f;
        if (fArr.length == 9) {
            ri0 = fArr[0];
            ri1 = fArr[1];
            ri2 = fArr[2];
            ri3 = fArr[3];
            ri4 = fArr[4];
            ri5 = fArr[5];
            ri6 = fArr[6];
            ri7 = fArr[7];
            ri8 = fArr[8];
            i = 9;
        } else if (fArr.length == 16) {
            ri0 = fArr[0];
            ri1 = fArr[1];
            ri2 = fArr[2];
            ri3 = fArr[4];
            ri4 = fArr[5];
            ri5 = fArr[6];
            ri6 = fArr[8];
            i = 9;
            ri7 = fArr[9];
            ri8 = fArr[10];
        } else {
            i = 9;
        }
        if (fArr2.length == i) {
            pri0 = fArr2[0];
            pri1 = fArr2[1];
            pri2 = fArr2[2];
            pri3 = fArr2[3];
            pri4 = fArr2[4];
            pri5 = fArr2[5];
            pri6 = fArr2[6];
            pri7 = fArr2[7];
            pri8 = fArr2[8];
        } else if (fArr2.length == 16) {
            pri0 = fArr2[0];
            pri1 = fArr2[1];
            pri2 = fArr2[2];
            pri3 = fArr2[4];
            pri4 = fArr2[5];
            pri5 = fArr2[6];
            pri6 = fArr2[8];
            pri7 = fArr2[9];
            pri8 = fArr2[10];
        }
        float f = ri0;
        float f2 = ri1;
        angleChange[0] = (float) Math.atan2((double) ((pri0 * ri1) + (pri3 * ri4) + (pri6 * ri7)), (double) ((pri1 * ri1) + (pri4 * ri4) + (pri7 * ri7)));
        angleChange[1] = (float) Math.asin((double) (-((pri2 * ri1) + (pri5 * ri4) + (pri8 * ri7))));
        angleChange[2] = (float) Math.atan2((double) (-((pri2 * ri0) + (pri5 * ri3) + (pri8 * ri6))), (double) ((pri2 * ri2) + (pri5 * ri5) + (pri8 * ri8)));
    }

    public static void getRotationMatrixFromVector(float[] R, float[] rotationVector) {
        float q0;
        float[] fArr = R;
        float[] fArr2 = rotationVector;
        float q1 = fArr2[0];
        float q2 = fArr2[1];
        float q3 = fArr2[2];
        if (fArr2.length >= 4) {
            q0 = fArr2[3];
        } else {
            float q02 = ((1.0f - (q1 * q1)) - (q2 * q2)) - (q3 * q3);
            q0 = q02 > 0.0f ? (float) Math.sqrt((double) q02) : 0.0f;
        }
        float sq_q1 = 2.0f * q1 * q1;
        float sq_q2 = 2.0f * q2 * q2;
        float sq_q3 = 2.0f * q3 * q3;
        float q1_q2 = 2.0f * q1 * q2;
        float q3_q0 = 2.0f * q3 * q0;
        float q1_q3 = 2.0f * q1 * q3;
        float q2_q0 = 2.0f * q2 * q0;
        float q2_q3 = 2.0f * q2 * q3;
        float q1_q0 = 2.0f * q1 * q0;
        if (fArr.length == 9) {
            fArr[0] = (1.0f - sq_q2) - sq_q3;
            fArr[1] = q1_q2 - q3_q0;
            fArr[2] = q1_q3 + q2_q0;
            fArr[3] = q1_q2 + q3_q0;
            fArr[4] = (1.0f - sq_q1) - sq_q3;
            fArr[5] = q2_q3 - q1_q0;
            fArr[6] = q1_q3 - q2_q0;
            fArr[7] = q2_q3 + q1_q0;
            fArr[8] = (1.0f - sq_q1) - sq_q2;
        } else if (fArr.length == 16) {
            fArr[0] = (1.0f - sq_q2) - sq_q3;
            fArr[1] = q1_q2 - q3_q0;
            fArr[2] = q1_q3 + q2_q0;
            fArr[3] = 0.0f;
            fArr[4] = q1_q2 + q3_q0;
            fArr[5] = (1.0f - sq_q1) - sq_q3;
            fArr[6] = q2_q3 - q1_q0;
            fArr[7] = 0.0f;
            fArr[8] = q1_q3 - q2_q0;
            fArr[9] = q2_q3 + q1_q0;
            fArr[10] = (1.0f - sq_q1) - sq_q2;
            fArr[11] = 0.0f;
            fArr[14] = 0.0f;
            fArr[13] = 0.0f;
            fArr[12] = 0.0f;
            fArr[15] = 1.0f;
        }
    }

    public static void getQuaternionFromVector(float[] Q, float[] rv) {
        if (rv.length >= 4) {
            Q[0] = rv[3];
        } else {
            Q[0] = ((1.0f - (rv[0] * rv[0])) - (rv[1] * rv[1])) - (rv[2] * rv[2]);
            float f = 0.0f;
            if (Q[0] > 0.0f) {
                f = (float) Math.sqrt((double) Q[0]);
            }
            Q[0] = f;
        }
        Q[1] = rv[0];
        Q[2] = rv[1];
        Q[3] = rv[2];
    }

    public boolean requestTriggerSensor(TriggerEventListener listener, Sensor sensor) {
        return requestTriggerSensorImpl(listener, sensor);
    }

    public boolean cancelTriggerSensor(TriggerEventListener listener, Sensor sensor) {
        return cancelTriggerSensorImpl(listener, sensor, true);
    }

    @SystemApi
    public boolean initDataInjection(boolean enable) {
        return initDataInjectionImpl(enable);
    }

    @SystemApi
    public boolean injectSensorData(Sensor sensor, float[] values, int accuracy, long timestamp) {
        if (sensor == null) {
            throw new IllegalArgumentException("sensor cannot be null");
        } else if (!sensor.isDataInjectionSupported()) {
            throw new IllegalArgumentException("sensor does not support data injection");
        } else if (values != null) {
            int expectedNumValues = Sensor.getMaxLengthValuesArray(sensor, 23);
            if (values.length != expectedNumValues) {
                throw new IllegalArgumentException("Wrong number of values for sensor " + sensor.getName() + " actual=" + values.length + " expected=" + expectedNumValues);
            } else if (accuracy < -1 || accuracy > 3) {
                throw new IllegalArgumentException("Invalid sensor accuracy");
            } else if (timestamp > 0) {
                return injectSensorDataImpl(sensor, values, accuracy, timestamp);
            } else {
                throw new IllegalArgumentException("Negative or zero sensor timestamp");
            }
        } else {
            throw new IllegalArgumentException("sensor data cannot be null");
        }
    }

    private LegacySensorManager getLegacySensorManager() {
        LegacySensorManager legacySensorManager;
        synchronized (this.mSensorListByType) {
            if (this.mLegacySensorManager == null) {
                Log.i(TAG, "This application is using deprecated SensorManager API which will be removed someday.  Please consider switching to the new API.");
                this.mLegacySensorManager = new LegacySensorManager(this);
            }
            legacySensorManager = this.mLegacySensorManager;
        }
        return legacySensorManager;
    }

    private static int getDelay(int rate) {
        switch (rate) {
            case 0:
                return 0;
            case 1:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_DTS_COUNT;
            case 2:
                return 66667;
            case 3:
                return 200000;
            default:
                return rate;
        }
    }

    public boolean setOperationParameter(SensorAdditionalInfo parameter) {
        return setOperationParameterImpl(parameter);
    }

    public boolean supportSensorFeature(int sensorFeature) {
        return supportSensorFeatureImpl(sensorFeature);
    }

    public int hwSetSensorConfig(String config) {
        return hwSetSensorConfigImpl(config);
    }
}
