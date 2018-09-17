package android.companion;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.PendingIntent;
import android.companion.-$Lambda$5JzRO2PPKyhIE1CwHHamoNS-two.AnonymousClass1;
import android.companion.IFindDeviceCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.Collections;
import java.util.List;

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

    private class CallbackProxy extends Stub implements ActivityLifecycleCallbacks {
        private Callback mCallback;
        private Handler mHandler;
        private AssociationRequest mRequest;

        /* synthetic */ CallbackProxy(CompanionDeviceManager this$0, AssociationRequest request, Callback callback, Handler handler, CallbackProxy -this4) {
            this(request, callback, handler);
        }

        private CallbackProxy(AssociationRequest request, Callback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
            this.mRequest = request;
            CompanionDeviceManager.this.getActivity().getApplication().registerActivityLifecycleCallbacks(this);
        }

        /* synthetic */ void lambda$-android_companion_CompanionDeviceManager$CallbackProxy_10909(PendingIntent launcher) {
            this.mCallback.onDeviceFound(launcher.getIntentSender());
        }

        public void onSuccess(PendingIntent launcher) {
            this.mHandler.post(new AnonymousClass1(this, launcher));
        }

        /* synthetic */ void lambda$-android_companion_CompanionDeviceManager$CallbackProxy_11077(CharSequence reason) {
            this.mCallback.onFailure(reason);
        }

        public void onFailure(CharSequence reason) {
            this.mHandler.post(new -$Lambda$5JzRO2PPKyhIE1CwHHamoNS-two(this, reason));
        }

        public void onActivityDestroyed(Activity activity) {
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
                this.mService.associate(request, new CallbackProxy(this, request, callback, Handler.mainIfNull(handler), null), getCallingPackage());
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
            } catch (SendIntentException e2) {
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

    private Activity getActivity() {
        return (Activity) this.mContext;
    }

    private String getCallingPackage() {
        return this.mContext.getPackageName();
    }
}
