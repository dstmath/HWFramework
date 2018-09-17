package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;

public abstract class InternalRemovalClient extends RemovalClient {
    public InternalRemovalClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int fingerId, int groupId, int userId, boolean restricted, String owner) {
        super(context, halDeviceId, token, receiver, fingerId, groupId, userId, restricted, owner);
    }
}
