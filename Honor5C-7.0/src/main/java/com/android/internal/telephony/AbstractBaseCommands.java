package com.android.internal.telephony;

import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;

public abstract class AbstractBaseCommands {
    protected RegistrantList mCaStateChangedRegistrants;
    protected Registrant mCommonImsaToMapconInfoRegistrant;
    protected Registrant mECCNumRegistrant;
    protected final RegistrantList mHWBufferRegistrants;
    protected Registrant mHwCrrConnIndRegistrant;
    protected Registrant mIccSimSwitchStartRegistrant;
    protected Registrant mIccSimSwitchStopRegistrant;
    protected RegistrantList mIccidChangedRegistrants;
    protected RegistrantList mMtStatusReportRegistrants;
    protected Registrant mNetRejectRegistrant;
    protected Registrant mRegPLMNSelInfoRegistrant;
    protected RegistrantList mReportVpStatusRegistrants;
    protected RegistrantList mSimHotPlugRegistrants;
    protected RegistrantList mSpeechInfoRegistrants;
    protected RegistrantList mUnsolRplmnsStateRegistrant;
    protected Registrant mVsimApDsFlowInfoRegistrant;
    protected Registrant mVsimDsFlowInfoRegistrant;
    protected Registrant mVsimRDHRegistrant;
    protected Registrant mVsimRegPLMNSelInfoRegistrant;
    protected Registrant mVsimTimerTaskExpiredRegistrant;

    public AbstractBaseCommands() {
        this.mSpeechInfoRegistrants = new RegistrantList();
        this.mReportVpStatusRegistrants = new RegistrantList();
        this.mUnsolRplmnsStateRegistrant = new RegistrantList();
        this.mMtStatusReportRegistrants = new RegistrantList();
        this.mCaStateChangedRegistrants = new RegistrantList();
        this.mSimHotPlugRegistrants = new RegistrantList();
        this.mIccidChangedRegistrants = new RegistrantList();
        this.mHWBufferRegistrants = new RegistrantList();
    }

    public void setImsSwitch(boolean on) {
    }

    public boolean getImsSwitch() {
        return false;
    }

    public void iccExchangeAPDU(int cla, int command, int channel, int p1, int p2, int p3, String data, Message response) {
    }

    public void iccOpenChannel(String AID, Message response) {
    }

    public void iccCloseChannel(int channel, Message response) {
    }

    public void iccGetATR(Message response) {
    }

    public void resetProfile(Message response) {
    }

    public void sendCloudMessageToModem(int event_id) {
    }

    public void setPowerGrade(int powerGrade, Message response) {
    }

    public void setWifiTxPowerGrade(int powerGrade, Message response) {
    }

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

    public void setVpMask(int vpMask, Message result) {
    }

    public void riseCdmaCutoffFreq(boolean on, Message msg) {
    }

    public void getPOLCapabilty(Message response) {
    }

    public void getCurrentPOLList(Message response) {
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
    }

    public void dataConnectionDetach(int mode, Message response) {
    }

    public void dataConnectionAttach(int mode, Message response) {
    }

    public void getCdmaChrInfo(Message result) {
    }

    public void restartRild(Message result) {
    }

    public void getModemSupportVSimVersion(Message result) {
    }

    public void supplyDepersonalization(String netpin, int type, Message result) {
    }

    public void setLTEReleaseVersion(boolean state, Message result) {
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

    public void switchBalongSim(int modem1ToSlot, int modem2ToSlot, int modem3ToSlot, Message result) {
    }

    public void getBalongSim(Message response) {
    }

    public void getSimHotPlugState(Message result) {
    }

    public void unregisterForModemCapEvent(Handler h) {
    }

    public void setHwRatCombineMode(int combineMode, Message result) {
    }

    public void getHwRatCombineMode(Message result) {
    }

    public void setHwRFChannelSwitch(int rfChannel, Message result) {
    }

    public void setCdmaModeSide(int modemID, Message result) {
    }

    public void getCdmaModeSide(Message result) {
    }

    public void registerForUimLockcard(Handler h, int what, Object obj) {
    }

    public void unregisterForUimLockcard(Handler h) {
    }

    public void resetAllConnections() {
    }

    public void requestSetEmergencyNumbers(String ecclist_withcard, String ecclist_nocard) {
    }

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
        this.mHwCrrConnIndRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForCrrConn(Handler h) {
        this.mHwCrrConnIndRegistrant.clear();
    }

    public void setOnRegPLMNSelInfo(Handler h, int what, Object obj) {
        this.mRegPLMNSelInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnRegPLMNSelInfo(Handler h) {
        this.mRegPLMNSelInfoRegistrant.clear();
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
        this.mNetRejectRegistrant = new Registrant(h, what, obj);
    }

    public void unSetOnNetReject(Handler h) {
        this.mNetRejectRegistrant.clear();
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

    public boolean updateSocketMapForSlaveSub(int modem0, int modem1, int modem2) {
        return true;
    }

    public void setHwVSimPower(int power, Message result) {
    }

    public void setISMCOEX(String setISMCoex, Message result) {
    }

    public void notifyCModemStatus(int state, Message result) {
    }

    public void testVoiceLoopBack(int mode) {
    }

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
        if (this.mCommonImsaToMapconInfoRegistrant != null && this.mCommonImsaToMapconInfoRegistrant.getHandler() == h) {
            this.mCommonImsaToMapconInfoRegistrant.clear();
            this.mCommonImsaToMapconInfoRegistrant = null;
        }
    }

    public boolean unregisterSarRegistrant(int type, Message result) {
        return false;
    }

    public boolean registerSarRegistrant(int type, Message result) {
        return false;
    }

    public void notifyAntOrMaxTxPowerInfo(byte[] data) {
    }

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

    public void registerForHWBuffer(Handler h, int what, Object obj) {
        this.mHWBufferRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForHWBuffer(Handler h) {
        this.mHWBufferRegistrants.remove(h);
    }

    public void sendHWBufferSolicited(Message result, int event, byte[] reqData) {
    }

    public void processHWBufferUnsolicited(byte[] respData) {
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return false;
    }

    public void getLteFreqWithWlanCoex(Message result) {
    }
}
