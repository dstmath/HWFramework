package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.util.Log;
import com.android.server.biometrics.fingerprint.FingerprintUserState;
import java.util.List;

public class FingerprintsUserStateEx {
    private FingerprintUserState mDefaultUserState;
    private FingerprintUserState mUDUserState;

    public FingerprintsUserStateEx(Context ctx, int userId) {
        this.mDefaultUserState = new FingerprintUserState(ctx, userId);
        this.mUDUserState = new FingerprintUserState(ctx, userId, 1);
    }

    public void addFinerprint(int fingerId, int groupId, int deviceIndex) {
        if (deviceIndex == 0) {
            FingerprintUserState fingerprintUserState = this.mDefaultUserState;
            fingerprintUserState.addBiometric(new Fingerprint(fingerprintUserState.getUniqueName(), groupId, fingerId, 0));
        } else if (deviceIndex == 1) {
            this.mUDUserState.addBiometric(new Fingerprint(this.mDefaultUserState.getUniqueName(), groupId, fingerId, 0));
        }
    }

    public List<Fingerprint> getFingerprints(int deviceIndex) {
        if (deviceIndex == 1) {
            List<Fingerprint> udFingers = this.mUDUserState.getBiometrics();
            Log.i("FingerprintsUserStateEx", "UD Finger size:" + udFingers.size());
            return udFingers;
        } else if (deviceIndex != -1) {
            return this.mDefaultUserState.getBiometrics();
        } else {
            List<Fingerprint> list = this.mDefaultUserState.getBiometrics();
            list.addAll(this.mUDUserState.getBiometrics());
            return list;
        }
    }

    public void removeFingerprint(int fingerId) {
        if (this.mDefaultUserState.isBiometricExist(fingerId)) {
            this.mDefaultUserState.removeBiometric(fingerId);
        } else {
            this.mUDUserState.removeBiometric(fingerId);
        }
    }

    public void renameFingerprint(int fingerId, CharSequence name) {
        if (this.mDefaultUserState.isBiometricExist(fingerId)) {
            this.mDefaultUserState.renameBiometric(fingerId, name);
        } else {
            this.mUDUserState.renameBiometric(fingerId, name);
        }
    }
}
