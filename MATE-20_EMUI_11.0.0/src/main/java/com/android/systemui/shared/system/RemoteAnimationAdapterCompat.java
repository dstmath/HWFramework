package com.android.systemui.shared.system;

import android.os.RemoteException;
import android.util.Log;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.IRemoteAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationTarget;

public class RemoteAnimationAdapterCompat {
    private final RemoteAnimationAdapter mWrapped;

    public RemoteAnimationAdapterCompat(RemoteAnimationRunnerCompat runner, long duration, long statusBarTransitionDelay) {
        this.mWrapped = new RemoteAnimationAdapter(wrapRemoteAnimationRunner(runner), duration, statusBarTransitionDelay);
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationAdapter getWrapped() {
        return this.mWrapped;
    }

    private static IRemoteAnimationRunner.Stub wrapRemoteAnimationRunner(final RemoteAnimationRunnerCompat remoteAnimationAdapter) {
        return new IRemoteAnimationRunner.Stub() {
            /* class com.android.systemui.shared.system.RemoteAnimationAdapterCompat.AnonymousClass1 */

            public void onAnimationStart(RemoteAnimationTarget[] apps, final IRemoteAnimationFinishedCallback finishedCallback) {
                RemoteAnimationRunnerCompat.this.onAnimationStart(RemoteAnimationTargetCompat.wrap(apps), new Runnable() {
                    /* class com.android.systemui.shared.system.RemoteAnimationAdapterCompat.AnonymousClass1.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            finishedCallback.onAnimationFinished();
                        } catch (RemoteException e) {
                            Log.e("ActivityOptionsCompat", "Failed to call app controlled animation finished callback", e);
                        }
                    }
                });
            }

            public void onAnimationCancelled() {
                RemoteAnimationRunnerCompat.this.onAnimationCancelled();
            }
        };
    }
}
