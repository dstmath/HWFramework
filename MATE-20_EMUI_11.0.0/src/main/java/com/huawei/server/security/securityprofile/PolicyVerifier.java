package com.huawei.server.security.securityprofile;

import android.support.annotation.Nullable;
import android.util.Log;
import huawei.android.security.securityprofile.ApkDigest;
import huawei.android.security.securityprofile.DigestMatcher;
import huawei.android.security.securityprofile.PolicyExtractor;
import java.io.ByteArrayInputStream;
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

public final class PolicyVerifier {
    private static final boolean DEBUG = SecurityProfileUtils.DEBUG;
    private static final String HUAWEI_APK_PRODUCTION = "Huawei APK Production";
    private static final int JWS_HEADER = 0;
    private static final int JWS_PARTS_LENGTH = 3;
    private static final int JWS_PAYLOAD = 1;
    private static final int JWS_SIGNATURE = 2;
    private static final String TAG = "SecurityProfileService";

    static boolean packageHasValidPolicy(String packageName, String apkPath) {
        try {
            getValidPolicyFromApkPath(packageName, apkPath);
            return true;
        } catch (PolicyExtractor.PolicyNotFoundException e) {
            Log.w(TAG, "Policy not found: " + e.getMessage());
            return false;
        } catch (PolicyVerifyFailedException e2) {
            Log.w(TAG, "Policy verified failed: " + e2.getMessage());
            return false;
        } catch (Exception e3) {
            Log.e(TAG, "Failed to get policy from Apk!");
            return false;
        }
    }

    static JSONObject getValidPolicyFromApkPath(String packageName, String apkPath) throws PolicyVerifyFailedException, PolicyExtractor.PolicyNotFoundException {
        if (packageName == null && apkPath == null) {
            throw new PolicyVerifyFailedException("invalid params");
        }
        byte[] jws = PolicyExtractor.getPolicyBlock(apkPath);
        ApkDigest apkDigest = PolicyExtractor.getApkDigestFromPolicyBlock(packageName, jws);
        JSONObject policy = verifyAndDecodePolicy(jws);
        if (DigestMatcher.packageMatchesDigest(apkPath, apkDigest)) {
            return policy;
        }
        throw new PolicyVerifyFailedException("Package's digest did not match policy digest");
    }

    static JSONObject verifyAndDecodePolicy(byte[] policyBlock) throws PolicyVerifyFailedException {
        Exception e;
        IllegalArgumentException e2;
        try {
            try {
                String[] parts = new String(policyBlock, StandardCharsets.UTF_8).split("\\.");
                if (parts.length == 3) {
                    byte[] signedData = (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8);
                    byte[] signature = Base64.getUrlDecoder().decode(parts[2]);
                    JSONObject header = new JSONObject(new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
                    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode((String) header.getJSONArray("x5c").get(0)));
                    Date timestamp = new Date();
                    timestamp.setTime(header.optLong("timestamp", System.currentTimeMillis()));
                    Signature sig = Signature.getInstance("SHA256withRSA");
                    Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(bis);
                    if (new CertificateVerifier().verifyCertificateChain(Arrays.asList(cert), timestamp)) {
                        String valueOfOU = getSubjectAttr(cert, "OU");
                        if (HUAWEI_APK_PRODUCTION.equals(valueOfOU)) {
                            sig.initVerify(cert);
                            sig.update(signedData);
                            if (sig.verify(signature)) {
                                return new JSONObject(new String(Base64.getUrlDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
                            }
                            throw new PolicyVerifyFailedException("verify signature instance failed!");
                        }
                        throw new PolicyVerifyFailedException("the OU field: " + valueOfOU + " of the subject is not correct.");
                    }
                    throw new PolicyVerifyFailedException("verify CertificateChain failed!");
                }
                throw new PolicyVerifyFailedException("verifyAndDecodePolicy policy not in excepted format.");
            } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | CertificateException | JSONException e3) {
                e = e3;
                throw new PolicyVerifyFailedException("signature verify failed by exception: " + e.getMessage());
            } catch (IllegalArgumentException e4) {
                e2 = e4;
                throw new PolicyVerifyFailedException("Failed to verify policy, parts not in Base64 scheme.");
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | CertificateException | JSONException e5) {
            e = e5;
            throw new PolicyVerifyFailedException("signature verify failed by exception: " + e.getMessage());
        } catch (IllegalArgumentException e6) {
            e2 = e6;
            throw new PolicyVerifyFailedException("Failed to verify policy, parts not in Base64 scheme.");
        }
    }

    @Nullable
    private static String getSubjectAttr(Certificate cert, String field) {
        if (!(cert instanceof X509Certificate)) {
            return null;
        }
        for (String attr : ((X509Certificate) cert).getSubjectDN().getName().split(",")) {
            String[] nameAndValue = attr.split("=");
            if (nameAndValue[0].equals(field)) {
                return nameAndValue[1];
            }
        }
        return null;
    }

    public static class PolicyVerifyFailedException extends Exception {
        private static final long serialVersionUID = 1;

        public PolicyVerifyFailedException(String message) {
            super(message);
        }

        public PolicyVerifyFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
