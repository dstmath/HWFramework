package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.internal.telephony.HwTelephonyPropertiesInner;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import com.huawei.internal.telephony.dataconnection.DcFailCauseEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import huawei.cust.HwCfgFilePolicy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

public class HwDataSelfCure {
    private static final int APN_CHANGE = 2;
    private static final int APN_ROAMING_AIRPLANE_MODE_CHANGE = 4;
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final int DATA_CURE_FAIL = 3;
    private static final int DATA_SELF_CURE_INIT_ACTION = Integer.MAX_VALUE;
    private static final int DATA_SELF_CURE_RESTART_RADIO_ACTION = 0;
    private static final boolean DBG = false;
    private static final int DELAY_ONLY_IPV6_CURE = 3000;
    private static final int DEND_SELF_CURE_CFG_STRING_ARRAY_LENGTH = 4;
    private static final int ESM_FLAG_ATTACH_REJECT = 6;
    private static final int ESM_FLAG_CURED = 5;
    private static final int ESM_FLAG_LTE_NOT_AVAILABLE = 7;
    private static final int ESM_FLAG_OFF = 0;
    private static final int ESM_FLAG_ON = 1;
    private static final int ESM_RECVOERY_IN_MS_DEFAULT = 60000;
    private static final int EVENT_DEND_SELF_CURE_CHECK = 2;
    private static final int EVENT_ONLY_IPV6_CHR = 1;
    private static final int EVENT_ONLY_IPV6_DETECT = 0;
    private static final int INVAILD_VALUE = -1;
    private static final int IP_TYPE_CURED = 1;
    private static boolean IS_IPV6_ONLY_CURE_ENABLE = false;
    private static final boolean IS_NETWORK_APN_CURE_ENABLE = SystemPropertiesEx.getBoolean("hw_mc.telephony.network_apn_cure", false);
    private static final int LTE_RAT = 2;
    private static final int MAX_DATA_RETRY = 1;
    private static final int MCC_LENGTH = 3;
    private static final int MULTI_CONN_TO_SAME_PDN_NOT_ALLOW_CURE = 2;
    private static final int NETWORK_APN_CURE = 4;
    private static final int ONLY_IPV6_CURE_IS_ACTIVE = 9;
    private static final int ONLY_IPV6_CURE_NOT_ACTIVE = 10;
    private static final int PROTOCOL_ERROR_CURE = 1;
    private static final int REJECT_TYPE_ATTACH = 4;
    private static final int RESTART_NO_IPV4_CURE_TIME = 14400000;
    private static final int RESTART_RADIO_PUNISH_TIME_IN_MS = 14400000;
    private static final int SECOND_PDP_COUNT_ZERO = 0;
    private static final int SECOND_PDP_DEFAULT_FAIL_COUNT = 10;
    private static int SECOND_PDP_MAX_FAIL_COUNT = 10;
    private static final int SECOND_PDP_NUMBER = 2;
    private static final int SINGLE_PDP = 8;
    private static final int TIME_UNIT_SECOND_TO_MS = 1000;
    private String TAG = "DataSelfCure";
    private ApnSetting mApnSetting;
    private String mCureOperator = BuildConfig.FLAVOR;
    private int mCurrCureReason = 0;
    private int mDataFailCount = 0;
    private DataCureState mDataSelfCureFlag = DataCureState.CURE_IDLE;
    private int mDataSuccessCount = 0;
    private DcTrackerEx mDcTrackerBase;
    private int mDendFailCount = 0;
    private Map<Integer, DendSelfCureCfg> mDendSelfCureCfg = new HashMap();
    private long mDendTimeStamp = 0;
    private int mEsmFlag = -1;
    private HwDcTrackerEx mHwDcTrackerEx;
    private boolean mIsApnCurePunishing = false;
    private boolean mIsNeedBackOffEsm = false;
    private boolean mIsNetworkApnCure = false;
    private boolean mIsOnlyIpv6Cure = false;
    private boolean mIsRoaminCure = false;
    private boolean mIsSinglePdp = false;
    private boolean mIsSinglePdpCureEnable = true;
    private String mLastDendCfgItem = null;
    private int mLastDendFailCause = 0;
    private long mLastEsmInfoResetTmp = 0;
    private long mLastNoIpv4CureTimestamp = 0;
    private LocalHandler mLocalHandler;
    private int mMultiPdpSuccCount = 0;
    private int mNewProtocol = 2;
    private int mOldFailCause = 0;
    private int mOldProtocol = 0;
    private ApnSetting mPdnRejApnSettings = null;
    private PhoneExt mPhone;
    private int mSecondPdpFailCount = 0;

    /* access modifiers changed from: private */
    public enum DataCureState {
        CURE_IDLE,
        CURING,
        CURED,
        CURE_DISABLING,
        CURE_DISABLE
    }

