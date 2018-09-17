package jcifs.smb;

import jcifs.Config;

class SmbComSessionSetupAndX extends AndXServerMessageBlock {
    private static final int BATCH_LIMIT = Config.getInt("jcifs.smb.client.SessionSetupAndX.TreeConnectAndX", 1);
    private static final boolean DISABLE_PLAIN_TEXT_PASSWORDS = Config.getBoolean("jcifs.smb.client.disablePlainTextPasswords", true);
    private String accountName;
    private byte[] blob;
    private int capabilities;
    Object cred;
    private byte[] lmHash;
    private byte[] ntHash;
    private String primaryDomain;
    SmbSession session;
    private int sessionKey;

    SmbComSessionSetupAndX(SmbSession session, ServerMessageBlock andx, Object cred) throws SmbException {
        super(andx);
        this.blob = null;
        this.command = (byte) 115;
        this.session = session;
        this.cred = cred;
        this.sessionKey = session.transport.sessionKey;
        this.capabilities = session.transport.capabilities;
        NtlmPasswordAuthentication auth;
        if (session.transport.server.security == 1) {
            if (cred instanceof NtlmPasswordAuthentication) {
                auth = (NtlmPasswordAuthentication) cred;
                String password;
                if (auth == NtlmPasswordAuthentication.ANONYMOUS) {
                    this.lmHash = new byte[0];
                    this.ntHash = new byte[0];
                    this.capabilities &= Integer.MAX_VALUE;
                } else if (session.transport.server.encryptedPasswords) {
                    this.lmHash = auth.getAnsiHash(session.transport.server.encryptionKey);
                    this.ntHash = auth.getUnicodeHash(session.transport.server.encryptionKey);
                    if (this.lmHash.length == 0 && this.ntHash.length == 0) {
                        throw new RuntimeException("Null setup prohibited.");
                    }
                } else if (DISABLE_PLAIN_TEXT_PASSWORDS) {
                    throw new RuntimeException("Plain text passwords are disabled");
                } else if (this.useUnicode) {
                    password = auth.getPassword();
                    this.lmHash = new byte[0];
                    this.ntHash = new byte[((password.length() + 1) * 2)];
                    writeString(password, this.ntHash, 0);
                } else {
                    password = auth.getPassword();
                    this.lmHash = new byte[((password.length() + 1) * 2)];
                    this.ntHash = new byte[0];
                    writeString(password, this.lmHash, 0);
                }
                this.accountName = auth.username;
                if (this.useUnicode) {
                    this.accountName = this.accountName.toUpperCase();
                }
                this.primaryDomain = auth.domain.toUpperCase();
            } else if (cred instanceof byte[]) {
                this.blob = (byte[]) cred;
            } else {
                throw new SmbException("Unsupported credential type");
            }
        } else if (session.transport.server.security != 0) {
            throw new SmbException("Unsupported");
        } else if (cred instanceof NtlmPasswordAuthentication) {
            auth = (NtlmPasswordAuthentication) cred;
            this.lmHash = new byte[0];
            this.ntHash = new byte[0];
            this.accountName = auth.username;
            if (this.useUnicode) {
                this.accountName = this.accountName.toUpperCase();
            }
            this.primaryDomain = auth.domain.toUpperCase();
        } else {
            throw new SmbException("Unsupported credential type");
        }
    }

    int getBatchLimit(byte command) {
        return command == (byte) 117 ? BATCH_LIMIT : 0;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        ServerMessageBlock.writeInt2((long) this.session.transport.snd_buf_size, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt2((long) this.session.transport.maxMpxCount, dst, dstIndex);
        dstIndex += 2;
        SmbTransport smbTransport = this.session.transport;
        ServerMessageBlock.writeInt2(1, dst, dstIndex);
        dstIndex += 2;
        ServerMessageBlock.writeInt4((long) this.sessionKey, dst, dstIndex);
        dstIndex += 4;
        if (this.blob != null) {
            ServerMessageBlock.writeInt2((long) this.blob.length, dst, dstIndex);
            dstIndex += 2;
        } else {
            ServerMessageBlock.writeInt2((long) this.lmHash.length, dst, dstIndex);
            dstIndex += 2;
            ServerMessageBlock.writeInt2((long) this.ntHash.length, dst, dstIndex);
            dstIndex += 2;
        }
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 0;
        dstIndex = dstIndex2 + 1;
        dst[dstIndex2] = (byte) 0;
        dstIndex2 = dstIndex + 1;
        dst[dstIndex] = (byte) 0;
        dstIndex = dstIndex2 + 1;
        dst[dstIndex2] = (byte) 0;
        ServerMessageBlock.writeInt4((long) this.capabilities, dst, dstIndex);
        return (dstIndex + 4) - start;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        if (this.blob != null) {
            System.arraycopy(this.blob, 0, dst, dstIndex, this.blob.length);
            dstIndex += this.blob.length;
        } else {
            System.arraycopy(this.lmHash, 0, dst, dstIndex, this.lmHash.length);
            dstIndex += this.lmHash.length;
            System.arraycopy(this.ntHash, 0, dst, dstIndex, this.ntHash.length);
            dstIndex += this.ntHash.length;
            dstIndex += writeString(this.accountName, dst, dstIndex);
            dstIndex += writeString(this.primaryDomain, dst, dstIndex);
        }
        SmbTransport smbTransport = this.session.transport;
        dstIndex += writeString(SmbTransport.NATIVE_OS, dst, dstIndex);
        smbTransport = this.session.transport;
        return (dstIndex + writeString(SmbTransport.NATIVE_LANMAN, dst, dstIndex)) - start;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        int i;
        int i2 = 0;
        StringBuilder append = new StringBuilder().append("SmbComSessionSetupAndX[").append(super.toString()).append(",snd_buf_size=").append(this.session.transport.snd_buf_size).append(",maxMpxCount=").append(this.session.transport.maxMpxCount).append(",VC_NUMBER=");
        SmbTransport smbTransport = this.session.transport;
        StringBuilder append2 = append.append(1).append(",sessionKey=").append(this.sessionKey).append(",lmHash.length=");
        if (this.lmHash == null) {
            i = 0;
        } else {
            i = this.lmHash.length;
        }
        append = append2.append(i).append(",ntHash.length=");
        if (this.ntHash != null) {
            i2 = this.ntHash.length;
        }
        append = append.append(i2).append(",capabilities=").append(this.capabilities).append(",accountName=").append(this.accountName).append(",primaryDomain=").append(this.primaryDomain).append(",NATIVE_OS=");
        SmbTransport smbTransport2 = this.session.transport;
        append = append.append(SmbTransport.NATIVE_OS).append(",NATIVE_LANMAN=");
        smbTransport2 = this.session.transport;
        return new String(append.append(SmbTransport.NATIVE_LANMAN).append("]").toString());
    }
}
