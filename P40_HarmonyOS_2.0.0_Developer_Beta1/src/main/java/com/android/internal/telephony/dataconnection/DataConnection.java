package com.android.internal.telephony.dataconnection;

import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.net.KeepalivePacketData;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.AccessNetworkConstants;
import android.telephony.DataFailCause;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataCallResponse;
import android.telephony.data.DataProfile;
import android.text.TextUtils;
import android.util.Pair;
import android.util.StatsLog;
import android.util.TimeUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.AbstractPhoneInternalInterface;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwDataConnectionManager;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.LinkCapacityEstimate;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.google.android.mms.pdu.CharacterSets;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class DataConnection extends StateMachine {
    static final int BASE = 262144;
    private static final int CMD_TO_STRING_COUNT = 29;
    private static final boolean DBG = true;
    private static final int DEFAULT_INTERNET_CONNECTION_SCORE = 50;
    private static final int EHRPD_MAX_RETRY = 2;
    static final int EVENT_BW_REFRESH_RESPONSE = 262158;
    static final int EVENT_CLEAR_LINK = 262171;
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
    static final int EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES = 262170;
    static final int EVENT_REEVALUATE_RESTRICTED_STATE = 262169;
    static final int EVENT_RESET = 262168;
    static final int EVENT_RESUME_LINK = 262172;
    static final int EVENT_RIL_CONNECTED = 262149;
    static final int EVENT_SETUP_DATA_CONNECTION_DONE = 262145;
    static final int EVENT_TEAR_DOWN_NOW = 262152;
    private static final int HANDOVER_STATE_BEING_TRANSFERRED = 2;
    private static final int HANDOVER_STATE_COMPLETED = 3;
    private static final int HANDOVER_STATE_IDLE = 1;
    private static final boolean HW_DBG = SystemProperties.getBoolean("ro.debuggable", false);
    private static boolean HW_SET_EHRPD_DATA = SystemProperties.getBoolean("ro.config.hwpp_set_ehrpd_data", false);
    private static final int INVAILID_PDU_SESSION_TYPE = -1;
    private static final boolean IS_NR_SLICES_SUPPORTED = HwFrameworkFactory.getHwInnerTelephonyManager().isNrSlicesSupported();
    private static final byte MATCH_ALL = 1;
    private static final int MATCH_ALL_SLICE_SCORE = 51;
    private static final String NETWORK_APN = "apn";
    private static final String NETWORK_TYPE = "MOBILE";
    private static final String NTWORK_IP_TYPE = "ipType";
    private static final String NULL_IP = "0.0.0.0";
    private static final int OTHER_CONNECTION_SCORE = 45;
    private static final String RAT_NAME_5G = "nr";
    private static final String RAT_NAME_EVDO = "evdo";
    private static final String SLICE_DNN = "dnn";
    private static final String SLICE_PDU_SESSION_TYPE = "pduSessionType";
    private static final String SLICE_SNSSAI = "snssai";
    private static final String SLICE_SSC_MODE = "sscMode";
    private static final String TCP_BUFFER_SIZES_1XRTT = "16384,32768,131072,4096,16384,102400";
    private static final String TCP_BUFFER_SIZES_EDGE = "4093,26280,70800,4096,16384,70800";
    private static final String TCP_BUFFER_SIZES_EHRPD = "131072,262144,1048576,4096,16384,524288";
    private static final String TCP_BUFFER_SIZES_EVDO = "4094,87380,262144,4096,16384,262144";
    private static final String TCP_BUFFER_SIZES_GPRS = "4092,8760,48000,4096,8760,48000";
    private static final String TCP_BUFFER_SIZES_HSDPA = "61167,367002,1101005,8738,52429,262114";
    private static final String TCP_BUFFER_SIZES_HSPA = "40778,244668,734003,16777,100663,301990";
    private static final String TCP_BUFFER_SIZES_HSPAP = "122334,734003,2202010,32040,192239,576717";
    private static final String TCP_BUFFER_SIZES_LTE = "524288,4194304,8388608,262144,524288,1048576";
    private static final String TCP_BUFFER_SIZES_NR = "2097152,6291456,16777216,512000,2097152,8388608";
    private static final String TCP_BUFFER_SIZES_UMTS = "58254,349525,1048576,58254,349525,1048576";
    private static final boolean VDBG = true;
    private static AtomicInteger mInstanceNumber = new AtomicInteger(0);
    private static String[] sCmdToString = new String[29];
    private boolean keepNetwork = false;
    private AsyncChannel mAc;
    private DcActivatingState mActivatingState = new DcActivatingState();
    private DcActiveState mActiveState = new DcActiveState();
    public Map<ApnContext, ConnectionParams> mApnContexts = new ConcurrentHashMap();
    private ApnSetting mApnSetting;
    public int mCid;
    private ConnectionParams mConnectionParams;
    private long mCreateTime;
    private int mDataRegState = KeepaliveStatus.INVALID_HANDLE;
    private DataServiceManager mDataServiceManager;
    private DcController mDcController;
    private int mDcFailCause;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcTracker mDct = null;
    private DcDefaultState mDefaultState = new DcDefaultState();
    private int mDisabledApnTypeBitMask = 0;
    private DisconnectParams mDisconnectParams;
    private DcDisconnectionErrorCreatingConnection mDisconnectingErrorCreatingConnection = new DcDisconnectionErrorCreatingConnection();
    private DcDisconnectingState mDisconnectingState = new DcDisconnectingState();
    private int mEhrpdFailCount = 0;
    private DcNetworkAgent mHandoverSourceNetworkAgent;
    private int mHandoverState;
    private HwCustDataConnection mHwCustDataConnection;
    private HwDataConnectionManager mHwDataConnectionManager;
    private int mId;
    private DcInactiveState mInactiveState = new DcInactiveState();
    private int mLastFailCause;
    private long mLastFailTime;
    private LinkProperties mLastLinkProperties = null;
    private LinkProperties mLinkProperties = new LinkProperties();
    private DcNetworkAgent mNetworkAgent;
    private NetworkInfo mNetworkInfo;
    private String[] mPcscfAddr;
    private Phone mPhone;
    private PhoneExt mPhoneExt;
    PendingIntent mReconnectIntent = null;
    private boolean mRestrictedNetworkOverride = false;
    private int mRilRat = KeepaliveStatus.INVALID_HANDLE;
    private int mScore;
    private int mSubId;
    private int mSubscriptionOverride;
    int mTag;
    private final String mTagSuffix;
    private final int mTransportType;
    private boolean mUnmeteredUseOnly = false;
    private Object mUserData;
    private boolean misLastFailed = false;

    @Retention(RetentionPolicy.SOURCE)
    public @interface HandoverState {
    }

    static /* synthetic */ int access$4008(DataConnection x0) {
        int i = x0.mEhrpdFailCount;
        x0.mEhrpdFailCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$5772(DataConnection x0, int x1) {
        int i = x0.mDisabledApnTypeBitMask & x1;
        x0.mDisabledApnTypeBitMask = i;
        return i;
    }

    static /* synthetic */ int access$5776(DataConnection x0, int x1) {
        int i = x0.mDisabledApnTypeBitMask | x1;
        x0.mDisabledApnTypeBitMask = i;
        return i;
    }

    static {
        String[] strArr = sCmdToString;
        strArr[0] = "EVENT_CONNECT";
        strArr[1] = "EVENT_SETUP_DATA_CONNECTION_DONE";
        strArr[3] = "EVENT_DEACTIVATE_DONE";
        strArr[4] = "EVENT_DISCONNECT";
        strArr[5] = "EVENT_RIL_CONNECTED";
        strArr[6] = "EVENT_DISCONNECT_ALL";
        strArr[7] = "EVENT_DATA_STATE_CHANGED";
        strArr[8] = "EVENT_TEAR_DOWN_NOW";
        strArr[9] = "EVENT_LOST_CONNECTION";
        strArr[11] = "EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED";
        strArr[12] = "EVENT_DATA_CONNECTION_ROAM_ON";
        strArr[13] = "EVENT_DATA_CONNECTION_ROAM_OFF";
        strArr[14] = "EVENT_BW_REFRESH_RESPONSE";
        strArr[15] = "EVENT_DATA_CONNECTION_VOICE_CALL_STARTED";
        strArr[16] = "EVENT_DATA_CONNECTION_VOICE_CALL_ENDED";
        strArr[17] = "EVENT_DATA_CONNECTION_OVERRIDE_CHANGED";
        strArr[18] = "EVENT_KEEPALIVE_STATUS";
        strArr[19] = "EVENT_KEEPALIVE_STARTED";
        strArr[20] = "EVENT_KEEPALIVE_STOPPED";
        strArr[21] = "EVENT_KEEPALIVE_START_REQUEST";
        strArr[22] = "EVENT_KEEPALIVE_STOP_REQUEST";
        strArr[23] = "EVENT_LINK_CAPACITY_CHANGED";
        strArr[24] = "EVENT_RESET";
        strArr[25] = "EVENT_REEVALUATE_RESTRICTED_STATE";
        strArr[26] = "EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES";
        strArr[27] = "EVENT_CLEAR_LINK";
        strArr[28] = "EVENT_RESUME_LINK";
    }

    public static class ConnectionParams {
        public ApnContext mApnContext;
        final int mConnectionGeneration;
        Message mOnCompletedMsg;
        int mProfileId;
        final int mRequestType;
        int mRilRat;
        final int mSubId;
        int mTag;
        boolean mdefered = false;

        ConnectionParams(ApnContext apnContext, int profileId, int rilRadioTechnology, Message onCompletedMsg, int connectionGeneration, int requestType, int subId) {
            this.mApnContext = apnContext;
            this.mProfileId = profileId;
            this.mRilRat = rilRadioTechnology;
            this.mOnCompletedMsg = onCompletedMsg;
            this.mConnectionGeneration = connectionGeneration;
            this.mRequestType = requestType;
            this.mSubId = subId;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mProfileId=" + this.mProfileId + " mRat=" + this.mRilRat + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + " mRequestType=" + DcTracker.requestTypeToString(this.mRequestType) + " mSubId=" + this.mSubId + "}";
        }
    }

    public static class DisconnectParams {
        public ApnContext mApnContext;
        Message mOnCompletedMsg;
        String mReason;
        final int mReleaseType;
        int mTag;

        DisconnectParams(ApnContext apnContext, String reason, int releaseType, Message onCompletedMsg) {
            this.mApnContext = apnContext;
            this.mReason = reason;
            this.mReleaseType = releaseType;
            this.mOnCompletedMsg = onCompletedMsg;
        }

        public String toString() {
            return "{mTag=" + this.mTag + " mApnContext=" + this.mApnContext + " mReason=" + this.mReason + " mReleaseType=" + DcTracker.releaseTypeToString(this.mReleaseType) + " mOnCompletedMsg=" + DataConnection.msgToString(this.mOnCompletedMsg) + "}";
        }
    }

    static String cmdToString(int cmd) {
        String value = null;
        int cmd2 = cmd - 262144;
        if (cmd2 >= 0) {
            String[] strArr = sCmdToString;
            if (cmd2 < strArr.length) {
                value = strArr[cmd2];
            }
        }
        if (value != null) {
            return value;
        }
        return "0x" + Integer.toHexString(262144 + cmd2);
    }

    public static DataConnection makeDataConnection(Phone phone, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        String transportType;
        if (dataServiceManager.getTransportType() == 1) {
            transportType = "C";
        } else {
            transportType = "I";
        }
        DataConnection dc = new DataConnection(phone, transportType + "-" + mInstanceNumber.incrementAndGet(), id, dct, dataServiceManager, failBringUpAll, dcc);
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
    public LinkProperties getLinkProperties() {
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
    public boolean hasBeenTransferred() {
        return this.mHandoverState == 3;
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
        DcNetworkAgent dcNetworkAgent = this.mNetworkAgent;
        if (dcNetworkAgent != null) {
            dcNetworkAgent.sendLinkProperties(this.mLinkProperties, this);
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

    public enum SetupResult {
        SUCCESS,
        ERROR_RADIO_NOT_AVAILABLE,
        ERROR_INVALID_ARG,
        ERROR_STALE,
        ERROR_DATA_SERVICE_SPECIFIC_ERROR;
        
        public int mFailCause = DataFailCause.getFailCause(0);

        private SetupResult() {
        }

        @Override // java.lang.Enum, java.lang.Object
        public String toString() {
            return name() + "  SetupResult.mFailCause=" + this.mFailCause;
        }
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
        DcNetworkAgent dcNetworkAgent;
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
        if (!result.newLp.equals(result.oldLp) && (dcNetworkAgent = this.mNetworkAgent) != null) {
            dcNetworkAgent.sendLinkProperties(this.mLinkProperties, this);
        }
        return result;
    }

    private void checkSetMtu(ApnSetting apn, LinkProperties lp) {
        if (lp != null && apn != null) {
            HwCustDataConnection hwCustDataConnection = this.mHwCustDataConnection;
            if (hwCustDataConnection != null && hwCustDataConnection.setMtuIfNeeded(lp, this.mPhone)) {
                return;
            }
            if (lp.getMtu() != 0) {
                log("MTU set by call response to: " + lp.getMtu());
            } else if (apn.getMtu() != 0) {
                lp.setMtu(apn.getMtu());
                log("MTU set by APN to: " + apn.getMtu());
            } else if (this.mDct.getHwDcTrackerEx().isCTSimCard(this.mPhone.getPhoneId())) {
                log("MTU not set in CT Card");
            } else {
                int mtu = this.mPhone.getContext().getResources().getInteger(17694846);
                if (mtu != 0) {
                    lp.setMtu(mtu);
                    log("MTU set by config resource to: " + mtu);
                }
            }
        }
    }

    private DataConnection(Phone phone, String tagSuffix, int id, DcTracker dct, DataServiceManager dataServiceManager, DcTesterFailBringUpAll failBringUpAll, DcController dcc) {
        super("DC-" + tagSuffix, dcc.getHandler());
        this.mTagSuffix = tagSuffix;
        this.mHwDataConnectionManager = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).getHwDataConnectionManager();
        setLogRecSize(300);
        setLogOnlyTransitions(true);
        log("DataConnection created");
        this.mPhone = phone;
        this.mPhoneExt = new PhoneExt();
        this.mPhoneExt.setPhone(this.mPhone);
        this.mDct = dct;
        this.mDataServiceManager = dataServiceManager;
        this.mTransportType = dataServiceManager.getTransportType();
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
        this.mHwCustDataConnection = (HwCustDataConnection) HwCustUtils.createObj(HwCustDataConnection.class, new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getHandoverSourceTransport() {
        if (this.mTransportType == 1) {
            return 2;
        }
        return 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int connect(ConnectionParams cp) {
        DataProfile dp;
        int reason;
        log("connect: carrier='" + this.mApnSetting.getEntryName() + "' APN='" + this.mApnSetting.getApnName() + "' proxy='" + this.mApnSetting.getProxyAddressAsString() + "' port='" + this.mApnSetting.getProxyPort() + "'");
        if (cp.mApnContext != null) {
            cp.mApnContext.requestLog("DataConnection.connect");
        }
        boolean z = true;
        if (this.mDcTesterFailBringUpAll.getDcFailBringUp().mCounter > 0) {
            DataCallResponse response = new DataCallResponse(this.mDcTesterFailBringUpAll.getDcFailBringUp().mFailCause, this.mDcTesterFailBringUpAll.getDcFailBringUp().mSuggestedRetryTime, 0, 0, 0, PhoneConfigurationManager.SSSS, (List) null, (List) null, (List) null, (List) null, 0);
            Message msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
            AsyncResult.forMessage(msg, response, (Throwable) null);
            sendMessage(msg);
            log("connect: FailBringUpAll=" + this.mDcTesterFailBringUpAll.getDcFailBringUp() + " send error response=" + response);
            DcFailBringUp dcFailBringUp = this.mDcTesterFailBringUpAll.getDcFailBringUp();
            dcFailBringUp.mCounter = dcFailBringUp.mCounter - 1;
            return 0;
        }
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = 0;
        byte sscMode = 0;
        String snssai = PhoneConfigurationManager.SSSS;
        String dnn = null;
        int pduSessionType = -1;
        if (isNrSlice(cp)) {
            sscMode = cp.mApnContext.getSscMode();
            snssai = cp.mApnContext.getSnssai();
            dnn = cp.mApnContext.getDnn();
            pduSessionType = cp.mApnContext.getPduSessionType();
            log("Activate a new network slice, sscMode = " + ((int) sscMode) + " sNssai = " + snssai + " dnn = " + dnn + " pduSessionType = " + pduSessionType);
        }
        Message msg2 = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
        msg2.obj = cp;
        HwDataConnectionManager hwDataConnectionManager = this.mHwDataConnectionManager;
        int profileType = 2;
        if (hwDataConnectionManager != null && hwDataConnectionManager.getNamSwitcherForSoftbank()) {
            HashMap<String, String> userInfo = this.mHwDataConnectionManager.encryptApnInfoForSoftBank(this.mPhoneExt, this.mApnSetting);
            if (userInfo != null) {
                userInfo.get("username");
                userInfo.get("password");
                log("onConnect: mApnSetting.user-mApnSetting.password handle finish");
                if (ServiceState.convertNetworkTypeBitmaskToBearerBitmask(this.mApnSetting.getNetworkTypeBitmask()) == 0) {
                    profileType = 0;
                } else if (!ServiceState.bearerBitmapHasCdma(ServiceState.convertNetworkTypeBitmaskToBearerBitmask(this.mApnSetting.getNetworkTypeBitmask()))) {
                    profileType = 1;
                }
                DataProfile.Builder persistent = new DataProfile.Builder().setProfileId(cp.mProfileId).setApn(dnn == null ? this.mApnSetting.getApnName() : dnn).setProtocolType(pduSessionType == -1 ? this.mApnSetting.getProtocol() : pduSessionType).setAuthType(this.mApnSetting.getAuthType()).setUserName(this.mApnSetting.getUser()).setPassword(this.mApnSetting.getPassword()).setType(profileType).setMaxConnectionsTime(this.mApnSetting.getMaxConnsTime()).setMaxConnections(this.mApnSetting.getMaxConns()).setWaitTime(this.mApnSetting.getWaitTime()).enable(this.mApnSetting.isEnabled()).setSupportedApnTypesBitmask(this.mApnSetting.getApnTypeBitmask()).setRoamingProtocolType(this.mApnSetting.getRoamingProtocol()).setBearerBitmask(ServiceState.convertNetworkTypeBitmaskToBearerBitmask(this.mApnSetting.getNetworkTypeBitmask())).setMtu(this.mApnSetting.getMtu()).setPersistent(this.mApnSetting.isPersistent());
                if (this.mDct.getPreferredApn() != this.mApnSetting) {
                    z = false;
                }
                DataProfile dp2 = persistent.setPreferred(z).build();
                dp2.setSnssai(snssai);
                dp2.setSscMode(sscMode);
                this.mDataServiceManager.setupDataCall(cp.mRilRat, dp2, this.mPhone.getServiceState().getDataRoamingFromRegistration(), this.mPhone.getDataRoamingEnabled(), 1, null, msg2);
                return 0;
            }
        }
        Bundle extraData = new Bundle();
        extraData.putString(SLICE_DNN, dnn);
        extraData.putString(SLICE_SNSSAI, snssai);
        extraData.putByte(SLICE_SSC_MODE, sscMode);
        extraData.putInt(SLICE_PDU_SESSION_TYPE, pduSessionType);
        ApnSetting regApn = this.mDct.getHwDcTrackerEx().getRegApnForCure(this.mApnSetting);
        if (regApn != null) {
            extraData.putString(NETWORK_APN, regApn.getApnName());
            extraData.putInt(NTWORK_IP_TYPE, regApn.getProtocol());
        }
        DataProfile dp3 = DcTracker.createDataProfile(this.mApnSetting, cp.mProfileId, this.mApnSetting.equals(this.mDct.getPreferredApn()), extraData);
        HwCustDataConnection hwCustDataConnection = this.mHwCustDataConnection;
        if (hwCustDataConnection == null || !hwCustDataConnection.whetherSetApnByCust(this.mPhone) || !HuaweiTelephonyConfigs.isHisiPlatform()) {
            dp = dp3;
        } else {
            dp = new DataProfile.Builder().setProfileId(dp3.getProfileId()).setApn(PhoneConfigurationManager.SSSS).setProtocolType(dp3.getProtocolType()).setAuthType(dp3.getAuthType()).setUserName(dp3.getUserName()).setPassword(dp3.getPassword()).setType(dp3.getType()).setMaxConnectionsTime(dp3.getMaxConnectionsTime()).setMaxConnections(dp3.getMaxConnections()).setWaitTime(dp3.getWaitTime()).enable(dp3.isEnabled()).setSupportedApnTypesBitmask(dp3.getSupportedApnTypesBitmask()).setRoamingProtocolType(dp3.getRoamingProtocolType()).setBearerBitmask(dp3.getBearerBitmask()).setMtu(dp3.getMtu()).setPersistent(dp3.isPersistent()).setPreferred(dp3.isPreferred()).build();
        }
        int reason2 = 1;
        HwCustDataConnection hwCustDataConnection2 = this.mHwCustDataConnection;
        if (hwCustDataConnection2 != null && hwCustDataConnection2.isEmergencyApnSetting(this.mApnSetting)) {
            reason2 = 15;
        }
        boolean isModemRoaming = this.mPhone.getServiceState().getDataRoamingFromRegistration();
        boolean allowRoaming = this.mPhone.getDataRoamingEnabled() || (isModemRoaming && !this.mPhone.getServiceState().getDataRoaming());
        LinkProperties linkProperties = null;
        if (cp.mRequestType == 2) {
            DcTracker dcTracker = this.mPhone.getDcTracker(getHandoverSourceTransport());
            if (dcTracker == null || cp.mApnContext == null) {
                loge("connect: Handover failed. dcTracker=" + dcTracker);
                if (!HW_DBG) {
                    return 65542;
                }
                loge("apnContext= " + cp.mApnContext);
                return 65542;
            }
            DataConnection dc = dcTracker.getDataConnectionByApnType(cp.mApnContext.getApnType());
            if (dc == null) {
                loge("connect: Can't find data connection for handover.");
                return 65542;
            }
            linkProperties = dc.getLinkProperties();
            this.mHandoverSourceNetworkAgent = dc.getNetworkAgent();
            log("Get the handover source network agent: " + this.mHandoverSourceNetworkAgent);
            dc.setHandoverState(2);
            if (linkProperties == null) {
                loge("connect: Can't find link properties of handover data connection. dc=" + dc);
                return 65542;
            }
            reason = 3;
        } else {
            reason = reason2;
        }
        this.mDataServiceManager.setupDataCall(ServiceState.rilRadioTechnologyToAccessNetworkType(cp.mRilRat), dp, isModemRoaming, allowRoaming, reason, linkProperties, msg2);
        TelephonyMetrics.getInstance().writeSetupDataCall(this.mPhone.getPhoneId(), cp.mRilRat, dp.getProfileId(), dp.getApn(), dp.getProtocolType());
        return 0;
    }

    public void onSubscriptionOverride(int overrideMask, int overrideValue) {
        this.mSubscriptionOverride = (this.mSubscriptionOverride & (~overrideMask)) | (overrideValue & overrideMask);
        sendMessage(obtainMessage(EVENT_DATA_CONNECTION_OVERRIDE_CHANGED));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tearDownData(Object o) {
        HwCustDataConnection hwCustDataConnection;
        int discReason = 1;
        ApnContext apnContext = null;
        if (o != null && (o instanceof DisconnectParams)) {
            DisconnectParams dp = (DisconnectParams) o;
            apnContext = dp.mApnContext;
            if (TextUtils.equals(dp.mReason, PhoneInternalInterface.REASON_RADIO_TURNED_OFF) || TextUtils.equals(dp.mReason, "pdpReset")) {
                discReason = 2;
            } else if (dp.mReleaseType == 3) {
                discReason = 3;
            }
        }
        String str = "tearDownData. mCid=" + this.mCid + ", reason=" + discReason;
        log(str);
        if (apnContext != null) {
            apnContext.requestLog(str);
        }
        if (!(apnContext == null || (hwCustDataConnection = this.mHwCustDataConnection) == null || !hwCustDataConnection.isEmergencyApnSetting(apnContext.getApnSetting()))) {
            discReason = 15;
        }
        this.mDataServiceManager.deactivateDataCall(this.mCid, discReason, obtainMessage(EVENT_DEACTIVATE_DONE, this.mTag, 0, o));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAllWithEvent(ApnContext alreadySent, int event, String reason) {
        NetworkInfo networkInfo = this.mNetworkInfo;
        networkInfo.setDetailedState(networkInfo.getDetailedState(), reason, this.mNetworkInfo.getExtraInfo());
        for (ConnectionParams cp : this.mApnContexts.values()) {
            ApnContext apnContext = cp.mApnContext;
            if (apnContext != alreadySent) {
                if (reason != null) {
                    apnContext.setReason(reason);
                }
                Message msg = this.mDct.obtainMessage(event, this.mCid, cp.mRequestType, new Pair<>(apnContext, Integer.valueOf(cp.mConnectionGeneration)));
                AsyncResult.forMessage(msg);
                msg.sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyConnectCompleted(ConnectionParams cp, int cause, boolean sendAll) {
        ApnContext alreadySent = null;
        if (!(cp == null || cp.mOnCompletedMsg == null)) {
            Message connectionCompletedMsg = cp.mOnCompletedMsg;
            cp.mOnCompletedMsg = null;
            alreadySent = cp.mApnContext;
            long timeStamp = System.currentTimeMillis();
            connectionCompletedMsg.arg1 = this.mCid;
            connectionCompletedMsg.arg2 = cp.mRequestType;
            if (cause == 0) {
                this.mCreateTime = timeStamp;
                AsyncResult.forMessage(connectionCompletedMsg);
            } else {
                this.mLastFailCause = cause;
                this.mLastFailTime = timeStamp;
                AsyncResult.forMessage(connectionCompletedMsg, Integer.valueOf(cause), new Throwable(DataFailCause.toString(cause)));
            }
            log("notifyConnectCompleted at " + timeStamp + " cause=" + cause + " connectionCompletedMsg=" + msgToString(connectionCompletedMsg));
            connectionCompletedMsg.sendToTarget();
        }
        if (sendAll) {
            log("Send to all. " + alreadySent + " " + DataFailCause.toString(cause));
            notifyAllWithEvent(alreadySent, 270371, DataFailCause.toString(cause));
        }
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0037: APUT  (r3v5 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r5v3 java.lang.String) */
    /* access modifiers changed from: public */
    private void notifyDisconnectCompleted(DisconnectParams dp, boolean sendAll) {
        DcTracker dcTracker;
        log("NotifyDisconnectCompleted");
        ApnContext alreadySent = null;
        String reason = null;
        if (!(dp == null || dp.mOnCompletedMsg == null)) {
            Message msg = dp.mOnCompletedMsg;
            dp.mOnCompletedMsg = null;
            if (msg.obj instanceof ApnContext) {
                alreadySent = (ApnContext) msg.obj;
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
            if (dp != null && AbstractPhoneInternalInterface.REASON_NO_RETRY_AFTER_DISCONNECT.equals(dp.mReason) && (dcTracker = this.mDct) != null && !dcTracker.getHwDcTrackerEx().isDataNeededWithWifiAndBt()) {
                reason = dp.mReason;
            }
            if (reason == null) {
                reason = DataFailCause.toString(65536);
            }
            notifyAllWithEvent(alreadySent, 270351, reason);
        }
        log("NotifyDisconnectCompleted DisconnectParams=*");
    }

    public int getDataConnectionId() {
        return this.mId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearSettings() {
        log("clearSettings");
        this.mCreateTime = -1;
        this.mLastFailTime = -1;
        this.mLastFailCause = 0;
        this.mCid = -1;
        this.mPcscfAddr = new String[5];
        this.mLinkProperties = new LinkProperties();
        this.mApnContexts.clear();
        this.mApnSetting = null;
        this.mUnmeteredUseOnly = false;
        this.mRestrictedNetworkOverride = false;
        this.mDcFailCause = 0;
        this.mDisabledApnTypeBitMask = 0;
        this.mSubId = -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SetupResult onSetupConnectionCompleted(int resultCode, DataCallResponse response, ConnectionParams cp) {
        log("onSetupConnectionCompleted: resultCode=" + resultCode + ", response=" + response);
        if (cp.mTag != this.mTag) {
            log("onSetupConnectionCompleted stale cp.tag=" + cp.mTag + ", mtag=" + this.mTag);
            return SetupResult.ERROR_STALE;
        } else if (resultCode == 4) {
            SetupResult result = SetupResult.ERROR_RADIO_NOT_AVAILABLE;
            result.mFailCause = 65537;
            return result;
        } else if (response.getCause() == 0) {
            log("onSetupConnectionCompleted received successful DataCallResponse");
            this.mCid = response.getId();
            this.mPcscfAddr = (String[]) response.getPcscfAddresses().stream().map($$Lambda$XZAGhHrbkIDyusER4MAM6luKcT0.INSTANCE).toArray($$Lambda$DataConnection$tFSpFGzTv_UdpzJlTMOvg8VO98.INSTANCE);
            if (cp.mApnContext.getApnType().equals("internaldefault")) {
                cp.mApnContext.setEnabled(false);
            }
            SetupResult result2 = updateLinkProperty(response).setupResult;
            if (cp.mApnContext.getApnType().equals("mms") || cp.mApnContext.getApnType().equals("ims") || response.getDnsAddresses() == null) {
                return result2;
            }
            int dnsSize = response.getDnsAddresses().size();
            String[] dnses = new String[dnsSize];
            for (int i = 0; i < dnsSize; i++) {
                dnses[i] = ((InetAddress) response.getDnsAddresses().get(i)).toString();
            }
            HwTelephonyFactory.getHwDataServiceChrManager().sendIntentDnsFailure(dnses);
            return result2;
        } else if (response.getCause() == 65537) {
            SetupResult result3 = SetupResult.ERROR_RADIO_NOT_AVAILABLE;
            result3.mFailCause = 65537;
            return result3;
        } else {
            SetupResult result4 = SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR;
            result4.mFailCause = DataFailCause.getFailCause(response.getCause());
            return result4;
        }
    }

    static /* synthetic */ String[] lambda$onSetupConnectionCompleted$0(int x$0) {
        return new String[x$0];
    }

    private boolean isDnsOk(String[] domainNameServers) {
        if (!NULL_IP.equals(domainNameServers[0]) || !NULL_IP.equals(domainNameServers[1]) || this.mPhone.isDnsCheckDisabled() || isIpAddress(this.mApnSetting.getMmsProxyAddressAsString())) {
            return true;
        }
        log(String.format("isDnsOk: return false apn.types=%d APN_TYPE_MMS=%s isIpAddress(%s)=%s", Integer.valueOf(this.mApnSetting.getApnTypeBitmask()), "mms", this.mApnSetting.getMmsProxyAddressAsString(), Boolean.valueOf(isIpAddress(this.mApnSetting.getMmsProxyAddressAsString()))));
        return false;
    }

    /* JADX INFO: Multiple debug info for r3v3 java.lang.String: [D('tcpBufferSizePropName' java.lang.String), D('i' int)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTcpBufferSizes(int rilRat) {
        String sizes = null;
        if (rilRat == 19) {
            rilRat = 14;
        }
        String ratName = ServiceState.rilRadioTechnologyToString(rilRat).toLowerCase(Locale.ROOT);
        if (rilRat == 7 || rilRat == 8 || rilRat == 12) {
            ratName = RAT_NAME_EVDO;
        }
        if (rilRat == 14 && isNRConnected()) {
            ratName = RAT_NAME_5G;
        }
        String[] configOverride = this.mPhone.getContext().getResources().getStringArray(17236039);
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
                    tcpBufferSizePropName = tcpBufferSizePropName + RAT_NAME_EVDO;
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
                    if (!isNRConnected()) {
                        sizes = TCP_BUFFER_SIZES_LTE;
                        tcpBufferSizePropName = tcpBufferSizePropName + "lte";
                        break;
                    } else {
                        sizes = TCP_BUFFER_SIZES_NR;
                        tcpBufferSizePropName = tcpBufferSizePropName + RAT_NAME_5G;
                        break;
                    }
                case 15:
                    sizes = TCP_BUFFER_SIZES_HSPAP;
                    tcpBufferSizePropName = tcpBufferSizePropName + "hspap";
                    break;
                case 20:
                    sizes = TCP_BUFFER_SIZES_NR;
                    tcpBufferSizePropName = tcpBufferSizePropName + RAT_NAME_5G;
                    break;
            }
        }
        HwDataConnectionManager hwDataConnectionManager = this.mHwDataConnectionManager;
        if (hwDataConnectionManager != null) {
            sizes = hwDataConnectionManager.calTcpBufferSizesByPropName(sizes, tcpBufferSizePropName, this.mPhoneExt);
        }
        this.mLinkProperties.setTcpBufferSizes(sizes);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldRestrictNetwork() {
        boolean isAnyRestrictedRequest = false;
        Iterator<ApnContext> it = this.mApnContexts.keySet().iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().hasRestrictedRequests(true)) {
                    isAnyRestrictedRequest = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (!isAnyRestrictedRequest || !ApnSettingUtils.isMetered(this.mApnSetting, this.mPhone)) {
            return false;
        }
        if (!this.mPhone.getDataEnabledSettings().isDataEnabled()) {
            return true;
        }
        if (this.mDct.getDataRoamingEnabled() || !this.mPhone.getServiceState().getDataRoaming()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isUnmeteredUseOnly() {
        if (this.mTransportType == 2 || this.mPhone.getDataEnabledSettings().isDataEnabled()) {
            return false;
        }
        if (this.mDct.getDataRoamingEnabled() && this.mPhone.getServiceState().getDataRoaming()) {
            return false;
        }
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            if (ApnSettingUtils.isMeteredApnType(apnContext.getApnTypeBitmask(), this.mPhone)) {
                return false;
            }
        }
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public NetworkCapabilities getNetworkCapabilities() {
        boolean shouldRestrictNetword;
        boolean z;
        char c;
        DcTracker dcTracker;
        NetworkCapabilities result = new NetworkCapabilities();
        boolean z2 = false;
        result.addTransportType(0);
        ArrayList<String> apnTypes = new ArrayList<>();
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            apnTypes.add(apnContext.getApnType());
        }
        ConnectionParams cp = this.mConnectionParams;
        boolean isBipNetwork = (cp == null || cp.mApnContext == null || (dcTracker = this.mDct) == null || !dcTracker.getHwDcTrackerEx().isBipApnType(cp.mApnContext.getApnType())) ? false : true;
        ApnSetting apnSetting = this.mApnSetting;
        if (apnSetting != null) {
            String[] types = ApnSetting.getApnTypesStringFromBitmask(apnSetting.getApnTypeBitmask() & (~this.mDisabledApnTypeBitMask)).split(",");
            HwDataConnectionManager hwDataConnectionManager = this.mHwDataConnectionManager;
            if (hwDataConnectionManager != null) {
                types = hwDataConnectionManager.getCompatibleSimilarApnSettingsTypes(this.mPhoneExt, this.mDct.getHwDcTrackerEx().getCTOperator(this.mDct.getHwDcTrackerEx().getOperatorNumeric()), this.mApnSetting, this.mDct.getAllApnList());
            }
            int length = types.length;
            int i = 0;
            while (i < length) {
                String type = types[i];
                if (this.mRestrictedNetworkOverride || !this.mUnmeteredUseOnly || !ApnSettingUtils.isMeteredApnType(ApnSetting.getApnTypesBitmaskFromString(type), this.mPhone)) {
                    switch (type.hashCode()) {
                        case -897488029:
                            if (type.equals(SLICE_SNSSAI)) {
                                c = 11;
                                break;
                            }
                            c = 65535;
                            break;
                        case 42:
                            if (type.equals(CharacterSets.MIMENAME_ANY_CHARSET)) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3352:
                            if (type.equals("ia")) {
                                c = '\b';
                                break;
                            }
                            c = 65535;
                            break;
                        case 98292:
                            if (type.equals("cbs")) {
                                c = 7;
                                break;
                            }
                            c = 65535;
                            break;
                        case 99837:
                            if (type.equals("dun")) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case 104399:
                            if (type.equals("ims")) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case 107938:
                            if (type.equals("mcx")) {
                                c = '\n';
                                break;
                            }
                            c = 65535;
                            break;
                        case 108243:
                            if (type.equals("mms")) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3149046:
                            if (type.equals("fota")) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case 3541982:
                            if (type.equals("supl")) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1544803905:
                            if (type.equals(TransportManager.IWLAN_OPERATION_MODE_DEFAULT)) {
                                c = 1;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1629013393:
                            if (type.equals("emergency")) {
                                c = '\t';
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            z = false;
                            result.addCapability(12);
                            result.addCapability(0);
                            if (!DcTracker.CT_SUPL_FEATURE_ENABLE || apnTypes.contains("supl") || !this.mDct.getHwDcTrackerEx().isCTSimCard(this.mPhone.getSubId())) {
                                result.addCapability(1);
                            } else {
                                log("ct supl feature enabled and apncontex didn't contain supl, didn't add supl capability");
                            }
                            result.addCapability(3);
                            result.addCapability(4);
                            result.addCapability(5);
                            result.addCapability(7);
                            result.addCapability(2);
                            HwDataConnectionManager hwDataConnectionManager2 = this.mHwDataConnectionManager;
                            if (hwDataConnectionManager2 == null) {
                                break;
                            } else {
                                hwDataConnectionManager2.addCapForApnTypeAll(result);
                                continue;
                            }
                        case 1:
                            z = false;
                            result.addCapability(12);
                            continue;
                        case 2:
                            z = false;
                            result.addCapability(0);
                            continue;
                        case 3:
                            if (!DcTracker.CT_SUPL_FEATURE_ENABLE || apnTypes.contains("supl") || !this.mDct.getHwDcTrackerEx().isCTSimCard(this.mPhone.getSubId())) {
                                result.addCapability(1);
                                z = false;
                                break;
                            } else {
                                log("ct supl feature enabled and apncontex didn't contain supl, didn't add supl capability");
                                z = false;
                                continue;
                            }
                        case 4:
                            result.addCapability(2);
                            z = false;
                            continue;
                        case 5:
                            result.addCapability(3);
                            z = false;
                            continue;
                        case 6:
                            result.addCapability(4);
                            z = false;
                            continue;
                        case 7:
                            result.addCapability(5);
                            z = false;
                            continue;
                        case '\b':
                            result.addCapability(7);
                            z = false;
                            continue;
                        case '\t':
                            result.addCapability(10);
                            z = false;
                            continue;
                        case '\n':
                            result.addCapability(23);
                            z = false;
                            continue;
                        case 11:
                            if (IS_NR_SLICES_SUPPORTED) {
                                if (isMatchAllSlice(this.mConnectionParams)) {
                                    result.addCapability(12);
                                }
                                ConnectionParams connectionParams = this.mConnectionParams;
                                if (!(connectionParams == null || connectionParams.mApnContext == null)) {
                                    result.addCapability(ApnSetting.getNetworkCapabilitiesFromStringFor5GSlice(this.mConnectionParams.mApnContext.getApnType()));
                                }
                                result.addCapability(13);
                                z = false;
                                break;
                            } else {
                                z = false;
                                continue;
                            }
                        default:
                            z = false;
                            HwDataConnectionManager hwDataConnectionManager3 = this.mHwDataConnectionManager;
                            if (hwDataConnectionManager3 != null) {
                                hwDataConnectionManager3.addCapAccordingToType(result, type);
                                break;
                            } else {
                                continue;
                            }
                    }
                } else {
                    log("Dropped the metered " + type + " for the unmetered data call.");
                    z = z2;
                }
                i++;
                z2 = z;
            }
            shouldRestrictNetword = z2;
            if ((!this.mUnmeteredUseOnly || this.mRestrictedNetworkOverride) && ApnSettingUtils.isMetered(this.mApnSetting, this.mPhone)) {
                result.removeCapability(11);
            } else {
                result.addCapability(11);
            }
            HwCustDataConnection hwCustDataConnection = this.mHwCustDataConnection;
            result = hwCustDataConnection != null ? hwCustDataConnection.getNetworkCapabilities(types, result, this.mApnSetting, this.mDct) : result;
            if (!isBipNetwork) {
                result.maybeMarkCapabilitiesRestricted();
            }
        } else {
            shouldRestrictNetword = false;
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
        if (i2 == 19) {
            up = 51200;
            down = 102400;
        } else if (i2 != 20) {
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
                    up = TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_TRAT_SWAP_FAILED;
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
            up = 200000;
            down = 1000000;
        }
        result.setLinkUpstreamBandwidthKbps(up);
        result.setLinkDownstreamBandwidthKbps(down);
        result.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(this.mSubId)));
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
    public boolean shouldSkip464Xlat() {
        int skip464Xlat = this.mApnSetting.getSkip464Xlat();
        if (skip464Xlat == 0) {
            return false;
        }
        if (skip464Xlat == 1) {
            return true;
        }
        NetworkCapabilities nc = getNetworkCapabilities();
        return nc.hasCapability(4) && !nc.hasCapability(12);
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
        String propertyPrefix = "net." + response.getInterfaceName() + ".";
        String[] dnsServers = {SystemProperties.get(propertyPrefix + "dns1"), SystemProperties.get(propertyPrefix + "dns2")};
        boolean okToUseSystemPropertyDns = isDnsOk(dnsServers);
        linkProperties.clear();
        if (response.getCause() == 0) {
            try {
                linkProperties.setInterfaceName(response.getInterfaceName());
                if (response.getAddresses().size() > 0) {
                    for (LinkAddress la : response.getAddresses()) {
                        if (!la.getAddress().isAnyLocalAddress()) {
                            log("addr/pl=* ");
                            linkProperties.addLinkAddress(la);
                        }
                    }
                    if (response.getDnsAddresses().size() > 0) {
                        for (InetAddress dns : response.getDnsAddresses()) {
                            if (!dns.isAnyLocalAddress()) {
                                linkProperties.addDnsServer(dns);
                                if (dns instanceof Inet4Address) {
                                    SystemProperties.set(propertyPrefix + "dns1", dns.getHostAddress());
                                }
                            }
                        }
                    } else if (okToUseSystemPropertyDns) {
                        for (String dnsAddr : dnsServers) {
                            String dnsAddr2 = dnsAddr.trim();
                            if (!dnsAddr2.isEmpty()) {
                                try {
                                    InetAddress ia = NetworkUtils.numericToInetAddress(dnsAddr2);
                                    if (!ia.isAnyLocalAddress()) {
                                        linkProperties.addDnsServer(ia);
                                    }
                                } catch (IllegalArgumentException e) {
                                    throw new UnknownHostException("Non-numeric dns addr=" + dnsAddr2);
                                }
                            }
                        }
                    } else {
                        throw new UnknownHostException("Empty dns response and no system default dns");
                    }
                    if (response.getPcscfAddresses().size() > 0) {
                        for (InetAddress pcscf : response.getPcscfAddresses()) {
                            linkProperties.addPcscfServer(pcscf);
                        }
                    }
                    for (InetAddress gateway : response.getGatewayAddresses()) {
                        linkProperties.addRoute(new RouteInfo(gateway));
                    }
                    linkProperties.setMtu(response.getMtu());
                    result = SetupResult.SUCCESS;
                } else {
                    throw new UnknownHostException("no address for ifname=" + response.getInterfaceName());
                }
            } catch (UnknownHostException e2) {
                log("setLinkProperties: UnknownHostException " + e2);
                result = SetupResult.ERROR_INVALID_ARG;
            }
        } else {
            result = SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR;
        }
        if (result != SetupResult.SUCCESS) {
            log("setLinkProperties: error clearing LinkProperties status=" + response.getCause() + " result=" + result);
            linkProperties.clear();
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean initConnection(ConnectionParams cp) {
        ApnContext apnContext = cp.mApnContext;
        boolean isBipUsingDefaultAPN = false;
        if (this.mApnSetting == null) {
            this.mApnSetting = apnContext.getApnSetting();
        }
        if (this.mApnSetting != null) {
            isBipUsingDefaultAPN = this.mDct.getHwDcTrackerEx().isBipApnType(apnContext.getApnType()) && TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(SystemProperties.get("gsm.bip.apn")) && this.mApnSetting.canHandleType(17);
        }
        ApnSetting apnSetting = this.mApnSetting;
        if (apnSetting == null || (!apnSetting.canHandleType(apnContext.getApnTypeBitmask()) && !isBipUsingDefaultAPN)) {
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

    private class DcDefaultState extends State {
        private DcDefaultState() {
        }

        public void enter() {
            DataConnection.this.log("DcDefaultState: enter");
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(DataConnection.this.mTransportType, DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOn(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_ON, null);
            DataConnection.this.mPhone.getServiceStateTracker().registerForDataRoamingOff(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF, null, true);
            DataConnection.this.mDcController.addDc(DataConnection.this);
        }

        public void exit() {
            DataConnection.this.log("DcDefaultState: exit");
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(DataConnection.this.mTransportType, DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(DataConnection.this.getHandler());
            DataConnection.this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(DataConnection.this.getHandler());
            DataConnection.this.mDcController.removeDc(DataConnection.this);
            if (DataConnection.this.mAc != null) {
                DataConnection.this.mAc.disconnected();
                DataConnection.this.mAc = null;
            }
            DataConnection.this.mApnContexts.clear();
            DataConnection dataConnection = DataConnection.this;
            dataConnection.mReconnectIntent = null;
            dataConnection.mDct = null;
            DataConnection.this.mApnSetting = null;
            DataConnection.this.mPhone = null;
            DataConnection.this.mDataServiceManager = null;
            DataConnection.this.mLinkProperties = null;
            DataConnection.this.mLastFailCause = 0;
            DataConnection.this.mUserData = null;
            DataConnection.this.mDcController = null;
            DataConnection.this.mDcTesterFailBringUpAll = null;
        }

        public boolean processMessage(Message msg) {
            DataConnection dataConnection = DataConnection.this;
            dataConnection.log("DcDefault msg=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
            switch (msg.what) {
                case 262144:
                    DataConnection.this.log("DcDefaultState: msg.what=EVENT_CONNECT, fail not expected");
                    DataConnection.this.notifyConnectCompleted((ConnectionParams) msg.obj, 65536, false);
                    break;
                case DataConnection.EVENT_DISCONNECT /* 262148 */:
                case DataConnection.EVENT_DISCONNECT_ALL /* 262150 */:
                case DataConnection.EVENT_REEVALUATE_RESTRICTED_STATE /* 262169 */:
                    DataConnection dataConnection2 = DataConnection.this;
                    dataConnection2.log("DcDefaultState deferring msg.what=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
                    DataConnection.this.deferMessage(msg);
                    break;
                case DataConnection.EVENT_TEAR_DOWN_NOW /* 262152 */:
                    DataConnection.this.log("DcDefaultState EVENT_TEAR_DOWN_NOW");
                    DataConnection.this.mDataServiceManager.deactivateDataCall(DataConnection.this.mCid, 1, null);
                    break;
                case DataConnection.EVENT_LOST_CONNECTION /* 262153 */:
                    DataConnection.this.logAndAddLogRec("DcDefaultState ignore EVENT_LOST_CONNECTION tag=" + msg.arg1 + ":mTag=" + DataConnection.this.mTag);
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /* 262155 */:
                    Pair<Integer, Integer> drsRatPair = (Pair) ((AsyncResult) msg.obj).result;
                    DataConnection.this.mDataRegState = ((Integer) drsRatPair.first).intValue();
                    DataConnection.this.updateTcpBufferSizes(((Integer) drsRatPair.second).intValue());
                    DataConnection.this.mRilRat = ((Integer) drsRatPair.second).intValue();
                    DataConnection dataConnection3 = DataConnection.this;
                    dataConnection3.log("DcDefaultState: EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED drs=" + DataConnection.this.mDataRegState + " mRilRat=" + DataConnection.this.mRilRat);
                    DataConnection.this.updateNetworkInfo();
                    DataConnection.this.updateNetworkInfoSuspendState();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties, DataConnection.this);
                        break;
                    }
                    break;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /* 262156 */:
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /* 262157 */:
                case DataConnection.EVENT_DATA_CONNECTION_OVERRIDE_CHANGED /* 262161 */:
                    DataConnection.this.updateNetworkInfo();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                        break;
                    }
                    break;
                case DataConnection.EVENT_KEEPALIVE_START_REQUEST /* 262165 */:
                case DataConnection.EVENT_KEEPALIVE_STOP_REQUEST /* 262166 */:
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.onSocketKeepaliveEvent(msg.arg1, -20);
                        break;
                    }
                    break;
                case DataConnection.EVENT_RESET /* 262168 */:
                    DataConnection.this.log("DcDefaultState: msg.what=REQ_RESET");
                    DataConnection dataConnection4 = DataConnection.this;
                    dataConnection4.transitionTo(dataConnection4.mInactiveState);
                    break;
                default:
                    DataConnection dataConnection5 = DataConnection.this;
                    dataConnection5.log("DcDefaultState: shouldn't happen but ignore msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    break;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkInfo() {
        ServiceState state = this.mPhone.getServiceState();
        int subtype = state.getDataNetworkType();
        this.mNetworkInfo.setSubtype(subtype, TelephonyManager.getNetworkTypeName(subtype));
        this.mNetworkInfo.setRoaming(state.getDataRoaming());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkInfoSuspendState() {
        CallTracker ct;
        if (this.mNetworkAgent == null) {
            Rlog.e(getName(), "Setting suspend state without a NetworkAgent");
        }
        ServiceStateTracker sst = this.mPhone.getServiceStateTracker();
        if (sst.getCurrentDataConnectionState() != 0) {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        } else if (sst.isConcurrentVoiceAndDataAllowed() || (ct = this.mPhone.getCallTracker()) == null || ct.getState() == PhoneConstants.State.IDLE) {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, this.mNetworkInfo.getExtraInfo());
        } else {
            this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.SUSPENDED, null, this.mNetworkInfo.getExtraInfo());
        }
    }

    /* access modifiers changed from: private */
    public class DcInactiveState extends State {
        private DcInactiveState() {
        }

        public void setEnterNotificationParams(ConnectionParams cp, int cause) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams cp,cause");
            DataConnection.this.mConnectionParams = cp;
            DataConnection.this.mDisconnectParams = null;
            DataConnection.this.mDcFailCause = cause;
        }

        public void setEnterNotificationParams(DisconnectParams dp) {
            DataConnection.this.log("DcInactiveState: setEnterNotificationParams dp");
            DataConnection.this.mConnectionParams = null;
            DataConnection.this.mDisconnectParams = dp;
            DataConnection.this.mDcFailCause = 0;
        }

        public void setEnterNotificationParams(int cause) {
            DataConnection.this.mConnectionParams = null;
            DataConnection.this.mDisconnectParams = null;
            DataConnection.this.mDcFailCause = cause;
        }

        public void enter() {
            DataConnection.this.mTag++;
            DataConnection.this.log("DcInactiveState: enter() mTag=" + DataConnection.this.mTag);
            StatsLog.write(75, 1, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
            if (DataConnection.this.mHandoverState == 2) {
                DataConnection.this.mHandoverState = 3;
            }
            if (DataConnection.this.mConnectionParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyConnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                if (DataConnection.this.mDcFailCause != 0) {
                    DataConnection.this.misLastFailed = true;
                }
                DataConnection dataConnection = DataConnection.this;
                dataConnection.notifyConnectCompleted(dataConnection.mConnectionParams, DataConnection.this.mDcFailCause, true);
            }
            if (DataConnection.this.mDisconnectParams != null) {
                DataConnection.this.log("DcInactiveState: enter notifyDisconnectCompleted +ALL failCause=" + DataConnection.this.mDcFailCause);
                DataConnection dataConnection2 = DataConnection.this;
                dataConnection2.notifyDisconnectCompleted(dataConnection2.mDisconnectParams, true);
            }
            if (DataConnection.this.mDisconnectParams == null && DataConnection.this.mConnectionParams == null && DataConnection.this.mDcFailCause != 0) {
                DataConnection.this.log("DcInactiveState: enter notifyAllDisconnectCompleted failCause=" + DataConnection.this.mDcFailCause);
                DataConnection dataConnection3 = DataConnection.this;
                dataConnection3.notifyAllWithEvent(null, 270351, DataFailCause.toString(dataConnection3.mDcFailCause));
            }
            DataConnection.this.mDcController.removeActiveDcByCid(DataConnection.this);
            DataConnection.this.clearSettings();
        }

        public void exit() {
            DataConnection.this.misLastFailed = false;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 262144:
                    DataConnection.this.log("DcInactiveState: mag.what=EVENT_CONNECT");
                    ConnectionParams cp = (ConnectionParams) msg.obj;
                    if (!DataConnection.this.initConnection(cp)) {
                        if (DataConnection.this.misLastFailed && cp.mdefered) {
                            DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT apnContext with defefed msg, not process ");
                            DataConnection.this.notifyConnectCompleted(cp, 65536, false);
                        }
                        DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT initConnection failed");
                        DataConnection.this.notifyConnectCompleted(cp, 65538, false);
                        DataConnection dataConnection = DataConnection.this;
                        dataConnection.transitionTo(dataConnection.mInactiveState);
                        return true;
                    }
                    int cause = DataConnection.this.connect(cp);
                    if (cause != 0) {
                        DataConnection.this.log("DcInactiveState: msg.what=EVENT_CONNECT connect failed");
                        DataConnection.this.notifyConnectCompleted(cp, cause, false);
                        DataConnection dataConnection2 = DataConnection.this;
                        dataConnection2.transitionTo(dataConnection2.mInactiveState);
                        return true;
                    }
                    if (DataConnection.this.mSubId == -1) {
                        DataConnection.this.mSubId = cp.mSubId;
                    }
                    DataConnection dataConnection3 = DataConnection.this;
                    dataConnection3.transitionTo(dataConnection3.mActivatingState);
                    return true;
                case DataConnection.EVENT_DISCONNECT /* 262148 */:
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT");
                    DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                    return true;
                case DataConnection.EVENT_DISCONNECT_ALL /* 262150 */:
                    DataConnection.this.log("DcInactiveState: msg.what=EVENT_DISCONNECT_ALL");
                    DataConnection.this.notifyDisconnectCompleted((DisconnectParams) msg.obj, false);
                    return true;
                case DataConnection.EVENT_RESET /* 262168 */:
                case DataConnection.EVENT_REEVALUATE_RESTRICTED_STATE /* 262169 */:
                    DataConnection dataConnection4 = DataConnection.this;
                    dataConnection4.log("DcInactiveState: msg.what=" + DataConnection.this.getWhatToString(msg.what) + ", ignore we're already done");
                    return true;
                default:
                    DataConnection dataConnection5 = DataConnection.this;
                    dataConnection5.log("DcInactiveState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class DcActivatingState extends State {
        private DcActivatingState() {
        }

        public void enter() {
            StatsLog.write(75, 2, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
            for (ApnContext apnContext : DataConnection.this.mApnContexts.keySet()) {
                if (apnContext.getState() == DctConstants.State.RETRYING) {
                    DataConnection.this.log("DcActivatingState: Set Retrying To Connecting!");
                    apnContext.setState(DctConstants.State.CONNECTING);
                    apnContext.setFailCause(0);
                }
            }
            DataConnection.this.setHandoverState(1);
        }

        public boolean processMessage(Message msg) {
            DataConnection.this.log("DcActivatingState: msg=" + DataConnection.msgToString(msg));
            int i = msg.what;
            if (i == 262144) {
                ((ConnectionParams) msg.obj).mdefered = true;
                DataConnection.this.deferMessage(msg);
                return true;
            } else if (i == DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE) {
                ConnectionParams cp = (ConnectionParams) msg.obj;
                DataCallResponse dataCallResponse = msg.getData().getParcelable("data_call_response");
                SetupResult result = DataConnection.this.onSetupConnectionCompleted(msg.arg1, dataCallResponse, cp);
                boolean isParaValid = (cp == null || dataCallResponse == null) ? false : true;
                if (DataConnection.IS_NR_SLICES_SUPPORTED && !SetupResult.SUCCESS.equals(result) && isParaValid) {
                    ApnContextEx apnContextEx = new ApnContextEx();
                    apnContextEx.setApnContext(cp.mApnContext);
                    DataConnection.this.mHwDataConnectionManager.reportDataFailReason(dataCallResponse.getCause(), apnContextEx);
                }
                if (!(result == SetupResult.ERROR_STALE || DataConnection.this.mConnectionParams == cp)) {
                    DataConnection.this.loge("DcActivatingState: WEIRD mConnectionsParams:" + DataConnection.this.mConnectionParams + " != cp:" + cp);
                }
                DataConnection.this.log("DcActivatingState onSetupConnectionCompleted result=" + result);
                if (cp.mApnContext != null) {
                    cp.mApnContext.requestLog("onSetupConnectionCompleted result=" + result);
                }
                if (!(result == SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR && DataConnection.this.mRilRat == 13)) {
                    DataConnection.this.mEhrpdFailCount = 0;
                }
                int i2 = AnonymousClass2.$SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[result.ordinal()];
                if (i2 == 1) {
                    DataConnection.this.mDcFailCause = 0;
                    DataConnection dataConnection = DataConnection.this;
                    dataConnection.transitionTo(dataConnection.mActiveState);
                } else if (i2 == 2) {
                    DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                    DataConnection dataConnection2 = DataConnection.this;
                    dataConnection2.transitionTo(dataConnection2.mInactiveState);
                    cp.mApnContext.setFailCause(result.mFailCause);
                } else if (i2 == 3) {
                    DataConnection.this.tearDownData(cp);
                    DataConnection dataConnection3 = DataConnection.this;
                    dataConnection3.transitionTo(dataConnection3.mDisconnectingErrorCreatingConnection);
                } else if (i2 == 4) {
                    long delay = DataConnection.this.getSuggestedRetryDelay(dataCallResponse);
                    cp.mApnContext.setModemSuggestedDelay(delay);
                    cp.mApnContext.setFailCause(result.mFailCause);
                    String str = "DcActivatingState: ERROR_DATA_SERVICE_SPECIFIC_ERROR  delay=" + delay + " result=" + result + " result.isRadioRestartFailure=" + DataFailCause.isRadioRestartFailure(DataConnection.this.mPhone.getContext(), result.mFailCause, DataConnection.this.mPhone.getSubId()) + " isPermanentFailure=" + DataConnection.this.mDct.isPermanentFailure(result.mFailCause);
                    DataConnection.this.log(str);
                    if (cp.mApnContext != null) {
                        cp.mApnContext.requestLog(str);
                    }
                    DataConnection.this.mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                    DataConnection dataConnection4 = DataConnection.this;
                    dataConnection4.transitionTo(dataConnection4.mInactiveState);
                    if (DataConnection.HW_SET_EHRPD_DATA && DataConnection.this.mDct.getHwDcTrackerEx().isCTSimCard(DataConnection.this.mPhone.getPhoneId()) && DataConnection.this.mRilRat == 13 && DataFailCause.isRadioRestartFailure(DataConnection.this.mPhone.getContext(), result.mFailCause, DataConnection.this.mPhone.getSubId())) {
                        String apnContextType = cp.mApnContext.getApnType();
                        if (TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContextType) || "mms".equals(apnContextType)) {
                            if (DataConnection.this.mEhrpdFailCount >= 2 || DataFailCause.isPermanentFailure(DataConnection.this.mPhone.getContext(), result.mFailCause, DataConnection.this.mPhone.getSubId())) {
                                DataConnection.this.mPhone.mCi.setEhrpdByQMI(false);
                                DataConnection.this.mEhrpdFailCount = 0;
                                DataConnection.this.logd("ehrpd fail times reaches EHRPD_MAX_RETRY or permanent fail ,disable eHRPD.");
                            } else {
                                DataConnection.access$4008(DataConnection.this);
                            }
                        }
                    }
                } else if (i2 == 5) {
                    DataConnection.this.loge("DcActivatingState: stale EVENT_SETUP_DATA_CONNECTION_DONE tag:" + cp.mTag + " != mTag:" + DataConnection.this.mTag);
                } else {
                    throw new RuntimeException("Unknown SetupResult, should not happen");
                }
                return true;
            } else if (i != DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED) {
                DataConnection.this.log("DcActivatingState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what) + " RefCount=" + DataConnection.this.mApnContexts.size());
                return false;
            } else {
                DataConnection.this.deferMessage(msg);
                return true;
            }
        }
    }

    /* renamed from: com.android.internal.telephony.dataconnection.DataConnection$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult = new int[SetupResult.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.SUCCESS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.ERROR_RADIO_NOT_AVAILABLE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.ERROR_INVALID_ARG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.ERROR_DATA_SERVICE_SPECIFIC_ERROR.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$dataconnection$DataConnection$SetupResult[SetupResult.ERROR_STALE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    /* access modifiers changed from: private */
    public class DcActiveState extends State {
        private DcActiveState() {
        }

        public void enter() {
            int factorySerialNumber;
            DataConnection.this.log("DcActiveState: enter dc=*");
            StatsLog.write(75, 3, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
            DataConnection.this.updateNetworkInfo();
            DataConnection.this.notifyAllWithEvent(null, 270336, PhoneInternalInterface.REASON_CONNECTED);
            if (DataConnection.this.mPhone.getCallTracker() != null) {
                DataConnection.this.mPhone.getCallTracker().registerForVoiceCallStarted(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED, null);
                DataConnection.this.mPhone.getCallTracker().registerForVoiceCallEnded(DataConnection.this.getHandler(), DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED, null);
            }
            DataConnection.this.mDcController.addActiveDcByCid(DataConnection.this);
            DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, DataConnection.this.mNetworkInfo.getReason(), null);
            DataConnection.this.mNetworkInfo.setExtraInfo(DataConnection.this.mApnSetting.getApnName());
            DataConnection dataConnection = DataConnection.this;
            dataConnection.updateTcpBufferSizes(dataConnection.mRilRat);
            NetworkMisc misc = new NetworkMisc();
            if (DataConnection.this.mPhone.getCarrierSignalAgent().hasRegisteredReceivers("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED")) {
                misc.provisioningNotificationDisabled = true;
            }
            misc.subscriberId = DataConnection.this.mPhone.getSubscriberId();
            misc.skip464xlat = DataConnection.this.shouldSkip464Xlat();
            DataConnection dataConnection2 = DataConnection.this;
            dataConnection2.mRestrictedNetworkOverride = dataConnection2.shouldRestrictNetwork();
            DataConnection dataConnection3 = DataConnection.this;
            dataConnection3.mUnmeteredUseOnly = dataConnection3.isUnmeteredUseOnly();
            DataConnection.this.log("mRestrictedNetworkOverride = " + DataConnection.this.mRestrictedNetworkOverride + ", mUnmeteredUseOnly = " + DataConnection.this.mUnmeteredUseOnly);
            if (DataConnection.this.mConnectionParams == null || DataConnection.this.mConnectionParams.mRequestType != 2) {
                DataConnection dataConnection4 = DataConnection.this;
                dataConnection4.mScore = dataConnection4.calculateScore();
                NetworkFactory factory = PhoneFactory.getNetworkFactory(DataConnection.this.mPhone.getPhoneId());
                int factorySerialNumber2 = factory == null ? -1 : factory.getSerialNumber();
                DataConnection dataConnection5 = DataConnection.this;
                if (dataConnection5.isNrSlice(dataConnection5.mConnectionParams)) {
                    factorySerialNumber = -1;
                } else {
                    factorySerialNumber = factorySerialNumber2;
                }
                DataConnection dataConnection6 = DataConnection.this;
                dataConnection6.mNetworkAgent = DcNetworkAgent.createDcNetworkAgent(dataConnection6, dataConnection6.mPhone, DataConnection.this.mNetworkInfo, DataConnection.this.mScore, misc, factorySerialNumber, DataConnection.this.mTransportType);
            } else {
                DataConnection dc = DataConnection.this.mPhone.getDcTracker(DataConnection.this.getHandoverSourceTransport()).getDataConnectionByApnType(DataConnection.this.mConnectionParams.mApnContext.getApnType());
                if (dc != null) {
                    dc.setHandoverState(3);
                }
                if (DataConnection.this.mHandoverSourceNetworkAgent != null) {
                    DataConnection.this.log("Transfer network agent successfully.");
                    DataConnection dataConnection7 = DataConnection.this;
                    dataConnection7.mNetworkAgent = dataConnection7.mHandoverSourceNetworkAgent;
                    DcNetworkAgent dcNetworkAgent = DataConnection.this.mNetworkAgent;
                    DataConnection dataConnection8 = DataConnection.this;
                    dcNetworkAgent.acquireOwnership(dataConnection8, dataConnection8.mTransportType);
                    DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                    DataConnection.this.mNetworkAgent.sendLinkProperties(DataConnection.this.mLinkProperties, DataConnection.this);
                    DataConnection.this.mHandoverSourceNetworkAgent = null;
                } else {
                    DataConnection.this.loge("Failed to get network agent from original data connection");
                    return;
                }
            }
            if (DataConnection.this.mTransportType == 1) {
                DataConnection.this.mPhone.mCi.registerForNattKeepaliveStatus(DataConnection.this.getHandler(), DataConnection.EVENT_KEEPALIVE_STATUS, null);
                DataConnection.this.mPhone.mCi.registerForLceInfo(DataConnection.this.getHandler(), DataConnection.EVENT_LINK_CAPACITY_CHANGED, null);
            }
            TelephonyMetrics.getInstance().writeRilDataCallEvent(DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mCid, DataConnection.this.mApnSetting.getApnTypeBitmask(), 1);
        }

        public void exit() {
            String reason;
            DataConnection dataConnection = DataConnection.this;
            dataConnection.log("DcActiveState: exit dc=" + this);
            DataConnection.this.mNetworkInfo.getReason();
            if (DataConnection.this.mDcController.isExecutingCarrierChange()) {
                reason = PhoneInternalInterface.REASON_CARRIER_CHANGE;
            } else if (DataConnection.this.mDisconnectParams == null || DataConnection.this.mDisconnectParams.mReason == null) {
                reason = DataFailCause.toString(DataConnection.this.mDcFailCause);
            } else {
                reason = DataConnection.this.mDisconnectParams.mReason;
            }
            if (DataConnection.this.mPhone.getCallTracker() != null) {
                DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallStarted(DataConnection.this.getHandler());
                DataConnection.this.mPhone.getCallTracker().unregisterForVoiceCallEnded(DataConnection.this.getHandler());
            }
            if (DataConnection.this.mHandoverState != 2) {
                DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, reason, DataConnection.this.mNetworkInfo.getExtraInfo());
            }
            if (DataConnection.this.mTransportType == 1) {
                DataConnection.this.mPhone.mCi.unregisterForNattKeepaliveStatus(DataConnection.this.getHandler());
                DataConnection.this.mPhone.mCi.unregisterForLceInfo(DataConnection.this.getHandler());
            }
            if (DataConnection.this.mNetworkAgent != null) {
                DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                DataConnection.this.mNetworkAgent.releaseOwnership(DataConnection.this);
            }
            DataConnection.this.keepNetwork = false;
            DataConnection.this.mLastLinkProperties = null;
            DataConnection.this.mNetworkAgent = null;
            TelephonyMetrics.getInstance().writeRilDataCallEvent(DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mCid, DataConnection.this.mApnSetting.getApnTypeBitmask(), 2);
        }

        /* JADX INFO: Multiple debug info for r2v37 int: [D('ar' android.os.AsyncResult), D('slotId' int)] */
        public boolean processMessage(Message msg) {
            int factorySerialNumber = -1;
            switch (msg.what) {
                case 262144:
                    ConnectionParams cp = (ConnectionParams) msg.obj;
                    DataConnection.this.mApnContexts.put(cp.mApnContext, cp);
                    DataConnection.access$5772(DataConnection.this, ~cp.mApnContext.getApnTypeBitmask());
                    DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                    DataConnection dataConnection = DataConnection.this;
                    dataConnection.log("DcActiveState: EVENT_CONNECT cp=" + cp + " dc=" + DataConnection.this);
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendRematchNetworkAndRequests(DataConnection.this.mNetworkInfo);
                    }
                    DataConnection.this.notifyConnectCompleted(cp, 0, false);
                    return true;
                case DataConnection.EVENT_SETUP_DATA_CONNECTION_DONE /* 262145 */:
                case 262146:
                case DataConnection.EVENT_DEACTIVATE_DONE /* 262147 */:
                case DataConnection.EVENT_RIL_CONNECTED /* 262149 */:
                case DataConnection.EVENT_DATA_STATE_CHANGED /* 262151 */:
                case DataConnection.EVENT_TEAR_DOWN_NOW /* 262152 */:
                case 262154:
                case DataConnection.EVENT_DATA_CONNECTION_DRS_OR_RAT_CHANGED /* 262155 */:
                case DataConnection.EVENT_RESET /* 262168 */:
                default:
                    DataConnection dataConnection2 = DataConnection.this;
                    dataConnection2.log("DcActiveState not handled msg.what=" + DataConnection.this.getWhatToString(msg.what));
                    return false;
                case DataConnection.EVENT_DISCONNECT /* 262148 */:
                    DisconnectParams dp = (DisconnectParams) msg.obj;
                    DataConnection.this.log("DcActiveState: EVENT_DISCONNECT dp=*");
                    if (DataConnection.this.mApnContexts.containsKey(dp.mApnContext)) {
                        DataConnection dataConnection3 = DataConnection.this;
                        dataConnection3.log("DcActiveState msg.what=EVENT_DISCONNECT RefCount=" + DataConnection.this.mApnContexts.size());
                        if (DataConnection.this.mApnContexts.size() == 1) {
                            DataConnection.this.mApnContexts.clear();
                            DataConnection.this.mDisconnectParams = dp;
                            DataConnection.this.mConnectionParams = null;
                            dp.mTag = DataConnection.this.mTag;
                            DataConnection.this.tearDownData(dp);
                            DataConnection dataConnection4 = DataConnection.this;
                            dataConnection4.transitionTo(dataConnection4.mDisconnectingState);
                        } else {
                            DataConnection.this.mApnContexts.remove(dp.mApnContext);
                            DataConnection.access$5776(DataConnection.this, dp.mApnContext.getApnTypeBitmask());
                            DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                            DataConnection.this.notifyDisconnectCompleted(dp, false);
                        }
                    } else {
                        if (DataConnection.HW_DBG) {
                            DataConnection dataConnection5 = DataConnection.this;
                            dataConnection5.log("DcActiveState ERROR no such apnContext=" + dp.mApnContext + " in this dc=" + DataConnection.this);
                        }
                        DataConnection.this.notifyDisconnectCompleted(dp, false);
                    }
                    return true;
                case DataConnection.EVENT_DISCONNECT_ALL /* 262150 */:
                    DataConnection dataConnection6 = DataConnection.this;
                    dataConnection6.log("DcActiveState EVENT_DISCONNECT clearing apn contexts, dc=" + DataConnection.this);
                    DisconnectParams dp2 = (DisconnectParams) msg.obj;
                    DataConnection.this.mDisconnectParams = dp2;
                    DataConnection.this.mConnectionParams = null;
                    dp2.mTag = DataConnection.this.mTag;
                    if (DataConnection.this.mApnContexts.size() != 1 || DataConnection.this.mHwCustDataConnection == null || !DataConnection.this.mApnContexts.keySet().stream().anyMatch(new Predicate() {
                        /* class com.android.internal.telephony.dataconnection.$$Lambda$DataConnection$DcActiveState$RGZOweDdQ8aljz2hw7wnWOEaSMc */

                        @Override // java.util.function.Predicate
                        public final boolean test(Object obj) {
                            return DataConnection.DcActiveState.this.lambda$processMessage$0$DataConnection$DcActiveState((ApnContext) obj);
                        }
                    })) {
                        DataConnection.this.tearDownData(dp2);
                    } else {
                        DataConnection.this.log("EVENT_DISCONNECT for emergency apn");
                        DataServiceManager dataServiceManager = DataConnection.this.mDataServiceManager;
                        int i = DataConnection.this.mCid;
                        DataConnection dataConnection7 = DataConnection.this;
                        dataServiceManager.deactivateDataCall(i, 15, dataConnection7.obtainMessage(DataConnection.EVENT_DEACTIVATE_DONE, dataConnection7.mTag, 0, dp2));
                    }
                    DataConnection dataConnection8 = DataConnection.this;
                    dataConnection8.transitionTo(dataConnection8.mDisconnectingState);
                    return true;
                case DataConnection.EVENT_LOST_CONNECTION /* 262153 */:
                    DataConnection dataConnection9 = DataConnection.this;
                    dataConnection9.log("DcActiveState EVENT_LOST_CONNECTION dc=" + DataConnection.this);
                    DataConnection.this.mInactiveState.setEnterNotificationParams(65540);
                    DataConnection dataConnection10 = DataConnection.this;
                    dataConnection10.transitionTo(dataConnection10.mInactiveState);
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_ON /* 262156 */:
                case DataConnection.EVENT_DATA_CONNECTION_ROAM_OFF /* 262157 */:
                case DataConnection.EVENT_DATA_CONNECTION_OVERRIDE_CHANGED /* 262161 */:
                    DataConnection.this.updateNetworkInfo();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                    }
                    return true;
                case DataConnection.EVENT_BW_REFRESH_RESPONSE /* 262158 */:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        DataConnection dataConnection11 = DataConnection.this;
                        dataConnection11.log("EVENT_BW_REFRESH_RESPONSE: error ignoring, e=" + ar.exception);
                    } else {
                        LinkCapacityEstimate lce = (LinkCapacityEstimate) ar.result;
                        NetworkCapabilities nc = DataConnection.this.getNetworkCapabilities();
                        if (DataConnection.this.mPhone.getLceStatus() == 1) {
                            nc.setLinkDownstreamBandwidthKbps(lce.downlinkCapacityKbps);
                            if (DataConnection.this.mNetworkAgent != null) {
                                DataConnection.this.mNetworkAgent.sendNetworkCapabilities(nc, DataConnection.this);
                            }
                        }
                    }
                    return true;
                case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_STARTED /* 262159 */:
                case DataConnection.EVENT_DATA_CONNECTION_VOICE_CALL_ENDED /* 262160 */:
                    DataConnection.this.updateNetworkInfo();
                    DataConnection.this.updateNetworkInfoSuspendState();
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo, DataConnection.this);
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_STATUS /* 262162 */:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception != null) {
                        DataConnection dataConnection12 = DataConnection.this;
                        dataConnection12.loge("EVENT_KEEPALIVE_STATUS: error in keepalive, e=" + ar2.exception);
                    }
                    if (ar2.result != null) {
                        DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus((KeepaliveStatus) ar2.result);
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_STARTED /* 262163 */:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    int slot = msg.arg1;
                    if (ar3.exception != null || ar3.result == null) {
                        DataConnection dataConnection13 = DataConnection.this;
                        dataConnection13.loge("EVENT_KEEPALIVE_STARTED: error starting keepalive, e=" + ar3.exception);
                        DataConnection.this.mNetworkAgent.onSocketKeepaliveEvent(slot, -31);
                    } else {
                        KeepaliveStatus ks = (KeepaliveStatus) ar3.result;
                        if (ks == null) {
                            DataConnection.this.loge("Null KeepaliveStatus received!");
                        } else {
                            DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStarted(slot, ks);
                        }
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_STOPPED /* 262164 */:
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    int handle = msg.arg1;
                    int i2 = msg.arg2;
                    if (ar4.exception != null) {
                        DataConnection dataConnection14 = DataConnection.this;
                        dataConnection14.loge("EVENT_KEEPALIVE_STOPPED: error stopping keepalive for handle=" + handle + " e=" + ar4.exception);
                        DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus(new KeepaliveStatus(3));
                    } else {
                        DataConnection dataConnection15 = DataConnection.this;
                        dataConnection15.log("Keepalive Stop Requested for handle=" + handle);
                        DataConnection.this.mNetworkAgent.keepaliveTracker.handleKeepaliveStatus(new KeepaliveStatus(handle, 1));
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_START_REQUEST /* 262165 */:
                    KeepalivePacketData pkt = (KeepalivePacketData) msg.obj;
                    int slotId = msg.arg1;
                    int intervalMillis = msg.arg2 * 1000;
                    if (DataConnection.this.mTransportType == 1) {
                        DataConnection.this.mPhone.mCi.startNattKeepalive(DataConnection.this.mCid, pkt, intervalMillis, DataConnection.this.obtainMessage(DataConnection.EVENT_KEEPALIVE_STARTED, slotId, 0, null));
                    } else if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.mNetworkAgent.onSocketKeepaliveEvent(msg.arg1, -20);
                    }
                    return true;
                case DataConnection.EVENT_KEEPALIVE_STOP_REQUEST /* 262166 */:
                    int slotId2 = msg.arg1;
                    int handle2 = DataConnection.this.mNetworkAgent.keepaliveTracker.getHandleForSlot(slotId2);
                    if (handle2 < 0) {
                        DataConnection dataConnection16 = DataConnection.this;
                        dataConnection16.loge("No slot found for stopSocketKeepalive! " + slotId2);
                        return true;
                    }
                    DataConnection dataConnection17 = DataConnection.this;
                    dataConnection17.logd("Stopping keepalive with handle: " + handle2);
                    DataConnection.this.mPhone.mCi.stopNattKeepalive(handle2, DataConnection.this.obtainMessage(DataConnection.EVENT_KEEPALIVE_STOPPED, handle2, slotId2, null));
                    return true;
                case DataConnection.EVENT_LINK_CAPACITY_CHANGED /* 262167 */:
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    if (ar5.exception != null) {
                        DataConnection dataConnection18 = DataConnection.this;
                        dataConnection18.loge("EVENT_LINK_CAPACITY_CHANGED e=" + ar5.exception);
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
                            DataConnection.this.mNetworkAgent.sendNetworkCapabilities(nc2, DataConnection.this);
                        }
                    }
                    return true;
                case DataConnection.EVENT_REEVALUATE_RESTRICTED_STATE /* 262169 */:
                    if (DataConnection.this.mRestrictedNetworkOverride && !DataConnection.this.shouldRestrictNetwork()) {
                        DataConnection dataConnection19 = DataConnection.this;
                        dataConnection19.log("Data connection becomes not-restricted. dc=" + this);
                        DataConnection.this.mRestrictedNetworkOverride = false;
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                    }
                    if (DataConnection.this.mUnmeteredUseOnly && !DataConnection.this.isUnmeteredUseOnly()) {
                        DataConnection.this.mUnmeteredUseOnly = false;
                        DataConnection.this.mNetworkAgent.sendNetworkCapabilities(DataConnection.this.getNetworkCapabilities(), DataConnection.this);
                    }
                    return true;
                case DataConnection.EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES /* 262170 */:
                    DataConnection.this.updateScore();
                    return true;
                case DataConnection.EVENT_CLEAR_LINK /* 262171 */:
                    DataConnection.this.log("DcActiveState EVENT_CLEAR_LINK");
                    DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, DataConnection.this.mNetworkInfo.getReason(), DataConnection.this.mNetworkInfo.getExtraInfo());
                    if (DataConnection.this.mNetworkAgent != null) {
                        DataConnection.this.log("DcActiveState EVENT_CLEAR_LINK sendNetworkInfo");
                        DataConnection.this.mNetworkAgent.sendNetworkInfo(DataConnection.this.mNetworkInfo);
                    }
                    DataConnection.this.keepNetwork = true;
                    DataConnection dataConnection20 = DataConnection.this;
                    dataConnection20.mLastLinkProperties = dataConnection20.mLinkProperties;
                    return true;
                case DataConnection.EVENT_RESUME_LINK /* 262172 */:
                    DataConnection.this.log("DcActiveState EVENT_RESUME_LINK");
                    DataConnection.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, DataConnection.this.mNetworkInfo.getReason(), DataConnection.this.mNetworkInfo.getExtraInfo());
                    if ((DataConnection.this.mNetworkAgent == null || DataConnection.this.mLastLinkProperties == null) ? false : true) {
                        DataConnection.this.log("DcActiveState EVENT_RESUME_LINK sendNetworkInfo");
                        NetworkMisc misc = new NetworkMisc();
                        if (DataConnection.this.mPhone.getCarrierSignalAgent().hasRegisteredReceivers("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED")) {
                            misc.provisioningNotificationDisabled = true;
                        }
                        misc.subscriberId = DataConnection.this.mPhone.getSubscriberId();
                        NetworkFactory factory = PhoneFactory.getNetworkFactory(DataConnection.this.mPhone.getPhoneId());
                        if (factory != null) {
                            factorySerialNumber = factory.getSerialNumber();
                        }
                        DataConnection dataConnection21 = DataConnection.this;
                        if (dataConnection21.isNrSlice(dataConnection21.mConnectionParams)) {
                            factorySerialNumber = -1;
                        }
                        DataConnection dataConnection22 = DataConnection.this;
                        dataConnection22.mNetworkAgent = DcNetworkAgent.createDcNetworkAgent(dataConnection22, dataConnection22.mPhone, DataConnection.this.mNetworkInfo, 50, misc, factorySerialNumber, DataConnection.this.mTransportType);
                        DataConnection.this.mLastLinkProperties = null;
                    }
                    DataConnection.this.keepNetwork = false;
                    return true;
            }
        }

        public /* synthetic */ boolean lambda$processMessage$0$DataConnection$DcActiveState(ApnContext apnContext) {
            return DataConnection.this.mHwCustDataConnection.isEmergencyApnSetting(apnContext.getApnSetting());
        }
    }

    /* access modifiers changed from: private */
    public class DcDisconnectingState extends State {
        private DcDisconnectingState() {
        }

        public void enter() {
            StatsLog.write(75, 4, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
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
                    DataConnection.this.mHwCustDataConnection.clearInternetPcoValue(DataConnection.this.mApnSetting.getProfileId(), DataConnection.this.mPhone);
                }
                if (dp.mTag == DataConnection.this.mTag) {
                    DataConnection.this.mInactiveState.setEnterNotificationParams(dp);
                    DataConnection dataConnection = DataConnection.this;
                    dataConnection.transitionTo(dataConnection.mInactiveState);
                } else {
                    DataConnection.this.log("DcDisconnectState stale EVENT_DEACTIVATE_DONE dp.tag=" + dp.mTag + " mTag=" + DataConnection.this.mTag);
                }
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class DcDisconnectionErrorCreatingConnection extends State {
        private DcDisconnectionErrorCreatingConnection() {
        }

        public void enter() {
            StatsLog.write(75, 5, DataConnection.this.mPhone.getPhoneId(), DataConnection.this.mId, DataConnection.this.mApnSetting != null ? (long) DataConnection.this.mApnSetting.getApnTypeBitmask() : 0, DataConnection.this.mApnSetting != null ? DataConnection.this.mApnSetting.canHandleType(17) : false);
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
                DataConnection.this.mInactiveState.setEnterNotificationParams(cp, 65538);
                DataConnection dataConnection2 = DataConnection.this;
                dataConnection2.transitionTo(dataConnection2.mInactiveState);
            } else {
                DataConnection dataConnection3 = DataConnection.this;
                dataConnection3.log("DcDisconnectionErrorCreatingConnection stale EVENT_DEACTIVATE_DONE dp.tag=" + cp.mTag + ", mTag=" + DataConnection.this.mTag);
            }
            return true;
        }
    }

    public void bringUp(ApnContext apnContext, int profileId, int rilRadioTechnology, Message onCompletedMsg, int connectionGeneration, int requestType, int subId) {
        if (HW_DBG) {
            log("bringUp: apnContext=" + apnContext + " onCompletedMsg=" + onCompletedMsg);
        }
        sendMessage(262144, new ConnectionParams(apnContext, profileId, rilRadioTechnology, onCompletedMsg, connectionGeneration, requestType, subId));
    }

    public void tearDown(ApnContext apnContext, String reason, Message onCompletedMsg) {
        log("tearDown: reason=" + reason + " onCompletedMsg=" + onCompletedMsg);
        if (HW_DBG) {
            log("tearDown: apnContext=" + apnContext);
        }
        sendMessage(EVENT_DISCONNECT, new DisconnectParams(apnContext, reason, 2, onCompletedMsg));
    }

    /* access modifiers changed from: package-private */
    public void tearDownNow() {
        log("tearDownNow()");
        sendMessage(obtainMessage(EVENT_TEAR_DOWN_NOW));
    }

    public void tearDownAll(String reason, int releaseType, Message onCompletedMsg) {
        log("tearDownAll: reason=" + reason + ", releaseType=" + releaseType);
        sendMessage(EVENT_DISCONNECT_ALL, new DisconnectParams(null, reason, releaseType, onCompletedMsg));
    }

    public void reset() {
        sendMessage(EVENT_RESET);
        log("reset");
    }

    /* access modifiers changed from: package-private */
    public void reevaluateRestrictedState() {
        sendMessage(EVENT_REEVALUATE_RESTRICTED_STATE);
        log("reevaluate restricted state");
    }

    /* access modifiers changed from: package-private */
    public void reevaluateDataConnectionProperties() {
        sendMessage(EVENT_REEVALUATE_DATA_CONNECTION_PROPERTIES);
        log("reevaluate data connection properties");
    }

    public ConnectionParams getConnectionParams() {
        return this.mConnectionParams;
    }

    public String[] getPcscfAddresses() {
        return this.mPcscfAddr;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getSuggestedRetryDelay(DataCallResponse response) {
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

    public List<ApnContext> getApnContexts() {
        return new ArrayList(this.mApnContexts.keySet());
    }

    /* access modifiers changed from: package-private */
    public DcNetworkAgent getNetworkAgent() {
        return this.mNetworkAgent;
    }

    /* access modifiers changed from: package-private */
    public void setHandoverState(int state) {
        this.mHandoverState = state;
    }

    /* access modifiers changed from: protected */
    public String getWhatToString(int what) {
        return cmdToString(what);
    }

    /* JADX INFO: Multiple debug info for r0v1 java.lang.String: [D('b' java.lang.StringBuilder), D('retVal' java.lang.String)] */
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
        Rlog.i("DC", s);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.i(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Rlog.i(getName(), s);
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

    private boolean isNRConnected() {
        ServiceState serviceState = this.mPhone.getServiceStateTracker().getmSSHw();
        int nsaState = serviceState.getNsaState();
        boolean isNsaConnected = nsaState >= 2 && nsaState <= 5;
        log("isNRConnected, nsaState = " + nsaState + ", nrState = " + serviceState.getNrState());
        return serviceState.getNrState() == 3 || isNsaConnected;
    }

    private void dumpToLog() {
        dump(null, new PrintWriter(new StringWriter(0)) {
            /* class com.android.internal.telephony.dataconnection.DataConnection.AnonymousClass1 */

            @Override // java.io.PrintWriter
            public void println(String s) {
                DataConnection.this.logd(s);
            }

            @Override // java.io.PrintWriter, java.io.Writer, java.io.Flushable
            public void flush() {
            }
        }, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScore() {
        int oldScore = this.mScore;
        this.mScore = calculateScore();
        if (oldScore != this.mScore && this.mNetworkAgent != null) {
            log("Updating score from " + oldScore + " to " + this.mScore);
            this.mNetworkAgent.sendNetworkScore(this.mScore, this);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int calculateScore() {
        int score = 45;
        for (ApnContext apnContext : this.mApnContexts.keySet()) {
            Iterator<NetworkRequest> it = apnContext.getNetworkRequests().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NetworkRequest networkRequest = it.next();
                if (networkRequest.hasCapability(12) && networkRequest.networkCapabilities.getNetworkSpecifier() == null) {
                    score = 50;
                    break;
                }
            }
        }
        if (isMatchAllSlice(this.mConnectionParams)) {
            return 51;
        }
        return score;
    }

    public void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, " ");
        pw.print("DataConnection ");
        DataConnection.super.dump(fd, pw, args);
        pw.flush();
        pw.increaseIndent();
        pw.println("transport type=" + AccessNetworkConstants.transportTypeToString(this.mTransportType));
        pw.println("mApnContexts.size=" + this.mApnContexts.size());
        pw.println("mApnContexts=" + this.mApnContexts);
        pw.println("mApnSetting=" + this.mApnSetting);
        pw.println("mTag=" + this.mTag);
        pw.println("mCid=" + this.mCid);
        pw.println("mConnectionParams=" + this.mConnectionParams);
        pw.println("mDisconnectParams=" + this.mDisconnectParams);
        pw.println("mDcFailCause=" + this.mDcFailCause);
        pw.println("mPhone=" + this.mPhone);
        pw.println("mSubId=" + this.mSubId);
        pw.println("mLinkProperties=" + this.mLinkProperties);
        pw.flush();
        pw.println("mDataRegState=" + this.mDataRegState);
        pw.println("mHandoverState=" + this.mHandoverState);
        pw.println("mRilRat=" + this.mRilRat);
        pw.println("mNetworkCapabilities=" + getNetworkCapabilities());
        pw.println("mCreateTime=" + TimeUtils.logTimeOfDay(this.mCreateTime));
        pw.println("mLastFailTime=" + TimeUtils.logTimeOfDay(this.mLastFailTime));
        pw.println("mLastFailCause=" + this.mLastFailCause);
        pw.println("mUserData=" + this.mUserData);
        pw.println("mSubscriptionOverride=" + Integer.toHexString(this.mSubscriptionOverride));
        pw.println("mRestrictedNetworkOverride=" + this.mRestrictedNetworkOverride);
        pw.println("mUnmeteredUseOnly=" + this.mUnmeteredUseOnly);
        pw.println("mInstanceNumber=" + mInstanceNumber);
        pw.println("mAc=" + this.mAc);
        pw.println("mScore=" + this.mScore);
        DcNetworkAgent dcNetworkAgent = this.mNetworkAgent;
        if (dcNetworkAgent != null) {
            dcNetworkAgent.dump(fd, pw, args);
        }
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

    public void clearLink() {
        sendMessage(EVENT_CLEAR_LINK);
    }

    public void resumeLink() {
        sendMessage(EVENT_RESUME_LINK);
    }

    public LinkProperties getLinkPropertiesHw() {
        return getLinkProperties();
    }

    public void setLinkPropertiesHttpProxyHw(ProxyInfo proxy) {
        setLinkPropertiesHttpProxy(proxy);
    }

    private boolean isMatchAllSlice(ConnectionParams cp) {
        ConnectionParams connectionParams;
        if (!isNrSlice(cp) || (connectionParams = this.mConnectionParams) == null || connectionParams.mApnContext == null || (this.mConnectionParams.mApnContext.getRouteBitmap() & MATCH_ALL) == 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isNrSlice(ConnectionParams cp) {
        if (!IS_NR_SLICES_SUPPORTED || cp == null || cp.mApnContext == null || cp.mApnContext.getApnType() == null) {
            return false;
        }
        return cp.mApnContext.isNrSliceApnContext();
    }
}