    public HwDataSelfCure(DcTrackerEx dcTrackerBase, HwDcTrackerEx dcTrackerEx, PhoneExt phoneExt) {
        this.mDcTrackerBase = dcTrackerBase;
        this.mHwDcTrackerEx = dcTrackerEx;
        HwDcTrackerEx hwDcTrackerEx = this.mHwDcTrackerEx;
        if (hwDcTrackerEx != null) {
            SECOND_PDP_MAX_FAIL_COUNT = hwDcTrackerEx.getPropIntParams("ro.config.hw_pdp_cure_fail_count", 10);
            IS_IPV6_ONLY_CURE_ENABLE = this.mHwDcTrackerEx.getPropBooleanParams("hw_sc.only_ipv6_allowed_cure", false);
        }
        this.mPhone = phoneExt;
        HandlerThread localHandlerThread = new HandlerThread("LocalHandler");
        localHandlerThread.start();
        this.mLocalHandler = new LocalHandler(localHandlerThread.getLooper());
    }

    private void log(String msg) {
        RlogEx.i(this.TAG, msg);
    }

    private void loge(String msg) {
        RlogEx.e(this.TAG, msg);
    }

    public boolean isNeedDataCure(int cause, ApnContextEx apnContext) {
        HwDcTrackerEx hwDcTrackerEx;
        if (apnContext == null || (hwDcTrackerEx = this.mHwDcTrackerEx) == null) {
            return false;
        }
        String simState = hwDcTrackerEx.getSimState();
        ApnSetting apnSetting = apnContext.getApnSetting();
        if (apnSetting == null || !"LOADED".equals(simState)) {
            return false;
        }
        checkSinglePdpCure(cause);
        if (!apnContext.getApnType().equals("default")) {
            return false;
        }
        if (this.mDataSelfCureFlag == DataCureState.CURE_IDLE) {
            return checkNeedDataCure(cause, apnContext);
        }
        if (this.mDataSelfCureFlag != DataCureState.CURED) {
            return false;
        }
        if (this.mApnSetting != null && apnSetting.getId() != this.mApnSetting.getId()) {
            clearInfoWhenApnChanged();
            return false;
        } else if (cause == 0) {
            this.mDataSuccessCount++;
            return false;
        } else if (isNeedApnCurePunish(apnContext)) {
            return false;
        } else {
            this.mDataFailCount++;
            if (this.mDataFailCount >= 1 && this.mDataSuccessCount == 0) {
                backOffToOringinal();
            }
            return false;
        }
    }

    private void clearInfoWhenApnChanged() {
        int i = this.mOldFailCause;
        if (i == 50 || i == 51) {
            log("default APN change backoff protocol cure");
            ApnSettingHelper.setProtocol(this.mApnSetting, this.mOldProtocol, this.mIsRoaminCure);
            sendIntentDataSelfCure(this.mOldFailCause, 2);
            clearDataCureInfo();
        }
    }

    private boolean checkNeedDataCure(int failReason, ApnContextEx apnContext) {
        PhoneExt phoneExt;
        if (this.mDcTrackerBase == null || (phoneExt = this.mPhone) == null || phoneExt.getServiceState() == null || apnContext == null) {
            return false;
        }
        boolean isRoaming = ServiceStateEx.getDataRoaming(this.mPhone.getServiceState());
        int protocol = getApnProtocol(apnContext.getApnSetting(), isRoaming);
        if (failReason == 50 || failReason == 51) {
            if (!isProtocolNeedCure(failReason, protocol)) {
                return false;
            }
            log("start to data self cure ");
            saveOldProtocolParams(apnContext.getApnSetting(), isRoaming);
            this.mOldFailCause = failReason;
            this.mCurrCureReason = 1;
            return true;
        } else if (failReason != 55) {
            return isNeedNetworkApnCure(failReason, apnContext);
        } else {
            String operator = this.mHwDcTrackerEx.getOperatorNumeric();
            if (!isNeedConfigEsmFlag(operator)) {
                return false;
            }
            log("we need change esm flag to 1:");
            this.mCureOperator = operator;
            this.mDataSelfCureFlag = DataCureState.CURED;
            this.mLastEsmInfoResetTmp = SystemClock.elapsedRealtime();
            this.mOldFailCause = failReason;
            this.mCurrCureReason = 2;
            resetEsmFlag();
            sendIntentDataSelfCure(this.mOldFailCause, 5);
            startEsmFlagCureTimer();
            return true;
        }
    }

