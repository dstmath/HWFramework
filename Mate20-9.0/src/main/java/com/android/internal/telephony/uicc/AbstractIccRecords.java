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
import android.provider.Settings;
import android.provider.SettingsEx;
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
    public static final int HW_CARRIER_FILE_C_IMSI = 4;
    public static final int HW_CARRIER_FILE_GID1 = 5;
    public static final int HW_CARRIER_FILE_GID2 = 6;
    public static final int HW_CARRIER_FILE_G_IMSI = 2;
    public static final int HW_CARRIER_FILE_G_MCCMNC = 3;
    public static final int HW_CARRIER_FILE_ICCID = 1;
    public static final int HW_CARRIER_FILE_SPN = 7;
    public static final int HW_CUST_EVENT_BASE = 100;
    protected static final boolean HW_IS_CHINA_TELECOM = (SystemProperties.get("ro.config.hw_opta", ProxyController.MODEM_0).equals("92") && SystemProperties.get("ro.config.hw_optb", ProxyController.MODEM_0).equals("156"));
    protected static final boolean HW_SIM_REFRESH = SystemProperties.getBoolean("ro.config.hwft_simrefresh", false);
    public static final int KT_SKT_CARD = 1;
    public static final int LGU_CARD = 0;
    public static final int LGU_PSEUDO_CARD = 3;
    protected static final String LOG_TAG_LGU = "LGU SIMRecords";
    protected static final String[] MCCMNC_CODES_HAVING_2DIGITS_MNC = {"40400", "40401", "40402", "40403", "40404", "40405", "40407", "40409", "40410", "40411", "40412", "40413", "40414", "40415", "40416", "40417", "40418", "40419", "40420", "40421", "40422", "40424", "40425", "40427", "40428", "40429", "40430", "40431", "40433", "40434", "40435", "40436", "40437", "40438", "40440", "40441", "40442", "40443", "40444", "40445", "40446", "40449", "40450", "40451", "40452", "40453", "40454", "40455", "40456", "40457", "40458", "40459", "40460", "40462", "40464", "40466", "40467", "40468", "40469", "40470", "40471", "40472", "40473", "40474", "40475", "40476", "40477", "40478", "40479", "40480", "40481", "40482", "40483", "40484", "40485", "40486", "40487", "40488", "40489", "40490", "40491", "40492", "40493", "40494", "40495", "40496", "40497", "40498", "40501", "40505", "40506", "40507", "40508", "40509", "40510", "40511", "40512", "40513", "40514", "40515", "40517", "40518", "40519", "40520", "40521", "40522", "40523", "40524", "40548", "40551", "40552", "40553", "40554", "40555", "40556", "40566", "40567", "40570", "23210"};
    protected static final String[] MCCMNC_CODES_HAVING_2DIGITS_MNC_ZERO_PREFIX_RELIANCE = {"40503", "40504"};
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
    protected String mMdn;

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
        return new byte[]{0};
    }

    public void setVoiceMailNumber(String voiceNumber) {
    }

    public void loadFile(String matchPath, String matchFile) {
    }

    /* access modifiers changed from: protected */
    public void onOperatorNumericLoadedHw() {
    }

    /* access modifiers changed from: protected */
    public void onAllRecordsLoadedHw() {
    }

    /* access modifiers changed from: protected */
    public void onIccIdLoadedHw() {
    }

    /* access modifiers changed from: protected */
    public void onImsiLoadedHw() {
    }

    /* access modifiers changed from: protected */
    public void loadGID1() {
    }

    protected static void rlog(String string) {
        Rlog.d(TAG, string);
    }

    /* access modifiers changed from: protected */
    public void custMncLength(String mcc) {
    }

    public String getOperatorNumericEx(ContentResolver cr, String name) {
        return null;
    }

    public static boolean getEmailAnrSupport() {
        return bEmailAnrSupport;
    }

    public static void loadEmailAnrSupportFlag(Context c) {
        try {
            boolean z = false;
            if (Settings.System.getInt(c.getContentResolver(), "is_email_anr_support", 0) > 0) {
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
            bAdnNumberLengthDefault = SettingsEx.Systemex.getInt(c.getContentResolver(), "sim_number_length", 20) > 20;
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

    /* access modifiers changed from: protected */
    public void getPbrRecordSize() {
    }

    public int getSlotId() {
        return 0;
    }

    public void updateSarMnc(String imsi) {
        if (imsi != null && imsi.length() >= 3 && PhoneFactory.getDefaultSubscription() == getSlotId()) {
            SystemProperties.set("reduce.sar.imsi.mnc", imsi.substring(0, 3));
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkFileInServiceTable(int efid, UsimServiceTable usimServiceTable, byte[] data) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void loadEons() {
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

    /* access modifiers changed from: protected */
    public void initFdnPsStatus(int slotId) {
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

    /* access modifiers changed from: protected */
    public void refreshCardType() {
    }

    public boolean isHwCustDataRoamingOpenArea() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateCarrierFile(int slotId, int fileType, String fileValue) {
    }

    public String getMdn() {
        return this.mMdn;
    }

    /* access modifiers changed from: protected */
    public void loadSimMatchedFileFromRilCache() {
    }

    /* access modifiers changed from: protected */
    public void onGetSimMatchedFileDone(Message msg) {
    }

    /* access modifiers changed from: protected */
    public void updateCsimImsi(byte[] data) {
    }

    /* access modifiers changed from: protected */
    public String getVmSimImsi() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void setVmSimImsi(String imsi) {
    }

    /* access modifiers changed from: protected */
    public void sendCspChangedBroadcast(boolean oldCspPlmnEnabled, boolean CspPlmnEnabled) {
    }

    /* access modifiers changed from: protected */
    public void adapterForDoubleRilChannelAfterImsiReady() {
    }
}
