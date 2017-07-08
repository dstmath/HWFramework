package android.telephony;

import com.android.internal.telephony.IPhoneCallback;

public class HwTelephonyManager {
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
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    public static final int UNKNOWN_CARD = -1;
    private static HwTelephonyManager sInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.HwTelephonyManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.HwTelephonyManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.HwTelephonyManager.<clinit>():void");
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
}
