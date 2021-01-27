package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;

public abstract class AbstractPhoneBase extends Handler implements PhoneInternalInterface {
    public static final int BUFFER_SIZE = 120;
    public static final int DEVICE_ID_MASK_ALL = 2;
    public static final int DEVICE_ID_MASK_IMEI = 1;
    private static final int DEVICE_ID_RETRY_COUNT = 5;
    private static final int DEVICE_ID_RETRY_INTERVAL = 6000;
    public static final int EVENT_CSCON_MODE_INFO = 116;
    public static final int EVENT_ECC_NUM = 104;
    protected static final int EVENT_GET_CALL_FORWARD_TIMER_DONE = 110;
    public static final int EVENT_GET_IMSI_DONE = 105;
    public static final int EVENT_GET_LTE_RELEASE_VERSION_DONE = 108;
    public static final int EVENT_GET_NVCFG_RESULT_INFO_DONE = 115;
    public static final int EVENT_HW_CUST_BASE = 100;
    public static final int EVENT_HW_LAA_STATE_CHANGED = 112;
    public static final int EVENT_RETRY_GET_DEVICE_ID = 1000;
    public static final int EVENT_RPLMNS_STATE_CHANGED = 117;
    protected static final int EVENT_SET_CALL_FORWARD_TIMER_DONE = 109;
    public static final int EVENT_SET_MODE_TO_AUTO = 111;
    public static final int EVENT_UNSOL_HW_CALL_ALT_SRV_DONE = 113;
    public static final int EVENT_UNSOL_SIM_NVCFG_FINISHED = 114;
    public static final int EVENT_UPLOAD_OPERATOR_INFO = 118;
    public static final int HW_ENCRYPT_CALL = 0;
    public static final int HW_KMC_REMOTE_COMMUNICATION = 1;
    private static final int INVALID_SCOPE = -1;
    private static final boolean IS_CHINA_TELECOM = (SystemProperties.get("ro.config.hw_opta", ProxyController.MODEM_0).equals("92") && SystemProperties.get("ro.config.hw_optb", ProxyController.MODEM_0).equals("156"));
    private static final boolean IS_RESTORE_AUTO = SystemProperties.getBoolean("ro.hwpp.restore_auto_ct_manual", false);
    private static final String LOG_TAG = "HwPhoneBase";
    public static final int LTE_RELEASE_VERSION_R10 = 1;
    public static final int LTE_RELEASE_VERSION_R10_WITH_HIGH_THROUGHPUT = 3;
    public static final int LTE_RELEASE_VERSION_R9 = 0;
    public static final int LTE_RELEASE_VERSION_R9_WITH_HIGH_THROUGHPUT = 2;
    public static final int SET_TO_AOTO_TIME = 5000;
    public static final int SPEECH_INFO_CODEC_NB = 1;
    public static final int SPEECH_INFO_CODEC_WB = 2;
    private CommandsInterface mAbstractPhoneBaseCi;
    private boolean mNeedShowOOS = false;
    private HwPhoneBaseReference mReference = HwTelephonyFactory.getHwTelephonyBaseManager().createHwPhoneBaseReference(this);
    private int speechInfoCodec = 1;

    public interface HwPhoneBaseReference {
        int getDataRoamingScope();

        boolean setDataRoamingScope(int i);
    }

    public abstract int getPhoneId();

    protected AbstractPhoneBase() {
    }

    protected AbstractPhoneBase(CommandsInterface ci) {
        this.mAbstractPhoneBaseCi = ci;
    }

    public static boolean isNrServiceOn(int curPrefMode) {
        return judgeNrAbilityByHwNetworkType(curPrefMode) || judgeNrAbilityByNetworkType(curPrefMode);
    }

    public static boolean judgeNrAbilityByHwNetworkType(int curPrefMode) {
        return curPrefMode == 64 || curPrefMode == 65 || curPrefMode == 66 || curPrefMode == 67 || curPrefMode == 68 || curPrefMode == 69;
    }

