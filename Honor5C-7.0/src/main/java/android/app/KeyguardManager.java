package android.app;

import android.app.trust.ITrustManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.view.IOnKeyguardExitResult.Stub;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

public class KeyguardManager {
    public static final String ACTION_CONFIRM_DEVICE_CREDENTIAL = "android.app.action.CONFIRM_DEVICE_CREDENTIAL";
    public static final String ACTION_CONFIRM_DEVICE_CREDENTIAL_WITH_USER = "android.app.action.CONFIRM_DEVICE_CREDENTIAL_WITH_USER";
    public static final String EXTRA_DESCRIPTION = "android.app.extra.DESCRIPTION";
    public static final String EXTRA_TITLE = "android.app.extra.TITLE";
    private ITrustManager mTrustManager;
    private IUserManager mUserManager;
    private IWindowManager mWM;

    /* renamed from: android.app.KeyguardManager.1 */
    class AnonymousClass1 extends Stub {
        final /* synthetic */ OnKeyguardExitResult val$callback;

        AnonymousClass1(OnKeyguardExitResult val$callback) {
            this.val$callback = val$callback;
        }

        public void onKeyguardExitResult(boolean success) throws RemoteException {
            if (this.val$callback != null) {
                this.val$callback.onKeyguardExitResult(success);
            }
        }
    }

    public class KeyguardLock {
        private final String mTag;
        private final IBinder mToken;

        KeyguardLock(String tag) {
            this.mToken = new Binder();
            this.mTag = tag;
        }

        public void disableKeyguard() {
            try {
                KeyguardManager.this.mWM.disableKeyguard(this.mToken, this.mTag);
            } catch (RemoteException e) {
            }
        }

        public void reenableKeyguard() {
            try {
                KeyguardManager.this.mWM.reenableKeyguard(this.mToken);
            } catch (RemoteException e) {
            }
        }
    }

    public interface OnKeyguardExitResult {
        void onKeyguardExitResult(boolean z);
    }

    public Intent createConfirmDeviceCredentialIntent(CharSequence title, CharSequence description) {
        if (!isDeviceSecure()) {
            return null;
        }
        Intent intent = new Intent(ACTION_CONFIRM_DEVICE_CREDENTIAL);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_DESCRIPTION, description);
        intent.setPackage("com.android.settings");
        return intent;
    }

    public Intent createConfirmDeviceCredentialIntent(CharSequence title, CharSequence description, int userId) {
        if (!isDeviceSecure(userId)) {
            return null;
        }
        Intent intent = new Intent(ACTION_CONFIRM_DEVICE_CREDENTIAL_WITH_USER);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_DESCRIPTION, description);
        intent.putExtra(Intent.EXTRA_USER_ID, userId);
        intent.setPackage("com.android.settings");
        return intent;
    }

    KeyguardManager() {
        this.mWM = WindowManagerGlobal.getWindowManagerService();
        this.mTrustManager = ITrustManager.Stub.asInterface(ServiceManager.getService(Context.TRUST_SERVICE));
        this.mUserManager = IUserManager.Stub.asInterface(ServiceManager.getService(Context.USER_SERVICE));
    }

    @Deprecated
    public KeyguardLock newKeyguardLock(String tag) {
        return new KeyguardLock(tag);
    }

    public boolean isKeyguardLocked() {
        try {
            return this.mWM.isKeyguardLocked();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isKeyguardSecure() {
        try {
            return this.mWM.isKeyguardSecure();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean inKeyguardRestrictedInputMode() {
        try {
            return this.mWM.inKeyguardRestrictedInputMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isDeviceLocked() {
        return isDeviceLocked(UserHandle.getCallingUserId());
    }

    public boolean isDeviceLocked(int userId) {
        try {
            return getTrustManager().isDeviceLocked(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isDeviceSecure() {
        return isDeviceSecure(UserHandle.getCallingUserId());
    }

    public boolean isDeviceSecure(int userId) {
        try {
            return getTrustManager().isDeviceSecure(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    private synchronized ITrustManager getTrustManager() {
        if (this.mTrustManager == null) {
            this.mTrustManager = ITrustManager.Stub.asInterface(ServiceManager.getService(Context.TRUST_SERVICE));
        }
        return this.mTrustManager;
    }

    @Deprecated
    public void exitKeyguardSecurely(OnKeyguardExitResult callback) {
        try {
            this.mWM.exitKeyguardSecurely(new AnonymousClass1(callback));
        } catch (RemoteException e) {
        }
    }
}
