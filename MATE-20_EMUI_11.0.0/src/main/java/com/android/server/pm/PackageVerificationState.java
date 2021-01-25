package com.android.server.pm;

import android.util.SparseBooleanArray;
import com.android.server.pm.PackageManagerService;

/* access modifiers changed from: package-private */
public class PackageVerificationState {
    private boolean mExtendedTimeout = false;
    private final PackageManagerService.InstallParams mParams;
    private boolean mRequiredVerificationComplete;
    private boolean mRequiredVerificationPassed;
    private final int mRequiredVerifierUid;
    private boolean mSufficientVerificationComplete;
    private boolean mSufficientVerificationPassed;
    private final SparseBooleanArray mSufficientVerifierUids = new SparseBooleanArray();

    PackageVerificationState(int requiredVerifierUid, PackageManagerService.InstallParams params) {
        this.mRequiredVerifierUid = requiredVerifierUid;
        this.mParams = params;
    }

    /* access modifiers changed from: package-private */
    public PackageManagerService.InstallParams getInstallParams() {
        return this.mParams;
    }

    /* access modifiers changed from: package-private */
    public void addSufficientVerifier(int uid) {
        this.mSufficientVerifierUids.put(uid, true);
    }

    /* access modifiers changed from: package-private */
    public boolean setVerifierResponse(int uid, int code) {
        if (uid == this.mRequiredVerifierUid) {
            this.mRequiredVerificationComplete = true;
            if (code != 1) {
                if (code != 2) {
                    this.mRequiredVerificationPassed = false;
                    return true;
                }
                this.mSufficientVerifierUids.clear();
            }
            this.mRequiredVerificationPassed = true;
            return true;
        } else if (!this.mSufficientVerifierUids.get(uid)) {
            return false;
        } else {
            if (code == 1) {
                this.mSufficientVerificationComplete = true;
                this.mSufficientVerificationPassed = true;
            }
            this.mSufficientVerifierUids.delete(uid);
            if (this.mSufficientVerifierUids.size() == 0) {
                this.mSufficientVerificationComplete = true;
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isVerificationComplete() {
        if (!this.mRequiredVerificationComplete) {
            return false;
        }
        if (this.mSufficientVerifierUids.size() == 0) {
            return true;
        }
        return this.mSufficientVerificationComplete;
    }

    /* access modifiers changed from: package-private */
    public boolean isInstallAllowed() {
        if (!this.mRequiredVerificationPassed) {
            return false;
        }
        if (this.mSufficientVerificationComplete) {
            return this.mSufficientVerificationPassed;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void extendTimeout() {
        if (!this.mExtendedTimeout) {
            this.mExtendedTimeout = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean timeoutExtended() {
        return this.mExtendedTimeout;
    }
}
