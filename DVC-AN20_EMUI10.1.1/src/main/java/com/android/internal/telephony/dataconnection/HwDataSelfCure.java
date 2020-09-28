package com.android.internal.telephony.dataconnection;

import android.net.LinkProperties;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.HwTelephonyPropertiesInner;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import java.util.regex.PatternSyntaxException;

public class HwDataSelfCure {
    private static final int APN_CHANGE = 2;
    private static final int APN_ROAMING_AIRPLANE_MODE_CHANGE = 4;
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final int DATA_CURE_FAIL = 3;
    private static final boolean DBG = false;
    private static final int DELAY_ONLY_IPV6_CURE = 3000;
    private static final int ESM_FLAG_ATTACH_REJECT = 6;
    private static final int ESM_FLAG_CURED = 5;
    private static final int ESM_FLAG_LTE_NOT_AVAILABLE = 7;
    private static final int ESM_RECVOERY_IN_MS_DEFAULT = 60000;
    private static final int EVENT_ONLY_IPV6_CHR = 1;
    private static final int EVENT_ONLY_IPV6_DETECT = 0;
    private static final int INVAILD_VALUE = -1;
    private static final int IP_TYPE_CURED = 1;
    private static final boolean IS_IPV6_ONLY_CURE_ENABLE = SystemProperties.getBoolean("hw_sc.only_ipv6_allowed_cure", false);
    private static final boolean IS_NETWORK_APN_CURE_ENABLE = SystemProperties.getBoolean("hw_mc.telephony.network_apn_cure", false);
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
    private static final int SECOND_PDP_COUNT_ZERO = 0;
    private static final int SECOND_PDP_DEFAULT_FAIL_COUNT = 10;
    private static final int SECOND_PDP_MAX_FAIL_COUNT = SystemProperties.getInt("ro.config.hw_pdp_cure_fail_count", 10);
    private static final int SECOND_PDP_NUMBER = 2;
    private static final int SINGLE_PDP = 8;
    private String TAG = "DataSelfCure";
    private ApnContextEx mApnContext;
    private ApnSetting mApnSetting;
    private String mCureOperator = "";
    private int mCurrCureReason = 0;
    private int mDataFailCount = 0;
    private DataCureState mDataSelfCureFlag = DataCureState.CURE_IDLE;
    private int mDataSuccessCount = 0;
    private DcTrackerEx mDcTrackerBase;
    private int mEsmFlag = -1;
    private HwDcTrackerEx mHwDcTrackerEx;
    private boolean mIsApnCurePunishing = false;
    private boolean mIsNeedBackOffEsm = false;
    private boolean mIsNetworkApnCure = false;
    private boolean mIsOnlyIpv6Cure = false;
    private boolean mIsRoaminCure = false;
    private boolean mIsSinglePdp = false;
    private boolean mIsSinglePdpCureEnable = true;
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
        this.mPhone = phoneExt;
        HandlerThread localHandlerThread = new HandlerThread("LocalHandler");
        localHandlerThread.start();
        this.mLocalHandler = new LocalHandler(localHandlerThread.getLooper());
    }

    private void logD(String msg) {
        if (this.mDcTrackerBase != null) {
            Rlog.d(this.TAG, msg);
        }
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
        if (50 == i || 51 == i) {
            logD("default APN change backoff protocol cure");
            this.mApnSetting.setProtocol(this.mOldProtocol, this.mIsRoaminCure);
            sendIntentDataSelfCure(this.mOldFailCause, 2);
            clearDataCureInfo();
        }
    }

    private boolean checkNeedDataCure(int failReason, ApnContextEx apnContext) {
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null || phoneExt.getServiceState() == null || apnContext == null) {
            return false;
        }
        boolean isRoaming = this.mPhone.getServiceState().getDataRoaming();
        int protocol = getApnProtocol(apnContext.getApnSetting(), isRoaming);
        if (failReason == 50 || failReason == 51) {
            if (apnContext.getApnSetting() == null || !isProtocolNeedCure(failReason, protocol)) {
                return false;
            }
            logD("start to data self cure ");
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
            logD("we need change esm flag to 1:");
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

    /* access modifiers changed from: private */
    public class LocalHandler extends Handler {
        public LocalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i == 1) {
                    HwDataSelfCure.this.sendOnlyIpv6CureResult(true);
                }
            } else if ((msg.obj instanceof ApnContextEx) && HwDataSelfCure.this.isNeedOnlyIpv6DataCure((ApnContextEx) msg.obj)) {
                HwDataSelfCure.this.executeRestartRadio();
            }
        }
    }

    private boolean isOnlyIpv6Connected(ApnContextEx apnContext) {
        PhoneExt phoneExt;
        if (apnContext == null || (phoneExt = this.mPhone) == null || phoneExt.getServiceState() == null) {
            return false;
        }
        ApnSetting apnSetting = apnContext.getApnSetting();
        boolean isRoaming = this.mPhone.getServiceState().getDataRoaming();
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
            logD("ipv6 only is close");
            return false;
        }
        HwDcTrackerEx hwDcTrackerEx = this.mHwDcTrackerEx;
        if (hwDcTrackerEx != null && "LOADED".equals(hwDcTrackerEx.getSimState()) && isInChina() && "default".equals(apnContext.getApnType()) && isOnlyIpv6Connected(apnContext)) {
            sendOnlyIpv6CureResult(false);
            long now = SystemClock.elapsedRealtime();
            if (TelephonyManager.getDefault().getCallState() == 0) {
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
            logD("start only ipv6 connected cure, restartRadio");
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

    private boolean isInChina() {
        if (this.mDcTrackerBase == null || this.mPhone.getContext() == null) {
            return false;
        }
        String mcc = null;
        if (this.mPhone.getContext().getSystemService("phone") instanceof TelephonyManager) {
            String operatorNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            if (operatorNumeric != null && operatorNumeric.length() > 3) {
                mcc = operatorNumeric.substring(0, 3);
                logD("isInChina current mcc = " + mcc);
            }
            if (CHINA_OPERATOR_MCC.equals(mcc)) {
                return true;
            }
        }
        return false;
    }

    private int getApnProtocol(ApnSetting apnSetting, boolean isRoaming) {
        if (isRoaming) {
            return apnSetting.getRoamingProtocol();
        }
        return apnSetting.getProtocol();
    }

    private boolean isProtocolSingle(int failReason, int protocol) {
        if (failReason == 51 && protocol == 0) {
            logD("need cure for IP ");
            return true;
        } else if (failReason == 50 && protocol == 1) {
            logD("need cure for IPV6 ");
            return true;
        } else {
            logD("can no be cure ");
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
        logD(" default4gSlotId:" + default4gSlotId + ", currentSub:" + currentSub + ", protocol:" + protocol);
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
            logD("set protocol back ");
            ApnSetting apnSetting = this.mApnSetting;
            if (apnSetting != null) {
                apnSetting.setProtocol(this.mOldProtocol, this.mIsRoaminCure);
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
            boolean isRoaming = this.mPhone.getServiceState().getDataRoaming();
            if (this.mDataSelfCureFlag != DataCureState.CURING || apnSetting2 == null || (apnSetting = this.mApnSetting) == null || this.mCurrCureReason != 1) {
                logD("not in data protocol cure state ");
            } else if (apnSetting.getId() == apnSetting2.getId() && isRoaming == this.mIsRoaminCure) {
                logD("UpdateApnInfo: apn protocol update to: " + this.mNewProtocol);
                apnSetting2.setProtocol(this.mNewProtocol, this.mIsRoaminCure);
                this.mDataSelfCureFlag = DataCureState.CURED;
                this.mApnSetting = apnSetting2;
                sendIntentDataSelfCure(this.mOldFailCause, 1);
                logD("UpdateApnInfo: local apnsetting protocol: " + getApnProtocol(this.mApnSetting, this.mIsRoaminCure));
            }
        }
    }

    private boolean isNeedConfigEsmFlag(String operator) {
        TelephonyManager tm;
        if (this.mDcTrackerBase == null || this.mPhone == null || this.mHwDcTrackerEx == null || (tm = TelephonyManager.getDefault()) == null || this.mDcTrackerBase.getTransportType() != 1 || getEsmFlag() != 0 || tm.getCallState() != 0 || this.mHwDcTrackerEx.getPrimarySlot() != this.mPhone.getPhoneId()) {
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
        logD("set esm info to: " + esmInfo + ", old esm:" + this.mEsmFlag);
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
            logD("resetDataCureInfo reason:" + reason + "old FailCause:" + this.mOldFailCause);
            if (reason.equals("apnChanged") || reason.equals("roamingOff") || reason.equals("roamingOn") || reason.equals("airplaneModeOn")) {
                int i = this.mOldFailCause;
                if (i == 50 || i == 51) {
                    ApnSetting apnSetting = this.mApnSetting;
                    if (apnSetting != null) {
                        apnSetting.setProtocol(this.mOldProtocol, this.mIsRoaminCure);
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
        this.mCureOperator = "";
    }

    public void handleNetworkRejectInfo(int rejectRat, int rejectType) {
        logD("handleNetworkRejectInfo rejectRat:" + rejectRat);
        if (rejectType != 4) {
            logD("handleNetworkRejectInfo rejectType:" + rejectType);
        } else if (rejectRat == 2 && this.mDataSelfCureFlag == DataCureState.CURED && this.mCurrCureReason == 2) {
            if (SystemClock.elapsedRealtime() - this.mLastEsmInfoResetTmp < 60000) {
                this.mIsNeedBackOffEsm = true;
                logD("networt reject back off esm");
            }
            TelephonyManager tm = TelephonyManager.getDefault();
            if (tm != null && this.mIsNeedBackOffEsm && tm.getCallState() == 0) {
                logD("networt reject back off now");
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
        if (this.mDcTrackerBase != null && (phoneExt = this.mPhone) != null && phoneExt.getServiceState() != null && this.mPhone.getServiceState().getRilDataRadioTechnology() != 14 && this.mDataSelfCureFlag == DataCureState.CURED && this.mCurrCureReason == 2 && (tm = TelephonyManager.getDefault()) != null && tm.getCallState() == 0) {
            logD("time out is not LTE back off now");
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
            if (TelephonyManager.getDefault().getPhoneCount() > 1) {
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
                logD("no need single pdp cure");
                return;
            }
            int apnHasConnectedNum = this.mDcTrackerBase.getApnStateCount(ApnContextEx.StateEx.CONNECTED);
            logD("current activate apn is " + apnHasConnectedNum);
            if (cause == DcFailCause.NONE.getErrorCode()) {
                if (apnHasConnectedNum != 1) {
                    this.mMultiPdpSuccCount++;
                    this.mSecondPdpFailCount = 0;
                    if (this.mMultiPdpSuccCount > 0) {
                        logD("second pdp activate success:" + this.mMultiPdpSuccCount);
                        setSinglePdpCureState(false);
                        setSinglePdpAllow(false);
                    }
                }
            } else if (apnHasConnectedNum == 1) {
                this.mSecondPdpFailCount++;
                logD("second pdp fail count:" + this.mSecondPdpFailCount);
                if (this.mSecondPdpFailCount >= SECOND_PDP_MAX_FAIL_COUNT) {
                    setSinglePdpAllow(true);
                    sendIntentDataSelfCure(0, 8);
                }
            }
        }
    }

    private void setSinglePdpAllow(boolean isSinglePdpAllowed) {
        this.mIsSinglePdp = isSinglePdpAllowed;
        DcTrackerEx dcTrackerEx = this.mDcTrackerBase;
        if (dcTrackerEx != null) {
            dcTrackerEx.setSinglePdpAllow(isSinglePdpAllow());
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
            logD("old pdp scheme");
            return false;
        }
        boolean isEnablePdpInOldEmui = this.mHwDcTrackerEx.isMultiPdpPlmnMatched(this.mHwDcTrackerEx.getOperatorNumeric());
        PhoneExt phoneExt = this.mPhone;
        int phoneId = phoneExt != null ? phoneExt.getPhoneId() : 0;
        boolean isCtCard = this.mHwDcTrackerEx.isCTSimCard(phoneId);
        boolean isDisableMultiPdps = SystemProperties.getBoolean(HwTelephonyPropertiesInner.PROP_SINGLE_PDP_HPLMN_MATCHED + phoneId, false);
        if (isEnablePdpInOldEmui || isDisableMultiPdps || isCtCard) {
            return false;
        }
        return true;
    }

    private void sendIntentDataSelfCure(int oldFailCause, int uploadReason) {
        logD("pdn cure send intent date to CHR apk");
        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentDataSelfCure(oldFailCause, uploadReason);
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
        logD("NetworkApnCure isSameApn: " + isSameApn);
        if (isSameApn || isWapApn(attachedApnSetting.getApnName()) || !isMatchedPdnRej(cause) || !this.mHwDcTrackerEx.isLTENetworks() || !isInChina()) {
            return false;
        }
        logD("NetworkApnCure subid: " + this.mPhone.getPhoneId());
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
        logD("mIsApnCurePunishing = " + this.mIsApnCurePunishing);
        return true;
    }

    public void apnCureFlagReset() {
        if (IS_NETWORK_APN_CURE_ENABLE) {
            logD("reset apn cure flags");
            this.mPdnRejApnSettings = null;
            this.mIsApnCurePunishing = false;
            this.mIsNetworkApnCure = false;
        }
    }

    private boolean isWapApn(String apn) {
        if (apn == null) {
            logD("apn is null.");
            return false;
        } else if (!apn.toLowerCase().contains("wap")) {
            return false;
        } else {
            logD("apn contain string wap.");
            return true;
        }
    }

    public ApnSetting getRegApnForCure(ApnSetting apnSetting) {
        HwDcTrackerEx hwDcTrackerEx;
        ApnSetting attachedApnSettings;
        if (this.mDcTrackerBase == null || (hwDcTrackerEx = this.mHwDcTrackerEx) == null || !this.mIsNetworkApnCure || (attachedApnSettings = hwDcTrackerEx.getAttachedApnSetting()) == null || this.mPdnRejApnSettings == null || !this.mHwDcTrackerEx.isLTENetworks()) {
            return null;
        }
        logD("rej_apn = " + this.mPdnRejApnSettings.getApnName() + ",req_apn = " + apnSetting.getApnName());
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
            String pdnRejCause = SystemProperties.get("hw_mc.telephony.pdn_rej_cause", "");
            for (String rcau : pdnRejCause.split(",")) {
                if (Integer.toString(failCause).equals(rcau)) {
                    logD("rej cause: " + pdnRejCause + ",ErrorCode matched: " + failCause);
                    return true;
                }
            }
            return false;
        } catch (PatternSyntaxException ex) {
            logD("Exception get pdn rej cause " + ex);
            return false;
        }
    }
}
