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
        RemoteAnimationAdapter remoteAnimationAdapter = new RemoteAnimationAdapter(wrapRemoteAnimationRunner(runner), duration, statusBarTransitionDelay);
        this.mWrapped = remoteAnimationAdapter;
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationAdapter getWrapped() {
        return this.mWrapped;
    }

    private static IRemoteAnimationRunner.Stub wrapRemoteAnimationRunner(final RemoteAnimationRunnerCompat remoteAnimationAdapter) {
        return new IRemoteAnimationRunner.Stub() {
            public void onAnimationStart(RemoteAnimationTarget[] apps, final IRemoteAnimationFinishedCallback finishedCallback) throws RemoteException {
                RemoteAnimationRunnerCompat.this.onAnimationStart(RemoteAnimationTargetCompat.wrap(apps), new Runnable() {
                    public void run() {
                        try {
                            finishedCallback.onAnimationFinished();
                        } catch (RemoteException e) {
                            Log.e("ActivityOptionsCompat", "Failed to call app controlled animation finished callback", e);
                        }
                    }
                });
            }

            public void onAnimationCancelled() throws RemoteException {
                RemoteAnimationRunnerCompat.this.onAnimationCancelled();
            }
        };
    }
}
