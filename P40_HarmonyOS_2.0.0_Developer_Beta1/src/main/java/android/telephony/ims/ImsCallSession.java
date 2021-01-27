package android.telephony.ims;

import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.CallQuality;
import android.telephony.Rlog;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.util.Log;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsVideoCallProvider;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.IccCardConstants;
import java.util.Objects;

public class ImsCallSession {
    private static final int CODE_HOLD_FOR_IMS = 2001;
    private static final int CODE_RESUME_FOR_IMS = 2002;
    private static final String DESCRIPTOR = "com.huawei.ims.HwImsCallSessionImpl";
    private static final String HW_VTLTE_ON = "ro.config.hw_vtlte_on";
    private static final boolean IS_HISI_PLATFORM = HuaweiTelephonyConfigs.isHisiPlatform();
    private static final boolean IS_VILTE_ENHANCEMENT_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_vilte_enhancement", false);
    private static final boolean IS_VTLTE_SUPPORTED = SystemProperties.getBoolean(HW_VTLTE_ON, false);
    private static final String TAG = "ImsCallSession";
    private boolean mClosed;
    private Listener mListener;
    private final IImsCallSession miSession;

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

    public static class Listener {
        public void callSessionProgressing(ImsCallSession session, ImsStreamMediaProfile profile) {
        }

        public void callSessionStarted(ImsCallSession session, ImsCallProfile profile) {
        }

        public void callSessionStartFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionTerminated(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionHeld(ImsCallSession session, ImsCallProfile profile) {
        }

        public void callSessionHoldFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionHoldReceived(ImsCallSession session, ImsCallProfile profile) {
        }

        public void callSessionResumed(ImsCallSession session, ImsCallProfile profile) {
        }

        public void callSessionResumeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionResumeReceived(ImsCallSession session, ImsCallProfile profile) {
        }

        public void callSessionMergeStarted(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
        }

        public void callSessionMergeComplete(ImsCallSession session) {
        }

        public void callSessionMergeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionUpdated(ImsCallSession session, ImsCallProfile profile) {
        }

        public void callSessionUpdateFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionUpdateReceived(ImsCallSession session, ImsCallProfile profile) {
        }

        public void callSessionConferenceExtended(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
        }

        public void callSessionConferenceExtendFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionConferenceExtendReceived(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
        }

        public void callSessionInviteParticipantsRequestDelivered(ImsCallSession session) {
        }

        public void callSessionInviteParticipantsRequestFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionRemoveParticipantsRequestDelivered(ImsCallSession session) {
        }

        public void callSessionRemoveParticipantsRequestFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
        }

        public void callSessionConferenceStateUpdated(ImsCallSession session, ImsConferenceState state) {
        }

        public void callSessionUssdMessageReceived(ImsCallSession session, int mode, String ussdMessage) {
        }

        public void callSessionMayHandover(ImsCallSession session, int srcAccessTech, int targetAccessTech) {
        }

