package com.android.internal.telephony;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.StringTranslateManager;
import android.text.TextUtils;
import com.android.internal.telephony.HwPhoneManager;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.gsm.HwGsmMmiCode;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.MmiCodeExt;
import com.huawei.internal.telephony.PhoneConstantsExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import huawei.com.android.internal.telephony.RoamingBroker;
import huawei.cust.HwCustUtils;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class HwPhoneManagerImpl extends DefaultHwPhoneManager {
    private static final int CODE_IS_UNSUPPORT_MMI_CODE = 3001;
    private static final String DESCRIPTOR = "com.android.ims.internal.IImsConfig";
    private static final boolean IS_SUPPORT_USSI = SystemPropertiesEx.getBoolean("ro.config.hw_support_ussi", true);
    private static final boolean IS_VSIM_SUPPORT = SystemPropertiesEx.getBoolean("ro.radio.vsim_support", false);
    private static final String LOG_TAG = "HwPhoneManagerImpl";
    private static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    private static HwPhoneProxyReference mHwPhoneProxyReference;
    private static HwPhoneManager mInstance = new HwPhoneManagerImpl();
    private static HwPhoneManager.PhoneServiceInterface phoneService = null;
    private ArrayList<HashMap<String, PhoneConstantsExt.DataStateEx>> mApnTypeAndDataState = null;
    Object mCust = HwCustUtils.createObj(HwCustPhoneManager.class, new Object[0]);
    private int mNumPhones;

    public static HwPhoneManager getDefault() {
        return mInstance;
    }

    private static void setHwPhoneProxyReference(HwPhoneProxyReference obj) {
        mHwPhoneProxyReference = obj;
    }

    private static void setPhoneService(HwPhoneManager.PhoneServiceInterface obj) {
        phoneService = obj;
    }

    public boolean changeMMItoUSSD(PhoneExt phone, String poundString) {
        return getCust().changeMMItoUSSD(phone, poundString);
    }

    public void loadHuaweiPhoneService(PhoneExt[] phoneExts, Context context) {
        try {
            setPhoneService((HwPhoneManager.PhoneServiceInterface) Class.forName("com.android.internal.telephony.HwPhoneService").newInstance());
            phoneService.setPhone(phoneExts, context);
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

    public Locale getSpecialLoacleConfig(Context context, int mcc) {
        return HwMccTable.getSpecialLoacleConfig(context, mcc);
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

    public void setGsmCdmaPhone(PhoneExt phoneExt, Context context) {
        setHwPhoneProxyReference(new HwPhoneProxyReference(phoneExt, context));
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

    public void setDefaultTimezone(Context context) {
        HwMccTable.setDefaultTimezone(context);
    }

    public void changedDefaultTimezone() {
        HwMccTable.setDefaultTimezone(false);
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

    public boolean isStringHuaweiIgnoreCode(PhoneExt phone, String dialString) {
        return getCust().isStringHuaweiIgnoreCode(phone, dialString) || HwGsmMmiCode.isStringHuaweiIgnoreCode(dialString);
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

    public String getRoamingBrokerImsi() {
        return RoamingBroker.getDefault().getRBImsi();
    }

    public void setRoamingBrokerImsi(String imsi) {
        RoamingBroker.getDefault().setImsi(imsi);
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
        return RoamingBroker.updateSelectionForRoamingBroker(selection);
    }

    public void setRoamingBrokerOperator(String plmn, int simId) {
        RoamingBroker.getDefault(Integer.valueOf(simId)).setOperator(plmn);
    }

    public String getRoamingBrokerVoicemail(int simId) {
        RoamingBroker.getDefault(Integer.valueOf(simId));
        return RoamingBroker.getRBVoicemail(Integer.valueOf(simId));
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

    public boolean processImsPhoneMmiCode(MmiCodeExt mmiCodeExt, PhoneExt imsPhone) {
        return HwGsmMmiCode.processImsPhoneMmiCode(mmiCodeExt, imsPhone);
    }

    public void handleMessageGsmMmiCode(MmiCodeExt mmiCodeExt, Message msg) {
        HwGsmMmiCode.handleMessageGsmMmiCode(mmiCodeExt, msg);
    }

    public boolean isSupportOrangeApn(PhoneExt phone) {
        return getCust().isSupportOrangeApn(phone);
    }

    public void addSpecialAPN(PhoneExt phone) {
        getCust().addSpecialAPN(phone);
    }

    public CharSequence getCallForwardingString(Context context, String sc) {
        return HwGsmMmiCode.getCallForwardingString(context, sc);
    }

    public boolean processSendUssdInImsCall(MmiCodeExt mmiCodeExt, PhoneExt imsPhone) {
        return HwGsmMmiCode.processSendUssdInImsCall(mmiCodeExt, imsPhone);
    }

    public boolean shouldRunUtIgnoreCSService(PhoneExt phoneExt, boolean isUt) {
        if (!isUt || phoneExt == null || !phoneExt.isImsRegistered() || phoneExt.getContext() == null) {
            return false;
        }
        if (IS_SUPPORT_USSI) {
            return true;
        }
        IccRecordsEx getIccRecords = phoneExt.getIccRecords();
        if (getIccRecords == null) {
            return false;
        }
        String hplmn = getIccRecords.getOperatorNumeric();
        if (TextUtils.isEmpty(hplmn)) {
            return false;
        }
        String ignoreCSServiceByMccMnc = Settings.System.getString(phoneExt.getContext().getContentResolver(), "plmn_ut_ignore_cs_service");
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

    public boolean needUnEscapeHtmlforUssdMsg(PhoneExt phone) {
        return HwGsmMmiCode.needUnEscapeHtmlforUssdMsg(phone);
    }

    public String unEscapeHtml4(String str) {
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
        String sensitiveInfo = PreferenceManager.getDefaultSharedPreferences(context).getString(encryptTag, BuildConfig.FLAVOR);
        if (BuildConfig.FLAVOR.equals(sensitiveInfo)) {
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
        IBinder binder = ServiceManagerEx.getService("ims_config");
        RlogEx.i(LOG_TAG, "checkMMICode");
        boolean isUnSupport = false;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(phoneId);
                data.writeString(mmiCode);
                boolean z = false;
                binder.transact(CODE_IS_UNSUPPORT_MMI_CODE, data, reply, 0);
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
        int numPhones = TelephonyManagerEx.getDefault().getPhoneCount();
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

    public boolean checkApnShouldNotify(int subId, String apnType, PhoneConstantsExt.DataStateEx state) {
        if ("default".equals(apnType) || (apnType != null && apnType.startsWith("snssai"))) {
            return true;
        }
        int phoneId = SubscriptionManagerEx.getPhoneId(subId);
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
}
