package android.hardware;

import android.hardware.camera2.params.TonemapCurve;
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
    private static final float[] mTempMatrix = new float[16];
    private LegacySensorManager mLegacySensorManager;
    private final SparseArray<List<Sensor>> mSensorListByType = new SparseArray();

    public static abstract class DynamicSensorCallback {
        public void onDynamicSensorConnected(Sensor sensor) {
        }

        public void onDynamicSensorDisconnected(Sensor sensor) {
        }
    }

    protected abstract boolean cancelTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor, boolean z);

    protected abstract int configureDirectChannelImpl(SensorDirectChannel sensorDirectChannel, Sensor sensor, int i);

    protected abstract SensorDirectChannel createDirectChannelImpl(MemoryFile memoryFile, HardwareBuffer hardwareBuffer);

    protected abstract void destroyDirectChannelImpl(SensorDirectChannel sensorDirectChannel);

    protected abstract boolean flushImpl(SensorEventListener sensorEventListener);

    protected abstract List<Sensor> getFullDynamicSensorList();

    protected abstract List<Sensor> getFullSensorList();

    protected abstract boolean initDataInjectionImpl(boolean z);

    protected abstract boolean injectSensorDataImpl(Sensor sensor, float[] fArr, int i, long j);

    protected abstract void registerDynamicSensorCallbackImpl(DynamicSensorCallback dynamicSensorCallback, Handler handler);

    protected abstract boolean registerListenerImpl(SensorEventListener sensorEventListener, Sensor sensor, int i, Handler handler, int i2, int i3);

    protected abstract boolean requestTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor);

    protected abstract boolean setOperationParameterImpl(SensorAdditionalInfo sensorAdditionalInfo);

    protected abstract void unregisterDynamicSensorCallbackImpl(DynamicSensorCallback dynamicSensorCallback);

    protected abstract void unregisterListenerImpl(SensorEventListener sensorEventListener, Sensor sensor);

    @Deprecated
    public int getSensors() {
        return getLegacySensorManager().getSensors();
    }

    public List<Sensor> getSensorList(int type) {
        List<Sensor> list;
        List<Sensor> fullList = getFullSensorList();
        synchronized (this.mSensorListByType) {
            list = (List) this.mSensorListByType.get(type);
            if (list == null) {
                if (type == -1) {
                    list = fullList;
                } else {
                    list = new ArrayList();
                    for (Sensor i : fullList) {
                        if (i.getType() == type) {
                            list.add(i);
                        }
                    }
                }
                list = Collections.unmodifiableList(list);
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
        List<Sensor> list = new ArrayList();
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
        if (type == 8 || type == 17 || type == 22 || type == 23 || type == 24 || type == 25 || type == 26 || type == 32 || type == Sensor.TYPE_GESTURE) {
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
        return registerListener(listener, sensor, samplingPeriodUs, null);
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

    void destroyDirectChannel(SensorDirectChannel channel) {
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
        if (getSensorList(32).size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean getRotationMatrix(float[] R, float[] I, float[] gravity, float[] geomagnetic) {
        float Ax = gravity[0];
        float Ay = gravity[1];
        float Az = gravity[2];
        if (((Ax * Ax) + (Ay * Ay)) + (Az * Az) < 0.96236104f) {
            return false;
        }
        float Ex = geomagnetic[0];
        float Ey = geomagnetic[1];
        float Ez = geomagnetic[2];
        float Hx = (Ey * Az) - (Ez * Ay);
        float Hy = (Ez * Ax) - (Ex * Az);
        float Hz = (Ex * Ay) - (Ey * Ax);
        float normH = (float) Math.sqrt((double) (((Hx * Hx) + (Hy * Hy)) + (Hz * Hz)));
        if (normH < 0.1f) {
            return false;
        }
        float invH = 1.0f / normH;
        Hx *= invH;
        Hy *= invH;
        Hz *= invH;
        float invA = 1.0f / ((float) Math.sqrt((double) (((Ax * Ax) + (Ay * Ay)) + (Az * Az))));
        Ax *= invA;
        Ay *= invA;
        Az *= invA;
        float Mx = (Ay * Hz) - (Az * Hy);
        float My = (Az * Hx) - (Ax * Hz);
        float Mz = (Ax * Hy) - (Ay * Hx);
        if (R != null) {
            if (R.length == 9) {
                R[0] = Hx;
                R[1] = Hy;
                R[2] = Hz;
                R[3] = Mx;
                R[4] = My;
                R[5] = Mz;
                R[6] = Ax;
                R[7] = Ay;
                R[8] = Az;
            } else if (R.length == 16) {
                R[0] = Hx;
                R[1] = Hy;
                R[2] = Hz;
                R[3] = TonemapCurve.LEVEL_BLACK;
                R[4] = Mx;
                R[5] = My;
                R[6] = Mz;
                R[7] = TonemapCurve.LEVEL_BLACK;
                R[8] = Ax;
                R[9] = Ay;
                R[10] = Az;
                R[11] = TonemapCurve.LEVEL_BLACK;
                R[12] = TonemapCurve.LEVEL_BLACK;
                R[13] = TonemapCurve.LEVEL_BLACK;
                R[14] = TonemapCurve.LEVEL_BLACK;
                R[15] = 1.0f;
            }
        }
        if (I != null) {
            float invE = 1.0f / ((float) Math.sqrt((double) (((Ex * Ex) + (Ey * Ey)) + (Ez * Ez))));
            float c = (((Ex * Mx) + (Ey * My)) + (Ez * Mz)) * invE;
            float s = (((Ex * Ax) + (Ey * Ay)) + (Ez * Az)) * invE;
            if (I.length == 9) {
                I[0] = 1.0f;
                I[1] = TonemapCurve.LEVEL_BLACK;
                I[2] = TonemapCurve.LEVEL_BLACK;
                I[3] = TonemapCurve.LEVEL_BLACK;
                I[4] = c;
                I[5] = s;
                I[6] = TonemapCurve.LEVEL_BLACK;
                I[7] = -s;
                I[8] = c;
            } else if (I.length == 16) {
                I[0] = 1.0f;
                I[1] = TonemapCurve.LEVEL_BLACK;
                I[2] = TonemapCurve.LEVEL_BLACK;
                I[4] = TonemapCurve.LEVEL_BLACK;
                I[5] = c;
                I[6] = s;
                I[8] = TonemapCurve.LEVEL_BLACK;
                I[9] = -s;
                I[10] = c;
                I[14] = TonemapCurve.LEVEL_BLACK;
                I[13] = TonemapCurve.LEVEL_BLACK;
                I[12] = TonemapCurve.LEVEL_BLACK;
                I[11] = TonemapCurve.LEVEL_BLACK;
                I[7] = TonemapCurve.LEVEL_BLACK;
                I[3] = TonemapCurve.LEVEL_BLACK;
                I[15] = 1.0f;
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
            float[] temp = mTempMatrix;
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
        int length = outR.length;
        if (inR.length != length) {
            return false;
        }
        if ((X & 124) != 0 || (Y & 124) != 0) {
            return false;
        }
        if ((X & 3) == 0 || (Y & 3) == 0) {
            return false;
        }
        if ((X & 3) == (Y & 3)) {
            return false;
        }
        int Z = X ^ Y;
        int x = (X & 3) - 1;
        int y = (Y & 3) - 1;
        int z = (Z & 3) - 1;
        if (((x ^ ((z + 1) % 3)) | (y ^ ((z + 2) % 3))) != 0) {
            Z ^= 128;
        }
        boolean sx = X >= 128;
        boolean sy = Y >= 128;
        boolean sz = Z >= 128;
        int rowLength = length == 16 ? 4 : 3;
        for (int j = 0; j < 3; j++) {
            int offset = j * rowLength;
            for (int i = 0; i < 3; i++) {
                if (x == i) {
                    outR[offset + i] = sx ? -inR[offset + 0] : inR[offset + 0];
                }
                if (y == i) {
                    outR[offset + i] = sy ? -inR[offset + 1] : inR[offset + 1];
                }
                if (z == i) {
                    float f;
                    int i2 = offset + i;
                    if (sz) {
                        f = -inR[offset + 2];
                    } else {
                        f = inR[offset + 2];
                    }
                    outR[i2] = f;
                }
            }
        }
        if (length == 16) {
            outR[14] = TonemapCurve.LEVEL_BLACK;
            outR[13] = TonemapCurve.LEVEL_BLACK;
            outR[12] = TonemapCurve.LEVEL_BLACK;
            outR[11] = TonemapCurve.LEVEL_BLACK;
            outR[7] = TonemapCurve.LEVEL_BLACK;
            outR[3] = TonemapCurve.LEVEL_BLACK;
            outR[15] = 1.0f;
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
        return (1.0f - ((float) Math.pow((double) (p / p0), 0.19029495120048523d))) * 44330.0f;
    }

    public static void getAngleChange(float[] angleChange, float[] R, float[] prevR) {
        float ri0 = TonemapCurve.LEVEL_BLACK;
        float ri1 = TonemapCurve.LEVEL_BLACK;
        float ri2 = TonemapCurve.LEVEL_BLACK;
        float ri3 = TonemapCurve.LEVEL_BLACK;
        float ri4 = TonemapCurve.LEVEL_BLACK;
        float ri5 = TonemapCurve.LEVEL_BLACK;
        float ri6 = TonemapCurve.LEVEL_BLACK;
        float ri7 = TonemapCurve.LEVEL_BLACK;
        float ri8 = TonemapCurve.LEVEL_BLACK;
        float pri0 = TonemapCurve.LEVEL_BLACK;
        float pri1 = TonemapCurve.LEVEL_BLACK;
        float pri2 = TonemapCurve.LEVEL_BLACK;
        float pri3 = TonemapCurve.LEVEL_BLACK;
        float pri4 = TonemapCurve.LEVEL_BLACK;
        float pri5 = TonemapCurve.LEVEL_BLACK;
        float pri6 = TonemapCurve.LEVEL_BLACK;
        float pri7 = TonemapCurve.LEVEL_BLACK;
        float pri8 = TonemapCurve.LEVEL_BLACK;
        if (R.length == 9) {
            ri0 = R[0];
            ri1 = R[1];
            ri2 = R[2];
            ri3 = R[3];
            ri4 = R[4];
            ri5 = R[5];
            ri6 = R[6];
            ri7 = R[7];
            ri8 = R[8];
        } else if (R.length == 16) {
            ri0 = R[0];
            ri1 = R[1];
            ri2 = R[2];
            ri3 = R[4];
            ri4 = R[5];
            ri5 = R[6];
            ri6 = R[8];
            ri7 = R[9];
            ri8 = R[10];
        }
        if (prevR.length == 9) {
            pri0 = prevR[0];
            pri1 = prevR[1];
            pri2 = prevR[2];
            pri3 = prevR[3];
            pri4 = prevR[4];
            pri5 = prevR[5];
            pri6 = prevR[6];
            pri7 = prevR[7];
            pri8 = prevR[8];
        } else if (prevR.length == 16) {
            pri0 = prevR[0];
            pri1 = prevR[1];
            pri2 = prevR[2];
            pri3 = prevR[4];
            pri4 = prevR[5];
            pri5 = prevR[6];
            pri6 = prevR[8];
            pri7 = prevR[9];
            pri8 = prevR[10];
        }
        float rd6 = ((pri2 * ri0) + (pri5 * ri3)) + (pri8 * ri6);
        float rd7 = ((pri2 * ri1) + (pri5 * ri4)) + (pri8 * ri7);
        float rd8 = ((pri2 * ri2) + (pri5 * ri5)) + (pri8 * ri8);
        angleChange[0] = (float) Math.atan2((double) (((pri0 * ri1) + (pri3 * ri4)) + (pri6 * ri7)), (double) (((pri1 * ri1) + (pri4 * ri4)) + (pri7 * ri7)));
        angleChange[1] = (float) Math.asin((double) (-rd7));
        angleChange[2] = (float) Math.atan2((double) (-rd6), (double) rd8);
    }

    public static void getRotationMatrixFromVector(float[] R, float[] rotationVector) {
        float q0;
        float q1 = rotationVector[0];
        float q2 = rotationVector[1];
        float q3 = rotationVector[2];
        if (rotationVector.length >= 4) {
            q0 = rotationVector[3];
        } else {
            q0 = ((1.0f - (q1 * q1)) - (q2 * q2)) - (q3 * q3);
            q0 = q0 > TonemapCurve.LEVEL_BLACK ? (float) Math.sqrt((double) q0) : TonemapCurve.LEVEL_BLACK;
        }
        float sq_q1 = (2.0f * q1) * q1;
        float sq_q2 = (2.0f * q2) * q2;
        float sq_q3 = (2.0f * q3) * q3;
        float q1_q2 = (2.0f * q1) * q2;
        float q3_q0 = (2.0f * q3) * q0;
        float q1_q3 = (2.0f * q1) * q3;
        float q2_q0 = (2.0f * q2) * q0;
        float q2_q3 = (2.0f * q2) * q3;
        float q1_q0 = (2.0f * q1) * q0;
        if (R.length == 9) {
            R[0] = (1.0f - sq_q2) - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;
            R[3] = q1_q2 + q3_q0;
            R[4] = (1.0f - sq_q1) - sq_q3;
            R[5] = q2_q3 - q1_q0;
            R[6] = q1_q3 - q2_q0;
            R[7] = q2_q3 + q1_q0;
            R[8] = (1.0f - sq_q1) - sq_q2;
        } else if (R.length == 16) {
            R[0] = (1.0f - sq_q2) - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;
            R[3] = TonemapCurve.LEVEL_BLACK;
            R[4] = q1_q2 + q3_q0;
            R[5] = (1.0f - sq_q1) - sq_q3;
            R[6] = q2_q3 - q1_q0;
            R[7] = TonemapCurve.LEVEL_BLACK;
            R[8] = q1_q3 - q2_q0;
            R[9] = q2_q3 + q1_q0;
            R[10] = (1.0f - sq_q1) - sq_q2;
            R[11] = TonemapCurve.LEVEL_BLACK;
            R[14] = TonemapCurve.LEVEL_BLACK;
            R[13] = TonemapCurve.LEVEL_BLACK;
            R[12] = TonemapCurve.LEVEL_BLACK;
            R[15] = 1.0f;
        }
    }

    public static void getQuaternionFromVector(float[] Q, float[] rv) {
        float f = TonemapCurve.LEVEL_BLACK;
        if (rv.length >= 4) {
            Q[0] = rv[3];
        } else {
            Q[0] = ((1.0f - (rv[0] * rv[0])) - (rv[1] * rv[1])) - (rv[2] * rv[2]);
            if (Q[0] > TonemapCurve.LEVEL_BLACK) {
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

    public boolean initDataInjection(boolean enable) {
        return initDataInjectionImpl(enable);
    }

    public boolean injectSensorData(Sensor sensor, float[] values, int accuracy, long timestamp) {
        if (sensor == null) {
            throw new IllegalArgumentException("sensor cannot be null");
        } else if (!sensor.isDataInjectionSupported()) {
            throw new IllegalArgumentException("sensor does not support data injection");
        } else if (values == null) {
            throw new IllegalArgumentException("sensor data cannot be null");
        } else {
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
                return 20000;
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
}
