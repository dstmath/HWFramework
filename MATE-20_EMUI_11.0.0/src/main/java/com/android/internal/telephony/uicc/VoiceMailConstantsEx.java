package com.android.internal.telephony.uicc;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.HwTelephony;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.dataconnection.ApnReminderEx;
import huawei.com.android.internal.telephony.RoamingBroker;
import huawei.cust.HwCfgFilePolicy;

public class VoiceMailConstantsEx implements IHwVoiceMailConstantsEx {
    private static final boolean HW_DBG = SystemPropertiesEx.getBoolean("ro.debuggable", false);
    private static final String LOG_TAG = "VoiceMailConstantsEx";
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final int SLOT0 = 0;
    private static final int SLOT1 = 1;
    static final int VMMODE_CUST_SIM_SP = 3;
    private static final int VMMODE_INVALID = -1;
    static final int VMMODE_SIM_CUST_SP = 1;
    static final int VMMODE_SIM_SP_CUST = 2;
    private static final int VOICEMAIL_PRIORITY_MODE = SystemPropertiesEx.getInt("ro.config.vm_prioritymode", 3);
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
        if (RoamingBroker.isRoamingBrokerActivated()) {
            String number = RoamingBroker.getRBVoicemail();
            if ((number != null && !number.isEmpty()) || this.mVoiceMailConstantsInner.containsCarrierInner(carrier)) {
                log("containsCarrier load voicemail number from RoamingBroker config");
                vnContainsVM = true;
            }
        } else {
            vnContainsVM = this.mVoiceMailConstantsInner.containsCarrierInner(carrier);
        }
        if (!vnContainsVM) {
            StringBuilder sb = new StringBuilder();
            sb.append("containsCarrier VoiceMailConfig doesn't contains the carrier");
            sb.append(HW_DBG ? carrier : "***");
            log(sb.toString());
        }
        return vnContainsVM;
    }

    public boolean containsCarrierHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        boolean vnContainsVM = false;
        log("containsCarrier slotId = " + slotId);
        if (slotId != 0 && slotId != 1) {
            return false;
        }
        if (RoamingBroker.getDefault(Integer.valueOf(slotId)).isRoamingBrokerActivated(Integer.valueOf(slotId))) {
            String number = RoamingBroker.getRBVoicemail();
            if ((number != null && !number.isEmpty()) || this.mVoiceMailConstantsInner.containsCarrierInner(carrier)) {
                log("containsCarrier load voicemail number from RoamingBroker config for slot" + slotId);
                vnContainsVM = true;
            }
        } else {
            vnContainsVM = this.mVoiceMailConstantsInner.containsCarrierInner(carrier);
        }
        if (!vnContainsVM) {
            StringBuilder sb = new StringBuilder();
            sb.append("containsCarrier VoiceMailConfig doesn't contains the carrier");
            sb.append(HW_DBG ? carrier : " *** ");
            sb.append(" for slot");
            sb.append(slotId);
            log(sb.toString());
        }
        return vnContainsVM;
    }

    public void getVoiceMailConfig(String carrier) {
        String number;
        log("voicemail number Priority = " + getVoicemailPriorityMode());
        this.mCurrentCarrier = carrier;
        if (!TextUtils.isEmpty(this.mVMNumberOnSim) && this.voicemailPriorityMode != 3) {
            log("load voicemail number from sim");
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
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
            String mIccId = tm != null ? tm.getSimSerialNumber() : null;
            if (!TextUtils.isEmpty(mIccId)) {
                number = sp.getString(mIccId + this.mSlotId, null);
            } else {
                number = sp.getString("vm_number_key" + this.mSlotId, null);
            }
            if (number != null) {
                log("load voicemail number from SP");
                this.mVMCurrentMNumber = number;
                this.mVMLoaded = true;
                return;
            }
        }
        if (!this.mVMLoaded) {
            log("load voicemail number from cust");
            this.mVMCurrentMNumber = this.mVoiceMailConstantsInner.getVoiceMailNumberInner(carrier);
            if (!isVMTagNotFromConf(carrier)) {
                this.mVMCurrentTag = this.mVoiceMailConstantsInner.getVoiceMailTagInner(carrier);
            }
        }
        this.mVMLoaded = true;
    }

    public void getVoiceMailConfig(String carrier, int slotId) {
        this.voicemailPriorityMode = getVoicemailPriorityMode();
        log("voicemail number Priority = " + this.voicemailPriorityMode);
        this.mCurrentCarrier = carrier;
        log("getVoiceMailConfig slotId = " + slotId);
        if (slotId == 0 || slotId == 1) {
            if (TextUtils.isEmpty(this.mVMNumberOnSim) || this.voicemailPriorityMode == 3) {
                if (!TextUtils.isEmpty(this.mVMNumberOnSim) || this.voicemailPriorityMode != 2) {
                    int i = this.voicemailPriorityMode;
                    if (i == 3 || i == 1) {
                        this.mIsVoiceMailFixed = true;
                    }
                } else {
                    this.mIsVoiceMailFixed = false;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
                    String number = sp.getString("vm_number_key" + slotId, null);
                    if (number != null) {
                        log("load voicemail number from SP");
                        this.mVMCurrentMNumber = number;
                        this.mVMLoaded = true;
                        return;
                    }
                }
                if (!ApnReminderEx.isPopupApnSettingsEmpty(this.mContext, slotId)) {
                    this.mVMCurrentMNumber = ApnReminderEx.getVoiceMailNumberByPreferedApn(this.mContext, slotId, getPreferedApnId(slotId), this.mVMCurrentMNumber);
                    this.mVMCurrentTag = ApnReminderEx.getVoiceMailTagByPreferedApn(this.mContext, slotId, getPreferedApnId(slotId), this.mVMCurrentTag);
                    log("load voicemail number and tag from apnReminder");
                    this.mVMLoaded = true;
                    return;
                }
                if (!this.mVMLoaded) {
                    log("load voicemail number from cust");
                    this.mVMCurrentMNumber = this.mVoiceMailConstantsInner.getVoiceMailNumberInner(carrier);
                    if (!isVMTagNotFromConf(carrier)) {
                        this.mVMCurrentTag = this.mVoiceMailConstantsInner.getVoiceMailTagInner(carrier);
                    }
                }
                this.mVMLoaded = true;
                return;
            }
            log("load voicemail number from sim");
            this.mIsVoiceMailFixed = false;
            this.mVMCurrentMNumber = this.mVMNumberOnSim;
            this.mVMCurrentTag = this.mVMTagOnSim;
            this.mVMLoaded = true;
        }
    }

    private boolean isVMTagNotFromConf(String carrier) {
        String strVMTagNotFromConf = Settings.System.getString(this.mContext.getContentResolver(), "hw_vmtag_not_from_conf");
        if (!TextUtils.isEmpty(strVMTagNotFromConf) && !TextUtils.isEmpty(carrier)) {
            for (String area : strVMTagNotFromConf.split(",")) {
                if (area.equals(carrier)) {
                    log("not load voicemail Tag from cust");
                    return true;
                }
            }
        }
        log("load voicemail Tag from cust");
        return false;
    }

    public boolean getVoiceMailFixed(String carrier) {
        String number;
        if (!this.mVMLoaded || (carrier != null && !carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier);
        }
        if (!RoamingBroker.isRoamingBrokerActivated() || (number = RoamingBroker.getRBVoicemail()) == null || number.isEmpty()) {
            return this.mIsVoiceMailFixed;
        }
        log("getVoiceMailFixed load voicemail number from RoamingBroker config");
        return true;
    }

    public boolean getVoiceMailFixed(String carrier, int slotId) {
        String number;
        if (!this.mVMLoaded || (carrier != null && !carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (!RoamingBroker.getDefault(Integer.valueOf(slotId)).isRoamingBrokerActivated(Integer.valueOf(slotId)) || (number = RoamingBroker.getRBVoicemail(Integer.valueOf(slotId))) == null || number.isEmpty()) {
            return this.mIsVoiceMailFixed;
        }
        log("getVoiceMailFixed load voicemail number from RoamingBroker config for slot" + slotId);
        return true;
    }

    public String getVoiceMailNumberHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier) {
        String number;
        if (!this.mVMLoaded || (carrier != null && !carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier);
        }
        if (!RoamingBroker.isRoamingBrokerActivated() || (number = RoamingBroker.getRBVoicemail()) == null || number.isEmpty()) {
            return this.mVMCurrentMNumber;
        }
        log("load voicemail number from RoamingBroker config");
        return RoamingBroker.getRBVoicemail();
    }

    public String getVoiceMailNumberHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        String number;
        if (!this.mVMLoaded || (carrier != null && !carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (!RoamingBroker.getDefault(Integer.valueOf(slotId)).isRoamingBrokerActivated(Integer.valueOf(slotId)) || (number = RoamingBroker.getRBVoicemail(Integer.valueOf(slotId))) == null || number.isEmpty()) {
            return this.mVMCurrentMNumber;
        }
        log("load voicemail number from RoamingBroker config for slot" + slotId);
        return number;
    }

    public String getVoiceMailTagHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier) {
        String number;
        if (!this.mVMLoaded || (carrier != null && !carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier);
        }
        if (!RoamingBroker.isRoamingBrokerActivated() || (number = RoamingBroker.getRBVoicemail()) == null || number.isEmpty()) {
            return this.mVMCurrentTag;
        }
        log("load voicemail number from RoamingBroker config");
        return null;
    }

    public String getVoiceMailTagHw(IVoiceMailConstantsInner voiceMailConstantsInner, String carrier, int slotId) {
        String number;
        if (!this.mVMLoaded || (carrier != null && !carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (!RoamingBroker.getDefault(Integer.valueOf(slotId)).isRoamingBrokerActivated(Integer.valueOf(slotId)) || (number = RoamingBroker.getRBVoicemail()) == null || number.isEmpty()) {
            return this.mVMCurrentTag;
        }
        log("load voicemail number from RoamingBroker config");
        return null;
    }

    public void resetVoiceMailLoadFlag() {
        log("Before resetVoiceMailLoadFlag, mVMLoaded = " + this.mVMLoaded);
        this.mVMLoaded = false;
    }

    private int getPreferedApnId(int slotId) {
        int apnId = -1;
        Cursor cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId), new String[]{"_id", HwTelephony.NumMatchs.NAME, "apn"}, null, null, HwTelephony.NumMatchs.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String apnName = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            String carrierName = cursor.getString(cursor.getColumnIndexOrThrow(HwTelephony.NumMatchs.NAME));
            StringBuilder sb = new StringBuilder();
            sb.append("getPreferedApnId: ");
            sb.append(apnId);
            sb.append(", apn: ");
            sb.append(apnName);
            sb.append(", name: ");
            sb.append(HW_DBG ? carrierName : "***");
            sb.append(" for slot");
            sb.append(slotId);
            log(sb.toString());
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
        StringBuilder sb = new StringBuilder();
        sb.append("loadVoiceMailConfigFromCard carrier:");
        String str = "***";
        sb.append(HW_DBG ? carrier : str);
        sb.append(", key:");
        sb.append(configName);
        log(sb.toString());
        if (TextUtils.isEmpty(carrier)) {
            return null;
        }
        String carrierFromCard = null;
        try {
            carrierFromCard = (String) HwCfgFilePolicy.getValue(configName, this.mSlotId, String.class);
        } catch (Exception e) {
            log("read voicemail error");
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("loadVoiceMailConfigFromCard, mSlotId:");
        sb2.append(this.mSlotId);
        sb2.append(", carrierFromCard:");
        if (HW_DBG) {
            str = carrierFromCard;
        }
        sb2.append(str);
        log(sb2.toString());
        return carrierFromCard;
    }
}
