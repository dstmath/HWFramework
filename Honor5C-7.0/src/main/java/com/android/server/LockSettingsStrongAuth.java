package com.android.server;

import android.app.trust.IStrongAuthTracker;
import android.content.Context;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Slog;
import android.util.SparseIntArray;
import com.android.internal.widget.LockPatternUtils.StrongAuthTracker;
import java.util.ArrayList;

public class LockSettingsStrongAuth {
    private static final int MSG_REGISTER_TRACKER = 2;
    private static final int MSG_REMOVE_USER = 4;
    private static final int MSG_REQUIRE_STRONG_AUTH = 1;
    private static final int MSG_UNREGISTER_TRACKER = 3;
    private static final String TAG = "LockSettings";
    private final int mDefaultStrongAuthFlags;
    private final Handler mHandler;
    private final SparseIntArray mStrongAuthForUser;
    private final ArrayList<IStrongAuthTracker> mStrongAuthTrackers;

    public LockSettingsStrongAuth(Context context) {
        this.mStrongAuthTrackers = new ArrayList();
        this.mStrongAuthForUser = new SparseIntArray();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case LockSettingsStrongAuth.MSG_REQUIRE_STRONG_AUTH /*1*/:
                        LockSettingsStrongAuth.this.handleRequireStrongAuth(msg.arg1, msg.arg2);
                    case LockSettingsStrongAuth.MSG_REGISTER_TRACKER /*2*/:
                        LockSettingsStrongAuth.this.handleAddStrongAuthTracker((IStrongAuthTracker) msg.obj);
                    case LockSettingsStrongAuth.MSG_UNREGISTER_TRACKER /*3*/:
                        LockSettingsStrongAuth.this.handleRemoveStrongAuthTracker((IStrongAuthTracker) msg.obj);
                    case LockSettingsStrongAuth.MSG_REMOVE_USER /*4*/:
                        LockSettingsStrongAuth.this.handleRemoveUser(msg.arg1);
                    default:
                }
            }
        };
        this.mDefaultStrongAuthFlags = StrongAuthTracker.getDefaultFlags(context);
    }

    private void handleAddStrongAuthTracker(IStrongAuthTracker tracker) {
        int i = 0;
        while (i < this.mStrongAuthTrackers.size()) {
            if (((IStrongAuthTracker) this.mStrongAuthTrackers.get(i)).asBinder() != tracker.asBinder()) {
                i += MSG_REQUIRE_STRONG_AUTH;
            } else {
                return;
            }
        }
        this.mStrongAuthTrackers.add(tracker);
        for (i = 0; i < this.mStrongAuthForUser.size(); i += MSG_REQUIRE_STRONG_AUTH) {
            try {
                tracker.onStrongAuthRequiredChanged(this.mStrongAuthForUser.valueAt(i), this.mStrongAuthForUser.keyAt(i));
            } catch (RemoteException e) {
                Slog.e(TAG, "Exception while adding StrongAuthTracker.", e);
            }
        }
    }

    private void handleRemoveStrongAuthTracker(IStrongAuthTracker tracker) {
        for (int i = 0; i < this.mStrongAuthTrackers.size(); i += MSG_REQUIRE_STRONG_AUTH) {
            if (((IStrongAuthTracker) this.mStrongAuthTrackers.get(i)).asBinder() == tracker.asBinder()) {
                this.mStrongAuthTrackers.remove(i);
                return;
            }
        }
    }

    private void handleRequireStrongAuth(int strongAuthReason, int userId) {
        if (userId == -1) {
            for (int i = 0; i < this.mStrongAuthForUser.size(); i += MSG_REQUIRE_STRONG_AUTH) {
                handleRequireStrongAuthOneUser(strongAuthReason, this.mStrongAuthForUser.keyAt(i));
            }
            return;
        }
        handleRequireStrongAuthOneUser(strongAuthReason, userId);
    }

    private void handleRequireStrongAuthOneUser(int strongAuthReason, int userId) {
        int newValue = 0;
        int oldValue = this.mStrongAuthForUser.get(userId, this.mDefaultStrongAuthFlags);
        if (strongAuthReason != 0) {
            newValue = oldValue | strongAuthReason;
        }
        if (oldValue != newValue) {
            this.mStrongAuthForUser.put(userId, newValue);
            notifyStrongAuthTrackers(newValue, userId);
        }
    }

    private void handleRemoveUser(int userId) {
        int index = this.mStrongAuthForUser.indexOfKey(userId);
        if (index >= 0) {
            this.mStrongAuthForUser.removeAt(index);
            notifyStrongAuthTrackers(this.mDefaultStrongAuthFlags, userId);
        }
    }

    private void notifyStrongAuthTrackers(int strongAuthReason, int userId) {
        int i = 0;
        while (i < this.mStrongAuthTrackers.size()) {
            try {
                ((IStrongAuthTracker) this.mStrongAuthTrackers.get(i)).onStrongAuthRequiredChanged(strongAuthReason, userId);
            } catch (DeadObjectException e) {
                Slog.d(TAG, "Removing dead StrongAuthTracker.");
                this.mStrongAuthTrackers.remove(i);
                i--;
            } catch (RemoteException e2) {
                Slog.e(TAG, "Exception while notifying StrongAuthTracker.", e2);
            }
            i += MSG_REQUIRE_STRONG_AUTH;
        }
    }

    public void registerStrongAuthTracker(IStrongAuthTracker tracker) {
        this.mHandler.obtainMessage(MSG_REGISTER_TRACKER, tracker).sendToTarget();
    }

    public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) {
        this.mHandler.obtainMessage(MSG_UNREGISTER_TRACKER, tracker).sendToTarget();
    }

    public void removeUser(int userId) {
        this.mHandler.obtainMessage(MSG_REMOVE_USER, userId, 0).sendToTarget();
    }

    public void requireStrongAuth(int strongAuthReason, int userId) {
        if (userId == -1 || userId >= 0) {
            this.mHandler.obtainMessage(MSG_REQUIRE_STRONG_AUTH, strongAuthReason, userId).sendToTarget();
            return;
        }
        throw new IllegalArgumentException("userId must be an explicit user id or USER_ALL");
    }

    public void reportUnlock(int userId) {
        requireStrongAuth(0, userId);
    }
}
