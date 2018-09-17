package android.telephony.ims;

import android.app.PendingIntent;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.feature.IRcsFeature;
import android.util.Log;
import com.android.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsServiceController;
import com.android.ims.internal.IImsServiceFeatureListener;
import com.android.ims.internal.IImsServiceFeatureListener.Stub;
import com.android.ims.internal.IImsUt;

public class ImsServiceProxy extends ImsServiceProxyCompat implements IRcsFeature {
    protected String LOG_TAG = "ImsServiceProxy";
    private Integer mFeatureStatusCached = null;
    private boolean mIsAvailable = true;
    private final IImsServiceFeatureListener mListenerBinder = new Stub() {
        public void imsFeatureCreated(int slotId, int feature) throws RemoteException {
            synchronized (ImsServiceProxy.this.mLock) {
                if (!ImsServiceProxy.this.mIsAvailable && ImsServiceProxy.this.mSlotId == slotId && feature == ImsServiceProxy.this.mSupportedFeature) {
                    Log.i(ImsServiceProxy.this.LOG_TAG, "Feature enabled on slotId: " + slotId + " for feature: " + feature);
                    ImsServiceProxy.this.mIsAvailable = true;
                }
            }
        }

        public void imsFeatureRemoved(int slotId, int feature) throws RemoteException {
            synchronized (ImsServiceProxy.this.mLock) {
                if (ImsServiceProxy.this.mIsAvailable && ImsServiceProxy.this.mSlotId == slotId && feature == ImsServiceProxy.this.mSupportedFeature) {
                    Log.i(ImsServiceProxy.this.LOG_TAG, "Feature disabled on slotId: " + slotId + " for feature: " + feature);
                    ImsServiceProxy.this.mIsAvailable = false;
                }
            }
        }

        public void imsStatusChanged(int slotId, int feature, int status) throws RemoteException {
            synchronized (ImsServiceProxy.this.mLock) {
                Log.i(ImsServiceProxy.this.LOG_TAG, "imsStatusChanged: slot: " + slotId + " feature: " + feature + " status: " + status);
                if (ImsServiceProxy.this.mSlotId == slotId && feature == ImsServiceProxy.this.mSupportedFeature) {
                    ImsServiceProxy.this.mFeatureStatusCached = Integer.valueOf(status);
                }
            }
            if (ImsServiceProxy.this.mStatusCallback != null) {
                ImsServiceProxy.this.mStatusCallback.notifyStatusChanged();
            }
        }
    };
    private final Object mLock = new Object();
    private INotifyStatusChanged mStatusCallback;
    private final int mSupportedFeature;

    public interface INotifyStatusChanged {
        void notifyStatusChanged();
    }

    public ImsServiceProxy(int slotId, IBinder binder, int featureType) {
        super(slotId, binder);
        this.mSupportedFeature = featureType;
    }

    public ImsServiceProxy(int slotId, int featureType) {
        super(slotId, null);
        this.mSupportedFeature = featureType;
    }

    public IImsServiceFeatureListener getListener() {
        return this.mListenerBinder;
    }

    public void setBinder(IBinder binder) {
        this.mBinder = binder;
    }

    public int startSession(PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
        int startSession;
        synchronized (this.mLock) {
            checkServiceIsReady();
            startSession = getServiceInterface(this.mBinder).startSession(this.mSlotId, this.mSupportedFeature, incomingCallIntent, listener);
        }
        return startSession;
    }

