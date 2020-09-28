package android.telephony.ims.compat.stub;

import android.os.Message;
import android.os.RemoteException;
import android.telephony.CallQuality;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import android.telephony.ims.aidl.IImsCallSessionListener;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsVideoCallProvider;

public class ImsCallSessionImplBase extends IImsCallSession.Stub {
    @Override // com.android.ims.internal.IImsCallSession
    public final void setListener(IImsCallSessionListener listener) throws RemoteException {
        setListener(new ImsCallSessionListenerConverter(listener));
    }

    public void setListener(com.android.ims.internal.IImsCallSessionListener listener) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void close() {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public String getCallId() {
        return null;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public ImsCallProfile getCallProfile() {
        return null;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public ImsCallProfile getLocalCallProfile() {
        return null;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public ImsCallProfile getRemoteCallProfile() {
        return null;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public String getProperty(String name) {
        return null;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public int getState() {
        return -1;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public boolean isInCall() {
        return false;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void setMute(boolean muted) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void start(String callee, ImsCallProfile profile) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void startConference(String[] participants, ImsCallProfile profile) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void accept(int callType, ImsStreamMediaProfile profile) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void deflect(String deflectNumber) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void reject(int reason) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void terminate(int reason) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void hangupForegroundResumeBackground(int reason) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void hangupWaitingOrBackground(int reason) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void hold(ImsStreamMediaProfile profile) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void resume(ImsStreamMediaProfile profile) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void merge() {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void update(int callType, ImsStreamMediaProfile profile) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void extendToConference(String[] participants) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void inviteParticipants(String[] participants) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void removeParticipants(String[] participants) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void sendDtmf(char c, Message result) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void startDtmf(char c) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void stopDtmf() {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void sendUssd(String ussdMessage) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public IImsVideoCallProvider getVideoCallProvider() {
        return null;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public boolean isMultiparty() {
        return false;
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void sendRttModifyRequest(ImsCallProfile toProfile) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void sendRttModifyResponse(boolean status) {
    }

    @Override // com.android.ims.internal.IImsCallSession
    public void sendRttMessage(String rttMessage) {
    }

    private class ImsCallSessionListenerConverter extends IImsCallSessionListener.Stub {
        private final android.telephony.ims.aidl.IImsCallSessionListener mNewListener;

        public ImsCallSessionListenerConverter(android.telephony.ims.aidl.IImsCallSessionListener listener) {
            this.mNewListener = listener;
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionProgressing(IImsCallSession i, ImsStreamMediaProfile imsStreamMediaProfile) throws RemoteException {
            this.mNewListener.callSessionProgressing(imsStreamMediaProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionStarted(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionInitiated(imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionStartFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionInitiatedFailed(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionTerminated(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionTerminated(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionHeld(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionHeld(imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionHoldFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionHoldFailed(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionHoldReceived(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionHoldReceived(imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionResumed(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionResumed(imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionResumeFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionResumeFailed(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionResumeReceived(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionResumeReceived(imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionMergeStarted(IImsCallSession i, IImsCallSession newSession, ImsCallProfile profile) throws RemoteException {
            this.mNewListener.callSessionMergeStarted(newSession, profile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionMergeComplete(IImsCallSession iImsCallSession) throws RemoteException {
            this.mNewListener.callSessionMergeComplete(iImsCallSession);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionMergeFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionMergeFailed(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionUpdated(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionUpdated(imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionUpdateFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionUpdateFailed(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionUpdateReceived(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionUpdateReceived(imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionConferenceExtended(IImsCallSession i, IImsCallSession newSession, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionConferenceExtended(newSession, imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionConferenceExtendFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionConferenceExtendFailed(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionConferenceExtendReceived(IImsCallSession i, IImsCallSession newSession, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionConferenceExtendReceived(newSession, imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionInviteParticipantsRequestDelivered(IImsCallSession i) throws RemoteException {
            this.mNewListener.callSessionInviteParticipantsRequestDelivered();
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionInviteParticipantsRequestFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionInviteParticipantsRequestFailed(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionRemoveParticipantsRequestDelivered(IImsCallSession i) throws RemoteException {
            this.mNewListener.callSessionRemoveParticipantsRequestDelivered();
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionRemoveParticipantsRequestFailed(IImsCallSession i, ImsReasonInfo imsReasonInfo) throws RemoteException {
            this.mNewListener.callSessionRemoveParticipantsRequestFailed(imsReasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionConferenceStateUpdated(IImsCallSession i, ImsConferenceState imsConferenceState) throws RemoteException {
            this.mNewListener.callSessionConferenceStateUpdated(imsConferenceState);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionUssdMessageReceived(IImsCallSession i, int mode, String message) throws RemoteException {
            this.mNewListener.callSessionUssdMessageReceived(mode, message);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionHandover(IImsCallSession i, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) throws RemoteException {
            this.mNewListener.callSessionHandover(srcAccessTech, targetAccessTech, reasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionHandoverFailed(IImsCallSession i, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) throws RemoteException {
            this.mNewListener.callSessionHandoverFailed(srcAccessTech, targetAccessTech, reasonInfo);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionMayHandover(IImsCallSession i, int srcAccessTech, int targetAccessTech) throws RemoteException {
            this.mNewListener.callSessionMayHandover(srcAccessTech, targetAccessTech);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionTtyModeReceived(IImsCallSession iImsCallSession, int mode) throws RemoteException {
            this.mNewListener.callSessionTtyModeReceived(mode);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionMultipartyStateChanged(IImsCallSession i, boolean isMultiparty) throws RemoteException {
            this.mNewListener.callSessionMultipartyStateChanged(isMultiparty);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionSuppServiceReceived(IImsCallSession i, ImsSuppServiceNotification imsSuppServiceNotification) throws RemoteException {
            this.mNewListener.callSessionSuppServiceReceived(imsSuppServiceNotification);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionRttModifyRequestReceived(IImsCallSession i, ImsCallProfile imsCallProfile) throws RemoteException {
            this.mNewListener.callSessionRttModifyRequestReceived(imsCallProfile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionRttModifyResponseReceived(int status) throws RemoteException {
            this.mNewListener.callSessionRttModifyResponseReceived(status);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionRttMessageReceived(String rttMessage) throws RemoteException {
            this.mNewListener.callSessionRttMessageReceived(rttMessage);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callSessionRttAudioIndicatorChanged(ImsStreamMediaProfile profile) throws RemoteException {
            this.mNewListener.callSessionRttAudioIndicatorChanged(profile);
        }

        @Override // com.android.ims.internal.IImsCallSessionListener
        public void callQualityChanged(CallQuality callQuality) throws RemoteException {
            this.mNewListener.callQualityChanged(callQuality);
        }
    }
}
