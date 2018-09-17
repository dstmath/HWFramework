package com.android.internal.telephony.uicc;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.HwTelephony.NumMatchs;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.VirtualNet;
import com.android.internal.telephony.dataconnection.ApnReminder;

public class VoiceMailConstantsEx extends VoiceMailConstants {
    private static final String LOG_TAG = "VoiceMailConstantsEx";
    private static final String MY_TAG = "VoiceMailConstantsEx";
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final int SUB1 = 0;
    private static final int SUB2 = 1;
    static final int VMMODE_CUST_SIM_SP = 3;
    static final int VMMODE_SIM_CUST_SP = 1;
    static final int VMMODE_SIM_SP_CUST = 2;
    private Context mContext;
    private String mCurrentCarrier = null;
    private boolean mIsVoiceMailFixed = false;
    private int mSlotId = 0;
    private String mVMCurrentMNumber = null;
    private String mVMCurrentTag = null;
    private boolean mVMLoaded = false;
    private String mVMNumberOnSim = null;
    private String mVMTagOnSim = null;
    private int voicemailPriorityMode;

    public VoiceMailConstantsEx(Context c, int slotId) {
        this.mContext = c;
        this.mSlotId = slotId;
        this.voicemailPriorityMode = SystemProperties.getInt("ro.config.vm_prioritymode", 3);
        log("load voicemail number Priority from systemproperty");
    }

    public int getVoicemailPriorityMode() {
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

    public void clearVoicemailLoadedFlag() {
        Log.d("VoiceMailConstantsEx", "Before clearVoicemailLoadedFlag, mVMLoaded = " + this.mVMLoaded);
        this.mVMLoaded = false;
    }

    public boolean containsCarrier(String carrier) {
        boolean vnContainsVM = false;
        if (HwTelephonyFactory.getHwPhoneManager().isVirtualNet() && carrier != null) {
            vnContainsVM = (!carrier.equals(HwTelephonyFactory.getHwPhoneManager().getVirtualNetNumeric()) || HwTelephonyFactory.getHwPhoneManager().getVirtualNetVoiceMailNumber() == null) ? false : HwTelephonyFactory.getHwPhoneManager().getVirtualNetVoiceMailNumber().length() > 0;
            log("containsCarrier load vitualNet Config");
            if (!ApnReminder.getInstance(this.mContext).isPopupApnSettingsEmpty()) {
                vnContainsVM = carrier.equals(HwTelephonyFactory.getHwPhoneManager().getVirtualNetNumeric());
                log("containsCarrier load apnReminder Config");
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || (number.isEmpty() ^ 1) == 0) || super.containsCarrier(carrier)) {
                log("containsCarrier load voicemail number from RoamingBroker config");
                vnContainsVM = true;
            }
        } else {
            vnContainsVM = super.containsCarrier(carrier);
        }
        if (!vnContainsVM) {
            log("containsCarrier VoiceMailConfig doesn't contains the carrier" + carrier);
        }
        return vnContainsVM;
    }

    public boolean containsCarrier(String carrier, int slotId) {
        boolean vnContainsVM = false;
        int subId = slotId;
        log("containsCarrier slotId = " + slotId);
        if (!(slotId == 0 || 1 == slotId)) {
            subId = 0;
        }
        if (VirtualNet.isVirtualNet(subId) && carrier != null) {
            vnContainsVM = (!carrier.equals(VirtualNet.getCurrentVirtualNet(subId).getNumeric()) || VirtualNet.getCurrentVirtualNet(subId).getVoiceMailNumber() == null) ? false : VirtualNet.getCurrentVirtualNet(subId).getVoiceMailNumber().length() > 0;
            log("containsCarrier load vitualNet Config");
            if (!ApnReminder.getInstance(this.mContext, subId).isPopupApnSettingsEmpty()) {
                vnContainsVM = carrier.equals(VirtualNet.getCurrentVirtualNet(subId).getNumeric());
                log("containsCarrier load apnReminder Config");
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(subId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || (number.isEmpty() ^ 1) == 0) || super.containsCarrier(carrier)) {
                log("containsCarrier load voicemail number from RoamingBroker config");
                vnContainsVM = true;
            }
        } else {
            vnContainsVM = super.containsCarrier(carrier);
        }
        if (!vnContainsVM) {
            log("containsCarrier VoiceMailConfig doesn't contains the carrier" + carrier + " for sub" + subId);
        }
        return vnContainsVM;
    }

