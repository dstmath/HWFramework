package com.android.server.security.securityprofile;

import android.util.Slog;
import huawei.android.security.securityprofile.ApkDigest;
import huawei.android.security.securityprofile.DigestMatcher;
import huawei.android.security.securityprofile.PolicyExtractor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

public class PolicyVerifier {
    private static final String HUAWEI_APK_PRODUCTION = "Huawei APK Production";
    private static final String TAG = "SecurityProfileService";

    public static boolean packageHasValidPolicy(String packageName, String apkPath) {
        if (apkPath == null) {
            return false;
        }
        try {
            byte[] jws = PolicyExtractor.getPolicy(apkPath);
            ApkDigest apkDigest = PolicyExtractor.getDigest(packageName, jws);
            if (verifyAndDecodePolicy(jws) == null) {
                Slog.e(TAG, "Policy verification failed");
                return false;
            } else if (DigestMatcher.packageMatchesDigest(apkPath, apkDigest)) {
                return true;
            } else {
                Slog.e(TAG, "Package digest did not match policy digest");
                return false;
            }
        } catch (PolicyExtractor.PolicyNotFoundException e) {
            Slog.w(TAG, "Policy block not found:" + e.getMessage() + ",apkPath:" + apkPath);
            return false;
        } catch (IOException e2) {
            Slog.e(TAG, "packageHasValidPolicy IOException:" + e2.getMessage());
            return false;
        } catch (Exception e3) {
            Slog.e(TAG, "packageHasValidPolicy Exception:" + e3.getMessage());
            return false;
        }
    }

    public static JSONObject verifyAndDecodePolicy(byte[] policyBlock) {
        try {
            try {
                String[] parts = new String(policyBlock, StandardCharsets.UTF_8).split("\\.");
                if (parts.length != 3) {
                    return null;
                }
                byte[] signedData = (parts[0] + "." + parts[1]).getBytes();
                byte[] signature = Base64.getUrlDecoder().decode(parts[2]);
                JSONObject header = new JSONObject(new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode((String) header.getJSONArray("x5c").get(0)));
                Date timestamp = new Date();
                timestamp.setTime(header.optLong("timestamp", System.currentTimeMillis()));
                Signature sig = Signature.getInstance("SHA256withRSA");
                Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(bis);
                if (!new CertificateVerifier().verifyCertificateChain(Arrays.asList(new Certificate[]{cert}), timestamp)) {
                    return null;
                }
                String valueofou = getSubjectAttr(cert, "OU");
                if (!HUAWEI_APK_PRODUCTION.equals(valueofou)) {
                    Slog.e(TAG, "the OU field:" + valueofou + " of the subject is not correct");
                    return null;
                }
                sig.initVerify(cert);
                sig.update(signedData);
                if (!sig.verify(signature)) {
                    return null;
                }
                return new JSONObject(new String(Base64.getUrlDecoder().decode(parts[1].getBytes())));
            } catch (NoSuchAlgorithmException e) {
                e = e;
                Slog.e(TAG, e.getMessage());
                return null;
            } catch (CertificateException e2) {
                e = e2;
                Slog.e(TAG, e.getMessage());
                return null;
            } catch (JSONException e3) {
                e = e3;
                Slog.e(TAG, e.getMessage());
                return null;
            } catch (SignatureException e4) {
                e = e4;
                Slog.e(TAG, e.getMessage());
                return null;
            } catch (InvalidKeyException e5) {
                e = e5;
                Slog.e(TAG, e.getMessage());
                return null;
            }
        } catch (NoSuchAlgorithmException e6) {
            e = e6;
            byte[] bArr = policyBlock;
            Slog.e(TAG, e.getMessage());
            return null;
        } catch (CertificateException e7) {
            e = e7;
            byte[] bArr2 = policyBlock;
            Slog.e(TAG, e.getMessage());
            return null;
        } catch (JSONException e8) {
            e = e8;
            byte[] bArr3 = policyBlock;
            Slog.e(TAG, e.getMessage());
            return null;
        } catch (SignatureException e9) {
            e = e9;
            byte[] bArr4 = policyBlock;
            Slog.e(TAG, e.getMessage());
            return null;
        } catch (InvalidKeyException e10) {
            e = e10;
            byte[] bArr5 = policyBlock;
            Slog.e(TAG, e.getMessage());
            return null;
        }
    }

    public static String getSubjectAttr(Certificate cert, String field) {
        for (String attr : ((X509Certificate) cert).getSubjectDN().getName().split(",")) {
            String[] nameandvalue = attr.split("=");
            if (nameandvalue[0].equals(field)) {
                return nameandvalue[1];
            }
        }
        return null;
    }
}
