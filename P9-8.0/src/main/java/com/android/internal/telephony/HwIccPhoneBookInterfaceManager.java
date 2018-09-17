package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
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

    public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) {
        synchronized (this.mLock2) {
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
                efid = updateEfForIccTypeHw(efid);
                Rlog.i(LOG_TAG, "updateAdnRecordsWithContentValuesInEfBySearch: efid=" + efid + ", pin2=" + "xxxx");
                synchronized (this.mLock) {
                    checkThread();
                    this.mSuccess = false;
                    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
                    Message response = this.mBaseHandler.obtainMessage(3, atomicBoolean);
                    AdnRecord oldAdn = new AdnRecord(oldTag, oldPhoneNumber, oldEmailArray, oldAnrArray);
                    AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmailArray, newAnrArray);
                    if (this.mAdnCache != null) {
                        this.mAdnCache.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
                        waitForResult(atomicBoolean);
                    } else {
                        Rlog.e(LOG_TAG, "Failure while trying to update by search due to uninitialised adncache");
                    }
                }
                return this.mSuccess;
            }
        }
    }

    public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        synchronized (this.mLock2) {
            if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
                throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
            }
            Rlog.i(LOG_TAG, "updateUsimAdnRecordsInEfByIndexHW: efid=" + efid + " sEf_id=" + sEf_id + " Index=" + index + " pin2=" + "xxxx");
            synchronized (this.mLock) {
                checkThread();
                this.mSuccess = false;
                AtomicBoolean status = new AtomicBoolean(false);
                Message response = this.mBaseHandler.obtainMessage(3, status);
                AdnRecord newAdn = new AdnRecord(newTag, newPhoneNumber, newEmails, newAnrNumbers);
                efid = updateEfForIccTypeHw(efid);
                if (this.mAdnCache != null) {
                    this.mAdnCache.updateUsimAdnByIndexHW(efid, newAdn, sEf_id, index, pin2, response);
                    waitForResult(status);
                } else {
                    Rlog.e(LOG_TAG, "Failure while trying to update by index due to uninitialised adncache");
                }
            }
        }
        return this.mSuccess;
    }

    public int getAdnCountHW() {
        if (this.mAdnCache == null) {
            Rlog.e(LOG_TAG, "mAdnCache is NULL when getAdnCountHW.");
            return 0;
        } else if (this.mPhone == null || (this.mPhone.getCurrentUiccAppType() != AppType.APPTYPE_USIM && this.mPhone.getCurrentUiccAppType() != AppType.APPTYPE_CSIM && this.mPhone.getCurrentUiccAppType() != AppType.APPTYPE_ISIM)) {
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
        } else if (this.mPhone == null || this.mPhone.mIccRecords.get() == null || (((IccRecords) this.mPhone.mIccRecords.get()).isGetPBRDone() ^ 1) == 0) {
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
            efid = updateEfForIccTypeHw(efid);
            int[] iArr;
            if (20272 == efid) {
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
                            iArr = new int[3];
                            return iArr;
                        }
                    }
                    loge("Failure while trying to load from SIM due to uninitialised adncache");
                    return null;
                }
                iArr = Arrays.copyOf(this.mRecordSize, 3);
                return iArr;
            }
            iArr = super.getAdnRecordsSize(efid);
            return iArr;
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
