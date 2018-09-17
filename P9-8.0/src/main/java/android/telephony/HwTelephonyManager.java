package android.telephony;

import android.os.Message;
import android.telecom.PhoneAccount;
import android.telephony.TelephonyManager.MultiSimVariants;
import com.android.internal.telephony.IPhoneCallback;

public class HwTelephonyManager {
    private static final /* synthetic */ int[] -android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues = null;
    public static final String CARD_TYPE_SIM1 = "gsm.sim1.type";
    public static final String CARD_TYPE_SIM2 = "gsm.sim2.type";
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    public static final int DUAL_MODE_CG_CARD = 40;
    public static final int DUAL_MODE_TELECOM_LTE_CARD = 43;
    public static final int DUAL_MODE_UG_CARD = 50;
    public static final int EXTRA_VALUE_NEW_SIM = 1;
    public static final int EXTRA_VALUE_NOCHANGE = 4;
    public static final int EXTRA_VALUE_REMOVE_SIM = 2;
    public static final int EXTRA_VALUE_REPOSITION_SIM = 3;
    public static final String INTENT_KEY_DETECT_STATUS = "simDetectStatus";
    public static final String INTENT_KEY_NEW_SIM_SLOT = "newSIMSlot";
    public static final String INTENT_KEY_NEW_SIM_STATUS = "newSIMStatus";
    public static final String INTENT_KEY_SIM_COUNT = "simCount";
    public static final int KEY_GET_FRAMEWORK_SUPPROT_VSIM_VER = 1;
    public static final int KEY_GET_MODEM_SUPPROT_VSIM_VER = 0;
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int PHONE_EVENT_IMSA_TO_MAPCON = 4;
    public static final int PHONE_EVENT_RADIO_AVAILABLE = 1;
    public static final int PHONE_EVENT_RADIO_UNAVAILABLE = 2;
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    public static final int SUPPORT_SYSTEMAPP_GET_DEVICEID = 1;
    public static final int UNKNOWN_CARD = -1;
    private static HwTelephonyManager sInstance = new HwTelephonyManager();

    public enum MultiSimVariantsEx {
        DSDS,
        DSDA,
        TSTS,
        UNKNOWN
    }

