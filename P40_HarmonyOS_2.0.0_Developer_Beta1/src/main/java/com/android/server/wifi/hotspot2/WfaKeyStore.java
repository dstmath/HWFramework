package com.android.server.wifi.hotspot2;

import android.os.Environment;
import android.util.Log;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class WfaKeyStore {
    private static final String DEFAULT_WFA_CERT_DIR = (Environment.getRootDirectory() + "/etc/security/cacerts_wfa");
    private static final String TAG = "PasspointWfaKeyStore";
    private KeyStore mKeyStore = null;
    private boolean mVerboseLoggingEnabled = false;

    public void load() {
        if (this.mKeyStore == null) {
            int index = 0;
            try {
                this.mKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                this.mKeyStore.load(null, null);
                for (X509Certificate cert : WfaCertBuilder.loadCertsFromDisk(DEFAULT_WFA_CERT_DIR)) {
                    this.mKeyStore.setCertificateEntry(String.format("%d", Integer.valueOf(index)), cert);
                    index++;
                }
                if (index <= 0) {
                    Log.wtf(TAG, "No certs loaded");
                }
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                e.printStackTrace();
            }
        }
    }

    public KeyStore get() {
        return this.mKeyStore;
    }
}
