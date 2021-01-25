package huawei.android.bluetooth;

import android.util.Log;

public class HwDeviceBatteryInfo {
    public static final int CHARGE_STATE_OFF = 0;
    public static final int CHARGE_STATE_ON = 1;
    public static final String HW_BATTERY_INFO_STR_ERROR = "-2";
    public static final String HW_BATTERY_INFO_STR_UNKNOWN = "-1";
    private static final int KEY_CHARGER_BATTERY_LEVEL = 6;
    private static final int KEY_CHARGER_CHARGE_STATE = 7;
    private static final int KEY_NORMAL_BATTERY_LEVEL = 1;
    private static final int KEY_TWS_LEFT_BATTERY_LEVEL = 2;
    private static final int KEY_TWS_LEFT_CHARGE_STATE = 3;
    private static final int KEY_TWS_RIGHT_BATTERY_LEVEL = 4;
    private static final int KEY_TWS_RIGHT_CHARGE_STATE = 5;
    private static final int KEY_VALUE_LENGTH = 2;
    private static final String TAG = "HwDeviceBatteryInfo";
    public static final int TYPE_CHARGER = 4;
    public static final int TYPE_NORMAL_DEVICE = 1;
    public static final int TYPE_TWS_DEVICE_LEFT = 2;
    public static final int TYPE_TWS_DEVICE_RIGHT = 3;
    public static final int UNKNOWN_VALUE = -1;
    private int mChargerBatteryValue = -1;
    private int mChargerChargeState = 0;
    private String mInfoStr;
    private int mLeftBatteryValue = -1;
    private int mLeftChargeState = 0;
    private int mNormalBatteryValue = -1;
    private int mRightBatteryValue = -1;
    private int mRightChargeState = 0;

    HwDeviceBatteryInfo(String infoStr) {
        this.mInfoStr = infoStr;
        parseInfo();
    }

    private void parseInfo() {
        if (!(HW_BATTERY_INFO_STR_UNKNOWN.equals(this.mInfoStr) || HW_BATTERY_INFO_STR_ERROR.equals(this.mInfoStr))) {
            String[] infos = this.mInfoStr.split(",");
            if (infos.length > 0) {
                int[] values = new int[infos.length];
                for (int i = 0; i < infos.length; i++) {
                    values[i] = Integer.parseInt(infos[i]);
                }
                int kvLength = values[0];
                if (kvLength <= 0) {
                    Log.e(TAG, "kvLength wrong : " + kvLength);
                    return;
                }
                int codedLength = (kvLength * 2) + 1;
                if (codedLength != values.length) {
                    Log.e(TAG, "data length wrong, coded length value : " + codedLength + ", exactly : " + values.length);
                    return;
                }
                for (int i2 = 1; i2 <= kvLength; i2++) {
                    int key = values[(i2 * 2) - 1];
                    int value = values[i2 * 2];
                    switch (key) {
                        case 1:
                            this.mNormalBatteryValue = value;
                            break;
                        case 2:
                            this.mLeftBatteryValue = value;
                            break;
                        case 3:
                            this.mLeftChargeState = value;
                            break;
                        case 4:
                            this.mRightBatteryValue = value;
                            break;
                        case 5:
                            this.mRightChargeState = value;
                            break;
                        case 6:
                            this.mChargerBatteryValue = value;
                            break;
                        case KEY_CHARGER_CHARGE_STATE /* 7 */:
                            this.mChargerChargeState = value;
                            break;
                    }
                }
            }
        }
    }

    public String getInfoStr() {
        return this.mInfoStr;
    }

    public int getBatteryValue(int type) {
        switch (type) {
            case 1:
                return this.mNormalBatteryValue;
            case 2:
                return this.mLeftBatteryValue;
            case 3:
                return this.mRightBatteryValue;
            case 4:
                return this.mChargerBatteryValue;
            default:
                return -1;
        }
    }

    public int getChargeState(int type) {
        switch (type) {
            case 1:
            default:
                return -1;
            case 2:
                return this.mLeftChargeState;
            case 3:
                return this.mRightChargeState;
            case 4:
                return this.mChargerChargeState;
        }
    }
}
