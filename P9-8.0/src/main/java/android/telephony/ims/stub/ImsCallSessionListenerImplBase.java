package android.telephony.ims.stub;

import com.android.ims.ImsCallProfile;
import com.android.ims.ImsConferenceState;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsStreamMediaProfile;
import com.android.ims.ImsSuppServiceNotification;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener.Stub;

public class ImsCallSessionListenerImplBase extends Stub {
    public void callSessionProgressing(IImsCallSession session, ImsStreamMediaProfile profile) {
    }

    public void callSessionStarted(IImsCallSession session, ImsCallProfile profile) {
    }

    public void callSessionStartFailed(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionTerminated(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionHeld(IImsCallSession session, ImsCallProfile profile) {
    }

    public void callSessionHoldFailed(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionHoldReceived(IImsCallSession session, ImsCallProfile profile) {
    }

    public void callSessionResumed(IImsCallSession session, ImsCallProfile profile) {
    }

    public void callSessionResumeFailed(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionResumeReceived(IImsCallSession session, ImsCallProfile profile) {
    }

    public void callSessionMergeStarted(IImsCallSession session, IImsCallSession newSession, ImsCallProfile profile) {
    }

    public void callSessionMergeComplete(IImsCallSession session) {
    }

    public void callSessionMergeFailed(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionUpdated(IImsCallSession session, ImsCallProfile profile) {
    }

    public void callSessionUpdateFailed(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionUpdateReceived(IImsCallSession session, ImsCallProfile profile) {
    }

    public void callSessionConferenceExtended(IImsCallSession session, IImsCallSession newSession, ImsCallProfile profile) {
    }

    public void callSessionConferenceExtendFailed(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionConferenceExtendReceived(IImsCallSession session, IImsCallSession newSession, ImsCallProfile profile) {
    }

    public void callSessionInviteParticipantsRequestDelivered(IImsCallSession session) {
    }

    public void callSessionInviteParticipantsRequestFailed(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionRemoveParticipantsRequestDelivered(IImsCallSession session) {
    }

    public void callSessionRemoveParticipantsRequestFailed(IImsCallSession session, ImsReasonInfo reasonInfo) {
    }

    public void callSessionConferenceStateUpdated(IImsCallSession session, ImsConferenceState state) {
    }

    public void callSessionUssdMessageReceived(IImsCallSession session, int mode, String ussdMessage) {
    }

    public void callSessionHandover(IImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
    }

    public void callSessionHandoverFailed(IImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
    }

    public void callSessionTtyModeReceived(IImsCallSession session, int mode) {
    }

    public void callSessionMultipartyStateChanged(IImsCallSession session, boolean isMultiParty) {
    }

    public void callSessionSuppServiceReceived(IImsCallSession session, ImsSuppServiceNotification suppSrvNotification) {
    }
}
