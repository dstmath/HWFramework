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
            if ((flags & 16) == 16) {
                this.createDisposition = 5;
            } else {
                this.createDisposition = 4;
            }
        } else if ((flags & 16) != 16) {
            this.createDisposition = 1;
        } else if ((flags & 32) == 32) {
            this.createDisposition = 2;
        } else {
            this.createDisposition = 3;
        }
        if ((createOptions & 1) == 0) {
            this.createOptions = createOptions | 64;
        } else {
            this.createOptions = createOptions;
        }
        this.impersonationLevel = 2;
        this.securityFlags = (byte) 3;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 0;
        this.namelen_index = dstIndex2;
        dstIndex = dstIndex2 + 2;
        ServerMessageBlock.writeInt4((long) this.flags0, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) this.rootDirectoryFid, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) this.desiredAccess, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt8(this.allocationSize, dst, dstIndex);
        dstIndex += 8;
        ServerMessageBlock.writeInt4((long) this.extFileAttributes, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) this.shareAccess, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) this.createDisposition, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) this.createOptions, dst, dstIndex);
        dstIndex += 4;
        ServerMessageBlock.writeInt4((long) this.impersonationLevel, dst, dstIndex);
        dstIndex += 4;
        dstIndex2 = dstIndex + 1;
        dst[dstIndex] = this.securityFlags;
        return dstIndex2 - start;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int length;
        int n = writeString(this.path, dst, dstIndex);
        if (this.useUnicode) {
            length = this.path.length() * 2;
        } else {
            length = n;
        }
        ServerMessageBlock.writeInt2((long) length, dst, this.namelen_index);
        return n;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComNTCreateAndX[" + super.toString() + ",flags=0x" + Hexdump.toHexString(this.flags0, 2) + ",rootDirectoryFid=" + this.rootDirectoryFid + ",desiredAccess=0x" + Hexdump.toHexString(this.desiredAccess, 4) + ",allocationSize=" + this.allocationSize + ",extFileAttributes=0x" + Hexdump.toHexString(this.extFileAttributes, 4) + ",shareAccess=0x" + Hexdump.toHexString(this.shareAccess, 4) + ",createDisposition=0x" + Hexdump.toHexString(this.createDisposition, 4) + ",createOptions=0x" + Hexdump.toHexString(this.createOptions, 8) + ",impersonationLevel=0x" + Hexdump.toHexString(this.impersonationLevel, 4) + ",securityFlags=0x" + Hexdump.toHexString(this.securityFlags, 2) + ",name=" + this.path + "]");
    }
}
