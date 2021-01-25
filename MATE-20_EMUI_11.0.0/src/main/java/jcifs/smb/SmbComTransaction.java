package jcifs.smb;

import java.util.Enumeration;
import jcifs.Config;
import jcifs.dcerpc.msrpc.samr;
import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public abstract class SmbComTransaction extends ServerMessageBlock implements Enumeration {
    private static final int DEFAULT_MAX_DATA_COUNT = (Config.getInt("jcifs.smb.client.transaction_buf_size", TRANSACTION_BUF_SIZE) - 512);
    private static final int DISCONNECT_TID = 1;
    static final int NET_SERVER_ENUM2 = 104;
    static final int NET_SERVER_ENUM3 = 215;
    static final int NET_SHARE_ENUM = 0;
    private static final int ONE_WAY_TRANSACTION = 2;
    private static final int PADDING_SIZE = 2;
    private static final int PRIMARY_SETUP_OFFSET = 61;
    private static final int SECONDARY_PARAMETER_OFFSET = 51;
    static final byte TRANS2_FIND_FIRST2 = 1;
    static final byte TRANS2_FIND_NEXT2 = 2;
    static final byte TRANS2_GET_DFS_REFERRAL = 16;
    static final byte TRANS2_QUERY_FS_INFORMATION = 3;
    static final byte TRANS2_QUERY_PATH_INFORMATION = 5;
    static final byte TRANS2_SET_FILE_INFORMATION = 8;
    static final int TRANSACTION_BUF_SIZE = 65535;
    static final byte TRANS_CALL_NAMED_PIPE = 84;
    static final byte TRANS_PEEK_NAMED_PIPE = 35;
    static final byte TRANS_TRANSACT_NAMED_PIPE = 38;
    static final byte TRANS_WAIT_NAMED_PIPE = 83;
    private int bufDataOffset;
    private int bufParameterOffset;
    protected int dataCount;
    protected int dataDisplacement;
    protected int dataOffset;
    private int fid;
    private int flags = 0;
    private boolean hasMore = true;
    private boolean isPrimary = true;
    int maxBufferSize;
    int maxDataCount = DEFAULT_MAX_DATA_COUNT;
    int maxParameterCount = samr.ACB_AUTOLOCK;
    byte maxSetupCount;
    String name = "";
    private int pad = 0;
    private int pad1 = 0;
    protected int parameterCount;
    protected int parameterDisplacement;
    protected int parameterOffset;
    protected int primarySetupOffset = PRIMARY_SETUP_OFFSET;
    protected int secondaryParameterOffset = SECONDARY_PARAMETER_OFFSET;
    int setupCount = 1;
    byte subCommand;
    int timeout = 0;
    int totalDataCount;
    int totalParameterCount;
    byte[] txn_buf;

    /* access modifiers changed from: package-private */
    public abstract int readDataWireFormat(byte[] bArr, int i, int i2);

    /* access modifiers changed from: package-private */
    public abstract int readParametersWireFormat(byte[] bArr, int i, int i2);

    /* access modifiers changed from: package-private */
    public abstract int readSetupWireFormat(byte[] bArr, int i, int i2);

    /* access modifiers changed from: package-private */
    public abstract int writeDataWireFormat(byte[] bArr, int i);

    /* access modifiers changed from: package-private */
    public abstract int writeParametersWireFormat(byte[] bArr, int i);

    /* access modifiers changed from: package-private */
    public abstract int writeSetupWireFormat(byte[] bArr, int i);

    SmbComTransaction() {
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public void reset() {
        super.reset();
        this.hasMore = true;
        this.isPrimary = true;
    }

    /* access modifiers changed from: package-private */
    public void reset(int key, String lastName) {
        reset();
    }

    @Override // java.util.Enumeration
    public boolean hasMoreElements() {
        return this.hasMore;
    }

    @Override // java.util.Enumeration
    public Object nextElement() {
        int i;
        int i2;
        if (this.isPrimary) {
            this.isPrimary = false;
            this.parameterOffset = this.primarySetupOffset + (this.setupCount * 2) + 2;
            if (this.command != -96) {
                if (this.command == 37 && !isResponse()) {
                    this.parameterOffset += stringWireLength(this.name, this.parameterOffset);
                }
            } else if (this.command == -96) {
                this.parameterOffset += 2;
            }
            this.pad = this.parameterOffset % 2;
            if (this.pad == 0) {
                i2 = 0;
            } else {
                i2 = 2 - this.pad;
            }
            this.pad = i2;
            this.parameterOffset += this.pad;
            this.totalParameterCount = writeParametersWireFormat(this.txn_buf, this.bufParameterOffset);
            this.bufDataOffset = this.totalParameterCount;
            int available = this.maxBufferSize - this.parameterOffset;
            this.parameterCount = Math.min(this.totalParameterCount, available);
            int available2 = available - this.parameterCount;
            this.dataOffset = this.parameterOffset + this.parameterCount;
            this.pad1 = this.dataOffset % 2;
            this.pad1 = this.pad1 == 0 ? 0 : 2 - this.pad1;
            this.dataOffset += this.pad1;
            this.totalDataCount = writeDataWireFormat(this.txn_buf, this.bufDataOffset);
            this.dataCount = Math.min(this.totalDataCount, available2);
        } else {
            if (this.command != -96) {
                this.command = TRANS_TRANSACT_NAMED_PIPE;
            } else {
                this.command = -95;
            }
            this.parameterOffset = SECONDARY_PARAMETER_OFFSET;
            if (this.totalParameterCount - this.parameterDisplacement > 0) {
                this.pad = this.parameterOffset % 2;
                if (this.pad == 0) {
                    i = 0;
                } else {
                    i = 2 - this.pad;
                }
                this.pad = i;
                this.parameterOffset += this.pad;
            }
            this.parameterDisplacement += this.parameterCount;
            int available3 = (this.maxBufferSize - this.parameterOffset) - this.pad;
            this.parameterCount = Math.min(this.totalParameterCount - this.parameterDisplacement, available3);
            int available4 = available3 - this.parameterCount;
            this.dataOffset = this.parameterOffset + this.parameterCount;
            this.pad1 = this.dataOffset % 2;
            this.pad1 = this.pad1 == 0 ? 0 : 2 - this.pad1;
            this.dataOffset += this.pad1;
            this.dataDisplacement += this.dataCount;
            this.dataCount = Math.min(this.totalDataCount - this.dataDisplacement, available4 - this.pad1);
        }
        if (this.parameterDisplacement + this.parameterCount >= this.totalParameterCount && this.dataDisplacement + this.dataCount >= this.totalDataCount) {
            this.hasMore = false;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2;
        writeInt2((long) this.totalParameterCount, dst, dstIndex);
        int dstIndex3 = dstIndex + 2;
        writeInt2((long) this.totalDataCount, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 2;
        if (this.command != 38) {
            writeInt2((long) this.maxParameterCount, dst, dstIndex4);
            int dstIndex5 = dstIndex4 + 2;
            writeInt2((long) this.maxDataCount, dst, dstIndex5);
            int dstIndex6 = dstIndex5 + 2;
            int dstIndex7 = dstIndex6 + 1;
            dst[dstIndex6] = this.maxSetupCount;
            int dstIndex8 = dstIndex7 + 1;
            dst[dstIndex7] = 0;
            writeInt2((long) this.flags, dst, dstIndex8);
            int dstIndex9 = dstIndex8 + 2;
            writeInt4((long) this.timeout, dst, dstIndex9);
            int dstIndex10 = dstIndex9 + 4;
            int dstIndex11 = dstIndex10 + 1;
            dst[dstIndex10] = 0;
            dstIndex4 = dstIndex11 + 1;
            dst[dstIndex11] = 0;
        }
        writeInt2((long) this.parameterCount, dst, dstIndex4);
        int dstIndex12 = dstIndex4 + 2;
        writeInt2((long) this.parameterOffset, dst, dstIndex12);
        int dstIndex13 = dstIndex12 + 2;
        if (this.command == 38) {
            writeInt2((long) this.parameterDisplacement, dst, dstIndex13);
            dstIndex13 += 2;
        }
        writeInt2((long) this.dataCount, dst, dstIndex13);
        int dstIndex14 = dstIndex13 + 2;
        writeInt2((long) (this.dataCount == 0 ? 0 : this.dataOffset), dst, dstIndex14);
        int dstIndex15 = dstIndex14 + 2;
        if (this.command == 38) {
            writeInt2((long) this.dataDisplacement, dst, dstIndex15);
            dstIndex2 = dstIndex15 + 2;
        } else {
            int dstIndex16 = dstIndex15 + 1;
            dst[dstIndex15] = (byte) this.setupCount;
            int dstIndex17 = dstIndex16 + 1;
            dst[dstIndex16] = 0;
            dstIndex2 = dstIndex17 + writeSetupWireFormat(dst, dstIndex17);
        }
        return dstIndex2 - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int p = this.pad;
        if (this.command == 37 && !isResponse()) {
            dstIndex += writeString(this.name, dst, dstIndex);
        }
        if (this.parameterCount > 0) {
            while (true) {
                p--;
                if (p <= 0) {
                    break;
                }
                dstIndex++;
                dst[dstIndex] = 0;
            }
            System.arraycopy(this.txn_buf, this.bufParameterOffset, dst, dstIndex, this.parameterCount);
            dstIndex += this.parameterCount;
        }
        if (this.dataCount > 0) {
            int p2 = this.pad1;
            while (true) {
                p2--;
                if (p2 <= 0) {
                    break;
                }
                dstIndex++;
                dst[dstIndex] = 0;
            }
            System.arraycopy(this.txn_buf, this.bufDataOffset, dst, dstIndex, this.dataCount);
            this.bufDataOffset += this.dataCount;
            dstIndex += this.dataCount;
        }
        return dstIndex - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    @Override // jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String(super.toString() + ",totalParameterCount=" + this.totalParameterCount + ",totalDataCount=" + this.totalDataCount + ",maxParameterCount=" + this.maxParameterCount + ",maxDataCount=" + this.maxDataCount + ",maxSetupCount=" + ((int) this.maxSetupCount) + ",flags=0x" + Hexdump.toHexString(this.flags, 2) + ",timeout=" + this.timeout + ",parameterCount=" + this.parameterCount + ",parameterOffset=" + this.parameterOffset + ",parameterDisplacement=" + this.parameterDisplacement + ",dataCount=" + this.dataCount + ",dataOffset=" + this.dataOffset + ",dataDisplacement=" + this.dataDisplacement + ",setupCount=" + this.setupCount + ",pad=" + this.pad + ",pad1=" + this.pad1);
    }
}
