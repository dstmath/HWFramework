package com.android.internal.telephony;

import android.os.Handler;
import android.os.Message;
import android.os.WorkSource;

public interface AbstractCommandsInterface {
    public static final int USSD_MODE_NW_HAS_RESPONDED = 13;
    public static final int USSD_NETWORK_TIMEOUT = 15;
    public static final int USSD_OPERATION_NOT_SUPPORTED = 14;
    public static final int USSD_SESSION_END = 12;

    void clearTrafficData(Message message);

    void closeRrc();

    boolean closeSwitchOfUploadAntOrMaxTxPower(int i);

    void closeSwitchOfUploadBandClass(Message message);

    boolean cmdForECInfo(int i, int i2, byte[] bArr);

    void dataConnectionAttach(int i, Message message);

    void dataConnectionDetach(int i, Message message);

    void deactivateEIMEDataCall(Message message);

    void fastSwitchBalongSim(int i, int i2, Message message);

    boolean getAntiFakeBaseStation(Message message);

    void getAttachedApnSettings(Message message);

    void getAvailableCSGNetworks(Message message);

    void getAvailableCSGNetworks(byte[] bArr, Message message);

    void getBalongSim(Message message);

    void getCardTrayInfo(Message message);

    void getCdmaGsmImsi(Message message);

    void getCdmaModeSide(Message message);

    void getCsconEnabled(Message message);

    void getCurrentCallsEx(Message message);

    void getCurrentPOLList(Message message);

    void getDevSubMode(Message message);

    void getEnhancedCellInfoList(Message message, WorkSource workSource);

    String getHwCDMAMlplVersion();

    String getHwCDMAMsplVersion();

    String getHwPrlVersion();

    String getHwUimid();

    void getICCID(Message message);

    void getImsDomain(Message message);

    boolean getImsSwitch();

    void getLaaDetailedState(String str, Message message);

    void getLocationInfo(Message message);

    void getLteFreqWithWlanCoex(Message message);

    void getLteReleaseVersion(Message message);

    void getModemCapability(Message message);

    void getModemSupportVSimVersion(Message message);

    String getNVESN();

    void getNrCellSsbId(Message message);

    void getNrOptionMode(Message message);

    void getNrSaState(Message message);

    void getNvcfgMatchedResult(Message message);

    void getPOLCapabilty(Message message);

    int getRILid();

    void getRegPlmn(Message message);

    void getRrcConnectionState(Message message);

    void getSimHotPlugState(Message message);

    void getSimMatchedFileFromRilCache(int i, Message message);

    void getSimMode(Message message);

    void getSimState(Message message);

    void getSimStateViaSysinfoEx(Message message);

    void getTrafficData(Message message);

    void handleMapconImsaReq(byte[] bArr, Message message);

    void handleUiccAuth(int i, byte[] bArr, byte[] bArr2, Message message);

    void hotSwitchSimSlot(int i, int i2, int i3, Message message);

    void hotSwitchSimSlotFor2Modem(int i, int i2, int i3, Message message);

    void hvCheckCard(Message message);

    void iccGetATR(Message message);

    void informModemTetherStatusToChangeGRO(int i, String str, Message message);

    boolean isFastSwitchInProcess();

    boolean isRadioAvailable();

    void notifyAntOrMaxTxPowerInfo(byte[] bArr);

    void notifyBandClassInfo(byte[] bArr);

    void notifyCModemStatus(int i, Message message);

    void notifyCellularCommParaReady(int i, int i2, Message message);

    void notifyDeviceState(String str, String str2, String str3, Message message);

    boolean openSwitchOfUploadAntOrMaxTxPower(int i);

    void openSwitchOfUploadBandClass(Message message);

    void processHWBufferUnsolicited(byte[] bArr);

    void processSmsAntiAttack(int i, int i2, Message message);

    void queryCardType(Message message);

    void queryEmergencyNumbers();

    void queryServiceCellBand(Message message);

    void registerCommonImsaToMapconInfo(Handler handler, int i, Object obj);

    void registerCsconModeInfo(Handler handler, int i, Object obj);

