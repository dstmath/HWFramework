package huawei.android.security.facerecognition.utils;

import android.os.SystemProperties;
import java.io.FileInputStream;
import java.io.IOException;

public class DeviceUtil {
    private static final int CAP_MIN = -100;
    private static final double DISABLE_CAP_THRESHOLD = (((double) SystemProperties.getInt("ro.config.face_disable_cap", CAP_MIN)) * 1.0d);
    private static final double DISABLE_TEMP_THRESHOLD = ((((double) SystemProperties.getInt("ro.config.face_disable_temp", TEMP_MIN)) * 1.0d) / 10.0d);
    private static final int MAX_BYTES = 10;
    private static final String TAG = "DeviceUtil";
    private static final int TEMP_MAX = 1000;
    private static final int TEMP_MIN = -1000;
    private static final double TEMP_THRESHOLD = ((((double) SystemProperties.getInt("ro.config.low_temp_tsh", TEMP_MIN)) * 1.0d) / 10.0d);
    private static final String sCapacityFilePath = "/sys/class/power_supply/Battery/capacity";
    private static final String sTempFilePath = "/sys/class/power_supply/Battery/temp";

    public static boolean reachDisabledTempCap(double temperature, double capacity) {
        LogUtil.d(TAG, "disable temperature threshold is : " + DISABLE_TEMP_THRESHOLD + ", cap is : " + DISABLE_CAP_THRESHOLD);
        return temperature <= DISABLE_TEMP_THRESHOLD && capacity <= DISABLE_CAP_THRESHOLD;
    }

    public static boolean isLowTemperature(double temperature) {
        LogUtil.d(TAG, "temperature threshold is : " + TEMP_THRESHOLD);
        return temperature <= TEMP_THRESHOLD;
    }

    public static double getBatteryCapacity() {
        return readDoubleFromFile(sCapacityFilePath);
    }

    public static double getBatteryTemperature() {
        return readDoubleFromFile(sTempFilePath) / 10.0d;
    }

    private static double readDoubleFromFile(String filePath) {
        FileInputStream in;
        double ret = 1000.0d;
        String retStr = null;
        try {
            in = new FileInputStream(filePath);
            byte[] max = new byte[10];
            int bytesRead = in.read(max, 0, 10);
            if (bytesRead <= 0) {
                in.close();
                return 1000.0d;
            }
            byte[] toReturn = new byte[bytesRead];
            System.arraycopy(max, 0, toReturn, 0, bytesRead);
            retStr = new String(toReturn, CharacterSets.DEFAULT_CHARSET_NAME);
            ret = Double.valueOf(retStr).doubleValue();
            in.close();
            return ret;
        } catch (IOException e) {
            LogUtil.w(TAG, "read status occurs IOException" + e.getMessage());
        } catch (NumberFormatException e2) {
            LogUtil.w(TAG, "wrong fromat : " + retStr);
        } catch (Throwable th) {
            r2.addSuppressed(th);
        }
        throw th;
    }
}
