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
    boolean isBroadcast = true;
    boolean isRecurAvailable;
    boolean isRecurDesired = true;
    boolean isResponse;
    boolean isTruncated;
    int nameTrnId;
    int opCode;
    int questionClass = 1;
    int questionCount = 1;
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
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) ((val >> 8) & 255);
        dst[dstIndex2] = (byte) (val & 255);
    }

    static void writeInt4(int val, byte[] dst, int dstIndex) {
        int i = dstIndex + 1;
        dst[dstIndex] = (byte) ((val >> 24) & 255);
        dstIndex = i + 1;
        dst[i] = (byte) ((val >> 16) & 255);
        i = dstIndex + 1;
        dst[dstIndex] = (byte) ((val >> 8) & 255);
        dst[i] = (byte) (val & 255);
    }

    static int readInt2(byte[] src, int srcIndex) {
        return ((src[srcIndex] & 255) << 8) + (src[srcIndex + 1] & 255);
    }

    static int readInt4(byte[] src, int srcIndex) {
        return ((((src[srcIndex] & 255) << 24) + ((src[srcIndex + 1] & 255) << 16)) + ((src[srcIndex + 2] & 255) << 8)) + (src[srcIndex + 3] & 255);
    }

    static int readNameTrnId(byte[] src, int srcIndex) {
        return readInt2(src, srcIndex);
    }

    NameServicePacket() {
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
        int i = 128;
        int i2 = 0;
        int start = dstIndex;
        writeInt2(this.nameTrnId, dst, dstIndex);
        dst[dstIndex + 2] = (byte) ((this.isRecurDesired ? 1 : 0) + (((((this.opCode << 3) & 120) + (this.isResponse ? 128 : 0)) + (this.isAuthAnswer ? 4 : 0)) + (this.isTruncated ? 2 : 0)));
        int i3 = (dstIndex + 2) + 1;
        if (!this.isRecurAvailable) {
            i = 0;
        }
        if (this.isBroadcast) {
            i2 = 16;
        }
        dst[i3] = (byte) ((i + i2) + (this.resultCode & 15));
        writeInt2(this.questionCount, dst, start + 4);
        writeInt2(this.answerCount, dst, start + 6);
        writeInt2(this.authorityCount, dst, start + 8);
        writeInt2(this.additionalCount, dst, start + 10);
        return 12;
    }

    int readHeaderWireFormat(byte[] src, int srcIndex) {
        boolean z;
        boolean z2 = false;
        this.nameTrnId = readInt2(src, srcIndex);
        this.isResponse = (src[srcIndex + 2] & 128) != 0;
        this.opCode = (src[srcIndex + 2] & 120) >> 3;
        if ((src[srcIndex + 2] & 4) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.isAuthAnswer = z;
        if ((src[srcIndex + 2] & 2) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.isTruncated = z;
        if ((src[srcIndex + 2] & 1) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.isRecurDesired = z;
        if ((src[(srcIndex + 2) + 1] & 128) == 0) {
            z = false;
        } else {
            z = true;
        }
        this.isRecurAvailable = z;
        if ((src[(srcIndex + 2) + 1] & 16) != 0) {
            z2 = true;
        }
        this.isBroadcast = z2;
        this.resultCode = src[(srcIndex + 2) + 1] & 15;
        this.questionCount = readInt2(src, srcIndex + 4);
        this.answerCount = readInt2(src, srcIndex + 6);
        this.authorityCount = readInt2(src, srcIndex + 8);
        this.additionalCount = readInt2(src, srcIndex + 10);
        return 12;
    }

    int writeQuestionSectionWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        dstIndex += this.questionName.writeWireFormat(dst, dstIndex);
        writeInt2(this.questionType, dst, dstIndex);
        dstIndex += 2;
        writeInt2(this.questionClass, dst, dstIndex);
        return (dstIndex + 2) - start;
    }

    int readQuestionSectionWireFormat(byte[] src, int srcIndex) {
        int start = srcIndex;
        srcIndex += this.questionName.readWireFormat(src, srcIndex);
        this.questionType = readInt2(src, srcIndex);
        srcIndex += 2;
        this.questionClass = readInt2(src, srcIndex);
        return (srcIndex + 2) - start;
    }

    int writeResourceRecordWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        if (this.recordName == this.questionName) {
            int i = dstIndex + 1;
            dst[dstIndex] = (byte) -64;
            dstIndex = i + 1;
            dst[i] = (byte) 12;
        } else {
            dstIndex += this.recordName.writeWireFormat(dst, dstIndex);
        }
        writeInt2(this.recordType, dst, dstIndex);
        dstIndex += 2;
        writeInt2(this.recordClass, dst, dstIndex);
        dstIndex += 2;
        writeInt4(this.ttl, dst, dstIndex);
        dstIndex += 4;
        this.rDataLength = writeRDataWireFormat(dst, dstIndex + 2);
        writeInt2(this.rDataLength, dst, dstIndex);
        return (dstIndex + (this.rDataLength + 2)) - start;
    }

    int readResourceRecordWireFormat(byte[] src, int srcIndex) {
        int start = srcIndex;
        if ((src[srcIndex] & 192) == 192) {
            this.recordName = this.questionName;
            srcIndex += 2;
        } else {
            srcIndex += this.recordName.readWireFormat(src, srcIndex);
        }
        this.recordType = readInt2(src, srcIndex);
        srcIndex += 2;
        this.recordClass = readInt2(src, srcIndex);
        srcIndex += 2;
        this.ttl = readInt4(src, srcIndex);
        srcIndex += 4;
        this.rDataLength = readInt2(src, srcIndex);
        srcIndex += 2;
        this.addrEntry = new NbtAddress[(this.rDataLength / 6)];
        int end = srcIndex + this.rDataLength;
        this.addrIndex = 0;
        while (srcIndex < end) {
            srcIndex += readRDataWireFormat(src, srcIndex);
            this.addrIndex++;
        }
        return srcIndex - start;
    }

    public String toString() {
        String opCodeString;
        String questionTypeString;
        String recordTypeString;
        String str;
        switch (this.opCode) {
            case 0:
                opCodeString = "QUERY";
                break;
            case 7:
                opCodeString = "WACK";
                break;
            default:
                opCodeString = Integer.toString(this.opCode);
                break;
        }
        String str2;
        switch (this.resultCode) {
            case 1:
                str2 = "FMT_ERR";
                break;
            case 2:
                str2 = "SRV_ERR";
                break;
            case 4:
                str2 = "IMP_ERR";
                break;
            case 5:
                str2 = "RFS_ERR";
                break;
            case 6:
                str2 = "ACT_ERR";
                break;
            case 7:
                str2 = "CFT_ERR";
                break;
            default:
                str2 = "0x" + Hexdump.toHexString(this.resultCode, 1);
                break;
        }
        switch (this.questionType) {
            case 32:
                questionTypeString = "NB";
                break;
            case NBSTAT /*33*/:
                questionTypeString = "NBSTAT";
                break;
            default:
                questionTypeString = "0x" + Hexdump.toHexString(this.questionType, 4);
                break;
        }
        switch (this.recordType) {
            case 1:
                recordTypeString = "A";
                break;
            case 2:
                recordTypeString = "NS";
                break;
            case SmbConstants.DEFAULT_MAX_MPX_COUNT /*10*/:
                recordTypeString = "NULL";
                break;
            case 32:
                recordTypeString = "NB";
                break;
            case NBSTAT /*33*/:
                recordTypeString = "NBSTAT";
                break;
            default:
                recordTypeString = "0x" + Hexdump.toHexString(this.recordType, 4);
                break;
        }
        StringBuilder append = new StringBuilder().append("nameTrnId=").append(this.nameTrnId).append(",isResponse=").append(this.isResponse).append(",opCode=").append(opCodeString).append(",isAuthAnswer=").append(this.isAuthAnswer).append(",isTruncated=").append(this.isTruncated).append(",isRecurAvailable=").append(this.isRecurAvailable).append(",isRecurDesired=").append(this.isRecurDesired).append(",isBroadcast=").append(this.isBroadcast).append(",resultCode=").append(this.resultCode).append(",questionCount=").append(this.questionCount).append(",answerCount=").append(this.answerCount).append(",authorityCount=").append(this.authorityCount).append(",additionalCount=").append(this.additionalCount).append(",questionName=").append(this.questionName).append(",questionType=").append(questionTypeString).append(",questionClass=");
        if (this.questionClass == 1) {
            str = "IN";
        } else {
            str = "0x" + Hexdump.toHexString(this.questionClass, 4);
        }
        append = append.append(str).append(",recordName=").append(this.recordName).append(",recordType=").append(recordTypeString).append(",recordClass=");
        if (this.recordClass == 1) {
            str = "IN";
        } else {
            str = "0x" + Hexdump.toHexString(this.recordClass, 4);
        }
        return new String(append.append(str).append(",ttl=").append(this.ttl).append(",rDataLength=").append(this.rDataLength).toString());
    }
}
