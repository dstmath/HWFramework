package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.util.Log;
import java.util.List;

class FingerprintsUserStateEx {
    private FingerprintsUserState mDefaultUserState;
    private FingerprintsUserState mUDUserState;

    public FingerprintsUserStateEx(Context ctx, int userId) {
        this.mDefaultUserState = new FingerprintsUserState(ctx, userId);
        this.mUDUserState = new FingerprintsUserState(ctx, userId, 1);
    }

    public void addFinerprint(int fingerId, int groupId, int deviceIndex) {
        if (deviceIndex == 0) {
            this.mDefaultUserState.addFingerprint(fingerId, groupId);
        } else if (deviceIndex == 1) {
            this.mUDUserState.addFingerprint(fingerId, groupId);
        }
    }

    public List<Fingerprint> getFingerprints(int deviceIndex) {
        if (deviceIndex == 1) {
            List<Fingerprint> udFingers = this.mUDUserState.getFingerprints();
            Log.i("FingerprintsUserStateEx", "UD Finger size:" + udFingers.size());
            return udFingers;
        } else if (deviceIndex != -1) {
            return this.mDefaultUserState.getFingerprints();
        } else {
            List<Fingerprint> list = this.mDefaultUserState.getFingerprints();
            list.addAll(this.mUDUserState.getFingerprints());
            return list;
        }
    }

    public void removeFingerprint(int fingerId) {
        if (this.mDefaultUserState.isFingerprintExist(fingerId)) {
            this.mDefaultUserState.removeFingerprint(fingerId);
        } else {
            this.mUDUserState.removeFingerprint(fingerId);
        }
    }

    public void renameFingerprint(int fingerId, CharSequence name) {
        if (this.mDefaultUserState.isFingerprintExist(fingerId)) {
            this.mDefaultUserState.renameFingerprint(fingerId, name);
        } else {
            this.mUDUserState.renameFingerprint(fingerId, name);
        }
    }
}
