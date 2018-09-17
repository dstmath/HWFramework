package com.android.server.rms.iaware.cpu;

import android.os.SystemProperties;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import java.util.List;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class SubSwitchConfig extends CPUCustBaseConfig {
    private static final int BIT_POS_MAX = 31;
    private static final int BIT_POS_MIN = 0;
    private static final String CONFIG_SUBSWITCH = "sub_switch";
    private static final int DEFAULT_SUBSWITCH = 0;
    private static final String ITEM_PROP_TYPE = "type";
    private static final String ITEM_TYPE_SWITCH = "switch";
    private static final String PROP_SUBSWITCH = "persist.sys.cpuset.subswitch";
    private static final String SUBITEM_PROP_BIT = "bit";
    private static final String TAG = "SubSwitchConfig";
    private int mSubSwitch;

    private int setBit(int r1, java.lang.String r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.cpu.SubSwitchConfig.setBit(int, java.lang.String, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.cpu.SubSwitchConfig.setBit(int, java.lang.String, int):int");
    }

    public SubSwitchConfig() {
        this.mSubSwitch = DEFAULT_SUBSWITCH;
        this.mSubSwitch = getSubSwitch();
    }

    public void setConfig(CPUFeature feature) {
        SystemProperties.set(PROP_SUBSWITCH, Integer.toString(this.mSubSwitch));
    }

    private int getSubSwitch() {
        List<Item> awareConfigItemList = getItemList(CONFIG_SUBSWITCH);
        if (awareConfigItemList == null) {
            AwareLog.d(TAG, "can not get subswitch config");
            return DEFAULT_SUBSWITCH;
        }
        for (Item item : awareConfigItemList) {
            Map<String, String> configPropertries = item.getProperties();
            if (configPropertries != null) {
                String itemType = (String) configPropertries.get(ITEM_PROP_TYPE);
                if (itemType != null && itemType.equals(ITEM_TYPE_SWITCH)) {
                    return getSubSwitchFromSubItem(item);
                }
            }
        }
        return DEFAULT_SUBSWITCH;
    }

    private int getSubSwitchFromSubItem(Item item) {
        List<SubItem> subItemList = getSubItem(item);
        if (subItemList == null) {
            AwareLog.d(TAG, "can not get subswitch config subitem");
            return DEFAULT_SUBSWITCH;
        }
        int subSwitch = DEFAULT_SUBSWITCH;
        for (SubItem subItem : subItemList) {
            String itemValue = subItem.getValue();
            if (itemValue != null) {
                Map<String, String> subItemProps = subItem.getProperties();
                if (subItemProps != null) {
                    String bitPosStr = (String) subItemProps.get(SUBITEM_PROP_BIT);
                    if (bitPosStr != null) {
                        try {
                            int bitPos = Integer.parseInt(bitPosStr);
                            if (isInBitPosRange(bitPos)) {
                                subSwitch = setBit(subSwitch, itemValue, bitPos);
                            }
                        } catch (NumberFormatException e) {
                            AwareLog.d(TAG, "parse bitpos failed:" + bitPosStr);
                        }
                    }
                }
            }
        }
        return subSwitch;
    }

    private boolean isInBitPosRange(int bitPos) {
        return bitPos >= 0 && bitPos <= BIT_POS_MAX;
    }
}