    public void endSession(int sessionId) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).endSession(this.mSlotId, this.mSupportedFeature, sessionId);
        }
    }

    public boolean isConnected(int callServiceType, int callType) throws RemoteException {
        boolean isConnected;
        synchronized (this.mLock) {
            checkServiceIsReady();
            isConnected = getServiceInterface(this.mBinder).isConnected(this.mSlotId, this.mSupportedFeature, callServiceType, callType);
        }
        return isConnected;
    }

    public boolean isOpened() throws RemoteException {
        boolean isOpened;
        synchronized (this.mLock) {
            checkServiceIsReady();
            isOpened = getServiceInterface(this.mBinder).isOpened(this.mSlotId, this.mSupportedFeature);
        }
        return isOpened;
    }

    public void addRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).addRegistrationListener(this.mSlotId, this.mSupportedFeature, listener);
        }
    }

    public void removeRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).removeRegistrationListener(this.mSlotId, this.mSupportedFeature, listener);
        }
    }

    public ImsCallProfile createCallProfile(int sessionId, int callServiceType, int callType) throws RemoteException {
        ImsCallProfile createCallProfile;
        synchronized (this.mLock) {
            checkServiceIsReady();
            createCallProfile = getServiceInterface(this.mBinder).createCallProfile(this.mSlotId, this.mSupportedFeature, sessionId, callServiceType, callType);
        }
        return createCallProfile;
    }

    public IImsCallSession createCallSession(int sessionId, ImsCallProfile profile, IImsCallSessionListener listener) throws RemoteException {
        IImsCallSession createCallSession;
        synchronized (this.mLock) {
            checkServiceIsReady();
            createCallSession = getServiceInterface(this.mBinder).createCallSession(this.mSlotId, this.mSupportedFeature, sessionId, profile, listener);
        }
        return createCallSession;
    }

    public IImsCallSession getPendingCallSession(int sessionId, String callId) throws RemoteException {
        IImsCallSession pendingCallSession;
        synchronized (this.mLock) {
            checkServiceIsReady();
            pendingCallSession = getServiceInterface(this.mBinder).getPendingCallSession(this.mSlotId, this.mSupportedFeature, sessionId, callId);
        }
        return pendingCallSession;
    }

    public IImsUt getUtInterface() throws RemoteException {
        IImsUt utInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            utInterface = getServiceInterface(this.mBinder).getUtInterface(this.mSlotId, this.mSupportedFeature);
        }
        return utInterface;
    }

    public IImsConfig getConfigInterface() throws RemoteException {
        IImsConfig configInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            configInterface = getServiceInterface(this.mBinder).getConfigInterface(this.mSlotId, this.mSupportedFeature);
        }
        return configInterface;
    }

    public void turnOnIms() throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).turnOnIms(this.mSlotId, this.mSupportedFeature);
        }
    }

    public void turnOffIms() throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).turnOffIms(this.mSlotId, this.mSupportedFeature);
        }
    }

    public IImsEcbm getEcbmInterface() throws RemoteException {
        IImsEcbm ecbmInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            ecbmInterface = getServiceInterface(this.mBinder).getEcbmInterface(this.mSlotId, this.mSupportedFeature);
        }
        return ecbmInterface;
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) throws RemoteException {
        synchronized (this.mLock) {
            checkServiceIsReady();
            getServiceInterface(this.mBinder).setUiTTYMode(this.mSlotId, this.mSupportedFeature, uiTtyMode, onComplete);
        }
    }

    public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
        IImsMultiEndpoint multiEndpointInterface;
        synchronized (this.mLock) {
            checkServiceIsReady();
            multiEndpointInterface = getServiceInterface(this.mBinder).getMultiEndpointInterface(this.mSlotId, this.mSupportedFeature);
        }
        return multiEndpointInterface;
    }

    /* JADX WARNING: Missing block: B:11:0x0031, code:
            r0 = retrieveFeatureStatus();
            r1 = r5.mLock;
     */
    /* JADX WARNING: Missing block: B:12:0x0037, code:
            monitor-enter(r1);
     */
    /* JADX WARNING: Missing block: B:13:0x0038, code:
            if (r0 != null) goto L_0x0040;
     */
    /* JADX WARNING: Missing block: B:15:0x003b, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:16:0x003c, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            r5.mFeatureStatusCached = r0;
     */
    /* JADX WARNING: Missing block: B:22:0x0042, code:
            monitor-exit(r1);
     */
    /* JADX WARNING: Missing block: B:23:0x0043, code:
            android.util.Log.i(r5.LOG_TAG, "getFeatureStatus - returning " + r0);
     */
    /* JADX WARNING: Missing block: B:24:0x0060, code:
            return r0.intValue();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getFeatureStatus() {
        synchronized (this.mLock) {
            if (!isBinderAlive() || this.mFeatureStatusCached == null) {
            } else {
                Log.i(this.LOG_TAG, "getFeatureStatus - returning cached: " + this.mFeatureStatusCached);
                int intValue = this.mFeatureStatusCached.intValue();
                return intValue;
            }
        }
    }

    private Integer retrieveFeatureStatus() {
        if (this.mBinder != null) {
            try {
                return Integer.valueOf(getServiceInterface(this.mBinder).getFeatureStatus(this.mSlotId, this.mSupportedFeature));
            } catch (RemoteException e) {
            }
        }
        return null;
    }

    public void setStatusCallback(INotifyStatusChanged c) {
        this.mStatusCallback = c;
    }

    public boolean isBinderReady() {
        return isBinderAlive() && getFeatureStatus() == 2;
    }

    public boolean isBinderAlive() {
        return (!this.mIsAvailable || this.mBinder == null) ? false : this.mBinder.isBinderAlive();
    }

    protected void checkServiceIsReady() throws RemoteException {
        if (!isBinderReady()) {
            throw new RemoteException("ImsServiceProxy is not ready to accept commands.");
        }
    }

    private IImsServiceController getServiceInterface(IBinder b) {
        return IImsServiceController.Stub.asInterface(b);
    }
}