        public void callSessionHandover(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void callSessionHandoverFailed(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void callSessionTtyModeReceived(ImsCallSession session, int mode) {
        }

        public void callSessionMultipartyStateChanged(ImsCallSession session, boolean isMultiParty) {
        }

        public void callSessionSuppServiceReceived(ImsCallSession session, ImsSuppServiceNotification suppServiceInfo) {
        }

        public void callSessionRttModifyRequestReceived(ImsCallSession session, ImsCallProfile callProfile) {
        }

        public void callSessionRttModifyResponseReceived(int status) {
        }

        public void callSessionRttMessageReceived(String rttMessage) {
        }

        public void callSessionRttAudioIndicatorChanged(ImsStreamMediaProfile profile) {
        }

        public void callQualityChanged(CallQuality callQuality) {
        }
    }

    public ImsCallSession(IImsCallSession iSession) {
        this.mClosed = false;
        this.miSession = iSession;
        if (iSession != null) {
            try {
                iSession.setListener(new IImsCallSessionListenerProxy());
            } catch (RemoteException e) {
            }
        } else {
            this.mClosed = true;
        }
    }

    public ImsCallSession(IImsCallSession iSession, Listener listener) {
        this(iSession);
        setListener(listener);
    }

    public void close() {
        synchronized (this) {
            if (!this.mClosed) {
                try {
                    this.miSession.close();
                    this.mClosed = true;
                } catch (RemoteException e) {
                }
            }
        }
    }

    public String getCallId() {
        if (this.mClosed) {
            return null;
        }
        try {
            return this.miSession.getCallId();
        } catch (RemoteException e) {
            return null;
        }
    }

    public ImsCallProfile getCallProfile() {
        if (this.mClosed) {
            return null;
        }
        try {
            return this.miSession.getCallProfile();
        } catch (RemoteException e) {
            return null;
        }
    }

    public ImsCallProfile getLocalCallProfile() {
        if (this.mClosed) {
            return null;
        }
        try {
            return this.miSession.getLocalCallProfile();
        } catch (RemoteException e) {
            return null;
        }
    }

    public ImsCallProfile getRemoteCallProfile() {
        if (this.mClosed) {
            return null;
        }
        try {
            return this.miSession.getRemoteCallProfile();
        } catch (RemoteException e) {
            return null;
        }
    }

    public IImsVideoCallProvider getVideoCallProvider() {
        if (this.mClosed) {
            return null;
        }
        try {
            return this.miSession.getVideoCallProvider();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getProperty(String name) {
        if (this.mClosed) {
            return null;
        }
        try {
            return this.miSession.getProperty(name);
        } catch (RemoteException e) {
            return null;
        }
    }

    public int getState() {
        if (this.mClosed) {
            return -1;
        }
        try {
            return this.miSession.getState();
        } catch (RemoteException e) {
            return -1;
        }
    }

    public boolean isAlive() {
        if (this.mClosed) {
            return false;
        }
        switch (getState()) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    public IImsCallSession getSession() {
        return this.miSession;
    }

    public boolean isInCall() {
        if (this.mClosed) {
            return false;
        }
        try {
            return this.miSession.isInCall();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setMute(boolean muted) {
        if (!this.mClosed) {
            try {
                this.miSession.setMute(muted);
            } catch (RemoteException e) {
            }
        }
    }

    public void start(String callee, ImsCallProfile profile) {
        if (!this.mClosed) {
            try {
                this.miSession.start(callee, profile);
            } catch (RemoteException e) {
            }
        }
    }

    public void start(String[] participants, ImsCallProfile profile) {
        if (!this.mClosed) {
            try {
                this.miSession.startConference(participants, profile);
            } catch (RemoteException e) {
            }
        }
    }

    public void accept(int callType, ImsStreamMediaProfile profile) {
        if (!this.mClosed) {
            try {
                this.miSession.accept(callType, profile);
            } catch (RemoteException e) {
            }
        }
    }

    public void deflect(String number) {
        if (!this.mClosed) {
            try {
                this.miSession.deflect(number);
            } catch (RemoteException e) {
            }
        }
    }

    public void reject(int reason) {
        if (!this.mClosed) {
            try {
                this.miSession.reject(reason);
            } catch (RemoteException e) {
            }
        }
    }

    public void terminate(int reason) {
        if (!this.mClosed) {
            try {
                this.miSession.terminate(reason);
            } catch (RemoteException e) {
            }
        }
    }

    public void hangupForegroundResumeBackground(int reason) {
        if (!this.mClosed) {
            try {
                this.miSession.hangupForegroundResumeBackground(reason);
            } catch (RemoteException e) {
                Rlog.e(TAG, "hangupForegroundResumeBackground: unexpected error on hangup");
            }
        }
    }

    public void hangupWaitingOrBackground(int reason) {
        if (!this.mClosed) {
            try {
                this.miSession.hangupWaitingOrBackground(reason);
            } catch (RemoteException e) {
                Rlog.e(TAG, "hangupWaitingOrBackground: unexpected error on hangup");
            }
        }
    }

    public void hold(int callType, ImsStreamMediaProfile profile) {
        if (!this.mClosed) {
            try {
                if (!isVilteEnhancementSupported() || !IS_HISI_PLATFORM) {
                    this.miSession.hold(profile);
                } else {
                    holdForIms(callType);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void resume(int callType, ImsStreamMediaProfile profile) {
        if (!this.mClosed) {
            try {
                if (!isVilteEnhancementSupported() || !IS_HISI_PLATFORM) {
                    this.miSession.resume(profile);
                } else {
                    resumeForIms(callType);
                }
            } catch (RemoteException e) {
            }
        }
    }

    private void holdForIms(int callType) {
        Rlog.d(TAG, "holdForIms callType:" + callType);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = this.miSession.asBinder();
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(callType);
                b.transact(2001, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException e) {
                Rlog.e(TAG, "holdForIms transact failed!");
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        } else {
            Rlog.d(TAG, "holdForIms can not get interface!");
        }
        _reply.recycle();
        _data.recycle();
    }

    private void resumeForIms(int callType) {
        Rlog.d(TAG, "resumeForIms callType:" + callType);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = this.miSession.asBinder();
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(callType);
                b.transact(2002, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException e) {
                Rlog.e(TAG, "resumeForIms transact failed!");
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        } else {
            Rlog.d(TAG, "resumeForIms can not get interface!");
        }
        _reply.recycle();
        _data.recycle();
    }

    private static boolean isVilteEnhancementSupported() {
        return IS_VTLTE_SUPPORTED && IS_VILTE_ENHANCEMENT_SUPPORTED;
    }

    public void merge() {
        if (!this.mClosed) {
            try {
                this.miSession.merge();
            } catch (RemoteException e) {
            }
        }
    }

    public void update(int callType, ImsStreamMediaProfile profile) {
        if (!this.mClosed) {
            try {
                this.miSession.update(callType, profile);
            } catch (RemoteException e) {
            }
        }
    }

    public void extendToConference(String[] participants) {
        if (!this.mClosed) {
            try {
                this.miSession.extendToConference(participants);
            } catch (RemoteException e) {
            }
        }
    }

    public void inviteParticipants(String[] participants) {
        if (!this.mClosed) {
            try {
                this.miSession.inviteParticipants(participants);
            } catch (RemoteException e) {
            }
        }
    }

    public void removeParticipants(String[] participants) {
        if (!this.mClosed) {
            try {
                this.miSession.removeParticipants(participants);
            } catch (RemoteException e) {
            }
        }
    }

    public void sendDtmf(char c, Message result) {
        if (!this.mClosed) {
            try {
                this.miSession.sendDtmf(c, result);
            } catch (RemoteException e) {
            }
        }
    }

    public void startDtmf(char c) {
        if (!this.mClosed) {
            try {
                this.miSession.startDtmf(c);
            } catch (RemoteException e) {
            }
        }
    }

    public void stopDtmf() {
        if (!this.mClosed) {
            try {
                this.miSession.stopDtmf();
            } catch (RemoteException e) {
            }
        }
    }

    public void sendUssd(String ussdMessage) {
        if (!this.mClosed) {
            try {
                this.miSession.sendUssd(ussdMessage);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean isMultiparty() {
        if (this.mClosed) {
            return false;
        }
        try {
            return this.miSession.isMultiparty();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void sendRttMessage(String rttMessage) {
        if (!this.mClosed) {
            try {
                this.miSession.sendRttMessage(rttMessage);
            } catch (RemoteException e) {
            }
        }
    }

    public void sendRttModifyRequest(ImsCallProfile to) {
        if (!this.mClosed) {
            try {
                this.miSession.sendRttModifyRequest(to);
            } catch (RemoteException e) {
            }
        }
    }

    public void sendRttModifyResponse(boolean response) {
        if (!this.mClosed) {
            try {
                this.miSession.sendRttModifyResponse(response);
            } catch (RemoteException e) {
            }
        }
    }

    private class IImsCallSessionListenerProxy extends IImsCallSessionListener.Stub {
        private IImsCallSessionListenerProxy() {
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionProgressing(ImsStreamMediaProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionProgressing(ImsCallSession.this, profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionInitiated(ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionStarted(ImsCallSession.this, profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionInitiatedFailed(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionStartFailed(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionTerminated(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionTerminated(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionHeld(ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionHeld(ImsCallSession.this, profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionHoldFailed(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionHoldFailed(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionHoldReceived(ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionHoldReceived(ImsCallSession.this, profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionResumed(ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionResumed(ImsCallSession.this, profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionResumeFailed(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionResumeFailed(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionResumeReceived(ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionResumeReceived(ImsCallSession.this, profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionMergeStarted(IImsCallSession newSession, ImsCallProfile profile) {
            Log.d(ImsCallSession.TAG, "callSessionMergeStarted");
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionMergeComplete(IImsCallSession newSession) {
            if (ImsCallSession.this.mListener == null) {
                return;
            }
            if (newSession != null) {
                ImsCallSession validActiveSession = ImsCallSession.this;
                try {
                    if (!Objects.equals(ImsCallSession.this.miSession.getCallId(), newSession.getCallId())) {
                        validActiveSession = new ImsCallSession(newSession);
                    }
                } catch (RemoteException e) {
                    Log.e(ImsCallSession.TAG, "callSessionMergeComplete: exception for getCallId!");
                }
                ImsCallSession.this.mListener.callSessionMergeComplete(validActiveSession);
                return;
            }
            ImsCallSession.this.mListener.callSessionMergeComplete(null);
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionMergeFailed(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionMergeFailed(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionUpdated(ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionUpdated(ImsCallSession.this, profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionUpdateFailed(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionUpdateFailed(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionUpdateReceived(ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionUpdateReceived(ImsCallSession.this, profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionConferenceExtended(IImsCallSession newSession, ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionConferenceExtended(ImsCallSession.this, new ImsCallSession(newSession), profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionConferenceExtendFailed(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionConferenceExtendFailed(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionConferenceExtendReceived(IImsCallSession newSession, ImsCallProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionConferenceExtendReceived(ImsCallSession.this, new ImsCallSession(newSession), profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionInviteParticipantsRequestDelivered() {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionInviteParticipantsRequestDelivered(ImsCallSession.this);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionInviteParticipantsRequestFailed(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionInviteParticipantsRequestFailed(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionRemoveParticipantsRequestDelivered() {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionRemoveParticipantsRequestDelivered(ImsCallSession.this);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionRemoveParticipantsRequestFailed(ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionRemoveParticipantsRequestFailed(ImsCallSession.this, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionConferenceStateUpdated(ImsConferenceState state) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionConferenceStateUpdated(ImsCallSession.this, state);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionUssdMessageReceived(int mode, String ussdMessage) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionUssdMessageReceived(ImsCallSession.this, mode, ussdMessage);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionMayHandover(int srcAccessTech, int targetAccessTech) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionMayHandover(ImsCallSession.this, srcAccessTech, targetAccessTech);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionHandover(int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionHandover(ImsCallSession.this, srcAccessTech, targetAccessTech, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionHandoverFailed(int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionHandoverFailed(ImsCallSession.this, srcAccessTech, targetAccessTech, reasonInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionTtyModeReceived(int mode) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionTtyModeReceived(ImsCallSession.this, mode);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionMultipartyStateChanged(boolean isMultiParty) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionMultipartyStateChanged(ImsCallSession.this, isMultiParty);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionSuppServiceReceived(ImsSuppServiceNotification suppServiceInfo) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionSuppServiceReceived(ImsCallSession.this, suppServiceInfo);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionRttModifyRequestReceived(ImsCallProfile callProfile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionRttModifyRequestReceived(ImsCallSession.this, callProfile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionRttModifyResponseReceived(int status) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionRttModifyResponseReceived(status);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionRttMessageReceived(String rttMessage) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionRttMessageReceived(rttMessage);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callSessionRttAudioIndicatorChanged(ImsStreamMediaProfile profile) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callSessionRttAudioIndicatorChanged(profile);
            }
        }

        @Override // android.telephony.ims.aidl.IImsCallSessionListener
        public void callQualityChanged(CallQuality callQuality) {
            if (ImsCallSession.this.mListener != null) {
                ImsCallSession.this.mListener.callQualityChanged(callQuality);
            }
        }
    }

    public String toString() {
        return "[ImsCallSession objId:" + System.identityHashCode(this) + " state:" + State.toString(getState()) + " callId:" + getCallId() + "]";
    }
}
