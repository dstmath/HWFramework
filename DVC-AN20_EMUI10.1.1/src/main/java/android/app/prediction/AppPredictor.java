package android.app.prediction;

import android.annotation.SystemApi;
import android.app.prediction.AppPredictor;
import android.app.prediction.IPredictionCallback;
import android.app.prediction.IPredictionManager;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.SettingsStringUtil;
import android.util.ArrayMap;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@SystemApi
public final class AppPredictor {
    private static final String TAG = AppPredictor.class.getSimpleName();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mIsClosed = new AtomicBoolean(false);
    private final IPredictionManager mPredictionManager = IPredictionManager.Stub.asInterface(ServiceManager.getService(Context.APP_PREDICTION_SERVICE));
    private final ArrayMap<Callback, CallbackWrapper> mRegisteredCallbacks = new ArrayMap<>();
    private final AppPredictionSessionId mSessionId;

    public interface Callback {
        void onTargetsAvailable(List<AppTarget> list);
    }

    AppPredictor(Context context, AppPredictionContext predictionContext) {
        this.mSessionId = new AppPredictionSessionId(context.getPackageName() + SettingsStringUtil.DELIMITER + UUID.randomUUID().toString());
        try {
            this.mPredictionManager.createPredictionSession(predictionContext, this.mSessionId);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to create predictor", e);
            e.rethrowAsRuntimeException();
        }
        this.mCloseGuard.open("close");
    }

    public void notifyAppTargetEvent(AppTargetEvent event) {
        if (!this.mIsClosed.get()) {
            try {
                this.mPredictionManager.notifyAppTargetEvent(this.mSessionId, event);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to notify app target event", e);
                e.rethrowAsRuntimeException();
            }
        } else {
            throw new IllegalStateException("This client has already been destroyed.");
        }
    }

    public void notifyLaunchLocationShown(String launchLocation, List<AppTargetId> targetIds) {
        if (!this.mIsClosed.get()) {
            try {
                this.mPredictionManager.notifyLaunchLocationShown(this.mSessionId, launchLocation, new ParceledListSlice(targetIds));
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to notify location shown event", e);
                e.rethrowAsRuntimeException();
            }
        } else {
            throw new IllegalStateException("This client has already been destroyed.");
        }
    }

    public void registerPredictionUpdates(Executor callbackExecutor, Callback callback) {
        if (this.mIsClosed.get()) {
            throw new IllegalStateException("This client has already been destroyed.");
        } else if (!this.mRegisteredCallbacks.containsKey(callback)) {
            try {
                Objects.requireNonNull(callback);
                CallbackWrapper callbackWrapper = new CallbackWrapper(callbackExecutor, new Consumer() {
                    /* class android.app.prediction.$$Lambda$1lqxDplfWlUwgBrOynX9L0oK_uA */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        AppPredictor.Callback.this.onTargetsAvailable((List) obj);
                    }
                });
                this.mPredictionManager.registerPredictionUpdates(this.mSessionId, callbackWrapper);
                this.mRegisteredCallbacks.put(callback, callbackWrapper);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to register for prediction updates", e);
                e.rethrowAsRuntimeException();
            }
        }
    }

    public void unregisterPredictionUpdates(Callback callback) {
        if (this.mIsClosed.get()) {
            throw new IllegalStateException("This client has already been destroyed.");
        } else if (this.mRegisteredCallbacks.containsKey(callback)) {
            try {
                this.mPredictionManager.unregisterPredictionUpdates(this.mSessionId, this.mRegisteredCallbacks.remove(callback));
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to unregister for prediction updates", e);
                e.rethrowAsRuntimeException();
            }
        }
    }

    public void requestPredictionUpdate() {
        if (!this.mIsClosed.get()) {
            try {
                this.mPredictionManager.requestPredictionUpdate(this.mSessionId);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to request prediction update", e);
                e.rethrowAsRuntimeException();
            }
        } else {
            throw new IllegalStateException("This client has already been destroyed.");
        }
    }

    public void sortTargets(List<AppTarget> targets, Executor callbackExecutor, Consumer<List<AppTarget>> callback) {
        if (!this.mIsClosed.get()) {
            try {
                this.mPredictionManager.sortAppTargets(this.mSessionId, new ParceledListSlice(targets), new CallbackWrapper(callbackExecutor, callback));
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to sort targets", e);
                e.rethrowAsRuntimeException();
            }
        } else {
            throw new IllegalStateException("This client has already been destroyed.");
        }
    }

    public void destroy() {
        if (!this.mIsClosed.getAndSet(true)) {
            CloseGuard closeGuard = this.mCloseGuard;
            if (closeGuard != null) {
                closeGuard.close();
            } else {
                Log.e(TAG, "mCloseGuard is null, in destroy()");
            }
            try {
                this.mPredictionManager.onDestroyPredictionSession(this.mSessionId);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to notify app target event", e);
                e.rethrowAsRuntimeException();
            }
        } else {
            throw new IllegalStateException("This client has already been destroyed.");
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            if (!this.mIsClosed.get()) {
                destroy();
            }
        } finally {
            super.finalize();
        }
    }

    public AppPredictionSessionId getSessionId() {
        return this.mSessionId;
    }

    static class CallbackWrapper extends IPredictionCallback.Stub {
        private final Consumer<List<AppTarget>> mCallback;
        private final Executor mExecutor;

        CallbackWrapper(Executor callbackExecutor, Consumer<List<AppTarget>> callback) {
            this.mCallback = callback;
            this.mExecutor = callbackExecutor;
        }

        @Override // android.app.prediction.IPredictionCallback
        public void onResult(ParceledListSlice result) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable(result) {
                    /* class android.app.prediction.$$Lambda$AppPredictor$CallbackWrapper$gCs3O3sYRlsXAOdelds31867YXo */
                    private final /* synthetic */ ParceledListSlice f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        AppPredictor.CallbackWrapper.this.lambda$onResult$0$AppPredictor$CallbackWrapper(this.f$1);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public /* synthetic */ void lambda$onResult$0$AppPredictor$CallbackWrapper(ParceledListSlice result) {
            this.mCallback.accept(result.getList());
        }
    }
}
