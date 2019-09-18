package huawei.com.android.server.policy.fingersense;

import android.util.Log;
import java.io.FileReader;
import java.io.IOException;

public class KnuckGestureSetting {
    private static final String ACC_PATH = "/sys/devices/platform/huawei_sensor/acc_info";
    public static final short DKF_D_I = 18;
    public static final short DKF_T_I = 17;
    public static final short DKS_D_I = 6;
    public static final short DKS_T_I = 5;
    public static final short DK_F_T = 14;
    public static final short DK_S_T = 1;
    public static final short FSF_1S_2F_C = 22;
    public static final short FSF_C_C = 21;
    public static final short FSF_D_C = 20;
    public static final short FSF_T_C = 19;
    public static final int KNOCK_GESTURE_DATA_RECORD = 936006000;
    public static final short LCDINFO = 25;
    private static final String LCD_PATH = "sys/class/graphics/fb0/lcd_model";
    public static final short LG_F_T = 15;
    public static final short LG_S_T = 3;
    public static final short L_C_S_T = 7;
    public static final short L_E_S_T = 8;
    public static final short L_L_S_T = 12;
    public static final short L_M_S_T = 9;
    public static final short L_S_S_T = 10;
    public static final short L_W_S_T = 11;
    public static final short RG_S_T = 2;
    public static final short SCRORIENTATION = 26;
    public static final short SKF_T_I = 16;
    public static final short SKS_T_I = 4;
    public static final short SK_F_T = 13;
    public static final short SK_S_T = 0;
    public static final int STATUS_S_F = 1;
    public static final short S_INFO = 24;
    private static final String TAG = "KnuckGestureSetting";
    public static final short TP_INFO = 23;
    private static final String TP_PATH = "/sys/touchscreen/touch_chip_info";
    public static final long reportIntervalMS = 1000;
    private static KnuckGestureSetting sInstance;
    private String accVendorName = getVendorName(ACC_PATH);
    private long lastReportFSTime;
    private String lcdInfo = getVendorName(LCD_PATH);
    private int mOrientation = -1;
    private String tpVendorName = getVendorName(TP_PATH);

    public long getLastReportFSTime() {
        return this.lastReportFSTime;
    }

    public void setLastReportFSTime(long reportTime) {
        this.lastReportFSTime = reportTime;
    }

    private KnuckGestureSetting() {
    }

    public static synchronized KnuckGestureSetting getInstance() {
        KnuckGestureSetting knuckGestureSetting;
        synchronized (KnuckGestureSetting.class) {
            if (sInstance == null) {
                sInstance = new KnuckGestureSetting();
            }
            knuckGestureSetting = sInstance;
        }
        return knuckGestureSetting;
    }

    private String getVendorName(String vendorPath) {
        FileReader reader = null;
        String vendorName = null;
        try {
            FileReader reader2 = new FileReader(vendorPath);
            char[] buf = new char[100];
            int n = reader2.read(buf, 0, 100);
            if (n > 1) {
                vendorName = new String(buf, 0, n);
                Log.d(TAG, "getVendorName: path:" + vendorPath + ", value:" + vendorName);
            }
            try {
                reader2.close();
            } catch (IOException e) {
            }
        } catch (IOException ex) {
            Log.e(TAG, "couldn't read vendor name from " + vendorPath + ":" + ex);
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
        return vendorName;
    }

    public String getTpVendorName() {
        return this.tpVendorName;
    }

    public String getAccVendorName() {
        return this.accVendorName;
    }

    public String getLcdInfo() {
        return this.lcdInfo;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void setOrientation(int orientation) {
        switch (orientation) {
            case 0:
            case 2:
                this.mOrientation = 0;
                return;
            case 1:
            case 3:
                this.mOrientation = 1;
                return;
            default:
                this.mOrientation = -1;
                return;
        }
    }
}
