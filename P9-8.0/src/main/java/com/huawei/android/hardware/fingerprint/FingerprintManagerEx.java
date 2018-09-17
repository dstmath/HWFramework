package com.huawei.android.hardware.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import java.util.ArrayList;
import java.util.List;

public class FingerprintManagerEx {
    private FingerprintManager mFingerprintManager;

    public interface RemovalCallback {
        void onRemovalError(FingerprintEx fingerprintEx, int i, CharSequence charSequence);

        void onRemovalSucceeded(FingerprintEx fingerprintEx);

        void onRemovalSucceeded(FingerprintEx fingerprintEx, int i);
    }

    public FingerprintManagerEx(Context context) {
        if (context == null) {
            throw new NullPointerException("The params context cannot be null.");
        }
        this.mFingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
    }

    public void remove(FingerprintEx fingerprintEx, int userId, final RemovalCallback removalCallback) {
        if (fingerprintEx == null) {
            throw new NullPointerException("The params fingerprintEx cannot be null.");
        }
        this.mFingerprintManager.remove(fingerprintEx.getFingerprint(), userId, new android.hardware.fingerprint.FingerprintManager.RemovalCallback() {
            public void onRemovalSucceeded(Fingerprint fingerprint, int remaining) {
                if (removalCallback != null) {
                    removalCallback.onRemovalSucceeded(new FingerprintEx(fingerprint), remaining);
                }
            }

            public void onRemovalSucceeded(Fingerprint fingerprint) {
                if (removalCallback != null) {
                    removalCallback.onRemovalSucceeded(new FingerprintEx(fingerprint));
                }
            }

            public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
                if (removalCallback != null) {
                    removalCallback.onRemovalError(new FingerprintEx(fp), errMsgId, errString);
                }
            }
        });
    }

    public void rename(int fpId, int userId, String newName) {
        this.mFingerprintManager.rename(fpId, userId, newName);
    }

    public List<FingerprintEx> getEnrolledFingerprints(int userID) {
        List<Fingerprint> list = this.mFingerprintManager.getEnrolledFingerprints(userID);
        List<FingerprintEx> result = new ArrayList();
        if (list == null || list.size() == 0) {
            return result;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            result.add(new FingerprintEx((Fingerprint) list.get(i)));
        }
        return result;
    }

    public long preEnroll() {
        return this.mFingerprintManager.preEnroll();
    }
}
