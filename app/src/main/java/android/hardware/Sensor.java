package android.hardware;

import java.util.UUID;

public final class Sensor {
    private static final int ADDITIONAL_INFO_MASK = 64;
    private static final int ADDITIONAL_INFO_SHIFT = 6;
    private static final int DATA_INJECTION_MASK = 16;
    private static final int DATA_INJECTION_SHIFT = 4;
    private static final int DYNAMIC_SENSOR_MASK = 32;
    private static final int DYNAMIC_SENSOR_SHIFT = 5;
    public static final int REPORTING_MODE_CONTINUOUS = 0;
    private static final int REPORTING_MODE_MASK = 14;
    public static final int REPORTING_MODE_ONE_SHOT = 2;
    public static final int REPORTING_MODE_ON_CHANGE = 1;
    private static final int REPORTING_MODE_SHIFT = 1;
    public static final int REPORTING_MODE_SPECIAL_TRIGGER = 3;
    private static final int SENSOR_FLAG_WAKE_UP_SENSOR = 1;
    public static final String SENSOR_STRING_TYPE_TILT_DETECTOR = "android.sensor.tilt_detector";
    public static final String STRING_TYPE_ACCELEROMETER = "android.sensor.accelerometer";
    public static final String STRING_TYPE_AMBIENT_TEMPERATURE = "android.sensor.ambient_temperature";
    public static final String STRING_TYPE_DEVICE_ORIENTATION = "android.sensor.device_orientation";
    public static final String STRING_TYPE_DYNAMIC_SENSOR_META = "android.sensor.dynamic_sensor_meta";
    public static final String STRING_TYPE_GAME_ROTATION_VECTOR = "android.sensor.game_rotation_vector";
    public static final String STRING_TYPE_GEOMAGNETIC_ROTATION_VECTOR = "android.sensor.geomagnetic_rotation_vector";
    public static final String STRING_TYPE_GESTURE = "android.sensor.gesture";
    public static final String STRING_TYPE_GLANCE_GESTURE = "android.sensor.glance_gesture";
    public static final String STRING_TYPE_GRAVITY = "android.sensor.gravity";
    public static final String STRING_TYPE_GYROSCOPE = "android.sensor.gyroscope";
    public static final String STRING_TYPE_GYROSCOPE_UNCALIBRATED = "android.sensor.gyroscope_uncalibrated";
    public static final String STRING_TYPE_HEART_BEAT = "android.sensor.heart_beat";
    public static final String STRING_TYPE_HEART_RATE = "android.sensor.heart_rate";
    public static final String STRING_TYPE_LIGHT = "android.sensor.light";
    public static final String STRING_TYPE_LINEAR_ACCELERATION = "android.sensor.linear_acceleration";
    public static final String STRING_TYPE_MAGNETIC_FIELD = "android.sensor.magnetic_field";
    public static final String STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED = "android.sensor.magnetic_field_uncalibrated";
    public static final String STRING_TYPE_MOTION_DETECT = "android.sensor.motion_detect";
    @Deprecated
    public static final String STRING_TYPE_ORIENTATION = "android.sensor.orientation";
    public static final String STRING_TYPE_PICK_UP_GESTURE = "android.sensor.pick_up_gesture";
    public static final String STRING_TYPE_POSE_6DOF = "android.sensor.pose_6dof";
    public static final String STRING_TYPE_PRESSURE = "android.sensor.pressure";
    public static final String STRING_TYPE_PROXIMITY = "android.sensor.proximity";
    public static final String STRING_TYPE_RELATIVE_HUMIDITY = "android.sensor.relative_humidity";
    public static final String STRING_TYPE_ROTATION_VECTOR = "android.sensor.rotation_vector";
    public static final String STRING_TYPE_SIGNIFICANT_MOTION = "android.sensor.significant_motion";
    public static final String STRING_TYPE_STATIONARY_DETECT = "android.sensor.stationary_detect";
    public static final String STRING_TYPE_STEP_COUNTER = "android.sensor.step_counter";
    public static final String STRING_TYPE_STEP_DETECTOR = "android.sensor.step_detector";
    @Deprecated
    public static final String STRING_TYPE_TEMPERATURE = "android.sensor.temperature";
    public static final String STRING_TYPE_WAKE_GESTURE = "android.sensor.wake_gesture";
    public static final String STRING_TYPE_WRIST_TILT_GESTURE = "android.sensor.wrist_tilt_gesture";
    public static final int TYPE_ACCELEROMETER = 1;
    public static final int TYPE_AIR_PRESS = 10003;
    public static final int TYPE_ALL = -1;
    public static final int TYPE_AMBIENT_TEMPERATURE = 13;
    public static final int TYPE_CAP_PROX = 10005;
    public static final int TYPE_DEVICE_ORIENTATION = 27;
    public static final int TYPE_DEVICE_PRIVATE_BASE = 65536;
    public static final int TYPE_DYNAMIC_SENSOR_META = 32;
    public static final int TYPE_GAME_ROTATION_VECTOR = 15;
    public static final int TYPE_GEOMAGNETIC_ROTATION_VECTOR = 20;
    public static final int TYPE_GESTURE = 10008;
    public static final int TYPE_GLANCE_GESTURE = 24;
    public static final int TYPE_GRAVITY = 9;
    public static final int TYPE_GYROSCOPE = 4;
    public static final int TYPE_GYROSCOPE_UNCALIBRATED = 16;
    public static final int TYPE_HALL = 10002;
    public static final int TYPE_HANDPRESS = 10004;
    public static final int TYPE_HEART_BEAT = 31;
    public static final int TYPE_HEART_RATE = 21;
    public static final int TYPE_LIGHT = 5;
    public static final int TYPE_LINEAR_ACCELERATION = 10;
    public static final int TYPE_MAGNETIC_FIELD = 2;
    public static final int TYPE_MAGNETIC_FIELD_UNCALIBRATED = 14;
    public static final int TYPE_MCU_LABC = 10007;
    public static final int TYPE_MOTION_ACCEL = 33171011;
    public static final int TYPE_MOTION_DETECT = 30;
    @Deprecated
    public static final int TYPE_ORIENTATION = 3;
    public static final int TYPE_PICK_UP_GESTURE = 25;
    public static final int TYPE_POSE_6DOF = 28;
    public static final int TYPE_PRESSURE = 6;
    public static final int TYPE_PROXIMITY = 8;
    public static final int TYPE_RELATIVE_HUMIDITY = 12;
    public static final int TYPE_ROTATE_SCREEN = 10006;
    public static final int TYPE_ROTATION_VECTOR = 11;
    public static final int TYPE_SIGNIFICANT_MOTION = 17;
    public static final int TYPE_STATIONARY_DETECT = 29;
    public static final int TYPE_STEP_COUNTER = 19;
    public static final int TYPE_STEP_DETECTOR = 18;
    @Deprecated
    public static final int TYPE_TEMPERATURE = 7;
    public static final int TYPE_TILT_DETECTOR = 22;
    public static final int TYPE_WAKE_GESTURE = 23;
    public static final int TYPE_WRIST_TILT_GESTURE = 26;
    private static final int[] sSensorReportingModes = null;
    private int mFifoMaxEventCount;
    private int mFifoReservedEventCount;
    private int mFlags;
    private int mHandle;
    private int mId;
    private int mMaxDelay;
    private float mMaxRange;
    private int mMinDelay;
    private String mName;
    private float mPower;
    private String mRequiredPermission;
    private float mResolution;
    private String mStringType;
    private int mType;
    private String mVendor;
    private int mVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.Sensor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.Sensor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.Sensor.<clinit>():void");
    }

    public int getReportingMode() {
        return (this.mFlags & TYPE_MAGNETIC_FIELD_UNCALIBRATED) >> TYPE_ACCELEROMETER;
    }

    static int getMaxLengthValuesArray(Sensor sensor, int sdkLevel) {
        if (sensor.mType == TYPE_ROTATION_VECTOR && sdkLevel <= TYPE_SIGNIFICANT_MOTION) {
            return TYPE_ORIENTATION;
        }
        int offset = sensor.mType;
        if (offset >= sSensorReportingModes.length) {
            return TYPE_GYROSCOPE_UNCALIBRATED;
        }
        return sSensorReportingModes[offset];
    }

    Sensor() {
    }

    public String getName() {
        return this.mName;
    }

    public String getVendor() {
        return this.mVendor;
    }

    public int getType() {
        return this.mType;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public float getMaximumRange() {
        return this.mMaxRange;
    }

    public float getResolution() {
        return this.mResolution;
    }

    public float getPower() {
        return this.mPower;
    }

    public int getMinDelay() {
        return this.mMinDelay;
    }

    public int getFifoReservedEventCount() {
        return this.mFifoReservedEventCount;
    }

    public int getFifoMaxEventCount() {
        return this.mFifoMaxEventCount;
    }

    public String getStringType() {
        return this.mStringType;
    }

    public UUID getUuid() {
        throw new UnsupportedOperationException();
    }

    public int getId() {
        return this.mId;
    }

    public String getRequiredPermission() {
        return this.mRequiredPermission;
    }

    public int getHandle() {
        return this.mHandle;
    }

    public int getMaxDelay() {
        return this.mMaxDelay;
    }

    public boolean isWakeUpSensor() {
        return (this.mFlags & TYPE_ACCELEROMETER) != 0;
    }

    public boolean isDynamicSensor() {
        return (this.mFlags & TYPE_DYNAMIC_SENSOR_META) != 0;
    }

    public boolean isAdditionalInfoSupported() {
        return (this.mFlags & ADDITIONAL_INFO_MASK) != 0;
    }

    public boolean isDataInjectionSupported() {
        return ((this.mFlags & TYPE_GYROSCOPE_UNCALIBRATED) >> TYPE_GYROSCOPE) != 0;
    }

    void setRange(float max, float res) {
        this.mMaxRange = max;
        this.mResolution = res;
    }

    public String toString() {
        return "{Sensor name=\"" + this.mName + "\", vendor=\"" + this.mVendor + "\", version=" + this.mVersion + ", type=" + this.mType + ", maxRange=" + this.mMaxRange + ", resolution=" + this.mResolution + ", power=" + this.mPower + ", minDelay=" + this.mMinDelay + "}";
    }

    private boolean setType(int value) {
        this.mType = value;
        switch (this.mType) {
            case TYPE_ACCELEROMETER /*1*/:
                this.mStringType = STRING_TYPE_ACCELEROMETER;
                return true;
            case TYPE_MAGNETIC_FIELD /*2*/:
                this.mStringType = STRING_TYPE_MAGNETIC_FIELD;
                return true;
            case TYPE_ORIENTATION /*3*/:
                this.mStringType = STRING_TYPE_ORIENTATION;
                return true;
            case TYPE_GYROSCOPE /*4*/:
                this.mStringType = STRING_TYPE_GYROSCOPE;
                return true;
            case TYPE_LIGHT /*5*/:
                this.mStringType = STRING_TYPE_LIGHT;
                return true;
            case TYPE_PRESSURE /*6*/:
                this.mStringType = STRING_TYPE_PRESSURE;
                return true;
            case TYPE_TEMPERATURE /*7*/:
                this.mStringType = STRING_TYPE_TEMPERATURE;
                return true;
            case TYPE_PROXIMITY /*8*/:
                this.mStringType = STRING_TYPE_PROXIMITY;
                return true;
            case TYPE_GRAVITY /*9*/:
                this.mStringType = STRING_TYPE_GRAVITY;
                return true;
            case TYPE_LINEAR_ACCELERATION /*10*/:
                this.mStringType = STRING_TYPE_LINEAR_ACCELERATION;
                return true;
            case TYPE_ROTATION_VECTOR /*11*/:
                this.mStringType = STRING_TYPE_ROTATION_VECTOR;
                return true;
            case TYPE_RELATIVE_HUMIDITY /*12*/:
                this.mStringType = STRING_TYPE_RELATIVE_HUMIDITY;
                return true;
            case TYPE_AMBIENT_TEMPERATURE /*13*/:
                this.mStringType = STRING_TYPE_AMBIENT_TEMPERATURE;
                return true;
            case TYPE_MAGNETIC_FIELD_UNCALIBRATED /*14*/:
                this.mStringType = STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED;
                return true;
            case TYPE_GAME_ROTATION_VECTOR /*15*/:
                this.mStringType = STRING_TYPE_GAME_ROTATION_VECTOR;
                return true;
            case TYPE_GYROSCOPE_UNCALIBRATED /*16*/:
                this.mStringType = STRING_TYPE_GYROSCOPE_UNCALIBRATED;
                return true;
            case TYPE_SIGNIFICANT_MOTION /*17*/:
                this.mStringType = STRING_TYPE_SIGNIFICANT_MOTION;
                return true;
            case TYPE_STEP_DETECTOR /*18*/:
                this.mStringType = STRING_TYPE_STEP_DETECTOR;
                return true;
            case TYPE_STEP_COUNTER /*19*/:
                this.mStringType = STRING_TYPE_STEP_COUNTER;
                return true;
            case TYPE_GEOMAGNETIC_ROTATION_VECTOR /*20*/:
                this.mStringType = STRING_TYPE_GEOMAGNETIC_ROTATION_VECTOR;
                return true;
            case TYPE_HEART_RATE /*21*/:
                this.mStringType = STRING_TYPE_HEART_RATE;
                return true;
            case TYPE_TILT_DETECTOR /*22*/:
                this.mStringType = SENSOR_STRING_TYPE_TILT_DETECTOR;
                return true;
            case TYPE_WAKE_GESTURE /*23*/:
                this.mStringType = STRING_TYPE_WAKE_GESTURE;
                return true;
            case TYPE_GLANCE_GESTURE /*24*/:
                this.mStringType = STRING_TYPE_GLANCE_GESTURE;
                return true;
            case TYPE_PICK_UP_GESTURE /*25*/:
                this.mStringType = STRING_TYPE_PICK_UP_GESTURE;
                return true;
            case TYPE_DEVICE_ORIENTATION /*27*/:
                this.mStringType = STRING_TYPE_DEVICE_ORIENTATION;
                return true;
            case TYPE_DYNAMIC_SENSOR_META /*32*/:
                this.mStringType = STRING_TYPE_DYNAMIC_SENSOR_META;
                return true;
            case TYPE_GESTURE /*10008*/:
                this.mStringType = STRING_TYPE_GESTURE;
                return true;
            default:
                return false;
        }
    }

    private void setUuid(long msb, long lsb) {
        this.mId = (int) msb;
    }
}
