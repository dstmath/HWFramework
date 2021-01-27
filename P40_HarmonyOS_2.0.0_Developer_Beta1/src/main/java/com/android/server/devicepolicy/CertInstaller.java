package com.android.server.devicepolicy;

import android.security.Credentials;
import android.security.KeyStore;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

public class CertInstaller {
    private static final int CERTIFICATE_DEFAULT = 0;
    private static final int CERTIFICATE_WIFI = 1;
    private static final KeyStore KEY_STORE = KeyStore.getInstance();
    private static final String TAG = "DPMS_CerInstall";

    private CertInstaller() {
    }

    private static boolean initCertDataByCertType(int certInstallType, CertData data) {
        if (certInstallType != 0) {
            if (certInstallType != 1) {
                return false;
            }
            data.setUid(1010);
            data.setFlag(0);
        } else if (!KEY_STORE.isUnlocked()) {
            HwLog.e(TAG, "Keystore is " + KEY_STORE.state().toString() + ". Credentials cannot be installed until device is unlocked");
            return false;
        } else {
            data.setUid(-1);
            data.setFlag(1);
        }
        return true;
    }

    private static boolean importKey(String alias, PrivateKey userKey, CertData data) {
        if (userKey == null) {
            return true;
        }
        if (KEY_STORE.importKey("USRPKEY_" + alias, userKey.getEncoded(), data.getUid(), data.getFlag())) {
            return true;
        }
        HwLog.e(TAG, "Failed to import key");
        return false;
    }

    private static boolean importCert(String name, CertData data, Certificate... objects) {
        byte[] certBuffer = null;
        try {
            certBuffer = Credentials.convertToPem(objects);
        } catch (CertificateEncodingException e) {
            HwLog.e(TAG, "Failed to install convertToPem CertificateEncodingException");
        } catch (IOException e2) {
            HwLog.e(TAG, "Failed to install convertToPem IOException");
        } catch (Exception e3) {
            HwLog.e(TAG, "Failed to install convertToPem Exception");
        }
        if (KEY_STORE.put(name, certBuffer, data.getUid(), data.getFlag())) {
            return true;
        }
        HwLog.e(TAG, "Failed to install " + name + " as uid " + data.getUid());
        return false;
    }

    public static boolean installCert(String alias, PrivateKey userKey, X509Certificate userCert, List<X509Certificate> caCerts, int certInstallType) {
        CertData data = new CertData();
        if (!initCertDataByCertType(certInstallType, data) || !importKey(alias, userKey, data)) {
            return false;
        }
        if (!importCert("USRCERT_" + alias, data, userCert)) {
            return false;
        }
        if (!importCert("CACERT_" + alias, data, (X509Certificate[]) caCerts.toArray(new X509Certificate[caCerts.size()]))) {
            return false;
        }
        HwLog.d(TAG, "install cert success!");
        return true;
    }

    /* access modifiers changed from: private */
    public static class CertData {
        private int flag;
        private int uid;

        private CertData() {
        }

        public void setFlag(int flag2) {
            this.flag = flag2;
        }

        public void setUid(int uid2) {
            this.uid = uid2;
        }

        public int getFlag() {
            return this.flag;
        }

        public int getUid() {
            return this.uid;
        }
    }
}