    private static /* synthetic */ int[] -getandroid-telephony-TelephonyManager$MultiSimVariantsSwitchesValues() {
        if (-android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues != null) {
            return -android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues;
        }
        int[] iArr = new int[MultiSimVariants.values().length];
        try {
            iArr[MultiSimVariants.DSDA.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[MultiSimVariants.DSDS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[MultiSimVariants.TSTS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[MultiSimVariants.UNKNOWN.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-telephony-TelephonyManager$MultiSimVariantsSwitchesValues = iArr;
        return iArr;
    }

    public static HwTelephonyManager getDefault() {
        return sInstance;
    }

    public String getMeid() {
        return HwTelephonyManagerInner.getDefault().getMeid();
    }

    public String getMeid(int slotId) {
        return HwTelephonyManagerInner.getDefault().getMeid(slotId);
    }

    public String getPesn() {
        return HwTelephonyManagerInner.getDefault().getPesn();
    }

    public String getPesn(int slotId) {
        return HwTelephonyManagerInner.getDefault().getPesn(slotId);
    }

    public void closeRrc() {
        HwTelephonyManagerInner.getDefault().closeRrc();
    }

    public int getSubState(long subId) {
        return HwTelephonyManagerInner.getDefault().getSubState(subId);
    }

    public void setUserPrefDataSlotId(int slotId) {
        HwTelephonyManagerInner.getDefault().setUserPrefDataSlotId(slotId);
    }

    public boolean checkCdmaSlaveCardMode(int mode) {
        return HwTelephonyManagerInner.getDefault().checkCdmaSlaveCardMode(mode);
    }

    public boolean isFullNetworkSupported() {
        return HwTelephonyManagerInner.getDefault().isFullNetworkSupported();
    }

    public boolean isChinaTelecom(int slotId) {
        return HwTelephonyManagerInner.getDefault().isChinaTelecom(slotId);
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public boolean isCDMASimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCDMASimCard(slotId);
    }

    public int getCardType(int slotId) {
        return HwTelephonyManagerInner.getDefault().getCardType(slotId);
    }

    public boolean isDomesticCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isDomesticCard(slotId);
    }

    public boolean isCTCdmaCardInGsmMode() {
        return HwTelephonyManagerInner.getDefault().isCTCdmaCardInGsmMode();
    }

    public int getDataState(long subId) {
        return HwTelephonyManagerInner.getDefault().getDataState(subId);
    }

    public void setLteServiceAbility(int ability) {
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(ability);
    }

    public int getLteServiceAbility() {
        return HwTelephonyManagerInner.getDefault().getLteServiceAbility();
    }

    public boolean isDualImsSupported() {
        return HwTelephonyManagerInner.getDefault().isDualImsSupported();
    }

    public int getLteServiceAbility(int subId) {
        return HwTelephonyManagerInner.getDefault().getLteServiceAbility(subId);
    }

    public void setLteServiceAbility(int subId, int ability) {
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(subId, ability);
    }

    public void setImsRegistrationState(int subId, boolean registered) {
        HwTelephonyManagerInner.getDefault().setImsRegistrationState(subId, registered);
    }

    public boolean isImsRegistered(int subId) {
        return HwTelephonyManagerInner.getDefault().isImsRegistered(subId);
    }

    public boolean isVolteAvailable(int subId) {
        return HwTelephonyManagerInner.getDefault().isVolteAvailable(subId);
    }

    public boolean isVideoTelephonyAvailable(int subId) {
        return HwTelephonyManagerInner.getDefault().isVideoTelephonyAvailable(subId);
    }

    public boolean isWifiCallingAvailable(int subId) {
        return HwTelephonyManagerInner.getDefault().isWifiCallingAvailable(subId);
    }

    public boolean isSubDeactivedByPowerOff(long sub) {
        return HwTelephonyManagerInner.getDefault().isSubDeactivedByPowerOff(sub);
    }

    public boolean isNeedToRadioPowerOn(long sub) {
        return HwTelephonyManagerInner.getDefault().isNeedToRadioPowerOn(sub);
    }

    public boolean isCardPresent(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCardPresent(slotId);
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public String getIccATR() {
        return HwTelephonyManagerInner.getDefault().getIccATR();
    }

    public int getPreferredDataSubscription() {
        return HwTelephonyManagerInner.getDefault().getPreferredDataSubscription();
    }

    public String getCdmaGsmImsi() {
        return HwTelephonyManagerInner.getDefault().getCdmaGsmImsi();
    }

    public String getCdmaGsmImsiForSubId(int subId) {
        return HwTelephonyManagerInner.getDefault().getCdmaGsmImsiForSubId(subId);
    }

    public CellLocation getCellLocation(int slotId) {
        return HwTelephonyManagerInner.getDefault().getCellLocation(slotId);
    }

    public String getCdmaMlplVersion() {
        return HwTelephonyManagerInner.getDefault().getCdmaMlplVersion();
    }

    public String getCdmaMsplVersion() {
        return HwTelephonyManagerInner.getDefault().getCdmaMsplVersion();
    }

    public boolean isLTESupported() {
        return HwTelephonyManagerInner.getDefault().isLTESupported();
    }

    public int getSpecCardType(int slotId) {
        return HwTelephonyManagerInner.getDefault().getSpecCardType(slotId);
    }

    public boolean isCardUimLocked(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCardUimLocked(slotId);
    }

    public boolean isRadioOn(int slotId) {
        return HwTelephonyManagerInner.getDefault().isRadioOn(slotId);
    }

    public boolean registerFor4GCardRadioAvailable(IPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().registerForRadioAvailable(callback);
    }

    public boolean unregisterFor4GCardRadioAvailable(IPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().unregisterForRadioAvailable(callback);
    }

    public boolean registerFor4GCardRadioNotAvailable(IPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().registerForRadioNotAvailable(callback);
    }

    public boolean unregisterFor4GCardRadioNotAvailable(IPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().unregisterForRadioNotAvailable(callback);
    }

    public boolean registerCommonImsaToMapconInfo(IPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().registerCommonImsaToMapconInfo(callback);
    }

    public boolean unregisterCommonImsaToMapconInfo(IPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().unregisterCommonImsaToMapconInfo(callback);
    }

    public boolean is4GCardRadioAvailable() {
        return HwTelephonyManagerInner.getDefault().isRadioAvailable();
    }

    public void setImsSwitch(boolean value) {
        HwTelephonyManagerInner.getDefault().setImsSwitch(value);
    }

    public boolean getImsSwitch() {
        return HwTelephonyManagerInner.getDefault().getImsSwitch();
    }

    public void setImsDomainConfig(int domainType) {
        HwTelephonyManagerInner.getDefault().setImsDomainConfig(domainType);
    }

    public boolean handleMapconImsaReq(byte[] Msg) {
        return HwTelephonyManagerInner.getDefault().handleMapconImsaReq(Msg);
    }

    public int getUiccAppType() {
        return HwTelephonyManagerInner.getDefault().getUiccAppType();
    }

    public int getImsDomain() {
        return HwTelephonyManagerInner.getDefault().getImsDomain();
    }

    public UiccAuthResponse handleUiccAuth(int auth_type, byte[] rand, byte[] auth) {
        return HwTelephonyManagerInner.getDefault().handleUiccAuth(auth_type, rand, auth);
    }

    public boolean registerForPhoneEvent(int phoneId, IPhoneCallback callback, int events) {
        return HwTelephonyManagerInner.getDefault().registerForPhoneEvent(phoneId, callback, events);
    }

    public boolean unregisterForPhoneEvent(IPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().unregisterForPhoneEvent(callback);
    }

    public boolean isRadioAvailable(int phoneId) {
        return HwTelephonyManagerInner.getDefault().isRadioAvailable(phoneId);
    }

    public void setImsSwitch(int phoneId, boolean value) {
        HwTelephonyManagerInner.getDefault().setImsSwitch(phoneId, value);
    }

    public boolean getImsSwitch(int phoneId) {
        return HwTelephonyManagerInner.getDefault().getImsSwitch(phoneId);
    }

    public void setImsDomainConfig(int phoneId, int domainType) {
        HwTelephonyManagerInner.getDefault().setImsDomainConfig(phoneId, domainType);
    }

    public boolean handleMapconImsaReq(int phoneId, byte[] Msg) {
        return HwTelephonyManagerInner.getDefault().handleMapconImsaReq(phoneId, Msg);
    }

    public int getUiccAppType(int phoneId) {
        return HwTelephonyManagerInner.getDefault().getUiccAppType(phoneId);
    }

    public int getImsDomain(int phoneId) {
        return HwTelephonyManagerInner.getDefault().getImsDomain(phoneId);
    }

    public UiccAuthResponse handleUiccAuth(int phoneId, int auth_type, byte[] rand, byte[] auth) {
        return HwTelephonyManagerInner.getDefault().handleUiccAuth(phoneId, auth_type, rand, auth);
    }

    public boolean isPlatformSupportVsim() {
        return HwVSimManager.getDefault().isPlatformSupportVsim();
    }

    public int maxVSimModemCount() {
        return HwVSimManager.getDefault().maxVSimModemCount();
    }

    public boolean isVSimEnabled() {
        return HwVSimManager.getDefault().isVSimEnabled();
    }

    public boolean getWaitingSwitchBalongSlot() {
        return HwTelephonyManagerInner.getDefault().getWaitingSwitchBalongSlot();
    }

    public int getVSimOccupiedSubId() {
        return HwVSimManager.getDefault().getVSimOccupiedSubId();
    }

    public int getPlatformSupportVSimVer(int key) {
        return HwVSimManager.getDefault().getPlatformSupportVSimVer(key);
    }

    public boolean hasIccCardForVSim(int slotId) {
        return HwVSimManager.getDefault().hasIccCardForVSim(slotId);
    }

    public boolean hasVSimIccCard() {
        return HwVSimManager.getDefault().hasVSimIccCard();
    }

    public int getSimStateForVSim(int slotIdx) {
        return HwVSimManager.getDefault().getSimStateForVSim(slotIdx);
    }

    public int getVSimState() {
        return HwVSimManager.getDefault().getVSimState();
    }

    public int getVSimSubId() {
        return HwVSimManager.getDefault().getVSimSubId();
    }

    public boolean isVSimInProcess() {
        return HwVSimManager.getDefault().isVSimInProcess();
    }

    public boolean isVSimOn() {
        return HwVSimManager.getDefault().isVSimOn();
    }

    public int enableVSim(String imsi, int cardtype, int apntype, String acqorder, String challenge) {
        return HwVSimManager.getDefault().enableVSim(imsi, cardtype, apntype, acqorder, challenge);
    }

    public int enableVSim(String imsi, int cardtype, int apntype, String acqorder, String tapath, int vsimloc, String challenge) {
        return HwVSimManager.getDefault().enableVSim(imsi, cardtype, apntype, acqorder, tapath, vsimloc, challenge);
    }

    public boolean disableVSim() {
        return HwVSimManager.getDefault().disableVSim();
    }

    public int setApn(int cardtype, int apntype, String challenge) {
        return HwVSimManager.getDefault().setApn(cardtype, apntype, challenge);
    }

    public int setAPN(String imsi, int cardtype, int apntype, String taPath, String challenge) {
        return HwVSimManager.getDefault().setApn(imsi, cardtype, apntype, taPath, challenge);
    }

    public boolean hasHardIccCardForVSim(int subId) {
        return HwVSimManager.getDefault().hasHardIccCardForVSim(subId);
    }

    public int getSimMode(int subId) {
        return HwVSimManager.getDefault().getSimMode(subId);
    }

    public void recoverSimMode() {
        HwVSimManager.getDefault().recoverSimMode();
    }

    public String getRegPlmn(int subId) {
        return HwVSimManager.getDefault().getRegPlmn(subId);
    }

    public String getTrafficData() {
        return HwVSimManager.getDefault().getTrafficData();
    }

    public Boolean clearTrafficData() {
        return HwVSimManager.getDefault().clearTrafficData();
    }

    public boolean dsFlowCfg(int repFlag, int threshold, int totalThreshold, int oper) {
        return HwVSimManager.getDefault().dsFlowCfg(repFlag, threshold, totalThreshold, oper);
    }

    public int getSimStateViaSysinfoEx(int subId) {
        return HwVSimManager.getDefault().getSimStateViaSysinfoEx(subId);
    }

    public int getCpserr(int subId) {
        return HwVSimManager.getDefault().getCpserr(subId);
    }

    public int getVsimAvailableNetworks(int subId, int type) {
        return HwVSimManager.getDefault().getVsimAvailableNetworks(subId, type);
    }

    public boolean setUserReservedSubId(int subId) {
        return HwVSimManager.getDefault().setUserReservedSubId(subId);
    }

    public int getUserReservedSubId() {
        return HwVSimManager.getDefault().getUserReservedSubId();
    }

    public String getDevSubMode(int subscription) {
        return HwVSimManager.getDefault().getDevSubMode(subscription);
    }

    public String getDevSubMode() {
        return HwVSimManager.getDefault().getDevSubMode();
    }

    public String getPreferredNetworkTypeForVSim(int subscription) {
        return HwVSimManager.getDefault().getPreferredNetworkTypeForVSim(subscription);
    }

    public String getPreferredNetworkTypeForVSim() {
        return HwVSimManager.getDefault().getPreferredNetworkTypeForVSim();
    }

    public int getVSimCurCardType() {
        return HwVSimManager.getDefault().getVSimCurCardType();
    }

    public int getOperatorWithDeviceCustomed() {
        return HwVSimManager.getDefault().getOperatorWithDeviceCustomed();
    }

    public int getDeviceNetworkCountryIso() {
        return HwVSimManager.getDefault().getDeviceNetworkCountryIso();
    }

    public String getVSimNetworkOperator() {
        return HwVSimManager.getDefault().getVSimNetworkOperator();
    }

    public String getVSimNetworkCountryIso() {
        return HwVSimManager.getDefault().getVSimNetworkCountryIso();
    }

    public String getVSimNetworkOperatorName() {
        return HwVSimManager.getDefault().getVSimNetworkOperatorName();
    }

    public int getVSimNetworkType() {
        return HwVSimManager.getDefault().getVSimNetworkType();
    }

    public String getVSimNetworkTypeName() {
        return HwVSimManager.getDefault().getVSimNetworkTypeName();
    }

    public String getVSimSubscriberId() {
        return HwVSimManager.getDefault().getVSimSubscriberId();
    }

    public boolean setVSimULOnlyMode(boolean isULOnly) {
        return HwVSimManager.getDefault().setVSimULOnlyMode(isULOnly);
    }

    public boolean getVSimULOnlyMode() {
        return HwVSimManager.getDefault().getVSimULOnlyMode();
    }

    public int getVSimPlatformCapability() {
        return HwVSimManager.getDefault().getVSimPlatformCapability();
    }

    public boolean switchVSimWorkMode(int workMode) {
        return HwVSimManager.getDefault().switchVSimWorkMode(workMode);
    }

    public int dialupForVSim() {
        return HwVSimManager.getDefault().dialupForVSim();
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return HwTelephonyManagerInner.getDefault().cmdForECInfo(event, action, buf);
    }

    public boolean notifyDeviceState(String device, String state, String extras) {
        return HwTelephonyManagerInner.getDefault().notifyDeviceState(device, state, extras);
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype, Message response) {
        HwTelephonyManagerInner.getDefault().notifyCellularCommParaReady(paratype, pathtype, response);
    }

    public boolean sendPseudocellCellInfo(int type, int lac, int cid, int radioTech, String plmn, int subId) {
        return HwTelephonyManagerInner.getDefault().sendPseudocellCellInfo(type, lac, cid, radioTech, plmn, subId);
    }

    public String getSubscriberId(TelephonyManager tm, int subId) {
        return tm.getSubscriberId(subId);
    }

    public String getSimCountryIso(TelephonyManager telephonyManager, int subId) {
        return telephonyManager.getSimCountryIso(subId);
    }

    public String getIccAuthentication(TelephonyManager telephonyManager, int subId, int appType, int authType, String data) {
        return telephonyManager.getIccAuthentication(subId, appType, authType, data);
    }

    public static int getNetworkClass(int networkType) {
        return TelephonyManager.getNetworkClass(networkType);
    }

    public static ServiceState getServiceStateForSubscriber(int subId) {
        return TelephonyManager.getDefault().getServiceStateForSubscriber(subId);
    }

    public static boolean isWifiCallingAvailable() {
        return TelephonyManager.getDefault().isWifiCallingAvailable();
    }

    public static String getSimOperator(int subId) {
        return TelephonyManager.getDefault().getSimOperator(subId);
    }

    public static String getSimOperatorNumeric() {
        return TelephonyManager.getDefault().getSimOperatorNumeric();
    }

    public static String getSimOperatorNumericForPhone(int subId) {
        return TelephonyManager.getDefault().getSimOperatorNumericForPhone(subId);
    }

    public static boolean isMultiSimEnabled() {
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    public static int getSubIdForPhoneAccount(PhoneAccount phoneAccount) {
        return TelephonyManager.getDefault().getSubIdForPhoneAccount(phoneAccount);
    }

    public static boolean isVideoCallingEnabled() {
        return TelephonyManager.getDefault().isVideoCallingEnabled();
    }

    public static boolean isDsdaEnabled() {
        return TelephonyManager.getDefault().getMultiSimConfiguration() == MultiSimVariants.DSDA;
    }

    public static boolean isDsdsEnabled() {
        return TelephonyManager.getDefault().getMultiSimConfiguration() == MultiSimVariants.DSDS;
    }

    public static int getDataNetworkType(TelephonyManager telephonyManager, int subId) {
        return telephonyManager.getDataNetworkType(subId);
    }

    public static boolean getDataEnabled(TelephonyManager telephonyManager) {
        return telephonyManager.getDataEnabled();
    }

    public boolean setDmRcsConfig(int subId, int rcsCapability, int devConfig, Message response) {
        return HwTelephonyManagerInner.getDefault().setDmRcsConfig(subId, rcsCapability, devConfig, response);
    }

    public boolean setRcsSwitch(int subId, int switchState, Message response) {
        return HwTelephonyManagerInner.getDefault().setRcsSwitch(subId, switchState, response);
    }

    public boolean getRcsSwitchState(int subId, Message response) {
        return HwTelephonyManagerInner.getDefault().getRcsSwitchState(subId, response);
    }

    public boolean setDmPcscf(int subId, String pcscf, Message response) {
        return HwTelephonyManagerInner.getDefault().setDmPcscf(subId, pcscf, response);
    }

    public boolean sendLaaCmd(int cmd, String reserved, Message response) {
        return HwTelephonyManagerInner.getDefault().sendLaaCmd(cmd, reserved, response);
    }

    public boolean getLaaDetailedState(String reserved, Message response) {
        return HwTelephonyManagerInner.getDefault().getLaaDetailedState(reserved, response);
    }

    public String getMsisdn(TelephonyManager telephonyManager, int subId) {
        if (telephonyManager == null) {
            return null;
        }
        return telephonyManager.getMsisdn(subId);
    }

    public int getNetworkType(int subId) {
        return TelephonyManager.getDefault().getNetworkType(subId);
    }

    public MultiSimVariantsEx getMultiSimConfiguration() {
        switch (-getandroid-telephony-TelephonyManager$MultiSimVariantsSwitchesValues()[TelephonyManager.getDefault().getMultiSimConfiguration().ordinal()]) {
            case 1:
                return MultiSimVariantsEx.DSDA;
            case 2:
                return MultiSimVariantsEx.DSDS;
            case 3:
                return MultiSimVariantsEx.TSTS;
            case 4:
                return MultiSimVariantsEx.UNKNOWN;
            default:
                return MultiSimVariantsEx.UNKNOWN;
        }
    }

    public String getSimSerialNumber(TelephonyManager telephonyManager, int subId) {
        if (telephonyManager == null) {
            return null;
        }
        return telephonyManager.getSimSerialNumber(subId);
    }

    public void registerForCallAltSrv(int subId, IPhoneCallback callback) {
        HwTelephonyManagerInner.getDefault().registerForCallAltSrv(subId, callback);
    }

    public void unregisterForCallAltSrv(int subId) {
        HwTelephonyManagerInner.getDefault().unregisterForCallAltSrv(subId);
    }

    public static String getSimOperatorName(int subId) {
        return TelephonyManager.getDefault().getSimOperatorName(subId);
    }

    public String getImsImpu(int subId) {
        return HwTelephonyManagerInner.getDefault().getImsImpu(subId);
    }

    public void writeSimLockNwData(String[] data) {
        HwTelephonyManagerInner.getDefault().writeSimLockNwData(data);
    }

    public boolean isImeiBindSlotSupported() {
        return HwTelephonyManagerInner.getDefault().isImeiBindSlotSupported();
    }
}
