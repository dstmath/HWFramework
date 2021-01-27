package com.android.server.policy;

import android.content.Context;
import android.os.Handler;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.policy.GlobalActionsProvider;
import com.android.server.policy.WindowManagerPolicy;

/* access modifiers changed from: package-private */
public class GlobalActions implements GlobalActionsProvider.GlobalActionsListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "GlobalActions";
    private final Context mContext;
    private boolean mDeviceProvisioned;
    private boolean mGlobalActionsAvailable;
    private final GlobalActionsProvider mGlobalActionsProvider;
    private final Handler mHandler;
    private boolean mKeyguardShowing;
    private LegacyGlobalActions mLegacyGlobalActions;
    private final Runnable mShowTimeout = new Runnable() {
        /* class com.android.server.policy.GlobalActions.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            GlobalActions.this.ensureLegacyCreated();
            GlobalActions.this.mLegacyGlobalActions.showDialog(GlobalActions.this.mKeyguardShowing, GlobalActions.this.mDeviceProvisioned);
        }
    };
    private boolean mShowing;
    private final WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;

    public GlobalActions(Context context, WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mGlobalActionsProvider = (GlobalActionsProvider) LocalServices.getService(GlobalActionsProvider.class);
        GlobalActionsProvider globalActionsProvider = this.mGlobalActionsProvider;
        if (globalActionsProvider != null) {
            globalActionsProvider.setGlobalActionsListener(this);
        } else {
            Slog.i(TAG, "No GlobalActionsProvider found, defaulting to LegacyGlobalActions");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void ensureLegacyCreated() {
        if (this.mLegacyGlobalActions == null) {
            this.mLegacyGlobalActions = new LegacyGlobalActions(this.mContext, this.mWindowManagerFuncs, new Runnable() {
                /* class com.android.server.policy.$$Lambda$j_3GF7S52oSV__e_mYWlY5TeyiM */

                @Override // java.lang.Runnable
                public final void run() {
                    GlobalActions.this.onGlobalActionsDismissed();
                }
            });
        }
    }

    public void showDialog(boolean keyguardShowing, boolean deviceProvisioned) {
        GlobalActionsProvider globalActionsProvider = this.mGlobalActionsProvider;
        if (globalActionsProvider == null || !globalActionsProvider.isGlobalActionsDisabled()) {
            this.mKeyguardShowing = keyguardShowing;
            this.mDeviceProvisioned = deviceProvisioned;
            this.mShowing = true;
            if (this.mGlobalActionsAvailable) {
                this.mHandler.postDelayed(this.mShowTimeout, 5000);
                this.mGlobalActionsProvider.showGlobalActions();
                return;
            }
            ensureLegacyCreated();
            this.mLegacyGlobalActions.showDialog(this.mKeyguardShowing, this.mDeviceProvisioned);
        }
    }

    @Override // com.android.server.policy.GlobalActionsProvider.GlobalActionsListener
    public void onGlobalActionsShown() {
        this.mHandler.removeCallbacks(this.mShowTimeout);
    }

    @Override // com.android.server.policy.GlobalActionsProvider.GlobalActionsListener
    public void onGlobalActionsDismissed() {
        this.mShowing = false;
    }

    @Override // com.android.server.policy.GlobalActionsProvider.GlobalActionsListener
    public void onGlobalActionsAvailableChanged(boolean available) {
        this.mGlobalActionsAvailable = available;
        if (this.mShowing && !this.mGlobalActionsAvailable) {
            ensureLegacyCreated();
            this.mLegacyGlobalActions.showDialog(this.mKeyguardShowing, this.mDeviceProvisioned);
        }
    }
}
