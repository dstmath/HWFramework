package com.huawei.iaware.userhabit;

import android.content.ContentResolver;
import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.server.rms.algorithm.utils.IAwareHabitUtils;
import com.android.server.rms.algorithm.utils.ProtectApp;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NRTHabitProtectListTrainAlgorithm {
    private static final String SEPARATOR = ";";
    private static final String TAG = "NRTHabitProtectListTrainAlgorithm";
    private static final int UNUSED_COUNT = 0;
    private static boolean mIsEmailAppsUsed;
    private static boolean mIsIMAppsUsed;
    private ContentResolver mContentResolver;
    private Context mContext;
    private final List<ProtectApp> mHabitProtectAppsList;
    private final List<ProtectApp> mProtectAppsList;
    private Map<String, Integer> mUsageCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.iaware.userhabit.NRTHabitProtectListTrainAlgorithm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.iaware.userhabit.NRTHabitProtectListTrainAlgorithm.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.iaware.userhabit.NRTHabitProtectListTrainAlgorithm.<clinit>():void");
    }

    public NRTHabitProtectListTrainAlgorithm(Context context) {
        this.mUsageCount = null;
        this.mContentResolver = null;
        this.mContext = null;
        this.mProtectAppsList = new ArrayList();
        this.mHabitProtectAppsList = new ArrayList();
        if (context != null) {
            this.mContext = context;
            this.mContentResolver = this.mContext.getContentResolver();
        }
    }

    void deinit() {
        clearHabitProtectList();
    }

    void habitProtectListTrain(Map<String, Integer> map, int i) {
        if (map == null || map.size() == 0) {
            AwareLog.d(TAG, "No need to train!");
            return;
        }
        AwareLog.i(TAG, "Habit protectList train begin.");
        long currentTimeMillis = System.currentTimeMillis();
        this.mUsageCount = map;
        loadHabitProtectList(i);
        if (checkAppsUsedInfo()) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (ProtectApp protectApp : this.mProtectAppsList) {
                int i2;
                float f;
                String appPkgName = protectApp.getAppPkgName();
                String recentUsed = protectApp.getRecentUsed();
                int appType = protectApp.getAppType();
                if (!mIsIMAppsUsed || appType != 0) {
                    if (mIsEmailAppsUsed) {
                        if (appType != 1) {
                        }
                    }
                }
                if (this.mUsageCount.containsKey(appPkgName)) {
                    appPkgName = recentUsed + this.mUsageCount.get(appPkgName) + SEPARATOR;
                } else {
                    appPkgName = recentUsed + 0 + SEPARATOR;
                }
                String[] split = appPkgName.split(SEPARATOR);
                int length = split.length;
                appType = 0;
                float avgUsedFrequency = protectApp.getAvgUsedFrequency();
                if (length <= 14) {
                    recentUsed = appPkgName;
                    i2 = 0;
                } else {
                    recentUsed = appPkgName.substring(appPkgName.indexOf(SEPARATOR) + 1);
                    i2 = 1;
                }
                while (i2 < split.length) {
                    appType += getRecuentUsedCount(split[i2]);
                    i2++;
                }
                if (length == 0) {
                    f = avgUsedFrequency;
                } else {
                    try {
                        f = Float.parseFloat(decimalFormat.format((double) (((float) appType) / ((float) length))));
                    } catch (NumberFormatException e) {
                        AwareLog.w(TAG, "parseFloat exception " + e.toString());
                        f = 0.0f;
                    }
                }
                protectApp.setRecentUsed(recentUsed);
                protectApp.setAvgUsedFrequency(f);
            }
            updateHabitProtectListDB(i);
            clearData();
            AwareLog.i(TAG, "habit protectList train end. spend times:" + (System.currentTimeMillis() - currentTimeMillis) + "ms");
            return;
        }
        AwareLog.i(TAG, "Ineffective train end. spend times:" + (System.currentTimeMillis() - currentTimeMillis) + "ms" + mIsEmailAppsUsed + " " + mIsIMAppsUsed);
    }

    private boolean checkAppsUsedInfo() {
        for (ProtectApp protectApp : this.mProtectAppsList) {
            String appPkgName = protectApp.getAppPkgName();
            int appType = protectApp.getAppType();
            if (this.mUsageCount.containsKey(appPkgName)) {
                if (appType == 0) {
                    setIsIMAppUsed(true);
                } else if (appType == 1) {
                    setIsEmailAppUsed(true);
                }
            }
        }
        return mIsEmailAppsUsed || mIsIMAppsUsed;
    }

    private static void setIsIMAppUsed(boolean z) {
        mIsIMAppsUsed = z;
    }

    private static void setIsEmailAppUsed(boolean z) {
        mIsEmailAppsUsed = z;
    }

    private int getRecuentUsedCount(String str) {
        int i = 0;
        try {
            i = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            AwareLog.w(TAG, "parseInt exception " + e.toString());
        }
        return i;
    }

    private void loadHabitProtectList(int i) {
        this.mProtectAppsList.clear();
        Collection arrayList = new ArrayList();
        IAwareHabitUtils.loadUnDeletedHabitProtectList(this.mContentResolver, arrayList, i);
        this.mProtectAppsList.addAll(arrayList);
    }

    private void clearData() {
        this.mUsageCount.clear();
        this.mProtectAppsList.clear();
        setIsIMAppUsed(false);
        setIsEmailAppUsed(false);
    }

    private void updateHabitProtectListDB(int i) {
        if (this.mProtectAppsList != null) {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (ProtectApp protectApp : this.mProtectAppsList) {
                if (!mIsIMAppsUsed || protectApp.getAppType() != 0) {
                    if (mIsEmailAppsUsed) {
                        if (protectApp.getAppType() != 1) {
                        }
                    }
                }
                IAwareHabitUtils.updateHabitProtectList(this.mContentResolver, protectApp.getAppPkgName(), protectApp.getRecentUsed(), decimalFormat.format((double) protectApp.getAvgUsedFrequency()), i);
            }
        }
    }

    void initHabitProtectList(int i) {
        synchronized (this.mHabitProtectAppsList) {
            this.mHabitProtectAppsList.clear();
            IAwareHabitUtils.loadHabitProtectList(this.mContext, this.mHabitProtectAppsList, i);
        }
    }

    private void clearHabitProtectList() {
        synchronized (this.mHabitProtectAppsList) {
            this.mHabitProtectAppsList.clear();
        }
    }

    void reportCheckHabitProtectList(String str, int i) {
        synchronized (this.mHabitProtectAppsList) {
            for (ProtectApp protectApp : this.mHabitProtectAppsList) {
                if (protectApp.getAppPkgName().equals(str)) {
                    if (protectApp.getDeletedTag() == 0) {
                        protectApp.setDeletedTag(1);
                    }
                    int i2 = 1;
                }
            }
            Object obj = null;
        }
        if (obj != null) {
            IAwareHabitUtils.deleteHabitProtectList(this.mContentResolver, str, i);
        }
    }

    void foregroundCheckHabitProtectList(String str, int i) {
        Object obj;
        Object obj2;
        synchronized (this.mHabitProtectAppsList) {
            for (ProtectApp protectApp : this.mHabitProtectAppsList) {
                if (protectApp.getAppPkgName().equals(str)) {
                    if (protectApp.getDeletedTag() != 1) {
                        obj = null;
                        obj2 = null;
                    } else {
                        protectApp.setDeletedTag(0);
                        int i2 = 1;
                        obj2 = null;
                    }
                }
            }
            obj = null;
            obj2 = 1;
        }
        if (obj2 != null) {
            int i3;
            boolean isInAppsTypeList = IAwareHabitUtils.isInAppsTypeList(str);
            int appType = IAwareHabitUtils.getAppType(this.mContext, str);
            if (appType == 0 || appType == 311) {
                i3 = 0;
            } else if (appType == 1 || appType == MemoryConstant.MSG_DIRECT_SWAPPINESS) {
                i3 = 1;
            } else {
                if ((appType == 5 || appType == 310) && !isInAppsTypeList) {
                    IAwareHabitUtils.insertHabitProtectList(this.mContentResolver, str, 2, i);
                }
                return;
            }
            IAwareHabitUtils.insertHabitProtectList(this.mContentResolver, str, i3, i);
            synchronized (this.mHabitProtectAppsList) {
                this.mHabitProtectAppsList.add(new ProtectApp(str, i3, 0, i));
            }
        }
        if (obj != null) {
            IAwareHabitUtils.updateDeletedHabitProtectList(this.mContentResolver, str, i);
        }
    }
}
