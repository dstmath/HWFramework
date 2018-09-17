package jcifs.smb;

import java.util.Date;
import jcifs.Config;
import jcifs.util.Hexdump;

class SmbComOpenAndX extends AndXServerMessageBlock {
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

    SmbComOpenAndX(String fileName, int access, int flags, ServerMessageBlock andx) {
        super(andx);
        this.path = fileName;
        this.command = (byte) 45;
        this.desiredAccess = access & 3;
        if (this.desiredAccess == 3) {
            this.desiredAccess = 2;
        }
        this.desiredAccess |= 64;
        this.desiredAccess &= -2;
        this.searchAttributes = 22;
        this.fileAttributes = 0;
        if ((flags & 64) == 64) {
            if ((flags & 16) == 16) {
                this.openFunction = 18;
            } else {
                this.openFunction = 2;
            }
        } else if ((flags & 16) != 16) {
            this.openFunction = 1;
        } else if ((flags & 32) == 32) {
            this.openFunction = 16;
        } else {
            this.openFunction = 17;
        }
    }

    int getBatchLimit(byte command) {
        return command == (byte) 46 ? BATCH_LIMIT : 0;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.flags, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.desiredAccess, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.searchAttributes, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.fileAttributes, dst, dstIndex);
        dstIndex += 2;
        this.creationTime = 0;
        ServerMessageBlock.writeInt4((long) this.creationTime, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt2((long) this.openFunction, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4((long) this.allocationSize, dst, dstIndex);
        dstIndex += 4;
        int i = 0;
        while (true) {
            int dstIndex2 = dstIndex;
            if (i >= 8) {
                return dstIndex2 - start;
            }
            dstIndex = dstIndex2 + 1;
            dst[dstIndex2] = (byte) 0;
            i++;
        }
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        if (this.useUnicode) {
            int dstIndex2 = dstIndex + 1;
            dst[dstIndex] = (byte) 0;
            dstIndex = dstIndex2;
        }
        return (dstIndex + writeString(this.path, dst, dstIndex)) - start;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComOpenAndX[" + super.toString() + ",flags=0x" + Hexdump.toHexString(this.flags, 2) + ",desiredAccess=0x" + Hexdump.toHexString(this.desiredAccess, 4) + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, 4) + ",fileAttributes=0x" + Hexdump.toHexString(this.fileAttributes, 4) + ",creationTime=" + new Date((long) this.creationTime) + ",openFunction=0x" + Hexdump.toHexString(this.openFunction, 2) + ",allocationSize=" + this.allocationSize + ",fileName=" + this.path + "]");
    }
}
