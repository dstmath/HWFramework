package android.util;

import android.content.pm.Signature;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class PackageUtils {
    private PackageUtils() {
    }

    public static String[] computeSignaturesSha256Digests(Signature[] signatures) {
        int signatureCount = signatures.length;
        String[] digests = new String[signatureCount];
        for (int i = 0; i < signatureCount; i++) {
            digests[i] = computeSha256Digest(signatures[i].toByteArray());
        }
        return digests;
    }

    public static String computeSignaturesSha256Digest(Signature[] signatures) {
        if (signatures.length == 1) {
            return computeSha256Digest(signatures[0].toByteArray());
        }
        return computeSignaturesSha256Digest(computeSignaturesSha256Digests(signatures));
    }

    public static String computeSignaturesSha256Digest(String[] sha256Digests) {
        if (sha256Digests.length == 1) {
            return sha256Digests[0];
        }
        Arrays.sort(sha256Digests);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (String sha256Digest : sha256Digests) {
            try {
                bytes.write(sha256Digest.getBytes());
            } catch (IOException e) {
            }
        }
        return computeSha256Digest(bytes.toByteArray());
    }

    public static byte[] computeSha256DigestBytes(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            messageDigest.update(data);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String computeSha256Digest(byte[] data) {
        return ByteStringUtils.toHexString(computeSha256DigestBytes(data));
    }
}
