package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LogException;
import com.android.internal.telephony.AbstractCallManager.CallManagerReference;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker.CdmaCallTrackerReference;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker.GsmCallTrackerReference;
import com.android.internal.telephony.AbstractGsmCdmaConnection.HwCdmaConnectionReference;
import com.android.internal.telephony.AbstractGsmCdmaPhone.CDMAPhoneReference;
import com.android.internal.telephony.AbstractGsmCdmaPhone.GSMPhoneReference;
import com.android.internal.telephony.AbstractPhoneBase.HwPhoneBaseReference;
import com.android.internal.telephony.HwPhoneManager.PhoneServiceInterface;
import com.android.internal.telephony.cdma.HwCDMAPhoneReference;
import com.android.internal.telephony.cdma.HwCdmaCallTrackerReference;
import com.android.internal.telephony.cdma.HwCdmaConnectionReferenceImpl;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.HwGSMPhoneReference;
import com.android.internal.telephony.gsm.HwGsmCallTrackerReference;
import com.android.internal.telephony.gsm.HwGsmMmiCode;
import com.android.internal.telephony.imsphone.AbstractImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.AbstractImsPhoneCallTracker.ImsPhoneCallTrackerReference;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerReference;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import huawei.com.android.internal.telephony.RoamingBroker;
import huawei.cust.HwCustUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class HwPhoneManagerImpl implements HwPhoneManager {
    private static final String DEFAULT_VM_NUMBER_CUST = SystemProperties.get("ro.voicemail.number", null);
    private static final boolean IS_SUPPORT_USSI = SystemProperties.getBoolean("ro.config.hw_support_ussi", false);
    private static final String LOG_TAG = "HwPhoneManagerImpl";
    private static final String MDN_NUMBER_CDMA = "mdn_number_key_cdma";
    private static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    private static final boolean SET_MDN_AS_VM_NUMBER = SystemProperties.getBoolean("ro.config.hw_setMDNasVMnum", false);
    protected static final String VM_NUMBER_CDMA = "vm_number_key_cdma";
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static HwPhoneProxyReference mHwPhoneProxyReference;
    private static HwPhoneManager mInstance = new HwPhoneManagerImpl();
    private static LogException mLogException = HwFrameworkFactory.getLogException();
    private static PhoneServiceInterface phoneService = null;
    private static PhoneSubInfoUtils phoneSubInfoUtils = new PhoneSubInfoUtils();
    Object mCust = HwCustUtils.createObj(HwCustPhoneManager.class, new Object[0]);

    public static HwPhoneManager getDefault() {
        return mInstance;
    }

    private static void setHwPhoneProxyReference(HwPhoneProxyReference obj) {
        mHwPhoneProxyReference = obj;
    }

    public HwPhoneBaseReference createHwPhoneBaseReference(AbstractPhoneBase phoneBase) {
        return new HwPhoneBaseReferenceImpl(phoneBase);
    }

    public GSMPhoneReference createHwGSMPhoneReference(AbstractGsmCdmaPhone gsmPhone) {
        return new HwGSMPhoneReference((GsmCdmaPhone) gsmPhone);
    }

    public CDMAPhoneReference createHwCDMAPhoneReference(AbstractGsmCdmaPhone cdmaPhone) {
        return new HwCDMAPhoneReference((GsmCdmaPhone) cdmaPhone);
    }

    public CdmaCallTrackerReference createHwCdmaCallTrackerReference(AbstractGsmCdmaCallTracker cdmaCallTracker) {
        return new HwCdmaCallTrackerReference((GsmCdmaCallTracker) cdmaCallTracker);
    }

    public GsmCallTrackerReference createHwGsmCallTrackerReference(AbstractGsmCdmaCallTracker gsmCallTracker) {
        return new HwGsmCallTrackerReference((GsmCdmaCallTracker) gsmCallTracker);
    }

    public boolean changeMMItoUSSD(GsmCdmaPhone phone, String poundString) {
        return getCust().changeMMItoUSSD(phone, poundString);
    }

    public ImsPhoneCallTrackerReference createHwImsPhoneCallTrackerReference(AbstractImsPhoneCallTracker imsPhoneCallTracker) {
        return new HwImsPhoneCallTrackerReference((ImsPhoneCallTracker) imsPhoneCallTracker);
    }

    public CallManagerReference createHwCallManagerReference(AbstractCallManager cm) {
        return new HwCallManagerReference((CallManager) cm);
    }

    public String getCDMAVoiceMailNumberHwCust(Context context, String line1Number, int phoneId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String vmNumberKey = VM_NUMBER_CDMA;
        vmNumberKey = VM_NUMBER_CDMA + phoneId;
        if (!DEFAULT_VM_NUMBER_CUST.equals("")) {
            return sp.getString(vmNumberKey, DEFAULT_VM_NUMBER_CUST);
        }
        if (!SET_MDN_AS_VM_NUMBER) {
            return sp.getString(vmNumberKey, "*86");
        }
        String MdnNumber = line1Number;
        String OldMdnNumber = sp.getString(MDN_NUMBER_CDMA, "");
        Editor editor = sp.edit();
        if (line1Number == null) {
            return sp.getString(vmNumberKey, "");
        }
        if (OldMdnNumber.equals("")) {
            editor.putString(MDN_NUMBER_CDMA, line1Number);
            editor.commit();
        }
        if (!line1Number.equals(OldMdnNumber)) {
            editor.putString(MDN_NUMBER_CDMA, line1Number);
            editor.putString(vmNumberKey, line1Number);
            editor.commit();
        }
        return sp.getString(vmNumberKey, line1Number);
    }

    private static void setPhoneService(PhoneServiceInterface obj) {
        phoneService = obj;
    }

    public void initHwTimeZoneUpdater(Context context) {
        HwTimeZoneUpdater hwTimeZoneUpdater = new HwTimeZoneUpdater(context);
    }

    public void loadHuaweiPhoneService(Phone phone, Context context) {
        try {
            Class HuaweiPhoneServiceClass = Class.forName("com.android.internal.telephony.HwPhoneService");
            HwFrameworkFactory.getHwInnerTelephonyManager().updateSigCustInfoFromXML(context);
            setPhoneService((PhoneServiceInterface) HuaweiPhoneServiceClass.newInstance());
            phoneService.setPhone(phone, context);
        } catch (InstantiationException e) {
            Rlog.d(LOG_TAG, "InstantiationException ");
        } catch (IllegalAccessException e2) {
            Rlog.d(LOG_TAG, "IllegalAccessException ");
        } catch (ClassNotFoundException e3) {
            Rlog.d(LOG_TAG, "ClassNotFoundException ");
        }
    }

    public <T> T createHwRil(Context context, int networkMode, int cdmaSubscription, Integer instanceId) {
        try {
            String sRILClassname = SystemProperties.get("ro.telephony.ril_class", "HwHisiRIL").trim();
            Rlog.i(LOG_TAG, "RILClassname is " + sRILClassname);
            Class<?> clazz = Class.forName("com.android.internal.telephony." + sRILClassname);
            return clazz.cast(clazz.getConstructor(new Class[]{Context.class, Integer.TYPE, Integer.TYPE, Integer.class}).newInstance(new Object[]{context, Integer.valueOf(networkMode), Integer.valueOf(cdmaSubscription), instanceId}));
        } catch (InstantiationException e) {
            Rlog.d(LOG_TAG, "InstantiationException ");
        } catch (IllegalAccessException e2) {
            Rlog.d(LOG_TAG, "IllegalAccessException ");
        } catch (ClassNotFoundException e3) {
            Rlog.d(LOG_TAG, "ClassNotFoundException ");
        } catch (NoSuchMethodException e4) {
            Rlog.d(LOG_TAG, "NoSuchMethodException ");
        } catch (InvocationTargetException e5) {
            Rlog.d(LOG_TAG, "InvocationTargetException ");
        }
        return new RIL(context, networkMode, cdmaSubscription, instanceId);
    }

    public void loadHuaweiPhoneService(Phone[] phones, Context context) {
        try {
            Class HuaweiPhoneServiceClass = Class.forName("com.android.internal.telephony.HwPhoneService");
            HwFrameworkFactory.getHwInnerTelephonyManager().updateSigCustInfoFromXML(context);
            setPhoneService((PhoneServiceInterface) HuaweiPhoneServiceClass.newInstance());
            phoneService.setPhone(phones, context);
        } catch (InstantiationException e) {
            Rlog.d(LOG_TAG, "InstantiationException ");
        } catch (IllegalAccessException e2) {
            Rlog.d(LOG_TAG, "IllegalAccessException ");
        } catch (ClassNotFoundException e3) {
            Rlog.d(LOG_TAG, "ClassNotFoundException ");
        }
    }

    public String custTimeZoneForMcc(int mcc) {
        return HwMccTable.custTimeZoneForMcc(mcc);
    }

    public String custCountryCodeForMcc(int mcc) {
        return HwMccTable.custCountryCodeForMcc(mcc);
    }

    public String custLanguageForMcc(int mcc) {
        return HwMccTable.custLanguageForMcc(mcc);
    }

    public int custSmallestDigitsMccForMnc(int mcc) {
        return HwMccTable.custSmallestDigitsMccForMnc(mcc);
    }

    public boolean useAutoSimLan(Context context) {
        return HwMccTable.useAutoSimLan(context);
    }

    public Locale getSpecialLoacleConfig(Context context, int mcc) {
        return HwMccTable.getSpecialLoacleConfig(context, mcc);
    }

    public String getBetterMatchLocale(Context context, String language, String country, String bestMatch) {
        return HwMccTable.getBetterMatchLocale(context, language, country, bestMatch);
    }

    public Locale getBetterMatchLocale(Context context, String language, String script, String country, Locale bestMatch) {
        return HwMccTable.getBetterMatchLocale(context, language, script, country, bestMatch);
    }

    public String custScriptForMcc(int mcc) {
        return HwMccTable.custScriptForMcc(mcc);
    }

    public void setMccTableImsi(String imsi) {
        HwMccTable.setImsi(imsi);
    }

    public void setMccTableIccId(String iccid) {
        HwMccTable.setIccId(iccid);
    }

    public void setMccTableMnc(int mnc) {
        HwMccTable.setMnc(mnc);
    }

    public void setGsmCdmaPhone(GsmCdmaPhone phoneProxy, Context context) {
        setHwPhoneProxyReference(new HwPhoneProxyReference(phoneProxy, context));
    }

    public HwPhoneProxyReference getDefaultPhone() {
        return mHwPhoneProxyReference;
    }

    public String getVirtualNetOperatorName(String plmnValue, boolean roaming, boolean hasNitzOperatorName, int slotId, String hplmn) {
        if (isMultiSimEnabled) {
            if (!VirtualNet.isVirtualNet(slotId) || (roaming ^ 1) == 0 || hplmn == null || !VirtualNet.getCurrentVirtualNet(slotId).validNetConfig() || hasNitzOperatorName) {
                return plmnValue;
            }
            plmnValue = VirtualNet.getCurrentVirtualNet(slotId).getOperatorName();
            Rlog.d(LOG_TAG, "getVirtualNetOperatorName: plmnValue = " + plmnValue + " slotId = " + slotId);
            return plmnValue;
        } else if (!VirtualNet.isVirtualNet() || (roaming ^ 1) == 0 || hplmn == null || !VirtualNet.getCurrentVirtualNet().validNetConfig() || hasNitzOperatorName) {
            return plmnValue;
        } else {
            return VirtualNet.getCurrentVirtualNet().getOperatorName();
        }
    }

    public void setRoamingBrokerIccId(String iccId) {
        RoamingBroker.getDefault().setIccId(iccId);
    }

    public void setRoamingBrokerOperator(String plmn) {
        RoamingBroker.getDefault().setOperator(plmn);
    }

    public boolean isVirtualNet() {
        return VirtualNet.isVirtualNet();
    }

    public String getVirtualNetNumeric() {
        if (VirtualNet.isVirtualNet()) {
            return VirtualNet.getCurrentVirtualNet().getNumeric();
        }
        return null;
    }

    public String getVirtualNetVoiceMailNumber() {
        if (VirtualNet.isVirtualNet()) {
            return VirtualNet.getCurrentVirtualNet().getVoiceMailNumber();
        }
        return null;
    }

    public String getVirtualNetVoicemailTag() {
        if (VirtualNet.isVirtualNet()) {
            return VirtualNet.getCurrentVirtualNet().getVoicemailTag();
        }
        return null;
    }

    public boolean isRoamingBrokerActivated() {
        return RoamingBroker.isRoamingBrokerActivated();
    }

    public String getRoamingBrokerVoicemail() {
        return RoamingBroker.getRBVoicemail();
    }

    public String getRoamingBrokerOperatorNumeric() {
        return RoamingBroker.getRBOperatorNumeric();
    }

    public void loadVirtualNetSpecialFiles(String plmn, SIMRecords simRecords) {
        VirtualNet.loadSpecialFiles(plmn, simRecords);
    }

    public void loadVirtualNet(String plmn, SIMRecords simRecords) {
        VirtualNet.loadVirtualNet(plmn, simRecords);
    }

    public void addVirtualNetSpecialFile(String filePath, String fileId, byte[] bytes) {
        VirtualNet.addSpecialFile(filePath, fileId, bytes);
    }

    public void addVirtualNetSpecialFile(String filePath, String fileId, byte[] bytes, int slotId) {
        VirtualNet.addSpecialFile(filePath, fileId, bytes, slotId);
    }

    public void saveUiccCardsToVirtualNet(UiccCard[] uiccCards) {
        VirtualNet.saveUiccCardsToVirtualNet(uiccCards);
    }

    public boolean isDefaultTimezone() {
        return HwMccTable.isDefaultTimezone;
    }

    public void changedDefaultTimezone() {
        HwMccTable.setDefaultTimezone(false);
    }

    public void setDefaultTimezone(Context context) {
        HwMccTable.setDefaultTimezone(context);
    }

    public boolean isShortCodeCustomization() {
        return HwGsmMmiCode.isShortCodeCustomization();
    }

    public String convertUssdMessage(String ussdMessage) {
        return HwGsmMmiCode.convertUssdMessage(ussdMessage);
    }

    public int siToServiceClass(String si) {
        return HwGsmMmiCode.siToServiceClass(si);
    }

    public boolean isStringHuaweiCustCode(String dialString) {
        return HwGsmMmiCode.isStringHuaweiCustCode(dialString);
    }

    public boolean isStringHuaweiIgnoreCode(GsmCdmaPhone phone, String dialString) {
        if (getCust().isStringHuaweiIgnoreCode(phone, dialString)) {
            return true;
        }
        return HwGsmMmiCode.isStringHuaweiIgnoreCode(dialString);
    }

    public String getPesn(AbstractPhoneSubInfo abstractPhoneSubInfo) {
        phoneSubInfoUtils.getContext((PhoneSubInfoController) abstractPhoneSubInfo).enforceCallingOrSelfPermission(READ_PHONE_STATE, "Requires READ_PHONE_STATE");
        return phoneSubInfoUtils.getPhone((PhoneSubInfoController) abstractPhoneSubInfo, PhoneFactory.getDefaultSubscription()).getPesn();
    }

    public HwCdmaConnectionReference createHwCdmaConnectionReference(AbstractGsmCdmaConnection abstractCdmaConnection) {
        return new HwCdmaConnectionReferenceImpl((GsmCdmaConnection) abstractCdmaConnection);
    }

    public HwCustPhoneManager getCust() {
        return (HwCustPhoneManager) this.mCust;
    }

    public boolean shouldSkipUpdateMccMnc(String mccmnc) {
        return HwMccTable.shouldSkipUpdateMccMnc(mccmnc);
    }

    public void setRoamingBrokerIccId(String iccId, int simId) {
        RoamingBroker.getDefault(Integer.valueOf(simId)).setIccId(iccId);
    }

    public void setRoamingBrokerImsi(String imsi) {
        RoamingBroker.getDefault().setImsi(imsi);
    }

    public String getRoamingBrokerImsi() {
        return RoamingBroker.getDefault().getRBImsi();
    }

    public void setRoamingBrokerImsi(String imsi, Integer simId) {
        RoamingBroker.getDefault(simId).setImsi(imsi);
    }

    public String getRoamingBrokerImsi(Integer simId) {
        return RoamingBroker.getDefault(simId).getRBImsi();
    }

    public boolean isRoamingBrokerActivated(Integer simId) {
        return RoamingBroker.getDefault(simId).isRoamingBrokerActivated(simId);
    }

    public String getRoamingBrokerOperatorNumeric(Integer simId) {
        return RoamingBroker.getDefault(simId).getRBOperatorNumeric(simId);
    }

    public String updateSelectionForRoamingBroker(String selection, int slotId) {
        return RoamingBroker.getDefault(Integer.valueOf(slotId)).updateSelectionForRoamingBroker(selection, slotId);
    }

    public String updateSelectionForRoamingBroker(String selection) {
        RoamingBroker.getDefault();
        return RoamingBroker.updateSelectionForRoamingBroker(selection);
    }

    public void setRoamingBrokerOperator(String plmn, int simId) {
        RoamingBroker.getDefault(Integer.valueOf(simId)).setOperator(plmn);
    }

    public String getRoamingBrokerVoicemail(int simId) {
        return RoamingBroker.getDefault(Integer.valueOf(simId)).getRBVoicemail(Integer.valueOf(simId));
    }

    public void triggerLogRadar(String bugType, String bodyMsg) {
        mLogException.msg("framework", 67, "Package:" + "telephony" + "\n" + "APK version:" + "001" + "\n" + "Bug type:" + bugType + "\n", bodyMsg);
    }

    public CharSequence processgoodPinString(Context context, String sc) {
        return HwGsmMmiCode.processgoodPinString(context, sc);
    }

    public CharSequence processBadPinString(Context context, String sc) {
        return HwGsmMmiCode.processBadPinString(context, sc);
    }

    public int handlePasswordError(String sc) {
        return HwGsmMmiCode.handlePasswordError(sc);
    }

    public int showMmiError(int sc) {
        return HwGsmMmiCode.showMmiError(sc);
    }

    public boolean processImsPhoneMmiCode(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        return HwGsmMmiCode.processImsPhoneMmiCode(gsmMmiCode, imsPhone);
    }

    public void handleMessageGsmMmiCode(GsmMmiCode gsmMmiCode, Message msg) {
        HwGsmMmiCode.handleMessageGsmMmiCode(gsmMmiCode, msg);
    }

    public String getVirtualNetEccWihCard(int slotId) {
        return getCust().getVirtualNetEccWihCard(slotId);
    }

    public String getVirtualNetEccNoCard(int slotId) {
        return getCust().getVirtualNetEccNoCard(slotId);
    }

    public boolean isSupportEccFormVirtualNet() {
        return getCust().isSupportEccFormVirtualNet();
    }

    public boolean isSupportOrangeApn(Phone phone) {
        return getCust().isSupportOrangeApn(phone);
    }

    public void addSpecialAPN(Phone phone) {
        getCust().addSpecialAPN(phone);
    }

    public CharSequence getCallForwardingString(Context context, String sc) {
        return HwGsmMmiCode.getCallForwardingString(context, sc);
    }

    public RadioIndication createHwRadioIndication(RIL ril) {
        return new HwRadioIndication(ril);
    }

    public RadioResponse createHwRadioResponse(RIL ril) {
        return new HwRadioResponse(ril);
    }

    public boolean shouldRunUtIgnoreCSService(Phone phone, boolean isUt) {
        if (!isUt || phone == null || (phone.isImsRegistered() ^ 1) != 0 || phone.getContext() == null) {
            return false;
        }
        if (IS_SUPPORT_USSI) {
            return true;
        }
        if (phone.mIccRecords == null || phone.mIccRecords.get() == null) {
            return false;
        }
        String hplmn = ((IccRecords) phone.mIccRecords.get()).getOperatorNumeric();
        if (TextUtils.isEmpty(hplmn)) {
            return false;
        }
        String ignoreCSServiceByMccMnc = System.getString(phone.getContext().getContentResolver(), "plmn_ut_ignore_cs_service");
        if (TextUtils.isEmpty(ignoreCSServiceByMccMnc)) {
            return false;
        }
        for (String mccmnc : ignoreCSServiceByMccMnc.split(",")) {
            if (hplmn.equals(mccmnc)) {
                return true;
            }
        }
        return false;
    }
}
