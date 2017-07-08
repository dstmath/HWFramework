package jcifs.netbios;

import java.net.InetAddress;
import jcifs.util.Hexdump;

abstract class NameServicePacket {
    static final int A = 1;
    static final int ACT_ERR = 6;
    static final int ADDITIONAL_OFFSET = 10;
    static final int ANSWER_OFFSET = 6;
    static final int AUTHORITY_OFFSET = 8;
    static final int CFT_ERR = 7;
    static final int FMT_ERR = 1;
    static final int HEADER_LENGTH = 12;
    static final int IMP_ERR = 4;
    static final int IN = 1;
    static final int NB = 32;
    static final int NBSTAT = 33;
    static final int NBSTAT_IN = 2162689;
    static final int NB_IN = 2097153;
    static final int NS = 2;
    static final int NULL = 10;
    static final int OPCODE_OFFSET = 2;
    static final int QUERY = 0;
    static final int QUESTION_OFFSET = 4;
    static final int RFS_ERR = 5;
    static final int SRV_ERR = 2;
    static final int WACK = 7;
    int additionalCount;
    InetAddress addr;
    NbtAddress[] addrEntry;
    int addrIndex;
    int answerCount;
    int authorityCount;
    boolean isAuthAnswer;
    boolean isBroadcast;
    boolean isRecurAvailable;
    boolean isRecurDesired;
    boolean isResponse;
    boolean isTruncated;
    int nameTrnId;
    int opCode;
    int questionClass;
    int questionCount;
    Name questionName;
    int questionType;
    int rDataLength;
    boolean received;
    int recordClass;
    Name recordName;
    int recordType;
    int resultCode;
    int ttl;

    abstract int readBodyWireFormat(byte[] bArr, int i);

    abstract int readRDataWireFormat(byte[] bArr, int i);

    abstract int writeBodyWireFormat(byte[] bArr, int i);

    abstract int writeRDataWireFormat(byte[] bArr, int i);

    static void writeInt2(int val, byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + IN;
        dst[dstIndex] = (byte) ((val >> AUTHORITY_OFFSET) & 255);
        dst[dstIndex2] = (byte) (val & 255);
    }

    static void writeInt4(int val, byte[] dst, int dstIndex) {
        int i = dstIndex + IN;
        dst[dstIndex] = (byte) ((val >> 24) & 255);
        dstIndex = i + IN;
        dst[i] = (byte) ((val >> 16) & 255);
        i = dstIndex + IN;
        dst[dstIndex] = (byte) ((val >> AUTHORITY_OFFSET) & 255);
        dst[i] = (byte) (val & 255);
    }

    static int readInt2(byte[] src, int srcIndex) {
        return ((src[srcIndex] & 255) << AUTHORITY_OFFSET) + (src[srcIndex + IN] & 255);
    }

    static int readInt4(byte[] src, int srcIndex) {
        return ((((src[srcIndex] & 255) << 24) + ((src[srcIndex + IN] & 255) << 16)) + ((src[srcIndex + SRV_ERR] & 255) << AUTHORITY_OFFSET)) + (src[srcIndex + 3] & 255);
    }

    static int readNameTrnId(byte[] src, int srcIndex) {
        return readInt2(src, srcIndex);
    }

    NameServicePacket() {
        this.isRecurDesired = true;
        this.isBroadcast = true;
        this.questionCount = IN;
        this.questionClass = IN;
    }

