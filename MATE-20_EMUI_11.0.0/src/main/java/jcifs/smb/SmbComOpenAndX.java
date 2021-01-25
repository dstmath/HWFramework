package jcifs.smb;

import java.util.Date;
import jcifs.Config;
import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public class SmbComOpenAndX extends AndXServerMessageBlock {
    private static final int BATCH_LIMIT = Config.getInt("jcifs.smb.client.OpenAndX.ReadAndX", 1);
    private static final int DO_NOT_CACHE = 4096;
    private static final int FLAGS_REQUEST_BATCH_OPLOCK = 4;
    private static final int FLAGS_REQUEST_OPLOCK = 2;
    private static final int FLAGS_RETURN_ADDITIONAL_INFO = 1;
    private static final int OPEN_FN_CREATE = 16;
    private static final int OPEN_FN_FAIL_IF_EXISTS = 0;
    private static final int OPEN_FN_OPEN = 1;
    private static final int OPEN_FN_TRUNC = 2;
    private static final int SHARING_COMPATIBILITY = 0;
    private static final int SHARING_DENY_NONE = 64;
    private static final int SHARING_DENY_READ_EXECUTE = 48;
    private static final int SHARING_DENY_READ_WRITE_EXECUTE = 16;
    private static final int SHARING_DENY_WRITE = 32;
    private static final int WRITE_THROUGH = 16384;
    int allocationSize;
    int creationTime;
    int desiredAccess;
    int fileAttributes;
    int flags;
    int openFunction;
    int searchAttributes;

    SmbComOpenAndX(String fileName, int access, int flags2, ServerMessageBlock andx) {
        super(andx);
        this.path = fileName;
        this.command = 45;
        this.desiredAccess = access & 3;
        if (this.desiredAccess == 3) {
            this.desiredAccess = 2;
        }
        this.desiredAccess |= 64;
        this.desiredAccess &= -2;
        this.searchAttributes = 22;
        this.fileAttributes = 0;
        if ((flags2 & 64) == 64) {
            if ((flags2 & 16) == 16) {
                this.openFunction = 18;
            } else {
                this.openFunction = 2;
            }
        } else if ((flags2 & 16) != 16) {
            this.openFunction = 1;
        } else if ((flags2 & 32) == 32) {
            this.openFunction = 16;
        } else {
            this.openFunction = 17;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.AndXServerMessageBlock
    public int getBatchLimit(byte command) {
        if (command == 46) {
            return BATCH_LIMIT;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        writeInt2((long) this.flags, dst, dstIndex);
        int dstIndex2 = dstIndex + 2;
        writeInt2((long) this.desiredAccess, dst, dstIndex2);
        int dstIndex3 = dstIndex2 + 2;
        writeInt2((long) this.searchAttributes, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 2;
        writeInt2((long) this.fileAttributes, dst, dstIndex4);
        int dstIndex5 = dstIndex4 + 2;
        this.creationTime = 0;
        writeInt4((long) this.creationTime, dst, dstIndex5);
        int dstIndex6 = dstIndex5 + 4;
        writeInt2((long) this.openFunction, dst, dstIndex6);
        int dstIndex7 = dstIndex6 + 2;
        writeInt4((long) this.allocationSize, dst, dstIndex7);
        int dstIndex8 = dstIndex7 + 4;
        for (int i = 0; i < 8; i++) {
            dstIndex8++;
            dst[dstIndex8] = 0;
        }
        return dstIndex8 - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        if (this.useUnicode) {
            dst[dstIndex] = 0;
            dstIndex++;
        }
        return (dstIndex + writeString(this.path, dst, dstIndex)) - dstIndex;
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

    @Override // jcifs.smb.AndXServerMessageBlock, jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComOpenAndX[" + super.toString() + ",flags=0x" + Hexdump.toHexString(this.flags, 2) + ",desiredAccess=0x" + Hexdump.toHexString(this.desiredAccess, 4) + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, 4) + ",fileAttributes=0x" + Hexdump.toHexString(this.fileAttributes, 4) + ",creationTime=" + new Date((long) this.creationTime) + ",openFunction=0x" + Hexdump.toHexString(this.openFunction, 2) + ",allocationSize=" + this.allocationSize + ",fileName=" + this.path + "]");
    }
}
