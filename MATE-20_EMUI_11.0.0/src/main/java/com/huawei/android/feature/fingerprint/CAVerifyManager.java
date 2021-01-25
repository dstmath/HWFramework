package com.huawei.android.feature.fingerprint;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.feature.fingerprint.signutils.Base64;
import com.huawei.android.feature.fingerprint.signutils.HexUtil;
import com.huawei.android.feature.fingerprint.signutils.SHA256Utils;
import com.huawei.android.feature.fingerprint.signutils.X509CertUtil;
import com.huawei.android.feature.module.DynamicModuleInfo;
import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

public class CAVerifyManager implements SignVerifyStrategy {
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String METADATA_HMS_CERT_CHAIN = "com.huawei.hms.sign_certchain";
    private static final String METADATA_HMS_SIGNATURE = "com.huawei.hms.fingerprint_signature";
    private static final String TAG = "CAVerifyManager";

    private static boolean checkCertChain(String str, String str2, String str3) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            Log.e(TAG, "Args is invalid.");
            return false;
        }
        List<X509Certificate> certChain = X509CertUtil.getCertChain(str2);
        if (certChain.size() == 0) {
            Log.e(TAG, "CertChain is empty.");
            return false;
        } else if (!X509CertUtil.verifyCertChain(X509CertUtil.getCBGRootCA(), certChain)) {
            Log.e(TAG, "failed to verify cert chain");
            return false;
        } else {
            X509Certificate x509Certificate = certChain.get(0);
            if (!X509CertUtil.checkSubjectCN(x509Certificate, "Huawei CBG HMS Kit")) {
                Log.e(TAG, "CN is invalid");
                return false;
            } else if (!X509CertUtil.checkSubjectOU(x509Certificate, "Huawei CBG Cloud Security Signer")) {
                Log.e(TAG, "OU is invalid");
                return false;
            } else {
                byte[] bArr = null;
                try {
                    bArr = str3.getBytes(DEFAULT_CHARSET);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "checkCertChain UnsupportedEncodingException:", e);
                }
                if (X509CertUtil.checkSignature(x509Certificate, bArr, Base64.decode(str))) {
                    return true;
                }
                Log.e(TAG, "signature is invalid: ");
                return false;
            }
        }
    }

    private static String getPackageSignature(Context context, String str) {
        byte[] packageSignatureBytes = getPackageSignatureBytes(context, str);
        if (packageSignatureBytes == null || packageSignatureBytes.length == 0) {
            return null;
        }
        return HexUtil.encodeHexString(SHA256Utils.digest(packageSignatureBytes), true);
    }

    private static byte[] getPackageSignatureBytes(Context context, String str) {
        PackageInfo packageArchiveInfo = context.getPackageManager().getPackageArchiveInfo(str, 64);
        if (packageArchiveInfo != null && packageArchiveInfo.signatures != null && packageArchiveInfo.signatures.length != 0) {
            return packageArchiveInfo.signatures[0].toByteArray();
        }
        Log.e(TAG, "Failed to get application signature certificate fingerprint.");
        return new byte[0];
    }

    public static boolean verifyCAFingerPrint(Context context, String str) {
        if (context == null || TextUtils.isEmpty(str)) {
            Log.w(TAG, "The context or modulePath is null.");
            return false;
        }
        PackageInfo packageArchiveInfo = context.getPackageManager().getPackageArchiveInfo(str, 128);
        if (packageArchiveInfo == null || packageArchiveInfo.applicationInfo == null) {
            Log.e(TAG, "PackageArchiveInfo is null.");
            return false;
        }
        String str2 = packageArchiveInfo.packageName;
        Bundle bundle = packageArchiveInfo.applicationInfo.metaData;
        if (bundle == null) {
            Log.e(TAG, "Verify package " + str2 + " failed for metadata is null.");
            return false;
        } else if (!bundle.containsKey(METADATA_HMS_SIGNATURE)) {
            Log.e(TAG, "Verify package " + str2 + " failed for no signer.");
            return false;
        } else if (!bundle.containsKey(METADATA_HMS_CERT_CHAIN)) {
            Log.e(TAG, "Verify package " + str2 + " failed for no cert chain.");
            return false;
        } else {
            String packageSignature = getPackageSignature(context, str);
            if (TextUtils.isEmpty(packageSignature)) {
                Log.e(TAG, "Get PackageSignature failed: null.");
                return false;
            }
            if (!checkCertChain(bundle.getString(METADATA_HMS_SIGNATURE), bundle.getString(METADATA_HMS_CERT_CHAIN), str2 + "&" + packageSignature)) {
                Log.e(TAG, "Check CertChain failed.");
                return false;
            }
            Log.i(TAG, "verify FingerPrint success.");
            return true;
        }
    }

    @Override // com.huawei.android.feature.fingerprint.SignVerifyStrategy
    public boolean verifyFingerPrint(Context context, DynamicModuleInfo dynamicModuleInfo) {
        if (dynamicModuleInfo == null) {
            Log.e(TAG, "The dynamic module info is null.");
            return false;
        }
        Log.i(TAG, "Verify strategy: root CA verify.");
        return verifyCAFingerPrint(context, dynamicModuleInfo.mTempPath);
    }
}
