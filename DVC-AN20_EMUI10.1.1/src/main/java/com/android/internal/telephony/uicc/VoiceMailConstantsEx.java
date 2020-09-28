package com.android.internal.telephony.uicc;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.dataconnection.ApnReminder;
import com.huawei.android.telephony.RlogEx;
import huawei.cust.HwCfgFilePolicy;

public class VoiceMailConstantsEx implements IHwVoiceMailConstantsEx {
    private static final String LOG_TAG = "VoiceMailConstantsEx";
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final int SUB1 = 0;
    private static final int SUB2 = 1;
    static final int VMMODE_CUST_SIM_SP = 3;
    private static final int VMMODE_INVALID = -1;
    static final int VMMODE_SIM_CUST_SP = 1;
    static final int VMMODE_SIM_SP_CUST = 2;
    private static final int VOICEMAIL_PRIORITY_MODE = SystemProperties.getInt("ro.config.vm_prioritymode", 3);
    private Context mContext;
    private String mCurrentCarrier = null;
    private boolean mIsVoiceMailFixed = false;
    private int mSlotId = 0;
    private String mVMCurrentMNumber = null;
    private String mVMCurrentTag = null;
    private boolean mVMLoaded = false;
    private String mVMNumberOnSim = null;
    private String mVMTagOnSim = null;
    private IVoiceMailConstantsInner mVoiceMailConstantsInner;
    private int mVoicemailPriorityModeWithCard = -1;
    private int voicemailPriorityMode;

    public VoiceMailConstantsEx(IVoiceMailConstantsInner voiceMailConstantsInner, Context context, int slotId) {
        this.mVoiceMailConstantsInner = voiceMailConstantsInner;
        this.mContext = context;
        this.mSlotId = slotId;
        this.voicemailPriorityMode = getVmPriorityMode();
        log("load voicemail number Priority from systemproperty");
    }

    public int getVoicemailPriorityMode() {
        this.voicemailPriorityMode = getVmPriorityMode();
        return this.voicemailPriorityMode;
    }

    public void setVoicemailOnSIM(String voicemailNumber, String voicemailTag) {
        if (voicemailNumber != null) {
            this.mVMNumberOnSim = voicemailNumber.trim();
        } else {
            this.mVMNumberOnSim = null;
        }
        this.mVMTagOnSim = voicemailTag;
        this.mVMLoaded = false;
    }

