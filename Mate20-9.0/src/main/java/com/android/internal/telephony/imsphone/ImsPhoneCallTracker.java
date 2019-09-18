package com.android.internal.telephony.imsphone;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.radio.V1_0.LastCallFailCause;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.telecom.ConferenceParticipant;
import android.telecom.Connection;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSuppServiceNotification;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.ims.stub.ImsConfigImplBase;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.util.SparseIntArray;
import com.android.ims.ImsCall;
import com.android.ims.ImsConfigListener;
import com.android.ims.ImsEcbm;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.ims.ImsMultiEndpoint;
import com.android.ims.ImsUtInterface;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsVideoCallProvider;
import com.android.ims.internal.ImsVideoCallProviderWrapper;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.google.android.mms.pdu.CharacterSets;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class ImsPhoneCallTracker extends AbstractImsPhoneCallTracker implements ImsPullCall, IHwImsPhoneCallTrackerInner {
    private static final boolean DBG = true;
    private static final int EVENT_CHECK_FOR_WIFI_HANDOVER = 25;
    private static final int EVENT_DATA_ENABLED_CHANGED = 23;
    private static final int EVENT_DIAL_PENDINGMO = 20;
    private static final int EVENT_EXIT_ECBM_BEFORE_PENDINGMO = 21;
    private static final int EVENT_HANGUP_PENDINGMO = 18;
    private static final int EVENT_ON_FEATURE_CAPABILITY_CHANGED = 26;
    private static final int EVENT_RESUME_BACKGROUND = 19;
    private static final int EVENT_SUPP_SERVICE_INDICATION = 27;
    private static final int EVENT_VT_DATA_USAGE_UPDATE = 22;
    private static final boolean FORCE_VERBOSE_STATE_LOGGING = false;
    private static final int HANDOVER_TO_WIFI_TIMEOUT_MS = 60000;
    static final String LOG_TAG = "ImsPhoneCallTracker";
    static final int MAX_CONNECTIONS = 7;
    static final int MAX_CONNECTIONS_PER_CALL = 5;
    private static final int ONE_CALL_LEFT_IN_IMS_CONF = 1;
    private static final SparseIntArray PRECISE_CAUSE_MAP = new SparseIntArray();
    private static final int TIMEOUT_HANGUP_PENDINGMO = 500;
    private static final boolean VERBOSE_STATE_LOGGING = Rlog.isLoggable(VERBOSE_STATE_TAG, 2);
    static final String VERBOSE_STATE_TAG = "IPCTState";
    private static final String VT_INTERFACE = "vt_data0";
    /* access modifiers changed from: private */
    public boolean isHwVolte = SystemProperties.getBoolean("ro.config.hw_volte_on", false);
    /* access modifiers changed from: private */
    public boolean mAllowAddCallDuringVideoCall = true;
    /* access modifiers changed from: private */
    public boolean mAllowEmergencyVideoCalls = false;
    private boolean mAlwaysPlayRemoteHoldTone = false;
    public ImsPhoneCall mBackgroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_BACKGROUND);
    /* access modifiers changed from: private */
    public ArrayList<ImsCall> mCallExpectedToResume = new ArrayList<>();
    private boolean mCarrierConfigLoaded = false;
    private int mClirMode = 0;
    private final ImsConfigImplBase.Callback mConfigCallback = new ImsConfigImplBase.Callback() {
        public void onConfigChanged(int item, int value) {
            sendConfigChangedIntent(item, Integer.toString(value));
        }

        public void onConfigChanged(int item, String value) {
            sendConfigChangedIntent(item, value);
        }

        private void sendConfigChangedIntent(int item, String value) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("sendConfigChangedIntent - [" + item + ", " + value + "]");
            Intent configChangedIntent = new Intent("com.android.intent.action.IMS_CONFIG_CHANGED");
            configChangedIntent.putExtra("item", item);
            configChangedIntent.putExtra("value", value);
            if (ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.mPhone.getContext() != null) {
                ImsPhoneCallTracker.this.mPhone.getContext().sendBroadcast(configChangedIntent);
            }
        }
    };
    private ArrayList<ImsPhoneConnection> mConnections = new ArrayList<>();
    /* access modifiers changed from: private */
    public HwCustImsPhoneCallTracker mCust;
    /* access modifiers changed from: private */
    public final AtomicInteger mDefaultDialerUid = new AtomicInteger(-1);
    private boolean mDesiredMute = false;
    private boolean mDropVideoCallWhenAnsweringAudioCall = false;
    public ImsPhoneCall mForegroundCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_FOREGROUND);
    public ImsPhoneCall mHandoverCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_HANDOVER);
    /* access modifiers changed from: private */
    public boolean mHasPerformedStartOfCallHandover = false;
    private boolean mIgnoreDataEnabledChangedForVideoCalls = false;
    /* access modifiers changed from: private */
    public ImsCall.Listener mImsCallListener = new ImsCall.Listener() {
        public void onCallProgressing(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallProgressing");
            ImsPhoneConnection unused = ImsPhoneCallTracker.this.mPendingMO = null;
            Call.State imsPhoneCallState = Call.State.ALERTING;
            if (!(imsCall == null || 2 == imsCall.getState())) {
                ImsPhoneCallTracker.this.log("DIALING");
                imsPhoneCallState = Call.State.DIALING;
            }
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, imsPhoneCallState, 0);
            if (imsCall != null) {
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallProgressing(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
            } else {
                ImsPhoneCallTracker.this.log("imscall is null can't write on progressing");
            }
        }

        public void onCallStarted(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallStarted");
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                boolean unused = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume.remove(imsCall);
            }
            ImsPhoneConnection unused2 = ImsPhoneCallTracker.this.mPendingMO = null;
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ACTIVE, 0);
            if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail && imsCall.isVideoCall() && !imsCall.isWifiCall()) {
                if (ImsPhoneCallTracker.this.isWifiConnected()) {
                    ImsPhoneCallTracker.this.sendMessageDelayed(ImsPhoneCallTracker.this.obtainMessage(25, imsCall), 60000);
                } else {
                    ImsPhoneCallTracker.this.registerForConnectivityChanges();
                }
            }
            boolean unused3 = ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover = false;
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStarted(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallUpdated(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallUpdated");
            if (imsCall != null) {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker.log("onCallUpdated: profile is " + imsCall.getCallProfile());
                    ImsPhoneCallTracker.this.processCallStateChange(imsCall, conn.getCall().mState, 0, true);
                    ImsPhoneCallTracker.this.mMetrics.writeImsCallState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), conn.getCall().mState);
                }
            }
        }

        public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            Call.State callState;
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onCallStartFailed reasonCode=" + reasonInfo.getCode());
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                ImsPhoneCallTracker.this.log("onCallStarted: starting a call as a result of a switch.");
                boolean unused = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume.remove(imsCall);
            }
            if (ImsPhoneCallTracker.this.mPendingMO != null) {
                if (reasonInfo.getCode() == 146 && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.IDLE) {
                    ImsPhoneCallTracker.this.mForegroundCall.detach(ImsPhoneCallTracker.this.mPendingMO);
                    ImsPhoneCallTracker.this.removeConnection(ImsPhoneCallTracker.this.mPendingMO);
                    ImsPhoneCallTracker.this.mPendingMO.finalize();
                    ImsPhoneConnection unused2 = ImsPhoneCallTracker.this.mPendingMO = null;
                    ImsPhoneCallTracker.this.mPhone.initiateSilentRedial();
                    return;
                }
                ImsPhoneConnection unused3 = ImsPhoneCallTracker.this.mPendingMO = null;
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
                if (conn != null) {
                    callState = conn.getState();
                } else {
                    callState = Call.State.DIALING;
                }
                int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo, callState);
                if (conn != null) {
                    conn.setPreciseDisconnectCause(ImsPhoneCallTracker.this.getPreciseDisconnectCauseFromReasonInfo(reasonInfo));
                }
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.DISCONNECTED, cause);
                ImsPhoneCallTracker.this.mMetrics.writeOnImsCallStartFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            }
        }

        public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            Call.State callState;
            String str;
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onCallTerminated reasonCode=" + reasonInfo.getCode());
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                callState = conn.getState();
            } else {
                callState = Call.State.ACTIVE;
            }
            int cause = ImsPhoneCallTracker.this.getDisconnectCauseFromReasonInfo(reasonInfo, callState);
            ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
            imsPhoneCallTracker2.log("cause = " + cause + " conn = " + conn);
            if (conn != null) {
                ImsVideoCallProviderWrapper wrapper = conn.getVideoProvider();
                if (wrapper instanceof ImsVideoCallProviderWrapper) {
                    wrapper.removeImsVideoProviderCallback(conn);
                }
            }
            if (ImsPhoneCallTracker.this.mOnHoldToneId == System.identityHashCode(conn)) {
                if (conn != null && ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                    ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                }
                boolean unused = ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                int unused2 = ImsPhoneCallTracker.this.mOnHoldToneId = -1;
            }
            if (conn != null) {
                if (conn.equals(ImsPhoneCallTracker.this.mPendingMO)) {
                    ImsPhoneCallTracker.this.log("mPendingMO == conn");
                    ImsPhoneConnection unused3 = ImsPhoneCallTracker.this.mPendingMO = null;
                }
                if (conn.isPulledCall() && ((reasonInfo.getCode() == 1015 || reasonInfo.getCode() == 336 || reasonInfo.getCode() == 332) && ImsPhoneCallTracker.this.mPhone != null && ImsPhoneCallTracker.this.mPhone.getExternalCallTracker() != null)) {
                    ImsPhoneCallTracker.this.log("Call pull failed.");
                    conn.onCallPullFailed(ImsPhoneCallTracker.this.mPhone.getExternalCallTracker().getConnectionById(conn.getPulledDialogId()));
                    cause = 0;
                } else if (conn.isIncoming() && conn.getConnectTime() == 0 && cause != 52) {
                    if (cause == 2) {
                        cause = 1;
                    } else if (cause == 3) {
                        cause = 16;
                    }
                    ImsPhoneCallTracker imsPhoneCallTracker3 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker3.log("Incoming connection of 0 connect time detected - translated cause = " + cause);
                }
            }
            if (cause == 2 && conn != null && conn.getImsCall() != null && conn.getImsCall().isMerged()) {
                cause = 45;
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallTerminated(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
            if (conn != null) {
                conn.setPreciseDisconnectCause(ImsPhoneCallTracker.this.getPreciseDisconnectCauseFromReasonInfo(reasonInfo));
            }
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.DISCONNECTED, cause);
            if (SystemProperties.getBoolean("ro.config.hw_add_sip_error_pop", false) && ImsPhoneCallTracker.this.mCust != null) {
                ImsPhoneCallTracker.this.mCust.addSipErrorPopup(reasonInfo, ImsPhoneCallTracker.this.mPhone.getContext());
            }
            if (ImsPhoneCallTracker.this.mForegroundCall.getState() != Call.State.ACTIVE) {
                if (ImsPhoneCallTracker.this.mRingingCall.getState().isRinging()) {
                    ImsPhoneConnection unused4 = ImsPhoneCallTracker.this.mPendingMO = null;
                } else if (ImsPhoneCallTracker.this.mPendingMO != null) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(20);
                }
            }
            if (ImsPhoneCallTracker.this.mCust != null) {
                ImsPhoneCallTracker.this.mCust.handleCallDropErrors(reasonInfo);
            }
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                ImsPhoneCallTracker.this.log("onCallTerminated: Call terminated in the midst of Switching Fg and Bg calls.");
                if (ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                    ImsPhoneCallTracker imsPhoneCallTracker4 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker4.log("onCallTerminated: switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                    if (!((ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.HOLDING) || (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.ACTIVE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE))) {
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    }
                }
                ImsPhoneCallTracker imsPhoneCallTracker5 = ImsPhoneCallTracker.this;
                StringBuilder sb = new StringBuilder();
                sb.append("onCallTerminated: foreground call in state ");
                sb.append(ImsPhoneCallTracker.this.mForegroundCall.getState());
                sb.append(" and ringing call in state ");
                if (ImsPhoneCallTracker.this.mRingingCall == null) {
                    str = "null";
                } else {
                    str = ImsPhoneCallTracker.this.mRingingCall.getState().toString();
                }
                sb.append(str);
                imsPhoneCallTracker5.log(sb.toString());
                if (ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING || ImsPhoneCallTracker.this.mRingingCall.getState() == Call.State.WAITING) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    boolean unused5 = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                }
            }
            if (ImsPhoneCallTracker.this.mShouldUpdateImsConfigOnDisconnect) {
                HwFrameworkFactory.updateImsServiceConfig(ImsPhoneCallTracker.this.mPhone.getContext(), ImsPhoneCallTracker.this.mPhone.getPhoneId(), true);
                boolean unused6 = ImsPhoneCallTracker.this.mShouldUpdateImsConfigOnDisconnect = false;
            }
        }

        public void onCallHeld(ImsCall imsCall) {
            if (ImsPhoneCallTracker.this.mForegroundCall.getImsCall() == imsCall) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallHeld (fg) " + imsCall);
            } else if (ImsPhoneCallTracker.this.mBackgroundCall.getImsCall() == imsCall) {
                ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                imsPhoneCallTracker2.log("onCallHeld (bg) " + imsCall);
            }
            synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                Call.State oldState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.HOLDING, 0);
                if (oldState == Call.State.ACTIVE) {
                    if (ImsPhoneCallTracker.this.mForegroundCall.getState() != Call.State.HOLDING) {
                        if (ImsPhoneCallTracker.this.mRingingCall.getState() != Call.State.WAITING) {
                            if (ImsPhoneCallTracker.this.mPendingMO != null) {
                                ImsPhoneCallTracker.this.dialPendingMO();
                            }
                            boolean unused = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        }
                    }
                    ImsPhoneCallTracker.this.sendEmptyMessage(19);
                } else if (oldState == Call.State.IDLE && ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.HOLDING) {
                    ImsPhoneCallTracker.this.sendEmptyMessage(19);
                    boolean unused2 = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                }
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHeld(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallHoldFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onCallHoldFailed reasonCode=" + reasonInfo.getCode());
            synchronized (ImsPhoneCallTracker.this.mSyncHold) {
                Call.State bgState = ImsPhoneCallTracker.this.mBackgroundCall.getState();
                if (reasonInfo.getCode() == 148) {
                    if (ImsPhoneCallTracker.this.mPendingMO != null) {
                        ImsPhoneCallTracker.this.dialPendingMO();
                    }
                } else if (bgState == Call.State.ACTIVE) {
                    ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                    if (ImsPhoneCallTracker.this.mPendingMO != null) {
                        ImsPhoneCallTracker.this.mPendingMO.setDisconnectCause(36);
                        ImsPhoneCallTracker.this.sendEmptyMessageDelayed(18, 500);
                    }
                }
                ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(PhoneInternalInterface.SuppService.HOLD);
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHoldFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
        }

        public void onCallResumed(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallResumed");
            if (ImsPhoneCallTracker.this.isHwVolte) {
                hwOnCallResumed(imsCall);
            } else if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && !ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallResumed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                boolean unused = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
            }
            ImsPhoneCallTracker.this.processCallStateChange(imsCall, Call.State.ACTIVE, 0);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        private void hwOnCallResumed(ImsCall imsCall) {
            if (imsCall == null) {
                ImsPhoneCallTracker.this.loge("imsCall is null, resume failed");
                return;
            }
            synchronized (ImsPhoneCallTracker.this.mSyncResume) {
                if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls) {
                    if (!ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                        ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                        imsPhoneCallTracker.log("onCallResumed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                        ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                        boolean unused = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                        ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                    } else {
                        ImsPhoneCallTracker.this.mCallExpectedToResume.remove(imsCall);
                    }
                    if (ImsPhoneCallTracker.this.mCallExpectedToResume.isEmpty()) {
                        ImsPhoneCallTracker.this.log("mCallExpectedToResume cleared, reset mSwitchingFgAndBgCalls to false");
                        boolean unused2 = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
                    }
                }
            }
        }

        public void onCallResumeFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            if (ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls && !ImsPhoneCallTracker.this.mCallExpectedToResume.contains(imsCall)) {
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallResumeFailed : switching " + ImsPhoneCallTracker.this.mForegroundCall + " with " + ImsPhoneCallTracker.this.mBackgroundCall);
                ImsPhoneCallTracker.this.mForegroundCall.switchWith(ImsPhoneCallTracker.this.mBackgroundCall);
                ImsPhoneCallTracker.this.mCallExpectedToResume.clear();
                boolean unused = ImsPhoneCallTracker.this.mSwitchingFgAndBgCalls = false;
            }
            ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(PhoneInternalInterface.SuppService.RESUME);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeFailed(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession(), reasonInfo);
        }

        public void onCallResumeReceived(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("onCallResumeReceived");
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                if (ImsPhoneCallTracker.this.mOnHoldToneStarted) {
                    ImsPhoneCallTracker.this.mPhone.stopOnHoldTone(conn);
                    boolean unused = ImsPhoneCallTracker.this.mOnHoldToneStarted = false;
                }
                conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_UNHELD", null);
            }
            boolean useVideoPauseWorkaround = ImsPhoneCallTracker.this.mPhone.getContext().getResources().getBoolean(17957062);
            if (conn != null && useVideoPauseWorkaround && ImsPhoneCallTracker.this.mSupportPauseVideo && VideoProfile.isVideo(conn.getVideoState())) {
                conn.changeToUnPausedState();
            }
            SuppServiceNotification supp = new SuppServiceNotification();
            supp.notificationType = 1;
            supp.code = 3;
            if (imsCall.isOnHold()) {
                supp.type = 1;
            } else {
                supp.type = 0;
            }
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onCallResumeReceived supp.type:" + supp.type);
            ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallResumeReceived(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getCallSession());
        }

        public void onCallHoldReceived(ImsCall imsCall) {
            ImsPhoneCallTracker.this.onCallHoldReceived(imsCall);
            ImsPhoneCallTracker.this.log("onCallHoldReceived");
        }

        public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onCallSuppServiceReceived: suppServiceInfo=" + suppServiceInfo);
            SuppServiceNotification supp = new SuppServiceNotification();
            supp.notificationType = suppServiceInfo.notificationType;
            supp.code = suppServiceInfo.code;
            supp.index = suppServiceInfo.index;
            supp.number = suppServiceInfo.number;
            supp.history = suppServiceInfo.history;
            ImsPhoneCallTracker.this.mPhone.notifySuppSvcNotification(supp);
        }

        public void onCallMerged(ImsCall call, ImsCall peerCall, boolean swapCalls) {
            ImsPhoneCall peerImsPhoneCall;
            ImsPhoneCallTracker.this.log("onCallMerged");
            ImsPhoneCall foregroundImsPhoneCall = ImsPhoneCallTracker.this.findConnection(call).getCall();
            ImsPhoneConnection peerConnection = ImsPhoneCallTracker.this.findConnection(peerCall);
            if (peerConnection == null) {
                peerImsPhoneCall = null;
            } else {
                peerImsPhoneCall = peerConnection.getCall();
            }
            if (swapCalls) {
                ImsPhoneCallTracker.this.switchAfterConferenceSuccess();
            }
            foregroundImsPhoneCall.merge(peerImsPhoneCall, Call.State.ACTIVE);
            try {
                ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
                ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                imsPhoneCallTracker.log("onCallMerged: ImsPhoneConnection=" + conn);
                ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                imsPhoneCallTracker2.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
                ImsPhoneCallTracker.this.setVideoCallProvider(conn, call);
                ImsPhoneCallTracker imsPhoneCallTracker3 = ImsPhoneCallTracker.this;
                imsPhoneCallTracker3.log("onCallMerged: CurrentVideoProvider=" + conn.getVideoProvider());
            } catch (Exception e) {
                ImsPhoneCallTracker imsPhoneCallTracker4 = ImsPhoneCallTracker.this;
                imsPhoneCallTracker4.loge("onCallMerged: exception " + e);
            }
            ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mForegroundCall.getImsCall(), Call.State.ACTIVE, 0);
            if (peerConnection != null) {
                ImsPhoneCallTracker.this.processCallStateChange(ImsPhoneCallTracker.this.mBackgroundCall.getImsCall(), Call.State.HOLDING, 0);
            }
            if (!call.isMergeRequestedByConf()) {
                ImsPhoneCallTracker.this.log("onCallMerged :: calling onMultipartyStateChanged()");
                onMultipartyStateChanged(call, true);
            } else {
                ImsPhoneCallTracker.this.log("onCallMerged :: Merge requested by existing conference.");
                call.resetIsMergeRequestedByConf(false);
            }
            ImsPhoneCallTracker.this.logState();
        }

        public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onCallMergeFailed reasonInfo=" + reasonInfo);
            ImsPhoneCallTracker.this.mPhone.notifySuppServiceFailed(PhoneInternalInterface.SuppService.CONFERENCE);
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
            if (conn != null) {
                conn.onConferenceMergeFailed();
                conn.handleMergeComplete();
            }
        }

        public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> participants) {
            ImsPhoneCallTracker.this.log("onConferenceParticipantsStateChanged");
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(call);
            if (conn != null) {
                conn.updateConferenceParticipants(participants);
            }
        }

        public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
            ImsPhoneCallTracker.this.mPhone.onTtyModeReceived(mode);
        }

        public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker.this.log("onCallHandover ::  srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            boolean isHandoverFromWifi = false;
            boolean isHandoverToWifi = (srcAccessTech == 0 || srcAccessTech == 18 || targetAccessTech != 18) ? false : true;
            if (!(srcAccessTech != 18 || targetAccessTech == 0 || targetAccessTech == 18)) {
                isHandoverFromWifi = true;
            }
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                if (conn.getDisconnectCause() == 0) {
                    if (isHandoverToWifi) {
                        ImsPhoneCallTracker.this.removeMessages(25);
                        if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromLTEToWifi && ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover) {
                            conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_LTE_TO_WIFI", null);
                        }
                        ImsPhoneCallTracker.this.unregisterForConnectivityChanges();
                    } else if (isHandoverFromWifi && imsCall.isVideoCall()) {
                        ImsPhoneCallTracker.this.registerForConnectivityChanges();
                    }
                }
                if (isHandoverFromWifi && imsCall.isVideoCall()) {
                    if (ImsPhoneCallTracker.this.mNotifyHandoverVideoFromWifiToLTE && ImsPhoneCallTracker.this.mIsDataEnabled) {
                        if (conn.getDisconnectCause() == 0) {
                            ImsPhoneCallTracker.this.log("onCallHandover :: notifying of WIFI to LTE handover.");
                            conn.onConnectionEvent("android.telephony.event.EVENT_HANDOVER_VIDEO_FROM_WIFI_TO_LTE", null);
                        } else {
                            ImsPhoneCallTracker.this.log("onCallHandover :: skip notify of WIFI to LTE handover for disconnected call.");
                        }
                    }
                    if (!ImsPhoneCallTracker.this.mIsDataEnabled && ImsPhoneCallTracker.this.mIsViLteDataMetered) {
                        ImsPhoneCallTracker.this.downgradeVideoCall(1407, conn);
                    }
                }
            } else {
                ImsPhoneCallTracker.this.loge("onCallHandover :: connection null.");
            }
            if (!ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover) {
                boolean unused = ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover = true;
            }
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 18, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
        }

        public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onCallHandoverFailed :: srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsCallHandoverEvent(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 19, imsCall.getCallSession(), srcAccessTech, targetAccessTech, reasonInfo);
            boolean isHandoverToWifi = srcAccessTech != 18 && targetAccessTech == 18;
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null && isHandoverToWifi) {
                ImsPhoneCallTracker.this.log("onCallHandoverFailed - handover to WIFI Failed");
                ImsPhoneCallTracker.this.removeMessages(25);
                if (imsCall.isVideoCall() && conn.getDisconnectCause() == 0) {
                    ImsPhoneCallTracker.this.registerForConnectivityChanges();
                }
                if (ImsPhoneCallTracker.this.mNotifyVtHandoverToWifiFail) {
                    conn.onHandoverToWifiFailed();
                }
            }
            if (!ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover) {
                boolean unused = ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover = true;
            }
        }

        public void onRttModifyRequestReceived(ImsCall imsCall) {
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.onRttModifyRequestReceived();
            }
        }

        public void onRttModifyResponseReceived(ImsCall imsCall, int status) {
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.onRttModifyResponseReceived(status);
            }
        }

        public void onRttMessageReceived(ImsCall imsCall, String message) {
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.onRttMessageReceived(message);
            }
        }

        public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            StringBuilder sb = new StringBuilder();
            sb.append("onMultipartyStateChanged to ");
            sb.append(isMultiParty ? "Y" : "N");
            imsPhoneCallTracker.log(sb.toString());
            ImsPhoneConnection conn = ImsPhoneCallTracker.this.findConnection(imsCall);
            if (conn != null) {
                conn.updateMultipartyState(isMultiParty);
            }
        }
    };
    private final ImsFeature.CapabilityCallback mImsCapabilityCallback = new ImsFeature.CapabilityCallback() {
        public void onCapabilitiesStatusChanged(ImsFeature.Capabilities config) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onCapabilitiesStatusChanged: " + config);
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = config;
            ImsPhoneCallTracker.this.removeMessages(26);
            ImsPhoneCallTracker.this.obtainMessage(26, args).sendToTarget();
        }
    };
    private ImsConfigListener.Stub mImsConfigListener = new ImsConfigListener.Stub() {
        public void onGetFeatureResponse(int feature, int network, int value, int status) {
        }

        public void onSetFeatureResponse(int feature, int network, int value, int status) {
            ImsPhoneCallTracker.this.mMetrics.writeImsSetFeatureValue(ImsPhoneCallTracker.this.mPhone.getPhoneId(), feature, network, value);
        }

        public void onGetVideoQuality(int status, int quality) {
        }

        public void onSetVideoQuality(int status) {
        }
    };
    /* access modifiers changed from: private */
    public ImsManager mImsManager;
    private final ImsManager.Connector mImsManagerConnector;
    private Map<Pair<Integer, String>, Integer> mImsReasonCodeMap = new ArrayMap();
    private final ImsRegistrationImplBase.Callback mImsRegistrationCallback = new ImsRegistrationImplBase.Callback() {
        public void onRegistered(int imsRadioTech) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onImsConnected imsRadioTech=" + imsRadioTech);
            ImsPhoneCallTracker.this.mPhone.setServiceState(0);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(true);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 1, null);
        }

        public void onRegistering(int imsRadioTech) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onImsProgressing imsRadioTech=" + imsRadioTech);
            ImsPhoneCallTracker.this.mPhone.setServiceState(1);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 2, null);
        }

        public void onDeregistered(ImsReasonInfo imsReasonInfo) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onImsDisconnected imsReasonInfo=" + imsReasonInfo);
            ImsPhoneCallTracker.this.mPhone.setServiceState(1);
            ImsPhoneCallTracker.this.mPhone.setImsRegistered(false);
            ImsPhoneCallTracker.this.mPhone.processDisconnectReason(imsReasonInfo);
            ImsPhoneCallTracker.this.mMetrics.writeOnImsConnectionState(ImsPhoneCallTracker.this.mPhone.getPhoneId(), 3, imsReasonInfo);
        }

        public void onSubscriberAssociatedUriChanged(Uri[] uris) {
            ImsPhoneCallTracker.this.log("registrationAssociatedUriChanged");
            ImsPhoneCallTracker.this.mPhone.setCurrentSubscriberUris(uris);
        }
    };
    /* access modifiers changed from: private */
    public ImsCall.Listener mImsUssdListener = new ImsCall.Listener() {
        public void onCallStarted(ImsCall imsCall) {
            ImsPhoneCallTracker.this.log("mImsUssdListener onCallStarted");
            if (imsCall == ImsPhoneCallTracker.this.mUssdSession && ImsPhoneCallTracker.this.mPendingUssd != null) {
                AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd);
                ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                Message unused = ImsPhoneCallTracker.this.mPendingUssd = null;
            }
        }

        public void onCallStartFailed(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("mImsUssdListener onCallStartFailed reasonCode=" + reasonInfo.getCode());
            onCallTerminated(imsCall, reasonInfo);
        }

        public void onCallTerminated(ImsCall imsCall, ImsReasonInfo reasonInfo) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("mImsUssdListener onCallTerminated reasonCode=" + reasonInfo.getCode());
            ImsPhoneCallTracker.this.removeMessages(25);
            boolean unused = ImsPhoneCallTracker.this.mHasPerformedStartOfCallHandover = false;
            ImsPhoneCallTracker.this.unregisterForConnectivityChanges();
            if (imsCall == ImsPhoneCallTracker.this.mUssdSession) {
                ImsCall unused2 = ImsPhoneCallTracker.this.mUssdSession = null;
                if (ImsPhoneCallTracker.this.mPendingUssd != null) {
                    AsyncResult.forMessage(ImsPhoneCallTracker.this.mPendingUssd, null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                    ImsPhoneCallTracker.this.mPendingUssd.sendToTarget();
                    Message unused3 = ImsPhoneCallTracker.this.mPendingUssd = null;
                }
            }
            imsCall.close();
        }

        public void onCallUssdMessageReceived(ImsCall call, int mode, String ussdMessage) {
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("mImsUssdListener onCallUssdMessageReceived mode=" + mode);
            int ussdMode = -1;
            switch (mode) {
                case 0:
                    ussdMode = 0;
                    break;
                case 1:
                    ussdMode = 1;
                    break;
            }
            ImsPhoneCallTracker.this.mPhone.onIncomingUSSD(ussdMode, ussdMessage);
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsDataEnabled = false;
    private boolean mIsInEmergencyCall = false;
    private boolean mIsMonitoringConnectivity = false;
    /* access modifiers changed from: private */
    public boolean mIsViLteDataMetered = false;
    /* access modifiers changed from: private */
    public TelephonyMetrics mMetrics;
    private MmTelFeature.MmTelCapabilities mMmTelCapabilities = new MmTelFeature.MmTelCapabilities();
    private final MmTelFeatureListener mMmTelFeatureListener = new MmTelFeatureListener();
    private ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            Rlog.i(ImsPhoneCallTracker.LOG_TAG, "Network available: " + network);
            ImsPhoneCallTracker.this.scheduleHandoverCheck();
        }
    };
    /* access modifiers changed from: private */
    public boolean mNotifyHandoverVideoFromLTEToWifi = false;
    private boolean mNotifyHandoverVideoFromLteToWifi = false;
    /* access modifiers changed from: private */
    public boolean mNotifyHandoverVideoFromWifiToLTE = false;
    /* access modifiers changed from: private */
    public boolean mNotifyVtHandoverToWifiFail = false;
    /* access modifiers changed from: private */
    public int mOnHoldToneId = -1;
    /* access modifiers changed from: private */
    public boolean mOnHoldToneStarted = false;
    private int mPendingCallVideoState;
    private Bundle mPendingIntentExtras;
    /* access modifiers changed from: private */
    public ImsPhoneConnection mPendingMO;
    /* access modifiers changed from: private */
    public Message mPendingUssd = null;
    ImsPhone mPhone;
    private PhoneNumberUtilsProxy mPhoneNumberUtilsProxy = $$Lambda$ImsPhoneCallTracker$QlPVd_3u4_verjHUDnkn6zaSe54.INSTANCE;
    private List<PhoneStateListener> mPhoneStateListeners = new ArrayList();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                int subId = intent.getIntExtra("subscription", -1);
                if (subId == ImsPhoneCallTracker.this.mPhone.getSubId()) {
                    ImsPhoneCallTracker.this.cacheCarrierConfiguration(subId);
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker.log("onReceive : Updating mAllowEmergencyVideoCalls = " + ImsPhoneCallTracker.this.mAllowEmergencyVideoCalls);
                }
            } else if ("android.telecom.action.CHANGE_DEFAULT_DIALER".equals(intent.getAction())) {
                ImsPhoneCallTracker.this.mDefaultDialerUid.set(ImsPhoneCallTracker.this.getPackageUid(context, intent.getStringExtra("android.telecom.extra.CHANGE_DEFAULT_DIALER_PACKAGE_NAME")));
            }
        }
    };
    public ImsPhoneCall mRingingCall = new ImsPhoneCall(this, ImsPhoneCall.CONTEXT_RINGING);
    private SharedPreferenceProxy mSharedPreferenceProxy = $$Lambda$ImsPhoneCallTracker$Zw03itjXT6LrhiYuD9nKFg2Wg.INSTANCE;
    /* access modifiers changed from: private */
    public boolean mShouldUpdateImsConfigOnDisconnect = false;
    private Call.SrvccState mSrvccState = Call.SrvccState.NONE;
    private PhoneConstants.State mState = PhoneConstants.State.IDLE;
    private boolean mSupportDowngradeVtToAudio = false;
    /* access modifiers changed from: private */
    public boolean mSupportPauseVideo = false;
    /* access modifiers changed from: private */
    public boolean mSwitchingFgAndBgCalls = false;
    /* access modifiers changed from: private */
    public Object mSyncHold = new Object();
    /* access modifiers changed from: private */
    public Object mSyncResume = new Object();
    private boolean mTreatDowngradedVideoCallsAsVideoCalls = false;
    /* access modifiers changed from: private */
    public ImsCall mUssdSession = null;
    private ImsUtInterface mUtInterface;
    private RegistrantList mVoiceCallEndedRegistrants = new RegistrantList();
    private RegistrantList mVoiceCallStartedRegistrants = new RegistrantList();
    private final HashMap<Integer, Long> mVtDataUsageMap = new HashMap<>();
    private volatile NetworkStats mVtDataUsageSnapshot = null;
    private volatile NetworkStats mVtDataUsageUidSnapshot = null;
    private int pendingCallClirMode;
    private boolean pendingCallInEcm = false;

    private class MmTelFeatureListener extends MmTelFeature.Listener {
        private MmTelFeatureListener() {
        }

        public void onIncomingCall(IImsCallSession c, Bundle extras) {
            ImsPhoneCallTracker.this.log("onReceive : incoming call intent");
            if (ImsPhoneCallTracker.this.mImsManager != null) {
                try {
                    if (extras.getBoolean("android:ussd", false)) {
                        ImsPhoneCallTracker.this.log("onReceive : USSD");
                        ImsCall unused = ImsPhoneCallTracker.this.mUssdSession = ImsPhoneCallTracker.this.mImsManager.takeCall(c, extras, ImsPhoneCallTracker.this.mImsUssdListener);
                        if (ImsPhoneCallTracker.this.mUssdSession != null) {
                            ImsPhoneCallTracker.this.mUssdSession.accept(2);
                        }
                        return;
                    }
                    boolean isUnknown = extras.getBoolean("android:isUnknown", false);
                    ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker.log("onReceive : isUnknown = " + isUnknown + " fg = " + ImsPhoneCallTracker.this.mForegroundCall.getState() + " bg = " + ImsPhoneCallTracker.this.mBackgroundCall.getState());
                    if (HwDeviceManager.disallowOp(1)) {
                        Log.i(ImsPhoneCallTracker.LOG_TAG, "MDM APK disallow open call.");
                        return;
                    }
                    ImsCall imsCall = ImsPhoneCallTracker.this.mImsManager.takeCall(c, extras, ImsPhoneCallTracker.this.mImsCallListener);
                    ImsPhoneConnection imsPhoneConnection = new ImsPhoneConnection((Phone) ImsPhoneCallTracker.this.mPhone, imsCall, ImsPhoneCallTracker.this, isUnknown ? ImsPhoneCallTracker.this.mForegroundCall : ImsPhoneCallTracker.this.mRingingCall, isUnknown);
                    ImsPhoneConnection conn = imsPhoneConnection;
                    if (ImsPhoneCallTracker.this.mForegroundCall.hasConnections()) {
                        ImsCall activeCall = ImsPhoneCallTracker.this.mForegroundCall.getFirstConnection().getImsCall();
                        if (!(activeCall == null || imsCall == null)) {
                            conn.setActiveCallDisconnectedOnAnswer(ImsPhoneCallTracker.this.shouldDisconnectActiveCallOnAnswer(activeCall, imsCall));
                        }
                    }
                    conn.setAllowAddCallDuringVideoCall(ImsPhoneCallTracker.this.mAllowAddCallDuringVideoCall);
                    ImsPhoneCallTracker.this.addConnection(conn);
                    ImsPhoneCallTracker.this.setVideoCallProvider(conn, imsCall);
                    TelephonyMetrics.getInstance().writeOnImsCallReceive(ImsPhoneCallTracker.this.mPhone.getPhoneId(), imsCall.getSession());
                    if (isUnknown) {
                        ImsPhoneCallTracker.this.mPhone.notifyUnknownConnection(conn);
                    } else {
                        if (!(ImsPhoneCallTracker.this.mForegroundCall.getState() == Call.State.IDLE && ImsPhoneCallTracker.this.mBackgroundCall.getState() == Call.State.IDLE)) {
                            conn.update(imsCall, Call.State.WAITING);
                        }
                        ImsPhoneCallTracker.this.mPhone.notifyNewRingingConnection(conn);
                        ImsPhoneCallTracker.this.mPhone.notifyIncomingRing();
                    }
                    ImsPhoneCallTracker.this.updatePhoneState();
                    ImsPhoneCallTracker.this.mPhone.notifyPreciseCallStateChanged();
                } catch (ImsException e) {
                    ImsPhoneCallTracker imsPhoneCallTracker2 = ImsPhoneCallTracker.this;
                    imsPhoneCallTracker2.loge("onReceive : exception " + e);
                } catch (RemoteException e2) {
                }
            }
        }

        public void onVoiceMessageCountUpdate(int count) {
            if (ImsPhoneCallTracker.this.mPhone == null || ImsPhoneCallTracker.this.mPhone.mDefaultPhone == null) {
                ImsPhoneCallTracker.this.loge("onVoiceMessageCountUpdate: null phone");
                return;
            }
            ImsPhoneCallTracker imsPhoneCallTracker = ImsPhoneCallTracker.this;
            imsPhoneCallTracker.log("onVoiceMessageCountChanged :: count=" + count);
            ImsPhoneCallTracker.this.mPhone.mDefaultPhone.setVoiceMessageCount(count);
        }
    }

    public interface PhoneNumberUtilsProxy {
        boolean isEmergencyNumber(String str);
    }

    public interface PhoneStateListener {
        void onPhoneStateChanged(PhoneConstants.State state, PhoneConstants.State state2);
    }

    public interface SharedPreferenceProxy {
        SharedPreferences getDefaultSharedPreferences(Context context);
    }

    static {
        PRECISE_CAUSE_MAP.append(101, 1200);
        PRECISE_CAUSE_MAP.append(102, 1201);
        PRECISE_CAUSE_MAP.append(103, 1202);
        PRECISE_CAUSE_MAP.append(106, 1203);
        PRECISE_CAUSE_MAP.append(107, 1204);
        PRECISE_CAUSE_MAP.append(AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE, 16);
        PRECISE_CAUSE_MAP.append(111, 1205);
        PRECISE_CAUSE_MAP.append(112, 1206);
        PRECISE_CAUSE_MAP.append(121, 1207);
        PRECISE_CAUSE_MAP.append(122, 1208);
        PRECISE_CAUSE_MAP.append(123, 1209);
        PRECISE_CAUSE_MAP.append(124, 1210);
        PRECISE_CAUSE_MAP.append(131, 1211);
        PRECISE_CAUSE_MAP.append(132, 1212);
        PRECISE_CAUSE_MAP.append(141, 1213);
        PRECISE_CAUSE_MAP.append(143, 1214);
        PRECISE_CAUSE_MAP.append(144, 1215);
        PRECISE_CAUSE_MAP.append(145, 1216);
        PRECISE_CAUSE_MAP.append(146, 1217);
        PRECISE_CAUSE_MAP.append(147, 1218);
        PRECISE_CAUSE_MAP.append(148, 1219);
        PRECISE_CAUSE_MAP.append(149, 1220);
        PRECISE_CAUSE_MAP.append(201, 1221);
        PRECISE_CAUSE_MAP.append(202, 1222);
        PRECISE_CAUSE_MAP.append(203, 1223);
        PRECISE_CAUSE_MAP.append(241, 241);
        PRECISE_CAUSE_MAP.append(321, 1300);
        PRECISE_CAUSE_MAP.append(331, 1310);
        PRECISE_CAUSE_MAP.append(332, 1311);
        PRECISE_CAUSE_MAP.append(333, 1312);
        PRECISE_CAUSE_MAP.append(334, 1313);
        PRECISE_CAUSE_MAP.append(335, 1314);
        PRECISE_CAUSE_MAP.append(336, 1315);
        PRECISE_CAUSE_MAP.append(337, 1316);
        PRECISE_CAUSE_MAP.append(338, 1317);
        PRECISE_CAUSE_MAP.append(339, 1318);
        PRECISE_CAUSE_MAP.append(340, 1319);
        PRECISE_CAUSE_MAP.append(341, 1320);
        PRECISE_CAUSE_MAP.append(342, 1321);
        PRECISE_CAUSE_MAP.append(351, 1330);
        PRECISE_CAUSE_MAP.append(352, 1331);
        PRECISE_CAUSE_MAP.append(353, 1332);
        PRECISE_CAUSE_MAP.append(354, 1333);
        PRECISE_CAUSE_MAP.append(361, 1340);
        PRECISE_CAUSE_MAP.append(362, 1341);
        PRECISE_CAUSE_MAP.append(363, 1342);
        PRECISE_CAUSE_MAP.append(364, 1343);
        PRECISE_CAUSE_MAP.append(401, 1400);
        PRECISE_CAUSE_MAP.append(402, 1401);
        PRECISE_CAUSE_MAP.append(403, 1402);
        PRECISE_CAUSE_MAP.append(404, 1403);
        PRECISE_CAUSE_MAP.append(501, 1500);
        PRECISE_CAUSE_MAP.append(502, 1501);
        PRECISE_CAUSE_MAP.append(503, 1502);
        PRECISE_CAUSE_MAP.append(504, 1503);
        PRECISE_CAUSE_MAP.append(505, 1504);
        PRECISE_CAUSE_MAP.append(506, 1505);
        PRECISE_CAUSE_MAP.append(510, 1510);
        PRECISE_CAUSE_MAP.append(801, 1800);
        PRECISE_CAUSE_MAP.append(802, 1801);
        PRECISE_CAUSE_MAP.append(803, 1802);
        PRECISE_CAUSE_MAP.append(804, 1803);
        PRECISE_CAUSE_MAP.append(821, 1804);
        PRECISE_CAUSE_MAP.append(901, 1900);
        PRECISE_CAUSE_MAP.append(902, 1901);
        PRECISE_CAUSE_MAP.append(1100, 2000);
        PRECISE_CAUSE_MAP.append(1014, 2100);
        PRECISE_CAUSE_MAP.append(CharacterSets.UTF_16, 2101);
        PRECISE_CAUSE_MAP.append(1016, 2102);
        PRECISE_CAUSE_MAP.append(1201, 2300);
        PRECISE_CAUSE_MAP.append(1202, 2301);
        PRECISE_CAUSE_MAP.append(1203, 2302);
        PRECISE_CAUSE_MAP.append(1300, 2400);
        PRECISE_CAUSE_MAP.append(1400, 2500);
        PRECISE_CAUSE_MAP.append(1401, 2501);
        PRECISE_CAUSE_MAP.append(1402, 2502);
        PRECISE_CAUSE_MAP.append(1403, 2503);
        PRECISE_CAUSE_MAP.append(1404, 2504);
        PRECISE_CAUSE_MAP.append(1405, 2505);
        PRECISE_CAUSE_MAP.append(1406, 2506);
        PRECISE_CAUSE_MAP.append(1407, 2507);
        PRECISE_CAUSE_MAP.append(1500, LastCallFailCause.RADIO_OFF);
        PRECISE_CAUSE_MAP.append(1501, LastCallFailCause.NO_VALID_SIM);
        PRECISE_CAUSE_MAP.append(1502, LastCallFailCause.RADIO_INTERNAL_ERROR);
        PRECISE_CAUSE_MAP.append(1503, LastCallFailCause.NETWORK_RESP_TIMEOUT);
        PRECISE_CAUSE_MAP.append(1504, LastCallFailCause.NETWORK_REJECT);
        PRECISE_CAUSE_MAP.append(1505, LastCallFailCause.RADIO_ACCESS_FAILURE);
        PRECISE_CAUSE_MAP.append(1506, LastCallFailCause.RADIO_LINK_FAILURE);
        PRECISE_CAUSE_MAP.append(1507, 255);
        PRECISE_CAUSE_MAP.append(1508, 256);
        PRECISE_CAUSE_MAP.append(1509, LastCallFailCause.RADIO_SETUP_FAILURE);
        PRECISE_CAUSE_MAP.append(1510, LastCallFailCause.RADIO_RELEASE_NORMAL);
        PRECISE_CAUSE_MAP.append(1511, LastCallFailCause.RADIO_RELEASE_ABNORMAL);
        PRECISE_CAUSE_MAP.append(1512, LastCallFailCause.ACCESS_CLASS_BLOCKED);
        PRECISE_CAUSE_MAP.append(1513, LastCallFailCause.NETWORK_DETACH);
        PRECISE_CAUSE_MAP.append(1515, 1);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_1, LastCallFailCause.OEM_CAUSE_1);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_2, LastCallFailCause.OEM_CAUSE_2);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_3, LastCallFailCause.OEM_CAUSE_3);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_4, LastCallFailCause.OEM_CAUSE_4);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_5, LastCallFailCause.OEM_CAUSE_5);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_6, LastCallFailCause.OEM_CAUSE_6);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_7, LastCallFailCause.OEM_CAUSE_7);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_8, LastCallFailCause.OEM_CAUSE_8);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_9, LastCallFailCause.OEM_CAUSE_9);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_10, LastCallFailCause.OEM_CAUSE_10);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_11, LastCallFailCause.OEM_CAUSE_11);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_12, LastCallFailCause.OEM_CAUSE_12);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_13, LastCallFailCause.OEM_CAUSE_13);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_14, LastCallFailCause.OEM_CAUSE_14);
        PRECISE_CAUSE_MAP.append(LastCallFailCause.OEM_CAUSE_15, LastCallFailCause.OEM_CAUSE_15);
    }

    public ImsPhoneCallTracker(ImsPhone phone) {
        this.mPhone = phone;
        this.mMetrics = TelephonyMetrics.getInstance();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        intentfilter.addAction("android.telecom.action.CHANGE_DEFAULT_DIALER");
        this.mPhone.getContext().registerReceiver(this.mReceiver, intentfilter);
        cacheCarrierConfiguration(this.mPhone.getSubId());
        this.mPhone.getDefaultPhone().registerForDataEnabledChanged(this, 23, null);
        this.mDefaultDialerUid.set(getPackageUid(this.mPhone.getContext(), ((TelecomManager) this.mPhone.getContext().getSystemService("telecom")).getDefaultDialerPackage()));
        long currentTime = SystemClock.elapsedRealtime();
        this.mVtDataUsageSnapshot = new NetworkStats(currentTime, 1);
        this.mVtDataUsageUidSnapshot = new NetworkStats(currentTime, 1);
        this.mCust = (HwCustImsPhoneCallTracker) HwCustUtils.createObj(HwCustImsPhoneCallTracker.class, new Object[]{this.mPhone.getContext()});
        this.mImsManagerConnector = new ImsManager.Connector(phone.getContext(), phone.getPhoneId(), new ImsManager.Connector.Listener() {
            public void connectionReady(ImsManager manager) throws ImsException {
                ImsManager unused = ImsPhoneCallTracker.this.mImsManager = manager;
                ImsPhoneCallTracker.this.startListeningForCalls();
            }

            public void connectionUnavailable() {
                ImsPhoneCallTracker.this.stopListeningForCalls();
            }
        });
        this.mImsManagerConnector.connect();
    }

    @VisibleForTesting
    public void setSharedPreferenceProxy(SharedPreferenceProxy sharedPreferenceProxy) {
        this.mSharedPreferenceProxy = sharedPreferenceProxy;
    }

    @VisibleForTesting
    public void setPhoneNumberUtilsProxy(PhoneNumberUtilsProxy phoneNumberUtilsProxy) {
        this.mPhoneNumberUtilsProxy = phoneNumberUtilsProxy;
    }

    @VisibleForTesting
    public void setRetryTimeout(ImsManager.Connector.RetryTimeout retryTimeout) {
        this.mImsManagerConnector.mRetryTimeout = retryTimeout;
    }

    /* access modifiers changed from: private */
    public int getPackageUid(Context context, String pkg) {
        int uid = -1;
        if (pkg == null) {
            return -1;
        }
        try {
            uid = context.getPackageManager().getPackageUid(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            loge("Cannot find package uid. pkg = " + pkg);
        }
        return uid;
    }

    /* access modifiers changed from: private */
    public void startListeningForCalls() throws ImsException {
        log("startListeningForCalls");
        this.mImsManager.open(this.mMmTelFeatureListener);
        this.mImsManager.addRegistrationCallback(this.mImsRegistrationCallback);
        this.mImsManager.addCapabilitiesCallback(this.mImsCapabilityCallback);
        this.mImsManager.setConfigListener(this.mImsConfigListener);
        this.mImsManager.getConfigInterface().addConfigCallback(this.mConfigCallback);
        getEcbmInterface().setEcbmStateListener(this.mPhone.getImsEcbmStateListener());
        if (this.mPhone.isInEcm()) {
            this.mPhone.exitEmergencyCallbackMode();
        }
        this.mImsManager.setUiTTYMode(this.mPhone.getContext(), Settings.Secure.getInt(this.mPhone.getContext().getContentResolver(), "preferred_tty_mode", 0), null);
        ImsMultiEndpoint multiEndpoint = getMultiEndpointInterface();
        if (multiEndpoint != null) {
            multiEndpoint.setExternalCallStateListener(this.mPhone.getExternalCallTracker().getExternalCallStateListener());
        }
        this.mUtInterface = getUtInterface();
        if (this.mUtInterface != null) {
            this.mUtInterface.registerForSuppServiceIndication(this, 27, null);
        }
        if (this.mCarrierConfigLoaded) {
            HwFrameworkFactory.updateImsServiceConfig(this.mPhone.getContext(), this.mPhone.getPhoneId(), true);
        }
    }

    /* access modifiers changed from: private */
    public void stopListeningForCalls() {
        log("stopListeningForCalls");
        resetImsCapabilities();
        if (this.mImsManager != null) {
            try {
                this.mImsManager.getConfigInterface().removeConfigCallback(this.mConfigCallback);
            } catch (ImsException e) {
                Log.w(LOG_TAG, "stopListeningForCalls: unable to remove config callback.");
            }
            this.mImsManager.close();
        }
    }

    public void dispose() {
        log("dispose");
        this.mRingingCall.dispose();
        this.mBackgroundCall.dispose();
        this.mForegroundCall.dispose();
        this.mHandoverCall.dispose();
        clearDisconnected();
        if (this.mUtInterface != null) {
            this.mUtInterface.unregisterForSuppServiceIndication(this);
        }
        this.mPhone.getContext().unregisterReceiver(this.mReceiver);
        this.mPhone.getDefaultPhone().unregisterForDataEnabledChanged(this);
        this.mImsManagerConnector.disconnect();
        if (this.mImsManager != null) {
            this.mImsManager.close();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        log("ImsPhoneCallTracker finalized");
    }

    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        this.mVoiceCallStartedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallStarted(Handler h) {
        this.mVoiceCallStartedRegistrants.remove(h);
    }

    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        this.mVoiceCallEndedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVoiceCallEnded(Handler h) {
        this.mVoiceCallEndedRegistrants.remove(h);
    }

    public int getClirMode() {
        if (this.mSharedPreferenceProxy == null || this.mPhone.getDefaultPhone() == null) {
            loge("dial; could not get default CLIR mode.");
            return 0;
        }
        SharedPreferences sp = this.mSharedPreferenceProxy.getDefaultSharedPreferences(this.mPhone.getContext());
        return sp.getInt(Phone.CLIR_KEY + this.mPhone.getDefaultPhone().getPhoneId(), 0);
    }

    public Connection dial(String dialString, int videoState, Bundle intentExtras) throws CallStateException {
        return dial(dialString, ((ImsPhone.ImsDialArgs.Builder) ((ImsPhone.ImsDialArgs.Builder) new ImsPhone.ImsDialArgs.Builder().setIntentExtras(intentExtras)).setVideoState(videoState)).setClirMode(getClirMode()).build());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:?, code lost:
        addConnection(r7.mPendingMO);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x011a, code lost:
        if (r15 != false) goto L_0x014e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x011c, code lost:
        if (r10 == false) goto L_0x0147;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x011e, code lost:
        if (r10 == false) goto L_0x0123;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0120, code lost:
        if (r11 == false) goto L_0x0123;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        getEcbmInterface().exitEmergencyCallbackMode();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        r7.mPhone.setOnEcbModeExitResponse(r7, 14, null);
        r7.pendingCallClirMode = r12;
        r7.mPendingCallVideoState = r13;
        r7.pendingCallInEcm = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x013b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x013c, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0146, code lost:
        throw new com.android.internal.telephony.CallStateException("service not available");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0147, code lost:
        dialInternal(r7.mPendingMO, r12, r13, r9.intentExtras);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x014e, code lost:
        updatePhoneState();
        r7.mPhone.notifyPreciseCallStateChanged();
        r0 = r7.mPendingMO;
     */
    public synchronized Connection dial(String dialString, ImsPhone.ImsDialArgs dialArgs) throws CallStateException {
        ImsPhoneConnection imsPhoneConnection;
        String str = dialString;
        ImsPhone.ImsDialArgs imsDialArgs = dialArgs;
        synchronized (this) {
            boolean isPhoneInEcmMode = isPhoneInEcbMode();
            boolean isEmergencyNumber = this.mPhoneNumberUtilsProxy.isEmergencyNumber(str);
            int clirMode = imsDialArgs.clirMode;
            int videoState = imsDialArgs.videoState;
            log("dial clirMode=" + clirMode);
            if (isEmergencyNumber) {
                clirMode = 2;
                log("dial emergency call, set clirModIe=" + 2);
            }
            int clirMode2 = clirMode;
            clearDisconnected();
            if (this.mImsManager == null) {
                throw new CallStateException("service not available");
            } else if (canDial()) {
                if (this.mHwCT != null) {
                    if (this.mHwCT.isBlockDialing(str, this.mPhone.getSubId())) {
                        log("Blocking non emergency call from non AIS card");
                        throw new CallStateException("Non emergency outgoing call not allowed");
                    }
                }
                if (isPhoneInEcmMode && isEmergencyNumber) {
                    handleEcmTimer(1);
                }
                if (isEmergencyNumber && VideoProfile.isVideo(videoState) && !this.mAllowEmergencyVideoCalls) {
                    loge("dial: carrier does not support video emergency calls; downgrade to audio-only");
                    videoState = 0;
                }
                int videoState2 = videoState;
                boolean holdBeforeDial = false;
                if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
                    if (this.mBackgroundCall.getState() == Call.State.IDLE) {
                        holdBeforeDial = true;
                        this.mPendingCallVideoState = videoState2;
                        this.mPendingIntentExtras = imsDialArgs.intentExtras;
                        switchWaitingOrHoldingAndActive();
                    } else {
                        throw new CallStateException("cannot dial in current state");
                    }
                }
                if (shouldNumberBePlacedOnIms(isEmergencyNumber, str)) {
                    Call.State fgState = Call.State.IDLE;
                    Call.State bgState = Call.State.IDLE;
                    this.mClirMode = clirMode2;
                    synchronized (this.mSyncHold) {
                        if (holdBeforeDial) {
                            try {
                                fgState = this.mForegroundCall.getState();
                                bgState = this.mBackgroundCall.getState();
                                if (fgState == Call.State.ACTIVE) {
                                    throw new CallStateException("cannot dial in current state");
                                } else if (bgState == Call.State.HOLDING) {
                                    holdBeforeDial = false;
                                }
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                        boolean holdBeforeDial2 = holdBeforeDial;
                        try {
                            ImsPhoneConnection imsPhoneConnection2 = r1;
                            ImsPhoneConnection imsPhoneConnection3 = new ImsPhoneConnection((Phone) this.mPhone, checkForTestEmergencyNumber(dialString), this, this.mForegroundCall, isEmergencyNumber);
                            this.mPendingMO = imsPhoneConnection2;
                            this.mPendingMO.setVideoState(videoState2);
                            if (imsDialArgs.rttTextStream != null) {
                                log("dial: setting RTT stream on mPendingMO");
                                this.mPendingMO.setCurrentRttTextStream(imsDialArgs.rttTextStream);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            boolean z = holdBeforeDial2;
                            Call.State state = fgState;
                            Call.State state2 = bgState;
                            throw th;
                        }
                    }
                } else {
                    Rlog.i(LOG_TAG, "dial: shouldNumberBePlacedOnIms = false");
                    throw new CallStateException(Phone.CS_FALLBACK);
                }
            } else {
                throw new CallStateException("cannot dial in current state");
            }
        }
        return imsPhoneConnection;
    }

    /* access modifiers changed from: package-private */
    public boolean isImsServiceReady() {
        if (this.mImsManager == null) {
            return false;
        }
        return this.mImsManager.isServiceReady();
    }

    private boolean shouldNumberBePlacedOnIms(boolean isEmergency, String number) {
        try {
            if (this.mImsManager != null) {
                int processCallResult = this.mImsManager.shouldProcessCall(isEmergency, new String[]{number});
                Rlog.i(LOG_TAG, "shouldProcessCall: number: " + Rlog.pii(LOG_TAG, number) + ", result: " + processCallResult);
                switch (processCallResult) {
                    case 0:
                        return true;
                    case 1:
                        Rlog.i(LOG_TAG, "shouldProcessCall: place over CSFB instead.");
                        return false;
                    default:
                        Rlog.w(LOG_TAG, "shouldProcessCall returned unknown result.");
                        return false;
                }
            } else {
                Rlog.w(LOG_TAG, "ImsManager unavailable, shouldProcessCall returning false.");
                return false;
            }
        } catch (ImsException e) {
            Rlog.w(LOG_TAG, "ImsService unavailable, shouldProcessCall returning false.");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void cacheCarrierConfiguration(int subId) {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (carrierConfigManager == null || !SubscriptionController.getInstance().isActiveSubId(subId)) {
            loge("cacheCarrierConfiguration: No carrier config service found or not active subId = " + subId);
            this.mCarrierConfigLoaded = false;
            return;
        }
        PersistableBundle carrierConfig = carrierConfigManager.getConfigForSubId(subId);
        if (carrierConfig == null) {
            loge("cacheCarrierConfiguration: Empty carrier config.");
            this.mCarrierConfigLoaded = false;
            return;
        }
        this.mCarrierConfigLoaded = true;
        updateCarrierConfigCache(carrierConfig);
    }

    @VisibleForTesting
    public void updateCarrierConfigCache(PersistableBundle carrierConfig) {
        Integer fromCode;
        String str;
        this.mAllowEmergencyVideoCalls = carrierConfig.getBoolean("allow_emergency_video_calls_bool");
        this.mTreatDowngradedVideoCallsAsVideoCalls = carrierConfig.getBoolean("treat_downgraded_video_calls_as_video_calls_bool");
        this.mDropVideoCallWhenAnsweringAudioCall = carrierConfig.getBoolean("drop_video_call_when_answering_audio_call_bool");
        this.mAllowAddCallDuringVideoCall = carrierConfig.getBoolean("allow_add_call_during_video_call");
        this.mNotifyVtHandoverToWifiFail = carrierConfig.getBoolean("notify_vt_handover_to_wifi_failure_bool");
        this.mSupportDowngradeVtToAudio = carrierConfig.getBoolean("support_downgrade_vt_to_audio_bool");
        this.mNotifyHandoverVideoFromWifiToLTE = carrierConfig.getBoolean("notify_handover_video_from_wifi_to_lte_bool");
        this.mNotifyHandoverVideoFromLTEToWifi = carrierConfig.getBoolean("notify_handover_video_from_lte_to_wifi_bool");
        this.mIgnoreDataEnabledChangedForVideoCalls = carrierConfig.getBoolean("ignore_data_enabled_changed_for_video_calls");
        this.mIsViLteDataMetered = carrierConfig.getBoolean("vilte_data_is_metered_bool");
        this.mSupportPauseVideo = carrierConfig.getBoolean("support_pause_ims_video_calls_bool");
        this.mAlwaysPlayRemoteHoldTone = carrierConfig.getBoolean("always_play_remote_hold_tone_bool");
        String[] mappings = carrierConfig.getStringArray("ims_reasoninfo_mapping_string_array");
        if (mappings == null || mappings.length <= 0) {
            log("No carrier ImsReasonInfo mappings defined.");
            return;
        }
        for (String mapping : mappings) {
            String[] values = mapping.split(Pattern.quote("|"));
            if (values.length == 3) {
                try {
                    if (values[0].equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                        fromCode = null;
                    } else {
                        fromCode = Integer.valueOf(Integer.parseInt(values[0]));
                    }
                    addReasonCodeRemapping(fromCode, values[1], Integer.valueOf(Integer.parseInt(values[2])));
                    if (("Loaded ImsReasonInfo mapping : fromCode = " + fromCode) == null) {
                        str = "any";
                    } else {
                        str = fromCode + " ; message = " + message + " ; toCode = " + toCode;
                    }
                    log(str);
                } catch (NumberFormatException e) {
                    loge("Invalid ImsReasonInfo mapping found: " + mapping);
                }
            }
        }
    }

    private void handleEcmTimer(int action) {
        this.mPhone.handleTimerInEmergencyCallbackMode(action);
        switch (action) {
            case 0:
            case 1:
                return;
            default:
                log("handleEcmTimer, unsupported action " + action);
                return;
        }
    }

    private void dialInternal(ImsPhoneConnection conn, int clirMode, int videoState, Bundle intentExtras) {
        ImsPhoneConnection imsPhoneConnection = conn;
        Bundle bundle = intentExtras;
        if (imsPhoneConnection != null) {
            if (conn.getAddress() == null || conn.getAddress().length() == 0 || conn.getAddress().indexOf(78) >= 0) {
                int i = clirMode;
                int i2 = videoState;
                imsPhoneConnection.setDisconnectCause(7);
                sendEmptyMessageDelayed(18, 500);
                return;
            }
            setMute(false);
            int serviceType = this.mPhoneNumberUtilsProxy.isEmergencyNumber(conn.getAddress()) ? 2 : 1;
            int callType = ImsCallProfile.getCallTypeFromVideoState(videoState);
            imsPhoneConnection.setVideoState(videoState);
            try {
                String[] callees = {conn.getAddress()};
                ImsCallProfile profile = this.mImsManager.createCallProfile(serviceType, callType);
                try {
                    profile.setCallExtraInt("oir", clirMode);
                    if (bundle != null) {
                        if (bundle.containsKey("android.telecom.extra.CALL_SUBJECT")) {
                            bundle.putString("DisplayText", cleanseInstantLetteringMessage(bundle.getString("android.telecom.extra.CALL_SUBJECT")));
                        }
                        if (conn.hasRttTextStream()) {
                            profile.mMediaProfile.mRttMode = 1;
                        }
                        if (bundle.containsKey("CallPull")) {
                            profile.mCallExtras.putBoolean("CallPull", bundle.getBoolean("CallPull"));
                            int dialogId = bundle.getInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID);
                            imsPhoneConnection.setIsPulledCall(true);
                            imsPhoneConnection.setPulledDialogId(dialogId);
                        }
                        profile.mCallExtras.putBundle("OemCallExtras", bundle);
                    }
                    ImsCall imsCall = this.mImsManager.makeCall(profile, callees, this.mImsCallListener);
                    imsPhoneConnection.setImsCall(imsCall);
                    setMute(false);
                    this.mMetrics.writeOnImsCallStart(this.mPhone.getPhoneId(), imsCall.getSession());
                    setVideoCallProvider(imsPhoneConnection, imsCall);
                    imsPhoneConnection.setAllowAddCallDuringVideoCall(this.mAllowAddCallDuringVideoCall);
                } catch (ImsException e) {
                    e = e;
                    loge("dialInternal : " + e);
                    imsPhoneConnection.setDisconnectCause(36);
                    sendEmptyMessageDelayed(18, 500);
                    retryGetImsService();
                } catch (RemoteException e2) {
                }
            } catch (ImsException e3) {
                e = e3;
                int i3 = clirMode;
                loge("dialInternal : " + e);
                imsPhoneConnection.setDisconnectCause(36);
                sendEmptyMessageDelayed(18, 500);
                retryGetImsService();
            } catch (RemoteException e4) {
                int i4 = clirMode;
            }
        }
    }

    public void acceptCall(int videoState) throws CallStateException {
        log("acceptCall");
        if (!this.mForegroundCall.getState().isAlive() || !this.mBackgroundCall.getState().isAlive()) {
            HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 2, LOG_TAG);
            sendAnswerResultCheckMessage();
            if (this.mRingingCall.getState() == Call.State.WAITING && this.mForegroundCall.getState().isAlive()) {
                setRingingCallMute(false);
                boolean answeringWillDisconnect = false;
                ImsCall activeCall = this.mForegroundCall.getImsCall();
                ImsCall ringingCall = this.mRingingCall.getImsCall();
                if (this.mForegroundCall.hasConnections() && this.mRingingCall.hasConnections()) {
                    answeringWillDisconnect = shouldDisconnectActiveCallOnAnswer(activeCall, ringingCall);
                }
                this.mPendingCallVideoState = videoState;
                if (answeringWillDisconnect) {
                    this.mForegroundCall.hangup();
                    try {
                        ringingCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState));
                    } catch (ImsException e) {
                        throw new CallStateException("cannot accept call");
                    }
                } else {
                    switchWaitingOrHoldingAndActive();
                }
            } else if (this.mRingingCall.getState().isRinging()) {
                log("acceptCall: incoming...");
                setRingingCallMute(false);
                try {
                    ImsCall imsCall = this.mRingingCall.getImsCall();
                    if (imsCall != null) {
                        imsCall.accept(ImsCallProfile.getCallTypeFromVideoState(videoState));
                        this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 2);
                        return;
                    }
                    throw new CallStateException("no valid ims call");
                } catch (ImsException e2) {
                    throw new CallStateException("cannot accept call");
                }
            } else {
                throw new CallStateException("phone not ringing");
            }
        } else {
            throw new CallStateException("cannot accept call");
        }
    }

    public void rejectCall() throws CallStateException {
        log("rejectCall");
        if (this.mRingingCall.getState().isRinging()) {
            hangup(this.mRingingCall);
            return;
        }
        throw new CallStateException("phone not ringing");
    }

    /* access modifiers changed from: private */
    public void switchAfterConferenceSuccess() {
        log("switchAfterConferenceSuccess fg =" + this.mForegroundCall.getState() + ", bg = " + this.mBackgroundCall.getState());
        if (this.mForegroundCall.getState() == Call.State.IDLE && this.mBackgroundCall.getState() == Call.State.HOLDING) {
            log("switchAfterConferenceSuccess");
            this.mForegroundCall.switchWith(this.mBackgroundCall);
        }
    }

    public void switchWaitingOrHoldingAndActive() throws CallStateException {
        log("switchWaitingOrHoldingAndActive");
        if (this.mRingingCall.getState() == Call.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (this.mForegroundCall.getState() == Call.State.ACTIVE) {
            ImsCall imsCall = this.mForegroundCall.getImsCall();
            if (imsCall != null) {
                boolean switchingWithWaitingCall = !this.mBackgroundCall.getState().isAlive() && this.mRingingCall != null && this.mRingingCall.getState() == Call.State.WAITING;
                this.mSwitchingFgAndBgCalls = true;
                if (switchingWithWaitingCall) {
                    for (Connection c : this.mRingingCall.getConnections()) {
                        if (c != null) {
                            this.mCallExpectedToResume.add(((ImsPhoneConnection) c).getImsCall());
                        }
                    }
                } else {
                    for (Connection c2 : this.mBackgroundCall.getConnections()) {
                        if (c2 != null) {
                            this.mCallExpectedToResume.add(((ImsPhoneConnection) c2).getImsCall());
                        }
                    }
                }
                this.mForegroundCall.switchWith(this.mBackgroundCall);
                try {
                    imsCall.hold(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 5);
                    if (this.mCallExpectedToResume.isEmpty()) {
                        log("mCallExpectedToResume is empty");
                        this.mSwitchingFgAndBgCalls = false;
                    }
                } catch (ImsException e) {
                    this.mForegroundCall.switchWith(this.mBackgroundCall);
                    throw new CallStateException(e.getMessage());
                }
            } else {
                throw new CallStateException("no ims call");
            }
        } else if (this.mBackgroundCall.getState() == Call.State.HOLDING) {
            resumeWaitingOrHolding();
        }
    }

    public void conference() {
        long conferenceConnectTime;
        ImsCall fgImsCall = this.mForegroundCall.getImsCall();
        if (fgImsCall == null) {
            log("conference no foreground ims call");
            return;
        }
        ImsCall bgImsCall = this.mBackgroundCall.getImsCall();
        if (bgImsCall == null) {
            log("conference no background ims call");
        } else if (fgImsCall.isCallSessionMergePending()) {
            log("conference: skip; foreground call already in process of merging.");
        } else if (bgImsCall.isCallSessionMergePending()) {
            log("conference: skip; background call already in process of merging.");
        } else {
            long foregroundConnectTime = this.mForegroundCall.getEarliestConnectTime();
            long backgroundConnectTime = this.mBackgroundCall.getEarliestConnectTime();
            if (foregroundConnectTime > 0 && backgroundConnectTime > 0) {
                conferenceConnectTime = Math.min(this.mForegroundCall.getEarliestConnectTime(), this.mBackgroundCall.getEarliestConnectTime());
                log("conference - using connect time = " + conferenceConnectTime);
            } else if (foregroundConnectTime > 0) {
                log("conference - bg call connect time is 0; using fg = " + foregroundConnectTime);
                conferenceConnectTime = foregroundConnectTime;
            } else {
                log("conference - fg call connect time is 0; using bg = " + backgroundConnectTime);
                conferenceConnectTime = backgroundConnectTime;
            }
            String foregroundId = "";
            ImsPhoneConnection foregroundConnection = this.mForegroundCall.getFirstConnection();
            if (foregroundConnection != null) {
                foregroundConnection.setConferenceConnectTime(conferenceConnectTime);
                foregroundConnection.handleMergeStart();
                foregroundId = foregroundConnection.getTelecomCallId();
            }
            String backgroundId = "";
            ImsPhoneConnection backgroundConnection = findConnection(bgImsCall);
            if (backgroundConnection != null) {
                backgroundConnection.handleMergeStart();
                backgroundId = backgroundConnection.getTelecomCallId();
            }
            log("conference: fgCallId=" + foregroundId + ", bgCallId=" + backgroundId);
            try {
                fgImsCall.merge(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState), bgImsCall);
            } catch (ImsException e) {
                log("conference " + e.getMessage());
            }
        }
    }

    public void explicitCallTransfer() {
    }

    public void clearDisconnected() {
        log("clearDisconnected");
        internalClearDisconnected();
        updatePhoneState();
        this.mPhone.notifyPreciseCallStateChanged();
    }

    public boolean canConference() {
        return this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING && !this.mBackgroundCall.isFull() && !this.mForegroundCall.isFull();
    }

    public boolean canDial() {
        String disableCall = SystemProperties.get("ro.telephony.disable-call", "false");
        Call.State fgCallState = this.mForegroundCall.getState();
        Call.State bgCallState = this.mBackgroundCall.getState();
        return this.mPendingMO == null && !this.mRingingCall.isRinging() && !disableCall.equals("true") && (fgCallState == Call.State.IDLE || fgCallState == Call.State.DISCONNECTED || fgCallState == Call.State.ACTIVE) && (bgCallState == Call.State.IDLE || bgCallState == Call.State.DISCONNECTED || bgCallState == Call.State.HOLDING);
    }

    public boolean canTransfer() {
        return this.mForegroundCall.getState() == Call.State.ACTIVE && this.mBackgroundCall.getState() == Call.State.HOLDING;
    }

    private void internalClearDisconnected() {
        this.mRingingCall.clearDisconnected();
        this.mForegroundCall.clearDisconnected();
        this.mBackgroundCall.clearDisconnected();
        this.mHandoverCall.clearDisconnected();
    }

    /* access modifiers changed from: private */
    public void updatePhoneState() {
        Object obj;
        PhoneConstants.State oldState = this.mState;
        boolean isPendingMOIdle = this.mPendingMO == null || !this.mPendingMO.getState().isAlive();
        if (this.mRingingCall.isRinging()) {
            this.mState = PhoneConstants.State.RINGING;
        } else if (!isPendingMOIdle || !this.mForegroundCall.isIdle() || !this.mBackgroundCall.isIdle()) {
            this.mState = PhoneConstants.State.OFFHOOK;
        } else {
            this.mState = PhoneConstants.State.IDLE;
        }
        if (this.mState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallEndedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else if (oldState == PhoneConstants.State.IDLE && oldState != this.mState) {
            this.mVoiceCallStartedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("updatePhoneState pendingMo = ");
        if (this.mPendingMO == null) {
            obj = "null";
        } else {
            obj = this.mPendingMO.getState();
        }
        sb.append(obj);
        sb.append(", fg= ");
        sb.append(this.mForegroundCall.getState());
        sb.append("(");
        sb.append(this.mForegroundCall.getConnections().size());
        sb.append("), bg= ");
        sb.append(this.mBackgroundCall.getState());
        sb.append("(");
        sb.append(this.mBackgroundCall.getConnections().size());
        sb.append(")");
        log(sb.toString());
        log("updatePhoneState oldState=" + oldState + ", newState=" + this.mState);
        if (this.mState != oldState) {
            this.mPhone.notifyPhoneStateChanged();
            this.mMetrics.writePhoneState(this.mPhone.getPhoneId(), this.mState);
            notifyPhoneStateChanged(oldState, this.mState);
        }
    }

    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }

    private void dumpState() {
        log("Phone State:" + this.mState);
        log("Ringing call: " + this.mRingingCall.toString());
        List l = this.mRingingCall.getConnections();
        int s = l.size();
        for (int i = 0; i < s; i++) {
            log(l.get(i).toString());
        }
        log("Foreground call: " + this.mForegroundCall.toString());
        List l2 = this.mForegroundCall.getConnections();
        int s2 = l2.size();
        for (int i2 = 0; i2 < s2; i2++) {
            log(l2.get(i2).toString());
        }
        log("Background call: " + this.mBackgroundCall.toString());
        List l3 = this.mBackgroundCall.getConnections();
        int s3 = l3.size();
        for (int i3 = 0; i3 < s3; i3++) {
            log(l3.get(i3).toString());
        }
    }

    public void setTtyMode(int ttyMode) {
        if (this.mImsManager == null) {
            Log.w(LOG_TAG, "ImsManager is null when setting TTY mode");
            return;
        }
        try {
            this.mImsManager.setTtyMode(ttyMode);
        } catch (ImsException e) {
            loge("setTtyMode : " + e);
            retryGetImsService();
        }
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        if (this.mImsManager == null) {
            this.mPhone.sendErrorResponse(onComplete, getImsManagerIsNullException());
            return;
        }
        try {
            this.mImsManager.setUiTTYMode(this.mPhone.getContext(), uiTtyMode, onComplete);
        } catch (ImsException e) {
            loge("setUITTYMode : " + e);
            this.mPhone.sendErrorResponse(onComplete, e);
            retryGetImsService();
        }
    }

    public void setMute(boolean mute) {
        if (((UserManager) this.mPhone.getContext().getSystemService("user")).getUserRestrictions().getBoolean("no_unmute_microphone", false)) {
            this.mDesiredMute = true;
        } else {
            this.mDesiredMute = mute;
        }
        this.mForegroundCall.setMute(this.mDesiredMute);
    }

    private void setRingingCallMute(boolean mute) {
        if (((UserManager) this.mPhone.getContext().getSystemService("user")).getUserRestrictions().getBoolean("no_unmute_microphone", false)) {
            mute = true;
        }
        this.mRingingCall.setMute(mute);
    }

    public boolean getMute() {
        return this.mDesiredMute;
    }

    public void sendDtmf(char c, Message result) {
        log("sendDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.sendDtmf(c, result);
        }
    }

    public void startDtmf(char c) {
        log("startDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.startDtmf(c);
        } else {
            loge("startDtmf : no foreground call");
        }
    }

    public void stopDtmf() {
        log("stopDtmf");
        ImsCall imscall = this.mForegroundCall.getImsCall();
        if (imscall != null) {
            imscall.stopDtmf();
        } else {
            loge("stopDtmf : no foreground call");
        }
    }

    public void hangup(ImsPhoneConnection conn) throws CallStateException {
        log("hangup connection");
        if (conn.getOwner() != this) {
            throw new CallStateException("ImsPhoneConnection " + conn + "does not belong to ImsPhoneCallTracker " + this);
        } else if (!HuaweiTelephonyConfigs.isHisiPlatform() || HwTelephonyFactory.getHwImsPhoneCallTrackerMgr() == null) {
            hangup(conn.getCall());
        } else {
            HwTelephonyFactory.getHwImsPhoneCallTrackerMgr().hangupHisiImsConnection(conn, this);
        }
    }

    public void hangup(ImsPhoneCall call) throws CallStateException {
        log("hangup call");
        if (call.getConnections().size() != 0) {
            ImsCall imsCall = call.getImsCall();
            boolean rejectCall = false;
            if (call == this.mRingingCall) {
                log("(ringing) hangup incoming");
                rejectCall = true;
            } else if (call == this.mForegroundCall) {
                if (call.isDialingOrAlerting()) {
                    log("(foregnd) hangup dialing or alerting...");
                } else {
                    log("(foregnd) hangup foreground");
                }
            } else if (call == this.mBackgroundCall) {
                log("(backgnd) hangup waiting or background");
            } else {
                throw new CallStateException("ImsPhoneCall " + call + "does not belong to ImsPhoneCallTracker " + this);
            }
            call.onHangupLocal();
            try {
                if (HuaweiTelephonyConfigs.isHisiPlatform() && HwTelephonyFactory.getHwImsPhoneCallTrackerMgr() != null) {
                    HwTelephonyFactory.getHwImsPhoneCallTrackerMgr().hangupHisiImsCall(call, this);
                } else if (imsCall != null) {
                    if (!rejectCall) {
                        imsCall.terminate(501);
                    } else if (this.mCust == null || this.mCust.getRejectCallCause(call) == -1) {
                        imsCall.reject(504);
                    } else {
                        log("rejectCallForCause !!!");
                        this.mCust.rejectCallForCause(imsCall);
                    }
                } else if (this.mPendingMO != null && call == this.mForegroundCall) {
                    this.mPendingMO.update(null, Call.State.DISCONNECTED);
                    this.mPendingMO.onDisconnect();
                    removeConnection(this.mPendingMO);
                    this.mPendingMO = null;
                    updatePhoneState();
                    removeMessages(20);
                }
                this.mPhone.notifyPreciseCallStateChanged();
            } catch (ImsException e) {
                throw new CallStateException(e.getMessage());
            }
        } else {
            throw new CallStateException("no connections");
        }
    }

    /* access modifiers changed from: package-private */
    public void callEndCleanupHandOverCallIfAny() {
        if (this.mHandoverCall.mConnections.size() > 0) {
            log("callEndCleanupHandOverCallIfAny, mHandoverCall.mConnections=" + this.mHandoverCall.mConnections);
            this.mHandoverCall.mConnections.clear();
            this.mConnections.clear();
            this.mState = PhoneConstants.State.IDLE;
        }
    }

    /* access modifiers changed from: package-private */
    public void resumeWaitingOrHolding() throws CallStateException {
        log("resumeWaitingOrHolding");
        try {
            if (this.mForegroundCall.getState().isAlive()) {
                ImsCall imsCall = this.mForegroundCall.getImsCall();
                if (imsCall != null) {
                    imsCall.resume(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall.getSession(), 6);
                }
            } else if (this.mRingingCall.getState() == Call.State.WAITING) {
                ImsCall imsCall2 = this.mRingingCall.getImsCall();
                if (imsCall2 != null) {
                    imsCall2.accept(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall2.getSession(), 2);
                }
            } else {
                ImsCall imsCall3 = this.mBackgroundCall.getImsCall();
                if (imsCall3 != null) {
                    imsCall3.resume(ImsCallProfile.getCallTypeFromVideoState(this.mPendingCallVideoState));
                    this.mMetrics.writeOnImsCommand(this.mPhone.getPhoneId(), imsCall3.getSession(), 6);
                }
            }
        } catch (ImsException e) {
            log("ImsException e = " + e);
        }
    }

    public void sendUSSD(String ussdString, Message response) {
        log("sendUSSD");
        try {
            if (this.mUssdSession != null) {
                this.mUssdSession.sendUssd(ussdString);
                AsyncResult.forMessage(response, null, null);
                response.sendToTarget();
            } else if (this.mImsManager == null) {
                this.mPhone.sendErrorResponse(response, getImsManagerIsNullException());
            } else {
                ImsCallProfile profile = this.mImsManager.createCallProfile(1, 2);
                profile.setCallExtraInt("dialstring", 2);
                this.mUssdSession = this.mImsManager.makeCall(profile, new String[]{ussdString}, this.mImsUssdListener);
            }
        } catch (ImsException e) {
            loge("sendUSSD : " + e);
            this.mPhone.sendErrorResponse(response, e);
            retryGetImsService();
        }
    }

    public void cancelUSSD() {
        if (this.mUssdSession != null) {
            try {
                this.mUssdSession.terminate(501);
            } catch (ImsException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public synchronized ImsPhoneConnection findConnection(ImsCall imsCall) {
        Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
        while (it.hasNext()) {
            ImsPhoneConnection conn = it.next();
            if (conn.getImsCall() == imsCall) {
                return conn;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public synchronized void removeConnection(ImsPhoneConnection conn) {
        this.mConnections.remove(conn);
        if (this.mIsInEmergencyCall) {
            boolean isEmergencyCallInList = false;
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ImsPhoneConnection imsPhoneConnection = it.next();
                if (imsPhoneConnection != null && imsPhoneConnection.isEmergency()) {
                    isEmergencyCallInList = true;
                    break;
                }
            }
            if (!isEmergencyCallInList) {
                this.mIsInEmergencyCall = false;
                this.mPhone.sendEmergencyCallStateChange(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public synchronized void addConnection(ImsPhoneConnection conn) {
        this.mConnections.add(conn);
        if (conn.isEmergency()) {
            this.mIsInEmergencyCall = true;
            this.mPhone.sendEmergencyCallStateChange(true);
        }
    }

    /* access modifiers changed from: private */
    public void processCallStateChange(ImsCall imsCall, Call.State state, int cause) {
        log("processCallStateChange " + imsCall + " state=" + state + " cause=" + cause);
        processCallStateChange(imsCall, state, cause, false);
    }

    /* access modifiers changed from: private */
    public void processCallStateChange(ImsCall imsCall, Call.State state, int cause, boolean ignoreState) {
        log("processCallStateChange state=" + state + " cause=" + cause + " ignoreState=" + ignoreState);
        if (imsCall != null) {
            ImsPhoneConnection conn = findConnection(imsCall);
            if (conn != null) {
                boolean z = false;
                if (state == Call.State.ALERTING && conn.getState() != state) {
                    HwTelephonyFactory.getHwChrServiceManager().reportCallException("Telephony", this.mPhone.getSubId(), 0, LOG_TAG);
                }
                conn.updateMediaCapabilities(imsCall);
                boolean changed = conn.update(imsCall, state);
                conn.updateMediaCapabilities(imsCall);
                if (state == Call.State.DISCONNECTED) {
                    if (conn.onDisconnect(cause) || changed) {
                        z = true;
                    }
                    changed = z;
                    conn.getCall().detach(conn);
                    removeConnection(conn);
                }
                if ((changed || ImsPhoneFactory.isimsAsNormalCon()) && conn.getCall() != this.mHandoverCall) {
                    updatePhoneState();
                    this.mPhone.notifyPreciseCallStateChanged();
                }
            }
        }
    }

    private void maybeSetVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) {
        if (conn.getVideoProvider() == null && imsCall.getCallSession().getVideoCallProvider() != null) {
            try {
                setVideoCallProvider(conn, imsCall);
            } catch (RemoteException e) {
                loge("maybeSetVideoCallProvider: exception " + e);
            }
        }
    }

    @VisibleForTesting
    public void addReasonCodeRemapping(Integer fromCode, String message, Integer toCode) {
        this.mImsReasonCodeMap.put(new Pair(fromCode, message), toCode);
    }

    @VisibleForTesting
    public int maybeRemapReasonCode(ImsReasonInfo reasonInfo) {
        int code = reasonInfo.getCode();
        Pair<Integer, String> toCheck = new Pair<>(Integer.valueOf(code), reasonInfo.getExtraMessage());
        Pair<Integer, String> wildcardToCheck = new Pair<>(null, reasonInfo.getExtraMessage());
        if (this.mImsReasonCodeMap.containsKey(toCheck)) {
            int toCode = this.mImsReasonCodeMap.get(toCheck).intValue();
            log("maybeRemapReasonCode : fromCode = " + reasonInfo.getCode() + " ; message = " + reasonInfo.getExtraMessage() + " ; toCode = " + toCode);
            return toCode;
        } else if (!this.mImsReasonCodeMap.containsKey(wildcardToCheck)) {
            return code;
        } else {
            int toCode2 = this.mImsReasonCodeMap.get(wildcardToCheck).intValue();
            log("maybeRemapReasonCode : fromCode(wildcard) = " + reasonInfo.getCode() + " ; message = " + reasonInfo.getExtraMessage() + " ; toCode = " + toCode2);
            return toCode2;
        }
    }

    @VisibleForTesting
    public int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo, Call.State callState) {
        if (this.mCust != null && this.mCust.isForkedCallLoggingEnabled()) {
            int custCause = this.mCust.getDisconnectCauseFromReasonInfo(reasonInfo);
            if (custCause != 36) {
                return custCause;
            }
        }
        switch (maybeRemapReasonCode(reasonInfo)) {
            case 21:
                return 21;
            case 106:
            case 121:
            case 122:
            case 123:
            case 124:
            case 131:
            case 132:
            case 144:
                return 18;
            case AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE:
                return 45;
            case 111:
                return 17;
            case 112:
            case 505:
                if (callState == Call.State.DIALING) {
                    return 62;
                }
                return 61;
            case 143:
                return 16;
            case 201:
            case 202:
            case 203:
            case 335:
                return 13;
            case 240:
                return 20;
            case 241:
                return 21;
            case 243:
                return 58;
            case 244:
                return 46;
            case 245:
                return 47;
            case 246:
                return 48;
            case LastCallFailCause.RADIO_OFF:
                return 66;
            case LastCallFailCause.OUT_OF_SERVICE:
                return 69;
            case LastCallFailCause.NO_VALID_SIM:
                return 70;
            case LastCallFailCause.RADIO_INTERNAL_ERROR:
                return 67;
            case LastCallFailCause.NETWORK_RESP_TIMEOUT:
                return 68;
            case 321:
            case 331:
            case 340:
            case 361:
            case 362:
                return 12;
            case 332:
                return 12;
            case 333:
            case 352:
            case 354:
                return 9;
            case 337:
            case 341:
                return 8;
            case 338:
                return 4;
            case 363:
                return 63;
            case 364:
                return 64;
            case 501:
                return 3;
            case 510:
                return 2;
            case 1014:
                return 52;
            case 1016:
                return 51;
            case 1100:
                return 1047;
            case 1403:
                return 53;
            case 1404:
                return 1049;
            case 1405:
                return 55;
            case 1406:
                return 54;
            case 1407:
                return 59;
            case 1512:
                return 60;
            case 1514:
                return 71;
            case 1515:
                return 25;
            case 3001:
                return 1048;
            default:
                return 36;
        }
    }

    /* access modifiers changed from: private */
    public int getPreciseDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        return PRECISE_CAUSE_MAP.get(maybeRemapReasonCode(reasonInfo), 65535);
    }

    private boolean isPhoneInEcbMode() {
        return this.mPhone != null && this.mPhone.isInEcm();
    }

    /* access modifiers changed from: private */
    public void dialPendingMO() {
        boolean isPhoneInEcmMode = isPhoneInEcbMode();
        boolean isEmergencyNumber = this.mPendingMO.isEmergency();
        if (!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyNumber)) {
            sendEmptyMessage(20);
        } else {
            sendEmptyMessage(21);
        }
    }

    public ImsUtInterface getUtInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getSupplementaryServiceConfiguration();
        }
        throw getImsManagerIsNullException();
    }

    private void transferHandoverConnections(ImsPhoneCall call) {
        if (call.mConnections != null) {
            Iterator it = call.mConnections.iterator();
            while (it.hasNext()) {
                Connection c = (Connection) it.next();
                c.mPreHandoverState = call.mState;
                log("Connection state before handover is " + c.getStateBeforeHandover());
            }
        }
        if (this.mHandoverCall.mConnections == null) {
            this.mHandoverCall.mConnections = call.mConnections;
        } else {
            this.mHandoverCall.mConnections.addAll(call.mConnections);
        }
        if (this.mHandoverCall.mConnections != null) {
            if (call.getImsCall() != null) {
                call.getImsCall().close();
            }
            Iterator it2 = this.mHandoverCall.mConnections.iterator();
            while (it2.hasNext()) {
                Connection c2 = (Connection) it2.next();
                ((ImsPhoneConnection) c2).changeParent(this.mHandoverCall);
                ((ImsPhoneConnection) c2).releaseWakeLock();
                if (c2.equals(this.mPendingMO)) {
                    log("srvcc mPendingMO == conn");
                    this.mPendingMO = null;
                }
            }
        }
        if (call.getState().isAlive()) {
            log("Call is alive and state is " + call.mState);
            this.mHandoverCall.mState = call.mState;
        }
        call.mConnections.clear();
        call.mState = Call.State.IDLE;
    }

    /* access modifiers changed from: package-private */
    public void notifySrvccState(Call.SrvccState state) {
        log("notifySrvccState state=" + state);
        this.mSrvccState = state;
        if (this.mSrvccState == Call.SrvccState.COMPLETED) {
            transferHandoverConnections(this.mForegroundCall);
            transferHandoverConnections(this.mBackgroundCall);
            transferHandoverConnections(this.mRingingCall);
        }
    }

    public void handleMessage(Message msg) {
        log("handleMessage what=" + msg.what);
        switch (msg.what) {
            case 14:
                if (this.pendingCallInEcm) {
                    dialInternal(this.mPendingMO, this.pendingCallClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                    this.mPendingIntentExtras = null;
                    this.pendingCallInEcm = false;
                }
                this.mPhone.unsetOnEcbModeExitResponse(this);
                break;
            case 18:
                if (this.mPendingMO != null) {
                    this.mPendingMO.onDisconnect();
                    removeConnection(this.mPendingMO);
                    this.mPendingMO = null;
                }
                this.mPendingIntentExtras = null;
                updatePhoneState();
                this.mPhone.notifyPreciseCallStateChanged();
                break;
            case 19:
                try {
                    resumeWaitingOrHolding();
                    break;
                } catch (CallStateException e) {
                    loge("handleMessage EVENT_RESUME_BACKGROUND exception=" + e);
                    break;
                }
            case 20:
                dialInternal(this.mPendingMO, this.mClirMode, this.mPendingCallVideoState, this.mPendingIntentExtras);
                this.mPendingIntentExtras = null;
                break;
            case 21:
                if (this.mPendingMO != null) {
                    try {
                        getEcbmInterface().exitEmergencyCallbackMode();
                        this.mPhone.setOnEcbModeExitResponse(this, 14, null);
                        this.pendingCallClirMode = this.mClirMode;
                        this.pendingCallInEcm = true;
                        break;
                    } catch (ImsException e2) {
                        e2.printStackTrace();
                        this.mPendingMO.setDisconnectCause(36);
                        sendEmptyMessageDelayed(18, 500);
                        break;
                    }
                }
                break;
            case 22:
                AsyncResult ar = (AsyncResult) msg.obj;
                ImsCall call = (ImsCall) ar.userObj;
                Long usage = Long.valueOf(((Long) ar.result).longValue());
                log("VT data usage update. usage = " + usage + ", imsCall = " + call);
                if (usage.longValue() > 0) {
                    updateVtDataUsage(call, usage.longValue());
                    break;
                }
                break;
            case 23:
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.result instanceof Pair) {
                    Pair<Boolean, Integer> p = (Pair) ar2.result;
                    onDataEnabledChanged(((Boolean) p.first).booleanValue(), ((Integer) p.second).intValue());
                    break;
                }
                break;
            case 25:
                if (msg.obj instanceof ImsCall) {
                    ImsCall imsCall = (ImsCall) msg.obj;
                    if (imsCall == this.mForegroundCall.getImsCall()) {
                        if (!imsCall.isWifiCall()) {
                            ImsPhoneConnection conn = findConnection(imsCall);
                            if (conn != null) {
                                Rlog.i(LOG_TAG, "handoverCheck: handover failed.");
                                conn.onHandoverToWifiFailed();
                            }
                            if (conn != null && imsCall.isVideoCall() && conn.getDisconnectCause() == 0) {
                                registerForConnectivityChanges();
                                break;
                            }
                        }
                    } else {
                        Rlog.i(LOG_TAG, "handoverCheck: no longer FG; check skipped.");
                        unregisterForConnectivityChanges();
                        return;
                    }
                }
                break;
            case 26:
                SomeArgs args = (SomeArgs) msg.obj;
                try {
                    handleFeatureCapabilityChanged((ImsFeature.Capabilities) args.arg1);
                    break;
                } finally {
                    args.recycle();
                }
            case 27:
                AsyncResult ar3 = (AsyncResult) msg.obj;
                ImsPhoneMmiCode mmiCode = new ImsPhoneMmiCode(this.mPhone);
                try {
                    mmiCode.setIsSsInfo(true);
                    mmiCode.processImsSsData(ar3);
                    break;
                } catch (ImsException e3) {
                    Rlog.e(LOG_TAG, "Exception in parsing SS Data: " + e3);
                    break;
                }
        }
    }

    private void updateVtDataUsage(ImsCall call, long dataUsage) {
        ImsCall imsCall = call;
        long oldUsage = 0;
        if (this.mVtDataUsageMap.containsKey(Integer.valueOf(imsCall.uniqueId))) {
            oldUsage = this.mVtDataUsageMap.get(Integer.valueOf(imsCall.uniqueId)).longValue();
        }
        long delta = dataUsage - oldUsage;
        this.mVtDataUsageMap.put(Integer.valueOf(imsCall.uniqueId), Long.valueOf(dataUsage));
        log("updateVtDataUsage: call=" + imsCall + ", delta=" + delta);
        long currentTime = SystemClock.elapsedRealtime();
        int isRoaming = this.mPhone.getServiceState().getDataRoaming();
        NetworkStats vtDataUsageSnapshot = new NetworkStats(currentTime, 1);
        vtDataUsageSnapshot.combineAllValues(this.mVtDataUsageSnapshot);
        NetworkStats.Entry entry = r11;
        NetworkStats.Entry entry2 = new NetworkStats.Entry(VT_INTERFACE, -1, 1, 0, 1, (int) isRoaming, 1, delta / 2, 0, delta / 2, 0, 0);
        vtDataUsageSnapshot.combineValues(entry);
        this.mVtDataUsageSnapshot = vtDataUsageSnapshot;
        NetworkStats vtDataUsageUidSnapshot = new NetworkStats(currentTime, 1);
        vtDataUsageUidSnapshot.combineAllValues(this.mVtDataUsageUidSnapshot);
        if (this.mDefaultDialerUid.get() == -1) {
            this.mDefaultDialerUid.set(getPackageUid(this.mPhone.getContext(), ((TelecomManager) this.mPhone.getContext().getSystemService("telecom")).getDefaultDialerPackage()));
        }
        NetworkStats.Entry entry3 = new NetworkStats.Entry(VT_INTERFACE, this.mDefaultDialerUid.get(), 1, 0, 1, (int) isRoaming, 1, delta / 2, 0, delta / 2, 0, 0);
        vtDataUsageUidSnapshot.combineValues(entry3);
        this.mVtDataUsageUidSnapshot = vtDataUsageUidSnapshot;
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + msg);
    }

    /* access modifiers changed from: package-private */
    public void logState() {
        if (VERBOSE_STATE_LOGGING) {
            Rlog.v(LOG_TAG, "Current IMS PhoneCall State:\n" + " Foreground: " + this.mForegroundCall + "\n" + " Background: " + this.mBackgroundCall + "\n" + " Ringing: " + this.mRingingCall + "\n" + " Handover: " + this.mHandoverCall + "\n");
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ImsPhoneCallTracker extends:");
        super.dump(fd, pw, args);
        pw.println(" mVoiceCallEndedRegistrants=" + this.mVoiceCallEndedRegistrants);
        pw.println(" mVoiceCallStartedRegistrants=" + this.mVoiceCallStartedRegistrants);
        pw.println(" mRingingCall=" + this.mRingingCall);
        pw.println(" mForegroundCall=" + this.mForegroundCall);
        pw.println(" mBackgroundCall=" + this.mBackgroundCall);
        pw.println(" mHandoverCall=" + this.mHandoverCall);
        pw.println(" mPendingMO=" + this.mPendingMO);
        pw.println(" mPhone=" + this.mPhone);
        pw.println(" mDesiredMute=" + this.mDesiredMute);
        pw.println(" mState=" + this.mState);
        pw.println(" mMmTelCapabilities=" + this.mMmTelCapabilities);
        pw.println(" mDefaultDialerUid=" + this.mDefaultDialerUid.get());
        pw.println(" mVtDataUsageSnapshot=" + this.mVtDataUsageSnapshot);
        pw.println(" mVtDataUsageUidSnapshot=" + this.mVtDataUsageUidSnapshot);
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        try {
            if (this.mImsManager != null) {
                this.mImsManager.dump(fd, pw, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mConnections != null && this.mConnections.size() > 0) {
            pw.println("mConnections:");
            for (int i = 0; i < this.mConnections.size(); i++) {
                pw.println("  [" + i + "]: " + this.mConnections.get(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handlePollCalls(AsyncResult ar) {
    }

    /* access modifiers changed from: package-private */
    public ImsEcbm getEcbmInterface() throws ImsException {
        if (this.mImsManager != null) {
            return this.mImsManager.getEcbmInterface();
        }
        throw getImsManagerIsNullException();
    }

    /* access modifiers changed from: package-private */
    public ImsMultiEndpoint getMultiEndpointInterface() throws ImsException {
        if (this.mImsManager != null) {
            try {
                return this.mImsManager.getMultiEndpointInterface();
            } catch (ImsException e) {
                if (e.getCode() == 902) {
                    return null;
                }
                throw e;
            }
        } else {
            throw getImsManagerIsNullException();
        }
    }

    public boolean isInEmergencyCall() {
        return this.mIsInEmergencyCall;
    }

    public boolean isVolteEnabled() {
        if (!(getImsRegistrationTech() == 0) || !this.mMmTelCapabilities.isCapable(1)) {
            return false;
        }
        return true;
    }

    public boolean isVowifiEnabled() {
        if (!(getImsRegistrationTech() == 1) || !this.mMmTelCapabilities.isCapable(1)) {
            return false;
        }
        return true;
    }

    public boolean isVideoCallEnabled() {
        return this.mMmTelCapabilities.isCapable(2);
    }

    public PhoneConstants.State getState() {
        return this.mState;
    }

    public int getImsRegistrationTech() {
        if (this.mImsManager != null) {
            return this.mImsManager.getRegistrationTech();
        }
        return -1;
    }

    private void retryGetImsService() {
        if (!this.mImsManager.isServiceAvailable()) {
            this.mImsManagerConnector.connect();
        }
    }

    /* access modifiers changed from: private */
    public void setVideoCallProvider(ImsPhoneConnection conn, ImsCall imsCall) throws RemoteException {
        IImsVideoCallProvider imsVideoCallProvider = imsCall.getCallSession().getVideoCallProvider();
        if (imsVideoCallProvider != null) {
            boolean useVideoPauseWorkaround = this.mPhone.getContext().getResources().getBoolean(17957062);
            ImsVideoCallProviderWrapper imsVideoCallProviderWrapper = new ImsVideoCallProviderWrapper(imsVideoCallProvider);
            if (useVideoPauseWorkaround) {
                imsVideoCallProviderWrapper.setUseVideoPauseWorkaround(useVideoPauseWorkaround);
            }
            conn.setVideoProvider(imsVideoCallProviderWrapper);
            imsVideoCallProviderWrapper.registerForDataUsageUpdate(this, 22, imsCall);
            imsVideoCallProviderWrapper.addImsVideoProviderCallback(conn);
        }
    }

    public boolean isUtEnabled() {
        return this.mMmTelCapabilities.isCapable(4);
    }

    private String cleanseInstantLetteringMessage(String callSubject) {
        if (TextUtils.isEmpty(callSubject)) {
            return callSubject;
        }
        CarrierConfigManager configMgr = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configMgr == null) {
            return callSubject;
        }
        PersistableBundle carrierConfig = configMgr.getConfigForSubId(this.mPhone.getSubId());
        if (carrierConfig == null) {
            return callSubject;
        }
        String invalidCharacters = carrierConfig.getString("carrier_instant_lettering_invalid_chars_string");
        if (!TextUtils.isEmpty(invalidCharacters)) {
            callSubject = callSubject.replaceAll(invalidCharacters, "");
        }
        String escapedCharacters = carrierConfig.getString("carrier_instant_lettering_escaped_chars_string");
        if (!TextUtils.isEmpty(escapedCharacters)) {
            callSubject = escapeChars(escapedCharacters, callSubject);
        }
        return callSubject;
    }

    private String escapeChars(String toEscape, String source) {
        StringBuilder escaped = new StringBuilder();
        for (char c : source.toCharArray()) {
            if (toEscape.contains(Character.toString(c))) {
                escaped.append("\\");
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    public ImsUtInterface getUtInterfaceEx() throws ImsException {
        return getUtInterface();
    }

    public boolean isUtEnabledForQcom() {
        return this.mMmTelCapabilities.isCapable(4) && (this.mCust == null || this.mCust.checkImsRegistered());
    }

    public void pullExternalCall(String number, int videoState, int dialogId) {
        Bundle extras = new Bundle();
        extras.putBoolean("CallPull", true);
        extras.putInt(ImsExternalCallTracker.EXTRA_IMS_EXTERNAL_CALL_ID, dialogId);
        try {
            this.mPhone.notifyUnknownConnection(dial(number, videoState, extras));
        } catch (CallStateException e) {
            loge("pullExternalCall failed - " + e);
        }
    }

    private ImsException getImsManagerIsNullException() {
        return new ImsException("no ims manager", 102);
    }

    /* access modifiers changed from: private */
    public boolean shouldDisconnectActiveCallOnAnswer(ImsCall activeCall, ImsCall incomingCall) {
        boolean isIncomingCallAudio;
        boolean z = false;
        if (activeCall == null || incomingCall == null || !this.mDropVideoCallWhenAnsweringAudioCall) {
            return false;
        }
        boolean isActiveCallVideo = activeCall.isVideoCall() || (this.mTreatDowngradedVideoCallsAsVideoCalls && activeCall.wasVideoCall());
        boolean isActiveCallOnWifi = activeCall.isWifiCall();
        boolean isVoWifiEnabled = this.mImsManager.isWfcEnabledByPlatform() && this.mImsManager.isWfcEnabledByUser();
        log("shouldDisconnectActiveCallOnAnswer : isActiveCallVideo=" + isActiveCallVideo + " isActiveCallOnWifi=" + isActiveCallOnWifi + " isIncomingCallAudio=" + isIncomingCallAudio + " isVowifiEnabled=" + isVoWifiEnabled);
        if (isActiveCallVideo && isActiveCallOnWifi && isIncomingCallAudio && !isVoWifiEnabled) {
            z = true;
        }
        return z;
    }

    public NetworkStats getVtDataUsage(boolean perUidStats) {
        if (this.mState != PhoneConstants.State.IDLE) {
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (it.hasNext()) {
                Connection.VideoProvider videoProvider = it.next().getVideoProvider();
                if (videoProvider != null) {
                    videoProvider.onRequestConnectionDataUsage();
                }
            }
        }
        return perUidStats ? this.mVtDataUsageUidSnapshot : this.mVtDataUsageSnapshot;
    }

    public void registerPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.add(listener);
    }

    public void unregisterPhoneStateListener(PhoneStateListener listener) {
        this.mPhoneStateListeners.remove(listener);
    }

    private void notifyPhoneStateChanged(PhoneConstants.State oldState, PhoneConstants.State newState) {
        for (PhoneStateListener listener : this.mPhoneStateListeners) {
            listener.onPhoneStateChanged(oldState, newState);
        }
    }

    private void modifyVideoCall(ImsCall imsCall, int newVideoState) {
        ImsPhoneConnection conn = findConnection(imsCall);
        if (conn != null) {
            int oldVideoState = conn.getVideoState();
            if (conn.getVideoProvider() != null) {
                conn.getVideoProvider().onSendSessionModifyRequest(new VideoProfile(oldVideoState), new VideoProfile(newVideoState));
            }
        }
    }

    private void onDataEnabledChanged(boolean enabled, int reason) {
        int reasonCode;
        log("onDataEnabledChanged: enabled=" + enabled + ", reason=" + reason);
        this.mIsDataEnabled = enabled;
        if (!this.mIsViLteDataMetered) {
            StringBuilder sb = new StringBuilder();
            sb.append("Ignore data ");
            sb.append(enabled ? "enabled" : "disabled");
            sb.append(" - carrier policy indicates that data is not metered for ViLTE calls.");
            log(sb.toString());
            return;
        }
        Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
        while (it.hasNext()) {
            it.next().handleDataEnabledChange(enabled);
        }
        if (reason == 3) {
            reasonCode = 1405;
        } else if (reason == 2) {
            reasonCode = 1406;
        } else {
            reasonCode = 1406;
        }
        maybeNotifyDataDisabled(enabled, reasonCode);
        handleDataEnabledChange(enabled, reasonCode);
        if (!this.mShouldUpdateImsConfigOnDisconnect && reason != 0 && this.mCarrierConfigLoaded) {
            HwFrameworkFactory.updateImsServiceConfig(this.mPhone.getContext(), this.mPhone.getPhoneId(), true);
        }
    }

    private void maybeNotifyDataDisabled(boolean enabled, int reasonCode) {
        if (!enabled) {
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (it.hasNext()) {
                ImsPhoneConnection conn = it.next();
                ImsCall imsCall = conn.getImsCall();
                if (imsCall != null && imsCall.isVideoCall() && !imsCall.isWifiCall() && conn.hasCapabilities(3)) {
                    if (reasonCode == 1406) {
                        conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_DISABLED", null);
                    } else if (reasonCode == 1405) {
                        conn.onConnectionEvent("android.telephony.event.EVENT_DOWNGRADE_DATA_LIMIT_REACHED", null);
                    }
                }
            }
        }
    }

    private void handleDataEnabledChange(boolean enabled, int reasonCode) {
        if (!enabled) {
            Iterator<ImsPhoneConnection> it = this.mConnections.iterator();
            while (it.hasNext()) {
                ImsPhoneConnection conn = it.next();
                ImsCall imsCall = conn.getImsCall();
                if (imsCall != null && imsCall.isVideoCall() && !imsCall.isWifiCall()) {
                    log("handleDataEnabledChange - downgrading " + conn);
                    downgradeVideoCall(reasonCode, conn);
                }
            }
        } else if (this.mSupportPauseVideo) {
            Iterator<ImsPhoneConnection> it2 = this.mConnections.iterator();
            while (it2.hasNext()) {
                ImsPhoneConnection conn2 = it2.next();
                log("handleDataEnabledChange - resuming " + conn2);
                if (VideoProfile.isPaused(conn2.getVideoState()) && conn2.wasVideoPausedFromSource(2)) {
                    conn2.resumeVideo(2);
                }
            }
            this.mShouldUpdateImsConfigOnDisconnect = false;
        }
    }

    /* access modifiers changed from: private */
    public void downgradeVideoCall(int reasonCode, ImsPhoneConnection conn) {
        ImsCall imsCall = conn.getImsCall();
        if (imsCall == null) {
            return;
        }
        if (conn.hasCapabilities(3)) {
            modifyVideoCall(imsCall, 0);
        } else if (!this.mSupportPauseVideo || reasonCode == 1407) {
            try {
                imsCall.terminate(501, reasonCode);
            } catch (ImsException e) {
                loge("Couldn't terminate call " + imsCall);
            }
        } else {
            this.mShouldUpdateImsConfigOnDisconnect = true;
            conn.pauseVideo(2);
        }
    }

    private void resetImsCapabilities() {
        log("Resetting Capabilities...");
        this.mMmTelCapabilities = new MmTelFeature.MmTelCapabilities();
    }

    /* access modifiers changed from: private */
    public boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        boolean z = false;
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) {
                if (ni.getType() == 1) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void registerForConnectivityChanges() {
        if (!this.mIsMonitoringConnectivity && this.mNotifyVtHandoverToWifiFail) {
            ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
            if (cm != null) {
                Rlog.i(LOG_TAG, "registerForConnectivityChanges");
                NetworkCapabilities capabilities = new NetworkCapabilities();
                capabilities.addTransportType(1);
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                builder.setCapabilities(capabilities);
                cm.registerNetworkCallback(builder.build(), this.mNetworkCallback);
                this.mIsMonitoringConnectivity = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public void unregisterForConnectivityChanges() {
        if (this.mIsMonitoringConnectivity && this.mNotifyVtHandoverToWifiFail) {
            ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
            if (cm != null) {
                Rlog.i(LOG_TAG, "unregisterForConnectivityChanges");
                cm.unregisterNetworkCallback(this.mNetworkCallback);
                this.mIsMonitoringConnectivity = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void scheduleHandoverCheck() {
        ImsCall fgCall = this.mForegroundCall.getImsCall();
        ImsPhoneConnection conn = this.mForegroundCall.getFirstConnection();
        if (this.mNotifyVtHandoverToWifiFail && fgCall != null && fgCall.isVideoCall() && conn != null && conn.getDisconnectCause() == 0 && !hasMessages(25)) {
            Rlog.i(LOG_TAG, "scheduleHandoverCheck: schedule");
            sendMessageDelayed(obtainMessage(25, fgCall), 60000);
        }
    }

    public boolean isCarrierDowngradeOfVtCallSupported() {
        return this.mSupportDowngradeVtToAudio;
    }

    @VisibleForTesting
    public void setDataEnabled(boolean isDataEnabled) {
        this.mIsDataEnabled = isDataEnabled;
    }

    private void handleFeatureCapabilityChanged(ImsFeature.Capabilities capabilities) {
        StringBuilder sb = new StringBuilder(120);
        sb.append("handleFeatureCapabilityChanged: ");
        sb.append(capabilities);
        log(sb.toString());
        this.mMmTelCapabilities = new MmTelFeature.MmTelCapabilities(capabilities);
        this.mPhone.notifyForVideoCapabilityChanged(isVideoCallEnabled());
        log("handleFeatureCapabilityChanged: isVolteEnabled=" + isVolteEnabled() + ", isVideoCallEnabled=" + isVideoCallEnabled() + ", isVowifiEnabled=" + isVowifiEnabled() + ", isUtEnabled=" + isUtEnabled());
        this.mPhone.onFeatureCapabilityChanged();
        this.mMetrics.writeOnImsCapabilities(this.mPhone.getPhoneId(), getImsRegistrationTech(), this.mMmTelCapabilities);
    }

    public void disableUTForQcom() {
        log("disableUTForQcom");
        this.mMmTelCapabilities.removeCapabilities(4);
    }

    public void enableUTForQcom() {
        log("enableUTForQcom");
        this.mMmTelCapabilities.addCapabilities(4);
    }

    public void markCallRejectCause(String telecomCallId, int cause) {
        log("markCallRejectByUser, telecomCallId: " + telecomCallId + ", cause:" + cause);
        if (this.mCust == null) {
            log("mCust is null!");
        } else {
            this.mCust.markCallRejectCause(telecomCallId, cause);
        }
    }

    @VisibleForTesting
    public void onCallHoldReceived(ImsCall imsCall) {
        log("onCallHoldReceived");
        ImsPhoneConnection conn = findConnection(imsCall);
        if (conn != null) {
            if (!this.mOnHoldToneStarted && ((ImsPhoneCall.isLocalTone(imsCall) || this.mAlwaysPlayRemoteHoldTone) && conn.getState() == Call.State.ACTIVE)) {
                this.mPhone.startOnHoldTone(conn);
                this.mOnHoldToneStarted = true;
                this.mOnHoldToneId = System.identityHashCode(conn);
            }
            conn.onConnectionEvent("android.telecom.event.CALL_REMOTELY_HELD", null);
            if (this.mPhone.getContext().getResources().getBoolean(17957062) && this.mSupportPauseVideo && VideoProfile.isVideo(conn.getVideoState())) {
                conn.changeToPausedState();
            }
        }
        SuppServiceNotification supp = new SuppServiceNotification();
        supp.notificationType = 1;
        supp.code = 2;
        if (imsCall.isOnHold()) {
            supp.type = 1;
        } else {
            supp.type = 0;
        }
        log("onCallHoldReceived supp.type:" + supp.type);
        this.mPhone.notifySuppSvcNotification(supp);
        this.mMetrics.writeOnImsCallHoldReceived(this.mPhone.getPhoneId(), imsCall.getCallSession());
    }

    @VisibleForTesting
    public void setAlwaysPlayRemoteHoldTone(boolean shouldPlayRemoteHoldTone) {
        this.mAlwaysPlayRemoteHoldTone = shouldPlayRemoteHoldTone;
    }

    public int getPhoneType() {
        log("getPhoneType getPhoneType: " + this.mPhone.getPhoneType());
        return this.mPhone.getPhoneType();
    }

    public boolean isVolteEnabledByPlatform() {
        ImsManager imsManager = ImsManager.getInstance(this.mPhone.getContext(), this.mPhone.getPhoneId());
        if (imsManager == null) {
            log("isVolteEnabledByPlatform imsManager == null ");
            return false;
        }
        log("isVolteEnabledByPlatform isVolteEnabledByPlatform: " + imsManager.isVolteEnabledByPlatform());
        return imsManager.isVolteEnabledByPlatform();
    }

    public ImsPhoneConnection getImsPhoneConnection() {
        return this.mPendingMO;
    }

    public HwCustImsPhoneCallTracker getHwCustImsPhoneCallTracker() {
        return this.mCust;
    }

    public void removeConnectionHw(ImsPhoneConnection conn) {
        removeConnection(conn);
    }

    public void updatePhoneStateHw() {
        updatePhoneState();
    }

    public void removeMessagesHw(int event) {
        super.removeMessages(event);
    }

    public ImsPhone getImsPhone() {
        return this.mPhone;
    }

    public void logHw(String msg) {
        log(msg);
    }

    public int getEventPendingMo() {
        return 20;
    }

    public ImsPhoneCallTracker getImsPhoneCallTracker() {
        return this;
    }
}
