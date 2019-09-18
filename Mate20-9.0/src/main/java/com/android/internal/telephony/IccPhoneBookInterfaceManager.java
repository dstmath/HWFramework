package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class IccPhoneBookInterfaceManager extends AbstractIccPhoneBookInterfaceManager {
    protected static final boolean ALLOW_SIM_OP_IN_UI_THREAD = false;
    protected static final boolean DBG = true;
    protected static final int EVENT_GET_SIZE_DONE = 1;
    protected static final int EVENT_LOAD_DONE = 2;
    protected static final int EVENT_UPDATE_DONE = 3;
    static final String LOG_TAG = "IccPhoneBookIM";
    protected AdnRecordCache mAdnCache;
    protected Handler mBaseHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case 1:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    synchronized (IccPhoneBookInterfaceManager.this.mLock) {
                        if (ar.exception == null) {
                            IccPhoneBookInterfaceManager.this.mRecordSize = (int[]) ar.result;
                            IccPhoneBookInterfaceManager.this.logd("GET_RECORD_SIZE Size " + IccPhoneBookInterfaceManager.this.mRecordSize[0] + " total " + IccPhoneBookInterfaceManager.this.mRecordSize[1] + " #record " + IccPhoneBookInterfaceManager.this.mRecordSize[2]);
                        }
                        notifyPending(ar);
                    }
                    return;
                case 2:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    synchronized (IccPhoneBookInterfaceManager.this.mLock) {
                        if (ar2.exception == null) {
                            IccPhoneBookInterfaceManager.this.mRecords = (List) ar2.result;
                        } else {
                            IccPhoneBookInterfaceManager.this.logd("Cannot load ADN records");
                            IccPhoneBookInterfaceManager.this.mRecords = null;
                        }
                        notifyPending(ar2);
                    }
                    return;
                case 3:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    synchronized (IccPhoneBookInterfaceManager.this.mLock) {
                        IccPhoneBookInterfaceManager iccPhoneBookInterfaceManager = IccPhoneBookInterfaceManager.this;
                        if (ar3.exception == null) {
                            z = true;
                        }
                        iccPhoneBookInterfaceManager.mSuccess = z;
                        notifyPending(ar3);
                    }
                    return;
                default:
                    return;
            }
        }

        private void notifyPending(AsyncResult ar) {
            if (ar.userObj != null) {
                ((AtomicBoolean) ar.userObj).set(true);
            }
            IccPhoneBookInterfaceManager.this.mLock.notifyAll();
        }
    };
    protected final Object mLock = new Object();
    protected Object mLock2 = new Object();
    protected Phone mPhone;
    protected int[] mRecordSize;
    protected List<AdnRecord> mRecords;
    protected boolean mSuccess;

    public IccPhoneBookInterfaceManager(Phone phone) {
        this.mPhone = phone;
        IccRecords r = phone.getIccRecords();
        if (r != null) {
            this.mAdnCache = r.getAdnCache();
        }
    }

    public void dispose() {
        if (this.mRecords != null) {
            this.mRecords.clear();
        }
    }

    public void updateIccRecords(IccRecords iccRecords) {
        if (iccRecords != null) {
            this.mAdnCache = iccRecords.getAdnCache();
        } else {
            this.mAdnCache = null;
        }
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, "[IccPbInterfaceManager] " + msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, "[IccPbInterfaceManager] " + msg);
    }

    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        synchronized (this.mLock2) {
            try {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") == 0) {
                    logd("updateAdnRecordsInEfBySearch: efid=0x" + Integer.toHexString(efid).toUpperCase() + " (" + oldTag + ")==> (" + newTag + ")");
                    int efid2 = updateEfForIccType(efid);
                    try {
                        synchronized (this.mLock) {
                            checkThread();
                            this.mSuccess = false;
                            AtomicBoolean status = new AtomicBoolean(false);
                            Message response = this.mBaseHandler.obtainMessage(3, status);
                            AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber);
                            AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber);
                            if (this.mAdnCache != null) {
                                this.mAdnCache.updateAdnBySearch(efid2, oldAdn, newAdn, pin2, response);
                                waitForResult(status);
                            } else {
                                loge("Failure while trying to update by search due to uninitialised adncache");
                            }
                        }
                        return this.mSuccess;
                    } catch (Throwable th) {
                        th = th;
                        int i = efid2;
                    }
                } else {
                    throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007d, code lost:
        return r1.mSuccess;
     */
    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        AtomicBoolean status;
        Message response;
        String str = newTag;
        synchronized (this.mLock2) {
            try {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") == 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("updateAdnRecordsInEfByIndex: efid=0x");
                    sb.append(Integer.toHexString(efid).toUpperCase());
                    sb.append(" Index=");
                    int i = index;
                    try {
                        sb.append(i);
                        sb.append(" ==> (");
                        sb.append(str);
                        sb.append(")");
                        logd(sb.toString());
                        synchronized (this.mLock) {
                            try {
                                checkThread();
                                this.mSuccess = false;
                                status = new AtomicBoolean(false);
                                response = this.mBaseHandler.obtainMessage(3, status);
                            } catch (Throwable th) {
                                th = th;
                                String str2 = newPhoneNumber;
                                throw th;
                            }
                            try {
                                AdnRecord newAdn = new AdnRecord(str, newPhoneNumber);
                                if (this.mAdnCache != null) {
                                    this.mAdnCache.updateAdnByIndex(efid, newAdn, i, pin2, response);
                                    waitForResult(status);
                                } else {
                                    loge("Failure while trying to update by index due to uninitialised adncache");
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        String str3 = newPhoneNumber;
                        throw th;
                    }
                } else {
                    String str4 = newPhoneNumber;
                    int i2 = index;
                    throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
                }
            } catch (Throwable th4) {
                th = th4;
                String str5 = newPhoneNumber;
                int i3 = index;
                throw th;
            }
        }
    }

    public int[] getAdnRecordsSize(int efid) {
        logd("getAdnRecordsSize: efid=" + efid);
        synchronized (this.mLock) {
            checkThread();
            this.mRecordSize = new int[3];
            AtomicBoolean status = new AtomicBoolean(false);
            Message response = this.mBaseHandler.obtainMessage(1, status);
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                fh.getEFLinearRecordSize(efid, response);
                waitForResult(status);
            }
        }
        return this.mRecordSize;
    }

    public List<AdnRecord> getAdnRecordsInEf(int efid) {
        List<AdnRecord> list;
        synchronized (this.mLock2) {
            if (this.mPhone != null) {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.READ_CONTACTS") != 0) {
                    throw new SecurityException("Requires android.permission.READ_CONTACTS permission");
                }
            }
            int efid2 = updateEfForIccType(efid);
            logd("getAdnRecordsInEF: efid=0x" + Integer.toHexString(efid2).toUpperCase());
            synchronized (this.mLock) {
                checkThread();
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(2, status);
                if (this.mAdnCache != null) {
                    this.mAdnCache.requestLoadAllAdnLike(efid2, this.mAdnCache.extensionEfForEf(efid2), response);
                    waitForResult(status);
                } else {
                    loge("Failure while trying to load from SIM due to uninitialised adncache");
                }
            }
            list = this.mRecords;
        }
        return list;
    }

    /* access modifiers changed from: protected */
    public void checkThread() {
        if (this.mBaseHandler.getLooper().equals(Looper.myLooper())) {
            loge("query() called on the main UI thread!");
            throw new IllegalStateException("You cannot call query on this provder from the main UI thread.");
        }
    }

    /* access modifiers changed from: protected */
    public void waitForResult(AtomicBoolean status) {
        while (!status.get()) {
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                logd("interrupted while trying to update by search");
            }
        }
    }

    private int updateEfForIccType(int efid) {
        if (efid == 28474 && (this.mPhone.getCurrentUiccAppType() == IccCardApplicationStatus.AppType.APPTYPE_USIM || this.mPhone.getCurrentUiccAppType() == IccCardApplicationStatus.AppType.APPTYPE_CSIM || this.mPhone.getCurrentUiccAppType() == IccCardApplicationStatus.AppType.APPTYPE_ISIM)) {
            return updateEfFor3gCardType(efid);
        }
        return efid;
    }

    public int getAlphaTagEncodingLength(String alphaTag) {
        return HwTelephonyFactory.getHwUiccManager().getAlphaTagEncodingLength(alphaTag);
    }

    public int updateEfForIccTypeHw(int efid) {
        return updateEfForIccType(efid);
    }
}
