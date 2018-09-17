package com.android.internal.telephony.uicc;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ProxyController;
import java.util.ArrayList;

public abstract class AbstractIccRecords extends Handler {
    private static final String ACTION_SIM_ICCID_READY = "android.intent.action.ACTION_SIM_ICCID_READY";
    private static final boolean DBG = true;
    public static final int EVENT_EONS = 100;
    private static final String EXTRA_SIM_ICCID = "iccid";
    public static final int HW_CUST_EVENT_BASE = 100;
    protected static final boolean HW_IS_CHINA_TELECOM;
    protected static final boolean HW_SIM_REFRESH = SystemProperties.getBoolean("ro.config.hwft_simrefresh", false);
    public static final int KT_SKT_CARD = 1;
    public static final int LGU_CARD = 0;
    public static final int LGU_PSEUDO_CARD = 3;
    protected static final String LOG_TAG_LGU = "LGU SIMRecords";
    public static final int OTHERS_CARD = 2;
    private static final int SIM_NUMBER_LENGTH_DEFAULT = 20;
    private static final String TAG = "AbstractIccRecords";
    private static boolean bAdnNumberLengthDefault = false;
    private static boolean bEmailAnrSupport = false;
    protected RegistrantList mCsgRecordsLoadedRegistrants = new RegistrantList();
    protected boolean mCsglexist = false;
    protected RegistrantList mIccIDLoadRegistrants = new RegistrantList();
    protected RegistrantList mIccRefreshRegistrants = new RegistrantList();
    protected boolean mImsiLoad = false;
    protected boolean mIs3Gphonebook = false;
    protected boolean mIsGetPBRDone = false;

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", ProxyController.MODEM_0).equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", ProxyController.MODEM_0).equals("156");
        } else {
            equals = false;
        }
        HW_IS_CHINA_TELECOM = equals;
    }

    public void registerForCsgRecordsLoaded(Handler h, int what, Object obj) {
        this.mCsgRecordsLoadedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForCsgRecordsLoaded(Handler h) {
        this.mCsgRecordsLoadedRegistrants.remove(h);
    }

    public synchronized void registerForIccRefresh(Handler h, int what, Object obj) {
        this.mIccRefreshRegistrants.add(new Registrant(h, what, obj));
    }

    public synchronized void unRegisterForIccRefresh(Handler h) {
        this.mIccRefreshRegistrants.remove(h);
    }

    public boolean getIccidSwitch() {
        return SystemProperties.getBoolean("ro.config.readiccid_switch", false);
    }

    public void sendIccidDoneBroadcast(String mIccId) {
        Intent intent = new Intent(ACTION_SIM_ICCID_READY);
        intent.putExtra(EXTRA_SIM_ICCID, mIccId);
        rlog(" SimRecords sendIccidDoneBroadcast EXTRA_SIM_ICCID=" + SubscriptionInfo.givePrintableIccid(mIccId));
        ActivityManagerNative.broadcastStickyIntent(intent, null, 0);
    }

    public boolean beforeHandleSimRefresh(IccRefreshResponse refreshResponse) {
        return false;
    }

    public boolean afterHandleSimRefresh(IccRefreshResponse refreshResponse) {
        return false;
    }

    public boolean beforeHandleRuimRefresh(IccRefreshResponse refreshResponse) {
        return false;
    }

    public boolean afterHandleRuimRefresh(IccRefreshResponse refreshResponse) {
        return false;
    }

    public byte[] getGID1() {
        return new byte[]{(byte) 0};
    }

    public void setVoiceMailNumber(String voiceNumber) {
    }

    public void loadFile(String matchPath, String matchFile) {
    }

    protected void onOperatorNumericLoadedHw() {
    }

    protected void onAllRecordsLoadedHw() {
    }

    protected void onIccIdLoadedHw() {
    }

    protected void onImsiLoadedHw() {
    }

    protected void loadGID1() {
    }

    protected static void rlog(String string) {
        Rlog.d(TAG, string);
    }

    protected void custMncLength(String mcc) {
    }

    public String getOperatorNumericEx(ContentResolver cr, String name) {
        return null;
    }

    public static boolean getEmailAnrSupport() {
        return bEmailAnrSupport;
    }

    public static void loadEmailAnrSupportFlag(Context c) {
        boolean z = false;
        try {
            if (System.getInt(c.getContentResolver(), "is_email_anr_support", 0) > 0) {
                z = true;
            }
            bEmailAnrSupport = z;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getAdnLongNumberSupport() {
        return bAdnNumberLengthDefault;
    }

    public static void loadAdnLongNumberFlag(Context c) {
        try {
            bAdnNumberLengthDefault = Systemex.getInt(c.getContentResolver(), "sim_number_length", 20) > 20;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean has3Gphonebook() {
        return this.mIs3Gphonebook;
    }

    public boolean isGetPBRDone() {
        return this.mIsGetPBRDone;
    }

    protected void getPbrRecordSize() {
    }

    public int getSlotId() {
        return 0;
    }

    public void updateSarMnc(String imsi) {
        if (imsi != null && imsi.length() >= 3 && PhoneFactory.getDefaultSubscription() == getSlotId()) {
            SystemProperties.set("reduce.sar.imsi.mnc", imsi.substring(0, 3));
        }
    }

    protected boolean checkFileInServiceTable(int efid, UsimServiceTable usimServiceTable, byte[] data) {
        return true;
    }

    protected void loadEons() {
    }

    public String getEons() {
        return null;
    }

    public boolean isEonsDisabled() {
        return true;
    }

    public boolean updateEons(String regOperator, int lac) {
        return true;
    }

    public ArrayList<OperatorInfo> getEonsForAvailableNetworks(ArrayList<OperatorInfo> arrayList) {
        return null;
    }

    protected void initFdnPsStatus(int slotId) {
    }

    public void sendDualSimChangeBroadcast(boolean isSimImsiRefreshing, String mLastImsi, String mImsi) {
    }

    public String decodeCdmaImsi(byte[] data) {
        return null;
    }

    public synchronized void registerForLoadIccID(Handler h, int what, Object obj) {
        this.mIccIDLoadRegistrants.add(new Registrant(h, what, obj));
    }

    public synchronized void unRegisterForLoadIccID(Handler h) {
        this.mIccIDLoadRegistrants.remove(h);
    }

    public void loadCardSpecialFile(int fileid) {
    }

    public String getActingHplmn() {
        return null;
    }

    public void setMdnNumber(String alphaTag, String number, Message onComplete) {
    }

    public boolean getImsiReady() {
        return this.mImsiLoad;
    }

    public byte[] getOcsgl() {
        return new byte[0];
    }

    public boolean getCsglexist() {
        return this.mCsglexist;
    }

    public void setCsglexist(boolean csglExist) {
        this.mCsglexist = csglExist;
    }

    protected void refreshCardType() {
    }
}
