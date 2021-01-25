package jcifs.smb;

import java.io.UnsupportedEncodingException;
import jcifs.Config;
import jcifs.util.Hexdump;

/* access modifiers changed from: package-private */
public class SmbComTreeConnectAndX extends AndXServerMessageBlock {
    private static final boolean DISABLE_PLAIN_TEXT_PASSWORDS = Config.getBoolean("jcifs.smb.client.disablePlainTextPasswords", true);
    private static byte[] batchLimits = {1, 1, 1, 1, 1, 1, 1, 1, 0};
    private boolean disconnectTid = false;
    private byte[] password;
    private int passwordLength;
    String path;
    private String service;
    private SmbSession session;

    static {
        String s = Config.getProperty("jcifs.smb.client.TreeConnectAndX.CheckDirectory");
        if (s != null) {
            batchLimits[0] = Byte.parseByte(s);
        }
        String s2 = Config.getProperty("jcifs.smb.client.TreeConnectAndX.CreateDirectory");
        if (s2 != null) {
            batchLimits[2] = Byte.parseByte(s2);
        }
        String s3 = Config.getProperty("jcifs.smb.client.TreeConnectAndX.Delete");
        if (s3 != null) {
            batchLimits[3] = Byte.parseByte(s3);
        }
        String s4 = Config.getProperty("jcifs.smb.client.TreeConnectAndX.DeleteDirectory");
        if (s4 != null) {
            batchLimits[4] = Byte.parseByte(s4);
        }
        String s5 = Config.getProperty("jcifs.smb.client.TreeConnectAndX.OpenAndX");
        if (s5 != null) {
            batchLimits[5] = Byte.parseByte(s5);
        }
        String s6 = Config.getProperty("jcifs.smb.client.TreeConnectAndX.Rename");
        if (s6 != null) {
            batchLimits[6] = Byte.parseByte(s6);
        }
        String s7 = Config.getProperty("jcifs.smb.client.TreeConnectAndX.Transaction");
        if (s7 != null) {
            batchLimits[7] = Byte.parseByte(s7);
        }
        String s8 = Config.getProperty("jcifs.smb.client.TreeConnectAndX.QueryInformation");
        if (s8 != null) {
            batchLimits[8] = Byte.parseByte(s8);
        }
    }

    SmbComTreeConnectAndX(SmbSession session2, String path2, String service2, ServerMessageBlock andx) {
        super(andx);
        this.session = session2;
        this.path = path2;
        this.service = service2;
        this.command = 117;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.AndXServerMessageBlock
    public int getBatchLimit(byte command) {
        switch (command & 255) {
            case 0:
                return batchLimits[2];
            case 1:
                return batchLimits[4];
            case 6:
                return batchLimits[3];
            case 7:
                return batchLimits[6];
            case 8:
                return batchLimits[8];
            case 16:
                return batchLimits[0];
            case 37:
                return batchLimits[7];
            case 45:
                return batchLimits[5];
            default:
                return 0;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        byte b = 1;
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
            b = 0;
        }
        dst[dstIndex] = b;
        dst[dstIndex2] = 0;
        writeInt2((long) this.passwordLength, dst, dstIndex2 + 1);
        return 4;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2;
        if (this.session.transport.server.security != 0 || (!this.session.auth.hashesExternal && this.session.auth.password.length() <= 0)) {
            dst[dstIndex] = 0;
            dstIndex2 = dstIndex + 1;
        } else {
            System.arraycopy(this.password, 0, dst, dstIndex, this.passwordLength);
            dstIndex2 = dstIndex + this.passwordLength;
        }
        int dstIndex3 = dstIndex2 + writeString(this.path, dst, dstIndex2);
        try {
            System.arraycopy(this.service.getBytes("ASCII"), 0, dst, dstIndex3, this.service.length());
            int dstIndex4 = dstIndex3 + this.service.length();
            dst[dstIndex4] = 0;
            return (dstIndex4 + 1) - dstIndex;
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
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
        return new String("SmbComTreeConnectAndX[" + super.toString() + ",disconnectTid=" + this.disconnectTid + ",passwordLength=" + this.passwordLength + ",password=" + Hexdump.toHexString(this.password, this.passwordLength, 0) + ",path=" + this.path + ",service=" + this.service + "]");
    }
}
