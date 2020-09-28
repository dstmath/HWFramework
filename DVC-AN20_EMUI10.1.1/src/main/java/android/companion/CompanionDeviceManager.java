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
    private final ICompanionDeviceManager mService;

    public static abstract class Callback {
        public abstract void onDeviceFound(IntentSender intentSender);

        public abstract void onFailure(CharSequence charSequence);
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
                this.mService.associate(request, new CallbackProxy(request, callback, Handler.mainIfNull(handler)), getCallingPackage());
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
                this.mContext.startIntentSender(this.mService.requestNotificationAccess(component).getIntentSender(), null, 0, 0, 0);
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
    /* access modifiers changed from: public */
    private Activity getActivity() {
        return (Activity) this.mContext;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCallingPackage() {
        return this.mContext.getPackageName();
    }

    /* access modifiers changed from: private */
    public class CallbackProxy extends IFindDeviceCallback.Stub implements Application.ActivityLifecycleCallbacks {
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

        @Override // android.companion.IFindDeviceCallback
        public void onSuccess(PendingIntent launcher) {
            lockAndPost($$Lambda$OThxsns9MAD5QsKURFQAFbt3qc.INSTANCE, launcher.getIntentSender());
        }

        @Override // android.companion.IFindDeviceCallback
        public void onFailure(CharSequence reason) {
            lockAndPost($$Lambda$ZUPGnRMz08ZrG1ogNO2O5Hso3I.INSTANCE, reason);
        }

        /* access modifiers changed from: package-private */
        public <T> void lockAndPost(BiConsumer<Callback, T> action, T payload) {
            synchronized (this.mLock) {
                if (this.mHandler != null) {
                    this.mHandler.post(new Runnable(action, payload) {
                        /* class android.companion.$$Lambda$CompanionDeviceManager$CallbackProxy$gkUVA3m3QgEEk8G84_kcBFARHvo */
                        private final /* synthetic */ BiConsumer f$1;
                        private final /* synthetic */ Object f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            CompanionDeviceManager.CallbackProxy.this.lambda$lockAndPost$0$CompanionDeviceManager$CallbackProxy(this.f$1, this.f$2);
                        }
                    });
                }
            }
        }

        public /* synthetic */ void lambda$lockAndPost$0$CompanionDeviceManager$CallbackProxy(BiConsumer action, Object payload) {
            Callback callback;
            synchronized (this.mLock) {
                callback = this.mCallback;
            }
            if (callback != null) {
                action.accept(callback, payload);
            }
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
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

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityStarted(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityResumed(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityPaused(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityStopped(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }
    }
}
