package com.android.server.wm;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.TokenWatcher;
import android.util.Log;
import android.util.Pair;
import com.android.server.policy.WindowManagerPolicy;

public class KeyguardDisableHandler extends Handler {
    private static final int ALLOW_DISABLE_NO = 0;
    private static final int ALLOW_DISABLE_UNKNOWN = -1;
    private static final int ALLOW_DISABLE_YES = 1;
    private static final String APP_NAME_ARTICLE = "com.ss.android.article.lite";
    static final int KEYGUARD_DISABLE = 1;
    static final int KEYGUARD_POLICY_CHANGED = 3;
    static final int KEYGUARD_REENABLE = 2;
    private static final String TAG = "WindowManager";
    /* access modifiers changed from: private */
    public int mAllowDisableKeyguard = -1;
    final Context mContext;
    KeyguardTokenWatcher mKeyguardTokenWatcher;
    final WindowManagerPolicy mPolicy;

    class KeyguardTokenWatcher extends TokenWatcher {
        public KeyguardTokenWatcher(Handler handler) {
            super(handler, KeyguardDisableHandler.TAG);
        }

        public void updateAllowState() {
            DevicePolicyManager dpm = (DevicePolicyManager) KeyguardDisableHandler.this.mContext.getSystemService("device_policy");
            if (dpm != null) {
                try {
                    int unused = KeyguardDisableHandler.this.mAllowDisableKeyguard = dpm.getPasswordQuality(null, ActivityManager.getService().getCurrentUser().id) == 0 ? 1 : 0;
                } catch (RemoteException e) {
                    Log.e(KeyguardDisableHandler.TAG, "RemoteException is in updateAllowState!");
                }
            }
        }

        public void acquired() {
            if (KeyguardDisableHandler.this.mAllowDisableKeyguard == -1) {
                updateAllowState();
            }
            if (KeyguardDisableHandler.this.mAllowDisableKeyguard == 1) {
                KeyguardDisableHandler.this.mPolicy.enableKeyguard(false);
            } else {
                Log.v(KeyguardDisableHandler.TAG, "Not disabling keyguard since device policy is enforced");
            }
        }

        public void released() {
            KeyguardDisableHandler.this.mPolicy.enableKeyguard(true);
        }
    }

    public KeyguardDisableHandler(Context context, WindowManagerPolicy policy) {
        this.mContext = context;
        this.mPolicy = policy;
    }

    private boolean checkCallingPackage(Context context, int uid) {
        if (context == null || context.getPackageManager() == null || !APP_NAME_ARTICLE.equals(context.getPackageManager().getNameForUid(uid))) {
            return false;
        }
        return true;
    }

    public void handleMessage(Message msg) {
        if (this.mKeyguardTokenWatcher == null) {
            this.mKeyguardTokenWatcher = new KeyguardTokenWatcher(this);
        }
        switch (msg.what) {
            case 1:
                if (!checkCallingPackage(this.mContext, msg.arg1)) {
                    Pair<IBinder, String> pair = (Pair) msg.obj;
                    this.mKeyguardTokenWatcher.acquire((IBinder) pair.first, (String) pair.second);
                    break;
                } else {
                    return;
                }
            case 2:
                this.mKeyguardTokenWatcher.release((IBinder) msg.obj);
                break;
            case 3:
                this.mAllowDisableKeyguard = -1;
                if (!this.mKeyguardTokenWatcher.isAcquired()) {
                    this.mPolicy.enableKeyguard(true);
                    break;
                } else {
                    this.mKeyguardTokenWatcher.updateAllowState();
                    if (this.mAllowDisableKeyguard != 1) {
                        this.mPolicy.enableKeyguard(true);
                        break;
                    }
                }
                break;
        }
    }
}
