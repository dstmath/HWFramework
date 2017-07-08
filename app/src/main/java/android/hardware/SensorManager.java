package android.hardware;

import android.bluetooth.BluetoothAssignedNumbers;
import android.os.Handler;
import android.os.Process;
import android.os.health.UidHealthStats;
import android.rms.iaware.Events;
import android.speech.tts.TextToSpeech.Engine;
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
    private static final float[] mTempMatrix = null;
    private LegacySensorManager mLegacySensorManager;
    private final SparseArray<List<Sensor>> mSensorListByType;

    public static abstract class DynamicSensorCallback {
        public void onDynamicSensorConnected(Sensor sensor) {
        }

        public void onDynamicSensorDisconnected(Sensor sensor) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.SensorManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.SensorManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.SensorManager.<clinit>():void");
    }

    protected abstract boolean cancelTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor, boolean z);

    protected abstract boolean flushImpl(SensorEventListener sensorEventListener);

    protected abstract List<Sensor> getFullDynamicSensorList();

    protected abstract List<Sensor> getFullSensorList();

    protected abstract boolean initDataInjectionImpl(boolean z);

    protected abstract boolean injectSensorDataImpl(Sensor sensor, float[] fArr, int i, long j);

    protected abstract void registerDynamicSensorCallbackImpl(DynamicSensorCallback dynamicSensorCallback, Handler handler);

    protected abstract boolean registerListenerImpl(SensorEventListener sensorEventListener, Sensor sensor, int i, Handler handler, int i2, int i3);

    protected abstract boolean requestTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor);

    protected abstract void unregisterDynamicSensorCallbackImpl(DynamicSensorCallback dynamicSensorCallback);

    protected abstract void unregisterListenerImpl(SensorEventListener sensorEventListener, Sensor sensor);

    public SensorManager() {
        this.mSensorListByType = new SparseArray();
    }

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
                if (type == SENSOR_STATUS_NO_CONTACT) {
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
        if (type == SENSOR_STATUS_NO_CONTACT) {
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
        if (!(type == SENSOR_MAGNETIC_FIELD || type == 17 || type == 22 || type == 23 || type == 24 || type == 25 || type == 26)) {
            if (type == UidHealthStats.TIMERS_WAKELOCKS_DRAW) {
            }
            for (Sensor sensor : l) {
                if (sensor.isWakeUpSensor() == wakeUpSensor) {
                    return sensor;
                }
            }
            return null;
        }
        wakeUpSensor = true;
        for (Sensor sensor2 : l) {
            if (sensor2.isWakeUpSensor() == wakeUpSensor) {
                return sensor2;
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
        return registerListener(listener, sensors, (int) SENSOR_STATUS_ACCURACY_HIGH);
    }

    @Deprecated
    public boolean registerListener(SensorListener listener, int sensors, int rate) {
        return getLegacySensorManager().registerListener(listener, sensors, rate);
    }

    @Deprecated
    public void unregisterListener(SensorListener listener) {
        unregisterListener(listener, (int) Process.PROC_TERM_MASK);
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
        return registerListenerImpl(listener, sensor, getDelay(samplingPeriodUs), null, maxReportLatencyUs, SENSOR_STATUS_UNRELIABLE);
    }

    public boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, Handler handler) {
        return registerListenerImpl(listener, sensor, getDelay(samplingPeriodUs), handler, SENSOR_STATUS_UNRELIABLE, SENSOR_STATUS_UNRELIABLE);
    }

    public boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs, Handler handler) {
        return registerListenerImpl(listener, sensor, getDelay(samplingPeriodUs), handler, maxReportLatencyUs, SENSOR_STATUS_UNRELIABLE);
    }

    public boolean flush(SensorEventListener listener) {
        return flushImpl(listener);
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
        if (getSensorList(SENSOR_PROXIMITY).size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean getRotationMatrix(float[] R, float[] I, float[] gravity, float[] geomagnetic) {
        float Ax = gravity[SENSOR_STATUS_UNRELIABLE];
        float Ay = gravity[SENSOR_STATUS_ACCURACY_LOW];
        float Az = gravity[SENSOR_STATUS_ACCURACY_MEDIUM];
        if (((Ax * Ax) + (Ay * Ay)) + (Az * Az) < 0.96236104f) {
            return false;
        }
        float Ex = geomagnetic[SENSOR_STATUS_UNRELIABLE];
        float Ey = geomagnetic[SENSOR_STATUS_ACCURACY_LOW];
        float Ez = geomagnetic[SENSOR_STATUS_ACCURACY_MEDIUM];
        float Hx = (Ey * Az) - (Ez * Ay);
        float Hy = (Ez * Ax) - (Ex * Az);
        float Hz = (Ex * Ay) - (Ey * Ax);
        float normH = (float) Math.sqrt((double) (((Hx * Hx) + (Hy * Hy)) + (Hz * Hz)));
        if (normH < 0.1f) {
            return false;
        }
        int length;
        float invH = Engine.DEFAULT_VOLUME / normH;
        Hx *= invH;
        Hy *= invH;
        Hz *= invH;
        float invA = Engine.DEFAULT_VOLUME / ((float) Math.sqrt((double) (((Ax * Ax) + (Ay * Ay)) + (Az * Az))));
        Ax *= invA;
        Ay *= invA;
        Az *= invA;
        float Mx = (Ay * Hz) - (Az * Hy);
        float My = (Az * Hx) - (Ax * Hz);
        float Mz = (Ax * Hy) - (Ay * Hx);
        if (R != null) {
            length = R.length;
            if (r0 == 9) {
                R[SENSOR_STATUS_UNRELIABLE] = Hx;
                R[SENSOR_STATUS_ACCURACY_LOW] = Hy;
                R[SENSOR_STATUS_ACCURACY_MEDIUM] = Hz;
                R[SENSOR_STATUS_ACCURACY_HIGH] = Mx;
                R[SENSOR_TEMPERATURE] = My;
                R[RAW_DATA_Z] = Mz;
                R[6] = Ax;
                R[7] = Ay;
                R[SENSOR_MAGNETIC_FIELD] = Az;
            } else {
                length = R.length;
                if (r0 == SENSOR_LIGHT) {
                    R[SENSOR_STATUS_UNRELIABLE] = Hx;
                    R[SENSOR_STATUS_ACCURACY_LOW] = Hy;
                    R[SENSOR_STATUS_ACCURACY_MEDIUM] = Hz;
                    R[SENSOR_STATUS_ACCURACY_HIGH] = 0.0f;
                    R[SENSOR_TEMPERATURE] = Mx;
                    R[RAW_DATA_Z] = My;
                    R[6] = Mz;
                    R[7] = 0.0f;
                    R[SENSOR_MAGNETIC_FIELD] = Ax;
                    R[9] = Ay;
                    R[10] = Az;
                    R[11] = 0.0f;
                    R[12] = 0.0f;
                    R[13] = 0.0f;
                    R[14] = 0.0f;
                    R[15] = Engine.DEFAULT_VOLUME;
                }
            }
        }
        if (I != null) {
            float invE = Engine.DEFAULT_VOLUME / ((float) Math.sqrt((double) (((Ex * Ex) + (Ey * Ey)) + (Ez * Ez))));
            float c = (((Ex * Mx) + (Ey * My)) + (Ez * Mz)) * invE;
            float s = (((Ex * Ax) + (Ey * Ay)) + (Ez * Az)) * invE;
            length = I.length;
            if (r0 == 9) {
                I[SENSOR_STATUS_UNRELIABLE] = Engine.DEFAULT_VOLUME;
                I[SENSOR_STATUS_ACCURACY_LOW] = 0.0f;
                I[SENSOR_STATUS_ACCURACY_MEDIUM] = 0.0f;
                I[SENSOR_STATUS_ACCURACY_HIGH] = 0.0f;
                I[SENSOR_TEMPERATURE] = c;
                I[RAW_DATA_Z] = s;
                I[6] = 0.0f;
                I[7] = -s;
                I[SENSOR_MAGNETIC_FIELD] = c;
            } else {
                length = I.length;
                if (r0 == SENSOR_LIGHT) {
                    I[SENSOR_STATUS_UNRELIABLE] = Engine.DEFAULT_VOLUME;
                    I[SENSOR_STATUS_ACCURACY_LOW] = 0.0f;
                    I[SENSOR_STATUS_ACCURACY_MEDIUM] = 0.0f;
                    I[SENSOR_TEMPERATURE] = 0.0f;
                    I[RAW_DATA_Z] = c;
                    I[6] = s;
                    I[SENSOR_MAGNETIC_FIELD] = 0.0f;
                    I[9] = -s;
                    I[10] = c;
                    I[14] = 0.0f;
                    I[13] = 0.0f;
                    I[12] = 0.0f;
                    I[11] = 0.0f;
                    I[7] = 0.0f;
                    I[SENSOR_STATUS_ACCURACY_HIGH] = 0.0f;
                    I[15] = Engine.DEFAULT_VOLUME;
                }
            }
        }
        return true;
    }

    public static float getInclination(float[] I) {
        if (I.length == 9) {
            return (float) Math.atan2((double) I[RAW_DATA_Z], (double) I[SENSOR_TEMPERATURE]);
        }
        return (float) Math.atan2((double) I[6], (double) I[RAW_DATA_Z]);
    }

    public static boolean remapCoordinateSystem(float[] inR, int X, int Y, float[] outR) {
        if (inR == outR) {
            float[] temp = mTempMatrix;
            synchronized (temp) {
                if (remapCoordinateSystemImpl(inR, X, Y, temp)) {
                    int size = outR.length;
                    for (int i = SENSOR_STATUS_UNRELIABLE; i < size; i += SENSOR_STATUS_ACCURACY_LOW) {
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
        if ((X & BluetoothAssignedNumbers.A_AND_R_CAMBRIDGE) != 0 || (Y & BluetoothAssignedNumbers.A_AND_R_CAMBRIDGE) != 0) {
            return false;
        }
        if ((X & SENSOR_STATUS_ACCURACY_HIGH) == 0 || (Y & SENSOR_STATUS_ACCURACY_HIGH) == 0) {
            return false;
        }
        if ((X & SENSOR_STATUS_ACCURACY_HIGH) == (Y & SENSOR_STATUS_ACCURACY_HIGH)) {
            return false;
        }
        int Z = X ^ Y;
        int x = (X & SENSOR_STATUS_ACCURACY_HIGH) + SENSOR_STATUS_NO_CONTACT;
        int y = (Y & SENSOR_STATUS_ACCURACY_HIGH) + SENSOR_STATUS_NO_CONTACT;
        int z = (Z & SENSOR_STATUS_ACCURACY_HIGH) + SENSOR_STATUS_NO_CONTACT;
        if (((x ^ ((z + SENSOR_STATUS_ACCURACY_LOW) % SENSOR_STATUS_ACCURACY_HIGH)) | (y ^ ((z + SENSOR_STATUS_ACCURACY_MEDIUM) % SENSOR_STATUS_ACCURACY_HIGH))) != 0) {
            Z ^= SENSOR_ORIENTATION_RAW;
        }
        boolean sx = X >= SENSOR_ORIENTATION_RAW;
        boolean sy = Y >= SENSOR_ORIENTATION_RAW;
        boolean sz = Z >= SENSOR_ORIENTATION_RAW;
        int rowLength = length == SENSOR_LIGHT ? SENSOR_TEMPERATURE : SENSOR_STATUS_ACCURACY_HIGH;
        for (int j = SENSOR_STATUS_UNRELIABLE; j < SENSOR_STATUS_ACCURACY_HIGH; j += SENSOR_STATUS_ACCURACY_LOW) {
            int offset = j * rowLength;
            for (int i = SENSOR_STATUS_UNRELIABLE; i < SENSOR_STATUS_ACCURACY_HIGH; i += SENSOR_STATUS_ACCURACY_LOW) {
                if (x == i) {
                    outR[offset + i] = sx ? -inR[offset + SENSOR_STATUS_UNRELIABLE] : inR[offset + SENSOR_STATUS_UNRELIABLE];
                }
                if (y == i) {
                    outR[offset + i] = sy ? -inR[offset + SENSOR_STATUS_ACCURACY_LOW] : inR[offset + SENSOR_STATUS_ACCURACY_LOW];
                }
                if (z == i) {
                    float f;
                    int i2 = offset + i;
                    if (sz) {
                        f = -inR[offset + SENSOR_STATUS_ACCURACY_MEDIUM];
                    } else {
                        f = inR[offset + SENSOR_STATUS_ACCURACY_MEDIUM];
                    }
                    outR[i2] = f;
                }
            }
        }
        if (length == SENSOR_LIGHT) {
            outR[14] = 0.0f;
            outR[13] = 0.0f;
            outR[12] = 0.0f;
            outR[11] = 0.0f;
            outR[7] = 0.0f;
            outR[SENSOR_STATUS_ACCURACY_HIGH] = 0.0f;
            outR[15] = Engine.DEFAULT_VOLUME;
        }
        return true;
    }

    public static float[] getOrientation(float[] R, float[] values) {
        if (R.length == 9) {
            values[SENSOR_STATUS_UNRELIABLE] = (float) Math.atan2((double) R[SENSOR_STATUS_ACCURACY_LOW], (double) R[SENSOR_TEMPERATURE]);
            values[SENSOR_STATUS_ACCURACY_LOW] = (float) Math.asin((double) (-R[7]));
            values[SENSOR_STATUS_ACCURACY_MEDIUM] = (float) Math.atan2((double) (-R[6]), (double) R[SENSOR_MAGNETIC_FIELD]);
        } else {
            values[SENSOR_STATUS_UNRELIABLE] = (float) Math.atan2((double) R[SENSOR_STATUS_ACCURACY_LOW], (double) R[RAW_DATA_Z]);
            values[SENSOR_STATUS_ACCURACY_LOW] = (float) Math.asin((double) (-R[9]));
            values[SENSOR_STATUS_ACCURACY_MEDIUM] = (float) Math.atan2((double) (-R[SENSOR_MAGNETIC_FIELD]), (double) R[10]);
        }
        return values;
    }

    public static float getAltitude(float p0, float p) {
        return (Engine.DEFAULT_VOLUME - ((float) Math.pow((double) (p / p0), 0.19029495120048523d))) * 44330.0f;
    }

    public static void getAngleChange(float[] angleChange, float[] R, float[] prevR) {
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
        int length = R.length;
        if (r0 == 9) {
            ri0 = R[SENSOR_STATUS_UNRELIABLE];
            ri1 = R[SENSOR_STATUS_ACCURACY_LOW];
            ri2 = R[SENSOR_STATUS_ACCURACY_MEDIUM];
            ri3 = R[SENSOR_STATUS_ACCURACY_HIGH];
            ri4 = R[SENSOR_TEMPERATURE];
            ri5 = R[RAW_DATA_Z];
            ri6 = R[6];
            ri7 = R[7];
            ri8 = R[SENSOR_MAGNETIC_FIELD];
        } else {
            length = R.length;
            if (r0 == SENSOR_LIGHT) {
                ri0 = R[SENSOR_STATUS_UNRELIABLE];
                ri1 = R[SENSOR_STATUS_ACCURACY_LOW];
                ri2 = R[SENSOR_STATUS_ACCURACY_MEDIUM];
                ri3 = R[SENSOR_TEMPERATURE];
                ri4 = R[RAW_DATA_Z];
                ri5 = R[6];
                ri6 = R[SENSOR_MAGNETIC_FIELD];
                ri7 = R[9];
                ri8 = R[10];
            }
        }
        length = prevR.length;
        if (r0 == 9) {
            pri0 = prevR[SENSOR_STATUS_UNRELIABLE];
            pri1 = prevR[SENSOR_STATUS_ACCURACY_LOW];
            pri2 = prevR[SENSOR_STATUS_ACCURACY_MEDIUM];
            pri3 = prevR[SENSOR_STATUS_ACCURACY_HIGH];
            pri4 = prevR[SENSOR_TEMPERATURE];
            pri5 = prevR[RAW_DATA_Z];
            pri6 = prevR[6];
            pri7 = prevR[7];
            pri8 = prevR[SENSOR_MAGNETIC_FIELD];
        } else {
            length = prevR.length;
            if (r0 == SENSOR_LIGHT) {
                pri0 = prevR[SENSOR_STATUS_UNRELIABLE];
                pri1 = prevR[SENSOR_STATUS_ACCURACY_LOW];
                pri2 = prevR[SENSOR_STATUS_ACCURACY_MEDIUM];
                pri3 = prevR[SENSOR_TEMPERATURE];
                pri4 = prevR[RAW_DATA_Z];
                pri5 = prevR[6];
                pri6 = prevR[SENSOR_MAGNETIC_FIELD];
                pri7 = prevR[9];
                pri8 = prevR[10];
            }
        }
        float rd6 = ((pri2 * ri0) + (pri5 * ri3)) + (pri8 * ri6);
        float rd7 = ((pri2 * ri1) + (pri5 * ri4)) + (pri8 * ri7);
        float rd8 = ((pri2 * ri2) + (pri5 * ri5)) + (pri8 * ri8);
        angleChange[SENSOR_STATUS_UNRELIABLE] = (float) Math.atan2((double) (((pri0 * ri1) + (pri3 * ri4)) + (pri6 * ri7)), (double) (((pri1 * ri1) + (pri4 * ri4)) + (pri7 * ri7)));
        angleChange[SENSOR_STATUS_ACCURACY_LOW] = (float) Math.asin((double) (-rd7));
        angleChange[SENSOR_STATUS_ACCURACY_MEDIUM] = (float) Math.atan2((double) (-rd6), (double) rd8);
    }

    public static void getRotationMatrixFromVector(float[] R, float[] rotationVector) {
        float q0;
        float q1 = rotationVector[SENSOR_STATUS_UNRELIABLE];
        float q2 = rotationVector[SENSOR_STATUS_ACCURACY_LOW];
        float q3 = rotationVector[SENSOR_STATUS_ACCURACY_MEDIUM];
        if (rotationVector.length >= SENSOR_TEMPERATURE) {
            q0 = rotationVector[SENSOR_STATUS_ACCURACY_HIGH];
        } else {
            q0 = ((Engine.DEFAULT_VOLUME - (q1 * q1)) - (q2 * q2)) - (q3 * q3);
            if (q0 > 0.0f) {
                q0 = (float) Math.sqrt((double) q0);
            } else {
                q0 = 0.0f;
            }
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
            R[SENSOR_STATUS_UNRELIABLE] = (Engine.DEFAULT_VOLUME - sq_q2) - sq_q3;
            R[SENSOR_STATUS_ACCURACY_LOW] = q1_q2 - q3_q0;
            R[SENSOR_STATUS_ACCURACY_MEDIUM] = q1_q3 + q2_q0;
            R[SENSOR_STATUS_ACCURACY_HIGH] = q1_q2 + q3_q0;
            R[SENSOR_TEMPERATURE] = (Engine.DEFAULT_VOLUME - sq_q1) - sq_q3;
            R[RAW_DATA_Z] = q2_q3 - q1_q0;
            R[6] = q1_q3 - q2_q0;
            R[7] = q2_q3 + q1_q0;
            R[SENSOR_MAGNETIC_FIELD] = (Engine.DEFAULT_VOLUME - sq_q1) - sq_q2;
        } else if (R.length == SENSOR_LIGHT) {
            R[SENSOR_STATUS_UNRELIABLE] = (Engine.DEFAULT_VOLUME - sq_q2) - sq_q3;
            R[SENSOR_STATUS_ACCURACY_LOW] = q1_q2 - q3_q0;
            R[SENSOR_STATUS_ACCURACY_MEDIUM] = q1_q3 + q2_q0;
            R[SENSOR_STATUS_ACCURACY_HIGH] = 0.0f;
            R[SENSOR_TEMPERATURE] = q1_q2 + q3_q0;
            R[RAW_DATA_Z] = (Engine.DEFAULT_VOLUME - sq_q1) - sq_q3;
            R[6] = q2_q3 - q1_q0;
            R[7] = 0.0f;
            R[SENSOR_MAGNETIC_FIELD] = q1_q3 - q2_q0;
            R[9] = q2_q3 + q1_q0;
            R[10] = (Engine.DEFAULT_VOLUME - sq_q1) - sq_q2;
            R[11] = 0.0f;
            R[14] = 0.0f;
            R[13] = 0.0f;
            R[12] = 0.0f;
            R[15] = Engine.DEFAULT_VOLUME;
        }
    }

    public static void getQuaternionFromVector(float[] Q, float[] rv) {
        float f = 0.0f;
        if (rv.length >= SENSOR_TEMPERATURE) {
            Q[SENSOR_STATUS_UNRELIABLE] = rv[SENSOR_STATUS_ACCURACY_HIGH];
        } else {
            Q[SENSOR_STATUS_UNRELIABLE] = ((Engine.DEFAULT_VOLUME - (rv[SENSOR_STATUS_UNRELIABLE] * rv[SENSOR_STATUS_UNRELIABLE])) - (rv[SENSOR_STATUS_ACCURACY_LOW] * rv[SENSOR_STATUS_ACCURACY_LOW])) - (rv[SENSOR_STATUS_ACCURACY_MEDIUM] * rv[SENSOR_STATUS_ACCURACY_MEDIUM]);
            if (Q[SENSOR_STATUS_UNRELIABLE] > 0.0f) {
                f = (float) Math.sqrt((double) Q[SENSOR_STATUS_UNRELIABLE]);
            }
            Q[SENSOR_STATUS_UNRELIABLE] = f;
        }
        Q[SENSOR_STATUS_ACCURACY_LOW] = rv[SENSOR_STATUS_UNRELIABLE];
        Q[SENSOR_STATUS_ACCURACY_MEDIUM] = rv[SENSOR_STATUS_ACCURACY_LOW];
        Q[SENSOR_STATUS_ACCURACY_HIGH] = rv[SENSOR_STATUS_ACCURACY_MEDIUM];
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
            } else if (accuracy < SENSOR_STATUS_NO_CONTACT || accuracy > SENSOR_STATUS_ACCURACY_HIGH) {
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
            case SENSOR_STATUS_UNRELIABLE /*0*/:
                return SENSOR_STATUS_UNRELIABLE;
            case SENSOR_STATUS_ACCURACY_LOW /*1*/:
                return Events.EVENT_BASE_MEM;
            case SENSOR_STATUS_ACCURACY_MEDIUM /*2*/:
                return 66667;
            case SENSOR_STATUS_ACCURACY_HIGH /*3*/:
                return 200000;
            default:
                return rate;
        }
    }
}
