package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.uicc.AbstractIccRecords;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.google.android.mms.pdu.PduHeaders;
import java.util.ArrayList;

public class UsimPhoneBookManager extends AbstractUsimPhoneBookManager implements IccConstants {
    private static final boolean DBG = true;
    private static final int EVENT_EMAIL_LOAD_DONE = 4;
    private static final int EVENT_IAP_LOAD_DONE = 3;
    private static final int EVENT_PBR_LOAD_DONE = 1;
    private static final int EVENT_USIM_ADN_LOAD_DONE = 2;
    private static final byte INVALID_BYTE = (byte) -1;
    private static final int INVALID_SFI = -1;
    private static final String LOG_TAG = "UsimPhoneBookManager";
    private static final int USIM_EFAAS_TAG = 199;
    private static final int USIM_EFADN_TAG = 192;
    private static final int USIM_EFANR_TAG = 196;
    private static final int USIM_EFCCP1_TAG = 203;
    private static final int USIM_EFEMAIL_TAG = 202;
    private static final int USIM_EFEXT1_TAG = 194;
    private static final int USIM_EFGRP_TAG = 198;
    private static final int USIM_EFGSD_TAG = 200;
    private static final int USIM_EFIAP_TAG = 193;
    private static final int USIM_EFPBC_TAG = 197;
    private static final int USIM_EFSNE_TAG = 195;
    private static final int USIM_EFUID_TAG = 201;
    private static final int USIM_TYPE1_TAG = 168;
    private static final int USIM_TYPE2_TAG = 169;
    private static final int USIM_TYPE3_TAG = 170;
    private AdnRecordCache mAdnCache;
    private ArrayList<byte[]> mEmailFileRecord;
    private SparseArray<ArrayList<String>> mEmailsForAdnRec;
    private IccFileHandler mFh;
    private ArrayList<byte[]> mIapFileRecord;
    private Boolean mIsPbrPresent;
    private Object mLock;
    private ArrayList<PbrRecord> mPbrRecords;
    private ArrayList<AdnRecord> mPhoneBookRecords;
    private boolean mRefreshCache;
    private SparseIntArray mSfiEfidTable;

    protected class File {
        private final int mEfid;
        private final int mIndex;
        private final int mParentTag;
        private final int mSfi;

        File(int parentTag, int efid, int sfi, int index) {
            this.mParentTag = parentTag;
            this.mEfid = efid;
            this.mSfi = sfi;
            this.mIndex = index;
        }

        public int getParentTag() {
            return this.mParentTag;
        }

        public int getEfid() {
            return this.mEfid;
        }

        public int getSfi() {
            return this.mSfi;
        }

        public int getIndex() {
            return this.mIndex;
        }
    }

    protected class PbrRecord {
        public SparseArray<File> mFileIds;
        private int mMasterFileRecordNum;

        PbrRecord(byte[] record) {
            this.mFileIds = new SparseArray();
            UsimPhoneBookManager.this.log("PBR rec: " + IccUtils.bytesToHexString(record));
            parseTag(new SimTlv(record, 0, record.length));
        }

        void parseTag(SimTlv tlv) {
            do {
                int tag = tlv.getTag();
                switch (tag) {
                    case UsimPhoneBookManager.USIM_TYPE1_TAG /*168*/:
                    case UsimPhoneBookManager.USIM_TYPE2_TAG /*169*/:
                    case UsimPhoneBookManager.USIM_TYPE3_TAG /*170*/:
                        byte[] data = tlv.getData();
                        parseEfAndSFI(new SimTlv(data, 0, data.length), tag);
                        break;
                }
            } while (tlv.nextObject());
        }

