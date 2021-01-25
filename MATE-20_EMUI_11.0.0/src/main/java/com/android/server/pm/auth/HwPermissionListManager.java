package com.android.server.pm.auth;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.huawei.cust.HwCfgFilePolicy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwPermissionListManager {
    private static final List<String> ALLOWED_PERMISSIONS = Arrays.asList("android.permission.ACCESS_SURFACE_FLINGER", "android.permission.BACKUP", "android.permission.BATTERY_STATS", "android.permission.BIND_APPWIDGET", "android.permission.BIND_DEVICE_ADMIN", "android.permission.CHANGE_BACKGROUND_DATA_SETTING", "android.permission.CHANGE_COMPONENT_ENABLED_STATE", "android.permission.CHANGE_CONFIGURATION", "android.permission.CLEAR_APP_USER_DATA", "android.permission.CONNECTIVITY_INTERNAL", "android.permission.CRYPT_KEEPER", "android.permission.DELETE_PACKAGES", "android.permission.DEVICE_POWER", "android.permission.FORCE_STOP_PACKAGES", "android.permission.GET_APP_OPS_STATS", "android.permission.INJECT_EVENTS", "android.permission.INSTALL_PACKAGES", "android.permission.INTERACT_ACROSS_USERS_FULL", "android.permission.MANAGE_DEVICE_ADMINS", "android.permission.MANAGE_NETWORK_POLICY", "android.permission.MANAGE_USB", "android.permission.MANAGE_USERS", "android.permission.MASTER_CLEAR", "android.permission.MODIFY_NETWORK_ACCOUNTING", "android.permission.MODIFY_PHONE_STATE", "android.permission.MOUNT_FORMAT_FILESYSTEMS", "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", "android.permission.PACKAGE_USAGE_STATS", "android.permission.PACKAGE_VERIFICATION_AGENT", "android.permission.READ_FRAME_BUFFER", "android.permission.READ_LOGS", "android.permission.READ_NETWORK_USAGE_HISTORY", "android.permission.REAL_GET_TASKS", "android.permission.REBOOT", "android.permission.REMOVE_TASKS", "android.permission.SET_PREFERRED_APPLICATIONS", "android.permission.SET_TIME", "android.permission.SET_TIME_ZONE", "android.permission.SET_WALLPAPER_COMPONENT", "android.permission.SHUTDOWN", "android.permission.STATUS_BAR", "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.UPDATE_APP_OPS_STATS", "android.permission.WRITE_APN_SETTINGS", "android.permission.WRITE_MEDIA_STORAGE", "android.permission.WRITE_SECURE_SETTINGS", "android.permission.WRITE_SETTINGS");
    private static final String CONFIG_FILE_PATH = "xml/mdm/ExtendSystemPermissions.xml";
    private static final int CUST_TYPE_CONFIG = 0;
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final int DEFAULT_LIST_SIZE = 20;
    private static final int DEFAULT_READ_SIZE = 1024;
    private static final int MATCH_GROUP_ONE = 1;
    private static final int MAX_FILE_LENGTH = 90000;
    private static final String PERMISSION_SPECIAL_PREFIX = "android.permission.";
    private static final Pattern PER_PATTERN = Pattern.compile("<allow-permission name=\"(.+)\" />");
    private static final String PUBLIC_KEY_MDM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2XVr6QCbWnZjNEaN+44ttGWQDi8SPk7Y1skVxvN7jOt760C68GGh95gqB4AAvOF8yICUqvsxTu19hh3v+vpW4clkGQl3t6wWkoZlzLoAxPRHurBVZyxhL4qFueukA0bsOLYEbU+8c9EZ8e8gyqtvD+Bu8lBpExM1bblhyOYi3thratzdDyJjmSRivss5iA0AJsHa9z+aap/AZAs1xjBzK01XwMydOKIK7Jeopval0I7HjERp0U8F0RU6ZwuMmg0xWbJULO6goUI5qZLpdNrBfloHexgGPQ9dA48JtiXNPqYezVhC3129Kf9ECzBtHn8Y/gm2RksCHJvAhxr7ewnl+QIDAQAB";
    private static final String SHA256WITHRSA = "SHA256withRSA";
    private static final Pattern SIGN_PATTERN = Pattern.compile("<signature>(.+)</signature>");
    private static final String TAG = "HwCertificationManager";
    private final List<String> mExtendSystemPermissions;
    private volatile boolean mIsNeedScanCfgFile;

    private HwPermissionListManager() {
        this.mIsNeedScanCfgFile = true;
        this.mExtendSystemPermissions = new ArrayList(20);
    }

    public static HwPermissionListManager getInstance() {
        return HwPermissionListManagerHolder.INSTANCE;
    }

    private List<String> getExtendSystemPermissions() {
        if (!this.mExtendSystemPermissions.isEmpty()) {
            return this.mExtendSystemPermissions;
        }
        if (this.mIsNeedScanCfgFile) {
            scanCfgFileAndVerify();
            this.mIsNeedScanCfgFile = false;
        }
        return this.mExtendSystemPermissions;
    }

    public boolean couldGrantExtendSystemPermissionToMdmApk(String permission) {
        if (TextUtils.isEmpty(permission)) {
            return false;
        }
        if (!ALLOWED_PERMISSIONS.contains(permission) && !getExtendSystemPermissions().contains(permission)) {
            return false;
        }
        return true;
    }

    public static boolean isPermissionControlledForMdmApk(String permission) {
        if (!TextUtils.isEmpty(permission) && permission.startsWith(PERMISSION_SPECIAL_PREFIX)) {
            return true;
        }
        return false;
    }

    private void scanCfgFileAndVerify() {
        long startTime = System.nanoTime();
        File xmlFile = HwCfgFilePolicy.getCfgFile(CONFIG_FILE_PATH, 0);
        if (xmlFile == null || !xmlFile.exists()) {
            HwAuthLogger.info("HwCertificationManager", "normal version, do not have extend system permissions");
        } else if (xmlFile.length() > 90000) {
            HwAuthLogger.error("HwCertificationManager", "ExtendSystemPermissions xml is more than of max length");
        } else if (verify(xmlFile)) {
            HwAuthLogger.info("HwCertificationManager", "getPermission from file total time = " + (System.nanoTime() - startTime) + "ns");
        }
    }

    private boolean verify(File file) {
        byte[] fileDataBytes = readProfileToByte(file);
        if (fileDataBytes == null) {
            return false;
        }
        try {
            String profileStr = new String(fileDataBytes, DEFAULT_CHARSET);
            Pair<byte[], String> pairInfo = getUnsignedProfileByteAndSignature(profileStr);
            if (pairInfo == null || pairInfo.first == null) {
                HwAuthLogger.error("HwCertificationManager", "pairInfo is null or signature info is null");
                return false;
            }
            PublicKey publicKey = DevicePublicKeyLoader.getPublicKeyForBase64(PUBLIC_KEY_MDM);
            if (publicKey == null) {
                HwAuthLogger.error("HwCertificationManager", "publicKey is null");
                return false;
            }
            RSAPublicKey pubKey = null;
            if (publicKey instanceof RSAPublicKey) {
                pubKey = (RSAPublicKey) publicKey;
            }
            if (pubKey == null) {
                HwAuthLogger.error("HwCertificationManager", "pubKey is null");
                return false;
            }
            String signatureData = (String) pairInfo.second;
            if (TextUtils.isEmpty(signatureData)) {
                HwAuthLogger.error("HwCertificationManager", "signatureData is null");
                return false;
            }
            boolean isVerifySuccess = verify((byte[]) pairInfo.first, pubKey, signatureData);
            if (isVerifySuccess) {
                matcherAndAddExtendSystemPermissionsToList(profileStr);
            }
            return isVerifySuccess;
        } catch (UnsupportedEncodingException e) {
            HwAuthLogger.error("HwCertificationManager", "Unsupported Encoding Exception when new string");
            return false;
        }
    }

    private boolean verify(byte[] data, RSAPublicKey pubKey, String sign) {
        try {
            Signature signTool = Signature.getInstance(SHA256WITHRSA);
            signTool.initVerify(pubKey);
            signTool.update(data);
            return signTool.verify(Base64.decode(sign, 0));
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            HwAuthLogger.error("HwCertificationManager", "verify Exception");
            return false;
        }
    }

    private Pair<byte[], String> getUnsignedProfileByteAndSignature(String profileStr) {
        if (TextUtils.isEmpty(profileStr)) {
            return null;
        }
        try {
            Matcher matcher = SIGN_PATTERN.matcher(profileStr);
            if (!matcher.find() || matcher.groupCount() <= 0) {
                return null;
            }
            String signatureInfo = matcher.group(1);
            return Pair.create(profileStr.replace(signatureInfo, "").getBytes(DEFAULT_CHARSET), signatureInfo);
        } catch (UnsupportedEncodingException e) {
            HwAuthLogger.error("HwCertificationManager", "getUnsignedProfileByteAndSignature error!");
            return null;
        }
    }

    private void matcherAndAddExtendSystemPermissionsToList(String xmlConent) {
        if (!TextUtils.isEmpty(xmlConent)) {
            Matcher perMatcher = PER_PATTERN.matcher(xmlConent);
            while (perMatcher.find()) {
                if (perMatcher.groupCount() > 0) {
                    this.mExtendSystemPermissions.add(perMatcher.group(1));
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0038, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0039, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003c, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0040, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0043, code lost:
        throw r3;
     */
    private byte[] readProfileToByte(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FileInputStream inputStream = new FileInputStream(file);
            byte[] caches = new byte[1024];
            while (true) {
                int value = inputStream.read(caches);
                if (value != -1) {
                    outputStream.write(caches, 0, value);
                    outputStream.flush();
                } else {
                    byte[] byteArrays = outputStream.toByteArray();
                    $closeResource(null, inputStream);
                    $closeResource(null, outputStream);
                    return byteArrays;
                }
            }
        } catch (IOException e) {
            HwAuthLogger.error("HwCertificationManager", "readProfileToByte error!!");
            return null;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private static class HwPermissionListManagerHolder {
        private static final HwPermissionListManager INSTANCE = new HwPermissionListManager();

        private HwPermissionListManagerHolder() {
        }
    }
}
