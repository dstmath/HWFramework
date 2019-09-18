package com.huawei.security.hccm;

import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.hccm.local.LocalClientCertificateManager;
import com.huawei.security.hccm.param.EnrollmentContext;
import com.huawei.security.hccm.param.EnrollmentParamsSpec;
import java.security.InvalidParameterException;
import java.security.Security;
import java.security.cert.Certificate;

public abstract class ClientCertificateManager {
    public static final String BC_KEY_STORE_PROVIDER = "BC";
    public static final String HCCM_SDK_VERSION = "9.0.0.2";
    public static final String HW_KEY_STORE_PROVIDER = "HwUniversalKeyStoreProvider";
    public static final String HW_KEY_STORE_TYPE = "HwKeyStore";
    private static final String MIN_HUKS_SERVICE_VERSION = "1.0.5";
    private static final String TAG = "ClientCertificateManager";
    private static LocalClientCertificateManager mLocalCCM;

    public abstract void delete(@NonNull String str) throws EnrollmentException;

    public abstract Certificate[] enroll(@NonNull EnrollmentParamsSpec enrollmentParamsSpec) throws Exception;

    public abstract EnrollmentContext find(@NonNull String str) throws EnrollmentException;

    public abstract void store(@NonNull EnrollmentContext enrollmentContext) throws EnrollmentException;

    public static synchronized ClientCertificateManager getInstance(String keyStoreType, String keyStoreProvider) throws InvalidParameterException, EnrollmentException {
        LocalClientCertificateManager localClientCertificateManager;
        synchronized (ClientCertificateManager.class) {
            if (Security.getProvider(keyStoreProvider) == null || Security.getProvider("BC") == null) {
                Log.e(TAG, "Defined key store provider is not supported!");
                throw new EnrollmentException("Defined key store provider is not supported!", -32);
            } else if (checkVersion()) {
                if (mLocalCCM == null) {
                    mLocalCCM = new LocalClientCertificateManager(keyStoreType, keyStoreProvider);
                    if (mLocalCCM == null) {
                        Log.e(TAG, "init LocalClientCertificateManager failed");
                        throw new EnrollmentException("init LocalClientCertificateManager failed", -31);
                    }
                }
                localClientCertificateManager = mLocalCCM;
            } else {
                throw new EnrollmentException("the current HUKS service version not support", -7);
            }
        }
        return localClientCertificateManager;
    }

    private static boolean checkVersion() {
        HwKeystoreManager hwKeystoreManager = HwKeystoreManager.getInstance();
        if (hwKeystoreManager == null) {
            Log.e(TAG, "get HwKeyStoreManager failed");
            return false;
        } else if (isCurrentVersionSupport(hwKeystoreManager.getHuksServiceVersion(), MIN_HUKS_SERVICE_VERSION)) {
            return true;
        } else {
            Log.e(TAG, "the current HUKS service not support");
            return false;
        }
    }

    public static boolean isCurrentVersionSupport(String curVer, String minVer) {
        String[] minVerArray = minVer.split("\\.");
        String[] curVerArray = curVer.split("\\.");
        int minLen = minVerArray.length < curVerArray.length ? minVerArray.length : curVerArray.length;
        Log.d(TAG, "the current version is " + curVer);
        for (int index = 0; index < minLen; index++) {
            if (Integer.parseInt(curVerArray[index]) - Integer.parseInt(minVerArray[index]) < 0) {
                Log.d(TAG, "version not support");
                return false;
            }
        }
        if (minVerArray.length <= curVerArray.length) {
            return true;
        }
        Log.e(TAG, "version not support for it's earlier version");
        return false;
    }

    public static String getHccmSdkVersion() {
        return HCCM_SDK_VERSION;
    }
}
