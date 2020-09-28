package huawei.android.security.facerecognition.utils;

import com.huawei.android.os.SystemPropertiesEx;
import java.io.FileInputStream;
import java.io.IOException;

public class DeviceUtil {
    private static final int CAP_MIN = -100;
    private static final double DISABLE_CAP_THRESHOLD = (((double) SystemPropertiesEx.getInt("ro.config.face_disable_cap", -100)) * 1.0d);
    private static final double DISABLE_TEMP_THRESHOLD = ((((double) SystemPropertiesEx.getInt("ro.config.face_disable_temp", -1000)) * 1.0d) / 10.0d);
    private static final int MAX_BYTES = 10;
    private static final String TAG = "DeviceUtil";
    private static final int TEMP_MAX = 1000;
    private static final int TEMP_MIN = -1000;
    private static final double TEMP_THRESHOLD = ((((double) SystemPropertiesEx.getInt("ro.config.low_temp_tsh", -1000)) * 1.0d) / 10.0d);
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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0039, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003e, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003f, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0042, code lost:
        throw r6;
     */
    private static double readDoubleFromFile(String filePath) {
        try {
            FileInputStream in = new FileInputStream(filePath);
            byte[] max = new byte[10];
            int bytesRead = in.read(max, 0, 10);
            if (bytesRead <= 0) {
                in.close();
                return 1000.0d;
            }
            byte[] toReturn = new byte[bytesRead];
            System.arraycopy(max, 0, toReturn, 0, bytesRead);
            double ret = Double.valueOf(new String(toReturn, "utf-8")).doubleValue();
            in.close();
            return ret;
        } catch (IOException e) {
            LogUtil.w(TAG, "read status occurs IOException" + e.getMessage());
            return 1000.0d;
        } catch (NumberFormatException e2) {
            LogUtil.w(TAG, "wrong fromat : " + ((String) null));
            return 1000.0d;
        }
    }
}
