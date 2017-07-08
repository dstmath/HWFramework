package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import com.google.android.mms.pdu.PduPersister;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class IsimUiccRecords extends IccRecords implements IsimRecords {
    private static final boolean DBG = true;
    private static final boolean DUMP_RECORDS = false;
    private static final int EVENT_AKA_AUTHENTICATE_DONE = 90;
    private static final int EVENT_APP_READY = 1;
    private static final int EVENT_ISIM_REFRESH = 31;
    public static final String INTENT_ISIM_REFRESH = "com.android.intent.isim_refresh";
    protected static final String LOG_TAG = "IsimUiccRecords";
    private static final int TAG_ISIM_VALUE = 128;
    private static final boolean VDBG = false;
    private String auth_rsp;
    private String mIsimDomain;
    private String mIsimImpi;
    private String[] mIsimImpu;
    private String mIsimIst;
    private String[] mIsimPcscf;
    private final Object mLock;

    private class EfIsimDomainLoaded implements IccRecordLoaded {
        private EfIsimDomainLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_DOMAIN";
        }

        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords.this.mIsimDomain = IsimUiccRecords.isimTlvToString(ar.result);
        }
    }

    private class EfIsimImpiLoaded implements IccRecordLoaded {
        private EfIsimImpiLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IMPI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords.this.mIsimImpi = IsimUiccRecords.isimTlvToString(ar.result);
        }
    }

    private class EfIsimImpuLoaded implements IccRecordLoaded {
        private EfIsimImpuLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IMPU";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> impuList = ar.result;
            IsimUiccRecords.this.log("EF_IMPU record count: " + impuList.size());
            IsimUiccRecords.this.mIsimImpu = new String[impuList.size()];
            int i = 0;
            for (byte[] identity : impuList) {
                int i2 = i + IsimUiccRecords.EVENT_APP_READY;
                IsimUiccRecords.this.mIsimImpu[i] = IsimUiccRecords.isimTlvToString(identity);
                i = i2;
            }
        }
    }

    private class EfIsimIstLoaded implements IccRecordLoaded {
        private EfIsimIstLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IST";
        }

        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords.this.mIsimIst = IccUtils.bytesToHexString(ar.result);
        }
    }

    private class EfIsimPcscfLoaded implements IccRecordLoaded {
        private EfIsimPcscfLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_PCSCF";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> pcscflist = ar.result;
            IsimUiccRecords.this.log("EF_PCSCF record count: " + pcscflist.size());
            IsimUiccRecords.this.mIsimPcscf = new String[pcscflist.size()];
            int i = 0;
            for (byte[] identity : pcscflist) {
                int i2 = i + IsimUiccRecords.EVENT_APP_READY;
                IsimUiccRecords.this.mIsimPcscf[i] = IsimUiccRecords.isimTlvToString(identity);
                i = i2;
            }
        }
    }

    public String toString() {
        return "IsimUiccRecords: " + super.toString() + "";
    }

    public IsimUiccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mLock = new Object();
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mRecordsRequested = DUMP_RECORDS;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mCi.registerForIccRefresh(this, EVENT_ISIM_REFRESH, null);
        this.mParentApp.registerForReady(this, EVENT_APP_READY, null);
        log("IsimUiccRecords X ctor this=" + this);
    }

    public void dispose() {
        log("Disposing " + this);
        this.mCi.unregisterForIccRefresh(this);
        this.mParentApp.unregisterForReady(this);
        resetRecords();
        super.dispose();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        if (this.mDestroyed.get()) {
            Rlog.e(LOG_TAG, "Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        loge("IsimUiccRecords: handleMessage " + msg + "[" + msg.what + "] ");
        try {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_APP_READY /*1*/:
                    onReady();
                    break;
                case EVENT_ISIM_REFRESH /*31*/:
                    ar = msg.obj;
                    loge("ISim REFRESH(EVENT_ISIM_REFRESH) with exception: " + ar.exception);
                    if (ar.exception == null) {
                        Intent intent = new Intent(INTENT_ISIM_REFRESH);
                        loge("send ISim REFRESH: com.android.intent.isim_refresh");
                        this.mContext.sendBroadcast(intent);
                        handleIsimRefresh((IccRefreshResponse) ar.result);
                        break;
                    }
                    break;
                case EVENT_AKA_AUTHENTICATE_DONE /*90*/:
                    ar = (AsyncResult) msg.obj;
                    log("EVENT_AKA_AUTHENTICATE_DONE");
                    if (ar.exception != null) {
                        log("Exception ISIM AKA: " + ar.exception);
                    } else {
                        try {
                            this.auth_rsp = (String) ar.result;
                            log("ISIM AKA: auth_rsp = " + this.auth_rsp);
                        } catch (Exception e) {
                            log("Failed to parse ISIM AKA contents: " + e);
                        }
                    }
                    synchronized (this.mLock) {
                        this.mLock.notifyAll();
                        break;
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing SIM record", exc);
        }
    }

    protected void fetchIsimRecords() {
        this.mRecordsRequested = DBG;
        this.mFh.loadEFTransparent(IccConstants.EF_IMPI, obtainMessage(100, new EfIsimImpiLoaded()));
        this.mRecordsToLoad += EVENT_APP_READY;
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_IMPU, obtainMessage(100, new EfIsimImpuLoaded()));
        this.mRecordsToLoad += EVENT_APP_READY;
        this.mFh.loadEFTransparent(IccConstants.EF_DOMAIN, obtainMessage(100, new EfIsimDomainLoaded()));
        this.mRecordsToLoad += EVENT_APP_READY;
        this.mFh.loadEFTransparent(IccConstants.EF_IST, obtainMessage(100, new EfIsimIstLoaded()));
        this.mRecordsToLoad += EVENT_APP_READY;
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_PCSCF, obtainMessage(100, new EfIsimPcscfLoaded()));
        this.mRecordsToLoad += EVENT_APP_READY;
        log("fetchIsimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    protected void resetRecords() {
        this.mIsimImpi = null;
        this.mIsimDomain = null;
        this.mIsimImpu = null;
        this.mIsimIst = null;
        this.mIsimPcscf = null;
        this.auth_rsp = null;
        this.mRecordsRequested = DUMP_RECORDS;
        this.mImsiLoad = DUMP_RECORDS;
    }

    private static String isimTlvToString(byte[] record) {
        SimTlv tlv = new SimTlv(record, 0, record.length);
        while (tlv.getTag() != TAG_ISIM_VALUE) {
            if (!tlv.nextObject()) {
                return null;
            }
        }
        return new String(tlv.getData(), Charset.forName("UTF-8"));
    }

    protected void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            onAllRecordsLoaded();
            this.mImsiLoad = DBG;
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    protected void onAllRecordsLoaded() {
        log("record load complete");
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
    }

    private void handleFileUpdate(int efid) {
        switch (efid) {
            case IccConstants.EF_IMPI /*28418*/:
                this.mFh.loadEFTransparent(IccConstants.EF_IMPI, obtainMessage(100, new EfIsimImpiLoaded()));
                this.mRecordsToLoad += EVENT_APP_READY;
                return;
            case IccConstants.EF_DOMAIN /*28419*/:
                this.mFh.loadEFTransparent(IccConstants.EF_DOMAIN, obtainMessage(100, new EfIsimDomainLoaded()));
                this.mRecordsToLoad += EVENT_APP_READY;
                return;
            case IccConstants.EF_IMPU /*28420*/:
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_IMPU, obtainMessage(100, new EfIsimImpuLoaded()));
                this.mRecordsToLoad += EVENT_APP_READY;
                return;
            case IccConstants.EF_IST /*28423*/:
                this.mFh.loadEFTransparent(IccConstants.EF_IST, obtainMessage(100, new EfIsimIstLoaded()));
                this.mRecordsToLoad += EVENT_APP_READY;
                return;
            case IccConstants.EF_PCSCF /*28425*/:
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_PCSCF, obtainMessage(100, new EfIsimPcscfLoaded()));
                this.mRecordsToLoad += EVENT_APP_READY;
                break;
        }
        fetchIsimRecords();
    }

    private void handleIsimRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null) {
            log("handleIsimRefresh received without input");
        } else if (refreshResponse.aid == null || refreshResponse.aid.equals(this.mParentApp.getAid())) {
            switch (refreshResponse.refreshResult) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                    log("handleIsimRefresh with REFRESH_RESULT_FILE_UPDATE");
                    handleFileUpdate(refreshResponse.efId);
                    break;
                case EVENT_APP_READY /*1*/:
                    log("handleIsimRefresh with REFRESH_RESULT_INIT");
                    fetchIsimRecords();
                    break;
                case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                    log("handleIsimRefresh with REFRESH_RESULT_RESET");
                    break;
                default:
                    log("handleIsimRefresh with unknown operation");
                    break;
            }
        } else {
            log("handleIsimRefresh received different app");
        }
    }

    public String getIsimImpi() {
        return this.mIsimImpi;
    }

    public String getIsimDomain() {
        return this.mIsimDomain;
    }

    public String[] getIsimImpu() {
        return this.mIsimImpu != null ? (String[]) this.mIsimImpu.clone() : null;
    }

    public String getIsimIst() {
        return this.mIsimIst;
    }

    public String[] getIsimPcscf() {
        return this.mIsimPcscf != null ? (String[]) this.mIsimPcscf.clone() : null;
    }

    public String getIsimChallengeResponse(String nonce) {
        log("getIsimChallengeResponse-nonce:" + nonce);
        try {
            synchronized (this.mLock) {
                this.mCi.requestIsimAuthentication(nonce, obtainMessage(EVENT_AKA_AUTHENTICATE_DONE));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    log("interrupted while trying to request Isim Auth");
                }
            }
            log("getIsimChallengeResponse-auth_rsp" + this.auth_rsp);
            return this.auth_rsp;
        } catch (Exception e2) {
            log("Fail while trying to request Isim Auth");
            return null;
        }
    }

    public int getDisplayRule(String plmn) {
        return 0;
    }

    public void onReady() {
        fetchIsimRecords();
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchIsimRecords();
        }
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, "[ISIM] " + s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "[ISIM] " + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("IsimRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.flush();
    }

    public int getVoiceMessageCount() {
        return 0;
    }
}