    public boolean containsCarrierHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier) {
        boolean vnContainsVM = false;
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if ((number != null && !number.isEmpty()) || this.mVoiceMailConstantsInner.containsCarrierInner(carrier)) {
                log("containsCarrier load voicemail number from RoamingBroker config");
                vnContainsVM = true;
            }
        } else {
            vnContainsVM = this.mVoiceMailConstantsInner.containsCarrierInner(carrier);
        }
        if (!vnContainsVM) {
            log("containsCarrier VoiceMailConfig doesn't contains the carrier" + carrier);
        }
        return vnContainsVM;
    }

    public boolean containsCarrierHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        boolean vnContainsVM = false;
        int subId = slotId;
        log("containsCarrier slotId = " + slotId);
        if (!(slotId == 0 || 1 == slotId)) {
            subId = 0;
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(subId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if ((number != null && !number.isEmpty()) || this.mVoiceMailConstantsInner.containsCarrierInner(carrier)) {
                log("containsCarrier load voicemail number from RoamingBroker config");
                vnContainsVM = true;
            }
        } else {
            vnContainsVM = this.mVoiceMailConstantsInner.containsCarrierInner(carrier);
        }
        if (!vnContainsVM) {
            log("containsCarrier VoiceMailConfig doesn't contains the carrier" + carrier + " for sub" + subId);
        }
        return vnContainsVM;
    }

    public void getVoiceMailConfig(String carrier) {
        StringBuilder sb;
        Log.d(LOG_TAG, "voicemail number Priority = " + getVoicemailPriorityMode());
        this.mCurrentCarrier = carrier;
        if (!TextUtils.isEmpty(this.mVMNumberOnSim) && this.voicemailPriorityMode != 3) {
            Log.d(LOG_TAG, "load voicemail number from sim");
            this.mIsVoiceMailFixed = false;
            this.mVMCurrentMNumber = this.mVMNumberOnSim;
            this.mVMCurrentTag = this.mVMTagOnSim;
            this.mVMLoaded = true;
        } else if (!TextUtils.isEmpty(this.mVMNumberOnSim) || this.voicemailPriorityMode != 2) {
            int i = this.voicemailPriorityMode;
            if (i == 3 || i == 1) {
                this.mIsVoiceMailFixed = true;
            }
        } else {
            this.mIsVoiceMailFixed = false;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            String mIccId = ((TelephonyManager) this.mContext.getSystemService("phone")) != null ? TelephonyManager.getDefault().getSimSerialNumber() : null;
            if (!TextUtils.isEmpty(mIccId)) {
                sb = new StringBuilder();
                sb.append(mIccId);
            } else {
                sb = new StringBuilder();
                sb.append("vm_number_key");
            }
            sb.append(this.mSlotId);
            String number = sp.getString(sb.toString(), null);
            if (number != null) {
                Log.d(LOG_TAG, "load voicemail number from SP");
                this.mVMCurrentMNumber = number;
                this.mVMLoaded = true;
                return;
            }
        }
        if (!this.mVMLoaded) {
            Log.d(LOG_TAG, "load voicemail number from cust");
            this.mVMCurrentMNumber = this.mVoiceMailConstantsInner.getVoiceMailNumberInner(carrier);
            if (!isVMTagNotFromConf(carrier)) {
                this.mVMCurrentTag = this.mVoiceMailConstantsInner.getVoiceMailTagInner(carrier);
            }
        }
        this.mVMLoaded = true;
    }

    public void getVoiceMailConfig(String carrier, int slotId) {
        this.voicemailPriorityMode = getVoicemailPriorityMode();
        Log.d(LOG_TAG, "voicemail number Priority = " + this.voicemailPriorityMode);
        this.mCurrentCarrier = carrier;
        int subId = slotId;
        log("getVoiceMailConfig slotId = " + slotId);
        if ((slotId == 0 || 1 == slotId) ? false : true) {
            subId = 0;
        }
        if (TextUtils.isEmpty(this.mVMNumberOnSim) || this.voicemailPriorityMode == 3) {
            if (!TextUtils.isEmpty(this.mVMNumberOnSim) || this.voicemailPriorityMode != 2) {
                int i = this.voicemailPriorityMode;
                if (i == 3 || i == 1) {
                    this.mIsVoiceMailFixed = true;
                }
            } else {
                this.mIsVoiceMailFixed = false;
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
                String number = sp.getString("vm_number_key" + subId, null);
                if (number != null) {
                    Log.d(LOG_TAG, "load voicemail number from SP");
                    this.mVMCurrentMNumber = number;
                    this.mVMLoaded = true;
                    return;
                }
            }
            ApnReminder apnReminder = ApnReminder.getInstance(this.mContext, subId);
            if (!apnReminder.isPopupApnSettingsEmpty()) {
                this.mVMCurrentMNumber = apnReminder.getVoiceMailNumberByPreferedApn(getPreferedApnId(subId), this.mVMCurrentMNumber);
                this.mVMCurrentTag = apnReminder.getVoiceMailTagByPreferedApn(getPreferedApnId(subId), this.mVMCurrentTag);
                log("load voicemail number and tag from apnReminder");
                this.mVMLoaded = true;
                return;
            }
            if (!this.mVMLoaded) {
                Log.d(LOG_TAG, "load voicemail number from cust");
                this.mVMCurrentMNumber = this.mVoiceMailConstantsInner.getVoiceMailNumberInner(carrier);
                if (!isVMTagNotFromConf(carrier)) {
                    this.mVMCurrentTag = this.mVoiceMailConstantsInner.getVoiceMailTagInner(carrier);
                }
            }
            this.mVMLoaded = true;
            return;
        }
        Log.d(LOG_TAG, "load voicemail number from sim");
        this.mIsVoiceMailFixed = false;
        this.mVMCurrentMNumber = this.mVMNumberOnSim;
        this.mVMCurrentTag = this.mVMTagOnSim;
        this.mVMLoaded = true;
    }

    private boolean isVMTagNotFromConf(String carrier) {
        String strVMTagNotFromConf = Settings.System.getString(this.mContext.getContentResolver(), "hw_vmtag_not_from_conf");
        if (!TextUtils.isEmpty(strVMTagNotFromConf) && !TextUtils.isEmpty(carrier)) {
            for (String area : strVMTagNotFromConf.split(",")) {
                if (area.equals(carrier)) {
                    Log.d(LOG_TAG, "not load voicemail Tag from cust");
                    return true;
                }
            }
        }
        Log.d(LOG_TAG, "load voicemail Tag from cust");
        return false;
    }

    public boolean getVoiceMailFixed(String carrier) {
        String number;
        if (!this.mVMLoaded || !carrier.equals(this.mCurrentCarrier)) {
            getVoiceMailConfig(carrier);
        }
        if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated() || (number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail()) == null || number.isEmpty()) {
            return this.mIsVoiceMailFixed;
        }
        log("getVoiceMailFixed load voicemail number from RoamingBroker config");
        return true;
    }

    public boolean getVoiceMailFixed(String carrier, int slotId) {
        String number;
        if (!this.mVMLoaded || !carrier.equals(this.mCurrentCarrier)) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId)) || (number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail()) == null || number.isEmpty()) {
            return this.mIsVoiceMailFixed;
        }
        log("getVoiceMailFixed load voicemail number from RoamingBroker config");
        return true;
    }

    public String getVoiceMailNumberHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier) {
        String number;
        if (!this.mVMLoaded || !carrier.equals(this.mCurrentCarrier)) {
            getVoiceMailConfig(carrier);
        }
        if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated() || (number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail()) == null || number.isEmpty()) {
            return this.mVMCurrentMNumber;
        }
        log("load voicemail number from RoamingBroker config");
        return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
    }

    public String getVoiceMailNumberHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        String number;
        if (!this.mVMLoaded || !carrier.equals(this.mCurrentCarrier)) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId)) || (number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail()) == null || number.isEmpty()) {
            return this.mVMCurrentMNumber;
        }
        log("load voicemail number from RoamingBroker config");
        return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
    }

    public String getVoiceMailTagHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier) {
        String number;
        if (!this.mVMLoaded || !carrier.equals(this.mCurrentCarrier)) {
            getVoiceMailConfig(carrier);
        }
        if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated() || (number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail()) == null || number.isEmpty()) {
            return this.mVMCurrentTag;
        }
        log("load voicemail number from RoamingBroker config");
        return null;
    }

    public String getVoiceMailTagHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        String number;
        if (!this.mVMLoaded || !carrier.equals(this.mCurrentCarrier)) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (!HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId)) || (number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail()) == null || number.isEmpty()) {
            return this.mVMCurrentTag;
        }
        log("load voicemail number from RoamingBroker config");
        return null;
    }

    public void resetVoiceMailLoadFlag() {
        log("Before resetVoiceMailLoadFlag, mVMLoaded = " + this.mVMLoaded);
        this.mVMLoaded = false;
    }

    private int getPreferedApnId() {
        int apnId = -1;
        Cursor cursor = this.mContext.getContentResolver().query(PREFERAPN_NO_UPDATE_URI, new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String apnName = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            String carrierName = cursor.getString(cursor.getColumnIndexOrThrow(HwTelephony.NumMatchs.NAME));
            log("getPreferedApnId: " + apnId + ", apn: " + apnName + ", name: " + carrierName);
        }
        if (cursor != null) {
            cursor.close();
        }
        return apnId;
    }

    private int getPreferedApnId(int slotId) {
        int apnId = -1;
        Cursor cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId), new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String apnName = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            String carrierName = cursor.getString(cursor.getColumnIndexOrThrow(HwTelephony.NumMatchs.NAME));
            log("getPreferedApnId: " + apnId + ", apn: " + apnName + ", name: " + carrierName + " for slot" + slotId);
        }
        if (cursor != null) {
            cursor.close();
        }
        return apnId;
    }

    private void log(String string) {
        RlogEx.i(LOG_TAG, string);
    }

    public void setVoicemailInClaro(int voicemailPriorityMode2) {
        log("setVoicemailInClaro from hw_default.xml");
        this.voicemailPriorityMode = voicemailPriorityMode2;
        this.mVoicemailPriorityModeWithCard = voicemailPriorityMode2;
        log("setVoicemailInClaro " + this.voicemailPriorityMode);
    }

    private int getVmPriorityMode() {
        int valueFromProp = VOICEMAIL_PRIORITY_MODE;
        Integer valueFromCard = (Integer) HwCfgFilePolicy.getValue("vm_prioritymode", this.mSlotId, Integer.class);
        log("getVmPriorityModeFromCard, slotId:" + this.mSlotId + ", card:" + valueFromCard + ", card(old):" + this.mVoicemailPriorityModeWithCard + ", prop: " + valueFromProp);
        if (valueFromCard != null) {
            return valueFromCard.intValue();
        }
        int i = this.mVoicemailPriorityModeWithCard;
        if (i != -1) {
            return i;
        }
        return valueFromProp;
    }

    public String loadVoiceMailConfigFromCard(String configName, String carrier) {
        log("loadVoiceMailConfigFromCard carrier:" + carrier + ", key:" + configName);
        if (TextUtils.isEmpty(carrier)) {
            return null;
        }
        String carrierFromCard = null;
        try {
            carrierFromCard = (String) HwCfgFilePolicy.getValue(configName, this.mSlotId, String.class);
        } catch (Exception e) {
            log("read voicemail error");
        }
        log("loadVoiceMailConfigFromCard, mSlotId:" + this.mSlotId + ", carrierFromCard:" + carrierFromCard);
        return carrierFromCard;
    }
}
