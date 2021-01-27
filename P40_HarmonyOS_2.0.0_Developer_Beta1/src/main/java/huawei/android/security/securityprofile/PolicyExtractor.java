package huawei.android.security.securityprofile;

import com.huawei.hwpartsecurity.BuildConfig;
import com.huawei.security.keystore.HwKeyProperties;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class PolicyExtractor {
    private static final int JWS_PARTS_LENGTH = 3;
    private static final int JWS_PAYLOAD = 1;
    private static final String TAG = "SecurityProfilePolicyExtractor";

    private PolicyExtractor() {
    }

    public static ApkDigest getApkDigestFromPolicyBlock(String packageName, byte[] policyBlock) throws PolicyNotFoundException {
        if (packageName == null || policyBlock == null) {
            throw new PolicyNotFoundException("getDigest illegal args, package name or policyBlock is null");
        }
        String[] parts = new String(policyBlock, StandardCharsets.UTF_8).split("\\.");
        if (parts.length == 3) {
            try {
                JSONObject apkDigest = new JSONObject(new String(Base64.getUrlDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)).getJSONObject("domains").getJSONArray(packageName).getJSONObject(0).getJSONObject("apk_digest");
                return new ApkDigest(apkDigest.optString("signature_scheme", "v2"), apkDigest.optString("digest_algorithm", HwKeyProperties.DIGEST_SHA256), apkDigest.optString("digest", BuildConfig.FLAVOR));
            } catch (JSONException e) {
                throw new PolicyNotFoundException("getPolicy err for JSONException: " + e.getMessage(), e);
            } catch (IllegalArgumentException e2) {
                throw new PolicyNotFoundException("Policy payload not in valid Base64 scheme");
            }
        } else {
            throw new PolicyNotFoundException("policy not in excepted format");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        throw r2;
     */
    public static byte[] getPolicyBlock(String apkFile) throws PolicyNotFoundException {
        if (apkFile != null) {
            try {
                RandomAccessFile apk = new RandomAccessFile(apkFile, "r");
                ByteBuffer policyBlockBuffer = ApkSigningBlockUtils.findHwSignature(apk).signatureBlockContent;
                byte[] policyBlock = new byte[policyBlockBuffer.remaining()];
                policyBlockBuffer.get(policyBlock);
                apk.close();
                return policyBlock;
            } catch (SignatureInvalidException e) {
                throw new PolicyNotFoundException("getPolicy err for SignatureInvalidException", e);
            } catch (FileNotFoundException e2) {
                throw new PolicyNotFoundException("getPolicy invalid file");
            } catch (IOException e3) {
                throw new PolicyNotFoundException("getPolicy err for IOException", e3);
            }
        } else {
            throw new PolicyNotFoundException("getPolicy err for apkFile is null");
        }
    }

    public static class PolicyNotFoundException extends Exception {
        private static final long serialVersionUID = 1;

        public PolicyNotFoundException(String message) {
            super(message);
        }

        public PolicyNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
