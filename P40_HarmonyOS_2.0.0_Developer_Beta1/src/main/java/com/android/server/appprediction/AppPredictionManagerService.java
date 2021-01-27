package com.android.server.appprediction;

import android.app.prediction.AppPredictionContext;
import android.app.prediction.AppPredictionSessionId;
import android.app.prediction.AppTargetEvent;
import android.app.prediction.IPredictionCallback;
import android.app.prediction.IPredictionManager;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.appprediction.AppPredictionManagerService;
import com.android.server.infra.AbstractMasterSystemService;
import com.android.server.infra.FrameworkResourcesServiceNameResolver;
import com.android.server.wm.ActivityTaskManagerInternal;
import java.io.FileDescriptor;
import java.util.function.Consumer;

public class AppPredictionManagerService extends AbstractMasterSystemService<AppPredictionManagerService, AppPredictionPerUserService> {
    private static final int MAX_TEMP_SERVICE_DURATION_MS = 120000;
    private static final String TAG = AppPredictionManagerService.class.getSimpleName();
    private ActivityTaskManagerInternal mActivityTaskManagerInternal = ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class));

    public AppPredictionManagerService(Context context) {
        super(context, new FrameworkResourcesServiceNameResolver(context, 17039807), null);
    }

    @Override // com.android.server.infra.AbstractMasterSystemService
    public AppPredictionPerUserService newServiceLocked(int resolvedUserId, boolean disabled) {
        return new AppPredictionPerUserService(this, this.mLock, resolvedUserId);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.appprediction.AppPredictionManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.appprediction.AppPredictionManagerService$PredictionManagerServiceStub, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("app_prediction", new PredictionManagerServiceStub());
    }

    @Override // com.android.server.infra.AbstractMasterSystemService
    public void enforceCallingPermissionForManagement() {
        getContext().enforceCallingPermission("android.permission.MANAGE_APP_PREDICTIONS", TAG);
    }

    @Override // com.android.server.infra.AbstractMasterSystemService
    public int getMaximumTemporaryServiceDurationMs() {
        return MAX_TEMP_SERVICE_DURATION_MS;
    }

    public class PredictionManagerServiceStub extends IPredictionManager.Stub {
        private PredictionManagerServiceStub() {
            AppPredictionManagerService.this = r1;
        }

        public void createPredictionSession(AppPredictionContext context, AppPredictionSessionId sessionId) {
            runForUserLocked("createPredictionSession", new Consumer(context, sessionId) {
                /* class com.android.server.appprediction.$$Lambda$AppPredictionManagerService$PredictionManagerServiceStub$NmwmTMZXXS4S7viVNKzU2genXA8 */
                private final /* synthetic */ AppPredictionContext f$0;
                private final /* synthetic */ AppPredictionSessionId f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AppPredictionManagerService.PredictionManagerServiceStub.lambda$createPredictionSession$0(this.f$0, this.f$1, (AppPredictionPerUserService) obj);
                }
            });
        }

        public void notifyAppTargetEvent(AppPredictionSessionId sessionId, AppTargetEvent event) {
            runForUserLocked("notifyAppTargetEvent", new Consumer(sessionId, event) {
                /* class com.android.server.appprediction.$$Lambda$AppPredictionManagerService$PredictionManagerServiceStub$4yDhFef19aMlJY7O6RdjSAvnk */
                private final /* synthetic */ AppPredictionSessionId f$0;
                private final /* synthetic */ AppTargetEvent f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AppPredictionManagerService.PredictionManagerServiceStub.lambda$notifyAppTargetEvent$1(this.f$0, this.f$1, (AppPredictionPerUserService) obj);
                }
            });
        }

        public void notifyLaunchLocationShown(AppPredictionSessionId sessionId, String launchLocation, ParceledListSlice targetIds) {
            runForUserLocked("notifyLaunchLocationShown", new Consumer(sessionId, launchLocation, targetIds) {
                /* class com.android.server.appprediction.$$Lambda$AppPredictionManagerService$PredictionManagerServiceStub$vWB3PdxOOvPr7p0_NmoqXeH8Ros */
                private final /* synthetic */ AppPredictionSessionId f$0;
                private final /* synthetic */ String f$1;
                private final /* synthetic */ ParceledListSlice f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AppPredictionManagerService.PredictionManagerServiceStub.lambda$notifyLaunchLocationShown$2(this.f$0, this.f$1, this.f$2, (AppPredictionPerUserService) obj);
                }
            });
        }

        public void sortAppTargets(AppPredictionSessionId sessionId, ParceledListSlice targets, IPredictionCallback callback) {
            runForUserLocked("sortAppTargets", new Consumer(sessionId, targets, callback) {
                /* class com.android.server.appprediction.$$Lambda$AppPredictionManagerService$PredictionManagerServiceStub$3HMCieo6UZfG43p_6ip1hrL0k */
                private final /* synthetic */ AppPredictionSessionId f$0;
                private final /* synthetic */ ParceledListSlice f$1;
                private final /* synthetic */ IPredictionCallback f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AppPredictionManagerService.PredictionManagerServiceStub.lambda$sortAppTargets$3(this.f$0, this.f$1, this.f$2, (AppPredictionPerUserService) obj);
                }
            });
        }

        public void registerPredictionUpdates(AppPredictionSessionId sessionId, IPredictionCallback callback) {
            runForUserLocked("registerPredictionUpdates", new Consumer(sessionId, callback) {
                /* class com.android.server.appprediction.$$Lambda$AppPredictionManagerService$PredictionManagerServiceStub$40EK4qcrrG55ENTthOaXAXWDA4 */
                private final /* synthetic */ AppPredictionSessionId f$0;
                private final /* synthetic */ IPredictionCallback f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AppPredictionManagerService.PredictionManagerServiceStub.lambda$registerPredictionUpdates$4(this.f$0, this.f$1, (AppPredictionPerUserService) obj);
                }
            });
        }

        public void unregisterPredictionUpdates(AppPredictionSessionId sessionId, IPredictionCallback callback) {
            runForUserLocked("unregisterPredictionUpdates", new Consumer(sessionId, callback) {
                /* class com.android.server.appprediction.$$Lambda$AppPredictionManagerService$PredictionManagerServiceStub$s2vrDOHz5x1TW_6jMihxp1iCAvg */
                private final /* synthetic */ AppPredictionSessionId f$0;
                private final /* synthetic */ IPredictionCallback f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AppPredictionManagerService.PredictionManagerServiceStub.lambda$unregisterPredictionUpdates$5(this.f$0, this.f$1, (AppPredictionPerUserService) obj);
                }
            });
        }

        public void requestPredictionUpdate(AppPredictionSessionId sessionId) {
            runForUserLocked("requestPredictionUpdate", new Consumer(sessionId) {
                /* class com.android.server.appprediction.$$Lambda$AppPredictionManagerService$PredictionManagerServiceStub$vSY20eQq5y5FXrxhhqOTcEmezTs */
                private final /* synthetic */ AppPredictionSessionId f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AppPredictionManagerService.PredictionManagerServiceStub.lambda$requestPredictionUpdate$6(this.f$0, (AppPredictionPerUserService) obj);
                }
            });
        }

        public void onDestroyPredictionSession(AppPredictionSessionId sessionId) {
            runForUserLocked("onDestroyPredictionSession", new Consumer(sessionId) {
                /* class com.android.server.appprediction.$$Lambda$AppPredictionManagerService$PredictionManagerServiceStub$gVNT40YbIbIqIJKiNGjlZGVJjc */
                private final /* synthetic */ AppPredictionSessionId f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AppPredictionManagerService.PredictionManagerServiceStub.lambda$onDestroyPredictionSession$7(this.f$0, (AppPredictionPerUserService) obj);
                }
            });
        }

        /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.appprediction.AppPredictionManagerService$PredictionManagerServiceStub */
        /* JADX WARN: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new AppPredictionManagerServiceShellCommand(AppPredictionManagerService.this).exec(this, in, out, err, args, callback, resultReceiver);
        }

        private void runForUserLocked(String func, Consumer<AppPredictionPerUserService> c) {
            int userId = UserHandle.getCallingUserId();
            if (AppPredictionManagerService.this.getContext().checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") == 0 || AppPredictionManagerService.this.mServiceNameResolver.isTemporary(userId) || AppPredictionManagerService.this.mActivityTaskManagerInternal.isCallerRecents(Binder.getCallingUid())) {
                long origId = Binder.clearCallingIdentity();
                try {
                    synchronized (AppPredictionManagerService.this.mLock) {
                        c.accept((AppPredictionPerUserService) AppPredictionManagerService.this.getServiceForUserLocked(userId));
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } else {
                String msg = "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " expected caller to hold PACKAGE_USAGE_STATS permission";
                Slog.w(AppPredictionManagerService.TAG, msg);
                throw new SecurityException(msg);
            }
        }
    }
}
