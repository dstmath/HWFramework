package jcifs.smb;

import java.util.Date;
import jcifs.util.Hexdump;

class SmbComOpenAndX extends AndXServerMessageBlock {
    private static final int BATCH_LIMIT = 0;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.SmbComOpenAndX.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.SmbComOpenAndX.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.SmbComOpenAndX.<clinit>():void");
    }

    SmbComOpenAndX(String fileName, int access, int flags, ServerMessageBlock andx) {
        super(andx);
        this.path = fileName;
        this.command = (byte) 45;
        this.desiredAccess = access & 3;
        if (this.desiredAccess == 3) {
            this.desiredAccess = OPEN_FN_TRUNC;
        }
        this.desiredAccess |= SHARING_DENY_NONE;
        this.desiredAccess &= -2;
        this.searchAttributes = 22;
        this.fileAttributes = SHARING_COMPATIBILITY;
        if ((flags & SHARING_DENY_NONE) == SHARING_DENY_NONE) {
            if ((flags & SHARING_DENY_READ_WRITE_EXECUTE) == SHARING_DENY_READ_WRITE_EXECUTE) {
                this.openFunction = 18;
            } else {
                this.openFunction = OPEN_FN_TRUNC;
            }
        } else if ((flags & SHARING_DENY_READ_WRITE_EXECUTE) != SHARING_DENY_READ_WRITE_EXECUTE) {
            this.openFunction = OPEN_FN_OPEN;
        } else if ((flags & SHARING_DENY_WRITE) == SHARING_DENY_WRITE) {
            this.openFunction = SHARING_DENY_READ_WRITE_EXECUTE;
        } else {
            this.openFunction = 17;
        }
    }

    int getBatchLimit(byte command) {
        return command == 46 ? BATCH_LIMIT : SHARING_COMPATIBILITY;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.flags, dst, dstIndex);
        dstIndex += OPEN_FN_TRUNC;
        ServerMessageBlock.writeInt2((long) this.desiredAccess, dst, dstIndex);
        dstIndex += OPEN_FN_TRUNC;
        ServerMessageBlock.writeInt2((long) this.searchAttributes, dst, dstIndex);
        dstIndex += OPEN_FN_TRUNC;
        ServerMessageBlock.writeInt2((long) this.fileAttributes, dst, dstIndex);
        dstIndex += OPEN_FN_TRUNC;
        this.creationTime = SHARING_COMPATIBILITY;
        ServerMessageBlock.writeInt4((long) this.creationTime, dst, dstIndex);
        dstIndex += FLAGS_REQUEST_BATCH_OPLOCK;
        ServerMessageBlock.writeInt2((long) this.openFunction, dst, dstIndex);
        dstIndex += OPEN_FN_TRUNC;
        ServerMessageBlock.writeInt4((long) this.allocationSize, dst, dstIndex);
        dstIndex += FLAGS_REQUEST_BATCH_OPLOCK;
        int i = SHARING_COMPATIBILITY;
        int dstIndex2 = dstIndex;
        while (i < 8) {
            dstIndex = dstIndex2 + OPEN_FN_OPEN;
            dst[dstIndex2] = (byte) 0;
            i += OPEN_FN_OPEN;
            dstIndex2 = dstIndex;
        }
        return dstIndex2 - start;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        if (this.useUnicode) {
            int dstIndex2 = dstIndex + OPEN_FN_OPEN;
            dst[dstIndex] = (byte) 0;
            dstIndex = dstIndex2;
        }
        return (dstIndex + writeString(this.path, dst, dstIndex)) - start;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return SHARING_COMPATIBILITY;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return SHARING_COMPATIBILITY;
    }

    public String toString() {
        return new String("SmbComOpenAndX[" + super.toString() + ",flags=0x" + Hexdump.toHexString(this.flags, (int) OPEN_FN_TRUNC) + ",desiredAccess=0x" + Hexdump.toHexString(this.desiredAccess, (int) FLAGS_REQUEST_BATCH_OPLOCK) + ",searchAttributes=0x" + Hexdump.toHexString(this.searchAttributes, (int) FLAGS_REQUEST_BATCH_OPLOCK) + ",fileAttributes=0x" + Hexdump.toHexString(this.fileAttributes, (int) FLAGS_REQUEST_BATCH_OPLOCK) + ",creationTime=" + new Date((long) this.creationTime) + ",openFunction=0x" + Hexdump.toHexString(this.openFunction, (int) OPEN_FN_TRUNC) + ",allocationSize=" + this.allocationSize + ",fileName=" + this.path + "]");
    }
}
