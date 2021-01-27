package com.huawei.internal.telephony.uicc;

import android.telephony.Rlog;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.ArrayList;
import java.util.Iterator;

public class AdnRecordExt {
    private static final int INVALID = -1;
    private static final String LOG_TAG = "AdnRecordExt";
    private AdnRecord mAdnRecord;

    private AdnRecordExt() {
    }

    public AdnRecordExt(String alphaTag, String number, String[] emails, String[] additionalNumbers) {
        this.mAdnRecord = new AdnRecord(alphaTag, number, emails, additionalNumbers);
    }

    public AdnRecordExt(String alphaTag, String number) {
        this.mAdnRecord = new AdnRecord(alphaTag, number);
    }

    public AdnRecordExt(int efid, int recordNumber, String alphaTag, String number) {
        this.mAdnRecord = new AdnRecord(efid, recordNumber, alphaTag, number);
    }

    public static AdnRecordExt from(Object object) {
        if (!(object instanceof AdnRecord)) {
            return null;
        }
        AdnRecordExt adnRecordEx = new AdnRecordExt();
        adnRecordEx.setAdnRecord((AdnRecord) object);
        return adnRecordEx;
    }

    public static ArrayList<AdnRecordExt> convertAdnRecordToExt(Object object) {
        ArrayList<AdnRecordExt> adnRecordExts = null;
        if (!(object instanceof ArrayList) || ((ArrayList) object).size() == 0 || !(((ArrayList) object).get(0) instanceof AdnRecord)) {
            Rlog.i(LOG_TAG, "convertAdnRecordToExt failed.");
        } else {
            adnRecordExts = new ArrayList<>();
            Iterator<AdnRecord> it = ((ArrayList) object).iterator();
            while (it.hasNext()) {
                AdnRecordExt adnRecordExt = new AdnRecordExt();
                adnRecordExt.setAdnRecord(it.next());
                adnRecordExts.add(adnRecordExt);
            }
        }
        return adnRecordExts;
    }

    public static Object convertToAdnRecords(ArrayList<AdnRecordExt> adnRecordExts) {
        if (adnRecordExts == null) {
            return null;
        }
        ArrayList<AdnRecord> adnRecords = new ArrayList<>();
        Iterator<AdnRecordExt> it = adnRecordExts.iterator();
        while (it.hasNext()) {
            Object adnRecord = it.next().getAdnRecord();
            if (adnRecord instanceof AdnRecord) {
                adnRecords.add((AdnRecord) adnRecord);
            }
        }
        return adnRecords;
    }

    public Object getAdnRecord() {
        return this.mAdnRecord;
    }

    public void setAdnRecord(AdnRecord adnRecord) {
        this.mAdnRecord = adnRecord;
    }

    public int getEfid() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getEfid();
        }
        return -1;
    }

    public void setEfid(int efid) {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            adnRecord.setEfid(efid);
        }
    }

    public int getRecordNumber() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getRecId();
        }
        return -1;
    }

    public void setRecordNumber(int recordNum) {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            adnRecord.setRecordNumber(recordNum);
        }
    }

    public String getNumber() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getNumber();
        }
        return null;
    }

    public String getAlphaTag() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getAlphaTag();
        }
        return null;
    }

    public int getExtRecord() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getExtRecord();
        }
        return -1;
    }

    public void setExtRecord(int extRecord) {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            adnRecord.setExtRecord(extRecord);
        }
    }

    public String[] getAdditionalNumbers() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getAdditionalNumbers();
        }
        return null;
    }

    public void setAdditionalNumbers(String[] additionalNumbers) {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            adnRecord.setAdditionalNumbers(additionalNumbers);
        }
    }

    public String[] getEmails() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.getEmails();
        }
        return null;
    }

    public void setEmails(String[] emails) {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            adnRecord.setEmails(emails);
        }
    }

    public boolean isSameAdnRecord(AdnRecordExt adnRecordExt) {
        if (this.mAdnRecord == null || adnRecordExt == null) {
            return false;
        }
        Object adnRecord = adnRecordExt.getAdnRecord();
        if (adnRecord instanceof AdnRecord) {
            return this.mAdnRecord.isEqual((AdnRecord) adnRecord);
        }
        return false;
    }

    public void updateAnrEmailArray(AdnRecordExt adnRecordExt, int emailFileNum, int anrFileNum) {
        if (adnRecordExt != null) {
            Object adnRecord = adnRecordExt.getAdnRecord();
            if (adnRecord instanceof AdnRecord) {
                this.mAdnRecord.updateAnrEmailArray((AdnRecord) adnRecord, emailFileNum, anrFileNum);
            }
        }
    }

    public boolean isEmpty() {
        AdnRecord adnRecord = this.mAdnRecord;
        if (adnRecord != null) {
            return adnRecord.isEmpty();
        }
        return true;
    }
}
