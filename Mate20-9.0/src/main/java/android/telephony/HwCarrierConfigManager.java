package android.telephony;

import android.content.Context;

public class HwCarrierConfigManager {
    public static final int HD_ICON_MASK_CALL_LOG = 12;
    public static final int HD_ICON_MASK_DIALER = 192;
    public static final int HD_ICON_MASK_INCALL_UI = 3;
    public static final int HD_ICON_MASK_STATUS_BAR = 48;
    public static final int HD_ICON_SHOW_RULE_IMS_REGISTRATION = 0;
    public static final int HD_ICON_SHOW_RULE_SPEECH_CODEC = 1;
    private static HwCarrierConfigManager sInstance = new HwCarrierConfigManager();

    public static HwCarrierConfigManager getDefault() {
        return sInstance;
    }

    public int getCarrierVolteHDIconFlag(Context context, int subId) {
        return HwCarrierConfigManagerInner.getDefault().getCarrierVolteHDIconFlag(context, subId);
    }

    public int getShowHDIconRule(Context context, int subId) {
        return HwCarrierConfigManagerInner.getDefault().getShowHDIconRule(context, subId);
    }

    public boolean needShowHDIcon(int flag, int mask) {
        return HwCarrierConfigManagerInner.getDefault().needShowHDIcon(flag, mask);
    }

    public int getVolteIconRule(Context context, int subId, int mask) {
        return HwCarrierConfigManagerInner.getDefault().getVolteIconRule(context, subId, mask);
    }

    public int getVowifiIconRule(Context context, int subId, int mask) {
        return HwCarrierConfigManagerInner.getDefault().getVowifiIconRule(context, subId, mask);
    }
}
