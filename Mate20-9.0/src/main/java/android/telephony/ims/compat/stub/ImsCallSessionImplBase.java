package android.telephony.ims.compat.stub;

import android.os.Message;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsVideoCallProvider;

public class ImsCallSessionImplBase extends IImsCallSession.Stub {

    private class ImsCallSessionListenerConverter extends IImsCallSessionListener.Stub {
        private final android.telephony.ims.aidl.IImsCallSessionListener mNewListener;

        public ImsCallSessionListenerConverter(android.telephony.ims.aidl.IImsCallSessionListener listener) {
            this.mNewListener = listener;
        }

        public void callSessionProgressing(IImsCallSession i, ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException {
            this.mNewListener.callSessionProgressing(imsStreamMediaProfile);
        }

        public void callSessionStarted(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionInitiated(imsCallProfile);
        }

        public void callSessionStartFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionInitiatedFailed(imsReasonInfo);
        }

        public void callSessionTerminated(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionTerminated(imsReasonInfo);
        }

        public void callSessionHeld(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionHeld(imsCallProfile);
        }

        public void callSessionHoldFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionHoldFailed(imsReasonInfo);
        }

        public void callSessionHoldReceived(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionHoldReceived(imsCallProfile);
        }

        public void callSessionResumed(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionResumed(imsCallProfile);
        }

        public void callSessionResumeFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionResumeFailed(imsReasonInfo);
        }

        public void callSessionResumeReceived(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionResumeReceived(imsCallProfile);
        }

        public void callSessionMergeStarted(IImsCallSession i, IImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
            this.mNewListener.callSessionMergeStarted(newSession, profile);
        }

        public void callSessionMergeComplete(IImsCallSession iImsCallSession) throws RemoteException {
            this.mNewListener.callSessionMergeComplete(iImsCallSession);
        }

        public void callSessionMergeFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionMergeFailed(imsReasonInfo);
        }

        public void callSessionUpdated(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionUpdated(imsCallProfile);
        }

        public void callSessionUpdateFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionUpdateFailed(imsReasonInfo);
        }

        public void callSessionUpdateReceived(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionUpdateReceived(imsCallProfile);
        }

        public void callSessionConferenceExtended(IImsCallSession i, IImsCallSession newSession, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionConferenceExtended(newSession, imsCallProfile);
        }

        public void callSessionConferenceExtendFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionConferenceExtendFailed(imsReasonInfo);
        }

        public void callSessionConferenceExtendReceived(IImsCallSession i, IImsCallSession newSession, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionConferenceExtendReceived(newSession, imsCallProfile);
        }

        public void callSessionInviteParticipantsRequestDelivered(IImsCallSession i) throws RemoteException {
            this.mNewListener.callSessionInviteParticipantsRequestDelivered();
        }

        public void callSessionInviteParticipantsRequestFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionInviteParticipantsRequestFailed(imsReasonInfo);
        }

        public void callSessionRemoveParticipantsRequestDelivered(IImsCallSession i) throws RemoteException {
            this.mNewListener.callSessionRemoveParticipantsRequestDelivered();
        }

        public void callSessionRemoveParticipantsRequestFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionRemoveParticipantsRequestFailed(imsReasonInfo);
        }

        public void callSessionConferenceStateUpdated(IImsCallSession i, ImsConferenceState imsConferenceState) throws RemoteException {
            this.mNewListener.callSessionConferenceStateUpdated(imsConferenceState);
        }

        public void callSessionUssdMessageReceived(IImsCallSession i, int mode, String message) throws RemoteException {
            this.mNewListener.callSessionUssdMessageReceived(mode, message);
        }

        public void callSessionHandover(IImsCallSession i, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) throws RemoteException {
            this.mNewListener.callSessionHandover(srcAccessTech, targetAccessTech, reasonInfo);
        }

        public void callSessionHandoverFailed(IImsCallSession i, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) throws RemoteException {
            this.mNewListener.callSessionHandoverFailed(srcAccessTech, targetAccessTech, reasonInfo);
        }

        public void callSessionMayHandover(IImsCallSession i, int srcAccessTech, int targetAccessTech) throws RemoteException {
            this.mNewListener.callSessionMayHandover(srcAccessTech, targetAccessTech);
        }

        public void callSessionTtyModeReceived(IImsCallSession iImsCallSession, int mode) throws RemoteException {
            this.mNewListener.callSessionTtyModeReceived(mode);
        }

        public void callSessionMultipartyStateChanged(IImsCallSession i, boolean isMultiparty) throws RemoteException {
            this.mNewListener.callSessionMultipartyStateChanged(isMultiparty);
        }

        public void callSessionSuppServiceReceived(IImsCallSession i, ImsSuppServiceNotification imsSuppServiceNotification) throws RemoteException {
            this.mNewListener.callSessionSuppServiceReceived(imsSuppServiceNotification);
        }

        public void callSessionRttModifyRequestReceived(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionRttModifyRequestReceived(imsCallProfile);
        }

        public void callSessionRttModifyResponseReceived(int status) throws RemoteException {
            this.mNewListener.callSessionRttModifyResponseReceived(status);
        }

        public void callSessionRttMessageReceived(String rttMessage) throws RemoteException {
            this.mNewListener.callSessionRttMessageReceived(rttMessage);
        }
    }

    public final void setListener(android.telephony.ims.aidl.IImsCallSessionListener listener) throws RemoteException {
        setListener((IImsCallSessionListener) new ImsCallSessionListenerConverter(listener));
    }

    public void setListener(IImsCallSessionListener listener) {
    }

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
}
