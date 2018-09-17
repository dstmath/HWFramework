package android.net.wifi;

import android.os.Parcel;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class ParcelUtil {
    public static void writePrivateKey(Parcel dest, PrivateKey key) {
        if (key == null) {
            dest.writeString(null);
            return;
        }
        dest.writeString(key.getAlgorithm());
        dest.writeByteArray(key.getEncoded());
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x001a A:{ExcHandler: java.security.NoSuchAlgorithmException (e java.security.NoSuchAlgorithmException), Splitter: B:4:0x000c} */
    /* JADX WARNING: Missing block: B:8:0x001b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static PrivateKey readPrivateKey(Parcel in) {
        String algorithm = in.readString();
        if (algorithm == null) {
            return null;
        }
        try {
            return KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(in.createByteArray()));
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public static void writeCertificate(Parcel dest, X509Certificate cert) {
        byte[] certBytes = null;
        if (cert != null) {
            try {
                certBytes = cert.getEncoded();
            } catch (CertificateEncodingException e) {
                Log.e("writeCertificate fail", e.getMessage());
            }
        }
        dest.writeByteArray(certBytes);
    }

    public static X509Certificate readCertificate(Parcel in) {
        byte[] certBytes = in.createByteArray();
        if (certBytes == null) {
            return null;
        }
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBytes));
        } catch (CertificateException e) {
            return null;
        }
    }

    public static void writeCertificates(Parcel dest, X509Certificate[] certs) {
        if (certs == null || certs.length == 0) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(certs.length);
        for (X509Certificate writeCertificate : certs) {
            writeCertificate(dest, writeCertificate);
        }
    }

    public static X509Certificate[] readCertificates(Parcel in) {
        int length = in.readInt();
        if (length == 0) {
            return null;
        }
        X509Certificate[] certs = new X509Certificate[length];
        for (int i = 0; i < length; i++) {
            certs[i] = readCertificate(in);
        }
        return certs;
    }

    public static String safeDisplayMac(String srcBssid) {
        if (srcBssid == null) {
            return "null";
        }
        int len = srcBssid.length();
        if (len < 12) {
            return "Can not display bssid";
        }
        return srcBssid.substring(0, 6) + "**:**" + srcBssid.substring(len - 6, len);
    }
}
