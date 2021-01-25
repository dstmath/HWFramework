package android.telephony;

import android.content.Context;
import android.os.PersistableBundle;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;

public class HwCarrierConfigManagerInner {
    private static final int BINARY = 2;
    public static final int HD_ICON_MASK_CALL_LOG = 12;
    public static final int HD_ICON_MASK_DIALER = 192;
    public static final int HD_ICON_MASK_INCALL_UI = 3;
    public static final int HD_ICON_MASK_STATUS_BAR = 48;
    private static final int HD_ICON_NOT_SET = -1;
    private static final int HW_VOLTE_ICON_RULE = SystemPropertiesEx.getInt("ro.config.hw_volte_icon_rule", 1);
    private static final String KEY_CARRIER_SHOW_VOLTE_HD_RULE = "carrier_show_volte_hd_rule";
    private static final String KEY_CARRIER_VOLTE_HD_ICON_FLAG = "carrier_volte_hd_icon_flag";
    private static final String KEY_CARRIER_VONR_HD_ICON_FLAG = "carrier_vonr_hd_icon_flag";
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

    public int getVonrIconRule(Context context, int subId, int mask) {
        if (context == null) {
            return HW_VOLTE_ICON_RULE;
        }
        int flag = getCarrierVonrIconFlag(context, subId);
        if (flag >= 0) {
            return getHDIconFromCarrierConfig(flag, mask);
        }
        return getVolteIconRule(context, subId, mask);
    }

    private int getCarrierVonrIconFlag(Context context, int subId) {
        PersistableBundle pb;
        if (context == null || (pb = getConfigForAnySubId(context, subId)) == null) {
            return -1;
        }
        String flagStr = pb.getString(KEY_CARRIER_VONR_HD_ICON_FLAG);
        if (flagStr == null) {
            RlogEx.e(TAG, "getCarrierVonrHDIconFlag flagStr null, subId = " + subId);
            return -1;
        }
        try {
            int flag = Integer.parseInt(flagStr, 2);
            RlogEx.d(TAG, "getCarrierVonrHDIconFlag flag = " + flag + ",subId = " + subId + ",flagStr = " + flagStr);
            return flag;
        } catch (NumberFormatException e) {
            RlogEx.e(TAG, "getCarrierVonrHDIconFlag parseInt error flagStr = " + flagStr + ",subId = " + subId);
            return -1;
        }
    }

    private int getHDIconFromCarrierConfig(int flag, int mask) {
        RlogEx.d(TAG, "getHDIconFromCarrierConfig flag = " + flag + ",mask = " + mask);
        int showHDIcon = flag & mask;
        StringBuilder sb = new StringBuilder();
        sb.append("showHDIcon original value is ");
        sb.append(showHDIcon);
        RlogEx.d(TAG, sb.toString());
        if (mask != 3) {
            if (mask == 12) {
                showHDIcon >>= 2;
            } else if (mask == 48) {
                showHDIcon = (showHDIcon >> 2) >> 2;
            } else if (mask == 192) {
                showHDIcon = ((showHDIcon >> 2) >> 2) >> 2;
            }
        }
        RlogEx.d(TAG, "showHDIcon final value is " + showHDIcon);
        return showHDIcon;
    }

    public int getCarrierVolteHDIconFlag(Context context, int subId) {
        PersistableBundle pb;
        if (context == null || (pb = getConfigForAnySubId(context, subId)) == null) {
            return -1;
        }
        String flagStr = pb.getString(KEY_CARRIER_VOLTE_HD_ICON_FLAG);
        if (flagStr == null) {
            RlogEx.e(TAG, "getCarrierVolteHDIconFlag flagStr null, subId = " + subId);
            return -1;
        }
        try {
            int flag = Integer.parseInt(flagStr, 2);
            RlogEx.d(TAG, "getCarrierVolteHDIconFlag flag = " + flag + ",subId = " + subId + ",flagStr = " + flagStr);
            return flag;
        } catch (NumberFormatException e) {
            RlogEx.e(TAG, "getCarrierVolteHDIconFlag parseInt error flagStr = " + flagStr + ",subId = " + subId);
            return -1;
        }
    }

    public int getCarrierVowifiHDIconFlag(Context context, int subId) {
        PersistableBundle pb;
        if (context == null || (pb = getConfigForAnySubId(context, subId)) == null) {
            return -1;
        }
        String flagStr = pb.getString(KEY_CARRIER_VOWIFI_HD_ICON_FLAG);
        if (flagStr == null) {
            RlogEx.e(TAG, "getCarrierVoWifiHDIconFlag flagStr null, subId = " + subId);
            return -1;
        }
        try {
            int flag = Integer.parseInt(flagStr, 2);
            RlogEx.d(TAG, "getCarrierVoWifiHDIconFlag flag = " + flag + ",subId = " + subId + ",flagStr = " + flagStr);
            return flag;
        } catch (NumberFormatException e) {
            RlogEx.e(TAG, "getCarrierVoWifiHDIconFlag parseInt error flagStr = " + flagStr + ",subId = " + subId);
            return -1;
        }
    }

    public int getShowHDIconRule(Context context, int subId) {
        PersistableBundle pb;
        if (context == null || (pb = getConfigForAnySubId(context, subId)) == null) {
            return -1;
        }
        int result = pb.getInt(KEY_CARRIER_SHOW_VOLTE_HD_RULE, -1);
        RlogEx.d(TAG, "getShowHDIconRule result = " + result + ":" + subId);
        return result;
    }

    public boolean needShowHDIcon(int flag, int mask) {
        RlogEx.d(TAG, "needShowHDIcon flag = " + flag + ",mask = " + mask);
        if ((mask & flag) != 0) {
            return true;
        }
        RlogEx.d(TAG, "needShowHDIcon carrier don't want to show HD icon in this UI.");
        return false;
    }

    private PersistableBundle getConfigForAnySubId(Context context, int subId) {
        CarrierConfigManager configMgr = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (configMgr == null) {
            return null;
        }
        if (subId < 0) {
            return configMgr.getConfig();
        }
        return configMgr.getConfigForSubId(subId);
    }
}