    public void getVoiceMailConfig(String carrier) {
        Log.d("VoiceMailConstantsEx", "voicemail number Priority = " + getVoicemailPriorityMode());
        this.mCurrentCarrier = carrier;
        if (!TextUtils.isEmpty(this.mVMNumberOnSim) && this.voicemailPriorityMode != 3) {
            Log.d("VoiceMailConstantsEx", "load voicemail number from sim");
            this.mIsVoiceMailFixed = false;
            this.mVMCurrentMNumber = this.mVMNumberOnSim;
            this.mVMCurrentTag = this.mVMTagOnSim;
            this.mVMLoaded = true;
        } else if (TextUtils.isEmpty(this.mVMNumberOnSim) && this.voicemailPriorityMode == 2) {
            this.mIsVoiceMailFixed = false;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            Object mIccId = ((TelephonyManager) this.mContext.getSystemService("phone")) != null ? TelephonyManager.getDefault().getSimSerialNumber() : null;
            String number = !TextUtils.isEmpty(mIccId) ? sp.getString(mIccId + this.mSlotId, null) : sp.getString("vm_number_key" + this.mSlotId, null);
            if (number != null) {
                Log.d("VoiceMailConstantsEx", "load voicemail number from SP");
                this.mVMCurrentMNumber = number;
                this.mVMLoaded = true;
                return;
            }
        } else if (this.voicemailPriorityMode == 3 || this.voicemailPriorityMode == 1) {
            this.mIsVoiceMailFixed = true;
        }
        if (!this.mVMLoaded && HwTelephonyFactory.getHwPhoneManager().isVirtualNet()) {
            Log.d("VoiceMailConstantsEx", "try to load voicemail number from virtualNet");
            this.mVMCurrentMNumber = HwTelephonyFactory.getHwPhoneManager().getVirtualNetVoiceMailNumber();
            this.mVMCurrentTag = HwTelephonyFactory.getHwPhoneManager().getVirtualNetVoicemailTag();
            ApnReminder apnReminder = ApnReminder.getInstance(this.mContext);
            if (!apnReminder.isPopupApnSettingsEmpty()) {
                this.mVMCurrentMNumber = apnReminder.getVoiceMailNumberByPreferedApn(getPreferedApnId(), this.mVMCurrentMNumber);
                this.mVMCurrentTag = apnReminder.getVoiceMailTagByPreferedApn(getPreferedApnId(), this.mVMCurrentTag);
                log("load voicemail number and tag from apnReminder");
                this.mVMLoaded = true;
                return;
            } else if (!TextUtils.isEmpty(this.mVMCurrentMNumber)) {
                Log.d("VoiceMailConstantsEx", "loaded voicemail number from virtualNet");
                this.mVMLoaded = true;
                return;
            }
        }
        if (!this.mVMLoaded) {
            Log.d("VoiceMailConstantsEx", "load voicemail number from cust");
            this.mVMCurrentMNumber = super.getVoiceMailNumber(carrier);
            if (!isVMTagNotFromConf(carrier)) {
                this.mVMCurrentTag = super.getVoiceMailTag(carrier);
            }
        }
        this.mVMLoaded = true;
    }

    public void getVoiceMailConfig(String carrier, int slotId) {
        Log.d("VoiceMailConstantsEx", "voicemail number Priority = " + getVoicemailPriorityMode());
        this.mCurrentCarrier = carrier;
        int subId = slotId;
        log("getVoiceMailConfig slotId = " + slotId);
        boolean isNotValidSlotId = (slotId == 0 || 1 == slotId) ? false : true;
        if (isNotValidSlotId) {
            subId = 0;
        }
        if (TextUtils.isEmpty(this.mVMNumberOnSim) || this.voicemailPriorityMode == 3) {
            if (TextUtils.isEmpty(this.mVMNumberOnSim) && this.voicemailPriorityMode == 2) {
                this.mIsVoiceMailFixed = false;
                String number = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("vm_number_key" + subId, null);
                if (number != null) {
                    Log.d("VoiceMailConstantsEx", "load voicemail number from SP");
                    this.mVMCurrentMNumber = number;
                    this.mVMLoaded = true;
                    return;
                }
            } else if (this.voicemailPriorityMode == 3 || this.voicemailPriorityMode == 1) {
                this.mIsVoiceMailFixed = true;
            }
            ApnReminder apnReminder = ApnReminder.getInstance(this.mContext, subId);
            if (apnReminder.isPopupApnSettingsEmpty()) {
                if (VirtualNet.isVirtualNet(subId)) {
                    Log.d("VoiceMailConstantsEx", "try to load voicemail number from virtualNet");
                    this.mVMCurrentMNumber = VirtualNet.getCurrentVirtualNet(subId).getVoiceMailNumber();
                    this.mVMCurrentTag = VirtualNet.getCurrentVirtualNet(subId).getVoicemailTag();
                    if (!TextUtils.isEmpty(this.mVMCurrentMNumber)) {
                        Log.d("VoiceMailConstantsEx", "loaded voicemail number from virtualNet");
                        this.mVMLoaded = true;
                        return;
                    }
                }
                if (!this.mVMLoaded) {
                    Log.d("VoiceMailConstantsEx", "load voicemail number from cust");
                    this.mVMCurrentMNumber = super.getVoiceMailNumber(carrier);
                    if (!isVMTagNotFromConf(carrier)) {
                        this.mVMCurrentTag = super.getVoiceMailTag(carrier);
                    }
                }
                this.mVMLoaded = true;
                return;
            }
            this.mVMCurrentMNumber = apnReminder.getVoiceMailNumberByPreferedApn(getPreferedApnId(subId), this.mVMCurrentMNumber);
            this.mVMCurrentTag = apnReminder.getVoiceMailTagByPreferedApn(getPreferedApnId(subId), this.mVMCurrentTag);
            log("load voicemail number and tag from apnReminder");
            this.mVMLoaded = true;
            return;
        }
        Log.d("VoiceMailConstantsEx", "load voicemail number from sim");
        this.mIsVoiceMailFixed = false;
        this.mVMCurrentMNumber = this.mVMNumberOnSim;
        this.mVMCurrentTag = this.mVMTagOnSim;
        this.mVMLoaded = true;
    }

