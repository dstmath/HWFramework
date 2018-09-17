package huawei.cust;

import android.os.SystemProperties;
import android.util.Log;
import android.util.PtmLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class HwCfgFilePolicy {
    public static final int BASE = 3;
    private static String[] CFG_DIRS = null;
    public static final int CLOUD_APN = 7;
    public static final int CLOUD_DPLMN = 6;
    public static final int CLOUD_MCC = 5;
    public static final int CUST = 4;
    public static final int CUST_TYPE_CONFIG = 0;
    public static final int CUST_TYPE_MEDIA = 1;
    public static final int EMUI = 1;
    public static final int GLOBAL = 0;
    private static String[] MEDIA_DIRS = null;
    public static final int PC = 2;
    private static String TAG = null;
    private static final int TXTSECTION = 2;
    private static final String[] VERSION_MARK = null;
    private static HashMap<String, String> mCfgVersions;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.cust.HwCfgFilePolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.cust.HwCfgFilePolicy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.cust.HwCfgFilePolicy.<clinit>():void");
    }

    public static ArrayList<File> getCfgFileList(String fileName, int type) throws NoClassDefFoundError {
        ArrayList<File> res = new ArrayList();
        if (fileName == null || fileName.length() == 0) {
            Log.e(TAG, "Error: file = [" + fileName + "]");
            return res;
        }
        String[] dirs = type == EMUI ? MEDIA_DIRS : CFG_DIRS;
        for (int i = GLOBAL; i < dirs.length; i += EMUI) {
            File file = new File(dirs[i], fileName);
            if (file.exists()) {
                res.add(file);
            }
        }
        if (res.size() == 0) {
            Log.w(TAG, "No config file found for:" + fileName);
        }
        return res;
    }

    public static File getCfgFile(String fileName, int type) throws NoClassDefFoundError {
        String[] dirs = type == EMUI ? MEDIA_DIRS : CFG_DIRS;
        for (int i = dirs.length - 1; i >= 0; i--) {
            File file = new File(dirs[i], fileName);
            if (file.exists()) {
                return file;
            }
        }
        Log.w(TAG, "No config file found for:" + fileName);
        Log.w(TAG, "CFG_DIRS length : " + CFG_DIRS.length);
        return null;
    }

    public static String[] getCfgPolicyDir(int type) throws NoClassDefFoundError {
        if (type == EMUI) {
            return (String[]) MEDIA_DIRS.clone();
        }
        return (String[]) CFG_DIRS.clone();
    }

    public static String getCfgVersion(int cfgType) throws NoClassDefFoundError {
        switch (cfgType) {
            case GLOBAL /*0*/:
            case EMUI /*1*/:
            case TXTSECTION /*2*/:
            case CUST /*4*/:
                if (!mCfgVersions.containsKey(VERSION_MARK[cfgType])) {
                    initFileVersions(getCfgFileList("version.txt", GLOBAL));
                }
                return (String) mCfgVersions.get(VERSION_MARK[cfgType]);
            case BASE /*3*/:
                return SystemProperties.get("ro.product.BaseVersion", null);
            case CLOUD_MCC /*5*/:
                String[] mccInfo = getDownloadCfgFile("/cloud/mcc", "cloud/mcc/version.txt");
                return mccInfo == null ? null : mccInfo[EMUI];
            case CLOUD_DPLMN /*6*/:
                String[] dplmnInfo = getDownloadCfgFile("/cloud/dplmn", "cloud/dplmn/version.txt");
                return dplmnInfo == null ? null : dplmnInfo[EMUI];
            case CLOUD_APN /*7*/:
                String[] apnInfo = getDownloadCfgFile("/cloud/apn", "cloud/apn/version.txt");
                return apnInfo == null ? null : apnInfo[EMUI];
            default:
                return null;
        }
    }

    private static void initFileVersions(ArrayList<File> cfgFileList) {
        for (File file : cfgFileList) {
            String[] versions = getVersionsFromFile(file);
            if (versions != null) {
                String oldversion = (String) mCfgVersions.get(versions[GLOBAL]);
                if (oldversion == null || oldversion.compareTo(versions[EMUI]) < 0) {
                    mCfgVersions.put(versions[GLOBAL], versions[EMUI]);
                }
            }
        }
    }

    private static String[] getVersionsFromFile(File file) {
        Throwable th;
        Scanner scanner = null;
        try {
            String[] versions;
            Scanner sc = new Scanner(file, "UTF-8");
            do {
                try {
                    if (sc.hasNextLine()) {
                        versions = sc.nextLine().split(PtmLog.KEY_VAL_SEP);
                    } else {
                        if (sc != null) {
                            sc.close();
                        }
                        Log.e(TAG, "version file format is wrong.");
                        return null;
                    }
                } catch (FileNotFoundException e) {
                    scanner = sc;
                } catch (NullPointerException e2) {
                    scanner = sc;
                } catch (Throwable th2) {
                    th = th2;
                    scanner = sc;
                }
            } while (TXTSECTION != versions.length);
            if (sc != null) {
                sc.close();
            }
            return versions;
        } catch (FileNotFoundException e3) {
            Log.e(TAG, "version file is not found.");
            if (scanner != null) {
                scanner.close();
            }
            return null;
        } catch (NullPointerException e4) {
            try {
                Log.e(TAG, "version file format is wrong.");
                if (scanner != null) {
                    scanner.close();
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (scanner != null) {
                    scanner.close();
                }
                throw th;
            }
        }
    }

    private static String[] getFileInfo(String baseDir, String verDir, String filePath) {
        File cfgPath = new File(baseDir, filePath);
        if (!cfgPath.exists()) {
            return null;
        }
        String[] info = new String[TXTSECTION];
        info[GLOBAL] = cfgPath.getPath();
        info[EMUI] = "";
        String[] vers = getVersionsFromFile(new File(baseDir, verDir + "/version.txt"));
        if (vers != null) {
            info[EMUI] = vers[EMUI];
        }
        return info;
    }

    public static String[] getDownloadCfgFile(String verDir, String filePath) throws NoClassDefFoundError {
        int i = GLOBAL;
        String[] cotaInfo = getFileInfo("/data/cota/", verDir, filePath);
        String[] cfgPolicyDir = getCfgPolicyDir(GLOBAL);
        int length = cfgPolicyDir.length;
        while (i < length) {
            String[] info = getFileInfo(cfgPolicyDir[i], verDir, filePath);
            if (info != null && (cotaInfo == null || info[EMUI].compareTo(cotaInfo[EMUI]) > 0)) {
                cotaInfo = info;
            }
            i += EMUI;
        }
        return cotaInfo;
    }
}
