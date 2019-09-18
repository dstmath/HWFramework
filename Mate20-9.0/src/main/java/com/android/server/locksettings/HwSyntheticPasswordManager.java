package com.android.server.locksettings;

import android.content.Context;
import android.os.RemoteException;
import android.os.UserManager;
import android.service.gatekeeper.IGateKeeperService;
import android.util.Slog;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.locksettings.SyntheticPasswordManager;

public class HwSyntheticPasswordManager extends SyntheticPasswordManager {
    private static final String TAG = "HwLSS-SPM";

    public HwSyntheticPasswordManager(Context context, LockSettingsStorage storage, UserManager userManager) {
        super(context, storage, userManager);
    }

    /* access modifiers changed from: protected */
    public void destroySPBlobKey(String keyAlias) {
        HwSyntheticPasswordManager.super.destroySPBlobKey(keyAlias);
        flog(TAG, "destroySPBlobKey " + keyAlias);
    }

    /* access modifiers changed from: protected */
    public void saveState(String stateName, byte[] data, long handle, int userId) {
        HwSyntheticPasswordManager.super.saveState(stateName, data, handle, userId);
        flog(TAG, "saveState U" + userId + "  " + stateName + " " + Long.toHexString(handle));
    }

    public VerifyCredentialResponse verifyChallenge(IGateKeeperService gatekeeper, SyntheticPasswordManager.AuthenticationToken auth, long challenge, int userId) throws RemoteException {
        VerifyCredentialResponse r = HwSyntheticPasswordManager.super.verifyChallenge(gatekeeper, auth, challenge, userId);
        StringBuilder sb = new StringBuilder();
        sb.append("verifyChallenge U");
        sb.append(userId);
        sb.append(" R:");
        sb.append(r == null ? "NUL" : Integer.toString(r.getResponseCode()));
        flog(TAG, sb.toString());
        return r;
    }

    /* access modifiers changed from: protected */
    public void destroyState(String stateName, long handle, int userId) {
        HwSyntheticPasswordManager.super.destroyState(stateName, handle, userId);
        flog(TAG, "destroyState U" + userId + "  " + stateName + " " + Long.toHexString(handle));
    }

    /* access modifiers changed from: package-private */
    public void errorLog(String tag, String msg) {
        Slog.e(tag, msg);
        this.mStorage.flog(tag, msg);
    }

    /* access modifiers changed from: package-private */
    public void warnLog(String tag, String msg) {
        Slog.w(tag, msg);
        this.mStorage.flog(tag, msg);
    }

    /* access modifiers changed from: package-private */
    public void flog(String tag, String msg) {
        this.mStorage.flog(tag, msg);
    }
}
