package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.WorkSource;
import android.telephony.Rlog;

public abstract class AbstractBaseCommands {
    private static final String LOG_TAG = "AbstractBaseCommands";
    protected Object crrConnRet = null;
    protected Object lastMimoStatus = null;
    protected RegistrantList m256QamStatusRegistrants = new RegistrantList();
    protected RegistrantList m4RMimoStatusRegistrants = new RegistrantList();
    protected RegistrantList mBalongModemResetRegistrants = new RegistrantList();
    protected RegistrantList mCaStateChangedRegistrants = new RegistrantList();
    protected RegistrantList mCallAltSrvRegistrants = new RegistrantList();
    protected Registrant mCommonImsaToMapconInfoRegistrant;
    protected final RegistrantList mDSDSModeStateRegistrants = new RegistrantList();
    protected Registrant mECCNumRegistrant;
    protected final RegistrantList mHWBufferRegistrants = new RegistrantList();
    protected RegistrantList mHwAntiFakeBaseStationRegistrants = new RegistrantList();
    protected final RegistrantList mHwCrrConnIndRegistrants = new RegistrantList();
    protected RegistrantList mHwRestartRildStatusRegistrants = new RegistrantList();
    protected final RegistrantList mHwRrcConnStateRegistrants = new RegistrantList();
    protected Registrant mIccSimSwitchStartRegistrant;
    protected Registrant mIccSimSwitchStopRegistrant;
    protected RegistrantList mIccidChangedRegistrants = new RegistrantList();
    protected RegistrantList mLaaStateChangeRegistrants = new RegistrantList();
    protected Object mLast256QamStatus = null;
    protected RegistrantList mLimitPDPActRegistrants = new RegistrantList();
    protected RegistrantList mModemCapRegistrants = new RegistrantList();
    protected RegistrantList mMtStatusReportRegistrants = new RegistrantList();
    protected RegistrantList mNetRejectRegistrant = new RegistrantList();
    protected RegistrantList mNvCfgFinishedRegistrants = new RegistrantList();
    protected RegistrantList mOperatorRegistrants = new RegistrantList();
    protected RegistrantList mRSrvccStateRegistrants = new RegistrantList();
    protected final RegistrantList mRegPLMNSelInfoRegistrants = new RegistrantList();
    protected RegistrantList mReportVpStatusRegistrants = new RegistrantList();
    protected Registrant mRestartRildNvMatchRegistrant;
    protected RegistrantList mSimHotPlugRegistrants = new RegistrantList();
    protected RegistrantList mSpeechInfoRegistrants = new RegistrantList();
    protected RegistrantList mUlfreqStateRegistrants = new RegistrantList();
    protected RegistrantList mUnsolRplmnsStateRegistrant = new RegistrantList();
    protected Registrant mVsimApDsFlowInfoRegistrant;
    protected Registrant mVsimDsFlowInfoRegistrant;
    protected Registrant mVsimRDHRegistrant;
    protected Registrant mVsimRegPLMNSelInfoRegistrant;
    protected Registrant mVsimTimerTaskExpiredRegistrant;

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public boolean getImsSwitch() {
        return false;
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setImsSwitch(boolean on) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setNrSwitch(boolean on, Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void iccGetATR(Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void resetProfile(Message response) {
    }

    public void sendCloudMessageToModem(int event_id) {
    }

    public void sendPseudocellCellInfo(int infoType, int lac, int cid, int radiotech, String plmn, Message result) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setPowerGrade(int powerGrade, Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setWifiTxPowerGrade(int powerGrade, Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void sendSMSSetLong(int flag, Message result) {
    }

    public String getNVESN() {
        return null;
    }

    public void registerForUnsolSpeechInfo(Handler h, int what, Object obj) {
        this.mSpeechInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForUnsolSpeechInfo(Handler h) {
        this.mSpeechInfoRegistrants.remove(h);
    }

    public void switchVoiceCallBackgroundState(int state, Message result) {
    }

    public void registerForReportVpStatus(Handler h, int what, Object obj) {
        this.mReportVpStatusRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForReportVpStatus(Handler h) {
        this.mReportVpStatusRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setVpMask(int vpMask, Message result) {
    }

    public void riseCdmaCutoffFreq(boolean on, Message msg) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getPOLCapabilty(Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getCurrentPOLList(Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void dataConnectionDetach(int mode, Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void dataConnectionAttach(int mode, Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setNrSaState(int on, Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getNrSaState(Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void restartRild(Message result) {
    }

    public void getModemSupportVSimVersion(Message result) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void supplyDepersonalization(String netpin, int type, Message result) {
    }

    public void setLTEReleaseVersion(int state, Message result) {
    }

    public void getLteReleaseVersion(Message result) {
    }

    public void queryCardType(Message msg) {
    }

    public void updateStackBinding(int stackId, int enable, Message response) {
    }

    public void getModemCapability(Message response) {
    }

    public void registerForModemCapEvent(Handler h, int what, Object obj) {
    }

    public void switchBalongSim(int modem1ToSlot, int modem2ToSlot, Message result) {
    }

    public boolean getAntiFakeBaseStation(Message onComplete) {
        return false;
    }

    public void switchBalongSim(int modem1ToSlot, int modem2ToSlot, int modem3ToSlot, Message result) {
    }

    public void getBalongSim(Message response) {
    }

    public void getSimHotPlugState(Message result) {
    }

    public void unregisterForModemCapEvent(Handler h) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setCdmaModeSide(int modemID, Message result) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getCdmaModeSide(Message result) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void registerForUimLockcard(Handler h, int what, Object obj) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void unregisterForUimLockcard(Handler h) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void resetAllConnections() {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void requestSetEmergencyNumbers(String ecclist_withcard, String ecclist_nocard) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void queryEmergencyNumbers() {
    }

    public void setOnECCNum(Handler h, int what, Object obj) {
        this.mECCNumRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnECCNum(Handler h) {
        this.mECCNumRegistrant.clear();
    }

    public void registerForRplmnsStateChanged(Handler h, int what, Object obj) {
        this.mUnsolRplmnsStateRegistrant.add(new Registrant(h, what, obj));
    }

    public void unregisterForRplmnsStateChanged(Handler h) {
        this.mUnsolRplmnsStateRegistrant.remove(h);
    }

    public void getICCID(Message response) {
    }

    public void setActiveModemMode(int mode, Message result) {
    }

    public void fastSwitchBalongSim(int modem1ToSlot, int modem2ToSlot, Message result) {
    }

    public void setFastSimSwitchInProcess(boolean on, Message response) {
    }

    public void setSimMode(int index, int enable, int slotID, Message response) {
    }

    public void getSimMode(Message response) {
    }

    public void getRegPlmn(Message response) {
    }

    public void getTrafficData(Message response) {
    }

    public void clearTrafficData(Message response) {
    }

    public void setApDsFlowCfg(int config, int threshold, int total_threshold, int oper, Message response) {
    }

    public void setDsFlowNvCfg(int enable, int interval, Message response) {
    }

    public void setApDsFlowCfg(Message response) {
    }

    public void setDsFlowNvCfg(Message response) {
    }

    public void getSimStateViaSysinfoEx(Message response) {
    }

    public void getDevSubMode(Message response) {
    }

    public void setTEEDataReady(int apn, int dh, int sim, Message response) {
    }

    public boolean isFastSwitchInProcess() {
        return false;
    }

    public void hvCheckCard(Message response) {
    }

    public void setOnVsimRDH(Handler h, int what, Object obj) {
        this.mVsimRDHRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnVsimRDH(Handler h) {
        this.mVsimRDHRegistrant.clear();
    }

    public void setOnVsimRegPLMNSelInfo(Handler h, int what, Object obj) {
        this.mVsimRegPLMNSelInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnVsimRegPLMNSelInfo(Handler h) {
        this.mVsimRegPLMNSelInfoRegistrant.clear();
    }

    public void setOnVsimDsFlowInfo(Handler h, int what, Object obj) {
        this.mVsimDsFlowInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnVsimDsFlowInfo(Handler h) {
        this.mVsimDsFlowInfoRegistrant.clear();
    }

    public void setOnVsimApDsFlowInfo(Handler h, int what, Object obj) {
        this.mVsimApDsFlowInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnVsimApDsFlowInfo(Handler h) {
        this.mVsimApDsFlowInfoRegistrant.clear();
    }

    public void setOnVsimTimerTaskExpired(Handler h, int what, Object obj) {
        this.mVsimTimerTaskExpiredRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnVsimTimerTaskExpired(Handler h) {
        this.mVsimTimerTaskExpiredRegistrant.clear();
    }

    public void setOnRestartRildNvMatch(Handler h, int what, Object obj) {
        this.mRestartRildNvMatchRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnRestartRildNvMatch(Handler h) {
        this.mRestartRildNvMatchRegistrant.clear();
    }

    public void registerForSimSwitchStart(Handler h, int what, Object obj) {
        this.mIccSimSwitchStartRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForSimSwitchStart(Handler h) {
        this.mIccSimSwitchStartRegistrant.clear();
    }

    public void registerForSimSwitchStop(Handler h, int what, Object obj) {
        this.mIccSimSwitchStopRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForSimSwitchStop(Handler h) {
        this.mIccSimSwitchStopRegistrant.clear();
    }

    public void registerForCrrConn(Handler h, int what, Object obj) {
        this.mHwCrrConnIndRegistrants.add(new Registrant(h, what, obj));
        Object obj2 = this.crrConnRet;
        if (obj2 != null) {
            this.mHwCrrConnIndRegistrants.notifyRegistrants(new AsyncResult((Object) null, obj2, (Throwable) null));
        }
    }

    public void unregisterForCrrConn(Handler h) {
        this.mHwCrrConnIndRegistrants.remove(h);
    }

    public void setOnRegPLMNSelInfo(Handler h, int what, Object obj) {
        this.mRegPLMNSelInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unSetOnRegPLMNSelInfo(Handler h) {
        this.mRegPLMNSelInfoRegistrants.remove(h);
    }

    public void registerForDSDSMode(Handler h, int what, Object obj) {
        this.mDSDSModeStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDSDSMode(Handler h) {
        this.mDSDSModeStateRegistrants.remove(h);
    }

    public int getRILid() {
        return 0;
    }

    public void getSimState(Message response) {
    }

    public void setSimState(int index, int enable, Message response) {
    }

    public void hotSwitchSimSlot(int modem0, int modem1, int modem2, Message result) {
    }

    public void hotSwitchSimSlotFor2Modem(int modem0, int modem1, int modem2, Message result) {
    }

    public boolean isRadioAvailable() {
        return true;
    }

    public void setOnNetReject(Handler h, int what, Object obj) {
        this.mNetRejectRegistrant.add(new Registrant(h, what, obj));
    }

    public void unSetOnNetReject(Handler h) {
        this.mNetRejectRegistrant.remove(h);
    }

    public void setUEOperationMode(int mode, Message result) {
    }

    public void registerForMtStatusReport(Handler h, int what, Object obj) {
        this.mMtStatusReportRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForMtStatusReport(Handler h) {
        this.mMtStatusReportRegistrants.remove(h);
    }

    public void registerForCaStateChanged(Handler h, int what, Object obj) {
        this.mCaStateChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCaStateChanged(Handler h) {
        this.mCaStateChangedRegistrants.remove(h);
    }

    public void registerForSimHotPlug(Handler h, int what, Object obj) {
        this.mSimHotPlugRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSimHotPlug(Handler h) {
        this.mSimHotPlugRegistrants.remove(h);
    }

    public void registerForIccidChanged(Handler h, int what, Object obj) {
        this.mIccidChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIccidChanged(Handler h) {
        this.mIccidChangedRegistrants.remove(h);
    }

    public void setNetworkRatAndSrvDomainCfg(int rat, int srvDomain, Message result) {
    }

    public void getLocationInfo(Message result) {
    }

    public void queryServiceCellBand(Message result) {
    }

    public void closeRrc() {
    }

    public boolean setEhrpdByQMI(boolean enable) {
        return true;
    }

    public String getHwPrlVersion() {
        return null;
    }

    public String getHwUimid() {
        return null;
    }

    public String getHwCDMAMlplVersion() {
        return null;
    }

    public String getHwCDMAMsplVersion() {
        return null;
    }

    public boolean updateSocketMapForSlaveSub(int modem0, int modem1, int modem2) {
        return true;
    }

    public void setHwVSimPower(int power, Message result) {
    }

    public void setISMCOEX(String setISMCoex, Message result) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void notifyCModemStatus(int state, Message result) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void registerForRSrvccStateChanged(Handler h, int what, Object obj) {
        this.mRSrvccStateRegistrants.add(new Registrant(h, what, obj));
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void unregisterForRSrvccStateChanged(Handler h) {
        this.mRSrvccStateRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getCdmaGsmImsi(Message result) {
    }

    public void setImsDomainConfig(int domainType, Message result) {
    }

    public void getImsDomain(Message result) {
    }

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message result) {
    }

    public void handleMapconImsaReq(byte[] Msg, Message result) {
    }

    public void registerCommonImsaToMapconInfo(Handler h, int what, Object obj) {
        this.mCommonImsaToMapconInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterCommonImsaToMapconInfo(Handler h) {
        Registrant registrant = this.mCommonImsaToMapconInfoRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mCommonImsaToMapconInfoRegistrant.clear();
            this.mCommonImsaToMapconInfoRegistrant = null;
        }
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public boolean unregisterSarRegistrant(int type, Message result) {
        return false;
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public boolean registerSarRegistrant(int type, Message result) {
        return false;
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void notifyAntOrMaxTxPowerInfo(byte[] data) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void notifyBandClassInfo(byte[] resultData) {
    }

    public boolean openSwitchOfUploadAntOrMaxTxPower(int mask) {
        return false;
    }

    public boolean closeSwitchOfUploadAntOrMaxTxPower(int mask) {
        return false;
    }

    public void openSwitchOfUploadBandClass(Message result) {
    }

    public void closeSwitchOfUploadBandClass(Message result) {
    }

    public void getAvailableCSGNetworks(byte[] data, Message response) {
    }

    public void setCSGNetworkSelectionModeManual(byte[] data, Message response) {
    }

    public void getAvailableCSGNetworks(Message response) {
    }

    public void setCSGNetworkSelectionModeManual(Object obj, Message response) {
    }

    public void registerForHWBuffer(Handler h, int what, Object obj) {
        this.mHWBufferRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForHWBuffer(Handler h) {
        this.mHWBufferRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void sendHWBufferSolicited(Message result, int event, byte[] reqData) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void processHWBufferUnsolicited(byte[] respData) {
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return false;
    }

    public void registerForLimitPDPAct(Handler h, int what, Object obj) {
        this.mLimitPDPActRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForLimitPDPAct(Handler h) {
        this.mLimitPDPActRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void notifyDeviceState(String device, String state, String extra, Message response) {
    }

    public void getLteFreqWithWlanCoex(Message result) {
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message result) {
    }

    public void setMobileDataEnable(int state, Message response) {
    }

    public void setRoamingDataEnable(int state, Message response) {
    }

    public void registerForUnsolBalongModemReset(Handler h, int what, Object obj) {
        this.mBalongModemResetRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForUnsolBalongModemReset(Handler h) {
        this.mBalongModemResetRegistrants.remove(h);
    }

    public void sendLaaCmd(int cmd, String reserved, Message response) {
    }

    public void rejectCallForCause(int gsmIndex, int cause, Message result) {
    }

    public void getLaaDetailedState(String reserved, Message response) {
    }

    public void registerForLaaStateChange(Handler h, int what, Object obj) {
        this.mLaaStateChangeRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForLaaStateChange(Handler h) {
        this.mLaaStateChangeRegistrants.remove(h);
    }

    public void registerForCallAltSrv(Handler h, int what, Object obj) {
        Rlog.d(LOG_TAG, "registerForCallAltSrv what=" + what);
        this.mCallAltSrvRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCallAltSrv(Handler h) {
        this.mCallAltSrvRegistrants.remove(h);
    }

    public void setupEIMEDataCall(Message result) {
    }

    public void deactivateEIMEDataCall(Message result) {
    }

    public void getEnhancedCellInfoList(Message result, WorkSource workSource) {
    }

    public void setDeepNoDisturbState(int state, Message result) {
    }

    public void setUplinkfreqEnable(int state, Message result) {
    }

    public void registerForUplinkfreqStateRpt(Handler h, int what, Object obj) {
        Rlog.d(LOG_TAG, "registerForUlfreqStateRpt what=" + what);
        this.mUlfreqStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForUplinkfreqStateRpt(Handler h) {
        this.mUlfreqStateRegistrants.remove(h);
    }

    public void getCurrentCallsEx(Message result) {
    }

    public void informModemTetherStatusToChangeGRO(int enable, String faceName, Message result) {
    }

    public void registerForUnsolNvCfgFinished(Handler h, int what, Object obj) {
        this.mNvCfgFinishedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForUnsolNvCfgFinished(Handler h) {
        this.mNvCfgFinishedRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void sendSimMatchedOperatorInfo(String opKey, String opName, int state, String reserveField, Message response) {
    }

    public void registerForUnsol4RMimoStatus(Handler h, int what, Object obj) {
        this.m4RMimoStatusRegistrants.add(new Registrant(h, what, obj));
        Object obj2 = this.lastMimoStatus;
        if (obj2 != null) {
            this.m4RMimoStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, obj2, (Throwable) null));
        }
        Rlog.d(LOG_TAG, "registerForUnsol4RMimoStatus lastMimoStatus");
    }

    public void unregisterForUnsol4RMimoStatus(Handler h) {
        this.m4RMimoStatusRegistrants.remove(h);
    }

    public void registerFor256QAMStatus(Handler h, int what, Object obj) {
        this.m256QamStatusRegistrants.add(new Registrant(h, what, obj));
        Object obj2 = this.mLast256QamStatus;
        if (obj2 != null) {
            this.m256QamStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, obj2, (Throwable) null));
        }
    }

    public void unregisterFor256QAMStatus(Handler h) {
        this.m256QamStatusRegistrants.remove(h);
    }

    public void sendSimChgTypeInfo(int type, Message response) {
    }

    public void registerUnsolHwRestartRildStatus(Handler h, int what, Object obj) {
        this.mHwRestartRildStatusRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterUnsolHwRestartRildStatus(Handler h) {
        this.mHwRestartRildStatusRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getSimMatchedFileFromRilCache(int fileId, Message response) {
    }

    public void getCardTrayInfo(Message result) {
    }

    public void registerForAntiFakeBaseStation(Handler h, int what, Object obj) {
        this.mHwAntiFakeBaseStationRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForAntiFakeBaseStation(Handler h) {
        this.mHwAntiFakeBaseStationRegistrants.remove(h);
    }

    public void getNvcfgMatchedResult(Message response) {
    }

    public void setCsconEnabled(int enable, Message onComplete) {
    }

    public void getCsconEnabled(Message onComplete) {
    }

    public void registerCsconModeInfo(Handler h, int what, Object obj) {
    }

    public void unregisterCsconModeInfo(Handler h) {
    }

    public void getAttachedApnSettings(Message response) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setTemperatureControlToModem(int level, int type, Message result) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void setNrOptionMode(int mode, Message msg) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getNrOptionMode(Message response) {
    }

    public void sendMutiChipSessionConfig(int sessionConfig, Message message) {
    }

    public void sendVsimDataToModem(Message message) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getNrCellSsbId(Message msg) {
        if (msg != null) {
            AsyncResult.forMessage(msg, (Object) null, (Throwable) null);
            msg.sendToTarget();
        }
    }

    public void processSmsAntiAttack(int operationType, int smsType, Message result) {
    }

    public void registerForRrcConnectionStateChange(Handler h, int what, Object obj) {
        this.mHwRrcConnStateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unRegisterForRrcConnectionStateChange(Handler h) {
        this.mHwRrcConnStateRegistrants.remove(h);
    }

    public void setVoNrSwitch(int status, Message msg) {
    }

    @Override // com.android.internal.telephony.AbstractCommandsInterface
    public void getRrcConnectionState(Message msg) {
    }

    public void registerForUnsolOperatorInfo(Handler h, int what, Object obj) {
        this.mOperatorRegistrants.add(new Registrant(h, what, obj));
    }

    public void unRegisterForUnsolOperatorInfo(Handler h) {
        this.mOperatorRegistrants.remove(h);
    }
}
