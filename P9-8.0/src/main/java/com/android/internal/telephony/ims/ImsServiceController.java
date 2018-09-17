package com.android.internal.telephony.ims;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.IPackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Pair;
import com.android.ims.internal.IImsFeatureStatusCallback;
import com.android.ims.internal.IImsFeatureStatusCallback.Stub;
import com.android.ims.internal.IImsServiceController;
import com.android.ims.internal.IImsServiceFeatureListener;
import com.android.internal.telephony.ims.-$Lambda$Dp0MKpTfGctn5WSf-VZIVicYMbM.AnonymousClass2;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImsServiceController {
    private static final String LOG_TAG = "ImsServiceController";
    private static final int REBIND_RETRY_TIME = 5000;
    private ImsServiceControllerCallbacks mCallbacks;
    private final ComponentName mComponentName;
    private final Context mContext;
    private Set<ImsFeatureStatusCallback> mFeatureStatusCallbacks;
    private Handler mHandler;
    private final HandlerThread mHandlerThread;
    private IImsServiceController mIImsServiceController;
    private ImsDeathRecipient mImsDeathRecipient;
    private HashSet<Pair<Integer, Integer>> mImsFeatures;
    private ImsServiceConnection mImsServiceConnection;
    private IBinder mImsServiceControllerBinder;
    private Set<IImsServiceFeatureListener> mImsStatusCallbacks;
    private boolean mIsBinding;
    private boolean mIsBound;
    private final Object mLock;
    private final IPackageManager mPackageManager;
    private RebindRetry mRebindRetry;
    private Runnable mRestartImsServiceRunnable;

    public interface RebindRetry {
        long getRetryTimeout();
    }

    public interface ImsServiceControllerCallbacks {
        void imsServiceFeatureCreated(int i, int i2, ImsServiceController imsServiceController);

        void imsServiceFeatureRemoved(int i, int i2, ImsServiceController imsServiceController);
    }

    class ImsDeathRecipient implements DeathRecipient {
        private ComponentName mComponentName;

        ImsDeathRecipient(ComponentName name) {
            this.mComponentName = name;
        }

        public void binderDied() {
            Log.e(ImsServiceController.LOG_TAG, "ImsService(" + this.mComponentName + ") died. Restarting...");
            ImsServiceController.this.notifyAllFeaturesRemoved();
            ImsServiceController.this.cleanUpService();
            ImsServiceController.this.startDelayedRebindToService();
        }
    }

    private class ImsFeatureStatusCallback {
        private final IImsFeatureStatusCallback mCallback = new Stub() {
            public void notifyImsFeatureStatus(int featureStatus) throws RemoteException {
                Log.i(ImsServiceController.LOG_TAG, "notifyImsFeatureStatus: slot=" + ImsFeatureStatusCallback.this.mSlotId + ", feature=" + ImsFeatureStatusCallback.this.mFeatureType + ", status=" + featureStatus);
                ImsServiceController.this.sendImsFeatureStatusChanged(ImsFeatureStatusCallback.this.mSlotId, ImsFeatureStatusCallback.this.mFeatureType, featureStatus);
            }
        };
        private int mFeatureType;
        private int mSlotId;

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
            synchronized (ImsServiceController.this.mLock) {
                ImsServiceController.this.mIsBound = true;
                ImsServiceController.this.mIsBinding = false;
                ImsServiceController.this.grantPermissionsToService();
                Log.d(ImsServiceController.LOG_TAG, "ImsService(" + name + "): onServiceConnected with binder: " + service);
                if (service != null) {
                    ImsServiceController.this.mImsDeathRecipient = new ImsDeathRecipient(name);
                    try {
                        service.linkToDeath(ImsServiceController.this.mImsDeathRecipient, 0);
                        ImsServiceController.this.mImsServiceControllerBinder = service;
                        ImsServiceController.this.mIImsServiceController = IImsServiceController.Stub.asInterface(service);
                        for (Pair<Integer, Integer> i : ImsServiceController.this.mImsFeatures) {
                            ImsServiceController.this.addImsServiceFeature(i);
                        }
                    } catch (RemoteException e) {
                        ImsServiceController.this.mIsBound = false;
                        ImsServiceController.this.mIsBinding = false;
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
                ImsServiceController.this.mIsBinding = false;
            }
            if (ImsServiceController.this.mIImsServiceController != null) {
                ImsServiceController.this.mImsServiceControllerBinder.unlinkToDeath(ImsServiceController.this.mImsDeathRecipient, 0);
            }
            ImsServiceController.this.notifyAllFeaturesRemoved();
            ImsServiceController.this.cleanUpService();
            Log.w(ImsServiceController.LOG_TAG, "ImsService(" + name + "): onServiceDisconnected. Rebinding...");
            ImsServiceController.this.startDelayedRebindToService();
        }
    }

    public void setRebindRetryTime(RebindRetry retry) {
        this.mRebindRetry = retry;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public ImsServiceController(Context context, ComponentName componentName, ImsServiceControllerCallbacks callbacks) {
        this.mLock = new Object();
        this.mHandlerThread = new HandlerThread("ImsServiceControllerHandler");
        this.mIsBound = false;
        this.mIsBinding = false;
        this.mImsStatusCallbacks = new HashSet();
        this.mFeatureStatusCallbacks = new HashSet();
        this.mRestartImsServiceRunnable = new Runnable() {
            public void run() {
                synchronized (ImsServiceController.this.mLock) {
                    if (ImsServiceController.this.mIsBound) {
                        return;
                    }
                    ImsServiceController.this.bind(ImsServiceController.this.mImsFeatures);
                }
            }
        };
        this.mRebindRetry = new -$Lambda$Dp0MKpTfGctn5WSf-VZIVicYMbM();
        this.mContext = context;
        this.mComponentName = componentName;
        this.mCallbacks = callbacks;
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    }

    public ImsServiceController(Context context, ComponentName componentName, ImsServiceControllerCallbacks callbacks, Handler testHandler) {
        this.mLock = new Object();
        this.mHandlerThread = new HandlerThread("ImsServiceControllerHandler");
        this.mIsBound = false;
        this.mIsBinding = false;
        this.mImsStatusCallbacks = new HashSet();
        this.mFeatureStatusCallbacks = new HashSet();
        this.mRestartImsServiceRunnable = /* anonymous class already generated */;
        this.mRebindRetry = new RebindRetry() {
            public final long getRetryTimeout() {
                return $m$0();
            }
        };
        this.mContext = context;
        this.mComponentName = componentName;
        this.mCallbacks = callbacks;
        this.mHandler = testHandler;
        this.mPackageManager = null;
    }

    /* JADX WARNING: Missing block: B:18:0x0087, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean bind(HashSet<Pair<Integer, Integer>> imsFeatureSet) {
        synchronized (this.mLock) {
            this.mHandler.removeCallbacks(this.mRestartImsServiceRunnable);
            if (this.mIsBound || (this.mIsBinding ^ 1) == 0) {
            } else {
                this.mIsBinding = true;
                this.mImsFeatures = imsFeatureSet;
                Intent imsServiceIntent = new Intent(ImsResolver.SERVICE_INTERFACE).setComponent(this.mComponentName);
                this.mImsServiceConnection = new ImsServiceConnection();
                Log.i(LOG_TAG, "Binding ImsService:" + this.mComponentName);
                try {
                    boolean bindService = this.mContext.bindService(imsServiceIntent, this.mImsServiceConnection, 67108929);
                    return bindService;
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error binding (" + this.mComponentName + ") with exception: " + e.getMessage());
                    return false;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0013, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unbind() throws RemoteException {
        synchronized (this.mLock) {
            this.mHandler.removeCallbacks(this.mRestartImsServiceRunnable);
            if (this.mImsServiceConnection == null || this.mImsDeathRecipient == null) {
            } else {
                changeImsServiceFeatures(new HashSet());
                removeImsServiceFeatureListener();
                this.mImsServiceControllerBinder.unlinkToDeath(this.mImsDeathRecipient, 0);
                Log.i(LOG_TAG, "Unbinding ImsService: " + this.mComponentName);
                this.mContext.unbindService(this.mImsServiceConnection);
                cleanUpService();
            }
        }
    }

    public void changeImsServiceFeatures(HashSet<Pair<Integer, Integer>> newImsFeatures) throws RemoteException {
        synchronized (this.mLock) {
            if (this.mIsBound) {
                HashSet<Pair<Integer, Integer>> newFeatures = new HashSet(newImsFeatures);
                newFeatures.removeAll(this.mImsFeatures);
                for (Pair<Integer, Integer> i : newFeatures) {
                    addImsServiceFeature(i);
                }
                HashSet<Pair<Integer, Integer>> oldFeatures = new HashSet(this.mImsFeatures);
                oldFeatures.removeAll(newImsFeatures);
                for (Pair<Integer, Integer> i2 : oldFeatures) {
                    removeImsServiceFeature(i2);
                }
            }
            Log.i(LOG_TAG, "Features changed (" + this.mImsFeatures + "->" + newImsFeatures + ") for " + "ImsService: " + this.mComponentName);
            this.mImsFeatures = newImsFeatures;
        }
    }

    public IImsServiceController getImsServiceController() {
        return this.mIImsServiceController;
    }

    public IBinder getImsServiceControllerBinder() {
        return this.mImsServiceControllerBinder;
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public void addImsServiceFeatureListener(IImsServiceFeatureListener callback) {
        if (callback != null) {
            synchronized (this.mLock) {
                this.mImsStatusCallbacks.add(callback);
            }
        }
    }

    private void removeImsServiceFeatureListener() {
        synchronized (this.mLock) {
            this.mImsStatusCallbacks.clear();
        }
    }

    private void startDelayedRebindToService() {
        if (!this.mHandler.hasCallbacks(this.mRestartImsServiceRunnable)) {
            this.mHandler.postDelayed(this.mRestartImsServiceRunnable, this.mRebindRetry.getRetryTimeout());
        }
    }

    private void grantPermissionsToService() {
        Log.i(LOG_TAG, "Granting Runtime permissions to:" + getComponentName());
        String[] pkgToGrant = new String[]{this.mComponentName.getPackageName()};
        try {
            if (this.mPackageManager != null) {
                this.mPackageManager.grantDefaultPermissionsToEnabledImsServices(pkgToGrant, this.mContext.getUserId());
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "Unable to grant permissions, binder died.");
        }
    }

    private void sendImsFeatureCreatedCallback(int slot, int feature) {
        synchronized (this.mLock) {
            Iterator<IImsServiceFeatureListener> i = this.mImsStatusCallbacks.iterator();
            while (i.hasNext()) {
                try {
                    ((IImsServiceFeatureListener) i.next()).imsFeatureCreated(slot, feature);
                } catch (RemoteException e) {
                    Log.w(LOG_TAG, "sendImsFeatureCreatedCallback: Binder died, removing callback. Exception:" + e.getMessage());
                    i.remove();
                }
            }
        }
    }

    private void sendImsFeatureRemovedCallback(int slot, int feature) {
        synchronized (this.mLock) {
            Iterator<IImsServiceFeatureListener> i = this.mImsStatusCallbacks.iterator();
            while (i.hasNext()) {
                try {
                    ((IImsServiceFeatureListener) i.next()).imsFeatureRemoved(slot, feature);
                } catch (RemoteException e) {
                    Log.w(LOG_TAG, "sendImsFeatureRemovedCallback: Binder died, removing callback. Exception:" + e.getMessage());
                    i.remove();
                }
            }
        }
    }

    private void sendImsFeatureStatusChanged(int slot, int feature, int status) {
        synchronized (this.mLock) {
            Iterator<IImsServiceFeatureListener> i = this.mImsStatusCallbacks.iterator();
            while (i.hasNext()) {
                try {
                    ((IImsServiceFeatureListener) i.next()).imsStatusChanged(slot, feature, status);
                } catch (RemoteException e) {
                    Log.w(LOG_TAG, "sendImsFeatureStatusChanged: Binder died, removing callback. Exception:" + e.getMessage());
                    i.remove();
                }
            }
        }
    }

    private void addImsServiceFeature(Pair<Integer, Integer> featurePair) throws RemoteException {
        if (this.mIImsServiceController == null || this.mCallbacks == null) {
            Log.w(LOG_TAG, "addImsServiceFeature called with null values.");
            return;
        }
        ImsFeatureStatusCallback c = new ImsFeatureStatusCallback(((Integer) featurePair.first).intValue(), ((Integer) featurePair.second).intValue());
        this.mFeatureStatusCallbacks.add(c);
        this.mIImsServiceController.createImsFeature(((Integer) featurePair.first).intValue(), ((Integer) featurePair.second).intValue(), c.getCallback());
        this.mCallbacks.imsServiceFeatureCreated(((Integer) featurePair.first).intValue(), ((Integer) featurePair.second).intValue(), this);
        sendImsFeatureCreatedCallback(((Integer) featurePair.first).intValue(), ((Integer) featurePair.second).intValue());
    }

    private void removeImsServiceFeature(Pair<Integer, Integer> featurePair) throws RemoteException {
        if (this.mIImsServiceController == null || this.mCallbacks == null) {
            Log.w(LOG_TAG, "removeImsServiceFeature called with null values.");
            return;
        }
        ImsFeatureStatusCallback callbackToRemove = (ImsFeatureStatusCallback) this.mFeatureStatusCallbacks.stream().filter(new AnonymousClass2(featurePair)).findFirst().orElse(null);
        if (callbackToRemove != null) {
            this.mFeatureStatusCallbacks.remove(callbackToRemove);
        }
        this.mIImsServiceController.removeImsFeature(((Integer) featurePair.first).intValue(), ((Integer) featurePair.second).intValue(), callbackToRemove != null ? callbackToRemove.getCallback() : null);
        this.mCallbacks.imsServiceFeatureRemoved(((Integer) featurePair.first).intValue(), ((Integer) featurePair.second).intValue(), this);
        sendImsFeatureRemovedCallback(((Integer) featurePair.first).intValue(), ((Integer) featurePair.second).intValue());
    }

    static /* synthetic */ boolean lambda$-com_android_internal_telephony_ims_ImsServiceController_19011(Pair featurePair, ImsFeatureStatusCallback c) {
        return c.mSlotId == ((Integer) featurePair.first).intValue() && c.mFeatureType == ((Integer) featurePair.second).intValue();
    }

    private void notifyAllFeaturesRemoved() {
        if (this.mCallbacks == null) {
            Log.w(LOG_TAG, "notifyAllFeaturesRemoved called with invalid callbacks.");
            return;
        }
        synchronized (this.mLock) {
            for (Pair<Integer, Integer> feature : this.mImsFeatures) {
                this.mCallbacks.imsServiceFeatureRemoved(((Integer) feature.first).intValue(), ((Integer) feature.second).intValue(), this);
                sendImsFeatureRemovedCallback(((Integer) feature.first).intValue(), ((Integer) feature.second).intValue());
            }
        }
    }

    private void cleanUpService() {
        synchronized (this.mLock) {
            this.mImsDeathRecipient = null;
            this.mImsServiceConnection = null;
            this.mImsServiceControllerBinder = null;
            this.mIImsServiceController = null;
            this.mIsBound = false;
        }
    }
}
