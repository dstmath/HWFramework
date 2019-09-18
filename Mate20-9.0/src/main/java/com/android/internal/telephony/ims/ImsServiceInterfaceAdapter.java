package com.android.internal.telephony.ims;

import android.app.PendingIntent;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsService;
import com.android.ims.internal.IImsUt;

public class ImsServiceInterfaceAdapter extends MmTelInterfaceAdapter {
    private static final int SERVICE_ID = 1;

    public ImsServiceInterfaceAdapter(int slotId, IBinder binder) {
        super(slotId, binder);
    }

    public int startSession(PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
        return getInterface().open(this.mSlotId, 1, incomingCallIntent, listener);
    }

    public void endSession(int sessionId) throws RemoteException {
        getInterface().close(sessionId);
    }

    public boolean isConnected(int callSessionType, int callType) throws RemoteException {
        return getInterface().isConnected(1, callSessionType, callType);
    }

    public boolean isOpened() throws RemoteException {
        return getInterface().isOpened(1);
    }

    public int getFeatureState() throws RemoteException {
        return 2;
    }

    public void addRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        getInterface().addRegistrationListener(this.mSlotId, 1, listener);
    }

    public void removeRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
    }

    public ImsCallProfile createCallProfile(int sessionId, int callSessionType, int callType) throws RemoteException {
        return getInterface().createCallProfile(sessionId, callSessionType, callType);
    }

    public IImsCallSession createCallSession(int sessionId, ImsCallProfile profile) throws RemoteException {
        return getInterface().createCallSession(sessionId, profile, null);
    }

    public IImsCallSession getPendingCallSession(int sessionId, String callId) throws RemoteException {
        return getInterface().getPendingCallSession(sessionId, callId);
    }

    public IImsUt getUtInterface() throws RemoteException {
        return getInterface().getUtInterface(1);
    }

    public IImsConfig getConfigInterface() throws RemoteException {
        return getInterface().getConfigInterface(this.mSlotId);
    }

    public void turnOnIms() throws RemoteException {
        getInterface().turnOnIms(this.mSlotId);
    }

    public void turnOffIms() throws RemoteException {
        getInterface().turnOffIms(this.mSlotId);
    }

    public IImsEcbm getEcbmInterface() throws RemoteException {
        return getInterface().getEcbmInterface(1);
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) throws RemoteException {
        getInterface().setUiTTYMode(1, uiTtyMode, onComplete);
    }

    public IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException {
        return getInterface().getMultiEndpointInterface(1);
    }

    private IImsService getInterface() throws RemoteException {
        IImsService feature = IImsService.Stub.asInterface(this.mBinder);
        if (feature != null) {
            return feature;
        }
        throw new RemoteException("Binder not Available");
    }
}
