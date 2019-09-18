package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import huawei.cust.HwCfgFilePolicy;
import java.util.Arrays;
import java.util.regex.Pattern;

public class HwCustPhoneManagerImpl extends HwCustPhoneManager {
    private static final boolean HWDBG = true;
    private static final boolean HWLOGW_E = true;
    protected static final boolean IS_SUPPORT_ORANGE_APN = SystemProperties.getBoolean("ro.config.support_orange_apn", false);
    private static final String LOG_TAG = "HwCustPhoneManagerImpl";
    static final String MOBILE_DATA_ALWAYS_CONFIG = "hw_mobile_data_always_config";
    static final String POWER_SAVING_ON = "power_saving_on";
    static final String POWER_SAVING_ON_INIT_FLAG = "power_saving_on_init";
    protected static final String ROW_CARRIERS_URI = "content://telephony/carriers";
    protected static final String ROW_PREFERAPN_URI = "content://telephony/carriers/preferapn";
    protected static final boolean SHOW_VOICEMAIL_USSD = SystemProperties.getBoolean("ro.config.show_vmail_number", false);
    protected static final String VOICEMAIL_NUMBER = "voicemail_number";
    private int hasSeted = 0;

    public boolean isStringHuaweiIgnoreCode(GsmCdmaPhone phone, String dialString) {
        log("isNormalDialogForSim");
        if (dialString == null) {
            Rlog.e(LOG_TAG, "isNormalDialogForSim null dial string");
            return false;
        }
        Context context = phone.getContext();
        if (SHOW_VOICEMAIL_USSD && PhoneNumberUtils.isVoiceMailNumber(dialString)) {
            return true;
        }
        if (phone == null) {
            return false;
        }
        try {
            String ussdData = (String) HwCfgFilePolicy.getValue("ussd_regex", SubscriptionManager.getSlotIndex(phone.getPhoneId()), String.class);
            if (!TextUtils.isEmpty(ussdData) && Pattern.compile(ussdData).matcher(dialString).matches()) {
                return true;
            }
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "get ussd_regex error:" + e);
        }
        String data = Settings.System.getString(context.getContentResolver(), "hw_ussd_regex");
        if (data != null) {
            String current_mccmnc = phone.getOperatorNumeric();
            if (TextUtils.isEmpty(current_mccmnc)) {
                Rlog.e(LOG_TAG, "current_mccmnc is null");
                return false;
            }
            for (String mccmnc_regex : data.split(";")) {
                if (!TextUtils.isEmpty(mccmnc_regex) && mccmnc_regex.indexOf(",") > 0) {
                    String mccmnc = mccmnc_regex.substring(0, mccmnc_regex.indexOf(","));
                    String ussdRegex = mccmnc_regex.substring(mccmnc_regex.indexOf(",") + 1);
                    if (current_mccmnc.equals(mccmnc) && Pattern.compile(ussdRegex).matcher(dialString).matches()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void initParamByPlmn(SIMRecords mSIMRecords, Context mContext) {
        initMobileDataAlwaysByPlmn(mSIMRecords, mContext);
    }

    public boolean isSupportOrangeApn(Phone phone) {
        if (phone == null || phone.mIccRecords == null || phone.mIccRecords.get() == null) {
            return false;
        }
        String mccmnc = ((IccRecords) phone.mIccRecords.get()).getOperatorNumeric();
        log("isSupportOrangeApn mccmnc = " + mccmnc + " IS_SUPPORT_ORANGE_APN = " + IS_SUPPORT_ORANGE_APN);
        if (mccmnc == null || !IS_SUPPORT_ORANGE_APN || !"20801".equals(mccmnc)) {
            return false;
        }
        return true;
    }

    public boolean changeMMItoUSSD(GsmCdmaPhone phone, String dialString) {
        String str = dialString;
        boolean z = false;
        if (str == null || phone == null) {
            Rlog.e(LOG_TAG, "changeMMItoUSSD dialString or phone is null");
            return false;
        }
        String dataFromCard = (String) HwCfgFilePolicy.getValue("hw_mmi_to_ussd", phone.getSubId(), String.class);
        if (dataFromCard != null) {
            log("changeMMItoUSSD subId:" + subId + ", card's length: " + dataFromCard.length());
            return dataFromCard.contains(str);
        }
        String data = Settings.System.getString(phone.getContext().getContentResolver(), "hw_mmi_to_ussd");
        if (data != null && data.length() > 0) {
            String current_mccmnc = phone.getOperatorNumeric();
            if (TextUtils.isEmpty(current_mccmnc)) {
                Rlog.e(LOG_TAG, "current_mccmnc is null");
                return false;
            }
            String[] datas = data.split(";");
            int length = datas.length;
            int i = 0;
            while (i < length) {
                String mccmnc_ussds = datas[i];
                if (mccmnc_ussds.indexOf(":") == -1) {
                    return z;
                }
                String mccmnc = mccmnc_ussds.substring(z ? 1 : 0, mccmnc_ussds.indexOf(":"));
                String ussds = mccmnc_ussds.substring(mccmnc_ussds.indexOf(":") + 1);
                if (current_mccmnc.equals(mccmnc) && Arrays.asList(ussds.split(",")).contains(str)) {
                    return true;
                }
                i++;
                z = false;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002d, code lost:
        if (r7 == null) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002f, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003b, code lost:
        if (r7 == null) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003e, code lost:
        log("addSpecialAPN hasSeted = " + r9.hasSeted);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0056, code lost:
        if (r9.hasSeted != 1) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0058, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0059, code lost:
        r1 = new android.content.ContentValues();
        r1.put("name", "netgprs.com");
        r1.put("apn", "netgprs.com");
        r1.put("mcc", "208");
        r1.put("mnc", "01");
        r1.put("numeric", "20801");
        r1.put("type", "default,supl");
        r0.insert(android.net.Uri.parse(ROW_CARRIERS_URI), r1);
     */
    public void addSpecialAPN(Phone phone) {
        if (phone != null) {
            ContentResolver resolver = phone.getContext().getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Uri.parse(ROW_CARRIERS_URI), null, "name='netgprs.com' and apn = 'netgprs.com'", null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        this.hasSeted = 1;
                    }
                }
                this.hasSeted = 0;
            } catch (SQLiteException e) {
                log("query carriers failed");
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    private void initMobileDataAlwaysByPlmn(SIMRecords mSIMRecords, Context mContext) {
        if (!isMobileDataAlwaysInitDone(mContext)) {
            String mobileDataAlwaysConfig = SettingsEx.Systemex.getString(mContext.getContentResolver(), MOBILE_DATA_ALWAYS_CONFIG);
            if (mobileDataAlwaysConfig != null && !"".equals(mobileDataAlwaysConfig)) {
                for (String config : mobileDataAlwaysConfig.split(";")) {
                    String plmn = mSIMRecords.getOperatorNumeric();
                    if (plmn != null && config.contains(plmn)) {
                        String[] configDetail = config.split(",");
                        if ("enable".equals(configDetail[1])) {
                            SettingsEx.Systemex.putInt(mContext.getContentResolver(), POWER_SAVING_ON, 0);
                            setMobileDataAlwaysInitDone(mContext);
                            log("[initMobileDataAlwaysByPlmn] set POWER_SAVING_ON to 0, plmn is " + plmn);
                        } else if ("disable".equals(configDetail[1])) {
                            SettingsEx.Systemex.putInt(mContext.getContentResolver(), POWER_SAVING_ON, 1);
                            setMobileDataAlwaysInitDone(mContext);
                            log("[initMobileDataAlwaysByPlmn] set POWER_SAVING_ON to 1, plmn is " + plmn);
                        }
                    }
                }
            }
        }
    }

    private boolean isMobileDataAlwaysInitDone(Context mContext) {
        if (SettingsEx.Systemex.getInt(mContext.getContentResolver(), POWER_SAVING_ON_INIT_FLAG, 0) == 1) {
            return true;
        }
        return false;
    }

    private void setMobileDataAlwaysInitDone(Context mContext) {
        SettingsEx.Systemex.putInt(mContext.getContentResolver(), POWER_SAVING_ON_INIT_FLAG, 1);
    }

    public boolean isVoicemailFromDbSupported() {
        return SystemProperties.getBoolean("ro.config.sprint_pim_ext", false);
    }

    public String getVoicemailFromDb(Context context, String number) {
        String voicemail = SettingsEx.Systemex.getString(context.getContentResolver(), VOICEMAIL_NUMBER);
        if (TextUtils.isEmpty(voicemail)) {
            return number;
        }
        log("getVoicemailFromDb length = " + voicemail.length());
        return voicemail;
    }

    private void log(String message) {
        Rlog.d(LOG_TAG, message);
    }

    public boolean isSupportEccFormVirtualNet() {
        return SystemProperties.getBoolean("ro.config.hw_support_vm_ecc", false);
    }

    public String getVirtualNetEccWihCard(int slotId) {
        if (VirtualNet.isVirtualNet(slotId)) {
            VirtualNet vt = VirtualNet.getCurrentVirtualNet(slotId);
            if (vt != null) {
                return vt.getEccWithCard();
            }
        }
        return null;
    }

    public String getVirtualNetEccNoCard(int slotId) {
        if (VirtualNet.isVirtualNet(slotId)) {
            VirtualNet vt = VirtualNet.getCurrentVirtualNet(slotId);
            if (vt != null) {
                return vt.getEccNoCard();
            }
        }
        return null;
    }
}
