package jcifs.smb;

import java.util.Enumeration;
import jcifs.dcerpc.msrpc.samr;
import jcifs.util.Hexdump;

abstract class SmbComTransaction extends ServerMessageBlock implements Enumeration {
    private static final int DEFAULT_MAX_DATA_COUNT = 0;
    private static final int DISCONNECT_TID = 1;
    static final int NET_SERVER_ENUM2 = 104;
    static final int NET_SERVER_ENUM3 = 215;
    static final int NET_SHARE_ENUM = 0;
    private static final int ONE_WAY_TRANSACTION = 2;
    private static final int PADDING_SIZE = 2;
    private static final int PRIMARY_SETUP_OFFSET = 61;
    private static final int SECONDARY_PARAMETER_OFFSET = 51;
    static final byte TRANS2_FIND_FIRST2 = (byte) 1;
    static final byte TRANS2_FIND_NEXT2 = (byte) 2;
    static final byte TRANS2_GET_DFS_REFERRAL = (byte) 16;
    static final byte TRANS2_QUERY_FS_INFORMATION = (byte) 3;
    static final byte TRANS2_QUERY_PATH_INFORMATION = (byte) 5;
    static final byte TRANS2_SET_FILE_INFORMATION = (byte) 8;
    static final int TRANSACTION_BUF_SIZE = 65535;
    static final byte TRANS_CALL_NAMED_PIPE = (byte) 84;
    static final byte TRANS_PEEK_NAMED_PIPE = (byte) 35;
    static final byte TRANS_TRANSACT_NAMED_PIPE = (byte) 38;
    static final byte TRANS_WAIT_NAMED_PIPE = (byte) 83;
    private int bufDataOffset;
    private int bufParameterOffset;
    protected int dataCount;
    protected int dataDisplacement;
    protected int dataOffset;
    private int fid;
    private int flags;
    private boolean hasMore;
    private boolean isPrimary;
    int maxBufferSize;
    int maxDataCount;
    int maxParameterCount;
    byte maxSetupCount;
    String name;
    private int pad;
    private int pad1;
    protected int parameterCount;
    protected int parameterDisplacement;
    protected int parameterOffset;
    protected int primarySetupOffset;
    protected int secondaryParameterOffset;
    int setupCount;
    byte subCommand;
    int timeout;
    int totalDataCount;
    int totalParameterCount;
    byte[] txn_buf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.SmbComTransaction.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.SmbComTransaction.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.SmbComTransaction.<clinit>():void");
    }

    abstract int readDataWireFormat(byte[] bArr, int i, int i2);

    abstract int readParametersWireFormat(byte[] bArr, int i, int i2);

    abstract int readSetupWireFormat(byte[] bArr, int i, int i2);

    abstract int writeDataWireFormat(byte[] bArr, int i);

    abstract int writeParametersWireFormat(byte[] bArr, int i);

    abstract int writeSetupWireFormat(byte[] bArr, int i);

    SmbComTransaction() {
        this.flags = NET_SHARE_ENUM;
        this.pad = NET_SHARE_ENUM;
        this.pad1 = NET_SHARE_ENUM;
        this.hasMore = true;
        this.isPrimary = true;
        this.maxDataCount = DEFAULT_MAX_DATA_COUNT;
        this.timeout = NET_SHARE_ENUM;
        this.setupCount = DISCONNECT_TID;
        this.name = "";
        this.maxParameterCount = samr.ACB_AUTOLOCK;
        this.primarySetupOffset = PRIMARY_SETUP_OFFSET;
        this.secondaryParameterOffset = SECONDARY_PARAMETER_OFFSET;
    }

    void reset() {
        super.reset();
        this.hasMore = true;
        this.isPrimary = true;
    }

    void reset(int key, String lastName) {
        reset();
    }

    public boolean hasMoreElements() {
        return this.hasMore;
    }

    public Object nextElement() {
        int i;
        int available;
        if (this.isPrimary) {
            this.isPrimary = false;
            this.parameterOffset = (this.primarySetupOffset + (this.setupCount * PADDING_SIZE)) + PADDING_SIZE;
            if (this.command != (byte) -96) {
                if (this.command == 37 && !isResponse()) {
                    this.parameterOffset += stringWireLength(this.name, this.parameterOffset);
                }
            } else if (this.command == (byte) -96) {
                this.parameterOffset += PADDING_SIZE;
            }
            this.pad = this.parameterOffset % PADDING_SIZE;
            if (this.pad == 0) {
                i = NET_SHARE_ENUM;
            } else {
                i = 2 - this.pad;
            }
            this.pad = i;
            this.parameterOffset += this.pad;
            this.totalParameterCount = writeParametersWireFormat(this.txn_buf, this.bufParameterOffset);
            this.bufDataOffset = this.totalParameterCount;
            available = this.maxBufferSize - this.parameterOffset;
            this.parameterCount = Math.min(this.totalParameterCount, available);
            available -= this.parameterCount;
            this.dataOffset = this.parameterOffset + this.parameterCount;
            this.pad1 = this.dataOffset % PADDING_SIZE;
            this.pad1 = this.pad1 == 0 ? NET_SHARE_ENUM : 2 - this.pad1;
            this.dataOffset += this.pad1;
            this.totalDataCount = writeDataWireFormat(this.txn_buf, this.bufDataOffset);
            this.dataCount = Math.min(this.totalDataCount, available);
        } else {
            if (this.command != (byte) -96) {
                this.command = TRANS_TRANSACT_NAMED_PIPE;
            } else {
                this.command = (byte) -95;
            }
            this.parameterOffset = SECONDARY_PARAMETER_OFFSET;
            if (this.totalParameterCount - this.parameterDisplacement > 0) {
                this.pad = this.parameterOffset % PADDING_SIZE;
                if (this.pad == 0) {
                    i = NET_SHARE_ENUM;
                } else {
                    i = 2 - this.pad;
                }
                this.pad = i;
                this.parameterOffset += this.pad;
            }
            this.parameterDisplacement += this.parameterCount;
            available = (this.maxBufferSize - this.parameterOffset) - this.pad;
            this.parameterCount = Math.min(this.totalParameterCount - this.parameterDisplacement, available);
            available -= this.parameterCount;
            this.dataOffset = this.parameterOffset + this.parameterCount;
            this.pad1 = this.dataOffset % PADDING_SIZE;
            this.pad1 = this.pad1 == 0 ? NET_SHARE_ENUM : 2 - this.pad1;
            this.dataOffset += this.pad1;
            this.dataDisplacement += this.dataCount;
            this.dataCount = Math.min(this.totalDataCount - this.dataDisplacement, available - this.pad1);
        }
        if (this.parameterDisplacement + this.parameterCount >= this.totalParameterCount && this.dataDisplacement + this.dataCount >= this.totalDataCount) {
            this.hasMore = false;
        }
        return this;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.totalParameterCount, dst, dstIndex);
        dstIndex += PADDING_SIZE;
        ServerMessageBlock.writeInt2((long) this.totalDataCount, dst, dstIndex);
        dstIndex += PADDING_SIZE;
        if (this.command != TRANS_TRANSACT_NAMED_PIPE) {
            ServerMessageBlock.writeInt2((long) this.maxParameterCount, dst, dstIndex);
            dstIndex += PADDING_SIZE;
            ServerMessageBlock.writeInt2((long) this.maxDataCount, dst, dstIndex);
            dstIndex += PADDING_SIZE;
            int dstIndex2 = dstIndex + DISCONNECT_TID;
            dst[dstIndex] = this.maxSetupCount;
            dstIndex = dstIndex2 + DISCONNECT_TID;
            dst[dstIndex2] = (byte) 0;
            ServerMessageBlock.writeInt2((long) this.flags, dst, dstIndex);
            dstIndex += PADDING_SIZE;
            ServerMessageBlock.writeInt4((long) this.timeout, dst, dstIndex);
            dstIndex += 4;
            dstIndex2 = dstIndex + DISCONNECT_TID;
            dst[dstIndex] = (byte) 0;
            dstIndex = dstIndex2 + DISCONNECT_TID;
            dst[dstIndex2] = (byte) 0;
        }
        ServerMessageBlock.writeInt2((long) this.parameterCount, dst, dstIndex);
        dstIndex += PADDING_SIZE;
        ServerMessageBlock.writeInt2((long) this.parameterOffset, dst, dstIndex);
        dstIndex += PADDING_SIZE;
        if (this.command == TRANS_TRANSACT_NAMED_PIPE) {
            ServerMessageBlock.writeInt2((long) this.parameterDisplacement, dst, dstIndex);
            dstIndex += PADDING_SIZE;
        }
        ServerMessageBlock.writeInt2((long) this.dataCount, dst, dstIndex);
        dstIndex += PADDING_SIZE;
        ServerMessageBlock.writeInt2((long) (this.dataCount == 0 ? NET_SHARE_ENUM : this.dataOffset), dst, dstIndex);
        dstIndex += PADDING_SIZE;
        if (this.command == TRANS_TRANSACT_NAMED_PIPE) {
            ServerMessageBlock.writeInt2((long) this.dataDisplacement, dst, dstIndex);
            dstIndex += PADDING_SIZE;
        } else {
            dstIndex2 = dstIndex + DISCONNECT_TID;
            dst[dstIndex] = (byte) this.setupCount;
            dstIndex = dstIndex2 + DISCONNECT_TID;
            dst[dstIndex2] = (byte) 0;
            dstIndex += writeSetupWireFormat(dst, dstIndex);
        }
        return dstIndex - start;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int p;
        int dstIndex2;
        int start = dstIndex;
        int p2 = this.pad;
        if (this.command == 37 && !isResponse()) {
            dstIndex += writeString(this.name, dst, dstIndex);
        }
        if (this.parameterCount > 0) {
            p = p2;
            dstIndex2 = dstIndex;
            while (true) {
                p2 = p - 1;
                if (p <= 0) {
                    break;
                }
                dstIndex = dstIndex2 + DISCONNECT_TID;
                dst[dstIndex2] = (byte) 0;
                p = p2;
                dstIndex2 = dstIndex;
            }
            System.arraycopy(this.txn_buf, this.bufParameterOffset, dst, dstIndex2, this.parameterCount);
            dstIndex = dstIndex2 + this.parameterCount;
        }
        if (this.dataCount > 0) {
            p = this.pad1;
            dstIndex2 = dstIndex;
            while (true) {
                p2 = p - 1;
                if (p <= 0) {
                    break;
                }
                dstIndex = dstIndex2 + DISCONNECT_TID;
                dst[dstIndex2] = (byte) 0;
                p = p2;
                dstIndex2 = dstIndex;
            }
            System.arraycopy(this.txn_buf, this.bufDataOffset, dst, dstIndex2, this.dataCount);
            this.bufDataOffset += this.dataCount;
            dstIndex = dstIndex2 + this.dataCount;
        }
        return dstIndex - start;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return NET_SHARE_ENUM;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return NET_SHARE_ENUM;
    }

    public String toString() {
        return new String(super.toString() + ",totalParameterCount=" + this.totalParameterCount + ",totalDataCount=" + this.totalDataCount + ",maxParameterCount=" + this.maxParameterCount + ",maxDataCount=" + this.maxDataCount + ",maxSetupCount=" + this.maxSetupCount + ",flags=0x" + Hexdump.toHexString(this.flags, (int) PADDING_SIZE) + ",timeout=" + this.timeout + ",parameterCount=" + this.parameterCount + ",parameterOffset=" + this.parameterOffset + ",parameterDisplacement=" + this.parameterDisplacement + ",dataCount=" + this.dataCount + ",dataOffset=" + this.dataOffset + ",dataDisplacement=" + this.dataDisplacement + ",setupCount=" + this.setupCount + ",pad=" + this.pad + ",pad1=" + this.pad1);
    }
}
