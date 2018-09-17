package android.util;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PackageUtils {
    private PackageUtils() {
    }

    public static String computePackageCertSha256Digest(PackageManager packageManager, String packageName, int userId) {
        try {
            return computeCertSha256Digest(packageManager.getPackageInfoAsUser(packageName, 64, userId).signatures[0]);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static String computeCertSha256Digest(Signature signature) {
        return computeSha256Digest(signature.toByteArray());
    }

    public static String computeSha256Digest(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            messageDigest.update(data);
            return ByteStringUtils.toHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
