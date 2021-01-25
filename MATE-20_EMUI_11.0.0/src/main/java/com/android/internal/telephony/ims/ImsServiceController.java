package com.android.internal.telephony.ims;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.IPackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.ims.ImsService;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.aidl.IImsRegistration;
import android.telephony.ims.aidl.IImsServiceController;
import android.telephony.ims.stub.ImsFeatureConfiguration;
import android.util.Log;
import com.android.ims.internal.IImsFeatureStatusCallback;
import com.android.ims.internal.IImsServiceFeatureCallback;
import com.android.internal.telephony.ExponentialBackoff;
import com.android.internal.telephony.ims.ImsServiceController;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ImsServiceController {
    private static final String LOG_TAG = "ImsServiceController";
    private static final int REBIND_MAXIMUM_DELAY_MS = 60000;
    private static final int REBIND_START_DELAY_MS = 2000;
    private ExponentialBackoff mBackoff;
    private ImsServiceControllerCallbacks mCallbacks;
    private final ComponentName mComponentName;
    protected final Context mContext;
    private ImsService.Listener mFeatureChangedListener = new ImsService.Listener() {
        /* class com.android.internal.telephony.ims.ImsServiceController.AnonymousClass1 */

        public void onUpdateSupportedImsFeatures(ImsFeatureConfiguration c) {
            if (ImsServiceController.this.mCallbacks != null) {
                ImsServiceController.this.mCallbacks.imsServiceFeaturesChanged(c, ImsServiceController.this);
            }
        }
    };
    private Set<ImsFeatureStatusCallback> mFeatureStatusCallbacks = new HashSet();
    private final HandlerThread mHandlerThread = new HandlerThread("ImsServiceControllerHandler");
    private IImsServiceController mIImsServiceController;
    private HashSet<ImsFeatureContainer> mImsFeatureBinders = new HashSet<>();
    private HashSet<ImsFeatureConfiguration.FeatureSlotPair> mImsFeatures;
    private ImsServiceConnection mImsServiceConnection;
    private Set<IImsServiceFeatureCallback> mImsStatusCallbacks = ConcurrentHashMap.newKeySet();
    private boolean mIsBinding = false;
    private boolean mIsBound = false;
    private final Object mLock = new Object();
    private final IPackageManager mPackageManager;
    private RebindRetry mRebindRetry = new RebindRetry() {
        /* class com.android.internal.telephony.ims.ImsServiceController.AnonymousClass3 */

        @Override // com.android.internal.telephony.ims.ImsServiceController.RebindRetry
        public long getStartDelay() {
            return 2000;
        }

        @Override // com.android.internal.telephony.ims.ImsServiceController.RebindRetry
        public long getMaximumDelay() {
            return 60000;
        }
    };
    private Runnable mRestartImsServiceRunnable = new Runnable() {
        /* class com.android.internal.telephony.ims.ImsServiceController.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (ImsServiceController.this.mLock) {
                if (!ImsServiceController.this.mIsBound) {
                    ImsServiceController.this.bind(ImsServiceController.this.mImsFeatures);
                }
            }
        }
    };

    public interface ImsServiceControllerCallbacks {
        void imsServiceFeatureCreated(int i, int i2, ImsServiceController imsServiceController);

        void imsServiceFeatureRemoved(int i, int i2, ImsServiceController imsServiceController);

        void imsServiceFeaturesChanged(ImsFeatureConfiguration imsFeatureConfiguration, ImsServiceController imsServiceController);
    }

    public interface RebindRetry {
        long getMaximumDelay();

        long getStartDelay();
    }

    public class ImsServiceConnection implements ServiceConnection {
        ImsServiceConnection() {
            ImsServiceController.this = this$0;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            ImsServiceController.this.mBackoff.stop();
            synchronized (ImsServiceController.this.mLock) {
                ImsServiceController.this.mIsBound = true;
                ImsServiceController.this.mIsBinding = false;
                Log.i(ImsServiceController.LOG_TAG, "ImsService(" + name + "): onServiceConnected with binder: " + service);
                if (service != null) {
                    try {
                        ImsServiceController.this.setServiceController(service);
                        ImsServiceController.this.notifyImsServiceReady();
                        Iterator it = ImsServiceController.this.mImsFeatures.iterator();
                        while (it.hasNext()) {
                            ImsServiceController.this.addImsServiceFeature((ImsFeatureConfiguration.FeatureSlotPair) it.next());
                        }
                    } catch (RemoteException e) {
                        ImsServiceController.this.mIsBound = false;
                        ImsServiceController.this.mIsBinding = false;
                        cleanupConnection();
                        ImsServiceController.this.startDelayedRebindToService();
                        Log.e(ImsServiceController.LOG_TAG, "ImsService(" + name + ") RemoteException:" + e.getMessage());
                    }
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            synchronized (ImsServiceController.this.mLock) {
                ImsServiceController.this.mIsBinding = false;
            }
            cleanupConnection();
            Log.w(ImsServiceController.LOG_TAG, "ImsService(" + name + "): onServiceDisconnected. Waiting...");
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            synchronized (ImsServiceController.this.mLock) {
                ImsServiceController.this.mIsBinding = false;
                ImsServiceController.this.mIsBound = false;
            }
            cleanupConnection();
            Log.w(ImsServiceController.LOG_TAG, "ImsService(" + name + "): onBindingDied. Starting rebind...");
            ImsServiceController.this.startDelayedRebindToService();
        }

        private void cleanupConnection() {
            ImsServiceController.this.cleanupAllFeatures();
            ImsServiceController.this.cleanUpService();
        }
    }

    public class ImsFeatureContainer {
        public int featureType;
        private IInterface mBinder;
        public int slotId;

        ImsFeatureContainer(int slotId2, int featureType2, IInterface binder) {
            ImsServiceController.this = r1;
            this.slotId = slotId2;
            this.featureType = featureType2;
            this.mBinder = binder;
        }

        public <T extends IInterface> T resolve(Class<T> className) {
            return className.cast(this.mBinder);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ImsFeatureContainer that = (ImsFeatureContainer) o;
            if (this.slotId != that.slotId || this.featureType != that.featureType) {
                return false;
            }
            IInterface iInterface = this.mBinder;
            if (iInterface != null) {
                return iInterface.equals(that.mBinder);
            }
            if (that.mBinder == null) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            int result = ((this.slotId * 31) + this.featureType) * 31;
            IInterface iInterface = this.mBinder;
            return result + (iInterface != null ? iInterface.hashCode() : 0);
        }
    }

    public class ImsFeatureStatusCallback {
        private final IImsFeatureStatusCallback mCallback = new IImsFeatureStatusCallback.Stub() {
            /* class com.android.internal.telephony.ims.ImsServiceController.ImsFeatureStatusCallback.AnonymousClass1 */

            public void notifyImsFeatureStatus(int featureStatus) {
                Log.i(ImsServiceController.LOG_TAG, "notifyImsFeatureStatus: slot=" + ImsFeatureStatusCallback.this.mSlotId + ", feature=" + ImsFeatureStatusCallback.this.mFeatureType + ", status=" + featureStatus);
                ImsServiceController.this.sendImsFeatureStatusChanged(ImsFeatureStatusCallback.this.mSlotId, ImsFeatureStatusCallback.this.mFeatureType, featureStatus);
            }
        };
        private int mFeatureType;
        private int mSlotId;

        ImsFeatureStatusCallback(int slotId, int featureType) {
            ImsServiceController.this = r1;
            this.mSlotId = slotId;
            this.mFeatureType = featureType;
        }

        public IImsFeatureStatusCallback getCallback() {
            return this.mCallback;
        }
    }

    public ImsServiceController(Context context, ComponentName componentName, ImsServiceControllerCallbacks callbacks) {
        this.mContext = context;
        this.mComponentName = componentName;
        this.mCallbacks = callbacks;
        this.mHandlerThread.start();
        this.mBackoff = new ExponentialBackoff(this.mRebindRetry.getStartDelay(), this.mRebindRetry.getMaximumDelay(), 2, this.mHandlerThread.getLooper(), this.mRestartImsServiceRunnable);
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    }

    public ImsServiceController(Context context, ComponentName componentName, ImsServiceControllerCallbacks callbacks, Handler handler, RebindRetry rebindRetry) {
        this.mContext = context;
        this.mComponentName = componentName;
        this.mCallbacks = callbacks;
        this.mBackoff = new ExponentialBackoff(rebindRetry.getStartDelay(), rebindRetry.getMaximumDelay(), 2, handler, this.mRestartImsServiceRunnable);
        this.mPackageManager = null;
    }

    public boolean bind(HashSet<ImsFeatureConfiguration.FeatureSlotPair> imsFeatureSet) {
        synchronized (this.mLock) {
            if (this.mIsBound || this.mIsBinding) {
                return false;
            }
            this.mIsBinding = true;
            this.mImsFeatures = imsFeatureSet;
            grantPermissionsToService();
            Intent imsServiceIntent = new Intent(getServiceInterface()).setComponent(this.mComponentName);
            this.mImsServiceConnection = new ImsServiceConnection();
            Log.i(LOG_TAG, "Binding ImsService:" + this.mComponentName);
            try {
                boolean bindSucceeded = startBindToService(imsServiceIntent, this.mImsServiceConnection, 67108929);
                if (!bindSucceeded) {
                    this.mIsBinding = false;
                    this.mBackoff.notifyFailed();
                }
                return bindSucceeded;
            } catch (Exception e) {
                this.mBackoff.notifyFailed();
                Log.e(LOG_TAG, "Error binding (" + this.mComponentName + ") with exception, rebinding in " + this.mBackoff.getCurrentDelay() + " ms");
                return false;
            }
        }
    }

    public boolean startBindToService(Intent intent, ImsServiceConnection connection, int flags) {
        return this.mContext.bindService(intent, connection, flags);
    }

    public void unbind() {
        synchronized (this.mLock) {
            this.mBackoff.stop();
            if (this.mImsServiceConnection != null) {
                changeImsServiceFeatures(new HashSet<>());
                removeImsServiceFeatureCallbacks();
                Log.i(LOG_TAG, "Unbinding ImsService: " + this.mComponentName);
                this.mContext.unbindService(this.mImsServiceConnection);
                cleanUpService();
            }
        }
    }

    public void changeImsServiceFeatures(HashSet<ImsFeatureConfiguration.FeatureSlotPair> newImsFeatures) {
        synchronized (this.mLock) {
            Log.i(LOG_TAG, "Features changed (" + this.mImsFeatures + "->" + newImsFeatures + ") for ImsService: " + this.mComponentName);
            Collection<?> oldImsFeatures = new HashSet<>(this.mImsFeatures);
            this.mImsFeatures = newImsFeatures;
            if (this.mIsBound) {
                HashSet<ImsFeatureConfiguration.FeatureSlotPair> newFeatures = new HashSet<>(this.mImsFeatures);
                newFeatures.removeAll(oldImsFeatures);
                Iterator<ImsFeatureConfiguration.FeatureSlotPair> it = newFeatures.iterator();
                while (it.hasNext()) {
                    addImsServiceFeature(it.next());
                }
                HashSet<ImsFeatureConfiguration.FeatureSlotPair> oldFeatures = new HashSet<>((Collection<? extends ImsFeatureConfiguration.FeatureSlotPair>) oldImsFeatures);
                oldFeatures.removeAll(this.mImsFeatures);
                Iterator<ImsFeatureConfiguration.FeatureSlotPair> it2 = oldFeatures.iterator();
                while (it2.hasNext()) {
                    removeImsServiceFeature(it2.next());
                }
            }
        }
    }

    public IImsServiceController getImsServiceController() {
        return this.mIImsServiceController;
    }

    public long getRebindDelay() {
        return this.mBackoff.getCurrentDelay();
    }

    public void stopBackoffTimerForTesting() {
        this.mBackoff.stop();
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public void addImsServiceFeatureCallback(IImsServiceFeatureCallback callback) {
        if (callback != null) {
            this.mImsStatusCallbacks.add(callback);
            synchronized (this.mLock) {
                if (this.mImsFeatures != null && !this.mImsFeatures.isEmpty()) {
                    try {
                        Iterator<ImsFeatureConfiguration.FeatureSlotPair> it = this.mImsFeatures.iterator();
                        while (it.hasNext()) {
                            ImsFeatureConfiguration.FeatureSlotPair i = it.next();
                            callback.imsFeatureCreated(i.slotId, i.featureType);
                        }
                    } catch (RemoteException e) {
                        Log.w(LOG_TAG, "addImsServiceFeatureCallback: exception notifying callback");
                    }
                }
            }
        }
    }

    public void enableIms(int slotId) {
        try {
            synchronized (this.mLock) {
                if (isServiceControllerAvailable()) {
                    this.mIImsServiceController.enableIms(slotId);
                }
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "Couldn't enable IMS: " + e.getMessage());
        }
    }

    public void disableIms(int slotId) {
        try {
            synchronized (this.mLock) {
                if (isServiceControllerAvailable()) {
                    this.mIImsServiceController.disableIms(slotId);
                }
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "Couldn't disable IMS: " + e.getMessage());
        }
    }

    public IImsMmTelFeature getMmTelFeature(int slotId) {
        synchronized (this.mLock) {
            ImsFeatureContainer f = getImsFeatureContainer(slotId, 1);
            if (f == null) {
                Log.w(LOG_TAG, "Requested null MMTelFeature on slot " + slotId);
                return null;
            }
            return f.resolve(IImsMmTelFeature.class);
        }
    }

    public IImsRcsFeature getRcsFeature(int slotId) {
        synchronized (this.mLock) {
            ImsFeatureContainer f = getImsFeatureContainer(slotId, 2);
            if (f == null) {
                Log.w(LOG_TAG, "Requested null RcsFeature on slot " + slotId);
                return null;
            }
            return f.resolve(IImsRcsFeature.class);
        }
    }

    public IImsRegistration getRegistration(int slotId) {
        IImsRegistration registration;
        synchronized (this.mLock) {
            registration = isServiceControllerAvailable() ? this.mIImsServiceController.getRegistration(slotId) : null;
        }
        return registration;
    }

    public IImsConfig getConfig(int slotId) {
        IImsConfig config;
        synchronized (this.mLock) {
            config = isServiceControllerAvailable() ? this.mIImsServiceController.getConfig(slotId) : null;
        }
        return config;
    }

    public void notifyImsServiceReady() {
        synchronized (this.mLock) {
            if (isServiceControllerAvailable()) {
                Log.i(LOG_TAG, "notifyImsServiceReady");
                this.mIImsServiceController.setListener(this.mFeatureChangedListener);
                this.mIImsServiceController.notifyImsServiceReadyForFeatureCreation();
            }
        }
    }

    public String getServiceInterface() {
        return "android.telephony.ims.ImsService";
    }

    public void setServiceController(IBinder serviceController) {
        this.mIImsServiceController = IImsServiceController.Stub.asInterface(serviceController);
    }

    public boolean isBound() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIsBound;
        }
        return z;
    }

    public boolean isServiceControllerAvailable() {
        return this.mIImsServiceController != null;
    }

    public void removeImsServiceFeatureCallbacks() {
        this.mImsStatusCallbacks.clear();
    }

    private void startDelayedRebindToService() {
        this.mBackoff.start();
    }

    private void grantPermissionsToService() {
        Log.i(LOG_TAG, "Granting Runtime permissions to:" + getComponentName());
        String[] pkgToGrant = {this.mComponentName.getPackageName()};
        try {
            if (this.mPackageManager != null) {
                this.mPackageManager.grantDefaultPermissionsToEnabledImsServices(pkgToGrant, this.mContext.getUserId());
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "Unable to grant permissions, binder died.");
        }
    }

    private void sendImsFeatureCreatedCallback(int slot, int feature) {
        Iterator<IImsServiceFeatureCallback> i = this.mImsStatusCallbacks.iterator();
        while (i.hasNext()) {
            try {
                i.next().imsFeatureCreated(slot, feature);
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "sendImsFeatureCreatedCallback: Binder died, removing callback. Exception:" + e.getMessage());
                i.remove();
            }
        }
    }

    private void sendImsFeatureRemovedCallback(int slot, int feature) {
        Iterator<IImsServiceFeatureCallback> i = this.mImsStatusCallbacks.iterator();
        while (i.hasNext()) {
            try {
                i.next().imsFeatureRemoved(slot, feature);
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "sendImsFeatureRemovedCallback: Binder died, removing callback. Exception:" + e.getMessage());
                i.remove();
            }
        }
    }

    private void sendImsFeatureStatusChanged(int slot, int feature, int status) {
        Iterator<IImsServiceFeatureCallback> i = this.mImsStatusCallbacks.iterator();
        while (i.hasNext()) {
            try {
                i.next().imsStatusChanged(slot, feature, status);
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "sendImsFeatureStatusChanged: Binder died, removing callback. Exception:" + e.getMessage());
                i.remove();
            }
        }
    }

    private void addImsServiceFeature(ImsFeatureConfiguration.FeatureSlotPair featurePair) {
        if (!isServiceControllerAvailable() || this.mCallbacks == null) {
            Log.w(LOG_TAG, "addImsServiceFeature called with null values.");
            return;
        }
        if (featurePair.featureType != 0) {
            ImsFeatureStatusCallback c = new ImsFeatureStatusCallback(featurePair.slotId, featurePair.featureType);
            this.mFeatureStatusCallbacks.add(c);
            addImsFeatureBinder(featurePair.slotId, featurePair.featureType, createImsFeature(featurePair.slotId, featurePair.featureType, c.getCallback()));
            this.mCallbacks.imsServiceFeatureCreated(featurePair.slotId, featurePair.featureType, this);
        } else {
            Log.i(LOG_TAG, "supports emergency calling on slot " + featurePair.slotId);
        }
        sendImsFeatureCreatedCallback(featurePair.slotId, featurePair.featureType);
    }

    private void removeImsServiceFeature(ImsFeatureConfiguration.FeatureSlotPair featurePair) {
        if (!isServiceControllerAvailable() || this.mCallbacks == null) {
            Log.w(LOG_TAG, "removeImsServiceFeature called with null values.");
            return;
        }
        if (featurePair.featureType != 0) {
            IImsFeatureStatusCallback iImsFeatureStatusCallback = null;
            ImsFeatureStatusCallback callbackToRemove = this.mFeatureStatusCallbacks.stream().filter(new Predicate(featurePair) {
                /* class com.android.internal.telephony.ims.$$Lambda$ImsServiceController$8NvoVXkZRS5LCradATGpNMBXAqg */
                private final /* synthetic */ ImsFeatureConfiguration.FeatureSlotPair f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ImsServiceController.lambda$removeImsServiceFeature$0(this.f$0, (ImsServiceController.ImsFeatureStatusCallback) obj);
                }
            }).findFirst().orElse(null);
            if (callbackToRemove != null) {
                this.mFeatureStatusCallbacks.remove(callbackToRemove);
            }
            removeImsFeatureBinder(featurePair.slotId, featurePair.featureType);
            this.mCallbacks.imsServiceFeatureRemoved(featurePair.slotId, featurePair.featureType, this);
            try {
                int i = featurePair.slotId;
                int i2 = featurePair.featureType;
                if (callbackToRemove != null) {
                    iImsFeatureStatusCallback = callbackToRemove.getCallback();
                }
                removeImsFeature(i, i2, iImsFeatureStatusCallback);
            } catch (RemoteException e) {
                Log.i(LOG_TAG, "Couldn't remove feature {" + featurePair.featureType + "}, connection is down: " + e.getMessage());
            }
        } else {
            Log.i(LOG_TAG, "doesn't support emergency calling on slot " + featurePair.slotId);
        }
        sendImsFeatureRemovedCallback(featurePair.slotId, featurePair.featureType);
    }

    static /* synthetic */ boolean lambda$removeImsServiceFeature$0(ImsFeatureConfiguration.FeatureSlotPair featurePair, ImsFeatureStatusCallback c) {
        return c.mSlotId == featurePair.slotId && c.mFeatureType == featurePair.featureType;
    }

    public IInterface createImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) {
        if (featureType == 1) {
            return this.mIImsServiceController.createMmTelFeature(slotId, c);
        }
        if (featureType != 2) {
            return null;
        }
        return this.mIImsServiceController.createRcsFeature(slotId, c);
    }

    public void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) {
        this.mIImsServiceController.removeImsFeature(slotId, featureType, c);
    }

    private void addImsFeatureBinder(int slotId, int featureType, IInterface b) {
        this.mImsFeatureBinders.add(new ImsFeatureContainer(slotId, featureType, b));
    }

    private void removeImsFeatureBinder(int slotId, int featureType) {
        ImsFeatureContainer container = (ImsFeatureContainer) this.mImsFeatureBinders.stream().filter(new Predicate(slotId, featureType) {
            /* class com.android.internal.telephony.ims.$$Lambda$ImsServiceController$rO36xbdAp6IQ5hFqLNNXDJPMers */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ImsServiceController.lambda$removeImsFeatureBinder$1(this.f$0, this.f$1, (ImsServiceController.ImsFeatureContainer) obj);
            }
        }).findFirst().orElse(null);
        if (container != null) {
            this.mImsFeatureBinders.remove(container);
        }
    }

    static /* synthetic */ boolean lambda$removeImsFeatureBinder$1(int slotId, int featureType, ImsFeatureContainer f) {
        return f.slotId == slotId && f.featureType == featureType;
    }

    private ImsFeatureContainer getImsFeatureContainer(int slotId, int featureType) {
        return (ImsFeatureContainer) this.mImsFeatureBinders.stream().filter(new Predicate(slotId, featureType) {
            /* class com.android.internal.telephony.ims.$$Lambda$ImsServiceController$w3xbtqEhKr7IY81qFuw0e94p84Y */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ImsServiceController.lambda$getImsFeatureContainer$2(this.f$0, this.f$1, (ImsServiceController.ImsFeatureContainer) obj);
            }
        }).findFirst().orElse(null);
    }

    static /* synthetic */ boolean lambda$getImsFeatureContainer$2(int slotId, int featureType, ImsFeatureContainer f) {
        return f.slotId == slotId && f.featureType == featureType;
    }

    private void cleanupAllFeatures() {
        synchronized (this.mLock) {
            Iterator<ImsFeatureConfiguration.FeatureSlotPair> it = this.mImsFeatures.iterator();
            while (it.hasNext()) {
                removeImsServiceFeature(it.next());
            }
            removeImsServiceFeatureCallbacks();
        }
    }

    private void cleanUpService() {
        synchronized (this.mLock) {
            this.mImsServiceConnection = null;
            setServiceController(null);
        }
    }
}
