package android.telephony.ims.stub;

import android.annotation.SystemApi;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSessionListener;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsVideoCallProvider;
import android.telephony.ims.aidl.IImsCallSessionListener;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsVideoCallProvider;
import com.android.internal.telephony.IccCardConstants;

@SystemApi
public class ImsCallSessionImplBase implements AutoCloseable {
    public static final int USSD_MODE_NOTIFY = 0;
    public static final int USSD_MODE_REQUEST = 1;
    private IImsCallSession mServiceImpl = new IImsCallSession.Stub() {
        /* class android.telephony.ims.stub.ImsCallSessionImplBase.AnonymousClass1 */

        @Override // com.android.ims.internal.IImsCallSession
        public void close() {
            ImsCallSessionImplBase.this.close();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public String getCallId() {
            return ImsCallSessionImplBase.this.getCallId();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public ImsCallProfile getCallProfile() {
            return ImsCallSessionImplBase.this.getCallProfile();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public ImsCallProfile getLocalCallProfile() {
            return ImsCallSessionImplBase.this.getLocalCallProfile();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public ImsCallProfile getRemoteCallProfile() {
            return ImsCallSessionImplBase.this.getRemoteCallProfile();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public String getProperty(String name) {
            return ImsCallSessionImplBase.this.getProperty(name);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public int getState() {
            return ImsCallSessionImplBase.this.getState();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public boolean isInCall() {
            return ImsCallSessionImplBase.this.isInCall();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void setListener(IImsCallSessionListener listener) {
            ImsCallSessionImplBase.this.setListener(new ImsCallSessionListener(listener));
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void setMute(boolean muted) {
            ImsCallSessionImplBase.this.setMute(muted);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void start(String callee, ImsCallProfile profile) {
            ImsCallSessionImplBase.this.start(callee, profile);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void startConference(String[] participants, ImsCallProfile profile) throws RemoteException {
            ImsCallSessionImplBase.this.startConference(participants, profile);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void accept(int callType, ImsStreamMediaProfile profile) {
            ImsCallSessionImplBase.this.accept(callType, profile);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void deflect(String deflectNumber) {
            ImsCallSessionImplBase.this.deflect(deflectNumber);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void reject(int reason) {
            ImsCallSessionImplBase.this.reject(reason);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void terminate(int reason) {
            ImsCallSessionImplBase.this.terminate(reason);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void hangupForegroundResumeBackground(int reason) throws RemoteException {
            ImsCallSessionImplBase.this.hangupForegroundResumeBackground(reason);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void hangupWaitingOrBackground(int reason) throws RemoteException {
            ImsCallSessionImplBase.this.hangupWaitingOrBackground(reason);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void hold(ImsStreamMediaProfile profile) {
            ImsCallSessionImplBase.this.hold(profile);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void resume(ImsStreamMediaProfile profile) {
            ImsCallSessionImplBase.this.resume(profile);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void merge() {
            ImsCallSessionImplBase.this.merge();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void update(int callType, ImsStreamMediaProfile profile) {
            ImsCallSessionImplBase.this.update(callType, profile);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void extendToConference(String[] participants) {
            ImsCallSessionImplBase.this.extendToConference(participants);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void inviteParticipants(String[] participants) {
            ImsCallSessionImplBase.this.inviteParticipants(participants);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void removeParticipants(String[] participants) {
            ImsCallSessionImplBase.this.removeParticipants(participants);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void sendDtmf(char c, Message result) {
            ImsCallSessionImplBase.this.sendDtmf(c, result);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void startDtmf(char c) {
            ImsCallSessionImplBase.this.startDtmf(c);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void stopDtmf() {
            ImsCallSessionImplBase.this.stopDtmf();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void sendUssd(String ussdMessage) {
            ImsCallSessionImplBase.this.sendUssd(ussdMessage);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public IImsVideoCallProvider getVideoCallProvider() {
            return ImsCallSessionImplBase.this.getVideoCallProvider();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public boolean isMultiparty() {
            return ImsCallSessionImplBase.this.isMultiparty();
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void sendRttModifyRequest(ImsCallProfile toProfile) {
            ImsCallSessionImplBase.this.sendRttModifyRequest(toProfile);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void sendRttModifyResponse(boolean status) {
            ImsCallSessionImplBase.this.sendRttModifyResponse(status);
        }

        @Override // com.android.ims.internal.IImsCallSession
        public void sendRttMessage(String rttMessage) {
            ImsCallSessionImplBase.this.sendRttMessage(rttMessage);
        }

        @Override // com.android.ims.internal.IImsCallSession.Stub, android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (ImsCallSessionImplBase.this.isHwCustCode(code)) {
                return ImsCallSessionImplBase.this.onTransact(code, data, reply, flags);
            }
            return super.onTransact(code, data, reply, flags);
        }
    };

    public static class State {
        public static final int ESTABLISHED = 4;
        public static final int ESTABLISHING = 3;
        public static final int IDLE = 0;
        public static final int INITIATED = 1;
        public static final int INVALID = -1;
        public static final int NEGOTIATING = 2;
        public static final int REESTABLISHING = 6;
        public static final int RENEGOTIATING = 5;
        public static final int TERMINATED = 8;
        public static final int TERMINATING = 7;

        public static String toString(int state) {
            switch (state) {
                case 0:
                    return "IDLE";
                case 1:
                    return "INITIATED";
                case 2:
                    return "NEGOTIATING";
                case 3:
                    return "ESTABLISHING";
                case 4:
                    return "ESTABLISHED";
                case 5:
                    return "RENEGOTIATING";
                case 6:
                    return "REESTABLISHING";
                case 7:
                    return "TERMINATING";
                case 8:
                    return "TERMINATED";
                default:
                    return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            }
        }

        private State() {
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }

    public boolean isHwCustCode(int code) {
        return false;
    }

    public final void setListener(IImsCallSessionListener listener) throws RemoteException {
        setListener(new ImsCallSessionListener(listener));
    }

    public void setListener(ImsCallSessionListener listener) {
    }

    @Override // java.lang.AutoCloseable
    public void close() {
    }

    public String getCallId() {
        return null;
    }

    public ImsCallProfile getCallProfile() {
        return null;
    }

    public ImsCallProfile getLocalCallProfile() {
        return null;
    }

    public ImsCallProfile getRemoteCallProfile() {
        return null;
    }

    public String getProperty(String name) {
        return null;
    }

    public int getState() {
        return -1;
    }

    public boolean isInCall() {
        return false;
    }

    public void setMute(boolean muted) {
    }

    public void start(String callee, ImsCallProfile profile) {
    }

    public void startConference(String[] participants, ImsCallProfile profile) {
    }

    public void accept(int callType, ImsStreamMediaProfile profile) {
    }

    public void deflect(String deflectNumber) {
    }

    public void reject(int reason) {
    }

    public void terminate(int reason) {
    }

    public void hangupForegroundResumeBackground(int reason) throws RemoteException {
    }

    public void hangupWaitingOrBackground(int reason) throws RemoteException {
    }

    public void hold(ImsStreamMediaProfile profile) {
    }

    public void resume(ImsStreamMediaProfile profile) {
    }

    public void merge() {
    }

    public void update(int callType, ImsStreamMediaProfile profile) {
    }

    public void extendToConference(String[] participants) {
    }

    public void inviteParticipants(String[] participants) {
    }

    public void removeParticipants(String[] participants) {
    }

    public void sendDtmf(char c, Message result) {
    }

    public void startDtmf(char c) {
    }

    public void stopDtmf() {
    }

    public void sendUssd(String ussdMessage) {
    }

    public IImsVideoCallProvider getVideoCallProvider() {
        ImsVideoCallProvider provider = getImsVideoCallProvider();
        if (provider != null) {
            return provider.getInterface();
        }
        return null;
    }

    public ImsVideoCallProvider getImsVideoCallProvider() {
        return null;
    }

    public boolean isMultiparty() {
        return false;
    }

    public void sendRttModifyRequest(ImsCallProfile toProfile) {
    }

    public void sendRttModifyResponse(boolean status) {
    }

    public void sendRttMessage(String rttMessage) {
    }

    public IImsCallSession getServiceImpl() {
        return this.mServiceImpl;
    }

    public void setServiceImpl(IImsCallSession serviceImpl) {
        this.mServiceImpl = serviceImpl;
    }
}
