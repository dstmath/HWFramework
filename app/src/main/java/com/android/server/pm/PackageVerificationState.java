package com.android.server.pm;

import android.util.SparseBooleanArray;
import com.android.server.wm.WindowState;

class PackageVerificationState {
    private final InstallArgs mArgs;
    private boolean mExtendedTimeout;
    private boolean mRequiredVerificationComplete;
    private boolean mRequiredVerificationPassed;
    private final int mRequiredVerifierUid;
    private boolean mSufficientVerificationComplete;
    private boolean mSufficientVerificationPassed;
    private final SparseBooleanArray mSufficientVerifierUids;

    public PackageVerificationState(int requiredVerifierUid, InstallArgs args) {
        this.mRequiredVerifierUid = requiredVerifierUid;
        this.mArgs = args;
        this.mSufficientVerifierUids = new SparseBooleanArray();
        this.mExtendedTimeout = false;
    }

    public InstallArgs getInstallArgs() {
        return this.mArgs;
    }

    public void addSufficientVerifier(int uid) {
        this.mSufficientVerifierUids.put(uid, true);
    }

    public boolean setVerifierResponse(int uid, int code) {
        if (uid == this.mRequiredVerifierUid) {
            this.mRequiredVerificationComplete = true;
            switch (code) {
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    break;
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    this.mSufficientVerifierUids.clear();
                    break;
                default:
                    this.mRequiredVerificationPassed = false;
                    break;
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

    public boolean isVerificationComplete() {
        if (!this.mRequiredVerificationComplete) {
            return false;
        }
        if (this.mSufficientVerifierUids.size() == 0) {
            return true;
        }
        return this.mSufficientVerificationComplete;
    }

    public boolean isInstallAllowed() {
        if (!this.mRequiredVerificationPassed) {
            return false;
        }
        if (this.mSufficientVerificationComplete) {
            return this.mSufficientVerificationPassed;
        }
        return true;
    }

    public void extendTimeout() {
        if (!this.mExtendedTimeout) {
            this.mExtendedTimeout = true;
        }
    }

    public boolean timeoutExtended() {
        return this.mExtendedTimeout;
    }
}
