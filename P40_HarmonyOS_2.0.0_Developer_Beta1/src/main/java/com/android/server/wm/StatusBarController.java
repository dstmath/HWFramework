package com.android.server.wm;

import android.os.IBinder;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wm.StatusBarController;
import com.android.server.wm.WindowManagerInternal;

public class StatusBarController extends BarController {
    private final WindowManagerInternal.AppTransitionListener mAppTransitionListener = new WindowManagerInternal.AppTransitionListener() {
        /* class com.android.server.wm.StatusBarController.AnonymousClass1 */
        private Runnable mAppTransitionCancelled = new Runnable() {
            /* class com.android.server.wm.$$Lambda$StatusBarController$1$CizMeoiz6ZVrkt6kAKpSV5htmyc */

            @Override // java.lang.Runnable
            public final void run() {
                StatusBarController.AnonymousClass1.this.lambda$$1$StatusBarController$1();
            }
        };
        private Runnable mAppTransitionFinished = new Runnable() {
            /* class com.android.server.wm.$$Lambda$StatusBarController$1$3FiQ0kybPCSlgcNJkCsNm5M12iA */

            @Override // java.lang.Runnable
            public final void run() {
                StatusBarController.AnonymousClass1.this.lambda$$2$StatusBarController$1();
            }
        };
        private Runnable mAppTransitionPending = new Runnable() {
            /* class com.android.server.wm.$$Lambda$StatusBarController$1$x4q7e0Eysf0ynMSdT1AJN_ucuI */

            @Override // java.lang.Runnable
            public final void run() {
                StatusBarController.AnonymousClass1.this.lambda$$0$StatusBarController$1();
            }
        };

        public /* synthetic */ void lambda$$0$StatusBarController$1() {
            StatusBarManagerInternal statusBar = StatusBarController.this.getStatusBarInternal();
            if (statusBar != null) {
                statusBar.appTransitionPending(StatusBarController.this.mDisplayId);
            }
        }

        public /* synthetic */ void lambda$$1$StatusBarController$1() {
            StatusBarManagerInternal statusBar = StatusBarController.this.getStatusBarInternal();
            if (statusBar != null) {
                statusBar.appTransitionCancelled(StatusBarController.this.mDisplayId);
            }
        }

        public /* synthetic */ void lambda$$2$StatusBarController$1() {
            StatusBarManagerInternal statusBar = StatusBarController.this.getStatusBarInternal();
            if (statusBar != null) {
                statusBar.appTransitionFinished(StatusBarController.this.mDisplayId);
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
        public void onAppTransitionPendingLocked() {
            StatusBarController.this.mHandler.post(this.mAppTransitionPending);
        }

        @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
        public int onAppTransitionStartingLocked(int transit, long duration, long statusBarAnimationStartTime, long statusBarAnimationDuration) {
            StatusBarController.this.mHandler.post(new Runnable(statusBarAnimationStartTime, statusBarAnimationDuration) {
                /* class com.android.server.wm.$$Lambda$StatusBarController$1$t71qcQIBSxRShk0Xohf1lk53bOw */
                private final /* synthetic */ long f$1;
                private final /* synthetic */ long f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StatusBarController.AnonymousClass1.this.lambda$onAppTransitionStartingLocked$3$StatusBarController$1(this.f$1, this.f$2);
                }
            });
            return 0;
        }

        public /* synthetic */ void lambda$onAppTransitionStartingLocked$3$StatusBarController$1(long statusBarAnimationStartTime, long statusBarAnimationDuration) {
            StatusBarManagerInternal statusBar = StatusBarController.this.getStatusBarInternal();
            if (statusBar != null) {
                statusBar.appTransitionStarting(StatusBarController.this.mDisplayId, statusBarAnimationStartTime, statusBarAnimationDuration);
            }
        }

        @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
        public void onAppTransitionCancelledLocked(int transit) {
            StatusBarController.this.mHandler.post(this.mAppTransitionCancelled);
        }

        @Override // com.android.server.wm.WindowManagerInternal.AppTransitionListener
        public void onAppTransitionFinishedLocked(IBinder token) {
            StatusBarController.this.mHandler.post(this.mAppTransitionFinished);
        }
    };

    StatusBarController(int displayId) {
        super("StatusBar", displayId, 67108864, 268435456, 1073741824, 1, 67108864, 8);
    }

    /* access modifiers changed from: package-private */
    public void setTopAppHidesStatusBar(boolean hidesStatusBar) {
        StatusBarManagerInternal statusBar = getStatusBarInternal();
        if (statusBar != null) {
            statusBar.setTopAppHidesStatusBar(hidesStatusBar);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.BarController
    public boolean skipAnimation() {
        return this.mWin.getAttrs().height == -1;
    }

    /* access modifiers changed from: package-private */
    public WindowManagerInternal.AppTransitionListener getAppTransitionListener() {
        return this.mAppTransitionListener;
    }
}
