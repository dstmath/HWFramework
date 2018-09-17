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
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.PhoneFactory;
import java.util.ArrayList;

public abstract class AbstractIccRecords extends Handler {
    private static final String ACTION_SIM_ICCID_READY = "android.intent.action.ACTION_SIM_ICCID_READY";
    private static final boolean DBG = true;
    public static final int EVENT_EONS = 100;
    private static final String EXTRA_SIM_ICCID = "iccid";
    public static final int HW_CUST_EVENT_BASE = 100;
    protected static final boolean HW_IS_CHINA_TELECOM = false;
    protected static final boolean HW_SIM_REFRESH = false;
    private static final int SIM_NUMBER_LENGTH_DEFAULT = 20;
    private static final String TAG = "AbstractIccRecords";
    private static boolean bAdnNumberLengthDefault;
    private static boolean bEmailAnrSupport;
    protected RegistrantList mCsgRecordsLoadedRegistrants;
    protected boolean mCsglexist;
    protected RegistrantList mIccIDLoadRegistrants;
    protected RegistrantList mIccRefreshRegistrants;
    protected boolean mImsiLoad;
    protected boolean mIs3Gphonebook;
    protected boolean mIsGetPBRDone;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.AbstractIccRecords.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.AbstractIccRecords.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.AbstractIccRecords.<clinit>():void");
    }

    public AbstractIccRecords() {
        this.mIs3Gphonebook = HW_SIM_REFRESH;
        this.mIsGetPBRDone = HW_SIM_REFRESH;
        this.mCsglexist = HW_SIM_REFRESH;
        this.mIccRefreshRegistrants = new RegistrantList();
        this.mImsiLoad = HW_SIM_REFRESH;
        this.mCsgRecordsLoadedRegistrants = new RegistrantList();
        this.mIccIDLoadRegistrants = new RegistrantList();
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
        return SystemProperties.getBoolean("ro.config.readiccid_switch", HW_SIM_REFRESH);
    }

    public void sendIccidDoneBroadcast(String mIccId) {
        Intent intent = new Intent(ACTION_SIM_ICCID_READY);
        intent.putExtra(EXTRA_SIM_ICCID, mIccId);
        rlog(" SimRecords sendIccidDoneBroadcast EXTRA_SIM_ICCID=" + mIccId);
        ActivityManagerNative.broadcastStickyIntent(intent, null, 0);
    }

    public boolean beforeHandleSimRefresh(IccRefreshResponse refreshResponse) {
        return HW_SIM_REFRESH;
    }

    public boolean afterHandleSimRefresh(IccRefreshResponse refreshResponse) {
        return HW_SIM_REFRESH;
    }

    public boolean beforeHandleRuimRefresh(IccRefreshResponse refreshResponse) {
        return HW_SIM_REFRESH;
    }

    public boolean afterHandleRuimRefresh(IccRefreshResponse refreshResponse) {
        return HW_SIM_REFRESH;
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
        boolean z = HW_SIM_REFRESH;
        try {
            if (Systemex.getInt(c.getContentResolver(), "is_email_anr_support", 0) > 0) {
                z = DBG;
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
            bAdnNumberLengthDefault = Systemex.getInt(c.getContentResolver(), "sim_number_length", SIM_NUMBER_LENGTH_DEFAULT) > SIM_NUMBER_LENGTH_DEFAULT ? DBG : HW_SIM_REFRESH;
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
        return DBG;
    }

    protected void loadEons() {
    }

    public String getEons() {
        return null;
    }

    public boolean isEonsDisabled() {
        return DBG;
    }

    public boolean updateEons(String regOperator, int lac) {
        return DBG;
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
}
