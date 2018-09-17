package com.huawei.facerecognition.utils;

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
    private static final String sCapacityFilePathQcom = "/sys/class/power_supply/battery/capacity";
    private static final String sTempFilePath = "/sys/class/power_supply/Battery/temp";
    private static final String sTempFilePathQcom = "/sys/class/power_supply/battery/temp";

    public static boolean reachDisabledTempCap(double temperature, double capacity) {
        LogUtil.d(TAG, "disable temperature threshold is : " + DISABLE_TEMP_THRESHOLD + ", cap is : " + DISABLE_CAP_THRESHOLD);
        if (temperature > DISABLE_TEMP_THRESHOLD || capacity > DISABLE_CAP_THRESHOLD) {
            return false;
        }
        return true;
    }

    public static boolean isLowTemperature(double temperature) {
        LogUtil.d(TAG, "temperature threshold is : " + TEMP_THRESHOLD);
        return temperature <= TEMP_THRESHOLD;
    }

    public static double getBatteryCapacity() {
        double ret = readDoubleFromFile(sCapacityFilePath);
        if (!isEqual(ret, 1000.0d)) {
            return ret;
        }
        LogUtil.d(TAG, "Read Qcom Capacity");
        return readDoubleFromFile(sCapacityFilePathQcom);
    }

    public static double getBatteryTemperature() {
        double ret = readDoubleFromFile(sTempFilePath);
        if (isEqual(ret, 1000.0d)) {
            LogUtil.d(TAG, "Read Qcom Temp");
            ret = readDoubleFromFile(sTempFilePathQcom);
        }
        return ret / 10.0d;
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x007f A:{SYNTHETIC, Splitter: B:40:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ae A:{SYNTHETIC, Splitter: B:53:0x00ae} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0084 A:{SYNTHETIC, Splitter: B:43:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x007f A:{SYNTHETIC, Splitter: B:40:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0084 A:{SYNTHETIC, Splitter: B:43:0x0084} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ae A:{SYNTHETIC, Splitter: B:53:0x00ae} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static double readDoubleFromFile(String filePath) {
        Throwable th;
        double ret = 1000.0d;
        String retStr = null;
        Throwable th2 = null;
        FileInputStream in = null;
        IOException e;
        try {
            FileInputStream in2 = new FileInputStream(filePath);
            try {
                byte[] max = new byte[10];
                int bytesRead = in2.read(max, 0, 10);
                if (bytesRead <= 0) {
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 == null) {
                        return 1000.0d;
                    }
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        in = in2;
                    } catch (NumberFormatException e3) {
                        LogUtil.w(TAG, "wrong fromat : " + retStr);
                        return ret;
                    }
                }
                byte[] toReturn = new byte[bytesRead];
                System.arraycopy(max, 0, toReturn, 0, bytesRead);
                String retStr2 = new String(toReturn, "utf-8");
                try {
                    ret = Double.valueOf(retStr2).doubleValue();
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (Throwable th4) {
                            th2 = th4;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (IOException e4) {
                            e = e4;
                        } catch (NumberFormatException e5) {
                            retStr = retStr2;
                            LogUtil.w(TAG, "wrong fromat : " + retStr);
                            return ret;
                        }
                    }
                    return ret;
                } catch (Throwable th5) {
                    th = th5;
                    in = in2;
                    retStr = retStr2;
                    if (in != null) {
                    }
                    if (th2 != null) {
                    }
                }
            } catch (Throwable th6) {
                th = th6;
                in = in2;
                if (in != null) {
                }
                if (th2 != null) {
                }
            }
            LogUtil.w(TAG, "read status occurs IOException" + e.getMessage());
            return ret;
        } catch (Throwable th7) {
            th = th7;
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable th8) {
                    if (th2 == null) {
                        th2 = th8;
                    } else if (th2 != th8) {
                        th2.addSuppressed(th8);
                    }
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (IOException e6) {
                    e = e6;
                } catch (NumberFormatException e7) {
                    LogUtil.w(TAG, "wrong fromat : " + retStr);
                    return ret;
                }
            }
            throw th;
        }
    }

    private static boolean isEqual(double left, double right) {
        return Math.abs(left - right) < 0.001d;
    }
}
