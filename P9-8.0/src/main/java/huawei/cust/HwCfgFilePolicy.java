package huawei.cust;

import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.util.Log;
import android.util.LogException;
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
    private static String[] MEDIA_DIRS = ((String[]) CFG_DIRS.clone());
    public static final int PC = 2;
    private static String TAG = "CfgFilePolicy";
    private static final int TXTSECTION = 2;
    private static final String[] VERSION_MARK = new String[]{"global_cfg_version", "emui_cfg_version", "pc_cfg_version", " ", "carrier_cfg_version"};
    private static HashMap<String, String> mCfgVersions = new HashMap();

    static {
        String policy = System.getenv("CUST_POLICY_DIRS");
        if (policy == null || policy.length() == 0) {
            Log.e(TAG, "****ERROR: env CUST_POLICY_DIRS not set, use default");
            policy = "/system/emui:/system/global:/system/etc:/oem:/data/cust:/cust_spec";
        }
        CFG_DIRS = policy.split(SettingsStringUtil.DELIMITER);
        String CFG_SUFFIX = "/etc";
        int i = 0;
        while (i < MEDIA_DIRS.length) {
            if (MEDIA_DIRS[i].endsWith(CFG_SUFFIX) && (MEDIA_DIRS[i].equals(CFG_SUFFIX) ^ 1) != 0) {
                MEDIA_DIRS[i] = MEDIA_DIRS[i].replace(CFG_SUFFIX, LogException.NO_VALUE);
            }
            i++;
        }
    }

    public static ArrayList<File> getCfgFileList(String fileName, int type) throws NoClassDefFoundError {
        ArrayList<File> res = new ArrayList();
        if (fileName == null || fileName.length() == 0) {
            Log.e(TAG, "Error: file = [" + fileName + "]");
            return res;
        }
        String[] dirs = type == 1 ? MEDIA_DIRS : CFG_DIRS;
        for (String file : dirs) {
            File file2 = new File(file, fileName);
            if (file2.exists()) {
                res.add(file2);
            }
        }
        return res;
    }

    public static File getCfgFile(String fileName, int type) throws NoClassDefFoundError {
        String[] dirs = type == 1 ? MEDIA_DIRS : CFG_DIRS;
        for (int i = dirs.length - 1; i >= 0; i--) {
            File file = new File(dirs[i], fileName);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static String[] getCfgPolicyDir(int type) throws NoClassDefFoundError {
        if (type == 1) {
            return (String[]) MEDIA_DIRS.clone();
        }
        return (String[]) CFG_DIRS.clone();
    }

    public static String getCfgVersion(int cfgType) throws NoClassDefFoundError {
        switch (cfgType) {
            case 0:
            case 1:
            case 2:
            case 4:
                if (!mCfgVersions.containsKey(VERSION_MARK[cfgType])) {
                    initFileVersions(getCfgFileList("version.txt", 0));
                }
                return (String) mCfgVersions.get(VERSION_MARK[cfgType]);
            case 3:
                return SystemProperties.get("ro.product.BaseVersion", null);
            case 5:
                String[] mccInfo = getDownloadCfgFile("/cloud/mcc", "cloud/mcc/version.txt");
                return mccInfo == null ? null : mccInfo[1];
            case 6:
                String[] dplmnInfo = getDownloadCfgFile("/cloud/dplmn", "cloud/dplmn/version.txt");
                return dplmnInfo == null ? null : dplmnInfo[1];
            case 7:
                String[] apnInfo = getDownloadCfgFile("/cloud/apn", "cloud/apn/version.txt");
                return apnInfo == null ? null : apnInfo[1];
            default:
                return null;
        }
    }

    private static void initFileVersions(ArrayList<File> cfgFileList) {
        for (File file : cfgFileList) {
            String[] versions = getVersionsFromFile(file);
            if (versions != null) {
                String oldversion = (String) mCfgVersions.get(versions[0]);
                if (oldversion == null || oldversion.compareTo(versions[1]) < 0) {
                    mCfgVersions.put(versions[0], versions[1]);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0040  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String[] getVersionsFromFile(File file) {
        Throwable th;
        Scanner sc = null;
        try {
            String[] versions;
            Scanner sc2 = new Scanner(file, "UTF-8");
            do {
                try {
                    if (sc2.hasNextLine()) {
                        versions = sc2.nextLine().split("=");
                    } else {
                        if (sc2 != null) {
                            sc2.close();
                        }
                        Log.e(TAG, "version file format is wrong.");
                        return null;
                    }
                } catch (FileNotFoundException e) {
                    sc = sc2;
                    Log.e(TAG, "version file is not found.");
                    if (sc != null) {
                        sc.close();
                    }
                    return null;
                } catch (NullPointerException e2) {
                    sc = sc2;
                    try {
                        Log.e(TAG, "version file format is wrong.");
                        if (sc != null) {
                            sc.close();
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (sc != null) {
                            sc.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    sc = sc2;
                    if (sc != null) {
                    }
                    throw th;
                }
            } while (2 != versions.length);
            if (sc2 != null) {
                sc2.close();
            }
            return versions;
        } catch (FileNotFoundException e3) {
            Log.e(TAG, "version file is not found.");
            if (sc != null) {
            }
            return null;
        } catch (NullPointerException e4) {
            Log.e(TAG, "version file format is wrong.");
            if (sc != null) {
            }
            return null;
        }
    }

    private static String[] getFileInfo(String baseDir, String verDir, String filePath) {
        if (!new File(baseDir, filePath).exists()) {
            return null;
        }
        String[] info = new String[]{new File(baseDir, filePath).getPath(), LogException.NO_VALUE};
        String[] vers = getVersionsFromFile(new File(baseDir, verDir + "/version.txt"));
        if (vers != null) {
            info[1] = vers[1];
        }
        return info;
    }

    public static String[] getDownloadCfgFile(String verDir, String filePath) throws NoClassDefFoundError {
        int i = 0;
        String[] cotaInfo = getFileInfo("/data/cota/para/", verDir, filePath);
        String[] cfgPolicyDir = getCfgPolicyDir(0);
        int length = cfgPolicyDir.length;
        while (i < length) {
            String[] info = getFileInfo(cfgPolicyDir[i], verDir, filePath);
            if (info != null && (cotaInfo == null || info[1].compareTo(cotaInfo[1]) > 0)) {
                cotaInfo = info;
            }
            i++;
        }
        return cotaInfo;
    }
}
