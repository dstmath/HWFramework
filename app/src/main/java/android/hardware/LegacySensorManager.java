package android.hardware;

import android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.AppAssociate;
import android.security.keymaster.KeymasterDefs;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import java.util.HashMap;

final class LegacySensorManager {
    private static boolean sInitialized;
    private static int sRotation;
    private static IWindowManager sWindowManager;
    private final HashMap<SensorListener, LegacyListener> mLegacyListenersMap;
    private final SensorManager mSensorManager;

    private static final class LegacyListener implements SensorEventListener {
        private int mSensors;
        private SensorListener mTarget;
        private float[] mValues;
        private final LmsFilter mYawfilter;

        boolean unregisterSensor(int r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.LegacySensorManager.LegacyListener.unregisterSensor(int):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 6 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.LegacySensorManager.LegacyListener.unregisterSensor(int):boolean");
        }

        LegacyListener(SensorListener target) {
            this.mValues = new float[6];
            this.mYawfilter = new LmsFilter();
            this.mTarget = target;
            this.mSensors = 0;
        }

        boolean registerSensor(int legacyType) {
            if ((this.mSensors & legacyType) != 0) {
                return false;
            }
            boolean alreadyHasOrientationSensor = hasOrientationSensor(this.mSensors);
            this.mSensors |= legacyType;
            if (alreadyHasOrientationSensor && hasOrientationSensor(legacyType)) {
                return false;
            }
            return true;
        }

        boolean hasSensors() {
            return this.mSensors != 0;
        }

        private static boolean hasOrientationSensor(int sensors) {
            return (sensors & IllegalCharacterValueSanitizer.AMP_AND_SPACE_LEGAL) != 0;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            try {
                this.mTarget.onAccuracyChanged(getLegacySensorType(sensor.getType()), accuracy);
            } catch (AbstractMethodError e) {
            }
        }

