package android.hardware;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SensorAdditionalInfo {
    public static final int TYPE_CUSTOM_INFO = 268435456;
    public static final int TYPE_DEBUG_INFO = 1073741824;
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface AdditionalInfoType {
    }

    SensorAdditionalInfo(Sensor aSensor, int aType, int aSerial, int[] aIntValues, float[] aFloatValues) {
        this.sensor = aSensor;
        this.type = aType;
        this.serial = aSerial;
        this.intValues = aIntValues;
        this.floatValues = aFloatValues;
    }

    public static SensorAdditionalInfo createLocalGeomagneticField(float strength, float declination, float inclination) {
        if (strength < 10.0f || strength > 100.0f || declination < 0.0f || ((double) declination) > 3.141592653589793d || ((double) inclination) < -1.5707963267948966d || ((double) inclination) > 1.5707963267948966d) {
            throw new IllegalArgumentException("Geomagnetic field info out of range");
        }
        return new SensorAdditionalInfo(null, 196608, 0, null, new float[]{strength, declination, inclination});
    }

    public static SensorAdditionalInfo createCustomInfo(Sensor aSensor, int type2, float[] data) {
        if (type2 >= 268435456 && type2 < 1073741824 && aSensor != null) {
            return new SensorAdditionalInfo(aSensor, type2, 0, null, data);
        }
        throw new IllegalArgumentException("invalid parameter(s): type: " + type2 + "; sensor: " + aSensor);
    }
}
