package defpackage;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;

/* renamed from: ap  reason: default package */
public final class ap {
    public static boolean a(X509Certificate x509Certificate, String str, String str2) {
        try {
            return checkSignature(x509Certificate, str.getBytes(StandardCharsets.UTF_8), Base64.decode(str2, 2));
        } catch (IllegalArgumentException e) {
            Log.e("X509CertUtil", " plainText exception: " + e.getMessage());
            return false;
        }
    }

    private static boolean canCertSign(List<X509Certificate> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            X509Certificate x509Certificate = list.get(i);
            if (!(x509Certificate == null ? false : x509Certificate.getBasicConstraints() == -1 ? false : x509Certificate.getKeyUsage()[5])) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkSignature(X509Certificate x509Certificate, byte[] bArr, byte[] bArr2) {
        try {
            Signature instance = Signature.getInstance(x509Certificate.getSigAlgName());
            instance.initVerify(x509Certificate.getPublicKey());
            instance.update(bArr);
            return instance.verify(bArr2);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            Log.e("X509CertUtil", "failed checkSignature : " + e.getMessage());
            return false;
        }
    }

    public static boolean checkSubject(X509Certificate x509Certificate, String str, String str2) {
        if (x509Certificate == null || TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        return str2.equals(getValueByKey(x509Certificate.getSubjectDN().getName(), str));
    }

    private static X509Certificate getCert(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            return getCert(Base64.decode(str, 2));
        } catch (IllegalArgumentException e) {
            Log.e("X509CertUtil", "getCert failed : " + e.getMessage());
            return null;
        }
    }

    public static X509Certificate getCert(byte[] bArr) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bArr));
        } catch (CertificateException e) {
            Log.e("X509CertUtil", "Failed to get cert: " + e.getMessage());
            return null;
        }
    }

    public static List<X509Certificate> getCertChain(List<String> list) {
        ArrayList arrayList = new ArrayList(list.size());
        for (String str : list) {
            arrayList.add(getCert(str));
        }
        return arrayList;
    }

    static List<String> getCertChainByJson(String str) {
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
            Log.e("X509CertUtil", "Failed to getCertChain: " + e.getMessage());
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
        if (list.size() == 0) {
            return false;
        }
        if (x509Certificate == null) {
            Log.e("X509CertUtil", "rootCert is null,verify failed ");
            return false;
        }
        PublicKey publicKey = x509Certificate.getPublicKey();
        for (X509Certificate x509Certificate2 : list) {
            try {
                x509Certificate2.verify(publicKey);
                publicKey = x509Certificate2.getPublicKey();
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | CertificateException e) {
                Log.e("X509CertUtil", "verify failed " + e.getMessage());
                return false;
            }
        }
        return canCertSign(list);
    }
}
