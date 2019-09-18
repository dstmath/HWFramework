package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.StringTranslateManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LogException;
import com.android.internal.telephony.AbstractCallManager;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker;
import com.android.internal.telephony.AbstractGsmCdmaConnection;
import com.android.internal.telephony.AbstractGsmCdmaPhone;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.cdma.HwCDMAPhoneReference;
import com.android.internal.telephony.cdma.HwCdmaCallTrackerReference;
import com.android.internal.telephony.cdma.HwCdmaConnectionReferenceImpl;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.HwGSMPhoneReference;
import com.android.internal.telephony.gsm.HwGsmCallTrackerReference;
import com.android.internal.telephony.gsm.HwGsmMmiCode;
import com.android.internal.telephony.imsphone.AbstractImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerReference;
import com.android.internal.telephony.imsphone.HwImsPhoneEx;
import com.android.internal.telephony.imsphone.IHwImsPhoneEx;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import huawei.com.android.internal.telephony.RoamingBroker;
import huawei.cust.HwCustUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class HwPhoneManagerImpl implements HwPhoneManager {
    private static final int CODE_IS_UNSUPPORT_MMI_CODE = 3001;
    private static final String DEFAULT_VM_NUMBER_CUST = SystemProperties.get("ro.voicemail.number", null);
    private static final String DESCRIPTOR = "com.android.ims.internal.IImsConfig";
    private static final boolean IS_SUPPORT_USSI = SystemProperties.getBoolean("ro.config.hw_support_ussi", true);
    private static final boolean IS_VSIM_SUPPORT = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private static final String LOG_TAG = "HwPhoneManagerImpl";
    private static final String MDN_NUMBER_CDMA = "mdn_number_key_cdma";
    private static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    private static final boolean SET_MDN_AS_VM_NUMBER = SystemProperties.getBoolean("ro.config.hw_setMDNasVMnum", false);
    protected static final String VM_NUMBER_CDMA = "vm_number_key_cdma";
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static HwPhoneProxyReference mHwPhoneProxyReference;
    private static HwPhoneManager mInstance = new HwPhoneManagerImpl();
    private static LogException mLogException = HwFrameworkFactory.getLogException();
    private static HwPhoneManager.PhoneServiceInterface phoneService = null;
    private static PhoneSubInfoUtils phoneSubInfoUtils = new PhoneSubInfoUtils();
    private ArrayList<HashMap<String, PhoneConstants.DataState>> mApnTypeAndDataState = null;
    Object mCust = HwCustUtils.createObj(HwCustPhoneManager.class, new Object[0]);
    private int mNumPhones;

    public static HwPhoneManager getDefault() {
        return mInstance;
    }

    private static void setHwPhoneProxyReference(HwPhoneProxyReference obj) {
        mHwPhoneProxyReference = obj;
    }

    public AbstractPhoneBase.HwPhoneBaseReference createHwPhoneBaseReference(AbstractPhoneBase phoneBase) {
        return new HwPhoneBaseReferenceImpl(phoneBase);
    }

    public AbstractGsmCdmaPhone.GSMPhoneReference createHwGSMPhoneReference(AbstractGsmCdmaPhone gsmPhone) {
        return new HwGSMPhoneReference((GsmCdmaPhone) gsmPhone);
    }

    public AbstractGsmCdmaPhone.CDMAPhoneReference createHwCDMAPhoneReference(AbstractGsmCdmaPhone cdmaPhone) {
        return new HwCDMAPhoneReference((GsmCdmaPhone) cdmaPhone);
    }

    public AbstractGsmCdmaCallTracker.CdmaCallTrackerReference createHwCdmaCallTrackerReference(AbstractGsmCdmaCallTracker cdmaCallTracker) {
        return new HwCdmaCallTrackerReference((GsmCdmaCallTracker) cdmaCallTracker);
    }

    public AbstractGsmCdmaCallTracker.GsmCallTrackerReference createHwGsmCallTrackerReference(AbstractGsmCdmaCallTracker gsmCallTracker) {
        return new HwGsmCallTrackerReference((GsmCdmaCallTracker) gsmCallTracker);
    }

    public boolean changeMMItoUSSD(GsmCdmaPhone phone, String poundString) {
        return getCust().changeMMItoUSSD(phone, poundString);
    }

    public AbstractImsPhoneCallTracker.ImsPhoneCallTrackerReference createHwImsPhoneCallTrackerReference(AbstractImsPhoneCallTracker imsPhoneCallTracker) {
        return new HwImsPhoneCallTrackerReference((ImsPhoneCallTracker) imsPhoneCallTracker);
    }

    public AbstractCallManager.CallManagerReference createHwCallManagerReference(AbstractCallManager cm) {
        return new HwCallManagerReference((CallManager) cm);
    }

    public String getCDMAVoiceMailNumberHwCust(Context context, String line1Number, int phoneId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String vmNumberKey = VM_NUMBER_CDMA + phoneId;
        if (!DEFAULT_VM_NUMBER_CUST.equals("")) {
            return sp.getString(vmNumberKey, DEFAULT_VM_NUMBER_CUST);
        }
        if (!SET_MDN_AS_VM_NUMBER) {
            return sp.getString(vmNumberKey, "*86");
        }
        String MdnNumber = line1Number;
        String OldMdnNumber = sp.getString(MDN_NUMBER_CDMA, "");
        SharedPreferences.Editor editor = sp.edit();
        if (MdnNumber == null) {
            return sp.getString(vmNumberKey, "");
        }
        if (OldMdnNumber.equals("")) {
            editor.putString(MDN_NUMBER_CDMA, MdnNumber);
            editor.commit();
        }
        if (!MdnNumber.equals(OldMdnNumber)) {
            editor.putString(MDN_NUMBER_CDMA, MdnNumber);
            editor.putString(vmNumberKey, MdnNumber);
            editor.commit();
        }
        return sp.getString(vmNumberKey, MdnNumber);
    }

    private static void setPhoneService(HwPhoneManager.PhoneServiceInterface obj) {
        phoneService = obj;
    }

    public void initHwTimeZoneUpdater(Context context) {
        new HwTimeZoneUpdater(context);
    }

    public void loadHuaweiPhoneService(Phone phone, Context context) {
        try {
            Class HuaweiPhoneServiceClass = Class.forName("com.android.internal.telephony.HwPhoneService");
            HwFrameworkFactory.getHwInnerTelephonyManager().updateSigCustInfoFromXML(context);
            setPhoneService(HuaweiPhoneServiceClass.newInstance());
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
            return new RIL(context, networkMode, cdmaSubscription, instanceId);
        } catch (IllegalAccessException e2) {
            Rlog.d(LOG_TAG, "IllegalAccessException ");
            return new RIL(context, networkMode, cdmaSubscription, instanceId);
        } catch (ClassNotFoundException e3) {
            Rlog.d(LOG_TAG, "ClassNotFoundException ");
            return new RIL(context, networkMode, cdmaSubscription, instanceId);
        } catch (NoSuchMethodException e4) {
            Rlog.d(LOG_TAG, "NoSuchMethodException ");
            return new RIL(context, networkMode, cdmaSubscription, instanceId);
        } catch (InvocationTargetException e5) {
            Rlog.d(LOG_TAG, "InvocationTargetException ");
            return new RIL(context, networkMode, cdmaSubscription, instanceId);
        }
    }

    public void loadHuaweiPhoneService(Phone[] phones, Context context) {
        try {
            Class HuaweiPhoneServiceClass = Class.forName("com.android.internal.telephony.HwPhoneService");
            HwFrameworkFactory.getHwInnerTelephonyManager().updateSigCustInfoFromXML(context);
            setPhoneService(HuaweiPhoneServiceClass.newInstance());
            phoneService.setPhone(phones, context);
            initApnTypeAndDataState();
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
            if (!VirtualNet.isVirtualNet(slotId) || roaming || hplmn == null || !VirtualNet.getCurrentVirtualNet(slotId).validNetConfig() || hasNitzOperatorName) {
                return plmnValue;
            }
            String plmnValue2 = VirtualNet.getCurrentVirtualNet(slotId).getOperatorName();
            Rlog.d(LOG_TAG, "getVirtualNetOperatorName: plmnValue = " + plmnValue2 + " slotId = " + slotId);
            return plmnValue2;
        } else if (!VirtualNet.isVirtualNet() || roaming || hplmn == null || !VirtualNet.getCurrentVirtualNet().validNetConfig() || hasNitzOperatorName) {
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
        return getCust().isStringHuaweiIgnoreCode(phone, dialString) || HwGsmMmiCode.isStringHuaweiIgnoreCode(dialString);
    }

    public String getPesn(AbstractPhoneSubInfo abstractPhoneSubInfo) {
        phoneSubInfoUtils.getContext((PhoneSubInfoController) abstractPhoneSubInfo).enforceCallingOrSelfPermission(READ_PHONE_STATE, "Requires READ_PHONE_STATE");
        return phoneSubInfoUtils.getPhone((PhoneSubInfoController) abstractPhoneSubInfo, PhoneFactory.getDefaultSubscription()).getPesn();
    }

    public AbstractGsmCdmaConnection.HwCdmaConnectionReference createHwCdmaConnectionReference(AbstractGsmCdmaConnection abstractCdmaConnection) {
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
        StringBuilder sb = new StringBuilder(256);
        sb.append("Package:");
        sb.append("telephony");
        sb.append("\n");
        sb.append("APK version:");
        sb.append("001");
        sb.append("\n");
        sb.append("Bug type:");
        sb.append(bugType);
        mLogException.msg("framework", 67, sb.append("\n").toString(), new StringBuilder(512).append(bodyMsg).toString());
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

    public int showMmiError(int sc, int slotid) {
        return HwGsmMmiCode.showMmiError(sc, slotid);
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

    public boolean processSendUssdInImsCall(GsmMmiCode gsmMmiCode, Phone imsPhone) {
        return HwGsmMmiCode.processSendUssdInImsCall(gsmMmiCode, imsPhone);
    }

    public boolean shouldRunUtIgnoreCSService(Phone phone, boolean isUt) {
        if (!isUt || phone == null || !phone.isImsRegistered() || phone.getContext() == null) {
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
        String ignoreCSServiceByMccMnc = Settings.System.getString(phone.getContext().getContentResolver(), "plmn_ut_ignore_cs_service");
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

    public boolean needUnEscapeHtmlforUssdMsg(Phone phone) {
        return HwGsmMmiCode.needUnEscapeHtmlforUssdMsg(phone);
    }

    public String unEscapeHtml4(String str) {
        StringTranslateManager.getDefault();
        return StringTranslateManager.unEscapeHtml4(str);
    }

    public void encryptInfo(Context context, String sensitiveInfo, String encryptTag) {
        if (context == null || sensitiveInfo == null || encryptTag == null) {
            Rlog.d(LOG_TAG, "encryptInfo: one or some params is null, do nothing.");
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        try {
            sensitiveInfo = HwAESCryptoUtil.encrypt(HwFullNetworkConstants.MASTER_PASSWORD, sensitiveInfo);
        } catch (Exception ex) {
            Rlog.d(LOG_TAG, "HwAESCryptoUtil encrypt excepiton:" + ex.getMessage());
        }
        editor.putString(encryptTag, sensitiveInfo);
        editor.apply();
    }

    public String decryptInfo(Context context, String encryptTag) {
        if (context == null || encryptTag == null) {
            Rlog.d(LOG_TAG, "decryptInfo: context or encryptTag is null, do nothing.");
            return null;
        }
        String sensitiveInfo = PreferenceManager.getDefaultSharedPreferences(context).getString(encryptTag, "");
        if (!"".equals(sensitiveInfo)) {
            try {
                sensitiveInfo = HwAESCryptoUtil.decrypt(HwFullNetworkConstants.MASTER_PASSWORD, sensitiveInfo);
            } catch (Exception ex) {
                Rlog.d(LOG_TAG, "HwAESCryptoUtil decrypt excepiton:" + ex.getMessage());
            }
        }
        return sensitiveInfo;
    }

    public void checkMMICode(String mmiCode, int phoneId) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("ims_config");
        Rlog.d(LOG_TAG, "checkMMICode");
        boolean z = false;
        boolean isUnSupport = false;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(phoneId);
                _data.writeString(mmiCode);
                b.transact(CODE_IS_UNSUPPORT_MMI_CODE, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() == 1) {
                    z = true;
                }
                isUnSupport = z;
            } catch (RemoteException localRemoteException) {
                Rlog.e(LOG_TAG, "RemoteException is " + localRemoteException);
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        } else {
            Rlog.e(LOG_TAG, "checkMMICode - can't get ims_config service");
        }
        _reply.recycle();
        _data.recycle();
        if (isUnSupport) {
            Rlog.d(LOG_TAG, "Not Support MMI Code=" + mmiCode);
            throw new RuntimeException("Invalid or Unsupported MMI Code");
        }
    }

    private void initApnTypeAndDataState() {
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        if (IS_VSIM_SUPPORT && 2 == numPhones) {
            numPhones++;
        }
        this.mNumPhones = numPhones;
        this.mApnTypeAndDataState = new ArrayList<>();
        for (int i = 0; i < numPhones; i++) {
            this.mApnTypeAndDataState.add(i, new HashMap());
        }
    }

    private boolean validatePhoneId(int phoneId) {
        return phoneId >= 0 && phoneId < this.mNumPhones;
    }

    public boolean checkApnShouldNotify(int subId, String apnType, PhoneConstants.DataState state) {
        if ("default".equals(apnType)) {
            return true;
        }
        int phoneId = SubscriptionManager.getPhoneId(subId);
        if (validatePhoneId(phoneId) && this.mApnTypeAndDataState.get(phoneId) != null) {
            if (!this.mApnTypeAndDataState.get(phoneId).containsKey(apnType)) {
                this.mApnTypeAndDataState.get(phoneId).put(apnType, state);
                return false;
            } else if (state != this.mApnTypeAndDataState.get(phoneId).get(apnType)) {
                this.mApnTypeAndDataState.get(phoneId).put(apnType, state);
                return true;
            }
        }
        return false;
    }

    public IHwImsPhoneEx createHwImsPhoneEx(ImsPhoneCallTracker imsCT) {
        return new HwImsPhoneEx(imsCT);
    }

    public IHwGsmCdmaPhoneEx createHwGsmCdmaPhoneEx(IHwGsmCdmaPhoneInner gsmCdmaPhoneInner) {
        if (gsmCdmaPhoneInner == null) {
            return null;
        }
        return new HwGsmCdmaPhoneEx(gsmCdmaPhoneInner);
    }

    public boolean isShortCodeHw(String dialString, GsmCdmaPhone phone) {
        return HwGsmMmiCode.isShortCodeHw(dialString, phone);
    }

    public int removeUssdCust(GsmCdmaPhone phone) {
        return HwGsmMmiCode.removeUssdCust(phone);
    }
}
