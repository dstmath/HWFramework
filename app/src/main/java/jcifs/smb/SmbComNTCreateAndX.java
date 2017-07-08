package jcifs.smb;

import jcifs.util.Hexdump;

class SmbComNTCreateAndX extends AndXServerMessageBlock {
    static final int FILE_CREATE = 2;
    static final int FILE_OPEN = 1;
    static final int FILE_OPEN_IF = 3;
    static final int FILE_OVERWRITE = 4;
    static final int FILE_OVERWRITE_IF = 5;
    static final int FILE_SEQUENTIAL_ONLY = 4;
    static final int FILE_SUPERSEDE = 0;
    static final int FILE_SYNCHRONOUS_IO_ALERT = 16;
    static final int FILE_SYNCHRONOUS_IO_NONALERT = 32;
    static final int FILE_WRITE_THROUGH = 2;
    static final int SECURITY_CONTEXT_TRACKING = 1;
    static final int SECURITY_EFFECTIVE_ONLY = 2;
    private long allocationSize;
    private int createDisposition;
    private int createOptions;
    int desiredAccess;
    private int extFileAttributes;
    int flags0;
    private int impersonationLevel;
    private int namelen_index;
    private int rootDirectoryFid;
    private byte securityFlags;
    private int shareAccess;

    SmbComNTCreateAndX(String name, int flags, int access, int shareAccess, int extFileAttributes, int createOptions, ServerMessageBlock andx) {
        super(andx);
        this.path = name;
        this.command = (byte) -94;
        this.desiredAccess = access;
        this.desiredAccess |= 137;
        this.extFileAttributes = extFileAttributes;
        this.shareAccess = shareAccess;
        if ((flags & 64) == 64) {
            if ((flags & FILE_SYNCHRONOUS_IO_ALERT) == FILE_SYNCHRONOUS_IO_ALERT) {
                this.createDisposition = FILE_OVERWRITE_IF;
            } else {
                this.createDisposition = FILE_SEQUENTIAL_ONLY;
            }
        } else if ((flags & FILE_SYNCHRONOUS_IO_ALERT) != FILE_SYNCHRONOUS_IO_ALERT) {
            this.createDisposition = SECURITY_CONTEXT_TRACKING;
        } else if ((flags & FILE_SYNCHRONOUS_IO_NONALERT) == FILE_SYNCHRONOUS_IO_NONALERT) {
            this.createDisposition = SECURITY_EFFECTIVE_ONLY;
        } else {
            this.createDisposition = FILE_OPEN_IF;
        }
        if ((createOptions & SECURITY_CONTEXT_TRACKING) == 0) {
            this.createOptions = createOptions | 64;
        } else {
            this.createOptions = createOptions;
        }
        this.impersonationLevel = SECURITY_EFFECTIVE_ONLY;
        this.securityFlags = (byte) 3;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + SECURITY_CONTEXT_TRACKING;
        dst[dstIndex] = (byte) 0;
        this.namelen_index = dstIndex2;
        dstIndex = dstIndex2 + SECURITY_EFFECTIVE_ONLY;
        ServerMessageBlock.writeInt4((long) this.flags0, dst, dstIndex);
        dstIndex += FILE_SEQUENTIAL_ONLY;
        ServerMessageBlock.writeInt4((long) this.rootDirectoryFid, dst, dstIndex);
        dstIndex += FILE_SEQUENTIAL_ONLY;
        ServerMessageBlock.writeInt4((long) this.desiredAccess, dst, dstIndex);
        dstIndex += FILE_SEQUENTIAL_ONLY;
        ServerMessageBlock.writeInt8(this.allocationSize, dst, dstIndex);
        dstIndex += 8;
        ServerMessageBlock.writeInt4((long) this.extFileAttributes, dst, dstIndex);
        dstIndex += FILE_SEQUENTIAL_ONLY;
        ServerMessageBlock.writeInt4((long) this.shareAccess, dst, dstIndex);
        dstIndex += FILE_SEQUENTIAL_ONLY;
        ServerMessageBlock.writeInt4((long) this.createDisposition, dst, dstIndex);
        dstIndex += FILE_SEQUENTIAL_ONLY;
        ServerMessageBlock.writeInt4((long) this.createOptions, dst, dstIndex);
        dstIndex += FILE_SEQUENTIAL_ONLY;
        ServerMessageBlock.writeInt4((long) this.impersonationLevel, dst, dstIndex);
        dstIndex += FILE_SEQUENTIAL_ONLY;
        dstIndex2 = dstIndex + SECURITY_CONTEXT_TRACKING;
        dst[dstIndex] = this.securityFlags;
        return dstIndex2 - start;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int length;
        int n = writeString(this.path, dst, dstIndex);
        if (this.useUnicode) {
            length = this.path.length() * SECURITY_EFFECTIVE_ONLY;
        } else {
            length = n;
        }
        ServerMessageBlock.writeInt2((long) length, dst, this.namelen_index);
        return n;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return FILE_SUPERSEDE;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return FILE_SUPERSEDE;
    }

    public String toString() {
        return new String("SmbComNTCreateAndX[" + super.toString() + ",flags=0x" + Hexdump.toHexString(this.flags0, (int) SECURITY_EFFECTIVE_ONLY) + ",rootDirectoryFid=" + this.rootDirectoryFid + ",desiredAccess=0x" + Hexdump.toHexString(this.desiredAccess, (int) FILE_SEQUENTIAL_ONLY) + ",allocationSize=" + this.allocationSize + ",extFileAttributes=0x" + Hexdump.toHexString(this.extFileAttributes, (int) FILE_SEQUENTIAL_ONLY) + ",shareAccess=0x" + Hexdump.toHexString(this.shareAccess, (int) FILE_SEQUENTIAL_ONLY) + ",createDisposition=0x" + Hexdump.toHexString(this.createDisposition, (int) FILE_SEQUENTIAL_ONLY) + ",createOptions=0x" + Hexdump.toHexString(this.createOptions, 8) + ",impersonationLevel=0x" + Hexdump.toHexString(this.impersonationLevel, (int) FILE_SEQUENTIAL_ONLY) + ",securityFlags=0x" + Hexdump.toHexString(this.securityFlags, (int) SECURITY_EFFECTIVE_ONLY) + ",name=" + this.path + "]");
    }
}
