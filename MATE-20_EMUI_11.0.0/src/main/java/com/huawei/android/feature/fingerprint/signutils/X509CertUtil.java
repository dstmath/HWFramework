package com.huawei.android.feature.fingerprint.signutils;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;

public class X509CertUtil {
    private static final String CERT_TYPE = "X.509";
    private static final String KEY_CN = "CN";
    private static final String KEY_OU = "OU";
    private static final int POS_CERT_SIGN = 5;
    private static final String ROOT_BASE64 = "MIIFZDCCA0ygAwIBAgIIYsLLTehAXpYwDQYJKoZIhvcNAQELBQAwUDELMAkGA1UEBhMCQ04xDzANBgNVBAoMBkh1YXdlaTETMBEGA1UECwwKSHVhd2VpIENCRzEbMBkGA1UEAwwSSHVhd2VpIENCRyBSb290IENBMB4XDTE3MDgyMTEwNTYyN1oXDTQyMDgxNTEwNTYyN1owUDELMAkGA1UEBhMCQ04xDzANBgNVBAoMBkh1YXdlaTETMBEGA1UECwwKSHVhd2VpIENCRzEbMBkGA1UEAwwSSHVhd2VpIENCRyBSb290IENBMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1OyKm3Ig/6eibB7Uz2o93UqGk2M784WdfF8mvffvu218d61G5M3Px54E3kefUTk5Ky1ywHvw7Rp9KDuYv7ktaHkk+yr59Ihseu3a7iM/C6SnMSGt+LfB/Bcob9Abw95EigXQ4yQddX9hbNrin3AwZw8wMjEISYYDo5GuYDL0NbAiYg2Y5GpfYIqRzoi6GqDz+evLrsl20kJeCEPgJZN4Jg00Iq9k++EKOZ5Jc/Zx22ZUgKpdwKABkvzshEgG6WWUPB+gosOiLv++inu/9blDpEzQZhjZ9WVHpURHDK1YlCvubVAMhDpnbqNHZ0AxlPletdoyugrH/OLKl5inhMXNj3Re7Hl8WsBWLUKp6sXFf0dvSFzqnr2jkhicS+K2IYZnjghC9cOBRO8fnkonh0EBt0evjUIKr5ClbCKioBX8JU+d4ldtWOpp2FlxeFTLreDJ5ZBU4//bQpTwYMt7gwMK+MO5WtokUx3UF98Z6GdUgbl6nBjBe82c7oIQXhHGHPnURQO7DDPgyVnNOnTPIkmiHJh/e3vkVhiZNHFCCLTip6GoJVrLxwb9i4q+d0thw4doxVJ5NB9OfDMV64/ybJgpf7m3Ld2yE0gsf1prrRlDFDXjlYyqqpf1l9Y0u3ctXo7UpXMgbyDEpUQhq3a7txZQO/17luTDoA6Tz1ADavvBwHkCAwEAAaNCMEAwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFKrE03lH6G4ja+/wqWwicz16GWmhMA0GCSqGSIb3DQEBCwUAA4ICAQC1d3TMB+VHZdGrWJbfaBShFNiCTN/MceSHOpzBn6JumQP4N7mxCOwdRSsGKQxV2NPH7LTXWNhUvUw5Sek96FWx/+Oa7jsj3WNAVtmS3zKpCQ5iGb08WIROcFnx3oUQ5rcO8r/lUk7Q2cN0E+rF4xsdQrH9k2cd3kAXZXBjfxfKPJTdPy1XnZR/h8H5EwEK5DWjSzK1wKd3G/Fxdm3E23pcr4FZgdYdOlFSiqW2TJ3Qe6lF4GOKOOydWHkpu54ieTsqoYcuMKnKMjT2SLNNgv9Gu5ipaG8Olz6g9C7Htp943lmK/1VtnhggpL3rDTsFX/+ehk7OtxuNzRMD9lXUtEfok7f8XB0dcL4ZjnEhDmp5QZqC1kMubHQtQnTauEiv0YkSGOwJAUZpK1PIff5GgxXYfaHfBC6Op4q02ppl5Q3URl7XIjYLjvs9t4S9xPe8tb6416V2fe1dZ62vOXMMKHkZjVihh+IceYpJYHuyfKoYJyahLOQXZykGK5iPAEEtq3HPfMVF43RKHOwfhrAH5KwelUA/0EkcR4Gzth1MKEqojdnYNemkkSy7aNPPT4LEm5R7sV6vG1CjwbgvQrWCgc4nMb8ngdfnVF7Ydqjqi9SAqUzIk4+Uf0ZY+6RY5IcHdCaiPaWIE1xURQ8B0DRUURsQwXdjZhgLN/DKJpCl5aCCxg==";
    private static final String TAG = "X509CertUtil";

