package com.android.server.security.tsmagent.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import java.io.ByteArrayInputStream;
import java.security.AccessControlException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PackageSignatureUtil {
    public static Certificate[] getInstalledAPPCerts(Context context, String packageName) throws CertificateException, NoSuchAlgorithmException, AccessControlException {
        if (packageName == null || packageName.length() == 0) {
            throw new AccessControlException("Package Name not defined");
        }
        try {
            PackageInfo foundPkgInfo = context.getPackageManager().getPackageInfo(packageName, 64);
            if (foundPkgInfo == null) {
                throw new AccessControlException("Package does not exist");
            }
            ArrayList<Certificate> appCerts = new ArrayList();
            for (Signature signature : foundPkgInfo.signatures) {
                appCerts.add(decodeCertificate(signature.toByteArray()));
            }
            return (Certificate[]) appCerts.toArray(new Certificate[appCerts.size()]);
        } catch (NameNotFoundException e) {
            throw new AccessControlException("Package does not exist");
        }
    }

    public static byte[] getAppCertHash(Certificate appCert) throws CertificateEncodingException {
        MessageDigest md = null;
        int i = 0;
        while (i < 10) {
            try {
                md = MessageDigest.getInstance("SHA");
                break;
            } catch (NoSuchAlgorithmException e) {
                HwLog.e("getAppCertHash NoSuchAlgorithmException");
                i++;
            }
        }
        if (md == null) {
            throw new AccessControlException("Hash can not be computed");
        }
        HwLog.i("Application Cert Hash Value: " + byteArrayToHex(md.digest(appCert.getEncoded())));
        return md.digest(appCert.getEncoded());
    }

    public static List<String> getInstalledAppHashList(Context context, String targetPackageName) throws AccessControlException, CertificateException, NoSuchAlgorithmException {
        List<String> hashesList = null;
        Certificate[] certificates = getInstalledAPPCerts(context, targetPackageName);
        if (certificates != null && certificates.length > 0) {
            HwLog.d("travel:accessControl fetch certificates array of caller successful ");
            hashesList = new ArrayList();
            for (Certificate certificate : certificates) {
                hashesList.add(byteArrayToHex(getAppCertHash(certificate)));
            }
        }
        return hashesList;
    }

    public static Certificate decodeCertificate(byte[] certData) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certData));
    }

    public static String byteArrayToHex(byte[] data) {
        if (data == null) {
            return null;
        }
        int len = data.length;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            if ((data[i] & 255) < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(data[i] & 255));
        }
        return sb.toString().toUpperCase(Locale.getDefault());
    }
}
