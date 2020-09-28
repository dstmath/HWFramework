package android.telephony;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.telecom.PhoneAccount;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccManager;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.IPhoneCallback;
import com.android.internal.telephony.euicc.HwEuiccManager;
import com.huawei.android.telephony.SubscriptionInfoEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.telephony.euicc.DownloadableSubscriptionEx;
import com.huawei.android.util.NoExtAPIException;
import java.util.List;

public class HwTelephonyManager {
    public static final String CARD_TYPE_SIM1 = "gsm.sim1.type";
    public static final String CARD_TYPE_SIM2 = "gsm.sim2.type";
    public static final int CARRIER_PRIVILEGE_STATUS_HAS_ACCESS = 1;
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
    public static final int SERVICE_ABILITY_OFF = 0;
    public static final int SERVICE_ABILITY_ON = 1;
    public static final int SERVICE_TYPE_LTE = 0;
    public static final int SERVICE_TYPE_NR = 1;
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    public static final int SUPPORT_SYSTEMAPP_GET_DEVICEID = 1;
    public static final int UNKNOWN_CARD = -1;
    private static HwTelephonyManager sInstance = new HwTelephonyManager();
    private HwPhoneCallback mHwPhoneCallback = null;

    public interface HwPhoneCallBackInterface {
        void onCallback1(int i);

        void onCallback2(int i, int i2);

        void onCallback3(int i, int i2, Bundle bundle);
    }

    public enum MultiSimVariantsEx {
        DSDS,
        DSDA,
        TSTS,
        UNKNOWN
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
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isCTCdmaCardInGsmMode() {
        return HwTelephonyManagerInner.getDefault().isCTCdmaCardInGsmMode();
    }

    public int getDataState(long subId) {
        return HwTelephonyManagerInner.getDefault().getDataState(subId);
    }

    public void setServiceAbility(int slotId, int type, int ability) {
        HwTelephonyManagerInner.getDefault().setServiceAbility(slotId, type, ability);
    }

