package android.telephony;

import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;

public class HwCarrierConfigManagerInner {
    private static final int BINARY = 2;
    public static final int HD_ICON_MASK_CALL_LOG = 12;
    public static final int HD_ICON_MASK_DIALER = 192;
    public static final int HD_ICON_MASK_INCALL_UI = 3;
    public static final int HD_ICON_MASK_STATUS_BAR = 48;
    private static final int HD_ICON_NOT_SET = -1;
    private static final int HW_VOLTE_ICON_RULE = SystemProperties.getInt("ro.config.hw_volte_icon_rule", 1);
    private static final String KEY_CARRIER_SHOW_VOLTE_HD_RULE = "carrier_show_volte_hd_rule";
    private static final String KEY_CARRIER_VOLTE_HD_ICON_FLAG = "carrier_volte_hd_icon_flag";
    private static final String KEY_CARRIER_VOWIFI_HD_ICON_FLAG = "carrier_vowifi_hd_icon_flag";
    private static final int NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG = -1;
    private static final int NOT_SHOW_HD_ICON_BY_CARRIER_CONFIG = 0;
    private static final int OFFSET = 2;
    private static final int SHOW_HD_ICON_BY_CARRIER_CONFIG = 1;
    private static final int SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG = 2;
    private static final String TAG = "HwCarrierConfigManagerInner";
    private static HwCarrierConfigManagerInner sInstance = new HwCarrierConfigManagerInner();

    private HwCarrierConfigManagerInner() {
    }

    public static HwCarrierConfigManagerInner getDefault() {
        return sInstance;
    }

    public int getVolteIconRule(Context context, int subId, int mask) {
        int flag = getCarrierVolteHDIconFlag(context, subId);
        if (flag >= 0) {
            return getHDIconFromCarrierConfig(flag, mask);
        }
        return HW_VOLTE_ICON_RULE;
    }

    public int getVowifiIconRule(Context context, int subId, int mask) {
        int flag = getCarrierVowifiHDIconFlag(context, subId);
        if (flag >= 0) {
            return getHDIconFromCarrierConfig(flag, mask);
        }
        return getVolteIconRule(context, subId, mask);
    }

    private int getHDIconFromCarrierConfig(int flag, int mask) {
        Rlog.d(TAG, "getHDIconFromCarrierConfig flag = " + flag + ",mask = " + mask);
        int showHDIcon = flag & mask;
        Rlog.d(TAG, "showHDIcon original value is " + showHDIcon);
        switch (mask) {
            case 12:
                showHDIcon >>= 2;
                break;
            case HD_ICON_MASK_STATUS_BAR /*48*/:
                showHDIcon = (showHDIcon >> 2) >> 2;
                break;
            case 192:
                showHDIcon = ((showHDIcon >> 2) >> 2) >> 2;
                break;
        }
        Rlog.d(TAG, "showHDIcon final value is " + showHDIcon);
        return showHDIcon;
    }

    public int getCarrierVolteHDIconFlag(Context context, int subId) {
        if (context == null) {
            return -1;
        }
        PersistableBundle pb = getConfigForAnySubId(context, subId);
        if (pb == null) {
            return -1;
        }
        String flagStr = pb.getString(KEY_CARRIER_VOLTE_HD_ICON_FLAG);
        if (flagStr == null) {
            Rlog.e(TAG, "getCarrierVolteHDIconFlag flagStr null, subId = " + subId);
            return -1;
        }
        try {
            int flag = Integer.parseInt(flagStr, 2);
            Rlog.d(TAG, "getCarrierVolteHDIconFlag flag = " + flag + ",subId = " + subId + ",flagStr = " + flagStr);
            return flag;
        } catch (NumberFormatException e) {
            Rlog.e(TAG, "getCarrierVolteHDIconFlag parseInt error flagStr = " + flagStr + ",subId = " + subId);
            return -1;
        }
    }

    public int getCarrierVowifiHDIconFlag(Context context, int subId) {
        if (context == null) {
            return -1;
        }
        PersistableBundle pb = getConfigForAnySubId(context, subId);
        if (pb == null) {
            return -1;
        }
        String flagStr = pb.getString(KEY_CARRIER_VOWIFI_HD_ICON_FLAG);
        if (flagStr == null) {
            Rlog.e(TAG, "getCarrierVoWifiHDIconFlag flagStr null, subId = " + subId);
            return -1;
        }
        try {
            int flag = Integer.parseInt(flagStr, 2);
            Rlog.d(TAG, "getCarrierVoWifiHDIconFlag flag = " + flag + ",subId = " + subId + ",flagStr = " + flagStr);
            return flag;
        } catch (NumberFormatException e) {
            Rlog.e(TAG, "getCarrierVoWifiHDIconFlag parseInt error flagStr = " + flagStr + ",subId = " + subId);
            return -1;
        }
    }

    public int getShowHDIconRule(Context context, int subId) {
        if (context == null) {
            return -1;
        }
        PersistableBundle pb = getConfigForAnySubId(context, subId);
        if (pb == null) {
            return -1;
        }
        int result = pb.getInt(KEY_CARRIER_SHOW_VOLTE_HD_RULE, -1);
        Rlog.d(TAG, "getShowHDIconRule result = " + result + ":" + subId);
        return result;
    }

    public boolean needShowHDIcon(int flag, int mask) {
        Rlog.d(TAG, "needShowHDIcon flag = " + flag + ",mask = " + mask);
        if ((mask & flag) != 0) {
            return true;
        }
        Rlog.d(TAG, "needShowHDIcon carrier don't want to show HD icon in this UI.");
        return false;
    }

    private PersistableBundle getConfigForAnySubId(Context context, int subId) {
        CarrierConfigManager configMgr = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (configMgr == null) {
            return null;
        }
        PersistableBundle pb;
        if (subId < 0) {
            pb = configMgr.getConfig();
        } else {
            pb = configMgr.getConfigForSubId(subId);
        }
        return pb;
    }
}
