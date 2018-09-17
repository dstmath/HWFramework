package com.android.server.devicepolicy;

import android.security.Credentials;
import android.security.KeyStore;
import android.util.Log;
import com.android.server.pm.HwPackageManagerService;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertInstaller {
    private static final int CERTIFICATE_DEFAULT = 0;
    private static final int CERTIFICATE_WIFI = 1;
    private static final String TAG = "DPMS_CerInstall";
    private static final KeyStore mKeyStore = KeyStore.getInstance();

    public static boolean installCert(String alias, PrivateKey userKey, X509Certificate userCert, List<X509Certificate> caCerts, int certInstallType) {
        int uid;
        int encryptFlag;
        switch (certInstallType) {
            case 0:
                if (mKeyStore.isUnlocked()) {
                    uid = -1;
                    encryptFlag = 1;
                    break;
                }
                Log.e(TAG, "Keystore is " + mKeyStore.state().toString() + ". Credentials cannot" + " be installed until device is unlocked");
                return false;
            case 1:
                uid = HwPackageManagerService.TRANSACTION_CODE_SET_HDB_KEY;
                encryptFlag = 0;
                break;
            default:
                return false;
        }
        String key = "USRPKEY_" + alias;
        if (userKey != null) {
            if (!mKeyStore.importKey(key, userKey.getEncoded(), uid, encryptFlag)) {
                Log.e(TAG, "Failed to install wifi cert");
                return false;
            }
        }
        byte[] certData = null;
        String certName = "USRCERT_" + alias;
        try {
            certData = Credentials.convertToPem(new Certificate[]{userCert});
        } catch (Exception e) {
            Log.e(TAG, "Failed to install convertToPem user cert");
        }
        if (mKeyStore.put(certName, certData, uid, encryptFlag)) {
            String caListName = "CACERT_" + alias;
            byte[] caListData = null;
            try {
                caListData = Credentials.convertToPem((X509Certificate[]) caCerts.toArray(new X509Certificate[caCerts.size()]));
            } catch (Exception e2) {
                Log.e(TAG, "Failed to install convertToPem ca cert");
            }
            if (mKeyStore.put(caListName, caListData, uid, encryptFlag)) {
                Log.d(TAG, "install cert success!");
                return true;
            }
            Log.e(TAG, "Failed to install " + caListName + " as uid " + uid);
            return false;
        }
        Log.e(TAG, "Failed to install " + certName + " as uid " + uid);
        return false;
    }
}