    int writeWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        dstIndex += writeHeaderWireFormat(dst, dstIndex);
        return (dstIndex + writeBodyWireFormat(dst, dstIndex)) - start;
    }

    int readWireFormat(byte[] src, int srcIndex) {
        int start = srcIndex;
        srcIndex += readHeaderWireFormat(src, srcIndex);
        return (srcIndex + readBodyWireFormat(src, srcIndex)) - start;
    }

    int writeHeaderWireFormat(byte[] dst, int dstIndex) {
        int i = SmbConstants.FLAGS_RESPONSE;
        int i2 = QUERY;
        int start = dstIndex;
        writeInt2(this.nameTrnId, dst, dstIndex);
        dst[dstIndex + SRV_ERR] = (byte) ((this.isRecurDesired ? IN : QUERY) + (((((this.opCode << 3) & 120) + (this.isResponse ? SmbConstants.FLAGS_RESPONSE : QUERY)) + (this.isAuthAnswer ? QUESTION_OFFSET : QUERY)) + (this.isTruncated ? SRV_ERR : QUERY)));
        int i3 = (dstIndex + SRV_ERR) + IN;
        if (!this.isRecurAvailable) {
            i = QUERY;
        }
        if (this.isBroadcast) {
            i2 = 16;
        }
        dst[i3] = (byte) ((i + i2) + (this.resultCode & 15));
        writeInt2(this.questionCount, dst, start + QUESTION_OFFSET);
        writeInt2(this.answerCount, dst, start + ANSWER_OFFSET);
        writeInt2(this.authorityCount, dst, start + AUTHORITY_OFFSET);
        writeInt2(this.additionalCount, dst, start + NULL);
        return HEADER_LENGTH;
    }

    int readHeaderWireFormat(byte[] src, int srcIndex) {
        boolean z;
        boolean z2 = false;
        this.nameTrnId = readInt2(src, srcIndex);
        this.isResponse = (src[srcIndex + SRV_ERR] & SmbConstants.FLAGS_RESPONSE) != 0;
        this.opCode = (src[srcIndex + SRV_ERR] & 120) >> 3;
        if ((src[srcIndex + SRV_ERR] & QUESTION_OFFSET) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.isAuthAnswer = z;
        if ((src[srcIndex + SRV_ERR] & SRV_ERR) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.isTruncated = z;
        if ((src[srcIndex + SRV_ERR] & IN) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.isRecurDesired = z;
        if ((src[(srcIndex + SRV_ERR) + IN] & SmbConstants.FLAGS_RESPONSE) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.isRecurAvailable = z;
        if ((src[(srcIndex + SRV_ERR) + IN] & 16) != 0) {
            z2 = true;
        }
        this.isBroadcast = z2;
        this.resultCode = src[(srcIndex + SRV_ERR) + IN] & 15;
        this.questionCount = readInt2(src, srcIndex + QUESTION_OFFSET);
        this.answerCount = readInt2(src, srcIndex + ANSWER_OFFSET);
        this.authorityCount = readInt2(src, srcIndex + AUTHORITY_OFFSET);
        this.additionalCount = readInt2(src, srcIndex + NULL);
        return HEADER_LENGTH;
    }

    int writeQuestionSectionWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        dstIndex += this.questionName.writeWireFormat(dst, dstIndex);
        writeInt2(this.questionType, dst, dstIndex);
        dstIndex += SRV_ERR;
        writeInt2(this.questionClass, dst, dstIndex);
        return (dstIndex + SRV_ERR) - start;
    }

    int readQuestionSectionWireFormat(byte[] src, int srcIndex) {
        int start = srcIndex;
        srcIndex += this.questionName.readWireFormat(src, srcIndex);
        this.questionType = readInt2(src, srcIndex);
        srcIndex += SRV_ERR;
        this.questionClass = readInt2(src, srcIndex);
        return (srcIndex + SRV_ERR) - start;
    }

    int writeResourceRecordWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        if (this.recordName == this.questionName) {
            int i = dstIndex + IN;
            dst[dstIndex] = (byte) -64;
            dstIndex = i + IN;
            dst[i] = (byte) 12;
        } else {
            dstIndex += this.recordName.writeWireFormat(dst, dstIndex);
        }
        writeInt2(this.recordType, dst, dstIndex);
        dstIndex += SRV_ERR;
        writeInt2(this.recordClass, dst, dstIndex);
        dstIndex += SRV_ERR;
        writeInt4(this.ttl, dst, dstIndex);
        dstIndex += QUESTION_OFFSET;
        this.rDataLength = writeRDataWireFormat(dst, dstIndex + SRV_ERR);
        writeInt2(this.rDataLength, dst, dstIndex);
        return (dstIndex + (this.rDataLength + SRV_ERR)) - start;
    }

    int readResourceRecordWireFormat(byte[] src, int srcIndex) {
        int start = srcIndex;
        if ((src[srcIndex] & 192) == 192) {
            this.recordName = this.questionName;
            srcIndex += SRV_ERR;
        } else {
            srcIndex += this.recordName.readWireFormat(src, srcIndex);
        }
        this.recordType = readInt2(src, srcIndex);
        srcIndex += SRV_ERR;
        this.recordClass = readInt2(src, srcIndex);
        srcIndex += SRV_ERR;
        this.ttl = readInt4(src, srcIndex);
        srcIndex += QUESTION_OFFSET;
        this.rDataLength = readInt2(src, srcIndex);
        srcIndex += SRV_ERR;
        this.addrEntry = new NbtAddress[(this.rDataLength / ANSWER_OFFSET)];
        int end = srcIndex + this.rDataLength;
        this.addrIndex = QUERY;
        while (srcIndex < end) {
            srcIndex += readRDataWireFormat(src, srcIndex);
            this.addrIndex += IN;
        }
        return srcIndex - start;
    }

    public String toString() {
        String opCodeString;
        String questionTypeString;
        String recordTypeString;
        String str;
        switch (this.opCode) {
            case QUERY /*0*/:
                opCodeString = "QUERY";
                break;
            case WACK /*7*/:
                opCodeString = "WACK";
                break;
            default:
                opCodeString = Integer.toString(this.opCode);
                break;
        }
        String str2;
        switch (this.resultCode) {
            case IN /*1*/:
                str2 = "FMT_ERR";
                break;
            case SRV_ERR /*2*/:
                str2 = "SRV_ERR";
                break;
            case QUESTION_OFFSET /*4*/:
                str2 = "IMP_ERR";
                break;
            case RFS_ERR /*5*/:
                str2 = "RFS_ERR";
                break;
            case ANSWER_OFFSET /*6*/:
                str2 = "ACT_ERR";
                break;
            case WACK /*7*/:
                str2 = "CFT_ERR";
                break;
            default:
                str2 = "0x" + Hexdump.toHexString(this.resultCode, (int) IN);
                break;
        }
        switch (this.questionType) {
            case NB /*32*/:
                questionTypeString = "NB";
                break;
            case NBSTAT /*33*/:
                questionTypeString = "NBSTAT";
                break;
            default:
                questionTypeString = "0x" + Hexdump.toHexString(this.questionType, (int) QUESTION_OFFSET);
                break;
        }
        switch (this.recordType) {
            case IN /*1*/:
                recordTypeString = "A";
                break;
            case SRV_ERR /*2*/:
                recordTypeString = "NS";
                break;
            case NULL /*10*/:
                recordTypeString = "NULL";
                break;
            case NB /*32*/:
                recordTypeString = "NB";
                break;
            case NBSTAT /*33*/:
                recordTypeString = "NBSTAT";
                break;
            default:
                recordTypeString = "0x" + Hexdump.toHexString(this.recordType, (int) QUESTION_OFFSET);
                break;
        }
        StringBuilder append = new StringBuilder().append("nameTrnId=").append(this.nameTrnId).append(",isResponse=").append(this.isResponse).append(",opCode=").append(opCodeString).append(",isAuthAnswer=").append(this.isAuthAnswer).append(",isTruncated=").append(this.isTruncated).append(",isRecurAvailable=").append(this.isRecurAvailable).append(",isRecurDesired=").append(this.isRecurDesired).append(",isBroadcast=").append(this.isBroadcast).append(",resultCode=").append(this.resultCode).append(",questionCount=").append(this.questionCount).append(",answerCount=").append(this.answerCount).append(",authorityCount=").append(this.authorityCount).append(",additionalCount=").append(this.additionalCount).append(",questionName=").append(this.questionName).append(",questionType=").append(questionTypeString).append(",questionClass=");
        if (this.questionClass == IN) {
            str = "IN";
        } else {
            str = "0x" + Hexdump.toHexString(this.questionClass, (int) QUESTION_OFFSET);
        }
        append = append.append(str).append(",recordName=").append(this.recordName).append(",recordType=").append(recordTypeString).append(",recordClass=");
        if (this.recordClass == IN) {
            str = "IN";
        } else {
            str = "0x" + Hexdump.toHexString(this.recordClass, (int) QUESTION_OFFSET);
        }
        return new String(append.append(str).append(",ttl=").append(this.ttl).append(",rDataLength=").append(this.rDataLength).toString());
    }
}
