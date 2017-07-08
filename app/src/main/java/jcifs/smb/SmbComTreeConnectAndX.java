package jcifs.smb;

import java.io.UnsupportedEncodingException;
import jcifs.util.Encdec;
import jcifs.util.Hexdump;

class SmbComTreeConnectAndX extends AndXServerMessageBlock {
    private static final boolean DISABLE_PLAIN_TEXT_PASSWORDS = false;
    private static byte[] batchLimits;
    private boolean disconnectTid;
    private byte[] password;
    private int passwordLength;
    String path;
    private String service;
    private SmbSession session;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.SmbComTreeConnectAndX.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.SmbComTreeConnectAndX.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.SmbComTreeConnectAndX.<clinit>():void");
    }

    SmbComTreeConnectAndX(SmbSession session, String path, String service, ServerMessageBlock andx) {
        super(andx);
        this.disconnectTid = false;
        this.session = session;
        this.path = path;
        this.service = service;
        this.command = (byte) 117;
    }

    int getBatchLimit(byte command) {
        switch (command & 255) {
            case SmbFile.FILE_NO_SHARE /*0*/:
                return batchLimits[2];
            case Encdec.TIME_1970_SEC_32BE /*1*/:
                return batchLimits[4];
            case Encdec.TIME_1601_NANOS_64BE /*6*/:
                return batchLimits[3];
            case Encdec.TIME_1970_MILLIS_64BE /*7*/:
                return batchLimits[6];
            case Encdec.TIME_1970_MILLIS_64LE /*8*/:
                return batchLimits[8];
            case SmbFile.TYPE_NAMED_PIPE /*16*/:
                return batchLimits[0];
            case 37:
                return batchLimits[7];
            case 45:
                return batchLimits[5];
            default:
                return 0;
        }
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        byte b = (byte) 1;
        if (this.session.transport.server.security != 0 || (!this.session.auth.hashesExternal && this.session.auth.password.length() <= 0)) {
            this.passwordLength = 1;
        } else if (this.session.transport.server.encryptedPasswords) {
            this.password = this.session.auth.getAnsiHash(this.session.transport.server.encryptionKey);
            this.passwordLength = this.password.length;
        } else if (DISABLE_PLAIN_TEXT_PASSWORDS) {
            throw new RuntimeException("Plain text passwords are disabled");
        } else {
            this.password = new byte[((this.session.auth.password.length() + 1) * 2)];
            this.passwordLength = writeString(this.session.auth.password, this.password, 0);
        }
        int dstIndex2 = dstIndex + 1;
        if (!this.disconnectTid) {
            b = (byte) 0;
        }
        dst[dstIndex] = b;
        dstIndex = dstIndex2 + 1;
        dst[dstIndex2] = (byte) 0;
        ServerMessageBlock.writeInt2((long) this.passwordLength, dst, dstIndex);
        return 4;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        if (this.session.transport.server.security != 0 || (!this.session.auth.hashesExternal && this.session.auth.password.length() <= 0)) {
            int dstIndex2 = dstIndex + 1;
            dst[dstIndex] = (byte) 0;
            dstIndex = dstIndex2;
        } else {
            System.arraycopy(this.password, 0, dst, dstIndex, this.passwordLength);
            dstIndex += this.passwordLength;
        }
        dstIndex += writeString(this.path, dst, dstIndex);
        try {
            System.arraycopy(this.service.getBytes("ASCII"), 0, dst, dstIndex, this.service.length());
            dstIndex += this.service.length();
            dstIndex2 = dstIndex + 1;
            dst[dstIndex] = (byte) 0;
            dstIndex = dstIndex2;
            return dstIndex2 - start;
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComTreeConnectAndX[" + super.toString() + ",disconnectTid=" + this.disconnectTid + ",passwordLength=" + this.passwordLength + ",password=" + Hexdump.toHexString(this.password, this.passwordLength, 0) + ",path=" + this.path + ",service=" + this.service + "]");
    }
}
