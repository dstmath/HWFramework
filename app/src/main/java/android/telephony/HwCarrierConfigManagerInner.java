package android.telephony;

import android.content.Context;
import android.os.PersistableBundle;

public class HwCarrierConfigManagerInner {
    private static final int BINARY = 2;
    public static final int HD_ICON_MASK_CALL_LOG = 12;
    public static final int HD_ICON_MASK_DIALER = 192;
    public static final int HD_ICON_MASK_INCALL_UI = 3;
    public static final int HD_ICON_MASK_STATUS_BAR = 48;
    private static final int HD_ICON_NOT_SET = -1;
    private static final int HW_VOLTE_ICON_RULE = 0;
    private static final String KEY_CARRIER_SHOW_VOLTE_HD_RULE = "carrier_show_volte_hd_rule";
    private static final String KEY_CARRIER_VOLTE_HD_ICON_FLAG = "carrier_volte_hd_icon_flag";
    private static final int NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG = -1;
    private static final int NOT_SHOW_HD_ICON_BY_CARRIER_CONFIG = 0;
    private static final int OFFSET = 2;
    private static final int SHOW_HD_ICON_BY_CARRIER_CONFIG = 1;
    private static final int SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG = 2;
    private static final String TAG = "HwCarrierConfigManagerInner";
    private static HwCarrierConfigManagerInner sInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.HwCarrierConfigManagerInner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.HwCarrierConfigManagerInner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.HwCarrierConfigManagerInner.<clinit>():void");
    }

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

    private int getHDIconFromCarrierConfig(int flag, int mask) {
        Rlog.d(TAG, "getHDIconFromCarrierConfig flag = " + flag + ",mask = " + mask);
        int showHDIcon = flag & mask;
        Rlog.d(TAG, "showHDIcon original value is " + showHDIcon);
        switch (mask) {
            case HD_ICON_MASK_CALL_LOG /*12*/:
                showHDIcon >>= SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG;
                break;
            case HD_ICON_MASK_STATUS_BAR /*48*/:
                showHDIcon = (showHDIcon >> SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG) >> SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG;
                break;
            case HD_ICON_MASK_DIALER /*192*/:
                showHDIcon = ((showHDIcon >> SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG) >> SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG) >> SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG;
                break;
        }
        Rlog.d(TAG, "showHDIcon final value is " + showHDIcon);
        return showHDIcon;
    }

    public int getCarrierVolteHDIconFlag(Context context, int subId) {
        if (context == null) {
            return NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG;
        }
        PersistableBundle pb = getConfigForAnySubId(context, subId);
        if (pb == null) {
            return NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG;
        }
        String flagStr = pb.getString(KEY_CARRIER_VOLTE_HD_ICON_FLAG);
        if (flagStr == null) {
            Rlog.e(TAG, "getCarrierVolteHDIconFlag flagStr null, subId = " + subId);
            return NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG;
        }
        try {
            int flag = Integer.parseInt(flagStr, SHOW_VOLTE_VOWIFI_ICON_BY_CARRIER_CONFIG);
            Rlog.d(TAG, "getCarrierVolteHDIconFlag flag = " + flag + ",subId = " + subId + ",flagStr = " + flagStr);
            return flag;
        } catch (NumberFormatException e) {
            Rlog.e(TAG, "getCarrierVolteHDIconFlag parseInt error flagStr = " + flagStr + ",subId = " + subId);
            return NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG;
        }
    }

    public int getShowHDIconRule(Context context, int subId) {
        if (context == null) {
            return NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG;
        }
        PersistableBundle pb = getConfigForAnySubId(context, subId);
        if (pb == null) {
            return NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG;
        }
        int result = pb.getInt(KEY_CARRIER_SHOW_VOLTE_HD_RULE, NOT_SET_SHOW_HD_ICON_BY_CARRIER_CONFIG);
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
