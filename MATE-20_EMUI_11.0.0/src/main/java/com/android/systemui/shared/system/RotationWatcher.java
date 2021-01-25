package com.android.systemui.shared.system;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.WindowManagerGlobal;

public abstract class RotationWatcher {
    private static final String TAG = "RotationWatcher";
    private final Context mContext;
    private boolean mIsWatching = false;
    private final IRotationWatcher mWatcher = new IRotationWatcher.Stub() {
        /* class com.android.systemui.shared.system.RotationWatcher.AnonymousClass1 */

        public void onRotationChanged(int rotation) {
            RotationWatcher.this.onRotationChanged(rotation);
        }
    };

    /* access modifiers changed from: protected */
    public abstract void onRotationChanged(int i);

    public RotationWatcher(Context context) {
        this.mContext = context;
    }

    public void enable() {
        if (!this.mIsWatching) {
            try {
                WindowManagerGlobal.getWindowManagerService().watchRotation(this.mWatcher, this.mContext.getDisplayId());
                this.mIsWatching = true;
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to set rotation watcher", e);
            }
        }
    }

    public void disable() {
        if (this.mIsWatching) {
            try {
                WindowManagerGlobal.getWindowManagerService().removeRotationWatcher(this.mWatcher);
                this.mIsWatching = false;
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to remove rotation watcher", e);
            }
        }
    }
}
