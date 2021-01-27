package com.android.server.wifi.hotspot2;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

public class WfaCertBuilder {
    private static final String TAG = "PasspointWfaCertBuilder";

    public static Set<X509Certificate> loadCertsFromDisk(String directory) {
        Set<X509Certificate> certs = new HashSet<>();
        try {
            File[] certFiles = new File(directory).listFiles();
            if (certFiles != null) {
                if (certFiles.length > 0) {
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    for (File certFile : certFiles) {
                        FileInputStream fis = new FileInputStream(certFile);
                        Certificate cert = certFactory.generateCertificate(fis);
                        if (cert instanceof X509Certificate) {
                            certs.add((X509Certificate) cert);
                        }
                        fis.close();
                    }
                    return certs;
                }
            }
            return certs;
        } catch (IOException | SecurityException | CertificateException e) {
            Log.e(TAG, "Unable to read cert " + e.getMessage());
        }
    }
}
