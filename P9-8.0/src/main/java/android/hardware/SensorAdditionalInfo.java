package android.hardware;

import android.hardware.camera2.params.TonemapCurve;

public class SensorAdditionalInfo {
    public static final int TYPE_DOCK_STATE = 196610;
    public static final int TYPE_FRAME_BEGIN = 0;
    public static final int TYPE_FRAME_END = 1;
    public static final int TYPE_HIGH_PERFORMANCE_MODE = 196611;
    public static final int TYPE_INTERNAL_TEMPERATURE = 65537;
    public static final int TYPE_LOCAL_GEOMAGNETIC_FIELD = 196608;
    public static final int TYPE_LOCAL_GRAVITY = 196609;
    public static final int TYPE_MAGNETIC_FIELD_CALIBRATION = 196612;
    public static final int TYPE_SAMPLING = 65540;
    public static final int TYPE_SENSOR_PLACEMENT = 65539;
    public static final int TYPE_UNTRACKED_DELAY = 65536;
    public static final int TYPE_VEC3_CALIBRATION = 65538;
    public final float[] floatValues;
    public final int[] intValues;
    public final Sensor sensor;
    public final int serial;
    public final int type;

    SensorAdditionalInfo(Sensor aSensor, int aType, int aSerial, int[] aIntValues, float[] aFloatValues) {
        this.sensor = aSensor;
        this.type = aType;
        this.serial = aSerial;
        this.intValues = aIntValues;
        this.floatValues = aFloatValues;
    }

    public static SensorAdditionalInfo createLocalGeomagneticField(float strength, float declination, float inclination) {
        if (strength < 10.0f || strength > 100.0f || declination < TonemapCurve.LEVEL_BLACK || ((double) declination) > 3.141592653589793d || ((double) inclination) < -1.5707963267948966d || ((double) inclination) > 1.5707963267948966d) {
            throw new IllegalArgumentException("Geomagnetic field info out of range");
        }
        return new SensorAdditionalInfo(null, 196608, 0, null, new float[]{strength, declination, inclination});
    }
}