    public void checkOnlyIpv6Cure(ApnContextEx apnContext) {
        if (isOnlyIpv6Connected(apnContext)) {
            this.mLocalHandler.sendMessageDelayed(this.mLocalHandler.obtainMessage(0, apnContext), 3000);
            return;
        }
        this.mLocalHandler.sendMessage(this.mLocalHandler.obtainMessage(1));
    }

    private boolean isOnlyIpv6Connected(ApnContextEx apnContext) {
        PhoneExt phoneExt;
        if (apnContext == null || (phoneExt = this.mPhone) == null || phoneExt.getServiceState() == null) {
            return false;
        }
        ApnSetting apnSetting = apnContext.getApnSetting();
        boolean isRoaming = ServiceStateEx.getDataRoaming(this.mPhone.getServiceState());
        if (apnSetting == null) {
            return false;
        }
        int protocol = getApnProtocol(apnSetting, isRoaming);
        if (apnContext.isDisconnected() || protocol != 2 || apnContext.isIpv4Connected() || !apnContext.isIpv6Connected()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedOnlyIpv6DataCure(ApnContextEx apnContext) {
        if (!IS_IPV6_ONLY_CURE_ENABLE) {
            log("ipv6 only is close");
            return false;
        }
        HwDcTrackerEx hwDcTrackerEx = this.mHwDcTrackerEx;
        if (hwDcTrackerEx != null && "LOADED".equals(hwDcTrackerEx.getSimState()) && this.mHwDcTrackerEx.isInChina() && "default".equals(apnContext.getApnType()) && isOnlyIpv6Connected(apnContext)) {
            sendOnlyIpv6CureResult(false);
            long now = SystemClock.elapsedRealtime();
            if (TelephonyManagerEx.getDefault().getCallState() == 0) {
                long j = this.mLastNoIpv4CureTimestamp;
                if (now - j > 14400000 || j == 0) {
                    this.mLastNoIpv4CureTimestamp = now;
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void executeRestartRadio() {
        if (this.mDcTrackerBase != null) {
            log("start only ipv6 connected cure, restartRadio");
            this.mIsOnlyIpv6Cure = true;
            this.mDcTrackerBase.sendRestartRadio();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendOnlyIpv6CureResult(boolean isActive) {
        if (this.mIsOnlyIpv6Cure) {
            if (!isActive) {
                sendIntentDataSelfCure(0, 10);
            } else {
                sendIntentDataSelfCure(0, 9);
            }
            this.mIsOnlyIpv6Cure = false;
        }
    }

    private int getApnProtocol(ApnSetting apnSetting, boolean isRoaming) {
        if (isRoaming) {
            return apnSetting.getRoamingProtocol();
        }
        return apnSetting.getProtocol();
    }

    private boolean isProtocolSingle(int failReason, int protocol) {
        if (failReason == 51 && protocol == 0) {
            log("need cure for IP ");
            return true;
        } else if (failReason == 50 && protocol == 1) {
            log("need cure for IPV6 ");
            return true;
        } else {
            log("can no be cure ");
            return false;
        }
    }

    private boolean isProtocolNeedCure(int failReason, int protocol) {
        PhoneExt phoneExt;
        if (this.mDcTrackerBase == null || (phoneExt = this.mPhone) == null || this.mHwDcTrackerEx == null) {
            return false;
        }
        int currentSub = phoneExt.getPhoneId();
        int default4gSlotId = this.mHwDcTrackerEx.getPrimarySlot();
        log(" default4gSlotId:" + default4gSlotId + ", currentSub:" + currentSub + ", protocol:" + protocol);
        if (!isProtocolSingle(failReason, protocol) || currentSub != default4gSlotId) {
            return false;
        }
        return true;
    }

    private void saveOldProtocolParams(ApnSetting apnSetting, boolean isRoaming) {
        if (apnSetting != null) {
            this.mOldProtocol = getApnProtocol(apnSetting, isRoaming);
            this.mIsRoaminCure = isRoaming;
            this.mDataSelfCureFlag = DataCureState.CURING;
            this.mApnSetting = apnSetting;
        }
    }

    private void backOffToOringinal() {
        int i = this.mOldFailCause;
        if (i == 50 || i == 51) {
            log("set protocol back ");
            ApnSetting apnSetting = this.mApnSetting;
            if (apnSetting != null) {
                ApnSettingHelper.setProtocol(apnSetting, this.mOldProtocol, this.mIsRoaminCure);
                sendIntentDataSelfCure(this.mOldFailCause, 3);
            }
            clearDataCureInfo();
            this.mDataSelfCureFlag = DataCureState.CURE_DISABLE;
        }
    }

    public void updateDataCureProtocol(ApnContextEx apn) {
        PhoneExt phoneExt;
        ApnSetting apnSetting;
        if (this.mDcTrackerBase != null && (phoneExt = this.mPhone) != null && phoneExt.getServiceState() != null && apn != null) {
            ApnSetting apnSetting2 = apn.getApnSetting();
            boolean isRoaming = ServiceStateEx.getDataRoaming(this.mPhone.getServiceState());
            if (this.mDataSelfCureFlag != DataCureState.CURING || apnSetting2 == null || (apnSetting = this.mApnSetting) == null || this.mCurrCureReason != 1) {
                log("not in data protocol cure state ");
            } else if (apnSetting.getId() == apnSetting2.getId() && isRoaming == this.mIsRoaminCure) {
                log("UpdateApnInfo: apn protocol update to: " + this.mNewProtocol);
                ApnSettingHelper.setProtocol(apnSetting2, this.mNewProtocol, this.mIsRoaminCure);
                this.mDataSelfCureFlag = DataCureState.CURED;
                this.mApnSetting = apnSetting2;
                sendIntentDataSelfCure(this.mOldFailCause, 1);
                log("UpdateApnInfo: local apnsetting protocol: " + getApnProtocol(this.mApnSetting, this.mIsRoaminCure));
            }
        }
    }

    private boolean isNeedConfigEsmFlag(String operator) {
        TelephonyManager tm;
        if (this.mDcTrackerBase == null || this.mPhone == null || this.mHwDcTrackerEx == null || (tm = TelephonyManagerEx.getDefault()) == null || this.mDcTrackerBase.getTransportType() != 1 || getEsmFlag() != 0 || tm.getCallState() != 0 || this.mHwDcTrackerEx.getPrimarySlot() != this.mPhone.getPhoneId()) {
            return false;
        }
        return true;
    }

    public int getDataCureEsmFlag(String operator) {
        if (operator == null || this.mCureOperator == null || this.mCurrCureReason != 2 || this.mDataSelfCureFlag != DataCureState.CURED || !operator.equals(this.mCureOperator)) {
            return 0;
        }
        return 1;
    }

    private int getEsmFlag() {
        return this.mEsmFlag;
    }

    public void setEsmFlag(int esmInfo) {
        log("set esm info to: " + esmInfo + ", old esm:" + this.mEsmFlag);
        this.mEsmFlag = esmInfo;
    }

    private void resetEsmFlag() {
        DcTrackerEx dcTrackerEx = this.mDcTrackerBase;
        if (dcTrackerEx != null) {
            dcTrackerEx.setInitialAttachApn();
        }
    }

    public void resetDataCureInfo(String reason) {
        if (this.mDcTrackerBase != null && this.mPhone != null && !TextUtils.isEmpty(reason)) {
            log("resetDataCureInfo reason:" + reason + "old FailCause:" + this.mOldFailCause);
            if (reason.equals("apnChanged") || reason.equals("roamingOff") || reason.equals("roamingOn") || reason.equals("airplaneModeOn")) {
                int i = this.mOldFailCause;
                if (i == 50 || i == 51) {
                    ApnSetting apnSetting = this.mApnSetting;
                    if (apnSetting != null) {
                        ApnSettingHelper.setProtocol(apnSetting, this.mOldProtocol, this.mIsRoaminCure);
                        sendIntentDataSelfCure(this.mOldFailCause, 4);
                    }
                    boolean isDataCureDisable = false;
                    if (this.mDataSelfCureFlag == DataCureState.CURE_DISABLE) {
                        isDataCureDisable = true;
                    }
                    clearDataCureInfo();
                    if (isDataCureDisable) {
                        this.mDataSelfCureFlag = DataCureState.CURE_DISABLE;
                    }
                }
            } else if (reason.equals("simLoaded") || reason.equals("simNotReady")) {
                clearDataCureInfo();
                clearSinglePdpCureState();
            }
        }
    }

    private void clearDataCureInfo() {
        this.mOldProtocol = -1;
        this.mDataSuccessCount = 0;
        this.mDataFailCount = 0;
        this.mIsRoaminCure = false;
        this.mDataSelfCureFlag = DataCureState.CURE_IDLE;
        this.mApnSetting = null;
        this.mCurrCureReason = 0;
        this.mOldFailCause = 0;
        this.mCureOperator = BuildConfig.FLAVOR;
    }

    public void handleNetworkRejectInfo(int rejectRat, int rejectType) {
        log("handleNetworkRejectInfo rejectRat:" + rejectRat);
        if (rejectType != 4) {
            log("handleNetworkRejectInfo rejectType:" + rejectType);
        } else if (rejectRat == 2 && this.mDataSelfCureFlag == DataCureState.CURED && this.mCurrCureReason == 2) {
            if (SystemClock.elapsedRealtime() - this.mLastEsmInfoResetTmp < 60000) {
                this.mIsNeedBackOffEsm = true;
                log("network reject back off esm");
            }
            TelephonyManager tm = TelephonyManagerEx.getDefault();
            if (tm != null && this.mIsNeedBackOffEsm && tm.getCallState() == 0) {
                log("network reject back off now");
                sendIntentDataSelfCure(this.mOldFailCause, 6);
                clearDataCureInfo();
                this.mDataSelfCureFlag = DataCureState.CURE_DISABLE;
                resetEsmFlag();
                this.mIsNeedBackOffEsm = false;
            }
        }
    }

    private void startEsmFlagCureTimer() {
        DcTrackerEx dcTrackerEx = this.mDcTrackerBase;
        if (dcTrackerEx != null) {
            this.mDcTrackerBase.sendMessageDelayed(dcTrackerEx.obtainMessage(271149, (Object) null), 60000);
        }
    }

    public void esmFlagCureTimerTimeOut() {
        PhoneExt phoneExt;
        TelephonyManager tm;
        if (this.mDcTrackerBase != null && (phoneExt = this.mPhone) != null && phoneExt.getServiceState() != null && ServiceStateEx.getRilDataRadioTechnology(this.mPhone.getServiceState()) != 14 && this.mDataSelfCureFlag == DataCureState.CURED && this.mCurrCureReason == 2 && (tm = TelephonyManagerEx.getDefault()) != null && tm.getCallState() == 0) {
            log("time out is not LTE back off now");
            sendIntentDataSelfCure(this.mOldFailCause, 7);
            clearDataCureInfo();
            this.mDataSelfCureFlag = DataCureState.CURE_DISABLE;
            resetEsmFlag();
        }
    }

    public void init() {
        if (this.mDcTrackerBase != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("-");
            sb.append(this.mDcTrackerBase.getTransportType() == 1 ? "C" : "I");
            String tagSuffix = sb.toString();
            if (TelephonyManagerEx.getDefault().getPhoneCount() > 1) {
                tagSuffix = tagSuffix + "-" + this.mPhone.getPhoneId();
            }
            this.TAG = "DataSelfCure" + tagSuffix;
        }
    }

    private void checkSinglePdpCure(int cause) {
        if (this.mDcTrackerBase != null && this.mIsSinglePdpCureEnable && !this.mIsSinglePdp) {
            if (!isNeedCureForSinglePdp()) {
                setSinglePdpCureState(false);
                setSinglePdpAllow(false);
                log("no need single pdp cure");
                return;
            }
            int apnHasConnectedNum = this.mDcTrackerBase.getApnStateCount(ApnContextEx.StateEx.CONNECTED);
            log("current activate apn is " + apnHasConnectedNum);
            if (cause == DcFailCauseEx.NONE.getErrorCode()) {
                if (apnHasConnectedNum != 1) {
                    this.mMultiPdpSuccCount++;
                    this.mSecondPdpFailCount = 0;
                    if (this.mMultiPdpSuccCount > 0) {
                        log("second pdp activate success:" + this.mMultiPdpSuccCount);
                        setSinglePdpCureState(false);
                        setSinglePdpAllow(false);
                    }
                }
            } else if (apnHasConnectedNum == 1) {
                this.mSecondPdpFailCount++;
                log("second pdp fail count:" + this.mSecondPdpFailCount);
                if (this.mSecondPdpFailCount >= SECOND_PDP_MAX_FAIL_COUNT) {
                    setSinglePdpAllow(true);
                    sendIntentDataSelfCure(0, 8);
                }
            }
        }
    }

    private void setSinglePdpCureState(boolean isCureState) {
        this.mIsSinglePdpCureEnable = isCureState;
    }

    private boolean isSinglePdpAllow() {
        if (!this.mIsSinglePdpCureEnable) {
            return false;
        }
        return this.mIsSinglePdp;
    }

    private void setSinglePdpAllow(boolean isSinglePdpAllowed) {
        this.mIsSinglePdp = isSinglePdpAllowed;
        DcTrackerEx dcTrackerEx = this.mDcTrackerBase;
        if (dcTrackerEx != null) {
            dcTrackerEx.setSinglePdpAllow(isSinglePdpAllow());
        }
    }

    private void clearSinglePdpCureState() {
        setSinglePdpCureState(true);
        setSinglePdpAllow(false);
        this.mSecondPdpFailCount = 0;
        this.mMultiPdpSuccCount = 0;
    }

    private boolean isNeedCureForSinglePdp() {
        if (this.mDcTrackerBase == null || this.mHwDcTrackerEx == null) {
            return false;
        }
        if (!HwTelephonyPropertiesInner.ENABLE_NEW_PDP_SCHEME) {
            log("old pdp scheme");
            return false;
        }
        boolean isEnablePdpInOldEmui = isMultiPdpPlmnMatched(this.mHwDcTrackerEx.getOperatorNumeric());
        int phoneId = this.mPhone.getPhoneId();
        boolean isCtCard = this.mHwDcTrackerEx.isCTSimCard(phoneId);
        boolean isDisableMultiPdps = SystemPropertiesEx.getBoolean("gsm.singlepdp.hplmn.matched" + phoneId, false);
        if (isEnablePdpInOldEmui || isDisableMultiPdps || isCtCard) {
            return false;
        }
        return true;
    }

    private boolean isMultiPdpPlmnMatched(String operator) {
        String plmnsConfig = Settings.System.getString(this.mPhone.getContext().getContentResolver(), "mpdn_plmn_matched_by_network");
        if (!TextUtils.isEmpty(plmnsConfig)) {
            String[] plmns = plmnsConfig.split(",");
            for (String plmn : plmns) {
                if (!TextUtils.isEmpty(plmn) && plmn.equals(operator)) {
                    return true;
                }
            }
            return false;
        }
        log("mpdn_plmn_matched_by_network is empty");
        return true;
    }

    private void sendIntentDataSelfCure(int oldFailCause, int uploadReason) {
        log("pdn cure send intent date to CHR apk");
        DcTrackerEx.sendIntentDataSelfCure(oldFailCause, uploadReason);
    }

    private boolean isNeedNetworkApnCure(int cause, ApnContextEx apn) {
        if (!IS_NETWORK_APN_CURE_ENABLE || this.mIsApnCurePunishing || apn == null) {
            return false;
        }
        ApnSetting attachedApnSetting = this.mHwDcTrackerEx.getAttachedApnSetting();
        ApnSetting apnSetting = apn.getApnSetting();
        if (apnSetting == null || attachedApnSetting == null) {
            return false;
        }
        boolean isSameApn = attachedApnSetting.getApnName().equalsIgnoreCase(apnSetting.getApnName());
        log("NetworkApnCure isSameApn: " + isSameApn);
        if (isSameApn || isWapApn(attachedApnSetting.getApnName()) || !isMatchedPdnRej(cause) || !this.mHwDcTrackerEx.isLTENetworks() || !this.mHwDcTrackerEx.isInChina()) {
            return false;
        }
        log("NetworkApnCure phoneid: " + this.mPhone.getPhoneId());
        this.mPdnRejApnSettings = apnSetting;
        this.mIsNetworkApnCure = true;
        return true;
    }

    private boolean isNeedApnCurePunish(ApnContextEx apn) {
        if (this.mPdnRejApnSettings == null || apn.getApnSetting() == null || !this.mPdnRejApnSettings.getApnName().equalsIgnoreCase(apn.getApnSetting().getApnName())) {
            return false;
        }
        this.mDataSelfCureFlag = DataCureState.CURE_IDLE;
        this.mIsApnCurePunishing = true;
        this.mIsNetworkApnCure = false;
        log("mIsApnCurePunishing = " + this.mIsApnCurePunishing);
        return true;
    }

    public void apnCureFlagReset() {
        if (IS_NETWORK_APN_CURE_ENABLE) {
            log("reset apn cure flags");
            this.mPdnRejApnSettings = null;
            this.mIsApnCurePunishing = false;
            this.mIsNetworkApnCure = false;
        }
    }

    private boolean isWapApn(String apn) {
        if (apn == null) {
            log("apn is null.");
            return false;
        } else if (!apn.toLowerCase(Locale.ENGLISH).contains("wap")) {
            return false;
        } else {
            log("apn contain string wap.");
            return true;
        }
    }

    public ApnSetting getRegApnForCure(ApnSetting apnSetting) {
        HwDcTrackerEx hwDcTrackerEx;
        ApnSetting attachedApnSettings;
        if (apnSetting == null || this.mDcTrackerBase == null || (hwDcTrackerEx = this.mHwDcTrackerEx) == null || !this.mIsNetworkApnCure || (attachedApnSettings = hwDcTrackerEx.getAttachedApnSetting()) == null || this.mPdnRejApnSettings == null || !this.mHwDcTrackerEx.isLTENetworks()) {
            return null;
        }
        log("rej_apn = " + this.mPdnRejApnSettings.getApnName() + ",req_apn = " + apnSetting.getApnName());
        if (!this.mPdnRejApnSettings.getApnName().equalsIgnoreCase(apnSetting.getApnName())) {
            return null;
        }
        this.mDataSelfCureFlag = DataCureState.CURED;
        this.mCurrCureReason = 4;
        ApnSetting networkCureApn = new ApnSetting.Builder().setEntryName("Emergency").setProtocol(attachedApnSettings.getProtocol()).setApnName(attachedApnSettings.getApnName()).setApnTypeBitmask(17).build();
        this.mDcTrackerBase.sendIntentWhenApnNeedReport(this.mPhone, this.mPdnRejApnSettings, 512, new LinkProperties());
        return networkCureApn;
    }

    private boolean isMatchedPdnRej(int failCause) {
        try {
            String pdnRejCause = SystemPropertiesEx.get("hw_mc.telephony.pdn_rej_cause", BuildConfig.FLAVOR);
            for (String rcau : pdnRejCause.split(",")) {
                if (Integer.toString(failCause).equals(rcau)) {
                    log("rej cause: " + pdnRejCause + ",ErrorCode matched: " + failCause);
                    return true;
                }
            }
            return false;
        } catch (PatternSyntaxException e) {
            log("Exception get pdn rej cause");
            return false;
        }
    }

    private String[] stringToStringArray(String stringText, String splitTag) {
        if (TextUtils.isEmpty(stringText)) {
            log("stringToStringArray,input string is null");
            return new String[0];
        }
        String[] stringArray = stringText.split(splitTag);
        if (stringArray.length == 0) {
            log("stringArray length is 0 after replace space.");
            return new String[0];
        }
        for (String str : stringArray) {
            if (TextUtils.isEmpty(str)) {
                return new String[0];
            }
        }
        return stringArray;
    }

    private Map<Integer, DendSelfCureCfg> getDendSelfCureCfg() {
        String cfgItemString;
        int slotId;
        int slotId2 = SubscriptionControllerEx.getInstance().getSlotIndex(this.mPhone.getSubId());
        String cfgItemString2 = (String) HwCfgFilePolicy.getValue("dend_data_self_cure_strategy", slotId2, String.class);
        if (cfgItemString2 != null) {
            if (cfgItemString2.length() <= 600) {
                String trimedCfgItemString = cfgItemString2.trim();
                if (trimedCfgItemString.equals(this.mLastDendCfgItem)) {
                    return this.mDendSelfCureCfg;
                }
                this.mDendSelfCureCfg.clear();
                this.mLastDendCfgItem = trimedCfgItemString;
                String[] cfgItemStringArray = stringToStringArray(trimedCfgItemString, ";");
                int length = cfgItemStringArray.length;
                int j = 0;
                int i = 0;
                while (i < length) {
                    String[] cfgStringArray = stringToStringArray(cfgItemStringArray[i], ":");
                    if (cfgStringArray.length != 4) {
                        log("DendSelfCureCfg cfgItemStringArray.length wrong");
                        slotId = slotId2;
                        cfgItemString = cfgItemString2;
                    } else {
                        try {
                            String[] cfgFailCauseArr = stringToStringArray(cfgStringArray[j], ",");
                            DendSelfCureCfg dendSelfCureCfg = new DendSelfCureCfg(Long.parseLong(cfgStringArray[1]), Integer.parseInt(cfgStringArray[2]), Integer.parseInt(cfgStringArray[3]));
                            while (true) {
                                slotId = slotId2;
                                try {
                                    if (j >= cfgFailCauseArr.length) {
                                        break;
                                    }
                                    cfgItemString = cfgItemString2;
                                    try {
                                        this.mDendSelfCureCfg.put(Integer.valueOf(Integer.parseInt(cfgFailCauseArr[j])), dendSelfCureCfg);
                                        j++;
                                        slotId2 = slotId;
                                        cfgFailCauseArr = cfgFailCauseArr;
                                        cfgItemString2 = cfgItemString;
                                    } catch (NumberFormatException e) {
                                        loge("getDendSelfCureCfg fail");
                                        i++;
                                        slotId2 = slotId;
                                        cfgItemString2 = cfgItemString;
                                        j = 0;
                                    }
                                } catch (NumberFormatException e2) {
                                    cfgItemString = cfgItemString2;
                                    loge("getDendSelfCureCfg fail");
                                    i++;
                                    slotId2 = slotId;
                                    cfgItemString2 = cfgItemString;
                                    j = 0;
                                }
                            }
                            cfgItemString = cfgItemString2;
                        } catch (NumberFormatException e3) {
                            slotId = slotId2;
                            cfgItemString = cfgItemString2;
                            loge("getDendSelfCureCfg fail");
                            i++;
                            slotId2 = slotId;
                            cfgItemString2 = cfgItemString;
                            j = 0;
                        }
                    }
                    i++;
                    slotId2 = slotId;
                    cfgItemString2 = cfgItemString;
                    j = 0;
                }
                return this.mDendSelfCureCfg;
            }
        }
        this.mLastDendCfgItem = null;
        this.mDendSelfCureCfg.clear();
        return this.mDendSelfCureCfg;
    }

    public void checkDataSelfCureAfterDisconnect(int failReason) {
        this.mLocalHandler.sendMessage(this.mLocalHandler.obtainMessage(2, Integer.valueOf(failReason)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dendDataSelfCureCheck(int failReason) {
        int dataSelfCureAction = getDataSelfCureAction(failReason);
        log("dendDataSelfCureCheck DendTimeStamp:" + this.mDendTimeStamp + " mDendFailCount:" + this.mDendFailCount + " dataSelfCureAction:" + dataSelfCureAction);
        if (dataSelfCureAction != 0) {
            loge("dendDataSelfCureCheck dataSelfCureAction:" + dataSelfCureAction);
            return;
        }
        DcTrackerEx dcTrackerEx = this.mDcTrackerBase;
        if (dcTrackerEx != null) {
            dcTrackerEx.sendRestartRadio();
            this.mHwDcTrackerEx.updateLastDoRecoveryTimestamp(4);
        }
    }

    private boolean isAllowedDendDataSelfCure(long now) {
        int current4gSlotId = this.mHwDcTrackerEx.getPrimarySlot();
        if (TelephonyManagerEx.getDefault().getCallState() != 0 || current4gSlotId != this.mPhone.getPhoneId()) {
            return false;
        }
        if ((now - this.mHwDcTrackerEx.getLastRadioResetTimestamp() >= 14400000 || this.mHwDcTrackerEx.getLastRadioResetTimestamp() == 0) && this.mDcTrackerBase.getTransportType() == 1) {
            return true;
        }
        return false;
    }

    private int getDataSelfCureAction(int failCause) {
        if (failCause == 0 || this.mLastDendFailCause != failCause || !getDendSelfCureCfg().containsKey(Integer.valueOf(failCause))) {
            resetDataSelfCurecfgParam(failCause);
            return Integer.MAX_VALUE;
        }
        this.mDendFailCount++;
        long now = SystemClock.elapsedRealtime();
        if (!isAllowedDendDataSelfCure(now)) {
            return Integer.MAX_VALUE;
        }
        DendSelfCureCfg dendRetryStrategy = this.mDendSelfCureCfg.get(Integer.valueOf(failCause));
        long configTimer = dendRetryStrategy.getConfigTimer();
        int configCount = dendRetryStrategy.getConfigCount();
        int configAction = dendRetryStrategy.getConfigAction();
        if (now - this.mDendTimeStamp > 1000 * configTimer) {
            resetDataSelfCurecfgParam(failCause);
            return Integer.MAX_VALUE;
        } else if (this.mDendFailCount <= configCount) {
            return Integer.MAX_VALUE;
        } else {
            resetDataSelfCurecfgParam(failCause);
            return configAction;
        }
    }

    private void resetDataSelfCurecfgParam(int failCause) {
        this.mLastDendFailCause = failCause;
        this.mDendTimeStamp = SystemClock.elapsedRealtime();
        log("resetDataSelfCurecfgParam failCause" + failCause);
        this.mDendFailCount = 1;
    }

    /* access modifiers changed from: private */
    public static class DendSelfCureCfg {
        private static final int ACTION_INDEX = 3;
        private static final int COUNT_INDEX = 2;
        private static final int DEND_SELF_CURE_CFG_MAX_LEN = 600;
        private static final int FAIL_CAUSE_LIST_INDEX = 0;
        private static final String STRING_COLON = ":";
        private static final String STRING_COMMA = ",";
        private static final String STRING_SEMICOLON = ";";
        private static final int TIMER_INDEX = 1;
        private static final String UNSOLICTIED_DEND_SELF_CURE_CFG = "dend_data_self_cure_strategy";
        private int mConfigAction = 0;
        private int mConfigCount = 0;
        private long mConfigTimer = 0;

        public DendSelfCureCfg(long configTimer, int configCount, int configAction) {
            this.mConfigTimer = configTimer;
            this.mConfigCount = configCount;
            this.mConfigAction = configAction;
        }

        public long getConfigTimer() {
            return this.mConfigTimer;
        }

        public int getConfigCount() {
            return this.mConfigCount;
        }

        public int getConfigAction() {
            return this.mConfigAction;
        }
    }

    /* access modifiers changed from: private */
    public class LocalHandler extends Handler {
        public LocalHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i == 1) {
                    HwDataSelfCure.this.sendOnlyIpv6CureResult(true);
                } else if (i == 2 && (msg.obj instanceof Integer)) {
                    HwDataSelfCure.this.dendDataSelfCureCheck(((Integer) msg.obj).intValue());
                }
            } else if ((msg.obj instanceof ApnContextEx) && HwDataSelfCure.this.isNeedOnlyIpv6DataCure((ApnContextEx) msg.obj)) {
                HwDataSelfCure.this.executeRestartRadio();
            }
        }
    }
}