        void parseEfAndSFI(SimTlv tlv, int parentTag) {
            int tagNumberWithinParentTag = 0;
            do {
                int tag = tlv.getTag();
                switch (tag) {
                    case UsimPhoneBookManager.USIM_EFADN_TAG /*192*/:
                    case UsimPhoneBookManager.USIM_EFIAP_TAG /*193*/:
                    case UsimPhoneBookManager.USIM_EFEXT1_TAG /*194*/:
                    case UsimPhoneBookManager.USIM_EFSNE_TAG /*195*/:
                    case UsimPhoneBookManager.USIM_EFANR_TAG /*196*/:
                    case UsimPhoneBookManager.USIM_EFPBC_TAG /*197*/:
                    case UsimPhoneBookManager.USIM_EFGRP_TAG /*198*/:
                    case UsimPhoneBookManager.USIM_EFAAS_TAG /*199*/:
                    case UsimPhoneBookManager.USIM_EFGSD_TAG /*200*/:
                    case UsimPhoneBookManager.USIM_EFUID_TAG /*201*/:
                    case UsimPhoneBookManager.USIM_EFEMAIL_TAG /*202*/:
                    case UsimPhoneBookManager.USIM_EFCCP1_TAG /*203*/:
                        int sfi = UsimPhoneBookManager.INVALID_SFI;
                        byte[] data = tlv.getData();
                        if (data.length >= UsimPhoneBookManager.EVENT_USIM_ADN_LOAD_DONE && data.length <= UsimPhoneBookManager.EVENT_IAP_LOAD_DONE) {
                            if (data.length == UsimPhoneBookManager.EVENT_IAP_LOAD_DONE) {
                                sfi = data[UsimPhoneBookManager.EVENT_USIM_ADN_LOAD_DONE] & PduHeaders.STORE_STATUS_ERROR_END;
                            }
                            this.mFileIds.put(tag, new File(parentTag, ((data[0] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (data[UsimPhoneBookManager.EVENT_PBR_LOAD_DONE] & PduHeaders.STORE_STATUS_ERROR_END), sfi, tagNumberWithinParentTag));
                            break;
                        }
                        UsimPhoneBookManager.this.log("Invalid TLV length: " + data.length);
                        break;
                        break;
                }
                tagNumberWithinParentTag += UsimPhoneBookManager.EVENT_PBR_LOAD_DONE;
            } while (tlv.nextObject());
        }
    }

    public UsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        this.mLock = new Object();
        this.mRefreshCache = false;
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList();
        this.mPbrRecords = null;
        this.mIsPbrPresent = Boolean.valueOf(DBG);
        this.mAdnCache = cache;
        this.mEmailsForAdnRec = new SparseArray();
        this.mSfiEfidTable = new SparseIntArray();
    }

    public void reset() {
        this.mPhoneBookRecords.clear();
        this.mIapFileRecord = null;
        this.mEmailFileRecord = null;
        this.mPbrRecords = null;
        this.mIsPbrPresent = Boolean.valueOf(DBG);
        this.mRefreshCache = false;
        this.mEmailsForAdnRec.clear();
        this.mSfiEfidTable.clear();
    }

    public ArrayList<AdnRecord> loadEfFilesFromUsim() {
        synchronized (this.mLock) {
            if (!this.mPhoneBookRecords.isEmpty()) {
                if (this.mRefreshCache) {
                    this.mRefreshCache = false;
                    refreshCache();
                }
                ArrayList<AdnRecord> arrayList = this.mPhoneBookRecords;
                return arrayList;
            } else if (this.mIsPbrPresent.booleanValue()) {
                if (this.mPbrRecords == null) {
                    readPbrFileAndWait();
                }
                if (this.mPbrRecords == null) {
                    return null;
                }
                int numRecs = this.mPbrRecords.size();
                log("loadEfFilesFromUsim: Loading adn and emails");
                for (int i = 0; i < numRecs; i += EVENT_PBR_LOAD_DONE) {
                    readAdnFileAndWait(i);
                    if (AbstractIccRecords.getEmailAnrSupport()) {
                        readEmailFileAndWait(i);
                    }
                }
                updatePhoneAdnRecord();
                return this.mPhoneBookRecords;
            } else {
                return null;
            }
        }
    }

    private void refreshCache() {
        if (this.mPbrRecords != null) {
            this.mPhoneBookRecords.clear();
            int numRecs = this.mPbrRecords.size();
            for (int i = 0; i < numRecs; i += EVENT_PBR_LOAD_DONE) {
                readAdnFileAndWait(i);
            }
        }
    }

    public void invalidateCache() {
        this.mRefreshCache = DBG;
    }

