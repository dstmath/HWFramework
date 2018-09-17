package com.google.android.maps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class KeyHelper {
    private KeyHelper() {
    }

    static String getSignatureFingerprint(PackageManager pm, String packageName) {
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            if (ai == null) {
                return null;
            }
            if ((ai.flags & 1) != 0) {
                return "SYSTEM";
            }
            PackageInfo pi = pm.getPackageInfo(packageName, 64);
            if (pi == null || pi.signatures == null || pi.signatures.length == 0 || pi.signatures[0] == null) {
                return null;
            }
            byte[] signature = pi.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (md == null) {
                return null;
            }
            byte[] digest = md.digest(signature);
            if (digest == null) {
                return null;
            }
            return toHex(digest);
        } catch (NameNotFoundException e) {
            return null;
        } catch (NoSuchAlgorithmException e2) {
            return null;
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return sb.toString();
    }
}
