package jcifs.smb;

import jcifs.Config;

/* access modifiers changed from: package-private */
public class SmbComSessionSetupAndX extends AndXServerMessageBlock {
    private static final int BATCH_LIMIT = Config.getInt("jcifs.smb.client.SessionSetupAndX.TreeConnectAndX", 1);
    private static final boolean DISABLE_PLAIN_TEXT_PASSWORDS = Config.getBoolean("jcifs.smb.client.disablePlainTextPasswords", true);
    private String accountName;
    private byte[] blob = null;
    private int capabilities;
    Object cred;
    private byte[] lmHash;
    private byte[] ntHash;
    private String primaryDomain;
    SmbSession session;
    private int sessionKey;

    SmbComSessionSetupAndX(SmbSession session2, ServerMessageBlock andx, Object cred2) throws SmbException {
        super(andx);
        this.command = 115;
        this.session = session2;
        this.cred = cred2;
        this.sessionKey = session2.transport.sessionKey;
        this.capabilities = session2.transport.capabilities;
        if (session2.transport.server.security == 1) {
            if (cred2 instanceof NtlmPasswordAuthentication) {
                NtlmPasswordAuthentication auth = (NtlmPasswordAuthentication) cred2;
                if (auth == NtlmPasswordAuthentication.ANONYMOUS) {
                    this.lmHash = new byte[0];
                    this.ntHash = new byte[0];
                    this.capabilities &= Integer.MAX_VALUE;
                } else if (session2.transport.server.encryptedPasswords) {
                    this.lmHash = auth.getAnsiHash(session2.transport.server.encryptionKey);
                    this.ntHash = auth.getUnicodeHash(session2.transport.server.encryptionKey);
                    if (this.lmHash.length == 0 && this.ntHash.length == 0) {
                        throw new RuntimeException("Null setup prohibited.");
                    }
                } else if (DISABLE_PLAIN_TEXT_PASSWORDS) {
                    throw new RuntimeException("Plain text passwords are disabled");
                } else if (this.useUnicode) {
                    String password = auth.getPassword();
                    this.lmHash = new byte[0];
                    this.ntHash = new byte[((password.length() + 1) * 2)];
                    writeString(password, this.ntHash, 0);
                } else {
                    String password2 = auth.getPassword();
                    this.lmHash = new byte[((password2.length() + 1) * 2)];
                    this.ntHash = new byte[0];
                    writeString(password2, this.lmHash, 0);
                }
                this.accountName = auth.username;
                if (this.useUnicode) {
                    this.accountName = this.accountName.toUpperCase();
                }
                this.primaryDomain = auth.domain.toUpperCase();
            } else if (cred2 instanceof byte[]) {
                this.blob = (byte[]) cred2;
            } else {
                throw new SmbException("Unsupported credential type");
            }
        } else if (session2.transport.server.security != 0) {
            throw new SmbException("Unsupported");
        } else if (cred2 instanceof NtlmPasswordAuthentication) {
            NtlmPasswordAuthentication auth2 = (NtlmPasswordAuthentication) cred2;
            this.lmHash = new byte[0];
            this.ntHash = new byte[0];
            this.accountName = auth2.username;
            if (this.useUnicode) {
                this.accountName = this.accountName.toUpperCase();
            }
            this.primaryDomain = auth2.domain.toUpperCase();
        } else {
            throw new SmbException("Unsupported credential type");
        }
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.AndXServerMessageBlock
    public int getBatchLimit(byte command) {
        if (command == 117) {
            return BATCH_LIMIT;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2;
        writeInt2((long) this.session.transport.snd_buf_size, dst, dstIndex);
        int dstIndex3 = dstIndex + 2;
        writeInt2((long) this.session.transport.maxMpxCount, dst, dstIndex3);
        int dstIndex4 = dstIndex3 + 2;
        SmbTransport smbTransport = this.session.transport;
        writeInt2(1, dst, dstIndex4);
        int dstIndex5 = dstIndex4 + 2;
        writeInt4((long) this.sessionKey, dst, dstIndex5);
        int dstIndex6 = dstIndex5 + 4;
        if (this.blob != null) {
            writeInt2((long) this.blob.length, dst, dstIndex6);
            dstIndex2 = dstIndex6 + 2;
        } else {
            writeInt2((long) this.lmHash.length, dst, dstIndex6);
            int dstIndex7 = dstIndex6 + 2;
            writeInt2((long) this.ntHash.length, dst, dstIndex7);
            dstIndex2 = dstIndex7 + 2;
        }
        int dstIndex8 = dstIndex2 + 1;
        dst[dstIndex2] = 0;
        int dstIndex9 = dstIndex8 + 1;
        dst[dstIndex8] = 0;
        int dstIndex10 = dstIndex9 + 1;
        dst[dstIndex9] = 0;
        int dstIndex11 = dstIndex10 + 1;
        dst[dstIndex10] = 0;
        writeInt4((long) this.capabilities, dst, dstIndex11);
        return (dstIndex11 + 4) - dstIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int dstIndex2;
        if (this.blob != null) {
            System.arraycopy(this.blob, 0, dst, dstIndex, this.blob.length);
            dstIndex2 = dstIndex + this.blob.length;
        } else {
            System.arraycopy(this.lmHash, 0, dst, dstIndex, this.lmHash.length);
            int dstIndex3 = dstIndex + this.lmHash.length;
            System.arraycopy(this.ntHash, 0, dst, dstIndex3, this.ntHash.length);
            int dstIndex4 = dstIndex3 + this.ntHash.length;
            int dstIndex5 = dstIndex4 + writeString(this.accountName, dst, dstIndex4);
            dstIndex2 = dstIndex5 + writeString(this.primaryDomain, dst, dstIndex5);
        }
        SmbTransport smbTransport = this.session.transport;
        int dstIndex6 = dstIndex2 + writeString(SmbTransport.NATIVE_OS, dst, dstIndex2);
        SmbTransport smbTransport2 = this.session.transport;
        return (dstIndex6 + writeString(SmbTransport.NATIVE_LANMAN, dst, dstIndex6)) - dstIndex;
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
        int length;
        int i = 0;
        StringBuilder append = new StringBuilder().append("SmbComSessionSetupAndX[").append(super.toString()).append(",snd_buf_size=").append(this.session.transport.snd_buf_size).append(",maxMpxCount=").append(this.session.transport.maxMpxCount).append(",VC_NUMBER=");
        SmbTransport smbTransport = this.session.transport;
        StringBuilder append2 = append.append(1).append(",sessionKey=").append(this.sessionKey).append(",lmHash.length=");
        if (this.lmHash == null) {
            length = 0;
        } else {
            length = this.lmHash.length;
        }
        StringBuilder append3 = append2.append(length).append(",ntHash.length=");
        if (this.ntHash != null) {
            i = this.ntHash.length;
        }
        StringBuilder append4 = append3.append(i).append(",capabilities=").append(this.capabilities).append(",accountName=").append(this.accountName).append(",primaryDomain=").append(this.primaryDomain).append(",NATIVE_OS=");
        SmbTransport smbTransport2 = this.session.transport;
        StringBuilder append5 = append4.append(SmbTransport.NATIVE_OS).append(",NATIVE_LANMAN=");
        SmbTransport smbTransport3 = this.session.transport;
        return new String(append5.append(SmbTransport.NATIVE_LANMAN).append("]").toString());
    }
}
