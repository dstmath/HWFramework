package com.android.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.radio.V1_2.ScanIntervalRange;
import android.net.KeepalivePacketData;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.data.DataCallResponse;
import android.telephony.data.DataProfile;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.CarrierSignalAgent;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwDataConnectionManager;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.LinkCapacityEstimate;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.google.android.mms.pdu.CharacterSets;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class DataConnection extends StateMachine {
    static final int BASE = 262144;
    private static final int CMD_TO_STRING_COUNT = 26;
    private static final boolean DBG = true;
    private static final int EHRPD_MAX_RETRY = 2;
    static final int EVENT_BW_REFRESH_RESPONSE = 262158;
    static final int EVENT_CLEAR_LINK = 262168;
    static final int EVENT_CONNECT = 262144;
    static final int EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED = 262155;
    static final int EVENT_DATA_CONNECTION_OVERRIDE_CHANGED = 262161;
    static final int EVENT_DATA_CONNECTION_ROAM_OFF = 262157;
    static final int EVENT_DATA_CONNECTION_ROAM_ON = 262156;
    static final int EVENT_DATA_CONNECTION_VOICE_CALL_ENDED = 262160;
    static final int EVENT_DATA_CONNECTION_VOICE_CALL_STARTED = 262159;
    static final int EVENT_DATA_STATE_CHANGED = 262151;
    static final int EVENT_DEACTIVATE_DONE = 262147;
    static final int EVENT_DISCONNECT = 262148;
    static final int EVENT_DISCONNECT_ALL = 262150;
    static final int EVENT_KEEPALIVE_STARTED = 262163;
    static final int EVENT_KEEPALIVE_START_REQUEST = 262165;
    static final int EVENT_KEEPALIVE_STATUS = 262162;
    static final int EVENT_KEEPALIVE_STOPPED = 262164;
    static final int EVENT_KEEPALIVE_STOP_REQUEST = 262166;
    static final int EVENT_LINK_CAPACITY_CHANGED = 262167;
    static final int EVENT_LOST_CONNECTION = 262153;
    static final int EVENT_RESUME_LINK = 262169;
    static final int EVENT_RIL_CONNECTED = 262149;
    static final int EVENT_SETUP_DATA_CONNECTION_DONE = 262145;
    static final int EVENT_TEAR_DOWN_NOW = 262152;
    /* access modifiers changed from: private */
    public static boolean HW_SET_EHRPD_DATA = SystemProperties.getBoolean("ro.config.hwpp_set_ehrpd_data", false);
    private static final int INITIAL_SCORE = 50;
    private static final String NETWORK_TYPE = "MOBILE";
    private static final int NO_DEFAULT_INITIAL_SCORE = 49;
    private static final String NULL_IP = "0.0.0.0";
    private static final String TCP_BUFFER_SIZES_1XRTT = "16384,32768,131072,4096,16384,102400";
    private static final String TCP_BUFFER_SIZES_EDGE = "4093,26280,70800,4096,16384,70800";
    private static final String TCP_BUFFER_SIZES_EHRPD = "131072,262144,1048576,4096,16384,524288";
    private static final String TCP_BUFFER_SIZES_EVDO = "4094,87380,262144,4096,16384,262144";
    private static final String TCP_BUFFER_SIZES_GPRS = "4092,8760,48000,4096,8760,48000";
    private static final String TCP_BUFFER_SIZES_HSDPA = "61167,367002,1101005,8738,52429,262114";
    private static final String TCP_BUFFER_SIZES_HSPA = "40778,244668,734003,16777,100663,301990";
    private static final String TCP_BUFFER_SIZES_HSPAP = "122334,734003,2202010,32040,192239,576717";
    private static final String TCP_BUFFER_SIZES_LTE = "524288,4194304,8388608,262144,524288,1048576";
    private static final String TCP_BUFFER_SIZES_UMTS = "58254,349525,1048576,58254,349525,1048576";
    private static final boolean VDBG = true;
    private static AtomicInteger mInstanceNumber = new AtomicInteger(0);
    private static String[] sCmdToString = new String[26];
    /* access modifiers changed from: private */
    public boolean keepNetwork = false;
    /* access modifiers changed from: private */
    public AsyncChannel mAc;
    /* access modifiers changed from: private */
    public DcActivatingState mActivatingState = new DcActivatingState();
    /* access modifiers changed from: private */
    public DcActiveState mActiveState = new DcActiveState();
    public HashMap<ApnContext, ConnectionParams> mApnContexts = null;
    /* access modifiers changed from: private */
    public ApnSetting mApnSetting;
    public int mCid;
    /* access modifiers changed from: private */
    public ConnectionParams mConnectionParams;
    private long mCreateTime;
    /* access modifiers changed from: private */
    public int mDataRegState = KeepaliveStatus.INVALID_HANDLE;
    /* access modifiers changed from: private */
    public DataServiceManager mDataServiceManager;
    /* access modifiers changed from: private */
    public DcController mDcController;
    /* access modifiers changed from: private */
    public DcFailCause mDcFailCause;
    /* access modifiers changed from: private */
    public DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    /* access modifiers changed from: private */
    public DcTracker mDct = null;
    private DcDefaultState mDefaultState = new DcDefaultState();
    /* access modifiers changed from: private */
    public DisconnectParams mDisconnectParams;
    /* access modifiers changed from: private */
    public DcDisconnectionErrorCreatingConnection mDisconnectingErrorCreatingConnection = new DcDisconnectionErrorCreatingConnection();
    /* access modifiers changed from: private */
    public DcDisconnectingState mDisconnectingState = new DcDisconnectingState();
    /* access modifiers changed from: private */
    public int mEhrpdFailCount = 0;
    /* access modifiers changed from: private */
    public HwCustDataConnection mHwCustDataConnection;
    private HwDataConnectionManager mHwDataConnectionManager = HwTelephonyFactory.getHwDataConnectionManager();
    /* access modifiers changed from: private */
    public int mId;
    /* access modifiers changed from: private */
    public DcInactiveState mInactiveState = new DcInactiveState();
    /* access modifiers changed from: private */
    public DcFailCause mLastFailCause;
    private long mLastFailTime;
    /* access modifiers changed from: private */
    public LinkProperties mLastLinkProperties = null;
    /* access modifiers changed from: private */
    public LinkProperties mLinkProperties = new LinkProperties();
    /* access modifiers changed from: private */
    public LocalLog mNetCapsLocalLog = new LocalLog(50);
    /* access modifiers changed from: private */
    public DcNetworkAgent mNetworkAgent;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo;
    protected String[] mPcscfAddr;
    /* access modifiers changed from: private */
    public Phone mPhone;
    PendingIntent mReconnectIntent = null;
    /* access modifiers changed from: private */
    public boolean mRestrictedNetworkOverride = false;
    /* access modifiers changed from: private */
    public int mRilRat = KeepaliveStatus.INVALID_HANDLE;
    private int mSubscriptionOverride;
    int mTag;
    /* access modifiers changed from: private */
    public Object mUserData;
    /* access modifiers changed from: private */
    public boolean misLastFailed = false;

    public static class ConnectionParams {
        ApnContext mApnContext;
        final int mConnectionGeneration;
        Message mOnCompletedMsg;
        int mProfileId;
        int mRilRat;
        int mTag;
        final boolean mUnmeteredUseOnly;
        boolean mdefered = false;

        ConnectionParams(ApnContext apnContext, int profileId, int rilRadioTechnology, boolean unmeteredUseOnly, Message onCompletedMsg, int connectionGeneration) {
            this.mApnContext = apnContext;
            this.mProfileId = profileId;
            this.mRilRat = rilRadioTechnology;
            this.mUnmeteredUseOnly = unmeteredUseOnly;
            this.mOnCompletedMsg = onCompletedMsg;
            this.mConnectionGeneration = connectionGeneration;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mProfileId=" + this.mProfileId + " mRat=" + this.mRilRat + " mUnmeteredUseOnly=" + this.mUnmeteredUseOnly + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    private class DcActivatingState extends State {
        private DcActivatingState() {
        }

        public void enter() {
            boolean z;
            int phoneId = DataConnection.this.mPhone.getPhoneId();
            int access$1100 = DataConnection.this.mId;
            long j = DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.typesBitmap : 0;
            if (DataConnection.this.mApnSetting != null) {
                z = DataConnection.this.mApnSetting.canHandleType("default");
            } else {
                z = false;
            }
            StatsLog.write(75, 2, phoneId, access$1100, j, z);
            for (ApnContext apnContext : DataConnection.this.mApnContexts.keySet()) {
                if (apnContext.getState() == DctConstants.State.RETRYING) {
                    DataConnection.this.log("DcActivatingState: Set Retrying To Connecting!");
                    apnContext.setState(DctConstants.State.CONNECTING);
                }
            }
        }

        public boolean processMessage(Message msg) {
            DataConnection.this.log("DcActivatingState: msg=" + DataConnection.msgToString(msg));
            int i = msg.what;
            boolean retVal = false;
            if (i != DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED) {
                switch (i) {
                    case InboundSmsTracker.DEST_PORT_FLAG_3GPP2:
                        ((ConnectionParams) msg.obj).mdefered = true;
                        DataConnection.this.deferMessage(msg);
                        retVal = true;
                        break;
                    case DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE /*262145*/:
                        ConnectionParams cp = (ConnectionParams) msg.obj;
                        DataCallResponse dataCallResponse = msg.getData().getParcelable("data_call_response");
                        SetupResult result = DataConnection.this.onSetupConnectionCompleted(msg.arg1, dataCallResponse, cp);
                        if (!(result == SetupResult.ERROR_STALE || DataConnection.this.mConnectionParams == cp)) {
                            DataConnection.this.loge("DcActivatingState: WEIRD mConnectionsParams:" + DataConnection.this.mConnectionParams + " != cp:" + cp);
                        }
                        DataConnection.this.log("DcActivatingState onSetupConnectionCompleted result=" + result);
                        if (cp.mApnContext != null) {
                            cp.mApnContext.requestLog("onSetupConnectionCompleted result=" + result);
                        }
                        if (!(result == SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR && DataConnection.this.mRilRat == 13)) {
                            int unused = DataConnection.this.mEhrpdFailCount = 0;
                        }
                        switch (result) {
                            case SUCCESS:
                                DcFailCause unused2 = DataConnection.this.mDcFailCause = DcFailCause.NONE;
                                DataConnection.this.transitionTo(DataConnection.this.mActiveState);
                                break;
                            case ERROR_RADIO_NOT_AVAILABLE:
                                DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                                DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                                break;
                            case ERROR_INVALID_ARG:
                                DataConnection.this.tearDownData(cp);
                                DataConnection.this.transitionTo(DataConnection.this.mDisconnectingErrorCreatingConnection);
                                break;
                            case ERROR_DATA_SERVICE_SPECIFIC_ERROR:
                                cp.mApnContext.setModemSuggestedDelay(DataConnection.this.getSuggestedRetryDelay(dataCallResponse));
                                String str = "DcActivatingState: ERROR_DATA_SERVICE_SPECIFIC_ERROR  delay=" + delay + " result=" + result + " result.isRestartRadioFail=" + result.mFailCause.isRestartRadioFail(DataConnection.this.mPhone.getContext(), DataConnection.this.mPhone.getSubId()) + " isPermanentFailure=" + DataConnection.this.mDct.isPermanentFailure(result.mFailCause);
                                DataConnection.this.log(str);
                                if (cp.mApnContext != null) {
                                    cp.mApnContext.requestLog(str);
                                }
                                DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                                DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                                if (DataConnection.HW_SET_EHRPD_DATA && DataConnection.this.mDct.isCTSimCard(DataConnection.this.mPhone.getPhoneId()) && DataConnection.this.mRilRat == 13 && !result.mFailCause.isRestartRadioFail(DataConnection.this.mPhone.getContext(), DataConnection.this.mPhone.getSubId())) {
                                    String apnContextType = cp.mApnContext.getApnType();
                                    if ("default".equals(apnContextType) || "mms".equals(apnContextType)) {
                                        if (DataConnection.this.mEhrpdFailCount < 2 && !result.mFailCause.isPermanentFailure(DataConnection.this.mPhone.getContext(), DataConnection.this.mPhone.getSubId())) {
                                            int unused3 = DataConnection.this.mEhrpdFailCount = DataConnection.this.mEhrpdFailCount + 1;
                                            break;
                                        } else {
                                            DataConnection.this.mPhone.mCi.setEhrpdByQMI(false);
                                            int unused4 = DataConnection.this.mEhrpdFailCount = 0;
                                            DataConnection.this.logd("ehrpd fail times reaches EHRPD_MAX_RETRY or permanent fail ,disable eHRPD.");
                                            break;
                                        }
                                    }
                                }
                                break;
                            case ERROR_STALE:
                                DataConnection.this.loge("DcActivatingState: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + cp.mTag + " != mTag:" + DataConnection.this.mTag);
                                break;
                            default:
                                throw new RuntimeException("Unknown SetupResult, should not happen");
                        }
                        retVal = true;
                        break;
                    default:
                        DataConnection.this.log("DcActivatingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
                        break;
                }
            } else {
                DataConnection.this.deferMessage(msg);
                retVal = true;
            }
            return retVal;
        }
    }

    private class DcActiveState extends State {
        private DcActiveState() {
        }

        public void enter() {
            boolean z;
            DataConnection.this.log("DcActiveState: enter dc=*");
            int phoneId = DataConnection.this.mPhone.getPhoneId();
            int access$1100 = DataConnection.this.mId;
            long j = DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.typesBitmap : 0;
            if (DataConnection.this.mApnSetting != null) {
                z = DataConnection.this.mApnSetting.canHandleType("default");
            } else {
                z = false;
            }
            StatsLog.write(75, 3, phoneId, access$1100, j, z);
            DataConnection.this.updateNetworkInfo();
            DataConnection.this.notifyAllOfConnected(PhoneInternalInterface.REASON_CONNECTED);
            if (DataConnection.this.mPhone.getCallTracker() != null) {
                DataConnection.this.mPhone.getCallTracker().registerForVoiceCallStarted(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED, null);
                DataConnection.this.mPhone.getCallTracker().registerForVoiceCallEnded(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED, null);
            }
            DataConnection.this.mDcController.addActiveDcByCid(DataConnection.this);
            DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, DataConnection.this.mNetworkInfo.getReason(), null);
            DataConnection.this.mNetworkInfo.setExtraInfo(DataConnection.this.mApnSetting.apn);
            DataConnection.this.updateTcpBufferSizes(DataConnection.this.mRilRat);
            NetworkMisc misc = new NetworkMisc();
            if (DataConnection.this.mPhone.getCarrierSignalAgent().hasRegisteredReceivers("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED")) {
                misc.provisioningNotificationDisabled = true;
            }
            misc.subscriberId = DataConnection.this.mPhone.getSubscriberId();
            DataConnection.this.setNetworkRestriction();
            DataConnection dataConnection = DataConnection.this;
            dataConnection.log("mRestrictedNetworkOverride = " + DataConnection.this.mRestrictedNetworkOverride);
            int initialScore = 50;
            if (PhoneFactory.IS_QCOM_DUAL_LTE_STACK && PhoneFactory.IS_DUAL_VOLTE_SUPPORTED && DataConnection.this.mConnectionParams != null && !"default".equalsIgnoreCase(DataConnection.this.mConnectionParams.mApnContext.getApnType())) {
                initialScore = 49;
                DataConnection dataConnection2 = DataConnection.this;
                dataConnection2.log("DcActiveState enter request ApnType :" + DataConnection.this.mConnectionParams.mApnContext.getApnType() + ", set initialScore : 49");
            }
            DataConnection dataConnection3 = DataConnection.this;
            DcNetworkAgent dcNetworkAgent = r3;
            DcNetworkAgent dcNetworkAgent2 = new DcNetworkAgent(DataConnection.this, DataConnection.this.getHandler().getLooper(), DataConnection.this.mPhone.getContext(), "DcNetworkAgent", DataConnection.this.mNetworkInfo, DataConnection.this.getNetworkCapabilities(), DataConnection.this.mLinkProperties, initialScore, misc);
            DcNetworkAgent unused = dataConnection3.mNetworkAgent = dcNetworkAgent;
            DataConnection.this.mPhone.mCi.registerForNattKeepaliveStatus(DataConnection.this.getHandler(), DataConnection.EVENT_KEEPALIVE_STATUS, null);
            DataConnection.this.mPhone.mCi.registerForLceInfo(DataConnection.this.getHandler(), DataConnection.EVENT_LINK_CAPACITY_CHANGED, null);
        }

        public void exit() {
            DataConnection dataConnection = DataConnection.this;
            dataConnection.log("DcActiveState: exit dc=" + this);
            String reason = DataConnection.this.mNetworkInfo.getReason();
            if (DataConnection.this.mDcController.isExecutingCarrierChange()) {
                reason = PhoneInternalInterface.REASON_CARRIER_CHANGE;
            } else if (DataConnection.this.mDisconnectParams != null && DataConnection.this.mDisconnectParams.mReason != null) {
                reason = DataConnection.this.mDisconnectParams.mReason;
            } else if (DataConnection.this.mDcFailCause != null) {
                reason = DataConnection.this.mDcFailCause.toString();
            }
            if (DataConnection.this.mPhone.getCallTracker() != null) {
                DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallStarted(DataConnection.this.getHandler());
                DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallEnded(DataConnection.this.getHandler());
            }
            DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, reason, DataConnection.this.mNetworkInfo.getExtraInfo());
            DataConnection.this.mPhone.mCi.unregisterForNattKeepaliveStatus(DataConnection.this.getHandler());
            DataConnection.this.mPhone.mCi.unregisterForLceInfo(DataConnection.this.getHandler());
            if (DataConnection.this.mNetworkAgent != null) {
                DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                DcNetworkAgent unused = DataConnection.this.mNetworkAgent = null;
            }
            boolean unused2 = DataConnection.this.keepNetwork = false;
            LinkProperties unused3 = DataConnection.this.mLastLinkProperties = null;
        }

        public boolean processMessage(Message msg) {
            boolean retVal;
            boolean retVal2;
            Message message = msg;
            int i = message.what;
            if (i == 262144) {
                ConnectionParams cp = (ConnectionParams) message.obj;
                DataConnection.this.mApnContexts.put(cp.mApnContext, cp);
                DataConnection dataConnection = DataConnection.this;
                dataConnection.log("DcActiveState: EVENT_CONNECT cp=" + cp + " dc=" + DataConnection.this);
                if (DataConnection.this.mNetworkAgent != null) {
                    DataConnection.this.mNetworkAgent.sendRematchNetworkAndRequests(DataConnection.this.mNetworkInfo);
                }
                DataConnection.this.notifyConnectCompleted(cp, DcFailCause.NONE, false);
                retVal = true;
            } else if (i == DataConnection.EVENT_DISCONNECT) {
                DisconnectParams dp = (DisconnectParams) message.obj;
                DataConnection.this.log("DcActiveState: EVENT_DISCONNECT dp=*");
                if (DataConnection.this.mApnContexts.containsKey(dp.mApnContext)) {
                    DataConnection dataConnection2 = DataConnection.this;
                    dataConnection2.log("DcActiveState msg.what=EVENT_DISCONNECT RefCount=" + DataConnection.this.mApnContexts.size());
                    if (DataConnection.this.mApnContexts.size() == 1) {
                        DataConnection.this.mApnContexts.clear();
                        DisconnectParams unused = DataConnection.this.mDisconnectParams = dp;
                        ConnectionParams unused2 = DataConnection.this.mConnectionParams = null;
                        dp.mTag = DataConnection.this.mTag;
                        DataConnection.this.tearDownData(dp);
                        DataConnection.this.transitionTo(DataConnection.this.mDisconnectingState);
                    } else {
                        DataConnection.this.mApnContexts.remove(dp.mApnContext);
                        DataConnection.this.notifyDisconnectCompleted(dp, false);
                    }
                } else {
                    DataConnection dataConnection3 = DataConnection.this;
                    dataConnection3.log("DcActiveState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + DataConnection.this);
                    DataConnection.this.notifyDisconnectCompleted(dp, false);
                }
                retVal = true;
            } else if (i == DataConnection.EVENT_DISCONNECT_ALL) {
                DataConnection dataConnection4 = DataConnection.this;
                dataConnection4.log("DcActiveState EVENT_DISCONNECT clearing apn contexts, dc=" + DataConnection.this);
                DisconnectParams dp2 = (DisconnectParams) message.obj;
                DisconnectParams unused3 = DataConnection.this.mDisconnectParams = dp2;
                ConnectionParams unused4 = DataConnection.this.mConnectionParams = null;
                dp2.mTag = DataConnection.this.mTag;
                DataConnection.this.tearDownData(dp2);
                DataConnection.this.transitionTo(DataConnection.this.mDisconnectingState);
                retVal = true;
            } else if (i != DataConnection.EVENT_LOST_CONNECTION) {
                switch (i) {
                    case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /*262156*/:
                    case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /*262157*/:
                    case DataConnection.EVENT_DATA_CONNECTION_OVERRIDE_CHANGED /*262161*/:
                        DataConnection.this.updateNetworkInfo();
                        if (DataConnection.this.mNetworkAgent != null) {
                            DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities());
                            DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                        }
                        retVal = true;
                        break;
                    case DataConnection.EVENT_BW_REFRESH_RESPONSE /*262158*/:
                        AsyncResult ar = (AsyncResult) message.obj;
                        if (ar.exception != null) {
                            DataConnection dataConnection5 = DataConnection.this;
                            dataConnection5.log("EVENT_BW_REFRESH_RESPONSE: error ignoring, e=" + ar.exception);
                        } else {
                            LinkCapacityEstimate lce = (LinkCapacityEstimate) ar.result;
                            NetworkCapabilities nc = DataConnection.this.getNetworkCapabilities();
                            if (DataConnection.this.mPhone.getLceStatus() == 1) {
                                nc.setLinkDownstreamBandwidthKbps(lce.downlinkCapacityKbps);
                                if (DataConnection.this.mNetworkAgent != null) {
                                    DataConnection.this.mNetworkAgent.sendNetworkCapabilities(nc);
                                }
                            }
                        }
                        retVal = true;
                        break;
                    case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED /*262159*/:
                    case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED /*262160*/:
                        DataConnection.this.updateNetworkInfo();
                        DataConnection.this.updateNetworkInfoSuspendState();
                        if (DataConnection.this.mNetworkAgent != null) {
                            DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities());
                            DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                        }
                        retVal = true;
                        break;
                    case DataConnection.EVENT_KEEPALIVE_STATUS /*262162*/:
                        AsyncResult ar2 = (AsyncResult) message.obj;
                        if (ar2.exception != null) {
                            DataConnection dataConnection6 = DataConnection.this;
                            dataConnection6.loge("EVENT_KEEPALIVE_STATUS: error in keepalive, e=" + ar2.exception);
                        }
                        if (ar2.result != null) {
                            DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus((KeepaliveStatus) ar2.result);
                        }
                        retVal = true;
                        break;
                    case DataConnection.EVENT_KEEPALIVE_STARTED /*262163*/:
                        AsyncResult ar3 = (AsyncResult) message.obj;
                        int slot = message.arg1;
                        if (ar3.exception != null || ar3.result == null) {
                            DataConnection dataConnection7 = DataConnection.this;
                            dataConnection7.loge("EVENT_KEEPALIVE_STARTED: error starting keepalive, e=" + ar3.exception);
                            DataConnection.this.mNetworkAgent.onPacketKeepaliveEvent(slot, -31);
                        } else {
                            KeepaliveStatus ks = (KeepaliveStatus) ar3.result;
                            if (ks == null) {
                                DataConnection.this.loge("Null KeepaliveStatus received!");
                            } else {
                                DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStarted(slot, ks);
                            }
                        }
                        retVal = true;
                        break;
                    case DataConnection.EVENT_KEEPALIVE_STOPPED /*262164*/:
                        AsyncResult ar4 = (AsyncResult) message.obj;
                        int handle = message.arg1;
                        int i2 = message.arg2;
                        if (ar4.exception != null) {
                            DataConnection dataConnection8 = DataConnection.this;
                            dataConnection8.loge("EVENT_KEEPALIVE_STOPPED: error stopping keepalive for handle=" + handle + " e=" + ar4.exception);
                            DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus(new KeepaliveStatus(3));
                        } else {
                            DataConnection dataConnection9 = DataConnection.this;
                            dataConnection9.log("Keepalive Stop Requested for handle=" + handle);
                            DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus(new KeepaliveStatus(handle, 1));
                        }
                        retVal2 = true;
                        break;
                    case DataConnection.EVENT_KEEPALIVE_START_REQUEST /*262165*/:
                        KeepalivePacketData pkt = (KeepalivePacketData) message.obj;
                        int slotId = message.arg1;
                        int intervalMillis = message.arg2 * 1000;
                        if (DataConnection.this.mDataServiceManager.getTransportType() == 1) {
                            DataConnection.this.mPhone.mCi.startNattKeepalive(DataConnection.this.mCid, pkt, intervalMillis, DataConnection.this.obtainMessage(DataConnection.EVENT_KEEPALIVE_STARTED, slotId, 0, null));
                        } else if (DataConnection.this.mNetworkAgent != null) {
                            DataConnection.this.mNetworkAgent.onPacketKeepaliveEvent(message.arg1, -20);
                        }
                        retVal2 = true;
                        break;
                    case DataConnection.EVENT_KEEPALIVE_STOP_REQUEST /*262166*/:
                        int slotId2 = message.arg1;
                        int handle2 = DataConnection.this.mNetworkAgent.keepaliveTracker.getHandleForSlot(slotId2);
                        if (handle2 >= 0) {
                            DataConnection dataConnection10 = DataConnection.this;
                            dataConnection10.logd("Stopping keepalive with handle: " + handle2);
                            DataConnection.this.mPhone.mCi.stopNattKeepalive(handle2, DataConnection.this.obtainMessage(DataConnection.EVENT_KEEPALIVE_STOPPED, handle2, slotId2, null));
                            retVal = true;
                            break;
                        } else {
                            DataConnection dataConnection11 = DataConnection.this;
                            dataConnection11.loge("No slot found for stopPacketKeepalive! " + slotId2);
                            retVal = true;
                            break;
                        }
                    case DataConnection.EVENT_LINK_CAPACITY_CHANGED /*262167*/:
                        AsyncResult ar5 = (AsyncResult) message.obj;
                        if (ar5.exception != null) {
                            DataConnection dataConnection12 = DataConnection.this;
                            dataConnection12.loge("EVENT_LINK_CAPACITY_CHANGED e=" + ar5.exception);
                        } else {
                            LinkCapacityEstimate lce2 = (LinkCapacityEstimate) ar5.result;
                            NetworkCapabilities nc2 = DataConnection.this.getNetworkCapabilities();
                            if (lce2.downlinkCapacityKbps != -1) {
                                nc2.setLinkDownstreamBandwidthKbps(lce2.downlinkCapacityKbps);
                            }
                            if (lce2.uplinkCapacityKbps != -1) {
                                nc2.setLinkUpstreamBandwidthKbps(lce2.uplinkCapacityKbps);
                            }
                            if (DataConnection.this.mNetworkAgent != null) {
                                DataConnection.this.mNetworkAgent.sendNetworkCapabilities(nc2);
                            }
                        }
                        retVal = true;
                        break;
                    case DataConnection.EVENT_CLEAR_LINK /*262168*/:
                        DataConnection.this.log("DcActiveState EVENT_CLEAR_LINK");
                        DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, DataConnection.this.mNetworkInfo.getReason(), DataConnection.this.mNetworkInfo.getExtraInfo());
                        if (DataConnection.this.mNetworkAgent != null) {
                            DataConnection.this.log("DcActiveState EVENT_CLEAR_LINK sendNetworkInfo");
                            DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                        }
                        boolean unused5 = DataConnection.this.keepNetwork = true;
                        LinkProperties unused6 = DataConnection.this.mLastLinkProperties = DataConnection.this.mLinkProperties;
                        retVal = true;
                        break;
                    case DataConnection.EVENT_RESUME_LINK /*262169*/:
                        DataConnection.this.log("DcActiveState EVENT_RESUME_LINK");
                        DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, DataConnection.this.mNetworkInfo.getReason(), DataConnection.this.mNetworkInfo.getExtraInfo());
                        if ((DataConnection.this.mNetworkAgent == null || DataConnection.this.mLastLinkProperties == null) ? false : true) {
                            DataConnection.this.log("DcActiveState EVENT_RESUME_LINK sendNetworkInfo");
                            NetworkMisc misc = new NetworkMisc();
                            CarrierSignalAgent carrierSignalAgent = DataConnection.this.mPhone.getCarrierSignalAgent();
                            if (carrierSignalAgent.hasRegisteredReceivers("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED")) {
                                misc.provisioningNotificationDisabled = true;
                            }
                            misc.subscriberId = DataConnection.this.mPhone.getSubscriberId();
                            DataConnection dataConnection13 = DataConnection.this;
                            DcNetworkAgent dcNetworkAgent = r8;
                            CarrierSignalAgent carrierSignalAgent2 = carrierSignalAgent;
                            DcNetworkAgent dcNetworkAgent2 = new DcNetworkAgent(DataConnection.this, DataConnection.this.getHandler().getLooper(), DataConnection.this.mPhone.getContext(), "DcNetworkAgent", DataConnection.this.mNetworkInfo, DataConnection.this.getNetworkCapabilities(), DataConnection.this.mLinkProperties, 50, misc);
                            DcNetworkAgent unused7 = dataConnection13.mNetworkAgent = dcNetworkAgent;
                            LinkProperties unused8 = DataConnection.this.mLastLinkProperties = null;
                        }
                        boolean unused9 = DataConnection.this.keepNetwork = false;
                        retVal = true;
                        break;
                    default:
                        DataConnection dataConnection14 = DataConnection.this;
                        dataConnection14.log("DcActiveState not handled msg.what=" + DataConnection.this.getWhatToString(message.what));
                        retVal = false;
                        break;
                }
                retVal = retVal2;
            } else {
                DataConnection dataConnection15 = DataConnection.this;
                dataConnection15.log("DcActiveState EVENT_LOST_CONNECTION dc=" + DataConnection.this);
                DataConnection.this.mInactiveState.setEnterNotificationParams(DcFailCause.LOST_CONNECTION);
                DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                retVal = true;
            }
            return retVal;
        }
    }

    private class DcDefaultState extends State {
        private DcDefaultState() {
        }

        public void enter() {
            DataConnection.this.log("DcDefaultState: enter");
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOn(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_ON, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOff(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF, null, true);
            DataConnection.this.mDcController.addDc(DataConnection.this);
        }

        public void exit() {
            DataConnection.this.log("DcDefaultState: exit");
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(DataConnection.this.getHandler());
            DataConnection.this.mDcController.removeDc(DataConnection.this);
            if (DataConnection.this.mAc != null) {
                DataConnection.this.mAc.disconnected();
                AsyncChannel unused = DataConnection.this.mAc = null;
            }
            DataConnection.this.mApnContexts = null;
            DataConnection.this.mReconnectIntent = null;
            DcTracker unused2 = DataConnection.this.mDct = null;
            ApnSetting unused3 = DataConnection.this.mApnSetting = null;
            Phone unused4 = DataConnection.this.mPhone = null;
            DataServiceManager unused5 = DataConnection.this.mDataServiceManager = null;
            LinkProperties unused6 = DataConnection.this.mLinkProperties = null;
            DcFailCause unused7 = DataConnection.this.mLastFailCause = null;
            Object unused8 = DataConnection.this.mUserData = null;
            DcController unused9 = DataConnection.this.mDcController = null;
            DcTesterFailBringUpAll unused10 = DataConnection.this.mDcTesterFailBringUpAll = null;
        }

        public boolean processMessage(Message msg) {
            DataConnection dataConnection = DataConnection.this;
            dataConnection.log("DcDefault msg=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
            switch (msg.what) {
                case 69633:
                    if (DataConnection.this.mAc == null) {
                        AsyncChannel unused = DataConnection.this.mAc = new AsyncChannel();
                        DataConnection.this.mAc.connected(null, DataConnection.this.getHandler(), msg.replyTo);
                        DataConnection.this.log("DcDefaultState: FULL_CONNECTION reply connected");
                        DataConnection.this.mAc.replyToMessage(msg, 69634, 0, DataConnection.this.mId, "hi");
                        break;
                    } else {
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.log("Disconnecting to previous connection mAc=" + DataConnection.this.mAc);
                        DataConnection.this.mAc.replyToMessage(msg, 69634, 3);
                        break;
                    }
                case 69636:
                    DataConnection.this.log("DcDefault: CMD_CHANNEL_DISCONNECTED before quiting call dump");
                    DataConnection.this.dumpToLog();
                    DataConnection.this.quit();
                    break;
                case InboundSmsTracker.DEST_PORT_FLAG_3GPP2:
                    DataConnection.this.log("DcDefaultState: msg.what=EVENT_CONNECT, fail not expected");
                    DataConnection.this.notifyConnectCompleted((ConnectionParams) msg.obj, DcFailCause.UNKNOWN, false);
                    break;
                case DataConnection.EVENT_DISCONNECT /*262148*/:
                    DataConnection dataConnection3 = DataConnection.this;
                    dataConnection3.log("DcDefaultState deferring msg.what=EVENT_DISCONNECT RefCount=" + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    break;
                case DataConnection.EVENT_DISCONNECT_ALL /*262150*/:
                    DataConnection dataConnection4 = DataConnection.this;
                    dataConnection4.log("DcDefaultState deferring msg.what=EVENT_DISCONNECT_ALL RefCount=" + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    break;
                case DataConnection.EVENT_TEAR_DOWN_NOW /*262152*/:
                    DataConnection.this.log("DcDefaultState EVENT_TEAR_DOWN_NOW");
                    DataConnection.this.mDataServiceManager.deactivateDataCall(DataConnection.this.mCid, 1, null);
                    break;
                case DataConnection.EVENT_LOST_CONNECTION /*262153*/:
                    DataConnection.this.logAndAddLogRec("DcDefaultState ignore EVENT_LOST_CONNECTION tag=" + msg.arg1 + ":mTag=" + DataConnection.this.mTag);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /*262155*/:
                    Pair<Integer, Integer> drsRatPair = (Pair) ((AsyncResult) msg.obj).result;
                    int unused2 = DataConnection.this.mDataRegState = ((Integer) drsRatPair.first).intValue();
                    if (DataConnection.this.mRilRat != ((Integer) drsRatPair.second).intValue()) {
                        DataConnection.this.updateTcpBufferSizes(((Integer) drsRatPair.second).intValue());
                    }
                    int unused3 = DataConnection.this.mRilRat = ((Integer) drsRatPair.second).intValue();
                    DataConnection dataConnection5 = DataConnection.this;
                    dataConnection5.log("DcDefaultState: EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED drs=" + DataConnection.this.mDataRegState + " mRilRat=" + DataConnection.this.mRilRat);
                    DataConnection.this.updateNetworkInfo();
                    DataConnection.this.updateNetworkInfoSuspendState();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities());
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                        DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties);
                        break;
                    }
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /*262156*/:
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /*262157*/:
                case DataConnection.EVENT_DATA_CONNECTION_OVERRIDE_CHANGED /*262161*/:
                    DataConnection.this.updateNetworkInfo();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities());
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                        break;
                    }
                    break;
                case DataConnection.EVENT_KEEPALIVE_START_REQUEST /*262165*/:
                case DataConnection.EVENT_KEEPALIVE_STOP_REQUEST /*262166*/:
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.onPacketKeepaliveEvent(msg.arg1, -20);
                        break;
                    }
                    break;
                case 266240:
                    boolean val = DataConnection.this.isInactive();
                    DataConnection dataConnection6 = DataConnection.this;
                    dataConnection6.log("REQ_IS_INACTIVE  isInactive=" + val);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_IS_INACTIVE, val);
                    break;
                case DcAsyncChannel.REQ_GET_CID:
                    int cid = DataConnection.this.getCid();
                    DataConnection dataConnection7 = DataConnection.this;
                    dataConnection7.log("REQ_GET_CID  cid=" + cid);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_CID, cid);
                    break;
                case DcAsyncChannel.REQ_GET_APNSETTING:
                    ApnSetting apnSetting = DataConnection.this.getApnSetting();
                    DataConnection dataConnection8 = DataConnection.this;
                    dataConnection8.log("REQ_GET_APNSETTING  mApnSetting=" + apnSetting);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_APNSETTING, apnSetting);
                    break;
                case DcAsyncChannel.REQ_GET_LINK_PROPERTIES:
                    LinkProperties lp = DataConnection.this.getCopyLinkProperties();
                    DataConnection.this.log("REQ_GET_LINK_PROPERTIES linkProperties");
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_LINK_PROPERTIES, lp);
                    break;
                case DcAsyncChannel.REQ_SET_LINK_PROPERTIES_HTTP_PROXY:
                    ProxyInfo proxy = (ProxyInfo) msg.obj;
                    DataConnection dataConnection9 = DataConnection.this;
                    dataConnection9.log("REQ_SET_LINK_PROPERTIES_HTTP_PROXY proxy=" + proxy);
                    DataConnection.this.setLinkPropertiesHttpProxy(proxy);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_SET_LINK_PROPERTIES_HTTP_PROXY);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties);
                        break;
                    }
                    break;
                case DcAsyncChannel.REQ_GET_NETWORK_CAPABILITIES:
                    NetworkCapabilities nc = DataConnection.this.getNetworkCapabilities();
                    DataConnection dataConnection10 = DataConnection.this;
                    dataConnection10.log("REQ_GET_NETWORK_CAPABILITIES networkCapabilities" + nc);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_GET_NETWORK_CAPABILITIES, nc);
                    break;
                case DcAsyncChannel.REQ_RESET:
                    DataConnection.this.log("DcDefaultState: msg.what=REQ_RESET");
                    DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                    break;
                case DcAsyncChannel.REQ_CHECK_APNCONTEXT:
                    boolean val2 = DataConnection.this.checkApnContext((ApnContext) msg.obj);
                    DataConnection dataConnection11 = DataConnection.this;
                    dataConnection11.log("REQ_CHECK_APNCONTEXT  checkApnContext=" + val2);
                    DataConnection.this.mAc.replyToMessage(msg, DcAsyncChannel.RSP_CHECK_APNCONTEXT, val2);
                    break;
                default:
                    DataConnection dataConnection12 = DataConnection.this;
                    dataConnection12.log("DcDefaultState: shouldn't happen but ignore msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    break;
            }
            return true;
        }
    }

    private class DcDisconnectingState extends State {
        private DcDisconnectingState() {
        }

        public void enter() {
            boolean z;
            int phoneId = DataConnection.this.mPhone.getPhoneId();
            int access$1100 = DataConnection.this.mId;
            long j = DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.typesBitmap : 0;
            if (DataConnection.this.mApnSetting != null) {
                z = DataConnection.this.mApnSetting.canHandleType("default");
            } else {
                z = false;
            }
            StatsLog.write(75, 4, phoneId, access$1100, j, z);
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 262144) {
                DataConnection.this.log("DcDisconnectingState msg.what=EVENT_CONNECT. Defer. RefCount = " + DataConnection.this.mApnContexts.size());
                DataConnection.this.deferMessage(msg);
                return true;
            } else if (i != DataConnection.EVENT_DEACTIVATE_DONE) {
                DataConnection.this.log("DcDisconnectingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                return false;
            } else {
                DisconnectParams dp = (DisconnectParams) msg.obj;
                String str = "DcDisconnectingState msg.what=EVENT_DEACTIVATE_DONE RefCount=" + DataConnection.this.mApnContexts.size();
                DataConnection.this.log(str);
                if (dp.mApnContext != null) {
                    dp.mApnContext.requestLog(str);
                }
                if (DataConnection.this.mHwCustDataConnection != null) {
                    DataConnection.this.mHwCustDataConnection.clearInternetPcoValue(DataConnection.this.mApnSetting.profileId, DataConnection.this.mPhone);
                }
                if (dp.mTag == DataConnection.this.mTag) {
                    DataConnection.this.mInactiveState.setEnterNotificationParams(dp);
                    DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
                } else {
                    DataConnection.this.log("DcDisconnectState stale EVENT_DEACTIVATE_DONE dp.tag=" + dp.mTag + " mTag=" + DataConnection.this.mTag);
                }
                return true;
            }
        }
    }

    private class DcDisconnectionErrorCreatingConnection extends State {
        private DcDisconnectionErrorCreatingConnection() {
        }

        public void enter() {
            boolean z;
            int phoneId = DataConnection.this.mPhone.getPhoneId();
            int access$1100 = DataConnection.this.mId;
            long j = DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.typesBitmap : 0;
            if (DataConnection.this.mApnSetting != null) {
                z = DataConnection.this.mApnSetting.canHandleType("default");
            } else {
                z = false;
            }
            StatsLog.write(75, 5, phoneId, access$1100, j, z);
        }

        public boolean processMessage(Message msg) {
            if (msg.what != DataConnection.EVENT_DEACTIVATE_DONE) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.log("DcDisconnectionErrorCreatingConnection not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                return false;
            }
            ConnectionParams cp = (ConnectionParams) msg.obj;
            if (cp.mTag == DataConnection.this.mTag) {
                DataConnection.this.log("DcDisconnectionErrorCreatingConnection msg.what=EVENT_DEACTIVATE_DONE");
                if (cp.mApnContext != null) {
                    cp.mApnContext.requestLog("DcDisconnectionErrorCreatingConnection msg.what=EVENT_DEACTIVATE_DONE");
                }
                DataConnection.this.mInactiveState.setEnterNotificationParams(cp, DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER);
                DataConnection.this.transitionTo(DataConnection.this.mInactiveState);
            } else {
                DataConnection dataConnection2 = DataConnection.this;
                dataConnection2.log("DcDisconnectionErrorCreatingConnection stale EVENT_DEACTIVATE_DONE dp.tag=" + cp.mTag + ", mTag=" + DataConnection.this.mTag);
            }
            return true;
        }
    }

    private class DcInactiveState extends State {
        private DcInactiveState() {
        }

        public void setEnterNotificationParams(ConnectionParams cp, DcFailCause cause) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams cp,cause");
            ConnectionParams unused = DataConnection.this.mConnectionParams = cp;
            DisconnectParams unused2 = DataConnection.this.mDisconnectParams = null;
            DcFailCause unused3 = DataConnection.this.mDcFailCause = cause;
        }

        public void setEnterNotificationParams(DisconnectParams dp) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams dp");
            ConnectionParams unused = DataConnection.this.mConnectionParams = null;
            DisconnectParams unused2 = DataConnection.this.mDisconnectParams = dp;
            DcFailCause unused3 = DataConnection.this.mDcFailCause = DcFailCause.NONE;
        }

        public void setEnterNotificationParams(DcFailCause cause) {
            ConnectionParams unused = DataConnection.this.mConnectionParams = null;
            DisconnectParams unused2 = DataConnection.this.mDisconnectParams = null;
            DcFailCause unused3 = DataConnection.this.mDcFailCause = cause;
        }

        public void enter() {
            boolean z;
            DataConnection.this.mTag++;
            DataConnection.this.log("DcInactiveState: enter() mTag=" + DataConnection.this.mTag);
            int phoneId = DataConnection.this.mPhone.getPhoneId();
            int access$1100 = DataConnection.this.mId;
            long j = DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.typesBitmap : 0;
            if (DataConnection.this.mApnSetting != null) {
                z = DataConnection.this.mApnSetting.canHandleType("default");
            } else {
                z = false;
            }
            StatsLog.write(75, 1, phoneId, access$1100, j, z);
            if (DataConnection.this.mConnectionParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyConnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                if (DataConnection.this.mDcFailCause != DcFailCause.NONE) {
                    boolean unused = DataConnection.this.misLastFailed = true;
                }
                DataConnection.this.notifyConnectCompleted(DataConnection.this.mConnectionParams, DataConnection.this.mDcFailCause, true);
            }
            if (DataConnection.this.mDisconnectParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyDisconnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                DataConnection.this.notifyDisconnectCompleted(DataConnection.this.mDisconnectParams, true);
            }
            if (DataConnection.this.mDisconnectParams == null && DataConnection.this.mConnectionParams == null && DataConnection.this.mDcFailCause != null) {
                DataConnection.this.log("DcInactiveState: enter notifyAllDisconnectCompleted failCause=" + DataConnection.this.mDcFailCause);
                DataConnection.this.notifyAllDisconnectCompleted(DataConnection.this.mDcFailCause);
            }
            DataConnection.this.mDcController.removeActiveDcByCid(DataConnection.this);
            DataConnection.this.clearSettings();
        }

        public void exit() {
            boolean unused = DataConnection.this.misLastFailed = false;
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            boolean retVal = false;
            if (i == 262144) {
                DataConnection.this.log("DcInactiveState: mag.what=EVENT_CONNECT");
                ConnectionParams cp = (ConnectionParams) msg.obj;
                if (true == DataConnection.this.misLastFailed && true == cp.mdefered) {
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT apnContext with defefed msg, not process ");
                    DataConnection.this.notifyConnectCompleted(cp, DcFailCause.UNKNOWN, false);
                } else if (DataConnection.this.initConnection(cp)) {
                    DataConnection.this.onConnect(DataConnection.this.mConnectionParams);
                    DataConnection.this.transitionTo(DataConnection.this.mActivatingState);
                } else {
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT initConnection failed");
                    DataConnection.this.notifyConnectCompleted(cp, DcFailCause.UNACCEPTABLE_NETWORK_PARAMETER, false);
                }
                retVal = true;
            } else if (i == DataConnection.EVENT_DISCONNECT) {
                DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT");
                DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                retVal = true;
            } else if (i == DataConnection.EVENT_DISCONNECT_ALL) {
                DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT_ALL");
                DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                retVal = true;
            } else if (i != 266252) {
                DataConnection dataConnection = DataConnection.this;
                dataConnection.log("DcInactiveState nothandled msg.what=" + DataConnection.this.getWhatToString(msg.what));
            } else {
                DataConnection.this.log("DcInactiveState: msg.what=RSP_RESET, ignore we're already reset");
                retVal = true;
            }
            return retVal;
        }
    }

    private class DcNetworkAgent extends NetworkAgent {
        public final DcKeepaliveTracker keepaliveTracker = new DcKeepaliveTracker();
        private NetworkCapabilities mNetworkCapabilities;
        final /* synthetic */ DataConnection this$0;

        private class DcKeepaliveTracker {
            private final SparseArray<KeepaliveRecord> mKeepalives;

            private class KeepaliveRecord {
                public int currentStatus;
                public int slotId;

                KeepaliveRecord(int slotId2, int status) {
                    this.slotId = slotId2;
                    this.currentStatus = status;
                }
            }

            private DcKeepaliveTracker() {
                this.mKeepalives = new SparseArray<>();
            }

            /* access modifiers changed from: package-private */
            public int getHandleForSlot(int slotId) {
                for (int i = 0; i < this.mKeepalives.size(); i++) {
                    if (this.mKeepalives.valueAt(i).slotId == slotId) {
                        return this.mKeepalives.keyAt(i);
                    }
                }
                return -1;
            }

            /* access modifiers changed from: package-private */
            public int keepaliveStatusErrorToPacketKeepaliveError(int error) {
                switch (error) {
                    case 0:
                        return 0;
                    case 1:
                        return -30;
                    default:
                        return -31;
                }
            }

            /* access modifiers changed from: package-private */
            public void handleKeepaliveStarted(int slot, KeepaliveStatus ks) {
                switch (ks.statusCode) {
                    case 0:
                        DcNetworkAgent.this.onPacketKeepaliveEvent(slot, 0);
                        break;
                    case 1:
                        DcNetworkAgent.this.onPacketKeepaliveEvent(slot, keepaliveStatusErrorToPacketKeepaliveError(ks.errorCode));
                        return;
                    case 2:
                        break;
                    default:
                        DataConnection dataConnection = DcNetworkAgent.this.this$0;
                        dataConnection.loge("Invalid KeepaliveStatus Code: " + ks.statusCode);
                        return;
                }
                DcNetworkAgent dcNetworkAgent = DcNetworkAgent.this;
                dcNetworkAgent.log("Adding keepalive handle=" + ks.sessionHandle + " slot = " + slot);
                this.mKeepalives.put(ks.sessionHandle, new KeepaliveRecord(slot, ks.statusCode));
            }

            /* access modifiers changed from: package-private */
            public void handleKeepaliveStatus(KeepaliveStatus ks) {
                KeepaliveRecord kr = this.mKeepalives.get(ks.sessionHandle);
                if (kr == null) {
                    DcNetworkAgent dcNetworkAgent = DcNetworkAgent.this;
                    dcNetworkAgent.log("Discarding keepalive event for different data connection:" + ks);
                    return;
                }
                switch (kr.currentStatus) {
                    case 0:
                        switch (ks.statusCode) {
                            case 0:
                            case 2:
                                DcNetworkAgent.this.this$0.loge("Active Keepalive received invalid status!");
                                break;
                            case 1:
                                DcNetworkAgent.this.this$0.loge("Keepalive received stopped status!");
                                DcNetworkAgent.this.onPacketKeepaliveEvent(kr.slotId, 0);
                                kr.currentStatus = 1;
                                this.mKeepalives.remove(ks.sessionHandle);
                                break;
                            default:
                                DataConnection dataConnection = DcNetworkAgent.this.this$0;
                                dataConnection.loge("Invalid Keepalive Status received, " + ks.statusCode);
                                break;
                        }
                    case 1:
                        DcNetworkAgent.this.this$0.loge("Inactive Keepalive received status!");
                        DcNetworkAgent.this.onPacketKeepaliveEvent(kr.slotId, -31);
                        break;
                    case 2:
                        switch (ks.statusCode) {
                            case 0:
                                DcNetworkAgent.this.log("Pending Keepalive received active status!");
                                kr.currentStatus = 0;
                                DcNetworkAgent.this.onPacketKeepaliveEvent(kr.slotId, 0);
                                break;
                            case 1:
                                DcNetworkAgent.this.onPacketKeepaliveEvent(kr.slotId, keepaliveStatusErrorToPacketKeepaliveError(ks.errorCode));
                                kr.currentStatus = 1;
                                this.mKeepalives.remove(ks.sessionHandle);
                                break;
                            case 2:
                                DcNetworkAgent.this.this$0.loge("Invalid unsolicied Keepalive Pending Status!");
                                break;
                            default:
                                DataConnection dataConnection2 = DcNetworkAgent.this.this$0;
                                dataConnection2.loge("Invalid Keepalive Status received, " + ks.statusCode);
                                break;
                        }
                    default:
                        DataConnection dataConnection3 = DcNetworkAgent.this.this$0;
                        dataConnection3.loge("Invalid Keepalive Status received, " + kr.currentStatus);
                        break;
                }
            }
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        public DcNetworkAgent(DataConnection dataConnection, Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, r10, lp, score, misc);
            NetworkCapabilities networkCapabilities = nc;
            DataConnection dataConnection2 = dataConnection;
            this.this$0 = dataConnection2;
            LocalLog access$5400 = dataConnection2.mNetCapsLocalLog;
            access$5400.log("New network agent created. capabilities=" + networkCapabilities);
            this.mNetworkCapabilities = networkCapabilities;
        }

        /* access modifiers changed from: protected */
        public void unwanted() {
            if (this.this$0.mNetworkAgent != this) {
                log("DcNetworkAgent: unwanted found mNetworkAgent=" + this.this$0.mNetworkAgent + ", which isn't me.  Aborting unwanted");
            } else if (this.this$0.mApnContexts != null) {
                if (this.this$0.keepNetwork) {
                    this.this$0.logi("DcNetworkAgent unwanted keepNetwork");
                    return;
                }
                for (ConnectionParams cp : this.this$0.mApnContexts.values()) {
                    ApnContext apnContext = cp.mApnContext;
                    Pair<ApnContext, Integer> pair = new Pair<>(apnContext, Integer.valueOf(cp.mConnectionGeneration));
                    if (!this.this$0.mDct.isDataNeededWithWifiAndBt()) {
                        log("DcNetworkAgent: [unwanted]: disconnect apnContext=" + apnContext + ". And no retry it after disconnected");
                        apnContext.setReason(AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT);
                    }
                    this.this$0.sendMessage(this.this$0.obtainMessage(DataConnection.EVENT_DISCONNECT, new DisconnectParams(apnContext, apnContext.getReason(), this.this$0.mDct.obtainMessage(270351, pair))));
                }
            }
        }

        /* access modifiers changed from: protected */
        public void pollLceData() {
            if (this.this$0.mPhone.getLceStatus() == 1) {
                this.this$0.mPhone.mCi.pullLceData(this.this$0.obtainMessage(DataConnection.EVENT_BW_REFRESH_RESPONSE));
            }
        }

        /* access modifiers changed from: protected */
        public void networkStatus(int status, String redirectUrl) {
            if (!TextUtils.isEmpty(redirectUrl)) {
                log("validation status: " + status + " with redirection URL: " + redirectUrl);
                this.this$0.mDct.obtainMessage(270380, redirectUrl).sendToTarget();
            }
        }

        public void sendNetworkCapabilities(NetworkCapabilities networkCapabilities) {
            if (!networkCapabilities.equals(this.mNetworkCapabilities)) {
                String logStr = "Changed from " + this.mNetworkCapabilities + " to " + networkCapabilities + ", Data RAT=" + this.this$0.mPhone.getServiceState().getRilDataRadioTechnology() + ", mApnSetting=" + this.this$0.mApnSetting;
                this.this$0.mNetCapsLocalLog.log(logStr);
                log(logStr);
                this.mNetworkCapabilities = networkCapabilities;
            }
            DataConnection.super.sendNetworkCapabilities(networkCapabilities);
        }

        /* access modifiers changed from: protected */
        public void startPacketKeepalive(Message msg) {
            this.this$0.obtainMessage(DataConnection.EVENT_KEEPALIVE_START_REQUEST, msg.arg1, msg.arg2, msg.obj).sendToTarget();
        }

        /* access modifiers changed from: protected */
        public void stopPacketKeepalive(Message msg) {
            this.this$0.obtainMessage(DataConnection.EVENT_KEEPALIVE_STOP_REQUEST, msg.arg1, msg.arg2, msg.obj).sendToTarget();
        }
    }

    public static class DisconnectParams {
        public ApnContext mApnContext;
        Message mOnCompletedMsg;
        String mReason;
        int mTag;

        DisconnectParams(ApnContext apnContext, String reason, Message onCompletedMsg) {
            this.mApnContext = apnContext;
            this.mReason = reason;
            this.mOnCompletedMsg = onCompletedMsg;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mReason=" + this.mReason + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    public enum SetupResult {
        SUCCESS,
        ERROR_RADIO_NOT_AVAILABLE,
        ERROR_INVALID_ARG,
        ERROR_STALE,
        ERROR_DATA_SERVICE_SPECIFIC_ERROR;
        
        public DcFailCause mFailCause;

        public String toString() {
            return name() + "  SetupResult.mFailCause=" + this.mFailCause;
        }
    }

    public static class UpdateLinkPropertyResult {
        public LinkProperties newLp;
        public LinkProperties oldLp;
        public SetupResult setupResult = SetupResult.SUCCESS;

        public UpdateLinkPropertyResult(LinkProperties curLp) {
            this.oldLp = curLp;
            this.newLp = curLp;
        }
    }

    static {
        sCmdToString[0] = "EVENT_CONNECT";
        sCmdToString[1] = "EVENT_SETUP_DATA_CONNECTION_DONE";
        sCmdToString[3] = "EVENT_DEACTIVATE_DONE";
        sCmdToString[4] = "EVENT_DISCONNECT";
        sCmdToString[5] = "EVENT_RIL_CONNECTED";
        sCmdToString[6] = "EVENT_DISCONNECT_ALL";
        sCmdToString[7] = "EVENT_DATA_STATE_CHANGED";
        sCmdToString[8] = "EVENT_TEAR_DOWN_NOW";
        sCmdToString[9] = "EVENT_LOST_CONNECTION";
        sCmdToString[11] = "EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED";
        sCmdToString[12] = "EVENT_DATA_CONNECTION_ROAM_ON";
        sCmdToString[13] = "EVENT_DATA_CONNECTION_ROAM_OFF";
        sCmdToString[14] = "EVENT_BW_REFRESH_RESPONSE";
        sCmdToString[15] = "EVENT_DATA_CONNECTION_VOICE_CALL_STARTED";
        sCmdToString[16] = "EVENT_DATA_CONNECTION_VOICE_CALL_ENDED";
        sCmdToString[17] = "EVENT_DATA_CONNECTION_OVERRIDE_CHANGED";
        sCmdToString[18] = "EVENT_KEEPALIVE_STATUS";
        sCmdToString[19] = "EVENT_KEEPALIVE_STARTED";
        sCmdToString[20] = "EVENT_KEEPALIVE_STOPPED";
        sCmdToString[21] = "EVENT_KEEPALIVE_START_REQUEST";
        sCmdToString[22] = "EVENT_KEEPALIVE_STOP_REQUEST";
        sCmdToString[23] = "EVENT_LINK_CAPACITY_CHANGED";
        sCmdToString[24] = "EVENT_CLEAR_LINK";
        sCmdToString[25] = "EVENT_RESUME_LINK";
    }

    static String cmdToString(int cmd) {
        String value;
        int cmd2 = cmd - InboundSmsTracker.DEST_PORT_FLAG_3GPP2;
        if (cmd2 < 0 || cmd2 >= sCmdToString.length) {
            value = DcAsyncChannel.cmdToString(cmd2 + InboundSmsTracker.DEST_PORT_FLAG_3GPP2);
        } else {
            value = sCmdToString[cmd2];
        }
        if (value != null) {
            return value;
        }
        return "0x" + Integer.toHexString(InboundSmsTracker.DEST_PORT_FLAG_3GPP2 + cmd2);
    }

    public static DataConnection makeDataConnection(Phone phone, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        DataConnection dc = new DataConnection(phone, "DC-" + mInstanceNumber.incrementAndGet(), id, dct, dataServiceManager, failBringUpAll, dcc);
        dc.start();
        dc.log("Made " + dc.getName());
        return dc;
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        log("dispose: call quiteNow()");
        quitNow();
    }

    /* access modifiers changed from: package-private */
    public LinkProperties getCopyLinkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    /* access modifiers changed from: package-private */
    public boolean isInactive() {
        return getCurrentState() == this.mInactiveState;
    }

    /* access modifiers changed from: package-private */
    public boolean isDisconnecting() {
        return getCurrentState() == this.mDisconnectingState;
    }

    /* access modifiers changed from: package-private */
    public boolean isActive() {
        return getCurrentState() == this.mActiveState;
    }

    /* access modifiers changed from: package-private */
    public boolean isActivating() {
        return getCurrentState() == this.mActivatingState;
    }

    /* access modifiers changed from: package-private */
    public int getCid() {
        return this.mCid;
    }

    /* access modifiers changed from: package-private */
    public ApnSetting getApnSetting() {
        return this.mApnSetting;
    }

    /* access modifiers changed from: package-private */
    public void setLinkPropertiesHttpProxy(ProxyInfo proxy) {
        this.mLinkProperties.setHttpProxy(proxy);
    }

    public boolean isIpv4Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            if (addr instanceof Inet4Address) {
                Inet4Address i4addr = (Inet4Address) addr;
                if (!i4addr.isAnyLocalAddress() && !i4addr.isLinkLocalAddress() && !i4addr.isLoopbackAddress() && !i4addr.isMulticastAddress()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isIpv6Connected() {
        for (InetAddress addr : this.mLinkProperties.getAddresses()) {
            if (addr instanceof Inet6Address) {
                Inet6Address i6addr = (Inet6Address) addr;
                if (!i6addr.isAnyLocalAddress() && !i6addr.isLinkLocalAddress() && !i6addr.isLoopbackAddress() && !i6addr.isMulticastAddress()) {
                    return true;
                }
            }
        }
        return false;
    }

    @VisibleForTesting
    public UpdateLinkPropertyResult updateLinkProperty(DataCallResponse newState) {
        UpdateLinkPropertyResult result = new UpdateLinkPropertyResult(this.mLinkProperties);
        if (newState == null) {
            return result;
        }
        result.newLp = new LinkProperties();
        result.setupResult = setLinkProperties(newState, result.newLp);
        if (result.setupResult != SetupResult.SUCCESS) {
            log("updateLinkProperty failed : " + result.setupResult);
            return result;
        }
        result.newLp.setHttpProxy(this.mLinkProperties.getHttpProxy());
        checkSetMtu(this.mApnSetting, result.newLp);
        this.mLinkProperties = result.newLp;
        updateTcpBufferSizes(this.mRilRat);
        if (!result.oldLp.equals(result.newLp)) {
            log("updateLinkProperty old LP=*");
        }
        if (!result.newLp.equals(result.oldLp) && this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        return result;
    }

    private void checkSetMtu(ApnSetting apn, LinkProperties lp) {
        if (lp != null && apn != null && lp != null) {
            if (this.mHwCustDataConnection != null && this.mHwCustDataConnection.setMtuIfNeeded(lp, this.mPhone)) {
                return;
            }
            if (lp.getMtu() != 0) {
                log("MTU set by call response to: " + lp.getMtu());
            } else if (apn == null || apn.mtu == 0) {
                int mtu = this.mPhone.getContext().getResources().getInteger(17694821);
                if (mtu != 0) {
                    lp.setMtu(mtu);
                    log("MTU set by config resource to: " + mtu);
                }
            } else {
                lp.setMtu(apn.mtu);
                log("MTU set by APN to: " + apn.mtu);
            }
        }
    }

    private DataConnection(Phone phone, String name, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        super(name, dcc.getHandler());
        setLogRecSize(ScanIntervalRange.MAX);
        setLogOnlyTransitions(true);
        log("DataConnection created");
        this.mPhone = phone;
        this.mDct = dct;
        this.mDataServiceManager = dataServiceManager;
        this.mDcTesterFailBringUpAll = failBringUpAll;
        this.mDcController = dcc;
        this.mId = id;
        this.mCid = -1;
        ServiceState ss = this.mPhone.getServiceState();
        this.mRilRat = ss.getRilDataRadioTechnology();
        this.mDataRegState = this.mPhone.getServiceState().getDataRegState();
        int networkType = ss.getDataNetworkType();
        this.mNetworkInfo = new NetworkInfo(0, networkType, NETWORK_TYPE, TelephonyManager.getNetworkTypeName(networkType));
        this.mNetworkInfo.setRoaming(ss.getDataRoaming());
        this.mNetworkInfo.setIsAvailable(true);
        addState(this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mActivatingState, this.mDefaultState);
        addState(this.mActiveState, this.mDefaultState);
        addState(this.mDisconnectingState, this.mDefaultState);
        addState(this.mDisconnectingErrorCreatingConnection, this.mDefaultState);
        setInitialState(this.mInactiveState);
        this.mApnContexts = new HashMap<>();
        this.mHwCustDataConnection = (HwCustDataConnection) HwCustUtils.createObj(HwCustDataConnection.class, new Object[0]);
    }

    /* access modifiers changed from: private */
    public void onConnect(ConnectionParams cp) {
        int i;
        ConnectionParams connectionParams = cp;
        log("onConnect: carrier='" + this.mApnSetting.carrier + "' APN='" + this.mApnSetting.apn + "' proxy='" + this.mApnSetting.proxy + "' port='" + this.mApnSetting.port + "'");
        if (connectionParams.mApnContext != null) {
            connectionParams.mApnContext.requestLog("DataConnection.onConnect");
        }
        int i2 = 1;
        if (this.mDcTesterFailBringUpAll.getDcFailBringUp().mCounter > 0) {
            DataCallResponse dataCallResponse = new DataCallResponse(this.mDcTesterFailBringUpAll.getDcFailBringUp().mFailCause.getErrorCode(), this.mDcTesterFailBringUpAll.getDcFailBringUp().mSuggestedRetryTime, 0, 0, "", "", null, null, null, null, 0);
            Message msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, connectionParams);
            AsyncResult.forMessage(msg, dataCallResponse, null);
            sendMessage(msg);
            log("onConnect: FailBringUpAll=" + this.mDcTesterFailBringUpAll.getDcFailBringUp() + " send error response=" + dataCallResponse);
            DcFailBringUp dcFailBringUp = this.mDcTesterFailBringUpAll.getDcFailBringUp();
            dcFailBringUp.mCounter = dcFailBringUp.mCounter - 1;
            return;
        }
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = DcFailCause.NONE;
        Message msg2 = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, connectionParams);
        msg2.obj = connectionParams;
        if (this.mHwDataConnectionManager != null && this.mHwDataConnectionManager.getNamSwitcherForSoftbank()) {
            HashMap<String, String> userInfo = this.mHwDataConnectionManager.encryptApnInfoForSoftBank(this.mPhone, this.mApnSetting);
            if (userInfo != null) {
                String username = userInfo.get("username");
                String password = userInfo.get("password");
                log("onConnect: mApnSetting.user-mApnSetting.password handle finish");
                DataServiceManager dataServiceManager = this.mDataServiceManager;
                int i3 = connectionParams.mRilRat;
                int i4 = connectionParams.mProfileId;
                String str = this.mApnSetting.apn;
                String str2 = this.mApnSetting.protocol;
                int i5 = this.mApnSetting.authType;
                if (this.mApnSetting.bearerBitmask == 0) {
                    i = 0;
                } else {
                    if (ServiceState.bearerBitmapHasCdma(this.mApnSetting.bearerBitmask)) {
                        i2 = 2;
                    }
                    i = i2;
                }
                int i6 = this.mApnSetting.maxConnsTime;
                int i7 = this.mApnSetting.maxConns;
                int i8 = this.mApnSetting.waitTime;
                HashMap<String, String> hashMap = userInfo;
                boolean z = this.mApnSetting.carrierEnabled;
                Object obj = "username";
                int i9 = this.mApnSetting.typesBitmap;
                Object obj2 = "password";
                DataProfile dataProfile = new DataProfile(i4, str, str2, i5, username, password, i, i6, i7, i8, z, i9, this.mApnSetting.roamingProtocol, this.mApnSetting.bearerBitmask, this.mApnSetting.mtu, this.mApnSetting.mvnoType, this.mApnSetting.mvnoMatchData, this.mApnSetting.modemCognitive);
                dataServiceManager.setupDataCall(i3, dataProfile, this.mPhone.getServiceState().getDataRoamingFromRegistration(), this.mPhone.getDataRoamingEnabled(), 1, null, msg2);
                return;
            }
        }
        DataProfile dp = DcTracker.createDataProfile(this.mApnSetting, connectionParams.mProfileId);
        if (this.mHwCustDataConnection != null && this.mHwCustDataConnection.whetherSetApnByCust(this.mPhone) && HuaweiTelephonyConfigs.isHisiPlatform()) {
            DataProfile dpCopy = dp;
            DataProfile dataProfile2 = new DataProfile(dpCopy.getProfileId(), "", dpCopy.getProtocol(), dpCopy.getAuthType(), dpCopy.getUserName(), dpCopy.getPassword(), dpCopy.getType(), dpCopy.getMaxConnsTime(), dpCopy.getMaxConns(), dpCopy.getWaitTime(), dpCopy.isEnabled(), dpCopy.getSupportedApnTypesBitmap(), dpCopy.getRoamingProtocol(), dpCopy.getBearerBitmap(), dpCopy.getMtu(), dpCopy.getMvnoType(), dpCopy.getMvnoMatchData(), dpCopy.isModemCognitive());
            dp = dataProfile2;
        }
        int reason = 1;
        if (this.mHwCustDataConnection != null && this.mHwCustDataConnection.isEmergencyApnSetting(this.mApnSetting)) {
            reason = 15;
        }
        int reason2 = reason;
        boolean isModemRoaming = this.mPhone.getServiceState().getDataRoamingFromRegistration();
        this.mDataServiceManager.setupDataCall(ServiceState.rilRadioTechnologyToAccessNetworkType(connectionParams.mRilRat), dp, isModemRoaming, this.mPhone.getDataRoamingEnabled() || (isModemRoaming && !this.mPhone.getServiceState().getDataRoaming()), reason2, null, msg2);
        TelephonyMetrics.getInstance().writeSetupDataCall(this.mPhone.getPhoneId(), connectionParams.mRilRat, dp.getProfileId(), dp.getApn(), dp.getProtocol());
    }

    public void onSubscriptionOverride(int overrideMask, int overrideValue) {
        this.mSubscriptionOverride = (this.mSubscriptionOverride & (~overrideMask)) | (overrideValue & overrideMask);
        sendMessage(obtainMessage(EVENT_DATA_CONNECTION_OVERRIDE_CHANGED));
    }

    /* access modifiers changed from: private */
    public void tearDownData(Object o) {
        int discReason = 1;
        ApnContext apnContext = null;
        if (o != null && (o instanceof DisconnectParams)) {
            DisconnectParams dp = (DisconnectParams) o;
            apnContext = dp.mApnContext;
            if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_RADIO_TURNED_OFF) || TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_PDP_RESET)) {
                discReason = 2;
            }
        }
        String str = "tearDownData. mCid=" + this.mCid + ", reason=" + discReason;
        log(str);
        if (apnContext != null) {
            apnContext.requestLog(str);
        }
        if (!(apnContext == null || this.mHwCustDataConnection == null || !this.mHwCustDataConnection.isEmergencyApnSetting(apnContext.getApnSetting()))) {
            discReason = 15;
        }
        this.mDataServiceManager.deactivateDataCall(this.mCid, discReason, obtainMessage(EVENT_DEACTIVATE_DONE, this.mTag, 0, o));
    }

    private void notifyAllWithEvent(ApnContext alreadySent, int event, String reason) {
        this.mNetworkInfo.setDetailedState(this.mNetworkInfo.getDetailedState(), reason, this.mNetworkInfo.getExtraInfo());
        for (ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (apnContext != alreadySent) {
                if (reason != null) {
                    apnContext.setReason(reason);
                }
                Message msg = this.mDct.obtainMessage(event, new Pair<>(apnContext, Integer.valueOf(cp.mConnectionGeneration)));
                AsyncResult.forMessage(msg);
                msg.sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyAllOfConnected(String reason) {
        notifyAllWithEvent(null, 270336, reason);
    }

    private void notifyAllOfDisconnectDcRetrying(String reason) {
        notifyAllWithEvent(null, 270370, reason);
    }

    /* access modifiers changed from: private */
    public void notifyAllDisconnectCompleted(DcFailCause cause) {
        notifyAllWithEvent(null, 270351, cause.toString());
    }

    /* access modifiers changed from: private */
    public void notifyConnectCompleted(ConnectionParams cp, DcFailCause cause, boolean sendAll) {
        ApnContext alreadySent = null;
        if (!(cp == null || cp.mOnCompletedMsg == null)) {
            Message connectionCompletedMsg = cp.mOnCompletedMsg;
            cp.mOnCompletedMsg = null;
            alreadySent = cp.mApnContext;
            long timeStamp = System.currentTimeMillis();
            connectionCompletedMsg.arg1 = this.mCid;
            if (cause == DcFailCause.NONE) {
                this.mCreateTime = timeStamp;
                AsyncResult.forMessage(connectionCompletedMsg);
            } else {
                this.mLastFailCause = cause;
                this.mLastFailTime = timeStamp;
                if (cause == null) {
                    cause = DcFailCause.UNKNOWN;
                }
                cp.mApnContext.setPdpFailCause(cause);
                AsyncResult.forMessage(connectionCompletedMsg, cause, new Throwable(cause.toString()));
            }
            log("notifyConnectCompleted at " + timeStamp + " cause=" + cause + " connectionCompletedMsg=" + msgToString(connectionCompletedMsg));
            connectionCompletedMsg.sendToTarget();
        }
        if (sendAll) {
            log("Send to all. " + alreadySent + " " + cause.toString());
            notifyAllWithEvent(alreadySent, 270371, cause.toString());
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: com.android.internal.telephony.dataconnection.ApnContext} */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    public void notifyDisconnectCompleted(DisconnectParams dp, boolean sendAll) {
        log("NotifyDisconnectCompleted");
        ApnContext alreadySent = null;
        String reason = null;
        if (!(dp == null || dp.mOnCompletedMsg == null)) {
            Message msg = dp.mOnCompletedMsg;
            dp.mOnCompletedMsg = null;
            if (msg.obj instanceof ApnContext) {
                alreadySent = msg.obj;
            }
            reason = dp.mReason;
            Object[] objArr = new Object[2];
            objArr[0] = msg.toString();
            objArr[1] = msg.obj instanceof String ? (String) msg.obj : "<no-reason>";
            log(String.format("msg=%s msg.obj=%s", objArr));
            AsyncResult.forMessage(msg);
            msg.sendToTarget();
        }
        if (sendAll) {
            if (reason == null) {
                reason = DcFailCause.UNKNOWN.toString();
            }
            notifyAllWithEvent(alreadySent, 270351, reason);
        }
        log("NotifyDisconnectCompleted DisconnectParams=*");
    }

    public int getDataConnectionId() {
        return this.mId;
    }

    /* access modifiers changed from: private */
    public void clearSettings() {
        log("clearSettings");
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = DcFailCause.NONE;
        this.mCid = -1;
        this.mPcscfAddr = new String[5];
        this.mLinkProperties = new LinkProperties();
        this.mApnContexts.clear();
        this.mApnSetting = null;
        this.mDcFailCause = null;
        this.mEhrpdFailCount = 0;
    }

    /* access modifiers changed from: private */
    public SetupResult onSetupConnectionCompleted(int resultCode, DataCallResponse response, ConnectionParams cp) {
        if (cp.mTag != this.mTag) {
            log("onSetupConnectionCompleted stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            return SetupResult.ERROR_STALE;
        } else if (resultCode == 4) {
            SetupResult result = SetupResult.ERROR_RADIO_NOT_AVAILABLE;
            result.mFailCause = DcFailCause.RADIO_NOT_AVAILABLE;
            return result;
        } else if (response.getStatus() == 0) {
            log("onSetupConnectionCompleted received successful DataCallResponse");
            this.mCid = response.getCallId();
            this.mPcscfAddr = (String[]) response.getPcscfs().toArray(new String[response.getPcscfs().size()]);
            if (cp.mApnContext.getApnType().equals("internaldefault")) {
                cp.mApnContext.setEnabled(false);
            }
            SetupResult result2 = updateLinkProperty(response).setupResult;
            if (cp.mApnContext.getApnType().equals("mms") || cp.mApnContext.getApnType().equals("ims") || response.getDnses() == null) {
                return result2;
            }
            int dnsSize = response.getDnses().size();
            String[] dnses = new String[dnsSize];
            for (int i = 0; i < dnsSize; i++) {
                dnses[i] = ((InetAddress) response.getDnses().get(i)).toString();
            }
            HwTelephonyFactory.getHwDataServiceChrManager().SendIntentDNSfailure(dnses);
            return result2;
        } else if (response.getStatus() == DcFailCause.RADIO_NOT_AVAILABLE.getErrorCode()) {
            SetupResult result3 = SetupResult.ERROR_RADIO_NOT_AVAILABLE;
            result3.mFailCause = DcFailCause.RADIO_NOT_AVAILABLE;
            return result3;
        } else {
            SetupResult result4 = SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR;
            result4.mFailCause = DcFailCause.fromInt(response.getStatus());
            return result4;
        }
    }

    private boolean isDnsOk(String[] domainNameServers) {
        if (!NULL_IP.equals(domainNameServers[0]) || !NULL_IP.equals(domainNameServers[1]) || this.mPhone.isDnsCheckDisabled() || (this.mApnSetting.types[0].equals("mms") && isIpAddress(this.mApnSetting.mmsProxy))) {
            return true;
        }
        log(String.format("isDnsOk: return false apn.types[0]=%s APN_TYPE_MMS=%s isIpAddress(%s)=%s", new Object[]{this.mApnSetting.types[0], "mms", this.mApnSetting.mmsProxy, Boolean.valueOf(isIpAddress(this.mApnSetting.mmsProxy))}));
        return false;
    }

    /* access modifiers changed from: private */
    public void updateTcpBufferSizes(int rilRat) {
        String sizes = null;
        if (rilRat == 19) {
            rilRat = 14;
        }
        String ratName = ServiceState.rilRadioTechnologyToString(rilRat).toLowerCase(Locale.ROOT);
        if (rilRat == 7 || rilRat == 8 || rilRat == 12) {
            ratName = "evdo";
        }
        String[] configOverride = this.mPhone.getContext().getResources().getStringArray(17236020);
        int i = 0;
        while (true) {
            if (i >= configOverride.length) {
                break;
            }
            String[] split = configOverride[i].split(":");
            if (ratName.equals(split[0]) && split.length == 2) {
                sizes = split[1];
                break;
            }
            i++;
        }
        String tcpBufferSizePropName = "hw.net.tcp.buffersize.";
        if (sizes == null) {
            switch (rilRat) {
                case 1:
                    sizes = TCP_BUFFER_SIZES_GPRS;
                    tcpBufferSizePropName = tcpBufferSizePropName + "gprs";
                    break;
                case 2:
                    sizes = TCP_BUFFER_SIZES_EDGE;
                    tcpBufferSizePropName = tcpBufferSizePropName + "edge";
                    break;
                case 3:
                case 17:
                    sizes = TCP_BUFFER_SIZES_UMTS;
                    tcpBufferSizePropName = tcpBufferSizePropName + "umts";
                    break;
                case 6:
                    sizes = TCP_BUFFER_SIZES_1XRTT;
                    tcpBufferSizePropName = tcpBufferSizePropName + "1xrtt";
                    break;
                case 7:
                case 8:
                case 12:
                    sizes = TCP_BUFFER_SIZES_EVDO;
                    tcpBufferSizePropName = tcpBufferSizePropName + "evdo";
                    break;
                case 9:
                    sizes = TCP_BUFFER_SIZES_HSDPA;
                    tcpBufferSizePropName = tcpBufferSizePropName + "hsdpa";
                    break;
                case 10:
                case 11:
                    sizes = TCP_BUFFER_SIZES_HSPA;
                    tcpBufferSizePropName = tcpBufferSizePropName + "hspa";
                    break;
                case 13:
                    sizes = TCP_BUFFER_SIZES_EHRPD;
                    tcpBufferSizePropName = tcpBufferSizePropName + "ehrpd";
                    break;
                case 14:
                case 19:
                    sizes = TCP_BUFFER_SIZES_LTE;
                    tcpBufferSizePropName = tcpBufferSizePropName + "lte";
                    break;
                case 15:
                    sizes = TCP_BUFFER_SIZES_HSPAP;
                    tcpBufferSizePropName = tcpBufferSizePropName + "hspap";
                    break;
            }
        }
        if (this.mHwDataConnectionManager != null) {
            sizes = this.mHwDataConnectionManager.calTcpBufferSizesByPropName(sizes, tcpBufferSizePropName, this.mPhone);
        }
        this.mLinkProperties.setTcpBufferSizes(sizes);
    }

    /* access modifiers changed from: private */
    public void setNetworkRestriction() {
        this.mRestrictedNetworkOverride = false;
        boolean noRestrictedRequests = true;
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            noRestrictedRequests &= apnContext.hasNoRestrictedRequests(true);
        }
        if (!noRestrictedRequests && this.mApnSetting.isMetered(this.mPhone)) {
            this.mRestrictedNetworkOverride = !this.mDct.isDataEnabled();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    public NetworkCapabilities getNetworkCapabilities() {
        NetworkCapabilities networkCapabilities;
        char c;
        NetworkCapabilities result = new NetworkCapabilities();
        boolean shouldRestrictNetword = false;
        result.addTransportType(0);
        ArrayList<String> apnTypes = new ArrayList<>();
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            apnTypes.add(apnContext.getApnType());
        }
        boolean isBipNetwork = (this.mConnectionParams == null || this.mConnectionParams.mApnContext == null || this.mDct == null || !this.mDct.isBipApnType(this.mConnectionParams.mApnContext.getApnType())) ? false : true;
        if (this.mApnSetting != null) {
            ArrayList<ApnSetting> securedDunApns = this.mDct.fetchDunApns();
            String[] types = this.mApnSetting.types;
            if (this.mHwDataConnectionManager != null) {
                types = this.mHwDataConnectionManager.getCompatibleSimilarApnSettingsTypes(this.mPhone, this.mDct.getCTOperator(this.mDct.getOperatorNumeric()), this.mApnSetting, this.mDct.getAllApnList());
            }
            String[] types2 = types;
            for (String type : types2) {
                if (this.mRestrictedNetworkOverride || this.mConnectionParams == null || !this.mConnectionParams.mUnmeteredUseOnly || !ApnSetting.isMeteredApnType(type, this.mPhone)) {
                    switch (type.hashCode()) {
                        case 42:
                            if (type.equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                                c = 0;
                                break;
                            }
                        case 3352:
                            if (type.equals("ia")) {
                                c = 8;
                                break;
                            }
                        case 98292:
                            if (type.equals("cbs")) {
                                c = 7;
                                break;
                            }
                        case 99837:
                            if (type.equals("dun")) {
                                c = 4;
                                break;
                            }
                        case 104399:
                            if (type.equals("ims")) {
                                c = 6;
                                break;
                            }
                        case 108243:
                            if (type.equals("mms")) {
                                c = 2;
                                break;
                            }
                        case 3149046:
                            if (type.equals("fota")) {
                                c = 5;
                                break;
                            }
                        case 3541982:
                            if (type.equals("supl")) {
                                c = 3;
                                break;
                            }
                        case 1544803905:
                            if (type.equals("default")) {
                                c = 1;
                                break;
                            }
                        case 1629013393:
                            if (type.equals("emergency")) {
                                c = 9;
                                break;
                            }
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            result.addCapability(12);
                            result.addCapability(0);
                            if (!DcTracker.CT_SUPL_FEATURE_ENABLE || apnTypes.contains("supl") || !this.mDct.isCTSimCard(this.mPhone.getSubId())) {
                                result.addCapability(1);
                            } else {
                                log("ct supl feature enabled and apncontex didn't contain supl, didn't add supl capability");
                            }
                            result.addCapability(3);
                            result.addCapability(4);
                            result.addCapability(5);
                            result.addCapability(7);
                            int size = securedDunApns.size();
                            int i = 0;
                            while (true) {
                                if (i < size) {
                                    if (this.mApnSetting.equals(securedDunApns.get(i))) {
                                        result.addCapability(2);
                                    } else {
                                        i++;
                                    }
                                }
                            }
                            if (this.mHwDataConnectionManager == null) {
                                break;
                            } else {
                                this.mHwDataConnectionManager.addCapForApnTypeAll(result);
                                break;
                            }
                        case 1:
                            result.addCapability(12);
                            break;
                        case 2:
                            result.addCapability(0);
                            break;
                        case 3:
                            if (DcTracker.CT_SUPL_FEATURE_ENABLE && !apnTypes.contains("supl") && this.mDct.isCTSimCard(this.mPhone.getSubId())) {
                                log("ct supl feature enabled and apncontex didn't contain supl, didn't add supl capability");
                                break;
                            } else {
                                result.addCapability(1);
                                break;
                            }
                        case 4:
                            result.addCapability(2);
                            break;
                        case 5:
                            result.addCapability(3);
                            break;
                        case 6:
                            result.addCapability(4);
                            break;
                        case 7:
                            result.addCapability(5);
                            break;
                        case 8:
                            result.addCapability(7);
                            break;
                        case 9:
                            result.addCapability(10);
                            break;
                        default:
                            if (this.mHwDataConnectionManager == null) {
                                break;
                            } else {
                                this.mHwDataConnectionManager.addCapAccordingToType(result, type);
                                break;
                            }
                    }
                } else {
                    log("Dropped the metered " + type + " for the unmetered data call.");
                }
            }
            if ((this.mConnectionParams == null || !this.mConnectionParams.mUnmeteredUseOnly || this.mRestrictedNetworkOverride) && this.mApnSetting.isMetered(this.mPhone)) {
                result.removeCapability(11);
            } else {
                result.addCapability(11);
            }
            if (this.mHwCustDataConnection != null) {
                shouldRestrictNetword = false;
                networkCapabilities = this.mHwCustDataConnection.getNetworkCapabilities(apnTypes, types2, result, this.mApnSetting, this.mDct);
            } else {
                shouldRestrictNetword = false;
                networkCapabilities = result;
            }
            result = networkCapabilities;
            if (!isBipNetwork) {
                result.maybeMarkCapabilitiesRestricted();
            }
        }
        if (this.mRestrictedNetworkOverride && !isBipNetwork) {
            shouldRestrictNetword = true;
        }
        if (shouldRestrictNetword) {
            result.removeCapability(13);
            result.removeCapability(2);
        }
        int up = 14;
        int down = 14;
        int i2 = this.mRilRat;
        if (i2 != 19) {
            switch (i2) {
                case 1:
                    up = 80;
                    down = 80;
                    break;
                case 2:
                    up = 59;
                    down = 236;
                    break;
                case 3:
                    up = 384;
                    down = 384;
                    break;
                case 4:
                case 5:
                    up = 14;
                    down = 14;
                    break;
                case 6:
                    up = 100;
                    down = 100;
                    break;
                case 7:
                    up = 153;
                    down = 2457;
                    break;
                case 8:
                    up = 1843;
                    down = 3174;
                    break;
                case 9:
                    up = 2048;
                    down = 14336;
                    break;
                case 10:
                    up = 5898;
                    down = 14336;
                    break;
                case 11:
                    up = 5898;
                    down = 14336;
                    break;
                case 12:
                    up = 1843;
                    down = 5017;
                    break;
                case 13:
                    up = 153;
                    down = 2516;
                    break;
                case 14:
                    up = 51200;
                    down = 102400;
                    break;
                case 15:
                    up = 11264;
                    down = 43008;
                    break;
            }
        } else {
            up = 51200;
            down = 102400;
        }
        result.setLinkUpstreamBandwidthKbps(up);
        result.setLinkDownstreamBandwidthKbps(down);
        result.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(this.mPhone.getSubId())));
        result.setCapability(18, !this.mPhone.getServiceState().getDataRoaming());
        result.addCapability(20);
        if ((this.mSubscriptionOverride & 1) != 0) {
            result.addCapability(11);
        }
        if ((this.mSubscriptionOverride & 2) != 0) {
            result.removeCapability(20);
        }
        return result;
    }

    @VisibleForTesting
    public static boolean isIpAddress(String address) {
        if (address == null) {
            return false;
        }
        return InetAddress.isNumeric(address);
    }

    private SetupResult setLinkProperties(DataCallResponse response, LinkProperties linkProperties) {
        SetupResult result;
        String dnsAddr;
        String propertyPrefix = "net." + response.getIfname() + ".";
        String[] dnsServers = {SystemProperties.get(propertyPrefix + "dns1"), "8.8.8.8"};
        boolean okToUseSystemPropertyDns = isDnsOk(dnsServers);
        linkProperties.clear();
        if (response.getStatus() == DcFailCause.NONE.getErrorCode()) {
            try {
                linkProperties.setInterfaceName(response.getIfname());
                if (response.getAddresses().size() > 0) {
                    for (LinkAddress la : response.getAddresses()) {
                        if (!la.getAddress().isAnyLocalAddress()) {
                            log("addr/pl=* ");
                            linkProperties.addLinkAddress(la);
                        }
                    }
                    if (response.getDnses().size() > 0) {
                        for (InetAddress dns : response.getDnses()) {
                            if (!dns.isAnyLocalAddress()) {
                                linkProperties.addDnsServer(dns);
                                if (dns instanceof Inet4Address) {
                                    SystemProperties.set(propertyPrefix + "dns1", dns.getHostAddress());
                                }
                            }
                        }
                    } else if (okToUseSystemPropertyDns) {
                        for (String dnsAddr2 : dnsServers) {
                            dnsAddr = dnsAddr2.trim();
                            if (!dnsAddr.isEmpty()) {
                                InetAddress ia = NetworkUtils.numericToInetAddress(dnsAddr);
                                if (!ia.isAnyLocalAddress()) {
                                    linkProperties.addDnsServer(ia);
                                }
                            }
                        }
                    } else {
                        throw new UnknownHostException("Empty dns response and no system default dns");
                    }
                    for (InetAddress gateway : response.getGateways()) {
                        linkProperties.addRoute(new RouteInfo(gateway));
                    }
                    linkProperties.setMtu(response.getMtu());
                    result = SetupResult.SUCCESS;
                } else {
                    throw new UnknownHostException("no address for ifname=" + response.getIfname());
                }
            } catch (IllegalArgumentException e) {
                throw new UnknownHostException("Non-numeric dns addr=" + dnsAddr);
            } catch (UnknownHostException e2) {
                log("setLinkProperties: UnknownHostException " + e2);
                result = SetupResult.ERROR_INVALID_ARG;
            }
        } else {
            result = SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR;
        }
        if (result != SetupResult.SUCCESS) {
            log("setLinkProperties: error clearing LinkProperties status=" + response.getStatus() + " result=" + result);
            linkProperties.clear();
        }
        return result;
    }

    /* access modifiers changed from: private */
    public boolean initConnection(ConnectionParams cp) {
        ApnContext apnContext = cp.mApnContext;
        boolean isBipUsingDefaultAPN = false;
        if (this.mApnSetting == null) {
            this.mApnSetting = apnContext.getApnSetting();
        }
        if (this.mApnSetting != null) {
            isBipUsingDefaultAPN = this.mDct.isBipApnType(apnContext.getApnType()) && "default".equals(SystemProperties.get("gsm.bip.apn")) && this.mApnSetting.canHandleType("default");
        }
        if (this.mApnSetting == null || (!this.mApnSetting.canHandleType(apnContext.getApnType()) && !isBipUsingDefaultAPN)) {
            log("initConnection: incompatible apnSetting in ConnectionParams cp=" + cp + " dc=" + this);
            return false;
        }
        this.mTag++;
        this.mConnectionParams = cp;
        this.mConnectionParams.mTag = this.mTag;
        this.mApnContexts.put(apnContext, cp);
        log("initConnection:  RefCount=" + this.mApnContexts.size());
        return true;
    }

    /* access modifiers changed from: private */
    public void updateNetworkInfo() {
        ServiceState state = this.mPhone.getServiceState();
        int subtype = state.getDataNetworkType();
        this.mNetworkInfo.setSubtype(subtype, TelephonyManager.getNetworkTypeName(subtype));
        this.mNetworkInfo.setRoaming(state.getDataRoaming());
    }

    /* access modifiers changed from: private */
    public void updateNetworkInfoSuspendState() {
        if (this.mNetworkAgent == null) {
            Rlog.e(getName(), "Setting suspend state without a NetworkAgent");
        }
        ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
        if (sst.getCurrentDataConnectionState() != 0) {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        } else {
            if (!sst.isConcurrentVoiceAndDataAllowed()) {
                CallTracker ct = this.mPhone.getCallTracker();
                if (!(ct == null || ct.getState() == PhoneConstants.State.IDLE)) {
                    this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
                    return;
                }
            }
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, this.mNetworkInfo.getExtraInfo());
        }
    }

    /* access modifiers changed from: package-private */
    public void tearDownNow() {
        log("tearDownNow()");
        sendMessage(obtainMessage(EVENT_TEAR_DOWN_NOW));
    }

    /* access modifiers changed from: private */
    public long getSuggestedRetryDelay(DataCallResponse response) {
        if (response.getSuggestedRetryTime() < 0) {
            log("No suggested retry delay.");
            return -2;
        } else if (response.getSuggestedRetryTime() != Integer.MAX_VALUE) {
            return (long) response.getSuggestedRetryTime();
        } else {
            log("Modem suggested not retrying.");
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        return cmdToString(what);
    }

    /* access modifiers changed from: private */
    public static String msgToString(Message msg) {
        if (msg == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder();
        b.append("{what=");
        b.append(cmdToString(msg.what));
        b.append(" when=");
        TimeUtils.formatDuration(msg.getWhen() - SystemClock.uptimeMillis(), b);
        if (msg.arg1 != 0) {
            b.append(" arg1=");
            b.append(msg.arg1);
        }
        if (msg.arg2 != 0) {
            b.append(" arg2=");
            b.append(msg.arg2);
        }
        if (msg.obj != null) {
            b.append(" obj=");
            b.append(msg.obj);
        }
        b.append(" target=");
        b.append(msg.getTarget());
        b.append(" replyTo=");
        b.append(msg.replyTo);
        b.append("}");
        return b.toString();
    }

    static void slog(String s) {
        Rlog.d("DC", s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        Rlog.v(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        Rlog.i(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logw(String s) {
        Rlog.w(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    public String toStringSimple() {
        return getName() + ": State=" + getCurrentState().getName() + " mApnSetting=" + this.mApnSetting + " RefCount=" + this.mApnContexts.size() + " mCid=" + this.mCid + " mCreateTime=" + this.mCreateTime + " mLastastFailTime=" + this.mLastFailTime + " mLastFailCause=" + this.mLastFailCause + " mTag=" + this.mTag + " mLinkProperties=" + this.mLinkProperties + " linkCapabilities=" + getNetworkCapabilities() + " mRestrictedNetworkOverride=" + this.mRestrictedNetworkOverride;
    }

    public String toString() {
        return "{" + toStringSimple() + "}";
    }

    /* access modifiers changed from: private */
    public void dumpToLog() {
        dump(null, new PrintWriter(new StringWriter(0)) {
            public void println(String s) {
                DataConnection.this.logd(s);
            }

            public void flush() {
            }
        }, null);
    }

    public void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, " ");
        pw.print("DataConnection ");
        DataConnection.super.dump(fd, pw, args);
        pw.flush();
        pw.increaseIndent();
        pw.println("mApnContexts.size=" + this.mApnContexts.size());
        pw.println("mApnContexts=" + this.mApnContexts);
        pw.println("mDataConnectionTracker=" + this.mDct);
        pw.println("mApnSetting=" + this.mApnSetting);
        pw.println("mTag=" + this.mTag);
        pw.println("mCid=" + this.mCid);
        pw.println("mConnectionParams=" + this.mConnectionParams);
        pw.println("mDisconnectParams=" + this.mDisconnectParams);
        pw.println("mDcFailCause=" + this.mDcFailCause);
        pw.println("mPhone=" + this.mPhone);
        pw.println("mLinkProperties=" + this.mLinkProperties);
        pw.flush();
        pw.println("mDataRegState=" + this.mDataRegState);
        pw.println("mRilRat=" + this.mRilRat);
        pw.println("mNetworkCapabilities=" + getNetworkCapabilities());
        pw.println("mCreateTime=" + TimeUtils.logTimeOfDay(this.mCreateTime));
        pw.println("mLastFailTime=" + TimeUtils.logTimeOfDay(this.mLastFailTime));
        pw.println("mLastFailCause=" + this.mLastFailCause);
        pw.println("mUserData=" + this.mUserData);
        pw.println("mSubscriptionOverride=" + Integer.toHexString(this.mSubscriptionOverride));
        pw.println("mInstanceNumber=" + mInstanceNumber);
        pw.println("mAc=" + this.mAc);
        pw.println("Network capabilities changed history:");
        pw.increaseIndent();
        this.mNetCapsLocalLog.dump(fd, pw, args);
        pw.decreaseIndent();
        pw.decreaseIndent();
        pw.println();
        pw.flush();
    }

    /* access modifiers changed from: package-private */
    public boolean checkApnContext(ApnContext apnContext) {
        if (apnContext == null) {
            return false;
        }
        return this.mApnContexts.containsKey(apnContext);
    }
}
