package android.telephony.ims.feature;

import android.app.PendingIntent;
import android.os.Message;
import android.os.RemoteException;
import com.android.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;

public interface IMMTelFeature {
    void addRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    ImsCallProfile createCallProfile(int i, int i2, int i3) throws RemoteException;

    IImsCallSession createCallSession(int i, ImsCallProfile imsCallProfile, IImsCallSessionListener iImsCallSessionListener) throws RemoteException;

    void endSession(int i) throws RemoteException;

    IImsConfig getConfigInterface() throws RemoteException;

    IImsEcbm getEcbmInterface() throws RemoteException;

    IImsMultiEndpoint getMultiEndpointInterface() throws RemoteException;

    IImsCallSession getPendingCallSession(int i, String str) throws RemoteException;

    IImsUt getUtInterface() throws RemoteException;

    boolean isConnected(int i, int i2) throws RemoteException;

    boolean isOpened() throws RemoteException;

    void removeRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    void setUiTTYMode(int i, Message message) throws RemoteException;

    int startSession(PendingIntent pendingIntent, IImsRegistrationListener iImsRegistrationListener) throws RemoteException;

    void turnOffIms() throws RemoteException;

    void turnOnIms() throws RemoteException;
}
