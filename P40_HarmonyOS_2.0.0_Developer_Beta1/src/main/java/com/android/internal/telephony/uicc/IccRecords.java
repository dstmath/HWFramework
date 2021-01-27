package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.provider.SettingsEx;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.test.SimulatedCommands;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.util.ArrayUtils;
import com.huawei.internal.telephony.OperatorInfoEx;
import com.huawei.internal.telephony.uicc.AdnRecordCacheEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class IccRecords extends Handler implements IIccRecordsInner, IccConstants {
    public static final int CALL_FORWARDING_STATUS_DISABLED = 0;
    public static final int CALL_FORWARDING_STATUS_ENABLED = 1;
    public static final int CALL_FORWARDING_STATUS_UNKNOWN = -1;
    public static final int CARRIER_NAME_DISPLAY_CONDITION_BITMASK_PLMN = 1;
    public static final int CARRIER_NAME_DISPLAY_CONDITION_BITMASK_SPN = 2;
    protected static final boolean DBG = true;
    public static final int DEFAULT_CARRIER_NAME_DISPLAY_CONDITION = 0;
    public static final int DEFAULT_VOICE_MESSAGE_COUNT = -2;
    static final Set<Integer> EFID_SET = new HashSet(Arrays.asList(Integer.valueOf((int) IccConstants.EF_ICCID), Integer.valueOf((int) IccConstants.EF_IST), Integer.valueOf((int) IccConstants.EF_AD), Integer.valueOf((int) IccConstants.EF_GID1), Integer.valueOf((int) IccConstants.EF_GID2), Integer.valueOf((int) IccConstants.EF_SST), Integer.valueOf((int) IccConstants.EF_SPN), Integer.valueOf((int) IccConstants.EF_SPN_CPHS), Integer.valueOf((int) IccConstants.EF_SPN_SHORT_CPHS), Integer.valueOf((int) IccConstants.EF_IMPI), 28474));
    private static final int EVENT_AKA_AUTHENTICATE_DONE = 90;
    protected static final int EVENT_APP_READY = 1;
    public static final int EVENT_CFI = 1;
    public static final int EVENT_GET_ICC_RECORD_DONE = 100;
    public static final int EVENT_MWI = 0;
    public static final int EVENT_REFRESH = 31;
    public static final int EVENT_SPN = 2;
    protected static final int HANDLER_ACTION_BASE = 1238272;
    protected static final int HANDLER_ACTION_NONE = 1238272;
    protected static final int HANDLER_ACTION_SEND_RESPONSE = 1238273;
    public static final int INVALID_CARRIER_NAME_DISPLAY_CONDITION_BITMASK = -1;
    protected static final int LOCKED_RECORDS_REQ_REASON_LOCKED = 1;
    protected static final int LOCKED_RECORDS_REQ_REASON_NETWORK_LOCKED = 2;
    protected static final int LOCKED_RECORDS_REQ_REASON_NONE = 0;
    public static final String[] MCCMNC_CODES_HAVING_3DIGITS_MNC = {"302370", "302720", SimulatedCommands.FAKE_MCC_MNC, "405025", "405026", "405027", "405028", "405029", "405030", "405031", "405032", "405033", "405034", "405035", "405036", "405037", "405038", "405039", "405040", "405041", "405042", "405043", "405044", "405045", "405046", "405047", "405750", "405751", "405752", "405753", "405754", "405755", "405756", "405799", "405800", "405801", "405802", "405803", "405804", "405805", "405806", "405807", "405808", "405809", "405810", "405811", "405812", "405813", "405814", "405815", "405816", "405817", "405818", "405819", "405820", "405821", "405822", "405823", "405824", "405825", "405826", "405827", "405828", "405829", "405830", "405831", "405832", "405833", "405834", "405835", "405836", "405837", "405838", "405839", "405840", "405841", "405842", "405843", "405844", "405845", "405846", "405847", "405848", "405849", "405850", "405851", "405852", "405853", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874", "405875", "405876", "405877", "405878", "405879", "405880", "405881", "405882", "405883", "405884", "405885", "405886", "405908", "405909", "405910", "405911", "405912", "405913", "405914", "405915", "405916", "405917", "405918", "405919", "405920", "405921", "405922", "405923", "405924", "405925", "405926", "405927", "405928", "405929", "405930", "405931", "405932", "502142", "502143", "502145", "502146", "502147", "502148"};
    public static final String PROPERTY_MCC_MATCHING_FYROM = "persist.sys.mcc_match_fyrom";
    static final int SIM_NUMBER_LENGTH_DEFAULT = 20;
    protected static final int UNINITIALIZED = -1;
    protected static final int UNKNOWN = 0;
    public static final int UNKNOWN_VOICE_MESSAGE_COUNT = -1;
    protected static final boolean VDBG = false;
    static boolean bAdnNumberLengthDefault = false;
    static boolean bEmailAnrSupport = false;
    protected static AtomicInteger sNextRequestId = new AtomicInteger(1);
    @UnsupportedAppUsage
    private IccIoResult auth_rsp;
    @UnsupportedAppUsage
    protected AdnRecordCache mAdnCache;
    protected int mCarrierNameDisplayCondition;
    CarrierTestOverride mCarrierTestOverride;
    @UnsupportedAppUsage
    protected CommandsInterface mCi;
    @UnsupportedAppUsage
    protected Context mContext;
    @UnsupportedAppUsage
    protected AtomicBoolean mDestroyed = new AtomicBoolean(false);
    protected String[] mEhplmns;
    public RegistrantList mFdnRecordsLoadedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected IccFileHandler mFh;
    protected String[] mFplmns;
    protected String mFullIccId;
    @UnsupportedAppUsage
    protected String mGid1;
    protected String mGid2;
    protected PlmnActRecord[] mHplmnActRecords;
    IHwIccRecordsEx mHwIccRecordsEx;
    @UnsupportedAppUsage
    protected String mIccId;
    @UnsupportedAppUsage
    protected String mImsi;
    protected RegistrantList mImsiReadyRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected boolean mIsVoiceMailFixed = false;
    protected AtomicBoolean mLoaded = new AtomicBoolean(false);
    @UnsupportedAppUsage
    private final Object mLock = new Object();
    protected RegistrantList mLockedRecordsLoadedRegistrants = new RegistrantList();
    protected int mLockedRecordsReqReason = 0;
    protected int mMailboxIndex = 0;
    @UnsupportedAppUsage
    protected int mMncLength = -1;
    protected String mMsisdn = null;
    protected String mMsisdnTag = null;
    protected RegistrantList mNetworkLockedRecordsLoadedRegistrants = new RegistrantList();
    protected RegistrantList mNetworkSelectionModeAutomaticRegistrants = new RegistrantList();
    protected String mNewMsisdn = null;
    protected String mNewMsisdnTag = null;
    protected RegistrantList mNewSmsRegistrants = new RegistrantList();
    protected String mNewVoiceMailNum = null;
    protected String mNewVoiceMailTag = null;
    protected PlmnActRecord[] mOplmnActRecords;
    @UnsupportedAppUsage
    protected UiccCardApplication mParentApp;
    protected final HashMap<Integer, Message> mPendingResponses = new HashMap<>();
    protected PlmnActRecord[] mPlmnActRecords;
    protected String mPnnHomeName;
    protected String mPrefLang;
    @UnsupportedAppUsage
    protected RegistrantList mRecordsEventsRegistrants = new RegistrantList();
    protected RegistrantList mRecordsLoadedRegistrants = new RegistrantList();
    protected RegistrantList mRecordsOverrideRegistrants = new RegistrantList();
    protected boolean mRecordsRequested = false;
    @UnsupportedAppUsage
    protected int mRecordsToLoad;
    protected String[] mSpdi;
    @UnsupportedAppUsage
    private String mSpn;
    protected RegistrantList mSpnUpdatedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected TelephonyManager mTelephonyManager;
    @UnsupportedAppUsage
    protected String mVoiceMailNum = null;
    protected String mVoiceMailTag = null;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CarrierNameDisplayConditionBitmask {
    }

    public interface IccRecordLoaded {
        String getEfName();

        void onRecordLoaded(AsyncResult asyncResult);
    }

    public abstract int getVoiceMessageCount();

    /* access modifiers changed from: protected */
    public abstract void handleFileUpdate(int i);

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract void log(String str);

    /* access modifiers changed from: protected */
    public abstract void loge(String str);

    /* access modifiers changed from: protected */
    public abstract void onAllRecordsLoaded();

    public abstract void onReady();

    /* access modifiers changed from: protected */
    public abstract void onRecordLoaded();

    public abstract void onRefresh(boolean z, int[] iArr);

    public abstract void setVoiceMailNumber(String str, String str2, Message message);

    public abstract void setVoiceMessageWaiting(int i, int i2);

    @Override // android.os.Handler, java.lang.Object
    public String toString() {
        String str;
        String str2;
        String str3;
        String iccIdToPrint = SubscriptionInfo.givePrintableIccid(this.mFullIccId);
        StringBuilder sb = new StringBuilder();
        sb.append("mDestroyed=");
        sb.append(this.mDestroyed);
        sb.append(" mContext=");
        sb.append(this.mContext);
        sb.append(" mCi=");
        sb.append(this.mCi);
        sb.append(" mFh=");
        sb.append(this.mFh);
        sb.append(" mParentApp=");
        sb.append(this.mParentApp);
        sb.append(" recordsToLoad=");
        sb.append(this.mRecordsToLoad);
        sb.append(" adnCache=");
        sb.append(this.mAdnCache);
        sb.append(" recordsRequested=");
        sb.append(this.mRecordsRequested);
        sb.append(" lockedRecordsReqReason=");
        sb.append(this.mLockedRecordsReqReason);
        sb.append(" iccid=");
        sb.append(iccIdToPrint);
        boolean isInTestMode = this.mCarrierTestOverride.isInTestMode();
        String str4 = PhoneConfigurationManager.SSSS;
        if (isInTestMode) {
            str = "mFakeIccid=" + this.mCarrierTestOverride.getFakeIccid();
        } else {
            str = str4;
        }
        sb.append(str);
        sb.append(" msisdnTag=");
        sb.append(this.mMsisdnTag);
        sb.append(" voiceMailNum=");
        sb.append(Rlog.pii(false, this.mVoiceMailNum));
        sb.append(" voiceMailTag=");
        sb.append(this.mVoiceMailTag);
        sb.append(" newVoiceMailNum=");
        sb.append(Rlog.pii(false, this.mNewVoiceMailNum));
        sb.append(" newVoiceMailTag=");
        sb.append(this.mNewVoiceMailTag);
        sb.append(" isVoiceMailFixed=");
        sb.append(this.mIsVoiceMailFixed);
        sb.append(" mImsi=");
        if (this.mImsi != null) {
            str2 = this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6));
        } else {
            str2 = "null";
        }
        sb.append(str2);
        if (this.mCarrierTestOverride.isInTestMode()) {
            str3 = " mFakeImsi=" + this.mCarrierTestOverride.getFakeIMSI();
        } else {
            str3 = str4;
        }
        sb.append(str3);
        sb.append(" mncLength=");
        sb.append(this.mMncLength);
        sb.append(" mailboxIndex=");
        sb.append(this.mMailboxIndex);
        sb.append(" spn=");
        sb.append(Log.HWINFO ? this.mSpn : "***");
        if (this.mCarrierTestOverride.isInTestMode()) {
            str4 = " mFakeSpn=" + this.mCarrierTestOverride.getFakeSpn();
        }
        sb.append(str4);
        return sb.toString();
    }

    public IccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        loadEmailAnrSupportFlag(c);
        loadAdnLongNumberFlag(c);
        this.mContext = c;
        this.mCi = ci;
        this.mFh = app.getIccFileHandler();
        this.mParentApp = app;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mCarrierTestOverride = new CarrierTestOverride();
        this.mCi.registerForIccRefresh(this, 31, null);
    }

    public void setCarrierTestOverride(String mccmnc, String imsi, String iccid, String gid1, String gid2, String pnn, String spn) {
        this.mCarrierTestOverride.override(mccmnc, imsi, iccid, gid1, gid2, pnn, spn);
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), mccmnc);
        this.mRecordsOverrideRegistrants.notifyRegistrants();
    }

    public void dispose() {
        this.mDestroyed.set(true);
        this.auth_rsp = null;
        synchronized (this.mLock) {
            this.mLock.notifyAll();
        }
        this.mCi.unregisterForIccRefresh(this);
        this.mParentApp = null;
        this.mFh = null;
        this.mCi = null;
        this.mContext = null;
        AdnRecordCache adnRecordCache = this.mAdnCache;
        if (adnRecordCache != null) {
            adnRecordCache.reset();
        }
        this.mLoaded.set(false);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void recordsRequired() {
    }

    public AdnRecordCache getAdnCache() {
        return this.mAdnCache;
    }

    public int storePendingResponseMessage(Message msg) {
        int key = sNextRequestId.getAndIncrement();
        synchronized (this.mPendingResponses) {
            this.mPendingResponses.put(Integer.valueOf(key), msg);
        }
        return key;
    }

    public Message retrievePendingResponseMessage(Integer key) {
        Message remove;
        synchronized (this.mPendingResponses) {
            remove = this.mPendingResponses.remove(key);
        }
        return remove;
    }

    @UnsupportedAppUsage
    public String getIccId() {
        if (!this.mCarrierTestOverride.isInTestMode() || this.mCarrierTestOverride.getFakeIccid() == null) {
            return this.mIccId;
        }
        return this.mCarrierTestOverride.getFakeIccid();
    }

    public String getFullIccId() {
        return this.mFullIccId;
    }

    @UnsupportedAppUsage
    public void registerForRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            for (int i = this.mRecordsLoadedRegistrants.size() - 1; i >= 0; i--) {
                Handler rH = ((Registrant) this.mRecordsLoadedRegistrants.get(i)).getHandler();
                if (rH != null && rH == h) {
                    return;
                }
            }
            Registrant r = new Registrant(h, what, obj);
            this.mRecordsLoadedRegistrants.add(r);
            if (this.mRecordsToLoad == 0 && this.mRecordsRequested && this.mParentApp != null && IccCardApplicationStatus.AppState.APPSTATE_READY == this.mParentApp.getState()) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    @UnsupportedAppUsage
    public void unregisterForRecordsLoaded(Handler h) {
        this.mRecordsLoadedRegistrants.remove(h);
    }

    public void registerForFdnRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            this.mFdnRecordsLoadedRegistrants.add(new Registrant(h, what, obj));
        }
    }

    public void unregisterForFdnRecordsLoaded(Handler h) {
        this.mFdnRecordsLoadedRegistrants.remove(h);
    }

    public void unregisterForRecordsOverride(Handler h) {
        this.mRecordsOverrideRegistrants.remove(h);
    }

    public void registerForRecordsOverride(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mRecordsOverrideRegistrants.add(r);
            if (getRecordsLoaded()) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void registerForLockedRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mLockedRecordsLoadedRegistrants.add(r);
            if (getLockedRecordsLoaded()) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void unregisterForLockedRecordsLoaded(Handler h) {
        this.mLockedRecordsLoadedRegistrants.remove(h);
    }

    public void registerForNetworkLockedRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mNetworkLockedRecordsLoadedRegistrants.add(r);
            if (getNetworkLockedRecordsLoaded()) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void unregisterForNetworkLockedRecordsLoaded(Handler h) {
        this.mNetworkLockedRecordsLoadedRegistrants.remove(h);
    }

    public void registerForImsiReady(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mImsiReadyRegistrants.add(r);
            if (getIMSI() != null && this.mParentApp != null && IccCardApplicationStatus.AppState.APPSTATE_READY == this.mParentApp.getState()) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void unregisterForImsiReady(Handler h) {
        this.mImsiReadyRegistrants.remove(h);
    }

    public void registerForSpnUpdate(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mSpnUpdatedRegistrants.add(r);
            if (!TextUtils.isEmpty(this.mSpn)) {
                r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        }
    }

    public void unregisterForSpnUpdate(Handler h) {
        this.mSpnUpdatedRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForRecordsEvents(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mRecordsEventsRegistrants.add(r);
        r.notifyResult(0);
        r.notifyResult(1);
    }

    @UnsupportedAppUsage
    public void unregisterForRecordsEvents(Handler h) {
        this.mRecordsEventsRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForNewSms(Handler h, int what, Object obj) {
        this.mNewSmsRegistrants.add(new Registrant(h, what, obj));
    }

    @UnsupportedAppUsage
    public void unregisterForNewSms(Handler h) {
        this.mNewSmsRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForNetworkSelectionModeAutomatic(Handler h, int what, Object obj) {
        this.mNetworkSelectionModeAutomaticRegistrants.add(new Registrant(h, what, obj));
    }

    @UnsupportedAppUsage
    public void unregisterForNetworkSelectionModeAutomatic(Handler h) {
        this.mNetworkSelectionModeAutomaticRegistrants.remove(h);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    @UnsupportedAppUsage
    public String getIMSI() {
        if (!this.mCarrierTestOverride.isInTestMode() || this.mCarrierTestOverride.getFakeIMSI() == null) {
            return this.mImsi;
        }
        return this.mCarrierTestOverride.getFakeIMSI();
    }

    public void setImsi(String inImsi) {
        this.mImsi = IccUtils.stripTrailingFs(inImsi);
        if (!Objects.equals(this.mImsi, inImsi)) {
            loge("Invalid IMSI padding digits received.");
        }
        if (TextUtils.isEmpty(this.mImsi)) {
            this.mImsi = null;
        }
        String str = this.mImsi;
        if (str != null && !str.matches("[0-9]+")) {
            loge("Invalid non-numeric IMSI digits received.");
            this.mImsi = null;
        }
        String str2 = this.mImsi;
        if (str2 != null && (str2.length() < 6 || this.mImsi.length() > 15)) {
            loge("invalid IMSI " + this.mImsi);
            this.mImsi = null;
        }
        log("IMSI: mMncLength=" + this.mMncLength);
        String str3 = this.mImsi;
        if (str3 != null && str3.length() >= 6) {
            log("IMSI: " + this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6)));
        }
        updateOperatorPlmn();
        this.mImsiReadyRegistrants.notifyRegistrants();
    }

    /* access modifiers changed from: protected */
    public void updateOperatorPlmn() {
        String imsi = getIMSI();
        if (imsi != null) {
            int i = this.mMncLength;
            if ((i == 0 || i == 2) && imsi.length() >= 6) {
                String mccmncCode = imsi.substring(0, 6);
                String[] strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                int length = strArr.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (strArr[i2].equals(mccmncCode)) {
                        this.mMncLength = 3;
                        log("IMSI: setting1 mMncLength=" + this.mMncLength);
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            if (this.mMncLength == 0) {
                try {
                    this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(imsi.substring(0, 3)));
                    log("setting2 mMncLength=" + this.mMncLength);
                } catch (NumberFormatException e) {
                    loge("Corrupt IMSI! setting3 mMncLength=" + this.mMncLength);
                }
            }
            int i3 = this.mMncLength;
            if (i3 != 0 && i3 != -1 && imsi.length() >= this.mMncLength + 3) {
                log("update mccmnc=" + imsi.substring(0, this.mMncLength + 3));
                MccTable.updateMccMncConfiguration(this.mContext, imsi.substring(0, this.mMncLength + 3));
            }
        }
    }

    public String getNAI() {
        return null;
    }

    @UnsupportedAppUsage
    public String getMsisdnNumber() {
        return this.mMsisdn;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    @UnsupportedAppUsage
    public String getGid1() {
        if (!this.mCarrierTestOverride.isInTestMode() || this.mCarrierTestOverride.getFakeGid1() == null) {
            return this.mGid1;
        }
        return this.mCarrierTestOverride.getFakeGid1();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String getGid2() {
        if (!this.mCarrierTestOverride.isInTestMode() || this.mCarrierTestOverride.getFakeGid2() == null) {
            return this.mGid2;
        }
        return this.mCarrierTestOverride.getFakeGid2();
    }

    public String getPnnHomeName() {
        if (!this.mCarrierTestOverride.isInTestMode() || this.mCarrierTestOverride.getFakePnnHomeName() == null) {
            return this.mPnnHomeName;
        }
        return this.mCarrierTestOverride.getFakePnnHomeName();
    }

    @UnsupportedAppUsage
    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        loge("setMsisdn() should not be invoked on base IccRecords");
        AsyncResult.forMessage(onComplete).exception = new IccIoResult(106, 130, (byte[]) null).getException();
        onComplete.sendToTarget();
    }

    public String getMsisdnAlphaTag() {
        return this.mMsisdnTag;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String getVoiceMailNumber() {
        return this.mVoiceMailNum;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    @UnsupportedAppUsage
    public String getServiceProviderName() {
        if (this.mCarrierTestOverride.isInTestMode() && this.mCarrierTestOverride.getFakeSpn() != null) {
            return this.mCarrierTestOverride.getFakeSpn();
        }
        String providerName = this.mSpn;
        UiccCardApplication parentApp = this.mParentApp;
        String str = "***";
        if (parentApp != null) {
            UiccProfile profile = parentApp.getUiccProfile();
            if (profile != null) {
                String brandOverride = profile.getOperatorBrandOverride();
                if (brandOverride != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("override, providerName=");
                    if (Log.HWINFO) {
                        str = providerName;
                    }
                    sb.append(str);
                    log(sb.toString());
                    return brandOverride;
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append("no brandOverride, providerName=");
                if (Log.HWINFO) {
                    str = providerName;
                }
                sb2.append(str);
                log(sb2.toString());
                return providerName;
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append("card is null, providerName=");
            if (Log.HWINFO) {
                str = providerName;
            }
            sb3.append(str);
            log(sb3.toString());
            return providerName;
        }
        StringBuilder sb4 = new StringBuilder();
        sb4.append("mParentApp is null, providerName=");
        if (Log.HWINFO) {
            str = providerName;
        }
        sb4.append(str);
        log(sb4.toString());
        return providerName;
    }

    /* access modifiers changed from: protected */
    public void setServiceProviderName(String spn) {
        if (!TextUtils.equals(this.mSpn, spn)) {
            this.mSpn = spn != null ? spn.trim() : null;
            this.mSpnUpdatedRegistrants.notifyRegistrants();
        }
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    @UnsupportedAppUsage
    public boolean getRecordsLoaded() {
        return this.mRecordsToLoad == 0 && this.mRecordsRequested;
    }

    /* access modifiers changed from: protected */
    public boolean getLockedRecordsLoaded() {
        return this.mRecordsToLoad == 0 && this.mLockedRecordsReqReason == 1;
    }

    /* access modifiers changed from: protected */
    public boolean getNetworkLockedRecordsLoaded() {
        return this.mRecordsToLoad == 0 && this.mLockedRecordsReqReason == 2;
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 31) {
            AsyncResult ar = (AsyncResult) msg.obj;
            log("Card REFRESH occurred: ");
            if (ar.exception == null) {
                handleRefresh((IccRefreshResponse) ar.result);
                return;
            }
            loge("Icc refresh Exception: " + ar.exception);
        } else if (i == EVENT_AKA_AUTHENTICATE_DONE) {
            AsyncResult ar2 = (AsyncResult) msg.obj;
            this.auth_rsp = null;
            log("EVENT_AKA_AUTHENTICATE_DONE");
            if (ar2.exception != null) {
                loge("Exception ICC SIM AKA: " + ar2.exception);
            } else {
                try {
                    this.auth_rsp = (IccIoResult) ar2.result;
                    log("ICC SIM AKA: auth_rsp = " + this.auth_rsp);
                } catch (Exception e) {
                    loge("Failed to parse ICC SIM AKA contents");
                }
            }
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
        } else if (i != 100) {
            super.handleMessage(msg);
        } else {
            try {
                AsyncResult ar3 = (AsyncResult) msg.obj;
                IccRecordLoaded recordLoaded = (IccRecordLoaded) ar3.userObj;
                log(recordLoaded.getEfName() + " LOADED");
                if (ar3.exception != null) {
                    loge("Record Load Exception: " + ar3.exception);
                } else {
                    recordLoaded.onRecordLoaded(ar3);
                }
            } catch (RuntimeException exc) {
                loge("Exception parsing SIM record: " + exc);
            } finally {
                onRecordLoaded();
            }
        }
    }

    public String getSimLanguage() {
        return this.mPrefLang;
    }

    /* access modifiers changed from: protected */
    public void setSimLanguage(byte[] efLi, byte[] efPl) {
        String[] locales = this.mContext.getAssets().getLocales();
        try {
            this.mPrefLang = findBestLanguage(efLi, locales);
        } catch (UnsupportedEncodingException e) {
            log("Unable to parse EF-LI: " + Arrays.toString(efLi));
        }
        if (this.mPrefLang == null) {
            try {
                this.mPrefLang = findBestLanguage(efPl, locales);
            } catch (UnsupportedEncodingException e2) {
                log("Unable to parse EF-PL: " + Arrays.toString(efLi));
            }
        }
    }

    protected static String findBestLanguage(byte[] languages, String[] locales) throws UnsupportedEncodingException {
        if (languages == null || locales == null) {
            return null;
        }
        for (int i = 0; i + 1 < languages.length; i += 2) {
            String lang = new String(languages, i, 2, "ISO-8859-1");
            for (int j = 0; j < locales.length; j++) {
                if (locales[j] != null && locales[j].length() >= 2 && locales[j].substring(0, 2).equalsIgnoreCase(lang)) {
                    return lang;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void onIccRefreshInit() {
        this.mAdnCache.reset();
        this.mMncLength = -1;
        UiccCardApplication parentApp = this.mParentApp;
        if (parentApp != null && parentApp.getState() == IccCardApplicationStatus.AppState.APPSTATE_READY) {
            sendMessage(obtainMessage(1));
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void handleRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null || this.mHwIccRecordsEx == null) {
            log("handleRefresh received without input, mHwIccRecordsEx = " + this.mHwIccRecordsEx);
        } else if (TextUtils.isEmpty(refreshResponse.aid) || refreshResponse.aid.equals(this.mParentApp.getAid())) {
            if (this instanceof SIMRecords) {
                log("before sim refresh");
                if (this.mHwIccRecordsEx.beforeHandleSimRefresh(refreshResponse.refreshResult, refreshResponse.efId)) {
                    return;
                }
            } else if (this instanceof RuimRecords) {
                log("before ruim refresh");
                if (this.mHwIccRecordsEx.beforeHandleRuimRefresh(refreshResponse.refreshResult)) {
                    return;
                }
            }
            int i = refreshResponse.refreshResult;
            if (i == 0) {
                log("handleRefresh with SIM_FILE_UPDATED");
                handleFileUpdate(refreshResponse.efId);
            } else if (i == 1) {
                log("handleRefresh with SIM_REFRESH_INIT");
                onIccRefreshInit();
                if (this instanceof SIMRecords) {
                    log("sim refresh init");
                    this.mParentApp.queryFdn();
                }
            } else if (i != 2) {
                log("handleRefresh with unknown operation");
            } else {
                log("handleRefresh with SIM_REFRESH_RESET");
            }
            if (this instanceof SIMRecords) {
                log("after sim refresh");
                if (!this.mHwIccRecordsEx.afterHandleSimRefresh(refreshResponse.refreshResult)) {
                }
            } else if (this instanceof RuimRecords) {
                log("after ruim refresh");
                if (!this.mHwIccRecordsEx.afterHandleRuimRefresh(refreshResponse.refreshResult)) {
                }
            }
        }
    }

    public int getCarrierNameDisplayCondition() {
        return this.mCarrierNameDisplayCondition;
    }

    public String[] getServiceProviderDisplayInformation() {
        return this.mSpdi;
    }

    public String[] getHomePlmns() {
        String hplmn = getOperatorNumeric();
        String[] hplmns = getEhplmns();
        String[] spdi = getServiceProviderDisplayInformation();
        if (ArrayUtils.isEmpty(hplmns)) {
            hplmns = new String[]{hplmn};
        }
        if (!ArrayUtils.isEmpty(spdi)) {
            return (String[]) ArrayUtils.concatElements(String.class, hplmns, spdi);
        }
        return hplmns;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean isCspPlmnEnabled() {
        return false;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    @UnsupportedAppUsage
    public String getOperatorNumeric() {
        return null;
    }

    public int getVoiceCallForwardingFlag() {
        return -1;
    }

    @UnsupportedAppUsage
    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
    }

    public boolean isLoaded() {
        return this.mLoaded.get();
    }

    public boolean isProvisioned() {
        return true;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String[] getEhplmns() {
        return this.mEhplmns;
    }

    public String[] getPlmnsFromHplmnActRecord() {
        PlmnActRecord[] plmnActRecordArr = this.mHplmnActRecords;
        if (plmnActRecordArr == null) {
            return null;
        }
        String[] hplmns = new String[plmnActRecordArr.length];
        int i = 0;
        while (true) {
            PlmnActRecord[] plmnActRecordArr2 = this.mHplmnActRecords;
            if (i >= plmnActRecordArr2.length) {
                return hplmns;
            }
            hplmns[i] = plmnActRecordArr2[i].plmn;
            i++;
        }
    }

    public IsimRecords getIsimRecords() {
        return null;
    }

    @UnsupportedAppUsage
    public UsimServiceTable getUsimServiceTable() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void setSystemProperty(String key, String val) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(this.mParentApp.getPhoneId(), key, val);
        StringBuilder sb = new StringBuilder();
        sb.append("[key, value]=");
        sb.append(key);
        sb.append(", ");
        sb.append(Log.HWINFO ? val : "***");
        log(sb.toString());
    }

    @UnsupportedAppUsage
    public String getIccSimChallengeResponse(int authContext, String data) {
        log("getIccSimChallengeResponse:");
        try {
            synchronized (this.mLock) {
                CommandsInterface ci = this.mCi;
                UiccCardApplication parentApp = this.mParentApp;
                if (ci == null || parentApp == null) {
                    loge("getIccSimChallengeResponse: Fail, ci or parentApp is null");
                    return null;
                }
                ci.requestIccSimAuthentication(authContext, data, parentApp.getAid(), obtainMessage(EVENT_AKA_AUTHENTICATE_DONE));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    loge("getIccSimChallengeResponse: Fail, interrupted while trying to request Icc Sim Auth");
                    return null;
                }
            }
            if (this.auth_rsp == null) {
                loge("getIccSimChallengeResponse: No authentication response");
                return null;
            }
            log("getIccSimChallengeResponse: return auth_rsp");
            return Base64.encodeToString(this.auth_rsp.payload, 2);
        } catch (Exception e2) {
            loge("getIccSimChallengeResponse: Fail while trying to request Icc Sim Auth");
            return null;
        }
    }

    public static int convertSpnDisplayConditionToBitmask(int condition) {
        int carrierNameDisplayCondition = 0;
        if ((condition & 1) == 1) {
            carrierNameDisplayCondition = 0 | 1;
        }
        if ((condition & 2) == 0) {
            return carrierNameDisplayCondition | 2;
        }
        return carrierNameDisplayCondition;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("IccRecords: " + this);
        pw.println(" mDestroyed=" + this.mDestroyed);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mFh=" + this.mFh);
        pw.println(" mParentApp=" + this.mParentApp);
        pw.println(" recordsLoadedRegistrants: size=" + this.mRecordsLoadedRegistrants.size());
        for (int i = 0; i < this.mRecordsLoadedRegistrants.size(); i++) {
            pw.println("  recordsLoadedRegistrants[" + i + "]=" + ((Registrant) this.mRecordsLoadedRegistrants.get(i)).getHandler());
        }
        pw.println(" mLockedRecordsLoadedRegistrants: size=" + this.mLockedRecordsLoadedRegistrants.size());
        for (int i2 = 0; i2 < this.mLockedRecordsLoadedRegistrants.size(); i2++) {
            pw.println("  mLockedRecordsLoadedRegistrants[" + i2 + "]=" + ((Registrant) this.mLockedRecordsLoadedRegistrants.get(i2)).getHandler());
        }
        pw.println(" mNetworkLockedRecordsLoadedRegistrants: size=" + this.mNetworkLockedRecordsLoadedRegistrants.size());
        for (int i3 = 0; i3 < this.mNetworkLockedRecordsLoadedRegistrants.size(); i3++) {
            pw.println("  mLockedRecordsLoadedRegistrants[" + i3 + "]=" + ((Registrant) this.mNetworkLockedRecordsLoadedRegistrants.get(i3)).getHandler());
        }
        pw.println(" mImsiReadyRegistrants: size=" + this.mImsiReadyRegistrants.size());
        for (int i4 = 0; i4 < this.mImsiReadyRegistrants.size(); i4++) {
            pw.println("  mImsiReadyRegistrants[" + i4 + "]=" + ((Registrant) this.mImsiReadyRegistrants.get(i4)).getHandler());
        }
        pw.println(" mRecordsEventsRegistrants: size=" + this.mRecordsEventsRegistrants.size());
        for (int i5 = 0; i5 < this.mRecordsEventsRegistrants.size(); i5++) {
            pw.println("  mRecordsEventsRegistrants[" + i5 + "]=" + ((Registrant) this.mRecordsEventsRegistrants.get(i5)).getHandler());
        }
        pw.println(" mNewSmsRegistrants: size=" + this.mNewSmsRegistrants.size());
        for (int i6 = 0; i6 < this.mNewSmsRegistrants.size(); i6++) {
            pw.println("  mNewSmsRegistrants[" + i6 + "]=" + ((Registrant) this.mNewSmsRegistrants.get(i6)).getHandler());
        }
        pw.println(" mNetworkSelectionModeAutomaticRegistrants: size=" + this.mNetworkSelectionModeAutomaticRegistrants.size());
        for (int i7 = 0; i7 < this.mNetworkSelectionModeAutomaticRegistrants.size(); i7++) {
            pw.println("  mNetworkSelectionModeAutomaticRegistrants[" + i7 + "]=" + ((Registrant) this.mNetworkSelectionModeAutomaticRegistrants.get(i7)).getHandler());
        }
        pw.println(" mRecordsRequested=" + this.mRecordsRequested);
        pw.println(" mLockedRecordsReqReason=" + this.mLockedRecordsReqReason);
        pw.println(" mRecordsToLoad=" + this.mRecordsToLoad);
        pw.println(" mRdnCache=" + this.mAdnCache);
        pw.println(" iccid=" + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
        pw.println(" mMsisdn=" + Rlog.pii(false, this.mMsisdn));
        pw.println(" mMsisdnTag=" + this.mMsisdnTag);
        pw.println(" mVoiceMailNum=" + Rlog.pii(false, this.mVoiceMailNum));
        pw.println(" mVoiceMailTag=" + this.mVoiceMailTag);
        pw.println(" mNewVoiceMailNum=" + Rlog.pii(false, this.mNewVoiceMailNum));
        pw.println(" mNewVoiceMailTag=" + this.mNewVoiceMailTag);
        pw.println(" mIsVoiceMailFixed=" + this.mIsVoiceMailFixed);
        StringBuilder sb = new StringBuilder();
        sb.append(" mImsi=");
        sb.append(this.mImsi != null ? this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6)) : "null");
        pw.println(sb.toString());
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeImsi=" + this.mCarrierTestOverride.getFakeIMSI());
        }
        pw.println(" mMncLength=" + this.mMncLength);
        pw.println(" mMailboxIndex=" + this.mMailboxIndex);
        pw.println(" mSpn=" + this.mSpn);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeSpn=" + this.mCarrierTestOverride.getFakeSpn());
        }
        pw.flush();
    }

    public static final class OperatorPlmnInfo {
        public final int lacTacEnd;
        public final int lacTacStart;
        public final int plmnNetworkNameIndex;
        public final String plmnNumericPattern;

        public OperatorPlmnInfo(String plmnNumericPattern2, int lacTacStart2, int lacTacEnd2, int plmnNetworkNameIndex2) {
            this.plmnNumericPattern = plmnNumericPattern2;
            this.lacTacStart = lacTacStart2;
            this.lacTacEnd = lacTacEnd2;
            this.plmnNetworkNameIndex = plmnNetworkNameIndex2;
        }

        public String toString() {
            return "{ plmnNumericPattern = " + this.plmnNumericPattern + "lacTacStart = " + this.lacTacStart + "lacTacEnd = " + this.lacTacEnd + "plmnNetworkNameIndex = " + this.plmnNetworkNameIndex + " }";
        }
    }

    public static final class PlmnNetworkName {
        public final String fullName;
        public final String shortName;

        public PlmnNetworkName(String fullName2, String shortName2) {
            this.fullName = fullName2;
            this.shortName = shortName2;
        }

        public String toString() {
            return "{ fullName = " + this.fullName + " shortName = " + this.shortName + " }";
        }
    }

    public static boolean getEmailAnrSupport() {
        return bEmailAnrSupport;
    }

    public static void loadEmailAnrSupportFlag(Context c) {
        try {
            boolean z = false;
            if (SettingsEx.Systemex.getInt(c.getContentResolver(), "is_email_anr_support", 0) > 0) {
                z = true;
            }
            bEmailAnrSupport = z;
        } catch (Exception e) {
            Rlog.e("IccRecords", "loadEmailAnrSupportFlag: error.");
        }
    }

    public static boolean getAdnLongNumberSupport() {
        return bAdnNumberLengthDefault;
    }

    public static void loadAdnLongNumberFlag(Context c) {
        try {
            bAdnNumberLengthDefault = SettingsEx.Systemex.getInt(c.getContentResolver(), "sim_number_length", 20) > 20;
        } catch (Exception e) {
            Rlog.e("IccRecords", "loadAdnLongNumberFlag: error.");
        }
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void disableRequestIccRecords() {
        this.mRecordsRequested = false;
    }

    public void clearLoadState() {
        this.mLoaded.set(false);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String getIccIdHw() {
        return this.mIccId;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void registerForLoadIccID(Handler h, int what, Object obj) {
        this.mHwIccRecordsEx.registerForLoadIccID(h, what, obj);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void unRegisterForLoadIccID(Handler h) {
        this.mHwIccRecordsEx.unRegisterForLoadIccID(h);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void notifyRegisterLoadIccID(Object userObj, Object result, Throwable exception) {
        this.mHwIccRecordsEx.notifyRegisterLoadIccID(userObj, result, exception);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void registerForCsgRecordsLoaded(Handler h, int what, Object obj) {
        this.mHwIccRecordsEx.registerForCsgRecordsLoaded(h, what, obj);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void unregisterForCsgRecordsLoaded(Handler h) {
        this.mHwIccRecordsEx.unregisterForCsgRecordsLoaded(h);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void notifyRegisterForCsgRecordsLoaded() {
        this.mHwIccRecordsEx.notifyRegisterForCsgRecordsLoaded();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void registerForIccRefresh(Handler h, int what, Object obj) {
        this.mHwIccRecordsEx.registerForIccRefresh(h, what, obj);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void unRegisterForIccRefresh(Handler h) {
        this.mHwIccRecordsEx.unRegisterForIccRefresh(h);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void notifyRegisterForIccRefresh() {
        this.mHwIccRecordsEx.notifyRegisterForIccRefresh();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setVoiceMailNumber(String voiceNumber) {
        this.mVoiceMailNum = voiceNumber;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String getOperatorNumericEx(ContentResolver cr, String name) {
        return this.mHwIccRecordsEx.getOperatorNumericEx(cr, name);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean has3Gphonebook() {
        return this.mHwIccRecordsEx.has3Gphonebook();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean isGetPBRDone() {
        return this.mHwIccRecordsEx.isGetPBRDone();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String getEons() {
        return this.mHwIccRecordsEx.getEons();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean isEonsDisabled() {
        return this.mHwIccRecordsEx.isEonsDisabled();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean updateEons(String regOperator, int lac) {
        return this.mHwIccRecordsEx.updateEons(regOperator, lac);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public ArrayList<OperatorInfoEx> getEonsForAvailableNetworks(ArrayList<OperatorInfoEx> avlNetworks) {
        return this.mHwIccRecordsEx.getEonsForAvailableNetworks(avlNetworks);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String[] getEhplmnOfSim() {
        return this.mHwIccRecordsEx.getEhplmnOfSim();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String getActingHplmn() {
        return this.mHwIccRecordsEx.getActingHplmn();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setMdnNumber(String alphaTag, String number, Message onComplete) {
        this.mHwIccRecordsEx.setMdnNumber(alphaTag, number, onComplete);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean getImsiReady() {
        return this.mHwIccRecordsEx.getImsiReady();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public byte[] getOcsgl() {
        return this.mHwIccRecordsEx.getOcsgl();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean getCsglexist() {
        return this.mHwIccRecordsEx.getCsglexist();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setCsglexist(boolean isCglExist) {
        this.mHwIccRecordsEx.setCsglexist(isCglExist);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean isHwCustDataRoamingOpenArea() {
        return this.mHwIccRecordsEx.isHwCustDataRoamingOpenArea();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public byte[] getGID1Hw() {
        return this.mHwIccRecordsEx.getGID1();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void addRecordsToLoadNum() {
        this.mRecordsToLoad++;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String getImsiHw() {
        return this.mImsi;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setImsiHw(String imsi) {
        this.mImsi = imsi;
        this.mImsiReadyRegistrants.notifyRegistrants();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void notifyRegisterForRecordsEvents(int event) {
        this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(event));
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean judgeIfDestroyed() {
        return this.mDestroyed.get();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setSystemPropertyHw(String key, String val) {
        setSystemProperty(key, val);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void handleFileUpdateHw(int efid) {
        handleFileUpdate(efid);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public int getMncLength() {
        return this.mMncLength;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setMncLength(int len) {
        this.mMncLength = len;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setVoiceFixedFlag(boolean isVoiceMailFixed) {
        this.mIsVoiceMailFixed = isVoiceMailFixed;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setVoiceMailTag(String voiceMailTag) {
        this.mVoiceMailTag = voiceMailTag;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void notifyRegisterForFdnRecordsLoaded() {
        this.mFdnRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void onRecordLoadedHw() {
        onRecordLoaded();
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public IccFileHandler getIccFileHandler() {
        return this.mFh;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public int getSlotId() {
        UiccCardApplication uiccCardApplication = this.mParentApp;
        if (uiccCardApplication != null && uiccCardApplication.getUiccCard() != null) {
            return this.mParentApp.getUiccCard().getPhoneId();
        }
        loge("error , mParentApp.getUiccCard  is null");
        return 0;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public AdnRecordCacheEx getAdnCacheHw() {
        return AdnRecordCacheEx.from(this.mAdnCache);
    }
}