    public static boolean judgeNrAbilityByNetworkType(int curPrefMode) {
        return curPrefMode == 23 || curPrefMode == 24 || curPrefMode == 26 || curPrefMode == 28 || curPrefMode == 29 || curPrefMode == 30 || curPrefMode == 31 || curPrefMode == 32 || curPrefMode == 25 || curPrefMode == 27 || curPrefMode == 33;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public boolean isSupportCFT() {
        Rlog.d(LOG_TAG, "isSupportCFT should be override by subclass");
        return false;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setCallForwardingUncondTimerOption(int startHour, int startMinute, int endHour, int endMinute, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, Message onComplete) {
        Rlog.d(LOG_TAG, "setCallForwardingUncondTimerOption should be override by subclass");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public boolean getImsSwitch() {
        Rlog.d(LOG_TAG, "getImsSwitch should be override by subclass");
        return false;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setImsSwitch(boolean on) {
        Rlog.d(LOG_TAG, "setImsSwitch should be override by subclass");
    }

    public void retryGetDeviceId(int curRetryCount, int deviceIdMask) {
        int count = curRetryCount + 1;
        if (count <= 5) {
            Rlog.i(LOG_TAG, "retryGetDeviceId:" + count + " try after " + DEVICE_ID_RETRY_INTERVAL + "ms");
            sendMessageDelayed(obtainMessage(1000, count, deviceIdMask, null), 6000);
        }
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface, com.android.internal.telephony.AbstractPhoneInternalInterface
    public String getMeid() {
        Rlog.d(LOG_TAG, "[HwPhoneBase] getMeid() is a CDMA method");
        return "Meid";
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public String getNVESN() {
        return this.mAbstractPhoneBaseCi.getNVESN();
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public String getPesn() {
        Rlog.e(LOG_TAG, "[HwPhoneBase] getPesn() is a CDMA method");
        return ProxyController.MODEM_0;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getCallbarringOption(String facility, String serviceClass, Message response) {
        Rlog.e(LOG_TAG, "[AbstractPhoneBase] getCallbarringOption.");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setCallbarringOption(String facility, String password, boolean isActivate, String serviceClass, Message response) {
        Rlog.e(LOG_TAG, "[AbstractPhoneBase] setCallbarringOption");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getCallbarringOption(String facility, int serviceClass, Message response) {
        Rlog.e(LOG_TAG, "[AbstractPhoneBase] getCallbarringOption...");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setCallbarringOption(String facility, String password, boolean isActivate, int serviceClass, Message response) {
        Rlog.e(LOG_TAG, "[AbstractPhoneBase] setCallbarringOption...");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getCallForwardingOption(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
        Rlog.e(LOG_TAG, "[AbstractPhoneBase] getCallForwardingOption");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void changeBarringPassword(String oldPassword, String newPassword, Message response) {
        Rlog.e(LOG_TAG, "[AbstractPhoneBase] changeBarringPassword");
    }

    /* access modifiers changed from: protected */
    public String getVoiceMailNumberHwCust() {
        Rlog.e(LOG_TAG, "[HwPhoneBase] getVoiceMailNumberHwCust() is a CDMA method");
        return PhoneConfigurationManager.SSSS;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void closeRrc() {
        Rlog.e(LOG_TAG, "[HwPhoneBase] closeRrc()");
    }

    public void cleanDeviceId() {
        Rlog.d(LOG_TAG, "cleanDeviceId should be override by subclass");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void registerForUnsolSpeechInfo(Handler h, int what, Object obj) {
        this.mAbstractPhoneBaseCi.registerForUnsolSpeechInfo(h, what, obj);
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void unregisterForUnsolSpeechInfo(Handler h) {
        this.mAbstractPhoneBaseCi.unregisterForUnsolSpeechInfo(h);
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public String getSpeechInfoCodec() {
        String ret = PhoneConfigurationManager.SSSS;
        if (this.speechInfoCodec == 2) {
            ret = "incall_wb=on";
        }
        if (this.speechInfoCodec == 1) {
            return "incall_wb=off";
        }
        return ret;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setSpeechInfoCodec(int speechinfocodec) {
        this.speechInfoCodec = speechinfocodec;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void switchVoiceCallBackgroundState(int state) {
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public boolean isMmiCode(String dialString) {
        return false;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getPOLCapabilty(Message response) {
        Rlog.d(LOG_TAG, "getPOLCapabilty should be override by subclass");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void getPreferedOperatorList(Message response) {
        Rlog.d(LOG_TAG, "getPreferedOperatorList should be override by subclass");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        Rlog.d(LOG_TAG, "setPOLEntry should be override by subclass");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void riseCdmaCutoffFreq(boolean on) {
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void setLTEReleaseVersion(int state, Message response) {
        Rlog.d(LOG_TAG, "setLTEReleaseVersion should be override by subclass");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public int getLteReleaseVersion() {
        Rlog.d(LOG_TAG, "getLteReleaseVersion should be override by subclass");
        return -1;
    }

    public void setOOSFlagOnSelectNetworkManually(boolean flag) {
        this.mNeedShowOOS = flag;
    }

    public boolean getOOSFlag() {
        return this.mNeedShowOOS;
    }

    public void restoreNetworkSelectionAuto() {
        if (IS_RESTORE_AUTO && IS_CHINA_TELECOM && HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == getPhoneId()) {
            Rlog.d(LOG_TAG, "set mode to automatic for ct when received manual complete");
            Message message = Message.obtain(this);
            message.what = 111;
            sendMessageDelayed(message, 5000);
        }
    }

    public void hasNetworkSelectionAuto() {
        if (IS_RESTORE_AUTO && IS_CHINA_TELECOM && HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == getPhoneId() && hasMessages(111)) {
            Rlog.d(LOG_TAG, "remove EVENT_SET_MODE_TO_AUTO");
            removeMessages(111);
        }
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public int getDataRoamingScope() {
        HwPhoneBaseReference hwPhoneBaseReference = this.mReference;
        if (hwPhoneBaseReference != null) {
            return hwPhoneBaseReference.getDataRoamingScope();
        }
        return -1;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public boolean setDataRoamingScope(int scope) {
        HwPhoneBaseReference hwPhoneBaseReference = this.mReference;
        if (hwPhoneBaseReference != null) {
            return hwPhoneBaseReference.setDataRoamingScope(scope);
        }
        return false;
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public boolean setISMCOEX(String setISMCoex) {
        Rlog.d(LOG_TAG, "setISMCOEX should be override by subclass");
        return false;
    }

    public void setImsDomainConfig(int domainType) {
        Rlog.d(LOG_TAG, "setImsDomainConfig should be override by subclass");
    }

    public void getImsDomain(Message response) {
        Rlog.d(LOG_TAG, "getImsDomain should be override by subclass");
    }

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message response) {
        Rlog.d(LOG_TAG, "handleUiccAuth should be override by subclass");
    }

    public void handleMapconImsaReq(byte[] Msg) {
        Rlog.d(LOG_TAG, "handleMapconImsaReq should be override by subclass");
    }

    public void setCSGNetworkSelectionModeManual(byte[] data, Message response) {
        Rlog.e(LOG_TAG, "[HwPhoneBase] setCSGNetworkSelectionModeManual() is a GSM method");
    }

    @Override // com.android.internal.telephony.AbstractPhoneInternalInterface
    public void selectCsgNetworkManually(Message response) {
        Rlog.e(LOG_TAG, "[HwPhoneBase] selectCsgNetworkManually() is a GSM method");
    }

    public void registerForHWBuffer(Handler h, int what, Object obj) {
        Rlog.v(LOG_TAG, "PhoneBase.registerForHWBuffer() >>h: " + h + ", what: " + what);
        this.mAbstractPhoneBaseCi.registerForHWBuffer(h, what, obj);
    }

    public void unregisterForHWBuffer(Handler h) {
        Rlog.v(LOG_TAG, "PhoneBase.unregisterForHWBuffer() >>h: " + h);
        this.mAbstractPhoneBaseCi.unregisterForHWBuffer(h);
    }

    public void sendHWSolicited(Message reqMsg, int event, byte[] reqData) {
        if (event < 0) {
            Rlog.w(LOG_TAG, "sendHWSolicited() event not less than 0 ");
        } else if (reqData == null || reqData.length > 120) {
            Rlog.w(LOG_TAG, "sendHWSolicited() reqData is null or length overstep");
        } else {
            this.mAbstractPhoneBaseCi.sendHWBufferSolicited(reqMsg, event, reqData);
        }
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return this.mAbstractPhoneBaseCi.cmdForECInfo(event, action, buf);
    }

    public void registerForCallAltSrv(Handler h, int what, Object obj) {
        CommandsInterface commandsInterface = this.mAbstractPhoneBaseCi;
        if (commandsInterface != null) {
            commandsInterface.registerForCallAltSrv(h, what, obj);
        }
    }

    public void unregisterForCallAltSrv(Handler h) {
        CommandsInterface commandsInterface = this.mAbstractPhoneBaseCi;
        if (commandsInterface != null) {
            commandsInterface.unregisterForCallAltSrv(h);
        }
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        Rlog.d(LOG_TAG, "notifyCellularCommParaReady should be override by subclass");
    }
}
