package android.telephony.ims;

import android.app.PendingIntent;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.feature.IMMTelFeature;
import com.android.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsService;
import com.android.ims.internal.IImsService.Stub;
import com.android.ims.internal.IImsUt;

public class ImsServiceProxyCompat implements IMMTelFeature {
    private static final int SERVICE_ID = 1;
    protected IBinder mBinder;
    protected final int mSlotId;

    public ImsServiceProxyCompat(int slotId, IBinder binder) {
        this.mSlotId = slotId;
        this.mBinder = binder;
    }

    public int startSession(PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).open(this.mSlotId, 1, incomingCallIntent, listener);
    }

    public void endSession(int sessionId) throws RemoteException {
        checkBinderConnection();
        getServiceInterface(this.mBinder).close(sessionId);
    }

    public boolean isConnected(int callServiceType, int callType) throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).isConnected(1, callServiceType, callType);
    }

    public boolean isOpened() throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).isOpened(1);
    }

    public void addRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        checkBinderConnection();
        getServiceInterface(this.mBinder).addRegistrationListener(this.mSlotId, 1, listener);
    }

    public void removeRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
    }

    public ImsCallProfile createCallProfile(int sessionId, int callServiceType, int callType) throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).createCallProfile(sessionId, callServiceType, callType);
    }

    public IImsCallSession createCallSession(int sessionId, ImsCallProfile profile, IImsCallSessionListener listener) throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).createCallSession(sessionId, profile, listener);
    }

    public IImsCallSession getPendingCallSession(int sessionId, String callId) throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).getPendingCallSession(sessionId, callId);
    }

    public IImsUt getUtInterface() throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).getUtInterface(1);
    }

    public IImsConfig getConfigInterface() throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).getConfigInterface(this.mSlotId);
    }

    public void turnOnIms() throws RemoteException {
        checkBinderConnection();
        getServiceInterface(this.mBinder).turnOnIms(this.mSlotId);
    }

    public void turnOffIms() throws RemoteException {
        checkBinderConnection();
        getServiceInterface(this.mBinder).turnOffIms(this.mSlotId);
    }

    public IImsEcbm getEcbmInterface() throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).getEcbmInterface(1);
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) throws RemoteException {
        checkBinderConnection();
        getServiceInterface(this.mBinder).setUiTTYMode(1, uiTtyMode, onComplete);
    }

    public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
        checkBinderConnection();
        return getServiceInterface(this.mBinder).getMultiEndpointInterface(1);
    }

    public int getFeatureStatus() {
        return 2;
    }

    public boolean isBinderAlive() {
        return this.mBinder != null ? this.mBinder.isBinderAlive() : false;
    }

    private IImsService getServiceInterface(IBinder b) {
        return Stub.asInterface(b);
    }

    protected void checkBinderConnection() throws RemoteException {
        if (!isBinderAlive()) {
            throw new RemoteException("ImsServiceProxy is not available for that feature.");
        }
    }
}
