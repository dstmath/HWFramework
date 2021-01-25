package com.huawei.fileprotect;

import android.content.Context;
import android.content.SharedPreferences;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.HiEventEx;
import com.huawei.android.app.HiViewEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import huawei.android.fileprotect.HwSfpIudfXattr;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class HwSfpPolicyManager {
    private static final String API_NAME_BIGDATA_KEY = "APINAME";
    private static final String COST_TIME_NAME_BIGDATA_KEY = "COSTTIME";
    private static final String DB_PATH = "/database/";
    private static final String DEFAULT_MDFS_SECURITY_VALUE_XATTR = "s2";
    private static final String DOT = ".";
    private static final String EMPTY_STRING = "";
    public static final int ERROR_CODE_CE_DE_CONTEXT = 3;
    public static final int ERROR_CODE_FAILED = 4;
    public static final int ERROR_CODE_INVALID_PARAM = 1;
    public static final int ERROR_CODE_LABEL_HAS_BEEN_SET = 2;
    public static final int ERROR_CODE_OK = 0;
    private static final int ERROR_TYPE = -1;
    private static final String FILES_PATH = "/files/";
    private static final String FILE_LOCATION_LOCAL = "local";
    public static final int FLAG_FILE_PROTECTION_COMPLETE = 0;
    public static final int FLAG_FILE_PROTECTION_COMPLETE_UNLESS_OPEN = 1;
    private static final String FLAG_FILE_PROTECTION_COMPLETE_UNLESS_OPEN_XATTR = "b";
    private static final String FLAG_FILE_PROTECTION_COMPLETE_XATTR = "a";
    private static final boolean IS_SUPPORT_IUDF = SystemPropertiesEx.getBoolean("ro.config.support_iudf", false);
    private static final String LABEL_BIGDATA_KEY = "LABEL";
    private static final String LABEL_CODE_FORMAT = "%s_%s_%d";
    public static final String LABEL_NAME_SECURITY_LEVEL = "SecurityLevel";
    private static final String LABEL_VALUE_PATTERN = "S[0-4]";
    public static final String LABEL_VALUE_S0 = "S0";
    public static final String LABEL_VALUE_S1 = "S1";
    public static final String LABEL_VALUE_S2 = "S2";
    public static final String LABEL_VALUE_S3 = "S3";
    public static final String LABEL_VALUE_S4 = "S4";
    private static final String LOG_TAG = "HwSfpPolicyManager";
    private static final long MAX_FILE_LENGTH = 20971520;
    private static final String MDFS_FILE_LOCATION = "location";
    private static final String MDFS_PATH = "/mnt/mdfs";
    private static final int NEITHER_ECE_NOR_SECE = 1;
    private static final String PACKAGE_BIGDATA_KEY = "PACKAGE";
    private static final String PACKAGE_NAME_ID = "com.huawei.fileprotect";
    private static final String PACKAGE_NAME_ID_BIGDATA_KEY = "PNAMEID";
    private static final int READ_SIZE = 4096;
    private static final String RESULT_BIGDATA_KEY = "RESULT";
    private static final int RESULT_ERROR = -1;
    private static final int RESULT_SUCCESS = 0;
    private static final String SD_CARD_PATH = (File.separator + "storage" + File.separator + "emulated" + File.separator);
    private static final String SECURITY_VALUE_XATTR_PATTERN = "s([0124]|(3[ab]))";
    private static final String SEPARATOR_DOT_AND_SLASH = "../";
    private static final String SERVICE_BIGDATA_KEY = "SERVICE";
    private static final int SET_LABEL_BIGDATA_ID = 992740301;
    private static final String SET_LABEL_METHOD_NAME = "setLabel";
    private static final String SHARED_PREFS_PATH = "/shared_prefs/";
    private static final String SHARED_PREFS_SUFFIX = ".xml";
    private static final String SLASH = "/";
    private static final int STORAGE_TYPE_ECE = 2;
    private static final int STORAGE_TYPE_NO_POLICY = 0;
    private static final int STORAGE_TYPE_SECE = 3;
    private static final String TYPE_DB_TAG = ".db";
    private static final int TYPE_DIR = 0;
    private static final int TYPE_FILE = 1;
    private static final String VERSION = "1.0.0.0";
    private static final String VERSION_BIGDATA_KEY = "VERSION";
    private static HwSfpPolicyManager sInstance = null;
    private String eceKeyDesc;
    private HashMap<Integer, String> mEceKeyDescMap = new HashMap<>();
    private HwSfpManager mHwSfpManager = HwSfpManager.getDefault();
    private HashMap<Integer, String> mSeceKeyDescMap = new HashMap<>();
    private String seceKeyDesc;

    private HwSfpPolicyManager() {
    }

    public static HwSfpPolicyManager getDefault() {
        HwSfpPolicyManager hwSfpPolicyManager;
        synchronized (HwSfpPolicyManager.class) {
            if (sInstance == null) {
                sInstance = new HwSfpPolicyManager();
            }
            hwSfpPolicyManager = sInstance;
        }
        return hwSfpPolicyManager;
    }

    public void setEcePolicy(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException, IOException {
        int ret;
        preCheck(context, name);
        int userId = UserHandleEx.myUserId();
        if (!this.mEceKeyDescMap.containsKey(Integer.valueOf(userId))) {
            this.eceKeyDesc = this.mHwSfpManager.getKeyDesc(userId, 2);
            if (this.eceKeyDesc != null) {
                this.mEceKeyDescMap.put(Integer.valueOf(userId), this.eceKeyDesc);
            } else {
                throw new IllegalStateException("Get ece key desc failed! Something wrong with the interface.");
            }
        } else {
            this.eceKeyDesc = this.mEceKeyDescMap.get(Integer.valueOf(userId));
        }
        String path = getExactPath(context, name, false);
        if (!isFileLengthInvalid(path)) {
            File file = new File(path);
            int fileFlag = !file.isDirectory();
            if (file.isDirectory() || this.mHwSfpManager.getFbeVersion() < 3) {
                ret = HwSfpIudfXattr.setFileXattrEx(path, this.eceKeyDesc, 2, fileFlag);
            } else {
                ret = copyToTempFileAndSetPolicy(context, path, null, 2);
            }
            if (ret != 0) {
                throw new IOException("Set ECE policy failed!");
            }
            return;
        }
        throw new IllegalStateException("The file length is over 20M");
    }

    public void setSecePolicy(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException, IOException {
        int ret;
        preCheck(context, name);
        int userId = UserHandleEx.myUserId();
        if (!this.mSeceKeyDescMap.containsKey(Integer.valueOf(userId))) {
            this.seceKeyDesc = this.mHwSfpManager.getKeyDesc(userId, 3);
            if (this.seceKeyDesc != null) {
                this.mSeceKeyDescMap.put(Integer.valueOf(userId), this.seceKeyDesc);
            } else {
                throw new IllegalStateException("Get sece key desc failed! Something wrong with the interface.");
            }
        } else {
            this.seceKeyDesc = this.mSeceKeyDescMap.get(Integer.valueOf(userId));
        }
        String path = getExactPath(context, name, false);
        if (!isFileLengthInvalid(path)) {
            File file = new File(path);
            int fileFlag = !file.isDirectory();
            if (file.isDirectory() || this.mHwSfpManager.getFbeVersion() < 3) {
                ret = HwSfpIudfXattr.setFileXattrEx(path, this.seceKeyDesc, 3, fileFlag);
            } else {
                ret = copyToTempFileAndSetPolicy(context, path, null, 3);
            }
            if (ret != 0) {
                throw new IOException("Set SECE policy failed!");
            }
            return;
        }
        throw new IllegalStateException("The file length is over 20M");
    }

    public static final int getPolicyProtectType(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        preCheck(context, name);
        String path = getExactPath(context, name, true);
        int type = HwSfpIudfXattr.getFileXattrEx(path, 1 ^ new File(path).isDirectory());
        if (type < 0) {
            return -1;
        }
        return type;
    }

    public SharedPreferences createEceSharePreference(Context context, String name, int mode) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        Log.i(LOG_TAG, "createEceSharePreference name " + name);
        preCheck(context, name);
        if (this.mHwSfpManager.getFbeVersion() < 3) {
            return createSharePreferenceForLowVer(context, name, mode, 2);
        }
        File sharedDir = new File(context.getDataDir(), "shared_prefs");
        File preFile = makeFileName(sharedDir, name + SHARED_PREFS_SUFFIX);
        if (preFile == null) {
            Log.e(LOG_TAG, "createEceSharePreference create file failed.");
            return null;
        } else if (!preFile.exists()) {
            SharedPreferences sharedFile = context.getSharedPreferences(name, mode);
            if (sharedFile == null) {
                Log.e(LOG_TAG, "createEceSharePreference sharedFile is null!");
                return null;
            }
            SharedPreferences.Editor editor = sharedFile.edit();
            editor.apply();
            editor.commit();
            try {
                copyToTempFileAndSetPolicy(context, preFile.getPath(), SHARED_PREFS_SUFFIX, 2);
                return context.getSharedPreferences(name, mode);
            } catch (IOException e) {
                Log.e(LOG_TAG, "createEceSharePreference set the sece policy failed ");
                context.deleteSharedPreferences(name);
                return null;
            }
        } else {
            throw new IllegalAccessException("the file exists");
        }
    }

    private SharedPreferences createSharePreferenceForLowVer(Context context, String name, int mode, int policy) {
        Log.i(LOG_TAG, "createSharePreferenceForLowVer name " + name);
        SharedPreferences sharedFile = context.getSharedPreferences(name, mode);
        if (sharedFile == null) {
            Log.e(LOG_TAG, "createSharePreferenceForLowVer sharedFile is null!");
            return null;
        }
        SharedPreferences.Editor editor = sharedFile.edit();
        editor.apply();
        editor.commit();
        File sharedDir = new File(context.getDataDir(), "shared_prefs");
        File preFile = makeFileName(sharedDir, name + SHARED_PREFS_SUFFIX);
        if (preFile == null || !preFile.exists()) {
            Log.e(LOG_TAG, "createSharePreferenceForLowVer create file failed.");
            context.deleteSharedPreferences(name);
            return null;
        }
        int result = setPolicyInner(context, name, policy);
        if (result == 0) {
            return sharedFile;
        }
        context.deleteSharedPreferences(name);
        Log.e(LOG_TAG, "createSharePreferenceForLowVer result = " + result);
        return null;
    }

    public SharedPreferences createSeceSharePreference(Context context, String name, int mode) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        Log.i(LOG_TAG, "createSeceSharePreference name " + name);
        preCheck(context, name);
        if (this.mHwSfpManager.getFbeVersion() < 3) {
            return createSharePreferenceForLowVer(context, name, mode, 3);
        }
        File sharedDir = new File(context.getDataDir(), "shared_prefs");
        File preFile = makeFileName(sharedDir, name + SHARED_PREFS_SUFFIX);
        if (preFile == null) {
            Log.e(LOG_TAG, "createSeceSharePreference create file failed.");
            return null;
        } else if (!preFile.exists()) {
            SharedPreferences sharedFile = context.getSharedPreferences(name, mode);
            if (sharedFile == null) {
                Log.e(LOG_TAG, "createSeceSharePreference sharedFile is null!");
                return null;
            }
            SharedPreferences.Editor editor = sharedFile.edit();
            editor.apply();
            editor.commit();
            try {
                String path = preFile.getPath();
                Log.e(LOG_TAG, "createSeceSharePreference path = " + path);
                int ret = copyToTempFileAndSetPolicy(context, path, SHARED_PREFS_SUFFIX, 3);
                if (ret == 0) {
                    return context.getSharedPreferences(name, mode);
                }
                Log.e(LOG_TAG, "createSeceSharePreference set policy result = " + ret);
                context.deleteSharedPreferences(name);
                return null;
            } catch (IOException e) {
                Log.e(LOG_TAG, "createEceSharePreference set the sece policy failed ");
                context.deleteSharedPreferences(name);
                return null;
            }
        } else {
            throw new IllegalArgumentException("the file exists");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0067, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0068, code lost:
        if (r3 != null) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006e, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006f, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0072, code lost:
        throw r5;
     */
    public FileOutputStream createEceFileOutputStream(Context context, String name, int mode) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        Log.i(LOG_TAG, "createEceFileOutputStream name " + name);
        preCheck(context, name);
        File file = makeFileName(context.getFilesDir(), name);
        if (file == null) {
            Log.e(LOG_TAG, "createEceFileOutputStream make file failed!");
            return null;
        } else if (!file.exists()) {
            try {
                FileOutputStream outputStream = context.openFileOutput(name, mode);
                if (setPolicyInner(context, file.getPath(), 2) != 0) {
                    boolean isDeleted = file.delete();
                    Log.e(LOG_TAG, "createEceFileOutputStream set policy failed delete result " + isDeleted);
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    return null;
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return outputStream;
            } catch (IOException e) {
                boolean isDeleted2 = file.delete();
                Log.e(LOG_TAG, "createEceFileOutputStream exception delete result " + isDeleted2);
                return null;
            }
        } else {
            throw new IllegalArgumentException("The file exists!");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006a, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006b, code lost:
        if (r3 != null) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0071, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0072, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0075, code lost:
        throw r5;
     */
    public FileOutputStream createSeceFileOutputStream(Context context, String name, int mode) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        Log.i(LOG_TAG, "createSeceFileOutputStream name " + name);
        preCheck(context, name);
        File file = makeFileName(context.getFilesDir(), name);
        if (file == null) {
            return null;
        }
        if (!file.exists()) {
            try {
                FileOutputStream outputStream = context.openFileOutput(name, mode);
                int ret = setPolicyInner(context, file.getPath(), 3);
                if (ret != 0) {
                    boolean isDeleted = file.delete();
                    Log.e(LOG_TAG, "createSeceFileOutputStream set policy result " + ret + " delete " + isDeleted);
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    return null;
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return outputStream;
            } catch (IOException e) {
                boolean isDeleted2 = file.delete();
                Log.e(LOG_TAG, "createSeceFileOutputStream exception delete result " + isDeleted2);
                return null;
            }
        } else {
            throw new IllegalArgumentException("The file exists!");
        }
    }

    public int setLabel(Context context, String filePath, String labelName, String labelValue, int flag) {
        long startTime = System.currentTimeMillis();
        int errorCode = setLabelInner(context, filePath, labelName, labelValue, flag);
        onReport(context, SET_LABEL_METHOD_NAME, getLabelCode(labelName, labelValue, flag), errorCode, System.currentTimeMillis() - startTime);
        return errorCode;
    }

    public String getLabel(Context context, String filePath, String labelName) {
        if (!LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            Log.w(LOG_TAG, "getLabel: invalid labelName!");
            return "";
        } else if (!isAbsolutePath(filePath) || !isFilePath(filePath) || context == null) {
            Log.w(LOG_TAG, "getLabel: invalid filePath!");
            return "";
        } else {
            try {
                return getLabelValueFromValue(getLabelFromXattr(getExactPath(context, filePath, false), labelName), labelName);
            } catch (FileNotFoundException | IllegalArgumentException e) {
                Log.w(LOG_TAG, "getLabel: invalid filePath!");
                return "";
            }
        }
    }

    public int getFlag(Context context, String filePath, String labelName) {
        if (!LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            Log.w(LOG_TAG, "getFlag: invalid labelName!");
            return -1;
        } else if (!isAbsolutePath(filePath) || !isFilePath(filePath) || context == null) {
            Log.w(LOG_TAG, "getFlag: invalid filePath!");
            return -1;
        } else {
            try {
                return getFlagFromValue(getLabelFromXattr(getExactPath(context, filePath, false), labelName), labelName);
            } catch (FileNotFoundException | IllegalArgumentException e) {
                Log.w(LOG_TAG, "getFlag: invalid filePath!");
                return -1;
            }
        }
    }

    private static void preCheck(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException {
        if (context == null || name == null) {
            throw new IllegalArgumentException("Input value is null!");
        } else if (!isSupportIudf()) {
            throw new IllegalStateException("Action is not permitted, the system does not support it!");
        } else if (name.contains(SEPARATOR_DOT_AND_SLASH)) {
            throw new IllegalArgumentException("Inputed file path contains illegal components!");
        } else if (context.isDeviceProtectedStorage()) {
            throw new IllegalAccessException("Could not set policy for DE context!");
        }
    }

    private static String getExactPath(Context context, String name, boolean isSupport) throws IllegalArgumentException, FileNotFoundException {
        String path;
        int userId = UserHandleEx.myUserId();
        String fileName = name;
        boolean validPathCondition = true;
        if (new File(name).isAbsolute()) {
            context.getPackageName();
            String internDataDir = context.getDataDir().getPath();
            String internSdDir = SD_CARD_PATH + userId + File.separatorChar;
            if (!isSupport) {
                validPathCondition = name.startsWith(internDataDir);
            } else if (!name.startsWith(internDataDir) && !name.startsWith(internSdDir)) {
                validPathCondition = false;
            }
            if (validPathCondition) {
                path = name;
            } else if (name.startsWith(MDFS_PATH)) {
                path = name;
            } else {
                throw new IllegalArgumentException("Input file path does not meet the requirement of absolute path!");
            }
        } else if (!name.contains(DOT)) {
            if (name.contains(SLASH)) {
                String[] names = name.split(SLASH);
                fileName = names[names.length - 1];
            }
            path = context.getDataDir() + SHARED_PREFS_PATH + fileName + SHARED_PREFS_SUFFIX;
        } else if (name.endsWith(TYPE_DB_TAG)) {
            if (name.contains(SLASH)) {
                String[] names2 = name.split(SLASH);
                fileName = names2[names2.length - 1];
            }
            path = context.getDataDir() + DB_PATH + fileName;
        } else {
            if (name.contains(SLASH)) {
                String[] names3 = name.split(SLASH);
                fileName = names3[names3.length - 1];
            }
            path = context.getDataDir() + FILES_PATH + fileName;
        }
        if (new File(path).exists()) {
            return path;
        }
        throw new FileNotFoundException("Target file does not exist!");
    }

    public static final boolean isSupportIudf() {
        return IS_SUPPORT_IUDF;
    }

    private int setLabelInner(Context context, String filePath, String labelName, String labelValue, int flag) {
        boolean z = true;
        if (!checkParamsForSetLabel(context, filePath, labelName, labelValue, flag)) {
            return 1;
        }
        try {
            String targetPath = getExactPath(context, filePath, false);
            boolean isDeviceProtectedStorage = context.isDeviceProtectedStorage();
            if (!LABEL_VALUE_S0.equals(labelValue) && !LABEL_VALUE_S1.equals(labelValue)) {
                z = false;
            }
            if (z ^ isDeviceProtectedStorage) {
                Log.w(LOG_TAG, "setLabel: labelValue not match for CE/DE context!");
                return 3;
            } else if (!isLocalFile(targetPath)) {
                Log.w(LOG_TAG, "setLabel: the file belongs to remote file!");
                return 4;
            } else if (!canSetLabel(targetPath, labelName)) {
                Log.w(LOG_TAG, "setLabel: the security level of target path has been set!");
                return 2;
            } else if (!setPolicyWithCheck(context, targetPath, mapToPolicyType(labelName, labelValue, flag))) {
                Log.e(LOG_TAG, "setLabel: failed to set policy!");
                return 4;
            } else if (!setLabelToXattr(targetPath, labelName, labelValue, flag)) {
                return 4;
            } else {
                return 0;
            }
        } catch (FileNotFoundException | IllegalArgumentException e) {
            Log.w(LOG_TAG, "setLabel: invalid filePath!");
            return 1;
        }
    }

    private void onReport(Context context, String apiName, String label, int errorCode, long costTime) {
        Log.i(LOG_TAG, "eventId : 992740301, result code : " + errorCode);
        if (context == null) {
            Log.e(LOG_TAG, "the context is null!");
        } else {
            HiViewEx.report(new HiEventEx((int) SET_LABEL_BIGDATA_ID).putString(PACKAGE_NAME_ID_BIGDATA_KEY, PACKAGE_NAME_ID).putString(PACKAGE_BIGDATA_KEY, context.getPackageName()).putString(SERVICE_BIGDATA_KEY, LOG_TAG).putString(VERSION_BIGDATA_KEY, VERSION).putString(API_NAME_BIGDATA_KEY, apiName).putLong(COST_TIME_NAME_BIGDATA_KEY, costTime).putInt(RESULT_BIGDATA_KEY, errorCode).putString(LABEL_BIGDATA_KEY, label));
        }
    }

    private String getLabelCode(String labelName, String labelValue, int flag) {
        String labelValueCode = "";
        String labelNameCode = labelName == null ? labelValueCode : labelName;
        if (labelValue != null) {
            labelValueCode = labelValue;
        }
        return String.format(Locale.ROOT, LABEL_CODE_FORMAT, labelNameCode, labelValueCode, Integer.valueOf(flag));
    }

    private File makeFileName(File base, String name) {
        if (name.indexOf(File.separatorChar) < 0) {
            return new File(base, name);
        }
        return null;
    }

    private void copyFilePermission(String srcName, String targetName) throws IOException {
        File srcFile = new File(srcName);
        File targetFile = new File(targetName);
        if (srcFile.exists()) {
            try {
                if (!targetFile.exists()) {
                    if (!targetFile.createNewFile()) {
                        throw new IOException("create file failed!");
                    }
                }
                Files.setPosixFilePermissions(Paths.get(targetFile.getCanonicalPath(), new String[0]), Files.getPosixFilePermissions(Paths.get(srcFile.getCanonicalPath(), new String[0]), new LinkOption[0]));
            } catch (IOException e) {
                Log.e(LOG_TAG, "copyFilePermission copy permission exception!");
                throw new IOException("copy file permission failed!");
            }
        } else {
            Log.e(LOG_TAG, "copyFilePermission " + targetName + " not exist!");
            throw new IOException("target file not exist.");
        }
    }

    private int setPolicyInner(Context context, String path, int policyType) {
        String keyDesc = this.mHwSfpManager.getKeyDesc(UserHandleEx.myUserId(), policyType);
        if (keyDesc == null || keyDesc.isEmpty()) {
            Log.e(LOG_TAG, "setPolicyInner key desc is null!");
            return -1;
        }
        int ret = HwSfpIudfXattr.setFileXattrEx(path, keyDesc, policyType, 1);
        Log.i(LOG_TAG, "setPolicyInner policy = " + policyType + " result = " + ret);
        return ret;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00be, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c4, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c5, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c9, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00cd, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d3, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00d4, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d8, code lost:
        throw r0;
     */
    private int copyToTempFileAndSetPolicy(Context context, String fileName, String suffix, int policy) throws IOException {
        if (isMutualExclusivePolicy(context, fileName, policy)) {
            Log.e(LOG_TAG, "copyToTempFileAndSetPolicy: isMutualExclusivePolicy failed");
            return -1;
        }
        File oldFile = new File(fileName);
        if (oldFile.exists()) {
            String tempFileName = fileName + ".bak";
            File tempFile = new File(tempFileName);
            if (!tempFile.createNewFile()) {
                Log.e(LOG_TAG, "copyToTempFileAndSetPolicy create file failed!");
                return -1;
            }
            int result = setPolicyInner(context, tempFileName, policy);
            if (result != 0) {
                Log.e(LOG_TAG, "copyToTempFileAndSetPolicy setPolicyInner faild isDeleted = " + tempFile.delete());
                return result;
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(tempFile);
                FileInputStream inputStream = new FileInputStream(oldFile);
                copyFilePermission(fileName, tempFileName);
                byte[] bytes = new byte[4096];
                for (int len = inputStream.read(bytes); len > 0; len = inputStream.read(bytes)) {
                    outputStream.write(bytes, 0, len);
                }
                inputStream.close();
                outputStream.close();
                oldFile.delete();
                boolean isOpSuccess = new File(tempFileName).renameTo(oldFile);
                inputStream.close();
                outputStream.close();
                Log.i(LOG_TAG, "copy and setPolicy isOpSuccess " + isOpSuccess);
                return result;
            } catch (IOException e) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                throw new IOException("copy to temp and set policy failed!");
            }
        } else {
            Log.e(LOG_TAG, "copyToTempFileAndSetPolicy " + fileName + " not exist.");
            throw new IOException(fileName + " is not exists!");
        }
    }

    private boolean isFileLengthInvalid(String path) {
        if (this.mHwSfpManager.getFbeVersion() < 3) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (!file.isFile() || file.length() <= MAX_FILE_LENGTH) {
            return false;
        }
        return true;
    }

    private boolean canSetLabelToXattr(String filePath, String xattrKey) {
        try {
            return filePath.startsWith(MDFS_PATH) && DEFAULT_MDFS_SECURITY_VALUE_XATTR.equals(new String(Os.getxattr(filePath, xattrKey), StandardCharsets.UTF_8));
        } catch (ErrnoException e) {
            if (e.errno == OsConstants.ENODATA) {
                return true;
            }
            Log.e(LOG_TAG, "canSetLabelToXattr: Os error!");
            return false;
        }
    }

    private String getLabelFromXattr(String filePath, String labelName) {
        try {
            return new String(Os.getxattr(filePath, transLabelNameToXattrKey(labelName)), StandardCharsets.UTF_8);
        } catch (ErrnoException e) {
            if (e.errno == OsConstants.ENODATA) {
                Log.i(LOG_TAG, "getLabelFromXattr: No data for label!");
                return "";
            }
            Log.e(LOG_TAG, "getLabelFromXattr: Os error!");
            return "";
        }
    }

    private boolean setLabelToXattr(String filePath, String labelName, String labelValue, int flag) {
        if (LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            try {
                Os.setxattr(filePath, transLabelNameToXattrKey(labelName), transSecurityLevelToXattrValue(labelValue, flag), 0);
                return true;
            } catch (ErrnoException e) {
                Log.e(LOG_TAG, "setLabelToXattr: Os error!");
                return false;
            }
        } else {
            Log.w(LOG_TAG, "setLabelToXattr: invalid labelName!");
            return false;
        }
    }

    private String transLabelNameToXattrKey(String labelName) {
        return String.format(Locale.ROOT, "user.%s", labelName.toLowerCase(Locale.ROOT));
    }

    private byte[] transSecurityLevelToXattrValue(String labelValue, int flag) {
        String value = labelValue;
        if (LABEL_VALUE_S3.equals(labelValue)) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            sb.append(flag == 1 ? FLAG_FILE_PROTECTION_COMPLETE_UNLESS_OPEN_XATTR : FLAG_FILE_PROTECTION_COMPLETE_XATTR);
            value = sb.toString();
        }
        return value.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
    }

    private boolean checkXattrValue(String value, String labelName) {
        if (!TextUtils.isEmpty(value) && LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            return Pattern.matches(SECURITY_VALUE_XATTR_PATTERN, value);
        }
        return false;
    }

    private String getLabelValueFromValue(String value, String labelName) {
        if (checkXattrValue(value, labelName) && LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            return value.toUpperCase(Locale.ROOT).substring(0, LABEL_VALUE_S0.length());
        }
        return "";
    }

    private int getFlagFromValue(String value, String labelName) {
        if (!checkXattrValue(value, labelName) || !LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            return -1;
        }
        if (value.endsWith(FLAG_FILE_PROTECTION_COMPLETE_UNLESS_OPEN_XATTR)) {
            return 1;
        }
        return 0;
    }

    private boolean canSetLabel(String filePath, String labelName) {
        return canSetLabelToXattr(filePath, transLabelNameToXattrKey(labelName));
    }

    private boolean checkLabelValueAndFlag(String labelValue, int flag) {
        if (TextUtils.isEmpty(labelValue) || !Pattern.matches(LABEL_VALUE_PATTERN, labelValue)) {
            Log.w(LOG_TAG, "checkLabelValueAndFlag: invalid labelValue!");
            return false;
        } else if (flag != 0 && flag != 1) {
            Log.w(LOG_TAG, "checkLabelValueAndFlag: invalid flag!");
            return false;
        } else if (flag != 1 || LABEL_VALUE_S3.equals(labelValue)) {
            return true;
        } else {
            Log.w(LOG_TAG, "checkLabelValueAndFlag: flag 0x0001 only available for S3!");
            return false;
        }
    }

    private boolean isLocalFile(String filePath) {
        if (!filePath.startsWith(MDFS_PATH)) {
            return true;
        }
        try {
            return FILE_LOCATION_LOCAL.equals(new String(Os.getxattr(filePath, MDFS_FILE_LOCATION), StandardCharsets.UTF_8));
        } catch (ErrnoException e) {
            Log.e(LOG_TAG, "isLocalFile: get file location failed!");
            return false;
        }
    }

    private boolean checkParamsForSetLabel(Context context, String filePath, String labelName, String labelValue, int flag) {
        if (!LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            Log.w(LOG_TAG, "checkParamsForSetLabel: invalid labelName!");
            return false;
        } else if (!isAbsolutePath(filePath) || !isFilePath(filePath)) {
            Log.w(LOG_TAG, "checkParamsForSetLabel: invalid filePath!");
            return false;
        } else if (context == null) {
            Log.w(LOG_TAG, "checkParamsForSetLabel: context is null!");
            return false;
        } else if (!checkLabelValueAndFlag(labelValue, flag)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isAbsolutePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            Log.w(LOG_TAG, "isAbsolutePath: filePath is empty!");
            return false;
        } else if (new File(filePath).isAbsolute()) {
            return true;
        } else {
            Log.i(LOG_TAG, "isAbsolutePath: filePath is not absolute path!");
            return false;
        }
    }

    private boolean isFilePath(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            return new File(filePath).isFile();
        }
        Log.w(LOG_TAG, "isFilePath: filePath is empty!");
        return false;
    }

    private int mapToPolicyType(String labelName, String labelValue, int flag) {
        if (!LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            return -1;
        }
        if (flag == 1) {
            return 3;
        }
        if (LABEL_VALUE_S3.equals(labelValue) || LABEL_VALUE_S4.equals(labelValue)) {
            return 2;
        }
        return 0;
    }

    private boolean setPolicyWithCheck(Context context, String filePath, int storageType) {
        if (storageType == -1) {
            Log.e(LOG_TAG, "setPolicyWithCheck: storage type error!");
            return false;
        } else if (storageType == 0 || !isSupportIudf()) {
            Log.i(LOG_TAG, "setPolicyWithCheck: no need to set policy!");
            return true;
        } else {
            int type = HwSfpIudfXattr.getFileXattrEx(filePath, 1);
            if (type == storageType) {
                Log.i(LOG_TAG, "setPolicyWithCheck: the policy of file has been set correctly!");
                return true;
            } else if (type == 1) {
                return setPolicyWithType(context, filePath, storageType);
            } else {
                Log.e(LOG_TAG, "setPolicyWithCheck: could not set correspond policy!");
                return false;
            }
        }
    }

    private boolean setPolicyWithType(Context context, String filePath, int storageType) {
        if (storageType == 2) {
            try {
                setEcePolicy(context, filePath);
                return true;
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "setPolicyWithCheck: illegal argument!");
                return false;
            } catch (IllegalStateException e2) {
                Log.e(LOG_TAG, "setPolicyWithCheck: illegal state!");
                return false;
            } catch (IllegalAccessException e3) {
                Log.e(LOG_TAG, "setPolicyWithCheck: illegal access!");
                return false;
            } catch (FileNotFoundException e4) {
                Log.e(LOG_TAG, "setPolicyWithCheck: invocation error!");
                return false;
            } catch (IOException e5) {
                Log.e(LOG_TAG, "setPolicyWithCheck: invocation failed!");
                return false;
            }
        } else if (storageType != 3) {
            return false;
        } else {
            setSecePolicy(context, filePath);
            return true;
        }
    }

    private boolean isMutualExclusivePolicy(Context context, String fileName, int policy) {
        try {
            int policyType = getPolicyProtectType(context, fileName);
            if ((policyType != 2 || policy != 3) && (policyType != 3 || policy != 2)) {
                return false;
            }
            Log.e(LOG_TAG, "isMutualExclusivePolicy: Set policy failed, file policy is already " + policyType);
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "isMutualExclusivePolicy: getPolicyType has illegal argument");
            return true;
        } catch (IllegalAccessException e2) {
            Log.e(LOG_TAG, "isMutualExclusivePolicy: getPolicyType has illegal access");
            return true;
        } catch (FileNotFoundException e3) {
            Log.e(LOG_TAG, "isMutualExclusivePolicy: getPolicyType has file error");
            return true;
        } catch (IllegalStateException e4) {
            Log.e(LOG_TAG, "isMutualExclusivePolicy: getPolicyType has illegal state");
            return true;
        }
    }
}
