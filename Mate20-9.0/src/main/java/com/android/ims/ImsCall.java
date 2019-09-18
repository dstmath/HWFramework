package com.android.ims;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.SystemProperties;
import android.telecom.ConferenceParticipant;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import android.util.Log;
import com.android.ims.internal.ICall;
import com.android.ims.internal.ImsStreamMediaSession;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ImsCall implements ICall, IHwImsCallManagerInner {
    private static final String CALL_DROP_WIFI_BACKHAUL_CONGESTION = "Call is dropped due to Wi-Fi backhaul is congested";
    private static final boolean CONF_DBG = true;
    private static final boolean DBG = Log.isLoggable(TAG, UPDATE_RESUME);
    private static final boolean FORCE_DEBUG = false;
    private static final boolean SKIP_USING_CEP_LIST = SystemProperties.getBoolean("ro.config.skip_using_cep_list", false);
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
    /* access modifiers changed from: private */
    public static final boolean VDBG = Log.isLoggable(TAG, 2);
    private static final String WIFI_LTE_ERROR_MESSAGE = "Call is dropped due to Wi-Fi signal is degraded";
    private static final boolean isImsAsNormal = HuaweiTelephonyConfigs.isHisiPlatform();
    private static final AtomicInteger sUniqueIdGenerator = new AtomicInteger();
    private boolean mAnswerWithRtt = false;
    /* access modifiers changed from: private */
    public ImsCallProfile mCallProfile = null;
    private boolean mCallSessionMergePending = false;
    private List<ConferenceParticipant> mConferenceParticipants;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mHold = false;
    private IHwImsCallEx mHwImsCallEx;
    private ImsCallSessionListenerProxy mImsCallSessionListenerProxy;
    private boolean mInCall = false;
    private boolean mIsConferenceHost = false;
    private boolean mIsMerged = false;
    /* access modifiers changed from: private */
    public ImsReasonInfo mLastReasonInfo = null;
    /* access modifiers changed from: private */
    public Listener mListener = null;
    /* access modifiers changed from: private */
    public Object mLockObj = new Object();
    private ImsStreamMediaSession mMediaSession = null;
    /* access modifiers changed from: private */
    public ImsCall mMergeHost = null;
    /* access modifiers changed from: private */
    public ImsCall mMergePeer = null;
    private boolean mMergeRequestedByConference = false;
    private boolean mMute = false;
    /* access modifiers changed from: private */
    public int mOverrideReason = 0;
    /* access modifiers changed from: private */
    public ImsCallProfile mProposedCallProfile = null;
    /* access modifiers changed from: private */
    public ImsCallSession mSession = null;
    private boolean mSessionEndDuringMerge = false;
    private ImsReasonInfo mSessionEndDuringMergeReasonInfo = null;
    private boolean mTerminationRequestPending = false;
    /* access modifiers changed from: private */
    public ImsCallSession mTransientConferenceSession = null;
    /* access modifiers changed from: private */
    public int mUpdateRequest = 0;
    private boolean mWasVideoCall = false;
    public final int uniqueId;

    @VisibleForTesting
    public class ImsCallSessionListenerProxy extends ImsCallSession.Listener {
        public ImsCallSessionListenerProxy() {
        }

        public void callSessionProgressing(ImsCallSession session, ImsStreamMediaProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionProgressing :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionProgressing :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionStarted :: session=" + session + " profile=" + profile);
            if (!ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.setCallSessionMergePending(false);
                if (!ImsCall.this.isTransientConferenceSession(session)) {
                    synchronized (ImsCall.this) {
                        listener = ImsCall.this.mListener;
                        ImsCall.this.setCallProfile(profile);
                    }
                    if (listener != null) {
                        try {
                            listener.onCallStarted(ImsCall.this);
                        } catch (Throwable t) {
                            ImsCall.this.loge("callSessionStarted :: ", t);
                        }
                    }
                    return;
                }
                return;
            }
            ImsCall imsCall2 = ImsCall.this;
            imsCall2.logi("callSessionStarted :: on transient session=" + session);
        }

        public void callSessionStartFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionStartFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionStartFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsReasonInfo unused = ImsCall.this.mLastReasonInfo = reasonInfo;
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
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionTerminated :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionTerminated :: on transient session=" + session);
                ImsCall.this.processMergeFailed(reasonInfo);
                return;
            }
            if (reasonInfo.getExtraMessage() != null && reasonInfo.getExtraMessage().trim().equals(ImsCall.WIFI_LTE_ERROR_MESSAGE)) {
                reasonInfo = new ImsReasonInfo(1100, reasonInfo.getExtraCode(), reasonInfo.getExtraMessage());
            } else if (reasonInfo.getExtraMessage() != null && reasonInfo.getExtraMessage().trim().equals(ImsCall.CALL_DROP_WIFI_BACKHAUL_CONGESTION)) {
                reasonInfo = new ImsReasonInfo(3001, reasonInfo.getExtraCode(), reasonInfo.getExtraMessage());
            } else if (ImsCall.this.mOverrideReason != 0) {
                ImsCall imsCall3 = ImsCall.this;
                imsCall3.logi("callSessionTerminated :: overrideReasonInfo=" + ImsCall.this.mOverrideReason);
                reasonInfo = new ImsReasonInfo(ImsCall.this.mOverrideReason, reasonInfo.getExtraCode(), reasonInfo.getExtraMessage());
            }
            ImsCall.this.processCallTerminated(reasonInfo);
            ImsCall.this.setCallSessionMergePending(false);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x004e, code lost:
            if (r1 == null) goto L_0x005e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
            r1.onCallHeld(r4.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0056, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0057, code lost:
            com.android.ims.ImsCall.access$400(r4.this$0, "callSessionHeld :: ", r0);
         */
        public void callSessionHeld(ImsCallSession session, ImsCallProfile profile) {
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionHeld :: session=" + session + "profile=" + profile);
            synchronized (ImsCall.this) {
                ImsCall.this.setCallSessionMergePending(false);
                ImsCall.this.setCallProfile(profile);
                boolean unused = ImsCall.this.mHold = ImsCall.CONF_DBG;
                if (ImsCall.this.mUpdateRequest == 2) {
                    ImsCall.this.mergeInternal();
                } else {
                    int unused2 = ImsCall.this.mUpdateRequest = 0;
                    Listener listener = ImsCall.this.mListener;
                }
            }
        }

        public void callSessionHoldFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionHoldFailed :: session" + session + "reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionHoldFailed :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall imsCall3 = ImsCall.this;
            imsCall3.logi("callSessionHoldFailed :: session=" + session + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this.mLockObj) {
                boolean unused = ImsCall.this.mHold = false;
            }
            synchronized (ImsCall.this) {
                if (ImsCall.this.mUpdateRequest == 2) {
                }
                int unused2 = ImsCall.this.mUpdateRequest = 0;
                listener = ImsCall.this.mListener;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionHoldReceived :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionHoldReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionResumed :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionResumed :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall.this.setCallSessionMergePending(false);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
                int unused = ImsCall.this.mUpdateRequest = 0;
                boolean unused2 = ImsCall.this.mHold = false;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionResumeFailed :: session=" + session + "reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionResumeFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this.mLockObj) {
                boolean unused = ImsCall.this.mHold = ImsCall.CONF_DBG;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                int unused2 = ImsCall.this.mUpdateRequest = 0;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionResumeReceived :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionResumeReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
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
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionMergeStarted :: session=" + session + " newSession=" + newSession + ", profile=" + profile);
        }

        private boolean doesCallSessionExistsInMerge(ImsCallSession cs) {
            String callId = cs.getCallId();
            if ((!ImsCall.this.isMergeHost() || !Objects.equals(ImsCall.this.mMergePeer.mSession.getCallId(), callId)) && ((!ImsCall.this.isMergePeer() || !Objects.equals(ImsCall.this.mMergeHost.mSession.getCallId(), callId)) && !Objects.equals(ImsCall.this.mSession.getCallId(), callId))) {
                return false;
            }
            return ImsCall.CONF_DBG;
        }

        public void callSessionMergeComplete(ImsCallSession newSession) {
            ImsCallSession imsCallSession;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionMergeComplete :: newSession =" + newSession);
            if (!ImsCall.this.isMergeHost()) {
                ImsCall.this.mMergeHost.processMergeComplete();
                return;
            }
            if (newSession != null) {
                ImsCall imsCall2 = ImsCall.this;
                if (doesCallSessionExistsInMerge(newSession)) {
                    imsCallSession = null;
                } else {
                    imsCallSession = newSession;
                }
                ImsCallSession unused = imsCall2.mTransientConferenceSession = imsCallSession;
            }
            ImsCall.this.processMergeComplete();
        }

        public void callSessionMergeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionMergeFailed :: session=" + session + "reasonInfo=" + reasonInfo);
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionUpdated :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionUpdated :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionUpdateFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionUpdateFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                int unused = ImsCall.this.mUpdateRequest = 0;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionUpdateReceived :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionUpdateReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCallProfile unused = ImsCall.this.mProposedCallProfile = profile;
                int unused2 = ImsCall.this.mUpdateRequest = ImsCall.UPDATE_UNSPECIFIED;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionConferenceExtended :: session=" + session + " newSession=" + newSession + ", profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionConferenceExtended :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall newCall = ImsCall.this.createNewCall(newSession, profile);
            if (newCall == null) {
                callSessionConferenceExtendFailed(session, new ImsReasonInfo());
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                int unused = ImsCall.this.mUpdateRequest = 0;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionConferenceExtendFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionConferenceExtendFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                int unused = ImsCall.this.mUpdateRequest = 0;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionConferenceExtendReceived :: newSession=" + newSession + ", profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionConferenceExtendReceived :: not supported for transient conference session" + session);
                return;
            }
            ImsCall newCall = ImsCall.this.createNewCall(newSession, profile);
            if (newCall != null) {
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
            Listener listener;
            ImsCall.this.logi("callSessionInviteParticipantsRequestDelivered ::");
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall = ImsCall.this;
                imsCall.logi("callSessionInviteParticipantsRequestDelivered :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionInviteParticipantsRequestFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionInviteParticipantsRequestFailed :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            ImsCall.this.logi("callSessionRemoveParticipantsRequestDelivered ::");
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall = ImsCall.this;
                imsCall.logi("callSessionRemoveParticipantsRequestDelivered :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionRemoveParticipantsRequestFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionRemoveParticipantsRequestFailed :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionConferenceStateUpdated :: state=" + state);
            ImsCall.this.conferenceStateUpdated(state);
        }

        public void callSessionUssdMessageReceived(ImsCallSession session, int mode, String ussdMessage) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionUssdMessageReceived :: mode=" + mode + ", ussdMessage=" + ussdMessage);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionUssdMessageReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionTtyModeReceived :: mode=" + mode);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            String str;
            if (ImsCall.VDBG) {
                ImsCall imsCall = ImsCall.this;
                StringBuilder sb = new StringBuilder();
                sb.append("callSessionMultipartyStateChanged isMultiParty: ");
                if (isMultiParty) {
                    str = "Y";
                } else {
                    str = "N";
                }
                sb.append(str);
                imsCall.logi(sb.toString());
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionHandover :: session=" + session + ", srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionHandoverFailed :: session=" + session + ", srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
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
            Listener listener;
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall = ImsCall.this;
                imsCall.logi("callSessionSuppServiceReceived :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall imsCall2 = ImsCall.this;
            imsCall2.logi("callSessionSuppServiceReceived :: session=" + session + ", suppServiceInfo" + suppServiceInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallSuppServiceReceived(ImsCall.this, suppServiceInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionSuppServiceReceived :: ", t);
                }
            }
        }

        public void callSessionRttModifyRequestReceived(ImsCallSession session, ImsCallProfile callProfile) {
            Listener listener;
            ImsCall.this.logi("callSessionRttModifyRequestReceived");
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (!callProfile.mMediaProfile.isRttCall()) {
                ImsCall.this.logi("callSessionRttModifyRequestReceived:: ignoring request, requested profile is not RTT.");
                return;
            }
            if (listener != null) {
                try {
                    listener.onRttModifyRequestReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRttModifyRequestReceived:: ", t);
                }
            }
        }

        public void callSessionRttModifyResponseReceived(int status) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionRttModifyResponseReceived: " + status);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onRttModifyResponseReceived(ImsCall.this, status);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRttModifyResponseReceived:: ", t);
                }
            }
        }

        public void callSessionRttMessageReceived(String rttMessage) {
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onRttMessageReceived(ImsCall.this, rttMessage);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRttModifyResponseReceived:: ", t);
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

        public void onRttModifyRequestReceived(ImsCall imsCall) {
        }

        public void onRttModifyResponseReceived(ImsCall imsCall, int status) {
        }

        public void onRttMessageReceived(ImsCall imsCall, String message) {
        }

        public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
        }
    }

    public ImsCall(Context context, ImsCallProfile profile) {
        this.mContext = context;
        setCallProfile(profile);
        this.uniqueId = sUniqueIdGenerator.getAndIncrement();
        this.mHwImsCallEx = HwImsFactory.getHwImsCallEx(this, context);
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
            return false;
        }
        return userId.equals(this.mCallProfile.getCallExtra("remote_uri", ""));
    }

    public boolean equalsTo(ICall call) {
        if (call != null && (call instanceof ImsCall)) {
            return equals(call);
        }
        return false;
    }

    public static boolean isSessionAlive(ImsCallSession session) {
        if (session == null || !session.isAlive()) {
            return false;
        }
        return CONF_DBG;
    }

    public ImsCallProfile getCallProfile() {
        ImsCallProfile imsCallProfile;
        synchronized (this.mLockObj) {
            imsCallProfile = this.mCallProfile;
        }
        return imsCallProfile;
    }

    @VisibleForTesting
    public void setCallProfile(ImsCallProfile profile) {
        synchronized (this.mLockObj) {
            this.mCallProfile = profile;
            trackVideoStateHistory(this.mCallProfile);
        }
    }

    public ImsCallProfile getLocalCallProfile() throws ImsException {
        ImsCallProfile localCallProfile;
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    localCallProfile = this.mSession.getLocalCallProfile();
                } catch (Throwable t) {
                    loge("getLocalCallProfile :: ", t);
                    throw new ImsException("getLocalCallProfile()", t, 0);
                }
            } else {
                throw new ImsException("No call session", 148);
            }
        }
        return localCallProfile;
    }

    public ImsCallProfile getRemoteCallProfile() throws ImsException {
        ImsCallProfile remoteCallProfile;
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    remoteCallProfile = this.mSession.getRemoteCallProfile();
                } catch (Throwable t) {
                    loge("getRemoteCallProfile :: ", t);
                    throw new ImsException("getRemoteCallProfile()", t, 0);
                }
            } else {
                throw new ImsException("No call session", 148);
            }
        }
        return remoteCallProfile;
    }

    public ImsCallProfile getProposedCallProfile() {
        synchronized (this.mLockObj) {
            if (!isInCall()) {
                return null;
            }
            ImsCallProfile imsCallProfile = this.mProposedCallProfile;
            return imsCallProfile;
        }
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        synchronized (this.mLockObj) {
            logi("getConferenceParticipants :: mConferenceParticipants" + this.mConferenceParticipants);
            if (this.mConferenceParticipants == null) {
                return null;
            }
            if (this.mConferenceParticipants.isEmpty()) {
                ArrayList arrayList = new ArrayList(0);
                return arrayList;
            }
            ArrayList arrayList2 = new ArrayList(this.mConferenceParticipants);
            return arrayList2;
        }
    }

    public int getState() {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                return 0;
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
            if (this.mSession != null) {
                try {
                    property = this.mSession.getProperty(name);
                } catch (Throwable t) {
                    loge("getCallExtra :: ", t);
                    throw new ImsException("getCallExtra()", t, 0);
                }
            } else {
                throw new ImsException("No call session", 148);
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
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mUpdateRequest != 0 ? CONF_DBG : false;
        }
        return z;
    }

    public boolean isPendingHold() {
        boolean z;
        synchronized (this.mLockObj) {
            int i = this.mUpdateRequest;
            z = CONF_DBG;
            if (i != 1) {
                z = false;
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
                return false;
            }
            boolean isMultiparty = this.mSession.isMultiparty();
            return isMultiparty;
        }
    }

    public boolean isConferenceHost() {
        boolean z;
        synchronized (this.mLockObj) {
            z = (!isMultiparty() || !this.mIsConferenceHost) ? false : CONF_DBG;
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
        setListener(listener, false);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r7.onCallError(r6, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001d, code lost:
        if (r1 == false) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        if (r2 == false) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0021, code lost:
        r7.onCallHeld(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0025, code lost:
        r7.onCallStarted(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002a, code lost:
        if (r3 == UPDATE_RESUME) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002e, code lost:
        if (r3 == 8) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0031, code lost:
        r7.onCallTerminated(r6, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0035, code lost:
        r7.onCallProgressing(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003a, code lost:
        loge("setListener() :: ", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        if (r4 == null) goto L_0x001d;
     */
    public void setListener(Listener listener, boolean callbackImmediately) {
        synchronized (this.mLockObj) {
            this.mListener = listener;
            if (listener != null) {
                if (callbackImmediately) {
                    boolean inCall = this.mInCall;
                    boolean onHold = this.mHold;
                    int state = getState();
                    ImsReasonInfo lastReasonInfo = this.mLastReasonInfo;
                }
            }
        }
    }

    public void setMute(boolean muted) throws ImsException {
        synchronized (this.mLockObj) {
            StringBuilder sb = new StringBuilder();
            sb.append("setMute :: turning mute ");
            sb.append(muted ? "on" : "off");
            logi(sb.toString());
            this.mMute = muted;
            try {
                this.mSession.setMute(muted);
            } catch (Throwable t) {
                loge("setMute :: ", t);
                throwImsException(t, 0);
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
                throwImsException(t, 0);
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
                throw new ImsException("start(1)", t, 0);
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
                throw new ImsException("start(n)", t, 0);
            }
        }
    }

    public void accept(int callType) throws ImsException {
        accept(callType, new ImsStreamMediaProfile());
    }

    public void accept(int callType, ImsStreamMediaProfile profile) throws ImsException {
        logi("accept :: callType=" + callType + ", profile=" + profile);
        if (this.mAnswerWithRtt) {
            profile.mRttMode = 1;
            logi("accept :: changing media profile RTT mode to full");
        }
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    this.mSession.accept(callType, profile);
                    if (this.mInCall && this.mProposedCallProfile != null) {
                        if (DBG) {
                            logi("accept :: call profile will be updated");
                        }
                        this.mCallProfile = this.mProposedCallProfile;
                        trackVideoStateHistory(this.mCallProfile);
                        this.mProposedCallProfile = null;
                    }
                    if (this.mInCall && this.mUpdateRequest == UPDATE_UNSPECIFIED) {
                        this.mUpdateRequest = 0;
                    }
                } catch (Throwable t) {
                    loge("accept :: ", t);
                    throw new ImsException("accept()", t, 0);
                }
            } else {
                throw new ImsException("No call to answer", 148);
            }
        }
    }

    public void deflect(String number) throws ImsException {
        logi("deflect :: session=" + this.mSession + ", number=" + Rlog.pii(TAG, number));
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    this.mSession.deflect(number);
                } catch (Throwable t) {
                    loge("deflect :: ", t);
                    throw new ImsException("deflect()", t, 0);
                }
            } else {
                throw new ImsException("No call to deflect", 148);
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
                this.mUpdateRequest = 0;
            }
        }
    }

    public void terminate(int reason, int overrideReason) throws ImsException {
        logi("terminate :: reason=" + reason + " ; overrideReadon=" + overrideReason);
        this.mOverrideReason = overrideReason;
        terminate(reason);
    }

    public void terminate(int reason) throws ImsException {
        logi("terminate :: reason=" + reason);
        synchronized (this.mLockObj) {
            this.mHold = false;
            this.mInCall = false;
            this.mTerminationRequestPending = CONF_DBG;
            if (this.mSession != null) {
                this.mSession.terminate(reason);
            }
        }
    }

    public void hold(int callType) throws ImsException {
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
            if (this.mSession != null) {
                this.mSession.hold(callType, createHoldMediaProfile());
                this.mHold = CONF_DBG;
                this.mUpdateRequest = 1;
            } else {
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void resume(int callType) throws ImsException {
        logi("resume :: ");
        if (!isOnHold()) {
            if (DBG) {
                logi("resume :: call is not being held");
            }
            return;
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                loge("resume :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
            }
            if (this.mSession != null) {
                this.mUpdateRequest = UPDATE_RESUME;
                this.mSession.resume(callType, createResumeMediaProfile());
                setMute(this.mMute);
            } else {
                loge("resume :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    private void merge(int callType) throws ImsException {
        logi("merge :: ");
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                setCallSessionMergePending(false);
                if (this.mMergePeer != null) {
                    this.mMergePeer.setCallSessionMergePending(false);
                }
                loge("merge :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
            }
            if (this.mMergePeer != null) {
                if (this.mMergePeer.mUpdateRequest != 0) {
                    setCallSessionMergePending(false);
                    this.mMergePeer.setCallSessionMergePending(false);
                    loge("merge :: peer call update is in progress; request=" + updateRequestToString(this.mMergePeer.mUpdateRequest));
                    throw new ImsException("Peer call update is in progress", 102);
                }
            }
            if (this.mSession != null) {
                if (!this.mHold) {
                    if (!this.mContext.getResources().getBoolean(17957109)) {
                        this.mSession.hold(callType, createHoldMediaProfile());
                        this.mHold = CONF_DBG;
                        this.mUpdateRequest = 2;
                    }
                }
                if (this.mMergePeer != null && !this.mMergePeer.isMultiparty() && !isMultiparty()) {
                    this.mUpdateRequest = UPDATE_MERGE;
                    this.mMergePeer.mUpdateRequest = UPDATE_MERGE;
                }
                this.mSession.merge();
            } else {
                loge("merge :: no call session");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void merge(int callType, ImsCall bgCall) throws ImsException {
        logi("merge(1) :: bgImsCall=" + bgCall);
        if (bgCall != null) {
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
            if (this.mHwImsCallEx != null && this.mHwImsCallEx.shouldSaveQcomParticipantList()) {
                this.mHwImsCallEx.updateQcomConferenceParticipantsList(bgCall);
            }
            merge(callType);
            return;
        }
        throw new ImsException("No background call", ImsManager.INCOMING_CALL_RESULT_CODE);
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
            if (this.mSession != null) {
                this.mSession.update(callType, mediaProfile);
                this.mUpdateRequest = UPDATE_UNSPECIFIED;
            } else {
                loge("update :: ");
                throw new ImsException("No call session", 148);
            }
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
            if (this.mSession != null) {
                this.mSession.extendToConference(participants);
                this.mUpdateRequest = UPDATE_EXTEND_TO_CONFERENCE;
            } else {
                loge("extendToConference :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void inviteParticipants(String[] participants) throws ImsException {
        logi("inviteParticipants ::");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.inviteParticipants(participants);
            } else {
                loge("inviteParticipants :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void removeParticipants(String[] participants) throws ImsException {
        logi("removeParticipants :: session=" + this.mSession);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.removeParticipants(participants);
            } else {
                loge("removeParticipants :: ");
                throw new ImsException("No call session", 148);
            }
        }
        if (this.mHwImsCallEx != null && this.mHwImsCallEx.shouldSaveQcomParticipantList()) {
            this.mHwImsCallEx.removeQcomParticipantsFromList(participants);
        }
    }

    public void sendDtmf(char c, Message result) {
        logi("sendDtmf");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.sendDtmf(c, result);
            }
        }
    }

    public void startDtmf(char c) {
        logi("startDtmf");
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
            if (this.mSession != null) {
                this.mSession.sendUssd(ussdMessage);
            } else {
                loge("sendUssd :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void sendRttMessage(String rttMessage) {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("sendRttMessage::no session");
            }
            if (!this.mCallProfile.mMediaProfile.isRttCall()) {
                logi("sendRttMessage::Not an rtt call, ignoring");
            } else {
                this.mSession.sendRttMessage(rttMessage);
            }
        }
    }

    public void sendRttModifyRequest() {
        logi("sendRttModifyRequest");
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("sendRttModifyRequest::no session");
            }
            if (this.mCallProfile.mMediaProfile.isRttCall()) {
                logi("sendRttModifyRequest::Already RTT call, ignoring.");
                return;
            }
            Parcel p = Parcel.obtain();
            this.mCallProfile.writeToParcel(p, 0);
            p.setDataPosition(0);
            ImsCallProfile requestedProfile = new ImsCallProfile(p);
            requestedProfile.mMediaProfile.setRttMode(1);
            this.mSession.sendRttModifyRequest(requestedProfile);
        }
    }

    public void sendRttModifyResponse(boolean status) {
        logi("sendRttModifyResponse");
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("sendRttModifyResponse::no session");
            }
            if (this.mCallProfile.mMediaProfile.isRttCall()) {
                logi("sendRttModifyResponse::Already RTT call, ignoring.");
            } else {
                this.mSession.sendRttModifyResponse(status);
            }
        }
    }

    public void setAnswerWithRtt() {
        this.mAnswerWithRtt = CONF_DBG;
    }

    private void clear(ImsReasonInfo lastReasonInfo) {
        this.mInCall = false;
        this.mHold = false;
        this.mUpdateRequest = 0;
        this.mLastReasonInfo = lastReasonInfo;
    }

    private ImsCallSession.Listener createCallSessionListener() {
        this.mImsCallSessionListenerProxy = new ImsCallSessionListenerProxy();
        return this.mImsCallSessionListenerProxy;
    }

    @VisibleForTesting
    public ImsCallSessionListenerProxy getImsCallSessionListenerProxy() {
        return this.mImsCallSessionListenerProxy;
    }

    /* access modifiers changed from: private */
    public ImsCall createNewCall(ImsCallSession session, ImsCallProfile profile) {
        ImsCall call = new ImsCall(this.mContext, profile);
        try {
            call.attachSession(session);
            return call;
        } catch (ImsException e) {
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
        mediaProfile.mAudioDirection = 2;
        if (mediaProfile.mVideoQuality != 0) {
            mediaProfile.mVideoDirection = 2;
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
            this.mHold = false;
            this.mUpdateRequest = 0;
        }
    }

    /* access modifiers changed from: private */
    public void mergeInternal() {
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
        ImsConferenceState imsConferenceState = state;
        if (imsConferenceState != null && imsConferenceState.mParticipants != null) {
            boolean shouldSaveQcomParticipantList = (this.mHwImsCallEx == null || !this.mHwImsCallEx.shouldSaveQcomParticipantList()) ? false : CONF_DBG;
            Set<Map.Entry<String, Bundle>> participants = imsConferenceState.mParticipants.entrySet();
            if (participants != null) {
                if (!SKIP_USING_CEP_LIST || !shouldSaveQcomParticipantList) {
                    this.mConferenceParticipants = new ArrayList(participants.size());
                    int nParticipantsQuitNumber = 0;
                    for (Map.Entry<String, Bundle> entry : participants) {
                        Bundle confInfo = entry.getValue();
                        String status = confInfo.getString("status");
                        String user = confInfo.getString("user");
                        String displayName = confInfo.getString("display-text");
                        String endpoint = confInfo.getString("endpoint");
                        logi("notifyConferenceStateUpdated :: key=XXX, status=" + status + ", user=XXX, displayName= XXX, endpoint=XXX");
                        Uri handle = Uri.parse(user);
                        if (endpoint == null) {
                            endpoint = "";
                        }
                        Uri endpointUri = Uri.parse(endpoint);
                        int connectionState = ImsConferenceState.getConnectionStateForStatus(status);
                        if (connectionState == UPDATE_UNSPECIFIED || connectionState == UPDATE_RESUME) {
                            nParticipantsQuitNumber++;
                        } else {
                            ConferenceParticipant conferenceParticipant = new ConferenceParticipant(handle, displayName, endpointUri, connectionState);
                            if (shouldSaveQcomParticipantList) {
                                Map.Entry<String, Bundle> entry2 = entry;
                                this.mHwImsCallEx.addQcomConferenceParticipant(conferenceParticipant);
                            } else {
                                this.mConferenceParticipants.add(conferenceParticipant);
                            }
                        }
                    }
                    if (shouldSaveQcomParticipantList) {
                        this.mHwImsCallEx.replaceQcomConferenceParticipantsList();
                    }
                    if (!(this.mConferenceParticipants == null || this.mConferenceParticipants.isEmpty() || this.mListener == null)) {
                        try {
                            this.mListener.onConferenceParticipantsStateChanged(this, this.mConferenceParticipants);
                        } catch (Throwable t) {
                            loge("notifyConferenceStateUpdated :: ", t);
                        }
                    }
                    if (this.mConferenceParticipants != null && this.mConferenceParticipants.isEmpty() && participants.size() != 0 && nParticipantsQuitNumber == participants.size()) {
                        try {
                            logd("notifyConferenceStateUpdated: All articipants have quit conference, terminate the conference.");
                            terminate(501);
                        } catch (ImsException e) {
                            loge("notifyConferenceStateUpdated: terminate the conference fail. e = " + e.getMessage());
                        }
                    }
                    return;
                }
                logi("notifyConferenceStateUpdated skipping replacing CEP participants");
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005a, code lost:
        if (r0 == null) goto L_0x0066;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r0.onCallTerminated(r3, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0060, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        loge("processCallTerminated :: ", r1);
     */
    public void processCallTerminated(ImsReasonInfo reasonInfo) {
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
                if (this.mHwImsCallEx != null && this.mHwImsCallEx.shouldSaveQcomParticipantList()) {
                    this.mHwImsCallEx.setIsQcomCEPPresent(false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isTransientConferenceSession(ImsCallSession session) {
        if (session == null || session == this.mSession || session != this.mTransientConferenceSession) {
            return false;
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
        String reasonInfo;
        int reasonCode;
        if (!isSessionAlive(this.mSession)) {
            logi("markCallAsMerged");
            setIsMerged(playDisconnectTone);
            this.mSessionEndDuringMerge = CONF_DBG;
            if (playDisconnectTone) {
                reasonCode = 510;
                reasonInfo = "Call ended by network";
            } else {
                reasonCode = 108;
                reasonInfo = "Call ended during conference merge process.";
            }
            this.mSessionEndDuringMergeReasonInfo = new ImsReasonInfo(reasonCode, 0, reasonInfo);
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

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f9, code lost:
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00fa, code lost:
        if (r4 == null) goto L_0x011e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r4.onCallMerged(r1, r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0100, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0101, code lost:
        loge("processMergeComplete :: ", r3);
     */
    public void processMergeComplete() {
        ImsCall finalPeerCall;
        ImsCall finalHostCall;
        boolean swapRequired;
        Listener listener;
        ImsCall finalPeerCall2;
        ImsCall finalHostCall2;
        logi("processMergeComplete :: ");
        if (!isMergeHost()) {
            loge("processMergeComplete :: We are not the merge host!");
            return;
        }
        boolean swapRequired2 = false;
        synchronized (this) {
            if (isMultiparty()) {
                setIsMerged(false);
                if (!this.mMergeRequestedByConference) {
                    this.mHold = false;
                    swapRequired2 = CONF_DBG;
                }
                this.mMergePeer.markCallAsMerged(false);
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
                    this.mMergePeer.mHold = false;
                    this.mHold = CONF_DBG;
                    if (this.mConferenceParticipants != null && !this.mConferenceParticipants.isEmpty()) {
                        this.mMergePeer.mConferenceParticipants = this.mConferenceParticipants;
                    }
                    finalHostCall2 = this.mMergePeer;
                    finalPeerCall2 = this;
                    swapRequired = CONF_DBG;
                    setIsMerged(false);
                    this.mMergePeer.setIsMerged(false);
                    logi("processMergeComplete :: transient will transfer to merge peer");
                } else if (isSessionAlive(this.mSession) || !isSessionAlive(this.mMergePeer.getCallSession())) {
                    finalHostCall2 = this;
                    finalPeerCall2 = this.mMergePeer;
                    this.mMergePeer.markCallAsMerged(false);
                    swapRequired = false;
                    setIsMerged(false);
                    this.mMergePeer.setIsMerged(CONF_DBG);
                    logi("processMergeComplete :: transient will stay with us (I'm the host).");
                } else {
                    finalHostCall2 = this;
                    finalPeerCall2 = this.mMergePeer;
                    swapRequired = false;
                    setIsMerged(false);
                    this.mMergePeer.setIsMerged(false);
                    logi("processMergeComplete :: transient will stay with the merge host");
                }
                finalPeerCall = finalPeerCall2;
                logi("processMergeComplete :: call=" + finalHostCall2 + " is the final host");
                finalHostCall2.setTransientSessionAsPrimary(transientConferenceSession);
                finalHostCall = finalHostCall2;
            }
            listener = finalHostCall.mListener;
            updateCallProfile(finalPeerCall);
            updateCallProfile(finalHostCall);
            clearMergeInfo();
            finalPeerCall.notifySessionTerminatedDuringMerge();
            finalHostCall.clearSessionTerminationFlags();
            finalHostCall.mIsConferenceHost = CONF_DBG;
        }
        if (this.mConferenceParticipants != null && !this.mConferenceParticipants.isEmpty()) {
            try {
                listener.onConferenceParticipantsStateChanged(finalHostCall, this.mConferenceParticipants);
            } catch (Throwable t) {
                loge("processMergeComplete :: ", t);
            }
        }
    }

    private static void updateCallProfile(ImsCall call) {
        if (call != null) {
            call.updateCallProfile();
        }
    }

    private void updateCallProfile() {
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                setCallProfile(this.mSession.getCallProfile());
            }
        }
    }

    private void notifySessionTerminatedDuringMerge() {
        Listener listener;
        boolean notifyFailure = false;
        ImsReasonInfo notifyFailureReasonInfo = null;
        synchronized (this) {
            listener = this.mListener;
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

    private void clearSessionTerminationFlags() {
        this.mSessionEndDuringMerge = false;
        this.mSessionEndDuringMergeReasonInfo = null;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
        if (r0 == null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r0.onCallMergeFailed(r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005f, code lost:
        loge("processMergeFailed :: ", r1);
     */
    public void processMergeFailed(ImsReasonInfo reasonInfo) {
        logi("processMergeFailed :: reason=" + reasonInfo);
        synchronized (this) {
            if (!isMergeHost()) {
                loge("processMergeFailed :: We are not the merge host!");
                return;
            }
            if (this.mTransientConferenceSession != null) {
                this.mTransientConferenceSession.setListener(null);
                this.mTransientConferenceSession = null;
            }
            Listener listener = this.mListener;
            markCallAsMerged(CONF_DBG);
            setCallSessionMergePending(false);
            notifySessionTerminatedDuringMerge();
            if (this.mMergePeer != null) {
                this.mMergePeer.markCallAsMerged(CONF_DBG);
                this.mMergePeer.setCallSessionMergePending(false);
                this.mMergePeer.notifySessionTerminatedDuringMerge();
            } else {
                loge("processMergeFailed :: No merge peer!");
            }
            clearMergeInfo();
        }
    }

    @VisibleForTesting
    public void conferenceStateUpdated(ImsConferenceState state) {
        Listener listener;
        synchronized (this) {
            notifyConferenceStateUpdated(state);
            listener = this.mListener;
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
            case 0:
                return "NONE";
            case 1:
                return "HOLD";
            case 2:
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
            this.mMergeHost.mUpdateRequest = 0;
            this.mMergeHost.mCallSessionMergePending = false;
        }
        if (this.mMergePeer != null) {
            this.mMergePeer.mMergeHost = null;
            this.mMergePeer.mUpdateRequest = 0;
            this.mMergePeer.mCallSessionMergePending = false;
        }
        this.mMergeHost = null;
        this.mMergePeer = null;
        this.mUpdateRequest = 0;
        this.mCallSessionMergePending = false;
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
        if (this.mMergePeer == null && this.mMergeHost == null) {
            return false;
        }
        return CONF_DBG;
    }

    /* access modifiers changed from: private */
    public boolean isMergeHost() {
        if (this.mMergePeer == null || this.mMergeHost != null) {
            return false;
        }
        return CONF_DBG;
    }

    /* access modifiers changed from: private */
    public boolean isMergePeer() {
        if (this.mMergePeer != null || this.mMergeHost == null) {
            return false;
        }
        return CONF_DBG;
    }

    public boolean isCallSessionMergePending() {
        return this.mCallSessionMergePending;
    }

    /* access modifiers changed from: private */
    public void setCallSessionMergePending(boolean callSessionMergePending) {
        this.mCallSessionMergePending = callSessionMergePending;
    }

    private boolean shouldProcessConferenceResult() {
        boolean areMergeTriggersDone = false;
        synchronized (this) {
            boolean areMergeTriggersDone2 = false;
            if (isMergeHost() || isMergePeer()) {
                if (isMergeHost()) {
                    logi("shouldProcessConferenceResult :: We are a merge host");
                    logi("shouldProcessConferenceResult :: Here is the merge peer=" + this.mMergePeer);
                    if (!isCallSessionMergePending() && !this.mMergePeer.isCallSessionMergePending()) {
                        areMergeTriggersDone2 = true;
                    }
                    areMergeTriggersDone = areMergeTriggersDone2;
                    if (!isMultiparty()) {
                        areMergeTriggersDone &= isSessionAlive(this.mTransientConferenceSession);
                    }
                } else if (isMergePeer()) {
                    logi("shouldProcessConferenceResult :: We are a merge peer");
                    logi("shouldProcessConferenceResult :: Here is the merge host=" + this.mMergeHost);
                    if (!isCallSessionMergePending() && !this.mMergeHost.isCallSessionMergePending()) {
                        areMergeTriggersDone2 = true;
                    }
                    areMergeTriggersDone = !this.mMergeHost.isMultiparty() ? areMergeTriggersDone2 & isSessionAlive(this.mMergeHost.mTransientConferenceSession) : isCallSessionMergePending() ^ CONF_DBG;
                } else {
                    loge("shouldProcessConferenceResult : merge in progress but call is neither host nor peer.");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("shouldProcessConferenceResult :: returning:");
                sb.append(areMergeTriggersDone ? ImsManager.TRUE : ImsManager.FALSE);
                logi(sb.toString());
                return areMergeTriggersDone;
            }
            loge("shouldProcessConferenceResult :: no merge in progress");
            return false;
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
        sb.append(" isVideo: ");
        sb.append(isVideoCall() ? "Y" : "N");
        sb.append(" wasVideo: ");
        sb.append(this.mWasVideoCall ? "Y" : "N");
        sb.append(" isWifi: ");
        sb.append(isWifiCall() ? "Y" : "N");
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
        return s + " ImsCall=" + this;
    }

    private void trackVideoStateHistory(ImsCallProfile profile) {
        this.mWasVideoCall = (this.mWasVideoCall || profile.isVideoCall()) ? CONF_DBG : false;
    }

    public boolean wasVideoCall() {
        return this.mWasVideoCall;
    }

    public boolean isVideoCall() {
        boolean z;
        synchronized (this.mLockObj) {
            z = (this.mCallProfile == null || !this.mCallProfile.isVideoCall()) ? false : CONF_DBG;
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        return r2;
     */
    public boolean isWifiCall() {
        synchronized (this.mLockObj) {
            boolean z = false;
            if (this.mCallProfile == null) {
                return false;
            }
            if (getRadioTechnology() == 18) {
                z = CONF_DBG;
            }
        }
    }

    public int getRadioTechnology() {
        int radioTechnology;
        synchronized (this.mLockObj) {
            if (this.mCallProfile == null) {
                return 0;
            }
            String callType = this.mCallProfile.getCallExtra("CallRadioTech");
            if (callType == null || callType.isEmpty()) {
                callType = this.mCallProfile.getCallExtra("callRadioTech");
            }
            try {
                radioTechnology = Integer.parseInt(callType);
            } catch (NumberFormatException e) {
                radioTechnology = 0;
            }
            return radioTechnology;
        }
    }

    /* access modifiers changed from: private */
    public void logi(String s) {
        Log.i(TAG, appendImsCallInfoToString(s));
    }

    private void logd(String s) {
        Log.d(TAG, appendImsCallInfoToString(s));
    }

    private void logv(String s) {
        Log.v(TAG, appendImsCallInfoToString(s));
    }

    /* access modifiers changed from: private */
    public void loge(String s) {
        Log.e(TAG, appendImsCallInfoToString(s));
    }

    /* access modifiers changed from: private */
    public void loge(String s, Throwable t) {
        Log.e(TAG, appendImsCallInfoToString(s), t);
    }

    public List<ConferenceParticipant> getConferenceParticipantsForExt() {
        if (this.mConferenceParticipants == null) {
            this.mConferenceParticipants = new ArrayList();
        }
        return this.mConferenceParticipants;
    }

    public Listener getListener() {
        return this.mListener;
    }

    public ImsCall getImsCall() {
        return this;
    }

    public void setConferenceParticipants(List<ConferenceParticipant> conferenceParticipants) {
        this.mConferenceParticipants = conferenceParticipants;
    }

    public IHwImsCallEx getHwImsCallEx() {
        return this.mHwImsCallEx;
    }

    public void setImsCallState(boolean hold, boolean inCall, boolean terminationRequestPending) {
        synchronized (this.mLockObj) {
            this.mHold = hold;
            this.mInCall = inCall;
            this.mTerminationRequestPending = terminationRequestPending;
        }
    }
}
