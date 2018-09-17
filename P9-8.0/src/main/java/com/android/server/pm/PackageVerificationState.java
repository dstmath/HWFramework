package com.android.server.pm;

import android.util.SparseBooleanArray;

class PackageVerificationState {
    private final InstallArgs mArgs;
    private boolean mExtendedTimeout = false;
    private boolean mRequiredVerificationComplete;
    private boolean mRequiredVerificationPassed;
    private final int mRequiredVerifierUid;
    private boolean mSufficientVerificationComplete;
    private boolean mSufficientVerificationPassed;
    private final SparseBooleanArray mSufficientVerifierUids = new SparseBooleanArray();

    public PackageVerificationState(int requiredVerifierUid, InstallArgs args) {
        this.mRequiredVerifierUid = requiredVerifierUid;
        this.mArgs = args;
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
                case 1:
                    break;
                case 2:
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
