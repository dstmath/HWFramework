package huawei.cust;

import android.common.HwFrameworkFactory;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HwCfgFilePolicy {
    public static final int BASE = 3;
    public static final int CLOUD_APN = 7;
    public static final int CLOUD_DPLMN = 6;
    public static final int CLOUD_MCC = 5;
    private static final int COMPATIBLE_VERSION = 2;
    private static final String COMPATIBLE_VERSION_TAG = "compatibleVersion";
    private static final String COTAINFO_DIR = "/data/cota/para/";
    public static final int CUST = 4;
    public static final int CUST_TYPE_CONFIG = 0;
    public static final int CUST_TYPE_MEDIA = 1;
    public static final int DEFAULT_SLOT = -2;
    private static final int DOT_VERSION_ITEM_MAX_LENGTH = 3;
    private static final int DOT_VERSION_MAX_NUM = 4;
    public static final int EMUI = 1;
    public static final int GLOBAL = 0;
    public static final String HW_ACTION_CARRIER_CONFIG_CHANGED = "com.huawei.action.CARRIER_CONFIG_CHANGED";
    public static final String HW_CARRIER_CONFIG_CHANGE_STATE = "state";
    public static final String HW_CARRIER_CONFIG_OPKEY = "opkey";
    public static final String HW_CARRIER_CONFIG_SLOT = "slot";
    public static final int HW_CONFIG_STATE_PARA_UPDATE = 3;
    public static final int HW_CONFIG_STATE_SIM_ABSENT = 2;
    public static final int HW_CONFIG_STATE_SIM_LOADED = 1;
    public static final int PC = 2;
    private static final int SUBTYPE = 4;
    private static final String SUBTYPE_TAG = "subtype";
    private static final String TAG = "CfgFilePolicy";
    private static final int TXTSECTION = 2;
    private static final int TYPE = 3;
    private static final String TYPE_TAG = "type";
    private static final int VERSION = 1;
    private static final String[] VERSION_MARKS = {"global_cfg_version", "emui_cfg_version", "pc_cfg_version", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, "carrier_cfg_version"};
    private static final String VERSION_TAG = "version";
    private static IHwCarrierConfigPolicy sCarrierConfigPolicy = HwFrameworkFactory.getHwCarrierConfigPolicy();
    private static String[] sCfgDirs;
    private static HashMap<String, String> sCfgVersions = new HashMap<>();
    private static int sCotaFlag = 0;
    private static String[] sMediaDirs;

    static {
        String policy = System.getenv("CUST_POLICY_DIRS");
        if (policy == null || policy.length() == 0) {
            Log.e(TAG, "**** ERROR: env CUST_POLICY_DIRS not set, use default");
            policy = "/system/emui:/system/global:/system/etc:/oem:/data/cust:/cust_spec";
        }
        refreshCustDirPolicy(policy);
    }

    public static ArrayList<File> getCfgFileList(String fileName, int type) throws NoClassDefFoundError {
        return getCfgFileListCommon(fileName, type, -2);
    }

    public static ArrayList<File> getCfgFileList(String fileName, int type, int slotId) throws NoClassDefFoundError {
        return getCfgFileListCommon(fileName, type, slotId);
    }

    private static ArrayList<File> getCfgFileListCommon(String fileName, int type, int slotId) throws NoClassDefFoundError {
        ArrayList<File> res = new ArrayList<>();
        if (fileName == null || fileName.length() == 0) {
            Log.e(TAG, "Error: file = [" + fileName + "]");
            return res;
        }
        for (String dir : getCfgPolicyDir(type, slotId)) {
            File file = new File(dir, fileName);
            if (file.exists()) {
                res.add(file);
            }
        }
        return res;
    }

    public static File getCfgFile(String fileName, int type) throws NoClassDefFoundError {
        return getCfgFileCommon(fileName, type, -2);
    }

    public static File getCfgFile(String fileName, int type, int slotId) throws NoClassDefFoundError {
        return getCfgFileCommon(fileName, type, slotId);
    }

    private static File getCfgFileCommon(String fileName, int type, int slotId) throws NoClassDefFoundError {
        String[] dirs = getCfgPolicyDir(type, slotId);
        for (int i = dirs.length - 1; i >= 0; i--) {
            File file = new File(dirs[i], fileName);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static String[] getCfgPolicyDir(int type) throws NoClassDefFoundError {
        return getCfgPolicyDirCommon(type, -2);
    }

    public static String[] getCfgPolicyDir(int type, int slotId) throws NoClassDefFoundError {
        return getCfgPolicyDirCommon(type, slotId);
    }

    private static String[] getCfgPolicyDirCommon(int type, int slotId) throws NoClassDefFoundError {
        String[] dirs;
        if (sCotaFlag != 1) {
            File custPolicyDirsFile = new File("/data/cota/cota_cfg/cust_policy_dirs.cfg");
            if (custPolicyDirsFile.exists()) {
                refreshCustDirPolicy(getCustPolicyDirs(custPolicyDirsFile));
            }
        }
        if (type == 1) {
            dirs = (String[]) sMediaDirs.clone();
        } else {
            dirs = (String[]) sCfgDirs.clone();
        }
        if (slotId != -2) {
            return parseCarrierPath(dirs, getOpKey(slotId));
        }
        try {
            return parseCarrierPath(dirs, getOpKey());
        } catch (Exception e) {
            Log.e(TAG, "parseCarrierPath fail.");
            return dirs;
        }
    }

    public static String getCfgVersion(int cfgType) throws NoClassDefFoundError {
        String version = null;
        switch (cfgType) {
            case 0:
            case 1:
            case 2:
            case 4:
                if (!sCfgVersions.containsKey(VERSION_MARKS[cfgType])) {
                    initFileVersions(getCfgFileList("version.txt", 0));
                }
                return sCfgVersions.get(VERSION_MARKS[cfgType]);
            case 3:
                return SystemProperties.get("ro.product.BaseVersion", null);
            case 5:
                String[] mccInfos = getDownloadCfgFile("/cloud/mcc", "cloud/mcc/version.txt");
                if (mccInfos != null) {
                    version = mccInfos[1];
                }
                return version;
            case 6:
                String[] dplmnInfos = getDownloadCfgFile("/cloud/dplmn", "cloud/dplmn/version.txt");
                if (dplmnInfos != null) {
                    version = dplmnInfos[1];
                }
                return version;
            case 7:
                String[] apnInfos = getDownloadCfgFile("/cloud/apn", "cloud/apn/version.txt");
                if (apnInfos != null) {
                    version = apnInfos[1];
                }
                return version;
            default:
                return null;
        }
    }

    private static void initFileVersions(ArrayList<File> cfgFileList) {
        String oldversion;
        Iterator<File> it = cfgFileList.iterator();
        while (it.hasNext()) {
            String[] versions = getVersionsFromFile(it.next());
            if (versions != null && ((oldversion = sCfgVersions.get(versions[0])) == null || oldversion.compareTo(versions[1]) < 0)) {
                sCfgVersions.put(versions[0], versions[1]);
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0011: APUT  
      (r1v1 'versionInfos' java.lang.String[] A[D('versionInfos' java.lang.String[])])
      (0 ??[int, short, byte, char])
      ("version")
     */
    private static String[] getVersionsFromFile(File file) {
        HashMap<String, String> fileInfos = readVersionFile(file);
        String[] versionInfos = new String[2];
        if (!fileInfos.containsKey("version")) {
            return null;
        }
        versionInfos[0] = "version";
        versionInfos[1] = fileInfos.get("version");
        return versionInfos;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004d, code lost:
        if (0 == 0) goto L_0x0053;
     */
    private static HashMap<String, String> readVersionFile(File file) {
        Scanner scanner = null;
        HashMap<String, String> fileInfos = new HashMap<>();
        try {
            scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                String[] contents = scanner.nextLine().split("=");
                if (contents.length == 2) {
                    if (!"version".equals(contents[0]) || isVersionFourSegments(contents[1])) {
                        fileInfos.put(contents[0], contents[1]);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "version file not found.");
        } catch (Throwable th) {
            if (0 != 0) {
                scanner.close();
            }
            throw th;
        }
        scanner.close();
        return fileInfos;
    }

    private static boolean isVersionFourSegments(String version) {
        String[] versionInfos = version.split("\\.");
        if (versionInfos.length != 4 || !isNumFormat(versionInfos)) {
            return false;
        }
        return true;
    }

    private static boolean isNumFormat(String[] version) {
        Pattern pattern = Pattern.compile("[0-9]{1,3}$");
        for (String ver : version) {
            if (!pattern.matcher(ver).matches()) {
                return false;
            }
        }
        return true;
    }

    private static String[] getFileInfo(String baseDir, String verDir, String filePath) {
        File cfgPath = new File(baseDir, filePath);
        if (!cfgPath.exists()) {
            return null;
        }
        String[] infos = {cfgPath.getPath(), "", "", "", ""};
        HashMap<String, String> versionInfos = readVersionFile(new File(baseDir, verDir + "/version.txt"));
        if (versionInfos.containsKey("version")) {
            infos[1] = versionInfos.get("version");
        }
        if (versionInfos.containsKey(COMPATIBLE_VERSION_TAG)) {
            infos[2] = versionInfos.get(COMPATIBLE_VERSION_TAG);
        }
        if (versionInfos.containsKey("type")) {
            infos[3] = versionInfos.get("type");
        }
        if (versionInfos.containsKey(SUBTYPE_TAG)) {
            infos[4] = versionInfos.get(SUBTYPE_TAG);
        }
        return infos;
    }

    public static String[] getDownloadCfgFile(String verDir, String filePath) throws NoClassDefFoundError {
        String[] cotaInfos = getFileInfo(COTAINFO_DIR, verDir, filePath);
        String[] cotaInfos2 = cotaInfos;
        for (String dir : getCfgPolicyDir(0)) {
            String[] presetInfos = getFileInfo(dir, verDir, filePath);
            if (isPresetNewerVersionInfo(presetInfos, cotaInfos2)) {
                cotaInfos2 = presetInfos;
            }
        }
        String[] newerInfos = new String[2];
        if (cotaInfos2 == null) {
            return null;
        }
        System.arraycopy(cotaInfos2, 0, newerInfos, 0, 2);
        return newerInfos;
    }

    private static boolean isPresetNewerVersionInfo(String[] presetInfo, String[] cotaInfos) {
        if (presetInfo == null) {
            return false;
        }
        if (cotaInfos == null || !presetInfo[2].equals(cotaInfos[2]) || !presetInfo[3].equals(cotaInfos[3]) || !presetInfo[4].equals(cotaInfos[4]) || "".equals(presetInfo[1]) || "".equals(cotaInfos[1])) {
            return true;
        }
        try {
            if (Long.parseLong(changeVersionToLongStr(presetInfo[1])) > Long.parseLong(changeVersionToLongStr(cotaInfos[1]))) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            Log.e(TAG, "version file format is not number type.");
            return true;
        }
    }

    private static String changeVersionToLongStr(String dotStr) {
        StringBuilder sb = new StringBuilder();
        String[] dots = dotStr.split("\\.");
        int i = 0;
        while (i < 4 && i < dots.length) {
            if (i == 0 || dots[i].length() >= 3) {
                sb.append(dots[i]);
            } else {
                sb.append(getLastVersionItem("000" + dots[i]));
            }
            i++;
        }
        return sb.toString();
    }

    private static String getLastVersionItem(String str) {
        return str.substring(str.length() - 3);
    }

    public static String getOpKey() {
        IHwCarrierConfigPolicy iHwCarrierConfigPolicy = sCarrierConfigPolicy;
        if (iHwCarrierConfigPolicy != null) {
            return iHwCarrierConfigPolicy.getOpKey();
        }
        Log.e(TAG, "Error: sCarrierConfigPolicy is null");
        return null;
    }

    public static String getOpKey(int slotId) {
        IHwCarrierConfigPolicy iHwCarrierConfigPolicy = sCarrierConfigPolicy;
        if (iHwCarrierConfigPolicy != null) {
            return iHwCarrierConfigPolicy.getOpKey(slotId);
        }
        Log.e(TAG, "Error: sCarrierConfigPolicy is null");
        return null;
    }

    public static <T> T getValue(String key, Class<T> clazz) {
        IHwCarrierConfigPolicy iHwCarrierConfigPolicy = sCarrierConfigPolicy;
        if (iHwCarrierConfigPolicy != null) {
            return (T) iHwCarrierConfigPolicy.getValue(key, clazz);
        }
        Log.e(TAG, "Error: sCarrierConfigPolicy is null");
        return null;
    }

    public static <T> T getValue(String key, int slotId, Class<T> clazz) {
        IHwCarrierConfigPolicy iHwCarrierConfigPolicy = sCarrierConfigPolicy;
        if (iHwCarrierConfigPolicy != null) {
            return (T) iHwCarrierConfigPolicy.getValue(key, slotId, clazz);
        }
        Log.e(TAG, "Error: sCarrierConfigPolicy is null");
        return null;
    }

    public static Map getFileConfig(String fileName) {
        IHwCarrierConfigPolicy iHwCarrierConfigPolicy = sCarrierConfigPolicy;
        if (iHwCarrierConfigPolicy != null) {
            return iHwCarrierConfigPolicy.getFileConfig(fileName);
        }
        Log.e(TAG, "Error: sCarrierConfigPolicy is null");
        return null;
    }

    public static Map getFileConfig(String fileName, int slotId) {
        IHwCarrierConfigPolicy iHwCarrierConfigPolicy = sCarrierConfigPolicy;
        if (iHwCarrierConfigPolicy != null) {
            return iHwCarrierConfigPolicy.getFileConfig(fileName, slotId);
        }
        Log.e(TAG, "Error: sCarrierConfigPolicy is null");
        return null;
    }

    private static String[] parseCarrierPath(String[] dirs, String opKey) {
        if (opKey == null) {
            return (String[]) dirs.clone();
        }
        ArrayList<String> paths = new ArrayList<>();
        for (String dir : dirs) {
            paths.add(dir);
            if (new File(dir, "carrier").exists()) {
                paths.add(dir + "/carrier/" + opKey);
            }
        }
        return (String[]) paths.toArray(new String[0]);
    }

    private static void refreshCustDirPolicy(String policy) {
        if (!TextUtils.isEmpty(policy)) {
            sCfgDirs = policy.split(SettingsStringUtil.DELIMITER);
            sMediaDirs = (String[]) sCfgDirs.clone();
            int i = 0;
            while (true) {
                String[] strArr = sMediaDirs;
                if (i < strArr.length) {
                    if (strArr[i].endsWith("/etc") && !sMediaDirs[i].equals("/etc")) {
                        String[] strArr2 = sMediaDirs;
                        strArr2[i] = strArr2[i].replace("/etc", "");
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private static String getCustPolicyDirs(File file) {
        Scanner sc = null;
        try {
            Scanner sc2 = new Scanner(file, "UTF-8");
            if (sc2.hasNextLine()) {
                sCotaFlag = 1;
                Log.d(TAG, "CustPolicyDirs file is found");
                String nextLine = sc2.nextLine();
                sc2.close();
                return nextLine;
            }
            sc2.close();
            Log.e(TAG, "CustPolicyDirs file format is wrong.");
            return null;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "CustPolicyDirs file is not found.");
            if (0 != 0) {
                sc.close();
            }
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "CustPolicyDirs file format is wrong.");
            if (0 != 0) {
                sc.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                sc.close();
            }
            throw th;
        }
    }
}
