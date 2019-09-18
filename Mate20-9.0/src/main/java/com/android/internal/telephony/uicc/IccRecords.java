package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class IccRecords extends AbstractIccRecords implements IccConstants {
    public static final int CALL_FORWARDING_STATUS_DISABLED = 0;
    public static final int CALL_FORWARDING_STATUS_ENABLED = 1;
    public static final int CALL_FORWARDING_STATUS_UNKNOWN = -1;
    protected static final boolean DBG = true;
    public static final int DEFAULT_VOICE_MESSAGE_COUNT = -2;
    static final Set<Integer> EFID_SET = new HashSet(Arrays.asList(new Integer[]{Integer.valueOf(IccConstants.EF_ICCID), Integer.valueOf(IccConstants.EF_IST), Integer.valueOf(IccConstants.EF_AD), Integer.valueOf(IccConstants.EF_GID1), Integer.valueOf(IccConstants.EF_GID2), Integer.valueOf(IccConstants.EF_SST), Integer.valueOf(IccConstants.EF_SPN), Integer.valueOf(IccConstants.EF_SPN_CPHS), Integer.valueOf(IccConstants.EF_SPN_SHORT_CPHS), Integer.valueOf(IccConstants.EF_IMPI), 28474}));
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
    protected static final int LOCKED_RECORDS_REQ_REASON_LOCKED = 1;
    protected static final int LOCKED_RECORDS_REQ_REASON_NETWORK_LOCKED = 2;
    protected static final int LOCKED_RECORDS_REQ_REASON_NONE = 0;
    public static final String PROPERTY_MCC_MATCHING_FYROM = "persist.sys.mcc_match_fyrom";
    public static final int SPN_RULE_SHOW_PLMN = 2;
    public static final int SPN_RULE_SHOW_SPN = 1;
    protected static final int UNINITIALIZED = -1;
    protected static final int UNKNOWN = 0;
    public static final int UNKNOWN_VOICE_MESSAGE_COUNT = -1;
    protected static final boolean VDBG = false;
    protected static AtomicInteger sNextRequestId = new AtomicInteger(1);
    private IccIoResult auth_rsp;
    protected AdnRecordCache mAdnCache;
    CarrierTestOverride mCarrierTestOverride;
    protected CommandsInterface mCi;
    protected Context mContext;
    protected AtomicBoolean mDestroyed = new AtomicBoolean(false);
    protected String[] mEhplmns;
    public RegistrantList mFdnRecordsLoadedRegistrants = new RegistrantList();
    protected IccFileHandler mFh;
    protected String[] mFplmns;
    protected String mFullIccId;
    protected String mGid1;
    protected String mGid2;
    protected PlmnActRecord[] mHplmnActRecords;
    protected String mIccId;
    protected String mImsi;
    protected RegistrantList mImsiReadyRegistrants = new RegistrantList();
    protected boolean mIsVoiceMailFixed = false;
    protected AtomicBoolean mLoaded = new AtomicBoolean(false);
    private final Object mLock = new Object();
    protected RegistrantList mLockedRecordsLoadedRegistrants = new RegistrantList();
    protected int mLockedRecordsReqReason = 0;
    protected int mMailboxIndex = 0;
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
    protected UiccCardApplication mParentApp;
    protected final HashMap<Integer, Message> mPendingResponses = new HashMap<>();
    protected PlmnActRecord[] mPlmnActRecords;
    protected String mPnnHomeName;
    protected String mPrefLang;
    protected RegistrantList mRecordsEventsRegistrants = new RegistrantList();
    protected RegistrantList mRecordsLoadedRegistrants = new RegistrantList();
    protected RegistrantList mRecordsOverrideRegistrants = new RegistrantList();
    protected boolean mRecordsRequested = false;
    protected int mRecordsToLoad;
    private String mSpn;
    protected RegistrantList mSpnUpdatedRegistrants = new RegistrantList();
    protected TelephonyManager mTelephonyManager;
    protected String mVoiceMailNum = null;
    protected String mVoiceMailTag = null;

    public interface IccRecordLoaded {
        String getEfName();

        void onRecordLoaded(AsyncResult asyncResult);
    }

    public abstract int getDisplayRule(ServiceState serviceState);

    public abstract int getVoiceMessageCount();

    /* access modifiers changed from: protected */
    public abstract void handleFileUpdate(int i);

    /* access modifiers changed from: protected */
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

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
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
        if (this.mCarrierTestOverride.isInTestMode()) {
            str = "mFakeIccid=" + this.mCarrierTestOverride.getFakeIccid();
        } else {
            str = "";
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
            str3 = "";
        }
        sb.append(str3);
        sb.append(" mncLength=");
        sb.append(this.mMncLength);
        sb.append(" mailboxIndex=");
        sb.append(this.mMailboxIndex);
        sb.append(" spn=");
        sb.append(this.mSpn);
        if (this.mCarrierTestOverride.isInTestMode()) {
            str4 = " mFakeSpn=" + this.mCarrierTestOverride.getFakeSpn();
        } else {
            str4 = "";
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
        this.mRecordsLoadedRegistrants.notifyRegistrants();
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
        if (this.mAdnCache != null) {
            this.mAdnCache.reset();
        }
        this.mLoaded.set(false);
    }

    /* access modifiers changed from: package-private */
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

    public String getIccId() {
        if (!this.mCarrierTestOverride.isInTestMode() || this.mCarrierTestOverride.getFakeIccid() == null) {
            return this.mIccId;
        }
        return this.mCarrierTestOverride.getFakeIccid();
    }

    public String getFullIccId() {
        return this.mFullIccId;
    }

    public void registerForRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            int i = this.mRecordsLoadedRegistrants.size() - 1;
            while (i >= 0) {
                Handler rH = ((Registrant) this.mRecordsLoadedRegistrants.get(i)).getHandler();
                if (rH == null || rH != h) {
                    i--;
                } else {
                    return;
                }
            }
            Registrant r = new Registrant(h, what, obj);
            this.mRecordsLoadedRegistrants.add(r);
            if (this.mRecordsToLoad == 0 && this.mRecordsRequested && this.mParentApp != null && IccCardApplicationStatus.AppState.APPSTATE_READY == this.mParentApp.getState()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

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
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void registerForLockedRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mLockedRecordsLoadedRegistrants.add(r);
            if (getLockedRecordsLoaded()) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
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
                r.notifyRegistrant(new AsyncResult(null, null, null));
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
            if (!(getIMSI() == null || this.mParentApp == null || IccCardApplicationStatus.AppState.APPSTATE_READY != this.mParentApp.getState())) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
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
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForSpnUpdate(Handler h) {
        this.mSpnUpdatedRegistrants.remove(h);
    }

    public void registerForRecordsEvents(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mRecordsEventsRegistrants.add(r);
        r.notifyResult(0);
        r.notifyResult(1);
    }

    public void unregisterForRecordsEvents(Handler h) {
        this.mRecordsEventsRegistrants.remove(h);
    }

    public void registerForNewSms(Handler h, int what, Object obj) {
        this.mNewSmsRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNewSms(Handler h) {
        this.mNewSmsRegistrants.remove(h);
    }

    public void registerForNetworkSelectionModeAutomatic(Handler h, int what, Object obj) {
        this.mNetworkSelectionModeAutomaticRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForNetworkSelectionModeAutomatic(Handler h) {
        this.mNetworkSelectionModeAutomaticRegistrants.remove(h);
    }

    public synchronized void registerForLoadIccID(Handler h, int what, Object obj) {
        super.registerForLoadIccID(h, what, obj);
        if (!TextUtils.isEmpty(this.mIccId)) {
            log("mIccId exist before registerForLoadIccID. mIccId = " + SubscriptionInfo.givePrintableIccid(this.mIccId));
            Object r = this.mIccId;
            Message m = Message.obtain(h, what, obj);
            AsyncResult.forMessage(m, r, null);
            m.sendToTarget();
        }
    }

    public String getIMSI() {
        if (!this.mCarrierTestOverride.isInTestMode() || this.mCarrierTestOverride.getFakeIMSI() == null) {
            return this.mImsi;
        }
        return this.mCarrierTestOverride.getFakeIMSI();
    }

    public void setImsi(String imsi) {
        this.mImsi = imsi;
        this.mImsiReadyRegistrants.notifyRegistrants();
    }

    public String getNAI() {
        return null;
    }

    public String getMsisdnNumber() {
        return this.mMsisdn;
    }

    public String getGid1() {
        if (!this.mCarrierTestOverride.isInTestMode() || this.mCarrierTestOverride.getFakeGid1() == null) {
            return this.mGid1;
        }
        return this.mCarrierTestOverride.getFakeGid1();
    }

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

    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        loge("setMsisdn() should not be invoked on base IccRecords");
        AsyncResult.forMessage(onComplete).exception = new IccIoResult(106, 130, (byte[]) null).getException();
        onComplete.sendToTarget();
    }

    public String getMsisdnAlphaTag() {
        return this.mMsisdnTag;
    }

    public String getVoiceMailNumber() {
        return this.mVoiceMailNum;
    }

    public String getServiceProviderName() {
        if (this.mCarrierTestOverride.isInTestMode() && this.mCarrierTestOverride.getFakeSpn() != null) {
            return this.mCarrierTestOverride.getFakeSpn();
        }
        String providerName = this.mSpn;
        UiccCardApplication parentApp = this.mParentApp;
        if (parentApp != null) {
            UiccProfile profile = parentApp.getUiccProfile();
            if (profile != null) {
                String brandOverride = profile.getOperatorBrandOverride();
                if (brandOverride != null) {
                    log("override, providerName=" + providerName);
                    providerName = brandOverride;
                } else {
                    log("no brandOverride, providerName=" + providerName);
                }
            } else {
                log("card is null, providerName=" + providerName);
            }
        } else {
            log("mParentApp is null, providerName=" + providerName);
        }
        return providerName;
    }

    /* access modifiers changed from: protected */
    public void setServiceProviderName(String spn) {
        if (!TextUtils.equals(this.mSpn, spn)) {
            this.mSpnUpdatedRegistrants.notifyRegistrants();
            this.mSpn = spn;
        }
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

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
                    loge("Failed to parse ICC SIM AKA contents: " + e);
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
            } catch (Throwable ar4) {
                onRecordLoaded();
                throw ar4;
            }
            onRecordLoaded();
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
    public void handleRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null) {
            log("handleRefresh received without input");
        } else if (TextUtils.isEmpty(refreshResponse.aid) || refreshResponse.aid.equals(this.mParentApp.getAid())) {
            if (this instanceof SIMRecords) {
                log("before sim refresh");
                if (beforeHandleSimRefresh(refreshResponse)) {
                    return;
                }
            } else if (this instanceof RuimRecords) {
                log("before ruim refresh");
                if (beforeHandleRuimRefresh(refreshResponse)) {
                    return;
                }
            }
            switch (refreshResponse.refreshResult) {
                case 0:
                    log("handleRefresh with SIM_FILE_UPDATED");
                    handleFileUpdate(refreshResponse.efId);
                    break;
                case 1:
                    log("handleRefresh with SIM_REFRESH_INIT");
                    onIccRefreshInit();
                    if (this instanceof SIMRecords) {
                        log("sim refresh init");
                        this.mParentApp.queryFdn();
                        break;
                    }
                    break;
                case 2:
                    log("handleRefresh with SIM_REFRESH_RESET");
                    break;
                default:
                    log("handleRefresh with unknown operation");
                    break;
            }
            if (this instanceof SIMRecords) {
                log("after sim refresh");
                if (afterHandleSimRefresh(refreshResponse)) {
                }
            } else if (this instanceof RuimRecords) {
                log("after ruim refresh");
                if (afterHandleRuimRefresh(refreshResponse)) {
                }
            }
        }
    }

    public boolean isCspPlmnEnabled() {
        return false;
    }

    public String getOperatorNumeric() {
        return null;
    }

    public int getVoiceCallForwardingFlag() {
        return -1;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
    }

    public boolean isLoaded() {
        return this.mLoaded.get();
    }

    public boolean isProvisioned() {
        return true;
    }

    public IsimRecords getIsimRecords() {
        return null;
    }

    public UsimServiceTable getUsimServiceTable() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void setSystemProperty(String key, String val) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(this.mParentApp.getPhoneId(), key, val);
        log("[key, value]=" + key + ", " + val);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        if (r6.auth_rsp != null) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002b, code lost:
        loge("getIccSimChallengeResponse: No authentication response");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0030, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        log("getIccSimChallengeResponse: return auth_rsp");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003f, code lost:
        return android.util.Base64.encodeToString(r6.auth_rsp.payload, 2);
     */
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
        } catch (Exception e2) {
            loge("getIccSimChallengeResponse: Fail while trying to request Icc Sim Auth");
            return null;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        String str;
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
        String iccIdToPrint = SubscriptionInfo.givePrintableIccid(this.mFullIccId);
        pw.println(" iccid=" + iccIdToPrint);
        pw.println(" mMsisdn=" + Rlog.pii(false, this.mMsisdn));
        pw.println(" mMsisdnTag=" + this.mMsisdnTag);
        pw.println(" mVoiceMailNum=" + Rlog.pii(false, this.mVoiceMailNum));
        pw.println(" mVoiceMailTag=" + this.mVoiceMailTag);
        pw.println(" mNewVoiceMailNum=" + Rlog.pii(false, this.mNewVoiceMailNum));
        pw.println(" mNewVoiceMailTag=" + this.mNewVoiceMailTag);
        pw.println(" mIsVoiceMailFixed=" + this.mIsVoiceMailFixed);
        StringBuilder sb = new StringBuilder();
        sb.append(" mImsi=");
        if (this.mImsi != null) {
            str = this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6));
        } else {
            str = "null";
        }
        sb.append(str);
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

    public String getCdmaGsmImsi() {
        return null;
    }

    public void disableRequestIccRecords() {
        this.mRecordsRequested = false;
    }

    public void clearLoadState() {
        this.mLoaded.set(false);
    }
}
