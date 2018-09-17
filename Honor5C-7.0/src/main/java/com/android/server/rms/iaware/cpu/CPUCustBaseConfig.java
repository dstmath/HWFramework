package com.android.server.rms.iaware.cpu;

import android.os.SystemProperties;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: CPUXmlConfiguration */
class CPUCustBaseConfig {
    public static final String CPUCONFIG_GAP_IDENTIFIER = ";";
    public static final String CPUCONFIG_INVALID_STR = "#";
    private static final String CPU_FEATURE = "CPU";
    public static final int IAWARED_SEND_MAXLEN = 512;
    protected static final String ITEM_BG = "bg";
    protected static final String ITEM_BOOST = "boost";
    protected static final String ITEM_BOOST_BY_EACH_FLING = "boost_by_each_fling";
    protected static final String ITEM_ENABLE_SKIPPED_FRAME = "enable_skipped_frame";
    protected static final String ITEM_FG = "fg";
    protected static final String ITEM_FLING_BOOST_BIG_CORE = "boost_big_core";
    protected static final String ITEM_FLING_BOOST_DURATION = "boost_duration";
    protected static final String ITEM_FLING_MIN_FREQ = "min_freq";
    protected static final String ITEM_GO_HISPEED_LOAD_B = "go_hispeed_load_b";
    protected static final String ITEM_GO_HISPEED_LOAD_L = "go_hispeed_load_l";
    protected static final String ITEM_HISPEED_FREQ_B = "hispeed_freq_b";
    protected static final String ITEM_HISPEED_FREQ_L = "hispeed_freq_l";
    protected static final String ITEM_KEYBG = "key_bg";
    protected static final String ITEM_SCROLLER_IPA_POWER = "ipa_power";
    protected static final String ITEM_SYSBG = "sys_bg";
    protected static final String ITEM_TABOOST = "ta_boost";
    protected static final String ITEM_TARGET_LOAD_FREQ_B = "target_load_freq_b";
    protected static final String ITEM_TARGET_LOAD_FREQ_L = "target_load_freq_l";
    protected static final String ITEM_TOP_APP = "top_app";
    public static final String[] mCpusetItemIndex = null;
    public static final String[] mInteractiveItemIndex = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.cpu.CPUCustBaseConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.cpu.CPUCustBaseConfig.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.cpu.CPUCustBaseConfig.<clinit>():void");
    }

    CPUCustBaseConfig() {
    }

    protected void setConfig(CPUFeature feature) {
    }

    protected List<Item> getItemList(String configName) {
        AwareConfig awareConfig = CPUResourceConfigControl.getInstance().getAwareCustConfig(CPU_FEATURE, configName);
        if (awareConfig == null) {
            return null;
        }
        return awareConfig.getConfigList();
    }

    protected List<SubItem> getSubItem(Item item) {
        if (item == null) {
            return null;
        }
        return item.getSubItemList();
    }

    protected void obtainConfigInfo(String configName, Map<String, String> item2PropMap, Map<String, CPUPropInfoItem> outInfoMap) {
        outInfoMap.clear();
        List<Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList != null) {
            for (Item item : awareConfigItemList) {
                List<SubItem> subItemList = getSubItem(item);
                if (subItemList != null) {
                    for (SubItem subItem : subItemList) {
                        String itemName = subItem.getName();
                        String itemValue = subItem.getValue();
                        String propName = (String) item2PropMap.get(itemName);
                        if (!(itemName == null || itemValue == null || propName == null)) {
                            outInfoMap.put(itemName, new CPUPropInfoItem(propName, itemValue));
                        }
                    }
                }
            }
        }
    }

    protected void applyConfig(Map<String, CPUPropInfoItem> infoMap) {
        for (Entry<String, CPUPropInfoItem> entry : infoMap.entrySet()) {
            CPUPropInfoItem item = (CPUPropInfoItem) entry.getValue();
            SystemProperties.set(item.mProp, item.mValue);
        }
    }
}
