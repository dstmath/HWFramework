package com.android.internal.telephony.vsim;

import android.os.FileObserver;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;

public class HwVSimApkObserver {
    private static String APKINSTALL_FILE_PATH = null;
    private static String CUSTSKYTONE_DIR_PATH = null;
    private static String HSF_APK_PATH = null;
    private static String HSF_DIR_PATH = null;
    private static final String LOG_TAG = "VSimApkObserver";
    private static String SKYTONE_APK_PATH;
    private static String SKYTONE_DIR_PATH;
    private boolean isRequestDisable;
    private CustSkytoneFileObserver mCustSkytoneApkObserver;
    private HSFFileObserver mHSFApkObserver;
    private SkytoneFileObserver mSkytoneApkObserver;
    private String mSkytoneFilPath;

    private class CustSkytoneFileObserver extends FileObserver {
        public CustSkytoneFileObserver(String path) {
            super(path);
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 512:
                case 1024:
                    HwVSimApkObserver.this.logd("CUSTSKYTONE:FileObserver.DELETE OR FileObserver.DELETE_SELF");
                    if (HwVSimApkObserver.this.needsToStopVSim()) {
                        HwVSimApkObserver.this.logd("SkyTone Apk is not Exist");
                        HwVSimApkObserver.this.stopVSimAsyn();
                    }
                default:
            }
        }
    }

    private class HSFFileObserver extends FileObserver {
        public HSFFileObserver() {
            super(HwVSimApkObserver.HSF_DIR_PATH);
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 512:
                case 1024:
                    HwVSimApkObserver.this.logd("HSF:FileObserver.DELETE OR FileObserver.DELETE_SELF");
                    if (HwVSimApkObserver.this.needsToStopVSim()) {
                        HwVSimApkObserver.this.logd("HSF Apk is not Exist");
                        HwVSimApkObserver.this.stopVSimAsyn();
                    }
                default:
            }
        }
    }

    private class SkytoneFileObserver extends FileObserver {
        public SkytoneFileObserver() {
            super(HwVSimApkObserver.SKYTONE_DIR_PATH);
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 512:
                case 1024:
                    HwVSimApkObserver.this.logd("SKYTONE:FileObserver.DELETE OR FileObserver.DELETE_SELF");
                    if (HwVSimApkObserver.this.needsToStopVSim()) {
                        HwVSimApkObserver.this.logd("SkyTone Apk is not Exist");
                        HwVSimApkObserver.this.stopVSimAsyn();
                    }
                default:
            }
        }
    }

    private static class VsimDisableThread extends Thread {
        public void run() {
            HwVSimController.getInstance().disableVSim();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.vsim.HwVSimApkObserver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.vsim.HwVSimApkObserver.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.vsim.HwVSimApkObserver.<clinit>():void");
    }

    public HwVSimApkObserver() {
        this.isRequestDisable = false;
        this.mSkytoneFilPath = null;
        this.mSkytoneApkObserver = new SkytoneFileObserver();
        this.mHSFApkObserver = new HSFFileObserver();
        this.mSkytoneFilPath = getCustSkyToneFilePath();
        if (this.mSkytoneFilPath != null) {
            this.mCustSkytoneApkObserver = new CustSkytoneFileObserver(this.mSkytoneFilPath);
        }
    }

    public void startWatching() {
        this.mSkytoneApkObserver.startWatching();
        this.mHSFApkObserver.startWatching();
        if (this.mCustSkytoneApkObserver != null) {
            this.mCustSkytoneApkObserver.startWatching();
        }
    }

    public void stopWatching() {
        this.mSkytoneApkObserver.stopWatching();
        this.mHSFApkObserver.stopWatching();
        if (this.mCustSkytoneApkObserver != null) {
            this.mCustSkytoneApkObserver.stopWatching();
        }
    }

    private boolean isApkExist() {
        if (new File(SKYTONE_APK_PATH).exists() || new File(HSF_APK_PATH).exists()) {
            return true;
        }
        return this.mSkytoneFilPath != null ? new File(this.mSkytoneFilPath).exists() : false;
    }

    private boolean needsToStopVSim() {
        return !isApkExist() ? HwVSimUtils.isVSimOn() : false;
    }

    private void stopVSimAsyn() {
        synchronized (this) {
            if (!this.isRequestDisable) {
                new VsimDisableThread().start();
                this.isRequestDisable = true;
            }
        }
    }

    private String getCustSkyToneFilePath() {
        List allAPKList = null;
        try {
            allAPKList = HwCfgFilePolicy.getCfgFileList(APKINSTALL_FILE_PATH, 0);
        } catch (NoClassDefFoundError e) {
            logd("HwCfgFilePolicy NoClassDefFoundError");
        }
        if (allAPKList != null && allAPKList.size() > 0) {
            HashSet<String> privInstallSet = new HashSet();
            HashSet<String> installList = getMultiAPKInstallList(allAPKList, privInstallSet);
            if (installList.size() > 0) {
                for (String installPath : installList) {
                    if (installPath != null && installPath.contains(CUSTSKYTONE_DIR_PATH)) {
                        logd("installPath : " + installPath);
                        return installPath;
                    }
                }
            }
            if (privInstallSet.size() > 0) {
                for (String privInstallPath : privInstallSet) {
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
        HashSet<String> allNonPriv = new HashSet();
        for (File list : lists) {
            logd("getMultiAPKInstallList  list : " + list);
            for (String file : getAPKInstallList(list, privInstallSet)) {
                allNonPriv.add(file);
            }
        }
        return allNonPriv;
    }

    private HashSet<String> getAPKInstallList(File scanApk, HashSet<String> privInstallSet) {
        IOException e;
        Throwable th;
        HashSet<String> installSet = new HashSet();
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scanApk), "UTF-8"));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] strSplit = line.trim().split(",");
                    String packagePath = getCustPackagePath(strSplit[0]);
                    if (2 == strSplit.length && isPackageFilename(strSplit[0].trim()) && strSplit[1].trim().equalsIgnoreCase("priv") && privInstallSet != null) {
                        if (packagePath != null) {
                            privInstallSet.add(packagePath.trim());
                        }
                    } else if (isPackageFilename(strSplit[0].trim()) && packagePath != null) {
                        installSet.add(packagePath.trim());
                    }
                } catch (FileNotFoundException e2) {
                    bufferedReader = reader;
                } catch (IOException e3) {
                    e = e3;
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
        } catch (FileNotFoundException e5) {
            try {
                logd("FileNotFound No such file or directory :" + scanApk.getPath());
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e42) {
                        e42.printStackTrace();
                    }
                }
                return installSet;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e422) {
                        e422.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            e.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e4222) {
                    e4222.printStackTrace();
                }
            }
            return installSet;
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
        return name != null ? name.endsWith(".apk") : false;
    }

    private void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }
}
