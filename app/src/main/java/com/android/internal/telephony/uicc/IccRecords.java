package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class IccRecords extends AbstractIccRecords implements IccConstants {
    public static final int CALL_FORWARDING_STATUS_DISABLED = 0;
    public static final int CALL_FORWARDING_STATUS_ENABLED = 1;
    public static final int CALL_FORWARDING_STATUS_UNKNOWN = -1;
    protected static final boolean DBG = true;
    private static final int EVENT_AKA_AUTHENTICATE_DONE = 90;
    protected static final int EVENT_APP_READY = 1;
    public static final int EVENT_CFI = 1;
    public static final int EVENT_GET_ICC_RECORD_DONE = 100;
    public static final int EVENT_MWI = 0;
    protected static final int EVENT_SET_MSISDN_DONE = 30;
    public static final int EVENT_SPN = 2;
    public static final String PROPERTY_MCC_MATCHING_FYROM = "persist.sys.mcc_match_fyrom";
    public static final int SPN_RULE_SHOW_PLMN = 2;
    public static final int SPN_RULE_SHOW_SPN = 1;
    protected static final int UNINITIALIZED = -1;
    protected static final int UNKNOWN = 0;
    protected static final boolean VDBG = false;
    private IccIoResult auth_rsp;
    protected AdnRecordCache mAdnCache;
    protected CommandsInterface mCi;
    protected Context mContext;
    protected AtomicBoolean mDestroyed;
    public RegistrantList mFdnRecordsLoadedRegistrants;
    protected IccFileHandler mFh;
    protected String mFullIccId;
    protected String mGid1;
    protected String mGid2;
    protected String mIccId;
    protected String mImsi;
    protected RegistrantList mImsiReadyRegistrants;
    protected boolean mIsVoiceMailFixed;
    private final Object mLock;
    protected int mMailboxIndex;
    protected int mMncLength;
    protected String mMsisdn;
    protected String mMsisdnTag;
    protected RegistrantList mNetworkSelectionModeAutomaticRegistrants;
    protected String mNewMsisdn;
    protected String mNewMsisdnTag;
    protected RegistrantList mNewSmsRegistrants;
    protected String mNewVoiceMailNum;
    protected String mNewVoiceMailTag;
    protected UiccCardApplication mParentApp;
    protected String mPrefLang;
    protected RegistrantList mRecordsEventsRegistrants;
    protected RegistrantList mRecordsLoadedRegistrants;
    protected boolean mRecordsRequested;
    protected int mRecordsToLoad;
    private String mSpn;
    protected TelephonyManager mTelephonyManager;
    protected String mVoiceMailNum;
    protected String mVoiceMailTag;

    public interface IccRecordLoaded {
        String getEfName();

        void onRecordLoaded(AsyncResult asyncResult);
    }

    public abstract int getDisplayRule(String str);

    public abstract int getVoiceMessageCount();

    protected abstract void log(String str);

    protected abstract void loge(String str);

    protected abstract void onAllRecordsLoaded();

    public abstract void onReady();

    protected abstract void onRecordLoaded();

    public abstract void onRefresh(boolean z, int[] iArr);

    public abstract void setVoiceMailNumber(String str, String str2, Message message);

    public abstract void setVoiceMessageWaiting(int i, int i2);

    public String toString() {
        return "mDestroyed=" + this.mDestroyed + " mContext=" + this.mContext + " mCi=" + this.mCi + " mFh=" + this.mFh + " mParentApp=" + this.mParentApp + " recordsLoadedRegistrants=" + this.mRecordsLoadedRegistrants + " mImsiReadyRegistrants=" + this.mImsiReadyRegistrants + " mRecordsEventsRegistrants=" + this.mRecordsEventsRegistrants + " mNewSmsRegistrants=" + this.mNewSmsRegistrants + " mNetworkSelectionModeAutomaticRegistrants=" + this.mNetworkSelectionModeAutomaticRegistrants + " recordsToLoad=" + this.mRecordsToLoad + " adnCache=" + this.mAdnCache + " recordsRequested=" + this.mRecordsRequested + " iccid=" + SubscriptionInfo.givePrintableIccid(this.mFullIccId) + " msisdnTag=" + this.mMsisdnTag + " voiceMailNum=" + this.mVoiceMailNum + " voiceMailTag=" + this.mVoiceMailTag + " newVoiceMailNum=" + this.mNewVoiceMailNum + " newVoiceMailTag=" + this.mNewVoiceMailTag + " isVoiceMailFixed=" + this.mIsVoiceMailFixed + "" + " mncLength=" + this.mMncLength + " mailboxIndex=" + this.mMailboxIndex + " spn=" + this.mSpn;
    }

    public IccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        this.mDestroyed = new AtomicBoolean(false);
        this.mRecordsLoadedRegistrants = new RegistrantList();
        this.mImsiReadyRegistrants = new RegistrantList();
        this.mRecordsEventsRegistrants = new RegistrantList();
        this.mNewSmsRegistrants = new RegistrantList();
        this.mNetworkSelectionModeAutomaticRegistrants = new RegistrantList();
        this.mFdnRecordsLoadedRegistrants = new RegistrantList();
        this.mRecordsRequested = false;
        this.mMsisdn = null;
        this.mMsisdnTag = null;
        this.mNewMsisdn = null;
        this.mNewMsisdnTag = null;
        this.mVoiceMailNum = null;
        this.mVoiceMailTag = null;
        this.mNewVoiceMailNum = null;
        this.mNewVoiceMailTag = null;
        this.mIsVoiceMailFixed = false;
        this.mMncLength = UNINITIALIZED;
        this.mMailboxIndex = EVENT_MWI;
        this.mLock = new Object();
        AbstractIccRecords.loadEmailAnrSupportFlag(c);
        AbstractIccRecords.loadAdnLongNumberFlag(c);
        this.mContext = c;
        this.mCi = ci;
        this.mFh = app.getIccFileHandler();
        this.mParentApp = app;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public void dispose() {
        this.mDestroyed.set(DBG);
        this.mParentApp = null;
        this.mFh = null;
        this.mCi = null;
        this.mContext = null;
    }

    void recordsRequired() {
    }

    public AdnRecordCache getAdnCache() {
        return this.mAdnCache;
    }

    public String getIccId() {
        return this.mIccId;
    }

    public String getFullIccId() {
        return this.mFullIccId;
    }

    public void registerForRecordsLoaded(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            int i = this.mRecordsLoadedRegistrants.size() + UNINITIALIZED;
            while (i >= 0) {
                Handler rH = ((Registrant) this.mRecordsLoadedRegistrants.get(i)).getHandler();
                if (rH == null || rH != h) {
                    i += UNINITIALIZED;
                } else {
                    return;
                }
            }
            Registrant r = new Registrant(h, what, obj);
            this.mRecordsLoadedRegistrants.add(r);
            if (this.mRecordsToLoad == 0 && this.mRecordsRequested && this.mParentApp != null && AppState.APPSTATE_READY == this.mParentApp.getState()) {
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

    public void registerForImsiReady(Handler h, int what, Object obj) {
        if (!this.mDestroyed.get()) {
            Registrant r = new Registrant(h, what, obj);
            this.mImsiReadyRegistrants.add(r);
            if (!(this.mImsi == null || this.mParentApp == null || AppState.APPSTATE_READY != this.mParentApp.getState())) {
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    public void unregisterForImsiReady(Handler h) {
        this.mImsiReadyRegistrants.remove(h);
    }

    public void registerForRecordsEvents(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mRecordsEventsRegistrants.add(r);
        r.notifyResult(Integer.valueOf(EVENT_MWI));
        r.notifyResult(Integer.valueOf(SPN_RULE_SHOW_SPN));
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
        return null;
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
        return null;
    }

    public String getGid2() {
        return null;
    }

    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mMsisdn = number;
        this.mMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mMsisdnTag + " " + "xxxxxxx");
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mMsisdnTag, this.mMsisdn), IccConstants.EF_MSISDN, IccConstants.EF_EXT1, SPN_RULE_SHOW_SPN, null, obtainMessage(EVENT_SET_MSISDN_DONE, onComplete));
    }

    public String getMsisdnAlphaTag() {
        return this.mMsisdnTag;
    }

    public String getVoiceMailNumber() {
        return this.mVoiceMailNum;
    }

    public String getServiceProviderName() {
        String providerName = this.mSpn;
        UiccCardApplication parentApp = this.mParentApp;
        if (parentApp != null) {
            UiccCard card = parentApp.getUiccCard();
            if (card != null) {
                String brandOverride = card.getOperatorBrandOverride();
                if (brandOverride != null) {
                    log("getServiceProviderName: override, providerName=" + providerName);
                    return brandOverride;
                }
                log("getServiceProviderName: no brandOverride, providerName=" + providerName);
                return providerName;
            }
            log("getServiceProviderName: card is null, providerName=" + providerName);
            return providerName;
        }
        log("getServiceProviderName: mParentApp is null, providerName=" + providerName);
        return providerName;
    }

    protected void setServiceProviderName(String spn) {
        this.mSpn = spn;
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    protected void onIccRefreshInit() {
        this.mAdnCache.reset();
        UiccCardApplication parentApp = this.mParentApp;
        if (parentApp != null && parentApp.getState() == AppState.APPSTATE_READY) {
            sendMessage(obtainMessage(SPN_RULE_SHOW_SPN));
        }
    }

    public boolean getRecordsLoaded() {
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            return DBG;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case EVENT_AKA_AUTHENTICATE_DONE /*90*/:
                ar = (AsyncResult) msg.obj;
                this.auth_rsp = null;
                log("EVENT_AKA_AUTHENTICATE_DONE");
                if (ar.exception != null) {
                    loge("Exception ICC SIM AKA: " + ar.exception);
                } else {
                    try {
                        this.auth_rsp = (IccIoResult) ar.result;
                        log("ICC SIM AKA: auth_rsp = " + this.auth_rsp);
                    } catch (Exception e) {
                        loge("Failed to parse ICC SIM AKA contents: " + e);
                    }
                }
                synchronized (this.mLock) {
                    this.mLock.notifyAll();
                    break;
                }
            case EVENT_GET_ICC_RECORD_DONE /*100*/:
                try {
                    ar = msg.obj;
                    IccRecordLoaded recordLoaded = ar.userObj;
                    log(recordLoaded.getEfName() + " LOADED");
                    if (ar.exception != null) {
                        loge("Record Load Exception: " + ar.exception);
                    } else {
                        recordLoaded.onRecordLoaded(ar);
                    }
                    onRecordLoaded();
                } catch (RuntimeException exc) {
                    loge("Exception parsing SIM record: " + exc);
                } catch (Throwable th) {
                    onRecordLoaded();
                }
            default:
                super.handleMessage(msg);
        }
    }

    public String getSimLanguage() {
        return this.mPrefLang;
    }

    protected void setSimLanguage(byte[] efLi, byte[] efPl) {
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
        for (int i = EVENT_MWI; i + SPN_RULE_SHOW_SPN < languages.length; i += SPN_RULE_SHOW_PLMN) {
            String lang = new String(languages, i, SPN_RULE_SHOW_PLMN, "ISO-8859-1");
            int j = EVENT_MWI;
            while (j < locales.length) {
                if (locales[j] != null && locales[j].length() >= SPN_RULE_SHOW_PLMN && locales[j].substring(EVENT_MWI, SPN_RULE_SHOW_PLMN).equalsIgnoreCase(lang)) {
                    return lang;
                }
                j += SPN_RULE_SHOW_SPN;
            }
        }
        return null;
    }

    public boolean isCspPlmnEnabled() {
        return false;
    }

    public String getOperatorNumeric() {
        return null;
    }

    public int getVoiceCallForwardingFlag() {
        return UNINITIALIZED;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
    }

    public boolean isProvisioned() {
        return DBG;
    }

    public IsimRecords getIsimRecords() {
        return null;
    }

    public UsimServiceTable getUsimServiceTable() {
        return null;
    }

    protected void setSystemProperty(String key, String val) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(this.mParentApp.getPhoneId(), key, val);
        log("[key, value]=" + key + ", " + val);
    }

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
                    log("getIccSimChallengeResponse: return auth_rsp");
                    return Base64.encodeToString(this.auth_rsp.payload, SPN_RULE_SHOW_PLMN);
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
        int i;
        pw.println("IccRecords: " + this);
        pw.println(" mDestroyed=" + this.mDestroyed);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mFh=" + this.mFh);
        pw.println(" mParentApp=" + this.mParentApp);
        pw.println(" recordsLoadedRegistrants: size=" + this.mRecordsLoadedRegistrants.size());
        for (i = EVENT_MWI; i < this.mRecordsLoadedRegistrants.size(); i += SPN_RULE_SHOW_SPN) {
            pw.println("  recordsLoadedRegistrants[" + i + "]=" + ((Registrant) this.mRecordsLoadedRegistrants.get(i)).getHandler());
        }
        pw.println(" mImsiReadyRegistrants: size=" + this.mImsiReadyRegistrants.size());
        for (i = EVENT_MWI; i < this.mImsiReadyRegistrants.size(); i += SPN_RULE_SHOW_SPN) {
            pw.println("  mImsiReadyRegistrants[" + i + "]=" + ((Registrant) this.mImsiReadyRegistrants.get(i)).getHandler());
        }
        pw.println(" mRecordsEventsRegistrants: size=" + this.mRecordsEventsRegistrants.size());
        for (i = EVENT_MWI; i < this.mRecordsEventsRegistrants.size(); i += SPN_RULE_SHOW_SPN) {
            pw.println("  mRecordsEventsRegistrants[" + i + "]=" + ((Registrant) this.mRecordsEventsRegistrants.get(i)).getHandler());
        }
        pw.println(" mNewSmsRegistrants: size=" + this.mNewSmsRegistrants.size());
        for (i = EVENT_MWI; i < this.mNewSmsRegistrants.size(); i += SPN_RULE_SHOW_SPN) {
            pw.println("  mNewSmsRegistrants[" + i + "]=" + ((Registrant) this.mNewSmsRegistrants.get(i)).getHandler());
        }
        pw.println(" mNetworkSelectionModeAutomaticRegistrants: size=" + this.mNetworkSelectionModeAutomaticRegistrants.size());
        for (i = EVENT_MWI; i < this.mNetworkSelectionModeAutomaticRegistrants.size(); i += SPN_RULE_SHOW_SPN) {
            pw.println("  mNetworkSelectionModeAutomaticRegistrants[" + i + "]=" + ((Registrant) this.mNetworkSelectionModeAutomaticRegistrants.get(i)).getHandler());
        }
        pw.println(" mRecordsRequested=" + this.mRecordsRequested);
        pw.println(" mRecordsToLoad=" + this.mRecordsToLoad);
        pw.println(" mRdnCache=" + this.mAdnCache);
        pw.println(" iccid=" + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
        if (TextUtils.isEmpty(this.mMsisdn)) {
            pw.println(" mMsisdn=null");
        } else {
            pw.println(" mMsisdn=" + "XXX");
        }
        pw.println(" mMsisdnTag=" + this.mMsisdnTag);
        pw.println(" mVoiceMailNum=" + this.mVoiceMailNum);
        pw.println(" mVoiceMailTag=" + this.mVoiceMailTag);
        pw.println(" mNewVoiceMailNum=" + this.mNewVoiceMailNum);
        pw.println(" mNewVoiceMailTag=" + this.mNewVoiceMailTag);
        pw.println(" mIsVoiceMailFixed=" + this.mIsVoiceMailFixed);
        pw.println(" mMncLength=" + this.mMncLength);
        pw.println(" mMailboxIndex=" + this.mMailboxIndex);
        pw.println(" mSpn=" + this.mSpn);
        pw.flush();
    }

    public String getCdmaGsmImsi() {
        return null;
    }

    public void disableRequestIccRecords() {
        this.mRecordsRequested = false;
    }
}
