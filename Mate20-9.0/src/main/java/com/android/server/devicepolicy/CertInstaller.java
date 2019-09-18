package com.android.server.devicepolicy;

import android.security.Credentials;
import android.security.KeyStore;
import android.util.Log;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
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
        String str = alias;
        switch (certInstallType) {
            case 0:
                if (mKeyStore.isUnlocked()) {
                    uid = -1;
                    encryptFlag = 1;
                    break;
                } else {
                    Log.e(TAG, "Keystore is " + mKeyStore.state().toString() + ". Credentials cannot be installed until device is unlocked");
                    return false;
                }
            case 1:
                uid = HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT;
                encryptFlag = 0;
                break;
            default:
                List<X509Certificate> list = caCerts;
                return false;
        }
        int encryptFlag2 = encryptFlag;
        int uid2 = uid;
        String key = "USRPKEY_" + str;
        if (userKey != null) {
            if (!mKeyStore.importKey(key, userKey.getEncoded(), uid2, encryptFlag2)) {
                Log.e(TAG, "Failed to install wifi cert");
                return false;
            }
        }
        byte[] certData = null;
        String certName = "USRCERT_" + str;
        try {
            certData = Credentials.convertToPem(new Certificate[]{userCert});
        } catch (Exception e) {
            Log.e(TAG, "Failed to install convertToPem user cert");
        }
        if (!mKeyStore.put(certName, certData, uid2, encryptFlag2)) {
            Log.e(TAG, "Failed to install " + certName + " as uid " + uid2);
            return false;
        }
        String caListName = "CACERT_" + str;
        byte[] caListData = null;
        try {
            caListData = Credentials.convertToPem((X509Certificate[]) caCerts.toArray(new X509Certificate[caCerts.size()]));
        } catch (Exception e2) {
            Exception exc = e2;
            Log.e(TAG, "Failed to install convertToPem ca cert");
        }
        if (!mKeyStore.put(caListName, caListData, uid2, encryptFlag2)) {
            Log.e(TAG, "Failed to install " + caListName + " as uid " + uid2);
            return false;
        }
        Log.d(TAG, "install cert success!");
        return true;
    }
}