    public int getServiceAbility(int slotId, int type) {
        return HwTelephonyManagerInner.getDefault().getServiceAbility(slotId, type);
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

    public int getLteServiceAbility(int slotId) {
        return HwTelephonyManagerInner.getDefault().getLteServiceAbility(slotId);
    }

    public void setLteServiceAbility(int slotId, int ability) {
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(slotId, ability);
    }

    public int getNetworkModeFromDB(int subId) {
        return HwTelephonyManagerInner.getDefault().getNetworkModeFromDB(subId);
    }

    public void saveNetworkModeToDB(int subId, int mode) {
        HwTelephonyManagerInner.getDefault().saveNetworkModeToDB(subId, mode);
    }

    public void setImsRegistrationState(int subId, boolean registered) {
        HwTelephonyManagerInner.getDefault().setImsRegistrationState(subId, registered);
    }

    public boolean isImsRegistered(int subId) {
        return HwTelephonyManagerInner.getDefault().isImsRegistered(subId);
    }

    public boolean isImsRegistered() {
        return TelephonyManagerEx.isImsRegistered();
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

    public void setNrSwitch(int phoneId, boolean value) {
        HwTelephonyManagerInner.getDefault().setNrSwitch(phoneId, value);
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

    public int enableVSim(int operation, Bundle bundle) {
        return HwVSimManager.getDefault().enableVSim(operation, bundle);
    }

    public boolean isSupportVSimByOperation(int operation) {
        return HwVSimManager.getDefault().isSupportVSimByOperation(operation);
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
        return HwTelephonyManagerInner.getDefault().getSubscriberId(tm, subId);
    }

    public String getSimCountryIso(TelephonyManager telephonyManager, int subId) {
        return HwTelephonyManagerInner.getDefault().getSimCountryIso(telephonyManager, subId);
    }

    public String getIccAuthentication(TelephonyManager telephonyManager, int subId, int appType, int authType, String data) {
        return HwTelephonyManagerInner.getDefault().getIccAuthentication(telephonyManager, subId, appType, authType, data);
    }

    public static int getNetworkClass(int networkType) {
        return TelephonyManagerEx.getNetworkClass(networkType);
    }

    public static ServiceState getServiceStateForSubscriber(int subId) {
        return HwTelephonyManagerInner.getDefault().getServiceStateForSubscriber(subId);
    }

    public static boolean isWifiCallingAvailable() {
        return TelephonyManagerEx.isWifiCallingAvailable();
    }

    public static String getSimOperator(int subId) {
        return HwTelephonyManagerInner.getDefault().getSimOperator(subId);
    }

    public static String getSimOperatorNumeric() {
        return TelephonyManagerEx.getSimOperatorNumeric();
    }

    public static String getSimOperatorNumericForPhone(int subId) {
        return TelephonyManagerEx.getSimOperatorNumericForPhone(subId);
    }

    public static boolean isMultiSimEnabled() {
        return TelephonyManagerEx.isMultiSimEnabled();
    }

    public static int getSubIdForPhoneAccount(PhoneAccount phoneAccount) {
        return TelephonyManagerEx.getSubIdForPhoneAccount(phoneAccount);
    }

    public static boolean isVideoCallingEnabled() {
        return TelephonyManagerEx.isVideoCallingEnabled();
    }

    public static boolean isDsdaEnabled() {
        return TelephonyManagerEx.getMultiSimConfiguration() == TelephonyManagerEx.MultiSimVariantsExt.DSDA;
    }

    public static boolean isDsdsEnabled() {
        return TelephonyManagerEx.getMultiSimConfiguration() == TelephonyManagerEx.MultiSimVariantsExt.DSDS;
    }

    public static int getDataNetworkType(TelephonyManager telephonyManager, int subId) {
        return HwTelephonyManagerInner.getDefault().getDataNetworkType(telephonyManager, subId);
    }

    public static boolean getDataEnabled(TelephonyManager telephonyManager) {
        return TelephonyManagerEx.getDataEnabled(telephonyManager);
    }

    public boolean sendLaaCmd(int cmd, String reserved, Message response) {
        return HwTelephonyManagerInner.getDefault().sendLaaCmd(cmd, reserved, response);
    }

    public boolean getLaaDetailedState(String reserved, Message response) {
        return HwTelephonyManagerInner.getDefault().getLaaDetailedState(reserved, response);
    }

    public String getMsisdn(TelephonyManager telephonyManager, int subId) {
        return HwTelephonyManagerInner.getDefault().getMsisdn(telephonyManager, subId);
    }

    public int getNetworkType(int subId) {
        return HwTelephonyManagerInner.getDefault().getNetworkType(subId);
    }

    /* renamed from: android.telephony.HwTelephonyManager$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$android$telephony$TelephonyManagerEx$MultiSimVariantsExt = new int[TelephonyManagerEx.MultiSimVariantsExt.values().length];

        static {
            try {
                $SwitchMap$com$huawei$android$telephony$TelephonyManagerEx$MultiSimVariantsExt[TelephonyManagerEx.MultiSimVariantsExt.DSDS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$android$telephony$TelephonyManagerEx$MultiSimVariantsExt[TelephonyManagerEx.MultiSimVariantsExt.DSDA.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$android$telephony$TelephonyManagerEx$MultiSimVariantsExt[TelephonyManagerEx.MultiSimVariantsExt.TSTS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$android$telephony$TelephonyManagerEx$MultiSimVariantsExt[TelephonyManagerEx.MultiSimVariantsExt.UNKNOWN.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public MultiSimVariantsEx getMultiSimConfiguration() {
        int i = AnonymousClass1.$SwitchMap$com$huawei$android$telephony$TelephonyManagerEx$MultiSimVariantsExt[TelephonyManagerEx.getMultiSimConfiguration().ordinal()];
        if (i == 1) {
            return MultiSimVariantsEx.DSDS;
        }
        if (i == 2) {
            return MultiSimVariantsEx.DSDA;
        }
        if (i == 3) {
            return MultiSimVariantsEx.TSTS;
        }
        if (i != 4) {
            return MultiSimVariantsEx.UNKNOWN;
        }
        return MultiSimVariantsEx.UNKNOWN;
    }

    public String getSimSerialNumber(TelephonyManager telephonyManager, int subId) {
        return HwTelephonyManagerInner.getDefault().getSimSerialNumber(telephonyManager, subId);
    }

    public void registerForCallAltSrv(int subId, IPhoneCallback callback) {
        HwTelephonyManagerInner.getDefault().registerForCallAltSrv(subId, callback);
    }

    public void unregisterForCallAltSrv(int subId) {
        HwTelephonyManagerInner.getDefault().unregisterForCallAltSrv(subId);
    }

    public boolean isImeiBindSlotSupported() {
        return HwTelephonyManagerInner.getDefault().isImeiBindSlotSupported();
    }

    public int invokeOemRilRequestRaw(int phoneId, byte[] oemReq, byte[] oemResp) {
        return HwTelephonyManagerInner.getDefault().invokeOemRilRequestRaw(phoneId, oemReq, oemResp);
    }

    public static String getSimOperatorName(int subId) {
        return HwTelephonyManagerInner.getDefault().getSimOperatorName(subId);
    }

    public boolean isCspPlmnEnabled(int subId) {
        return HwTelephonyManagerInner.getDefault().isCspPlmnEnabled(subId);
    }

    public void setNetworkSelectionModeAutomatic(int subId) {
        HwTelephonyManagerInner.getDefault().setNetworkSelectionModeAutomatic(subId);
    }

    public int[] supplyPinReportResultForSubscriber(int subId, String pin) {
        return HwTelephonyManagerInner.getDefault().supplyPinReportResultForSubscriber(subId, pin);
    }

    public int[] supplyPukReportResultForSubscriber(int subId, String puk, String pin) {
        return HwTelephonyManagerInner.getDefault().supplyPukReportResultForSubscriber(subId, puk, pin);
    }

    public void setCallForwardingOption(int subId, int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message response) {
        HwTelephonyManagerInner.getDefault().setCallForwardingOption(subId, commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, response);
    }

    public void getCallForwardingOption(int subId, int commandInterfaceCFReason, Message response) {
        HwTelephonyManagerInner.getDefault().getCallForwardingOption(subId, commandInterfaceCFReason, response);
    }

    public boolean setSubscription(int subId, boolean activate, Message response) {
        return HwTelephonyManagerInner.getDefault().setSubscription(subId, activate, response);
    }

    public String getImsImpu(int subId) {
        return HwTelephonyManagerInner.getDefault().getImsImpu(subId);
    }

    public String getLine1NumberFromImpu(int subId) {
        return HwTelephonyManagerInner.getDefault().getLine1NumberFromImpu(subId);
    }

    public boolean isSecondaryCardGsmOnly() {
        return HwTelephonyManagerInner.getDefault().isSecondaryCardGsmOnly();
    }

    public boolean bindSimToProfile(int slotId) {
        return HwTelephonyManagerInner.getDefault().bindSimToProfile(slotId);
    }

    public boolean setDeepNoDisturbState(int slotId, int state) {
        return HwTelephonyManagerInner.getDefault().setDeepNoDisturbState(slotId, state);
    }

    public boolean setLine1Number(int subId, String alphaTag, String number, Message onComplete) {
        return HwTelephonyManagerInner.getDefault().setLine1Number(subId, alphaTag, number, onComplete);
    }

    public void informModemTetherStatusToChangeGRO(int enable, String faceName) {
        HwTelephonyManagerInner.getDefault().informModemTetherStatusToChangeGRO(enable, faceName);
    }

    public int getPreferredNetworkType(int subId) {
        return HwTelephonyManagerInner.getDefault().getPreferredNetworkType(subId);
    }

    public boolean setPreferredNetworkType(int subId, int networkType) {
        return HwTelephonyManagerInner.getDefault().setPreferredNetworkType(subId, networkType);
    }

    public boolean sendSimMatchedOperatorInfo(int slotId, String opKey, String opName, int state, String reserveField) {
        return HwTelephonyManagerInner.getDefault().sendSimMatchedOperatorInfo(slotId, opKey, opName, state, reserveField);
    }

    public static int checkCarrierPrivileges(String pkgName) {
        return TelephonyManagerEx.checkCarrierPrivilegesForPackageAnyPhone(pkgName);
    }

    public static List<String> getPackagesWithCarrierPrivileges() {
        return TelephonyManagerEx.getPackagesWithCarrierPrivileges();
    }

    public boolean getAntiFakeBaseStation(Message response) {
        return HwTelephonyManagerInner.getDefault().getAntiFakeBaseStation(response);
    }

    public byte[] getCardTrayInfo() {
        return HwTelephonyManagerInner.getDefault().getCardTrayInfo();
    }

    private static class HwPhoneCallback extends PhoneCallback {
        private HwPhoneCallBackInterface mHwPhoneCallBackInterface;

        private HwPhoneCallback() {
            this.mHwPhoneCallBackInterface = null;
        }

        /* synthetic */ HwPhoneCallback(AnonymousClass1 x0) {
            this();
        }

        public void onCallback1(int param) {
            HwPhoneCallBackInterface hwPhoneCallBackInterface = this.mHwPhoneCallBackInterface;
            if (hwPhoneCallBackInterface != null) {
                hwPhoneCallBackInterface.onCallback1(param);
            }
        }

        public void onCallback2(int param1, int param2) {
            HwPhoneCallBackInterface hwPhoneCallBackInterface = this.mHwPhoneCallBackInterface;
            if (hwPhoneCallBackInterface != null) {
                hwPhoneCallBackInterface.onCallback2(param1, param2);
            }
        }

        public void onCallback3(int param1, int param2, Bundle param3) {
            HwPhoneCallBackInterface hwPhoneCallBackInterface = this.mHwPhoneCallBackInterface;
            if (hwPhoneCallBackInterface != null) {
                hwPhoneCallBackInterface.onCallback3(param1, param2, param3);
            }
        }

        public void setHwPhoneCallBack(HwPhoneCallBackInterface callBack) {
            this.mHwPhoneCallBackInterface = callBack;
        }
    }

    public boolean registerForAntiFakeBaseStation(HwPhoneCallBackInterface callback) {
        if (this.mHwPhoneCallback == null) {
            this.mHwPhoneCallback = new HwPhoneCallback(null);
        }
        this.mHwPhoneCallback.setHwPhoneCallBack(callback);
        return HwTelephonyManagerInner.getDefault().registerForAntiFakeBaseStation(this.mHwPhoneCallback.mCallbackStub);
    }

    public boolean unregisterForAntiFakeBaseStation() {
        HwPhoneCallback hwPhoneCallback = this.mHwPhoneCallback;
        if (hwPhoneCallback != null) {
            hwPhoneCallback.setHwPhoneCallBack(null);
        }
        return HwTelephonyManagerInner.getDefault().unregisterForAntiFakeBaseStation();
    }

    public boolean setCsconEnabled(boolean isEnabled) {
        return HwTelephonyManagerInner.getDefault().setCsconEnabled(isEnabled);
    }

    public int[] getCsconEnabled() {
        return HwTelephonyManagerInner.getDefault().getCsconEnabled();
    }

    public boolean isQcomPlatform() {
        return HuaweiTelephonyConfigs.isQcomPlatform();
    }

    public boolean isMTKPlatform() {
        return HuaweiTelephonyConfigs.isMTKPlatform();
    }

    public boolean isHisiPlatform() {
        return HuaweiTelephonyConfigs.isHisiPlatform();
    }

    public void requestDefaultSmdpAddress(String cardId, PendingIntent callbackIntent) {
        HwEuiccManager.requestDefaultSmdpAddress(cardId, callbackIntent);
    }

    public void setDefaultSmdpAddress(String cardId, String defaultSmdpAddress, PendingIntent callbackIntent) {
        HwEuiccManager.setDefaultSmdpAddress(cardId, defaultSmdpAddress, callbackIntent);
    }

    public void resetMemory(String cardId, int options, PendingIntent callbackIntent) {
        HwEuiccManager.resetMemory(cardId, options, callbackIntent);
    }

    public void cancelSession() {
        HwEuiccManager.cancelSession();
    }

    public void updateSubscriptionNickname(EuiccManager euiccManager, int subscriptionId, String nickname, PendingIntent callbackIntent) {
        HwEuiccManager.updateSubscriptionNickname(euiccManager, subscriptionId, nickname, callbackIntent);
    }

    public void continueOperation(EuiccManager euiccManager, Intent resolutionIntent, Bundle resolutionExtras) {
        HwEuiccManager.continueOperation(euiccManager, resolutionIntent, resolutionExtras);
    }

    public int getDataRegisteredState(int phoneId) {
        return HwTelephonyManagerInner.getDefault().getDataRegisteredState(phoneId);
    }

    public int getVoiceRegisteredState(int phoneId) {
        return HwTelephonyManagerInner.getDefault().getVoiceRegisteredState(phoneId);
    }

    public boolean setTemperatureControlToModem(int level, int type, int subId, Message response) {
        return HwTelephonyManagerInner.getDefault().setTemperatureControlToModem(level, type, subId, response);
    }

    public boolean isNrSupported() {
        return HwTelephonyManagerInner.getDefault().isNrSupported();
    }

    public boolean[] getMobilePhysicsLayerStatus(int slotId) {
        return HwTelephonyManagerInner.getDefault().getMobilePhysicsLayerStatus(slotId);
    }

    public int getRrcConnectionState(int slotId) {
        return HwTelephonyManagerInner.getDefault().getRrcConnectionState(slotId);
    }

    public boolean isEuicc(int slotId) {
        return HwTelephonyManagerInner.getDefault().isEuicc(slotId);
    }

    public boolean switchSlots(int[] physicalSlots) {
        return TelephonyManagerEx.switchSlots(physicalSlots);
    }

    public String getCarrierName(DownloadableSubscription downloadableSubscription) {
        if (downloadableSubscription != null) {
            return DownloadableSubscriptionEx.getCarrierName(downloadableSubscription);
        }
        return "";
    }

    public int getProfileClass(SubscriptionInfo subscriptionInfo) {
        if (subscriptionInfo != null) {
            return SubscriptionInfoEx.getProfileClass(subscriptionInfo);
        }
        return -1;
    }

    public void getDownloadableSubscriptionMetadata(EuiccManager euiccManager, DownloadableSubscription subscription, PendingIntent callbackIntent) {
        HwEuiccManager.getDownloadableSubscriptionMetadata(euiccManager, subscription, callbackIntent);
    }

    public void getDefaultDownloadableSubscriptionList(EuiccManager euiccManager, PendingIntent callbackIntent) {
        HwEuiccManager.getDefaultDownloadableSubscriptionList(euiccManager, callbackIntent);
    }

    public boolean setNrOptionMode(int mode, Message msg) {
        return HwTelephonyManagerInner.getDefault().setNrOptionMode(mode, msg);
    }

    public int getNrOptionMode() {
        return HwTelephonyManagerInner.getDefault().getNrOptionMode();
    }

    public boolean isNsaState(int phoneId) {
        return HwTelephonyManagerInner.getDefault().isNsaState(phoneId);
    }

    public NrCellSsbId getNrCellSsbId(int slotId) {
        return HwTelephonyManagerInner.getDefault().getNrCellSsbId(slotId);
    }
}
