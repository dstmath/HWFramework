package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwIccPhoneBookInterfaceManager extends IccPhoneBookInterfaceManager {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HwIccPhoneBookInterfaceManager";
    private UsimPhoneBookManager mUsimPhoneBookManager;

    public HwIccPhoneBookInterfaceManager(Phone phone) {
        super(phone);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00ed, code lost:
        return r1.mSuccess;
     */
    public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) {
        StringBuilder sb;
        int efid2;
        ContentValues contentValues = values;
        synchronized (this.mLock2) {
            try {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
                    throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
                } else if (contentValues == null) {
                    Rlog.e(LOG_TAG, "input values is null.");
                    return false;
                } else {
                    String oldTag = contentValues.getAsString("tag");
                    String newTag = contentValues.getAsString("newTag");
                    String oldPhoneNumber = contentValues.getAsString("number");
                    String newPhoneNumber = contentValues.getAsString("newNumber");
                    String oldEmail = contentValues.getAsString("emails");
                    String newEmail = contentValues.getAsString("newEmails");
                    String oldAnr = contentValues.getAsString("anrs");
                    String newAnr = contentValues.getAsString("newAnrs");
                    String[] oldEmailArray = TextUtils.isEmpty(oldEmail) ? null : new String[]{oldEmail};
                    String[] newEmailArray = TextUtils.isEmpty(newEmail) ? null : new String[]{newEmail};
                    String[] oldAnrArray = TextUtils.isEmpty(oldAnr) ? null : new String[]{oldAnr};
                    String[] newAnrArray = TextUtils.isEmpty(newAnr) ? null : new String[]{newAnr};
                    int efid3 = updateEfForIccTypeHw(efid);
                    try {
                        sb = new StringBuilder();
                        String str = oldEmail;
                        sb.append("updateAdnRecordsWithContentValuesInEfBySearch: efid=");
                        efid2 = efid3;
                    } catch (Throwable th) {
                        th = th;
                        int i = efid3;
                        throw th;
                    }
                    try {
                        sb.append(efid2);
                        String str2 = newEmail;
                        sb.append(", pin2=xxxx");
                        Rlog.i(LOG_TAG, sb.toString());
                        synchronized (this.mLock) {
                            try {
                                checkThread();
                                this.mSuccess = false;
                                AtomicBoolean status = new AtomicBoolean(false);
                                String str3 = oldAnr;
                                try {
                                    Message response = this.mBaseHandler.obtainMessage(3, status);
                                    AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber, oldEmailArray, oldAnrArray);
                                    AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmailArray, newAnrArray);
                                    if (this.mAdnCache != null) {
                                        this.mAdnCache.updateAdnBySearch(efid2, oldAdn, newAdn, pin2, response);
                                        waitForResult(status);
                                    } else {
                                        Rlog.e(LOG_TAG, "Failure while trying to update by search due to uninitialised adncache");
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                String str4 = oldAnr;
                                throw th;
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                int i2 = efid;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0087, code lost:
        return r1.mSuccess;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0088, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0089, code lost:
        r4 = r7;
     */
    public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        int i;
        AtomicBoolean status;
        Message response;
        synchronized (this.mLock2) {
            try {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") == 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("updateUsimAdnRecordsInEfByIndexHW: efid=");
                    try {
                        sb.append(efid);
                        sb.append(" sEf_id=");
                        int i2 = sEf_id;
                        try {
                            sb.append(i2);
                            sb.append(" Index=");
                            i = index;
                        } catch (Throwable th) {
                            th = th;
                            String str = newTag;
                            String str2 = newPhoneNumber;
                            int i3 = index;
                            throw th;
                        }
                        try {
                            sb.append(i);
                            sb.append(" pin2=xxxx");
                            Rlog.i(LOG_TAG, sb.toString());
                            synchronized (this.mLock) {
                                try {
                                    checkThread();
                                    this.mSuccess = false;
                                    status = new AtomicBoolean(false);
                                    response = this.mBaseHandler.obtainMessage(3, status);
                                } catch (Throwable th2) {
                                    th = th2;
                                    String str3 = newTag;
                                    String str4 = newPhoneNumber;
                                    throw th;
                                }
                                try {
                                    AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmails, newAnrNumbers);
                                    int efid2 = updateEfForIccTypeHw(efid);
                                    try {
                                        if (this.mAdnCache != null) {
                                            this.mAdnCache.updateUsimAdnByIndexHW(efid2, newAdn, i2, i, pin2, response);
                                            waitForResult(status);
                                        } else {
                                            Rlog.e(LOG_TAG, "Failure while trying to update by index due to uninitialised adncache");
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        int i4 = efid2;
                                        throw th;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            String str5 = newTag;
                            String str6 = newPhoneNumber;
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        String str7 = newTag;
                        String str8 = newPhoneNumber;
                        int i5 = sEf_id;
                        int i32 = index;
                        throw th;
                    }
                } else {
                    int i6 = efid;
                    String str9 = newTag;
                    String str10 = newPhoneNumber;
                    int i7 = sEf_id;
                    int i8 = index;
                    throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
                }
            } catch (Throwable th7) {
                th = th7;
                int i9 = efid;
                String str72 = newTag;
                String str82 = newPhoneNumber;
                int i52 = sEf_id;
                int i322 = index;
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
        if (!IccRecords.getEmailAnrSupport()) {
            Rlog.e(LOG_TAG, "getRecordsSize return null as prop not open.");
            return null;
        } else if (this.mPhone == null || this.mPhone.mIccRecords.get() == null || ((IccRecords) this.mPhone.mIccRecords.get()).isGetPBRDone()) {
            synchronized (this.mLock2) {
                if (getAdnCountHW() == 0) {
                    Rlog.e(LOG_TAG, "getRecordsSize: adn is not ever read!");
                    getAdnRecordsInEf(28474);
                }
                Rlog.d(LOG_TAG, "getRecordsSize: adn all loaded!");
                synchronized (this.mLock) {
                    checkThread();
                    if (this.mAdnCache != null) {
                        int[] recordsSizeHW = this.mAdnCache.getRecordsSizeHW();
                        return recordsSizeHW;
                    }
                    Rlog.e(LOG_TAG, "mAdnCache is NULL when getRecordsSizeHW.");
                    return null;
                }
            }
        } else {
            Rlog.e(LOG_TAG, "getRecordsSize(): is not get PBR done, please wait!");
            return null;
        }
    }

    public int updateEfFor3gCardType(int efid) {
        if (this.mPhone == null || this.mPhone.mIccRecords.get() == null) {
            Rlog.d(LOG_TAG, "Translate EF_ADN to EF_PBR");
            return 20272;
        } else if (((IccRecords) this.mPhone.mIccRecords.get()).has3Gphonebook()) {
            Rlog.d(LOG_TAG, "Translate EF_ADN to EF_PBR");
            return 20272;
        } else {
            Rlog.d(LOG_TAG, "updateEfForIccType use EF_ADN");
            return efid;
        }
    }

    public int[] getAdnRecordsSize(int efid) {
        synchronized (this.mLock2) {
            int efid2 = updateEfForIccTypeHw(efid);
            if (20272 == efid2) {
                this.mRecordSize = new int[3];
                IccFileHandler fh = this.mPhone.getIccFileHandler();
                if (fh != null) {
                    if (this.mPhone.mIccRecords != null) {
                        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
                        if (r != null) {
                            this.mAdnCache = r.getAdnCache();
                        }
                    }
                    if (this.mAdnCache != null) {
                        this.mUsimPhoneBookManager = this.mAdnCache.getUsimPhoneBookManager();
                        if (this.mUsimPhoneBookManager != null) {
                            this.mUsimPhoneBookManager.setIccFileHandler(fh);
                            this.mRecordSize = this.mUsimPhoneBookManager.getAdnRecordsSizeFromEF();
                        }
                        if (this.mRecordSize == null) {
                            loge("null == mRecordSize");
                            int[] iArr = new int[3];
                            return iArr;
                        }
                    } else {
                        loge("Failure while trying to load from SIM due to uninitialised adncache");
                        return null;
                    }
                }
                int[] copyOf = Arrays.copyOf(this.mRecordSize, 3);
                return copyOf;
            }
            int[] adnRecordsSize = HwIccPhoneBookInterfaceManager.super.getAdnRecordsSize(efid2);
            return adnRecordsSize;
        }
    }

    public int getSpareExt1CountHW() {
        if (this.mAdnCache != null) {
            return this.mAdnCache.getSpareExt1CountHW();
        }
        Rlog.e(LOG_TAG, "mAdnCache is NULL when getSpareExt1CountHW.");
        return -1;
    }
}
