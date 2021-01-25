package jcifs.netbios;

import java.net.InetAddress;
import jcifs.smb.SmbConstants;
import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public abstract class NameServicePacket {
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

    /* access modifiers changed from: package-private */
    public abstract int readBodyWireFormat(byte[] bArr, int i);

    /* access modifiers changed from: package-private */
    public abstract int readRDataWireFormat(byte[] bArr, int i);

    /* access modifiers changed from: package-private */
    public abstract int writeBodyWireFormat(byte[] bArr, int i);

    /* access modifiers changed from: package-private */
    public abstract int writeRDataWireFormat(byte[] bArr, int i);

    static void writeInt2(int val, byte[] dst, int dstIndex) {
        dst[dstIndex] = (byte) ((val >> 8) & 255);
        dst[dstIndex + 1] = (byte) (val & 255);
    }

    static void writeInt4(int val, byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) ((val >> 24) & 255);
        int dstIndex3 = dstIndex2 + 1;
        dst[dstIndex2] = (byte) ((val >> 16) & 255);
        dst[dstIndex3] = (byte) ((val >> 8) & 255);
        dst[dstIndex3 + 1] = (byte) (val & 255);
    }

    static int readInt2(byte[] src, int srcIndex) {
        return ((src[srcIndex] & 255) << 8) + (src[srcIndex + 1] & 255);
    }

    static int readInt4(byte[] src, int srcIndex) {
        return ((src[srcIndex] & 255) << 24) + ((src[srcIndex + 1] & 255) << 16) + ((src[srcIndex + 2] & 255) << 8) + (src[srcIndex + 3] & 255);
    }

    static int readNameTrnId(byte[] src, int srcIndex) {
        return readInt2(src, srcIndex);
    }

    NameServicePacket() {
    }

    /* access modifiers changed from: package-private */
    public int writeWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + writeHeaderWireFormat(dst, dstIndex);
        return (dstIndex2 + writeBodyWireFormat(dst, dstIndex2)) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    public int readWireFormat(byte[] src, int srcIndex) {
        int srcIndex2 = srcIndex + readHeaderWireFormat(src, srcIndex);
        return (srcIndex2 + readBodyWireFormat(src, srcIndex2)) - srcIndex;
    }

    /* access modifiers changed from: package-private */
    public int writeHeaderWireFormat(byte[] dst, int dstIndex) {
        int i = 128;
        int i2 = 0;
        writeInt2(this.nameTrnId, dst, dstIndex);
        dst[dstIndex + 2] = (byte) ((this.isRecurDesired ? 1 : 0) + ((this.opCode << 3) & 120) + (this.isResponse ? 128 : 0) + (this.isAuthAnswer ? 4 : 0) + (this.isTruncated ? 2 : 0));
        int i3 = dstIndex + 2 + 1;
        if (!this.isRecurAvailable) {
            i = 0;
        }
        if (this.isBroadcast) {
            i2 = 16;
        }
        dst[i3] = (byte) (i + i2 + (this.resultCode & 15));
        writeInt2(this.questionCount, dst, dstIndex + 4);
        writeInt2(this.answerCount, dst, dstIndex + 6);
        writeInt2(this.authorityCount, dst, dstIndex + 8);
        writeInt2(this.additionalCount, dst, dstIndex + 10);
        return 12;
    }

    /* access modifiers changed from: package-private */
    public int readHeaderWireFormat(byte[] src, int srcIndex) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        boolean z5 = false;
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
            z2 = false;
        } else {
            z2 = true;
        }
        this.isTruncated = z2;
        if ((src[srcIndex + 2] & 1) == 0) {
            z3 = false;
        } else {
            z3 = true;
        }
        this.isRecurDesired = z3;
        if ((src[srcIndex + 2 + 1] & 128) == 0) {
            z4 = false;
        } else {
            z4 = true;
        }
        this.isRecurAvailable = z4;
        if ((src[srcIndex + 2 + 1] & 16) != 0) {
            z5 = true;
        }
        this.isBroadcast = z5;
        this.resultCode = src[srcIndex + 2 + 1] & 15;
        this.questionCount = readInt2(src, srcIndex + 4);
        this.answerCount = readInt2(src, srcIndex + 6);
        this.authorityCount = readInt2(src, srcIndex + 8);
        this.additionalCount = readInt2(src, srcIndex + 10);
        return 12;
    }

    /* access modifiers changed from: package-private */
    public int writeQuestionSectionWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + this.questionName.writeWireFormat(dst, dstIndex);
        writeInt2(this.questionType, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 2;
        writeInt2(this.questionClass, dst, dstIndex3);
        return (dstIndex3 + 2) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    public int readQuestionSectionWireFormat(byte[] src, int srcIndex) {
        int srcIndex2 = srcIndex + this.questionName.readWireFormat(src, srcIndex);
        this.questionType = readInt2(src, srcIndex2);
        int srcIndex3 = srcIndex2 + 2;
        this.questionClass = readInt2(src, srcIndex3);
        return (srcIndex3 + 2) - srcIndex;
    }

    /* access modifiers changed from: package-private */
    public int writeResourceRecordWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2;
        if (this.recordName == this.questionName) {
            int dstIndex3 = dstIndex + 1;
            dst[dstIndex] = -64;
            dstIndex2 = dstIndex3 + 1;
            dst[dstIndex3] = 12;
        } else {
            dstIndex2 = dstIndex + this.recordName.writeWireFormat(dst, dstIndex);
        }
        writeInt2(this.recordType, dst, dstIndex2);
        int dstIndex4 = dstIndex2 + 2;
        writeInt2(this.recordClass, dst, dstIndex4);
        int dstIndex5 = dstIndex4 + 2;
        writeInt4(this.ttl, dst, dstIndex5);
        int dstIndex6 = dstIndex5 + 4;
        this.rDataLength = writeRDataWireFormat(dst, dstIndex6 + 2);
        writeInt2(this.rDataLength, dst, dstIndex6);
        return (dstIndex6 + (this.rDataLength + 2)) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    public int readResourceRecordWireFormat(byte[] src, int srcIndex) {
        int srcIndex2;
        if ((src[srcIndex] & 192) == 192) {
            this.recordName = this.questionName;
            srcIndex2 = srcIndex + 2;
        } else {
            srcIndex2 = srcIndex + this.recordName.readWireFormat(src, srcIndex);
        }
        this.recordType = readInt2(src, srcIndex2);
        int srcIndex3 = srcIndex2 + 2;
        this.recordClass = readInt2(src, srcIndex3);
        int srcIndex4 = srcIndex3 + 2;
        this.ttl = readInt4(src, srcIndex4);
        int srcIndex5 = srcIndex4 + 4;
        this.rDataLength = readInt2(src, srcIndex5);
        int srcIndex6 = srcIndex5 + 2;
        this.addrEntry = new NbtAddress[(this.rDataLength / 6)];
        int end = srcIndex6 + this.rDataLength;
        this.addrIndex = 0;
        while (srcIndex6 < end) {
            srcIndex6 += readRDataWireFormat(src, srcIndex6);
            this.addrIndex++;
        }
        return srcIndex6 - srcIndex;
    }

    public String toString() {
        String opCodeString;
        String questionTypeString;
        String recordTypeString;
        String str;
        String str2;
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
        switch (this.resultCode) {
            case 1:
                break;
            case 2:
                break;
            case 3:
            default:
                String str3 = "0x" + Hexdump.toHexString(this.resultCode, 1);
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
        }
        switch (this.questionType) {
            case 32:
                questionTypeString = "NB";
                break;
            case NBSTAT /* 33 */:
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
            case SmbConstants.DEFAULT_MAX_MPX_COUNT /* 10 */:
                recordTypeString = "NULL";
                break;
            case 32:
                recordTypeString = "NB";
                break;
            case NBSTAT /* 33 */:
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
        StringBuilder append2 = append.append(str).append(",recordName=").append(this.recordName).append(",recordType=").append(recordTypeString).append(",recordClass=");
        if (this.recordClass == 1) {
            str2 = "IN";
        } else {
            str2 = "0x" + Hexdump.toHexString(this.recordClass, 4);
        }
        return new String(append2.append(str2).append(",ttl=").append(this.ttl).append(",rDataLength=").append(this.rDataLength).toString());
    }
}
