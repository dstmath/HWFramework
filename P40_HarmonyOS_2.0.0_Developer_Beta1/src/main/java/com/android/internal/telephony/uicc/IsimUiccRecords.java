package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.IccRecords;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
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
    @UnsupportedAppUsage
    private String auth_rsp;
    @UnsupportedAppUsage
    private String mIsimDomain;
    @UnsupportedAppUsage
    private String mIsimImpi;
    @UnsupportedAppUsage
    private String[] mIsimImpu;
    @UnsupportedAppUsage
    private String mIsimIst;
    @UnsupportedAppUsage
    private String[] mIsimPcscf;
    @UnsupportedAppUsage
    private final Object mLock = new Object();

    @Override // com.android.internal.telephony.uicc.IccRecords, android.os.Handler, java.lang.Object
    public String toString() {
        return "IsimUiccRecords: " + super.toString() + PhoneConfigurationManager.SSSS;
    }

    public IsimUiccRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mAdnCache = new AdnRecordCache(this.mFh);
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication(app);
        CommandsInterfaceEx commandsInterfaceEx = new CommandsInterfaceEx();
        commandsInterfaceEx.setCommandsInterface(ci);
        this.mHwIccRecordsEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwIccRecordsEx(this, uiccCardApplicationEx, c, commandsInterfaceEx);
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        log("IsimUiccRecords X ctor this=" + this);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void dispose() {
        log("Disposing " + this);
        this.mCi.unregisterForIccRefresh(this);
        this.mParentApp.unregisterForReady(this);
        resetRecords();
        super.dispose();
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, android.os.Handler
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
                        log("Failed to parse ISIM AKA contents");
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
    @UnsupportedAppUsage
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
        this.mHwIccRecordsEx.setImsiReady(false);
    }

    /* access modifiers changed from: private */
    public class EfIsimImpiLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimImpiLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_ISIM_IMPI";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords.this.mIsimImpi = IsimUiccRecords.isimTlvToString((byte[]) ar.result);
        }
    }

    /* access modifiers changed from: private */
    public class EfIsimImpuLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimImpuLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_ISIM_IMPU";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> impuList = (ArrayList) ar.result;
            IsimUiccRecords isimUiccRecords = IsimUiccRecords.this;
            isimUiccRecords.log("EF_IMPU record count: " + impuList.size());
            IsimUiccRecords.this.mIsimImpu = new String[impuList.size()];
            int i = 0;
            Iterator<byte[]> it = impuList.iterator();
            while (it.hasNext()) {
                IsimUiccRecords.this.mIsimImpu[i] = IsimUiccRecords.isimTlvToString(it.next());
                i++;
            }
        }
    }

    /* access modifiers changed from: private */
    public class EfIsimDomainLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimDomainLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_ISIM_DOMAIN";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords.this.mIsimDomain = IsimUiccRecords.isimTlvToString((byte[]) ar.result);
        }
    }

    /* access modifiers changed from: private */
    public class EfIsimIstLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimIstLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_ISIM_IST";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            IsimUiccRecords.this.mIsimIst = IccUtils.bytesToHexString((byte[]) ar.result);
        }
    }

    /* access modifiers changed from: private */
    public class EfIsimPcscfLoaded implements IccRecords.IccRecordLoaded {
        private EfIsimPcscfLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_ISIM_PCSCF";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> pcscflist = (ArrayList) ar.result;
            IsimUiccRecords isimUiccRecords = IsimUiccRecords.this;
            isimUiccRecords.log("EF_PCSCF record count: " + pcscflist.size());
            IsimUiccRecords.this.mIsimPcscf = new String[pcscflist.size()];
            int i = 0;
            Iterator<byte[]> it = pcscflist.iterator();
            while (it.hasNext()) {
                IsimUiccRecords.this.mIsimPcscf[i] = IsimUiccRecords.isimTlvToString(it.next());
                i++;
            }
        }
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
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
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (getRecordsLoaded()) {
            onAllRecordsLoaded();
            this.mHwIccRecordsEx.setImsiReady(true);
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
            this.mLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else if (this.mLockedRecordsReqReason == 2) {
            this.mNetworkLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else {
            loge("onLockedAllRecordsLoaded: unexpected mLockedRecordsReqReason " + this.mLockedRecordsReqReason);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onAllRecordsLoaded() {
        log("record load complete");
        this.mLoaded.set(true);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void handleFileUpdate(int efid) {
        if (efid != 28423) {
            if (efid != 28425) {
                switch (efid) {
                    case IccConstants.EF_IMPI /* 28418 */:
                        this.mFh.loadEFTransparent(IccConstants.EF_IMPI, obtainMessage(100, new EfIsimImpiLoaded()));
                        this.mRecordsToLoad++;
                        return;
                    case IccConstants.EF_DOMAIN /* 28419 */:
                        this.mFh.loadEFTransparent(IccConstants.EF_DOMAIN, obtainMessage(100, new EfIsimDomainLoaded()));
                        this.mRecordsToLoad++;
                        return;
                    case IccConstants.EF_IMPU /* 28420 */:
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

    @Override // com.android.internal.telephony.uicc.IsimRecords
    public String getIsimImpi() {
        return this.mIsimImpi;
    }

    @Override // com.android.internal.telephony.uicc.IsimRecords
    public String getIsimDomain() {
        return this.mIsimDomain;
    }

    @Override // com.android.internal.telephony.uicc.IsimRecords
    public String[] getIsimImpu() {
        String[] strArr = this.mIsimImpu;
        if (strArr != null) {
            return (String[]) strArr.clone();
        }
        return null;
    }

    @Override // com.android.internal.telephony.uicc.IsimRecords
    public String getIsimIst() {
        return this.mIsimIst;
    }

    @Override // com.android.internal.telephony.uicc.IsimRecords
    public String[] getIsimPcscf() {
        String[] strArr = this.mIsimPcscf;
        if (strArr != null) {
            return (String[]) strArr.clone();
        }
        return null;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onReady() {
        fetchIsimRecords();
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchIsimRecords();
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMessageWaiting(int line, int countWaiting) {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void log(String s) {
        Rlog.i(LOG_TAG, "[ISIM] " + s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[ISIM] " + s);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("IsimRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.flush();
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getVoiceMessageCount() {
        return 0;
    }
}
