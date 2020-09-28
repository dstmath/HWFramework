package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.WorkSource;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telephony.CarrierRestrictionRules;
import android.telephony.CellInfo;
import android.telephony.ClientRequestStats;
import android.telephony.ICellInfoCallback;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.NeighboringCellInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneNumberRange;
import android.telephony.RadioAccessFamily;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyHistogram;
import android.telephony.UiccCardInfo;
import android.telephony.UiccSlotInfo;
import android.telephony.VisualVoicemailSmsFilterSettings;
import android.telephony.emergency.EmergencyNumber;
import android.telephony.ims.aidl.IImsCapabilityCallback;
import android.telephony.ims.aidl.IImsConfig;
import android.telephony.ims.aidl.IImsConfigCallback;
import android.telephony.ims.aidl.IImsMmTelFeature;
import android.telephony.ims.aidl.IImsRcsFeature;
import android.telephony.ims.aidl.IImsRegistration;
import android.telephony.ims.aidl.IImsRegistrationCallback;
import com.android.ims.internal.IImsServiceFeatureCallback;
import com.android.internal.telephony.IIntegerConsumer;
import com.android.internal.telephony.INumberVerificationCallback;
import java.util.List;
import java.util.Map;

public interface ITelephony extends IInterface {
    void cacheMmTelCapabilityProvisioning(int i, int i2, int i3, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void call(String str, String str2) throws RemoteException;

    boolean canChangeDtmfToneLength(int i, String str) throws RemoteException;

    void carrierActionReportDefaultNetworkStatus(int i, boolean z) throws RemoteException;

    void carrierActionResetAll(int i) throws RemoteException;

    void carrierActionSetMeteredApnsEnabled(int i, boolean z) throws RemoteException;

    void carrierActionSetRadioEnabled(int i, boolean z) throws RemoteException;

    int checkCarrierPrivilegesForPackage(int i, String str) throws RemoteException;

    int checkCarrierPrivilegesForPackageAnyPhone(String str) throws RemoteException;

    @UnsupportedAppUsage
    void dial(String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean disableDataConnectivity() throws RemoteException;

    void disableIms(int i) throws RemoteException;

    @UnsupportedAppUsage
    void disableLocationUpdates() throws RemoteException;

    void disableLocationUpdatesForSubscriber(int i) throws RemoteException;

    void disableVisualVoicemailSmsFilter(String str, int i) throws RemoteException;

    boolean doesSwitchMultiSimConfigTriggerReboot(int i, String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean enableDataConnectivity() throws RemoteException;

    void enableIms(int i) throws RemoteException;

    @UnsupportedAppUsage
    void enableLocationUpdates() throws RemoteException;

    void enableLocationUpdatesForSubscriber(int i) throws RemoteException;

    boolean enableModemForSlot(int i, boolean z) throws RemoteException;

    void enableVideoCalling(boolean z) throws RemoteException;

    void enableVisualVoicemailSmsFilter(String str, int i, VisualVoicemailSmsFilterSettings visualVoicemailSmsFilterSettings) throws RemoteException;

    void enqueueSmsPickResult(String str, IIntegerConsumer iIntegerConsumer) throws RemoteException;

    void factoryReset(int i) throws RemoteException;

    @UnsupportedAppUsage
    int getActivePhoneType() throws RemoteException;

    int getActivePhoneTypeForSlot(int i) throws RemoteException;

    VisualVoicemailSmsFilterSettings getActiveVisualVoicemailSmsFilterSettings(int i) throws RemoteException;

    String getAidForAppType(int i, int i2) throws RemoteException;

    List<CellInfo> getAllCellInfo(String str) throws RemoteException;

    CarrierRestrictionRules getAllowedCarriers() throws RemoteException;

    int getCalculatedPreferredNetworkType(String str) throws RemoteException;

    @UnsupportedAppUsage
    int getCallState() throws RemoteException;

    int getCallStateForSlot(int i) throws RemoteException;

    int getCardIdForDefaultEuicc(int i, String str) throws RemoteException;

    int getCarrierIdFromMccMnc(int i, String str, boolean z) throws RemoteException;

    int getCarrierIdListVersion(int i) throws RemoteException;

    List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int i) throws RemoteException;

    int getCarrierPrivilegeStatus(int i) throws RemoteException;

    int getCarrierPrivilegeStatusForUid(int i, int i2) throws RemoteException;

    int getCdmaEriIconIndex(String str) throws RemoteException;

    int getCdmaEriIconIndexForSubscriber(int i, String str) throws RemoteException;

    int getCdmaEriIconMode(String str) throws RemoteException;

    int getCdmaEriIconModeForSubscriber(int i, String str) throws RemoteException;

    String getCdmaEriText(String str) throws RemoteException;

    String getCdmaEriTextForSubscriber(int i, String str) throws RemoteException;

    String getCdmaMdn(int i) throws RemoteException;

    String getCdmaMin(int i) throws RemoteException;

    String getCdmaPrlVersion(int i) throws RemoteException;

    int getCdmaRoamingMode(int i) throws RemoteException;

    Bundle getCellLocation(String str) throws RemoteException;

    CellNetworkScanResult getCellNetworkScanResults(int i, String str) throws RemoteException;

    List<String> getCertsFromCarrierPrivilegeAccessRules(int i) throws RemoteException;

    List<ClientRequestStats> getClientRequestStats(String str, int i) throws RemoteException;

    int getDataActivationState(int i, String str) throws RemoteException;

    @UnsupportedAppUsage
    int getDataActivity() throws RemoteException;

    @UnsupportedAppUsage
    boolean getDataEnabled(int i) throws RemoteException;

    int getDataNetworkType(String str) throws RemoteException;

    int getDataNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    @UnsupportedAppUsage
    int getDataState() throws RemoteException;

    String getDefaultSmsApp(int i) throws RemoteException;

    String getDeviceId(String str) throws RemoteException;

    String getDeviceSoftwareVersionForSlot(int i, String str) throws RemoteException;

    boolean getEmergencyCallbackMode(int i) throws RemoteException;

    Map getEmergencyNumberList(String str) throws RemoteException;

    List<String> getEmergencyNumberListTestMode() throws RemoteException;

    String getEsn(int i) throws RemoteException;

    String[] getForbiddenPlmns(int i, int i2, String str) throws RemoteException;

    String getImeiForSlot(int i, String str) throws RemoteException;

    IImsConfig getImsConfig(int i, int i2) throws RemoteException;

    int getImsProvisioningInt(int i, int i2) throws RemoteException;

    boolean getImsProvisioningStatusForCapability(int i, int i2, int i3) throws RemoteException;

    String getImsProvisioningString(int i, int i2) throws RemoteException;

    int getImsRegTechnologyForMmTel(int i) throws RemoteException;

    IImsRegistration getImsRegistration(int i, int i2) throws RemoteException;

    String getImsService(int i, boolean z) throws RemoteException;

    String getLine1AlphaTagForDisplay(int i, String str) throws RemoteException;

    String getLine1NumberForDisplay(int i, String str) throws RemoteException;

    int getLteOnCdmaMode(String str) throws RemoteException;

    int getLteOnCdmaModeForSubscriber(int i, String str) throws RemoteException;

    String getManufacturerCodeForSlot(int i) throws RemoteException;

    String getMeidForSlot(int i, String str) throws RemoteException;

    String[] getMergedSubscriberIds(int i, String str) throws RemoteException;

    IImsMmTelFeature getMmTelFeatureAndListen(int i, IImsServiceFeatureCallback iImsServiceFeatureCallback) throws RemoteException;

    String getMmsUAProfUrl(int i) throws RemoteException;

    String getMmsUserAgent(int i) throws RemoteException;

    List<NeighboringCellInfo> getNeighboringCellInfo(String str) throws RemoteException;

    String getNetworkCountryIsoForPhone(int i) throws RemoteException;

    int getNetworkSelectionMode(int i) throws RemoteException;

    int getNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    int getNumberOfModemsWithSimultaneousDataConnections(int i, String str) throws RemoteException;

    List<String> getPackagesWithCarrierPrivileges(int i) throws RemoteException;

    List<String> getPackagesWithCarrierPrivilegesForAllPhones() throws RemoteException;

    String[] getPcscfAddress(String str, String str2) throws RemoteException;

    PhoneAccountHandle getPhoneAccountHandleForSubscriptionId(int i) throws RemoteException;

    int getPreferredNetworkType(int i) throws RemoteException;

    int getRadioAccessFamily(int i, String str) throws RemoteException;

    int getRadioHalVersion() throws RemoteException;

    int getRadioPowerState(int i, String str) throws RemoteException;

    IImsRcsFeature getRcsFeatureAndListen(int i, IImsServiceFeatureCallback iImsServiceFeatureCallback) throws RemoteException;

    ServiceState getServiceStateForSubscriber(int i, String str) throws RemoteException;

    SignalStrength getSignalStrength(int i) throws RemoteException;

    String getSimLocaleForSubscriber(int i) throws RemoteException;

    int[] getSlotsMapping() throws RemoteException;

    String[] getSmsApps(int i) throws RemoteException;

    int getSubIdForPhoneAccount(PhoneAccount phoneAccount) throws RemoteException;

    int getSubscriptionCarrierId(int i) throws RemoteException;

    String getSubscriptionCarrierName(int i) throws RemoteException;

    int getSubscriptionSpecificCarrierId(int i) throws RemoteException;

    String getSubscriptionSpecificCarrierName(int i) throws RemoteException;

    List<TelephonyHistogram> getTelephonyHistograms() throws RemoteException;

    boolean getTetherApnRequiredForSubscriber(int i) throws RemoteException;

    String getTypeAllocationCodeForSlot(int i) throws RemoteException;

    List<UiccCardInfo> getUiccCardsInfo(String str) throws RemoteException;

    UiccSlotInfo[] getUiccSlotsInfo() throws RemoteException;

    String getVisualVoicemailPackageName(String str, int i) throws RemoteException;

    Bundle getVisualVoicemailSettings(String str, int i) throws RemoteException;

    VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings(String str, int i) throws RemoteException;

    int getVoWiFiModeSetting(int i) throws RemoteException;

    int getVoWiFiRoamingModeSetting(int i) throws RemoteException;

    int getVoiceActivationState(int i, String str) throws RemoteException;

    int getVoiceMessageCountForSubscriber(int i, String str) throws RemoteException;

    int getVoiceNetworkTypeForSubscriber(int i, String str) throws RemoteException;

    Uri getVoicemailRingtoneUri(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    NetworkStats getVtDataUsage(int i, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    boolean handlePinMmi(String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean handlePinMmiForSubscriber(int i, String str) throws RemoteException;

    void handleUssdRequest(int i, String str, ResultReceiver resultReceiver) throws RemoteException;

    @UnsupportedAppUsage
    boolean hasIccCard() throws RemoteException;

    boolean hasIccCardUsingSlotIndex(int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean iccCloseLogicalChannel(int i, int i2) throws RemoteException;

    boolean iccCloseLogicalChannelBySlot(int i, int i2) throws RemoteException;

    byte[] iccExchangeSimIO(int i, int i2, int i3, int i4, int i5, int i6, String str) throws RemoteException;

    IccOpenLogicalChannelResponse iccOpenLogicalChannel(int i, String str, String str2, int i2) throws RemoteException;

    IccOpenLogicalChannelResponse iccOpenLogicalChannelBySlot(int i, String str, String str2, int i2) throws RemoteException;

    String iccTransmitApduBasicChannel(int i, String str, int i2, int i3, int i4, int i5, int i6, String str2) throws RemoteException;

    String iccTransmitApduBasicChannelBySlot(int i, String str, int i2, int i3, int i4, int i5, int i6, String str2) throws RemoteException;

    @UnsupportedAppUsage
    String iccTransmitApduLogicalChannel(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str) throws RemoteException;

    String iccTransmitApduLogicalChannelBySlot(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str) throws RemoteException;

    int invokeOemRilRequestRaw(byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean isAdvancedCallingSettingEnabled(int i) throws RemoteException;

    boolean isApnMetered(int i, int i2) throws RemoteException;

    boolean isAvailable(int i, int i2, int i3) throws RemoteException;

    boolean isCapable(int i, int i2, int i3) throws RemoteException;

    boolean isConcurrentVoiceAndDataAllowed(int i) throws RemoteException;

    boolean isDataAllowedInVoiceCall(int i) throws RemoteException;

    boolean isDataConnectivityPossible(int i) throws RemoteException;

    boolean isDataEnabled(int i) throws RemoteException;

    boolean isDataEnabledForApn(int i, int i2, String str) throws RemoteException;

    boolean isDataRoamingEnabled(int i) throws RemoteException;

    boolean isEmergencyNumber(String str, boolean z) throws RemoteException;

    boolean isEmergencyNumberForSubId(int i, String str, boolean z) throws RemoteException;

    boolean isHearingAidCompatibilitySupported() throws RemoteException;

    boolean isImsRegistered(int i) throws RemoteException;

    boolean isInEmergencySmsMode() throws RemoteException;

    boolean isManualNetworkSelectionAllowed(int i) throws RemoteException;

    boolean isMmTelCapabilityProvisionedInCache(int i, int i2, int i3) throws RemoteException;

    boolean isModemEnabledForSlot(int i, String str) throws RemoteException;

    int isMultiSimSupported(String str) throws RemoteException;

    boolean isRadioOn(String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean isRadioOnForSubscriber(int i, String str) throws RemoteException;

    boolean isRttSupported(int i) throws RemoteException;

    boolean isTtyModeSupported() throws RemoteException;

    boolean isTtyOverVolteEnabled(int i) throws RemoteException;

    boolean isUserDataEnabled(int i) throws RemoteException;

    boolean isVideoCallingEnabled(String str) throws RemoteException;

    boolean isVideoTelephonyAvailable(int i) throws RemoteException;

    boolean isVoWiFiRoamingSettingEnabled(int i) throws RemoteException;

    boolean isVoWiFiSettingEnabled(int i) throws RemoteException;

    boolean isVoicemailVibrationEnabled(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    boolean isVtSettingEnabled(int i) throws RemoteException;

    boolean isWifiCallingAvailable(int i) throws RemoteException;

    boolean isWorldPhone(int i, String str) throws RemoteException;

    boolean needMobileRadioShutdown() throws RemoteException;

    boolean needsOtaServiceProvisioning() throws RemoteException;

    String nvReadItem(int i) throws RemoteException;

    boolean nvWriteCdmaPrl(byte[] bArr) throws RemoteException;

    boolean nvWriteItem(int i, String str) throws RemoteException;

    boolean rebootModem(int i) throws RemoteException;

    void refreshUiccProfile(int i) throws RemoteException;

    void registerImsProvisioningChangedCallback(int i, IImsConfigCallback iImsConfigCallback) throws RemoteException;

    void registerImsRegistrationCallback(int i, IImsRegistrationCallback iImsRegistrationCallback) throws RemoteException;

    void registerMmTelCapabilityCallback(int i, IImsCapabilityCallback iImsCapabilityCallback) throws RemoteException;

    void requestCellInfoUpdate(int i, ICellInfoCallback iCellInfoCallback, String str) throws RemoteException;

    void requestCellInfoUpdateWithWorkSource(int i, ICellInfoCallback iCellInfoCallback, String str, WorkSource workSource) throws RemoteException;

    void requestModemActivityInfo(ResultReceiver resultReceiver) throws RemoteException;

    int requestNetworkScan(int i, NetworkScanRequest networkScanRequest, Messenger messenger, IBinder iBinder, String str) throws RemoteException;

    void requestNumberVerification(PhoneNumberRange phoneNumberRange, long j, INumberVerificationCallback iNumberVerificationCallback, String str) throws RemoteException;

    boolean resetModemConfig(int i) throws RemoteException;

    void sendDialerSpecialCode(String str, String str2) throws RemoteException;

    String sendEnvelopeWithStatus(int i, String str) throws RemoteException;

    void sendVisualVoicemailSmsForSubscriber(String str, int i, String str2, int i2, String str3, PendingIntent pendingIntent) throws RemoteException;

    void setAdvancedCallingSettingEnabled(int i, boolean z) throws RemoteException;

    int setAllowedCarriers(CarrierRestrictionRules carrierRestrictionRules) throws RemoteException;

    void setCarrierTestOverride(int i, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) throws RemoteException;

    boolean setCdmaRoamingMode(int i, int i2) throws RemoteException;

    boolean setCdmaSubscriptionMode(int i, int i2) throws RemoteException;

    void setCellInfoListRate(int i) throws RemoteException;

    void setDataActivationState(int i, int i2) throws RemoteException;

    boolean setDataAllowedDuringVoiceCall(int i, boolean z) throws RemoteException;

    void setDataEnabledProperties(String str, boolean z) throws RemoteException;

    void setDataRoamingEnabled(int i, boolean z) throws RemoteException;

    void setDefaultSmsApp(int i, String str) throws RemoteException;

    int setImsProvisioningInt(int i, int i2, int i3) throws RemoteException;

    void setImsProvisioningStatusForCapability(int i, int i2, int i3, boolean z) throws RemoteException;

    int setImsProvisioningString(int i, int i2, String str) throws RemoteException;

    void setImsRegistrationState(boolean z) throws RemoteException;

    boolean setImsService(int i, boolean z, String str) throws RemoteException;

    boolean setLine1NumberForDisplayForSubscriber(int i, String str, String str2) throws RemoteException;

    void setMultiSimCarrierRestriction(boolean z) throws RemoteException;

    void setNetworkSelectionModeAutomatic(int i) throws RemoteException;

    boolean setNetworkSelectionModeManual(int i, OperatorInfo operatorInfo, boolean z) throws RemoteException;

    boolean setOperatorBrandOverride(int i, String str) throws RemoteException;

    void setPolicyDataEnabled(boolean z, int i) throws RemoteException;

    boolean setPreferredNetworkType(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    boolean setRadio(boolean z) throws RemoteException;

    void setRadioCapability(RadioAccessFamily[] radioAccessFamilyArr) throws RemoteException;

    boolean setRadioForSubscriber(int i, boolean z) throws RemoteException;

    void setRadioIndicationUpdateMode(int i, int i2, int i3) throws RemoteException;

    boolean setRadioPower(boolean z) throws RemoteException;

    boolean setRoamingOverride(int i, List<String> list, List<String> list2, List<String> list3, List<String> list4) throws RemoteException;

    void setRttCapabilitySetting(int i, boolean z) throws RemoteException;

    void setSimPowerStateForSlot(int i, int i2) throws RemoteException;

    void setUserDataEnabled(int i, boolean z) throws RemoteException;

    void setVoWiFiModeSetting(int i, int i2) throws RemoteException;

    void setVoWiFiNonPersistent(int i, boolean z, int i2) throws RemoteException;

    void setVoWiFiRoamingModeSetting(int i, int i2) throws RemoteException;

    void setVoWiFiRoamingSettingEnabled(int i, boolean z) throws RemoteException;

    void setVoWiFiSettingEnabled(int i, boolean z) throws RemoteException;

    void setVoiceActivationState(int i, int i2) throws RemoteException;

    boolean setVoiceMailNumber(int i, String str, String str2) throws RemoteException;

    void setVoicemailRingtoneUri(String str, PhoneAccountHandle phoneAccountHandle, Uri uri) throws RemoteException;

    void setVoicemailVibrationEnabled(String str, PhoneAccountHandle phoneAccountHandle, boolean z) throws RemoteException;

    void setVtSettingEnabled(int i, boolean z) throws RemoteException;

    void shutdownMobileRadios() throws RemoteException;

    void stopNetworkScan(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    boolean supplyPin(String str) throws RemoteException;

    boolean supplyPinForSubscriber(int i, String str) throws RemoteException;

    int[] supplyPinReportResult(String str) throws RemoteException;

    int[] supplyPinReportResultForSubscriber(int i, String str) throws RemoteException;

    boolean supplyPuk(String str, String str2) throws RemoteException;

    boolean supplyPukForSubscriber(int i, String str, String str2) throws RemoteException;

    int[] supplyPukReportResult(String str, String str2) throws RemoteException;

    int[] supplyPukReportResultForSubscriber(int i, String str, String str2) throws RemoteException;

    void switchMultiSimConfig(int i) throws RemoteException;

    boolean switchSlots(int[] iArr) throws RemoteException;

    @UnsupportedAppUsage
    void toggleRadioOnOff() throws RemoteException;

    void toggleRadioOnOffForSubscriber(int i) throws RemoteException;

    void unregisterImsProvisioningChangedCallback(int i, IImsConfigCallback iImsConfigCallback) throws RemoteException;

    void unregisterImsRegistrationCallback(int i, IImsRegistrationCallback iImsRegistrationCallback) throws RemoteException;

    void unregisterMmTelCapabilityCallback(int i, IImsCapabilityCallback iImsCapabilityCallback) throws RemoteException;

    void updateEmergencyNumberListTestMode(int i, EmergencyNumber emergencyNumber) throws RemoteException;

    @UnsupportedAppUsage
    void updateServiceLocation() throws RemoteException;

    void updateServiceLocationForSubscriber(int i) throws RemoteException;

    public static class Default implements ITelephony {
        @Override // com.android.internal.telephony.ITelephony
        public void dial(String number) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void call(String callingPackage, String number) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isRadioOn(String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isRadioOnForSubscriber(int subId, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean supplyPin(String pin) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean supplyPinForSubscriber(int subId, String pin) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean supplyPuk(String puk, String pin) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean supplyPukForSubscriber(int subId, String puk, String pin) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int[] supplyPinReportResult(String pin) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int[] supplyPinReportResultForSubscriber(int subId, String pin) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int[] supplyPukReportResult(String puk, String pin) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int[] supplyPukReportResultForSubscriber(int subId, String puk, String pin) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean handlePinMmi(String dialString) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void handleUssdRequest(int subId, String ussdRequest, ResultReceiver wrappedCallback) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean handlePinMmiForSubscriber(int subId, String dialString) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void toggleRadioOnOff() throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void toggleRadioOnOffForSubscriber(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setRadio(boolean turnOn) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setRadioForSubscriber(int subId, boolean turnOn) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setRadioPower(boolean turnOn) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void updateServiceLocation() throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void updateServiceLocationForSubscriber(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void enableLocationUpdates() throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void enableLocationUpdatesForSubscriber(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void disableLocationUpdates() throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void disableLocationUpdatesForSubscriber(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean enableDataConnectivity() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean disableDataConnectivity() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isDataConnectivityPossible(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public Bundle getCellLocation(String callingPkg) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getNetworkCountryIsoForPhone(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<NeighboringCellInfo> getNeighboringCellInfo(String callingPkg) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCallState() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCallStateForSlot(int slotIndex) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getDataActivity() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getDataState() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getActivePhoneType() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getActivePhoneTypeForSlot(int slotIndex) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCdmaEriIconIndex(String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCdmaEriIconIndexForSubscriber(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCdmaEriIconMode(String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCdmaEriIconModeForSubscriber(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getCdmaEriText(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getCdmaEriTextForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean needsOtaServiceProvisioning() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setVoiceMailNumber(int subId, String alphaTag, String number) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVoiceActivationState(int subId, int activationState) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setDataActivationState(int subId, int activationState) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getVoiceActivationState(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getDataActivationState(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getVoiceMessageCountForSubscriber(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isConcurrentVoiceAndDataAllowed(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public Bundle getVisualVoicemailSettings(String callingPackage, int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getVisualVoicemailPackageName(String callingPackage, int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void enableVisualVoicemailSmsFilter(String callingPackage, int subId, VisualVoicemailSmsFilterSettings settings) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void disableVisualVoicemailSmsFilter(String callingPackage, int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings(String callingPackage, int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public VisualVoicemailSmsFilterSettings getActiveVisualVoicemailSmsFilterSettings(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void sendVisualVoicemailSmsForSubscriber(String callingPackage, int subId, String number, int port, String text, PendingIntent sentIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void sendDialerSpecialCode(String callingPackageName, String inputCode) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getDataNetworkType(String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getDataNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getVoiceNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean hasIccCard() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean hasIccCardUsingSlotIndex(int slotIndex) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getLteOnCdmaMode(String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getLteOnCdmaModeForSubscriber(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<CellInfo> getAllCellInfo(String callingPkg) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void requestCellInfoUpdate(int subId, ICellInfoCallback cb, String callingPkg) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void requestCellInfoUpdateWithWorkSource(int subId, ICellInfoCallback cb, String callingPkg, WorkSource ws) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setCellInfoListRate(int rateInMillis) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public IccOpenLogicalChannelResponse iccOpenLogicalChannelBySlot(int slotIndex, String callingPackage, String AID, int p2) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public IccOpenLogicalChannelResponse iccOpenLogicalChannel(int subId, String callingPackage, String AID, int p2) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean iccCloseLogicalChannelBySlot(int slotIndex, int channel) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean iccCloseLogicalChannel(int subId, int channel) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String iccTransmitApduLogicalChannelBySlot(int slotIndex, int channel, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String iccTransmitApduLogicalChannel(int subId, int channel, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String iccTransmitApduBasicChannelBySlot(int slotIndex, String callingPackage, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String iccTransmitApduBasicChannel(int subId, String callingPackage, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public byte[] iccExchangeSimIO(int subId, int fileID, int command, int p1, int p2, int p3, String filePath) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String sendEnvelopeWithStatus(int subId, String content) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String nvReadItem(int itemID) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean nvWriteItem(int itemID, String itemValue) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean nvWriteCdmaPrl(byte[] preferredRoamingList) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean resetModemConfig(int slotIndex) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean rebootModem(int slotIndex) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCalculatedPreferredNetworkType(String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getPreferredNetworkType(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean getTetherApnRequiredForSubscriber(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void enableIms(int slotId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void disableIms(int slotId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public IImsMmTelFeature getMmTelFeatureAndListen(int slotId, IImsServiceFeatureCallback callback) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public IImsRcsFeature getRcsFeatureAndListen(int slotId, IImsServiceFeatureCallback callback) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public IImsRegistration getImsRegistration(int slotId, int feature) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public IImsConfig getImsConfig(int slotId, int feature) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setImsService(int slotId, boolean isCarrierImsService, String packageName) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getImsService(int slotId, boolean isCarrierImsService) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setNetworkSelectionModeAutomatic(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public CellNetworkScanResult getCellNetworkScanResults(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int requestNetworkScan(int subId, NetworkScanRequest request, Messenger messenger, IBinder binder, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void stopNetworkScan(int subId, int scanId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setNetworkSelectionModeManual(int subId, OperatorInfo operatorInfo, boolean persisSelection) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setPreferredNetworkType(int subId, int networkType) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setUserDataEnabled(int subId, boolean enable) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean getDataEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isUserDataEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isDataEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isManualNetworkSelectionAllowed(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String[] getPcscfAddress(String apnType, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setImsRegistrationState(boolean registered) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getCdmaMdn(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getCdmaMin(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void requestNumberVerification(PhoneNumberRange range, long timeoutMillis, INumberVerificationCallback callback, String callingPackage) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCarrierPrivilegeStatus(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCarrierPrivilegeStatusForUid(int subId, int uid) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int checkCarrierPrivilegesForPackage(int subId, String pkgName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int checkCarrierPrivilegesForPackageAnyPhone(String pkgName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setLine1NumberForDisplayForSubscriber(int subId, String alphaTag, String number) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getLine1NumberForDisplay(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getLine1AlphaTagForDisplay(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String[] getMergedSubscriberIds(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setOperatorBrandOverride(int subId, String brand) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setRoamingOverride(int subId, List<String> list, List<String> list2, List<String> list3, List<String> list4) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean needMobileRadioShutdown() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void shutdownMobileRadios() throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setRadioCapability(RadioAccessFamily[] rafs) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getRadioAccessFamily(int phoneId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void enableVideoCalling(boolean enable) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isVideoCallingEnabled(String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean canChangeDtmfToneLength(int subId, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isWorldPhone(int subId, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isTtyModeSupported() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isRttSupported(int subscriptionId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isHearingAidCompatibilitySupported() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isImsRegistered(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isWifiCallingAvailable(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isVideoTelephonyAvailable(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getImsRegTechnologyForMmTel(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getDeviceId(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getImeiForSlot(int slotIndex, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getTypeAllocationCodeForSlot(int slotIndex) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getMeidForSlot(int slotIndex, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getManufacturerCodeForSlot(int slotIndex) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getDeviceSoftwareVersionForSlot(int slotIndex, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getSubIdForPhoneAccount(PhoneAccount phoneAccount) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public PhoneAccountHandle getPhoneAccountHandleForSubscriptionId(int subscriptionId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void factoryReset(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getSimLocaleForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void requestModemActivityInfo(ResultReceiver result) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public ServiceState getServiceStateForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public Uri getVoicemailRingtoneUri(PhoneAccountHandle accountHandle) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVoicemailRingtoneUri(String callingPackage, PhoneAccountHandle phoneAccountHandle, Uri uri) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isVoicemailVibrationEnabled(PhoneAccountHandle accountHandle) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVoicemailVibrationEnabled(String callingPackage, PhoneAccountHandle phoneAccountHandle, boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<String> getPackagesWithCarrierPrivileges(int phoneId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<String> getPackagesWithCarrierPrivilegesForAllPhones() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getAidForAppType(int subId, int appType) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getEsn(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getCdmaPrlVersion(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<TelephonyHistogram> getTelephonyHistograms() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int setAllowedCarriers(CarrierRestrictionRules carrierRestrictionRules) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public CarrierRestrictionRules getAllowedCarriers() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getSubscriptionCarrierId(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getSubscriptionCarrierName(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getSubscriptionSpecificCarrierId(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getSubscriptionSpecificCarrierName(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCarrierIdFromMccMnc(int slotIndex, String mccmnc, boolean isSubscriptionMccMnc) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void carrierActionSetMeteredApnsEnabled(int subId, boolean visible) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void carrierActionSetRadioEnabled(int subId, boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void carrierActionReportDefaultNetworkStatus(int subId, boolean report) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void carrierActionResetAll(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public NetworkStats getVtDataUsage(int subId, boolean perUidStats) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setPolicyDataEnabled(boolean enabled, int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<ClientRequestStats> getClientRequestStats(String callingPackage, int subid) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setDataEnabledProperties(String appName, boolean enable) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setSimPowerStateForSlot(int slotIndex, int state) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public String[] getForbiddenPlmns(int subId, int appType, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean getEmergencyCallbackMode(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public SignalStrength getSignalStrength(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCardIdForDefaultEuicc(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<UiccCardInfo> getUiccCardsInfo(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public UiccSlotInfo[] getUiccSlotsInfo() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean switchSlots(int[] physicalSlots) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setRadioIndicationUpdateMode(int subId, int filters, int mode) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isDataRoamingEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setDataRoamingEnabled(int subId, boolean isEnabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCdmaRoamingMode(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setCdmaRoamingMode(int subId, int mode) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setCdmaSubscriptionMode(int subId, int mode) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setCarrierTestOverride(int subId, String mccmnc, String imsi, String iccid, String gid1, String gid2, String plmn, String spn, String carrierPrivilegeRules, String apn) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getCarrierIdListVersion(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void refreshUiccProfile(int subId) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getNumberOfModemsWithSimultaneousDataConnections(int subId, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getNetworkSelectionMode(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isInEmergencySmsMode() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String[] getSmsApps(int userId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getDefaultSmsApp(int userId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setDefaultSmsApp(int userId, String packageName) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getRadioPowerState(int slotIndex, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void registerImsRegistrationCallback(int subId, IImsRegistrationCallback c) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void unregisterImsRegistrationCallback(int subId, IImsRegistrationCallback c) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void registerMmTelCapabilityCallback(int subId, IImsCapabilityCallback c) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void unregisterMmTelCapabilityCallback(int subId, IImsCapabilityCallback c) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isCapable(int subId, int capability, int regTech) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isAvailable(int subId, int capability, int regTech) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isAdvancedCallingSettingEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setAdvancedCallingSettingEnabled(int subId, boolean isEnabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isVtSettingEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVtSettingEnabled(int subId, boolean isEnabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isVoWiFiSettingEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVoWiFiSettingEnabled(int subId, boolean isEnabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isVoWiFiRoamingSettingEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVoWiFiRoamingSettingEnabled(int subId, boolean isEnabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVoWiFiNonPersistent(int subId, boolean isCapable, int mode) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getVoWiFiModeSetting(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVoWiFiModeSetting(int subId, int mode) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getVoWiFiRoamingModeSetting(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setVoWiFiRoamingModeSetting(int subId, int mode) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setRttCapabilitySetting(int subId, boolean isEnabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isTtyOverVolteEnabled(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public Map getEmergencyNumberList(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isEmergencyNumber(String number, boolean exactMatch) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<String> getCertsFromCarrierPrivilegeAccessRules(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void registerImsProvisioningChangedCallback(int subId, IImsConfigCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void unregisterImsProvisioningChangedCallback(int subId, IImsConfigCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setImsProvisioningStatusForCapability(int subId, int capability, int tech, boolean isProvisioned) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean getImsProvisioningStatusForCapability(int subId, int capability, int tech) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isMmTelCapabilityProvisionedInCache(int subId, int capability, int tech) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void cacheMmTelCapabilityProvisioning(int subId, int capability, int tech, boolean isProvisioned) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getImsProvisioningInt(int subId, int key) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getImsProvisioningString(int subId, int key) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int setImsProvisioningInt(int subId, int key, int value) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int setImsProvisioningString(int subId, int key, String value) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void updateEmergencyNumberListTestMode(int action, EmergencyNumber num) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public List<String> getEmergencyNumberListTestMode() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean enableModemForSlot(int slotIndex, boolean enable) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void setMultiSimCarrierRestriction(boolean isMultiSimCarrierRestricted) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public int isMultiSimSupported(String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void switchMultiSimConfig(int numOfSims) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean doesSwitchMultiSimConfigTriggerReboot(int subId, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int[] getSlotsMapping() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public int getRadioHalVersion() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isModemEnabledForSlot(int slotIndex, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isDataEnabledForApn(int apnType, int subId, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isApnMetered(int apnType, int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public void enqueueSmsPickResult(String callingPackage, IIntegerConsumer subIdResult) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getMmsUserAgent(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public String getMmsUAProfUrl(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean setDataAllowedDuringVoiceCall(int subId, boolean allow) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isDataAllowedInVoiceCall(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ITelephony
        public boolean isEmergencyNumberForSubId(int subId, String number, boolean exactMatch) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITelephony {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ITelephony";
        static final int TRANSACTION_cacheMmTelCapabilityProvisioning = 232;
        static final int TRANSACTION_call = 2;
        static final int TRANSACTION_canChangeDtmfToneLength = 133;
        static final int TRANSACTION_carrierActionReportDefaultNetworkStatus = 173;
        static final int TRANSACTION_carrierActionResetAll = 174;
        static final int TRANSACTION_carrierActionSetMeteredApnsEnabled = 171;
        static final int TRANSACTION_carrierActionSetRadioEnabled = 172;
        static final int TRANSACTION_checkCarrierPrivilegesForPackage = 117;
        static final int TRANSACTION_checkCarrierPrivilegesForPackageAnyPhone = 118;
        static final int TRANSACTION_dial = 1;
        static final int TRANSACTION_disableDataConnectivity = 28;
        static final int TRANSACTION_disableIms = 92;
        static final int TRANSACTION_disableLocationUpdates = 25;
        static final int TRANSACTION_disableLocationUpdatesForSubscriber = 26;
        static final int TRANSACTION_disableVisualVoicemailSmsFilter = 56;
        static final int TRANSACTION_doesSwitchMultiSimConfigTriggerReboot = 243;
        static final int TRANSACTION_enableDataConnectivity = 27;
        static final int TRANSACTION_enableIms = 91;
        static final int TRANSACTION_enableLocationUpdates = 23;
        static final int TRANSACTION_enableLocationUpdatesForSubscriber = 24;
        static final int TRANSACTION_enableModemForSlot = 239;
        static final int TRANSACTION_enableVideoCalling = 131;
        static final int TRANSACTION_enableVisualVoicemailSmsFilter = 55;
        static final int TRANSACTION_enqueueSmsPickResult = 249;
        static final int TRANSACTION_factoryReset = 150;
        static final int TRANSACTION_getActivePhoneType = 37;
        static final int TRANSACTION_getActivePhoneTypeForSlot = 38;
        static final int TRANSACTION_getActiveVisualVoicemailSmsFilterSettings = 58;
        static final int TRANSACTION_getAidForAppType = 160;
        static final int TRANSACTION_getAllCellInfo = 69;
        static final int TRANSACTION_getAllowedCarriers = 165;
        static final int TRANSACTION_getCalculatedPreferredNetworkType = 88;
        static final int TRANSACTION_getCallState = 33;
        static final int TRANSACTION_getCallStateForSlot = 34;
        static final int TRANSACTION_getCardIdForDefaultEuicc = 183;
        static final int TRANSACTION_getCarrierIdFromMccMnc = 170;
        static final int TRANSACTION_getCarrierIdListVersion = 194;
        static final int TRANSACTION_getCarrierPackageNamesForIntentAndPhone = 119;
        static final int TRANSACTION_getCarrierPrivilegeStatus = 115;
        static final int TRANSACTION_getCarrierPrivilegeStatusForUid = 116;
        static final int TRANSACTION_getCdmaEriIconIndex = 39;
        static final int TRANSACTION_getCdmaEriIconIndexForSubscriber = 40;
        static final int TRANSACTION_getCdmaEriIconMode = 41;
        static final int TRANSACTION_getCdmaEriIconModeForSubscriber = 42;
        static final int TRANSACTION_getCdmaEriText = 43;
        static final int TRANSACTION_getCdmaEriTextForSubscriber = 44;
        static final int TRANSACTION_getCdmaMdn = 112;
        static final int TRANSACTION_getCdmaMin = 113;
        static final int TRANSACTION_getCdmaPrlVersion = 162;
        static final int TRANSACTION_getCdmaRoamingMode = 190;
        static final int TRANSACTION_getCellLocation = 30;
        static final int TRANSACTION_getCellNetworkScanResults = 100;
        static final int TRANSACTION_getCertsFromCarrierPrivilegeAccessRules = 226;
        static final int TRANSACTION_getClientRequestStats = 177;
        static final int TRANSACTION_getDataActivationState = 50;
        static final int TRANSACTION_getDataActivity = 35;
        static final int TRANSACTION_getDataEnabled = 106;
        static final int TRANSACTION_getDataNetworkType = 62;
        static final int TRANSACTION_getDataNetworkTypeForSubscriber = 63;
        static final int TRANSACTION_getDataState = 36;
        static final int TRANSACTION_getDefaultSmsApp = 200;
        static final int TRANSACTION_getDeviceId = 142;
        static final int TRANSACTION_getDeviceSoftwareVersionForSlot = 147;
        static final int TRANSACTION_getEmergencyCallbackMode = 181;
        static final int TRANSACTION_getEmergencyNumberList = 224;
        static final int TRANSACTION_getEmergencyNumberListTestMode = 238;
        static final int TRANSACTION_getEsn = 161;
        static final int TRANSACTION_getForbiddenPlmns = 180;
        static final int TRANSACTION_getImeiForSlot = 143;
        static final int TRANSACTION_getImsConfig = 96;
        static final int TRANSACTION_getImsProvisioningInt = 233;
        static final int TRANSACTION_getImsProvisioningStatusForCapability = 230;
        static final int TRANSACTION_getImsProvisioningString = 234;
        static final int TRANSACTION_getImsRegTechnologyForMmTel = 141;
        static final int TRANSACTION_getImsRegistration = 95;
        static final int TRANSACTION_getImsService = 98;
        static final int TRANSACTION_getLine1AlphaTagForDisplay = 122;
        static final int TRANSACTION_getLine1NumberForDisplay = 121;
        static final int TRANSACTION_getLteOnCdmaMode = 67;
        static final int TRANSACTION_getLteOnCdmaModeForSubscriber = 68;
        static final int TRANSACTION_getManufacturerCodeForSlot = 146;
        static final int TRANSACTION_getMeidForSlot = 145;
        static final int TRANSACTION_getMergedSubscriberIds = 123;
        static final int TRANSACTION_getMmTelFeatureAndListen = 93;
        static final int TRANSACTION_getMmsUAProfUrl = 251;
        static final int TRANSACTION_getMmsUserAgent = 250;
        static final int TRANSACTION_getNeighboringCellInfo = 32;
        static final int TRANSACTION_getNetworkCountryIsoForPhone = 31;
        static final int TRANSACTION_getNetworkSelectionMode = 197;
        static final int TRANSACTION_getNetworkTypeForSubscriber = 61;
        static final int TRANSACTION_getNumberOfModemsWithSimultaneousDataConnections = 196;
        static final int TRANSACTION_getPackagesWithCarrierPrivileges = 158;
        static final int TRANSACTION_getPackagesWithCarrierPrivilegesForAllPhones = 159;
        static final int TRANSACTION_getPcscfAddress = 110;
        static final int TRANSACTION_getPhoneAccountHandleForSubscriptionId = 149;
        static final int TRANSACTION_getPreferredNetworkType = 89;
        static final int TRANSACTION_getRadioAccessFamily = 130;
        static final int TRANSACTION_getRadioHalVersion = 245;
        static final int TRANSACTION_getRadioPowerState = 202;
        static final int TRANSACTION_getRcsFeatureAndListen = 94;
        static final int TRANSACTION_getServiceStateForSubscriber = 153;
        static final int TRANSACTION_getSignalStrength = 182;
        static final int TRANSACTION_getSimLocaleForSubscriber = 151;
        static final int TRANSACTION_getSlotsMapping = 244;
        static final int TRANSACTION_getSmsApps = 199;
        static final int TRANSACTION_getSubIdForPhoneAccount = 148;
        static final int TRANSACTION_getSubscriptionCarrierId = 166;
        static final int TRANSACTION_getSubscriptionCarrierName = 167;
        static final int TRANSACTION_getSubscriptionSpecificCarrierId = 168;
        static final int TRANSACTION_getSubscriptionSpecificCarrierName = 169;
        static final int TRANSACTION_getTelephonyHistograms = 163;
        static final int TRANSACTION_getTetherApnRequiredForSubscriber = 90;
        static final int TRANSACTION_getTypeAllocationCodeForSlot = 144;
        static final int TRANSACTION_getUiccCardsInfo = 184;
        static final int TRANSACTION_getUiccSlotsInfo = 185;
        static final int TRANSACTION_getVisualVoicemailPackageName = 54;
        static final int TRANSACTION_getVisualVoicemailSettings = 53;
        static final int TRANSACTION_getVisualVoicemailSmsFilterSettings = 57;
        static final int TRANSACTION_getVoWiFiModeSetting = 218;
        static final int TRANSACTION_getVoWiFiRoamingModeSetting = 220;
        static final int TRANSACTION_getVoiceActivationState = 49;
        static final int TRANSACTION_getVoiceMessageCountForSubscriber = 51;
        static final int TRANSACTION_getVoiceNetworkTypeForSubscriber = 64;
        static final int TRANSACTION_getVoicemailRingtoneUri = 154;
        static final int TRANSACTION_getVtDataUsage = 175;
        static final int TRANSACTION_handlePinMmi = 13;
        static final int TRANSACTION_handlePinMmiForSubscriber = 15;
        static final int TRANSACTION_handleUssdRequest = 14;
        static final int TRANSACTION_hasIccCard = 65;
        static final int TRANSACTION_hasIccCardUsingSlotIndex = 66;
        static final int TRANSACTION_iccCloseLogicalChannel = 76;
        static final int TRANSACTION_iccCloseLogicalChannelBySlot = 75;
        static final int TRANSACTION_iccExchangeSimIO = 81;
        static final int TRANSACTION_iccOpenLogicalChannel = 74;
        static final int TRANSACTION_iccOpenLogicalChannelBySlot = 73;
        static final int TRANSACTION_iccTransmitApduBasicChannel = 80;
        static final int TRANSACTION_iccTransmitApduBasicChannelBySlot = 79;
        static final int TRANSACTION_iccTransmitApduLogicalChannel = 78;
        static final int TRANSACTION_iccTransmitApduLogicalChannelBySlot = 77;
        static final int TRANSACTION_invokeOemRilRequestRaw = 126;
        static final int TRANSACTION_isAdvancedCallingSettingEnabled = 209;
        static final int TRANSACTION_isApnMetered = 248;
        static final int TRANSACTION_isAvailable = 208;
        static final int TRANSACTION_isCapable = 207;
        static final int TRANSACTION_isConcurrentVoiceAndDataAllowed = 52;
        static final int TRANSACTION_isDataAllowedInVoiceCall = 253;
        static final int TRANSACTION_isDataConnectivityPossible = 29;
        static final int TRANSACTION_isDataEnabled = 108;
        static final int TRANSACTION_isDataEnabledForApn = 247;
        static final int TRANSACTION_isDataRoamingEnabled = 188;
        static final int TRANSACTION_isEmergencyNumber = 225;
        static final int TRANSACTION_isEmergencyNumberForSubId = 254;
        static final int TRANSACTION_isHearingAidCompatibilitySupported = 137;
        static final int TRANSACTION_isImsRegistered = 138;
        static final int TRANSACTION_isInEmergencySmsMode = 198;
        static final int TRANSACTION_isManualNetworkSelectionAllowed = 109;
        static final int TRANSACTION_isMmTelCapabilityProvisionedInCache = 231;
        static final int TRANSACTION_isModemEnabledForSlot = 246;
        static final int TRANSACTION_isMultiSimSupported = 241;
        static final int TRANSACTION_isRadioOn = 3;
        static final int TRANSACTION_isRadioOnForSubscriber = 4;
        static final int TRANSACTION_isRttSupported = 136;
        static final int TRANSACTION_isTtyModeSupported = 135;
        static final int TRANSACTION_isTtyOverVolteEnabled = 223;
        static final int TRANSACTION_isUserDataEnabled = 107;
        static final int TRANSACTION_isVideoCallingEnabled = 132;
        static final int TRANSACTION_isVideoTelephonyAvailable = 140;
        static final int TRANSACTION_isVoWiFiRoamingSettingEnabled = 215;
        static final int TRANSACTION_isVoWiFiSettingEnabled = 213;
        static final int TRANSACTION_isVoicemailVibrationEnabled = 156;
        static final int TRANSACTION_isVtSettingEnabled = 211;
        static final int TRANSACTION_isWifiCallingAvailable = 139;
        static final int TRANSACTION_isWorldPhone = 134;
        static final int TRANSACTION_needMobileRadioShutdown = 127;
        static final int TRANSACTION_needsOtaServiceProvisioning = 45;
        static final int TRANSACTION_nvReadItem = 83;
        static final int TRANSACTION_nvWriteCdmaPrl = 85;
        static final int TRANSACTION_nvWriteItem = 84;
        static final int TRANSACTION_rebootModem = 87;
        static final int TRANSACTION_refreshUiccProfile = 195;
        static final int TRANSACTION_registerImsProvisioningChangedCallback = 227;
        static final int TRANSACTION_registerImsRegistrationCallback = 203;
        static final int TRANSACTION_registerMmTelCapabilityCallback = 205;
        static final int TRANSACTION_requestCellInfoUpdate = 70;
        static final int TRANSACTION_requestCellInfoUpdateWithWorkSource = 71;
        static final int TRANSACTION_requestModemActivityInfo = 152;
        static final int TRANSACTION_requestNetworkScan = 101;
        static final int TRANSACTION_requestNumberVerification = 114;
        static final int TRANSACTION_resetModemConfig = 86;
        static final int TRANSACTION_sendDialerSpecialCode = 60;
        static final int TRANSACTION_sendEnvelopeWithStatus = 82;
        static final int TRANSACTION_sendVisualVoicemailSmsForSubscriber = 59;
        static final int TRANSACTION_setAdvancedCallingSettingEnabled = 210;
        static final int TRANSACTION_setAllowedCarriers = 164;
        static final int TRANSACTION_setCarrierTestOverride = 193;
        static final int TRANSACTION_setCdmaRoamingMode = 191;
        static final int TRANSACTION_setCdmaSubscriptionMode = 192;
        static final int TRANSACTION_setCellInfoListRate = 72;
        static final int TRANSACTION_setDataActivationState = 48;
        static final int TRANSACTION_setDataAllowedDuringVoiceCall = 252;
        static final int TRANSACTION_setDataEnabledProperties = 178;
        static final int TRANSACTION_setDataRoamingEnabled = 189;
        static final int TRANSACTION_setDefaultSmsApp = 201;
        static final int TRANSACTION_setImsProvisioningInt = 235;
        static final int TRANSACTION_setImsProvisioningStatusForCapability = 229;
        static final int TRANSACTION_setImsProvisioningString = 236;
        static final int TRANSACTION_setImsRegistrationState = 111;
        static final int TRANSACTION_setImsService = 97;
        static final int TRANSACTION_setLine1NumberForDisplayForSubscriber = 120;
        static final int TRANSACTION_setMultiSimCarrierRestriction = 240;
        static final int TRANSACTION_setNetworkSelectionModeAutomatic = 99;
        static final int TRANSACTION_setNetworkSelectionModeManual = 103;
        static final int TRANSACTION_setOperatorBrandOverride = 124;
        static final int TRANSACTION_setPolicyDataEnabled = 176;
        static final int TRANSACTION_setPreferredNetworkType = 104;
        static final int TRANSACTION_setRadio = 18;
        static final int TRANSACTION_setRadioCapability = 129;
        static final int TRANSACTION_setRadioForSubscriber = 19;
        static final int TRANSACTION_setRadioIndicationUpdateMode = 187;
        static final int TRANSACTION_setRadioPower = 20;
        static final int TRANSACTION_setRoamingOverride = 125;
        static final int TRANSACTION_setRttCapabilitySetting = 222;
        static final int TRANSACTION_setSimPowerStateForSlot = 179;
        static final int TRANSACTION_setUserDataEnabled = 105;
        static final int TRANSACTION_setVoWiFiModeSetting = 219;
        static final int TRANSACTION_setVoWiFiNonPersistent = 217;
        static final int TRANSACTION_setVoWiFiRoamingModeSetting = 221;
        static final int TRANSACTION_setVoWiFiRoamingSettingEnabled = 216;
        static final int TRANSACTION_setVoWiFiSettingEnabled = 214;
        static final int TRANSACTION_setVoiceActivationState = 47;
        static final int TRANSACTION_setVoiceMailNumber = 46;
        static final int TRANSACTION_setVoicemailRingtoneUri = 155;
        static final int TRANSACTION_setVoicemailVibrationEnabled = 157;
        static final int TRANSACTION_setVtSettingEnabled = 212;
        static final int TRANSACTION_shutdownMobileRadios = 128;
        static final int TRANSACTION_stopNetworkScan = 102;
        static final int TRANSACTION_supplyPin = 5;
        static final int TRANSACTION_supplyPinForSubscriber = 6;
        static final int TRANSACTION_supplyPinReportResult = 9;
        static final int TRANSACTION_supplyPinReportResultForSubscriber = 10;
        static final int TRANSACTION_supplyPuk = 7;
        static final int TRANSACTION_supplyPukForSubscriber = 8;
        static final int TRANSACTION_supplyPukReportResult = 11;
        static final int TRANSACTION_supplyPukReportResultForSubscriber = 12;
        static final int TRANSACTION_switchMultiSimConfig = 242;
        static final int TRANSACTION_switchSlots = 186;
        static final int TRANSACTION_toggleRadioOnOff = 16;
        static final int TRANSACTION_toggleRadioOnOffForSubscriber = 17;
        static final int TRANSACTION_unregisterImsProvisioningChangedCallback = 228;
        static final int TRANSACTION_unregisterImsRegistrationCallback = 204;
        static final int TRANSACTION_unregisterMmTelCapabilityCallback = 206;
        static final int TRANSACTION_updateEmergencyNumberListTestMode = 237;
        static final int TRANSACTION_updateServiceLocation = 21;
        static final int TRANSACTION_updateServiceLocationForSubscriber = 22;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITelephony asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITelephony)) {
                return new Proxy(obj);
            }
            return (ITelephony) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "dial";
                case 2:
                    return "call";
                case 3:
                    return "isRadioOn";
                case 4:
                    return "isRadioOnForSubscriber";
                case 5:
                    return "supplyPin";
                case 6:
                    return "supplyPinForSubscriber";
                case 7:
                    return "supplyPuk";
                case 8:
                    return "supplyPukForSubscriber";
                case 9:
                    return "supplyPinReportResult";
                case 10:
                    return "supplyPinReportResultForSubscriber";
                case 11:
                    return "supplyPukReportResult";
                case 12:
                    return "supplyPukReportResultForSubscriber";
                case 13:
                    return "handlePinMmi";
                case 14:
                    return "handleUssdRequest";
                case 15:
                    return "handlePinMmiForSubscriber";
                case 16:
                    return "toggleRadioOnOff";
                case 17:
                    return "toggleRadioOnOffForSubscriber";
                case 18:
                    return "setRadio";
                case 19:
                    return "setRadioForSubscriber";
                case 20:
                    return "setRadioPower";
                case 21:
                    return "updateServiceLocation";
                case 22:
                    return "updateServiceLocationForSubscriber";
                case 23:
                    return "enableLocationUpdates";
                case 24:
                    return "enableLocationUpdatesForSubscriber";
                case 25:
                    return "disableLocationUpdates";
                case 26:
                    return "disableLocationUpdatesForSubscriber";
                case 27:
                    return "enableDataConnectivity";
                case 28:
                    return "disableDataConnectivity";
                case 29:
                    return "isDataConnectivityPossible";
                case 30:
                    return "getCellLocation";
                case 31:
                    return "getNetworkCountryIsoForPhone";
                case 32:
                    return "getNeighboringCellInfo";
                case 33:
                    return "getCallState";
                case 34:
                    return "getCallStateForSlot";
                case 35:
                    return "getDataActivity";
                case 36:
                    return "getDataState";
                case 37:
                    return "getActivePhoneType";
                case 38:
                    return "getActivePhoneTypeForSlot";
                case 39:
                    return "getCdmaEriIconIndex";
                case 40:
                    return "getCdmaEriIconIndexForSubscriber";
                case 41:
                    return "getCdmaEriIconMode";
                case 42:
                    return "getCdmaEriIconModeForSubscriber";
                case 43:
                    return "getCdmaEriText";
                case 44:
                    return "getCdmaEriTextForSubscriber";
                case 45:
                    return "needsOtaServiceProvisioning";
                case 46:
                    return "setVoiceMailNumber";
                case 47:
                    return "setVoiceActivationState";
                case 48:
                    return "setDataActivationState";
                case 49:
                    return "getVoiceActivationState";
                case 50:
                    return "getDataActivationState";
                case 51:
                    return "getVoiceMessageCountForSubscriber";
                case 52:
                    return "isConcurrentVoiceAndDataAllowed";
                case 53:
                    return "getVisualVoicemailSettings";
                case 54:
                    return "getVisualVoicemailPackageName";
                case 55:
                    return "enableVisualVoicemailSmsFilter";
                case 56:
                    return "disableVisualVoicemailSmsFilter";
                case 57:
                    return "getVisualVoicemailSmsFilterSettings";
                case 58:
                    return "getActiveVisualVoicemailSmsFilterSettings";
                case 59:
                    return "sendVisualVoicemailSmsForSubscriber";
                case 60:
                    return "sendDialerSpecialCode";
                case 61:
                    return "getNetworkTypeForSubscriber";
                case 62:
                    return "getDataNetworkType";
                case 63:
                    return "getDataNetworkTypeForSubscriber";
                case 64:
                    return "getVoiceNetworkTypeForSubscriber";
                case 65:
                    return "hasIccCard";
                case 66:
                    return "hasIccCardUsingSlotIndex";
                case 67:
                    return "getLteOnCdmaMode";
                case 68:
                    return "getLteOnCdmaModeForSubscriber";
                case 69:
                    return "getAllCellInfo";
                case 70:
                    return "requestCellInfoUpdate";
                case 71:
                    return "requestCellInfoUpdateWithWorkSource";
                case 72:
                    return "setCellInfoListRate";
                case 73:
                    return "iccOpenLogicalChannelBySlot";
                case 74:
                    return "iccOpenLogicalChannel";
                case 75:
                    return "iccCloseLogicalChannelBySlot";
                case 76:
                    return "iccCloseLogicalChannel";
                case 77:
                    return "iccTransmitApduLogicalChannelBySlot";
                case 78:
                    return "iccTransmitApduLogicalChannel";
                case 79:
                    return "iccTransmitApduBasicChannelBySlot";
                case 80:
                    return "iccTransmitApduBasicChannel";
                case 81:
                    return "iccExchangeSimIO";
                case 82:
                    return "sendEnvelopeWithStatus";
                case 83:
                    return "nvReadItem";
                case 84:
                    return "nvWriteItem";
                case 85:
                    return "nvWriteCdmaPrl";
                case 86:
                    return "resetModemConfig";
                case 87:
                    return "rebootModem";
                case 88:
                    return "getCalculatedPreferredNetworkType";
                case 89:
                    return "getPreferredNetworkType";
                case 90:
                    return "getTetherApnRequiredForSubscriber";
                case 91:
                    return "enableIms";
                case 92:
                    return "disableIms";
                case 93:
                    return "getMmTelFeatureAndListen";
                case 94:
                    return "getRcsFeatureAndListen";
                case 95:
                    return "getImsRegistration";
                case 96:
                    return "getImsConfig";
                case 97:
                    return "setImsService";
                case 98:
                    return "getImsService";
                case 99:
                    return "setNetworkSelectionModeAutomatic";
                case 100:
                    return "getCellNetworkScanResults";
                case 101:
                    return "requestNetworkScan";
                case 102:
                    return "stopNetworkScan";
                case 103:
                    return "setNetworkSelectionModeManual";
                case 104:
                    return "setPreferredNetworkType";
                case 105:
                    return "setUserDataEnabled";
                case 106:
                    return "getDataEnabled";
                case 107:
                    return "isUserDataEnabled";
                case 108:
                    return "isDataEnabled";
                case 109:
                    return "isManualNetworkSelectionAllowed";
                case 110:
                    return "getPcscfAddress";
                case 111:
                    return "setImsRegistrationState";
                case 112:
                    return "getCdmaMdn";
                case 113:
                    return "getCdmaMin";
                case 114:
                    return "requestNumberVerification";
                case 115:
                    return "getCarrierPrivilegeStatus";
                case 116:
                    return "getCarrierPrivilegeStatusForUid";
                case 117:
                    return "checkCarrierPrivilegesForPackage";
                case 118:
                    return "checkCarrierPrivilegesForPackageAnyPhone";
                case 119:
                    return "getCarrierPackageNamesForIntentAndPhone";
                case 120:
                    return "setLine1NumberForDisplayForSubscriber";
                case 121:
                    return "getLine1NumberForDisplay";
                case 122:
                    return "getLine1AlphaTagForDisplay";
                case 123:
                    return "getMergedSubscriberIds";
                case 124:
                    return "setOperatorBrandOverride";
                case 125:
                    return "setRoamingOverride";
                case 126:
                    return "invokeOemRilRequestRaw";
                case 127:
                    return "needMobileRadioShutdown";
                case 128:
                    return "shutdownMobileRadios";
                case 129:
                    return "setRadioCapability";
                case 130:
                    return "getRadioAccessFamily";
                case 131:
                    return "enableVideoCalling";
                case 132:
                    return "isVideoCallingEnabled";
                case 133:
                    return "canChangeDtmfToneLength";
                case 134:
                    return "isWorldPhone";
                case 135:
                    return "isTtyModeSupported";
                case 136:
                    return "isRttSupported";
                case 137:
                    return "isHearingAidCompatibilitySupported";
                case 138:
                    return "isImsRegistered";
                case 139:
                    return "isWifiCallingAvailable";
                case 140:
                    return "isVideoTelephonyAvailable";
                case 141:
                    return "getImsRegTechnologyForMmTel";
                case 142:
                    return "getDeviceId";
                case 143:
                    return "getImeiForSlot";
                case 144:
                    return "getTypeAllocationCodeForSlot";
                case 145:
                    return "getMeidForSlot";
                case 146:
                    return "getManufacturerCodeForSlot";
                case 147:
                    return "getDeviceSoftwareVersionForSlot";
                case 148:
                    return "getSubIdForPhoneAccount";
                case 149:
                    return "getPhoneAccountHandleForSubscriptionId";
                case 150:
                    return "factoryReset";
                case 151:
                    return "getSimLocaleForSubscriber";
                case 152:
                    return "requestModemActivityInfo";
                case 153:
                    return "getServiceStateForSubscriber";
                case 154:
                    return "getVoicemailRingtoneUri";
                case 155:
                    return "setVoicemailRingtoneUri";
                case 156:
                    return "isVoicemailVibrationEnabled";
                case 157:
                    return "setVoicemailVibrationEnabled";
                case 158:
                    return "getPackagesWithCarrierPrivileges";
                case 159:
                    return "getPackagesWithCarrierPrivilegesForAllPhones";
                case 160:
                    return "getAidForAppType";
                case 161:
                    return "getEsn";
                case 162:
                    return "getCdmaPrlVersion";
                case 163:
                    return "getTelephonyHistograms";
                case 164:
                    return "setAllowedCarriers";
                case 165:
                    return "getAllowedCarriers";
                case 166:
                    return "getSubscriptionCarrierId";
                case 167:
                    return "getSubscriptionCarrierName";
                case 168:
                    return "getSubscriptionSpecificCarrierId";
                case 169:
                    return "getSubscriptionSpecificCarrierName";
                case 170:
                    return "getCarrierIdFromMccMnc";
                case 171:
                    return "carrierActionSetMeteredApnsEnabled";
                case 172:
                    return "carrierActionSetRadioEnabled";
                case 173:
                    return "carrierActionReportDefaultNetworkStatus";
                case 174:
                    return "carrierActionResetAll";
                case 175:
                    return "getVtDataUsage";
                case 176:
                    return "setPolicyDataEnabled";
                case 177:
                    return "getClientRequestStats";
                case 178:
                    return "setDataEnabledProperties";
                case 179:
                    return "setSimPowerStateForSlot";
                case 180:
                    return "getForbiddenPlmns";
                case 181:
                    return "getEmergencyCallbackMode";
                case 182:
                    return "getSignalStrength";
                case 183:
                    return "getCardIdForDefaultEuicc";
                case 184:
                    return "getUiccCardsInfo";
                case 185:
                    return "getUiccSlotsInfo";
                case 186:
                    return "switchSlots";
                case 187:
                    return "setRadioIndicationUpdateMode";
                case 188:
                    return "isDataRoamingEnabled";
                case 189:
                    return "setDataRoamingEnabled";
                case 190:
                    return "getCdmaRoamingMode";
                case 191:
                    return "setCdmaRoamingMode";
                case 192:
                    return "setCdmaSubscriptionMode";
                case 193:
                    return "setCarrierTestOverride";
                case 194:
                    return "getCarrierIdListVersion";
                case 195:
                    return "refreshUiccProfile";
                case 196:
                    return "getNumberOfModemsWithSimultaneousDataConnections";
                case 197:
                    return "getNetworkSelectionMode";
                case 198:
                    return "isInEmergencySmsMode";
                case 199:
                    return "getSmsApps";
                case 200:
                    return "getDefaultSmsApp";
                case 201:
                    return "setDefaultSmsApp";
                case 202:
                    return "getRadioPowerState";
                case 203:
                    return "registerImsRegistrationCallback";
                case 204:
                    return "unregisterImsRegistrationCallback";
                case 205:
                    return "registerMmTelCapabilityCallback";
                case 206:
                    return "unregisterMmTelCapabilityCallback";
                case 207:
                    return "isCapable";
                case 208:
                    return "isAvailable";
                case 209:
                    return "isAdvancedCallingSettingEnabled";
                case 210:
                    return "setAdvancedCallingSettingEnabled";
                case 211:
                    return "isVtSettingEnabled";
                case 212:
                    return "setVtSettingEnabled";
                case 213:
                    return "isVoWiFiSettingEnabled";
                case 214:
                    return "setVoWiFiSettingEnabled";
                case 215:
                    return "isVoWiFiRoamingSettingEnabled";
                case 216:
                    return "setVoWiFiRoamingSettingEnabled";
                case 217:
                    return "setVoWiFiNonPersistent";
                case 218:
                    return "getVoWiFiModeSetting";
                case 219:
                    return "setVoWiFiModeSetting";
                case 220:
                    return "getVoWiFiRoamingModeSetting";
                case 221:
                    return "setVoWiFiRoamingModeSetting";
                case 222:
                    return "setRttCapabilitySetting";
                case 223:
                    return "isTtyOverVolteEnabled";
                case 224:
                    return "getEmergencyNumberList";
                case 225:
                    return "isEmergencyNumber";
                case 226:
                    return "getCertsFromCarrierPrivilegeAccessRules";
                case 227:
                    return "registerImsProvisioningChangedCallback";
                case 228:
                    return "unregisterImsProvisioningChangedCallback";
                case 229:
                    return "setImsProvisioningStatusForCapability";
                case 230:
                    return "getImsProvisioningStatusForCapability";
                case 231:
                    return "isMmTelCapabilityProvisionedInCache";
                case 232:
                    return "cacheMmTelCapabilityProvisioning";
                case 233:
                    return "getImsProvisioningInt";
                case 234:
                    return "getImsProvisioningString";
                case 235:
                    return "setImsProvisioningInt";
                case 236:
                    return "setImsProvisioningString";
                case 237:
                    return "updateEmergencyNumberListTestMode";
                case 238:
                    return "getEmergencyNumberListTestMode";
                case 239:
                    return "enableModemForSlot";
                case 240:
                    return "setMultiSimCarrierRestriction";
                case 241:
                    return "isMultiSimSupported";
                case 242:
                    return "switchMultiSimConfig";
                case 243:
                    return "doesSwitchMultiSimConfigTriggerReboot";
                case 244:
                    return "getSlotsMapping";
                case 245:
                    return "getRadioHalVersion";
                case 246:
                    return "isModemEnabledForSlot";
                case 247:
                    return "isDataEnabledForApn";
                case 248:
                    return "isApnMetered";
                case 249:
                    return "enqueueSmsPickResult";
                case 250:
                    return "getMmsUserAgent";
                case 251:
                    return "getMmsUAProfUrl";
                case 252:
                    return "setDataAllowedDuringVoiceCall";
                case 253:
                    return "isDataAllowedInVoiceCall";
                case 254:
                    return "isEmergencyNumberForSubId";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ResultReceiver _arg2;
            VisualVoicemailSmsFilterSettings _arg22;
            PendingIntent _arg5;
            WorkSource _arg3;
            NetworkScanRequest _arg1;
            Messenger _arg23;
            OperatorInfo _arg12;
            PhoneNumberRange _arg0;
            Intent _arg02;
            byte[] _arg13;
            PhoneAccount _arg03;
            ResultReceiver _arg04;
            PhoneAccountHandle _arg05;
            PhoneAccountHandle _arg14;
            Uri _arg24;
            PhoneAccountHandle _arg06;
            PhoneAccountHandle _arg15;
            CarrierRestrictionRules _arg07;
            EmergencyNumber _arg16;
            if (code != 1598968902) {
                IBinder iBinder = null;
                IBinder iBinder2 = null;
                IBinder iBinder3 = null;
                IBinder iBinder4 = null;
                boolean _arg08 = false;
                boolean _arg25 = false;
                boolean _arg17 = false;
                boolean _arg09 = false;
                boolean _arg18 = false;
                boolean _arg32 = false;
                boolean _arg33 = false;
                boolean _arg19 = false;
                boolean _arg110 = false;
                boolean _arg111 = false;
                boolean _arg112 = false;
                boolean _arg113 = false;
                boolean _arg114 = false;
                boolean _arg115 = false;
                boolean _arg116 = false;
                boolean _arg117 = false;
                boolean _arg010 = false;
                boolean _arg118 = false;
                boolean _arg119 = false;
                boolean _arg120 = false;
                boolean _arg26 = false;
                boolean _arg27 = false;
                boolean _arg011 = false;
                boolean _arg012 = false;
                boolean _arg121 = false;
                boolean _arg28 = false;
                boolean _arg122 = false;
                boolean _arg123 = false;
                boolean _arg013 = false;
                boolean _arg124 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        dial(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        call(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRadioOn = isRadioOn(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isRadioOn ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRadioOnForSubscriber = isRadioOnForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isRadioOnForSubscriber ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supplyPin = supplyPin(data.readString());
                        reply.writeNoException();
                        reply.writeInt(supplyPin ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supplyPinForSubscriber = supplyPinForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(supplyPinForSubscriber ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supplyPuk = supplyPuk(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(supplyPuk ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supplyPukForSubscriber = supplyPukForSubscriber(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(supplyPukForSubscriber ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result = supplyPinReportResult(data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result2 = supplyPinReportResultForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result3 = supplyPukReportResult(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result3);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result4 = supplyPukReportResultForSubscriber(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result4);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean handlePinMmi = handlePinMmi(data.readString());
                        reply.writeNoException();
                        reply.writeInt(handlePinMmi ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg014 = data.readInt();
                        String _arg125 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        handleUssdRequest(_arg014, _arg125, _arg2);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean handlePinMmiForSubscriber = handlePinMmiForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(handlePinMmiForSubscriber ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        toggleRadioOnOff();
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        toggleRadioOnOffForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        boolean radio = setRadio(_arg08);
                        reply.writeNoException();
                        reply.writeInt(radio ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg015 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg124 = true;
                        }
                        boolean radioForSubscriber = setRadioForSubscriber(_arg015, _arg124);
                        reply.writeNoException();
                        reply.writeInt(radioForSubscriber ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = true;
                        }
                        boolean radioPower = setRadioPower(_arg013);
                        reply.writeNoException();
                        reply.writeInt(radioPower ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        updateServiceLocation();
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        updateServiceLocationForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        enableLocationUpdates();
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        enableLocationUpdatesForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        disableLocationUpdates();
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        disableLocationUpdatesForSubscriber(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableDataConnectivity = enableDataConnectivity();
                        reply.writeNoException();
                        reply.writeInt(enableDataConnectivity ? 1 : 0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableDataConnectivity = disableDataConnectivity();
                        reply.writeNoException();
                        reply.writeInt(disableDataConnectivity ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDataConnectivityPossible = isDataConnectivityPossible(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDataConnectivityPossible ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result5 = getCellLocation(data.readString());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getNetworkCountryIsoForPhone(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        List<NeighboringCellInfo> _result7 = getNeighboringCellInfo(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result7);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getCallState();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getCallStateForSlot(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getDataActivity();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getDataState();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getActivePhoneType();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getActivePhoneTypeForSlot(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getCdmaEriIconIndex(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getCdmaEriIconIndexForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = getCdmaEriIconMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = getCdmaEriIconModeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = getCdmaEriText(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        String _result19 = getCdmaEriTextForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result19);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        boolean needsOtaServiceProvisioning = needsOtaServiceProvisioning();
                        reply.writeNoException();
                        reply.writeInt(needsOtaServiceProvisioning ? 1 : 0);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        boolean voiceMailNumber = setVoiceMailNumber(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(voiceMailNumber ? 1 : 0);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        setVoiceActivationState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        setDataActivationState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = getVoiceActivationState(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = getDataActivationState(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = getVoiceMessageCountForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isConcurrentVoiceAndDataAllowed = isConcurrentVoiceAndDataAllowed(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isConcurrentVoiceAndDataAllowed ? 1 : 0);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result23 = getVisualVoicemailSettings(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result23 != null) {
                            reply.writeInt(1);
                            _result23.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        String _result24 = getVisualVoicemailPackageName(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result24);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        int _arg126 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = VisualVoicemailSmsFilterSettings.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        enableVisualVoicemailSmsFilter(_arg016, _arg126, _arg22);
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        disableVisualVoicemailSmsFilter(data.readString(), data.readInt());
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        VisualVoicemailSmsFilterSettings _result25 = getVisualVoicemailSmsFilterSettings(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result25 != null) {
                            reply.writeInt(1);
                            _result25.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        VisualVoicemailSmsFilterSettings _result26 = getActiveVisualVoicemailSmsFilterSettings(data.readInt());
                        reply.writeNoException();
                        if (_result26 != null) {
                            reply.writeInt(1);
                            _result26.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        int _arg127 = data.readInt();
                        String _arg29 = data.readString();
                        int _arg34 = data.readInt();
                        String _arg4 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        sendVisualVoicemailSmsForSubscriber(_arg017, _arg127, _arg29, _arg34, _arg4, _arg5);
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        sendDialerSpecialCode(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        int _result27 = getNetworkTypeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = getDataNetworkType(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        int _result29 = getDataNetworkTypeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        int _result30 = getVoiceNetworkTypeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasIccCard = hasIccCard();
                        reply.writeNoException();
                        reply.writeInt(hasIccCard ? 1 : 0);
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasIccCardUsingSlotIndex = hasIccCardUsingSlotIndex(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasIccCardUsingSlotIndex ? 1 : 0);
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = getLteOnCdmaMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = getLteOnCdmaModeForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        List<CellInfo> _result33 = getAllCellInfo(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result33);
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        requestCellInfoUpdate(data.readInt(), ICellInfoCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg018 = data.readInt();
                        ICellInfoCallback _arg128 = ICellInfoCallback.Stub.asInterface(data.readStrongBinder());
                        String _arg210 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        requestCellInfoUpdateWithWorkSource(_arg018, _arg128, _arg210, _arg3);
                        reply.writeNoException();
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        setCellInfoListRate(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        IccOpenLogicalChannelResponse _result34 = iccOpenLogicalChannelBySlot(data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result34 != null) {
                            reply.writeInt(1);
                            _result34.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        IccOpenLogicalChannelResponse _result35 = iccOpenLogicalChannel(data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result35 != null) {
                            reply.writeInt(1);
                            _result35.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        boolean iccCloseLogicalChannelBySlot = iccCloseLogicalChannelBySlot(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(iccCloseLogicalChannelBySlot ? 1 : 0);
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        boolean iccCloseLogicalChannel = iccCloseLogicalChannel(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(iccCloseLogicalChannel ? 1 : 0);
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        String _result36 = iccTransmitApduLogicalChannelBySlot(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result36);
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        String _result37 = iccTransmitApduLogicalChannel(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result37);
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        String _result38 = iccTransmitApduBasicChannelBySlot(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result38);
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        String _result39 = iccTransmitApduBasicChannel(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result39);
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result40 = iccExchangeSimIO(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result40);
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        String _result41 = sendEnvelopeWithStatus(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result41);
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        String _result42 = nvReadItem(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result42);
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        boolean nvWriteItem = nvWriteItem(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(nvWriteItem ? 1 : 0);
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        boolean nvWriteCdmaPrl = nvWriteCdmaPrl(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(nvWriteCdmaPrl ? 1 : 0);
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        boolean resetModemConfig = resetModemConfig(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(resetModemConfig ? 1 : 0);
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        boolean rebootModem = rebootModem(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(rebootModem ? 1 : 0);
                        return true;
                    case 88:
                        data.enforceInterface(DESCRIPTOR);
                        int _result43 = getCalculatedPreferredNetworkType(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result43);
                        return true;
                    case 89:
                        data.enforceInterface(DESCRIPTOR);
                        int _result44 = getPreferredNetworkType(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result44);
                        return true;
                    case 90:
                        data.enforceInterface(DESCRIPTOR);
                        boolean tetherApnRequiredForSubscriber = getTetherApnRequiredForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(tetherApnRequiredForSubscriber ? 1 : 0);
                        return true;
                    case 91:
                        data.enforceInterface(DESCRIPTOR);
                        enableIms(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 92:
                        data.enforceInterface(DESCRIPTOR);
                        disableIms(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 93:
                        data.enforceInterface(DESCRIPTOR);
                        IImsMmTelFeature _result45 = getMmTelFeatureAndListen(data.readInt(), IImsServiceFeatureCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result45 != null) {
                            iBinder = _result45.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 94:
                        data.enforceInterface(DESCRIPTOR);
                        IImsRcsFeature _result46 = getRcsFeatureAndListen(data.readInt(), IImsServiceFeatureCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result46 != null) {
                            iBinder4 = _result46.asBinder();
                        }
                        reply.writeStrongBinder(iBinder4);
                        return true;
                    case 95:
                        data.enforceInterface(DESCRIPTOR);
                        IImsRegistration _result47 = getImsRegistration(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result47 != null) {
                            iBinder3 = _result47.asBinder();
                        }
                        reply.writeStrongBinder(iBinder3);
                        return true;
                    case 96:
                        data.enforceInterface(DESCRIPTOR);
                        IImsConfig _result48 = getImsConfig(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result48 != null) {
                            iBinder2 = _result48.asBinder();
                        }
                        reply.writeStrongBinder(iBinder2);
                        return true;
                    case 97:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg019 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg123 = true;
                        }
                        boolean imsService = setImsService(_arg019, _arg123, data.readString());
                        reply.writeNoException();
                        reply.writeInt(imsService ? 1 : 0);
                        return true;
                    case 98:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg020 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg122 = true;
                        }
                        String _result49 = getImsService(_arg020, _arg122);
                        reply.writeNoException();
                        reply.writeString(_result49);
                        return true;
                    case 99:
                        data.enforceInterface(DESCRIPTOR);
                        setNetworkSelectionModeAutomatic(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        CellNetworkScanResult _result50 = getCellNetworkScanResults(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result50 != null) {
                            reply.writeInt(1);
                            _result50.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg021 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = NetworkScanRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _result51 = requestNetworkScan(_arg021, _arg1, _arg23, data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result51);
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        stopNetworkScan(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg022 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = OperatorInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg28 = true;
                        }
                        boolean networkSelectionModeManual = setNetworkSelectionModeManual(_arg022, _arg12, _arg28);
                        reply.writeNoException();
                        reply.writeInt(networkSelectionModeManual ? 1 : 0);
                        return true;
                    case 104:
                        data.enforceInterface(DESCRIPTOR);
                        boolean preferredNetworkType = setPreferredNetworkType(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(preferredNetworkType ? 1 : 0);
                        return true;
                    case 105:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg023 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg121 = true;
                        }
                        setUserDataEnabled(_arg023, _arg121);
                        reply.writeNoException();
                        return true;
                    case 106:
                        data.enforceInterface(DESCRIPTOR);
                        boolean dataEnabled = getDataEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(dataEnabled ? 1 : 0);
                        return true;
                    case 107:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUserDataEnabled = isUserDataEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUserDataEnabled ? 1 : 0);
                        return true;
                    case 108:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDataEnabled = isDataEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDataEnabled ? 1 : 0);
                        return true;
                    case 109:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isManualNetworkSelectionAllowed = isManualNetworkSelectionAllowed(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isManualNetworkSelectionAllowed ? 1 : 0);
                        return true;
                    case 110:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result52 = getPcscfAddress(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeStringArray(_result52);
                        return true;
                    case 111:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = true;
                        }
                        setImsRegistrationState(_arg012);
                        reply.writeNoException();
                        return true;
                    case 112:
                        data.enforceInterface(DESCRIPTOR);
                        String _result53 = getCdmaMdn(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result53);
                        return true;
                    case 113:
                        data.enforceInterface(DESCRIPTOR);
                        String _result54 = getCdmaMin(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result54);
                        return true;
                    case 114:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PhoneNumberRange.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        requestNumberVerification(_arg0, data.readLong(), INumberVerificationCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 115:
                        data.enforceInterface(DESCRIPTOR);
                        int _result55 = getCarrierPrivilegeStatus(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result55);
                        return true;
                    case 116:
                        data.enforceInterface(DESCRIPTOR);
                        int _result56 = getCarrierPrivilegeStatusForUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result56);
                        return true;
                    case 117:
                        data.enforceInterface(DESCRIPTOR);
                        int _result57 = checkCarrierPrivilegesForPackage(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result57);
                        return true;
                    case 118:
                        data.enforceInterface(DESCRIPTOR);
                        int _result58 = checkCarrierPrivilegesForPackageAnyPhone(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result58);
                        return true;
                    case 119:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        List<String> _result59 = getCarrierPackageNamesForIntentAndPhone(_arg02, data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result59);
                        return true;
                    case 120:
                        data.enforceInterface(DESCRIPTOR);
                        boolean line1NumberForDisplayForSubscriber = setLine1NumberForDisplayForSubscriber(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(line1NumberForDisplayForSubscriber ? 1 : 0);
                        return true;
                    case 121:
                        data.enforceInterface(DESCRIPTOR);
                        String _result60 = getLine1NumberForDisplay(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result60);
                        return true;
                    case 122:
                        data.enforceInterface(DESCRIPTOR);
                        String _result61 = getLine1AlphaTagForDisplay(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result61);
                        return true;
                    case 123:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result62 = getMergedSubscriberIds(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeStringArray(_result62);
                        return true;
                    case 124:
                        data.enforceInterface(DESCRIPTOR);
                        boolean operatorBrandOverride = setOperatorBrandOverride(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(operatorBrandOverride ? 1 : 0);
                        return true;
                    case 125:
                        data.enforceInterface(DESCRIPTOR);
                        boolean roamingOverride = setRoamingOverride(data.readInt(), data.createStringArrayList(), data.createStringArrayList(), data.createStringArrayList(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(roamingOverride ? 1 : 0);
                        return true;
                    case 126:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _arg024 = data.createByteArray();
                        int _arg1_length = data.readInt();
                        if (_arg1_length < 0) {
                            _arg13 = null;
                        } else {
                            _arg13 = new byte[_arg1_length];
                        }
                        int _result63 = invokeOemRilRequestRaw(_arg024, _arg13);
                        reply.writeNoException();
                        reply.writeInt(_result63);
                        reply.writeByteArray(_arg13);
                        return true;
                    case 127:
                        data.enforceInterface(DESCRIPTOR);
                        boolean needMobileRadioShutdown = needMobileRadioShutdown();
                        reply.writeNoException();
                        reply.writeInt(needMobileRadioShutdown ? 1 : 0);
                        return true;
                    case 128:
                        data.enforceInterface(DESCRIPTOR);
                        shutdownMobileRadios();
                        reply.writeNoException();
                        return true;
                    case 129:
                        data.enforceInterface(DESCRIPTOR);
                        setRadioCapability((RadioAccessFamily[]) data.createTypedArray(RadioAccessFamily.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 130:
                        data.enforceInterface(DESCRIPTOR);
                        int _result64 = getRadioAccessFamily(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result64);
                        return true;
                    case 131:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = true;
                        }
                        enableVideoCalling(_arg011);
                        reply.writeNoException();
                        return true;
                    case 132:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVideoCallingEnabled = isVideoCallingEnabled(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isVideoCallingEnabled ? 1 : 0);
                        return true;
                    case 133:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canChangeDtmfToneLength = canChangeDtmfToneLength(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(canChangeDtmfToneLength ? 1 : 0);
                        return true;
                    case 134:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWorldPhone = isWorldPhone(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isWorldPhone ? 1 : 0);
                        return true;
                    case 135:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTtyModeSupported = isTtyModeSupported();
                        reply.writeNoException();
                        reply.writeInt(isTtyModeSupported ? 1 : 0);
                        return true;
                    case 136:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRttSupported = isRttSupported(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isRttSupported ? 1 : 0);
                        return true;
                    case 137:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHearingAidCompatibilitySupported = isHearingAidCompatibilitySupported();
                        reply.writeNoException();
                        reply.writeInt(isHearingAidCompatibilitySupported ? 1 : 0);
                        return true;
                    case 138:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isImsRegistered = isImsRegistered(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isImsRegistered ? 1 : 0);
                        return true;
                    case 139:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isWifiCallingAvailable = isWifiCallingAvailable(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isWifiCallingAvailable ? 1 : 0);
                        return true;
                    case 140:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVideoTelephonyAvailable = isVideoTelephonyAvailable(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVideoTelephonyAvailable ? 1 : 0);
                        return true;
                    case 141:
                        data.enforceInterface(DESCRIPTOR);
                        int _result65 = getImsRegTechnologyForMmTel(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result65);
                        return true;
                    case 142:
                        data.enforceInterface(DESCRIPTOR);
                        String _result66 = getDeviceId(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result66);
                        return true;
                    case 143:
                        data.enforceInterface(DESCRIPTOR);
                        String _result67 = getImeiForSlot(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result67);
                        return true;
                    case 144:
                        data.enforceInterface(DESCRIPTOR);
                        String _result68 = getTypeAllocationCodeForSlot(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result68);
                        return true;
                    case 145:
                        data.enforceInterface(DESCRIPTOR);
                        String _result69 = getMeidForSlot(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result69);
                        return true;
                    case 146:
                        data.enforceInterface(DESCRIPTOR);
                        String _result70 = getManufacturerCodeForSlot(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result70);
                        return true;
                    case 147:
                        data.enforceInterface(DESCRIPTOR);
                        String _result71 = getDeviceSoftwareVersionForSlot(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result71);
                        return true;
                    case 148:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = PhoneAccount.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        int _result72 = getSubIdForPhoneAccount(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result72);
                        return true;
                    case 149:
                        data.enforceInterface(DESCRIPTOR);
                        PhoneAccountHandle _result73 = getPhoneAccountHandleForSubscriptionId(data.readInt());
                        reply.writeNoException();
                        if (_result73 != null) {
                            reply.writeInt(1);
                            _result73.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 150:
                        data.enforceInterface(DESCRIPTOR);
                        factoryReset(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 151:
                        data.enforceInterface(DESCRIPTOR);
                        String _result74 = getSimLocaleForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result74);
                        return true;
                    case 152:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ResultReceiver.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        requestModemActivityInfo(_arg04);
                        return true;
                    case 153:
                        data.enforceInterface(DESCRIPTOR);
                        ServiceState _result75 = getServiceStateForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result75 != null) {
                            reply.writeInt(1);
                            _result75.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 154:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        Uri _result76 = getVoicemailRingtoneUri(_arg05);
                        reply.writeNoException();
                        if (_result76 != null) {
                            reply.writeInt(1);
                            _result76.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 155:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg025 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg24 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        setVoicemailRingtoneUri(_arg025, _arg14, _arg24);
                        reply.writeNoException();
                        return true;
                    case 156:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        boolean isVoicemailVibrationEnabled = isVoicemailVibrationEnabled(_arg06);
                        reply.writeNoException();
                        reply.writeInt(isVoicemailVibrationEnabled ? 1 : 0);
                        return true;
                    case 157:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg026 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg27 = true;
                        }
                        setVoicemailVibrationEnabled(_arg026, _arg15, _arg27);
                        reply.writeNoException();
                        return true;
                    case 158:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result77 = getPackagesWithCarrierPrivileges(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result77);
                        return true;
                    case 159:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result78 = getPackagesWithCarrierPrivilegesForAllPhones();
                        reply.writeNoException();
                        reply.writeStringList(_result78);
                        return true;
                    case 160:
                        data.enforceInterface(DESCRIPTOR);
                        String _result79 = getAidForAppType(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result79);
                        return true;
                    case 161:
                        data.enforceInterface(DESCRIPTOR);
                        String _result80 = getEsn(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result80);
                        return true;
                    case 162:
                        data.enforceInterface(DESCRIPTOR);
                        String _result81 = getCdmaPrlVersion(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result81);
                        return true;
                    case 163:
                        data.enforceInterface(DESCRIPTOR);
                        List<TelephonyHistogram> _result82 = getTelephonyHistograms();
                        reply.writeNoException();
                        reply.writeTypedList(_result82);
                        return true;
                    case 164:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = CarrierRestrictionRules.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        int _result83 = setAllowedCarriers(_arg07);
                        reply.writeNoException();
                        reply.writeInt(_result83);
                        return true;
                    case 165:
                        data.enforceInterface(DESCRIPTOR);
                        CarrierRestrictionRules _result84 = getAllowedCarriers();
                        reply.writeNoException();
                        if (_result84 != null) {
                            reply.writeInt(1);
                            _result84.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 166:
                        data.enforceInterface(DESCRIPTOR);
                        int _result85 = getSubscriptionCarrierId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result85);
                        return true;
                    case 167:
                        data.enforceInterface(DESCRIPTOR);
                        String _result86 = getSubscriptionCarrierName(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result86);
                        return true;
                    case 168:
                        data.enforceInterface(DESCRIPTOR);
                        int _result87 = getSubscriptionSpecificCarrierId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result87);
                        return true;
                    case 169:
                        data.enforceInterface(DESCRIPTOR);
                        String _result88 = getSubscriptionSpecificCarrierName(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result88);
                        return true;
                    case 170:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg027 = data.readInt();
                        String _arg129 = data.readString();
                        if (data.readInt() != 0) {
                            _arg26 = true;
                        }
                        int _result89 = getCarrierIdFromMccMnc(_arg027, _arg129, _arg26);
                        reply.writeNoException();
                        reply.writeInt(_result89);
                        return true;
                    case 171:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg028 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg120 = true;
                        }
                        carrierActionSetMeteredApnsEnabled(_arg028, _arg120);
                        reply.writeNoException();
                        return true;
                    case 172:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg029 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg119 = true;
                        }
                        carrierActionSetRadioEnabled(_arg029, _arg119);
                        reply.writeNoException();
                        return true;
                    case 173:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg030 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg118 = true;
                        }
                        carrierActionReportDefaultNetworkStatus(_arg030, _arg118);
                        reply.writeNoException();
                        return true;
                    case 174:
                        data.enforceInterface(DESCRIPTOR);
                        carrierActionResetAll(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 175:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkStats _result90 = getVtDataUsage(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result90 != null) {
                            reply.writeInt(1);
                            _result90.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 176:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        setPolicyDataEnabled(_arg010, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 177:
                        data.enforceInterface(DESCRIPTOR);
                        List<ClientRequestStats> _result91 = getClientRequestStats(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result91);
                        return true;
                    case 178:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg031 = data.readString();
                        if (data.readInt() != 0) {
                            _arg117 = true;
                        }
                        setDataEnabledProperties(_arg031, _arg117);
                        reply.writeNoException();
                        return true;
                    case 179:
                        data.enforceInterface(DESCRIPTOR);
                        setSimPowerStateForSlot(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 180:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result92 = getForbiddenPlmns(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeStringArray(_result92);
                        return true;
                    case 181:
                        data.enforceInterface(DESCRIPTOR);
                        boolean emergencyCallbackMode = getEmergencyCallbackMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(emergencyCallbackMode ? 1 : 0);
                        return true;
                    case 182:
                        data.enforceInterface(DESCRIPTOR);
                        SignalStrength _result93 = getSignalStrength(data.readInt());
                        reply.writeNoException();
                        if (_result93 != null) {
                            reply.writeInt(1);
                            _result93.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 183:
                        data.enforceInterface(DESCRIPTOR);
                        int _result94 = getCardIdForDefaultEuicc(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result94);
                        return true;
                    case 184:
                        data.enforceInterface(DESCRIPTOR);
                        List<UiccCardInfo> _result95 = getUiccCardsInfo(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result95);
                        return true;
                    case 185:
                        data.enforceInterface(DESCRIPTOR);
                        UiccSlotInfo[] _result96 = getUiccSlotsInfo();
                        reply.writeNoException();
                        reply.writeTypedArray(_result96, 1);
                        return true;
                    case 186:
                        data.enforceInterface(DESCRIPTOR);
                        boolean switchSlots = switchSlots(data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(switchSlots ? 1 : 0);
                        return true;
                    case 187:
                        data.enforceInterface(DESCRIPTOR);
                        setRadioIndicationUpdateMode(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 188:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDataRoamingEnabled = isDataRoamingEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDataRoamingEnabled ? 1 : 0);
                        return true;
                    case 189:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg032 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg116 = true;
                        }
                        setDataRoamingEnabled(_arg032, _arg116);
                        reply.writeNoException();
                        return true;
                    case 190:
                        data.enforceInterface(DESCRIPTOR);
                        int _result97 = getCdmaRoamingMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result97);
                        return true;
                    case 191:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cdmaRoamingMode = setCdmaRoamingMode(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(cdmaRoamingMode ? 1 : 0);
                        return true;
                    case 192:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cdmaSubscriptionMode = setCdmaSubscriptionMode(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(cdmaSubscriptionMode ? 1 : 0);
                        return true;
                    case 193:
                        data.enforceInterface(DESCRIPTOR);
                        setCarrierTestOverride(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 194:
                        data.enforceInterface(DESCRIPTOR);
                        int _result98 = getCarrierIdListVersion(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result98);
                        return true;
                    case 195:
                        data.enforceInterface(DESCRIPTOR);
                        refreshUiccProfile(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 196:
                        data.enforceInterface(DESCRIPTOR);
                        int _result99 = getNumberOfModemsWithSimultaneousDataConnections(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result99);
                        return true;
                    case 197:
                        data.enforceInterface(DESCRIPTOR);
                        int _result100 = getNetworkSelectionMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result100);
                        return true;
                    case 198:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInEmergencySmsMode = isInEmergencySmsMode();
                        reply.writeNoException();
                        reply.writeInt(isInEmergencySmsMode ? 1 : 0);
                        return true;
                    case 199:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result101 = getSmsApps(data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result101);
                        return true;
                    case 200:
                        data.enforceInterface(DESCRIPTOR);
                        String _result102 = getDefaultSmsApp(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result102);
                        return true;
                    case 201:
                        data.enforceInterface(DESCRIPTOR);
                        setDefaultSmsApp(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 202:
                        data.enforceInterface(DESCRIPTOR);
                        int _result103 = getRadioPowerState(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result103);
                        return true;
                    case 203:
                        data.enforceInterface(DESCRIPTOR);
                        registerImsRegistrationCallback(data.readInt(), IImsRegistrationCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 204:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterImsRegistrationCallback(data.readInt(), IImsRegistrationCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 205:
                        data.enforceInterface(DESCRIPTOR);
                        registerMmTelCapabilityCallback(data.readInt(), IImsCapabilityCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 206:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterMmTelCapabilityCallback(data.readInt(), IImsCapabilityCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 207:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isCapable = isCapable(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isCapable ? 1 : 0);
                        return true;
                    case 208:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAvailable = isAvailable(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAvailable ? 1 : 0);
                        return true;
                    case 209:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAdvancedCallingSettingEnabled = isAdvancedCallingSettingEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAdvancedCallingSettingEnabled ? 1 : 0);
                        return true;
                    case 210:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg033 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg115 = true;
                        }
                        setAdvancedCallingSettingEnabled(_arg033, _arg115);
                        reply.writeNoException();
                        return true;
                    case 211:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVtSettingEnabled = isVtSettingEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVtSettingEnabled ? 1 : 0);
                        return true;
                    case 212:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg034 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg114 = true;
                        }
                        setVtSettingEnabled(_arg034, _arg114);
                        reply.writeNoException();
                        return true;
                    case 213:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVoWiFiSettingEnabled = isVoWiFiSettingEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVoWiFiSettingEnabled ? 1 : 0);
                        return true;
                    case 214:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg035 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg113 = true;
                        }
                        setVoWiFiSettingEnabled(_arg035, _arg113);
                        reply.writeNoException();
                        return true;
                    case 215:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVoWiFiRoamingSettingEnabled = isVoWiFiRoamingSettingEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVoWiFiRoamingSettingEnabled ? 1 : 0);
                        return true;
                    case 216:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg036 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg112 = true;
                        }
                        setVoWiFiRoamingSettingEnabled(_arg036, _arg112);
                        reply.writeNoException();
                        return true;
                    case 217:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg037 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg111 = true;
                        }
                        setVoWiFiNonPersistent(_arg037, _arg111, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 218:
                        data.enforceInterface(DESCRIPTOR);
                        int _result104 = getVoWiFiModeSetting(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result104);
                        return true;
                    case 219:
                        data.enforceInterface(DESCRIPTOR);
                        setVoWiFiModeSetting(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 220:
                        data.enforceInterface(DESCRIPTOR);
                        int _result105 = getVoWiFiRoamingModeSetting(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result105);
                        return true;
                    case 221:
                        data.enforceInterface(DESCRIPTOR);
                        setVoWiFiRoamingModeSetting(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 222:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg038 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg110 = true;
                        }
                        setRttCapabilitySetting(_arg038, _arg110);
                        reply.writeNoException();
                        return true;
                    case 223:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTtyOverVolteEnabled = isTtyOverVolteEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isTtyOverVolteEnabled ? 1 : 0);
                        return true;
                    case 224:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result106 = getEmergencyNumberList(data.readString());
                        reply.writeNoException();
                        reply.writeMap(_result106);
                        return true;
                    case 225:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg039 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = true;
                        }
                        boolean isEmergencyNumber = isEmergencyNumber(_arg039, _arg19);
                        reply.writeNoException();
                        reply.writeInt(isEmergencyNumber ? 1 : 0);
                        return true;
                    case 226:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result107 = getCertsFromCarrierPrivilegeAccessRules(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result107);
                        return true;
                    case 227:
                        data.enforceInterface(DESCRIPTOR);
                        registerImsProvisioningChangedCallback(data.readInt(), IImsConfigCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 228:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterImsProvisioningChangedCallback(data.readInt(), IImsConfigCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 229:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg040 = data.readInt();
                        int _arg130 = data.readInt();
                        int _arg211 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg33 = true;
                        }
                        setImsProvisioningStatusForCapability(_arg040, _arg130, _arg211, _arg33);
                        reply.writeNoException();
                        return true;
                    case 230:
                        data.enforceInterface(DESCRIPTOR);
                        boolean imsProvisioningStatusForCapability = getImsProvisioningStatusForCapability(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(imsProvisioningStatusForCapability ? 1 : 0);
                        return true;
                    case 231:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isMmTelCapabilityProvisionedInCache = isMmTelCapabilityProvisionedInCache(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isMmTelCapabilityProvisionedInCache ? 1 : 0);
                        return true;
                    case 232:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg041 = data.readInt();
                        int _arg131 = data.readInt();
                        int _arg212 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = true;
                        }
                        cacheMmTelCapabilityProvisioning(_arg041, _arg131, _arg212, _arg32);
                        reply.writeNoException();
                        return true;
                    case 233:
                        data.enforceInterface(DESCRIPTOR);
                        int _result108 = getImsProvisioningInt(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result108);
                        return true;
                    case 234:
                        data.enforceInterface(DESCRIPTOR);
                        String _result109 = getImsProvisioningString(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result109);
                        return true;
                    case 235:
                        data.enforceInterface(DESCRIPTOR);
                        int _result110 = setImsProvisioningInt(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result110);
                        return true;
                    case 236:
                        data.enforceInterface(DESCRIPTOR);
                        int _result111 = setImsProvisioningString(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result111);
                        return true;
                    case 237:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg042 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg16 = EmergencyNumber.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        updateEmergencyNumberListTestMode(_arg042, _arg16);
                        reply.writeNoException();
                        return true;
                    case 238:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result112 = getEmergencyNumberListTestMode();
                        reply.writeNoException();
                        reply.writeStringList(_result112);
                        return true;
                    case 239:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg043 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg18 = true;
                        }
                        boolean enableModemForSlot = enableModemForSlot(_arg043, _arg18);
                        reply.writeNoException();
                        reply.writeInt(enableModemForSlot ? 1 : 0);
                        return true;
                    case 240:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = true;
                        }
                        setMultiSimCarrierRestriction(_arg09);
                        reply.writeNoException();
                        return true;
                    case 241:
                        data.enforceInterface(DESCRIPTOR);
                        int _result113 = isMultiSimSupported(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result113);
                        return true;
                    case 242:
                        data.enforceInterface(DESCRIPTOR);
                        switchMultiSimConfig(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 243:
                        data.enforceInterface(DESCRIPTOR);
                        boolean doesSwitchMultiSimConfigTriggerReboot = doesSwitchMultiSimConfigTriggerReboot(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(doesSwitchMultiSimConfigTriggerReboot ? 1 : 0);
                        return true;
                    case 244:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result114 = getSlotsMapping();
                        reply.writeNoException();
                        reply.writeIntArray(_result114);
                        return true;
                    case 245:
                        data.enforceInterface(DESCRIPTOR);
                        int _result115 = getRadioHalVersion();
                        reply.writeNoException();
                        reply.writeInt(_result115);
                        return true;
                    case 246:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isModemEnabledForSlot = isModemEnabledForSlot(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isModemEnabledForSlot ? 1 : 0);
                        return true;
                    case 247:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDataEnabledForApn = isDataEnabledForApn(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isDataEnabledForApn ? 1 : 0);
                        return true;
                    case 248:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isApnMetered = isApnMetered(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isApnMetered ? 1 : 0);
                        return true;
                    case 249:
                        data.enforceInterface(DESCRIPTOR);
                        enqueueSmsPickResult(data.readString(), IIntegerConsumer.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 250:
                        data.enforceInterface(DESCRIPTOR);
                        String _result116 = getMmsUserAgent(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result116);
                        return true;
                    case 251:
                        data.enforceInterface(DESCRIPTOR);
                        String _result117 = getMmsUAProfUrl(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result117);
                        return true;
                    case 252:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg044 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg17 = true;
                        }
                        boolean dataAllowedDuringVoiceCall = setDataAllowedDuringVoiceCall(_arg044, _arg17);
                        reply.writeNoException();
                        reply.writeInt(dataAllowedDuringVoiceCall ? 1 : 0);
                        return true;
                    case 253:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDataAllowedInVoiceCall = isDataAllowedInVoiceCall(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDataAllowedInVoiceCall ? 1 : 0);
                        return true;
                    case 254:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg045 = data.readInt();
                        String _arg132 = data.readString();
                        if (data.readInt() != 0) {
                            _arg25 = true;
                        }
                        boolean isEmergencyNumberForSubId = isEmergencyNumberForSubId(_arg045, _arg132, _arg25);
                        reply.writeNoException();
                        reply.writeInt(isEmergencyNumberForSubId ? 1 : 0);
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
        public static class Proxy implements ITelephony {
            public static ITelephony sDefaultImpl;
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

            @Override // com.android.internal.telephony.ITelephony
            public void dial(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dial(number);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void call(String callingPackage, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(number);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().call(callingPackage, number);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isRadioOn(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRadioOn(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isRadioOnForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRadioOnForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean supplyPin(String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pin);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyPin(pin);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean supplyPinForSubscriber(int subId, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(pin);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyPinForSubscriber(subId, pin);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean supplyPuk(String puk, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(puk);
                    _data.writeString(pin);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyPuk(puk, pin);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean supplyPukForSubscriber(int subId, String puk, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(puk);
                    _data.writeString(pin);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyPukForSubscriber(subId, puk, pin);
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

            @Override // com.android.internal.telephony.ITelephony
            public int[] supplyPinReportResult(String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pin);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyPinReportResult(pin);
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

            @Override // com.android.internal.telephony.ITelephony
            public int[] supplyPinReportResultForSubscriber(int subId, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(pin);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyPinReportResultForSubscriber(subId, pin);
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

            @Override // com.android.internal.telephony.ITelephony
            public int[] supplyPukReportResult(String puk, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(puk);
                    _data.writeString(pin);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyPukReportResult(puk, pin);
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

            @Override // com.android.internal.telephony.ITelephony
            public int[] supplyPukReportResultForSubscriber(int subId, String puk, String pin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(puk);
                    _data.writeString(pin);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supplyPukReportResultForSubscriber(subId, puk, pin);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean handlePinMmi(String dialString) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(dialString);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handlePinMmi(dialString);
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

            @Override // com.android.internal.telephony.ITelephony
            public void handleUssdRequest(int subId, String ussdRequest, ResultReceiver wrappedCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(ussdRequest);
                    if (wrappedCallback != null) {
                        _data.writeInt(1);
                        wrappedCallback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().handleUssdRequest(subId, ussdRequest, wrappedCallback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean handlePinMmiForSubscriber(int subId, String dialString) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(dialString);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handlePinMmiForSubscriber(subId, dialString);
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

            @Override // com.android.internal.telephony.ITelephony
            public void toggleRadioOnOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().toggleRadioOnOff();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void toggleRadioOnOffForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().toggleRadioOnOffForSubscriber(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean setRadio(boolean turnOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(turnOn ? 1 : 0);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setRadio(turnOn);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setRadioForSubscriber(int subId, boolean turnOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = true;
                    _data.writeInt(turnOn ? 1 : 0);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setRadioForSubscriber(subId, turnOn);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setRadioPower(boolean turnOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(turnOn ? 1 : 0);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setRadioPower(turnOn);
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

            @Override // com.android.internal.telephony.ITelephony
            public void updateServiceLocation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateServiceLocation();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void updateServiceLocationForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateServiceLocationForSubscriber(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void enableLocationUpdates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableLocationUpdates();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void enableLocationUpdatesForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableLocationUpdatesForSubscriber(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void disableLocationUpdates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableLocationUpdates();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void disableLocationUpdatesForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableLocationUpdatesForSubscriber(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean enableDataConnectivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableDataConnectivity();
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean disableDataConnectivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableDataConnectivity();
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isDataConnectivityPossible(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDataConnectivityPossible(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public Bundle getCellLocation(String callingPkg) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCellLocation(callingPkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getNetworkCountryIsoForPhone(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkCountryIsoForPhone(phoneId);
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

            @Override // com.android.internal.telephony.ITelephony
            public List<NeighboringCellInfo> getNeighboringCellInfo(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNeighboringCellInfo(callingPkg);
                    }
                    _reply.readException();
                    List<NeighboringCellInfo> _result = _reply.createTypedArrayList(NeighboringCellInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getCallState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCallState();
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCallStateForSlot(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCallStateForSlot(slotIndex);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getDataActivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataActivity();
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

            @Override // com.android.internal.telephony.ITelephony
            public int getDataState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataState();
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

            @Override // com.android.internal.telephony.ITelephony
            public int getActivePhoneType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivePhoneType();
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

            @Override // com.android.internal.telephony.ITelephony
            public int getActivePhoneTypeForSlot(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivePhoneTypeForSlot(slotIndex);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCdmaEriIconIndex(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaEriIconIndex(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCdmaEriIconIndexForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaEriIconIndexForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCdmaEriIconMode(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaEriIconMode(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCdmaEriIconModeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaEriIconModeForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getCdmaEriText(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaEriText(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getCdmaEriTextForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaEriTextForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean needsOtaServiceProvisioning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().needsOtaServiceProvisioning();
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setVoiceMailNumber(int subId, String alphaTag, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(alphaTag);
                    _data.writeString(number);
                    boolean _result = false;
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setVoiceMailNumber(subId, alphaTag, number);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setVoiceActivationState(int subId, int activationState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(activationState);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoiceActivationState(subId, activationState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void setDataActivationState(int subId, int activationState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(activationState);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDataActivationState(subId, activationState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getVoiceActivationState(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(49, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceActivationState(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getDataActivationState(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(50, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataActivationState(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getVoiceMessageCountForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceMessageCountForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isConcurrentVoiceAndDataAllowed(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isConcurrentVoiceAndDataAllowed(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public Bundle getVisualVoicemailSettings(String callingPackage, int subId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisualVoicemailSettings(callingPackage, subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getVisualVoicemailPackageName(String callingPackage, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(54, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisualVoicemailPackageName(callingPackage, subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void enableVisualVoicemailSmsFilter(String callingPackage, int subId, VisualVoicemailSmsFilterSettings settings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    if (settings != null) {
                        _data.writeInt(1);
                        settings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableVisualVoicemailSmsFilter(callingPackage, subId, settings);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void disableVisualVoicemailSmsFilter(String callingPackage, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(56, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().disableVisualVoicemailSmsFilter(callingPackage, subId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings(String callingPackage, int subId) throws RemoteException {
                VisualVoicemailSmsFilterSettings _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVisualVoicemailSmsFilterSettings(callingPackage, subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VisualVoicemailSmsFilterSettings.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public VisualVoicemailSmsFilterSettings getActiveVisualVoicemailSmsFilterSettings(int subId) throws RemoteException {
                VisualVoicemailSmsFilterSettings _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveVisualVoicemailSmsFilterSettings(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VisualVoicemailSmsFilterSettings.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public void sendVisualVoicemailSmsForSubscriber(String callingPackage, int subId, String number, int port, String text, PendingIntent sentIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPackage);
                        try {
                            _data.writeInt(subId);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(number);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(port);
                        try {
                            _data.writeString(text);
                            if (sentIntent != null) {
                                _data.writeInt(1);
                                sentIntent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendVisualVoicemailSmsForSubscriber(callingPackage, subId, number, port, text, sentIntent);
                            _reply.recycle();
                            _data.recycle();
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
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void sendDialerSpecialCode(String callingPackageName, String inputCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackageName);
                    _data.writeString(inputCode);
                    if (this.mRemote.transact(60, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendDialerSpecialCode(callingPackageName, inputCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(61, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkTypeForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getDataNetworkType(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(62, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataNetworkType(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getDataNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(63, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataNetworkTypeForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getVoiceNetworkTypeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(64, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceNetworkTypeForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean hasIccCard() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(65, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasIccCard();
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean hasIccCardUsingSlotIndex(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    boolean _result = false;
                    if (!this.mRemote.transact(66, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasIccCardUsingSlotIndex(slotIndex);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getLteOnCdmaMode(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(67, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLteOnCdmaMode(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getLteOnCdmaModeForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(68, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLteOnCdmaModeForSubscriber(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public List<CellInfo> getAllCellInfo(String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(69, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllCellInfo(callingPkg);
                    }
                    _reply.readException();
                    List<CellInfo> _result = _reply.createTypedArrayList(CellInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void requestCellInfoUpdate(int subId, ICellInfoCallback cb, String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeString(callingPkg);
                    if (this.mRemote.transact(70, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestCellInfoUpdate(subId, cb, callingPkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void requestCellInfoUpdateWithWorkSource(int subId, ICellInfoCallback cb, String callingPkg, WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeString(callingPkg);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestCellInfoUpdateWithWorkSource(subId, cb, callingPkg, ws);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void setCellInfoListRate(int rateInMillis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rateInMillis);
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCellInfoListRate(rateInMillis);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public IccOpenLogicalChannelResponse iccOpenLogicalChannelBySlot(int slotIndex, String callingPackage, String AID, int p2) throws RemoteException {
                IccOpenLogicalChannelResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    _data.writeString(AID);
                    _data.writeInt(p2);
                    if (!this.mRemote.transact(73, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().iccOpenLogicalChannelBySlot(slotIndex, callingPackage, AID, p2);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = IccOpenLogicalChannelResponse.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public IccOpenLogicalChannelResponse iccOpenLogicalChannel(int subId, String callingPackage, String AID, int p2) throws RemoteException {
                IccOpenLogicalChannelResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    _data.writeString(AID);
                    _data.writeInt(p2);
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().iccOpenLogicalChannel(subId, callingPackage, AID, p2);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = IccOpenLogicalChannelResponse.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean iccCloseLogicalChannelBySlot(int slotIndex, int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeInt(channel);
                    boolean _result = false;
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().iccCloseLogicalChannelBySlot(slotIndex, channel);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean iccCloseLogicalChannel(int subId, int channel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(channel);
                    boolean _result = false;
                    if (!this.mRemote.transact(76, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().iccCloseLogicalChannel(subId, channel);
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

            @Override // com.android.internal.telephony.ITelephony
            public String iccTransmitApduLogicalChannelBySlot(int slotIndex, int channel, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(slotIndex);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(channel);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(cla);
                        try {
                            _data.writeInt(instruction);
                            _data.writeInt(p1);
                            _data.writeInt(p2);
                            _data.writeInt(p3);
                            _data.writeString(data);
                            if (this.mRemote.transact(77, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                String _result = _reply.readString();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            String iccTransmitApduLogicalChannelBySlot = Stub.getDefaultImpl().iccTransmitApduLogicalChannelBySlot(slotIndex, channel, cla, instruction, p1, p2, p3, data);
                            _reply.recycle();
                            _data.recycle();
                            return iccTransmitApduLogicalChannelBySlot;
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
            }

            @Override // com.android.internal.telephony.ITelephony
            public String iccTransmitApduLogicalChannel(int subId, int channel, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(channel);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(cla);
                        try {
                            _data.writeInt(instruction);
                            _data.writeInt(p1);
                            _data.writeInt(p2);
                            _data.writeInt(p3);
                            _data.writeString(data);
                            if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                String _result = _reply.readString();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            String iccTransmitApduLogicalChannel = Stub.getDefaultImpl().iccTransmitApduLogicalChannel(subId, channel, cla, instruction, p1, p2, p3, data);
                            _reply.recycle();
                            _data.recycle();
                            return iccTransmitApduLogicalChannel;
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
            }

            @Override // com.android.internal.telephony.ITelephony
            public String iccTransmitApduBasicChannelBySlot(int slotIndex, String callingPackage, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(slotIndex);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPackage);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(cla);
                        try {
                            _data.writeInt(instruction);
                            _data.writeInt(p1);
                            _data.writeInt(p2);
                            _data.writeInt(p3);
                            _data.writeString(data);
                            if (this.mRemote.transact(79, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                String _result = _reply.readString();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            String iccTransmitApduBasicChannelBySlot = Stub.getDefaultImpl().iccTransmitApduBasicChannelBySlot(slotIndex, callingPackage, cla, instruction, p1, p2, p3, data);
                            _reply.recycle();
                            _data.recycle();
                            return iccTransmitApduBasicChannelBySlot;
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
            }

            @Override // com.android.internal.telephony.ITelephony
            public String iccTransmitApduBasicChannel(int subId, String callingPackage, int cla, int instruction, int p1, int p2, int p3, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPackage);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(cla);
                        try {
                            _data.writeInt(instruction);
                            _data.writeInt(p1);
                            _data.writeInt(p2);
                            _data.writeInt(p3);
                            _data.writeString(data);
                            if (this.mRemote.transact(80, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                String _result = _reply.readString();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            String iccTransmitApduBasicChannel = Stub.getDefaultImpl().iccTransmitApduBasicChannel(subId, callingPackage, cla, instruction, p1, p2, p3, data);
                            _reply.recycle();
                            _data.recycle();
                            return iccTransmitApduBasicChannel;
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
            }

            @Override // com.android.internal.telephony.ITelephony
            public byte[] iccExchangeSimIO(int subId, int fileID, int command, int p1, int p2, int p3, String filePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(fileID);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(command);
                        try {
                            _data.writeInt(p1);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(p2);
                            _data.writeInt(p3);
                            _data.writeString(filePath);
                            if (this.mRemote.transact(81, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                byte[] _result = _reply.createByteArray();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            byte[] iccExchangeSimIO = Stub.getDefaultImpl().iccExchangeSimIO(subId, fileID, command, p1, p2, p3, filePath);
                            _reply.recycle();
                            _data.recycle();
                            return iccExchangeSimIO;
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
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public String sendEnvelopeWithStatus(int subId, String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(content);
                    if (!this.mRemote.transact(82, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendEnvelopeWithStatus(subId, content);
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

            @Override // com.android.internal.telephony.ITelephony
            public String nvReadItem(int itemID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(itemID);
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().nvReadItem(itemID);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean nvWriteItem(int itemID, String itemValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(itemID);
                    _data.writeString(itemValue);
                    boolean _result = false;
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().nvWriteItem(itemID, itemValue);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean nvWriteCdmaPrl(byte[] preferredRoamingList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(preferredRoamingList);
                    boolean _result = false;
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().nvWriteCdmaPrl(preferredRoamingList);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean resetModemConfig(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    boolean _result = false;
                    if (!this.mRemote.transact(86, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resetModemConfig(slotIndex);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean rebootModem(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    boolean _result = false;
                    if (!this.mRemote.transact(87, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().rebootModem(slotIndex);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCalculatedPreferredNetworkType(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(88, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCalculatedPreferredNetworkType(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getPreferredNetworkType(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(89, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreferredNetworkType(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean getTetherApnRequiredForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(90, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTetherApnRequiredForSubscriber(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void enableIms(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (this.mRemote.transact(91, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableIms(slotId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void disableIms(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    if (this.mRemote.transact(92, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableIms(slotId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public IImsMmTelFeature getMmTelFeatureAndListen(int slotId, IImsServiceFeatureCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(93, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMmTelFeatureAndListen(slotId, callback);
                    }
                    _reply.readException();
                    IImsMmTelFeature _result = IImsMmTelFeature.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public IImsRcsFeature getRcsFeatureAndListen(int slotId, IImsServiceFeatureCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(94, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRcsFeatureAndListen(slotId, callback);
                    }
                    _reply.readException();
                    IImsRcsFeature _result = IImsRcsFeature.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public IImsRegistration getImsRegistration(int slotId, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(feature);
                    if (!this.mRemote.transact(95, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsRegistration(slotId, feature);
                    }
                    _reply.readException();
                    IImsRegistration _result = IImsRegistration.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public IImsConfig getImsConfig(int slotId, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(feature);
                    if (!this.mRemote.transact(96, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsConfig(slotId, feature);
                    }
                    _reply.readException();
                    IImsConfig _result = IImsConfig.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean setImsService(int slotId, boolean isCarrierImsService, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    boolean _result = true;
                    _data.writeInt(isCarrierImsService ? 1 : 0);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(97, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setImsService(slotId, isCarrierImsService, packageName);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getImsService(int slotId, boolean isCarrierImsService) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(isCarrierImsService ? 1 : 0);
                    if (!this.mRemote.transact(98, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsService(slotId, isCarrierImsService);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setNetworkSelectionModeAutomatic(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(99, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetworkSelectionModeAutomatic(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public CellNetworkScanResult getCellNetworkScanResults(int subId, String callingPackage) throws RemoteException {
                CellNetworkScanResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(100, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCellNetworkScanResults(subId, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CellNetworkScanResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public int requestNetworkScan(int subId, NetworkScanRequest request, Messenger messenger, IBinder binder, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(101, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestNetworkScan(subId, request, messenger, binder, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public void stopNetworkScan(int subId, int scanId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(scanId);
                    if (this.mRemote.transact(102, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopNetworkScan(subId, scanId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean setNetworkSelectionModeManual(int subId, OperatorInfo operatorInfo, boolean persisSelection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = true;
                    if (operatorInfo != null) {
                        _data.writeInt(1);
                        operatorInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(persisSelection ? 1 : 0);
                    if (!this.mRemote.transact(103, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setNetworkSelectionModeManual(subId, operatorInfo, persisSelection);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setPreferredNetworkType(int subId, int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(networkType);
                    boolean _result = false;
                    if (!this.mRemote.transact(104, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPreferredNetworkType(subId, networkType);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setUserDataEnabled(int subId, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(105, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserDataEnabled(subId, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean getDataEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(106, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isUserDataEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(107, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUserDataEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isDataEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(108, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDataEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isManualNetworkSelectionAllowed(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(109, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isManualNetworkSelectionAllowed(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public String[] getPcscfAddress(String apnType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apnType);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(110, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPcscfAddress(apnType, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setImsRegistrationState(boolean registered) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(registered ? 1 : 0);
                    if (this.mRemote.transact(111, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsRegistrationState(registered);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public String getCdmaMdn(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(112, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaMdn(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getCdmaMin(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(113, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaMin(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void requestNumberVerification(PhoneNumberRange range, long timeoutMillis, INumberVerificationCallback callback, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (range != null) {
                        _data.writeInt(1);
                        range.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(timeoutMillis);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(114, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestNumberVerification(range, timeoutMillis, callback, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getCarrierPrivilegeStatus(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(115, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierPrivilegeStatus(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCarrierPrivilegeStatusForUid(int subId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(116, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierPrivilegeStatusForUid(subId, uid);
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

            @Override // com.android.internal.telephony.ITelephony
            public int checkCarrierPrivilegesForPackage(int subId, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(117, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkCarrierPrivilegesForPackage(subId, pkgName);
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

            @Override // com.android.internal.telephony.ITelephony
            public int checkCarrierPrivilegesForPackageAnyPhone(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(118, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkCarrierPrivilegesForPackageAnyPhone(pkgName);
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

            @Override // com.android.internal.telephony.ITelephony
            public List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(119, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierPackageNamesForIntentAndPhone(intent, phoneId);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean setLine1NumberForDisplayForSubscriber(int subId, String alphaTag, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(alphaTag);
                    _data.writeString(number);
                    boolean _result = false;
                    if (!this.mRemote.transact(120, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setLine1NumberForDisplayForSubscriber(subId, alphaTag, number);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getLine1NumberForDisplay(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(121, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLine1NumberForDisplay(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getLine1AlphaTagForDisplay(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(122, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLine1AlphaTagForDisplay(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public String[] getMergedSubscriberIds(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(123, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMergedSubscriberIds(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setOperatorBrandOverride(int subId, String brand) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(brand);
                    boolean _result = false;
                    if (!this.mRemote.transact(124, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setOperatorBrandOverride(subId, brand);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setRoamingOverride(int subId, List<String> gsmRoamingList, List<String> gsmNonRoamingList, List<String> cdmaRoamingList, List<String> cdmaNonRoamingList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStringList(gsmRoamingList);
                        try {
                            _data.writeStringList(gsmNonRoamingList);
                            try {
                                _data.writeStringList(cdmaRoamingList);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
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
                        _data.writeStringList(cdmaNonRoamingList);
                        try {
                            boolean _result = false;
                            if (this.mRemote.transact(125, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean roamingOverride = Stub.getDefaultImpl().setRoamingOverride(subId, gsmRoamingList, gsmNonRoamingList, cdmaRoamingList, cdmaNonRoamingList);
                            _reply.recycle();
                            _data.recycle();
                            return roamingOverride;
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

            @Override // com.android.internal.telephony.ITelephony
            public int invokeOemRilRequestRaw(byte[] oemReq, byte[] oemResp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(oemReq);
                    if (oemResp == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(oemResp.length);
                    }
                    if (!this.mRemote.transact(126, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().invokeOemRilRequestRaw(oemReq, oemResp);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean needMobileRadioShutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(127, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().needMobileRadioShutdown();
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

            @Override // com.android.internal.telephony.ITelephony
            public void shutdownMobileRadios() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(128, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().shutdownMobileRadios();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void setRadioCapability(RadioAccessFamily[] rafs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(rafs, 0);
                    if (this.mRemote.transact(129, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRadioCapability(rafs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getRadioAccessFamily(int phoneId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(130, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRadioAccessFamily(phoneId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public void enableVideoCalling(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(131, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableVideoCalling(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isVideoCallingEnabled(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(132, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVideoCallingEnabled(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean canChangeDtmfToneLength(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(133, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canChangeDtmfToneLength(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isWorldPhone(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(134, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWorldPhone(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isTtyModeSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(135, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTtyModeSupported();
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isRttSupported(int subscriptionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    boolean _result = false;
                    if (!this.mRemote.transact(136, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRttSupported(subscriptionId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isHearingAidCompatibilitySupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(137, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHearingAidCompatibilitySupported();
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isImsRegistered(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(138, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isImsRegistered(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isWifiCallingAvailable(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(139, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWifiCallingAvailable(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isVideoTelephonyAvailable(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(140, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVideoTelephonyAvailable(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getImsRegTechnologyForMmTel(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(141, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsRegTechnologyForMmTel(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getDeviceId(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(142, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceId(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getImeiForSlot(int slotIndex, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(143, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImeiForSlot(slotIndex, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getTypeAllocationCodeForSlot(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    if (!this.mRemote.transact(144, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTypeAllocationCodeForSlot(slotIndex);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getMeidForSlot(int slotIndex, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(145, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMeidForSlot(slotIndex, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getManufacturerCodeForSlot(int slotIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    if (!this.mRemote.transact(146, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getManufacturerCodeForSlot(slotIndex);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getDeviceSoftwareVersionForSlot(int slotIndex, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(147, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceSoftwareVersionForSlot(slotIndex, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getSubIdForPhoneAccount(PhoneAccount phoneAccount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (phoneAccount != null) {
                        _data.writeInt(1);
                        phoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(148, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubIdForPhoneAccount(phoneAccount);
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

            @Override // com.android.internal.telephony.ITelephony
            public PhoneAccountHandle getPhoneAccountHandleForSubscriptionId(int subscriptionId) throws RemoteException {
                PhoneAccountHandle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    if (!this.mRemote.transact(149, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPhoneAccountHandleForSubscriptionId(subscriptionId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PhoneAccountHandle.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public void factoryReset(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(150, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().factoryReset(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public String getSimLocaleForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(151, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSimLocaleForSubscriber(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void requestModemActivityInfo(ResultReceiver result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(152, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestModemActivityInfo(result);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public ServiceState getServiceStateForSubscriber(int subId, String callingPackage) throws RemoteException {
                ServiceState _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(153, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceStateForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ServiceState.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public Uri getVoicemailRingtoneUri(PhoneAccountHandle accountHandle) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(154, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoicemailRingtoneUri(accountHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Uri.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setVoicemailRingtoneUri(String callingPackage, PhoneAccountHandle phoneAccountHandle, Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (phoneAccountHandle != null) {
                        _data.writeInt(1);
                        phoneAccountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(155, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoicemailRingtoneUri(callingPackage, phoneAccountHandle, uri);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isVoicemailVibrationEnabled(PhoneAccountHandle accountHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(156, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVoicemailVibrationEnabled(accountHandle);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setVoicemailVibrationEnabled(String callingPackage, PhoneAccountHandle phoneAccountHandle, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    int i = 1;
                    if (phoneAccountHandle != null) {
                        _data.writeInt(1);
                        phoneAccountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(157, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoicemailVibrationEnabled(callingPackage, phoneAccountHandle, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public List<String> getPackagesWithCarrierPrivileges(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(158, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackagesWithCarrierPrivileges(phoneId);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public List<String> getPackagesWithCarrierPrivilegesForAllPhones() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(159, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackagesWithCarrierPrivilegesForAllPhones();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public String getAidForAppType(int subId, int appType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(appType);
                    if (!this.mRemote.transact(160, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAidForAppType(subId, appType);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getEsn(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(161, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEsn(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getCdmaPrlVersion(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(162, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaPrlVersion(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public List<TelephonyHistogram> getTelephonyHistograms() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(163, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTelephonyHistograms();
                    }
                    _reply.readException();
                    List<TelephonyHistogram> _result = _reply.createTypedArrayList(TelephonyHistogram.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int setAllowedCarriers(CarrierRestrictionRules carrierRestrictionRules) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (carrierRestrictionRules != null) {
                        _data.writeInt(1);
                        carrierRestrictionRules.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(164, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAllowedCarriers(carrierRestrictionRules);
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

            @Override // com.android.internal.telephony.ITelephony
            public CarrierRestrictionRules getAllowedCarriers() throws RemoteException {
                CarrierRestrictionRules _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(165, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllowedCarriers();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CarrierRestrictionRules.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getSubscriptionCarrierId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(166, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubscriptionCarrierId(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getSubscriptionCarrierName(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(167, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubscriptionCarrierName(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getSubscriptionSpecificCarrierId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(168, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubscriptionSpecificCarrierId(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getSubscriptionSpecificCarrierName(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(169, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubscriptionSpecificCarrierName(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCarrierIdFromMccMnc(int slotIndex, String mccmnc, boolean isSubscriptionMccMnc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(mccmnc);
                    _data.writeInt(isSubscriptionMccMnc ? 1 : 0);
                    if (!this.mRemote.transact(170, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierIdFromMccMnc(slotIndex, mccmnc, isSubscriptionMccMnc);
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

            @Override // com.android.internal.telephony.ITelephony
            public void carrierActionSetMeteredApnsEnabled(int subId, boolean visible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(visible ? 1 : 0);
                    if (this.mRemote.transact(171, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().carrierActionSetMeteredApnsEnabled(subId, visible);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void carrierActionSetRadioEnabled(int subId, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(172, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().carrierActionSetRadioEnabled(subId, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void carrierActionReportDefaultNetworkStatus(int subId, boolean report) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(report ? 1 : 0);
                    if (this.mRemote.transact(173, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().carrierActionReportDefaultNetworkStatus(subId, report);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void carrierActionResetAll(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(174, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().carrierActionResetAll(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public NetworkStats getVtDataUsage(int subId, boolean perUidStats) throws RemoteException {
                NetworkStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(perUidStats ? 1 : 0);
                    if (!this.mRemote.transact(175, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVtDataUsage(subId, perUidStats);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkStats.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setPolicyDataEnabled(boolean enabled, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(176, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPolicyDataEnabled(enabled, subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public List<ClientRequestStats> getClientRequestStats(String callingPackage, int subid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subid);
                    if (!this.mRemote.transact(177, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getClientRequestStats(callingPackage, subid);
                    }
                    _reply.readException();
                    List<ClientRequestStats> _result = _reply.createTypedArrayList(ClientRequestStats.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void setDataEnabledProperties(String appName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appName);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(178, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDataEnabledProperties(appName, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void setSimPowerStateForSlot(int slotIndex, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeInt(state);
                    if (this.mRemote.transact(179, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSimPowerStateForSlot(slotIndex, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public String[] getForbiddenPlmns(int subId, int appType, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(appType);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(180, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getForbiddenPlmns(subId, appType, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean getEmergencyCallbackMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(181, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEmergencyCallbackMode(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public SignalStrength getSignalStrength(int subId) throws RemoteException {
                SignalStrength _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(182, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSignalStrength(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SignalStrength.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getCardIdForDefaultEuicc(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(183, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCardIdForDefaultEuicc(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public List<UiccCardInfo> getUiccCardsInfo(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(184, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUiccCardsInfo(callingPackage);
                    }
                    _reply.readException();
                    List<UiccCardInfo> _result = _reply.createTypedArrayList(UiccCardInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public UiccSlotInfo[] getUiccSlotsInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(185, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUiccSlotsInfo();
                    }
                    _reply.readException();
                    UiccSlotInfo[] _result = (UiccSlotInfo[]) _reply.createTypedArray(UiccSlotInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean switchSlots(int[] physicalSlots) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(physicalSlots);
                    boolean _result = false;
                    if (!this.mRemote.transact(186, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().switchSlots(physicalSlots);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setRadioIndicationUpdateMode(int subId, int filters, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(filters);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(187, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRadioIndicationUpdateMode(subId, filters, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isDataRoamingEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(188, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDataRoamingEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setDataRoamingEnabled(int subId, boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(isEnabled ? 1 : 0);
                    if (this.mRemote.transact(189, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDataRoamingEnabled(subId, isEnabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getCdmaRoamingMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(190, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCdmaRoamingMode(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setCdmaRoamingMode(int subId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(mode);
                    boolean _result = false;
                    if (!this.mRemote.transact(191, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCdmaRoamingMode(subId, mode);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setCdmaSubscriptionMode(int subId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(mode);
                    boolean _result = false;
                    if (!this.mRemote.transact(192, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCdmaSubscriptionMode(subId, mode);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setCarrierTestOverride(int subId, String mccmnc, String imsi, String iccid, String gid1, String gid2, String plmn, String spn, String carrierPrivilegeRules, String apn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(mccmnc);
                        _data.writeString(imsi);
                        _data.writeString(iccid);
                        _data.writeString(gid1);
                        _data.writeString(gid2);
                        _data.writeString(plmn);
                        _data.writeString(spn);
                        _data.writeString(carrierPrivilegeRules);
                        _data.writeString(apn);
                        if (this.mRemote.transact(193, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().setCarrierTestOverride(subId, mccmnc, imsi, iccid, gid1, gid2, plmn, spn, carrierPrivilegeRules, apn);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getCarrierIdListVersion(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(194, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierIdListVersion(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void refreshUiccProfile(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(195, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().refreshUiccProfile(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getNumberOfModemsWithSimultaneousDataConnections(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(196, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNumberOfModemsWithSimultaneousDataConnections(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int getNetworkSelectionMode(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(197, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkSelectionMode(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isInEmergencySmsMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(198, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInEmergencySmsMode();
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

            @Override // com.android.internal.telephony.ITelephony
            public String[] getSmsApps(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(199, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSmsApps(userId);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getDefaultSmsApp(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(200, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultSmsApp(userId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setDefaultSmsApp(int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(201, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultSmsApp(userId, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getRadioPowerState(int slotIndex, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(202, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRadioPowerState(slotIndex, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public void registerImsRegistrationCallback(int subId, IImsRegistrationCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (this.mRemote.transact(203, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerImsRegistrationCallback(subId, c);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void unregisterImsRegistrationCallback(int subId, IImsRegistrationCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (this.mRemote.transact(204, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterImsRegistrationCallback(subId, c);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void registerMmTelCapabilityCallback(int subId, IImsCapabilityCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (this.mRemote.transact(205, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerMmTelCapabilityCallback(subId, c);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void unregisterMmTelCapabilityCallback(int subId, IImsCapabilityCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (this.mRemote.transact(206, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterMmTelCapabilityCallback(subId, c);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isCapable(int subId, int capability, int regTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(capability);
                    _data.writeInt(regTech);
                    boolean _result = false;
                    if (!this.mRemote.transact(207, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCapable(subId, capability, regTech);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isAvailable(int subId, int capability, int regTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(capability);
                    _data.writeInt(regTech);
                    boolean _result = false;
                    if (!this.mRemote.transact(208, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAvailable(subId, capability, regTech);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isAdvancedCallingSettingEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(209, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAdvancedCallingSettingEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setAdvancedCallingSettingEnabled(int subId, boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(isEnabled ? 1 : 0);
                    if (this.mRemote.transact(210, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAdvancedCallingSettingEnabled(subId, isEnabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isVtSettingEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(211, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVtSettingEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setVtSettingEnabled(int subId, boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(isEnabled ? 1 : 0);
                    if (this.mRemote.transact(212, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVtSettingEnabled(subId, isEnabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isVoWiFiSettingEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(213, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVoWiFiSettingEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setVoWiFiSettingEnabled(int subId, boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(isEnabled ? 1 : 0);
                    if (this.mRemote.transact(214, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoWiFiSettingEnabled(subId, isEnabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isVoWiFiRoamingSettingEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(215, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVoWiFiRoamingSettingEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setVoWiFiRoamingSettingEnabled(int subId, boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(isEnabled ? 1 : 0);
                    if (this.mRemote.transact(216, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoWiFiRoamingSettingEnabled(subId, isEnabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void setVoWiFiNonPersistent(int subId, boolean isCapable, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(isCapable ? 1 : 0);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(217, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoWiFiNonPersistent(subId, isCapable, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getVoWiFiModeSetting(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(218, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoWiFiModeSetting(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setVoWiFiModeSetting(int subId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(219, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoWiFiModeSetting(subId, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getVoWiFiRoamingModeSetting(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(220, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoWiFiRoamingModeSetting(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setVoWiFiRoamingModeSetting(int subId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(221, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVoWiFiRoamingModeSetting(subId, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void setRttCapabilitySetting(int subId, boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(isEnabled ? 1 : 0);
                    if (this.mRemote.transact(222, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRttCapabilitySetting(subId, isEnabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isTtyOverVolteEnabled(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(223, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTtyOverVolteEnabled(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public Map getEmergencyNumberList(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(224, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEmergencyNumberList(callingPackage);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean isEmergencyNumber(String number, boolean exactMatch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(number);
                    boolean _result = true;
                    _data.writeInt(exactMatch ? 1 : 0);
                    if (!this.mRemote.transact(225, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEmergencyNumber(number, exactMatch);
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

            @Override // com.android.internal.telephony.ITelephony
            public List<String> getCertsFromCarrierPrivilegeAccessRules(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(226, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCertsFromCarrierPrivilegeAccessRules(subId);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void registerImsProvisioningChangedCallback(int subId, IImsConfigCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(227, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerImsProvisioningChangedCallback(subId, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void unregisterImsProvisioningChangedCallback(int subId, IImsConfigCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(228, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterImsProvisioningChangedCallback(subId, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public void setImsProvisioningStatusForCapability(int subId, int capability, int tech, boolean isProvisioned) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(capability);
                    _data.writeInt(tech);
                    _data.writeInt(isProvisioned ? 1 : 0);
                    if (this.mRemote.transact(229, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setImsProvisioningStatusForCapability(subId, capability, tech, isProvisioned);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean getImsProvisioningStatusForCapability(int subId, int capability, int tech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(capability);
                    _data.writeInt(tech);
                    boolean _result = false;
                    if (!this.mRemote.transact(230, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsProvisioningStatusForCapability(subId, capability, tech);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isMmTelCapabilityProvisionedInCache(int subId, int capability, int tech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(capability);
                    _data.writeInt(tech);
                    boolean _result = false;
                    if (!this.mRemote.transact(231, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isMmTelCapabilityProvisionedInCache(subId, capability, tech);
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

            @Override // com.android.internal.telephony.ITelephony
            public void cacheMmTelCapabilityProvisioning(int subId, int capability, int tech, boolean isProvisioned) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(capability);
                    _data.writeInt(tech);
                    _data.writeInt(isProvisioned ? 1 : 0);
                    if (this.mRemote.transact(232, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cacheMmTelCapabilityProvisioning(subId, capability, tech, isProvisioned);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int getImsProvisioningInt(int subId, int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(key);
                    if (!this.mRemote.transact(233, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsProvisioningInt(subId, key);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getImsProvisioningString(int subId, int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(key);
                    if (!this.mRemote.transact(234, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsProvisioningString(subId, key);
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

            @Override // com.android.internal.telephony.ITelephony
            public int setImsProvisioningInt(int subId, int key, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(key);
                    _data.writeInt(value);
                    if (!this.mRemote.transact(235, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setImsProvisioningInt(subId, key, value);
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

            @Override // com.android.internal.telephony.ITelephony
            public int setImsProvisioningString(int subId, int key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(key);
                    _data.writeString(value);
                    if (!this.mRemote.transact(236, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setImsProvisioningString(subId, key, value);
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

            @Override // com.android.internal.telephony.ITelephony
            public void updateEmergencyNumberListTestMode(int action, EmergencyNumber num) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    if (num != null) {
                        _data.writeInt(1);
                        num.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(237, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateEmergencyNumberListTestMode(action, num);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public List<String> getEmergencyNumberListTestMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(238, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEmergencyNumberListTestMode();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean enableModemForSlot(int slotIndex, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(239, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableModemForSlot(slotIndex, enable);
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

            @Override // com.android.internal.telephony.ITelephony
            public void setMultiSimCarrierRestriction(boolean isMultiSimCarrierRestricted) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isMultiSimCarrierRestricted ? 1 : 0);
                    if (this.mRemote.transact(240, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMultiSimCarrierRestriction(isMultiSimCarrierRestricted);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public int isMultiSimSupported(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(241, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isMultiSimSupported(callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public void switchMultiSimConfig(int numOfSims) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(numOfSims);
                    if (this.mRemote.transact(242, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().switchMultiSimConfig(numOfSims);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public boolean doesSwitchMultiSimConfigTriggerReboot(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(243, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().doesSwitchMultiSimConfigTriggerReboot(subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public int[] getSlotsMapping() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(244, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSlotsMapping();
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

            @Override // com.android.internal.telephony.ITelephony
            public int getRadioHalVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(245, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRadioHalVersion();
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isModemEnabledForSlot(int slotIndex, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotIndex);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(246, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isModemEnabledForSlot(slotIndex, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isDataEnabledForApn(int apnType, int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(apnType);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(247, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDataEnabledForApn(apnType, subId, callingPackage);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isApnMetered(int apnType, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(apnType);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(248, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isApnMetered(apnType, subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public void enqueueSmsPickResult(String callingPackage, IIntegerConsumer subIdResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(subIdResult != null ? subIdResult.asBinder() : null);
                    if (this.mRemote.transact(249, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().enqueueSmsPickResult(callingPackage, subIdResult);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ITelephony
            public String getMmsUserAgent(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(250, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMmsUserAgent(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public String getMmsUAProfUrl(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(251, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMmsUAProfUrl(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean setDataAllowedDuringVoiceCall(int subId, boolean allow) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = true;
                    _data.writeInt(allow ? 1 : 0);
                    if (!this.mRemote.transact(252, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDataAllowedDuringVoiceCall(subId, allow);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isDataAllowedInVoiceCall(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(253, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDataAllowedInVoiceCall(subId);
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

            @Override // com.android.internal.telephony.ITelephony
            public boolean isEmergencyNumberForSubId(int subId, String number, boolean exactMatch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(number);
                    boolean _result = true;
                    _data.writeInt(exactMatch ? 1 : 0);
                    if (!this.mRemote.transact(254, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEmergencyNumberForSubId(subId, number, exactMatch);
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
        }

        public static boolean setDefaultImpl(ITelephony impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITelephony getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
