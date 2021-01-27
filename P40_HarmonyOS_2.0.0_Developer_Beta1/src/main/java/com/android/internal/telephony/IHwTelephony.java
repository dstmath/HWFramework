package com.android.internal.telephony;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.NrCellSsbId;
import android.telephony.UiccAuthResponse;
import com.android.internal.telephony.IPhoneCallback;

public interface IHwTelephony extends IInterface {
    boolean bindSimToProfile(int i) throws RemoteException;

    String blockingGetIccATR(int i) throws RemoteException;

    boolean changeSimPinCode(String str, String str2, int i) throws RemoteException;

    void closeRrc() throws RemoteException;

    boolean cmdForECInfo(int i, int i2, byte[] bArr) throws RemoteException;

    int get2GServiceAbility() throws RemoteException;

    boolean getAntiFakeBaseStation(Message message) throws RemoteException;

    String getCTOperator(int i, String str) throws RemoteException;

    void getCallForwardingOption(int i, int i2, Message message) throws RemoteException;

    byte[] getCardTrayInfo() throws RemoteException;

    String getCdmaGsmImsi() throws RemoteException;

    String getCdmaGsmImsiForSubId(int i) throws RemoteException;

    String getCdmaMlplVersion() throws RemoteException;

    String getCdmaMsplVersion() throws RemoteException;

    Bundle getCellLocation(int i) throws RemoteException;

    int[] getCsconEnabled() throws RemoteException;

    int getDataRegisteredState(int i) throws RemoteException;

    int getDataStateForSubscriber(int i) throws RemoteException;

    int getDefault4GSlotId() throws RemoteException;

    String getDemoString() throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    int getImsDomain() throws RemoteException;

    int getImsDomainByPhoneId(int i) throws RemoteException;

    String getImsImpu(int i) throws RemoteException;

    boolean getImsSwitch() throws RemoteException;

    boolean getImsSwitchByPhoneId(int i) throws RemoteException;

    boolean getLaaDetailedState(String str, Message message) throws RemoteException;

    int getLevel(int i, int i2, int i3, int i4) throws RemoteException;

    String getLine1NumberFromImpu(int i) throws RemoteException;

    int getLteServiceAbility() throws RemoteException;

    int getLteServiceAbilityForSlotId(int i) throws RemoteException;

    String getMeidForSubscriber(int i) throws RemoteException;

    boolean[] getMobilePhysicsLayerStatus(int i) throws RemoteException;

    String getNVESN() throws RemoteException;

    int getNetworkModeFromDB(int i) throws RemoteException;

    NrCellSsbId getNrCellSsbId(int i) throws RemoteException;

    int getNrOptionMode() throws RemoteException;

    int getOnDemandDataSubId() throws RemoteException;

    String getPesnForSubscriber(int i) throws RemoteException;

    int getPreferredDataSubscription() throws RemoteException;

    int getServiceAbilityForSlotId(int i, int i2) throws RemoteException;

    int getSpecCardType(int i) throws RemoteException;

    int getSubState(int i) throws RemoteException;

    int getUiccAppType() throws RemoteException;

    int getUiccAppTypeByPhoneId(int i) throws RemoteException;

    int getUiccCardType(int i) throws RemoteException;

    String getUniqueDeviceId(int i, String str) throws RemoteException;

    int getVoiceRegisteredState(int i) throws RemoteException;

    boolean getWaitingSwitchBalongSlot() throws RemoteException;

    boolean handleMapconImsaReq(byte[] bArr) throws RemoteException;

    boolean handleMapconImsaReqByPhoneId(int i, byte[] bArr) throws RemoteException;

