package com.android.internal.telephony;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.IccPhoneBookInterfaceManager;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import java.util.Arrays;

public class HwIccPhoneBookInterfaceManager extends IccPhoneBookInterfaceManager {
    private static final String ACTION_READ_SIM_CONTACTS_DONE = "com.huawei.intent.action.READ_SIM_CONTACTS_DONE";
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HwIccPhoneBookInterfaceManager";
    private volatile boolean isGettingAdnRecordsSize = false;
    private UsimPhoneBookManager mUsimPhoneBookManager;

    public HwIccPhoneBookInterfaceManager(Phone phone) {
        super(phone);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00f1, code lost:
        if (r0.getRequestResult() != null) goto L_0x00f6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00f3, code lost:
        r16 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00f6, code lost:
        r16 = ((java.lang.Boolean) r0.getRequestResult()).booleanValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0103, code lost:
        return r16;
     */
    public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) {
        StringBuilder sb;
        synchronized (this.mLock2) {
            try {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
                    throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
                } else if (values == null) {
                    Rlog.e(LOG_TAG, "input values is null.");
                    return false;
                } else {
                    String oldTag = values.getAsString("tag");
                    String newTag = values.getAsString("newTag");
                    String oldPhoneNumber = values.getAsString("number");
                    String newPhoneNumber = values.getAsString("newNumber");
                    String oldEmail = values.getAsString("emails");
                    String newEmail = values.getAsString("newEmails");
                    String oldAnr = values.getAsString("anrs");
                    String newAnr = values.getAsString("newAnrs");
                    String[] oldEmailArray = TextUtils.isEmpty(oldEmail) ? null : new String[]{oldEmail};
                    String[] newEmailArray = TextUtils.isEmpty(newEmail) ? null : new String[]{newEmail};
                    String[] oldAnrArray = TextUtils.isEmpty(oldAnr) ? null : new String[]{oldAnr};
                    String[] newAnrArray = TextUtils.isEmpty(newAnr) ? null : new String[]{newAnr};
                    int efid2 = updateEfForIccTypeHw(efid);
                    try {
                        sb = new StringBuilder();
                        sb.append("updateAdnRecordsWithContentValuesInEfBySearch: efid=");
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                    try {
                        sb.append(efid2);
                        sb.append(", pin2=xxxx");
                        Rlog.i(LOG_TAG, sb.toString());
                        checkThread();
                        IccPhoneBookInterfaceManager.PhoneBookRequestHw updateRequest = new IccPhoneBookInterfaceManager.PhoneBookRequestHw();
                        synchronized (updateRequest.request) {
                            try {
                                try {
                                    try {
                                        Message response = this.mBaseHandler.obtainMessage(3, updateRequest.request);
                                        AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber, oldEmailArray, oldAnrArray);
                                        AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmailArray, newAnrArray);
                                        if (this.mAdnCache != null) {
                                            this.mAdnCache.updateAdnBySearch(efid2, oldAdn, newAdn, pin2, response);
                                            waitForResult(updateRequest.request);
                                        } else {
                                            Rlog.e(LOG_TAG, "Failure while trying to update by search due to uninitialised adncache");
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        throw th;
                    }
                }
            } catch (Throwable th6) {
                th = th6;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008b, code lost:
        if (r0.getRequestResult() != null) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x008d, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x008f, code lost:
        r0 = ((java.lang.Boolean) r0.getRequestResult()).booleanValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x009a, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a3, code lost:
        r0 = th;
     */
    public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        synchronized (this.mLock2) {
            try {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") == 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("updateUsimAdnRecordsInEfByIndexHW: efid=");
                    try {
                        sb.append(efid);
                        sb.append(" sEf_id=");
                        try {
                            sb.append(sEf_id);
                            sb.append(" Index=");
                        } catch (Throwable th) {
                            th = th;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                        try {
                            sb.append(index);
                            sb.append(" pin2=xxxx");
                            Rlog.i(LOG_TAG, sb.toString());
                            checkThread();
                            IccPhoneBookInterfaceManager.PhoneBookRequestHw updateRequest = new IccPhoneBookInterfaceManager.PhoneBookRequestHw();
                            synchronized (updateRequest.request) {
                                try {
                                    Message response = this.mBaseHandler.obtainMessage(3, updateRequest.request);
                                    try {
                                        AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmails, newAnrNumbers);
                                        int efid2 = updateEfForIccTypeHw(efid);
                                        try {
                                            if (this.mAdnCache != null) {
                                                this.mAdnCache.updateUsimAdnByIndexHW(efid2, newAdn, sEf_id, index, pin2, response);
                                                waitForResult(updateRequest.request);
                                            } else {
                                                Rlog.e(LOG_TAG, "Failure while trying to update by index due to uninitialised adncache");
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            throw th;
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        throw th;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    throw th;
                                }
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } else {
                    try {
                        throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
                    } catch (Throwable th8) {
                        th = th8;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th9) {
                th = th9;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    public int getAdnCountHW() {
        if (this.mAdnCache == null) {
            Rlog.e(LOG_TAG, "mAdnCache is NULL when getAdnCountHW.");
            return 0;
        } else if (this.mPhone == null || (this.mPhone.getCurrentUiccAppType() != IccCardApplicationStatus.AppType.APPTYPE_USIM && this.mPhone.getCurrentUiccAppType() != IccCardApplicationStatus.AppType.APPTYPE_CSIM && this.mPhone.getCurrentUiccAppType() != IccCardApplicationStatus.AppType.APPTYPE_ISIM)) {
            return this.mAdnCache.getAdnCountHW();
        } else {
            return this.mAdnCache.getUsimAdnCountHW();
        }
    }

    public int getAnrCountHW() {
        if (this.mAdnCache != null) {
            return this.mAdnCache.getAnrCountHW();
        }
        Rlog.e(LOG_TAG, "mAdnCache is NULL when getAnrCountHW.");
        return 0;
    }

    public int getEmailCountHW() {
        if (this.mAdnCache != null) {
            return this.mAdnCache.getEmailCountHW();
        }
        Rlog.e(LOG_TAG, "mAdnCache is NULL when getEmailCountHW.");
        return 0;
    }

    public int getSpareAnrCountHW() {
        if (this.mAdnCache != null) {
            return this.mAdnCache.getSpareAnrCountHW();
        }
        Rlog.e(LOG_TAG, "mAdnCache is NULL when getSpareAnrCountHW.");
        return 0;
    }

    public int getSpareEmailCountHW() {
        if (this.mAdnCache != null) {
            return this.mAdnCache.getSpareEmailCountHW();
        }
        Rlog.e(LOG_TAG, "mAdnCache is NULL when getSpareEmailCountHW.");
        return 0;
    }

    public int[] getRecordsSizeHW() {
        if (!IccRecordsEx.getEmailAnrSupport()) {
            Rlog.e(LOG_TAG, "getRecordsSize return null as prop not open.");
            return null;
        } else if (this.mPhone != null && this.mPhone.mIccRecords.get() != null && !((IccRecords) this.mPhone.mIccRecords.get()).isGetPBRDone()) {
            Rlog.e(LOG_TAG, "getRecordsSize(): is not get PBR done, please wait!");
            return null;
        } else if (this.isGettingAdnRecordsSize) {
            Rlog.e(LOG_TAG, "getRecordsSize(): is running already, please wait!");
            return null;
        } else {
            synchronized (this.mLock2) {
                if (getAdnCountHW() == 0) {
                    Rlog.e(LOG_TAG, "getRecordsSize: adn is not ever read!");
                    this.isGettingAdnRecordsSize = true;
                    try {
                        getAdnRecordsInEf(28474);
                    } catch (SecurityException e) {
                        this.isGettingAdnRecordsSize = false;
                        log("getRecordsSize():catch exception.");
                        return null;
                    }
                }
                log("getRecordsSize: adn all loaded! GettingAdnRecordsSize = " + this.isGettingAdnRecordsSize);
                if (this.isGettingAdnRecordsSize) {
                    Intent intent = new Intent(ACTION_READ_SIM_CONTACTS_DONE);
                    intent.setPackage("com.huawei.contacts");
                    intent.addFlags(268435456);
                    this.mPhone.getContext().sendBroadcast(intent);
                }
                this.isGettingAdnRecordsSize = false;
                log("read sim card done");
                checkThread();
                if (this.mAdnCache != null) {
                    return this.mAdnCache.getRecordsSizeHW();
                }
                Rlog.e(LOG_TAG, "mAdnCache is NULL when getRecordsSizeHW.");
                return null;
            }
        }
    }

    public int updateEfFor3gCardType(int efid) {
        if (this.mPhone == null || this.mPhone.mIccRecords.get() == null) {
            log("Translate EF_ADN to EF_PBR");
            return 20272;
        } else if (((IccRecords) this.mPhone.mIccRecords.get()).has3Gphonebook()) {
            log("Translate EF_ADN to EF_PBR");
            return 20272;
        } else {
            log("updateEfForIccType use EF_ADN");
            return efid;
        }
    }

    public int[] getAdnRecordsSize(int efid) {
        IccRecords r;
        synchronized (this.mLock2) {
            int efid2 = updateEfForIccTypeHw(efid);
            if (20272 != efid2) {
                return HwIccPhoneBookInterfaceManager.super.getAdnRecordsSize(efid2);
            }
            int[] recordSize = new int[3];
            IccFileHandler fh = this.mPhone.getIccFileHandler();
            if (fh != null) {
                if (!(this.mPhone.mIccRecords == null || (r = (IccRecords) this.mPhone.mIccRecords.get()) == null)) {
                    this.mAdnCache = r.getAdnCache();
                }
                if (this.mAdnCache != null) {
                    this.mUsimPhoneBookManager = this.mAdnCache.getUsimPhoneBookManager();
                    if (this.mUsimPhoneBookManager != null) {
                        this.mUsimPhoneBookManager.setIccFileHandler(fh);
                        recordSize = this.mUsimPhoneBookManager.getAdnRecordsSizeFromEF();
                    }
                    if (recordSize == null) {
                        loge("null == recordSize");
                        return new int[3];
                    }
                } else {
                    loge("Failure while trying to load from SIM due to uninitialised adncache");
                    return null;
                }
            }
            return Arrays.copyOf(recordSize, 3);
        }
    }

    public int getSpareExt1CountHW() {
        if (this.mAdnCache != null) {
            return this.mAdnCache.getSpareExt1CountHW();
        }
        Rlog.e(LOG_TAG, "mAdnCache is NULL when getSpareExt1CountHW.");
        return -1;
    }

    private static void log(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }
}