    public static boolean canCertSign(X509Certificate x509Certificate) {
        if (x509Certificate == null || x509Certificate.getBasicConstraints() == -1) {
            return false;
        }
        return x509Certificate.getKeyUsage()[5];
    }

    public static boolean canCertSign(List<X509Certificate> list) {
        for (int i = 1; i < list.size(); i++) {
            if (!canCertSign(list.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkSignature(X509Certificate x509Certificate, byte[] bArr, byte[] bArr2) {
        if (x509Certificate == null || bArr == null || bArr2 == null) {
            Log.w(TAG, "checkSignature parameter is null");
            return false;
        }
        try {
            Signature instance = Signature.getInstance(x509Certificate.getSigAlgName());
            instance.initVerify(x509Certificate.getPublicKey());
            instance.update(bArr);
            return instance.verify(bArr2);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            Log.e(TAG, "failed checkSignature,Exception:", e);
            return false;
        }
    }

    public static boolean checkSubject(X509Certificate x509Certificate, String str, String str2) {
        if (x509Certificate == null || TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        return str2.equals(getValueByKey(x509Certificate.getSubjectDN().getName(), str));
    }

    public static boolean checkSubjectCN(X509Certificate x509Certificate, String str) {
        return checkSubject(x509Certificate, KEY_CN, str);
    }

    public static boolean checkSubjectOU(X509Certificate x509Certificate, String str) {
        return checkSubject(x509Certificate, KEY_OU, str);
    }

    public static X509Certificate getCBGRootCA() {
        return getCert(Base64.decode(ROOT_BASE64, 2));
    }

    public static X509Certificate getCert(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return getCert(Base64.decode(str));
    }

    public static X509Certificate getCert(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        try {
            Certificate generateCertificate = CertificateFactory.getInstance(CERT_TYPE).generateCertificate(new ByteArrayInputStream(bArr));
            if (generateCertificate instanceof X509Certificate) {
                return (X509Certificate) generateCertificate;
            }
        } catch (CertificateException e) {
            Log.e(TAG, "Failed to get cert,CertificateException:", e);
        }
        return null;
    }

    public static List<X509Certificate> getCertChain(String str) {
        return getCertChain(getCertChainByJson(str));
    }

    public static List<X509Certificate> getCertChain(List<String> list) {
        if (list == null) {
            Log.w(TAG, "base64 CertChain is null.");
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (String str : list) {
            X509Certificate cert = getCert(str);
            if (cert == null) {
                Log.e(TAG, "Failed to get cert from CertChain");
            } else {
                arrayList.add(cert);
            }
        }
        return arrayList;
    }

    private static List<String> getCertChainByJson(String str) {
        try {
            JSONArray jSONArray = new JSONArray(str);
            if (jSONArray.length() <= 1) {
                return Collections.emptyList();
            }
            ArrayList arrayList = new ArrayList(jSONArray.length());
            for (int i = 0; i < jSONArray.length(); i++) {
                arrayList.add(jSONArray.getString(i));
            }
            return arrayList;
        } catch (JSONException e) {
            Log.e(TAG, "Failed to getCertChain,JSONException:", e);
            return Collections.emptyList();
        }
    }

    private static String getValueByKey(String str, String str2) {
        int indexOf = str.toUpperCase(Locale.getDefault()).indexOf(str2 + "=");
        if (indexOf == -1) {
            return null;
        }
        int indexOf2 = str.indexOf(",", indexOf);
        return indexOf2 != -1 ? str.substring(indexOf + str2.length() + 1, indexOf2) : str.substring(indexOf + str2.length() + 1);
    }

    public static boolean verifyCertChain(X509Certificate x509Certificate, List<X509Certificate> list) {
        if (list.size() == 0 || x509Certificate == null) {
            return false;
        }
        try {
            x509Certificate.checkValidity();
            PublicKey publicKey = x509Certificate.getPublicKey();
            for (int size = list.size() - 1; size >= 0; size--) {
                try {
                    X509Certificate x509Certificate2 = list.get(size);
                    x509Certificate2.verify(publicKey);
                    x509Certificate2.checkValidity();
                    publicKey = x509Certificate2.getPublicKey();
                } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | CertificateException e) {
                    Log.e(TAG, "verify failed,Exception:", e);
                    return false;
                }
            }
            return canCertSign(list);
        } catch (CertificateExpiredException | CertificateNotYetValidException e2) {
            Log.e(TAG, "verifyCertChain Exception:", e2);
            return false;
        }
    }
}
