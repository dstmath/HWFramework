package com.android.server.policy.keyguard;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.policy.IKeyguardService;
import com.android.server.LocalServices;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.policy.keyguard.KeyguardStateMonitor;

public class HwKeyguardStateMonitor extends KeyguardStateMonitor {
    private static final String TAG = "HwKeyguardStateMonitor";
    private DefaultGestureNavManager mGestureNavPolicy = ((DefaultGestureNavManager) LocalServices.getService(DefaultGestureNavManager.class));
    private InputManagerServiceEx.DefaultHwInputManagerLocalService mHwInputManagerInternal = ((InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServices.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class));

    public HwKeyguardStateMonitor(Context context, IKeyguardService service, KeyguardStateMonitor.StateCallback callback) {
        super(context, service, callback);
    }

    public void onShowingStateChanged(boolean showing) {
        HwKeyguardStateMonitor.super.onShowingStateChanged(showing);
        notifyKeyguardStateChanged(showing);
    }

    private void notifyKeyguardStateChanged(boolean showing) {
        InputManagerServiceEx.DefaultHwInputManagerLocalService defaultHwInputManagerLocalService = this.mHwInputManagerInternal;
        if (defaultHwInputManagerLocalService != null) {
            defaultHwInputManagerLocalService.setKeyguardState(showing);
        }
        DefaultGestureNavManager defaultGestureNavManager = this.mGestureNavPolicy;
        if (defaultGestureNavManager != null) {
            defaultGestureNavManager.onKeyguardShowingChanged(showing);
        }
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            try {
                hwAft.notifyKeyguardStateChange(showing);
            } catch (RemoteException e) {
                Slog.e(TAG, "notifyKeyguardStateChange throw exception");
            }
        }
    }
}
