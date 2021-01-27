package com.android.systemui.shared.recents.view;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public abstract class AppTransitionAnimationSpecsFuture {
    private FutureTask<List<AppTransitionAnimationSpecCompat>> mComposeTask = new FutureTask<>(new Callable<List<AppTransitionAnimationSpecCompat>>() {
        /* class com.android.systemui.shared.recents.view.AppTransitionAnimationSpecsFuture.AnonymousClass1 */

        @Override // java.util.concurrent.Callable
        public List<AppTransitionAnimationSpecCompat> call() throws Exception {
            return AppTransitionAnimationSpecsFuture.this.composeSpecs();
        }
    });
    private final IAppTransitionAnimationSpecsFuture mFuture = new IAppTransitionAnimationSpecsFuture.Stub() {
        /* class com.android.systemui.shared.recents.view.AppTransitionAnimationSpecsFuture.AnonymousClass2 */

        public AppTransitionAnimationSpec[] get() throws RemoteException {
            try {
                if (!AppTransitionAnimationSpecsFuture.this.mComposeTask.isDone()) {
                    AppTransitionAnimationSpecsFuture.this.mHandler.post(AppTransitionAnimationSpecsFuture.this.mComposeTask);
                }
                List<AppTransitionAnimationSpecCompat> specs = (List) AppTransitionAnimationSpecsFuture.this.mComposeTask.get();
                AppTransitionAnimationSpecsFuture.this.mComposeTask = null;
                if (specs == null) {
                    return null;
                }
                int specs_size = specs.size();
                AppTransitionAnimationSpec[] arr = new AppTransitionAnimationSpec[specs_size];
                for (int i = 0; i < specs_size; i++) {
                    arr[i] = specs.get(i).toAppTransitionAnimationSpec();
                }
                return arr;
            } catch (Exception e) {
                return null;
            }
        }
    };
    private final Handler mHandler;

    public abstract List<AppTransitionAnimationSpecCompat> composeSpecs();

    public AppTransitionAnimationSpecsFuture(Handler handler) {
        this.mHandler = handler;
    }

    public final IAppTransitionAnimationSpecsFuture getFuture() {
        return this.mFuture;
    }

    public final void composeSpecsSynchronous() {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            this.mComposeTask.run();
            return;
        }
        throw new RuntimeException("composeSpecsSynchronous() called from wrong looper");
    }
}
