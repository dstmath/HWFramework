package android.companion;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.companion.CompanionDeviceManager;
import android.companion.IFindDeviceCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public final class CompanionDeviceManager {
    public static final String COMPANION_DEVICE_DISCOVERY_PACKAGE_NAME = "com.android.companiondevicemanager";
    private static final boolean DEBUG = false;
    public static final String EXTRA_DEVICE = "android.companion.extra.DEVICE";
    private static final String LOG_TAG = "CompanionDeviceManager";
    private final Context mContext;
    /* access modifiers changed from: private */
    public final ICompanionDeviceManager mService;

    public static abstract class Callback {
        public abstract void onDeviceFound(IntentSender intentSender);

        public abstract void onFailure(CharSequence charSequence);
    }

    private class CallbackProxy extends IFindDeviceCallback.Stub implements Application.ActivityLifecycleCallbacks {
        private Callback mCallback;
        private Handler mHandler;
        final Object mLock;
        private AssociationRequest mRequest;

        private CallbackProxy(AssociationRequest request, Callback callback, Handler handler) {
            this.mLock = new Object();
            this.mCallback = callback;
            this.mHandler = handler;
            this.mRequest = request;
            CompanionDeviceManager.this.getActivity().getApplication().registerActivityLifecycleCallbacks(this);
        }

        public void onSuccess(PendingIntent launcher) {
            lockAndPost($$Lambda$OThxsns9MAD5QsKURFQAFbt3qc.INSTANCE, launcher.getIntentSender());
        }

        public void onFailure(CharSequence reason) {
            lockAndPost($$Lambda$ZUPGnRMz08ZrG1ogNO2O5Hso3I.INSTANCE, reason);
        }

        /* access modifiers changed from: package-private */
        public <T> void lockAndPost(BiConsumer<Callback, T> action, T payload) {
            synchronized (this.mLock) {
                if (this.mHandler != null) {
                    this.mHandler.post(new Runnable(action, payload) {
                        private final /* synthetic */ BiConsumer f$1;
                        private final /* synthetic */ Object f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            CompanionDeviceManager.CallbackProxy.lambda$lockAndPost$0(CompanionDeviceManager.CallbackProxy.this, this.f$1, this.f$2);
                        }
                    });
                }
            }
        }

        public static /* synthetic */ void lambda$lockAndPost$0(CallbackProxy callbackProxy, BiConsumer action, Object payload) {
            Callback callback;
            synchronized (callbackProxy.mLock) {
                callback = callbackProxy.mCallback;
            }
            if (callback != null) {
                action.accept(callback, payload);
            }
        }

        public void onActivityDestroyed(Activity activity) {
            synchronized (this.mLock) {
                if (activity == CompanionDeviceManager.this.getActivity()) {
                    try {
                        CompanionDeviceManager.this.mService.stopScan(this.mRequest, this, CompanionDeviceManager.this.getCallingPackage());
                    } catch (RemoteException e) {
                        e.rethrowFromSystemServer();
                    }
                    CompanionDeviceManager.this.getActivity().getApplication().unregisterActivityLifecycleCallbacks(this);
                    this.mCallback = null;
                    this.mHandler = null;
                    this.mRequest = null;
                }
            }
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivityResumed(Activity activity) {
        }

        public void onActivityPaused(Activity activity) {
        }

        public void onActivityStopped(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }
    }

    public CompanionDeviceManager(ICompanionDeviceManager service, Context context) {
        this.mService = service;
        this.mContext = context;
    }

    public void associate(AssociationRequest request, Callback callback, Handler handler) {
        if (checkFeaturePresent()) {
            Preconditions.checkNotNull(request, "Request cannot be null");
            Preconditions.checkNotNull(callback, "Callback cannot be null");
            try {
                ICompanionDeviceManager iCompanionDeviceManager = this.mService;
                CallbackProxy callbackProxy = new CallbackProxy(request, callback, Handler.mainIfNull(handler));
                iCompanionDeviceManager.associate(request, callbackProxy, getCallingPackage());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public List<String> getAssociations() {
        if (!checkFeaturePresent()) {
            return Collections.emptyList();
        }
        try {
            return this.mService.getAssociations(getCallingPackage(), this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void disassociate(String deviceMacAddress) {
        if (checkFeaturePresent()) {
            try {
                this.mService.disassociate(deviceMacAddress, getCallingPackage());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void requestNotificationAccess(ComponentName component) {
        if (checkFeaturePresent()) {
            try {
                PendingIntent pendingIntent = this.mService.requestNotificationAccess(component);
                if (pendingIntent != null) {
                    this.mContext.startIntentSender(pendingIntent.getIntentSender(), null, 0, 0, 0);
                } else {
                    Log.e(LOG_TAG, "pendingIntent is null in function requestNotificationAccess");
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (IntentSender.SendIntentException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    public boolean hasNotificationAccess(ComponentName component) {
        if (!checkFeaturePresent()) {
            return false;
        }
        try {
            return this.mService.hasNotificationAccess(component);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean checkFeaturePresent() {
        return this.mService != null;
    }

    /* access modifiers changed from: private */
    public Activity getActivity() {
        return (Activity) this.mContext;
    }

    /* access modifiers changed from: private */
    public String getCallingPackage() {
        return this.mContext.getPackageName();
    }
}
