package com.android.ims;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.aidl.IImsCapabilityCallback;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsConfigCallback;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsMmTelListener;
import android.telephony.ims.aidl.IImsRegistration;
import android.telephony.ims.aidl.IImsRegistrationCallback;
import android.telephony.ims.aidl.IImsSmsListener;
import android.telephony.ims.feature.CapabilityChangeRequest;
import android.telephony.ims.feature.MmTelFeature;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.ims.MmTelFeatureConnection;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsServiceFeatureCallback;
import com.android.ims.internal.IImsUt;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MmTelFeatureConnection {
    protected static final String TAG = "MmTelFeatureConnection";
    private static boolean sImsSupportedOnDevice = true;
    protected IBinder mBinder;
    private final CapabilityCallbackManager mCapabilityCallbackManager;
    private IImsConfig mConfigBinder;
    private Context mContext;
    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.ims.$$Lambda$MmTelFeatureConnection$ij8S4RNRiQPHfppwkejp36BG78I */

        public final void binderDied() {
            MmTelFeatureConnection.this.lambda$new$0$MmTelFeatureConnection();
        }
    };
    private Integer mFeatureStateCached = null;
    private volatile boolean mIsAvailable = false;
    private final IImsServiceFeatureCallback mListenerBinder = new IImsServiceFeatureCallback.Stub() {
        /* class com.android.ims.MmTelFeatureConnection.AnonymousClass1 */

        public void imsFeatureCreated(int slotId, int feature) {
            synchronized (MmTelFeatureConnection.this.mLock) {
                if (MmTelFeatureConnection.this.mSlotId == slotId) {
                    if (feature == 0) {
                        MmTelFeatureConnection.this.mSupportsEmergencyCalling = true;
                        Log.i(MmTelFeatureConnection.TAG, "Emergency calling enabled on slotId: " + slotId);
                    } else if (feature == 1) {
                        if (!MmTelFeatureConnection.this.mIsAvailable) {
                            Log.i(MmTelFeatureConnection.TAG, "MmTel enabled on slotId: " + slotId);
                            MmTelFeatureConnection.this.mIsAvailable = true;
                        }
                    }
                }
            }
        }

        public void imsFeatureRemoved(int slotId, int feature) {
            synchronized (MmTelFeatureConnection.this.mLock) {
                if (MmTelFeatureConnection.this.mSlotId == slotId) {
                    if (feature == 0) {
                        MmTelFeatureConnection.this.mSupportsEmergencyCalling = false;
                        Log.i(MmTelFeatureConnection.TAG, "Emergency calling disabled on slotId: " + slotId);
                    } else if (feature == 1) {
                        Log.i(MmTelFeatureConnection.TAG, "MmTel removed on slotId: " + slotId);
                        MmTelFeatureConnection.this.onRemovedOrDied();
                    }
                }
            }
        }

        public void imsStatusChanged(int slotId, int feature, int status) {
            synchronized (MmTelFeatureConnection.this.mLock) {
                Log.i(MmTelFeatureConnection.TAG, "imsStatusChanged: slot: " + slotId + " feature: " + feature + " status: " + status);
                if (MmTelFeatureConnection.this.mSlotId == slotId && feature == 1) {
                    MmTelFeatureConnection.this.mFeatureStateCached = Integer.valueOf(status);
                    if (MmTelFeatureConnection.this.mStatusCallback != null) {
                        MmTelFeatureConnection.this.mStatusCallback.notifyStateChanged();
                    }
                }
            }
        }
    };
    private final Object mLock = new Object();
    private final ProvisioningCallbackManager mProvisioningCallbackManager;
    private IImsRegistration mRegistrationBinder;
    private final ImsRegistrationCallbackAdapter mRegistrationCallbackManager;
    protected final int mSlotId;
    private IFeatureUpdate mStatusCallback;
    private boolean mSupportsEmergencyCalling = false;

    public interface IFeatureUpdate {
        void notifyStateChanged();

        void notifyUnavailable();
    }

    @VisibleForTesting
    public static abstract class CallbackAdapterManager<T extends IInterface> {
        private static final String TAG = "CallbackAdapterManager";
        private final SparseArray<Set<T>> mCallbackSubscriptionMap = new SparseArray<>();
        private final Context mContext;
        private final Object mLock;
        private final RemoteCallbackList<T> mRemoteCallbacks = new RemoteCallbackList<>();
        @VisibleForTesting
        public SubscriptionManager.OnSubscriptionsChangedListener mSubChangedListener;

        public abstract void registerCallback(T t);

        public abstract void unregisterCallback(T t);

        public CallbackAdapterManager(Context context, Object lock) {
            this.mContext = context;
            this.mLock = lock;
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            this.mSubChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
                /* class com.android.ims.MmTelFeatureConnection.CallbackAdapterManager.AnonymousClass1 */

                public void onSubscriptionsChanged() {
                    SubscriptionManager manager = (SubscriptionManager) CallbackAdapterManager.this.mContext.getSystemService(SubscriptionManager.class);
                    if (manager == null) {
                        Log.w(CallbackAdapterManager.TAG, "onSubscriptionsChanged: could not find SubscriptionManager.");
                        return;
                    }
                    List<SubscriptionInfo> subInfos = manager.getActiveSubscriptionInfoList(false);
                    if (subInfos == null) {
                        subInfos = Collections.emptyList();
                    }
                    Collection<?> newSubIds = (Set) subInfos.stream().map($$Lambda$szO0o3matefQqo6NBdzsr9eCw.INSTANCE).collect(Collectors.toSet());
                    synchronized (CallbackAdapterManager.this.mLock) {
                        Set<Integer> storedSubIds = new ArraySet<>(CallbackAdapterManager.this.mCallbackSubscriptionMap.size());
                        for (int keyIndex = 0; keyIndex < CallbackAdapterManager.this.mCallbackSubscriptionMap.size(); keyIndex++) {
                            storedSubIds.add(Integer.valueOf(CallbackAdapterManager.this.mCallbackSubscriptionMap.keyAt(keyIndex)));
                        }
                        storedSubIds.removeAll(newSubIds);
                        for (Integer subId : storedSubIds) {
                            CallbackAdapterManager.this.removeCallbacksForSubscription(subId.intValue());
                        }
                    }
                }
            };
        }

        public final void addCallback(T localCallback) {
            synchronized (this.mLock) {
                registerCallback(localCallback);
                Log.i(TAG, "Local callback added: " + localCallback);
                this.mRemoteCallbacks.register(localCallback);
            }
        }

        public final void addCallbackForSubscription(T localCallback, int subId) {
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                synchronized (this.mLock) {
                    addCallback(localCallback);
                    linkCallbackToSubscription(localCallback, subId);
                }
            }
        }

        public final void removeCallback(T localCallback) {
            Log.i(TAG, "Local callback removed: " + localCallback);
            synchronized (this.mLock) {
                if (this.mRemoteCallbacks.unregister(localCallback)) {
                    unregisterCallback(localCallback);
                }
            }
        }

        public final void removeCallbackForSubscription(T localCallback, int subId) {
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                synchronized (this.mLock) {
                    removeCallback(localCallback);
                    unlinkCallbackFromSubscription(localCallback, subId);
                }
            }
        }

        private void linkCallbackToSubscription(T callback, int subId) {
            synchronized (this.mLock) {
                if (this.mCallbackSubscriptionMap.size() == 0) {
                    registerForSubscriptionsChanged();
                }
                Set<T> callbacksPerSub = this.mCallbackSubscriptionMap.get(subId);
                if (callbacksPerSub == null) {
                    callbacksPerSub = new ArraySet();
                    this.mCallbackSubscriptionMap.put(subId, callbacksPerSub);
                }
                callbacksPerSub.add(callback);
            }
        }

        private void unlinkCallbackFromSubscription(T callback, int subId) {
            synchronized (this.mLock) {
                Set<T> callbacksPerSub = this.mCallbackSubscriptionMap.get(subId);
                if (callbacksPerSub != null) {
                    callbacksPerSub.remove(callback);
                    if (callbacksPerSub.isEmpty()) {
                        this.mCallbackSubscriptionMap.remove(subId);
                    }
                }
                if (this.mCallbackSubscriptionMap.size() == 0) {
                    unregisterForSubscriptionsChanged();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        public void removeCallbacksForSubscription(int subId) {
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                synchronized (this.mLock) {
                    Set<T> callbacksPerSub = this.mCallbackSubscriptionMap.get(subId);
                    if (callbacksPerSub != null) {
                        this.mCallbackSubscriptionMap.remove(subId);
                        for (T callback : callbacksPerSub) {
                            removeCallback(callback);
                        }
                        if (this.mCallbackSubscriptionMap.size() == 0) {
                            unregisterForSubscriptionsChanged();
                        }
                    }
                }
            }
        }

        private void clearCallbacksForAllSubscriptions() {
            synchronized (this.mLock) {
                List<Integer> keys = new ArrayList<>();
                for (int keyIndex = 0; keyIndex < this.mCallbackSubscriptionMap.size(); keyIndex++) {
                    keys.add(Integer.valueOf(this.mCallbackSubscriptionMap.keyAt(keyIndex)));
                }
                keys.forEach(new Consumer() {
                    /* class com.android.ims.$$Lambda$MmTelFeatureConnection$CallbackAdapterManager$xhSdbzmL46sv3qoJLYbOhV0PL3w */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        MmTelFeatureConnection.CallbackAdapterManager.this.removeCallbacksForSubscription(((Integer) obj).intValue());
                    }
                });
            }
        }

        private void registerForSubscriptionsChanged() {
            SubscriptionManager manager = (SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class);
            if (manager != null) {
                manager.addOnSubscriptionsChangedListener(this.mSubChangedListener);
            } else {
                Log.w(TAG, "registerForSubscriptionsChanged: could not find SubscriptionManager.");
            }
        }

        private void unregisterForSubscriptionsChanged() {
            SubscriptionManager manager = (SubscriptionManager) this.mContext.getSystemService(SubscriptionManager.class);
            if (manager != null) {
                manager.removeOnSubscriptionsChangedListener(this.mSubChangedListener);
            } else {
                Log.w(TAG, "unregisterForSubscriptionsChanged: could not find SubscriptionManager.");
            }
        }

        public final void close() {
            synchronized (this.mLock) {
                for (int ii = this.mRemoteCallbacks.getRegisteredCallbackCount() - 1; ii >= 0; ii--) {
                    T callbackItem = this.mRemoteCallbacks.getRegisteredCallbackItem(ii);
                    unregisterCallback(callbackItem);
                    this.mRemoteCallbacks.unregister(callbackItem);
                }
                clearCallbacksForAllSubscriptions();
                Log.i(TAG, "Closing connection and clearing callbacks");
            }
        }
    }

    /* access modifiers changed from: private */
    public class ImsRegistrationCallbackAdapter extends CallbackAdapterManager<IImsRegistrationCallback> {
        public ImsRegistrationCallbackAdapter(Context context, Object lock) {
            super(context, lock);
        }

        public void registerCallback(IImsRegistrationCallback localCallback) {
            IImsRegistration imsRegistration = MmTelFeatureConnection.this.getRegistration();
            if (imsRegistration != null) {
                try {
                    imsRegistration.addRegistrationCallback(localCallback);
                } catch (RemoteException e) {
                    throw new IllegalStateException("ImsRegistrationCallbackAdapter: MmTelFeature binder is dead.");
                }
            } else {
                Log.e(MmTelFeatureConnection.TAG, "ImsRegistrationCallbackAdapter: ImsRegistration is null");
                throw new IllegalStateException("ImsRegistrationCallbackAdapter: MmTelFeature isnot available!");
            }
        }

        public void unregisterCallback(IImsRegistrationCallback localCallback) {
            IImsRegistration imsRegistration = MmTelFeatureConnection.this.getRegistration();
            if (imsRegistration != null) {
                try {
                    imsRegistration.removeRegistrationCallback(localCallback);
                } catch (RemoteException e) {
                    Log.w(MmTelFeatureConnection.TAG, "ImsRegistrationCallbackAdapter - unregisterCallback: couldn't remove registration callback");
                }
            } else {
                Log.e(MmTelFeatureConnection.TAG, "ImsRegistrationCallbackAdapter: ImsRegistration is null");
            }
        }
    }

    /* access modifiers changed from: private */
    public class CapabilityCallbackManager extends CallbackAdapterManager<IImsCapabilityCallback> {
        public CapabilityCallbackManager(Context context, Object lock) {
            super(context, lock);
        }

        public void registerCallback(IImsCapabilityCallback localCallback) {
            IImsMmTelFeature binder;
            synchronized (MmTelFeatureConnection.this.mLock) {
                try {
                    MmTelFeatureConnection.this.checkServiceIsReady();
                    binder = MmTelFeatureConnection.this.getServiceInterface(MmTelFeatureConnection.this.mBinder);
                } catch (RemoteException e) {
                    throw new IllegalStateException("CapabilityCallbackManager - MmTelFeature binder is dead.");
                }
            }
            if (binder != null) {
                try {
                    binder.addCapabilityCallback(localCallback);
                } catch (RemoteException e2) {
                    throw new IllegalStateException(" CapabilityCallbackManager - MmTelFeature binder is null.");
                }
            } else {
                Log.w(MmTelFeatureConnection.TAG, "CapabilityCallbackManager, register: Couldn't get binder");
                throw new IllegalStateException("CapabilityCallbackManager: MmTelFeature is not available!");
            }
        }

        public void unregisterCallback(IImsCapabilityCallback localCallback) {
            IImsMmTelFeature binder;
            synchronized (MmTelFeatureConnection.this.mLock) {
                try {
                    MmTelFeatureConnection.this.checkServiceIsReady();
                    binder = MmTelFeatureConnection.this.getServiceInterface(MmTelFeatureConnection.this.mBinder);
                } catch (RemoteException e) {
                    Log.w(MmTelFeatureConnection.TAG, "CapabilityCallbackManager, unregister: couldn't get binder.");
                    return;
                }
            }
            if (binder != null) {
                try {
                    binder.removeCapabilityCallback(localCallback);
                } catch (RemoteException e2) {
                    Log.w(MmTelFeatureConnection.TAG, "CapabilityCallbackManager, unregister: Binder is dead.");
                }
            } else {
                Log.w(MmTelFeatureConnection.TAG, "CapabilityCallbackManager, unregister: binder is null.");
            }
        }
    }

    /* access modifiers changed from: private */
    public class ProvisioningCallbackManager extends CallbackAdapterManager<IImsConfigCallback> {
        public ProvisioningCallbackManager(Context context, Object lock) {
            super(context, lock);
        }

        public void registerCallback(IImsConfigCallback localCallback) {
            IImsConfig binder = MmTelFeatureConnection.this.getConfigInterface();
            if (binder != null) {
                try {
                    binder.addImsConfigCallback(localCallback);
                } catch (RemoteException e) {
                    throw new IllegalStateException("ImsService is not available!");
                }
            } else {
                Log.w(MmTelFeatureConnection.TAG, "ProvisioningCallbackManager - couldn't register, binder is null.");
                throw new IllegalStateException("ImsConfig is not available!");
            }
        }

        public void unregisterCallback(IImsConfigCallback localCallback) {
            IImsConfig binder = MmTelFeatureConnection.this.getConfigInterface();
            if (binder == null) {
                Log.w(MmTelFeatureConnection.TAG, "ProvisioningCallbackManager - couldn't unregister, binder is null.");
                return;
            }
            try {
                binder.removeImsConfigCallback(localCallback);
            } catch (RemoteException e) {
                Log.w(MmTelFeatureConnection.TAG, "ProvisioningCallbackManager - couldn't unregister, binder is dead.");
            }
        }
    }

    public /* synthetic */ void lambda$new$0$MmTelFeatureConnection() {
        Log.w(TAG, "DeathRecipient triggered, binder died.");
        if (this.mContext == null || Looper.getMainLooper() == null) {
            onRemovedOrDied();
        } else {
            this.mContext.getMainExecutor().execute(new Runnable() {
                /* class com.android.ims.$$Lambda$MmTelFeatureConnection$NxZFB3RppXJngUWEmxSWd3I_s4 */

                public final void run() {
                    MmTelFeatureConnection.this.onRemovedOrDied();
                }
            });
        }
    }

    public static MmTelFeatureConnection create(Context context, int slotId) {
        MmTelFeatureConnection serviceProxy = new MmTelFeatureConnection(context, slotId);
        if (!ImsManager.isImsSupportedOnDevice(context)) {
            sImsSupportedOnDevice = false;
            return serviceProxy;
        }
        TelephonyManager tm = getTelephonyManager(context);
        if (tm == null) {
            Rlog.w(TAG, "create: TelephonyManager is null!");
            return serviceProxy;
        }
        IImsMmTelFeature binder = tm.getImsMmTelFeatureAndListen(slotId, serviceProxy.getListener());
        if (binder != null) {
            serviceProxy.setBinder(binder.asBinder());
            serviceProxy.getFeatureState();
        } else {
            Rlog.w(TAG, "create: binder is null! Slot Id: " + slotId);
        }
        return serviceProxy;
    }

    public static TelephonyManager getTelephonyManager(Context context) {
        return (TelephonyManager) context.getSystemService("phone");
    }

    public MmTelFeatureConnection(Context context, int slotId) {
        this.mSlotId = slotId;
        this.mContext = context;
        this.mRegistrationCallbackManager = new ImsRegistrationCallbackAdapter(context, this.mLock);
        this.mCapabilityCallbackManager = new CapabilityCallbackManager(context, this.mLock);
        this.mProvisioningCallbackManager = new ProvisioningCallbackManager(context, this.mLock);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void onRemovedOrDied() {
        synchronized (this.mLock) {
            this.mRegistrationCallbackManager.close();
            this.mCapabilityCallbackManager.close();
            this.mProvisioningCallbackManager.close();
            if (this.mIsAvailable) {
                this.mIsAvailable = false;
                this.mRegistrationBinder = null;
                this.mConfigBinder = null;
                if (this.mBinder != null) {
                    this.mBinder.unlinkToDeath(this.mDeathRecipient, 0);
                }
                if (this.mStatusCallback != null) {
                    this.mStatusCallback.notifyUnavailable();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        if (r1 == null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        r0 = r1.getImsRegistration(r4.mSlotId, 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r3 = r4.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0023, code lost:
        if (r4.mRegistrationBinder != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        r4.mRegistrationBinder = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0027, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002a, code lost:
        return r4.mRegistrationBinder;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
        r1 = getTelephonyManager(r4.mContext);
     */
    private IImsRegistration getRegistration() {
        synchronized (this.mLock) {
            if (this.mRegistrationBinder != null) {
                return this.mRegistrationBinder;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        if (r1 == null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        r0 = r1.getImsConfig(r4.mSlotId, 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r3 = r4.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0023, code lost:
        if (r4.mConfigBinder != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        r4.mConfigBinder = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0027, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002a, code lost:
        return r4.mConfigBinder;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x000c, code lost:
        r1 = getTelephonyManager(r4.mContext);
     */
    private IImsConfig getConfig() {
        synchronized (this.mLock) {
            if (this.mConfigBinder != null) {
                return this.mConfigBinder;
            }
        }
    }

    public boolean isEmergencyMmTelAvailable() {
        return this.mSupportsEmergencyCalling;
    }

    public IImsServiceFeatureCallback getListener() {
        return this.mListenerBinder;
    }

    public void setBinder(IBinder binder) {
        synchronized (this.mLock) {
            this.mBinder = binder;
            try {
                if (this.mBinder != null) {
                    this.mBinder.linkToDeath(this.mDeathRecipient, 0);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void openConnection(MmTelFeature.Listener listener) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).setListener(listener);
        }
    }

    public void closeConnection() {
        this.mRegistrationCallbackManager.close();
        this.mCapabilityCallbackManager.close();
        this.mProvisioningCallbackManager.close();
        try {
            synchronized (this.mLock) {
                if (isBinderAlive()) {
                    getServiceInterface(this.mBinder).setListener((IImsMmTelListener) null);
                }
            }
        } catch (RemoteException e) {
            Log.w(TAG, "closeConnection: couldn't remove listener!");
        }
    }

    public void addRegistrationCallback(IImsRegistrationCallback callback) {
        this.mRegistrationCallbackManager.addCallback(callback);
    }

    public void addRegistrationCallbackForSubscription(IImsRegistrationCallback callback, int subId) {
        this.mRegistrationCallbackManager.addCallbackForSubscription(callback, subId);
    }

    public void removeRegistrationCallback(IImsRegistrationCallback callback) {
        this.mRegistrationCallbackManager.removeCallback(callback);
    }

    public void removeRegistrationCallbackForSubscription(IImsRegistrationCallback callback, int subId) {
        this.mRegistrationCallbackManager.removeCallbackForSubscription(callback, subId);
    }

    public void addCapabilityCallback(IImsCapabilityCallback callback) {
        this.mCapabilityCallbackManager.addCallback(callback);
    }

    public void addCapabilityCallbackForSubscription(IImsCapabilityCallback callback, int subId) {
        this.mCapabilityCallbackManager.addCallbackForSubscription(callback, subId);
    }

    public void removeCapabilityCallback(IImsCapabilityCallback callback) {
        this.mCapabilityCallbackManager.removeCallback(callback);
    }

    public void removeCapabilityCallbackForSubscription(IImsCapabilityCallback callback, int subId) {
        this.mCapabilityCallbackManager.removeCallbackForSubscription(callback, subId);
    }

    public void addProvisioningCallbackForSubscription(IImsConfigCallback callback, int subId) {
        this.mProvisioningCallbackManager.addCallbackForSubscription(callback, subId);
    }

    public void removeProvisioningCallbackForSubscription(IImsConfigCallback callback, int subId) {
        this.mProvisioningCallbackManager.removeCallbackForSubscription(callback, subId);
    }

    public void changeEnabledCapabilities(CapabilityChangeRequest request, IImsCapabilityCallback callback) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).changeCapabilitiesConfiguration(request, callback);
        }
    }

    public void queryEnabledCapabilities(int capability, int radioTech, IImsCapabilityCallback callback) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).queryCapabilityConfiguration(capability, radioTech, callback);
        }
    }

    public MmTelFeature.MmTelCapabilities queryCapabilityStatus() throws RemoteException {
        MmTelFeature.MmTelCapabilities mmTelCapabilities;
        synchronized (this.mLock) {
            checkServiceIsReady();
            mmTelCapabilities = new MmTelFeature.MmTelCapabilities(getServiceInterface(this.mBinder).queryCapabilityStatus());
        }
        return mmTelCapabilities;
    }

    public ImsCallProfile createCallProfile(int callServiceType, int callType) throws RemoteException {
        ImsCallProfile createCallProfile;
        synchronized (this.mLock) {
            checkServiceIsReady();
            createCallProfile = getServiceInterface(this.mBinder).createCallProfile(callServiceType, callType);
        }
        return createCallProfile;
    }

    public IImsCallSession createCallSession(ImsCallProfile profile) throws RemoteException {
        IImsCallSession createCallSession;
        synchronized (this.mLock) {
            checkServiceIsReady();
            createCallSession = getServiceInterface(this.mBinder).createCallSession(profile);
        }
        return createCallSession;
    }

    public IImsUt getUtInterface() throws RemoteException {
        IImsUt utInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            utInterface = getServiceInterface(this.mBinder).getUtInterface();
        }
        return utInterface;
    }

    public IImsConfig getConfigInterface() {
        return getConfig();
    }

    public int getRegistrationTech() throws RemoteException {
        IImsRegistration registration = getRegistration();
        if (registration != null) {
            return registration.getRegistrationTechnology();
        }
        return -1;
    }

    public IImsEcbm getEcbmInterface() throws RemoteException {
        IImsEcbm ecbmInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            ecbmInterface = getServiceInterface(this.mBinder).getEcbmInterface();
        }
        return ecbmInterface;
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).setUiTtyMode(uiTtyMode, onComplete);
        }
    }

    public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
        IImsMultiEndpoint multiEndpointInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            multiEndpointInterface = getServiceInterface(this.mBinder).getMultiEndpointInterface();
        }
        return multiEndpointInterface;
    }

    public void sendSms(int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).sendSms(token, messageRef, format, smsc, isRetry, pdu);
        }
    }

    public void acknowledgeSms(int token, int messageRef, int result) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).acknowledgeSms(token, messageRef, result);
        }
    }

    public void acknowledgeSmsReport(int token, int messageRef, int result) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).acknowledgeSmsReport(token, messageRef, result);
        }
    }

    public String getSmsFormat() throws RemoteException {
        String smsFormat;
        synchronized (this.mLock) {
            checkServiceIsReady();
            smsFormat = getServiceInterface(this.mBinder).getSmsFormat();
        }
        return smsFormat;
    }

    public void onSmsReady() throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).onSmsReady();
        }
    }

    public void setSmsListener(IImsSmsListener listener) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).setSmsListener(listener);
        }
    }

    public int shouldProcessCall(boolean isEmergency, String[] numbers) throws RemoteException {
        int shouldProcessCall;
        if (!isEmergency || isEmergencyMmTelAvailable()) {
            synchronized (this.mLock) {
                checkServiceIsReady();
                shouldProcessCall = getServiceInterface(this.mBinder).shouldProcessCall(numbers);
            }
            return shouldProcessCall;
        }
        Log.i(TAG, "MmTel does not support emergency over IMS, fallback to CS.");
        return 1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        r1 = retrieveFeatureState();
        r2 = r3.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        if (r1 != null) goto L_0x0022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0021, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0022, code lost:
        r3.mFeatureStateCached = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0024, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0025, code lost:
        android.util.Log.i(com.android.ims.MmTelFeatureConnection.TAG, "getFeatureState - returning " + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003f, code lost:
        return r1.intValue();
     */
    public int getFeatureState() {
        synchronized (this.mLock) {
            if (isBinderAlive() && this.mFeatureStateCached != null) {
                return this.mFeatureStateCached.intValue();
            }
        }
    }

    private Integer retrieveFeatureState() {
        IBinder iBinder = this.mBinder;
        if (iBinder == null) {
            return null;
        }
        try {
            return Integer.valueOf(getServiceInterface(iBinder).getFeatureState());
        } catch (RemoteException e) {
            return null;
        }
    }

    public void setStatusCallback(IFeatureUpdate c) {
        this.mStatusCallback = c;
    }

    public boolean isBinderReady() {
        return isBinderAlive() && getFeatureState() == 2;
    }

    public boolean isBinderAlive() {
        IBinder iBinder;
        return this.mIsAvailable && (iBinder = this.mBinder) != null && iBinder.isBinderAlive();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkServiceIsReady() throws RemoteException {
        if (!sImsSupportedOnDevice) {
            throw new RemoteException("IMS is not supported on this device.");
        } else if (!isBinderReady()) {
            throw new RemoteException("ImsServiceProxy is not ready to accept commands.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IImsMmTelFeature getServiceInterface(IBinder b) {
        return IImsMmTelFeature.Stub.asInterface(b);
    }
}
