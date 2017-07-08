package com.android.internal.telephony.uicc;

import android.content.ContentResolver;
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
    private static final Uri PREFERAPN_NO_UPDATE_URI = null;
    private static final int SUB1 = 0;
    private static final int SUB2 = 1;
    static final int VMMODE_CUST_SIM_SP = 3;
    static final int VMMODE_SIM_CUST_SP = 1;
    static final int VMMODE_SIM_SP_CUST = 2;
    private Context mContext;
    private String mCurrentCarrier;
    private boolean mIsVoiceMailFixed;
    private int mSlotId;
    private String mVMCurrentMNumber;
    private String mVMCurrentTag;
    private boolean mVMLoaded;
    private String mVMNumberOnSim;
    private String mVMTagOnSim;
    private int voicemailPriorityMode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.VoiceMailConstantsEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.VoiceMailConstantsEx.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.VoiceMailConstantsEx.<clinit>():void");
    }

    public VoiceMailConstantsEx(Context c, int slotId) {
        this.mIsVoiceMailFixed = false;
        this.mVMNumberOnSim = null;
        this.mVMTagOnSim = null;
        this.mCurrentCarrier = null;
        this.mVMCurrentMNumber = null;
        this.mVMCurrentTag = null;
        this.mVMLoaded = false;
        this.mSlotId = SUB1;
        this.mContext = c;
        this.mSlotId = slotId;
        this.voicemailPriorityMode = SystemProperties.getInt("ro.config.vm_prioritymode", VMMODE_CUST_SIM_SP);
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
        Log.d(MY_TAG, "Before clearVoicemailLoadedFlag, mVMLoaded = " + this.mVMLoaded);
        this.mVMLoaded = false;
    }

    public boolean containsCarrier(String carrier) {
        boolean z = false;
        if (HwTelephonyFactory.getHwPhoneManager().isVirtualNet() && carrier != null) {
            z = (!carrier.equals(HwTelephonyFactory.getHwPhoneManager().getVirtualNetNumeric()) || HwTelephonyFactory.getHwPhoneManager().getVirtualNetVoiceMailNumber() == null) ? false : HwTelephonyFactory.getHwPhoneManager().getVirtualNetVoiceMailNumber().length() > 0;
            log("containsCarrier load vitualNet Config");
            if (!ApnReminder.getInstance(this.mContext).isPopupApnSettingsEmpty()) {
                z = carrier.equals(HwTelephonyFactory.getHwPhoneManager().getVirtualNetNumeric());
                log("containsCarrier load apnReminder Config");
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || number.isEmpty()) || super.containsCarrier(carrier)) {
                log("containsCarrier load voicemail number from RoamingBroker config");
                z = true;
            }
        } else {
            z = super.containsCarrier(carrier);
        }
        if (!z) {
            log("containsCarrier VoiceMailConfig doesn't contains the carrier" + carrier);
        }
        return z;
    }

    public boolean containsCarrier(String carrier, int slotId) {
        boolean z = false;
        int subId = slotId;
        log("containsCarrier slotId = " + slotId);
        if (!(slotId == 0 || VMMODE_SIM_CUST_SP == slotId)) {
            subId = SUB1;
        }
        if (VirtualNet.isVirtualNet(subId) && carrier != null) {
            z = (!carrier.equals(VirtualNet.getCurrentVirtualNet(subId).getNumeric()) || VirtualNet.getCurrentVirtualNet(subId).getVoiceMailNumber() == null) ? false : VirtualNet.getCurrentVirtualNet(subId).getVoiceMailNumber().length() > 0;
            log("containsCarrier load vitualNet Config");
            if (!ApnReminder.getInstance(this.mContext, subId).isPopupApnSettingsEmpty()) {
                z = carrier.equals(VirtualNet.getCurrentVirtualNet(subId).getNumeric());
                log("containsCarrier load apnReminder Config");
            }
        } else if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(subId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || number.isEmpty()) || super.containsCarrier(carrier)) {
                log("containsCarrier load voicemail number from RoamingBroker config");
                z = true;
            }
        } else {
            z = super.containsCarrier(carrier);
        }
        if (!z) {
            log("containsCarrier VoiceMailConfig doesn't contains the carrier" + carrier + " for sub" + subId);
        }
        return z;
    }

    public void getVoiceMailConfig(String carrier) {
        Log.d(MY_TAG, "voicemail number Priority = " + getVoicemailPriorityMode());
        this.mCurrentCarrier = carrier;
        if (this.mVMNumberOnSim != null && !this.mVMNumberOnSim.isEmpty() && this.voicemailPriorityMode != VMMODE_CUST_SIM_SP) {
            Log.d(MY_TAG, "load voicemail number from sim");
            this.mIsVoiceMailFixed = false;
            this.mVMCurrentMNumber = this.mVMNumberOnSim;
            this.mVMCurrentTag = this.mVMTagOnSim;
            this.mVMLoaded = true;
        } else if ((this.mVMNumberOnSim == null || this.mVMNumberOnSim.isEmpty()) && this.voicemailPriorityMode == VMMODE_SIM_SP_CUST) {
            String number;
            this.mIsVoiceMailFixed = false;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            Object mIccId = null;
            if (((TelephonyManager) this.mContext.getSystemService("phone")) != null) {
                mIccId = TelephonyManager.getDefault().getSimSerialNumber();
            }
            if (TextUtils.isEmpty(mIccId)) {
                number = sp.getString("vm_number_key" + this.mSlotId, null);
            } else {
                number = sp.getString(mIccId + this.mSlotId, null);
            }
            if (number != null) {
                Log.d(MY_TAG, "load voicemail number from SP");
                this.mVMCurrentMNumber = number;
                this.mVMLoaded = true;
                return;
            }
        } else if (this.voicemailPriorityMode == VMMODE_CUST_SIM_SP || this.voicemailPriorityMode == VMMODE_SIM_CUST_SP) {
            this.mIsVoiceMailFixed = true;
        }
        if (!this.mVMLoaded && HwTelephonyFactory.getHwPhoneManager().isVirtualNet()) {
            Log.d(MY_TAG, "try to load voicemail number from virtualNet");
            this.mVMCurrentMNumber = HwTelephonyFactory.getHwPhoneManager().getVirtualNetVoiceMailNumber();
            this.mVMCurrentTag = HwTelephonyFactory.getHwPhoneManager().getVirtualNetVoicemailTag();
            ApnReminder apnReminder = ApnReminder.getInstance(this.mContext);
            if (!apnReminder.isPopupApnSettingsEmpty()) {
                this.mVMCurrentMNumber = apnReminder.getVoiceMailNumberByPreferedApn(getPreferedApnId(), this.mVMCurrentMNumber);
                this.mVMCurrentTag = apnReminder.getVoiceMailTagByPreferedApn(getPreferedApnId(), this.mVMCurrentTag);
                log("load voicemail number and tag from apnReminder");
                this.mVMLoaded = true;
                return;
            } else if (this.mVMCurrentMNumber != null && this.mVMCurrentMNumber.length() > 0) {
                Log.d(MY_TAG, "loaded voicemail number from virtualNet");
                this.mVMLoaded = true;
                return;
            }
        }
        if (!this.mVMLoaded) {
            Log.d(MY_TAG, "load voicemail number from cust");
            this.mVMCurrentMNumber = super.getVoiceMailNumber(carrier);
            if (!isVMTagNotFromConf(carrier)) {
                this.mVMCurrentTag = super.getVoiceMailTag(carrier);
            }
        }
        this.mVMLoaded = true;
    }

    public void getVoiceMailConfig(String carrier, int slotId) {
        Log.d(MY_TAG, "voicemail number Priority = " + getVoicemailPriorityMode());
        this.mCurrentCarrier = carrier;
        int subId = slotId;
        log("getVoiceMailConfig slotId = " + slotId);
        if (!(slotId == 0 || VMMODE_SIM_CUST_SP == slotId)) {
            subId = SUB1;
        }
        if (this.mVMNumberOnSim == null || this.mVMNumberOnSim.isEmpty() || this.voicemailPriorityMode == VMMODE_CUST_SIM_SP) {
            if ((this.mVMNumberOnSim == null || this.mVMNumberOnSim.isEmpty()) && this.voicemailPriorityMode == VMMODE_SIM_SP_CUST) {
                this.mIsVoiceMailFixed = false;
                String number = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("vm_number_key" + subId, null);
                if (number != null) {
                    Log.d(MY_TAG, "load voicemail number from SP");
                    this.mVMCurrentMNumber = number;
                    this.mVMLoaded = true;
                    return;
                }
            } else if (this.voicemailPriorityMode == VMMODE_CUST_SIM_SP || this.voicemailPriorityMode == VMMODE_SIM_CUST_SP) {
                this.mIsVoiceMailFixed = true;
            }
            ApnReminder apnReminder = ApnReminder.getInstance(this.mContext, subId);
            if (apnReminder.isPopupApnSettingsEmpty()) {
                if (VirtualNet.isVirtualNet(subId)) {
                    Log.d(MY_TAG, "try to load voicemail number from virtualNet");
                    this.mVMCurrentMNumber = VirtualNet.getCurrentVirtualNet(subId).getVoiceMailNumber();
                    this.mVMCurrentTag = VirtualNet.getCurrentVirtualNet(subId).getVoicemailTag();
                    if (this.mVMCurrentMNumber != null && this.mVMCurrentMNumber.length() > 0) {
                        Log.d(MY_TAG, "loaded voicemail number from virtualNet");
                        this.mVMLoaded = true;
                        return;
                    }
                }
                if (!this.mVMLoaded) {
                    Log.d(MY_TAG, "load voicemail number from cust");
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
        Log.d(MY_TAG, "load voicemail number from sim");
        this.mIsVoiceMailFixed = false;
        this.mVMCurrentMNumber = this.mVMNumberOnSim;
        this.mVMCurrentTag = this.mVMTagOnSim;
        this.mVMLoaded = true;
    }

    private boolean isVMTagNotFromConf(String carrier) {
        String strVMTagNotFromConf = System.getString(this.mContext.getContentResolver(), "hw_vmtag_not_from_conf");
        if (!(TextUtils.isEmpty(strVMTagNotFromConf) || TextUtils.isEmpty(carrier))) {
            String[] areaArray = strVMTagNotFromConf.split(",");
            int length = areaArray.length;
            for (int i = SUB1; i < length; i += VMMODE_SIM_CUST_SP) {
                if (areaArray[i].equals(carrier)) {
                    Log.d(MY_TAG, "not load voicemail Tag from cust");
                    return true;
                }
            }
        }
        Log.d(MY_TAG, "load voicemail Tag from cust");
        return false;
    }

    public boolean getVoiceMailFixed(String carrier) {
        if (!(this.mVMLoaded && carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || number.isEmpty())) {
                log("getVoiceMailFixed load voicemail number from RoamingBroker config");
                return true;
            }
        }
        return this.mIsVoiceMailFixed;
    }

    public boolean getVoiceMailFixed(String carrier, int slotId) {
        if (!(this.mVMLoaded && carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || number.isEmpty())) {
                log("getVoiceMailFixed load voicemail number from RoamingBroker config");
                return true;
            }
        }
        return this.mIsVoiceMailFixed;
    }

    public String getVoiceMailNumber(String carrier) {
        if (!(this.mVMLoaded && carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || number.isEmpty())) {
                log("load voicemail number from RoamingBroker config");
                return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            }
        }
        return this.mVMCurrentMNumber;
    }

    public String getVoiceMailNumber(String carrier, int slotId) {
        if (!(this.mVMLoaded && carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || number.isEmpty())) {
                log("load voicemail number from RoamingBroker config");
                return HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            }
        }
        return this.mVMCurrentMNumber;
    }

    public String getVoiceMailTag(String carrier) {
        if (!(this.mVMLoaded && carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated()) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || number.isEmpty())) {
                log("load voicemail number from RoamingBroker config");
                return null;
            }
        }
        return this.mVMCurrentTag;
    }

    public String getVoiceMailTag(String carrier, int slotId) {
        if (!(this.mVMLoaded && carrier.equals(this.mCurrentCarrier))) {
            getVoiceMailConfig(carrier, slotId);
        }
        if (HwTelephonyFactory.getHwPhoneManager().isRoamingBrokerActivated(Integer.valueOf(slotId))) {
            String number = HwTelephonyFactory.getHwPhoneManager().getRoamingBrokerVoicemail();
            if (!(number == null || number.isEmpty())) {
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
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri uri = PREFERAPN_NO_UPDATE_URI;
        String[] strArr = new String[VMMODE_CUST_SIM_SP];
        strArr[SUB1] = "_id";
        strArr[VMMODE_SIM_CUST_SP] = NumMatchs.NAME;
        strArr[VMMODE_SIM_SP_CUST] = "apn";
        Cursor cursor = contentResolver.query(uri, strArr, null, null, NumMatchs.DEFAULT_SORT_ORDER);
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
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri withAppendedId = ContentUris.withAppendedId(PREFERAPN_NO_UPDATE_URI, (long) slotId);
        String[] strArr = new String[VMMODE_CUST_SIM_SP];
        strArr[SUB1] = "_id";
        strArr[VMMODE_SIM_CUST_SP] = NumMatchs.NAME;
        strArr[VMMODE_SIM_SP_CUST] = "apn";
        Cursor cursor = contentResolver.query(withAppendedId, strArr, null, null, NumMatchs.DEFAULT_SORT_ORDER);
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
        Rlog.d(MY_TAG, string);
    }

    public void setVoicemailInClaro(int voicemailPriorityMode) {
        Rlog.d(MY_TAG, "setVoicemailInClaro from hw_default.xml");
        this.voicemailPriorityMode = voicemailPriorityMode;
        Rlog.d(MY_TAG, "setVoicemailInClaro " + this.voicemailPriorityMode);
    }
}
