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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class HwSfpPolicyManager {
    private static final String API_NAME_BIGDATA_KEY = "APINAME";
    private static final String BACKUP_SUFFIX = ".bak";
    private static final String COST_TIME_NAME_BIGDATA_KEY = "COSTTIME";
    private static final String DB_PATH = (File.separator + "database" + File.separator);
    private static final int DEFAULT_CAPACITY = 4;
    private static final String DEFAULT_MDFS_SECURITY_VALUE_XATTR = "s2";
    private static final String DOT = ".";
    private static final String EMPTY_STRING = "";
    public static final int ERROR_CODE_CE_DE_CONTEXT = 3;
    public static final int ERROR_CODE_FAILED = 4;
    public static final int ERROR_CODE_INVALID_PARAM = 1;
    public static final int ERROR_CODE_LABEL_HAS_BEEN_SET = 2;
    public static final int ERROR_CODE_OK = 0;
    private static final int ERROR_TYPE = -1;
    private static final String FILES_PATH = (File.separator + "files" + File.separator);
    private static final String FILE_LOCATION_LOCAL = "local";
    public static final int FLAG_FILE_PROTECTION_COMPLETE = 0;
    public static final int FLAG_FILE_PROTECTION_COMPLETE_UNLESS_OPEN = 1;
    private static final String FLAG_FILE_PROTECTION_COMPLETE_UNLESS_OPEN_XATTR = "b";
    private static final String FLAG_FILE_PROTECTION_COMPLETE_XATTR = "a";
    private static final boolean IS_SUPPORT_IUDF = SystemPropertiesEx.getBoolean("ro.config.support_iudf", false);
    private static final String LABEL_BIGDATA_KEY = "LABEL";
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
    private static final String SEPARATOR_DOT_AND_SLASH = (".." + File.separator);
    private static final String SERVICE_BIGDATA_KEY = "SERVICE";
    private static final String SET_ECE_OR_SECE_METHOD_NAME = "setEceOrSece";
    private static final int SET_LABEL_BIGDATA_ID = 992740301;
    private static final String SET_LABEL_METHOD_NAME = "setLabel";
    private static final String SHARED_PREFS = "shared_prefs";
    private static final String SHARED_PREFS_SUFFIX = ".xml";
    private static final String SLASH = File.separator;
    private static final int STORAGE_TYPE_ECE = 2;
    private static final int STORAGE_TYPE_NO_POLICY = 0;
    private static final int STORAGE_TYPE_SECE = 3;
    private static final String TYPE_DB_TAG = ".db";
    private static final int TYPE_DIR = 0;
    private static final int TYPE_FILE = 1;
    private static final int TYPE_VALID_START = 0;
    private static final String VERSION = "1.0.0.0";
    private static final String VERSION_BIGDATA_KEY = "VERSION";
    private static final int XATTR_CREATE_FLAG = 0;
    private static HwSfpPolicyManager sInstance = null;
    private String mEceKeyDesc;
    private Map<Integer, String> mEceKeyDescMap = new ConcurrentHashMap(4);
    private HwSfpManager mHwSfpManager = HwSfpManager.getDefault();
    private String mSeceKeyDesc;
    private Map<Integer, String> mSeceKeyDescMap = new ConcurrentHashMap(4);

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
        preCheck(context, name);
        int userId = UserHandleEx.myUserId();
        if (!this.mEceKeyDescMap.containsKey(Integer.valueOf(userId))) {
            this.mEceKeyDesc = this.mHwSfpManager.getKeyDesc(userId, 2);
            if (this.mEceKeyDesc != null) {
                this.mEceKeyDescMap.put(Integer.valueOf(userId), this.mEceKeyDesc);
            } else {
                throw new IllegalStateException("Get ece key desc failed! Something wrong with the interface!");
            }
        } else {
            this.mEceKeyDesc = this.mEceKeyDescMap.get(Integer.valueOf(userId));
        }
        setSfpPolicyInner(context, name, this.mEceKeyDesc, 2);
    }

    public void setSecePolicy(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException, IOException {
        preCheck(context, name);
        int userId = UserHandleEx.myUserId();
        if (!this.mSeceKeyDescMap.containsKey(Integer.valueOf(userId))) {
            this.mSeceKeyDesc = this.mHwSfpManager.getKeyDesc(userId, 3);
            if (this.mSeceKeyDesc != null) {
                this.mSeceKeyDescMap.put(Integer.valueOf(userId), this.mSeceKeyDesc);
            } else {
                throw new IllegalStateException("Get sece key desc failed! Something wrong with the interface!");
            }
        } else {
            this.mSeceKeyDesc = this.mSeceKeyDescMap.get(Integer.valueOf(userId));
        }
        setSfpPolicyInner(context, name, this.mSeceKeyDesc, 3);
    }

    public static final int getPolicyProtectType(Context context, String name) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        preCheck(context, name);
        String path = getExactPath(context, name, SET_LABEL_METHOD_NAME);
        int type = HwSfpIudfXattr.getFileXattrEx(path, !new File(path).isDirectory());
        if (type < 0) {
            return -1;
        }
        return type;
    }

    public SharedPreferences createEceSharePreference(Context context, String name, int mode) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        Log.i(LOG_TAG, "createEceSharePreference name " + name);
        preCheck(context, name);
        return createSharePreference(context, name, mode, 2);
    }

    public SharedPreferences createSeceSharePreference(Context context, String name, int mode) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        Log.i(LOG_TAG, "createSeceSharePreference name " + name);
        preCheck(context, name);
        return createSharePreference(context, name, mode, 3);
    }

    public FileOutputStream createEceFileOutputStream(Context context, String name, int mode) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        Log.i(LOG_TAG, "createEceFileOutputStream name " + name);
        preCheck(context, name);
        return createFileOutputStream(context, name, mode, 2);
    }

    public FileOutputStream createSeceFileOutputStream(Context context, String name, int mode) throws IllegalArgumentException, IllegalStateException, IllegalAccessException, FileNotFoundException {
        Log.i(LOG_TAG, "createSeceFileOutputStream name " + name);
        preCheck(context, name);
        return createFileOutputStream(context, name, mode, 3);
    }

    public int setLabel(Context context, String filePath, String labelName, String labelValue, int flag) {
        long startTime = System.currentTimeMillis();
        int errorCode = setLabelInner(context, filePath, labelName, labelValue, flag);
        reportToHiEvent(context, SET_LABEL_METHOD_NAME, getLabelCode(labelName, labelValue, flag), errorCode, System.currentTimeMillis() - startTime);
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
                return getLabelValueFromValue(getLabelFromXattr(getExactPath(context, filePath, SET_LABEL_METHOD_NAME), labelName), labelName);
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
                return getFlagFromValue(getLabelFromXattr(getExactPath(context, filePath, SET_LABEL_METHOD_NAME), labelName), labelName);
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

    private static String getExactPath(Context context, String name, String methodType) throws IllegalArgumentException, FileNotFoundException {
        String path;
        int userId = UserHandleEx.myUserId();
        String fileName = name;
        boolean validPathCondition = true;
        if (new File(name).isAbsolute()) {
            context.getPackageName();
            String internDataDir = context.getDataDir().getPath();
            String internSdDir = SD_CARD_PATH + userId + File.separatorChar;
            if (!methodType.equals(SET_LABEL_METHOD_NAME)) {
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
            path = context.getDataDir() + File.separator + SHARED_PREFS + File.separator + fileName + SHARED_PREFS_SUFFIX;
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

    private void setSfpPolicyInner(Context context, String name, String keyDesc, int storageType) throws IllegalStateException, FileNotFoundException, IOException {
        int ret;
        String path = getExactPath(context, name, SET_ECE_OR_SECE_METHOD_NAME);
        if (!isFileInvalid(path)) {
            File file = new File(path);
            int fileFlag = !file.isDirectory();
            if (file.isDirectory() || this.mHwSfpManager.getFbeVersion() < 3) {
                ret = HwSfpIudfXattr.setFileXattrEx(path, keyDesc, storageType, fileFlag);
            } else {
                ret = copyToTempFileAndSetPolicy(context, path, null, storageType);
            }
            if (ret != 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Set ");
                sb.append(storageType == 2 ? "ECE" : "SECE");
                sb.append(" policy failed!");
                throw new IOException(sb.toString());
            }
            return;
        }
        throw new IllegalStateException("The file length is over 20M!");
    }

    private SharedPreferences createSharePreference(Context context, String name, int mode, int storageType) throws IllegalArgumentException, FileNotFoundException {
        String str;
        int ret;
        String str2;
        File sharedDir = new File(context.getDataDir(), SHARED_PREFS);
        File preFile = formatFileName(sharedDir, name + SHARED_PREFS_SUFFIX);
        String str3 = "createEceSharePreference";
        if (preFile == null) {
            StringBuilder sb = new StringBuilder();
            if (storageType != 2) {
                str3 = "createSeceSharePreference";
            }
            sb.append(str3);
            sb.append(" create file failed!");
            Log.e(LOG_TAG, sb.toString());
            return null;
        } else if (!preFile.exists()) {
            SharedPreferences sharedFile = context.getSharedPreferences(name, mode);
            if (sharedFile == null) {
                StringBuilder sb2 = new StringBuilder();
                if (storageType != 2) {
                    str3 = "createSeceSharePreference";
                }
                sb2.append(str3);
                sb2.append(" sharedFile is null!");
                Log.e(LOG_TAG, sb2.toString());
                return null;
            }
            SharedPreferences.Editor editor = sharedFile.edit();
            editor.apply();
            editor.commit();
            try {
                String path = preFile.getPath();
                StringBuilder sb3 = new StringBuilder();
                if (storageType == 2) {
                    str = str3;
                } else {
                    str = "createSeceSharePreference";
                }
                sb3.append(str);
                sb3.append(" path = ");
                sb3.append(path);
                Log.e(LOG_TAG, sb3.toString());
                if (this.mHwSfpManager.getFbeVersion() < 3) {
                    ret = setPolicyInner(context, path, storageType);
                } else {
                    ret = copyToTempFileAndSetPolicy(context, path, SHARED_PREFS_SUFFIX, storageType);
                }
                if (ret == 0) {
                    return context.getSharedPreferences(name, mode);
                }
                StringBuilder sb4 = new StringBuilder();
                if (storageType == 2) {
                    str2 = str3;
                } else {
                    str2 = "createSeceSharePreference";
                }
                sb4.append(str2);
                sb4.append(" set policy result = ");
                sb4.append(ret);
                Log.e(LOG_TAG, sb4.toString());
                context.deleteSharedPreferences(name);
                return null;
            } catch (IOException e) {
                StringBuilder sb5 = new StringBuilder();
                if (storageType != 2) {
                    str3 = "createSeceSharePreference";
                }
                sb5.append(str3);
                sb5.append(" set the policy failed!");
                Log.e(LOG_TAG, sb5.toString());
                context.deleteSharedPreferences(name);
                return null;
            }
        } else {
            throw new IllegalArgumentException("the file exists!");
        }
    }

    private FileOutputStream createFileOutputStream(Context context, String name, int mode, int storageType) throws IllegalArgumentException, FileNotFoundException {
        String str;
        String str2 = "createEceFileOutputStream";
        File file = formatFileName(context.getFilesDir(), name);
        if (file == null) {
            return null;
        }
        if (!file.exists()) {
            try {
                FileOutputStream outputStream = context.openFileOutput(name, mode);
                int ret = setPolicyInner(context, file.getPath(), storageType);
                if (ret == 0) {
                    return outputStream;
                }
                boolean isDeleted = file.delete();
                StringBuilder sb = new StringBuilder();
                if (storageType == 2) {
                    str = str2;
                } else {
                    str = "createSeceFileOutputStream";
                }
                sb.append(str);
                sb.append(" set policy result ");
                sb.append(ret);
                sb.append(" delete ");
                sb.append(isDeleted);
                Log.e(LOG_TAG, sb.toString());
                return null;
            } catch (IOException e) {
                boolean isDeleted2 = file.delete();
                StringBuilder sb2 = new StringBuilder();
                if (storageType != 2) {
                    str2 = "createSeceFileOutputStream";
                }
                sb2.append(str2);
                sb2.append(" exception delete result ");
                sb2.append(isDeleted2);
                Log.e(LOG_TAG, sb2.toString());
            }
        } else {
            throw new IllegalArgumentException("The file exists!");
        }
    }

    private int setLabelInner(Context context, String filePath, String labelName, String labelValue, int flag) {
        boolean z = true;
        if (!checkParamsForSetLabel(context, filePath, labelName, labelValue, flag)) {
            return 1;
        }
        try {
            String targetPath = getExactPath(context, filePath, SET_LABEL_METHOD_NAME);
            boolean isDeviceProtectedStorage = context.isDeviceProtectedStorage();
            if (!LABEL_VALUE_S0.equals(labelValue) && !LABEL_VALUE_S1.equals(labelValue)) {
                z = false;
            }
            if (z ^ isDeviceProtectedStorage) {
                Log.w(LOG_TAG, "setLabelInner: labelValue is not match to the CE/DE context!");
                return 3;
            } else if (!isLocalFile(targetPath)) {
                Log.w(LOG_TAG, "setLabelInner: the file belongs to remote file!");
                return 4;
            } else {
                int errorCode = checkSetLabel(targetPath, labelName);
                if (errorCode == 4) {
                    Log.w(LOG_TAG, "setLabelInner: not support set/get xattr attribute for the file!");
                    return 4;
                } else if (errorCode == 2) {
                    Log.w(LOG_TAG, "setLabelInner: the security level of target path has been set!");
                    return 2;
                } else if (!setPolicyWithCheck(context, targetPath, mapToPolicyType(labelName, labelValue, flag))) {
                    Log.e(LOG_TAG, "setLabelInner: failed to set policy!");
                    return 4;
                } else if (!setLabelToXattr(targetPath, labelName, labelValue, flag)) {
                    return 4;
                } else {
                    return 0;
                }
            }
        } catch (FileNotFoundException | IllegalArgumentException e) {
            Log.w(LOG_TAG, "setLabelInner: invalid filePath!");
            return 1;
        }
    }

    private void reportToHiEvent(Context context, String apiName, String label, int errorCode, long costTime) {
        Log.i(LOG_TAG, "eventId : 992740301, result code : " + errorCode);
        if (context == null) {
            Log.e(LOG_TAG, "The context is null!");
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
        return String.format(Locale.ROOT, "%s_%s_%d", labelNameCode, labelValueCode, Integer.valueOf(flag));
    }

    private File formatFileName(File base, String name) {
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
            throw new IOException("target file not exist!");
        }
    }

    private int setPolicyInner(Context context, String path, int policyType) {
        String keyDesc = this.mHwSfpManager.getKeyDesc(UserHandleEx.myUserId(), policyType);
        if (TextUtils.isEmpty(keyDesc)) {
            Log.e(LOG_TAG, "setPolicyInner: key desc is null!");
            return -1;
        }
        int ret = HwSfpIudfXattr.setFileXattrEx(path, keyDesc, policyType, 1);
        Log.i(LOG_TAG, "setPolicyInner: policy = " + policyType + " result = " + ret);
        return ret;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b8, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00be, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00bf, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c3, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00cd, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ce, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d2, code lost:
        throw r0;
     */
    private int copyToTempFileAndSetPolicy(Context context, String fileName, String suffix, int policy) throws IOException {
        if (isMutualExclusivePolicy(context, fileName, policy)) {
            Log.e(LOG_TAG, "copyToTempFileAndSetPolicy: isMutualExclusivePolicy failed!");
            return -1;
        }
        File oldFile = new File(fileName);
        if (oldFile.exists()) {
            String tempFileName = fileName + BACKUP_SUFFIX;
            File tempFile = new File(tempFileName);
            if (!tempFile.createNewFile()) {
                Log.e(LOG_TAG, "copyToTempFileAndSetPolicy: create file failed!");
                return -1;
            }
            int result = setPolicyInner(context, tempFileName, policy);
            if (result != 0) {
                Log.e(LOG_TAG, "copyToTempFileAndSetPolicy: setPolicyInner faild isDeleted = " + tempFile.delete());
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
            Log.e(LOG_TAG, "copyToTempFileAndSetPolicy: " + fileName + " not exist!");
            throw new IOException(fileName + " is not exists!");
        }
    }

    private boolean isFileInvalid(String path) {
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

    private int checkSetLabelToXattr(String filePath, String xattrKey) {
        try {
            return (!filePath.startsWith(MDFS_PATH) || !DEFAULT_MDFS_SECURITY_VALUE_XATTR.equals(new String(Os.getxattr(filePath, xattrKey), StandardCharsets.UTF_8))) ? 2 : 0;
        } catch (ErrnoException ex) {
            if (ex.errno == OsConstants.ENODATA) {
                return 0;
            }
            if (ex.errno == OsConstants.EOPNOTSUPP) {
                Log.e(LOG_TAG, "checkSetLabelToXattr: The xattr ability is not supported!");
                return 4;
            }
            Log.e(LOG_TAG, "checkSetLabelToXattr: Os error!");
            return 2;
        }
    }

    private String getLabelFromXattr(String filePath, String labelName) {
        try {
            return new String(Os.getxattr(filePath, mapLabelNameToXattrKey(labelName)), StandardCharsets.UTF_8);
        } catch (ErrnoException ex) {
            if (ex.errno == OsConstants.ENODATA) {
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
                Os.setxattr(filePath, mapLabelNameToXattrKey(labelName), transSecurityLevelToXattrValue(labelValue, flag), 0);
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

    private String mapLabelNameToXattrKey(String labelName) {
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

    private boolean isXattrValueVaild(String value, String labelName) {
        if (!TextUtils.isEmpty(value) && LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            return Pattern.matches(SECURITY_VALUE_XATTR_PATTERN, value);
        }
        return false;
    }

    private String getLabelValueFromValue(String value, String labelName) {
        if (isXattrValueVaild(value, labelName) && LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            return value.toUpperCase(Locale.ROOT).substring(0, LABEL_VALUE_S0.length());
        }
        return "";
    }

    private int getFlagFromValue(String value, String labelName) {
        if (!isXattrValueVaild(value, labelName) || !LABEL_NAME_SECURITY_LEVEL.equals(labelName)) {
            return -1;
        }
        if (value.endsWith(FLAG_FILE_PROTECTION_COMPLETE_UNLESS_OPEN_XATTR)) {
            return 1;
        }
        return 0;
    }

    private int checkSetLabel(String filePath, String labelName) {
        return checkSetLabelToXattr(filePath, mapLabelNameToXattrKey(labelName));
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
        } else if (context != null) {
            return checkLabelValueAndFlag(labelValue, flag);
        } else {
            Log.w(LOG_TAG, "checkParamsForSetLabel: context is null!");
            return false;
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
        int userId = UserHandleEx.myUserId();
        if (filePath.startsWith(SD_CARD_PATH + userId + File.separatorChar)) {
            Log.i(LOG_TAG, "setPolicyWithCheck: can't set ECE/SECE for the file in sdcard directory!");
            return true;
        } else if (storageType == -1) {
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
            Log.e(LOG_TAG, "isMutualExclusivePolicy: getPolicyType has illegal argument!");
            return true;
        } catch (IllegalAccessException e2) {
            Log.e(LOG_TAG, "isMutualExclusivePolicy: getPolicyType has illegal access!");
            return true;
        } catch (FileNotFoundException e3) {
            Log.e(LOG_TAG, "isMutualExclusivePolicy: getPolicyType has file error!");
            return true;
        } catch (IllegalStateException e4) {
            Log.e(LOG_TAG, "isMutualExclusivePolicy: getPolicyType has illegal state!");
            return true;
        }
    }
}
