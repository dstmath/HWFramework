package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.IccRecords;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

public class IsimUiccRecords extends IccRecords implements IsimRecords {
    private static final boolean DBG = true;
    private static final boolean DUMP_RECORDS = false;
    private static final int EVENT_APP_READY = 1;
    private static final int EVENT_ISIM_AUTHENTICATE_DONE = 91;
    public static final String INTENT_ISIM_REFRESH = "com.android.intent.isim_refresh";
    protected static final String LOG_TAG = "IsimUiccRecords";
    private static final int TAG_ISIM_VALUE = 128;
    private static final boolean VDBG = false;
    private String auth_rsp;
    /* access modifiers changed from: private */
    public String mIsimDomain;
    /* access modifiers changed from: private */
    public String mIsimImpi;
    /* access modifiers changed from: private */
    public String[] mIsimImpu;
    /* access modifiers changed from: private */
    public String mIsimIst;
    /* access modifiers changed from: private */
    public String[] mIsimPcscf;
    private final Object mLock = new Object();

    private class EfIsimDomainLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimDomainLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_DOMAIN";
        }

        public void onRecordLoaded(AsyncResult ar) {
            String unused = IsimUiccRecords.this.mIsimDomain = IsimUiccRecords.isimTlvToString((byte[]) ar.result);
        }
    }

    private class EfIsimImpiLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimImpiLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IMPI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            String unused = IsimUiccRecords.this.mIsimImpi = IsimUiccRecords.isimTlvToString((byte[]) ar.result);
        }
    }

    private class EfIsimImpuLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimImpuLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IMPU";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> impuList = (ArrayList) ar.result;
            IsimUiccRecords isimUiccRecords = IsimUiccRecords.this;
            isimUiccRecords.log("EF_IMPU record count: " + impuList.size());
            String[] unused = IsimUiccRecords.this.mIsimImpu = new String[impuList.size()];
            int i = 0;
            Iterator<byte[]> it = impuList.iterator();
            while (it.hasNext()) {
                IsimUiccRecords.this.mIsimImpu[i] = IsimUiccRecords.isimTlvToString(it.next());
                i++;
            }
        }
    }

    private class EfIsimIstLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimIstLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_IST";
        }

        public void onRecordLoaded(AsyncResult ar) {
            String unused = IsimUiccRecords.this.mIsimIst = IccUtils.bytesToHexString((byte[]) ar.result);
        }
    }

    private class EfIsimPcscfLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimPcscfLoaded() {
        }

        public String getEfName() {
            return "EF_ISIM_PCSCF";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> pcscflist = (ArrayList) ar.result;
            IsimUiccRecords isimUiccRecords = IsimUiccRecords.this;
            isimUiccRecords.log("EF_PCSCF record count: " + pcscflist.size());
            String[] unused = IsimUiccRecords.this.mIsimPcscf = new String[pcscflist.size()];
            int i = 0;
            Iterator<byte[]> it = pcscflist.iterator();
            while (it.hasNext()) {
                IsimUiccRecords.this.mIsimPcscf[i] = IsimUiccRecords.isimTlvToString(it.next());
                i++;
            }
        }
    }

    public String toString() {
        return "IsimUiccRecords: " + super.toString() + "";
    }

    public IsimUiccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        log("IsimUiccRecords X ctor this=" + this);
    }

    public void dispose() {
        log("Disposing " + this);
        this.mCi.unregisterForIccRefresh(this);
        this.mParentApp.unregisterForReady(this);
        resetRecords();
        super.dispose();
    }

    public void handleMessage(Message msg) {
        if (this.mDestroyed.get()) {
            Rlog.e(LOG_TAG, "Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        loge("IsimUiccRecords: handleMessage " + msg + "[" + msg.what + "] ");
        try {
            int i = msg.what;
            if (i == 1) {
                onReady();
            } else if (i == 31) {
                broadcastRefresh();
                super.handleMessage(msg);
            } else if (i != 91) {
                super.handleMessage(msg);
            } else {
                AsyncResult ar = (AsyncResult) msg.obj;
                log("EVENT_ISIM_AUTHENTICATE_DONE");
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
                }
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing SIM record", exc);
        }
    }

    /* access modifiers changed from: protected */
    public void fetchIsimRecords() {
        this.mRecordsRequested = true;
        this.mFh.loadEFTransparent(IccConstants.EF_IMPI, obtainMessage(100, new EfIsimImpiLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_IMPU, obtainMessage(100, new EfIsimImpuLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_DOMAIN, obtainMessage(100, new EfIsimDomainLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_IST, obtainMessage(100, new EfIsimIstLoaded()));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_PCSCF, obtainMessage(100, new EfIsimPcscfLoaded()));
        this.mRecordsToLoad++;
        log("fetchIsimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
        this.mIsimImpi = null;
        this.mIsimDomain = null;
        this.mIsimImpu = null;
        this.mIsimIst = null;
        this.mIsimPcscf = null;
        this.auth_rsp = null;
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mLoaded.set(false);
        this.mImsiLoad = false;
    }

    /* access modifiers changed from: private */
    public static String isimTlvToString(byte[] record) {
        SimTlv tlv = new SimTlv(record, 0, record.length);
        while (tlv.getTag() != 128) {
            if (!tlv.nextObject()) {
                return null;
            }
        }
        return new String(tlv.getData(), Charset.forName("UTF-8"));
    }

    /* access modifiers changed from: protected */
    public void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (getRecordsLoaded()) {
            onAllRecordsLoaded();
            this.mImsiLoad = true;
        } else if (getLockedRecordsLoaded() || getNetworkLockedRecordsLoaded()) {
            onLockedAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    private void onLockedAllRecordsLoaded() {
        log("SIM locked; record load complete");
        if (this.mLockedRecordsReqReason == 1) {
            this.mLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else if (this.mLockedRecordsReqReason == 2) {
            this.mNetworkLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else {
            loge("onLockedAllRecordsLoaded: unexpected mLockedRecordsReqReason " + this.mLockedRecordsReqReason);
        }
    }

    /* access modifiers changed from: protected */
    public void onAllRecordsLoaded() {
        log("record load complete");
        this.mLoaded.set(true);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
    }

    /* access modifiers changed from: protected */
    public void handleFileUpdate(int efid) {
        if (efid != 28423) {
            if (efid != 28425) {
                switch (efid) {
                    case IccConstants.EF_IMPI:
                        this.mFh.loadEFTransparent(IccConstants.EF_IMPI, obtainMessage(100, new EfIsimImpiLoaded()));
                        this.mRecordsToLoad++;
                        return;
                    case IccConstants.EF_DOMAIN:
                        this.mFh.loadEFTransparent(IccConstants.EF_DOMAIN, obtainMessage(100, new EfIsimDomainLoaded()));
                        this.mRecordsToLoad++;
                        return;
                    case IccConstants.EF_IMPU:
                        this.mFh.loadEFLinearFixedAll(IccConstants.EF_IMPU, obtainMessage(100, new EfIsimImpuLoaded()));
                        this.mRecordsToLoad++;
                        return;
                }
            } else {
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_PCSCF, obtainMessage(100, new EfIsimPcscfLoaded()));
                this.mRecordsToLoad++;
            }
            fetchIsimRecords();
            return;
        }
        this.mFh.loadEFTransparent(IccConstants.EF_IST, obtainMessage(100, new EfIsimIstLoaded()));
        this.mRecordsToLoad++;
    }

    private void broadcastRefresh() {
        Intent intent = new Intent(INTENT_ISIM_REFRESH);
        log("send ISim REFRESH: com.android.intent.isim_refresh");
        this.mContext.sendBroadcast(intent);
    }

    public String getIsimImpi() {
        return this.mIsimImpi;
    }

    public String getIsimDomain() {
        return this.mIsimDomain;
    }

    public String[] getIsimImpu() {
        if (this.mIsimImpu != null) {
            return (String[]) this.mIsimImpu.clone();
        }
        return null;
    }

    public String getIsimIst() {
        return this.mIsimIst;
    }

    public String[] getIsimPcscf() {
        if (this.mIsimPcscf != null) {
            return (String[]) this.mIsimPcscf.clone();
        }
        return null;
    }

    public int getDisplayRule(ServiceState serviceState) {
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

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(LOG_TAG, "[ISIM] " + s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
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
