package com.android.server.backup;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackupUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "BackupUtils";

    public static boolean signaturesMatch(ArrayList<byte[]> storedSigHashes, PackageInfo target, PackageManagerInternal pmi) {
        if (target == null || target.packageName == null) {
            return false;
        }
        if ((target.applicationInfo.flags & 1) != 0) {
            return true;
        }
        if (ArrayUtils.isEmpty(storedSigHashes)) {
            return false;
        }
        SigningInfo signingInfo = target.signingInfo;
        if (signingInfo == null) {
            Slog.w(TAG, "signingInfo is empty, app was either unsigned or the flag PackageManager#GET_SIGNING_CERTIFICATES was not specified");
            return false;
        }
        int nStored = storedSigHashes.size();
        if (nStored == 1) {
            return pmi.isDataRestoreSafe(storedSigHashes.get(0), target.packageName);
        }
        ArrayList<byte[]> deviceHashes = hashSignatureArray(signingInfo.getApkContentsSigners());
        int nDevice = deviceHashes.size();
        for (int i = 0; i < nStored; i++) {
            boolean match = false;
            int j = 0;
            while (true) {
                if (j >= nDevice) {
                    break;
                } else if (Arrays.equals(storedSigHashes.get(i), deviceHashes.get(j))) {
                    match = true;
                    break;
                } else {
                    j++;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    public static byte[] hashSignature(byte[] signature) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(signature);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Slog.w(TAG, "No SHA-256 algorithm found!");
            return null;
        }
    }

    public static byte[] hashSignature(Signature signature) {
        return hashSignature(signature.toByteArray());
    }

    public static ArrayList<byte[]> hashSignatureArray(Signature[] sigs) {
        if (sigs == null) {
            return null;
        }
        ArrayList<byte[]> hashes = new ArrayList<>(sigs.length);
        for (Signature s : sigs) {
            hashes.add(hashSignature(s));
        }
        return hashes;
    }

    public static ArrayList<byte[]> hashSignatureArray(List<byte[]> sigs) {
        if (sigs == null) {
            return null;
        }
        ArrayList<byte[]> hashes = new ArrayList<>(sigs.size());
        for (byte[] s : sigs) {
            hashes.add(hashSignature(s));
        }
        return hashes;
    }
}
