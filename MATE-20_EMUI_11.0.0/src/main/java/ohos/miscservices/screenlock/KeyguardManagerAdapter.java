package ohos.miscservices.screenlock;

import android.app.Activity;
import android.app.KeyguardManager;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.adapter.utils.AdaptUtil;
import ohos.miscservices.screenlock.interfaces.UnlockScreenCallback;

public class KeyguardManagerAdapter {
    public static final int STATE_CALLBACK_NOT_SET = 0;
    public static final int STATE_CALLBACK_UNLOCK_FAIL = 2;
    public static final int STATE_CALLBACK_UNLOCK_SUCCESS = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "KeyguardManagerAdapter");
    private static KeyguardManager keyGuardManager;
    private boolean isUnlockAdaptSuccess;
    private int unlockCallbackState;

    private KeyguardManagerAdapter() {
        AdaptUtil.getKeyguardManager().ifPresent($$Lambda$KeyguardManagerAdapter$4CHaMPWU20wI3Bpm26WWYVVZUcI.INSTANCE);
    }

    public static KeyguardManagerAdapter getInstance() {
        HiLog.debug(TAG, "getInstance: get an instance of KeyguardManagerAdapter", new Object[0]);
        return KeyguardManagerAdapterInner.singleton;
    }

    private static class KeyguardManagerAdapterInner {
        private static KeyguardManagerAdapter singleton = new KeyguardManagerAdapter();

        private KeyguardManagerAdapterInner() {
        }
    }

    public boolean isLocked() {
        KeyguardManager keyguardManager = keyGuardManager;
        if (keyguardManager != null) {
            return keyguardManager.isKeyguardLocked();
        }
        HiLog.error(TAG, "isLocked failed, because the current key guard mgr is null, default return true.", new Object[0]);
        return true;
    }

    public void unlock(Context context, UnlockScreenCallback unlockScreenCallback) {
        if (context == null) {
            HiLog.error(TAG, "The given abilityContext is required not null.", new Object[0]);
            this.isUnlockAdaptSuccess = false;
            return;
        }
        Object hostContext = context.getHostContext();
        Activity activity = null;
        if (hostContext instanceof Activity) {
            activity = (Activity) hostContext;
        }
        if (activity == null) {
            HiLog.warn(TAG, "unlock failed, because the activity is null.", new Object[0]);
            this.isUnlockAdaptSuccess = false;
            return;
        }
        HiLog.debug(TAG, "unlock activity=%{public}s", activity);
        KeyguardManager keyguardManager = keyGuardManager;
        if (keyguardManager == null) {
            HiLog.error(TAG, "unlock failed, because the current key guard mgr is null.", new Object[0]);
            this.isUnlockAdaptSuccess = false;
            return;
        }
        keyguardManager.requestDismissKeyguard(activity, new KeyguardDismissCallbackImpl(unlockScreenCallback));
        this.isUnlockAdaptSuccess = true;
    }

    class KeyguardDismissCallbackImpl extends KeyguardManager.KeyguardDismissCallback {
        private UnlockScreenCallback callback;

        KeyguardDismissCallbackImpl(UnlockScreenCallback unlockScreenCallback) {
            this.callback = unlockScreenCallback;
            if (unlockScreenCallback == null) {
                HiLog.info(KeyguardManagerAdapter.TAG, "Not set UnlockScreenCallback.", new Object[0]);
                KeyguardManagerAdapter.this.unlockCallbackState = 0;
            }
        }

        @Override // android.app.KeyguardManager.KeyguardDismissCallback
        public void onDismissError() {
            HiLog.info(KeyguardManagerAdapter.TAG, "onDismissError", new Object[0]);
            UnlockScreenCallback unlockScreenCallback = this.callback;
            if (unlockScreenCallback != null) {
                unlockScreenCallback.onUnlockFailed();
                KeyguardManagerAdapter.this.unlockCallbackState = 2;
            }
        }

        @Override // android.app.KeyguardManager.KeyguardDismissCallback
        public void onDismissSucceeded() {
            HiLog.info(KeyguardManagerAdapter.TAG, "onDismissSucceeded", new Object[0]);
            UnlockScreenCallback unlockScreenCallback = this.callback;
            if (unlockScreenCallback != null) {
                unlockScreenCallback.onUnlockSucceeded();
                KeyguardManagerAdapter.this.unlockCallbackState = 1;
            }
        }

        @Override // android.app.KeyguardManager.KeyguardDismissCallback
        public void onDismissCancelled() {
            HiLog.info(KeyguardManagerAdapter.TAG, "onDismissCancelled", new Object[0]);
        }
    }

    public boolean isUnlockAdaptSuccess() {
        return this.isUnlockAdaptSuccess;
    }

    public int getUnlockCallbackState() {
        return this.unlockCallbackState;
    }
}