    private void readPbrFileAndWait() {
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_PBR, obtainMessage(EVENT_PBR_LOAD_DONE));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
        }
    }

    private void readEmailFileAndWait(int recId) {
        SparseArray<File> files = ((PbrRecord) this.mPbrRecords.get(recId)).mFileIds;
        if (files != null) {
            File email = (File) files.get(USIM_EFEMAIL_TAG);
            if (email != null) {
                if (email.getParentTag() == USIM_TYPE2_TAG) {
                    if (files.get(USIM_EFIAP_TAG) == null) {
                        Rlog.e(LOG_TAG, "Can't locate EF_IAP in EF_PBR.");
                        return;
                    }
                    log("EF_IAP exists. Loading EF_IAP to retrieve the index.");
                    readIapFileAndWait(((File) files.get(USIM_EFIAP_TAG)).getEfid());
                    if (this.mIapFileRecord == null) {
                        Rlog.e(LOG_TAG, "Error: IAP file is empty");
                        return;
                    }
                    log("EF_EMAIL order in PBR record: " + email.getIndex());
                }
                int emailEfid = email.getEfid();
                log("EF_EMAIL exists in PBR. efid = 0x" + Integer.toHexString(emailEfid).toUpperCase());
                for (int i = 0; i < recId; i += EVENT_PBR_LOAD_DONE) {
                    if (this.mPbrRecords.get(i) != null) {
                        SparseArray<File> previousFileIds = ((PbrRecord) this.mPbrRecords.get(i)).mFileIds;
                        if (previousFileIds != null) {
                            File id = (File) previousFileIds.get(USIM_EFEMAIL_TAG);
                            if (id != null && id.getEfid() == emailEfid) {
                                log("Skipped this EF_EMAIL which was loaded earlier");
                                return;
                            }
                        }
                        continue;
                    }
                }
                this.mFh.loadEFLinearFixedAll(emailEfid, obtainMessage(EVENT_EMAIL_LOAD_DONE));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                }
                if (this.mEmailFileRecord == null) {
                    Rlog.e(LOG_TAG, "Error: Email file is empty");
                } else if (email.getParentTag() != USIM_TYPE2_TAG || this.mIapFileRecord == null) {
                    buildType1EmailList(recId);
                } else {
                    buildType2EmailList(recId);
                }
            }
        }
    }

    private void buildType1EmailList(int recId) {
        if (this.mPbrRecords.get(recId) != null) {
            int numRecs = ((PbrRecord) this.mPbrRecords.get(recId)).mMasterFileRecordNum;
            log("Building type 1 email list. recId = " + recId + ", numRecs = " + numRecs);
            int i = 0;
            while (i < numRecs) {
                try {
                    byte[] emailRec = (byte[]) this.mEmailFileRecord.get(i);
                    int sfi = emailRec[emailRec.length - 2];
                    int adnRecId = emailRec[emailRec.length + INVALID_SFI];
                    String email = readEmailRecord(i);
                    if (!(email == null || email.equals(""))) {
                        int adnEfid;
                        if (sfi == INVALID_SFI || this.mSfiEfidTable.get(sfi) == 0) {
                            File file = (File) ((PbrRecord) this.mPbrRecords.get(recId)).mFileIds.get(USIM_EFADN_TAG);
                            if (file != null) {
                                adnEfid = file.getEfid();
                            }
                        } else {
                            adnEfid = this.mSfiEfidTable.get(sfi);
                        }
                        int index = ((CallFailCause.ERROR_UNSPECIFIED & adnEfid) << 8) | ((adnRecId + INVALID_SFI) & PduHeaders.STORE_STATUS_ERROR_END);
                        ArrayList<String> emailList = (ArrayList) this.mEmailsForAdnRec.get(index);
                        if (emailList == null) {
                            emailList = new ArrayList();
                        }
                        log("Adding email #" + i + " list to index 0x" + Integer.toHexString(index).toUpperCase());
                        emailList.add(email);
                        this.mEmailsForAdnRec.put(index, emailList);
                    }
                    i += EVENT_PBR_LOAD_DONE;
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "Error: Improper ICC card: No email record for ADN, continuing");
                }
            }
        }
    }

    private boolean buildType2EmailList(int recId) {
        if (this.mPbrRecords.get(recId) == null) {
            return false;
        }
        int numRecs = ((PbrRecord) this.mPbrRecords.get(recId)).mMasterFileRecordNum;
        log("Building type 2 email list. recId = " + recId + ", numRecs = " + numRecs);
        File adnFile = (File) ((PbrRecord) this.mPbrRecords.get(recId)).mFileIds.get(USIM_EFADN_TAG);
        if (adnFile == null) {
            Rlog.e(LOG_TAG, "Error: Improper ICC card: EF_ADN does not exist in PBR files");
            return false;
        }
        int adnEfid = adnFile.getEfid();
        for (int i = 0; i < numRecs; i += EVENT_PBR_LOAD_DONE) {
            try {
                String email = readEmailRecord(((byte[]) this.mIapFileRecord.get(i))[((File) ((PbrRecord) this.mPbrRecords.get(recId)).mFileIds.get(USIM_EFEMAIL_TAG)).getIndex()] + INVALID_SFI);
                if (!(email == null || email.equals(""))) {
                    int index = ((CallFailCause.ERROR_UNSPECIFIED & adnEfid) << 8) | (i & PduHeaders.STORE_STATUS_ERROR_END);
                    ArrayList<String> emailList = (ArrayList) this.mEmailsForAdnRec.get(index);
                    if (emailList == null) {
                        emailList = new ArrayList();
                    }
                    emailList.add(email);
                    log("Adding email list to index 0x" + Integer.toHexString(index).toUpperCase());
                    this.mEmailsForAdnRec.put(index, emailList);
                }
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "Error: Improper ICC card: Corrupted EF_IAP");
            }
        }
        return DBG;
    }

    private void readIapFileAndWait(int efid) {
        this.mFh.loadEFLinearFixedAll(efid, obtainMessage(EVENT_IAP_LOAD_DONE));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readIapFileAndWait");
        }
    }

    private void updatePhoneAdnRecord() {
        int numAdnRecs = this.mPhoneBookRecords.size();
        for (int i = 0; i < numAdnRecs; i += EVENT_PBR_LOAD_DONE) {
            AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i);
            try {
                ArrayList<String> emailList = (ArrayList) this.mEmailsForAdnRec.get(((CallFailCause.ERROR_UNSPECIFIED & rec.getEfid()) << 8) | ((rec.getRecId() + INVALID_SFI) & PduHeaders.STORE_STATUS_ERROR_END));
                if (emailList != null) {
                    String[] emails = new String[emailList.size()];
                    System.arraycopy(emailList.toArray(), 0, emails, 0, emailList.size());
                    rec.setEmails(emails);
                    log("Adding email list to ADN (0x" + Integer.toHexString(((AdnRecord) this.mPhoneBookRecords.get(i)).getEfid()).toUpperCase() + ") record #" + ((AdnRecord) this.mPhoneBookRecords.get(i)).getRecId());
                    this.mPhoneBookRecords.set(i, rec);
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }

    private String readEmailRecord(int recId) {
        try {
            byte[] emailRec = (byte[]) this.mEmailFileRecord.get(recId);
            return IccUtils.adnStringFieldToString(emailRec, 0, emailRec.length - 2);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void readAdnFileAndWait(int recId) {
        SparseArray<File> files = ((PbrRecord) this.mPbrRecords.get(recId)).mFileIds;
        if (files != null && files.size() != 0) {
            int extEf = 0;
            if (files.get(USIM_EFEXT1_TAG) != null) {
                extEf = ((File) files.get(USIM_EFEXT1_TAG)).getEfid();
            }
            if (files.get(USIM_EFADN_TAG) != null) {
                int previousSize = this.mPhoneBookRecords.size();
                this.mAdnCache.requestLoadAllAdnLike(((File) files.get(USIM_EFADN_TAG)).getEfid(), extEf, obtainMessage(EVENT_USIM_ADN_LOAD_DONE));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
                }
                ((PbrRecord) this.mPbrRecords.get(recId)).mMasterFileRecordNum = this.mPhoneBookRecords.size() - previousSize;
            }
        }
    }

    private void createPbrFile(ArrayList<byte[]> records) {
        if (records == null) {
            this.mPbrRecords = null;
            this.mIsPbrPresent = Boolean.valueOf(false);
            return;
        }
        this.mPbrRecords = new ArrayList();
        for (int i = 0; i < records.size(); i += EVENT_PBR_LOAD_DONE) {
            if (((byte[]) records.get(i))[0] != INVALID_BYTE) {
                this.mPbrRecords.add(new PbrRecord((byte[]) records.get(i)));
            }
        }
        for (PbrRecord record : this.mPbrRecords) {
            File file = (File) record.mFileIds.get(USIM_EFADN_TAG);
            if (file != null) {
                int sfi = file.getSfi();
                if (sfi != INVALID_SFI) {
                    this.mSfiEfidTable.put(sfi, ((File) record.mFileIds.get(USIM_EFADN_TAG)).getEfid());
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        Object obj;
        switch (msg.what) {
            case EVENT_PBR_LOAD_DONE /*1*/:
                ar = msg.obj;
                if (ar.exception == null) {
                    createPbrFile((ArrayList) ar.result);
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case EVENT_USIM_ADN_LOAD_DONE /*2*/:
                log("Loading USIM ADN records done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mPhoneBookRecords.addAll((ArrayList) ar.result);
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case EVENT_IAP_LOAD_DONE /*3*/:
                log("Loading USIM IAP records done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mIapFileRecord = (ArrayList) ar.result;
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case EVENT_EMAIL_LOAD_DONE /*4*/:
                log("Loading USIM Email records done");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mEmailFileRecord = (ArrayList) ar.result;
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            default:
                return;
        }
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }
}
