package jcifs.smb;

import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public class SmbComNTCreateAndX extends AndXServerMessageBlock {
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

    SmbComNTCreateAndX(String name, int flags, int access, int shareAccess2, int extFileAttributes2, int createOptions2, ServerMessageBlock andx) {
        super(andx);
        this.path = name;
        this.command = -94;
        this.desiredAccess = access;
        this.desiredAccess |= 137;
        this.extFileAttributes = extFileAttributes2;
        this.shareAccess = shareAccess2;
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
        if ((createOptions2 & 1) == 0) {
            this.createOptions = createOptions2 | 64;
        } else {
            this.createOptions = createOptions2;
        }
        this.impersonationLevel = 2;
        this.securityFlags = 3;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = 0;
        this.namelen_index = dstIndex2;
        int dstIndex3 = dstIndex2 + 2;
        writeInt4((long) this.flags0, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 4;
        writeInt4((long) this.rootDirectoryFid, dst, dstIndex4);
        int dstIndex5 = dstIndex4 + 4;
        writeInt4((long) this.desiredAccess, dst, dstIndex5);
        int dstIndex6 = dstIndex5 + 4;
        writeInt8(this.allocationSize, dst, dstIndex6);
        int dstIndex7 = dstIndex6 + 8;
        writeInt4((long) this.extFileAttributes, dst, dstIndex7);
        int dstIndex8 = dstIndex7 + 4;
        writeInt4((long) this.shareAccess, dst, dstIndex8);
        int dstIndex9 = dstIndex8 + 4;
        writeInt4((long) this.createDisposition, dst, dstIndex9);
        int dstIndex10 = dstIndex9 + 4;
        writeInt4((long) this.createOptions, dst, dstIndex10);
        int dstIndex11 = dstIndex10 + 4;
        writeInt4((long) this.impersonationLevel, dst, dstIndex11);
        int dstIndex12 = dstIndex11 + 4;
        dst[dstIndex12] = this.securityFlags;
        return (dstIndex12 + 1) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int i;
        int n = writeString(this.path, dst, dstIndex);
        if (this.useUnicode) {
            i = this.path.length() * 2;
        } else {
            i = n;
        }
        writeInt2((long) i, dst, this.namelen_index);
        return n;
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
        return new String("SmbComNTCreateAndX[" + super.toString() + ",flags=0x" + Hexdump.toHexString(this.flags0, 2) + ",rootDirectoryFid=" + this.rootDirectoryFid + ",desiredAccess=0x" + Hexdump.toHexString(this.desiredAccess, 4) + ",allocationSize=" + this.allocationSize + ",extFileAttributes=0x" + Hexdump.toHexString(this.extFileAttributes, 4) + ",shareAccess=0x" + Hexdump.toHexString(this.shareAccess, 4) + ",createDisposition=0x" + Hexdump.toHexString(this.createDisposition, 4) + ",createOptions=0x" + Hexdump.toHexString(this.createOptions, 8) + ",impersonationLevel=0x" + Hexdump.toHexString(this.impersonationLevel, 4) + ",securityFlags=0x" + Hexdump.toHexString((int) this.securityFlags, 2) + ",name=" + this.path + "]");
    }
}
