package com.android.server.rms.iaware.appmng;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.text.TextUtils;
import android.util.ArraySet;
import com.android.internal.util.MemInfoReader;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public final class AppMngConfig {
    private static final String APPMNG = "AppManagement";
    private static final String HABIT_FILTER_LIST = "HabitFilterList";
    private static final String TAG = "AppMngConfig";
    private static boolean sAbroadFlag;
    private static int sAdjCustTopN;
    private static long sBgDecay;
    private static int sImCnt;
    private static long sKeySysDecay;
    private static boolean sKillMore;
    private static long sMemMB;
    private static boolean sRestartFlag;
    private static long sSysDecay;
    private static int sTopN;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.appmng.AppMngConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.appmng.AppMngConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.appmng.AppMngConfig.<clinit>():void");
    }

    public static int getTopN() {
        return sTopN;
    }

    public static int getImCnt() {
        return sImCnt;
    }

    public static long getSysDecay() {
        return sSysDecay;
    }

    public static long getKeySysDecay() {
        return sKeySysDecay;
    }

    public static boolean getRestartFlag() {
        return sRestartFlag;
    }

    public static int getAdjCustTopN() {
        return sAdjCustTopN;
    }

    public static long getBgDecay() {
        return sBgDecay;
    }

    public static boolean getAbroadFlag() {
        return sAbroadFlag;
    }

    public static void setTopN(int value) {
        sTopN = value;
    }

    public static void setImCnt(int value) {
        sImCnt = value;
    }

    public static void setSysDecay(long value) {
        sSysDecay = value;
    }

    public static void setKeySysDecay(long value) {
        sKeySysDecay = value;
    }

    public static long getMemorySize() {
        return sMemMB;
    }

    public static void setRestartFlag(boolean restartFlag) {
        sRestartFlag = restartFlag;
    }

    public static void setAdjCustTopN(int topN) {
        sAdjCustTopN = topN;
    }

    public static void setBgDecay(long decay) {
        sBgDecay = decay;
    }

    public static void setAbroadFlag(boolean flag) {
        sAbroadFlag = flag;
    }

    public static void setKillMoreFlag(boolean killMore) {
        sKillMore = killMore;
    }

    public static boolean getKillMoreFlag() {
        return sKillMore;
    }

    public static void init() {
        MemInfoReader minfo = new MemInfoReader();
        minfo.readMemInfo();
        sMemMB = minfo.getTotalSize() / MemoryConstant.MB_SIZE;
    }

    private static AwareConfig getConfig(String featureName, String configName) {
        if (TextUtils.isEmpty(featureName) || TextUtils.isEmpty(configName)) {
            AwareLog.e(TAG, "featureName or configName is null");
            return null;
        }
        AwareConfig configList = null;
        try {
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                configList = awareservice.getConfig(featureName, configName);
            } else {
                AwareLog.i(TAG, "can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "AppMngFeature getConfig RemoteException");
        }
        return configList;
    }

    public static ArraySet<String> getHabitFilterListFromCMS() {
        AwareConfig habitConfig = getConfig(APPMNG, HABIT_FILTER_LIST);
        if (habitConfig == null) {
            AwareLog.w(TAG, "getHabitFilterListFromCMS failure cause null configList");
            return null;
        }
        ArraySet<String> pkgSet = new ArraySet();
        for (Item item : habitConfig.getConfigList()) {
            if (item == null || item.getSubItemList() == null) {
                AwareLog.w(TAG, "getHabitFilterListFromCMS continue cause null item");
            } else {
                for (SubItem subitem : item.getSubItemList()) {
                    if (!(subitem == null || TextUtils.isEmpty(subitem.getValue()))) {
                        pkgSet.add(subitem.getValue());
                    }
                }
            }
        }
        return pkgSet;
    }
}