    void registerFor256QAMStatus(Handler handler, int i, Object obj);

    void registerForAntiFakeBaseStation(Handler handler, int i, Object obj);

    void registerForCaStateChanged(Handler handler, int i, Object obj);

    void registerForCallAltSrv(Handler handler, int i, Object obj);

    void registerForCrrConn(Handler handler, int i, Object obj);

    void registerForDSDSMode(Handler handler, int i, Object obj);

    void registerForHWBuffer(Handler handler, int i, Object obj);

    void registerForIccidChanged(Handler handler, int i, Object obj);

    void registerForLaaStateChange(Handler handler, int i, Object obj);

    void registerForLimitPDPAct(Handler handler, int i, Object obj);

    void registerForModemCapEvent(Handler handler, int i, Object obj);

    void registerForRSrvccStateChanged(Handler handler, int i, Object obj);

    void registerForReportVpStatus(Handler handler, int i, Object obj);

    void registerForRplmnsStateChanged(Handler handler, int i, Object obj);

    void registerForRrcConnectionStateChange(Handler handler, int i, Object obj);

    void registerForSimHotPlug(Handler handler, int i, Object obj);

    void registerForSimSwitchStart(Handler handler, int i, Object obj);

    void registerForSimSwitchStop(Handler handler, int i, Object obj);

    void registerForUimLockcard(Handler handler, int i, Object obj);

    void registerForUnsol4RMimoStatus(Handler handler, int i, Object obj);

    void registerForUnsolBalongModemReset(Handler handler, int i, Object obj);

    void registerForUnsolNvCfgFinished(Handler handler, int i, Object obj);

    void registerForUnsolOperatorInfo(Handler handler, int i, Object obj);

    void registerForUnsolSpeechInfo(Handler handler, int i, Object obj);

    void registerForUplinkfreqStateRpt(Handler handler, int i, Object obj);

    boolean registerSarRegistrant(int i, Message message);

    void registerUnsolHwRestartRildStatus(Handler handler, int i, Object obj);

    void rejectCallForCause(int i, int i2, Message message);

    void requestSetEmergencyNumbers(String str, String str2);

    void resetAllConnections();

    void resetProfile(Message message);

    void restartRild(Message message);

    void riseCdmaCutoffFreq(boolean z, Message message);

    void sendCloudMessageToModem(int i);

    void sendHWBufferSolicited(Message message, int i, byte[] bArr);

    void sendLaaCmd(int i, String str, Message message);

    void sendMutiChipSessionConfig(int i, Message message);

    void sendPseudocellCellInfo(int i, int i2, int i3, int i4, String str, Message message);

    void sendSMSSetLong(int i, Message message);

    void sendSimChgTypeInfo(int i, Message message);

    void sendSimMatchedOperatorInfo(String str, String str2, int i, String str3, Message message);

    void sendVsimDataToModem(Message message);

    void setActiveModemMode(int i, Message message);

    void setApDsFlowCfg(int i, int i2, int i3, int i4, Message message);

    void setApDsFlowCfg(Message message);

    void setCSGNetworkSelectionModeManual(Object obj, Message message);

    void setCSGNetworkSelectionModeManual(byte[] bArr, Message message);

    void setCdmaModeSide(int i, Message message);

    void setCsconEnabled(int i, Message message);

    void setDeepNoDisturbState(int i, Message message);

    void setDsFlowNvCfg(int i, int i2, Message message);

    void setDsFlowNvCfg(Message message);

    boolean setEhrpdByQMI(boolean z);

    void setFastSimSwitchInProcess(boolean z, Message message);

    void setHwVSimPower(int i, Message message);

    void setISMCOEX(String str, Message message);

    void setImsDomainConfig(int i, Message message);

    void setImsSwitch(boolean z);

    void setLTEReleaseVersion(int i, Message message);

    void setMobileDataEnable(int i, Message message);

    void setNetworkRatAndSrvDomainCfg(int i, int i2, Message message);

    void setNrOptionMode(int i, Message message);

    void setNrSaState(int i, Message message);

    void setNrSwitch(boolean z, Message message);

