package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.IBinder;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

public abstract class InternalEnumerateClient extends EnumerateClient {
    private List<Fingerprint> mEnrolledList;
    private List<Fingerprint> mEnumeratedList = new ArrayList();

    public InternalEnumerateClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int groupId, int userId, boolean restricted, String owner, List<Fingerprint> enrolledList) {
        super(context, halDeviceId, token, receiver, userId, groupId, restricted, owner);
        this.mEnrolledList = enrolledList;
    }

    private void handleEnumeratedFingerprint(int fingerId, int groupId, int remaining) {
        boolean matched = false;
        for (int i = 0; i < this.mEnrolledList.size(); i++) {
            if (((Fingerprint) this.mEnrolledList.get(i)).getFingerId() == fingerId) {
                this.mEnrolledList.remove(i);
                matched = true;
                Slog.e("FingerprintService", "Matched fingerprint fid=" + fingerId);
                break;
            }
        }
        if (!matched && fingerId != 0) {
            this.mEnumeratedList.add(new Fingerprint("", groupId, fingerId, getHalDeviceId()));
        }
    }

    private void doFingerprintCleanup() {
        if (this.mEnrolledList != null) {
            for (Fingerprint f : this.mEnrolledList) {
                Slog.e("FingerprintService", "Internal Enumerate: Removing dangling enrolled fingerprint: " + f.getName() + " " + f.getFingerId() + " " + f.getGroupId() + " " + f.getDeviceId());
                FingerprintUtils.getInstance().removeFingerprintIdForUser(getContext(), f.getFingerId(), getTargetUserId());
            }
            this.mEnrolledList.clear();
        }
    }

    public List<Fingerprint> getEnumeratedList() {
        return this.mEnumeratedList;
    }

    public boolean onEnumerationResult(int fingerId, int groupId, int remaining) {
        handleEnumeratedFingerprint(fingerId, groupId, remaining);
        if (remaining == 0) {
            doFingerprintCleanup();
        }
        if (fingerId == 0) {
            return true;
        }
        return false;
    }
}
