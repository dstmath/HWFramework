package android.hardware.contexthub.V1_0;

import java.util.ArrayList;

public final class SensorType {
    public static final int ACCELEROMETER = 1;
    public static final int AMBIENT_LIGHT_SENSOR = 6;
    public static final int AUDIO = 768;
    public static final int BAROMETER = 4;
    public static final int BLE = 1280;
    public static final int CAMERA = 1024;
    public static final int GPS = 256;
    public static final int GYROSCOPE = 2;
    public static final int INSTANT_MOTION_DETECT = 8;
    public static final int MAGNETOMETER = 3;
    public static final int PRIVATE_SENSOR_BASE = 65536;
    public static final int PROXIMITY_SENSOR = 5;
    public static final int RESERVED = 0;
    public static final int STATIONARY_DETECT = 7;
    public static final int WIFI = 512;
    public static final int WWAN = 1536;

    public static final String toString(int o) {
        if (o == 0) {
            return "RESERVED";
        }
        if (o == 1) {
            return "ACCELEROMETER";
        }
        if (o == 2) {
            return "GYROSCOPE";
        }
        if (o == 3) {
            return "MAGNETOMETER";
        }
        if (o == 4) {
            return "BAROMETER";
        }
        if (o == 5) {
            return "PROXIMITY_SENSOR";
        }
        if (o == 6) {
            return "AMBIENT_LIGHT_SENSOR";
        }
        if (o == 7) {
            return "STATIONARY_DETECT";
        }
        if (o == 8) {
            return "INSTANT_MOTION_DETECT";
        }
        if (o == 256) {
            return "GPS";
        }
        if (o == 512) {
            return "WIFI";
        }
        if (o == 768) {
            return "AUDIO";
        }
        if (o == 1024) {
            return "CAMERA";
        }
        if (o == 1280) {
            return "BLE";
        }
        if (o == 1536) {
            return "WWAN";
        }
        if (o == 65536) {
            return "PRIVATE_SENSOR_BASE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("RESERVED");
        if ((o & 1) == 1) {
            list.add("ACCELEROMETER");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("GYROSCOPE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("MAGNETOMETER");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("BAROMETER");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("PROXIMITY_SENSOR");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("AMBIENT_LIGHT_SENSOR");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("STATIONARY_DETECT");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("INSTANT_MOTION_DETECT");
            flipped |= 8;
        }
        if ((o & 256) == 256) {
            list.add("GPS");
            flipped |= 256;
        }
        if ((o & 512) == 512) {
            list.add("WIFI");
            flipped |= 512;
        }
        if ((o & 768) == 768) {
            list.add("AUDIO");
            flipped |= 768;
        }
        if ((o & 1024) == 1024) {
            list.add("CAMERA");
            flipped |= 1024;
        }
        if ((o & 1280) == 1280) {
            list.add("BLE");
            flipped |= 1280;
        }
        if ((o & 1536) == 1536) {
            list.add("WWAN");
            flipped |= 1536;
        }
        if ((o & 65536) == 65536) {
            list.add("PRIVATE_SENSOR_BASE");
            flipped |= 65536;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