    void setOnECCNum(Handler handler, int i, Object obj);

    void setOnNetReject(Handler handler, int i, Object obj);

    void setOnRegPLMNSelInfo(Handler handler, int i, Object obj);

    void setOnRestartRildNvMatch(Handler handler, int i, Object obj);

    void setOnVsimApDsFlowInfo(Handler handler, int i, Object obj);

    void setOnVsimDsFlowInfo(Handler handler, int i, Object obj);

    void setOnVsimRDH(Handler handler, int i, Object obj);

    void setOnVsimRegPLMNSelInfo(Handler handler, int i, Object obj);

    void setOnVsimTimerTaskExpired(Handler handler, int i, Object obj);

    void setPOLEntry(int i, String str, int i2, Message message);

    void setPowerGrade(int i, Message message);

    void setRoamingDataEnable(int i, Message message);

    void setSimMode(int i, int i2, int i3, Message message);

    void setSimState(int i, int i2, Message message);

    void setTEEDataReady(int i, int i2, int i3, Message message);

    void setTemperatureControlToModem(int i, int i2, Message message);

    void setUEOperationMode(int i, Message message);

    void setUplinkfreqEnable(int i, Message message);

    void setVoNrSwitch(int i, Message message);

    void setVpMask(int i, Message message);

    void setWifiTxPowerGrade(int i, Message message);

    void setupEIMEDataCall(Message message);

    void supplyDepersonalization(String str, int i, Message message);

    void switchBalongSim(int i, int i2, int i3, Message message);

    void switchBalongSim(int i, int i2, Message message);

    void switchVoiceCallBackgroundState(int i, Message message);

    void unRegisterForRrcConnectionStateChange(Handler handler);

    void unRegisterForUnsolOperatorInfo(Handler handler);

    void unSetOnECCNum(Handler handler);

    void unSetOnNetReject(Handler handler);

    void unSetOnRegPLMNSelInfo(Handler handler);

    void unSetOnRestartRildNvMatch(Handler handler);

    void unSetOnVsimApDsFlowInfo(Handler handler);

    void unSetOnVsimDsFlowInfo(Handler handler);

    void unSetOnVsimRDH(Handler handler);

    void unSetOnVsimRegPLMNSelInfo(Handler handler);

    void unSetOnVsimTimerTaskExpired(Handler handler);

    void unregisterCommonImsaToMapconInfo(Handler handler);

    void unregisterCsconModeInfo(Handler handler);

    void unregisterFor256QAMStatus(Handler handler);

    void unregisterForAntiFakeBaseStation(Handler handler);

    void unregisterForCaStateChanged(Handler handler);

    void unregisterForCallAltSrv(Handler handler);

    void unregisterForCrrConn(Handler handler);

    void unregisterForDSDSMode(Handler handler);

    void unregisterForHWBuffer(Handler handler);

    void unregisterForIccidChanged(Handler handler);

    void unregisterForLaaStateChange(Handler handler);

    void unregisterForLimitPDPAct(Handler handler);

    void unregisterForModemCapEvent(Handler handler);

    void unregisterForRSrvccStateChanged(Handler handler);

    void unregisterForReportVpStatus(Handler handler);

    void unregisterForRplmnsStateChanged(Handler handler);

    void unregisterForSimHotPlug(Handler handler);

    void unregisterForSimSwitchStart(Handler handler);

    void unregisterForSimSwitchStop(Handler handler);

    void unregisterForUimLockcard(Handler handler);

    void unregisterForUnsol4RMimoStatus(Handler handler);

    void unregisterForUnsolBalongModemReset(Handler handler);

    void unregisterForUnsolNvCfgFinished(Handler handler);

    void unregisterForUnsolSpeechInfo(Handler handler);

    void unregisterForUplinkfreqStateRpt(Handler handler);

    boolean unregisterSarRegistrant(int i, Message message);

    void unregisterUnsolHwRestartRildStatus(Handler handler);

    boolean updateSocketMapForSlaveSub(int i, int i2, int i3);

    void updateStackBinding(int i, int i2, Message message);
}
