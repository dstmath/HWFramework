package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Flog;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import com.android.server.pm.auth.HwCertificationManager;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class BdReportUtils {
    private static final int LEGAL_RECORD_NUM = 4;
    private static final String POLICY_PGKNAME_SEPARATOR = "_";
    private static final String SEPARATOR = ":";
    private static final int SIGNATURE_LENGTH = 16;
    private static final String TAG = "BdReportUtils";
    private static final String TAG_NULL = "null";
    private static final String TERMINATOR = ";";

    public static void reportSetPolicyPkgData(ComponentName who, Context reportContext, Bundle policyData) {
        if (who != null && reportContext != null && policyData != null) {
            String ownerPkgName = who.getPackageName();
            if (!TextUtils.isEmpty(ownerPkgName)) {
                String[] ownerPkgData = getOwnerPkgData(ownerPkgName, reportContext);
                String[] pkgPolicyData = getPkgPolicyData(policyData);
                Flog.bdReport(991311022, String.format(Locale.ENGLISH, "{oPkg:%s,oPkgSig:%s,oPkgVer:%s,pkg:%s,bundle:%s}", ownerPkgName, ownerPkgData[0], ownerPkgData[1], pkgPolicyData[0], pkgPolicyData[1]));
            }
        }
    }

    public static void reportInstallPkgData(String ownerPkgName, String pkgName, Context reportContext) {
        if (!TextUtils.isEmpty(ownerPkgName) && reportContext != null) {
            Flog.bdReport(991311020, String.format(Locale.ENGLISH, "{oPkg:%s,pkg:%s}", ownerPkgName, getHashCodeForString(pkgName)));
        }
    }

    public static void reportUninstallPkgData(String ownerPkgName, String pkgName, Context reportContext) {
        if (!TextUtils.isEmpty(ownerPkgName) && reportContext != null) {
            Flog.bdReport(991311021, String.format(Locale.ENGLISH, "{oPkg:%s,pkg:%s}", ownerPkgName, getHashCodeForString(pkgName)));
        }
    }

    private static String[] getOwnerPkgData(String ownerPkgName, Context reportContext) {
        String[] ownerPkgData = {TAG_NULL, TAG_NULL};
        String ownerPkgSignature = HwCertificationManager.getInstance().getSignatureOfCert(ownerPkgName, 0);
        if (!TextUtils.isEmpty(ownerPkgSignature)) {
            if (ownerPkgSignature.length() < SIGNATURE_LENGTH) {
                ownerPkgData[0] = ownerPkgSignature;
            } else {
                ownerPkgData[0] = ownerPkgSignature.substring(0, SIGNATURE_LENGTH);
            }
        }
        PackageManager packageManager = reportContext.getPackageManager();
        if (packageManager == null) {
            return ownerPkgData;
        }
        String ownerPkgVersion = TAG_NULL;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(ownerPkgName, 0);
            if (packageInfo != null) {
                ownerPkgVersion = packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            HwLog.e(TAG, "getOwnerPkgData NameNotFoundException");
        }
        if (!TextUtils.isEmpty(ownerPkgVersion)) {
            ownerPkgData[1] = ownerPkgVersion;
        }
        return ownerPkgData;
    }

    private static String[] getPkgPolicyData(Bundle policyData) {
        char c = 2;
        String[] pkgPolicyData = {TAG_NULL, TAG_NULL};
        String diffValue = policyData.getString("diffValue");
        if (TextUtils.isEmpty(diffValue)) {
            return pkgPolicyData;
        }
        pkgPolicyData[0] = SettingsMDMPlugin.EMPTY_STRING;
        String[] terminatorList = diffValue.split(TERMINATOR);
        int length = terminatorList.length;
        int i = 0;
        while (i < length) {
            String[] infoList = terminatorList[i].split(SEPARATOR);
            if (infoList.length == 4) {
                pkgPolicyData[0] = pkgPolicyData[0] + getHashCodeForString(infoList[0]) + POLICY_PGKNAME_SEPARATOR;
                if (TAG_NULL.equals(pkgPolicyData[1])) {
                    boolean isAddItem = policyData.getBoolean("isAddItem");
                    String str = "1";
                    pkgPolicyData[1] = "true".equals(infoList[c]) ? str : HwDevicePolicyManagerService.DYNAMIC_ROOT_STATE_SAFE;
                    StringBuilder sb = new StringBuilder();
                    sb.append(pkgPolicyData[1]);
                    sb.append("true".equals(infoList[3]) ? str : HwDevicePolicyManagerService.DYNAMIC_ROOT_STATE_SAFE);
                    pkgPolicyData[1] = sb.toString();
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(pkgPolicyData[1]);
                    if (!isAddItem) {
                        str = HwDevicePolicyManagerService.DYNAMIC_ROOT_STATE_SAFE;
                    }
                    sb2.append(str);
                    pkgPolicyData[1] = sb2.toString();
                }
            }
            i++;
            c = 2;
        }
        if (TAG_NULL.equals(pkgPolicyData[1])) {
            pkgPolicyData[0] = TAG_NULL;
        }
        return pkgPolicyData;
    }

    private static String getHashCodeForString(String data) {
        if (TextUtils.isEmpty(data)) {
            return TAG_NULL;
        }
        try {
            String dataSha256 = sha256(data.getBytes("UTF-8"));
            if (TextUtils.isEmpty(dataSha256) || TAG_NULL.equals(dataSha256)) {
                return TAG_NULL;
            }
            return dataSha256.substring(0, 32);
        } catch (UnsupportedEncodingException e) {
            HwLog.e(TAG, "getHashCodeForString UnsupportedEncodingException");
            return TAG_NULL;
        }
    }

    private static String sha256(byte[] data) {
        if (data == null) {
            return TAG_NULL;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            return bytesToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            HwLog.e(TAG, "sha256 NoSuchAlgorithmException");
            return TAG_NULL;
        }
    }

    private static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return TAG_NULL;
        }
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] chars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int byteValue = bytes[j] & 255;
            chars[j * 2] = hexChars[byteValue >>> 4];
            chars[(j * 2) + 1] = hexChars[byteValue & 15];
        }
        return new String(chars).toUpperCase(Locale.ENGLISH);
    }
}
