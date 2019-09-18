package com.android.internal.telephony.vsim;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.FileObserver;
import com.android.internal.util.ArrayUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class HwVSimApkObserver {
    private static String APKINSTALL_FILE_PATH = "xml/APKInstallListEMUI5Release.txt";
    private static String CUSTSKYTONE_DIR_PATH = "app/SkyTone";
    private static String HSF_APK_PATH = "/system/priv-app/HwServiceFramework/HwServiceFramework.apk";
    /* access modifiers changed from: private */
    public static String HSF_DIR_PATH = "/system/priv-app/HwServiceFramework/";
    private static final String LOG_TAG = "VSimApkObserver";
    private static String SKYTONE_APK_PATH = "/system/app/SkyTone/SkyTone.apk";
    /* access modifiers changed from: private */
    public static String SKYTONE_DIR_PATH = "/system/app/SkyTone/";
    private static final String VSIM_BROADCAST_RECEIVER_NAME = "com.huawei.android.vsim.receiver.VSimBroadcastReceiver";
    private static final String VSIM_PKG_NAME = "com.huawei.skytone";
    private static final String VSIM_SERVICE_NAME = "com.huawei.android.vsim.service.VSimService";
    private boolean isRequestDisable;
    private final BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private CustSkytoneFileObserver mCustSkytoneApkObserver;
    private HSFFileObserver mHSFApkObserver;
    private boolean mIsRegBroadcastReceiver;
    private SkytoneFileObserver mSkytoneApkObserver;
    private String mSkytoneFilPath;

    private class CustSkytoneFileObserver extends FileObserver {
        public CustSkytoneFileObserver(String path) {
            super(path);
        }

        public void onEvent(int event, String path) {
            if (event == 512 || event == 1024) {
                HwVSimApkObserver.this.logd("CUSTSKYTONE:FileObserver.DELETE OR FileObserver.DELETE_SELF");
                if (HwVSimApkObserver.this.needsToStopVSim()) {
                    HwVSimApkObserver.this.logd("SkyTone Apk is not Exist");
                    HwVSimApkObserver.this.stopVSimAsyn();
                }
            }
        }
    }

    private class HSFFileObserver extends FileObserver {
        public HSFFileObserver() {
            super(HwVSimApkObserver.HSF_DIR_PATH);
        }

        public void onEvent(int event, String path) {
            if (event == 512 || event == 1024) {
                HwVSimApkObserver.this.logd("HSF:FileObserver.DELETE OR FileObserver.DELETE_SELF");
                if (HwVSimApkObserver.this.needsToStopVSim()) {
                    HwVSimApkObserver.this.logd("HSF Apk is not Exist");
                    HwVSimApkObserver.this.stopVSimAsyn();
                }
            }
        }
    }

    private class SkytoneFileObserver extends FileObserver {
        public SkytoneFileObserver() {
            super(HwVSimApkObserver.SKYTONE_DIR_PATH);
        }

        public void onEvent(int event, String path) {
            if (event == 512 || event == 1024) {
                HwVSimApkObserver.this.logd("SKYTONE:FileObserver.DELETE OR FileObserver.DELETE_SELF");
                if (HwVSimApkObserver.this.needsToStopVSim()) {
                    HwVSimApkObserver.this.logd("SkyTone Apk is not Exist");
                    HwVSimApkObserver.this.stopVSimAsyn();
                }
            }
        }
    }

    private static class VsimDisableThread extends Thread {
        public void run() {
            HwVSimController.getInstance().disableVSim();
        }
    }

    public HwVSimApkObserver() {
        this.isRequestDisable = false;
        this.mSkytoneFilPath = null;
        this.mSkytoneApkObserver = new SkytoneFileObserver();
        this.mHSFApkObserver = new HSFFileObserver();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null) {
                        HwVSimApkObserver hwVSimApkObserver = HwVSimApkObserver.this;
                        hwVSimApkObserver.logd("Receive broadcast action : " + action);
                        if ("com.huawei.vsim.action.SIM_TRAFFIC".equals(action) || "com.huawei.vsim.action.SIM_PLMN_SELINFO".equals(action)) {
                            HwVSimApkObserver.this.judegIfNeedDisableVSim();
                        }
                    }
                }
            }
        };
        this.mIsRegBroadcastReceiver = false;
        this.mSkytoneFilPath = getCustSkyToneFilePath();
        if (this.mSkytoneFilPath != null) {
            this.mCustSkytoneApkObserver = new CustSkytoneFileObserver(this.mSkytoneFilPath);
        }
    }

    public void startWatching(Context context) {
        logd("startWatching");
        synchronized (this) {
            this.isRequestDisable = false;
        }
        this.mSkytoneApkObserver.startWatching();
        this.mHSFApkObserver.startWatching();
        if (this.mCustSkytoneApkObserver != null) {
            this.mCustSkytoneApkObserver.startWatching();
        }
        this.mContext = context;
        registerNetStateReceiver();
    }

    public void stopWatching() {
        logd("stopWatching");
        this.mSkytoneApkObserver.stopWatching();
        this.mHSFApkObserver.stopWatching();
        if (this.mCustSkytoneApkObserver != null) {
            this.mCustSkytoneApkObserver.stopWatching();
        }
        unregisterNetStateReceiver();
    }

    /* access modifiers changed from: private */
    public void judegIfNeedDisableVSim() {
        if (HwVSimUtils.isVSimOn() && needsToStopVSimByMainComponent()) {
            logd("SkyTone Apk Main Component is not Enabled, disable vsim.");
            stopVSimAsyn();
        }
    }

    public void registerNetStateReceiver() {
        if (!this.mIsRegBroadcastReceiver) {
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction("com.huawei.vsim.action.SIM_TRAFFIC");
            mFilter.addAction("com.huawei.vsim.action.SIM_PLMN_SELINFO");
            this.mContext.registerReceiver(this.mBroadcastReceiver, mFilter);
            this.mIsRegBroadcastReceiver = true;
        }
    }

    public void unregisterNetStateReceiver() {
        if (this.mIsRegBroadcastReceiver) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mIsRegBroadcastReceiver = false;
        }
    }

    public boolean isServiceAutoStartForbidden(ComponentName... componentNames) {
        logd("into isServiceAutoStartForbidden");
        if (this.mContext == null || ArrayUtils.isEmpty(componentNames)) {
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            logd("PackageManager is null");
            return false;
        }
        for (ComponentName componentName : componentNames) {
            logd(componentName + "state is " + pm.getComponentEnabledSetting(componentName));
            if (2 == pm.getComponentEnabledSetting(componentNames[r3])) {
                logd("componentName AutoStartForbidden" + componentName.getShortClassName());
                return true;
            }
        }
        try {
            if (pm.getApplicationInfo(VSIM_PKG_NAME, 0) == null) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            logd("NameNotFoundException when get applicationinfo by skytone name");
            return true;
        }
    }

    private boolean isApkExist() {
        return new File(SKYTONE_APK_PATH).exists() || new File(HSF_APK_PATH).exists() || (this.mSkytoneFilPath != null && new File(this.mSkytoneFilPath).exists());
    }

    /* access modifiers changed from: private */
    public boolean needsToStopVSim() {
        return !isApkExist() && HwVSimUtils.isVSimOn();
    }

    private boolean needsToStopVSimByMainComponent() {
        return isServiceAutoStartForbidden(new ComponentName(VSIM_PKG_NAME, VSIM_BROADCAST_RECEIVER_NAME), new ComponentName(VSIM_PKG_NAME, VSIM_SERVICE_NAME));
    }

    /* access modifiers changed from: private */
    public void stopVSimAsyn() {
        synchronized (this) {
            if (!this.isRequestDisable) {
                new VsimDisableThread().start();
                this.isRequestDisable = true;
            }
        }
    }

    private String getCustSkyToneFilePath() {
        ArrayList<File> allAPKList = null;
        try {
            allAPKList = HwCfgFilePolicy.getCfgFileList(APKINSTALL_FILE_PATH, 0);
        } catch (NoClassDefFoundError e) {
            logd("HwCfgFilePolicy NoClassDefFoundError");
        }
        if (allAPKList != null && allAPKList.size() > 0) {
            HashSet<String> privInstallSet = new HashSet<>();
            HashSet<String> installList = getMultiAPKInstallList(allAPKList, privInstallSet);
            if (installList.size() > 0) {
                Iterator<String> it = installList.iterator();
                while (it.hasNext()) {
                    String installPath = it.next();
                    if (installPath != null && installPath.contains(CUSTSKYTONE_DIR_PATH)) {
                        logd("installPath : " + installPath);
                        return installPath;
                    }
                }
            }
            if (privInstallSet.size() > 0) {
                Iterator<String> it2 = privInstallSet.iterator();
                while (it2.hasNext()) {
                    String privInstallPath = it2.next();
                    if (privInstallPath != null && privInstallPath.contains(CUSTSKYTONE_DIR_PATH)) {
                        logd("privInstallSet : " + privInstallPath);
                        return privInstallPath;
                    }
                }
            }
        }
        return null;
    }

    private HashSet<String> getMultiAPKInstallList(List<File> lists, HashSet<String> privInstallSet) {
        HashSet<String> allNonPriv = new HashSet<>();
        for (File list : lists) {
            logd("getMultiAPKInstallList  list : " + list);
            Iterator<String> it = getAPKInstallList(list, privInstallSet).iterator();
            while (it.hasNext()) {
                allNonPriv.add(it.next());
            }
        }
        return allNonPriv;
    }

    private HashSet<String> getAPKInstallList(File scanApk, HashSet<String> privInstallSet) {
        HashSet<String> installSet = new HashSet<>();
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(scanApk), "UTF-8"));
            while (true) {
                String readLine = reader2.readLine();
                String line = readLine;
                if (readLine != null) {
                    String[] strSplit = line.trim().split(",");
                    String packagePath = getCustPackagePath(strSplit[0]);
                    if (2 != strSplit.length || !isPackageFilename(strSplit[0].trim()) || !strSplit[1].trim().equalsIgnoreCase("priv") || privInstallSet == null) {
                        if (isPackageFilename(strSplit[0].trim()) && packagePath != null) {
                            installSet.add(packagePath.trim());
                        }
                    } else if (packagePath != null) {
                        privInstallSet.add(packagePath.trim());
                    }
                } else {
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            reader2.close();
        } catch (FileNotFoundException e2) {
            logd("FileNotFound No such file or directory :" + scanApk.getPath());
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
        return installSet;
    }

    private String getCustPackagePath(String readLine) {
        int lastIndex = readLine.lastIndexOf(47);
        if (lastIndex > 0) {
            return readLine.substring(0, lastIndex + 1);
        }
        logd("getAPKInstallList ERROR:  " + readLine);
        return null;
    }

    private boolean isPackageFilename(String name) {
        return name != null && name.endsWith(".apk");
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }
}
