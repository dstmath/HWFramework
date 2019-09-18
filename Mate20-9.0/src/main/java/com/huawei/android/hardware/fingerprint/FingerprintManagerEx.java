package com.huawei.android.hardware.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class FingerprintManagerEx {
    private final String TAG = "FingerprintManagerEx";
    private FingerprintManager mFingerprintManager;

    public interface RemovalCallback {
        void onRemovalError(FingerprintEx fingerprintEx, int i, CharSequence charSequence);

        void onRemovalSucceeded(FingerprintEx fingerprintEx);

        void onRemovalSucceeded(FingerprintEx fingerprintEx, int i);
    }

    public FingerprintManagerEx(Context context) {
        if (context != null) {
            this.mFingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            return;
        }
        throw new NullPointerException("The params context cannot be null.");
    }

    public void remove(FingerprintEx fingerprintEx, int userId, final RemovalCallback removalCallback) {
        if (fingerprintEx == null) {
            throw new NullPointerException("The params fingerprintEx cannot be null.");
        } else if (this.mFingerprintManager == null) {
            Log.e("FingerprintManagerEx", "call remove() Error:the service Context.FINGERPRINT_SERVICE is not supported.");
        } else {
            this.mFingerprintManager.remove(fingerprintEx.getFingerprint(), userId, new FingerprintManager.RemovalCallback() {
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
    }

    public void rename(int fpId, int userId, String newName) {
        if (this.mFingerprintManager == null) {
            Log.e("FingerprintManagerEx", "call rename() Error:the service Context.FINGERPRINT_SERVICE is not supported.");
        } else {
            this.mFingerprintManager.rename(fpId, userId, newName);
        }
    }

    public List<FingerprintEx> getEnrolledFingerprints(int userID) {
        if (this.mFingerprintManager == null) {
            Log.e("FingerprintManagerEx", "call getEnrolledFingerprints() Error:the service Context.FINGERPRINT_SERVICE is not supported.");
            return null;
        }
        List<Fingerprint> list = this.mFingerprintManager.getEnrolledFingerprints(userID);
        List<FingerprintEx> result = new ArrayList<>();
        if (list == null || list.size() == 0) {
            return result;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            result.add(new FingerprintEx(list.get(i)));
        }
        return result;
    }

    public long preEnroll() {
        if (this.mFingerprintManager != null) {
            return this.mFingerprintManager.preEnroll();
        }
        Log.e("FingerprintManagerEx", "call preEnroll() Error:the service Context.FINGERPRINT_SERVICE is not supported. return the default value:0");
        return 0;
    }

    public static List<Fingerprint> getEnrolledFingerprints(FingerprintManager fingerprintManager, int userId) {
        return fingerprintManager.getEnrolledFingerprints();
    }
}
