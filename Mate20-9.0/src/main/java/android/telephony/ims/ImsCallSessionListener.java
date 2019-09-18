package android.telephony.ims;

import android.annotation.SystemApi;
import android.os.RemoteException;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.telephony.ims.stub.ImsCallSessionImplBase;
import com.android.ims.internal.IImsCallSession;

@SystemApi
public class ImsCallSessionListener {
    private final IImsCallSessionListener mListener;

    public ImsCallSessionListener(IImsCallSessionListener l) {
        this.mListener = l;
    }

    public void callSessionProgressing(ImsStreamMediaProfile profile) {
        try {
            this.mListener.callSessionProgressing(profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionInitiated(ImsCallProfile profile) {
        try {
            this.mListener.callSessionInitiated(profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionInitiatedFailed(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionInitiatedFailed(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionTerminated(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionTerminated(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionHeld(ImsCallProfile profile) {
        try {
            this.mListener.callSessionHeld(profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionHoldFailed(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionHoldFailed(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionHoldReceived(ImsCallProfile profile) {
        try {
            this.mListener.callSessionHoldReceived(profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionResumed(ImsCallProfile profile) {
        try {
            this.mListener.callSessionResumed(profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionResumeFailed(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionResumeFailed(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionResumeReceived(ImsCallProfile profile) {
        try {
            this.mListener.callSessionResumeReceived(profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionMergeStarted(ImsCallSessionImplBase newSession, ImsCallProfile profile) {
        try {
            this.mListener.callSessionMergeStarted(newSession != null ? newSession.getServiceImpl() : null, profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionMergeStarted(IImsCallSession newSession, ImsCallProfile profile) {
        try {
            this.mListener.callSessionMergeStarted(newSession, profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionMergeComplete(ImsCallSessionImplBase newSession) {
        try {
            this.mListener.callSessionMergeComplete(newSession != null ? newSession.getServiceImpl() : null);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionMergeComplete(IImsCallSession newSession) {
        try {
            this.mListener.callSessionMergeComplete(newSession);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionMergeFailed(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionMergeFailed(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionUpdated(ImsCallProfile profile) {
        try {
            this.mListener.callSessionUpdated(profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionUpdateFailed(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionUpdateFailed(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionUpdateReceived(ImsCallProfile profile) {
        try {
            this.mListener.callSessionUpdateReceived(profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionConferenceExtended(ImsCallSessionImplBase newSession, ImsCallProfile profile) {
        try {
            this.mListener.callSessionConferenceExtended(newSession != null ? newSession.getServiceImpl() : null, profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionConferenceExtended(IImsCallSession newSession, ImsCallProfile profile) {
        try {
            this.mListener.callSessionConferenceExtended(newSession, profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionConferenceExtendFailed(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionConferenceExtendFailed(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionConferenceExtendReceived(ImsCallSessionImplBase newSession, ImsCallProfile profile) {
        try {
            this.mListener.callSessionConferenceExtendReceived(newSession != null ? newSession.getServiceImpl() : null, profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionConferenceExtendReceived(IImsCallSession newSession, ImsCallProfile profile) {
        try {
            this.mListener.callSessionConferenceExtendReceived(newSession, profile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionInviteParticipantsRequestDelivered() {
        try {
            this.mListener.callSessionInviteParticipantsRequestDelivered();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionInviteParticipantsRequestFailed(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionInviteParticipantsRequestFailed(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionRemoveParticipantsRequestDelivered() {
        try {
            this.mListener.callSessionRemoveParticipantsRequestDelivered();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionRemoveParticipantsRequestFailed(ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionInviteParticipantsRequestFailed(reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionConferenceStateUpdated(ImsConferenceState state) {
        try {
            this.mListener.callSessionConferenceStateUpdated(state);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionUssdMessageReceived(int mode, String ussdMessage) {
        try {
            this.mListener.callSessionUssdMessageReceived(mode, ussdMessage);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionMayHandover(int srcAccessTech, int targetAccessTech) {
        try {
            this.mListener.callSessionMayHandover(srcAccessTech, targetAccessTech);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionHandover(int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionHandover(srcAccessTech, targetAccessTech, reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionHandoverFailed(int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        try {
            this.mListener.callSessionHandoverFailed(srcAccessTech, targetAccessTech, reasonInfo);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionTtyModeReceived(int mode) {
        try {
            this.mListener.callSessionTtyModeReceived(mode);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionMultipartyStateChanged(boolean isMultiParty) {
        try {
            this.mListener.callSessionMultipartyStateChanged(isMultiParty);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionSuppServiceReceived(ImsSuppServiceNotification suppSrvNotification) {
        try {
            this.mListener.callSessionSuppServiceReceived(suppSrvNotification);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionRttModifyRequestReceived(ImsCallProfile callProfile) {
        try {
            this.mListener.callSessionRttModifyRequestReceived(callProfile);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionRttModifyResponseReceived(int status) {
        try {
            this.mListener.callSessionRttModifyResponseReceived(status);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void callSessionRttMessageReceived(String rttMessage) {
        try {
            this.mListener.callSessionRttMessageReceived(rttMessage);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
