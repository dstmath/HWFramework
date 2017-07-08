package com.android.internal.telephony.uicc;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.HwTelephonyFactory;
import com.google.android.mms.pdu.PduHeaders;

public class AdnRecord implements Parcelable {
    static final int ADN_BCD_NUMBER_LENGTH = 0;
    static final int ADN_CAPABILITY_ID = 12;
    static final int ADN_DIALING_NUMBER_END = 11;
    static final int ADN_DIALING_NUMBER_START = 2;
    static final int ADN_EXTENSION_ID = 13;
    static final int ADN_TON_AND_NPI = 1;
    public static final Creator<AdnRecord> CREATOR = null;
    static final int EXT_RECORD_LENGTH_BYTES = 13;
    static final int EXT_RECORD_TYPE_ADDITIONAL_DATA = 2;
    static final int EXT_RECORD_TYPE_MASK = 3;
    static final int FOOTER_SIZE_BYTES = 14;
    static final String LOG_TAG = "AdnRecord";
    static final int MAX_EXT_CALLED_PARTY_LENGTH = 10;
    static final int MAX_NUMBER_SIZE_BYTES = 11;
    String[] mAdditionalNumbers;
    String mAlphaTag;
    int mEfid;
    String[] mEmails;
    int mExtRecord;
    String mNumber;
    int mRecordNumber;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.AdnRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.AdnRecord.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.AdnRecord.<clinit>():void");
    }

    public AdnRecord(byte[] record) {
        this((int) ADN_BCD_NUMBER_LENGTH, (int) ADN_BCD_NUMBER_LENGTH, record);
    }

    public AdnRecord(int efid, int recordNumber, byte[] record) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mAdditionalNumbers = null;
        this.mExtRecord = PduHeaders.STORE_STATUS_ERROR_END;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        parseRecord(record);
    }

    public AdnRecord(String alphaTag, String number) {
        this((int) ADN_BCD_NUMBER_LENGTH, (int) ADN_BCD_NUMBER_LENGTH, alphaTag, number);
    }

    public AdnRecord(String alphaTag, String number, String[] emails) {
        this(ADN_BCD_NUMBER_LENGTH, ADN_BCD_NUMBER_LENGTH, alphaTag, number, emails);
    }

    public AdnRecord(String alphaTag, String number, String[] emails, String[] additionalNumbers) {
        this(ADN_BCD_NUMBER_LENGTH, ADN_BCD_NUMBER_LENGTH, alphaTag, number, emails, additionalNumbers);
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String[] emails) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mAdditionalNumbers = null;
        this.mExtRecord = PduHeaders.STORE_STATUS_ERROR_END;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = emails;
        this.mAdditionalNumbers = null;
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number, String[] emails, String[] additionalNumbers) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mAdditionalNumbers = null;
        this.mExtRecord = PduHeaders.STORE_STATUS_ERROR_END;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = emails;
        this.mAdditionalNumbers = additionalNumbers;
    }

    public AdnRecord(int efid, int recordNumber, String alphaTag, String number) {
        this.mAlphaTag = null;
        this.mNumber = null;
        this.mAdditionalNumbers = null;
        this.mExtRecord = PduHeaders.STORE_STATUS_ERROR_END;
        this.mEfid = efid;
        this.mRecordNumber = recordNumber;
        this.mAlphaTag = alphaTag;
        this.mNumber = number;
        this.mEmails = null;
        this.mAdditionalNumbers = null;
    }

    public String getAlphaTag() {
        return this.mAlphaTag;
    }

    public int getEfid() {
        return this.mEfid;
    }

    public int getRecId() {
        return this.mRecordNumber;
    }

    public String getNumber() {
        return this.mNumber;
    }

    public void setNumber(String number) {
        this.mNumber = number;
    }

    public String[] getEmails() {
        return this.mEmails;
    }

    public void setEmails(String[] emails) {
        this.mEmails = emails;
    }

    public String[] getAdditionalNumbers() {
        return this.mAdditionalNumbers;
    }

    public void setAdditionalNumbers(String[] additionalNumbers) {
        this.mAdditionalNumbers = additionalNumbers;
    }

    public String toString() {
        return "ADN Record '" + this.mAlphaTag + "' '" + this.mNumber + " " + this.mEmails + " " + this.mAdditionalNumbers + "'";
    }

    public boolean isEmpty() {
        if (TextUtils.isEmpty(this.mAlphaTag) && TextUtils.isEmpty(this.mNumber) && this.mEmails == null && this.mAdditionalNumbers == null) {
            return true;
        }
        return false;
    }

    public int getExtRecord() {
        return this.mExtRecord;
    }

    public void setExtRecord(int mExtRecord) {
        this.mExtRecord = mExtRecord;
    }

    public boolean hasExtendedRecord() {
        return (this.mExtRecord == 0 || this.mExtRecord == PduHeaders.STORE_STATUS_ERROR_END) ? false : true;
    }

    private static boolean stringCompareNullEqualsEmpty(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null) {
            s1 = "";
        }
        if (s2 == null) {
            s2 = "";
        }
        return s1.equals(s2);
    }

    public boolean isEqual(AdnRecord adn) {
        if (stringCompareNullEqualsEmpty(this.mAlphaTag, adn.mAlphaTag) && stringCompareNullEqualsEmpty(this.mNumber, adn.mNumber) && HwTelephonyFactory.getHwUiccManager().arrayCompareNullEqualsEmpty(this.mEmails, adn.mEmails)) {
            return HwTelephonyFactory.getHwUiccManager().arrayCompareNullEqualsEmpty(this.mAdditionalNumbers, adn.mAdditionalNumbers);
        }
        return false;
    }

    public void updateAnrEmailArray(AdnRecord adn, int emailFileNum, int anrFileNum) {
        this.mEmails = HwTelephonyFactory.getHwUiccManager().updateAnrEmailArrayHelper(this.mEmails, adn.mEmails, emailFileNum);
        this.mAdditionalNumbers = HwTelephonyFactory.getHwUiccManager().updateAnrEmailArrayHelper(this.mAdditionalNumbers, adn.mAdditionalNumbers, anrFileNum);
    }

    public int describeContents() {
        return ADN_BCD_NUMBER_LENGTH;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEfid);
        dest.writeInt(this.mRecordNumber);
        dest.writeString(this.mAlphaTag);
        dest.writeString(this.mNumber);
        dest.writeStringArray(this.mEmails);
        dest.writeStringArray(this.mAdditionalNumbers);
    }

    public byte[] buildAdnString(int recordSize) {
        return HwTelephonyFactory.getHwUiccManager().buildAdnStringHw(recordSize, this.mAlphaTag, this.mNumber);
    }

    public void appendExtRecord(byte[] extRecord) {
        try {
            if (extRecord.length == EXT_RECORD_LENGTH_BYTES && (extRecord[ADN_BCD_NUMBER_LENGTH] & EXT_RECORD_TYPE_MASK) == EXT_RECORD_TYPE_ADDITIONAL_DATA && (extRecord[ADN_TON_AND_NPI] & PduHeaders.STORE_STATUS_ERROR_END) <= MAX_EXT_CALLED_PARTY_LENGTH) {
                this.mNumber += PhoneNumberUtils.calledPartyBCDFragmentToString(extRecord, EXT_RECORD_TYPE_ADDITIONAL_DATA, extRecord[ADN_TON_AND_NPI] & PduHeaders.STORE_STATUS_ERROR_END);
                this.mNumber = HwTelephonyFactory.getHwUiccManager().prependPlusInLongAdnNumber(this.mNumber);
            }
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecord ext record", ex);
        }
    }

    private void parseRecord(byte[] record) {
        try {
            this.mAlphaTag = IccUtils.adnStringFieldToString(record, ADN_BCD_NUMBER_LENGTH, record.length - 14);
            int footerOffset = record.length - 14;
            int numberLength = record[footerOffset] & PduHeaders.STORE_STATUS_ERROR_END;
            if (numberLength > MAX_NUMBER_SIZE_BYTES) {
                numberLength = MAX_NUMBER_SIZE_BYTES;
            }
            this.mNumber = PhoneNumberUtils.calledPartyBCDToString(record, footerOffset + ADN_TON_AND_NPI, numberLength);
            this.mExtRecord = record[record.length - 1] & PduHeaders.STORE_STATUS_ERROR_END;
            this.mEmails = null;
            this.mAdditionalNumbers = null;
        } catch (RuntimeException ex) {
            Rlog.w(LOG_TAG, "Error parsing AdnRecord", ex);
            this.mNumber = "";
            this.mAlphaTag = "";
            this.mEmails = null;
            this.mAdditionalNumbers = null;
        }
    }
}
