package com.android.server.wm;

import android.app.admin.DevicePolicyCache;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.UserManagerInternal;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.utils.UserTokenWatcher;
import com.android.server.wm.LockTaskController;

/* access modifiers changed from: package-private */
public class KeyguardDisableHandler {
    private static final String TAG = "WindowManager";
    private final UserTokenWatcher mAppTokenWatcher;
    private final UserTokenWatcher.Callback mCallback = new UserTokenWatcher.Callback() {
        /* class com.android.server.wm.KeyguardDisableHandler.AnonymousClass1 */

        public void acquired(int userId) {
            KeyguardDisableHandler.this.updateKeyguardEnabled(userId);
        }

        public void released(int userId) {
            KeyguardDisableHandler.this.updateKeyguardEnabled(userId);
        }
    };
    private int mCurrentUser = 0;
    private Injector mInjector;
    private final UserTokenWatcher mSystemTokenWatcher;

    /* access modifiers changed from: package-private */
    public interface Injector {
        boolean dpmRequiresPassword(int i);

        void enableKeyguard(boolean z);

        int getProfileParentId(int i);

        boolean isKeyguardSecure(int i);
    }

    @VisibleForTesting
    KeyguardDisableHandler(Injector injector, Handler handler) {
        this.mInjector = injector;
        this.mAppTokenWatcher = new UserTokenWatcher(this.mCallback, handler, TAG);
        this.mSystemTokenWatcher = new UserTokenWatcher(this.mCallback, handler, TAG);
    }

    public void setCurrentUser(int user) {
        synchronized (this) {
            this.mCurrentUser = user;
            updateKeyguardEnabledLocked(-1);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateKeyguardEnabled(int userId) {
        synchronized (this) {
            updateKeyguardEnabledLocked(userId);
        }
    }

    private void updateKeyguardEnabledLocked(int userId) {
        if (this.mCurrentUser == userId || userId == -1) {
            this.mInjector.enableKeyguard(shouldKeyguardBeEnabled(this.mCurrentUser));
        }
    }

    /* access modifiers changed from: package-private */
    public void disableKeyguard(IBinder token, String tag, int callingUid, int userId) {
        watcherForCallingUid(token, callingUid).acquire(token, tag, this.mInjector.getProfileParentId(userId));
    }

    /* access modifiers changed from: package-private */
    public void reenableKeyguard(IBinder token, int callingUid, int userId) {
        watcherForCallingUid(token, callingUid).release(token, this.mInjector.getProfileParentId(userId));
    }

    private UserTokenWatcher watcherForCallingUid(IBinder token, int callingUid) {
        if (Process.isApplicationUid(callingUid)) {
            return this.mAppTokenWatcher;
        }
        if (callingUid == 1000 && (token instanceof LockTaskController.LockTaskToken)) {
            return this.mSystemTokenWatcher;
        }
        throw new UnsupportedOperationException("Only apps can use the KeyguardLock API");
    }

    private boolean shouldKeyguardBeEnabled(int userId) {
        boolean dpmRequiresPassword = this.mInjector.dpmRequiresPassword(this.mCurrentUser);
        return !(((!dpmRequiresPassword && !this.mInjector.isKeyguardSecure(this.mCurrentUser)) && this.mAppTokenWatcher.isAcquired(userId)) || ((dpmRequiresPassword ^ true) && this.mSystemTokenWatcher.isAcquired(userId)));
    }

    static KeyguardDisableHandler create(Context context, final WindowManagerPolicy policy, Handler handler) {
        final UserManagerInternal userManager = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        return new KeyguardDisableHandler(new Injector() {
            /* class com.android.server.wm.KeyguardDisableHandler.AnonymousClass2 */

            @Override // com.android.server.wm.KeyguardDisableHandler.Injector
            public boolean dpmRequiresPassword(int userId) {
                return DevicePolicyCache.getInstance().getPasswordQuality(userId) != 0;
            }

            @Override // com.android.server.wm.KeyguardDisableHandler.Injector
            public boolean isKeyguardSecure(int userId) {
                return policy.isKeyguardSecure(userId);
            }

            @Override // com.android.server.wm.KeyguardDisableHandler.Injector
            public int getProfileParentId(int userId) {
                return userManager.getProfileParentId(userId);
            }

            @Override // com.android.server.wm.KeyguardDisableHandler.Injector
            public void enableKeyguard(boolean enabled) {
                policy.enableKeyguard(enabled);
            }
        }, handler);
    }
}
