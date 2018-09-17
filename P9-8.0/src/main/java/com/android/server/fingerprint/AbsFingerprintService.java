package com.android.server.fingerprint;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.SystemService;

public abstract class AbsFingerprintService extends SystemService {
    public AbsFingerprintService(Context context) {
        super(context);
    }

    public void updateFingerprints(int userId) {
    }

    public boolean shouldAuthBothSpaceFingerprints(String opPackageName, int flags) {
        return true;
    }

    public int removeUserData(int groupId, byte[] path) {
        return 0;
    }

    public boolean onHwTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }

    public boolean checkPrivacySpaceEnroll(int userId, int currentUserId) {
        return false;
    }

    public boolean checkNeedPowerpush() {
        return false;
    }

    protected void stopPickupTrunOff() {
    }

    protected int setKidsFingerprint(int userID, boolean isKeyguard) {
        return 0;
    }
}
