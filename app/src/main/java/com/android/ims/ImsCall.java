package com.android.ims;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.SettingsEx.Systemex;
import android.telecom.ConferenceParticipant;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.ims.internal.ICall;
import com.android.ims.internal.ImsCallSession;
import com.android.ims.internal.ImsCallSession.State;
import com.android.ims.internal.ImsStreamMediaSession;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class ImsCall implements ICall {
    private static final boolean CONF_DBG = true;
    private static final boolean DBG = false;
    private static final boolean FORCE_DEBUG = false;
    private static final String TAG = "ImsCall";
    private static final int UPDATE_EXTEND_TO_CONFERENCE = 5;
    private static final int UPDATE_HOLD = 1;
    private static final int UPDATE_HOLD_MERGE = 2;
    private static final int UPDATE_MERGE = 4;
    private static final int UPDATE_NONE = 0;
    private static final int UPDATE_RESUME = 3;
    private static final int UPDATE_UNSPECIFIED = 6;
    public static final int USSD_MODE_NOTIFY = 0;
    public static final int USSD_MODE_REQUEST = 1;
    private static final boolean VDBG = false;
    private static final boolean isImsAsNormal = false;
    private ImsCallProfile mCallProfile;
    private boolean mCallSessionMergePending;
    private List<ConferenceParticipant> mConferenceParticipants;
    private Context mContext;
    private boolean mHold;
    private boolean mInCall;
    private boolean mIsCEPPresent;
    private boolean mIsConferenceHost;
    private boolean mIsMerged;
    private ImsReasonInfo mLastReasonInfo;
    private Listener mListener;
    private Object mLockObj;
    private ImsStreamMediaSession mMediaSession;
    private ImsCall mMergeHost;
    private ImsCall mMergePeer;
    private boolean mMergeRequestedByConference;
    private boolean mMute;
    private ImsCallProfile mProposedCallProfile;
    private ImsCallSession mSession;
    private boolean mSessionEndDuringMerge;
    private ImsReasonInfo mSessionEndDuringMergeReasonInfo;
    private boolean mTerminationRequestPending;
    private ImsCallSession mTransientConferenceSession;
    private int mUpdateRequest;

    private class ImsCallSessionListenerProxy extends com.android.ims.internal.ImsCallSession.Listener {
        private ImsCallSessionListenerProxy() {
        }

        public void callSessionProgressing(ImsCallSession session, ImsStreamMediaProfile profile) {
            ImsCall.this.logi("callSessionProgressing :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionProgressing :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mCallProfile.mMediaProfile.copyFrom(profile);
            }
            if (listener != null) {
                try {
                    listener.onCallProgressing(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionProgressing :: ", t);
                }
            }
        }

        public void callSessionStarted(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionStarted :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionStarted :: on transient session=" + session);
                return;
            }
            ImsCall.this.setCallSessionMergePending(ImsCall.FORCE_DEBUG);
            if (!ImsCall.this.isTransientConferenceSession(session)) {
                Listener listener;
                synchronized (ImsCall.this) {
                    listener = ImsCall.this.mListener;
                    ImsCall.this.mCallProfile = profile;
                }
                if (listener != null) {
                    try {
                        listener.onCallStarted(ImsCall.this);
                    } catch (Throwable t) {
                        ImsCall.this.loge("callSessionStarted :: ", t);
                    }
                }
            }
        }

        public void callSessionStartFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionStartFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionStartFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mLastReasonInfo = reasonInfo;
            }
            if (listener != null) {
                try {
                    listener.onCallStartFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionStarted :: ", t);
                }
            }
        }

        public void callSessionTerminated(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.logi("callSessionTerminated :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionTerminated :: on transient session=" + session);
                ImsCall.this.processMergeFailed(reasonInfo);
                return;
            }
            ImsCall.this.processCallTerminated(reasonInfo);
            ImsCall.this.setCallSessionMergePending(ImsCall.FORCE_DEBUG);
        }

        public void callSessionHeld(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionHeld :: session=" + session + "profile=" + profile);
            synchronized (ImsCall.this) {
                ImsCall.this.setCallSessionMergePending(ImsCall.FORCE_DEBUG);
                ImsCall.this.mCallProfile = profile;
                ImsCall.this.mHold = ImsCall.CONF_DBG;
                if (ImsCall.this.mUpdateRequest == ImsCall.UPDATE_HOLD_MERGE) {
                    ImsCall.this.mergeInternal();
                    return;
                }
                ImsCall.this.mUpdateRequest = ImsCall.USSD_MODE_NOTIFY;
                Listener listener = ImsCall.this.mListener;
                if (listener != null) {
                    try {
                        listener.onCallHeld(ImsCall.this);
                    } catch (Throwable t) {
                        ImsCall.this.loge("callSessionHeld :: ", t);
                    }
                }
            }
        }

        public void callSessionHoldFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionHoldFailed :: session" + session + "reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionHoldFailed :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall.this.logi("callSessionHoldFailed :: session=" + session + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this.mLockObj) {
                ImsCall.this.mHold = ImsCall.FORCE_DEBUG;
            }
            synchronized (ImsCall.this) {
                if (ImsCall.this.mUpdateRequest == ImsCall.UPDATE_HOLD_MERGE) {
                }
                ImsCall.this.mUpdateRequest = ImsCall.USSD_MODE_NOTIFY;
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallHoldFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHoldFailed :: ", t);
                }
            }
        }

        public void callSessionHoldReceived(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionHoldReceived :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionHoldReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mCallProfile = profile;
            }
            if (listener != null) {
                try {
                    listener.onCallHoldReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHoldReceived :: ", t);
                }
            }
        }

        public void callSessionResumed(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionResumed :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionResumed :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall.this.setCallSessionMergePending(ImsCall.FORCE_DEBUG);
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mCallProfile = profile;
                ImsCall.this.mUpdateRequest = ImsCall.USSD_MODE_NOTIFY;
                ImsCall.this.mHold = ImsCall.FORCE_DEBUG;
            }
            if (listener != null) {
                try {
                    listener.onCallResumed(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumed :: ", t);
                }
            }
        }

        public void callSessionResumeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionResumeFailed :: session=" + session + "reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionResumeFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this.mLockObj) {
                ImsCall.this.mHold = ImsCall.CONF_DBG;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = ImsCall.USSD_MODE_NOTIFY;
            }
            if (listener != null) {
                try {
                    listener.onCallResumeFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumeFailed :: ", t);
                }
            }
        }

        public void callSessionResumeReceived(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionResumeReceived :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionResumeReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mCallProfile = profile;
            }
            if (listener != null) {
                try {
                    listener.onCallResumeReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumeReceived :: ", t);
                }
            }
        }

        public void callSessionMergeStarted(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionMergeStarted :: session=" + session + " newSession=" + newSession + ", profile=" + profile);
        }

        private boolean doesCallSessionExistsInMerge(ImsCallSession cs) {
            String callId = cs.getCallId();
            if ((ImsCall.this.isMergeHost() && Objects.equals(ImsCall.this.mMergePeer.mSession.getCallId(), callId)) || (ImsCall.this.isMergePeer() && Objects.equals(ImsCall.this.mMergeHost.mSession.getCallId(), callId))) {
                return ImsCall.CONF_DBG;
            }
            return Objects.equals(ImsCall.this.mSession.getCallId(), callId);
        }

        public void callSessionMergeComplete(ImsCallSession newSession) {
            ImsCall.this.logi("callSessionMergeComplete :: newSession =" + newSession);
            if (ImsCall.this.isMergeHost()) {
                if (newSession != null) {
                    ImsCall imsCall = ImsCall.this;
                    if (doesCallSessionExistsInMerge(newSession)) {
                        newSession = null;
                    }
                    imsCall.mTransientConferenceSession = newSession;
                }
                ImsCall.this.processMergeComplete();
                return;
            }
            ImsCall.this.mMergeHost.processMergeComplete();
        }

        public void callSessionMergeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionMergeFailed :: session=" + session + "reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                if (ImsCall.this.isMergeHost()) {
                    ImsCall.this.processMergeFailed(reasonInfo);
                } else if (ImsCall.this.mMergeHost != null) {
                    ImsCall.this.mMergeHost.processMergeFailed(reasonInfo);
                } else {
                    ImsCall.this.loge("callSessionMergeFailed :: No merge host for this conference!");
                }
            }
        }

        public void callSessionUpdated(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionUpdated :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionUpdated :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mCallProfile = profile;
            }
            if (listener != null) {
                try {
                    listener.onCallUpdated(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdated :: ", t);
                }
            }
        }

        public void callSessionUpdateFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionUpdateFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionUpdateFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = ImsCall.USSD_MODE_NOTIFY;
            }
            if (listener != null) {
                try {
                    listener.onCallUpdateFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdateFailed :: ", t);
                }
            }
        }

        public void callSessionUpdateReceived(ImsCallSession session, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionUpdateReceived :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionUpdateReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mProposedCallProfile = profile;
                ImsCall.this.mUpdateRequest = ImsCall.UPDATE_UNSPECIFIED;
            }
            if (listener != null) {
                try {
                    listener.onCallUpdateReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdateReceived :: ", t);
                }
            }
        }

        public void callSessionConferenceExtended(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionConferenceExtended :: session=" + session + " newSession=" + newSession + ", profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionConferenceExtended :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall newCall = ImsCall.this.createNewCall(newSession, profile);
            if (newCall == null) {
                callSessionConferenceExtendFailed(session, new ImsReasonInfo());
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = ImsCall.USSD_MODE_NOTIFY;
            }
            if (listener != null) {
                try {
                    listener.onCallConferenceExtended(ImsCall.this, newCall);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionConferenceExtended :: ", t);
                }
            }
        }

        public void callSessionConferenceExtendFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionConferenceExtendFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionConferenceExtendFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = ImsCall.USSD_MODE_NOTIFY;
            }
            if (listener != null) {
                try {
                    listener.onCallConferenceExtendFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionConferenceExtendFailed :: ", t);
                }
            }
        }

        public void callSessionConferenceExtendReceived(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            ImsCall.this.logi("callSessionConferenceExtendReceived :: newSession=" + newSession + ", profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionConferenceExtendReceived :: not supported for transient conference session" + session);
                return;
            }
            ImsCall newCall = ImsCall.this.createNewCall(newSession, profile);
            if (newCall != null) {
                Listener listener;
                synchronized (ImsCall.this) {
                    listener = ImsCall.this.mListener;
                }
                if (listener != null) {
                    try {
                        listener.onCallConferenceExtendReceived(ImsCall.this, newCall);
                    } catch (Throwable t) {
                        ImsCall.this.loge("callSessionConferenceExtendReceived :: ", t);
                    }
                }
            }
        }

        public void callSessionInviteParticipantsRequestDelivered(ImsCallSession session) {
            ImsCall.this.logi("callSessionInviteParticipantsRequestDelivered ::");
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionInviteParticipantsRequestDelivered :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallInviteParticipantsRequestDelivered(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionInviteParticipantsRequestDelivered :: ", t);
                }
            }
        }

        public void callSessionInviteParticipantsRequestFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionInviteParticipantsRequestFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionInviteParticipantsRequestFailed :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallInviteParticipantsRequestFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionInviteParticipantsRequestFailed :: ", t);
                }
            }
        }

        public void callSessionRemoveParticipantsRequestDelivered(ImsCallSession session) {
            ImsCall.this.logi("callSessionRemoveParticipantsRequestDelivered ::");
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionRemoveParticipantsRequestDelivered :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallRemoveParticipantsRequestDelivered(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRemoveParticipantsRequestDelivered :: ", t);
                }
            }
        }

        public void callSessionRemoveParticipantsRequestFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionRemoveParticipantsRequestFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionRemoveParticipantsRequestFailed :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallRemoveParticipantsRequestFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRemoveParticipantsRequestFailed :: ", t);
                }
            }
        }

        public void callSessionConferenceStateUpdated(ImsCallSession session, ImsConferenceState state) {
            ImsCall.this.logi("callSessionConferenceStateUpdated :: state=" + state);
            ImsCall.this.conferenceStateUpdated(state);
        }

        public void callSessionUssdMessageReceived(ImsCallSession session, int mode, String ussdMessage) {
            ImsCall.this.logi("callSessionUssdMessageReceived :: mode=" + mode + ", ussdMessage=" + ussdMessage);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionUssdMessageReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallUssdMessageReceived(ImsCall.this, mode, ussdMessage);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUssdMessageReceived :: ", t);
                }
            }
        }

        public void callSessionTtyModeReceived(ImsCallSession session, int mode) {
            ImsCall.this.logi("callSessionTtyModeReceived :: mode=" + mode);
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallSessionTtyModeReceived(ImsCall.this, mode);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionTtyModeReceived :: ", t);
                }
            }
        }

        public void callSessionMultipartyStateChanged(ImsCallSession session, boolean isMultiParty) {
            if (ImsCall.VDBG) {
                String str;
                ImsCall imsCall = ImsCall.this;
                StringBuilder append = new StringBuilder().append("callSessionMultipartyStateChanged isMultiParty: ");
                if (isMultiParty) {
                    str = "Y";
                } else {
                    str = "N";
                }
                imsCall.logi(append.append(str).toString());
            }
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onMultipartyStateChanged(ImsCall.this, isMultiParty);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionMultipartyStateChanged :: ", t);
                }
            }
        }

        public void callSessionHandover(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ImsCall.this.logi("callSessionHandover :: session=" + session + ", srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallHandover(ImsCall.this, srcAccessTech, targetAccessTech, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHandover :: ", t);
                }
            }
        }

        public void callSessionHandoverFailed(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ImsCall.this.loge("callSessionHandoverFailed :: session=" + session + ", srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallHandoverFailed(ImsCall.this, srcAccessTech, targetAccessTech, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHandoverFailed :: ", t);
                }
            }
        }

        public void callSessionSuppServiceReceived(ImsCallSession session, ImsSuppServiceNotification suppServiceInfo) {
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.logi("callSessionSuppServiceReceived :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall.this.logi("callSessionSuppServiceReceived :: session=" + session + ", suppServiceInfo" + suppServiceInfo);
            synchronized (ImsCall.this) {
                Listener listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallSuppServiceReceived(ImsCall.this, suppServiceInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionSuppServiceReceived :: ", t);
                }
            }
        }
    }

    public static class Listener {
        public void onCallProgressing(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallStarted(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallStartFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallTerminated(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallStateChanged(call);
        }

        public void onCallHeld(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallHoldFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallHoldReceived(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallResumed(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallResumeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallResumeReceived(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallMerged(ImsCall call, ImsCall peerCall, boolean swapCalls) {
            onCallStateChanged(call);
        }

        public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallUpdated(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallUpdateFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallUpdateReceived(ImsCall call) {
        }

        public void onCallConferenceExtended(ImsCall call, ImsCall newCall) {
            onCallStateChanged(call);
        }

        public void onCallConferenceExtendFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallConferenceExtendReceived(ImsCall call, ImsCall newCall) {
            onCallStateChanged(call);
        }

        public void onCallInviteParticipantsRequestDelivered(ImsCall call) {
        }

        public void onCallInviteParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallRemoveParticipantsRequestDelivered(ImsCall call) {
        }

        public void onCallRemoveParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallConferenceStateUpdated(ImsCall call, ImsConferenceState state) {
        }

        public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> list) {
        }

        public void onCallUssdMessageReceived(ImsCall call, int mode, String ussdMessage) {
        }

        public void onCallError(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallStateChanged(ImsCall call) {
        }

        public void onCallStateChanged(ImsCall call, int state) {
        }

        public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
        }

        public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
        }

        public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.ImsCall.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.ImsCall.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCall.<clinit>():void");
    }

    public ImsCall(Context context, ImsCallProfile profile) {
        this.mIsCEPPresent = FORCE_DEBUG;
        this.mLockObj = new Object();
        this.mInCall = FORCE_DEBUG;
        this.mHold = FORCE_DEBUG;
        this.mMute = FORCE_DEBUG;
        this.mUpdateRequest = USSD_MODE_NOTIFY;
        this.mListener = null;
        this.mMergePeer = null;
        this.mMergeHost = null;
        this.mMergeRequestedByConference = FORCE_DEBUG;
        this.mSession = null;
        this.mCallProfile = null;
        this.mProposedCallProfile = null;
        this.mLastReasonInfo = null;
        this.mMediaSession = null;
        this.mTransientConferenceSession = null;
        this.mSessionEndDuringMerge = FORCE_DEBUG;
        this.mSessionEndDuringMergeReasonInfo = null;
        this.mIsMerged = FORCE_DEBUG;
        this.mCallSessionMergePending = FORCE_DEBUG;
        this.mTerminationRequestPending = FORCE_DEBUG;
        this.mIsConferenceHost = FORCE_DEBUG;
        this.mContext = context;
        this.mCallProfile = profile;
    }

    public void close() {
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.close();
                this.mSession = null;
            } else {
                logi("close :: Cannot close Null call session!");
            }
            this.mCallProfile = null;
            this.mProposedCallProfile = null;
            this.mLastReasonInfo = null;
            this.mMediaSession = null;
        }
    }

    public boolean checkIfRemoteUserIsSame(String userId) {
        if (userId == null) {
            return FORCE_DEBUG;
        }
        return userId.equals(this.mCallProfile.getCallExtra("remote_uri", ""));
    }

    public boolean equalsTo(ICall call) {
        if (call != null && (call instanceof ImsCall)) {
            return equals(call);
        }
        return FORCE_DEBUG;
    }

    public static boolean isSessionAlive(ImsCallSession session) {
        return session != null ? session.isAlive() : FORCE_DEBUG;
    }

    public ImsCallProfile getCallProfile() {
        ImsCallProfile imsCallProfile;
        synchronized (this.mLockObj) {
            imsCallProfile = this.mCallProfile;
        }
        return imsCallProfile;
    }

    public ImsCallProfile getLocalCallProfile() throws ImsException {
        ImsCallProfile localCallProfile;
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            }
            try {
                localCallProfile = this.mSession.getLocalCallProfile();
            } catch (Throwable t) {
                loge("getLocalCallProfile :: ", t);
                ImsException imsException = new ImsException("getLocalCallProfile()", t, USSD_MODE_NOTIFY);
            }
        }
        return localCallProfile;
    }

    public ImsCallProfile getRemoteCallProfile() throws ImsException {
        ImsCallProfile remoteCallProfile;
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            }
            try {
                remoteCallProfile = this.mSession.getRemoteCallProfile();
            } catch (Throwable t) {
                loge("getRemoteCallProfile :: ", t);
                ImsException imsException = new ImsException("getRemoteCallProfile()", t, USSD_MODE_NOTIFY);
            }
        }
        return remoteCallProfile;
    }

    public ImsCallProfile getProposedCallProfile() {
        synchronized (this.mLockObj) {
            if (isInCall()) {
                ImsCallProfile imsCallProfile = this.mProposedCallProfile;
                return imsCallProfile;
            }
            return null;
        }
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        List<ConferenceParticipant> list;
        synchronized (this.mLockObj) {
            logi("getConferenceParticipants :: mConferenceParticipants" + this.mConferenceParticipants);
            list = this.mConferenceParticipants;
        }
        return list;
    }

    public int getState() {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                return USSD_MODE_NOTIFY;
            }
            int state = this.mSession.getState();
            return state;
        }
    }

    public ImsCallSession getCallSession() {
        ImsCallSession imsCallSession;
        synchronized (this.mLockObj) {
            imsCallSession = this.mSession;
        }
        return imsCallSession;
    }

    public ImsStreamMediaSession getMediaSession() {
        ImsStreamMediaSession imsStreamMediaSession;
        synchronized (this.mLockObj) {
            imsStreamMediaSession = this.mMediaSession;
        }
        return imsStreamMediaSession;
    }

    public String getCallExtra(String name) throws ImsException {
        String property;
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            }
            try {
                property = this.mSession.getProperty(name);
            } catch (Throwable t) {
                loge("getCallExtra :: ", t);
                ImsException imsException = new ImsException("getCallExtra()", t, USSD_MODE_NOTIFY);
            }
        }
        return property;
    }

    public ImsReasonInfo getLastReasonInfo() {
        ImsReasonInfo imsReasonInfo;
        synchronized (this.mLockObj) {
            imsReasonInfo = this.mLastReasonInfo;
        }
        return imsReasonInfo;
    }

    public boolean hasPendingUpdate() {
        boolean z = FORCE_DEBUG;
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                z = CONF_DBG;
            }
        }
        return z;
    }

    public boolean isPendingHold() {
        boolean z = CONF_DBG;
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != USSD_MODE_REQUEST) {
                z = FORCE_DEBUG;
            }
        }
        return z;
    }

    public boolean isInCall() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mInCall;
        }
        return z;
    }

    public boolean isMuted() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mMute;
        }
        return z;
    }

    public boolean isOnHold() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mHold;
        }
        return z;
    }

    public boolean isMultiparty() {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                return FORCE_DEBUG;
            }
            boolean isMultiparty = this.mSession.isMultiparty();
            return isMultiparty;
        }
    }

    public boolean isConferenceHost() {
        boolean z;
        synchronized (this.mLockObj) {
            z = isMultiparty() ? this.mIsConferenceHost : FORCE_DEBUG;
        }
        return z;
    }

    public void setIsMerged(boolean isMerged) {
        this.mIsMerged = isMerged;
    }

    public boolean isMerged() {
        return this.mIsMerged;
    }

    public void setListener(Listener listener) {
        setListener(listener, FORCE_DEBUG);
    }

    public void setListener(Listener listener, boolean callbackImmediately) {
        synchronized (this.mLockObj) {
            this.mListener = listener;
            if (listener == null || !callbackImmediately) {
                return;
            }
            boolean inCall = this.mInCall;
            boolean onHold = this.mHold;
            int state = getState();
            ImsReasonInfo lastReasonInfo = this.mLastReasonInfo;
            if (lastReasonInfo == null) {
                if (!inCall) {
                    switch (state) {
                        case UPDATE_RESUME /*3*/:
                            listener.onCallProgressing(this);
                            break;
                        case State.TERMINATED /*8*/:
                            listener.onCallTerminated(this, lastReasonInfo);
                            break;
                        default:
                            break;
                    }
                } else if (onHold) {
                    listener.onCallHeld(this);
                } else {
                    listener.onCallStarted(this);
                }
            } else {
                try {
                    listener.onCallError(this, lastReasonInfo);
                } catch (Throwable t) {
                    loge("setListener() :: ", t);
                }
            }
        }
    }

    public void setMute(boolean muted) throws ImsException {
        synchronized (this.mLockObj) {
            logi("setMute :: turning mute " + (muted ? "on" : "off"));
            this.mMute = muted;
            try {
                this.mSession.setMute(muted);
            } catch (Throwable t) {
                loge("setMute :: ", t);
                throwImsException(t, USSD_MODE_NOTIFY);
            }
        }
    }

    public void attachSession(ImsCallSession session) throws ImsException {
        logi("attachSession :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            try {
                this.mSession.setListener(createCallSessionListener());
            } catch (Throwable t) {
                loge("attachSession :: ", t);
                throwImsException(t, USSD_MODE_NOTIFY);
            }
        }
    }

    public void start(ImsCallSession session, String callee) throws ImsException {
        logi("start(1) :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            try {
                session.setListener(createCallSessionListener());
                session.start(callee, this.mCallProfile);
            } catch (Throwable t) {
                loge("start(1) :: ", t);
                ImsException imsException = new ImsException("start(1)", t, USSD_MODE_NOTIFY);
            }
        }
    }

    public void start(ImsCallSession session, String[] participants) throws ImsException {
        logi("start(n) :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            try {
                session.setListener(createCallSessionListener());
                session.start(participants, this.mCallProfile);
            } catch (Throwable t) {
                loge("start(n) :: ", t);
                ImsException imsException = new ImsException("start(n)", t, USSD_MODE_NOTIFY);
            }
        }
    }

    public void accept(int callType) throws ImsException {
        accept(callType, new ImsStreamMediaProfile());
    }

    public void accept(int callType, ImsStreamMediaProfile profile) throws ImsException {
        logi("accept :: callType=" + callType + ", profile=" + profile);
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                throw new ImsException("No call to answer", 148);
            }
            try {
                this.mSession.accept(callType, profile);
                if (this.mInCall && this.mProposedCallProfile != null) {
                    if (DBG) {
                        logi("accept :: call profile will be updated");
                    }
                    this.mCallProfile = this.mProposedCallProfile;
                    this.mProposedCallProfile = null;
                }
                if (this.mInCall && this.mUpdateRequest == UPDATE_UNSPECIFIED) {
                    this.mUpdateRequest = USSD_MODE_NOTIFY;
                }
            } catch (Throwable t) {
                loge("accept :: ", t);
                ImsException imsException = new ImsException("accept()", t, USSD_MODE_NOTIFY);
            }
        }
    }

    public void reject(int reason) throws ImsException {
        logi("reject :: reason=" + reason);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.reject(reason);
            }
            if (this.mInCall && this.mProposedCallProfile != null) {
                if (DBG) {
                    logi("reject :: call profile is not updated; destroy it...");
                }
                this.mProposedCallProfile = null;
            }
            if (this.mInCall && this.mUpdateRequest == UPDATE_UNSPECIFIED) {
                this.mUpdateRequest = USSD_MODE_NOTIFY;
            }
        }
    }

    public void terminate(int reason) throws ImsException {
        logi("terminate :: reason=" + reason);
        synchronized (this.mLockObj) {
            this.mHold = FORCE_DEBUG;
            this.mInCall = FORCE_DEBUG;
            this.mTerminationRequestPending = CONF_DBG;
            if (this.mSession != null) {
                this.mSession.terminate(reason);
            }
        }
    }

    public void hold() throws ImsException {
        logi("hold :: ");
        if (isOnHold()) {
            if (DBG) {
                logi("hold :: call is already on hold");
            }
            return;
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("hold :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
            }
            if (this.mSession == null) {
                throw new ImsException("No call session", 148);
            }
            this.mSession.hold(createHoldMediaProfile());
            this.mHold = CONF_DBG;
            this.mUpdateRequest = USSD_MODE_REQUEST;
        }
    }

    public void resume() throws ImsException {
        logi("resume :: ");
        if (isOnHold()) {
            synchronized (this.mLockObj) {
                if (this.mUpdateRequest != 0) {
                    loge("resume :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                }
                if (this.mSession == null) {
                    loge("resume :: ");
                    throw new ImsException("No call session", 148);
                }
                this.mUpdateRequest = UPDATE_RESUME;
                this.mSession.resume(createResumeMediaProfile());
            }
            return;
        }
        if (DBG) {
            logi("resume :: call is not being held");
        }
    }

    private void merge() throws ImsException {
        logi("merge :: ");
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("merge :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
            }
            if (this.mSession == null) {
                loge("merge :: no call session");
                throw new ImsException("No call session", 148);
            }
            if (this.mHold || this.mContext.getResources().getBoolean(17957003)) {
                if (!(this.mMergePeer == null || this.mMergePeer.isMultiparty())) {
                    if (!isMultiparty()) {
                        this.mUpdateRequest = UPDATE_MERGE;
                        this.mMergePeer.mUpdateRequest = UPDATE_MERGE;
                    }
                }
                this.mSession.merge();
            } else {
                this.mSession.hold(createHoldMediaProfile());
                this.mHold = CONF_DBG;
                this.mUpdateRequest = UPDATE_HOLD_MERGE;
            }
        }
    }

    public void merge(ImsCall bgCall) throws ImsException {
        logi("merge(1) :: bgImsCall=" + bgCall);
        if (bgCall == null) {
            throw new ImsException("No background call", ImsManager.INCOMING_CALL_RESULT_CODE);
        }
        synchronized (this.mLockObj) {
            setCallSessionMergePending(CONF_DBG);
            bgCall.setCallSessionMergePending(CONF_DBG);
            if ((isMultiparty() || bgCall.isMultiparty()) && !isMultiparty()) {
                setMergeHost(bgCall);
            } else {
                setMergePeer(bgCall);
            }
        }
        if (isMultiparty()) {
            this.mMergeRequestedByConference = CONF_DBG;
        } else {
            logi("merge : mMergeRequestedByConference not set");
        }
        if (shouldSaveParticipantList()) {
            updateConferenceParticipantsList(bgCall);
        }
        merge();
    }

    public void update(int callType, ImsStreamMediaProfile mediaProfile) throws ImsException {
        logi("update :: callType=" + callType + ", mediaProfile=" + mediaProfile);
        if (isOnHold()) {
            if (DBG) {
                logi("update :: call is on hold");
            }
            throw new ImsException("Not in a call to update call", 102);
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0 && DBG) {
                logi("update :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
            }
            if (this.mSession == null) {
                loge("update :: ");
                throw new ImsException("No call session", 148);
            }
            this.mSession.update(callType, mediaProfile);
            this.mUpdateRequest = UPDATE_UNSPECIFIED;
        }
    }

    public void extendToConference(String[] participants) throws ImsException {
        logi("extendToConference ::");
        if (isOnHold()) {
            if (DBG) {
                logi("extendToConference :: call is on hold");
            }
            throw new ImsException("Not in a call to extend a call to conference", 102);
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                logi("extendToConference :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
            }
            if (this.mSession == null) {
                loge("extendToConference :: ");
                throw new ImsException("No call session", 148);
            }
            this.mSession.extendToConference(participants);
            this.mUpdateRequest = UPDATE_EXTEND_TO_CONFERENCE;
        }
    }

    public void inviteParticipants(String[] participants) throws ImsException {
        logi("inviteParticipants ::");
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("inviteParticipants :: ");
                throw new ImsException("No call session", 148);
            } else {
                this.mSession.inviteParticipants(participants);
            }
        }
    }

    public void removeParticipants(String[] participants) throws ImsException {
        logi("removeParticipants :: session=" + this.mSession);
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("removeParticipants :: ");
                throw new ImsException("No call session", 148);
            }
            this.mSession.removeParticipants(participants);
            if (!(!shouldSaveParticipantList() || this.mIsCEPPresent || participants == null || this.mConferenceParticipants == null)) {
                int length = participants.length;
                for (int i = USSD_MODE_NOTIFY; i < length; i += USSD_MODE_REQUEST) {
                    String participant = participants[i];
                    if (DBG) {
                        logd("Looping for participant " + participant);
                    }
                    for (ConferenceParticipant c : this.mConferenceParticipants) {
                        if (DBG) {
                            logd("Check handle for c = " + c.getHandle());
                        }
                        if (participant != null && Uri.parse(participant).equals(c.getHandle())) {
                            logd("Remove participant " + participant);
                            this.mConferenceParticipants.remove(c);
                            break;
                        }
                    }
                }
                if (this.mListener != null) {
                    try {
                        this.mListener.onConferenceParticipantsStateChanged(this, this.mConferenceParticipants);
                    } catch (Throwable t) {
                        loge("removeparticipants :: ", t);
                    }
                }
            }
        }
    }

    public void sendDtmf(char c, Message result) {
        logi("sendDtmf :: code=" + c);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.sendDtmf(c, result);
            }
        }
    }

    public void startDtmf(char c) {
        logi("startDtmf :: code=" + c);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.startDtmf(c);
            }
        }
    }

    public void stopDtmf() {
        logi("stopDtmf :: ");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.stopDtmf();
            }
        }
    }

    public void sendUssd(String ussdMessage) throws ImsException {
        logi("sendUssd :: ussdMessage=" + ussdMessage);
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("sendUssd :: ");
                throw new ImsException("No call session", 148);
            } else {
                this.mSession.sendUssd(ussdMessage);
            }
        }
    }

    private void clear(ImsReasonInfo lastReasonInfo) {
        this.mInCall = FORCE_DEBUG;
        this.mHold = FORCE_DEBUG;
        this.mUpdateRequest = USSD_MODE_NOTIFY;
        this.mLastReasonInfo = lastReasonInfo;
    }

    private com.android.ims.internal.ImsCallSession.Listener createCallSessionListener() {
        return new ImsCallSessionListenerProxy();
    }

    private ImsCall createNewCall(ImsCallSession session, ImsCallProfile profile) {
        ImsCall call = new ImsCall(this.mContext, profile);
        try {
            call.attachSession(session);
            return call;
        } catch (ImsException e) {
            if (call == null) {
                return call;
            }
            call.close();
            return null;
        }
    }

    private ImsStreamMediaProfile createHoldMediaProfile() {
        ImsStreamMediaProfile mediaProfile = new ImsStreamMediaProfile();
        if (this.mCallProfile == null) {
            return mediaProfile;
        }
        mediaProfile.mAudioQuality = this.mCallProfile.mMediaProfile.mAudioQuality;
        mediaProfile.mVideoQuality = this.mCallProfile.mMediaProfile.mVideoQuality;
        mediaProfile.mAudioDirection = UPDATE_HOLD_MERGE;
        if (mediaProfile.mVideoQuality != 0) {
            mediaProfile.mVideoDirection = UPDATE_HOLD_MERGE;
        }
        return mediaProfile;
    }

    private ImsStreamMediaProfile createResumeMediaProfile() {
        ImsStreamMediaProfile mediaProfile = new ImsStreamMediaProfile();
        if (this.mCallProfile == null) {
            return mediaProfile;
        }
        mediaProfile.mAudioQuality = this.mCallProfile.mMediaProfile.mAudioQuality;
        mediaProfile.mVideoQuality = this.mCallProfile.mMediaProfile.mVideoQuality;
        mediaProfile.mAudioDirection = UPDATE_RESUME;
        if (mediaProfile.mVideoQuality != 0) {
            mediaProfile.mVideoDirection = UPDATE_RESUME;
        }
        return mediaProfile;
    }

    private void enforceConversationMode() {
        if (this.mInCall) {
            this.mHold = FORCE_DEBUG;
            this.mUpdateRequest = USSD_MODE_NOTIFY;
        }
    }

    private void mergeInternal() {
        logi("mergeInternal :: ");
        this.mSession.merge();
        this.mUpdateRequest = UPDATE_MERGE;
    }

    private void notifyConferenceSessionTerminated(ImsReasonInfo reasonInfo) {
        Listener listener = this.mListener;
        clear(reasonInfo);
        if (listener != null) {
            try {
                listener.onCallTerminated(this, reasonInfo);
            } catch (Throwable t) {
                loge("notifyConferenceSessionTerminated :: ", t);
            }
        }
    }

    private void notifyConferenceStateUpdated(ImsConferenceState state) {
        if (state != null && state.mParticipants != null) {
            Set<Entry<String, Bundle>> participants = state.mParticipants.entrySet();
            if (participants != null) {
                int nParticipantsQuitNumber = USSD_MODE_NOTIFY;
                List<ConferenceParticipant> conferenceParticipants = new ArrayList(participants.size());
                for (Entry<String, Bundle> entry : participants) {
                    Bundle confInfo = (Bundle) entry.getValue();
                    String status = confInfo.getString("status");
                    String user = confInfo.getString("user");
                    String displayName = confInfo.getString("display-text");
                    String endpoint = confInfo.getString("endpoint");
                    logi("notifyConferenceStateUpdated :: key=XXX, status=" + status + ", user=" + "XXX" + ", displayName= " + "XXX" + ", endpoint=" + "XXX");
                    Uri handle = Uri.parse(user);
                    if (endpoint == null) {
                        endpoint = "";
                    }
                    Uri endpointUri = Uri.parse(endpoint);
                    int connectionState = ImsConferenceState.getConnectionStateForStatus(status);
                    if (connectionState != UPDATE_UNSPECIFIED) {
                        conferenceParticipants.add(new ConferenceParticipant(handle, displayName, endpointUri, connectionState));
                    } else {
                        nParticipantsQuitNumber += USSD_MODE_REQUEST;
                    }
                }
                synchronized (this.mLockObj) {
                    this.mConferenceParticipants = conferenceParticipants;
                    if (shouldSaveParticipantList()) {
                        this.mIsCEPPresent = CONF_DBG;
                    }
                    if (this.mConferenceParticipants != null) {
                        if (!this.mConferenceParticipants.isEmpty()) {
                            if (this.mListener != null) {
                                try {
                                    this.mListener.onConferenceParticipantsStateChanged(this, this.mConferenceParticipants);
                                } catch (Throwable t) {
                                    loge("notifyConferenceStateUpdated :: ", t);
                                }
                            }
                        }
                    }
                }
                if (this.mConferenceParticipants != null) {
                    if (this.mConferenceParticipants.isEmpty() && participants.size() != 0 && nParticipantsQuitNumber == participants.size()) {
                        try {
                            logd("notifyConferenceStateUpdated: All articipants have quit conference, terminate the conference.");
                            terminate(501);
                        } catch (ImsException e) {
                            loge("notifyConferenceStateUpdated: terminate the conference fail. e = " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void processCallTerminated(ImsReasonInfo reasonInfo) {
        logi("processCallTerminated :: reason=" + reasonInfo + " userInitiated = " + this.mTerminationRequestPending);
        synchronized (this) {
            if (isCallSessionMergePending() && !isImsAsNormal) {
                logi("processCallTerminated :: burying termination during ongoing merge.");
                this.mSessionEndDuringMerge = CONF_DBG;
                this.mSessionEndDuringMergeReasonInfo = reasonInfo;
            } else if (isMultiparty()) {
                notifyConferenceSessionTerminated(reasonInfo);
            } else {
                Listener listener = this.mListener;
                clear(reasonInfo);
                if (shouldSaveParticipantList()) {
                    this.mIsCEPPresent = FORCE_DEBUG;
                }
                if (listener != null) {
                    try {
                        listener.onCallTerminated(this, reasonInfo);
                    } catch (Throwable t) {
                        loge("processCallTerminated :: ", t);
                    }
                }
            }
        }
    }

    private boolean isTransientConferenceSession(ImsCallSession session) {
        if (session == null || session == this.mSession || session != this.mTransientConferenceSession) {
            return FORCE_DEBUG;
        }
        return CONF_DBG;
    }

    private void setTransientSessionAsPrimary(ImsCallSession transientSession) {
        synchronized (this) {
            this.mSession.setListener(null);
            this.mSession = transientSession;
            this.mSession.setListener(createCallSessionListener());
        }
    }

    private void markCallAsMerged(boolean playDisconnectTone) {
        if (!isSessionAlive(this.mSession)) {
            String reasonInfo;
            logi("markCallAsMerged");
            setIsMerged(playDisconnectTone);
            this.mSessionEndDuringMerge = CONF_DBG;
            if (playDisconnectTone) {
                reasonInfo = "Call ended by network";
            } else {
                reasonInfo = "Call ended during conference merge process.";
            }
            this.mSessionEndDuringMergeReasonInfo = new ImsReasonInfo(USSD_MODE_NOTIFY, USSD_MODE_NOTIFY, reasonInfo);
        }
    }

    public boolean isMergeRequestedByConf() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mMergeRequestedByConference;
        }
        return z;
    }

    public void resetIsMergeRequestedByConf(boolean value) {
        synchronized (this.mLockObj) {
            this.mMergeRequestedByConference = value;
        }
    }

    public ImsCallSession getSession() {
        ImsCallSession imsCallSession;
        synchronized (this.mLockObj) {
            imsCallSession = this.mSession;
        }
        return imsCallSession;
    }

    private void processMergeComplete() {
        logi("processMergeComplete :: ");
        if (isMergeHost()) {
            boolean swapRequired = FORCE_DEBUG;
            synchronized (this) {
                ImsCall finalHostCall;
                ImsCall finalPeerCall;
                if (isMultiparty()) {
                    setIsMerged(FORCE_DEBUG);
                    if (!this.mMergeRequestedByConference) {
                        this.mHold = FORCE_DEBUG;
                        swapRequired = CONF_DBG;
                    }
                    this.mMergePeer.markCallAsMerged(FORCE_DEBUG);
                    finalHostCall = this;
                    finalPeerCall = this.mMergePeer;
                } else if (this.mTransientConferenceSession == null) {
                    loge("processMergeComplete :: No transient session!");
                    return;
                } else if (this.mMergePeer == null) {
                    loge("processMergeComplete :: No merge peer!");
                    return;
                } else {
                    ImsCallSession transientConferenceSession = this.mTransientConferenceSession;
                    this.mTransientConferenceSession = null;
                    transientConferenceSession.setListener(null);
                    if (isSessionAlive(this.mSession) && !isSessionAlive(this.mMergePeer.getCallSession())) {
                        this.mMergePeer.mHold = FORCE_DEBUG;
                        this.mHold = CONF_DBG;
                        if (!(this.mConferenceParticipants == null || this.mConferenceParticipants.isEmpty())) {
                            this.mMergePeer.mConferenceParticipants = this.mConferenceParticipants;
                        }
                        finalHostCall = this.mMergePeer;
                        finalPeerCall = this;
                        swapRequired = CONF_DBG;
                        setIsMerged(FORCE_DEBUG);
                        this.mMergePeer.setIsMerged(FORCE_DEBUG);
                        logi("processMergeComplete :: transient will transfer to merge peer");
                    } else if (isSessionAlive(this.mSession) || !isSessionAlive(this.mMergePeer.getCallSession())) {
                        finalHostCall = this;
                        finalPeerCall = this.mMergePeer;
                        this.mMergePeer.markCallAsMerged(FORCE_DEBUG);
                        swapRequired = FORCE_DEBUG;
                        setIsMerged(FORCE_DEBUG);
                        this.mMergePeer.setIsMerged(CONF_DBG);
                        logi("processMergeComplete :: transient will stay with us (I'm the host).");
                    } else {
                        finalHostCall = this;
                        finalPeerCall = this.mMergePeer;
                        swapRequired = FORCE_DEBUG;
                        setIsMerged(FORCE_DEBUG);
                        this.mMergePeer.setIsMerged(FORCE_DEBUG);
                        logi("processMergeComplete :: transient will stay with the merge host");
                    }
                    logi("processMergeComplete :: call=" + finalHostCall + " is the final host");
                    finalHostCall.setTransientSessionAsPrimary(transientConferenceSession);
                }
                Listener listener = finalHostCall.mListener;
                updateCallProfile(finalPeerCall);
                updateCallProfile(finalHostCall);
                clearMergeInfo();
                finalPeerCall.notifySessionTerminatedDuringMerge();
                finalHostCall.clearSessionTerminationFlags();
                finalHostCall.mIsConferenceHost = CONF_DBG;
                if (listener != null) {
                    try {
                        listener.onCallMerged(finalHostCall, finalPeerCall, swapRequired);
                    } catch (Throwable t) {
                        loge("processMergeComplete :: ", t);
                    }
                    synchronized (this.mLockObj) {
                        if (!(this.mConferenceParticipants == null || this.mConferenceParticipants.isEmpty())) {
                            try {
                                listener.onConferenceParticipantsStateChanged(finalHostCall, this.mConferenceParticipants);
                            } catch (Throwable t2) {
                                loge("processMergeComplete :: ", t2);
                            }
                        }
                    }
                }
                return;
            }
        }
        loge("processMergeComplete :: We are not the merge host!");
    }

    private static void updateCallProfile(ImsCall call) {
        if (call != null) {
            call.updateCallProfile();
        }
    }

    private void updateCallProfile() {
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mCallProfile = this.mSession.getCallProfile();
            }
        }
    }

    private void notifySessionTerminatedDuringMerge() {
        boolean notifyFailure = FORCE_DEBUG;
        ImsReasonInfo notifyFailureReasonInfo = null;
        synchronized (this) {
            Listener listener = this.mListener;
            if (this.mSessionEndDuringMerge) {
                logi("notifySessionTerminatedDuringMerge ::reporting terminate during merge");
                notifyFailure = CONF_DBG;
                notifyFailureReasonInfo = this.mSessionEndDuringMergeReasonInfo;
            }
            clearSessionTerminationFlags();
        }
        if (listener != null && notifyFailure) {
            try {
                processCallTerminated(notifyFailureReasonInfo);
            } catch (Throwable t) {
                loge("notifySessionTerminatedDuringMerge :: ", t);
            }
        }
    }

    private void updateConferenceParticipantsList(ImsCall bgCall) {
        logi("updateConferenceParticpantList :: bgImsCall=" + bgCall);
        if (bgCall != null) {
            ImsCall confCall = this;
            ImsCall childCall = bgCall;
            if (bgCall.isMultiparty()) {
                logd("updateConferenceParticipantsList: BG call is conference");
                confCall = bgCall;
                childCall = this;
            } else if (!isMultiparty()) {
                logd("updateConferenceParticipantsList: Make this call as conference and add child");
                addToConferenceParticipantList(this);
            }
            confCall.addToConferenceParticipantList(childCall);
        }
    }

    private void addToConferenceParticipantList(ImsCall childCall) {
        if (childCall != null) {
            ImsCallProfile profile = childCall.getCallProfile();
            if (profile == null) {
                loge("addToConferenceParticipantList: null profile for childcall");
                return;
            }
            String handle = profile.getCallExtra("oi", null);
            String name = profile.getCallExtra("cna", "");
            if (handle == null) {
                loge("addToConferenceParticipantList: Invalid number for childcall");
                return;
            }
            Uri userUri = Uri.parse(handle);
            ConferenceParticipant participant = new ConferenceParticipant(userUri, name, userUri, UPDATE_MERGE);
            synchronized (this.mLockObj) {
                if (this.mConferenceParticipants == null) {
                    this.mConferenceParticipants = new ArrayList();
                }
                if (DBG) {
                    logd("Adding participant: " + participant + " to list");
                }
                this.mConferenceParticipants.add(participant);
            }
        }
    }

    private boolean shouldSaveParticipantList() {
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            String localCEPMccMnc = Systemex.getString(this.mContext.getContentResolver(), "hw_local_cep_mccmnc");
            if (localCEPMccMnc != null) {
                String[] localCEPMccMncList = localCEPMccMnc.split("[,]");
                String currentOperator = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperatorNumeric(HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId());
                int length = localCEPMccMncList.length;
                for (int i = USSD_MODE_NOTIFY; i < length; i += USSD_MODE_REQUEST) {
                    if (localCEPMccMncList[i].equals(currentOperator)) {
                        return CONF_DBG;
                    }
                }
            }
        }
        return FORCE_DEBUG;
    }

    private void clearSessionTerminationFlags() {
        this.mSessionEndDuringMerge = FORCE_DEBUG;
        this.mSessionEndDuringMergeReasonInfo = null;
    }

    private void processMergeFailed(ImsReasonInfo reasonInfo) {
        logi("processMergeFailed :: reason=" + reasonInfo);
        synchronized (this) {
            if (isMergeHost()) {
                if (this.mTransientConferenceSession != null) {
                    this.mTransientConferenceSession.setListener(null);
                    this.mTransientConferenceSession = null;
                }
                Listener listener = this.mListener;
                markCallAsMerged(CONF_DBG);
                setCallSessionMergePending(FORCE_DEBUG);
                notifySessionTerminatedDuringMerge();
                if (this.mMergePeer != null) {
                    this.mMergePeer.markCallAsMerged(CONF_DBG);
                    this.mMergePeer.setCallSessionMergePending(FORCE_DEBUG);
                    this.mMergePeer.notifySessionTerminatedDuringMerge();
                } else {
                    loge("processMergeFailed :: No merge peer!");
                }
                clearMergeInfo();
                if (listener != null) {
                    try {
                        listener.onCallMergeFailed(this, reasonInfo);
                    } catch (Throwable t) {
                        loge("processMergeFailed :: ", t);
                    }
                }
                return;
            }
            loge("processMergeFailed :: We are not the merge host!");
        }
    }

    public void conferenceStateUpdated(ImsConferenceState state) {
        synchronized (this) {
            notifyConferenceStateUpdated(state);
            Listener listener = this.mListener;
        }
        if (listener != null) {
            try {
                listener.onCallConferenceStateUpdated(this, state);
            } catch (Throwable t) {
                loge("callSessionConferenceStateUpdated :: ", t);
            }
        }
    }

    private String updateRequestToString(int updateRequest) {
        switch (updateRequest) {
            case USSD_MODE_NOTIFY /*0*/:
                return "NONE";
            case USSD_MODE_REQUEST /*1*/:
                return "HOLD";
            case UPDATE_HOLD_MERGE /*2*/:
                return "HOLD_MERGE";
            case UPDATE_RESUME /*3*/:
                return "RESUME";
            case UPDATE_MERGE /*4*/:
                return "MERGE";
            case UPDATE_EXTEND_TO_CONFERENCE /*5*/:
                return "EXTEND_TO_CONFERENCE";
            case UPDATE_UNSPECIFIED /*6*/:
                return "UNSPECIFIED";
            default:
                return "UNKNOWN";
        }
    }

    private void clearMergeInfo() {
        logi("clearMergeInfo :: clearing all merge info");
        if (this.mMergeHost != null) {
            this.mMergeHost.mMergePeer = null;
            this.mMergeHost.mUpdateRequest = USSD_MODE_NOTIFY;
            this.mMergeHost.mCallSessionMergePending = FORCE_DEBUG;
        }
        if (this.mMergePeer != null) {
            this.mMergePeer.mMergeHost = null;
            this.mMergePeer.mUpdateRequest = USSD_MODE_NOTIFY;
            this.mMergePeer.mCallSessionMergePending = FORCE_DEBUG;
        }
        this.mMergeHost = null;
        this.mMergePeer = null;
        this.mUpdateRequest = USSD_MODE_NOTIFY;
        this.mCallSessionMergePending = FORCE_DEBUG;
    }

    private void setMergePeer(ImsCall mergePeer) {
        this.mMergePeer = mergePeer;
        this.mMergeHost = null;
        mergePeer.mMergeHost = this;
        mergePeer.mMergePeer = null;
    }

    public void setMergeHost(ImsCall mergeHost) {
        this.mMergeHost = mergeHost;
        this.mMergePeer = null;
        mergeHost.mMergeHost = null;
        mergeHost.mMergePeer = this;
    }

    private boolean isMerging() {
        return (this.mMergePeer == null && this.mMergeHost == null) ? FORCE_DEBUG : CONF_DBG;
    }

    private boolean isMergeHost() {
        return (this.mMergePeer == null || this.mMergeHost != null) ? FORCE_DEBUG : CONF_DBG;
    }

    private boolean isMergePeer() {
        return (this.mMergePeer != null || this.mMergeHost == null) ? FORCE_DEBUG : CONF_DBG;
    }

    private boolean isCallSessionMergePending() {
        return this.mCallSessionMergePending;
    }

    private void setCallSessionMergePending(boolean callSessionMergePending) {
        this.mCallSessionMergePending = callSessionMergePending;
    }

    private boolean shouldProcessConferenceResult() {
        boolean areMergeTriggersDone = FORCE_DEBUG;
        synchronized (this) {
            if (isMergeHost() || isMergePeer()) {
                if (isMergeHost()) {
                    logi("shouldProcessConferenceResult :: We are a merge host");
                    logi("shouldProcessConferenceResult :: Here is the merge peer=" + this.mMergePeer);
                    areMergeTriggersDone = !isCallSessionMergePending() ? this.mMergePeer.isCallSessionMergePending() ? FORCE_DEBUG : CONF_DBG : FORCE_DEBUG;
                    if (!isMultiparty()) {
                        areMergeTriggersDone &= isSessionAlive(this.mTransientConferenceSession);
                    }
                } else if (isMergePeer()) {
                    logi("shouldProcessConferenceResult :: We are a merge peer");
                    logi("shouldProcessConferenceResult :: Here is the merge host=" + this.mMergeHost);
                    areMergeTriggersDone = !isCallSessionMergePending() ? this.mMergeHost.isCallSessionMergePending() ? FORCE_DEBUG : CONF_DBG : FORCE_DEBUG;
                    areMergeTriggersDone = !this.mMergeHost.isMultiparty() ? areMergeTriggersDone & isSessionAlive(this.mMergeHost.mTransientConferenceSession) : isCallSessionMergePending() ? FORCE_DEBUG : CONF_DBG;
                } else {
                    loge("shouldProcessConferenceResult : merge in progress but call is neither host nor peer.");
                }
                logi("shouldProcessConferenceResult :: returning:" + (areMergeTriggersDone ? "true" : "false"));
                return areMergeTriggersDone;
            }
            loge("shouldProcessConferenceResult :: no merge in progress");
            return FORCE_DEBUG;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ImsCall objId:");
        sb.append(System.identityHashCode(this));
        sb.append(" onHold:");
        sb.append(isOnHold() ? "Y" : "N");
        sb.append(" mute:");
        sb.append(isMuted() ? "Y" : "N");
        sb.append(" updateRequest:");
        sb.append(updateRequestToString(this.mUpdateRequest));
        sb.append(" merging:");
        sb.append(isMerging() ? "Y" : "N");
        if (isMerging()) {
            if (isMergePeer()) {
                sb.append("P");
            } else {
                sb.append("H");
            }
        }
        sb.append(" merge action pending:");
        sb.append(isCallSessionMergePending() ? "Y" : "N");
        sb.append(" merged:");
        sb.append(isMerged() ? "Y" : "N");
        sb.append(" multiParty:");
        sb.append(isMultiparty() ? "Y" : "N");
        sb.append(" confHost:");
        sb.append(isConferenceHost() ? "Y" : "N");
        sb.append(" buried term:");
        sb.append(this.mSessionEndDuringMerge ? "Y" : "N");
        sb.append(" session:");
        sb.append(this.mSession);
        sb.append(" transientSession:");
        sb.append(this.mTransientConferenceSession);
        sb.append("]");
        return sb.toString();
    }

    private void throwImsException(Throwable t, int code) throws ImsException {
        if (t instanceof ImsException) {
            throw ((ImsException) t);
        }
        throw new ImsException(String.valueOf(code), t, code);
    }

    private String appendImsCallInfoToString(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s);
        sb.append(" ImsCall=");
        sb.append(this);
        return sb.toString();
    }

    private void logi(String s) {
        Log.i(TAG, appendImsCallInfoToString(s));
    }

    private void logd(String s) {
        Log.d(TAG, appendImsCallInfoToString(s));
    }

    private void logv(String s) {
        Log.v(TAG, appendImsCallInfoToString(s));
    }

    private void loge(String s) {
        Log.e(TAG, appendImsCallInfoToString(s));
    }

    private void loge(String s, Throwable t) {
        Log.e(TAG, appendImsCallInfoToString(s), t);
    }
}
