package jcifs.smb;

import java.util.Enumeration;
import jcifs.util.LogStream;

/* access modifiers changed from: package-private */
public abstract class SmbComTransactionResponse extends ServerMessageBlock implements Enumeration {
    private static final int DISCONNECT_TID = 1;
    private static final int ONE_WAY_TRANSACTION = 2;
    private static final int SETUP_OFFSET = 61;
    protected int bufDataStart;
    protected int bufParameterStart;
    int dataCount;
    protected int dataDisplacement;
    private boolean dataDone;
    protected int dataOffset;
    boolean hasMore = true;
    boolean isPrimary = true;
    int numEntries;
    private int pad;
    private int pad1;
    protected int parameterCount;
    protected int parameterDisplacement;
    protected int parameterOffset;
    private boolean parametersDone;
    FileEntry[] results;
    protected int setupCount;
    int status;
    byte subCommand;
    protected int totalDataCount;
    protected int totalParameterCount;
    byte[] txn_buf = null;

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

    SmbComTransactionResponse() {
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public void reset() {
        super.reset();
        this.bufDataStart = 0;
        this.hasMore = true;
        this.isPrimary = true;
        this.dataDone = false;
        this.parametersDone = false;
    }

    @Override // java.util.Enumeration
    public boolean hasMoreElements() {
        return this.errorCode == 0 && this.hasMore;
    }

    @Override // java.util.Enumeration
    public Object nextElement() {
        if (this.isPrimary) {
            this.isPrimary = false;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        this.totalParameterCount = readInt2(buffer, bufferIndex);
        if (this.bufDataStart == 0) {
            this.bufDataStart = this.totalParameterCount;
        }
        int bufferIndex2 = bufferIndex + 2;
        this.totalDataCount = readInt2(buffer, bufferIndex2);
        int bufferIndex3 = bufferIndex2 + 4;
        this.parameterCount = readInt2(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 2;
        this.parameterOffset = readInt2(buffer, bufferIndex4);
        int bufferIndex5 = bufferIndex4 + 2;
        this.parameterDisplacement = readInt2(buffer, bufferIndex5);
        int bufferIndex6 = bufferIndex5 + 2;
        this.dataCount = readInt2(buffer, bufferIndex6);
        int bufferIndex7 = bufferIndex6 + 2;
        this.dataOffset = readInt2(buffer, bufferIndex7);
        int bufferIndex8 = bufferIndex7 + 2;
        this.dataDisplacement = readInt2(buffer, bufferIndex8);
        int bufferIndex9 = bufferIndex8 + 2;
        this.setupCount = buffer[bufferIndex9] & 255;
        int bufferIndex10 = bufferIndex9 + 2;
        if (this.setupCount != 0) {
            LogStream logStream = log;
            if (LogStream.level > 2) {
                log.println("setupCount is not zero: " + this.setupCount);
            }
        }
        return bufferIndex10 - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        this.pad1 = 0;
        this.pad = 0;
        if (this.parameterCount > 0) {
            int i = this.parameterOffset - (bufferIndex - this.headerStart);
            this.pad = i;
            int bufferIndex2 = bufferIndex + i;
            System.arraycopy(buffer, bufferIndex2, this.txn_buf, this.bufParameterStart + this.parameterDisplacement, this.parameterCount);
            bufferIndex = bufferIndex2 + this.parameterCount;
        }
        if (this.dataCount > 0) {
            int i2 = this.dataOffset - (bufferIndex - this.headerStart);
            this.pad1 = i2;
            int bufferIndex3 = bufferIndex + i2;
            System.arraycopy(buffer, bufferIndex3, this.txn_buf, this.bufDataStart + this.dataDisplacement, this.dataCount);
            int bufferIndex4 = bufferIndex3 + this.dataCount;
        }
        if (!this.parametersDone && this.parameterDisplacement + this.parameterCount == this.totalParameterCount) {
            this.parametersDone = true;
        }
        if (!this.dataDone && this.dataDisplacement + this.dataCount == this.totalDataCount) {
            this.dataDone = true;
        }
        if (this.parametersDone && this.dataDone) {
            this.hasMore = false;
            readParametersWireFormat(this.txn_buf, this.bufParameterStart, this.totalParameterCount);
            readDataWireFormat(this.txn_buf, this.bufDataStart, this.totalDataCount);
        }
        return this.pad + this.parameterCount + this.pad1 + this.dataCount;
    }

    @Override // jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String(super.toString() + ",totalParameterCount=" + this.totalParameterCount + ",totalDataCount=" + this.totalDataCount + ",parameterCount=" + this.parameterCount + ",parameterOffset=" + this.parameterOffset + ",parameterDisplacement=" + this.parameterDisplacement + ",dataCount=" + this.dataCount + ",dataOffset=" + this.dataOffset + ",dataDisplacement=" + this.dataDisplacement + ",setupCount=" + this.setupCount + ",pad=" + this.pad + ",pad1=" + this.pad1);
    }
}