    private boolean isVMTagNotFromConf(String carrier) {
        String strVMTagNotFromConf = System.getString(this.mContext.getContentResolver(), "hw_vmtag_not_from_conf");
        if (!(TextUtils.isEmpty(strVMTagNotFromConf) || (TextUtils.isEmpty(carrier) ^ 1) == 0)) {
            for (String area : strVMTagNotFromConf.split(",")) {
                if (area.equals(carrier)) {
                    Log.d("VoiceMailConstantsEx", "not load voicemail Tag from cust");
                    return true;
                }
            }
        }
        Log.d("VoiceMailConstantsEx", "load voicemail Tag from cust");
        return false;
    }

    public boolean getVoiceMailFixed(String carrier) {
        if (!(this.mVMLoaded && (carrier.equals(this.mCurrentCarrier) ^ 1) == 0)) {
            getVoiceMailConfig(carrier);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || (number.isEmpty() ^ 1) == 0)) {
                log("getVoiceMailFixed load voicemail number from RoamingBroker config");
                return true;
            }
        }
        return this.mIsVoiceMailFixed;
    }

    public boolean getVoiceMailFixed(String carrier, int slotId) {
        if (!(this.mVMLoaded && (carrier.equals(this.mCurrentCarrier) ^ 1) == 0)) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || (number.isEmpty() ^ 1) == 0)) {
                log("getVoiceMailFixed load voicemail number from RoamingBroker config");
                return true;
            }
        }
        return this.mIsVoiceMailFixed;
    }

    public String getVoiceMailNumber(String carrier) {
        if (!(this.mVMLoaded && (carrier.equals(this.mCurrentCarrier) ^ 1) == 0)) {
            getVoiceMailConfig(carrier);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || (number.isEmpty() ^ 1) == 0)) {
                log("load voicemail number from RoamingBroker config");
                return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            }
        }
        return this.mVMCurrentMNumber;
    }

    public String getVoiceMailNumber(String carrier, int slotId) {
        if (!(this.mVMLoaded && (carrier.equals(this.mCurrentCarrier) ^ 1) == 0)) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || (number.isEmpty() ^ 1) == 0)) {
                log("load voicemail number from RoamingBroker config");
                return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            }
        }
        return this.mVMCurrentMNumber;
    }

    public String getVoiceMailTag(String carrier) {
        if (!(this.mVMLoaded && (carrier.equals(this.mCurrentCarrier) ^ 1) == 0)) {
            getVoiceMailConfig(carrier);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || (number.isEmpty() ^ 1) == 0)) {
                log("load voicemail number from RoamingBroker config");
                return null;
            }
        }
        return this.mVMCurrentTag;
    }

    public String getVoiceMailTag(String carrier, int slotId) {
        if (!(this.mVMLoaded && (carrier.equals(this.mCurrentCarrier) ^ 1) == 0)) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || (number.isEmpty() ^ 1) == 0)) {
                log("load voicemail number from RoamingBroker config");
                return null;
            }
        }
        return this.mVMCurrentTag;
    }

    public void resetVoiceMailLoadFlag() {
        this.mVMLoaded = false;
    }

    private int getPreferedApnId() {
        int apnId = -1;
        Cursor cursor = this.mContext.getContentResolver().query(PREFERAPN_NO_UPDATE_URI, new String[]{"_id", NumMatchs.NAME, "apn"}, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String apnName = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            log("getPreferedApnId: " + apnId + ", apn: " + apnName + ", name: " + cursor.getString(cursor.getColumnIndexOrThrow(NumMatchs.NAME)));
        }
        if (cursor != null) {
            cursor.close();
        }
        return apnId;
    }

    private int getPreferedApnId(int slotId) {
        int apnId = -1;
        Cursor cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId), new String[]{"_id", NumMatchs.NAME, "apn"}, null, null, NumMatchs.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            String apnName = cursor.getString(cursor.getColumnIndexOrThrow("apn"));
            log("getPreferedApnId: " + apnId + ", apn: " + apnName + ", name: " + cursor.getString(cursor.getColumnIndexOrThrow(NumMatchs.NAME)) + " for slot" + slotId);
        }
        if (cursor != null) {
            cursor.close();
        }
        return apnId;
    }

    private void log(String string) {
        Rlog.d("VoiceMailConstantsEx", string);
    }

    public void setVoicemailInClaro(int voicemailPriorityMode) {
        Rlog.d("VoiceMailConstantsEx", "setVoicemailInClaro from hw_default.xml");
        this.voicemailPriorityMode = voicemailPriorityMode;
        Rlog.d("VoiceMailConstantsEx", "setVoicemailInClaro " + this.voicemailPriorityMode);
    }
}
