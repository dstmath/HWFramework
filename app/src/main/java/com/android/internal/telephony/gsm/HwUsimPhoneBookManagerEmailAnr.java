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
    private static final int ANR_TON_NPI_ID = 2;
    private static final int DATA_DESCRIPTION_ID_IN_EFEXT1 = 2;
    private static final boolean DBG = true;
    private static final int EVENT_ANR_LOAD_DONE = 5;
    private static final int EVENT_EF_ANR_RECORD_SIZE_DONE = 7;
    private static final int EVENT_EF_EMAIL_RECORD_SIZE_DONE = 6;
    private static final int EVENT_EF_EXT1_RECORD_SIZE_DONE = 13;
    private static final int EVENT_EF_IAP_RECORD_SIZE_DONE = 10;
    private static final int EVENT_EMAIL_LOAD_DONE = 4;
    private static final int EVENT_EXT1_LOAD_DONE = 12;
    protected static final int EVENT_GET_SIZE_DONE = 101;
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
    private static final int MAX_NUMBER_DATA_SIZE_EFEXT1 = 10;
    private static final int MAX_NUMBER_SIZE_BYTES = 11;
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
    private ArrayList<Integer> mAdnLengthList;
    private Map<Integer, ArrayList<byte[]>> mAnrFileRecord;
    private Map<Integer, ArrayList<Integer>> mAnrFlags;
    private ArrayList<Integer>[] mAnrFlagsRecord;
    private boolean mAnrPresentInIap;
    private int mAnrTagNumberInIap;
    private Map<Integer, ArrayList<byte[]>> mEmailFileRecord;
    private Map<Integer, ArrayList<Integer>> mEmailFlags;
    private ArrayList<Integer>[] mEmailFlagsRecord;
    private boolean mEmailPresentInIap;
    private int mEmailTagNumberInIap;
    private Map<Integer, ArrayList<byte[]>> mExt1FileRecord;
    private Map<Integer, ArrayList<Integer>> mExt1Flags;
    private ArrayList<Integer>[] mExt1FlagsRecord;
    private IccFileHandler mFh;
    private Map<Integer, ArrayList<byte[]>> mIapFileRecord;
    private boolean mIapPresent;
    private Boolean mIsPbrPresent;
    private Object mLock;
    private PbrFile mPbrFile;
    private ArrayList<AdnRecord> mPhoneBookRecords;
    private Map<Integer, ArrayList<Integer>> mRecordNums;
    private int[] mRecordSize;
    private boolean mRefreshCache;
    private boolean mSuccess;
    private int[] temRecordSize;

    private class PbrFile {
        boolean isInvalidAnrType;
        boolean isInvalidEmailType;
        boolean isNoAnrExist;
        boolean isNoEmailExist;
        HashMap<Integer, ArrayList<Integer>> mAnrFileIds;
        HashMap<Integer, ArrayList<Integer>> mEmailFileIds;
        HashMap<Integer, Map<Integer, Integer>> mFileIds;

        PbrFile(ArrayList<byte[]> records) {
            this.isInvalidEmailType = false;
            this.isInvalidAnrType = false;
            this.isNoEmailExist = false;
            this.isNoAnrExist = false;
            this.mFileIds = new HashMap();
            this.mAnrFileIds = new HashMap();
            this.mEmailFileIds = new HashMap();
            int recNum = HwUsimPhoneBookManagerEmailAnr.FREE_TAG_IN_EFEXT1;
            for (byte[] record : records) {
                Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "before making TLVs, data is " + IccUtils.bytesToHexString(record));
                if (!(record == null || IccUtils.bytesToHexString(record).startsWith("ffff"))) {
                    SimTlv recTlv = new SimTlv(record, HwUsimPhoneBookManagerEmailAnr.FREE_TAG_IN_EFEXT1, record.length);
                    if (recTlv.isValidObject()) {
                        parseTag(recTlv, recNum);
                        if (this.mFileIds.get(Integer.valueOf(recNum)) != null) {
                            recNum += HwUsimPhoneBookManagerEmailAnr.LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                        }
                    } else {
                        Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "null == recTlv || !recTlv.isValidObject() is true");
                    }
                }
            }
        }

        void parseTag(SimTlv tlv, int recNum) {
            Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseTag: recNum=xxxxxx");
            HwUsimPhoneBookManagerEmailAnr.this.mIapPresent = false;
            Map<Integer, Integer> val = new HashMap();
            ArrayList<Integer> anrList = new ArrayList();
            ArrayList<Integer> emailList = new ArrayList();
            do {
                int tag = tlv.getTag();
                switch (tag) {
                    case HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG /*168*/:
                    case HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG /*169*/:
                    case HwUsimPhoneBookManagerEmailAnr.USIM_TYPE3_TAG /*170*/:
                        byte[] data = tlv.getData();
                        if (data != null && data.length != 0) {
                            parseEf(new SimTlv(data, HwUsimPhoneBookManagerEmailAnr.FREE_TAG_IN_EFEXT1, data.length), val, tag, anrList, emailList);
                            break;
                        } else if (tag == HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG) {
                            Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseTag: invalid A8 data, ignore the whole record");
                            return;
                        }
                        break;
                }
            } while (tlv.nextObject());
            if (anrList.size() != 0) {
                this.mAnrFileIds.put(Integer.valueOf(recNum), anrList);
                Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseTag: recNum=xxxxxx ANR file list:" + anrList);
            }
            if (emailList.size() != 0) {
                Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseTag: recNum=xxxxxx EMAIL file list:" + emailList);
                this.mEmailFileIds.put(Integer.valueOf(recNum), emailList);
            }
            this.mFileIds.put(Integer.valueOf(recNum), val);
            if (val.size() != 0) {
                if (!val.containsKey(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFEMAIL_TAG))) {
                    this.isNoEmailExist = HwUsimPhoneBookManagerEmailAnr.DBG;
                }
                if (!val.containsKey(Integer.valueOf(HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG))) {
                    this.isNoAnrExist = HwUsimPhoneBookManagerEmailAnr.DBG;
                }
            }
        }

        void parseEf(SimTlv tlv, Map<Integer, Integer> val, int parentTag, ArrayList<Integer> anrList, ArrayList<Integer> emailList) {
            int tagNumberWithinParentTag = HwUsimPhoneBookManagerEmailAnr.FREE_TAG_IN_EFEXT1;
            do {
                int tag = tlv.getTag();
                if (parentTag == HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG && tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFIAP_TAG) {
                    HwUsimPhoneBookManagerEmailAnr.this.mIapPresent = HwUsimPhoneBookManagerEmailAnr.DBG;
                }
                if (parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG || HwUsimPhoneBookManagerEmailAnr.this.mIapPresent) {
                    if (!HwUsimPhoneBookManagerEmailAnr.this.mEmailPresentInIap && parentTag == HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG && HwUsimPhoneBookManagerEmailAnr.this.mIapPresent && tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFEMAIL_TAG) {
                        HwUsimPhoneBookManagerEmailAnr.this.mEmailPresentInIap = HwUsimPhoneBookManagerEmailAnr.DBG;
                        HwUsimPhoneBookManagerEmailAnr.this.mEmailTagNumberInIap = tagNumberWithinParentTag;
                        HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: EmailPresentInIap tag = " + HwUsimPhoneBookManagerEmailAnr.this.mEmailTagNumberInIap);
                    }
                    if (!HwUsimPhoneBookManagerEmailAnr.this.mAnrPresentInIap && parentTag == HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG && HwUsimPhoneBookManagerEmailAnr.this.mIapPresent && tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG) {
                        HwUsimPhoneBookManagerEmailAnr.this.mAnrPresentInIap = HwUsimPhoneBookManagerEmailAnr.DBG;
                        HwUsimPhoneBookManagerEmailAnr.this.mAnrTagNumberInIap = tagNumberWithinParentTag;
                        HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: AnrPresentInIap tag = " + HwUsimPhoneBookManagerEmailAnr.this.mAnrTagNumberInIap);
                    }
                    switch (tag) {
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFADN_TAG /*192*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFIAP_TAG /*193*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFEXT1_TAG /*194*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFSNE_TAG /*195*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG /*196*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFPBC_TAG /*197*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFGRP_TAG /*198*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFAAS_TAG /*199*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFGSD_TAG /*200*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFUID_TAG /*201*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFEMAIL_TAG /*202*/:
                        case HwUsimPhoneBookManagerEmailAnr.USIM_EFCCP1_TAG /*203*/:
                            byte[] data = tlv.getData();
                            if (data != null && data.length >= HwUsimPhoneBookManagerEmailAnr.EXT_TAG_IN_EFEXT1) {
                                int efid = ((data[HwUsimPhoneBookManagerEmailAnr.FREE_TAG_IN_EFEXT1] & HwSubscriptionManager.SUB_INIT_STATE) << HwUsimPhoneBookManagerEmailAnr.EVENT_UPDATE_EMAIL_RECORD_DONE) | (data[HwUsimPhoneBookManagerEmailAnr.LENGTH_DESCRIPTION_ID_IN_EFEXT1] & HwSubscriptionManager.SUB_INIT_STATE);
                                if (!val.containsKey(Integer.valueOf(tag))) {
                                    if (!(shouldIgnoreEmail(tag, parentTag) || shouldIgnoreAnr(tag, parentTag))) {
                                        val.put(Integer.valueOf(tag), Integer.valueOf(efid));
                                        if (parentTag == HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG) {
                                            if (tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG) {
                                                anrList.add(Integer.valueOf(efid));
                                            } else if (tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFEMAIL_TAG) {
                                                emailList.add(Integer.valueOf(efid));
                                            }
                                        }
                                        Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "parseEf.put(" + tag + "," + efid + ") parent tag:" + parentTag);
                                        break;
                                    }
                                }
                                Rlog.d(HwUsimPhoneBookManagerEmailAnr.LOG_TAG, "already have (" + tag + "," + efid + ") parent tag:" + parentTag);
                                break;
                            }
                    }
                    tagNumberWithinParentTag += HwUsimPhoneBookManagerEmailAnr.LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                }
            } while (tlv.nextObject());
        }

        boolean shouldIgnoreEmail(int tag, int parentTag) {
            if (tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFEMAIL_TAG && (this.isInvalidEmailType || (parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG && parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG))) {
                HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: invalid Email type!");
                this.isInvalidEmailType = HwUsimPhoneBookManagerEmailAnr.DBG;
                return HwUsimPhoneBookManagerEmailAnr.DBG;
            } else if (tag != HwUsimPhoneBookManagerEmailAnr.USIM_EFEMAIL_TAG || !this.isNoEmailExist) {
                return false;
            } else {
                HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: isNoEmailExist");
                return HwUsimPhoneBookManagerEmailAnr.DBG;
            }
        }

        boolean shouldIgnoreAnr(int tag, int parentTag) {
            if (tag == HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG && (this.isInvalidAnrType || (parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE1_TAG && parentTag != HwUsimPhoneBookManagerEmailAnr.USIM_TYPE2_TAG))) {
                HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: invalid Anr type!");
                this.isInvalidAnrType = HwUsimPhoneBookManagerEmailAnr.DBG;
                return HwUsimPhoneBookManagerEmailAnr.DBG;
            } else if (tag != HwUsimPhoneBookManagerEmailAnr.USIM_EFANR_TAG || !this.isNoAnrExist) {
                return false;
            } else {
                HwUsimPhoneBookManagerEmailAnr.this.log("parseEf: isNoAnrExist");
                return HwUsimPhoneBookManagerEmailAnr.DBG;
            }
        }
    }

    public HwUsimPhoneBookManagerEmailAnr(IccFileHandler fh) {
        super(fh, null);
        this.mLock = new Object();
        this.mEmailPresentInIap = false;
        this.mEmailTagNumberInIap = FREE_TAG_IN_EFEXT1;
        this.mAnrPresentInIap = false;
        this.mAnrTagNumberInIap = FREE_TAG_IN_EFEXT1;
        this.mIapPresent = false;
        this.mAdnLengthList = null;
        this.mSuccess = false;
        this.mRefreshCache = false;
        this.mRecordSize = new int[EVENT_IAP_LOAD_DONE];
        this.temRecordSize = new int[EVENT_IAP_LOAD_DONE];
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList();
        this.mPbrFile = null;
        this.mIsPbrPresent = Boolean.valueOf(DBG);
    }

    public HwUsimPhoneBookManagerEmailAnr(IccFileHandler fh, AdnRecordCache cache) {
        super(fh, cache);
        this.mLock = new Object();
        this.mEmailPresentInIap = false;
        this.mEmailTagNumberInIap = FREE_TAG_IN_EFEXT1;
        this.mAnrPresentInIap = false;
        this.mAnrTagNumberInIap = FREE_TAG_IN_EFEXT1;
        this.mIapPresent = false;
        this.mAdnLengthList = null;
        this.mSuccess = false;
        this.mRefreshCache = false;
        this.mRecordSize = new int[EVENT_IAP_LOAD_DONE];
        this.temRecordSize = new int[EVENT_IAP_LOAD_DONE];
        this.mFh = fh;
        this.mPhoneBookRecords = new ArrayList();
        this.mAdnLengthList = new ArrayList();
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
        this.mIsPbrPresent = Boolean.valueOf(DBG);
        this.mAdnCache = cache;
    }

    public void reset() {
        if (!(this.mAnrFlagsRecord == null || this.mEmailFlagsRecord == null || this.mPbrFile == null)) {
            for (int i = FREE_TAG_IN_EFEXT1; i < this.mPbrFile.mFileIds.size(); i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                this.mAnrFlagsRecord[i].clear();
                this.mEmailFlagsRecord[i].clear();
            }
        }
        if (IccRecords.getAdnLongNumberSupport()) {
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
        this.mIsPbrPresent = Boolean.valueOf(DBG);
        this.mRefreshCache = false;
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
                if (this.mPbrFile == null) {
                    readPbrFileAndWait();
                }
                if (this.mPbrFile == null) {
                    return null;
                }
                int i;
                int numRecs = this.mPbrFile.mFileIds.size();
                if (this.mAnrFlagsRecord == null && this.mEmailFlagsRecord == null) {
                    this.mAnrFlagsRecord = new ArrayList[numRecs];
                    this.mEmailFlagsRecord = new ArrayList[numRecs];
                    for (i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                        this.mAnrFlagsRecord[i] = new ArrayList();
                        this.mEmailFlagsRecord[i] = new ArrayList();
                    }
                }
                if (this.mAdnLengthList != null && this.mAdnLengthList.size() == 0) {
                    for (i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                        this.mAdnLengthList.add(Integer.valueOf(FREE_TAG_IN_EFEXT1));
                    }
                }
                for (i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    readAdnFileAndWait(i);
                    readEmailFileAndWait(i);
                    readAnrFileAndWait(i);
                }
                if (IccRecords.getAdnLongNumberSupport()) {
                    loadExt1FilesFromUsim(numRecs);
                }
                return this.mPhoneBookRecords;
            } else {
                return null;
            }
        }
    }

    private void refreshCache() {
        if (this.mPbrFile != null) {
            this.mPhoneBookRecords.clear();
            int numRecs = this.mPbrFile.mFileIds.size();
            for (int i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                readAdnFileAndWait(i);
            }
        }
    }

    public void invalidateCache() {
        this.mRefreshCache = DBG;
    }

    private void readPbrFileAndWait() {
        this.mFh.loadEFLinearFixedAll(20272, obtainMessage(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
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
        Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds != null && fileIds.containsKey(Integer.valueOf(USIM_EFEMAIL_TAG))) {
            if (this.mEmailPresentInIap) {
                if (fileIds.containsKey(Integer.valueOf(USIM_EFIAP_TAG))) {
                    readIapFileAndWait(((Integer) fileIds.get(Integer.valueOf(USIM_EFIAP_TAG))).intValue(), recNum);
                } else {
                    log("fileIds don't contain USIM_EFIAP_TAG");
                }
                if (hasRecordIn(this.mIapFileRecord, recNum)) {
                    this.mFh.loadEFLinearFixedAll(((Integer) fileIds.get(Integer.valueOf(USIM_EFEMAIL_TAG))).intValue(), obtainMessage(EVENT_EMAIL_LOAD_DONE, Integer.valueOf(recNum)));
                    log("readEmailFileAndWait email efid is : " + fileIds.get(Integer.valueOf(USIM_EFEMAIL_TAG)));
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                    }
                } else {
                    Rlog.e(LOG_TAG, "Error: IAP file is empty");
                    return;
                }
            }
            for (Integer intValue : (ArrayList) this.mPbrFile.mEmailFileIds.get(Integer.valueOf(recNum))) {
                int efid = intValue.intValue();
                this.mFh.loadEFLinearFixedPartHW(efid, getValidRecordNums(recNum), obtainMessage(EVENT_EMAIL_LOAD_DONE, Integer.valueOf(recNum)));
                log("readEmailFileAndWait email efid is : " + efid + " recNum:" + recNum);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                }
            }
            ArrayList<byte[]> emailFileArray = (ArrayList) this.mEmailFileRecord.get(Integer.valueOf(recNum));
            if (emailFileArray != null) {
                for (int m = FREE_TAG_IN_EFEXT1; m < emailFileArray.size(); m += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    this.mEmailFlagsRecord[recNum].add(Integer.valueOf(FREE_TAG_IN_EFEXT1));
                }
            }
            this.mEmailFlags.put(Integer.valueOf(recNum), this.mEmailFlagsRecord[recNum]);
            if (hasRecordIn(this.mEmailFileRecord, recNum)) {
                updatePhoneAdnRecordWithEmail(recNum);
            } else {
                Rlog.e(LOG_TAG, "Error: Email file is empty");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readAnrFileAndWait(int recNum) {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from readAnrFileAndWait");
            return;
        }
        Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (!(fileIds == null || fileIds.isEmpty() || !fileIds.containsKey(Integer.valueOf(USIM_EFANR_TAG)))) {
            if (this.mAnrPresentInIap) {
                if (fileIds.containsKey(Integer.valueOf(USIM_EFIAP_TAG))) {
                    readIapFileAndWait(((Integer) fileIds.get(Integer.valueOf(USIM_EFIAP_TAG))).intValue(), recNum);
                } else {
                    log("fileIds don't contain USIM_EFIAP_TAG");
                }
                if (hasRecordIn(this.mIapFileRecord, recNum)) {
                    this.mFh.loadEFLinearFixedAll(((Integer) fileIds.get(Integer.valueOf(USIM_EFANR_TAG))).intValue(), obtainMessage(EVENT_ANR_LOAD_DONE, Integer.valueOf(recNum)));
                    log("readAnrFileAndWait anr efid is : " + fileIds.get(Integer.valueOf(USIM_EFANR_TAG)));
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                    }
                } else {
                    Rlog.e(LOG_TAG, "Error: IAP file is empty");
                    return;
                }
            }
            for (Integer intValue : (ArrayList) this.mPbrFile.mAnrFileIds.get(Integer.valueOf(recNum))) {
                int efid = intValue.intValue();
                this.mFh.loadEFLinearFixedPartHW(efid, getValidRecordNums(recNum), obtainMessage(EVENT_ANR_LOAD_DONE, Integer.valueOf(recNum)));
                log("readAnrFileAndWait anr efid is : " + efid + " recNum:" + recNum);
                try {
                    this.mLock.wait();
                } catch (InterruptedException e2) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
                }
            }
            ArrayList<byte[]> anrFileArray = (ArrayList) this.mAnrFileRecord.get(Integer.valueOf(recNum));
            if (anrFileArray != null) {
                for (int m = FREE_TAG_IN_EFEXT1; m < anrFileArray.size(); m += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    this.mAnrFlagsRecord[recNum].add(Integer.valueOf(FREE_TAG_IN_EFEXT1));
                }
            }
            this.mAnrFlags.put(Integer.valueOf(recNum), this.mAnrFlagsRecord[recNum]);
            if (hasRecordIn(this.mAnrFileRecord, recNum)) {
                updatePhoneAdnRecordWithAnr(recNum);
            } else {
                Rlog.e(LOG_TAG, "Error: Anr file is empty");
            }
        }
    }

    private void readIapFileAndWait(int efid, int recNum) {
        log("pbrIndex is " + recNum + ",iap efid is : " + efid);
        this.mFh.loadEFLinearFixedPartHW(efid, getValidRecordNums(recNum), obtainMessage(EVENT_IAP_LOAD_DONE, Integer.valueOf(recNum)));
        try {
            this.mLock.wait();
        } catch (InterruptedException e) {
            Rlog.e(LOG_TAG, "Interrupted Exception in readIapFileAndWait");
        }
    }

    public boolean updateEmailFile(int adnRecNum, String oldEmail, String newEmail, int efidIndex) {
        int pbrIndex = getPbrIndexBy(adnRecNum - 1);
        int efid = getEfidByTag(pbrIndex, USIM_EFEMAIL_TAG, efidIndex);
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
            this.mSuccess = DBG;
        } else if (getEmptyEmailNum_Pbrindex(pbrIndex) == 0) {
            log("updateEmailFile getEmptyEmailNum_Pbrindex=0, pbrIndex is " + pbrIndex);
            this.mSuccess = false;
            return this.mSuccess;
        } else {
            this.mSuccess = updateIapFile(adnRecNum, oldEmail, newEmail, USIM_EFEMAIL_TAG);
        }
        if (this.mSuccess) {
            synchronized (this.mLock) {
                this.mFh.getEFLinearRecordSize(efid, obtainMessage(EVENT_EF_EMAIL_RECORD_SIZE_DONE, adnRecNum, efid, emails));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "interrupted while trying to update by search");
                }
            }
        }
        if (this.mEmailPresentInIap && this.mSuccess && !TextUtils.isEmpty(oldEmail) && TextUtils.isEmpty(newEmail)) {
            this.mSuccess = updateIapFile(adnRecNum, oldEmail, newEmail, USIM_EFEMAIL_TAG);
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
            this.mSuccess = DBG;
        } else if (getEmptyAnrNum_Pbrindex(pbrIndex) == 0) {
            log("updateAnrFile getEmptyAnrNum_Pbrindex=0, pbrIndex is " + pbrIndex);
            this.mSuccess = false;
            return this.mSuccess;
        } else {
            this.mSuccess = updateIapFile(adnRecNum, oldAnr, newAnr, USIM_EFANR_TAG);
        }
        synchronized (this.mLock) {
            this.mFh.getEFLinearRecordSize(efid, obtainMessage(EVENT_EF_ANR_RECORD_SIZE_DONE, adnRecNum, efid, anrs));
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
        int efid = getEfidByTag(getPbrIndexBy(adnRecNum - 1), USIM_EFIAP_TAG, FREE_TAG_IN_EFEXT1);
        this.mSuccess = false;
        int recordNumber = -1;
        if (efid == -1) {
            return this.mSuccess;
        }
        switch (tag) {
            case USIM_EFANR_TAG /*196*/:
                recordNumber = getAnrRecNumber(adnRecNum - 1, this.mPhoneBookRecords.size(), oldValue);
                break;
            case USIM_EFEMAIL_TAG /*202*/:
                recordNumber = getEmailRecNumber(adnRecNum - 1, this.mPhoneBookRecords.size(), oldValue);
                break;
        }
        if (TextUtils.isEmpty(newValue)) {
            recordNumber = -1;
        }
        log("updateIapFile  efid=" + efid + ", recordNumber= " + recordNumber + ", adnRecNum=" + adnRecNum);
        synchronized (this.mLock) {
            this.mFh.getEFLinearRecordSize(efid, obtainMessage(MAX_NUMBER_DATA_SIZE_EFEXT1, adnRecNum, recordNumber, Integer.valueOf(tag)));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "interrupted while trying to update by search");
            }
        }
        return this.mSuccess;
    }

    private int getEfidByTag(int recNum, int tag, int efidIndex) {
        if (this.mPbrFile == null || this.mPbrFile.mFileIds == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from getEfidByTag");
            return -1;
        }
        Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds == null || !fileIds.containsKey(Integer.valueOf(tag))) {
            return -1;
        }
        int efid;
        if (!this.mEmailPresentInIap && USIM_EFEMAIL_TAG == tag) {
            efid = ((Integer) ((ArrayList) this.mPbrFile.mEmailFileIds.get(Integer.valueOf(recNum))).get(efidIndex)).intValue();
        } else if (this.mAnrPresentInIap || USIM_EFANR_TAG != tag) {
            efid = ((Integer) fileIds.get(Integer.valueOf(tag))).intValue();
        } else {
            efid = ((Integer) ((ArrayList) this.mPbrFile.mAnrFileIds.get(Integer.valueOf(recNum))).get(efidIndex)).intValue();
        }
        return efid;
    }

    public int getPbrIndexBy(int adnIndex) {
        int len = this.mAdnLengthList.size();
        int size = FREE_TAG_IN_EFEXT1;
        for (int i = FREE_TAG_IN_EFEXT1; i < len; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            size += ((Integer) this.mAdnLengthList.get(i)).intValue();
            if (adnIndex < size) {
                return i;
            }
        }
        return -1;
    }

    public int getPbrIndexByEfid(int efid) {
        if (!(this.mPbrFile == null || this.mPbrFile.mFileIds == null)) {
            for (int i = FREE_TAG_IN_EFEXT1; i < this.mPbrFile.mFileIds.size(); i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                Map<Integer, Integer> val = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(i));
                if (val != null && val.containsValue(Integer.valueOf(efid))) {
                    return i;
                }
            }
        }
        return FREE_TAG_IN_EFEXT1;
    }

    public int getInitIndexByPbr(int pbrIndex) {
        return getInitIndexBy(pbrIndex);
    }

    private int getInitIndexBy(int pbrIndex) {
        int index = FREE_TAG_IN_EFEXT1;
        while (pbrIndex > 0) {
            index += ((Integer) this.mAdnLengthList.get(pbrIndex - 1)).intValue();
            pbrIndex--;
        }
        return index;
    }

    private boolean hasRecordIn(Map<Integer, ArrayList<byte[]>> record, int pbrIndex) {
        if (record == null || record.isEmpty()) {
            return false;
        }
        try {
            if (record.get(Integer.valueOf(pbrIndex)) == null) {
                return false;
            }
            return DBG;
        } catch (IndexOutOfBoundsException e) {
            Rlog.e(LOG_TAG, "record is empty in pbrIndex" + pbrIndex);
            return false;
        }
    }

    private void updatePhoneAdnRecordWithEmail(int pbrIndex) {
        if (hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
            int numAdnRecs = ((Integer) this.mAdnLengthList.get(pbrIndex)).intValue();
            if (this.mEmailPresentInIap) {
                if (hasRecordIn(this.mIapFileRecord, pbrIndex)) {
                    int i = FREE_TAG_IN_EFEXT1;
                    while (i < numAdnRecs) {
                        try {
                            byte[] record = (byte[]) ((ArrayList) this.mIapFileRecord.get(Integer.valueOf(pbrIndex))).get(i);
                            try {
                                int recNum = record[this.mEmailTagNumberInIap];
                                if (recNum > 0) {
                                    String[] emails = new String[LENGTH_DESCRIPTION_ID_IN_EFEXT1];
                                    emails[FREE_TAG_IN_EFEXT1] = readEmailRecord(recNum - 1, pbrIndex, FREE_TAG_IN_EFEXT1);
                                    int adnRecIndex = i + getInitIndexBy(pbrIndex);
                                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnRecIndex);
                                    if (!(rec == null || TextUtils.isEmpty(emails[FREE_TAG_IN_EFEXT1]))) {
                                        rec.setEmails(emails);
                                        this.mPhoneBookRecords.set(adnRecIndex, rec);
                                        ((ArrayList) this.mEmailFlags.get(Integer.valueOf(pbrIndex))).set(recNum - 1, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                                    }
                                }
                                i += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                            } catch (IndexOutOfBoundsException e) {
                                int i2 = this.mEmailTagNumberInIap;
                                Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithEmail: IndexOutOfBoundsException mEmailTagNumberInIap: " + r0 + " len:" + record.length);
                            }
                        } catch (IndexOutOfBoundsException e2) {
                            Rlog.e(LOG_TAG, "Error: Improper ICC card: No IAP record for ADN, continuing");
                        }
                    }
                    int emailRecsSize = ((ArrayList) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex))).size();
                    for (int index = FREE_TAG_IN_EFEXT1; index < emailRecsSize; index += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                        if (LENGTH_DESCRIPTION_ID_IN_EFEXT1 != ((Integer) ((ArrayList) this.mEmailFlags.get(Integer.valueOf(pbrIndex))).get(index)).intValue()) {
                            if (!"".equals(readEmailRecord(index, pbrIndex, FREE_TAG_IN_EFEXT1))) {
                                byte[] emailRec = (byte[]) ((ArrayList) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex))).get(index);
                                i = FREE_TAG_IN_EFEXT1;
                                while (true) {
                                    int length = emailRec.length;
                                    if (i >= r0) {
                                        break;
                                    }
                                    emailRec[i] = (byte) -1;
                                    i += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                                }
                            }
                        }
                    }
                    log("updatePhoneAdnRecordWithEmail: no need to parse type1 EMAIL file");
                    return;
                }
            }
            int len = ((Integer) this.mAdnLengthList.get(pbrIndex)).intValue();
            if (!this.mEmailPresentInIap) {
                parseType1EmailFile(len, pbrIndex);
            }
        }
    }

    private void updatePhoneAdnRecordWithAnr(int pbrIndex) {
        if (hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            int numAdnRecs = ((Integer) this.mAdnLengthList.get(pbrIndex)).intValue();
            if (this.mAnrPresentInIap) {
                if (hasRecordIn(this.mIapFileRecord, pbrIndex)) {
                    int i = FREE_TAG_IN_EFEXT1;
                    while (i < numAdnRecs) {
                        try {
                            byte[] record = (byte[]) ((ArrayList) this.mIapFileRecord.get(Integer.valueOf(pbrIndex))).get(i);
                            try {
                                int recNum = record[this.mAnrTagNumberInIap];
                                if (recNum > 0) {
                                    String[] anrs = new String[LENGTH_DESCRIPTION_ID_IN_EFEXT1];
                                    anrs[FREE_TAG_IN_EFEXT1] = readAnrRecord(recNum - 1, pbrIndex, FREE_TAG_IN_EFEXT1);
                                    int adnRecIndex = i + getInitIndexBy(pbrIndex);
                                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(adnRecIndex);
                                    if (!(rec == null || TextUtils.isEmpty(anrs[FREE_TAG_IN_EFEXT1]))) {
                                        rec.setAdditionalNumbers(anrs);
                                        this.mPhoneBookRecords.set(adnRecIndex, rec);
                                        ((ArrayList) this.mAnrFlags.get(Integer.valueOf(pbrIndex))).set(recNum - 1, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                                    }
                                }
                                i += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                            } catch (IndexOutOfBoundsException e) {
                                int i2 = this.mAnrTagNumberInIap;
                                Rlog.e(LOG_TAG, "updatePhoneAdnRecordWithAnr: IndexOutOfBoundsException mAnrTagNumberInIap: " + r0 + " len:" + record.length);
                            }
                        } catch (IndexOutOfBoundsException e2) {
                            Rlog.e(LOG_TAG, "Error: Improper ICC card: No IAP record for ADN, continuing");
                        }
                    }
                    int anrRecsSize = ((ArrayList) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex))).size();
                    for (int index = FREE_TAG_IN_EFEXT1; index < anrRecsSize; index += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                        if (LENGTH_DESCRIPTION_ID_IN_EFEXT1 != ((Integer) ((ArrayList) this.mAnrFlags.get(Integer.valueOf(pbrIndex))).get(index)).intValue()) {
                            if (!"".equals(readAnrRecord(index, pbrIndex, FREE_TAG_IN_EFEXT1))) {
                                byte[] anrRec = (byte[]) ((ArrayList) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex))).get(index);
                                for (i = FREE_TAG_IN_EFEXT1; i < anrRec.length; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                                    anrRec[i] = (byte) -1;
                                }
                            }
                        }
                    }
                    log("updatePhoneAdnRecordWithAnr: no need to parse type1 ANR file");
                    return;
                }
            }
            if (!this.mAnrPresentInIap) {
                parseType1AnrFile(numAdnRecs, pbrIndex);
            }
        }
    }

    void parseType1EmailFile(int numRecs, int pbrIndex) {
        int numEmailFiles = ((ArrayList) this.mPbrFile.mEmailFileIds.get(Integer.valueOf(pbrIndex))).size();
        ArrayList<String> emailList = new ArrayList();
        int adnInitIndex = getInitIndexBy(pbrIndex);
        if (hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
            log("parseType1EmailFile: pbrIndex is: " + pbrIndex + ", numRecs is: " + numRecs);
            for (int i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                int count = FREE_TAG_IN_EFEXT1;
                emailList.clear();
                for (int j = FREE_TAG_IN_EFEXT1; j < numEmailFiles; j += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    String email = readEmailRecord(i, pbrIndex, j * numRecs);
                    emailList.add(email);
                    if (TextUtils.isEmpty(email)) {
                        email = "";
                    } else {
                        count += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                        ((ArrayList) this.mEmailFlags.get(Integer.valueOf(pbrIndex))).set((j * numRecs) + i, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                    }
                }
                if (count != 0) {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i + adnInitIndex);
                    if (rec != null) {
                        String[] emails = new String[emailList.size()];
                        System.arraycopy(emailList.toArray(), FREE_TAG_IN_EFEXT1, emails, FREE_TAG_IN_EFEXT1, emailList.size());
                        rec.setEmails(emails);
                        this.mPhoneBookRecords.set(i + adnInitIndex, rec);
                    }
                }
            }
        }
    }

    void parseType1AnrFile(int numRecs, int pbrIndex) {
        int numAnrFiles = ((ArrayList) this.mPbrFile.mAnrFileIds.get(Integer.valueOf(pbrIndex))).size();
        ArrayList<String> anrList = new ArrayList();
        int adnInitIndex = getInitIndexBy(pbrIndex);
        if (hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            log("parseType1AnrFile: pbrIndex is: " + pbrIndex + ", numRecs is: " + numRecs + ", numAnrFiles " + numAnrFiles);
            for (int i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                int count = FREE_TAG_IN_EFEXT1;
                anrList.clear();
                for (int j = FREE_TAG_IN_EFEXT1; j < numAnrFiles; j += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    String anr = readAnrRecord(i, pbrIndex, j * numRecs);
                    anrList.add(anr);
                    if (TextUtils.isEmpty(anr)) {
                        anr = "";
                    } else {
                        count += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                        ((ArrayList) this.mAnrFlags.get(Integer.valueOf(pbrIndex))).set((j * numRecs) + i, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                    }
                }
                if (count != 0) {
                    AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i + adnInitIndex);
                    if (rec != null) {
                        String[] anrs = new String[anrList.size()];
                        System.arraycopy(anrList.toArray(), FREE_TAG_IN_EFEXT1, anrs, FREE_TAG_IN_EFEXT1, anrList.size());
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
            byte[] emailRec = (byte[]) ((ArrayList) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex))).get(recNum + offSet);
            return IccUtils.adnStringFieldToString(emailRec, FREE_TAG_IN_EFEXT1, emailRec.length - 2);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private String readAnrRecord(int recNum, int pbrIndex, int offSet) {
        if (!hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            return null;
        }
        try {
            byte[] anrRec = (byte[]) ((ArrayList) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex))).get(recNum + offSet);
            int numberLength = anrRec[LENGTH_DESCRIPTION_ID_IN_EFEXT1] & HwSubscriptionManager.SUB_INIT_STATE;
            if (numberLength > MAX_NUMBER_SIZE_BYTES) {
                return "";
            }
            return PhoneNumberUtils.calledPartyBCDToString(anrRec, EXT_TAG_IN_EFEXT1, numberLength);
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
        Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds != null && !fileIds.isEmpty()) {
            int extEf = FREE_TAG_IN_EFEXT1;
            if (fileIds.containsKey(Integer.valueOf(USIM_EFEXT1_TAG))) {
                extEf = ((Integer) fileIds.get(Integer.valueOf(USIM_EFEXT1_TAG))).intValue();
            }
            log("readAdnFileAndWait adn efid is : " + fileIds.get(Integer.valueOf(USIM_EFADN_TAG)));
            if (fileIds.containsKey(Integer.valueOf(USIM_EFADN_TAG))) {
                this.mAdnCache.requestLoadAllAdnLike(((Integer) fileIds.get(Integer.valueOf(USIM_EFADN_TAG))).intValue(), extEf, obtainMessage(EXT_TAG_IN_EFEXT1, Integer.valueOf(recNum)));
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
                }
            }
        }
    }

    private int getEmailRecNumber(int adnRecIndex, int numRecs, String oldEmail) {
        int pbrIndex = getPbrIndexBy(adnRecIndex);
        int recordIndex = adnRecIndex - getInitIndexBy(pbrIndex);
        log("getEmailRecNumber adnRecIndex is: " + adnRecIndex + ", recordIndex is :" + recordIndex);
        if (!hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
            log("getEmailRecNumber recordNumber is: " + -1);
            return -1;
        } else if (!this.mEmailPresentInIap || !hasRecordIn(this.mIapFileRecord, pbrIndex)) {
            return recordIndex + LENGTH_DESCRIPTION_ID_IN_EFEXT1;
        } else {
            byte[] record = null;
            try {
                record = (byte[]) ((ArrayList) this.mIapFileRecord.get(Integer.valueOf(pbrIndex))).get(recordIndex);
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "IndexOutOfBoundsException in getEmailRecNumber");
            }
            if (record == null || this.mEmailTagNumberInIap >= record.length || record[this.mEmailTagNumberInIap] == -1 || (record[this.mEmailTagNumberInIap] & HwSubscriptionManager.SUB_INIT_STATE) <= 0 || (record[this.mEmailTagNumberInIap] & HwSubscriptionManager.SUB_INIT_STATE) > ((ArrayList) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex))).size()) {
                int recsSize = ((ArrayList) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex))).size();
                log("getEmailRecNumber recsSize is: " + recsSize);
                if (TextUtils.isEmpty(oldEmail)) {
                    for (int i = FREE_TAG_IN_EFEXT1; i < recsSize; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                        if (TextUtils.isEmpty(readEmailRecord(i, pbrIndex, FREE_TAG_IN_EFEXT1))) {
                            log("getEmailRecNumber: Got empty record.Email record num is :" + (i + LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                            return i + LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                        }
                    }
                }
                log("getEmailRecNumber: no email record index found");
                return -1;
            }
            int recordNumber = record[this.mEmailTagNumberInIap] & HwSubscriptionManager.SUB_INIT_STATE;
            log(" getEmailRecNumber: record is " + IccUtils.bytesToHexString(record) + ", the email recordNumber is :" + recordNumber);
            return recordNumber;
        }
    }

    private int getAnrRecNumber(int adnRecIndex, int numRecs, String oldAnr) {
        int pbrIndex = getPbrIndexBy(adnRecIndex);
        int recordIndex = adnRecIndex - getInitIndexBy(pbrIndex);
        if (!hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
            return -1;
        }
        if (!this.mAnrPresentInIap || !hasRecordIn(this.mIapFileRecord, pbrIndex)) {
            return recordIndex + LENGTH_DESCRIPTION_ID_IN_EFEXT1;
        }
        byte[] record = null;
        try {
            record = (byte[]) ((ArrayList) this.mIapFileRecord.get(Integer.valueOf(pbrIndex))).get(recordIndex);
        } catch (IndexOutOfBoundsException e) {
            Rlog.e(LOG_TAG, "IndexOutOfBoundsException in getAnrRecNumber");
        }
        if (record == null || this.mAnrTagNumberInIap >= record.length || record[this.mAnrTagNumberInIap] == -1 || (record[this.mAnrTagNumberInIap] & HwSubscriptionManager.SUB_INIT_STATE) <= 0 || (record[this.mAnrTagNumberInIap] & HwSubscriptionManager.SUB_INIT_STATE) > ((ArrayList) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex))).size()) {
            int recsSize = ((ArrayList) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex))).size();
            log("getAnrRecNumber: anr record size is :" + recsSize);
            if (TextUtils.isEmpty(oldAnr)) {
                for (int i = FREE_TAG_IN_EFEXT1; i < recsSize; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    if (TextUtils.isEmpty(readAnrRecord(i, pbrIndex, FREE_TAG_IN_EFEXT1))) {
                        log("getAnrRecNumber: Empty anr record. Anr record num is :" + (i + LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                        return i + LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                    }
                }
            }
            log("getAnrRecNumber: no anr record index found");
            return -1;
        }
        int recordNumber = record[this.mAnrTagNumberInIap] & HwSubscriptionManager.SUB_INIT_STATE;
        log("getAnrRecNumber: recnum from iap is :" + recordNumber);
        return recordNumber;
    }

    private byte[] buildEmailData(int length, int adnRecIndex, String email) {
        byte[] data = new byte[length];
        for (int i = FREE_TAG_IN_EFEXT1; i < length; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            data[i] = (byte) -1;
        }
        if (TextUtils.isEmpty(email)) {
            log("[buildEmailData] Empty email record");
            return data;
        }
        byte[] byteEmail = GsmAlphabet.stringToGsm8BitPacked(email);
        if (byteEmail.length > data.length) {
            System.arraycopy(byteEmail, FREE_TAG_IN_EFEXT1, data, FREE_TAG_IN_EFEXT1, data.length);
        } else {
            System.arraycopy(byteEmail, FREE_TAG_IN_EFEXT1, data, FREE_TAG_IN_EFEXT1, byteEmail.length);
        }
        int recordIndex = adnRecIndex - getInitIndexBy(getPbrIndexBy(adnRecIndex));
        if (this.mEmailPresentInIap) {
            data[length - 1] = (byte) (recordIndex + LENGTH_DESCRIPTION_ID_IN_EFEXT1);
        }
        return data;
    }

    private byte[] buildAnrData(int length, int adnRecIndex, String anr) {
        byte[] data = new byte[length];
        for (int i = FREE_TAG_IN_EFEXT1; i < length; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            data[i] = (byte) -1;
        }
        if (TextUtils.isEmpty(anr)) {
            log("[buildAnrData] Empty anr record");
            return data;
        }
        data[FREE_TAG_IN_EFEXT1] = (byte) 0;
        byte[] byteAnr = PhoneNumberUtils.numberToCalledPartyBCD(anr);
        if (byteAnr == null) {
            return null;
        }
        if (byteAnr.length > MAX_NUMBER_SIZE_BYTES) {
            System.arraycopy(byteAnr, FREE_TAG_IN_EFEXT1, data, EXT_TAG_IN_EFEXT1, MAX_NUMBER_SIZE_BYTES);
            data[LENGTH_DESCRIPTION_ID_IN_EFEXT1] = (byte) 11;
        } else {
            System.arraycopy(byteAnr, FREE_TAG_IN_EFEXT1, data, EXT_TAG_IN_EFEXT1, byteAnr.length);
            data[LENGTH_DESCRIPTION_ID_IN_EFEXT1] = (byte) byteAnr.length;
        }
        data[EVENT_EF_EXT1_RECORD_SIZE_DONE] = (byte) -1;
        data[EVENT_UPDATE_EXT1_RECORD_DONE] = (byte) -1;
        if (length == 17) {
            data[ANR_ADN_RECORD_IDENTIFIER_ID] = (byte) ((adnRecIndex - getInitIndexBy(getPbrIndexBy(adnRecIndex))) + LENGTH_DESCRIPTION_ID_IN_EFEXT1);
        }
        return data;
    }

    private void createPbrFile(ArrayList<byte[]> records) {
        if (records == null) {
            this.mPbrFile = null;
            this.mIsPbrPresent = Boolean.valueOf(false);
            return;
        }
        this.mPbrFile = new PbrFile(records);
    }

    private void putValidRecNums(int pbrIndex) {
        ArrayList<Integer> recordNums = new ArrayList();
        log("pbr index is " + pbrIndex + ", initAdnIndex is " + getInitIndexBy(pbrIndex));
        for (int i = FREE_TAG_IN_EFEXT1; i < ((Integer) this.mAdnLengthList.get(pbrIndex)).intValue(); i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            recordNums.add(Integer.valueOf(i + LENGTH_DESCRIPTION_ID_IN_EFEXT1));
        }
        if (recordNums.size() == 0) {
            recordNums.add(Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
        }
        this.mRecordNums.put(Integer.valueOf(pbrIndex), recordNums);
    }

    private ArrayList<Integer> getValidRecordNums(int pbrIndex) {
        return (ArrayList) this.mRecordNums.get(Integer.valueOf(pbrIndex));
    }

    public void handleMessage(Message msg) {
        String oldAnr = null;
        String newAnr = null;
        String oldEmail = null;
        String newEmail = null;
        AsyncResult ar;
        Object obj;
        int pbrIndex;
        ArrayList<byte[]> tmp;
        int adnRecIndex;
        int efid;
        int[] recordSize;
        int recordNumber;
        byte[] data;
        int actualRecNumber;
        int efidIndex;
        int i;
        int recordIndex;
        switch (msg.what) {
            case LENGTH_DESCRIPTION_ID_IN_EFEXT1 /*1*/:
                log("Loading PBR done");
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
            case EXT_TAG_IN_EFEXT1 /*2*/:
                log("Loading USIM ADN records done");
                ar = (AsyncResult) msg.obj;
                pbrIndex = ((Integer) ar.userObj).intValue();
                if (ar.exception == null) {
                    try {
                        this.mPhoneBookRecords.addAll((ArrayList) ar.result);
                        while (pbrIndex > this.mAdnLengthList.size()) {
                            log("add empty item,pbrIndex=" + pbrIndex + " mAdnLengthList.size=" + this.mAdnLengthList.size());
                            this.mAdnLengthList.add(Integer.valueOf(FREE_TAG_IN_EFEXT1));
                        }
                        this.mAdnLengthList.set(pbrIndex, Integer.valueOf(((ArrayList) ar.result).size()));
                        putValidRecNums(pbrIndex);
                    } catch (Exception e) {
                        log("Interrupted Exception in getAdnRecordsSizeAndWait");
                    }
                } else {
                    log("can't load USIM ADN records");
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
                pbrIndex = ((Integer) ar.userObj).intValue();
                if (ar.exception == null) {
                    this.mIapFileRecord.put(Integer.valueOf(pbrIndex), (ArrayList) ar.result);
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
                pbrIndex = ((Integer) ar.userObj).intValue();
                if (ar.exception == null && this.mPbrFile != null) {
                    ArrayList<byte[]> tmpList = (ArrayList) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex));
                    if (tmpList == null) {
                        this.mEmailFileRecord.put(Integer.valueOf(pbrIndex), (ArrayList) ar.result);
                    } else {
                        tmpList.addAll((ArrayList) ar.result);
                        this.mEmailFileRecord.put(Integer.valueOf(pbrIndex), tmpList);
                    }
                    log("handlemessage EVENT_EMAIL_LOAD_DONE size is: " + ((ArrayList) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex))).size());
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case EVENT_ANR_LOAD_DONE /*5*/:
                log("Loading USIM Anr records done");
                ar = (AsyncResult) msg.obj;
                pbrIndex = ((Integer) ar.userObj).intValue();
                if (ar.exception == null && this.mPbrFile != null) {
                    tmp = (ArrayList) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex));
                    if (tmp == null) {
                        this.mAnrFileRecord.put(Integer.valueOf(pbrIndex), (ArrayList) ar.result);
                    } else {
                        tmp.addAll((ArrayList) ar.result);
                        this.mAnrFileRecord.put(Integer.valueOf(pbrIndex), tmp);
                    }
                    log("handlemessage EVENT_ANR_LOAD_DONE size is: " + ((ArrayList) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex))).size());
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case EVENT_EF_EMAIL_RECORD_SIZE_DONE /*6*/:
                log("Loading EF_EMAIL_RECORD_SIZE_DONE");
                ar = (AsyncResult) msg.obj;
                String emails = ar.userObj;
                adnRecIndex = msg.arg1 - 1;
                efid = msg.arg2;
                String[] email = emails.split(",");
                if (email.length == LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    oldEmail = email[FREE_TAG_IN_EFEXT1];
                    newEmail = "";
                } else if (email.length > LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    oldEmail = email[FREE_TAG_IN_EFEXT1];
                    newEmail = email[LENGTH_DESCRIPTION_ID_IN_EFEXT1];
                }
                if (ar.exception == null) {
                    recordSize = (int[]) ar.result;
                    recordNumber = getEmailRecNumber(adnRecIndex, this.mPhoneBookRecords.size(), oldEmail);
                    if (recordSize.length == EVENT_IAP_LOAD_DONE && recordNumber <= recordSize[EXT_TAG_IN_EFEXT1] && recordNumber > 0) {
                        data = buildEmailData(recordSize[FREE_TAG_IN_EFEXT1], adnRecIndex, newEmail);
                        actualRecNumber = recordNumber;
                        if (!this.mEmailPresentInIap) {
                            efidIndex = ((ArrayList) this.mPbrFile.mEmailFileIds.get(Integer.valueOf(getPbrIndexBy(adnRecIndex)))).indexOf(Integer.valueOf(efid));
                            if (efidIndex == -1) {
                                log("wrong efid index:" + efid);
                                return;
                            } else {
                                actualRecNumber = recordNumber + (((Integer) this.mAdnLengthList.get(getPbrIndexBy(adnRecIndex))).intValue() * efidIndex);
                                log("EMAIL index:" + efidIndex + " efid:" + efid + " actual RecNumber:" + actualRecNumber);
                            }
                        }
                        this.mFh.updateEFLinearFixed(efid, recordNumber, data, null, obtainMessage(EVENT_UPDATE_EMAIL_RECORD_DONE, actualRecNumber, adnRecIndex, data));
                        break;
                    }
                    this.mSuccess = false;
                    synchronized (this.mLock) {
                        this.mLock.notify();
                        break;
                    }
                    return;
                }
                this.mSuccess = false;
                synchronized (this.mLock) {
                    this.mLock.notify();
                    break;
                }
                return;
            case EVENT_EF_ANR_RECORD_SIZE_DONE /*7*/:
                log("Loading EF_ANR_RECORD_SIZE_DONE");
                ar = (AsyncResult) msg.obj;
                String anrs = ar.userObj;
                adnRecIndex = msg.arg1 - 1;
                efid = msg.arg2;
                String[] anr = anrs.split(",");
                if (anr.length == LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    oldAnr = anr[FREE_TAG_IN_EFEXT1];
                    newAnr = "";
                } else if (anr.length > LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    oldAnr = anr[FREE_TAG_IN_EFEXT1];
                    newAnr = anr[LENGTH_DESCRIPTION_ID_IN_EFEXT1];
                }
                if (ar.exception == null) {
                    recordSize = (int[]) ar.result;
                    recordNumber = getAnrRecNumber(adnRecIndex, this.mPhoneBookRecords.size(), oldAnr);
                    if (recordSize.length == EVENT_IAP_LOAD_DONE && recordNumber <= recordSize[EXT_TAG_IN_EFEXT1] && recordNumber > 0) {
                        data = buildAnrData(recordSize[FREE_TAG_IN_EFEXT1], adnRecIndex, newAnr);
                        if (data != null) {
                            actualRecNumber = recordNumber;
                            if (!this.mAnrPresentInIap) {
                                efidIndex = ((ArrayList) this.mPbrFile.mAnrFileIds.get(Integer.valueOf(getPbrIndexBy(adnRecIndex)))).indexOf(Integer.valueOf(efid));
                                if (efidIndex == -1) {
                                    log("wrong efid index:" + efid);
                                    return;
                                } else {
                                    actualRecNumber = recordNumber + (((Integer) this.mAdnLengthList.get(getPbrIndexBy(adnRecIndex))).intValue() * efidIndex);
                                    log("ANR index:" + efidIndex + " efid:" + efid + " actual RecNumber:" + actualRecNumber);
                                }
                            }
                            this.mFh.updateEFLinearFixed(efid, recordNumber, data, null, obtainMessage(EVENT_UPDATE_ANR_RECORD_DONE, actualRecNumber, adnRecIndex, data));
                            break;
                        }
                        this.mSuccess = false;
                        synchronized (this.mLock) {
                            this.mLock.notify();
                            break;
                        }
                        return;
                    }
                    this.mSuccess = false;
                    synchronized (this.mLock) {
                        this.mLock.notify();
                        break;
                    }
                    return;
                }
                this.mSuccess = false;
                synchronized (this.mLock) {
                    this.mLock.notify();
                    break;
                }
                return;
                break;
            case EVENT_UPDATE_EMAIL_RECORD_DONE /*8*/:
                log("Loading UPDATE_EMAIL_RECORD_DONE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    this.mSuccess = false;
                }
                data = (byte[]) ar.userObj;
                recordNumber = msg.arg1;
                pbrIndex = getPbrIndexBy(msg.arg2);
                log("EVENT_UPDATE_EMAIL_RECORD_DONE");
                this.mSuccess = DBG;
                if (hasRecordIn(this.mEmailFileRecord, pbrIndex)) {
                    ((ArrayList) this.mEmailFileRecord.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, data);
                    i = FREE_TAG_IN_EFEXT1;
                    while (i < data.length) {
                        log("EVENT_UPDATE_EMAIL_RECORD_DONE data = " + data[i] + ",i is " + i);
                        if (data[i] != -1) {
                            log("EVENT_UPDATE_EMAIL_RECORD_DONE data !=0xff");
                            ((ArrayList) this.mEmailFlags.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                        } else {
                            ((ArrayList) this.mEmailFlags.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, Integer.valueOf(FREE_TAG_IN_EFEXT1));
                            i += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                        }
                    }
                } else {
                    log("Email record is empty");
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case EVENT_UPDATE_ANR_RECORD_DONE /*9*/:
                log("Loading UPDATE_ANR_RECORD_DONE");
                ar = (AsyncResult) msg.obj;
                data = (byte[]) ar.userObj;
                recordNumber = msg.arg1;
                pbrIndex = getPbrIndexBy(msg.arg2);
                if (ar.exception != null) {
                    this.mSuccess = false;
                }
                log("EVENT_UPDATE_ANR_RECORD_DONE");
                this.mSuccess = DBG;
                if (hasRecordIn(this.mAnrFileRecord, pbrIndex)) {
                    ((ArrayList) this.mAnrFileRecord.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, data);
                    i = FREE_TAG_IN_EFEXT1;
                    while (i < data.length) {
                        if (data[i] != -1) {
                            ((ArrayList) this.mAnrFlags.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                        } else {
                            ((ArrayList) this.mAnrFlags.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, Integer.valueOf(FREE_TAG_IN_EFEXT1));
                            i += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                        }
                    }
                } else {
                    log("Anr record is empty");
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case MAX_NUMBER_DATA_SIZE_EFEXT1 /*10*/:
                log("EVENT_EF_IAP_RECORD_SIZE_DONE");
                ar = (AsyncResult) msg.obj;
                recordNumber = msg.arg2;
                adnRecIndex = msg.arg1 - 1;
                efid = getEfidByTag(getPbrIndexBy(adnRecIndex), USIM_EFIAP_TAG, FREE_TAG_IN_EFEXT1);
                int tag = ((Integer) ar.userObj).intValue();
                if (ar.exception == null) {
                    pbrIndex = getPbrIndexBy(adnRecIndex);
                    efid = getEfidByTag(pbrIndex, USIM_EFIAP_TAG, FREE_TAG_IN_EFEXT1);
                    recordSize = (int[]) ar.result;
                    recordIndex = adnRecIndex - getInitIndexBy(pbrIndex);
                    log("handleIAP_RECORD_SIZE_DONE adnRecIndex is: " + adnRecIndex + ", recordNumber is: " + recordNumber + ", recordIndex is: " + recordIndex);
                    if (recordSize.length == EVENT_IAP_LOAD_DONE && recordIndex + LENGTH_DESCRIPTION_ID_IN_EFEXT1 <= recordSize[EXT_TAG_IN_EFEXT1] && recordNumber != 0) {
                        if (hasRecordIn(this.mIapFileRecord, pbrIndex)) {
                            data = (byte[]) ((ArrayList) this.mIapFileRecord.get(Integer.valueOf(pbrIndex))).get(recordIndex);
                            byte[] record_data = new byte[data.length];
                            System.arraycopy(data, FREE_TAG_IN_EFEXT1, record_data, FREE_TAG_IN_EFEXT1, record_data.length);
                            switch (tag) {
                                case USIM_EFANR_TAG /*196*/:
                                    record_data[this.mAnrTagNumberInIap] = (byte) recordNumber;
                                    break;
                                case USIM_EFEMAIL_TAG /*202*/:
                                    record_data[this.mEmailTagNumberInIap] = (byte) recordNumber;
                                    break;
                            }
                            log(" IAP  efid= " + efid + ", update IAP index= " + recordIndex + " with value= " + IccUtils.bytesToHexString(record_data));
                            this.mFh.updateEFLinearFixed(efid, recordIndex + LENGTH_DESCRIPTION_ID_IN_EFEXT1, record_data, null, obtainMessage(MAX_NUMBER_SIZE_BYTES, adnRecIndex, recordNumber, record_data));
                            break;
                        }
                    }
                    this.mSuccess = false;
                    synchronized (this.mLock) {
                        this.mLock.notify();
                        break;
                    }
                    return;
                }
                this.mSuccess = false;
                synchronized (this.mLock) {
                    this.mLock.notify();
                    break;
                }
                return;
                break;
            case MAX_NUMBER_SIZE_BYTES /*11*/:
                log("EVENT_UPDATE_IAP_RECORD_DONE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    this.mSuccess = false;
                }
                data = (byte[]) ar.userObj;
                adnRecIndex = msg.arg1;
                pbrIndex = getPbrIndexBy(adnRecIndex);
                recordIndex = adnRecIndex - getInitIndexBy(pbrIndex);
                log("handleMessage EVENT_UPDATE_IAP_RECORD_DONE recordIndex is: " + recordIndex + ", adnRecIndex is: " + adnRecIndex);
                this.mSuccess = DBG;
                if (hasRecordIn(this.mIapFileRecord, pbrIndex)) {
                    ((ArrayList) this.mIapFileRecord.get(Integer.valueOf(pbrIndex))).set(recordIndex, data);
                    log("Iap record is added");
                } else {
                    log("Iap record is empty");
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case EVENT_EXT1_LOAD_DONE /*12*/:
                log("LOAD_EXT1_RECORDS_DONE");
                ar = (AsyncResult) msg.obj;
                pbrIndex = msg.arg1;
                efid = msg.arg2;
                if (ar.exception == null) {
                    tmp = (ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex));
                    if (tmp == null) {
                        this.mExt1FileRecord.put(Integer.valueOf(pbrIndex), (ArrayList) ar.result);
                    } else {
                        tmp.addAll((ArrayList) ar.result);
                        this.mExt1FileRecord.put(Integer.valueOf(pbrIndex), tmp);
                    }
                    log("handlemessage EVENT_EXT1_LOAD_DONE size is: " + ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex))).size());
                    ArrayList<byte[]> ext1FileArray = (ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex));
                    if (ext1FileArray != null) {
                        if (this.mExt1FlagsRecord == null) {
                            this.mExt1FlagsRecord = new ArrayList[(pbrIndex + LENGTH_DESCRIPTION_ID_IN_EFEXT1)];
                            this.mExt1FlagsRecord[pbrIndex] = new ArrayList();
                        }
                        for (int m = FREE_TAG_IN_EFEXT1; m < ext1FileArray.size(); m += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                            this.mExt1FlagsRecord[pbrIndex].add(Integer.valueOf(FREE_TAG_IN_EFEXT1));
                        }
                    }
                    this.mExt1Flags.put(Integer.valueOf(pbrIndex), this.mExt1FlagsRecord[pbrIndex]);
                    if (efid == 28474) {
                        updateExt1RecordFlagsForSim(pbrIndex);
                    } else {
                        updateExt1RecordFlags(pbrIndex);
                    }
                }
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                this.mLock.notify();
                break;
            case EVENT_EF_EXT1_RECORD_SIZE_DONE /*13*/:
                log("LOAD_EXT1_RECORD_SIZE_DONE");
                ar = (AsyncResult) msg.obj;
                AdnRecord newAdnRecord = ar.userObj;
                String mNumber = newAdnRecord.getNumber();
                adnRecIndex = msg.arg1 - 1;
                efid = msg.arg2;
                if (ar.exception == null) {
                    recordSize = (int[]) ar.result;
                    recordNumber = newAdnRecord.getExtRecord();
                    if (recordSize.length == EVENT_IAP_LOAD_DONE && recordNumber <= recordSize[EXT_TAG_IN_EFEXT1] && recordNumber > 0) {
                        String newExt1;
                        if (mNumber.length() > ADN_RECORD_LENGTH_DEFAULT) {
                            newExt1 = mNumber.substring(ADN_RECORD_LENGTH_DEFAULT);
                        } else {
                            newExt1 = "";
                            newAdnRecord.setExtRecord(HwSubscriptionManager.SUB_INIT_STATE);
                        }
                        data = buildExt1Data(recordSize[FREE_TAG_IN_EFEXT1], adnRecIndex, newExt1);
                        this.mFh.updateEFLinearFixed(efid, recordNumber, data, null, obtainMessage(EVENT_UPDATE_EXT1_RECORD_DONE, recordNumber, adnRecIndex, data));
                        break;
                    }
                    this.mSuccess = false;
                    synchronized (this.mLock) {
                        this.mLock.notify();
                        break;
                    }
                    return;
                }
                this.mSuccess = false;
                synchronized (this.mLock) {
                    this.mLock.notify();
                    break;
                }
                return;
                break;
            case EVENT_UPDATE_EXT1_RECORD_DONE /*14*/:
                log("UPDATE_EXT1_RECORD_DONE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    data = (byte[]) ar.userObj;
                    recordNumber = msg.arg1;
                    pbrIndex = getPbrIndexBy(msg.arg2);
                    this.mSuccess = DBG;
                    if (hasRecordIn(this.mExt1FileRecord, pbrIndex)) {
                        ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, data);
                        i = FREE_TAG_IN_EFEXT1;
                        while (i < data.length) {
                            if (data[i] == -1 || data[i] == null) {
                                ((ArrayList) this.mExt1Flags.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, Integer.valueOf(FREE_TAG_IN_EFEXT1));
                                i += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                            } else {
                                log("EVENT_UPDATE_EXT1_RECORD_DONE data !=0xff and 0x00");
                                ((ArrayList) this.mExt1Flags.get(Integer.valueOf(pbrIndex))).set(recordNumber - 1, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                            }
                        }
                    } else {
                        log("Ext1 record is empty");
                    }
                    obj = this.mLock;
                    synchronized (obj) {
                        break;
                    }
                    this.mLock.notify();
                    break;
                }
                this.mSuccess = false;
                synchronized (this.mLock) {
                    this.mLock.notify();
                    break;
                }
                return;
            case EVENT_GET_SIZE_DONE /*101*/:
                ar = (AsyncResult) msg.obj;
                obj = this.mLock;
                synchronized (obj) {
                    break;
                }
                if (ar.exception == null) {
                    this.mRecordSize = (int[]) ar.result;
                    log("GET_RECORD_SIZE Size " + this.mRecordSize[FREE_TAG_IN_EFEXT1] + " total " + this.mRecordSize[LENGTH_DESCRIPTION_ID_IN_EFEXT1] + " #record " + this.mRecordSize[EXT_TAG_IN_EFEXT1]);
                }
                this.mLock.notify();
                break;
        }
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public int getAnrCount() {
        int count = FREE_TAG_IN_EFEXT1;
        if (this.mAnrPresentInIap && hasRecordIn(this.mIapFileRecord, FREE_TAG_IN_EFEXT1)) {
            try {
                byte[] record = (byte[]) ((ArrayList) this.mIapFileRecord.get(Integer.valueOf(FREE_TAG_IN_EFEXT1))).get(FREE_TAG_IN_EFEXT1);
                if (record != null && this.mAnrTagNumberInIap >= record.length) {
                    log("getAnrCount mAnrTagNumberInIap: " + this.mAnrTagNumberInIap + " len:" + record.length);
                    return FREE_TAG_IN_EFEXT1;
                }
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "Error: getAnrCount ICC card: No IAP record for ADN, continuing");
                return FREE_TAG_IN_EFEXT1;
            }
        }
        int pbrIndex = this.mAnrFlags.size();
        for (int j = FREE_TAG_IN_EFEXT1; j < pbrIndex; j += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            count += ((ArrayList) this.mAnrFlags.get(Integer.valueOf(j))).size();
        }
        log("getAnrCount count is: " + count);
        return count;
    }

    public int getEmailCount() {
        int count = FREE_TAG_IN_EFEXT1;
        if (this.mEmailPresentInIap && hasRecordIn(this.mIapFileRecord, FREE_TAG_IN_EFEXT1)) {
            try {
                byte[] record = (byte[]) ((ArrayList) this.mIapFileRecord.get(Integer.valueOf(FREE_TAG_IN_EFEXT1))).get(FREE_TAG_IN_EFEXT1);
                if (record != null && this.mEmailTagNumberInIap >= record.length) {
                    log("getEmailCount mEmailTagNumberInIap: " + this.mEmailTagNumberInIap + " len:" + record.length);
                    return FREE_TAG_IN_EFEXT1;
                }
            } catch (IndexOutOfBoundsException e) {
                Rlog.e(LOG_TAG, "Error: getEmailCount ICC card: No IAP record for ADN, continuing");
                return FREE_TAG_IN_EFEXT1;
            }
        }
        int pbrIndex = this.mEmailFlags.size();
        for (int j = FREE_TAG_IN_EFEXT1; j < pbrIndex; j += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            count += ((ArrayList) this.mEmailFlags.get(Integer.valueOf(j))).size();
        }
        log("getEmailCount count is: " + count);
        return count;
    }

    public int getSpareAnrCount() {
        int count = FREE_TAG_IN_EFEXT1;
        int pbrIndex = this.mAnrFlags.size();
        for (int j = FREE_TAG_IN_EFEXT1; j < pbrIndex; j += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            for (int i = FREE_TAG_IN_EFEXT1; i < ((ArrayList) this.mAnrFlags.get(Integer.valueOf(j))).size(); i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                if (((Integer) ((ArrayList) this.mAnrFlags.get(Integer.valueOf(j))).get(i)).intValue() == 0) {
                    count += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                }
            }
        }
        log("getSpareAnrCount count is" + count);
        return count;
    }

    public int getSpareEmailCount() {
        int count = FREE_TAG_IN_EFEXT1;
        int pbrIndex = this.mEmailFlags.size();
        for (int j = FREE_TAG_IN_EFEXT1; j < pbrIndex; j += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            for (int i = FREE_TAG_IN_EFEXT1; i < ((ArrayList) this.mEmailFlags.get(Integer.valueOf(j))).size(); i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                if (((Integer) ((ArrayList) this.mEmailFlags.get(Integer.valueOf(j))).get(i)).intValue() == 0) {
                    count += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                }
            }
        }
        log("getSpareEmailCount count is: " + count);
        return count;
    }

    public int getUsimAdnCount() {
        if (this.mPhoneBookRecords == null || this.mPhoneBookRecords.isEmpty()) {
            return FREE_TAG_IN_EFEXT1;
        }
        log("getUsimAdnCount count is" + this.mPhoneBookRecords.size());
        return this.mPhoneBookRecords.size();
    }

    public int getEmptyEmailNum_Pbrindex(int pbrindex) {
        int count = FREE_TAG_IN_EFEXT1;
        if (!this.mEmailPresentInIap) {
            return LENGTH_DESCRIPTION_ID_IN_EFEXT1;
        }
        if (this.mEmailFlags.containsKey(Integer.valueOf(pbrindex))) {
            int size = ((ArrayList) this.mEmailFlags.get(Integer.valueOf(pbrindex))).size();
            for (int i = FREE_TAG_IN_EFEXT1; i < size; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                if (((Integer) ((ArrayList) this.mEmailFlags.get(Integer.valueOf(pbrindex))).get(i)).intValue() == 0) {
                    count += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                }
            }
        }
        return count;
    }

    public int getEmptyAnrNum_Pbrindex(int pbrindex) {
        int count = FREE_TAG_IN_EFEXT1;
        if (!this.mAnrPresentInIap) {
            return LENGTH_DESCRIPTION_ID_IN_EFEXT1;
        }
        if (this.mAnrFlags.containsKey(Integer.valueOf(pbrindex))) {
            int size = ((ArrayList) this.mAnrFlags.get(Integer.valueOf(pbrindex))).size();
            for (int i = FREE_TAG_IN_EFEXT1; i < size; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                if (((Integer) ((ArrayList) this.mAnrFlags.get(Integer.valueOf(pbrindex))).get(i)).intValue() == 0) {
                    count += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                }
            }
        }
        return count;
    }

    public int getEmailFilesCountEachAdn() {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from getEmailFilesCountEachAdn");
            return FREE_TAG_IN_EFEXT1;
        }
        Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(FREE_TAG_IN_EFEXT1));
        if (fileIds == null || !fileIds.containsKey(Integer.valueOf(USIM_EFEMAIL_TAG))) {
            return FREE_TAG_IN_EFEXT1;
        }
        if (this.mEmailPresentInIap) {
            return LENGTH_DESCRIPTION_ID_IN_EFEXT1;
        }
        return ((ArrayList) this.mPbrFile.mEmailFileIds.get(Integer.valueOf(FREE_TAG_IN_EFEXT1))).size();
    }

    public int getAnrFilesCountEachAdn() {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from getAnrFilesCountEachAdn");
            return FREE_TAG_IN_EFEXT1;
        }
        Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(FREE_TAG_IN_EFEXT1));
        if (fileIds == null || !fileIds.containsKey(Integer.valueOf(USIM_EFANR_TAG))) {
            return FREE_TAG_IN_EFEXT1;
        }
        if (this.mAnrPresentInIap) {
            return LENGTH_DESCRIPTION_ID_IN_EFEXT1;
        }
        return ((ArrayList) this.mPbrFile.mAnrFileIds.get(Integer.valueOf(FREE_TAG_IN_EFEXT1))).size();
    }

    public int getAdnRecordsFreeSize() {
        int freeRecs = FREE_TAG_IN_EFEXT1;
        log("getAdnRecordsFreeSize(): enter.");
        int totalRecs = getUsimAdnCount();
        if (totalRecs != 0) {
            for (int i = FREE_TAG_IN_EFEXT1; i < totalRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                if (((AdnRecord) this.mPhoneBookRecords.get(i)).isEmpty()) {
                    freeRecs += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                }
            }
        } else {
            log("getAdnRecordsFreeSize(): error. ");
        }
        log("getAdnRecordsFreeSize(): freeRecs = " + freeRecs);
        return freeRecs;
    }

    public ArrayList<AdnRecord> getPhonebookRecords() {
        if (this.mPhoneBookRecords.isEmpty()) {
            return null;
        }
        return this.mPhoneBookRecords;
    }

    public void setIccFileHandler(IccFileHandler fh) {
        this.mFh = fh;
    }

    public int[] getAdnRecordsSizeFromEF() {
        synchronized (this.mLock) {
            if (this.mIsPbrPresent.booleanValue()) {
                if (this.mPbrFile == null) {
                    readPbrFileAndWait();
                }
                if (this.mPbrFile == null) {
                    return null;
                }
                int numRecs = this.mPbrFile.mFileIds.size();
                this.temRecordSize[FREE_TAG_IN_EFEXT1] = FREE_TAG_IN_EFEXT1;
                this.temRecordSize[LENGTH_DESCRIPTION_ID_IN_EFEXT1] = FREE_TAG_IN_EFEXT1;
                this.temRecordSize[EXT_TAG_IN_EFEXT1] = FREE_TAG_IN_EFEXT1;
                for (int i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                    this.mRecordSize[FREE_TAG_IN_EFEXT1] = FREE_TAG_IN_EFEXT1;
                    this.mRecordSize[LENGTH_DESCRIPTION_ID_IN_EFEXT1] = FREE_TAG_IN_EFEXT1;
                    this.mRecordSize[EXT_TAG_IN_EFEXT1] = FREE_TAG_IN_EFEXT1;
                    getAdnRecordsSizeAndWait(i);
                    Rlog.d(LOG_TAG, "getAdnRecordsSizeFromEF: recordSize[2]=" + this.mRecordSize[EXT_TAG_IN_EFEXT1]);
                    if (this.mRecordSize[FREE_TAG_IN_EFEXT1] != 0) {
                        this.temRecordSize[FREE_TAG_IN_EFEXT1] = this.mRecordSize[FREE_TAG_IN_EFEXT1];
                    }
                    if (this.mRecordSize[LENGTH_DESCRIPTION_ID_IN_EFEXT1] != 0) {
                        this.temRecordSize[LENGTH_DESCRIPTION_ID_IN_EFEXT1] = this.mRecordSize[LENGTH_DESCRIPTION_ID_IN_EFEXT1];
                    }
                    this.temRecordSize[EXT_TAG_IN_EFEXT1] = this.mRecordSize[EXT_TAG_IN_EFEXT1] + this.temRecordSize[EXT_TAG_IN_EFEXT1];
                }
                Rlog.d(LOG_TAG, "getAdnRecordsSizeFromEF: temRecordSize[2]=" + this.temRecordSize[EXT_TAG_IN_EFEXT1]);
                int[] iArr = this.temRecordSize;
                return iArr;
            }
            return null;
        }
    }

    public void getAdnRecordsSizeAndWait(int recNum) {
        if (this.mPbrFile != null) {
            Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
            if (fileIds != null && !fileIds.isEmpty()) {
                int efid = ((Integer) fileIds.get(Integer.valueOf(USIM_EFADN_TAG))).intValue();
                Rlog.d(LOG_TAG, "getAdnRecordsSize: efid=" + efid);
                this.mFh.getEFLinearRecordSize(efid, obtainMessage(EVENT_GET_SIZE_DONE));
                boolean isWait = DBG;
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
        int size = FREE_TAG_IN_EFEXT1;
        if (!(this.mPbrFile == null || this.mPbrFile.mFileIds == null)) {
            size = this.mPbrFile.mFileIds.size();
        }
        log("getPbrFileSize:" + size);
        return size;
    }

    public int getEFidInPBR(int recNum, int tag) {
        int efid = FREE_TAG_IN_EFEXT1;
        if (this.mPbrFile == null) {
            return FREE_TAG_IN_EFEXT1;
        }
        Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds == null) {
            return FREE_TAG_IN_EFEXT1;
        }
        if (fileIds.containsKey(Integer.valueOf(tag))) {
            efid = ((Integer) fileIds.get(Integer.valueOf(tag))).intValue();
        }
        log("getEFidInPBR, efid = " + efid + ", recNum = " + recNum + ", tag = " + tag);
        return efid;
    }

    private void initExt1FileRecordAndFlags() {
        this.mExt1FileRecord = new HashMap();
        this.mExt1Flags = new HashMap();
    }

    private void resetExt1Variables() {
        if (this.mExt1FlagsRecord != null && this.mPbrFile != null) {
            for (int i = FREE_TAG_IN_EFEXT1; i < this.mExt1FlagsRecord.length; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                this.mExt1FlagsRecord[i].clear();
            }
        } else if (this.mExt1FlagsRecord != null && this.mPbrFile == null) {
            this.mExt1FlagsRecord[FREE_TAG_IN_EFEXT1].clear();
        }
        this.mExt1Flags.clear();
        this.mExt1FileRecord.clear();
    }

    private void loadExt1FilesFromUsim(int numRecs) {
        int i;
        this.mExt1FlagsRecord = new ArrayList[numRecs];
        for (i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            this.mExt1FlagsRecord[i] = new ArrayList();
        }
        for (i = FREE_TAG_IN_EFEXT1; i < numRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            readExt1FileAndWait(i);
        }
    }

    private void readExt1FileAndWait(int recNum) {
        if (this.mPbrFile == null) {
            Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from readExt1FileAndWait");
            return;
        }
        Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(recNum));
        if (fileIds == null || fileIds.isEmpty()) {
            Rlog.e(LOG_TAG, "fileIds is NULL, exiting from readExt1FileAndWait");
            return;
        }
        if (fileIds.containsKey(Integer.valueOf(USIM_EFEXT1_TAG))) {
            this.mFh.loadEFLinearFixedAll(((Integer) fileIds.get(Integer.valueOf(USIM_EFEXT1_TAG))).intValue(), obtainMessage(EVENT_EXT1_LOAD_DONE, recNum, ((Integer) fileIds.get(Integer.valueOf(USIM_EFEXT1_TAG))).intValue()));
            log("readExt1FileAndWait EXT1 efid is : " + fileIds.get(Integer.valueOf(USIM_EFEXT1_TAG)));
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                Rlog.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
            }
        }
    }

    private void updateExt1RecordFlags(int pbrIndex) {
        if (hasRecordIn(this.mExt1FileRecord, pbrIndex)) {
            int i;
            int numAdnRecs = ((Integer) this.mAdnLengthList.get(pbrIndex)).intValue();
            for (i = FREE_TAG_IN_EFEXT1; i < numAdnRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i + getInitIndexBy(pbrIndex));
                if (!(rec == null || rec.getExtRecord() == HwSubscriptionManager.SUB_INIT_STATE || rec.getExtRecord() <= 0)) {
                    ((ArrayList) this.mExt1Flags.get(Integer.valueOf(pbrIndex))).set(rec.getExtRecord() - 1, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
                }
            }
            int extRecsSize = ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex))).size();
            for (int index = FREE_TAG_IN_EFEXT1; index < extRecsSize; index += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                if (LENGTH_DESCRIPTION_ID_IN_EFEXT1 != ((Integer) ((ArrayList) this.mExt1Flags.get(Integer.valueOf(pbrIndex))).get(index)).intValue()) {
                    byte[] extRec = (byte[]) ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex))).get(index);
                    String extRecord = readExt1Record(index, pbrIndex, FREE_TAG_IN_EFEXT1);
                    if (extRec[FREE_TAG_IN_EFEXT1] == EXT_TAG_IN_EFEXT1 && "".equals(extRecord)) {
                        for (i = FREE_TAG_IN_EFEXT1; i < extRec.length; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                            extRec[i] = (byte) -1;
                        }
                    }
                }
            }
            log("updateExt1RecordFlags done");
        }
    }

    public void readExt1FileForSim(int efid) {
        if (efid == 28474) {
            this.mFh.loadEFLinearFixedAll(28490, obtainMessage(EVENT_EXT1_LOAD_DONE, FREE_TAG_IN_EFEXT1, efid));
            log("readExt1FileForSim Ext1 efid is : 28490");
        }
    }

    private void updateExt1RecordFlagsForSim(int recNum) {
        int i;
        this.mPhoneBookRecords = this.mAdnCache.getAdnFilesForSim();
        int numAdnRecs = this.mPhoneBookRecords.size();
        if (this.mAdnLengthList.size() == 0) {
            this.mAdnLengthList.add(Integer.valueOf(FREE_TAG_IN_EFEXT1));
        }
        this.mAdnLengthList.set(recNum, Integer.valueOf(numAdnRecs));
        for (i = FREE_TAG_IN_EFEXT1; i < numAdnRecs; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            AdnRecord rec = (AdnRecord) this.mPhoneBookRecords.get(i);
            if (!(rec == null || rec.getExtRecord() == HwSubscriptionManager.SUB_INIT_STATE || rec.getExtRecord() <= 0)) {
                ((ArrayList) this.mExt1Flags.get(Integer.valueOf(recNum))).set(rec.getExtRecord() - 1, Integer.valueOf(LENGTH_DESCRIPTION_ID_IN_EFEXT1));
            }
        }
        int extRecsSize = ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(recNum))).size();
        for (int index = FREE_TAG_IN_EFEXT1; index < extRecsSize; index += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            if (LENGTH_DESCRIPTION_ID_IN_EFEXT1 != ((Integer) ((ArrayList) this.mExt1Flags.get(Integer.valueOf(recNum))).get(index)).intValue()) {
                byte[] extRec = (byte[]) ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(recNum))).get(index);
                String anrRecord = readExt1Record(index, recNum, FREE_TAG_IN_EFEXT1);
                if (extRec[FREE_TAG_IN_EFEXT1] == EXT_TAG_IN_EFEXT1 && "".equals(anrRecord)) {
                    for (i = FREE_TAG_IN_EFEXT1; i < extRec.length; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                        extRec[i] = (byte) -1;
                    }
                }
            }
        }
        log("updateExt1RecordFlags done");
    }

    public boolean updateExt1File(int adnRecNum, AdnRecord oldAdnRecord, AdnRecord newAdnRecord, int tagOrEfid) {
        int pbrIndex = getPbrIndexBy(adnRecNum - 1);
        String oldNumber = oldAdnRecord.getNumber();
        String newNumber = newAdnRecord.getNumber();
        this.mSuccess = false;
        if (IccRecords.getAdnLongNumberSupport()) {
            log("updateExt1File adnRecNum: " + adnRecNum);
            if (oldNumber.length() > ADN_RECORD_LENGTH_DEFAULT || newNumber.length() > ADN_RECORD_LENGTH_DEFAULT) {
                int efid;
                if (tagOrEfid == USIM_EFEXT1_TAG) {
                    if (this.mPbrFile == null || this.mPbrFile.mFileIds == null) {
                        Rlog.e(LOG_TAG, "mPbrFile is NULL, exiting from updateExt1File");
                        return this.mSuccess;
                    }
                    Map<Integer, Integer> fileIds = (Map) this.mPbrFile.mFileIds.get(Integer.valueOf(pbrIndex));
                    if (fileIds == null) {
                        return this.mSuccess;
                    }
                    if (!fileIds.containsKey(Integer.valueOf(tagOrEfid))) {
                        return this.mSuccess;
                    }
                    efid = ((Integer) fileIds.get(Integer.valueOf(tagOrEfid))).intValue();
                } else if (tagOrEfid != 28490) {
                    return this.mSuccess;
                } else {
                    efid = tagOrEfid;
                }
                if (oldAdnRecord.getExtRecord() == HwSubscriptionManager.SUB_INIT_STATE && !TextUtils.isEmpty(newNumber)) {
                    int recNum = getExt1RecNumber(adnRecNum);
                    if (recNum == -1) {
                        return this.mSuccess;
                    }
                    newAdnRecord.setExtRecord(recNum);
                    log("Index Number in Ext is " + recNum);
                }
                synchronized (this.mLock) {
                    this.mFh.getEFLinearRecordSize(efid, obtainMessage(EVENT_EF_EXT1_RECORD_SIZE_DONE, adnRecNum, efid, newAdnRecord));
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "interrupted while trying to update by search");
                    }
                }
                return this.mSuccess;
            }
            this.mSuccess = DBG;
            return this.mSuccess;
        }
        this.mSuccess = DBG;
        return this.mSuccess;
    }

    private String readExt1Record(int pbrIndex, int recNum, int offset) {
        if (!hasRecordIn(this.mExt1FileRecord, pbrIndex)) {
            return null;
        }
        try {
            byte[] extRec = (byte[]) ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex))).get(recNum + offset);
            if ((extRec[FREE_TAG_IN_EFEXT1] & HwSubscriptionManager.SUB_INIT_STATE) == 0) {
                return "";
            }
            int numberLength = extRec[LENGTH_DESCRIPTION_ID_IN_EFEXT1] & HwSubscriptionManager.SUB_INIT_STATE;
            if (numberLength > MAX_NUMBER_DATA_SIZE_EFEXT1) {
                return "";
            }
            return PhoneNumberUtils.calledPartyBCDFragmentToString(extRec, EXT_TAG_IN_EFEXT1, numberLength);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private byte[] buildExt1Data(int length, int adnRecIndex, String ext) {
        byte[] data = new byte[length];
        for (int i = FREE_TAG_IN_EFEXT1; i < length; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            data[i] = (byte) -1;
        }
        data[FREE_TAG_IN_EFEXT1] = (byte) 0;
        if (TextUtils.isEmpty(ext)) {
            log("[buildExtData] Empty ext1 record");
            return data;
        }
        byte[] byteExt = PhoneNumberUtils.numberToCalledPartyBCD(ext);
        if (byteExt == null) {
            return data;
        }
        data[FREE_TAG_IN_EFEXT1] = (byte) 2;
        if (byteExt.length > MAX_NUMBER_SIZE_BYTES) {
            System.arraycopy(byteExt, LENGTH_DESCRIPTION_ID_IN_EFEXT1, data, EXT_TAG_IN_EFEXT1, MAX_NUMBER_DATA_SIZE_EFEXT1);
            data[LENGTH_DESCRIPTION_ID_IN_EFEXT1] = (byte) 10;
        } else {
            System.arraycopy(byteExt, LENGTH_DESCRIPTION_ID_IN_EFEXT1, data, EXT_TAG_IN_EFEXT1, byteExt.length - 1);
            data[LENGTH_DESCRIPTION_ID_IN_EFEXT1] = (byte) (byteExt.length - 1);
        }
        return data;
    }

    private int getExt1RecNumber(int adnRecIndex) {
        int pbrIndex = getPbrIndexBy(adnRecIndex - 1);
        log("getExt1RecNumber adnRecIndex is: " + adnRecIndex);
        if (!hasRecordIn(this.mExt1FileRecord, pbrIndex)) {
            return -1;
        }
        int extRecordNumber = ((AdnRecord) this.mPhoneBookRecords.get(adnRecIndex - 1)).getExtRecord();
        if (extRecordNumber != HwSubscriptionManager.SUB_INIT_STATE && extRecordNumber > 0 && extRecordNumber <= ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex))).size()) {
            return extRecordNumber;
        }
        int recordSize = ((ArrayList) this.mExt1FileRecord.get(Integer.valueOf(pbrIndex))).size();
        log("ext record Size: " + recordSize);
        for (int i = FREE_TAG_IN_EFEXT1; i < recordSize; i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            if (TextUtils.isEmpty(readExt1Record(pbrIndex, i, FREE_TAG_IN_EFEXT1))) {
                return i + LENGTH_DESCRIPTION_ID_IN_EFEXT1;
            }
        }
        return -1;
    }

    public int getExt1Count() {
        int count = FREE_TAG_IN_EFEXT1;
        int pbrIndex = this.mExt1Flags.size();
        for (int j = FREE_TAG_IN_EFEXT1; j < pbrIndex; j += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            count += ((ArrayList) this.mExt1Flags.get(Integer.valueOf(j))).size();
        }
        log("getExt1Count count is: " + count);
        return count;
    }

    public int getSpareExt1Count() {
        int count = FREE_TAG_IN_EFEXT1;
        int pbrIndex = this.mExt1Flags.size();
        for (int j = FREE_TAG_IN_EFEXT1; j < pbrIndex; j += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
            for (int i = FREE_TAG_IN_EFEXT1; i < ((ArrayList) this.mExt1Flags.get(Integer.valueOf(j))).size(); i += LENGTH_DESCRIPTION_ID_IN_EFEXT1) {
                if (((Integer) ((ArrayList) this.mExt1Flags.get(Integer.valueOf(j))).get(i)).intValue() == 0) {
                    count += LENGTH_DESCRIPTION_ID_IN_EFEXT1;
                }
            }
        }
        log("getSpareExt1Count count is: " + count);
        return count;
    }
}
