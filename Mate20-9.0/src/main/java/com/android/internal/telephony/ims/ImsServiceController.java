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
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.ExponentialBackoff;
import com.android.internal.telephony.ims.ImsServiceController;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ImsServiceController {
    private static final String LOG_TAG = "ImsServiceController";
    private static final int REBIND_MAXIMUM_DELAY_MS = 60000;
    private static final int REBIND_START_DELAY_MS = 2000;
    /* access modifiers changed from: private */
    public ExponentialBackoff mBackoff;
    /* access modifiers changed from: private */
    public ImsServiceControllerCallbacks mCallbacks;
    private final ComponentName mComponentName;
    protected final Context mContext;
    private ImsService.Listener mFeatureChangedListener = new ImsService.Listener() {
        public void onUpdateSupportedImsFeatures(ImsFeatureConfiguration c) {
            if (ImsServiceController.this.mCallbacks != null) {
                ImsServiceController.this.mCallbacks.imsServiceFeaturesChanged(c, ImsServiceController.this);
            }
        }
    };
    private Set<ImsFeatureStatusCallback> mFeatureStatusCallbacks = new HashSet();
    private final HandlerThread mHandlerThread = new HandlerThread("ImsServiceControllerHandler");
    private IImsServiceController mIImsServiceController;
    /* access modifiers changed from: private */
    public ImsDeathRecipient mImsDeathRecipient;
    private HashSet<ImsFeatureContainer> mImsFeatureBinders = new HashSet<>();
    /* access modifiers changed from: private */
    public HashSet<ImsFeatureConfiguration.FeatureSlotPair> mImsFeatures;
    private ImsServiceConnection mImsServiceConnection;
    /* access modifiers changed from: private */
    public IBinder mImsServiceControllerBinder;
    private Set<IImsServiceFeatureCallback> mImsStatusCallbacks = ConcurrentHashMap.newKeySet();
    /* access modifiers changed from: private */
    public boolean mIsBinding = false;
    /* access modifiers changed from: private */
    public boolean mIsBound = false;
    protected final Object mLock = new Object();
    private final IPackageManager mPackageManager;
    private RebindRetry mRebindRetry = new RebindRetry() {
        public long getStartDelay() {
            return 2000;
        }

        public long getMaximumDelay() {
            return 60000;
        }
    };
    private Runnable mRestartImsServiceRunnable = new Runnable() {
        public void run() {
            synchronized (ImsServiceController.this.mLock) {
                if (!ImsServiceController.this.mIsBound) {
                    ImsServiceController.this.bind(ImsServiceController.this.mImsFeatures);
                }
            }
        }
    };

    class ImsDeathRecipient implements IBinder.DeathRecipient {
        private ComponentName mComponentName;

        ImsDeathRecipient(ComponentName name) {
            this.mComponentName = name;
        }

        public void binderDied() {
            Log.e(ImsServiceController.LOG_TAG, "ImsService(" + this.mComponentName + ") died. Restarting...");
            synchronized (ImsServiceController.this.mLock) {
                boolean unused = ImsServiceController.this.mIsBinding = false;
                boolean unused2 = ImsServiceController.this.mIsBound = false;
            }
            ImsServiceController.this.cleanupAllFeatures();
            ImsServiceController.this.cleanUpService();
            ImsServiceController.this.startDelayedRebindToService();
        }
    }

    private class ImsFeatureContainer {
        public int featureType;
        private IInterface mBinder;
        public int slotId;

        ImsFeatureContainer(int slotId2, int featureType2, IInterface binder) {
            this.slotId = slotId2;
            this.featureType = featureType2;
            this.mBinder = binder;
        }

        public <T extends IInterface> T resolve(Class<T> className) {
            return (IInterface) className.cast(this.mBinder);
        }

        public boolean equals(Object o) {
            boolean z = true;
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
            if (this.mBinder != null) {
                z = this.mBinder.equals(that.mBinder);
            } else if (that.mBinder != null) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (31 * ((31 * this.slotId) + this.featureType)) + (this.mBinder != null ? this.mBinder.hashCode() : 0);
        }
    }

    private class ImsFeatureStatusCallback {
        private final IImsFeatureStatusCallback mCallback = new IImsFeatureStatusCallback.Stub() {
            public void notifyImsFeatureStatus(int featureStatus) throws RemoteException {
                Log.i(ImsServiceController.LOG_TAG, "notifyImsFeatureStatus: slot=" + ImsFeatureStatusCallback.this.mSlotId + ", feature=" + ImsFeatureStatusCallback.this.mFeatureType + ", status=" + featureStatus);
                ImsServiceController.this.sendImsFeatureStatusChanged(ImsFeatureStatusCallback.this.mSlotId, ImsFeatureStatusCallback.this.mFeatureType, featureStatus);
            }
        };
        /* access modifiers changed from: private */
        public int mFeatureType;
        /* access modifiers changed from: private */
        public int mSlotId;

        ImsFeatureStatusCallback(int slotId, int featureType) {
            this.mSlotId = slotId;
            this.mFeatureType = featureType;
        }

        public IImsFeatureStatusCallback getCallback() {
            return this.mCallback;
        }
    }

    class ImsServiceConnection implements ServiceConnection {
        ImsServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            ImsServiceController.this.mBackoff.stop();
            synchronized (ImsServiceController.this.mLock) {
                boolean unused = ImsServiceController.this.mIsBound = true;
                boolean unused2 = ImsServiceController.this.mIsBinding = false;
                Log.d(ImsServiceController.LOG_TAG, "ImsService(" + name + "): onServiceConnected with binder: " + service);
                if (service != null) {
                    ImsDeathRecipient unused3 = ImsServiceController.this.mImsDeathRecipient = new ImsDeathRecipient(name);
                    try {
                        service.linkToDeath(ImsServiceController.this.mImsDeathRecipient, 0);
                        IBinder unused4 = ImsServiceController.this.mImsServiceControllerBinder = service;
                        ImsServiceController.this.setServiceController(service);
                        ImsServiceController.this.notifyImsServiceReady();
                        Iterator it = ImsServiceController.this.mImsFeatures.iterator();
                        while (it.hasNext()) {
                            ImsServiceController.this.addImsServiceFeature((ImsFeatureConfiguration.FeatureSlotPair) it.next());
                        }
                    } catch (RemoteException e) {
                        boolean unused5 = ImsServiceController.this.mIsBound = false;
                        boolean unused6 = ImsServiceController.this.mIsBinding = false;
                        if (ImsServiceController.this.mImsDeathRecipient != null) {
                            ImsServiceController.this.mImsDeathRecipient.binderDied();
                        }
                        Log.e(ImsServiceController.LOG_TAG, "ImsService(" + name + ") RemoteException:" + e.getMessage());
                    }
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (ImsServiceController.this.mLock) {
                boolean unused = ImsServiceController.this.mIsBinding = false;
            }
            cleanupConnection();
            Log.w(ImsServiceController.LOG_TAG, "ImsService(" + name + "): onServiceDisconnected. Waiting...");
        }

        public void onBindingDied(ComponentName name) {
            synchronized (ImsServiceController.this.mLock) {
                boolean unused = ImsServiceController.this.mIsBinding = false;
                boolean unused2 = ImsServiceController.this.mIsBound = false;
            }
            cleanupConnection();
            Log.w(ImsServiceController.LOG_TAG, "ImsService(" + name + "): onBindingDied. Starting rebind...");
            ImsServiceController.this.startDelayedRebindToService();
        }

        private void cleanupConnection() {
            if (ImsServiceController.this.isServiceControllerAvailable()) {
                ImsServiceController.this.mImsServiceControllerBinder.unlinkToDeath(ImsServiceController.this.mImsDeathRecipient, 0);
            }
            ImsServiceController.this.cleanupAllFeatures();
            ImsServiceController.this.cleanUpService();
        }
    }

    public interface ImsServiceControllerCallbacks {
        void imsServiceFeatureCreated(int i, int i2, ImsServiceController imsServiceController);

        void imsServiceFeatureRemoved(int i, int i2, ImsServiceController imsServiceController);

        void imsServiceFeaturesChanged(ImsFeatureConfiguration imsFeatureConfiguration, ImsServiceController imsServiceController);
    }

    @VisibleForTesting
    public interface RebindRetry {
        long getMaximumDelay();

        long getStartDelay();
    }

    public ImsServiceController(Context context, ComponentName componentName, ImsServiceControllerCallbacks callbacks) {
        this.mContext = context;
        this.mComponentName = componentName;
        this.mCallbacks = callbacks;
        this.mHandlerThread.start();
        ExponentialBackoff exponentialBackoff = new ExponentialBackoff(this.mRebindRetry.getStartDelay(), this.mRebindRetry.getMaximumDelay(), 2, this.mHandlerThread.getLooper(), this.mRestartImsServiceRunnable);
        this.mBackoff = exponentialBackoff;
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    }

    @VisibleForTesting
    public ImsServiceController(Context context, ComponentName componentName, ImsServiceControllerCallbacks callbacks, Handler handler, RebindRetry rebindRetry) {
        this.mContext = context;
        this.mComponentName = componentName;
        this.mCallbacks = callbacks;
        ExponentialBackoff exponentialBackoff = new ExponentialBackoff(rebindRetry.getStartDelay(), rebindRetry.getMaximumDelay(), 2, handler, this.mRestartImsServiceRunnable);
        this.mBackoff = exponentialBackoff;
        this.mPackageManager = null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0096, code lost:
        return false;
     */
    public boolean bind(HashSet<ImsFeatureConfiguration.FeatureSlotPair> imsFeatureSet) {
        synchronized (this.mLock) {
            if (!this.mIsBound && !this.mIsBinding) {
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
                    Log.e(LOG_TAG, "Error binding (" + this.mComponentName + ") with exception: " + e.getMessage() + ", rebinding in " + this.mBackoff.getCurrentDelay() + " ms");
                    return false;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean startBindToService(Intent intent, ImsServiceConnection connection, int flags) {
        return this.mContext.bindService(intent, connection, flags);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0049, code lost:
        return;
     */
    public void unbind() throws RemoteException {
        synchronized (this.mLock) {
            this.mBackoff.stop();
            if (this.mImsServiceConnection != null) {
                if (this.mImsDeathRecipient != null) {
                    changeImsServiceFeatures(new HashSet());
                    removeImsServiceFeatureCallbacks();
                    this.mImsServiceControllerBinder.unlinkToDeath(this.mImsDeathRecipient, 0);
                    Log.i(LOG_TAG, "Unbinding ImsService: " + this.mComponentName);
                    this.mContext.unbindService(this.mImsServiceConnection);
                    cleanUpService();
                }
            }
        }
    }

    public void changeImsServiceFeatures(HashSet<ImsFeatureConfiguration.FeatureSlotPair> newImsFeatures) throws RemoteException {
        synchronized (this.mLock) {
            Log.i(LOG_TAG, "Features changed (" + this.mImsFeatures + "->" + newImsFeatures + ") for ImsService: " + this.mComponentName);
            HashSet<ImsFeatureConfiguration.FeatureSlotPair> oldImsFeatures = new HashSet<>(this.mImsFeatures);
            this.mImsFeatures = newImsFeatures;
            if (this.mIsBound) {
                HashSet<ImsFeatureConfiguration.FeatureSlotPair> newFeatures = new HashSet<>(this.mImsFeatures);
                newFeatures.removeAll(oldImsFeatures);
                Iterator<ImsFeatureConfiguration.FeatureSlotPair> it = newFeatures.iterator();
                while (it.hasNext()) {
                    addImsServiceFeature(it.next());
                }
                HashSet<ImsFeatureConfiguration.FeatureSlotPair> oldFeatures = new HashSet<>(oldImsFeatures);
                oldFeatures.removeAll(this.mImsFeatures);
                Iterator<ImsFeatureConfiguration.FeatureSlotPair> it2 = oldFeatures.iterator();
                while (it2.hasNext()) {
                    removeImsServiceFeature(it2.next());
                }
            }
        }
    }

    @VisibleForTesting
    public IImsServiceController getImsServiceController() {
        return this.mIImsServiceController;
    }

    @VisibleForTesting
    public IBinder getImsServiceControllerBinder() {
        return this.mImsServiceControllerBinder;
    }

    @VisibleForTesting
    public long getRebindDelay() {
        return this.mBackoff.getCurrentDelay();
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003d, code lost:
        return;
     */
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
            IImsMmTelFeature resolve = f.resolve(IImsMmTelFeature.class);
            return resolve;
        }
    }

    public IImsRcsFeature getRcsFeature(int slotId) {
        synchronized (this.mLock) {
            ImsFeatureContainer f = getImsFeatureContainer(slotId, 2);
            if (f == null) {
                Log.w(LOG_TAG, "Requested null RcsFeature on slot " + slotId);
                return null;
            }
            IImsRcsFeature resolve = f.resolve(IImsRcsFeature.class);
            return resolve;
        }
    }

    public IImsRegistration getRegistration(int slotId) throws RemoteException {
        IImsRegistration registration;
        synchronized (this.mLock) {
            registration = isServiceControllerAvailable() ? this.mIImsServiceController.getRegistration(slotId) : null;
        }
        return registration;
    }

    public IImsConfig getConfig(int slotId) throws RemoteException {
        IImsConfig config;
        synchronized (this.mLock) {
            config = isServiceControllerAvailable() ? this.mIImsServiceController.getConfig(slotId) : null;
        }
        return config;
    }

    /* access modifiers changed from: protected */
    public void notifyImsServiceReady() throws RemoteException {
        synchronized (this.mLock) {
            if (isServiceControllerAvailable()) {
                Log.d(LOG_TAG, "notifyImsServiceReady");
                this.mIImsServiceController.setListener(this.mFeatureChangedListener);
                this.mIImsServiceController.notifyImsServiceReadyForFeatureCreation();
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getServiceInterface() {
        return "android.telephony.ims.ImsService";
    }

    /* access modifiers changed from: protected */
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

    /* access modifiers changed from: protected */
    public boolean isServiceControllerAvailable() {
        return this.mIImsServiceController != null;
    }

    @VisibleForTesting
    public void removeImsServiceFeatureCallbacks() {
        this.mImsStatusCallbacks.clear();
    }

    /* access modifiers changed from: private */
    public void startDelayedRebindToService() {
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

    /* access modifiers changed from: private */
    public void sendImsFeatureStatusChanged(int slot, int feature, int status) {
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

    /* access modifiers changed from: private */
    public void addImsServiceFeature(ImsFeatureConfiguration.FeatureSlotPair featurePair) throws RemoteException {
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
            ImsFeatureStatusCallback callbackToRemove = (ImsFeatureStatusCallback) this.mFeatureStatusCallbacks.stream().filter(new Predicate(featurePair) {
                private final /* synthetic */ ImsFeatureConfiguration.FeatureSlotPair f$0;

                {
                    this.f$0 = r1;
                }

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

    /* access modifiers changed from: protected */
    public IInterface createImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) throws RemoteException {
        switch (featureType) {
            case 1:
                return this.mIImsServiceController.createMmTelFeature(slotId, c);
            case 2:
                return this.mIImsServiceController.createRcsFeature(slotId, c);
            default:
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public void removeImsFeature(int slotId, int featureType, IImsFeatureStatusCallback c) throws RemoteException {
        this.mIImsServiceController.removeImsFeature(slotId, featureType, c);
    }

    private void addImsFeatureBinder(int slotId, int featureType, IInterface b) {
        this.mImsFeatureBinders.add(new ImsFeatureContainer(slotId, featureType, b));
    }

    private void removeImsFeatureBinder(int slotId, int featureType) {
        ImsFeatureContainer container = (ImsFeatureContainer) this.mImsFeatureBinders.stream().filter(new Predicate(slotId, featureType) {
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

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
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final boolean test(Object obj) {
                return ImsServiceController.lambda$getImsFeatureContainer$2(this.f$0, this.f$1, (ImsServiceController.ImsFeatureContainer) obj);
            }
        }).findFirst().orElse(null);
    }

    static /* synthetic */ boolean lambda$getImsFeatureContainer$2(int slotId, int featureType, ImsFeatureContainer f) {
        return f.slotId == slotId && f.featureType == featureType;
    }

    /* access modifiers changed from: private */
    public void cleanupAllFeatures() {
        synchronized (this.mLock) {
            Iterator<ImsFeatureConfiguration.FeatureSlotPair> it = this.mImsFeatures.iterator();
            while (it.hasNext()) {
                removeImsServiceFeature(it.next());
            }
            removeImsServiceFeatureCallbacks();
        }
    }

    /* access modifiers changed from: private */
    public void cleanUpService() {
        synchronized (this.mLock) {
            this.mImsDeathRecipient = null;
            this.mImsServiceConnection = null;
            this.mImsServiceControllerBinder = null;
            setServiceController(null);
        }
    }
}
