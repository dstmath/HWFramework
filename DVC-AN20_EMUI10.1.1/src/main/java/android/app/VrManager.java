package android.app;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.VrManager;
import android.content.ComponentName;
import android.os.RemoteException;
import android.service.vr.IPersistentVrStateCallbacks;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.ArrayMap;
import java.util.Map;
import java.util.concurrent.Executor;

@SystemApi
public class VrManager {
    private Map<VrStateCallback, CallbackEntry> mCallbackMap = new ArrayMap();
    @UnsupportedAppUsage
    private final IVrManager mService;

    /* access modifiers changed from: private */
    public static class CallbackEntry {
        final VrStateCallback mCallback;
        final Executor mExecutor;
        final IPersistentVrStateCallbacks mPersistentStateCallback = new IPersistentVrStateCallbacks.Stub() {
            /* class android.app.VrManager.CallbackEntry.AnonymousClass2 */

            public /* synthetic */ void lambda$onPersistentVrStateChanged$0$VrManager$CallbackEntry$2(boolean enabled) {
                CallbackEntry.this.mCallback.onPersistentVrStateChanged(enabled);
            }

            @Override // android.service.vr.IPersistentVrStateCallbacks
            public void onPersistentVrStateChanged(boolean enabled) {
                CallbackEntry.this.mExecutor.execute(new Runnable(enabled) {
                    /* class android.app.$$Lambda$VrManager$CallbackEntry$2$KvHLIXm37igcOqTEl46YdjhHMk */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        VrManager.CallbackEntry.AnonymousClass2.this.lambda$onPersistentVrStateChanged$0$VrManager$CallbackEntry$2(this.f$1);
                    }
                });
            }
        };
        final IVrStateCallbacks mStateCallback = new IVrStateCallbacks.Stub() {
            /* class android.app.VrManager.CallbackEntry.AnonymousClass1 */

            public /* synthetic */ void lambda$onVrStateChanged$0$VrManager$CallbackEntry$1(boolean enabled) {
                CallbackEntry.this.mCallback.onVrStateChanged(enabled);
            }

            @Override // android.service.vr.IVrStateCallbacks
            public void onVrStateChanged(boolean enabled) {
                CallbackEntry.this.mExecutor.execute(new Runnable(enabled) {
                    /* class android.app.$$Lambda$VrManager$CallbackEntry$1$rgUBVVG1QhelpvAp8W3UQHDHJdU */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        VrManager.CallbackEntry.AnonymousClass1.this.lambda$onVrStateChanged$0$VrManager$CallbackEntry$1(this.f$1);
                    }
                });
            }
        };

        CallbackEntry(VrStateCallback callback, Executor executor) {
            this.mCallback = callback;
            this.mExecutor = executor;
        }
    }

    public VrManager(IVrManager service) {
        this.mService = service;
    }

    public void registerVrStateCallback(Executor executor, VrStateCallback callback) {
        if (callback != null && !this.mCallbackMap.containsKey(callback)) {
            CallbackEntry entry = new CallbackEntry(callback, executor);
            this.mCallbackMap.put(callback, entry);
            try {
                this.mService.registerListener(entry.mStateCallback);
                this.mService.registerPersistentVrStateListener(entry.mPersistentStateCallback);
            } catch (RemoteException e) {
                try {
                    unregisterVrStateCallback(callback);
                } catch (Exception e2) {
                    e.rethrowFromSystemServer();
                }
            }
        }
    }

    public void unregisterVrStateCallback(VrStateCallback callback) {
        CallbackEntry entry = this.mCallbackMap.remove(callback);
        if (entry != null) {
            try {
                this.mService.unregisterListener(entry.mStateCallback);
            } catch (RemoteException e) {
            }
            try {
                this.mService.unregisterPersistentVrStateListener(entry.mPersistentStateCallback);
            } catch (RemoteException e2) {
            }
        }
    }

    public boolean isVrModeEnabled() {
        try {
            return this.mService.getVrModeState();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return false;
        }
    }

    public boolean isPersistentVrModeEnabled() {
        try {
            return this.mService.getPersistentVrModeEnabled();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return false;
        }
    }

    public void setPersistentVrModeEnabled(boolean enabled) {
        try {
            this.mService.setPersistentVrModeEnabled(enabled);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setVr2dDisplayProperties(Vr2dDisplayProperties vr2dDisplayProp) {
        try {
            this.mService.setVr2dDisplayProperties(vr2dDisplayProp);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setAndBindVrCompositor(ComponentName componentName) {
        try {
            this.mService.setAndBindCompositor(componentName == null ? null : componentName.flattenToString());
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setStandbyEnabled(boolean standby) {
        try {
            this.mService.setStandbyEnabled(standby);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setVrInputMethod(ComponentName componentName) {
    }

    public int getVr2dDisplayId() {
        try {
            return this.mService.getVr2dDisplayId();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return -1;
        }
    }
}