    UiccAuthResponse handleUiccAuth(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    UiccAuthResponse handleUiccAuthByPhoneId(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    void informModemTetherStatusToChangeGRO(int i, String str) throws RemoteException;

    void inputDialerSpecialCode(String str) throws RemoteException;

    int invokeOemRilRequestRaw(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean isAISCard(int i) throws RemoteException;

    boolean isCTCdmaCardInGsmMode() throws RemoteException;

    boolean isCardUimLocked(int i) throws RemoteException;

    boolean isCspPlmnEnabled(int i) throws RemoteException;

    boolean isCtSimCard(int i) throws RemoteException;

    boolean isCustomAis() throws RemoteException;

    boolean isDomesticCard(int i) throws RemoteException;

    boolean isEuicc(int i) throws RemoteException;

    boolean isImsRegisteredForSubId(int i) throws RemoteException;

    boolean isLTESupported() throws RemoteException;

    boolean isNeedToRadioPowerOn(long j) throws RemoteException;

    boolean isNsaState(int i) throws RemoteException;

    boolean isRadioAvailable() throws RemoteException;

    boolean isRadioAvailableByPhoneId(int i) throws RemoteException;

    boolean isRadioOn(int i) throws RemoteException;

    boolean isSecondaryCardGsmOnly() throws RemoteException;

    boolean isSetDefault4GSlotIdEnabled() throws RemoteException;

    boolean isSubDeactivedByPowerOff(long j) throws RemoteException;

    boolean isVideoTelephonyAvailableForSubId(int i) throws RemoteException;

    boolean isVolteAvailableForSubId(int i) throws RemoteException;

    boolean isWifiCallingAvailableForSubId(int i) throws RemoteException;

    void notifyCModemStatus(int i, IPhoneCallback iPhoneCallback) throws RemoteException;

    void notifyCellularCommParaReady(int i, int i2, Message message) throws RemoteException;

    boolean notifyDeviceState(String str, String str2, String str3) throws RemoteException;

    String[] queryServiceCellBand() throws RemoteException;

    boolean registerCommonImsaToMapconInfo(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForAntiFakeBaseStation(IPhoneCallback iPhoneCallback) throws RemoteException;

    void registerForCallAltSrv(int i, IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForPhoneEvent(int i, IPhoneCallback iPhoneCallback, int i2) throws RemoteException;

    boolean registerForRadioAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForRadioNotAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean registerForWirelessState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    void saveNetworkModeToDB(int i, int i2) throws RemoteException;

    boolean sendLaaCmd(int i, String str, Message message) throws RemoteException;

    boolean sendPseudocellCellInfo(int i, int i2, int i3, int i4, String str, int i5) throws RemoteException;

    boolean sendSimMatchedOperatorInfo(int i, String str, String str2, int i2, String str3) throws RemoteException;

    void set2GServiceAbility(int i) throws RemoteException;

    void setCallForwardingOption(int i, int i2, int i3, String str, int i4, Message message) throws RemoteException;

    boolean setCellTxPower(int i) throws RemoteException;

    boolean setCsconEnabled(boolean z) throws RemoteException;

    void setDataEnabledWithoutPromp(boolean z) throws RemoteException;

    void setDataRoamingEnabledWithoutPromp(boolean z) throws RemoteException;

    boolean setDeepNoDisturbState(int i, int i2) throws RemoteException;

    void setDefault4GSlotId(int i, Message message) throws RemoteException;

    void setDefaultDataSlotId(int i) throws RemoteException;

    void setDefaultMobileEnable(boolean z) throws RemoteException;

    boolean setISMCOEX(String str) throws RemoteException;

    void setImsDomainConfig(int i) throws RemoteException;

    void setImsDomainConfigByPhoneId(int i, int i2) throws RemoteException;

    void setImsRegistrationStateForSubId(int i, boolean z) throws RemoteException;

    void setImsSwitch(boolean z) throws RemoteException;

    void setImsSwitchByPhoneId(int i, boolean z) throws RemoteException;

    boolean setLine1Number(int i, String str, String str2, Message message) throws RemoteException;

    void setLteServiceAbility(int i) throws RemoteException;

    void setLteServiceAbilityForSlotId(int i, int i2) throws RemoteException;

    boolean setMaxTxPower(int i, int i2) throws RemoteException;

    boolean setNrOptionMode(int i, Message message) throws RemoteException;

    void setNrSwitch(int i, boolean z) throws RemoteException;

    boolean setPinLockEnabled(boolean z, String str, int i) throws RemoteException;

    void setPreferredNetworkType(int i) throws RemoteException;

    void setServiceAbilityForSlotId(int i, int i2, int i3) throws RemoteException;

    boolean setSubscription(int i, boolean z, Message message) throws RemoteException;

    boolean setTemperatureControlToModem(int i, int i2, int i3, Message message) throws RemoteException;

    boolean setUplinkFreqBandwidthReportState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    void setUserPrefDataSlotId(int i) throws RemoteException;

    boolean setWifiTxPower(int i) throws RemoteException;

    boolean unregisterCommonImsaToMapconInfo(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForAntiFakeBaseStation() throws RemoteException;

    void unregisterForCallAltSrv(int i) throws RemoteException;

    void unregisterForPhoneEvent(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForRadioAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForRadioNotAvailable(IPhoneCallback iPhoneCallback) throws RemoteException;

    boolean unregisterForWirelessState(int i, int i2, IPhoneCallback iPhoneCallback) throws RemoteException;

    void updateCrurrentPhone(int i) throws RemoteException;

    void waitingSetDefault4GSlotDone(boolean z) throws RemoteException;

    public static class Default implements IHwTelephony {
        @Override // com.android.internal.telephony.IHwTelephony
        public String getDemoString() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getMeidForSubscriber(int slotId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getPesnForSubscriber(int slotId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getSubState(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setUserPrefDataSlotId(int slotId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void closeRrc() throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isCTCdmaCardInGsmMode() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setDefaultMobileEnable(boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setDataEnabledWithoutPromp(boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setDataRoamingEnabledWithoutPromp(boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getNVESN() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getDataStateForSubscriber(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setLteServiceAbility(int ability) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getLteServiceAbility() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setServiceAbilityForSlotId(int slotId, int type, int ability) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getServiceAbilityForSlotId(int slotId, int type) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getLteServiceAbilityForSlotId(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setLteServiceAbilityForSlotId(int slotId, int ability) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isImsRegisteredForSubId(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isWifiCallingAvailableForSubId(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isVolteAvailableForSubId(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isVideoTelephonyAvailableForSubId(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setImsRegistrationStateForSubId(int subId, boolean registered) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void set2GServiceAbility(int ability) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int get2GServiceAbility() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isSubDeactivedByPowerOff(long sub) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isNeedToRadioPowerOn(long sub) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getDefault4GSlotId() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setDefaultDataSlotId(int slotId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void updateCrurrentPhone(int lteSlot) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getSpecCardType(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isCardUimLocked(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isRadioOn(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setPreferredNetworkType(int nwMode) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setDefault4GSlotId(int slotId, Message msg) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isSetDefault4GSlotIdEnabled() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void waitingSetDefault4GSlotDone(boolean waiting) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean getWaitingSwitchBalongSlot() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getOnDemandDataSubId() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getPreferredDataSubscription() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getCdmaGsmImsi() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getUiccCardType(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public Bundle getCellLocation(int slotId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getCdmaMlplVersion() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getCdmaMsplVersion() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getUniqueDeviceId(int scope, String callingPackageName) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isLTESupported() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setISMCOEX(String setISMCoex) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isDomesticCard(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setWifiTxPower(int power) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setCellTxPower(int power) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String[] queryServiceCellBand() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean registerForRadioAvailable(IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean unregisterForRadioAvailable(IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean registerForRadioNotAvailable(IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isRadioAvailable() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setImsSwitch(boolean value) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean getImsSwitch() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setNrSwitch(int phoneId, boolean value) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setImsDomainConfig(int domainType) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean handleMapconImsaReq(byte[] Msg) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getUiccAppType() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getImsDomain() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean registerForPhoneEvent(int phoneId, IPhoneCallback callback, int events) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void unregisterForPhoneEvent(IPhoneCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isRadioAvailableByPhoneId(int phoneId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setImsSwitchByPhoneId(int phoneId, boolean value) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean getImsSwitchByPhoneId(int phoneId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setImsDomainConfigByPhoneId(int phoneId, int domainType) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean handleMapconImsaReqByPhoneId(int phoneId, byte[] Msg) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getUiccAppTypeByPhoneId(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getImsDomainByPhoneId(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public UiccAuthResponse handleUiccAuthByPhoneId(int phoneId, int auth_type, byte[] rand, byte[] auth) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setMaxTxPower(int type, int power) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean cmdForECInfo(int event, int action, byte[] buf) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isCtSimCard(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void notifyCModemStatus(int status, IPhoneCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean notifyDeviceState(String device, String state, String extras) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setPinLockEnabled(boolean enablePinLock, String password, int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean changeSimPinCode(String oldPinCode, String newPinCode, int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean sendPseudocellCellInfo(int type, int lac, int cid, int radioTech, String plmn, int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean sendLaaCmd(int cmd, String reserved, Message response) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean getLaaDetailedState(String reserved, Message response) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void registerForCallAltSrv(int subId, IPhoneCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void unregisterForCallAltSrv(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int invokeOemRilRequestRaw(int phoneId, byte[] oemReq, byte[] oemResp) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isCspPlmnEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void setCallForwardingOption(int subId, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message response) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void getCallForwardingOption(int subId, int commandInterfaceCFReason, Message response) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setSubscription(int subId, boolean activate, Message response) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getImsImpu(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getLine1NumberFromImpu(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isSecondaryCardGsmOnly() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean bindSimToProfile(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getCdmaGsmImsiForSubId(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setLine1Number(int subId, String alphaTag, String number, Message onComplete) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setDeepNoDisturbState(int slotId, int state) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setUplinkFreqBandwidthReportState(int slotId, int state, IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void informModemTetherStatusToChangeGRO(int enable, String faceName) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean sendSimMatchedOperatorInfo(int slotId, String opKey, String opName, int state, String reserveField) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean[] getMobilePhysicsLayerStatus(int slotId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean getAntiFakeBaseStation(Message response) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean registerForAntiFakeBaseStation(IPhoneCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean unregisterForAntiFakeBaseStation() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public byte[] getCardTrayInfo() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setCsconEnabled(boolean enable) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int[] getCsconEnabled() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getLevel(int type, int rssi, int ecio, int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getDataRegisteredState(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getVoiceRegisteredState(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getNetworkModeFromDB(int slotId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void saveNetworkModeToDB(int slotId, int mode) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setTemperatureControlToModem(int level, int type, int subId, Message response) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String blockingGetIccATR(int index) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isEuicc(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean setNrOptionMode(int mode, Message msg) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public int getNrOptionMode() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isNsaState(int phoneId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public NrCellSsbId getNrCellSsbId(int slotId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isAISCard(int slotId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public boolean isCustomAis() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public String getCTOperator(int slotId, String operator) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwTelephony
        public void inputDialerSpecialCode(String inputCode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwTelephony {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwTelephony";
        static final int TRANSACTION_bindSimToProfile = 101;
        static final int TRANSACTION_blockingGetIccATR = 121;
        static final int TRANSACTION_changeSimPinCode = 87;
        static final int TRANSACTION_closeRrc = 6;
        static final int TRANSACTION_cmdForECInfo = 81;
        static final int TRANSACTION_get2GServiceAbility = 25;
        static final int TRANSACTION_getAntiFakeBaseStation = 109;
        static final int TRANSACTION_getCTOperator = 129;
        static final int TRANSACTION_getCallForwardingOption = 96;
        static final int TRANSACTION_getCardTrayInfo = 112;
        static final int TRANSACTION_getCdmaGsmImsi = 41;
        static final int TRANSACTION_getCdmaGsmImsiForSubId = 102;
        static final int TRANSACTION_getCdmaMlplVersion = 44;
        static final int TRANSACTION_getCdmaMsplVersion = 45;
        static final int TRANSACTION_getCellLocation = 43;
        static final int TRANSACTION_getCsconEnabled = 114;
        static final int TRANSACTION_getDataRegisteredState = 116;
        static final int TRANSACTION_getDataStateForSubscriber = 12;
        static final int TRANSACTION_getDefault4GSlotId = 28;
        static final int TRANSACTION_getDemoString = 1;
        static final int TRANSACTION_getHwInnerService = 130;
        static final int TRANSACTION_getImsDomain = 66;
        static final int TRANSACTION_getImsDomainByPhoneId = 76;
        static final int TRANSACTION_getImsImpu = 98;
        static final int TRANSACTION_getImsSwitch = 61;
        static final int TRANSACTION_getImsSwitchByPhoneId = 72;
        static final int TRANSACTION_getLaaDetailedState = 90;
        static final int TRANSACTION_getLevel = 115;
        static final int TRANSACTION_getLine1NumberFromImpu = 99;
        static final int TRANSACTION_getLteServiceAbility = 14;
        static final int TRANSACTION_getLteServiceAbilityForSlotId = 17;
        static final int TRANSACTION_getMeidForSubscriber = 2;
        static final int TRANSACTION_getMobilePhysicsLayerStatus = 108;
        static final int TRANSACTION_getNVESN = 11;
        static final int TRANSACTION_getNetworkModeFromDB = 118;
        static final int TRANSACTION_getNrCellSsbId = 126;
        static final int TRANSACTION_getNrOptionMode = 124;
        static final int TRANSACTION_getOnDemandDataSubId = 39;
        static final int TRANSACTION_getPesnForSubscriber = 3;
        static final int TRANSACTION_getPreferredDataSubscription = 40;
        static final int TRANSACTION_getServiceAbilityForSlotId = 16;
        static final int TRANSACTION_getSpecCardType = 31;
        static final int TRANSACTION_getSubState = 4;
        static final int TRANSACTION_getUiccAppType = 65;
        static final int TRANSACTION_getUiccAppTypeByPhoneId = 75;
        static final int TRANSACTION_getUiccCardType = 42;
        static final int TRANSACTION_getUniqueDeviceId = 46;
        static final int TRANSACTION_getVoiceRegisteredState = 117;
        static final int TRANSACTION_getWaitingSwitchBalongSlot = 38;
        static final int TRANSACTION_handleMapconImsaReq = 64;
        static final int TRANSACTION_handleMapconImsaReqByPhoneId = 74;
        static final int TRANSACTION_handleUiccAuth = 67;
        static final int TRANSACTION_handleUiccAuthByPhoneId = 77;
        static final int TRANSACTION_informModemTetherStatusToChangeGRO = 106;
        static final int TRANSACTION_inputDialerSpecialCode = 131;
        static final int TRANSACTION_invokeOemRilRequestRaw = 93;
        static final int TRANSACTION_isAISCard = 127;
        static final int TRANSACTION_isCTCdmaCardInGsmMode = 7;
        static final int TRANSACTION_isCardUimLocked = 32;
        static final int TRANSACTION_isCspPlmnEnabled = 94;
        static final int TRANSACTION_isCtSimCard = 82;
        static final int TRANSACTION_isCustomAis = 128;
        static final int TRANSACTION_isDomesticCard = 49;
        static final int TRANSACTION_isEuicc = 122;
        static final int TRANSACTION_isImsRegisteredForSubId = 19;
        static final int TRANSACTION_isLTESupported = 47;
        static final int TRANSACTION_isNeedToRadioPowerOn = 27;
        static final int TRANSACTION_isNsaState = 125;
        static final int TRANSACTION_isRadioAvailable = 59;
        static final int TRANSACTION_isRadioAvailableByPhoneId = 70;
        static final int TRANSACTION_isRadioOn = 33;
        static final int TRANSACTION_isSecondaryCardGsmOnly = 100;
        static final int TRANSACTION_isSetDefault4GSlotIdEnabled = 36;
        static final int TRANSACTION_isSubDeactivedByPowerOff = 26;
        static final int TRANSACTION_isVideoTelephonyAvailableForSubId = 22;
        static final int TRANSACTION_isVolteAvailableForSubId = 21;
        static final int TRANSACTION_isWifiCallingAvailableForSubId = 20;
        static final int TRANSACTION_notifyCModemStatus = 83;
        static final int TRANSACTION_notifyCellularCommParaReady = 85;
        static final int TRANSACTION_notifyDeviceState = 84;
        static final int TRANSACTION_queryServiceCellBand = 52;
        static final int TRANSACTION_registerCommonImsaToMapconInfo = 57;
        static final int TRANSACTION_registerForAntiFakeBaseStation = 110;
        static final int TRANSACTION_registerForCallAltSrv = 91;
        static final int TRANSACTION_registerForPhoneEvent = 68;
        static final int TRANSACTION_registerForRadioAvailable = 53;
        static final int TRANSACTION_registerForRadioNotAvailable = 55;
        static final int TRANSACTION_registerForWirelessState = 78;
        static final int TRANSACTION_saveNetworkModeToDB = 119;
        static final int TRANSACTION_sendLaaCmd = 89;
        static final int TRANSACTION_sendPseudocellCellInfo = 88;
        static final int TRANSACTION_sendSimMatchedOperatorInfo = 107;
        static final int TRANSACTION_set2GServiceAbility = 24;
        static final int TRANSACTION_setCallForwardingOption = 95;
        static final int TRANSACTION_setCellTxPower = 51;
        static final int TRANSACTION_setCsconEnabled = 113;
        static final int TRANSACTION_setDataEnabledWithoutPromp = 9;
        static final int TRANSACTION_setDataRoamingEnabledWithoutPromp = 10;
        static final int TRANSACTION_setDeepNoDisturbState = 104;
        static final int TRANSACTION_setDefault4GSlotId = 35;
        static final int TRANSACTION_setDefaultDataSlotId = 29;
        static final int TRANSACTION_setDefaultMobileEnable = 8;
        static final int TRANSACTION_setISMCOEX = 48;
        static final int TRANSACTION_setImsDomainConfig = 63;
        static final int TRANSACTION_setImsDomainConfigByPhoneId = 73;
        static final int TRANSACTION_setImsRegistrationStateForSubId = 23;
        static final int TRANSACTION_setImsSwitch = 60;
        static final int TRANSACTION_setImsSwitchByPhoneId = 71;
        static final int TRANSACTION_setLine1Number = 103;
        static final int TRANSACTION_setLteServiceAbility = 13;
        static final int TRANSACTION_setLteServiceAbilityForSlotId = 18;
        static final int TRANSACTION_setMaxTxPower = 80;
        static final int TRANSACTION_setNrOptionMode = 123;
        static final int TRANSACTION_setNrSwitch = 62;
        static final int TRANSACTION_setPinLockEnabled = 86;
        static final int TRANSACTION_setPreferredNetworkType = 34;
        static final int TRANSACTION_setServiceAbilityForSlotId = 15;
        static final int TRANSACTION_setSubscription = 97;
        static final int TRANSACTION_setTemperatureControlToModem = 120;
        static final int TRANSACTION_setUplinkFreqBandwidthReportState = 105;
        static final int TRANSACTION_setUserPrefDataSlotId = 5;
        static final int TRANSACTION_setWifiTxPower = 50;
        static final int TRANSACTION_unregisterCommonImsaToMapconInfo = 58;
        static final int TRANSACTION_unregisterForAntiFakeBaseStation = 111;
        static final int TRANSACTION_unregisterForCallAltSrv = 92;
        static final int TRANSACTION_unregisterForPhoneEvent = 69;
        static final int TRANSACTION_unregisterForRadioAvailable = 54;
        static final int TRANSACTION_unregisterForRadioNotAvailable = 56;
        static final int TRANSACTION_unregisterForWirelessState = 79;
        static final int TRANSACTION_updateCrurrentPhone = 30;
        static final int TRANSACTION_waitingSetDefault4GSlotDone = 37;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwTelephony asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwTelephony)) {
                return new Proxy(obj);
            }
            return (IHwTelephony) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Message _arg1;
            Message _arg2;
            Message _arg22;
            Message _arg12;
            Message _arg5;
            Message _arg23;
            Message _arg24;
            Message _arg3;
            Message _arg0;
            Message _arg32;
            Message _arg13;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getDemoString();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getMeidForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getPesnForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getSubState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setUserPrefDataSlotId(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        closeRrc();
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCTCdmaCardInGsmMode = isCTCdmaCardInGsmMode();
                        reply.writeNoException();
                        reply.writeInt(isCTCdmaCardInGsmMode ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setDefaultMobileEnable(_arg02);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setDataEnabledWithoutPromp(_arg02);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setDataRoamingEnabledWithoutPromp(_arg02);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getNVESN();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getDataStateForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        setLteServiceAbility(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getLteServiceAbility();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        setServiceAbilityForSlotId(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getServiceAbilityForSlotId(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getLteServiceAbilityForSlotId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        setLteServiceAbilityForSlotId(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isImsRegisteredForSubId = isImsRegisteredForSubId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isImsRegisteredForSubId ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWifiCallingAvailableForSubId = isWifiCallingAvailableForSubId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isWifiCallingAvailableForSubId ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVolteAvailableForSubId = isVolteAvailableForSubId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVolteAvailableForSubId ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVideoTelephonyAvailableForSubId = isVideoTelephonyAvailableForSubId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVideoTelephonyAvailableForSubId ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setImsRegistrationStateForSubId(_arg03, _arg02);
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        set2GServiceAbility(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = get2GServiceAbility();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSubDeactivedByPowerOff = isSubDeactivedByPowerOff(data.readLong());
                        reply.writeNoException();
                        reply.writeInt(isSubDeactivedByPowerOff ? 1 : 0);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNeedToRadioPowerOn = isNeedToRadioPowerOn(data.readLong());
                        reply.writeNoException();
                        reply.writeInt(isNeedToRadioPowerOn ? 1 : 0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getDefault4GSlotId();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        setDefaultDataSlotId(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        updateCrurrentPhone(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getSpecCardType(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCardUimLocked = isCardUimLocked(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isCardUimLocked ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRadioOn = isRadioOn(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isRadioOn ? 1 : 0);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        setPreferredNetworkType(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setDefault4GSlotId(_arg04, _arg1);
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSetDefault4GSlotIdEnabled = isSetDefault4GSlotIdEnabled();
                        reply.writeNoException();
                        reply.writeInt(isSetDefault4GSlotIdEnabled ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        waitingSetDefault4GSlotDone(_arg02);
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        boolean waitingSwitchBalongSlot = getWaitingSwitchBalongSlot();
                        reply.writeNoException();
                        reply.writeInt(waitingSwitchBalongSlot ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getOnDemandDataSubId();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getPreferredDataSubscription();
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        String _result15 = getCdmaGsmImsi();
                        reply.writeNoException();
                        reply.writeString(_result15);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getUiccCardType(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result17 = getCellLocation(data.readInt());
                        reply.writeNoException();
                        if (_result17 != null) {
                            reply.writeInt(1);
                            _result17.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = getCdmaMlplVersion();
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        String _result19 = getCdmaMsplVersion();
                        reply.writeNoException();
                        reply.writeString(_result19);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        String _result20 = getUniqueDeviceId(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result20);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isLTESupported = isLTESupported();
                        reply.writeNoException();
                        reply.writeInt(isLTESupported ? 1 : 0);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        boolean ismcoex = setISMCOEX(data.readString());
                        reply.writeNoException();
                        reply.writeInt(ismcoex ? 1 : 0);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDomesticCard = isDomesticCard(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDomesticCard ? 1 : 0);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        boolean wifiTxPower = setWifiTxPower(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(wifiTxPower ? 1 : 0);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cellTxPower = setCellTxPower(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(cellTxPower ? 1 : 0);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result21 = queryServiceCellBand();
                        reply.writeNoException();
                        reply.writeStringArray(_result21);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerForRadioAvailable = registerForRadioAvailable(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerForRadioAvailable ? 1 : 0);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterForRadioAvailable = unregisterForRadioAvailable(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterForRadioAvailable ? 1 : 0);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerForRadioNotAvailable = registerForRadioNotAvailable(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerForRadioNotAvailable ? 1 : 0);
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterForRadioNotAvailable = unregisterForRadioNotAvailable(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterForRadioNotAvailable ? 1 : 0);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerCommonImsaToMapconInfo = registerCommonImsaToMapconInfo(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerCommonImsaToMapconInfo ? 1 : 0);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterCommonImsaToMapconInfo = unregisterCommonImsaToMapconInfo(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterCommonImsaToMapconInfo ? 1 : 0);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRadioAvailable = isRadioAvailable();
                        reply.writeNoException();
                        reply.writeInt(isRadioAvailable ? 1 : 0);
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setImsSwitch(_arg02);
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        boolean imsSwitch = getImsSwitch();
                        reply.writeNoException();
                        reply.writeInt(imsSwitch ? 1 : 0);
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setNrSwitch(_arg05, _arg02);
                        reply.writeNoException();
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        setImsDomainConfig(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        boolean handleMapconImsaReq = handleMapconImsaReq(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(handleMapconImsaReq ? 1 : 0);
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = getUiccAppType();
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = getImsDomain();
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        UiccAuthResponse _result24 = handleUiccAuth(data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result24 != null) {
                            reply.writeInt(1);
                            _result24.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerForPhoneEvent = registerForPhoneEvent(data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(registerForPhoneEvent ? 1 : 0);
                        return true;
                    case TRANSACTION_unregisterForPhoneEvent /* 69 */:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterForPhoneEvent(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isRadioAvailableByPhoneId /* 70 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRadioAvailableByPhoneId = isRadioAvailableByPhoneId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isRadioAvailableByPhoneId ? 1 : 0);
                        return true;
                    case TRANSACTION_setImsSwitchByPhoneId /* 71 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setImsSwitchByPhoneId(_arg06, _arg02);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getImsSwitchByPhoneId /* 72 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean imsSwitchByPhoneId = getImsSwitchByPhoneId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(imsSwitchByPhoneId ? 1 : 0);
                        return true;
                    case TRANSACTION_setImsDomainConfigByPhoneId /* 73 */:
                        data.enforceInterface(DESCRIPTOR);
                        setImsDomainConfigByPhoneId(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_handleMapconImsaReqByPhoneId /* 74 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean handleMapconImsaReqByPhoneId = handleMapconImsaReqByPhoneId(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(handleMapconImsaReqByPhoneId ? 1 : 0);
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = getUiccAppTypeByPhoneId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case TRANSACTION_getImsDomainByPhoneId /* 76 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result26 = getImsDomainByPhoneId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case TRANSACTION_handleUiccAuthByPhoneId /* 77 */:
                        data.enforceInterface(DESCRIPTOR);
                        UiccAuthResponse _result27 = handleUiccAuthByPhoneId(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result27 != null) {
                            reply.writeInt(1);
                            _result27.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_registerForWirelessState /* 78 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerForWirelessState = registerForWirelessState(data.readInt(), data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerForWirelessState ? 1 : 0);
                        return true;
                    case TRANSACTION_unregisterForWirelessState /* 79 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterForWirelessState = unregisterForWirelessState(data.readInt(), data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterForWirelessState ? 1 : 0);
                        return true;
                    case TRANSACTION_setMaxTxPower /* 80 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean maxTxPower = setMaxTxPower(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(maxTxPower ? 1 : 0);
                        return true;
                    case TRANSACTION_cmdForECInfo /* 81 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        int _arg14 = data.readInt();
                        byte[] _arg25 = data.createByteArray();
                        boolean cmdForECInfo = cmdForECInfo(_arg07, _arg14, _arg25);
                        reply.writeNoException();
                        reply.writeInt(cmdForECInfo ? 1 : 0);
                        reply.writeByteArray(_arg25);
                        return true;
                    case TRANSACTION_isCtSimCard /* 82 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCtSimCard = isCtSimCard(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isCtSimCard ? 1 : 0);
                        return true;
                    case TRANSACTION_notifyCModemStatus /* 83 */:
                        data.enforceInterface(DESCRIPTOR);
                        notifyCModemStatus(data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_notifyDeviceState /* 84 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean notifyDeviceState = notifyDeviceState(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(notifyDeviceState ? 1 : 0);
                        return true;
                    case TRANSACTION_notifyCellularCommParaReady /* 85 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        notifyCellularCommParaReady(_arg08, _arg15, _arg2);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setPinLockEnabled /* 86 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean pinLockEnabled = setPinLockEnabled(_arg02, data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(pinLockEnabled ? 1 : 0);
                        return true;
                    case TRANSACTION_changeSimPinCode /* 87 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean changeSimPinCode = changeSimPinCode(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(changeSimPinCode ? 1 : 0);
                        return true;
                    case TRANSACTION_sendPseudocellCellInfo /* 88 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean sendPseudocellCellInfo = sendPseudocellCellInfo(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(sendPseudocellCellInfo ? 1 : 0);
                        return true;
                    case TRANSACTION_sendLaaCmd /* 89 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        boolean sendLaaCmd = sendLaaCmd(_arg09, _arg16, _arg22);
                        reply.writeNoException();
                        reply.writeInt(sendLaaCmd ? 1 : 0);
                        return true;
                    case TRANSACTION_getLaaDetailedState /* 90 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean laaDetailedState = getLaaDetailedState(_arg010, _arg12);
                        reply.writeNoException();
                        reply.writeInt(laaDetailedState ? 1 : 0);
                        return true;
                    case TRANSACTION_registerForCallAltSrv /* 91 */:
                        data.enforceInterface(DESCRIPTOR);
                        registerForCallAltSrv(data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_unregisterForCallAltSrv /* 92 */:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterForCallAltSrv(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_invokeOemRilRequestRaw /* 93 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        byte[] _arg17 = data.createByteArray();
                        byte[] _arg26 = data.createByteArray();
                        int _result28 = invokeOemRilRequestRaw(_arg011, _arg17, _arg26);
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        reply.writeByteArray(_arg26);
                        return true;
                    case TRANSACTION_isCspPlmnEnabled /* 94 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCspPlmnEnabled = isCspPlmnEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isCspPlmnEnabled ? 1 : 0);
                        return true;
                    case TRANSACTION_setCallForwardingOption /* 95 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        int _arg18 = data.readInt();
                        int _arg27 = data.readInt();
                        String _arg33 = data.readString();
                        int _arg4 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        setCallForwardingOption(_arg012, _arg18, _arg27, _arg33, _arg4, _arg5);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getCallForwardingOption /* 96 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        int _arg19 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        getCallForwardingOption(_arg013, _arg19, _arg23);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setSubscription /* 97 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg014 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        if (data.readInt() != 0) {
                            _arg24 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        boolean subscription = setSubscription(_arg014, _arg02, _arg24);
                        reply.writeNoException();
                        reply.writeInt(subscription ? 1 : 0);
                        return true;
                    case TRANSACTION_getImsImpu /* 98 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result29 = getImsImpu(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result29);
                        return true;
                    case TRANSACTION_getLine1NumberFromImpu /* 99 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result30 = getLine1NumberFromImpu(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result30);
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSecondaryCardGsmOnly = isSecondaryCardGsmOnly();
                        reply.writeNoException();
                        reply.writeInt(isSecondaryCardGsmOnly ? 1 : 0);
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        boolean bindSimToProfile = bindSimToProfile(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(bindSimToProfile ? 1 : 0);
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        String _result31 = getCdmaGsmImsiForSubId(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result31);
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg015 = data.readInt();
                        String _arg110 = data.readString();
                        String _arg28 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        boolean line1Number = setLine1Number(_arg015, _arg110, _arg28, _arg3);
                        reply.writeNoException();
                        reply.writeInt(line1Number ? 1 : 0);
                        return true;
                    case TRANSACTION_setDeepNoDisturbState /* 104 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deepNoDisturbState = setDeepNoDisturbState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(deepNoDisturbState ? 1 : 0);
                        return true;
                    case TRANSACTION_setUplinkFreqBandwidthReportState /* 105 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean uplinkFreqBandwidthReportState = setUplinkFreqBandwidthReportState(data.readInt(), data.readInt(), IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(uplinkFreqBandwidthReportState ? 1 : 0);
                        return true;
                    case TRANSACTION_informModemTetherStatusToChangeGRO /* 106 */:
                        data.enforceInterface(DESCRIPTOR);
                        informModemTetherStatusToChangeGRO(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_sendSimMatchedOperatorInfo /* 107 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean sendSimMatchedOperatorInfo = sendSimMatchedOperatorInfo(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(sendSimMatchedOperatorInfo ? 1 : 0);
                        return true;
                    case TRANSACTION_getMobilePhysicsLayerStatus /* 108 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean[] _result32 = getMobilePhysicsLayerStatus(data.readInt());
                        reply.writeNoException();
                        reply.writeBooleanArray(_result32);
                        return true;
                    case TRANSACTION_getAntiFakeBaseStation /* 109 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean antiFakeBaseStation = getAntiFakeBaseStation(_arg0);
                        reply.writeNoException();
                        reply.writeInt(antiFakeBaseStation ? 1 : 0);
                        return true;
                    case TRANSACTION_registerForAntiFakeBaseStation /* 110 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerForAntiFakeBaseStation = registerForAntiFakeBaseStation(IPhoneCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerForAntiFakeBaseStation ? 1 : 0);
                        return true;
                    case TRANSACTION_unregisterForAntiFakeBaseStation /* 111 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterForAntiFakeBaseStation = unregisterForAntiFakeBaseStation();
                        reply.writeNoException();
                        reply.writeInt(unregisterForAntiFakeBaseStation ? 1 : 0);
                        return true;
                    case TRANSACTION_getCardTrayInfo /* 112 */:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result33 = getCardTrayInfo();
                        reply.writeNoException();
                        reply.writeByteArray(_result33);
                        return true;
                    case TRANSACTION_setCsconEnabled /* 113 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean csconEnabled = setCsconEnabled(_arg02);
                        reply.writeNoException();
                        reply.writeInt(csconEnabled ? 1 : 0);
                        return true;
                    case TRANSACTION_getCsconEnabled /* 114 */:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result34 = getCsconEnabled();
                        reply.writeNoException();
                        reply.writeIntArray(_result34);
                        return true;
                    case TRANSACTION_getLevel /* 115 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result35 = getLevel(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case TRANSACTION_getDataRegisteredState /* 116 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result36 = getDataRegisteredState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result36);
                        return true;
                    case TRANSACTION_getVoiceRegisteredState /* 117 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result37 = getVoiceRegisteredState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result37);
                        return true;
                    case TRANSACTION_getNetworkModeFromDB /* 118 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result38 = getNetworkModeFromDB(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result38);
                        return true;
                    case TRANSACTION_saveNetworkModeToDB /* 119 */:
                        data.enforceInterface(DESCRIPTOR);
                        saveNetworkModeToDB(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_setTemperatureControlToModem /* 120 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg016 = data.readInt();
                        int _arg111 = data.readInt();
                        int _arg29 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        boolean temperatureControlToModem = setTemperatureControlToModem(_arg016, _arg111, _arg29, _arg32);
                        reply.writeNoException();
                        reply.writeInt(temperatureControlToModem ? 1 : 0);
                        return true;
                    case TRANSACTION_blockingGetIccATR /* 121 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result39 = blockingGetIccATR(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result39);
                        return true;
                    case TRANSACTION_isEuicc /* 122 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEuicc = isEuicc(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isEuicc ? 1 : 0);
                        return true;
                    case TRANSACTION_setNrOptionMode /* 123 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg017 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean nrOptionMode = setNrOptionMode(_arg017, _arg13);
                        reply.writeNoException();
                        reply.writeInt(nrOptionMode ? 1 : 0);
                        return true;
                    case TRANSACTION_getNrOptionMode /* 124 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result40 = getNrOptionMode();
                        reply.writeNoException();
                        reply.writeInt(_result40);
                        return true;
                    case TRANSACTION_isNsaState /* 125 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNsaState = isNsaState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isNsaState ? 1 : 0);
                        return true;
                    case TRANSACTION_getNrCellSsbId /* 126 */:
                        data.enforceInterface(DESCRIPTOR);
                        NrCellSsbId _result41 = getNrCellSsbId(data.readInt());
                        reply.writeNoException();
                        if (_result41 != null) {
                            reply.writeInt(1);
                            _result41.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 127:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAISCard = isAISCard(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAISCard ? 1 : 0);
                        return true;
                    case 128:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCustomAis = isCustomAis();
                        reply.writeNoException();
                        reply.writeInt(isCustomAis ? 1 : 0);
                        return true;
                    case TRANSACTION_getCTOperator /* 129 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result42 = getCTOperator(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result42);
                        return true;
                    case TRANSACTION_getHwInnerService /* 130 */:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result43 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result43);
                        return true;
                    case TRANSACTION_inputDialerSpecialCode /* 131 */:
                        data.enforceInterface(DESCRIPTOR);
                        inputDialerSpecialCode(data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwTelephony {
            public static IHwTelephony sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getDemoString() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDemoString();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getMeidForSubscriber(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMeidForSubscriber(slotId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getPesnForSubscriber(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPesnForSubscriber(slotId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getSubState(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubState(slotId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setUserPrefDataSlotId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserPrefDataSlotId(slotId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void closeRrc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeRrc();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isCTCdmaCardInGsmMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCTCdmaCardInGsmMode();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setDefaultMobileEnable(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultMobileEnable(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setDataEnabledWithoutPromp(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDataEnabledWithoutPromp(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setDataRoamingEnabledWithoutPromp(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDataRoamingEnabledWithoutPromp(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getNVESN() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNVESN();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getDataStateForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataStateForSubscriber(subId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setLteServiceAbility(int ability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ability);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLteServiceAbility(ability);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getLteServiceAbility() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLteServiceAbility();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setServiceAbilityForSlotId(int slotId, int type, int ability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(type);
                    _data.writeInt(ability);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setServiceAbilityForSlotId(slotId, type, ability);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getServiceAbilityForSlotId(int slotId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceAbilityForSlotId(slotId, type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getLteServiceAbilityForSlotId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLteServiceAbilityForSlotId(slotId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setLteServiceAbilityForSlotId(int slotId, int ability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(ability);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLteServiceAbilityForSlotId(slotId, ability);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isImsRegisteredForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isImsRegisteredForSubId(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isWifiCallingAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWifiCallingAvailableForSubId(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isVolteAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVolteAvailableForSubId(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isVideoTelephonyAvailableForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVideoTelephonyAvailableForSubId(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setImsRegistrationStateForSubId(int subId, boolean registered) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(registered ? 1 : 0);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsRegistrationStateForSubId(subId, registered);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void set2GServiceAbility(int ability) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ability);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().set2GServiceAbility(ability);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int get2GServiceAbility() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().get2GServiceAbility();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isSubDeactivedByPowerOff(long sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sub);
                    boolean _result = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSubDeactivedByPowerOff(sub);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isNeedToRadioPowerOn(long sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sub);
                    boolean _result = false;
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNeedToRadioPowerOn(sub);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getDefault4GSlotId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefault4GSlotId();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setDefaultDataSlotId(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultDataSlotId(slotId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void updateCrurrentPhone(int lteSlot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(lteSlot);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateCrurrentPhone(lteSlot);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getSpecCardType(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSpecCardType(slotId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isCardUimLocked(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCardUimLocked(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isRadioOn(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRadioOn(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setPreferredNetworkType(int nwMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nwMode);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPreferredNetworkType(nwMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setDefault4GSlotId(int slotId, Message msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (msg != null) {
                        _data.writeInt(1);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefault4GSlotId(slotId, msg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isSetDefault4GSlotIdEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSetDefault4GSlotIdEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void waitingSetDefault4GSlotDone(boolean waiting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(waiting ? 1 : 0);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().waitingSetDefault4GSlotDone(waiting);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean getWaitingSwitchBalongSlot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWaitingSwitchBalongSlot();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getOnDemandDataSubId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOnDemandDataSubId();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getPreferredDataSubscription() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreferredDataSubscription();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getCdmaGsmImsi() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaGsmImsi();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getUiccCardType(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUiccCardType(slotId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public Bundle getCellLocation(int slotId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCellLocation(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getCdmaMlplVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaMlplVersion();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getCdmaMsplVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaMsplVersion();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getUniqueDeviceId(int scope, String callingPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scope);
                    _data.writeString(callingPackageName);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUniqueDeviceId(scope, callingPackageName);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isLTESupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isLTESupported();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setISMCOEX(String setISMCoex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(setISMCoex);
                    boolean _result = false;
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setISMCOEX(setISMCoex);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isDomesticCard(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDomesticCard(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setWifiTxPower(int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(power);
                    boolean _result = false;
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setWifiTxPower(power);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setCellTxPower(int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(power);
                    boolean _result = false;
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCellTxPower(power);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String[] queryServiceCellBand() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryServiceCellBand();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean registerForRadioAvailable(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerForRadioAvailable(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean unregisterForRadioAvailable(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterForRadioAvailable(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean registerForRadioNotAvailable(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(55, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerForRadioNotAvailable(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean unregisterForRadioNotAvailable(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(56, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterForRadioNotAvailable(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerCommonImsaToMapconInfo(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterCommonImsaToMapconInfo(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isRadioAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(59, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRadioAvailable();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setImsSwitch(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(60, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsSwitch(value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean getImsSwitch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsSwitch();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setNrSwitch(int phoneId, boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(62, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNrSwitch(phoneId, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setImsDomainConfig(int domainType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(domainType);
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsDomainConfig(domainType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean handleMapconImsaReq(byte[] Msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(Msg);
                    boolean _result = false;
                    if (!this.mRemote.transact(64, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleMapconImsaReq(Msg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getUiccAppType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(65, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUiccAppType();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getImsDomain() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(66, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsDomain();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) throws RemoteException {
                UiccAuthResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(auth_type);
                    _data.writeByteArray(rand);
                    _data.writeByteArray(auth);
                    if (!this.mRemote.transact(67, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleUiccAuth(auth_type, rand, auth);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UiccAuthResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean registerForPhoneEvent(int phoneId, IPhoneCallback callback, int events) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(events);
                    boolean _result = false;
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerForPhoneEvent(phoneId, callback, events);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void unregisterForPhoneEvent(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_unregisterForPhoneEvent, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterForPhoneEvent(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isRadioAvailableByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isRadioAvailableByPhoneId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRadioAvailableByPhoneId(phoneId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setImsSwitchByPhoneId(int phoneId, boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_setImsSwitchByPhoneId, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsSwitchByPhoneId(phoneId, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean getImsSwitchByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_getImsSwitchByPhoneId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsSwitchByPhoneId(phoneId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setImsDomainConfigByPhoneId(int phoneId, int domainType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(domainType);
                    if (this.mRemote.transact(Stub.TRANSACTION_setImsDomainConfigByPhoneId, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsDomainConfigByPhoneId(phoneId, domainType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean handleMapconImsaReqByPhoneId(int phoneId, byte[] Msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeByteArray(Msg);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_handleMapconImsaReqByPhoneId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleMapconImsaReqByPhoneId(phoneId, Msg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getUiccAppTypeByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUiccAppTypeByPhoneId(phoneId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getImsDomainByPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getImsDomainByPhoneId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsDomainByPhoneId(phoneId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public UiccAuthResponse handleUiccAuthByPhoneId(int phoneId, int auth_type, byte[] rand, byte[] auth) throws RemoteException {
                UiccAuthResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(auth_type);
                    _data.writeByteArray(rand);
                    _data.writeByteArray(auth);
                    if (!this.mRemote.transact(Stub.TRANSACTION_handleUiccAuthByPhoneId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleUiccAuthByPhoneId(phoneId, auth_type, rand, auth);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UiccAuthResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean registerForWirelessState(int type, int slotId, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_registerForWirelessState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerForWirelessState(type, slotId, callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean unregisterForWirelessState(int type, int slotId, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_unregisterForWirelessState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterForWirelessState(type, slotId, callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setMaxTxPower(int type, int power) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(power);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_setMaxTxPower, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMaxTxPower(type, power);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean cmdForECInfo(int event, int action, byte[] buf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeInt(action);
                    _data.writeByteArray(buf);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_cmdForECInfo, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cmdForECInfo(event, action, buf);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.readByteArray(buf);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isCtSimCard(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isCtSimCard, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCtSimCard(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void notifyCModemStatus(int status, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_notifyCModemStatus, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyCModemStatus(status, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean notifyDeviceState(String device, String state, String extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(device);
                    _data.writeString(state);
                    _data.writeString(extras);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_notifyDeviceState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyDeviceState(device, state, extras);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(paratype);
                    _data.writeInt(pathtype);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_notifyCellularCommParaReady, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyCellularCommParaReady(paratype, pathtype, response);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setPinLockEnabled(boolean enablePinLock, String password, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enablePinLock ? 1 : 0);
                    _data.writeString(password);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setPinLockEnabled, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPinLockEnabled(enablePinLock, password, subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean changeSimPinCode(String oldPinCode, String newPinCode, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(oldPinCode);
                    _data.writeString(newPinCode);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_changeSimPinCode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().changeSimPinCode(oldPinCode, newPinCode, subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean sendPseudocellCellInfo(int type, int lac, int cid, int radioTech, String plmn, int subId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(type);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(lac);
                        try {
                            _data.writeInt(cid);
                            try {
                                _data.writeInt(radioTech);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(plmn);
                        try {
                            _data.writeInt(subId);
                            boolean _result = false;
                            if (this.mRemote.transact(Stub.TRANSACTION_sendPseudocellCellInfo, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean sendPseudocellCellInfo = Stub.getDefaultImpl().sendPseudocellCellInfo(type, lac, cid, radioTech, plmn, subId);
                            _reply.recycle();
                            _data.recycle();
                            return sendPseudocellCellInfo;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean sendLaaCmd(int cmd, String reserved, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmd);
                    _data.writeString(reserved);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_sendLaaCmd, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendLaaCmd(cmd, reserved, response);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean getLaaDetailedState(String reserved, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reserved);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_getLaaDetailedState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLaaDetailedState(reserved, response);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void registerForCallAltSrv(int subId, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_registerForCallAltSrv, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerForCallAltSrv(subId, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void unregisterForCallAltSrv(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(Stub.TRANSACTION_unregisterForCallAltSrv, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterForCallAltSrv(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int invokeOemRilRequestRaw(int phoneId, byte[] oemReq, byte[] oemResp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeByteArray(oemReq);
                    _data.writeByteArray(oemResp);
                    if (!this.mRemote.transact(Stub.TRANSACTION_invokeOemRilRequestRaw, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().invokeOemRilRequestRaw(phoneId, oemReq, oemResp);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(oemResp);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isCspPlmnEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isCspPlmnEnabled, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCspPlmnEnabled(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void setCallForwardingOption(int subId, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message response) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                        try {
                            _data.writeInt(commandInterfaceCFAction);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(commandInterfaceCFReason);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(dialingNumber);
                        try {
                            _data.writeInt(timerSeconds);
                            if (response != null) {
                                _data.writeInt(1);
                                response.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(Stub.TRANSACTION_setCallForwardingOption, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().setCallForwardingOption(subId, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, response);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void getCallForwardingOption(int subId, int commandInterfaceCFReason, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(commandInterfaceCFReason);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_getCallForwardingOption, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getCallForwardingOption(subId, commandInterfaceCFReason, response);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setSubscription(int subId, boolean activate, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = true;
                    _data.writeInt(activate ? 1 : 0);
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_setSubscription, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSubscription(subId, activate, response);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getImsImpu(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getImsImpu, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsImpu(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getLine1NumberFromImpu(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getLine1NumberFromImpu, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLine1NumberFromImpu(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isSecondaryCardGsmOnly() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(100, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSecondaryCardGsmOnly();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean bindSimToProfile(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(101, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().bindSimToProfile(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getCdmaGsmImsiForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(102, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaGsmImsiForSubId(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setLine1Number(int subId, String alphaTag, String number, Message onComplete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(alphaTag);
                    _data.writeString(number);
                    boolean _result = true;
                    if (onComplete != null) {
                        _data.writeInt(1);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(103, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setLine1Number(subId, alphaTag, number, onComplete);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setDeepNoDisturbState(int slotId, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(state);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_setDeepNoDisturbState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeepNoDisturbState(slotId, state);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setUplinkFreqBandwidthReportState(int slotId, int state, IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(state);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_setUplinkFreqBandwidthReportState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setUplinkFreqBandwidthReportState(slotId, state, callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void informModemTetherStatusToChangeGRO(int enable, String faceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeString(faceName);
                    if (this.mRemote.transact(Stub.TRANSACTION_informModemTetherStatusToChangeGRO, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().informModemTetherStatusToChangeGRO(enable, faceName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean sendSimMatchedOperatorInfo(int slotId, String opKey, String opName, int state, String reserveField) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(slotId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(opKey);
                        try {
                            _data.writeString(opName);
                            try {
                                _data.writeInt(state);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(reserveField);
                        try {
                            boolean _result = false;
                            if (this.mRemote.transact(Stub.TRANSACTION_sendSimMatchedOperatorInfo, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean sendSimMatchedOperatorInfo = Stub.getDefaultImpl().sendSimMatchedOperatorInfo(slotId, opKey, opName, state, reserveField);
                            _reply.recycle();
                            _data.recycle();
                            return sendSimMatchedOperatorInfo;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean[] getMobilePhysicsLayerStatus(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getMobilePhysicsLayerStatus, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMobilePhysicsLayerStatus(slotId);
                    }
                    _reply.readException();
                    boolean[] _result = _reply.createBooleanArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean getAntiFakeBaseStation(Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_getAntiFakeBaseStation, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAntiFakeBaseStation(response);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean registerForAntiFakeBaseStation(IPhoneCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_registerForAntiFakeBaseStation, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerForAntiFakeBaseStation(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean unregisterForAntiFakeBaseStation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_unregisterForAntiFakeBaseStation, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterForAntiFakeBaseStation();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public byte[] getCardTrayInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCardTrayInfo, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCardTrayInfo();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setCsconEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setCsconEnabled, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCsconEnabled(enable);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int[] getCsconEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCsconEnabled, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCsconEnabled();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getLevel(int type, int rssi, int ecio, int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(rssi);
                    _data.writeInt(ecio);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getLevel, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLevel(type, rssi, ecio, phoneId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getDataRegisteredState(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getDataRegisteredState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataRegisteredState(phoneId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getVoiceRegisteredState(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getVoiceRegisteredState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceRegisteredState(phoneId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getNetworkModeFromDB(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getNetworkModeFromDB, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkModeFromDB(slotId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void saveNetworkModeToDB(int slotId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(Stub.TRANSACTION_saveNetworkModeToDB, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().saveNetworkModeToDB(slotId, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setTemperatureControlToModem(int level, int type, int subId, Message response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    _data.writeInt(type);
                    _data.writeInt(subId);
                    boolean _result = true;
                    if (response != null) {
                        _data.writeInt(1);
                        response.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_setTemperatureControlToModem, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setTemperatureControlToModem(level, type, subId, response);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String blockingGetIccATR(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    if (!this.mRemote.transact(Stub.TRANSACTION_blockingGetIccATR, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().blockingGetIccATR(index);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isEuicc(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isEuicc, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEuicc(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean setNrOptionMode(int mode, Message msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    boolean _result = true;
                    if (msg != null) {
                        _data.writeInt(1);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(Stub.TRANSACTION_setNrOptionMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setNrOptionMode(mode, msg);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public int getNrOptionMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getNrOptionMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNrOptionMode();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isNsaState(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isNsaState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNsaState(phoneId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public NrCellSsbId getNrCellSsbId(int slotId) throws RemoteException {
                NrCellSsbId _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getNrCellSsbId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNrCellSsbId(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NrCellSsbId.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isAISCard(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = false;
                    if (!this.mRemote.transact(127, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAISCard(slotId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public boolean isCustomAis() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(128, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCustomAis();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public String getCTOperator(int slotId, String operator) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeString(operator);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCTOperator, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCTOperator(slotId, operator);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getHwInnerService, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwTelephony
            public void inputDialerSpecialCode(String inputCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputCode);
                    if (this.mRemote.transact(Stub.TRANSACTION_inputDialerSpecialCode, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().inputDialerSpecialCode(inputCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwTelephony impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwTelephony getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
