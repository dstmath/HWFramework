package huawei.android.security.securityprofile;

import android.util.Log;
import java.util.Base64;

public class DigestMatcher {
    private static final String TAG = "SecurityProfileDigestMatcher";

    private DigestMatcher() {
    }

    public static boolean packageMatchesDigest(String apkPath, ApkDigest apkDigest) {
        if (apkPath == null || apkDigest == null || !apkDigest.isValid()) {
            Log.w(TAG, "apkPath or apkDigest is null");
            return false;
        }
        byte[] calculatedDigest = null;
        String apkSignatureScheme = apkDigest.apkSignatureScheme;
        String digestAlgorithm = apkDigest.digestAlgorithm;
        char c = 65535;
        int hashCode = apkSignatureScheme.hashCode();
        if (hashCode != 3708) {
            if (hashCode != 3709) {
                if (hashCode == 1890020211 && apkSignatureScheme.equals("v1_manifest")) {
                    c = 0;
                }
            } else if (apkSignatureScheme.equals("v3")) {
                c = 2;
            }
        } else if (apkSignatureScheme.equals("v2")) {
            c = 1;
        }
        if (c == 0) {
            calculatedDigest = ApkSigningBlockUtils.findV1Digest(apkPath, digestAlgorithm);
        } else if (c == 1) {
            calculatedDigest = ApkSigningBlockUtils.findV2Digest(apkPath, digestAlgorithm);
        } else if (c != 2) {
            Log.e(TAG, "apkSignatureScheme is not supported: " + apkSignatureScheme);
        } else {
            Log.w(TAG, "v3 apkSignatureScheme is not supported!");
            calculatedDigest = ApkSigningBlockUtils.findV3Digest(apkPath, digestAlgorithm);
        }
        if (calculatedDigest == null) {
            return false;
        }
        return apkDigest.base64Digest.equals(Base64.getEncoder().encodeToString(calculatedDigest));
    }
}
