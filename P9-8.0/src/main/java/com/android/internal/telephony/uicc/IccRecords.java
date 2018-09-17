package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class IccRecords extends AbstractIccRecords implements IccConstants {
    public static final int CALL_FORWARDING_STATUS_DISABLED = 0;
    public static final int CALL_FORWARDING_STATUS_ENABLED = 1;
    public static final int CALL_FORWARDING_STATUS_UNKNOWN = -1;
    protected static final boolean DBG = true;
    public static final int DEFAULT_VOICE_MESSAGE_COUNT = -2;
    private static final int EVENT_AKA_AUTHENTICATE_DONE = 90;
    protected static final int EVENT_APP_READY = 1;
    public static final int EVENT_CFI = 1;
    public static final int EVENT_GET_ICC_RECORD_DONE = 100;
    public static final int EVENT_MWI = 0;
    public static final int EVENT_SPN = 2;
    protected static final int HANDLER_ACTION_BASE = 1238272;
    protected static final int HANDLER_ACTION_NONE = 1238272;
    protected static final int HANDLER_ACTION_SEND_RESPONSE = 1238273;
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
    private final Object mLock = new Object();
    protected int mMailboxIndex = 0;
    protected int mMncLength = -1;
    protected String mMsisdn = null;
    protected String mMsisdnTag = null;
    protected RegistrantList mNetworkSelectionModeAutomaticRegistrants = new RegistrantList();
    protected String mNewMsisdn = null;
    protected String mNewMsisdnTag = null;
    protected RegistrantList mNewSmsRegistrants = new RegistrantList();
    protected String mNewVoiceMailNum = null;
    protected String mNewVoiceMailTag = null;
    protected PlmnActRecord[] mOplmnActRecords;
    protected UiccCardApplication mParentApp;
    protected final HashMap<Integer, Message> mPendingResponses = new HashMap();
    protected PlmnActRecord[] mPlmnActRecords;
    protected String mPrefLang;
    protected RegistrantList mRecordsEventsRegistrants = new RegistrantList();
    protected RegistrantList mRecordsLoadedRegistrants = new RegistrantList();
    protected boolean mRecordsRequested = false;
    protected int mRecordsToLoad;
    private String mSpn;
    protected TelephonyManager mTelephonyManager;
    protected String mVoiceMailNum = null;
    protected String mVoiceMailTag = null;

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
        String str;
        StringBuilder append = new StringBuilder().append("mDestroyed=").append(this.mDestroyed).append(" mContext=").append(this.mContext).append(" mCi=").append(this.mCi).append(" mFh=").append(this.mFh).append(" mParentApp=").append(this.mParentApp).append(" recordsLoadedRegistrants=").append(this.mRecordsLoadedRegistrants).append(" mImsiReadyRegistrants=").append(this.mImsiReadyRegistrants).append(" mRecordsEventsRegistrants=").append(this.mRecordsEventsRegistrants).append(" mNewSmsRegistrants=").append(this.mNewSmsRegistrants).append(" mNetworkSelectionModeAutomaticRegistrants=").append(this.mNetworkSelectionModeAutomaticRegistrants).append(" recordsToLoad=").append(this.mRecordsToLoad).append(" adnCache=").append(this.mAdnCache).append(" recordsRequested=").append(this.mRecordsRequested).append(" iccid=").append(SubscriptionInfo.givePrintableIccid(this.mFullIccId)).append(" msisdnTag=").append(this.mMsisdnTag).append(" voiceMailNum=").append(Rlog.pii(false, this.mVoiceMailNum)).append(" voiceMailTag=").append(this.mVoiceMailTag).append(" newVoiceMailNum=").append(Rlog.pii(false, this.mNewVoiceMailNum)).append(" newVoiceMailTag=").append(this.mNewVoiceMailTag).append(" isVoiceMailFixed=").append(this.mIsVoiceMailFixed).append(" mImsi=");
        if (this.mImsi != null) {
            str = this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6));
        } else {
            str = "null";
        }
        return append.append(str).append(" mncLength=").append(this.mMncLength).append(" mailboxIndex=").append(this.mMailboxIndex).append(" spn=").append(this.mSpn).toString();
    }

    public IccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        AbstractIccRecords.loadEmailAnrSupportFlag(c);
        AbstractIccRecords.loadAdnLongNumberFlag(c);
        this.mContext = c;
        this.mCi = ci;
        this.mFh = app.getIccFileHandler();
        this.mParentApp = app;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public void dispose() {
        this.mDestroyed.set(true);
        this.auth_rsp = null;
        synchronized (this.mLock) {
            this.mLock.notifyAll();
        }
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

    public int storePendingResponseMessage(Message msg) {
        int key = sNextRequestId.getAndIncrement();
        synchronized (this.mPendingResponses) {
            this.mPendingResponses.put(Integer.valueOf(key), msg);
        }
        return key;
    }

    public Message retrievePendingResponseMessage(Integer key) {
        Message message;
        synchronized (this.mPendingResponses) {
            message = (Message) this.mPendingResponses.remove(key);
        }
        return message;
    }

    public String getIccId() {
        return this.mIccId;
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
        r.notifyResult(Integer.valueOf(0));
        r.notifyResult(Integer.valueOf(1));
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
        String providerName = this.mSpn;
        UiccCardApplication parentApp = this.mParentApp;
        if (parentApp != null) {
            UiccCard card = parentApp.getUiccCard();
            if (card != null) {
                String brandOverride = card.getOperatorBrandOverride();
                if (brandOverride != null) {
                    log("override, providerName=" + providerName);
                    return brandOverride;
                }
                log("no brandOverride, providerName=" + providerName);
                return providerName;
            }
            log("card is null, providerName=" + providerName);
            return providerName;
        }
        log("mParentApp is null, providerName=" + providerName);
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
            sendMessage(obtainMessage(1));
        }
    }

    public boolean getRecordsLoaded() {
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            return true;
        }
        return false;
    }

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
                    try {
                        this.mLock.notifyAll();
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                return;
            case 100:
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
                    return;
                } catch (RuntimeException exc) {
                    loge("Exception parsing SIM record: " + exc);
                    onRecordLoaded();
                    return;
                } catch (Throwable th2) {
                    onRecordLoaded();
                    throw th2;
                }
            default:
                super.handleMessage(msg);
                return;
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
        for (int i = 0; i + 1 < languages.length; i += 2) {
            String lang = new String(languages, i, 2, "ISO-8859-1");
            int j = 0;
            while (j < locales.length) {
                if (locales[j] != null && locales[j].length() >= 2 && locales[j].substring(0, 2).equalsIgnoreCase(lang)) {
                    return lang;
                }
                j++;
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
        return -1;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
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

    protected void setSystemProperty(String key, String val) {
        TelephonyManager.getDefault();
        TelephonyManager.setTelephonyProperty(this.mParentApp.getPhoneId(), key, val);
        log("[key, value]=" + key + ", " + val);
    }

    /* JADX WARNING: Missing block: B:14:0x0027, code:
            if (r8.auth_rsp != null) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:15:0x0029, code:
            loge("getIccSimChallengeResponse: No authentication response");
     */
    /* JADX WARNING: Missing block: B:16:0x002f, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:34:0x004c, code:
            log("getIccSimChallengeResponse: return auth_rsp");
     */
    /* JADX WARNING: Missing block: B:35:0x005b, code:
            return android.util.Base64.encodeToString(r8.auth_rsp.payload, 2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        int i;
        pw.println("IccRecords: " + this);
        pw.println(" mDestroyed=" + this.mDestroyed);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mFh=" + this.mFh);
        pw.println(" mParentApp=" + this.mParentApp);
        pw.println(" recordsLoadedRegistrants: size=" + this.mRecordsLoadedRegistrants.size());
        for (i = 0; i < this.mRecordsLoadedRegistrants.size(); i++) {
            pw.println("  recordsLoadedRegistrants[" + i + "]=" + ((Registrant) this.mRecordsLoadedRegistrants.get(i)).getHandler());
        }
        pw.println(" mImsiReadyRegistrants: size=" + this.mImsiReadyRegistrants.size());
        for (i = 0; i < this.mImsiReadyRegistrants.size(); i++) {
            pw.println("  mImsiReadyRegistrants[" + i + "]=" + ((Registrant) this.mImsiReadyRegistrants.get(i)).getHandler());
        }
        pw.println(" mRecordsEventsRegistrants: size=" + this.mRecordsEventsRegistrants.size());
        for (i = 0; i < this.mRecordsEventsRegistrants.size(); i++) {
            pw.println("  mRecordsEventsRegistrants[" + i + "]=" + ((Registrant) this.mRecordsEventsRegistrants.get(i)).getHandler());
        }
        pw.println(" mNewSmsRegistrants: size=" + this.mNewSmsRegistrants.size());
        for (i = 0; i < this.mNewSmsRegistrants.size(); i++) {
            pw.println("  mNewSmsRegistrants[" + i + "]=" + ((Registrant) this.mNewSmsRegistrants.get(i)).getHandler());
        }
        pw.println(" mNetworkSelectionModeAutomaticRegistrants: size=" + this.mNetworkSelectionModeAutomaticRegistrants.size());
        for (i = 0; i < this.mNetworkSelectionModeAutomaticRegistrants.size(); i++) {
            pw.println("  mNetworkSelectionModeAutomaticRegistrants[" + i + "]=" + ((Registrant) this.mNetworkSelectionModeAutomaticRegistrants.get(i)).getHandler());
        }
        pw.println(" mRecordsRequested=" + this.mRecordsRequested);
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
        pw.println(" mImsi=" + (this.mImsi != null ? this.mImsi.substring(0, 6) + Rlog.pii(false, this.mImsi.substring(6)) : "null"));
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
