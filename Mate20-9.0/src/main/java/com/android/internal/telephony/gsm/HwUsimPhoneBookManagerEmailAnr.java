package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class HwUsimPhoneBookManagerEmailAnr extends UsimPhoneBookManager {
    private static final int ADN_RECORD_LENGTH_DEFAULT = 20;
    private static final int ANR_ADDITIONAL_NUMBER_END_ID = 12;
    private static final int ANR_ADDITIONAL_NUMBER_START_ID = 3;
    private static final int ANR_ADN_RECORD_IDENTIFIER_ID = 16;
    private static final int ANR_ADN_SFI_ID = 15;
    private static final int ANR_BCD_NUMBER_LENGTH = 1;
    private static final int ANR_CAPABILITY_ID = 13;
    private static final int ANR_DESCRIPTION_ID = 0;
    private static final int ANR_EXTENSION_ID = 14;
    private static final int ANR_RECORD_LENGTH = 15;
    private static final int ANR_TON_NPI_ID = 2;
    private static final int DATA_DESCRIPTION_ID_IN_EFEXT1 = 2;
    private static final int DATA_SIZE_IN_EFEXT1 = 13;
    private static final boolean DBG = true;
    private static final int EVENT_ANR_LOAD_DONE = 5;
    private static final int EVENT_EF_ANR_RECORD_SIZE_DONE = 7;
    private static final int EVENT_EF_EMAIL_RECORD_SIZE_DONE = 6;
    private static final int EVENT_EF_EXT1_RECORD_SIZE_DONE = 13;
    private static final int EVENT_EF_IAP_RECORD_SIZE_DONE = 10;
    private static final int EVENT_EMAIL_LOAD_DONE = 4;
    private static final int EVENT_EXT1_LOAD_DONE = 12;
    private static final int EVENT_GET_SIZE_DONE = 101;
    private static final int EVENT_IAP_LOAD_DONE = 3;
    private static final int EVENT_PBR_LOAD_DONE = 1;
    private static final int EVENT_UPDATE_ANR_RECORD_DONE = 9;
    private static final int EVENT_UPDATE_EMAIL_RECORD_DONE = 8;
    private static final int EVENT_UPDATE_EXT1_RECORD_DONE = 14;
    private static final int EVENT_UPDATE_IAP_RECORD_DONE = 11;
    private static final int EVENT_USIM_ADN_LOAD_DONE = 2;
    private static final int EXT1_RECORD_LENGTH_MAX_DEFAULT = 10;
    private static final int EXT_DESCRIPTION_ID_IN_EFEXT1 = 0;
    private static final int EXT_TAG_IN_EFEXT1 = 2;
    private static final int FREE_TAG_IN_EFEXT1 = 0;
    private static final int LENGTH_DESCRIPTION_ID_IN_EFEXT1 = 1;
    private static final String LOG_TAG = "HwUsimPhoneBookManagerEmailAnr";
    private static final int MAX_NUMBER_SIZE_BYTES = 11;
    private static final int RECORDS_SIZE_ARRAY_VALID_LENGTH = 3;
    private static final int RECORDS_TOTAL_NUMBER_ARRAY_INDEX = 2;
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
    /* access modifiers changed from: private */
    public static final HashSet<Integer> USIM_EF_TAG_SET = new HashSet<Integer>() {
        {
            add(202);
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFADN_TAG));
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFEXT1_TAG));
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG));
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFPBC_TAG));
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFGRP_TAG));
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFAAS_TAG));
            add(200);
            add(201);
            add(203);
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFIAP_TAG));
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFSNE_TAG));
        }
    };
    private static final int USIM_TYPE1_TAG = 168;
    private static final int USIM_TYPE2_TAG = 169;
    private static final int USIM_TYPE3_TAG = 170;
    /* access modifiers changed from: private */
    public static final HashSet<Integer> USIM_TYPE_TAG_SET = new HashSet<Integer>() {
        {
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG));
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG));
            add(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_TYPE3_TAG));
        }
    };
    private AdnRecordCache mAdnCache;
    private ArrayList<Integer> mAdnLengthList = null;
    private Map<Integer, ArrayList<byte[]>> mAnrFileRecord;
    private Map<Integer, ArrayList<Integer>> mAnrFlags;
    private ArrayList<Integer>[] mAnrFlagsRecord;
    private boolean mAnrPresentInIap = false;
    private int mAnrTagNumberInIap = 0;
    private Map<Integer, ArrayList<byte[]>> mEmailFileRecord;
    private Map<Integer, ArrayList<Integer>> mEmailFlags;
    private ArrayList<Integer>[] mEmailFlagsRecord;
    private boolean mEmailPresentInIap = false;
    private int mEmailTagNumberInIap = 0;
    private Map<Integer, ArrayList<byte[]>> mExt1FileRecord;
    private Map<Integer, ArrayList<Integer>> mExt1Flags;
    private ArrayList<Integer>[] mExt1FlagsRecord;
    private IccFileHandler mFh;
    private Map<Integer, ArrayList<byte[]>> mIapFileRecord;
    /* access modifiers changed from: private */
    public boolean mIapPresent = false;
    private Boolean mIsPbrPresent;
    private final Object mLock = new Object();
    private PbrFile mPbrFile;
    private ArrayList<AdnRecord> mPhoneBookRecords;
    private Map<Integer, ArrayList<Integer>> mRecordNums;
    private int[] mRecordSize = new int[3];
    private boolean mRefreshCache = false;
    private boolean mSuccess = false;
    private int[] temRecordSize = new int[3];

    private class PbrFile {
        boolean isInvalidAnrType = false;
        boolean isInvalidEmailType = false;
        boolean isNoAnrExist = false;
        boolean isNoEmailExist = false;
        HashMap<Integer, ArrayList<Integer>> mAnrFileIds = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> mEmailFileIds = new HashMap<>();
        HashMap<Integer, Map<Integer, Integer>> mFileIds = new HashMap<>();

        PbrFile(ArrayList<byte[]> records) {
            int recNum = 0;
            if (records != null) {
                int listSize = records.size();
                for (int i = 0; i < listSize; i++) {
                    byte[] record = records.get(i);
                    Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "before making TLVs, data is " + IccUtils.bytesToHexString(record));
                    if (record != null && !IccUtils.bytesToHexString(record).startsWith("ffff")) {
                        SimTlv recTlv = new SimTlv(record, 0, record.length);
                        if (!recTlv.isValidObject()) {
                            Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "null == recTlv || !recTlv.isValidObject() is true");
                        } else {
                            parseTag(recTlv, recNum);
                            if (this.mFileIds.get(Integer.valueOf(recNum)) != null) {
                                recNum++;
                            }
                        }
                    }
                }
            }
        }

        private void parseTag(SimTlv tlv, int recNum) {
            Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseTag: recNum=xxxxxx");
            boolean unused = HwUsimPhoneBookManagerEmailAnr.this.mIapPresent = false;
            HashMap hashMap = new HashMap();
            ArrayList arrayList = new ArrayList();
            ArrayList<Integer> emailList = new ArrayList<>();
            while (true) {
                ArrayList<Integer> emailList2 = emailList;
                int tag = tlv.getTag();
                if (HwUsimPhoneBookManagerEmailAnr.USIM_TYPE_TAG_SET.contains(Integer.valueOf(tag))) {
                    byte[] data = tlv.getData();
                    if (data != null && data.length != 0) {
                        parseEf(new SimTlv(data, 0, data.length), hashMap, tag, arrayList, emailList2);
                    } else if (tag == HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG) {
                        Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseTag: invalid A8 data, ignore the whole record");
                        return;
                    }
                }
                if (!tlv.nextObject()) {
                    if (arrayList.size() != 0) {
                        this.mAnrFileIds.put(Integer.valueOf(recNum), arrayList);
                        Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseTag: recNum=xxxxxx ANR file list:" + arrayList);
                    }
                    if (emailList2.size() != 0) {
                        Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseTag: recNum=xxxxxx EMAIL file list:" + emailList2);
                        this.mEmailFileIds.put(Integer.valueOf(recNum), emailList2);
                    }
                    this.mFileIds.put(Integer.valueOf(recNum), hashMap);
                    if (hashMap.size() != 0) {
                        if (!hashMap.containsKey(202)) {
                            this.isNoEmailExist = true;
                        }
                        if (!hashMap.containsKey(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG))) {
                            this.isNoAnrExist = true;
                        }
                    }
                    return;
                }
                emailList = emailList2;
            }
        }

        /* access modifiers changed from: package-private */
        public void parseEf(SimTlv tlv, Map<Integer, Integer> val, int parentTag, ArrayList<Integer> anrList, ArrayList<Integer> emailList) {
            int tagNumberWithinParentTag = 0;
            do {
                int tag = tlv.getTag();
                if (parentTag == HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG && tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFIAP_TAG) {
                    boolean unused = HwUsimPhoneBookManagerEmailAnr.this.mIapPresent = true;
                }
                if (parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG || HwUsimPhoneBookManagerEmailAnr.this.mIapPresent) {
                    HwUsimPhoneBookManagerEmailAnr.this.syncDataFromTagInParseEf(parentTag, tag, tagNumberWithinParentTag);
                    if (HwUsimPhoneBookManagerEmailAnr.USIM_EF_TAG_SET.contains(Integer.valueOf(tag))) {
                        parseOneEfTag(tlv, val, tag, parentTag, anrList, emailList);
                    }
                    tagNumberWithinParentTag++;
                }
            } while (tlv.nextObject());
        }

        private void parseOneEfTag(SimTlv tlv, Map<Integer, Integer> val, int tag, int parentTag, ArrayList<Integer> anrList, ArrayList<Integer> emailList) {
            byte[] data = tlv.getData();
            if (data != null && data.length >= 2) {
                int efid = ((data[0] & 255) << 8) | (data[1] & HwSubscriptionManager.SUB_INIT_STATE);
                if (val.containsKey(Integer.valueOf(tag))) {
                    Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "already have (" + tag + "," + efid + ") parent tag:" + parentTag);
                } else if (!shouldIgnoreEmail(tag, parentTag) && !shouldIgnoreAnr(tag, parentTag)) {
                    val.put(Integer.valueOf(tag), Integer.valueOf(efid));
                    if (parentTag == HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG) {
                        if (tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG) {
                            anrList.add(Integer.valueOf(efid));
                        } else if (tag == 202) {
                            emailList.add(Integer.valueOf(efid));
                        }
                    }
                    Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseEf.put(" + tag + "," + efid + ") parent tag:" + parentTag);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean shouldIgnoreEmail(int tag, int parentTag) {
            if (tag == 202 && (this.isInvalidEmailType || (parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG && parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG))) {
                HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: invalid Email type!");
                this.isInvalidEmailType = true;
                return true;
            } else if (tag != 202 || !this.isNoEmailExist) {
                return false;
            } else {
                HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: isNoEmailExist");
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean shouldIgnoreAnr(int tag, int parentTag) {
            if (tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG && (this.isInvalidAnrType || (parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG && parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG))) {
                HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: invalid Anr type!");
                this.isInvalidAnrType = true;
                return true;
            } else if (tag != HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG || !this.isNoAnrExist) {
                return false;
            } else {
                HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: isNoAnrExist");
                return true;
            }
        }
    }

    public HwUsimPhoneBookManagerEmailAnr(IccFileHandler fh) {
        super(fh, null);
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList<>();
        this.mPbrFile = null;
        this.mIsPbrPresent = true;
    }

    public HwUsimPhoneBookManagerEmailAnr(IccFileHandler fh, AdnRecordCache cache) {
        super(fh, cache);
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList<>();
        this.mAdnLengthList = new ArrayList<>();
        this.mIapFileRecord = new HashMap();
        this.mEmailFileRecord = new HashMap();
        this.mAnrFileRecord = new HashMap();
        this.mRecordNums = new HashMap();
        this.mPbrFile = null;
        this.mAnrFlags = new HashMap();
        this.mEmailFlags = new HashMap();
        if (IccRecords.getAdnLongNumberSupport()) {
            initExt1FileRecordAndFlags();
        }
        this.mIsPbrPresent = true;
        this.mAdnCache = cache;
    }

    public void reset() {
        if (!(this.mAnrFlagsRecord == null || this.mEmailFlagsRecord == null || this.mPbrFile == null)) {
            int pbsFileSize = this.mPbrFile.mFileIds.size();
            for (int i = 0; i < pbsFileSize; i++) {
                this.mAnrFlagsRecord[i].clear();
                this.mEmailFlagsRecord[i].clear();
            }
        }
        if (IccRecords.getAdnLongNumberSupport() != 0) {
            resetExt1Variables();
        }
        this.mAnrFlags.clear();
        this.mEmailFlags.clear();
        this.mPhoneBookRecords.clear();
        this.mIapFileRecord.clear();
        this.mEmailFileRecord.clear();
        this.mAnrFileRecord.clear();
        this.mRecordNums.clear();
        this.mPbrFile = null;
        this.mAdnLengthList.clear();
        this.mIsPbrPresent = true;
        this.mRefreshCache = false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0097, code lost:
        return r6.mPhoneBookRecords;
     */
    public ArrayList<AdnRecord> loadEfFilesFromUsim() {
        synchronized (this.mLock) {
            if (!this.mPhoneBookRecords.isEmpty()) {
                if (this.mRefreshCache) {
                    this.mRefreshCache = false;
                    refreshCache();
                }
                ArrayList<AdnRecord> arrayList = this.mPhoneBookRecords;
                return arrayList;
            } else if (!this.mIsPbrPresent.booleanValue()) {
                return null;
            } else {
                if (this.mPbrFile == null) {
                    readPbrFileAndWait();
                }
                if (this.mPbrFile == null) {
                    return null;
                }
                int numRecs = this.mPbrFile.mFileIds.size();
                if (this.mAnrFlagsRecord == null && this.mEmailFlagsRecord == null) {
                    this.mAnrFlagsRecord = new ArrayList[numRecs];
                    this.mEmailFlagsRecord = new ArrayList[numRecs];
                    for (int i = 0; i < numRecs; i++) {
                        this.mAnrFlagsRecord[i] = new ArrayList<>();
                        this.mEmailFlagsRecord[i] = new ArrayList<>();
                    }
                }
                if (this.mAdnLengthList != null && this.mAdnLengthList.size() == 0) {
                    for (int i2 = 0; i2 < numRecs; i2++) {
                        this.mAdnLengthList.add(0);
                    }
                }
                for (int i3 = 0; i3 < numRecs; i3++) {
                    readAdnFileAndWait(i3);
                    readEmailFileAndWait(i3);
                    readAnrFileAndWait(i3);
                }
                if (IccRecords.getAdnLongNumberSupport() != 0) {
                    loadExt1FilesFromUsim(numRecs);
                }
            }
        }
    }

    private void refreshCache() {
        if (this.mPbrFile != null) {
            this.mPhoneBookRecords.clear();
            int numRecs = this.mPbrFile.mFileIds.size();
            for (int i = 0; i < numRecs; i++) {
                readAdnFileAndWait(i);
            }
        }
    }

    public void invalidateCache() {
        this.mRefreshCache = true;
    }

    private void readPbrFileAndWait() {
        this.mFh.loadEFLinearFixedAll(20272, obtainMessage(1));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
        }
    }

    private void readEmailFileAndWait(int recNum) {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from readEmailFileAndWait");
            return;
        }
        Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds != null && fileIds.containsKey(202)) {
            if (this.mEmailPresentInIap) {
                if (fileIds.containsKey(Integer.valueOf(USIM_EFIAP_TAG))) {
                    readIapFileAndWait(fileIds.get(Integer.valueOf(USIM_EFIAP_TAG)).intValue(), recNum);
                } else {
                    log("fileIds don't contain USIM_EFIAP_TAG");
                }
                if (!hasRecordIn(this.mIapFileRecord, recNum)) {
                    Rlog.e(LOG_TAG, "Error: IAP file is empty");
                    return;
                }
                this.mFh.loadEFLinearFixedAll(fileIds.get(202).intValue(), obtainMessage(4, Integer.valueOf(recNum)));
                log("readEmailFileAndWait email efid is : " + fileIds.get(202));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                }
            } else {
                Iterator it = this.mPbrFile.mEmailFileIds.get(Integer.valueOf(recNum)).iterator();
                while (it.hasNext()) {
                    this.mFh.loadEFLinearFixedPartHW(((Integer) it.next()).intValue(), getValidRecordNums(recNum), obtainMessage(4, Integer.valueOf(recNum)));
                    log("readEmailFileAndWait email efid is : " + efid + " recNum:" + recNum);
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e2) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                    }
                }
            }
            ArrayList<byte[]> emailFileArray = this.mEmailFileRecord.get(Integer.valueOf(recNum));
            if (emailFileArray != null) {
                int emailDileArraySize = emailFileArray.size();
                for (int m = 0; m < emailDileArraySize; m++) {
                    this.mEmailFlagsRecord[recNum].add(0);
                }
            }
            this.mEmailFlags.put(Integer.valueOf(recNum), this.mEmailFlagsRecord[recNum]);
            updatePhoneAdnRecordWithEmail(recNum);
        }
    }

    private void readAnrFileAndWait(int recNum) {
        Map<Integer, Integer> fileIds = initFileIds(recNum);
        if (!(fileIds == null || fileIds.isEmpty()) && fileIds.containsKey(Integer.valueOf(USIM_EFANR_TAG))) {
            if (this.mAnrPresentInIap) {
                if (fileIds.containsKey(Integer.valueOf(USIM_EFIAP_TAG))) {
                    readIapFileAndWait(fileIds.get(Integer.valueOf(USIM_EFIAP_TAG)).intValue(), recNum);
                } else {
                    log("fileIds don't contain USIM_EFIAP_TAG");
                }
                if (!hasRecordIn(this.mIapFileRecord, recNum)) {
                    Rlog.e(LOG_TAG, "Error: IAP file is empty");
                    return;
                }
                this.mFh.loadEFLinearFixedAll(fileIds.get(Integer.valueOf(USIM_EFANR_TAG)).intValue(), obtainMessage(5, Integer.valueOf(recNum)));
                log("readAnrFileAndWait anr efid is : " + fileIds.get(Integer.valueOf(USIM_EFANR_TAG)));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                }
            } else {
                Iterator it = this.mPbrFile.mAnrFileIds.get(Integer.valueOf(recNum)).iterator();
                while (it.hasNext()) {
                    this.mFh.loadEFLinearFixedPartHW(((Integer) it.next()).intValue(), getValidRecordNums(recNum), obtainMessage(5, Integer.valueOf(recNum)));
                    log("readAnrFileAndWait anr efid is : " + efid + " recNum:" + recNum);
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e2) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                    }
                }
            }
            ArrayList<byte[]> anrFileArray = this.mAnrFileRecord.get(Integer.valueOf(recNum));
            if (anrFileArray != null) {
                int anrFileArraySize = anrFileArray.size();
                for (int m = 0; m < anrFileArraySize; m++) {
                    this.mAnrFlagsRecord[recNum].add(0);
                }
            }
            this.mAnrFlags.put(Integer.valueOf(recNum), this.mAnrFlagsRecord[recNum]);
            updatePhoneAdnRecordWithAnr(recNum);
        }
    }

    private Map<Integer, Integer> initFileIds(int recNum) {
        if (this.mPbrFile != null) {
            return this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        }
        Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from readAnrFileAndWait");
        return null;
    }

    private void readIapFileAndWait(int efid, int recNum) {
        log("pbrIndex is " + recNum + ",iap efid is : " + efid);
        this.mFh.loadEFLinearFixedPartHW(efid, getValidRecordNums(recNum), obtainMessage(3, Integer.valueOf(recNum)));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readIapFileAndWait");
        }
    }

    public boolean updateEmailFile(int adnRecNum, String oldEmail, String newEmail, int efidIndex) {
        int pbrIndex = getPbrIndexBy(adnRecNum - 1);
        int efid = getEfidByTag(pbrIndex, 202, efidIndex);
        if (oldEmail == null) {
            oldEmail = "";
        }
        if (newEmail == null) {
            newEmail = "";
        }
        String emails = oldEmail + "," + newEmail;
        this.mSuccess = false;
        log("updateEmailFile  efid" + efid + " adnRecNum: " + adnRecNum);
        if (efid == -1) {
            return this.mSuccess;
        }
        if (!this.mEmailPresentInIap || !TextUtils.isEmpty(oldEmail) || TextUtils.isEmpty(newEmail)) {
            this.mSuccess = true;
        } else if (getEmptyEmailNum_Pbrindex(pbrIndex) == 0) {
            log("updateEmailFile getEmptyEmailNum_Pbrindex=0, pbrIndex is " + pbrIndex);
            this.mSuccess = false;
            return this.mSuccess;
        } else {
            this.mSuccess = updateIapFile(adnRecNum, oldEmail, newEmail, 202);
        }
        if (this.mSuccess) {
            synchronized (this.mLock) {
                this.mFh.getEFLinearRecordSize(efid, obtainMessage(6, adnRecNum, efid, emails));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "interrupted while trying to update by search");
                }
            }
        }
        if (this.mEmailPresentInIap && this.mSuccess && !TextUtils.isEmpty(oldEmail) && TextUtils.isEmpty(newEmail)) {
            this.mSuccess = updateIapFile(adnRecNum, oldEmail, newEmail, 202);
        }
        return this.mSuccess;
    }

    public boolean updateAnrFile(int adnRecNum, String oldAnr, String newAnr, int efidIndex) {
        int pbrIndex = getPbrIndexBy(adnRecNum - 1);
        int efid = getEfidByTag(pbrIndex, USIM_EFANR_TAG, efidIndex);
        if (oldAnr == null) {
            oldAnr = "";
        }
        if (newAnr == null) {
            newAnr = "";
        }
        String anrs = oldAnr + "," + newAnr;
        this.mSuccess = false;
        log("updateAnrFile  efid" + efid + ", adnRecNum: " + adnRecNum);
        if (efid == -1) {
            return this.mSuccess;
        }
        if (!this.mAnrPresentInIap || !TextUtils.isEmpty(oldAnr) || TextUtils.isEmpty(newAnr)) {
            this.mSuccess = true;
        } else if (getEmptyAnrNum_Pbrindex(pbrIndex) == 0) {
            log("updateAnrFile getEmptyAnrNum_Pbrindex=0, pbrIndex is " + pbrIndex);
            this.mSuccess = false;
            return this.mSuccess;
        } else {
            this.mSuccess = updateIapFile(adnRecNum, oldAnr, newAnr, USIM_EFANR_TAG);
        }
        synchronized (this.mLock) {
            this.mFh.getEFLinearRecordSize(efid, obtainMessage(7, adnRecNum, efid, anrs));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "interrupted while trying to update by search");
            }
        }
        if (this.mAnrPresentInIap && this.mSuccess && !TextUtils.isEmpty(oldAnr) && TextUtils.isEmpty(newAnr)) {
            this.mSuccess = updateIapFile(adnRecNum, oldAnr, newAnr, USIM_EFANR_TAG);
        }
        return this.mSuccess;
    }

    private boolean updateIapFile(int adnRecNum, String oldValue, String newValue, int tag) {
        int efid = getEfidByTag(getPbrIndexBy(adnRecNum - 1), USIM_EFIAP_TAG, 0);
        this.mSuccess = false;
        int recordNumber = -1;
        if (efid == -1) {
            return this.mSuccess;
        }
        if (tag == USIM_EFANR_TAG) {
            recordNumber = getAnrRecNumber(adnRecNum - 1, this.mPhoneBookRecords.size(), oldValue);
        } else if (tag == 202) {
            recordNumber = getEmailRecNumber(adnRecNum - 1, this.mPhoneBookRecords.size(), oldValue);
        }
        if (TextUtils.isEmpty(newValue)) {
            recordNumber = -1;
        }
        log("updateIapFile  efid=" + efid + ", recordNumber= " + recordNumber + ", adnRecNum=" + adnRecNum);
        synchronized (this.mLock) {
            this.mFh.getEFLinearRecordSize(efid, obtainMessage(10, adnRecNum, recordNumber, Integer.valueOf(tag)));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "interrupted while trying to update by search");
            }
        }
        return this.mSuccess;
    }

    private int getEfidByTag(int recNum, int tag, int efidIndex) {
        int efid;
        if (this.mPbrFile == null || this.mPbrFile.mFileIds == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from getEfidByTag");
            return -1;
        }
        Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds == null || !fileIds.containsKey(Integer.valueOf(tag))) {
            return -1;
        }
        if (this.mEmailPresentInIap || 202 != tag) {
            if (this.mAnrPresentInIap || USIM_EFANR_TAG != tag) {
                efid = fileIds.get(Integer.valueOf(tag)).intValue();
            } else if (!hasRecordIn(this.mPbrFile.mAnrFileIds.get(Integer.valueOf(recNum)), efidIndex)) {
                return -1;
            } else {
                efid = ((Integer) this.mPbrFile.mAnrFileIds.get(Integer.valueOf(recNum)).get(efidIndex)).intValue();
            }
        } else if (!hasRecordIn(this.mPbrFile.mEmailFileIds.get(Integer.valueOf(recNum)), efidIndex)) {
            return -1;
        } else {
            efid = ((Integer) this.mPbrFile.mEmailFileIds.get(Integer.valueOf(recNum)).get(efidIndex)).intValue();
        }
        return efid;
    }

    public int getPbrIndexBy(int adnIndex) {
        int len = this.mAdnLengthList.size();
        int size = 0;
        for (int i = 0; i < len; i++) {
            size += this.mAdnLengthList.get(i).intValue();
            if (adnIndex < size) {
                return i;
            }
        }
        return -1;
    }

    public int getPbrIndexByEfid(int efid) {
        if (!(this.mPbrFile == null || this.mPbrFile.mFileIds == null)) {
            int pbrFileIdSize = this.mPbrFile.mFileIds.size();
            for (int i = 0; i < pbrFileIdSize; i++) {
                Map<Integer, Integer> val = this.mPbrFile.mFileIds.get(Integer.valueOf(i));
                if (val != null && val.containsValue(Integer.valueOf(efid))) {
                    return i;
                }
            }
        }
        return 0;
    }

    public int getInitIndexByPbr(int pbrIndex) {
        return getInitIndexBy(pbrIndex);
    }

    private int getInitIndexBy(int pbrIndex) {
        int index = 0;
        while (pbrIndex > 0) {
            index += this.mAdnLengthList.get(pbrIndex - 1).intValue();
            pbrIndex--;
        }
        return index;
    }

    private boolean hasRecordIn(ArrayList<Integer> record, int pbrIndex) {
        if (record == null || record.isEmpty() || record.size() <= pbrIndex) {
            return false;
        }
        return true;
    }

    private boolean hasRecordIn(Map<Integer, ArrayList<byte[]>> record, int pbrIndex) {
        if (record == null || record.isEmpty()) {
            return false;
        }
        try {
            if (record.get(Integer.valueOf(pbrIndex)) == null) {
                return false;
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            Rlog.e(LOG_TAG, "record is empty in pbrIndex" + pbrIndex);
            return false;
        }
    }

    private void updatePhoneAdnRecordWithEmail(int pbrIndex) {
        if (!hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
            Rlog.e(LOG_TAG, "Error: Email file is empty");
        } else if (!hasRecordIn(this.mAdnLengthList, pbrIndex)) {
            Rlog.e(LOG_TAG, "Error: mAdnLengthList is invalid");
        } else {
            int numAdnRecs = this.mAdnLengthList.get(pbrIndex).intValue();
            if (!this.mEmailPresentInIap || !hasRecordIn(this.mIapFileRecord, pbrIndex)) {
                int len = this.mAdnLengthList.get(pbrIndex).intValue();
                if (!this.mEmailPresentInIap) {
                    parseType1EmailFile(len, pbrIndex);
                }
                return;
            }
            for (int i = 0; i < numAdnRecs; i++) {
                if (getRecNumber(i, pbrIndex, this.mEmailTagNumberInIap) > 0) {
                    String[] emails = {readEmailRecord(recNum - 1, pbrIndex, 0)};
                    int adnRecIndex = getInitIndexBy(pbrIndex) + i;
                    AdnRecord rec = this.mPhoneBookRecords.get(adnRecIndex);
                    if (rec != null && !TextUtils.isEmpty(emails[0])) {
                        rec.setEmails(emails);
                        this.mPhoneBookRecords.set(adnRecIndex, rec);
                        this.mEmailFlags.get(Integer.valueOf(pbrIndex)).set(recNum - 1, 1);
                    }
                }
            }
            int emailRecsSize = this.mEmailFileRecord.get(Integer.valueOf(pbrIndex)).size();
            for (int index = 0; index < emailRecsSize; index++) {
                if (1 != ((Integer) this.mEmailFlags.get(Integer.valueOf(pbrIndex)).get(index)).intValue() && !"".equals(readEmailRecord(index, pbrIndex, 0))) {
                    byte[] emailRec = (byte[]) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex)).get(index);
                    for (int i2 = 0; i2 < emailRec.length; i2++) {
                        emailRec[i2] = -1;
                    }
                }
            }
            log("updatePhoneAdnRecordWithEmail: no need to parse type1 EMAIL file");
        }
    }

    private void updatePhoneAdnRecordWithAnr(int pbrIndex) {
        if (!hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            Rlog.e(LOG_TAG, "Error: Anr file is empty");
        } else if (!hasRecordIn(this.mAdnLengthList, pbrIndex)) {
            Rlog.e(LOG_TAG, "Error: mAdnLengthList is invalid");
        } else {
            int numAdnRecs = this.mAdnLengthList.get(pbrIndex).intValue();
            if (!this.mAnrPresentInIap || !hasRecordIn(this.mIapFileRecord, pbrIndex)) {
                if (!this.mAnrPresentInIap) {
                    parseType1AnrFile(numAdnRecs, pbrIndex);
                }
                return;
            }
            for (int i = 0; i < numAdnRecs; i++) {
                int recNum = getRecNumber(i, pbrIndex, this.mAnrTagNumberInIap);
                if (recNum == -1) {
                    break;
                }
                if (recNum > 0) {
                    String[] anrs = {readAnrRecord(recNum - 1, pbrIndex, 0)};
                    int adnRecIndex = getInitIndexBy(pbrIndex) + i;
                    AdnRecord rec = this.mPhoneBookRecords.get(adnRecIndex);
                    if (rec != null && !TextUtils.isEmpty(anrs[0])) {
                        rec.setAdditionalNumbers(anrs);
                        this.mPhoneBookRecords.set(adnRecIndex, rec);
                        this.mAnrFlags.get(Integer.valueOf(pbrIndex)).set(recNum - 1, 1);
                    }
                }
            }
            int anrRecsSize = this.mAnrFileRecord.get(Integer.valueOf(pbrIndex)).size();
            for (int index = 0; index < anrRecsSize; index++) {
                if (1 != ((Integer) this.mAnrFlags.get(Integer.valueOf(pbrIndex)).get(index)).intValue() && !"".equals(readAnrRecord(index, pbrIndex, 0))) {
                    byte[] anrRec = (byte[]) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex)).get(index);
                    for (int i2 = 0; i2 < anrRec.length; i2++) {
                        anrRec[i2] = -1;
                    }
                }
            }
            log("updatePhoneAdnRecordWithAnr: no need to parse type1 ANR file");
        }
    }

    /* access modifiers changed from: package-private */
    public void parseType1EmailFile(int numRecs, int pbrIndex) {
        int numEmailFiles = this.mPbrFile.mEmailFileIds.get(Integer.valueOf(pbrIndex)).size();
        ArrayList<String> emailList = new ArrayList<>();
        int adnInitIndex = getInitIndexBy(pbrIndex);
        if (hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
            log("parseType1EmailFile: pbrIndex is: " + pbrIndex + ", numRecs is: " + numRecs);
            for (int i = 0; i < numRecs; i++) {
                emailList.clear();
                int count = 0;
                for (int j = 0; j < numEmailFiles; j++) {
                    String email = readEmailRecord(i, pbrIndex, j * numRecs);
                    emailList.add(email);
                    if (!TextUtils.isEmpty(email)) {
                        count++;
                        this.mEmailFlags.get(Integer.valueOf(pbrIndex)).set((j * numRecs) + i, 1);
                    }
                }
                if (count != 0) {
                    AdnRecord rec = this.mPhoneBookRecords.get(i + adnInitIndex);
                    if (rec != null) {
                        String[] emails = new String[emailList.size()];
                        System.arraycopy(emailList.toArray(), 0, emails, 0, emailList.size());
                        rec.setEmails(emails);
                        this.mPhoneBookRecords.set(i + adnInitIndex, rec);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void parseType1AnrFile(int numRecs, int pbrIndex) {
        int numAnrFiles = this.mPbrFile.mAnrFileIds.get(Integer.valueOf(pbrIndex)).size();
        ArrayList<String> anrList = new ArrayList<>();
        int adnInitIndex = getInitIndexBy(pbrIndex);
        if (hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            log("parseType1AnrFile: pbrIndex is: " + pbrIndex + ", numRecs is: " + numRecs + ", numAnrFiles " + numAnrFiles);
            for (int i = 0; i < numRecs; i++) {
                anrList.clear();
                int count = 0;
                for (int j = 0; j < numAnrFiles; j++) {
                    String anr = readAnrRecord(i, pbrIndex, j * numRecs);
                    anrList.add(anr);
                    if (!TextUtils.isEmpty(anr)) {
                        count++;
                        this.mAnrFlags.get(Integer.valueOf(pbrIndex)).set((j * numRecs) + i, 1);
                    }
                }
                if (count != 0) {
                    AdnRecord rec = this.mPhoneBookRecords.get(i + adnInitIndex);
                    if (rec != null) {
                        String[] anrs = new String[anrList.size()];
                        System.arraycopy(anrList.toArray(), 0, anrs, 0, anrList.size());
                        rec.setAdditionalNumbers(anrs);
                        this.mPhoneBookRecords.set(i + adnInitIndex, rec);
                    }
                }
            }
        }
    }

    private String readEmailRecord(int recNum, int pbrIndex, int offSet) {
        if (!hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
            return null;
        }
        try {
            byte[] emailRec = (byte[]) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex)).get(recNum + offSet);
            return IccUtils.adnStringFieldToString(emailRec, 0, emailRec.length - 2);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private String readAnrRecord(int recNum, int pbrIndex, int offSet) {
        if (!hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            return null;
        }
        try {
            byte[] anrRec = (byte[]) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex)).get(recNum + offSet);
            int numberLength = 255 & anrRec[1];
            if (numberLength > 11) {
                return "";
            }
            return PhoneNumberUtils.calledPartyBCDToString(anrRec, 2, numberLength);
        } catch (IndexOutOfBoundsException e) {
            Rlog.e(LOG_TAG, "Error: Improper ICC card: No anr record for ADN, continuing");
            return null;
        }
    }

    private void readAdnFileAndWait(int recNum) {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from readAdnFileAndWait");
            return;
        }
        Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds != null && !fileIds.isEmpty()) {
            int extEf = 0;
            if (fileIds.containsKey(Integer.valueOf(USIM_EFEXT1_TAG))) {
                extEf = fileIds.get(Integer.valueOf(USIM_EFEXT1_TAG)).intValue();
            }
            log("readAdnFileAndWait adn efid is : " + fileIds.get(Integer.valueOf(USIM_EFADN_TAG)));
            if (fileIds.containsKey(Integer.valueOf(USIM_EFADN_TAG))) {
                this.mAdnCache.requestLoadAllAdnLike(fileIds.get(Integer.valueOf(USIM_EFADN_TAG)).intValue(), extEf, obtainMessage(2, Integer.valueOf(recNum)));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
                }
            }
        }
    }

    private int getEmailRecNumber(int adnRecIndex, int numRecs, String oldEmail) {
        int recsSize;
        int recordNumber;
        int pbrIndex = getPbrIndexBy(adnRecIndex);
        int recordIndex = adnRecIndex - getInitIndexBy(pbrIndex);
        log("getEmailRecNumber adnRecIndex is: " + adnRecIndex + ", recordIndex is :" + recordIndex);
        if (!hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
            log("getEmailRecNumber recordNumber is: " + -1);
            return -1;
        } else if (!this.mEmailPresentInIap || !hasRecordIn(this.mIapFileRecord, pbrIndex)) {
            return recordIndex + 1;
        } else {
            byte[] record = null;
            try {
                record = (byte[]) this.mIapFileRecord.get(Integer.valueOf(pbrIndex)).get(recordIndex);
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "IndexOutOfBoundsException in getEmailRecNumber");
            }
            if (record == null || this.mEmailTagNumberInIap >= record.length || record[this.mEmailTagNumberInIap] == -1 || (record[this.mEmailTagNumberInIap] & 255) <= 0 || (record[this.mEmailTagNumberInIap] & 255) > this.mEmailFileRecord.get(Integer.valueOf(pbrIndex)).size()) {
                log("getEmailRecNumber recsSize is: " + recsSize);
                if (TextUtils.isEmpty(oldEmail)) {
                    for (int i = 0; i < recsSize; i++) {
                        if (TextUtils.isEmpty(readEmailRecord(i, pbrIndex, 0))) {
                            log("getEmailRecNumber: Got empty record.Email record num is :" + (i + 1));
                            return i + 1;
                        }
                    }
                }
                log("getEmailRecNumber: no email record index found");
                return -1;
            }
            log(" getEmailRecNumber: record is " + IccUtils.bytesToHexString(record) + ", the email recordNumber is :" + recordNumber);
            return recordNumber;
        }
    }

    private int getAnrRecNumber(int adnRecIndex, int numRecs, String oldAnr) {
        int recsSize;
        int recordNumber;
        int pbrIndex = getPbrIndexBy(adnRecIndex);
        int recordIndex = adnRecIndex - getInitIndexBy(pbrIndex);
        if (!hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            return -1;
        }
        if (!this.mAnrPresentInIap || !hasRecordIn(this.mIapFileRecord, pbrIndex)) {
            return recordIndex + 1;
        }
        byte[] record = null;
        try {
            record = (byte[]) this.mIapFileRecord.get(Integer.valueOf(pbrIndex)).get(recordIndex);
        } catch (IndexOutOfBoundsException e) {
            Rlog.e(LOG_TAG, "IndexOutOfBoundsException in getAnrRecNumber");
        }
        if (record == null || this.mAnrTagNumberInIap >= record.length || record[this.mAnrTagNumberInIap] == -1 || (record[this.mAnrTagNumberInIap] & 255) <= 0 || (record[this.mAnrTagNumberInIap] & 255) > this.mAnrFileRecord.get(Integer.valueOf(pbrIndex)).size()) {
            log("getAnrRecNumber: anr record size is :" + recsSize);
            if (TextUtils.isEmpty(oldAnr)) {
                for (int i = 0; i < recsSize; i++) {
                    if (TextUtils.isEmpty(readAnrRecord(i, pbrIndex, 0))) {
                        log("getAnrRecNumber: Empty anr record. Anr record num is :" + (i + 1));
                        return i + 1;
                    }
                }
            }
            log("getAnrRecNumber: no anr record index found");
            return -1;
        }
        log("getAnrRecNumber: recnum from iap is :" + recordNumber);
        return recordNumber;
    }

    private byte[] buildEmailData(int length, int adnRecIndex, String email) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = -1;
        }
        if (TextUtils.isEmpty(email) != 0) {
            log("[buildEmailData] Empty email record");
            return data;
        }
        byte[] byteEmail = GsmAlphabet.stringToGsm8BitPacked(email);
        if (byteEmail.length > data.length) {
            System.arraycopy(byteEmail, 0, data, 0, data.length);
        } else {
            System.arraycopy(byteEmail, 0, data, 0, byteEmail.length);
        }
        int recordIndex = adnRecIndex - getInitIndexBy(getPbrIndexBy(adnRecIndex));
        if (this.mEmailPresentInIap) {
            data[length - 1] = (byte) (recordIndex + 1);
        }
        return data;
    }

    private byte[] buildAnrData(int length, int adnRecIndex, String anr) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = -1;
        }
        if (length < 15) {
            log("The length is invalid " + length);
            return data;
        } else if (TextUtils.isEmpty(anr)) {
            log("[buildAnrData] Empty anr record");
            return data;
        } else {
            data[0] = 0;
            byte[] byteAnr = PhoneNumberUtils.numberToCalledPartyBCD(anr);
            if (byteAnr == null) {
                return null;
            }
            if (byteAnr.length > 11) {
                System.arraycopy(byteAnr, 0, data, 2, 11);
                data[1] = (byte) 11;
            } else {
                System.arraycopy(byteAnr, 0, data, 2, byteAnr.length);
                data[1] = (byte) byteAnr.length;
            }
            data[13] = -1;
            data[14] = -1;
            if (length == 17) {
                data[16] = (byte) ((adnRecIndex - getInitIndexBy(getPbrIndexBy(adnRecIndex))) + 1);
            }
            return data;
        }
    }

    private void createPbrFile(ArrayList<byte[]> records) {
        if (records == null) {
            this.mPbrFile = null;
            this.mIsPbrPresent = false;
            return;
        }
        this.mPbrFile = new PbrFile(records);
    }

    private void putValidRecNums(int pbrIndex) {
        ArrayList<Integer> recordNums = new ArrayList<>();
        int initAdnIndex = getInitIndexBy(pbrIndex);
        log("pbr index is " + pbrIndex + ", initAdnIndex is " + initAdnIndex);
        int adnListLengh = 0;
        if (this.mAdnLengthList != null) {
            adnListLengh = this.mAdnLengthList.get(pbrIndex).intValue();
        }
        for (int i = 0; i < adnListLengh; i++) {
            recordNums.add(Integer.valueOf(i + 1));
        }
        if (recordNums.size() == 0) {
            recordNums.add(1);
        }
        this.mRecordNums.put(Integer.valueOf(pbrIndex), recordNums);
    }

    private ArrayList<Integer> getValidRecordNums(int pbrIndex) {
        return this.mRecordNums.get(Integer.valueOf(pbrIndex));
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i != EVENT_GET_SIZE_DONE) {
            switch (i) {
                case 1:
                    handlePbrLoadDone(msg);
                    return;
                case 2:
                    handleUsimAdnLoadDone(msg);
                    return;
                case 3:
                    handleIapLoadDone(msg);
                    return;
                case 4:
                    handleEmailLoadDone(msg);
                    return;
                case 5:
                    handeAnrLoadDone(msg);
                    return;
                case 6:
                    handleEfEmailRecordSizeDone(msg);
                    return;
                case 7:
                    handleEfAnrRecordSizeDone(msg);
                    return;
                case 8:
                    handleUpdateEmailRecordDone(msg);
                    return;
                case 9:
                    handleUpdateAnrRecordDone(msg);
                    return;
                case 10:
                    handleEfIapRecordSizeDone(msg);
                    return;
                case 11:
                    handleUpdateIapRecordDone(msg);
                    return;
                case 12:
                    handleExt1LoadDone(msg);
                    return;
                case 13:
                    handleEfExt1RecordSizeDone(msg);
                    return;
                case 14:
                    handleUpdateExt1RecordDone(msg);
                    return;
                default:
                    return;
            }
        } else {
            handleGetSizeDone(msg);
        }
    }

    private void handlePbrLoadDone(Message msg) {
        log("Loading PBR done");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null) {
            createPbrFile((ArrayList) ar.result);
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleUsimAdnLoadDone(Message msg) {
        log("handleUsimAdnLoadDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        int pbrIndex = ((Integer) ar.userObj).intValue();
        if (ar.exception == null) {
            try {
                this.mPhoneBookRecords.addAll((ArrayList) ar.result);
                while (pbrIndex > this.mAdnLengthList.size()) {
                    log("add empty item,pbrIndex=" + pbrIndex + " mAdnLengthList.size=" + this.mAdnLengthList.size());
                    this.mAdnLengthList.add(0);
                }
                this.mAdnLengthList.set(pbrIndex, Integer.valueOf(((ArrayList) ar.result).size()));
                putValidRecNums(pbrIndex);
            } catch (Exception e) {
                log("Interrupted Exception in getAdnRecordsSizeAndWait");
            }
        } else {
            log("can't load USIM ADN records");
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleIapLoadDone(Message msg) {
        log("handleIapLoadDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        int pbrIndex = ((Integer) ar.userObj).intValue();
        if (ar.exception == null) {
            this.mIapFileRecord.put(Integer.valueOf(pbrIndex), (ArrayList) ar.result);
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleEmailLoadDone(Message msg) {
        log("handleEmailLoadDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        int pbrIndex = ((Integer) ar.userObj).intValue();
        if (ar.exception == null && this.mPbrFile != null) {
            ArrayList<byte[]> tmpList = this.mEmailFileRecord.get(Integer.valueOf(pbrIndex));
            if (tmpList == null) {
                this.mEmailFileRecord.put(Integer.valueOf(pbrIndex), (ArrayList) ar.result);
            } else {
                tmpList.addAll((ArrayList) ar.result);
                this.mEmailFileRecord.put(Integer.valueOf(pbrIndex), tmpList);
            }
            log("handlemessage EVENT_EMAIL_LOAD_DONE size is: " + this.mEmailFileRecord.get(Integer.valueOf(pbrIndex)).size());
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handeAnrLoadDone(Message msg) {
        log("handeAnrLoadDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        int pbrIndex = ((Integer) ar.userObj).intValue();
        if (ar.exception == null && this.mPbrFile != null) {
            ArrayList<byte[]> tmp = this.mAnrFileRecord.get(Integer.valueOf(pbrIndex));
            if (tmp == null) {
                this.mAnrFileRecord.put(Integer.valueOf(pbrIndex), (ArrayList) ar.result);
            } else {
                tmp.addAll((ArrayList) ar.result);
                this.mAnrFileRecord.put(Integer.valueOf(pbrIndex), tmp);
            }
            log("handlemessage EVENT_ANR_LOAD_DONE size is: " + this.mAnrFileRecord.get(Integer.valueOf(pbrIndex)).size());
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleEfEmailRecordSizeDone(Message msg) {
        Message message = msg;
        log("handleEfEmailRecordSizeDone");
        AsyncResult ar = (AsyncResult) message.obj;
        int adnRecIndex = message.arg1 - 1;
        int efid = message.arg2;
        String[] email = ((String) ar.userObj).split(",");
        String oldEmail = null;
        String newEmail = null;
        if (email.length == 1) {
            oldEmail = email[0];
            newEmail = "";
        } else if (email.length > 1) {
            oldEmail = email[0];
            newEmail = email[1];
        }
        String oldEmail2 = oldEmail;
        String newEmail2 = newEmail;
        if (ar.exception != null) {
            this.mSuccess = false;
            synchronized (this.mLock) {
                this.mLock.notify();
            }
            return;
        }
        int[] recordSize = (int[]) ar.result;
        int recordNumber = getEmailRecNumber(adnRecIndex, this.mPhoneBookRecords.size(), oldEmail2);
        if (recordSize.length != 3 || recordNumber > recordSize[2]) {
            int[] iArr = recordSize;
        } else if (recordNumber <= 0) {
            int i = recordNumber;
            int[] iArr2 = recordSize;
        } else {
            byte[] data = buildEmailData(recordSize[0], adnRecIndex, newEmail2);
            int actualRecNumber = recordNumber;
            if (!this.mEmailPresentInIap) {
                int efidIndex = this.mPbrFile.mEmailFileIds.get(Integer.valueOf(getPbrIndexBy(adnRecIndex))).indexOf(Integer.valueOf(efid));
                if (efidIndex == -1) {
                    log("wrong efid index:" + efid);
                    return;
                }
                actualRecNumber = recordNumber + (this.mAdnLengthList.get(getPbrIndexBy(adnRecIndex)).intValue() * efidIndex);
                log("EMAIL index:" + efidIndex + " efid:" + efid + " actual RecNumber:" + actualRecNumber);
            }
            int actualRecNumber2 = actualRecNumber;
            int i2 = actualRecNumber2;
            int i3 = recordNumber;
            int[] iArr3 = recordSize;
            this.mFh.updateEFLinearFixed(efid, recordNumber, data, null, obtainMessage(8, actualRecNumber2, adnRecIndex, data));
            return;
        }
        this.mSuccess = false;
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleEfAnrRecordSizeDone(Message msg) {
        Message message = msg;
        log("handleEfAnrRecordSizeDone");
        AsyncResult ar = (AsyncResult) message.obj;
        int adnRecIndex = message.arg1 - 1;
        int efid = message.arg2;
        String[] anr = ((String) ar.userObj).split(",");
        String oldAnr = null;
        String newAnr = null;
        if (anr.length == 1) {
            oldAnr = anr[0];
            newAnr = "";
        } else if (anr.length > 1) {
            oldAnr = anr[0];
            newAnr = anr[1];
        }
        String oldAnr2 = oldAnr;
        String newAnr2 = newAnr;
        if (ar.exception != null) {
            this.mSuccess = false;
            synchronized (this.mLock) {
                this.mLock.notify();
            }
            return;
        }
        int[] recordSize = (int[]) ar.result;
        int recordNumber = getAnrRecNumber(adnRecIndex, this.mPhoneBookRecords.size(), oldAnr2);
        if (recordSize.length != 3 || recordNumber > recordSize[2]) {
            int[] iArr = recordSize;
        } else if (recordNumber <= 0) {
            int i = recordNumber;
            int[] iArr2 = recordSize;
        } else {
            byte[] data = buildAnrData(recordSize[0], adnRecIndex, newAnr2);
            if (data == null) {
                this.mSuccess = false;
                synchronized (this.mLock) {
                    this.mLock.notify();
                }
                return;
            }
            int actualRecNumber = recordNumber;
            if (!this.mAnrPresentInIap) {
                int efidIndex = this.mPbrFile.mAnrFileIds.get(Integer.valueOf(getPbrIndexBy(adnRecIndex))).indexOf(Integer.valueOf(efid));
                if (efidIndex == -1) {
                    log("wrong efid index:" + efid);
                    return;
                }
                actualRecNumber = recordNumber + (this.mAdnLengthList.get(getPbrIndexBy(adnRecIndex)).intValue() * efidIndex);
                log("ANR index:" + efidIndex + " efid:" + efid + " actual RecNumber:" + actualRecNumber);
            }
            byte[] bArr = data;
            int i2 = recordNumber;
            int[] iArr3 = recordSize;
            this.mFh.updateEFLinearFixed(efid, recordNumber, data, null, obtainMessage(9, actualRecNumber, adnRecIndex, data));
            return;
        }
        this.mSuccess = false;
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleUpdateEmailRecordDone(Message msg) {
        log("handleUpdateEmailRecordDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception != null) {
            this.mSuccess = false;
        }
        byte[] data = (byte[]) ar.userObj;
        int recordNumber = msg.arg1;
        int pbrIndex = getPbrIndexBy(msg.arg2);
        log("EVENT_UPDATE_EMAIL_RECORD_DONE");
        this.mSuccess = true;
        if (hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
            this.mEmailFileRecord.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, data);
            int i = 0;
            while (true) {
                if (i >= data.length) {
                    break;
                }
                log("EVENT_UPDATE_EMAIL_RECORD_DONE data = " + data[i] + ",i is " + i);
                if (data[i] != -1) {
                    log("EVENT_UPDATE_EMAIL_RECORD_DONE data !=0xff");
                    this.mEmailFlags.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, 1);
                    break;
                }
                this.mEmailFlags.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, 0);
                i++;
            }
        } else {
            log("Email record is empty");
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleUpdateAnrRecordDone(Message msg) {
        log("handleUpdateAnrRecordDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        byte[] data = (byte[]) ar.userObj;
        int recordNumber = msg.arg1;
        int pbrIndex = getPbrIndexBy(msg.arg2);
        if (ar.exception != null) {
            this.mSuccess = false;
        }
        log("EVENT_UPDATE_ANR_RECORD_DONE");
        this.mSuccess = true;
        if (hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            this.mAnrFileRecord.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, data);
            int i = 0;
            while (true) {
                if (i >= data.length) {
                    break;
                } else if (data[i] != -1) {
                    this.mAnrFlags.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, 1);
                    break;
                } else {
                    this.mAnrFlags.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, 0);
                    i++;
                }
            }
        } else {
            log("Anr record is empty");
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleEfIapRecordSizeDone(Message msg) {
        Message message = msg;
        log("handleEfIapRecordSizeDone");
        AsyncResult ar = (AsyncResult) message.obj;
        int recordNumber = message.arg2;
        int adnRecIndex = message.arg1 - 1;
        int pbrIndexBy = getPbrIndexBy(adnRecIndex);
        int tag = ((Integer) ar.userObj).intValue();
        if (ar.exception != null) {
            this.mSuccess = false;
            synchronized (this.mLock) {
                this.mLock.notify();
            }
            return;
        }
        int pbrIndex = getPbrIndexBy(adnRecIndex);
        int efid = getEfidByTag(pbrIndex, USIM_EFIAP_TAG, 0);
        int[] recordSize = (int[]) ar.result;
        int recordIndex = adnRecIndex - getInitIndexBy(pbrIndex);
        log("handleIAP_RECORD_SIZE_DONE adnRecIndex is: " + adnRecIndex + ", recordNumber is: " + recordNumber + ", recordIndex is: " + recordIndex);
        if (isIapRecordParamInvalid(recordSize, recordIndex, recordNumber)) {
            this.mSuccess = false;
            synchronized (this.mLock) {
                this.mLock.notify();
            }
            return;
        }
        if (hasRecordIn(this.mIapFileRecord, pbrIndex)) {
            byte[] data = (byte[]) this.mIapFileRecord.get(Integer.valueOf(pbrIndex)).get(recordIndex);
            byte[] recordData = new byte[data.length];
            System.arraycopy(data, 0, recordData, 0, recordData.length);
            if (tag == USIM_EFANR_TAG) {
                recordData[this.mAnrTagNumberInIap] = (byte) recordNumber;
            } else if (tag == 202) {
                recordData[this.mEmailTagNumberInIap] = (byte) recordNumber;
            }
            log(" IAP  efid= " + efid + ", update IAP index= " + recordIndex + " with value= " + IccUtils.bytesToHexString(recordData));
            byte[] bArr = recordData;
            int i = recordIndex;
            int[] iArr = recordSize;
            this.mFh.updateEFLinearFixed(efid, recordIndex + 1, recordData, null, obtainMessage(11, adnRecIndex, recordNumber, recordData));
        } else {
            int[] iArr2 = recordSize;
        }
    }

    private void handleUpdateIapRecordDone(Message msg) {
        log("handleUpdateIapRecordDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception != null) {
            this.mSuccess = false;
        }
        byte[] data = (byte[]) ar.userObj;
        int adnRecIndex = msg.arg1;
        int pbrIndex = getPbrIndexBy(adnRecIndex);
        int recordIndex = adnRecIndex - getInitIndexBy(pbrIndex);
        log("handleUpdateIapRecordDone recordIndex is: " + recordIndex + ", adnRecIndex is: " + adnRecIndex);
        this.mSuccess = true;
        if (hasRecordIn(this.mIapFileRecord, pbrIndex)) {
            this.mIapFileRecord.get(Integer.valueOf(pbrIndex)).set(recordIndex, data);
            log("Iap record is added");
        } else {
            log("Iap record is empty");
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleGetSizeDone(Message msg) {
        log("handleGetSizeDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        synchronized (this.mLock) {
            if (ar.exception == null) {
                this.mRecordSize = (int[]) ar.result;
                log("GET_RECORD_SIZE Size " + this.mRecordSize[0] + " total " + this.mRecordSize[1] + " #record " + this.mRecordSize[2]);
            }
            this.mLock.notify();
        }
    }

    private void handleExt1LoadDone(Message msg) {
        log("handleExt1LoadDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        int pbrIndex = msg.arg1;
        int efid = msg.arg2;
        if (ar.exception == null) {
            ArrayList<byte[]> tmp = this.mExt1FileRecord.get(Integer.valueOf(pbrIndex));
            if (tmp == null) {
                this.mExt1FileRecord.put(Integer.valueOf(pbrIndex), (ArrayList) ar.result);
            } else {
                tmp.addAll((ArrayList) ar.result);
                this.mExt1FileRecord.put(Integer.valueOf(pbrIndex), tmp);
            }
            log("handleExt1LoadDone size is: " + this.mExt1FileRecord.get(Integer.valueOf(pbrIndex)).size());
            ArrayList<byte[]> ext1FileArray = this.mExt1FileRecord.get(Integer.valueOf(pbrIndex));
            if (ext1FileArray != null) {
                if (this.mExt1FlagsRecord == null) {
                    this.mExt1FlagsRecord = new ArrayList[(pbrIndex + 1)];
                    this.mExt1FlagsRecord[pbrIndex] = new ArrayList<>();
                }
                if (pbrIndex < this.mExt1FlagsRecord.length) {
                    int ext1FileArraySize = ext1FileArray.size();
                    for (int m = 0; m < ext1FileArraySize; m++) {
                        this.mExt1FlagsRecord[pbrIndex].add(0);
                    }
                }
            }
            this.mExt1Flags.put(Integer.valueOf(pbrIndex), this.mExt1FlagsRecord[pbrIndex]);
            if (efid == 28474) {
                updateExt1RecordFlagsForSim(pbrIndex);
            } else {
                updateExt1RecordFlags(pbrIndex);
            }
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private void handleEfExt1RecordSizeDone(Message msg) {
        String newExt1;
        Message message = msg;
        log("handleEfExt1RecordSizeDone");
        AsyncResult ar = (AsyncResult) message.obj;
        AdnRecord newAdnRecord = (AdnRecord) ar.userObj;
        String mNumber = newAdnRecord.getNumber();
        int adnRecIndex = message.arg1 - 1;
        int efid = message.arg2;
        if (ar.exception != null) {
            this.mSuccess = false;
            synchronized (this.mLock) {
                this.mLock.notify();
            }
            return;
        }
        int[] recordSize = (int[]) ar.result;
        int recordNumber = newAdnRecord.getExtRecord();
        if (recordSize.length != 3 || recordNumber > recordSize[2] || recordNumber <= 0) {
            this.mSuccess = false;
            synchronized (this.mLock) {
                this.mLock.notify();
            }
            return;
        }
        if (mNumber.length() > 20) {
            newExt1 = mNumber.substring(20);
        } else {
            newExt1 = "";
            newAdnRecord.setExtRecord(HwSubscriptionManager.SUB_INIT_STATE);
        }
        byte[] data = buildExt1Data(recordSize[0], adnRecIndex, newExt1);
        byte[] bArr = data;
        this.mFh.updateEFLinearFixed(efid, recordNumber, data, null, obtainMessage(14, recordNumber, adnRecIndex, data));
    }

    private void handleUpdateExt1RecordDone(Message msg) {
        log("handleUpdateExt1RecordDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception != null) {
            this.mSuccess = false;
            synchronized (this.mLock) {
                this.mLock.notify();
            }
            return;
        }
        byte[] data = (byte[]) ar.userObj;
        int recordNumber = msg.arg1;
        int pbrIndex = getPbrIndexBy(msg.arg2);
        this.mSuccess = true;
        if (hasRecordIn(this.mExt1FileRecord, pbrIndex)) {
            this.mExt1FileRecord.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, data);
            int i = 0;
            while (true) {
                if (i < data.length) {
                    if (data[i] != -1 && data[i] != 0) {
                        log("EVENT_UPDATE_EXT1_RECORD_DONE data !=0xff and 0x00");
                        this.mExt1Flags.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, 1);
                        break;
                    }
                    this.mExt1Flags.get(Integer.valueOf(pbrIndex)).set(recordNumber - 1, 0);
                    i++;
                } else {
                    break;
                }
            }
        } else {
            log("Ext1 record is empty");
        }
        synchronized (this.mLock) {
            this.mLock.notify();
        }
    }

    private boolean isIapRecordParamInvalid(int[] recordSize, int recordIndex, int recordNumber) {
        return 3 != recordSize.length || recordIndex + 1 > recordSize[2] || recordIndex < 0 || recordNumber == 0;
    }

    /* access modifiers changed from: private */
    public void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public int getAnrCount() {
        int count = 0;
        if (this.mAnrPresentInIap && hasRecordIn(this.mIapFileRecord, 0)) {
            try {
                byte[] record = (byte[]) this.mIapFileRecord.get(0).get(0);
                if (record != null && this.mAnrTagNumberInIap >= record.length) {
                    log("getAnrCount mAnrTagNumberInIap: " + this.mAnrTagNumberInIap + " len:" + record.length);
                    return 0;
                }
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "Error: getAnrCount ICC card: No IAP record for ADN, continuing");
                return 0;
            }
        }
        int pbrIndex = this.mAnrFlags.size();
        for (int j = 0; j < pbrIndex; j++) {
            count += this.mAnrFlags.get(Integer.valueOf(j)).size();
        }
        log("getAnrCount count is: " + count);
        return count;
    }

    public int getEmailCount() {
        int count = 0;
        if (this.mEmailPresentInIap && hasRecordIn(this.mIapFileRecord, 0)) {
            try {
                byte[] record = (byte[]) this.mIapFileRecord.get(0).get(0);
                if (record != null && this.mEmailTagNumberInIap >= record.length) {
                    log("getEmailCount mEmailTagNumberInIap: " + this.mEmailTagNumberInIap + " len:" + record.length);
                    return 0;
                }
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "Error: getEmailCount ICC card: No IAP record for ADN, continuing");
                return 0;
            }
        }
        int pbrIndex = this.mEmailFlags.size();
        for (int j = 0; j < pbrIndex; j++) {
            count += this.mEmailFlags.get(Integer.valueOf(j)).size();
        }
        log("getEmailCount count is: " + count);
        return count;
    }

    public int getSpareAnrCount() {
        int pbrIndex = this.mAnrFlags.size();
        int count = 0;
        int j = 0;
        while (j < pbrIndex) {
            int anrFlagSize = 0;
            if (this.mAnrFlags.get(Integer.valueOf(j)) != null) {
                anrFlagSize = this.mAnrFlags.get(Integer.valueOf(j)).size();
            }
            int count2 = count;
            for (int i = 0; i < anrFlagSize; i++) {
                if (((Integer) this.mAnrFlags.get(Integer.valueOf(j)).get(i)).intValue() == 0) {
                    count2++;
                }
            }
            j++;
            count = count2;
        }
        log("getSpareAnrCount count is" + count);
        return count;
    }

    public int getSpareEmailCount() {
        int pbrIndex = this.mEmailFlags.size();
        int count = 0;
        int j = 0;
        while (j < pbrIndex) {
            int emailFlagSize = 0;
            if (this.mEmailFlags.get(Integer.valueOf(j)) != null) {
                emailFlagSize = this.mEmailFlags.get(Integer.valueOf(j)).size();
            }
            int count2 = count;
            for (int i = 0; i < emailFlagSize; i++) {
                if (((Integer) this.mEmailFlags.get(Integer.valueOf(j)).get(i)).intValue() == 0) {
                    count2++;
                }
            }
            j++;
            count = count2;
        }
        log("getSpareEmailCount count is: " + count);
        return count;
    }

    public int getUsimAdnCount() {
        if (this.mPhoneBookRecords == null || this.mPhoneBookRecords.isEmpty()) {
            return 0;
        }
        log("getUsimAdnCount count is" + this.mPhoneBookRecords.size());
        return this.mPhoneBookRecords.size();
    }

    public int getEmptyEmailNum_Pbrindex(int pbrindex) {
        int count = 0;
        if (!this.mEmailPresentInIap) {
            return 1;
        }
        if (this.mEmailFlags.containsKey(Integer.valueOf(pbrindex))) {
            int size = this.mEmailFlags.get(Integer.valueOf(pbrindex)).size();
            for (int i = 0; i < size; i++) {
                if (((Integer) this.mEmailFlags.get(Integer.valueOf(pbrindex)).get(i)).intValue() == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getEmptyAnrNum_Pbrindex(int pbrindex) {
        int count = 0;
        if (!this.mAnrPresentInIap) {
            return 1;
        }
        if (this.mAnrFlags.containsKey(Integer.valueOf(pbrindex))) {
            int size = this.mAnrFlags.get(Integer.valueOf(pbrindex)).size();
            for (int i = 0; i < size; i++) {
                if (((Integer) this.mAnrFlags.get(Integer.valueOf(pbrindex)).get(i)).intValue() == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getEmailFilesCountEachAdn() {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from getEmailFilesCountEachAdn");
            return 0;
        }
        Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(0);
        if (fileIds == null || !fileIds.containsKey(202)) {
            return 0;
        }
        if (!this.mEmailPresentInIap) {
            return this.mPbrFile.mEmailFileIds.get(0).size();
        }
        return 1;
    }

    public int getAnrFilesCountEachAdn() {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from getAnrFilesCountEachAdn");
            return 0;
        }
        Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(0);
        if (fileIds == null || !fileIds.containsKey(Integer.valueOf(USIM_EFANR_TAG))) {
            return 0;
        }
        if (!this.mAnrPresentInIap) {
            return this.mPbrFile.mAnrFileIds.get(0).size();
        }
        return 1;
    }

    public int getAdnRecordsFreeSize() {
        int freeRecs = 0;
        log("getAdnRecordsFreeSize(): enter.");
        int totalRecs = getUsimAdnCount();
        if (totalRecs != 0) {
            for (int i = 0; i < totalRecs; i++) {
                if (this.mPhoneBookRecords.get(i).isEmpty()) {
                    freeRecs++;
                }
            }
        } else {
            log("getAdnRecordsFreeSize(): error. ");
        }
        log("getAdnRecordsFreeSize(): freeRecs = " + freeRecs);
        return freeRecs;
    }

    public ArrayList<AdnRecord> getPhonebookRecords() {
        if (!this.mPhoneBookRecords.isEmpty()) {
            return this.mPhoneBookRecords;
        }
        return null;
    }

    public void setIccFileHandler(IccFileHandler fh) {
        this.mFh = fh;
    }

    public int[] getAdnRecordsSizeFromEF() {
        synchronized (this.mLock) {
            if (!this.mIsPbrPresent.booleanValue()) {
                return null;
            }
            if (this.mPbrFile == null) {
                readPbrFileAndWait();
            }
            if (this.mPbrFile == null) {
                return null;
            }
            int numRecs = this.mPbrFile.mFileIds.size();
            this.temRecordSize[0] = 0;
            this.temRecordSize[1] = 0;
            this.temRecordSize[2] = 0;
            for (int i = 0; i < numRecs; i++) {
                this.mRecordSize[0] = 0;
                this.mRecordSize[1] = 0;
                this.mRecordSize[2] = 0;
                getAdnRecordsSizeAndWait(i);
                Rlog.d(LOG_TAG, "getAdnRecordsSizeFromEF: recordSize[2]=" + this.mRecordSize[2]);
                if (this.mRecordSize[0] != 0) {
                    this.temRecordSize[0] = this.mRecordSize[0];
                }
                if (this.mRecordSize[1] != 0) {
                    this.temRecordSize[1] = this.mRecordSize[1];
                }
                this.temRecordSize[2] = this.mRecordSize[2] + this.temRecordSize[2];
            }
            Rlog.d(LOG_TAG, "getAdnRecordsSizeFromEF: temRecordSize[2]=" + this.temRecordSize[2]);
            int[] iArr = this.temRecordSize;
            return iArr;
        }
    }

    public void getAdnRecordsSizeAndWait(int recNum) {
        if (this.mPbrFile != null) {
            Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
            if (fileIds != null && !fileIds.isEmpty()) {
                int efid = fileIds.get(Integer.valueOf(USIM_EFADN_TAG)).intValue();
                Rlog.d(LOG_TAG, "getAdnRecordsSize: efid=" + efid);
                this.mFh.getEFLinearRecordSize(efid, obtainMessage(EVENT_GET_SIZE_DONE));
                boolean isWait = true;
                while (isWait) {
                    try {
                        this.mLock.wait();
                        isWait = false;
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in getAdnRecordsSizeAndWait");
                    }
                }
            }
        }
    }

    public int getPbrFileSize() {
        int size = 0;
        if (!(this.mPbrFile == null || this.mPbrFile.mFileIds == null)) {
            size = this.mPbrFile.mFileIds.size();
        }
        log("getPbrFileSize:" + size);
        return size;
    }

    public int getEFidInPBR(int recNum, int tag) {
        int efid = 0;
        if (this.mPbrFile == null) {
            return 0;
        }
        Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds == null) {
            return 0;
        }
        if (fileIds.containsKey(Integer.valueOf(tag))) {
            efid = fileIds.get(Integer.valueOf(tag)).intValue();
        }
        log("getEFidInPBR, efid = " + efid + ", recNum = " + recNum + ", tag = " + tag);
        return efid;
    }

    private void initExt1FileRecordAndFlags() {
        this.mExt1FileRecord = new HashMap();
        this.mExt1Flags = new HashMap();
    }

    private void resetExt1Variables() {
        int i = 0;
        if (this.mExt1FlagsRecord != null && this.mPbrFile != null) {
            while (true) {
                int i2 = i;
                if (i2 >= this.mExt1FlagsRecord.length) {
                    break;
                }
                this.mExt1FlagsRecord[i2].clear();
                i = i2 + 1;
            }
        } else if (this.mExt1FlagsRecord != null && this.mPbrFile == null) {
            this.mExt1FlagsRecord[0].clear();
        }
        this.mExt1Flags.clear();
        this.mExt1FileRecord.clear();
    }

    private void loadExt1FilesFromUsim(int numRecs) {
        this.mExt1FlagsRecord = new ArrayList[numRecs];
        for (int i = 0; i < numRecs; i++) {
            this.mExt1FlagsRecord[i] = new ArrayList<>();
        }
        for (int i2 = 0; i2 < numRecs; i2++) {
            readExt1FileAndWait(i2);
        }
    }

    private void readExt1FileAndWait(int recNum) {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from readExt1FileAndWait");
            return;
        }
        Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds == null || fileIds.isEmpty()) {
            Rlog.e(LOG_TAG, "fileIds is NULL, exiting from readExt1FileAndWait");
            return;
        }
        if (fileIds.containsKey(Integer.valueOf(USIM_EFEXT1_TAG))) {
            this.mFh.loadEFLinearFixedAll(fileIds.get(Integer.valueOf(USIM_EFEXT1_TAG)).intValue(), obtainMessage(12, recNum, fileIds.get(Integer.valueOf(USIM_EFEXT1_TAG)).intValue()));
            log("readExt1FileAndWait EXT1 efid is : " + fileIds.get(Integer.valueOf(USIM_EFEXT1_TAG)));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
            }
        }
    }

    private void updateExt1RecordFlags(int pbrIndex) {
        if (hasRecordIn(this.mExt1FileRecord, pbrIndex) && hasRecordIn(this.mAdnLengthList, pbrIndex)) {
            int numAdnRecs = this.mAdnLengthList.get(pbrIndex).intValue();
            for (int i = 0; i < numAdnRecs; i++) {
                AdnRecord rec = this.mPhoneBookRecords.get(getInitIndexBy(pbrIndex) + i);
                if (rec != null && rec.getExtRecord() != 255 && rec.getExtRecord() > 0 && rec.getExtRecord() <= this.mExt1Flags.get(Integer.valueOf(pbrIndex)).size()) {
                    this.mExt1Flags.get(Integer.valueOf(pbrIndex)).set(rec.getExtRecord() - 1, 1);
                }
            }
            int extRecsSize = this.mExt1FileRecord.get(Integer.valueOf(pbrIndex)).size();
            for (int index = 0; index < extRecsSize; index++) {
                if (1 != ((Integer) this.mExt1Flags.get(Integer.valueOf(pbrIndex)).get(index)).intValue()) {
                    byte[] extRec = (byte[]) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex)).get(index);
                    String extRecord = readExt1Record(pbrIndex, index, 0);
                    if (extRec != null && extRec.length > 0 && extRec[0] == 2 && "".equals(extRecord)) {
                        for (int i2 = 0; i2 < extRec.length; i2++) {
                            extRec[i2] = -1;
                        }
                    }
                }
            }
            log("updateExt1RecordFlags done");
        }
    }

    public void readExt1FileForSim(int efid) {
        if (efid == 28474) {
            this.mFh.loadEFLinearFixedAll(28490, obtainMessage(12, 0, efid));
            log("readExt1FileForSim Ext1 efid is : 28490");
        }
    }

    private void updateExt1RecordFlagsForSim(int recNum) {
        this.mPhoneBookRecords = this.mAdnCache.getAdnFilesForSim();
        int numAdnRecs = this.mPhoneBookRecords.size();
        if (this.mAdnLengthList.size() == 0) {
            this.mAdnLengthList.add(0);
        }
        this.mAdnLengthList.set(recNum, Integer.valueOf(numAdnRecs));
        for (int i = 0; i < numAdnRecs; i++) {
            AdnRecord rec = this.mPhoneBookRecords.get(i);
            if (rec != null && rec.getExtRecord() != 255 && rec.getExtRecord() > 0 && rec.getExtRecord() <= this.mExt1Flags.get(Integer.valueOf(recNum)).size()) {
                this.mExt1Flags.get(Integer.valueOf(recNum)).set(rec.getExtRecord() - 1, 1);
            }
        }
        int extRecsSize = this.mExt1FileRecord.get(Integer.valueOf(recNum)).size();
        for (int index = 0; index < extRecsSize; index++) {
            if (1 != ((Integer) this.mExt1Flags.get(Integer.valueOf(recNum)).get(index)).intValue()) {
                byte[] extRec = (byte[]) this.mExt1FileRecord.get(Integer.valueOf(recNum)).get(index);
                String extRecord = readExt1Record(recNum, index, 0);
                if (extRec != null && extRec.length > 0 && extRec[0] == 2 && "".equals(extRecord)) {
                    for (int i2 = 0; i2 < extRec.length; i2++) {
                        extRec[i2] = -1;
                    }
                }
            }
        }
        log("updateExt1RecordFlags done");
    }

    public boolean updateExt1File(int adnRecNum, AdnRecord oldAdnRecord, AdnRecord newAdnRecord, int tagOrEfid) {
        int efid;
        int pbrIndex = getPbrIndexBy(adnRecNum - 1);
        String oldNumber = oldAdnRecord.getNumber();
        String newNumber = newAdnRecord.getNumber();
        this.mSuccess = false;
        if (!IccRecords.getAdnLongNumberSupport()) {
            this.mSuccess = true;
            return this.mSuccess;
        }
        log("updateExt1File adnRecNum: " + adnRecNum);
        if (oldNumber == null || newNumber == null || oldNumber.length() > 20 || newNumber.length() > 20) {
            if (tagOrEfid == USIM_EFEXT1_TAG) {
                if (this.mPbrFile == null || this.mPbrFile.mFileIds == null) {
                    Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from updateExt1File");
                    return this.mSuccess;
                }
                Map<Integer, Integer> fileIds = this.mPbrFile.mFileIds.get(Integer.valueOf(pbrIndex));
                if (fileIds == null) {
                    return this.mSuccess;
                }
                if (!fileIds.containsKey(Integer.valueOf(tagOrEfid))) {
                    return this.mSuccess;
                }
                efid = fileIds.get(Integer.valueOf(tagOrEfid)).intValue();
            } else if (tagOrEfid != 28490) {
                return this.mSuccess;
            } else {
                efid = tagOrEfid;
            }
            int efid2 = efid;
            if (oldAdnRecord.getExtRecord() == 255 && !TextUtils.isEmpty(newNumber)) {
                int recNum = getExt1RecNumber(adnRecNum);
                if (recNum == -1) {
                    return this.mSuccess;
                }
                newAdnRecord.setExtRecord(recNum);
                log("Index Number in Ext is " + recNum);
            }
            synchronized (this.mLock) {
                this.mFh.getEFLinearRecordSize(efid2, obtainMessage(13, adnRecNum, efid2, newAdnRecord));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "interrupted while trying to update by search");
                }
            }
            return this.mSuccess;
        }
        this.mSuccess = true;
        return this.mSuccess;
    }

    private String readExt1Record(int pbrIndex, int recNum, int offset) {
        if (!hasRecordIn(this.mExt1FileRecord, pbrIndex)) {
            return null;
        }
        try {
            byte[] extRec = (byte[]) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex)).get(recNum + offset);
            if (extRec == null) {
                return "";
            }
            if (extRec.length != 13) {
                return "";
            }
            if ((extRec[0] & 255) == 0) {
                return "";
            }
            int numberLength = extRec[1] & HwSubscriptionManager.SUB_INIT_STATE;
            if (numberLength > 10) {
                return "";
            }
            return PhoneNumberUtils.calledPartyBCDFragmentToString(extRec, 2, numberLength);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private byte[] buildExt1Data(int length, int adnRecIndex, String ext) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = -1;
        }
        data[0] = 0;
        if (TextUtils.isEmpty(ext) || length != 13) {
            log("[buildExtData] Empty ext1 record");
            return data;
        }
        byte[] byteExt = PhoneNumberUtils.numberToCalledPartyBCD(ext);
        if (byteExt == null) {
            return data;
        }
        data[0] = 2;
        if (byteExt.length > 11) {
            System.arraycopy(byteExt, 1, data, 2, 10);
            data[1] = 10;
        } else {
            System.arraycopy(byteExt, 1, data, 2, byteExt.length - 1);
            data[1] = (byte) (byteExt.length - 1);
        }
        return data;
    }

    private int getExt1RecNumber(int adnRecIndex) {
        int pbrIndex = getPbrIndexBy(adnRecIndex - 1);
        log("getExt1RecNumber adnRecIndex is: " + adnRecIndex);
        if (!hasRecordIn(this.mExt1FileRecord, pbrIndex)) {
            return -1;
        }
        int extRecordNumber = this.mPhoneBookRecords.get(adnRecIndex - 1).getExtRecord();
        if (extRecordNumber != 255 && extRecordNumber > 0 && extRecordNumber <= this.mExt1FileRecord.get(Integer.valueOf(pbrIndex)).size()) {
            return extRecordNumber;
        }
        int recordSize = this.mExt1FileRecord.get(Integer.valueOf(pbrIndex)).size();
        log("ext record Size: " + recordSize);
        for (int i = 0; i < recordSize; i++) {
            if (TextUtils.isEmpty(readExt1Record(pbrIndex, i, 0))) {
                return i + 1;
            }
        }
        return -1;
    }

    public int getExt1Count() {
        int count = 0;
        int pbrIndex = this.mExt1Flags.size();
        for (int j = 0; j < pbrIndex; j++) {
            count += this.mExt1Flags.get(Integer.valueOf(j)).size();
        }
        log("getExt1Count count is: " + count);
        return count;
    }

    public int getSpareExt1Count() {
        int pbrIndex = this.mExt1Flags.size();
        int count = 0;
        int j = 0;
        while (j < pbrIndex) {
            int extFlagsSize = this.mExt1Flags.get(Integer.valueOf(j)).size();
            int count2 = count;
            for (int i = 0; i < extFlagsSize; i++) {
                if (((Integer) this.mExt1Flags.get(Integer.valueOf(j)).get(i)).intValue() == 0) {
                    count2++;
                }
            }
            j++;
            count = count2;
        }
        log("getSpareExt1Count count is: " + count);
        return count;
    }

    private int getRecNumber(int i, int pbrIndex, int tagNumber) {
        try {
            byte[] record = (byte[]) this.mIapFileRecord.get(Integer.valueOf(pbrIndex)).get(i);
            try {
                return record[tagNumber];
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithAnr: IndexOutOfBoundsException mAnrTagNumberInIap: " + this.mAnrTagNumberInIap + " tagNumber: " + tagNumber + " len:" + record.length);
                return -1;
            }
        } catch (IndexOutOfBoundsException e2) {
            Rlog.e(LOG_TAG, "Error: Improper ICC card: No IAP record for ADN, continuing");
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public void syncDataFromTagInParseEf(int parentTag, int tag, int tagNumberWithinParentTag) {
        if (!this.mEmailPresentInIap && parentTag == USIM_TYPE2_TAG && this.mIapPresent && tag == 202) {
            this.mEmailPresentInIap = true;
            this.mEmailTagNumberInIap = tagNumberWithinParentTag;
            log("parseEf: EmailPresentInIap tag = " + this.mEmailTagNumberInIap);
        }
        if (!this.mAnrPresentInIap && parentTag == USIM_TYPE2_TAG && this.mIapPresent && tag == USIM_EFANR_TAG) {
            this.mAnrPresentInIap = true;
            this.mAnrTagNumberInIap = tagNumberWithinParentTag;
            log("parseEf: AnrPresentInIap tag = " + this.mAnrTagNumberInIap);
        }
    }
}
