package com.android.server.rms.memrepair;

import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.android.server.rms.memrepair.NativeAppMemRepair;
import java.util.ArrayList;
import java.util.List;

public class SysMemMngBigData {
    private static final String COLON = ":";
    private static final Object LOCK = new Object();
    public static final int NATIVE_APP = 1002;
    private static final String SINGLE_QUOTE = "\"";
    public static final int SYS_APP = 1001;
    private static final String TAG = "SysMemBigData";
    private static SysMemMngBigData sInstance;
    private final List<String> mNativeStringList = new ArrayList();
    private final List<String> mSysStringList = new ArrayList();

    private SysMemMngBigData() {
    }

    public static SysMemMngBigData getInstance() {
        SysMemMngBigData sysMemMngBigData;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new SysMemMngBigData();
            }
            sysMemMngBigData = sInstance;
        }
        return sysMemMngBigData;
    }

    public void fillSysMemBigData(AwareProcessBlockInfo sysApp, NativeAppMemRepair.NativeProcessInfo nativeApp, int type) {
        if (type == 1001) {
            if (sysApp != null && sysApp.procProcessList != null) {
                String strProcs = "";
                for (AwareProcessInfo info : sysApp.procProcessList) {
                    if (info != null) {
                        strProcs = strProcs + info.procPid + " ";
                    }
                }
                StringBuffer sb = new StringBuffer();
                sb.append(keyAndValue(SceneRecogFeature.DATA_PID, strProcs));
                sb.append(keyAndValue("uid", String.valueOf(sysApp.procUid)));
                sb.append(keyAndValue("pkg", sysApp.procPackageName));
                sb.append(keyAndValue("cleanType", sysApp.procCleanType.description()));
                sb.append(keyAndValue("weight", String.valueOf(sysApp.procWeight)));
                sb.append(keyAndValue("reason", sysApp.procReason));
                synchronized (this.mSysStringList) {
                    this.mSysStringList.add(sb.toString());
                }
            } else {
                return;
            }
        }
        if (type == 1002 && nativeApp != null) {
            StringBuffer sb2 = new StringBuffer();
            sb2.append(keyAndValue(SceneRecogFeature.DATA_PID, String.valueOf(nativeApp.getPid())));
            sb2.append(keyAndValue("procName", nativeApp.getProcessName()));
            sb2.append(keyAndValue("thresholdPss", String.valueOf(nativeApp.getPssThreshold())));
            sb2.append(keyAndValue("currPss", String.valueOf(nativeApp.getCurrentPss())));
            synchronized (this.mNativeStringList) {
                this.mNativeStringList.add(sb2.toString());
            }
        }
    }

    public String savaBigData(boolean clearData) {
        List<String> sysStringListClone = new ArrayList<>();
        List<String> nativeStringListClone = new ArrayList<>();
        synchronized (this.mSysStringList) {
            sysStringListClone.addAll(this.mSysStringList);
        }
        synchronized (this.mNativeStringList) {
            nativeStringListClone.addAll(this.mNativeStringList);
        }
        StringBuffer sb = new StringBuffer();
        String separator = System.lineSeparator();
        sb.append("[iAware-SysMemMngBigData_Start]");
        sb.append(separator);
        sb.append("[SYS_APP]");
        sb.append(separator);
        for (String str : sysStringListClone) {
            sb.append(str);
            sb.append(separator);
        }
        sb.append("[NATIVE_APP]");
        sb.append(separator);
        for (String str2 : nativeStringListClone) {
            sb.append(str2);
            sb.append(separator);
        }
        sb.append("[iAware-SysMemMngBigData_End]");
        sb.append(separator);
        if (clearData) {
            clear();
        }
        return sb.toString();
    }

    public void clear() {
        synchronized (this.mSysStringList) {
            this.mSysStringList.clear();
        }
        synchronized (this.mNativeStringList) {
            this.mNativeStringList.clear();
        }
    }

    private static String keyAndValue(String key, String value) {
        return SINGLE_QUOTE + key + SINGLE_QUOTE + COLON + SINGLE_QUOTE + value + SINGLE_QUOTE;
    }
}
