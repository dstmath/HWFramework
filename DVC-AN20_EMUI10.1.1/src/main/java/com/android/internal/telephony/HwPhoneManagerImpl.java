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
import android.telephony.StringTranslateManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LogException;
import com.android.internal.telephony.AbstractCallManager;
import com.android.internal.telephony.AbstractGsmCdmaCallTracker;
import com.android.internal.telephony.AbstractGsmCdmaConnection;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.cdma.HwCdmaCallTrackerReference;
import com.android.internal.telephony.cdma.HwCdmaConnectionReferenceImpl;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.HwGsmCallTrackerReference;
import com.android.internal.telephony.gsm.HwGsmMmiCode;
import com.android.internal.telephony.imsphone.AbstractImsPhoneCallTracker;
import com.android.internal.telephony.imsphone.HwImsPhoneCallTrackerReference;
import com.android.internal.telephony.imsphone.HwImsPhoneEx;
import com.android.internal.telephony.imsphone.IHwImsPhoneEx;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.uicc.IccRecords;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;
import huawei.com.android.internal.telephony.RoamingBroker;
import huawei.cust.HwCustUtils;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class HwPhoneManagerImpl implements HwPhoneManager {
    private static final int CODE_IS_UNSUPPORT_MMI_CODE = 3001;
    private static final String DEFAULT_VM_NUMBER_CUST = SystemProperties.get("ro.voicemail.number", (String) null);
    private static final String DESCRIPTOR = "com.android.ims.internal.IImsConfig";
    private static final boolean IS_SUPPORT_USSI = SystemProperties.getBoolean("ro.config.hw_support_ussi", true);
    private static final boolean IS_VSIM_SUPPORT = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private static final String LOG_TAG = "HwPhoneManagerImpl";
    private static final String MDN_NUMBER_CDMA = "mdn_number_key_cdma";
    private static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    private static final boolean SET_MDN_AS_VM_NUMBER = SystemProperties.getBoolean("ro.config.hw_setMDNasVMnum", false);
    protected static final String VM_NUMBER_CDMA = "vm_number_key_cdma";
    private static HwPhoneProxyReference mHwPhoneProxyReference;
    private static HwPhoneManager mInstance = new HwPhoneManagerImpl();
    private static LogException mLogException = HwFrameworkFactory.getLogException();
    private static HwPhoneManager.PhoneServiceInterface phoneService = null;
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
        String oldMdnNumber = sp.getString(MDN_NUMBER_CDMA, "");
        SharedPreferences.Editor editor = sp.edit();
        if (line1Number == null) {
            return sp.getString(vmNumberKey, "");
        }
        if (oldMdnNumber.equals("")) {
            editor.putString(MDN_NUMBER_CDMA, line1Number);
            editor.commit();
        }
        if (!line1Number.equals(oldMdnNumber)) {
            editor.putString(MDN_NUMBER_CDMA, line1Number);
            editor.putString(vmNumberKey, line1Number);
            editor.commit();
        }
        return sp.getString(vmNumberKey, line1Number);
    }

    private static void setPhoneService(HwPhoneManager.PhoneServiceInterface obj) {
        phoneService = obj;
    }

    public void initHwTimeZoneUpdater(Context context) {
        new HwTimeZoneUpdater(context);
    }

    public void loadHuaweiPhoneService(Phone phone, Context context) {
    }

    public <T> T createHwRil(Context context, int networkMode, int cdmaSubscription, Integer instanceId) {
        try {
            String sRILClassname = SystemProperties.get("ro.telephony.ril_class", "HwHisiRIL").trim();
            RlogEx.i(LOG_TAG, "RILClassname is " + sRILClassname);
            Class<?> clazz = Class.forName("com.android.internal.telephony." + sRILClassname);
            return (T) clazz.cast(clazz.getConstructor(Context.class, Integer.TYPE, Integer.TYPE, Integer.class).newInstance(context, Integer.valueOf(networkMode), Integer.valueOf(cdmaSubscription), instanceId));
        } catch (InstantiationException e) {
            RlogEx.i(LOG_TAG, "InstantiationException ");
            return (T) new RIL(context, networkMode, cdmaSubscription, instanceId);
        } catch (IllegalAccessException e2) {
            RlogEx.i(LOG_TAG, "IllegalAccessException ");
            return (T) new RIL(context, networkMode, cdmaSubscription, instanceId);
        } catch (ClassNotFoundException e3) {
            RlogEx.i(LOG_TAG, "ClassNotFoundException ");
            return (T) new RIL(context, networkMode, cdmaSubscription, instanceId);
        } catch (NoSuchMethodException e4) {
            RlogEx.i(LOG_TAG, "NoSuchMethodException ");
            return (T) new RIL(context, networkMode, cdmaSubscription, instanceId);
        } catch (InvocationTargetException e5) {
            RlogEx.i(LOG_TAG, "InvocationTargetException ");
            return (T) new RIL(context, networkMode, cdmaSubscription, instanceId);
        }
    }

    public void loadHuaweiPhoneService(PhoneExt[] phones, Context context) {
        try {
            Class huaweiPhoneServiceClass = Class.forName("com.android.internal.telephony.HwPhoneService");
            HwFrameworkFactory.getHwInnerTelephonyManager().updateSigCustInfoFromXML(context);
            setPhoneService((HwPhoneManager.PhoneServiceInterface) huaweiPhoneServiceClass.newInstance());
            phoneService.setPhone(phones, context);
            initApnTypeAndDataState();
        } catch (InstantiationException e) {
            RlogEx.i(LOG_TAG, "InstantiationException ");
        } catch (IllegalAccessException e2) {
            RlogEx.i(LOG_TAG, "IllegalAccessException ");
        } catch (ClassNotFoundException e3) {
            RlogEx.i(LOG_TAG, "ClassNotFoundException ");
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

    public void setRoamingBrokerIccId(String iccId) {
        RoamingBroker.getDefault().setIccId(iccId);
    }

    public void setRoamingBrokerOperator(String plmn) {
        RoamingBroker.getDefault().setOperator(plmn);
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
            RlogEx.i(LOG_TAG, "encryptInfo: one or some params is null, do nothing.");
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        try {
            sensitiveInfo = HwAESCryptoUtil.encrypt(HwFullNetworkManager.getInstance().getMasterPassword(), sensitiveInfo);
        } catch (NoSuchProviderException e) {
            RlogEx.i(LOG_TAG, "HwAESCryptoUtil encryptInfo NoSuchProviderException");
        } catch (Exception e2) {
            RlogEx.i(LOG_TAG, "HwAESCryptoUtil encrypt excepiton");
        }
        editor.putString(encryptTag, sensitiveInfo);
        editor.apply();
    }

    public String decryptInfo(Context context, String encryptTag) {
        if (context == null || encryptTag == null) {
            RlogEx.i(LOG_TAG, "decryptInfo: context or encryptTag is null, do nothing.");
            return null;
        }
        String sensitiveInfo = PreferenceManager.getDefaultSharedPreferences(context).getString(encryptTag, "");
        if ("".equals(sensitiveInfo)) {
            return sensitiveInfo;
        }
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkManager.getInstance().getMasterPassword(), sensitiveInfo);
        } catch (NoSuchProviderException e) {
            RlogEx.i(LOG_TAG, "HwAESCryptoUtil decryptInfo NoSuchProviderException");
            return sensitiveInfo;
        } catch (Exception e2) {
            RlogEx.i(LOG_TAG, "HwAESCryptoUtil decrypt excepiton");
            return sensitiveInfo;
        }
    }

    public void checkMMICode(String mmiCode, int phoneId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("ims_config");
        RlogEx.i(LOG_TAG, "checkMMICode");
        boolean isUnSupport = false;
        if (b != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(phoneId);
                data.writeString(mmiCode);
                boolean z = false;
                b.transact(CODE_IS_UNSUPPORT_MMI_CODE, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    z = true;
                }
                isUnSupport = z;
            } catch (RemoteException e) {
                RlogEx.e(LOG_TAG, "checkMMICode RemoteException ");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        } else {
            RlogEx.e(LOG_TAG, "checkMMICode - can't get ims_config service");
        }
        reply.recycle();
        data.recycle();
        if (isUnSupport) {
            RlogEx.i(LOG_TAG, "Not Support MMI Code=" + mmiCode);
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
            this.mApnTypeAndDataState.add(i, new HashMap<>());
        }
    }

    private boolean validatePhoneId(int phoneId) {
        return phoneId >= 0 && phoneId < this.mNumPhones;
    }

    public boolean checkApnShouldNotify(int subId, String apnType, PhoneConstants.DataState state) {
        if ("default".equals(apnType) || (apnType != null && apnType.startsWith("snssai"))) {
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

    public IHwGsmCdmaPhoneEx createHwGsmCdmaPhoneEx(IHwGsmCdmaPhoneInner iHwGsmCdmaPhoneInner, PhoneExt phoneExt) {
        if (phoneExt == null || iHwGsmCdmaPhoneInner == null) {
            return null;
        }
        return new HwGsmCdmaPhoneEx(iHwGsmCdmaPhoneInner, phoneExt);
    }

    public boolean isShortCodeHw(String dialString, GsmCdmaPhone phone) {
        return HwGsmMmiCode.isShortCodeHw(dialString, phone);
    }

    public int removeUssdCust(GsmCdmaPhone phone) {
        return HwGsmMmiCode.removeUssdCust(phone);
    }
}