        public void onSensorChanged(SensorEvent event) {
            float[] v = this.mValues;
            v[0] = event.values[0];
            v[1] = event.values[1];
            v[2] = event.values[2];
            int type = event.sensor.getType();
            int legacyType = getLegacySensorType(type);
            mapSensorDataToWindow(legacyType, v, LegacySensorManager.getRotation());
            if (type == 3) {
                if ((this.mSensors & KeymasterDefs.KM_ALGORITHM_HMAC) != 0) {
                    this.mTarget.onSensorChanged(KeymasterDefs.KM_ALGORITHM_HMAC, v);
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

        private void mapSensorDataToWindow(int sensor, float[] values, int orientation) {
            float x = values[0];
            float y = values[1];
            float z = values[2];
            switch (sensor) {
                case AudioState.ROUTE_EARPIECE /*1*/:
                case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                    z = -z;
                    break;
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    x = -x;
                    y = -y;
                    z = -z;
                    break;
                case AudioState.ROUTE_SPEAKER /*8*/:
                    x = -x;
                    y = -y;
                    break;
            }
            values[0] = x;
            values[1] = y;
            values[2] = z;
            values[3] = x;
            values[4] = y;
            values[5] = z;
            if ((orientation & 1) != 0) {
                switch (sensor) {
                    case AudioState.ROUTE_EARPIECE /*1*/:
                    case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                        values[0] = ((float) (x < 270.0f ? 90 : -270)) + x;
                        values[1] = z;
                        values[2] = y;
                        break;
                    case AudioState.ROUTE_BLUETOOTH /*2*/:
                    case AudioState.ROUTE_SPEAKER /*8*/:
                        values[0] = -y;
                        values[1] = x;
                        values[2] = z;
                        break;
                }
            }
            if ((orientation & 2) != 0) {
                x = values[0];
                y = values[1];
                z = values[2];
                switch (sensor) {
                    case AudioState.ROUTE_EARPIECE /*1*/:
                    case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                        values[0] = x >= 180.0f ? x - 180.0f : x + 180.0f;
                        values[1] = -y;
                        values[2] = -z;
                    case AudioState.ROUTE_BLUETOOTH /*2*/:
                    case AudioState.ROUTE_SPEAKER /*8*/:
                        values[0] = -x;
                        values[1] = -y;
                        values[2] = z;
                    default:
                }
            }
        }

        private static int getLegacySensorType(int type) {
            switch (type) {
                case AudioState.ROUTE_EARPIECE /*1*/:
                    return 2;
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    return 8;
                case Engine.DEFAULT_STREAM /*3*/:
                    return KeymasterDefs.KM_ALGORITHM_HMAC;
                case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                    return 4;
                default:
                    return 0;
            }
        }
    }

    private static final class LmsFilter {
        private static final int COUNT = 12;
        private static final float PREDICTION_RATIO = 0.33333334f;
        private static final float PREDICTION_TIME = 0.08f;
        private static final int SENSORS_RATE_MS = 20;
        private int mIndex;
        private long[] mT;
        private float[] mV;

        public LmsFilter() {
            this.mV = new float[24];
            this.mT = new long[24];
            this.mIndex = COUNT;
        }

        public float filter(long time, float in) {
            float v = in;
            float v1 = this.mV[this.mIndex];
            if (in - v1 > 180.0f) {
                v = in - 360.0f;
            } else if (v1 - in > 180.0f) {
                v = in + 360.0f;
            }
            this.mIndex++;
            int i = this.mIndex;
            if (r0 >= 24) {
                this.mIndex = COUNT;
            }
            this.mV[this.mIndex] = v;
            this.mT[this.mIndex] = time;
            this.mV[this.mIndex - 12] = v;
            this.mT[this.mIndex - 12] = time;
            float E = 0.0f;
            float D = 0.0f;
            float C = 0.0f;
            float B = 0.0f;
            float A = 0.0f;
            for (int i2 = 0; i2 < 11; i2++) {
                int j = (this.mIndex - 1) - i2;
                float Z = this.mV[j];
                float T = ((float) (((this.mT[j] / 2) + (this.mT[j + 1] / 2)) - time)) * 1.0E-9f;
                float dT = ((float) (this.mT[j] - this.mT[j + 1])) * 1.0E-9f;
                dT *= dT;
                A += Z * dT;
                B += (T * dT) * T;
                C += T * dT;
                D += (T * dT) * Z;
                E += dT;
            }
            float b = ((A * B) + (C * D)) / ((E * B) + (C * C));
            float f = (b + (PREDICTION_TIME * (((E * b) - A) / C))) * 0.0027777778f;
            if ((f >= 0.0f ? f : -f) >= NetworkHistoryUtils.RECOVERY_PERCENTAGE) {
                f = (f - ((float) Math.ceil((double) (NetworkHistoryUtils.RECOVERY_PERCENTAGE + f)))) + Engine.DEFAULT_VOLUME;
            }
            if (f < 0.0f) {
                f += Engine.DEFAULT_VOLUME;
            }
            return f * 360.0f;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.LegacySensorManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.LegacySensorManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.LegacySensorManager.<clinit>():void");
    }

    public LegacySensorManager(SensorManager sensorManager) {
        this.mLegacyListenersMap = new HashMap();
        this.mSensorManager = sensorManager;
        synchronized (SensorManager.class) {
            if (!sInitialized) {
                sWindowManager = Stub.asInterface(ServiceManager.getService(AppAssociate.ASSOC_WINDOW));
                if (sWindowManager != null) {
                    try {
                        sRotation = sWindowManager.watchRotation(new IRotationWatcher.Stub() {
                            public void onRotationChanged(int rotation) {
                                LegacySensorManager.onRotationChanged(rotation);
                            }
                        });
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    public int getSensors() {
        int result = 0;
        for (Sensor i : this.mSensorManager.getFullSensorList()) {
            switch (i.getType()) {
                case AudioState.ROUTE_EARPIECE /*1*/:
                    result |= 2;
                    break;
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    result |= 8;
                    break;
                case Engine.DEFAULT_STREAM /*3*/:
                    result |= IllegalCharacterValueSanitizer.AMP_AND_SPACE_LEGAL;
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public boolean registerListener(SensorListener listener, int sensors, int rate) {
        if (listener == null) {
            return false;
        }
        boolean result;
        if (registerLegacyListener(2, 1, listener, sensors, rate)) {
            result = true;
        } else {
            result = false;
        }
        if (registerLegacyListener(8, 2, listener, sensors, rate)) {
            result = true;
        }
        if (registerLegacyListener(KeymasterDefs.KM_ALGORITHM_HMAC, 3, listener, sensors, rate)) {
            result = true;
        }
        if (registerLegacyListener(1, 3, listener, sensors, rate)) {
            result = true;
        }
        if (registerLegacyListener(4, 7, listener, sensors, rate)) {
            result = true;
        }
        return result;
    }

    private boolean registerLegacyListener(int legacyType, int type, SensorListener listener, int sensors, int rate) {
        boolean z = false;
        if ((sensors & legacyType) != 0) {
            Sensor sensor = this.mSensorManager.getDefaultSensor(type);
            if (sensor != null) {
                synchronized (this.mLegacyListenersMap) {
                    SensorEventListener legacyListener = (LegacyListener) this.mLegacyListenersMap.get(listener);
                    if (legacyListener == null) {
                        legacyListener = new LegacyListener(listener);
                        this.mLegacyListenersMap.put(listener, legacyListener);
                    }
                    if (legacyListener.registerSensor(legacyType)) {
                        z = this.mSensorManager.registerListener(legacyListener, sensor, rate);
                    } else {
                        z = true;
                    }
                }
            }
        }
        return z;
    }

    public void unregisterListener(SensorListener listener, int sensors) {
        if (listener != null) {
            unregisterLegacyListener(2, 1, listener, sensors);
            unregisterLegacyListener(8, 2, listener, sensors);
            unregisterLegacyListener(KeymasterDefs.KM_ALGORITHM_HMAC, 3, listener, sensors);
            unregisterLegacyListener(1, 3, listener, sensors);
            unregisterLegacyListener(4, 7, listener, sensors);
        }
    }

    private void unregisterLegacyListener(int legacyType, int type, SensorListener listener, int sensors) {
        if ((sensors & legacyType) != 0) {
            Sensor sensor = this.mSensorManager.getDefaultSensor(type);
            if (sensor != null) {
                synchronized (this.mLegacyListenersMap) {
                    SensorEventListener legacyListener = (LegacyListener) this.mLegacyListenersMap.get(listener);
                    if (legacyListener != null && legacyListener.unregisterSensor(legacyType)) {
                        this.mSensorManager.unregisterListener(legacyListener, sensor);
                        if (!legacyListener.hasSensors()) {
                            this.mLegacyListenersMap.remove(listener);
                        }
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
}
