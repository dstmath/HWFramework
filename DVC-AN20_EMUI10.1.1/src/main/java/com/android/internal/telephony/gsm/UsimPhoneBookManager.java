package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccConstants;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;
import java.util.Iterator;

public class UsimPhoneBookManager extends AbstractUsimPhoneBookManager implements IccConstants {
    private static final boolean DBG = true;
    private static final int EVENT_EMAIL_LOAD_DONE = 4;
    private static final int EVENT_IAP_LOAD_DONE = 3;
    private static final int EVENT_PBR_LOAD_DONE = 1;
    private static final int EVENT_USIM_ADN_LOAD_DONE = 2;
    private static final byte INVALID_BYTE = -1;
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
    private Object mLock = new Object();
    private ArrayList<PbrRecord> mPbrRecords;
    private ArrayList<AdnRecord> mPhoneBookRecords;
    private boolean mRefreshCache = false;
    private SparseIntArray mSfiEfidTable;

    /* access modifiers changed from: protected */
    public class File {
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

    public UsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList<>();
        this.mPbrRecords = null;
        this.mIsPbrPresent = true;
        this.mAdnCache = cache;
        this.mEmailsForAdnRec = new SparseArray<>();
        this.mSfiEfidTable = new SparseIntArray();
    }

    public void reset() {
        this.mPhoneBookRecords.clear();
        this.mIapFileRecord = null;
        this.mEmailFileRecord = null;
        this.mPbrRecords = null;
        this.mIsPbrPresent = true;
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
                return this.mPhoneBookRecords;
            } else if (!this.mIsPbrPresent.booleanValue()) {
                return null;
            } else {
                if (this.mPbrRecords == null) {
                    readPbrFileAndWait();
                }
                if (this.mPbrRecords == null) {
                    return null;
                }
                int numRecs = this.mPbrRecords.size();
                log("loadEfFilesFromUsim: Loading adn and emails");
                for (int i = 0; i < numRecs; i++) {
                    readAdnFileAndWait(i);
                    if (IccRecords.getEmailAnrSupport()) {
                        readEmailFileAndWait(i);
                    }
                }
                updatePhoneAdnRecord();
                return this.mPhoneBookRecords;
            }
        }
    }

    private void refreshCache() {
        if (this.mPbrRecords != null) {
            this.mPhoneBookRecords.clear();
            int numRecs = this.mPbrRecords.size();
            for (int i = 0; i < numRecs; i++) {
                readAdnFileAndWait(i);
            }
        }
    }

    public void invalidateCache() {
        this.mRefreshCache = true;
    }

    private void readPbrFileAndWait() {
        this.mFh.loadEFLinearFixedAll(IccConstants.EF_PBR, obtainMessage(1));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
        }
    }

    private void readEmailFileAndWait(int recId) {
        File email;
        SparseArray<File> previousFileIds;
        File id;
        SparseArray<File> files = this.mPbrRecords.get(recId).mFileIds;
        if (files != null && (email = files.get(USIM_EFEMAIL_TAG)) != null) {
            if (email.getParentTag() == 169) {
                if (files.get(193) == null) {
                    Rlog.e(LOG_TAG, "Can't locate EF_IAP in EF_PBR.");
                    return;
                }
                log("EF_IAP exists. Loading EF_IAP to retrieve the index.");
                readIapFileAndWait(files.get(193).getEfid());
                if (this.mIapFileRecord == null) {
                    Rlog.e(LOG_TAG, "Error: IAP file is empty");
                    return;
                }
                log("EF_EMAIL order in PBR record: " + email.getIndex());
            }
            int emailEfid = email.getEfid();
            log("EF_EMAIL exists in PBR. efid = 0x" + Integer.toHexString(emailEfid).toUpperCase());
            for (int i = 0; i < recId; i++) {
                if (this.mPbrRecords.get(i) != null && (previousFileIds = this.mPbrRecords.get(i).mFileIds) != null && (id = previousFileIds.get(USIM_EFEMAIL_TAG)) != null && id.getEfid() == emailEfid) {
                    log("Skipped this EF_EMAIL which was loaded earlier");
                    return;
                }
            }
            this.mFh.loadEFLinearFixedAll(emailEfid, obtainMessage(4));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
            }
            if (this.mEmailFileRecord == null) {
                Rlog.e(LOG_TAG, "Error: Email file is empty");
            } else if (email.getParentTag() != 169 || this.mIapFileRecord == null) {
                buildType1EmailList(recId);
            } else {
                buildType2EmailList(recId);
            }
        }
    }

    private void buildType1EmailList(int recId) {
        int adnEfid;
        if (this.mPbrRecords.get(recId) != null) {
            int numRecs = this.mPbrRecords.get(recId).mMasterFileRecordNum;
            log("Building type 1 email list. recId = " + recId + ", numRecs = " + numRecs);
            for (int i = 0; i < numRecs; i++) {
                try {
                    byte[] emailRec = this.mEmailFileRecord.get(i);
                    byte b = emailRec[emailRec.length - 2];
                    byte b2 = emailRec[emailRec.length - 1];
                    String email = readEmailRecord(i);
                    if (email != null && !email.equals(PhoneConfigurationManager.SSSS)) {
                        if (b == -1 || this.mSfiEfidTable.get(b) == 0) {
                            File file = (File) this.mPbrRecords.get(recId).mFileIds.get(192);
                            if (file != null) {
                                adnEfid = file.getEfid();
                            }
                        } else {
                            adnEfid = this.mSfiEfidTable.get(b);
                        }
                        int index = ((65535 & adnEfid) << 8) | ((b2 + INVALID_BYTE) & 255);
                        ArrayList<String> emailList = this.mEmailsForAdnRec.get(index);
                        if (emailList == null) {
                            emailList = new ArrayList<>();
                        }
                        log("Adding email #" + i + " list to index 0x" + Integer.toHexString(index).toUpperCase());
                        emailList.add(email);
                        this.mEmailsForAdnRec.put(index, emailList);
                    }
                } catch (IndexOutOfBoundsException e) {
                    Rlog.e(LOG_TAG, "Error: Improper ICC card: No email record for ADN, continuing");
                    return;
                }
            }
        }
    }

    private boolean buildType2EmailList(int recId) {
        if (this.mPbrRecords.get(recId) == null) {
            return false;
        }
        int numRecs = this.mPbrRecords.get(recId).mMasterFileRecordNum;
        log("Building type 2 email list. recId = " + recId + ", numRecs = " + numRecs);
        File adnFile = (File) this.mPbrRecords.get(recId).mFileIds.get(192);
        if (adnFile == null) {
            Rlog.e(LOG_TAG, "Error: Improper ICC card: EF_ADN does not exist in PBR files");
            return false;
        }
        int adnEfid = adnFile.getEfid();
        for (int i = 0; i < numRecs; i++) {
            try {
                String email = readEmailRecord(this.mIapFileRecord.get(i)[((File) this.mPbrRecords.get(recId).mFileIds.get(USIM_EFEMAIL_TAG)).getIndex()] + INVALID_BYTE);
                if (email != null && !email.equals(PhoneConfigurationManager.SSSS)) {
                    int index = ((65535 & adnEfid) << 8) | (i & 255);
                    ArrayList<String> emailList = this.mEmailsForAdnRec.get(index);
                    if (emailList == null) {
                        emailList = new ArrayList<>();
                    }
                    emailList.add(email);
                    log("Adding email list to index 0x" + Integer.toHexString(index).toUpperCase());
                    this.mEmailsForAdnRec.put(index, emailList);
                }
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "Error: Improper ICC card: Corrupted EF_IAP");
            }
        }
        return true;
    }

    private void readIapFileAndWait(int efid) {
        this.mFh.loadEFLinearFixedAll(efid, obtainMessage(3));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readIapFileAndWait");
        }
    }

    private void updatePhoneAdnRecord() {
        int numAdnRecs = this.mPhoneBookRecords.size();
        for (int i = 0; i < numAdnRecs; i++) {
            AdnRecord rec = this.mPhoneBookRecords.get(i);
            try {
                ArrayList<String> emailList = this.mEmailsForAdnRec.get(((65535 & rec.getEfid()) << 8) | ((rec.getRecId() - 1) & 255));
                if (emailList != null) {
                    String[] emails = new String[emailList.size()];
                    System.arraycopy(emailList.toArray(), 0, emails, 0, emailList.size());
                    rec.setEmails(emails);
                    log("Adding email list to ADN (0x" + Integer.toHexString(this.mPhoneBookRecords.get(i).getEfid()).toUpperCase() + ") record #" + this.mPhoneBookRecords.get(i).getRecId());
                    this.mPhoneBookRecords.set(i, rec);
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }

    private String readEmailRecord(int recId) {
        try {
            byte[] emailRec = this.mEmailFileRecord.get(recId);
            return IccUtils.adnStringFieldToString(emailRec, 0, emailRec.length - 2);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void readAdnFileAndWait(int recId) {
        SparseArray<File> files = this.mPbrRecords.get(recId).mFileIds;
        if (files != null && files.size() != 0) {
            int extEf = 0;
            if (files.get(194) != null) {
                extEf = files.get(194).getEfid();
            }
            if (files.get(192) != null) {
                int previousSize = this.mPhoneBookRecords.size();
                this.mAdnCache.requestLoadAllAdnLike(files.get(192).getEfid(), extEf, obtainMessage(2));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
                }
                this.mPbrRecords.get(recId).mMasterFileRecordNum = this.mPhoneBookRecords.size() - previousSize;
            }
        }
    }

    private void createPbrFile(ArrayList<byte[]> records) {
        int sfi;
        if (records == null) {
            this.mPbrRecords = null;
            this.mIsPbrPresent = false;
            return;
        }
        this.mPbrRecords = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i)[0] != -1) {
                this.mPbrRecords.add(new PbrRecord(records.get(i)));
            }
        }
        Iterator<PbrRecord> it = this.mPbrRecords.iterator();
        while (it.hasNext()) {
            PbrRecord record = it.next();
            File file = (File) record.mFileIds.get(192);
            if (!(file == null || (sfi = file.getSfi()) == -1)) {
                this.mSfiEfidTable.put(sfi, ((File) record.mFileIds.get(192)).getEfid());
            }
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            log("Loading PBR records done");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                createPbrFile((ArrayList) ar.result);
            }
            synchronized (this.mLock) {
                this.mLock.notify();
            }
        } else if (i == 2) {
            log("Loading USIM ADN records done");
            AsyncResult ar2 = (AsyncResult) msg.obj;
            if (ar2.exception == null) {
                this.mPhoneBookRecords.addAll((ArrayList) ar2.result);
            }
            synchronized (this.mLock) {
                this.mLock.notify();
            }
        } else if (i == 3) {
            log("Loading USIM IAP records done");
            AsyncResult ar3 = (AsyncResult) msg.obj;
            if (ar3.exception == null) {
                this.mIapFileRecord = (ArrayList) ar3.result;
            }
            synchronized (this.mLock) {
                this.mLock.notify();
            }
        } else if (i == 4) {
            log("Loading USIM Email records done");
            AsyncResult ar4 = (AsyncResult) msg.obj;
            if (ar4.exception == null) {
                this.mEmailFileRecord = (ArrayList) ar4.result;
            }
            synchronized (this.mLock) {
                this.mLock.notify();
            }
        }
    }

    /* access modifiers changed from: protected */
    public class PbrRecord {
        private SparseArray<File> mFileIds = new SparseArray<>();
        private int mMasterFileRecordNum;

        PbrRecord(byte[] record) {
            UsimPhoneBookManager.this.log("PBR rec: " + IccUtils.bytesToHexString(record));
            parseTag(new SimTlv(record, 0, record.length));
        }

        /* access modifiers changed from: package-private */
        public void parseTag(SimTlv tlv) {
            do {
                int tag = tlv.getTag();
                switch (tag) {
                    case 168:
                    case 169:
                    case 170:
                        byte[] data = tlv.getData();
                        parseEfAndSFI(new SimTlv(data, 0, data.length), tag);
                        break;
                }
            } while (tlv.nextObject());
        }

        /* access modifiers changed from: package-private */
        public void parseEfAndSFI(SimTlv tlv, int parentTag) {
            int sfi;
            int tagNumberWithinParentTag = 0;
            do {
                int tag = tlv.getTag();
                switch (tag) {
                    case 192:
                    case 193:
                    case 194:
                    case 195:
                    case 196:
                    case 197:
                    case UsimPhoneBookManager.USIM_EFGRP_TAG /*{ENCODED_INT: 198}*/:
                    case UsimPhoneBookManager.USIM_EFAAS_TAG /*{ENCODED_INT: 199}*/:
                    case 200:
                    case UsimPhoneBookManager.USIM_EFUID_TAG /*{ENCODED_INT: 201}*/:
                    case UsimPhoneBookManager.USIM_EFEMAIL_TAG /*{ENCODED_INT: 202}*/:
                    case UsimPhoneBookManager.USIM_EFCCP1_TAG /*{ENCODED_INT: 203}*/:
                        byte[] data = tlv.getData();
                        if (data.length >= 2 && data.length <= 3) {
                            if (data.length == 3) {
                                sfi = data[2] & UsimPhoneBookManager.INVALID_BYTE;
                            } else {
                                sfi = -1;
                            }
                            this.mFileIds.put(tag, new File(parentTag, ((data[0] & UsimPhoneBookManager.INVALID_BYTE) << 8) | (data[1] & UsimPhoneBookManager.INVALID_BYTE), sfi, tagNumberWithinParentTag));
                            break;
                        } else {
                            UsimPhoneBookManager.this.log("Invalid TLV length: " + data.length);
                            break;
                        }
                        break;
                }
                tagNumberWithinParentTag++;
            } while (tlv.nextObject());
        }

        public SparseArray<File> getFileIdHw() {
            return this.mFileIds;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }
}
