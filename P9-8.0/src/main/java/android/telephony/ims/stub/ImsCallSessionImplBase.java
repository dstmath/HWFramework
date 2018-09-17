package android.telephony.ims.stub;

import android.os.Message;
import android.os.RemoteException;
import com.android.ims.ImsCallProfile;
import com.android.ims.ImsStreamMediaProfile;
import com.android.ims.internal.IImsCallSession.Stub;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsVideoCallProvider;

public class ImsCallSessionImplBase extends Stub {
    public void close() throws RemoteException {
    }

    public String getCallId() throws RemoteException {
        return null;
    }

    public ImsCallProfile getCallProfile() throws RemoteException {
        return null;
    }

    public ImsCallProfile getLocalCallProfile() throws RemoteException {
        return null;
    }

    public ImsCallProfile getRemoteCallProfile() throws RemoteException {
        return null;
    }

    public String getProperty(String name) throws RemoteException {
        return null;
    }

    public int getState() throws RemoteException {
        return -1;
    }

    public boolean isInCall() throws RemoteException {
        return false;
    }

    public void setListener(IImsCallSessionListener listener) throws RemoteException {
    }

    public void setMute(boolean muted) throws RemoteException {
    }

    public void start(String callee, ImsCallProfile profile) throws RemoteException {
    }

    public void startConference(String[] participants, ImsCallProfile profile) throws RemoteException {
    }

    public void accept(int callType, ImsStreamMediaProfile profile) throws RemoteException {
    }

    public void reject(int reason) throws RemoteException {
    }

    public void terminate(int reason) throws RemoteException {
    }

    public void hangupForegroundResumeBackground(int reason) throws RemoteException {
    }

    public void hangupWaitingOrBackground(int reason) throws RemoteException {
    }

    public void hold(ImsStreamMediaProfile profile) throws RemoteException {
    }

    public void resume(ImsStreamMediaProfile profile) throws RemoteException {
    }

    public void merge() throws RemoteException {
    }

    public void update(int callType, ImsStreamMediaProfile profile) throws RemoteException {
    }

    public void extendToConference(String[] participants) throws RemoteException {
    }

    public void inviteParticipants(String[] participants) throws RemoteException {
    }

    public void removeParticipants(String[] participants) throws RemoteException {
    }

    public void sendDtmf(char c, Message result) throws RemoteException {
    }

    public void startDtmf(char c) throws RemoteException {
    }

    public void stopDtmf() throws RemoteException {
    }

    public void sendUssd(String ussdMessage) throws RemoteException {
    }

    public IImsVideoCallProvider getVideoCallProvider() throws RemoteException {
        return null;
    }

    public boolean isMultiparty() throws RemoteException {
        return false;
    }
}
