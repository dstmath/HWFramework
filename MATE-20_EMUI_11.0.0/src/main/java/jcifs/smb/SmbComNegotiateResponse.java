package jcifs.smb;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import jcifs.smb.SmbTransport;
import jcifs.util.Hexdump;
import jcifs.util.LogStream;

class SmbComNegotiateResponse extends ServerMessageBlock {
    int dialectIndex;
    SmbTransport.ServerData server;

    SmbComNegotiateResponse(SmbTransport.ServerData server2) {
        this.server = server2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        boolean z;
        boolean z2;
        boolean z3 = true;
        this.dialectIndex = readInt2(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + 2;
        if (this.dialectIndex > 10) {
            return bufferIndex2 - bufferIndex;
        }
        int bufferIndex3 = bufferIndex2 + 1;
        this.server.securityMode = buffer[bufferIndex2] & 255;
        this.server.security = this.server.securityMode & 1;
        SmbTransport.ServerData serverData = this.server;
        if ((this.server.securityMode & 2) == 2) {
            z = true;
        } else {
            z = false;
        }
        serverData.encryptedPasswords = z;
        SmbTransport.ServerData serverData2 = this.server;
        if ((this.server.securityMode & 4) == 4) {
            z2 = true;
        } else {
            z2 = false;
        }
        serverData2.signaturesEnabled = z2;
        SmbTransport.ServerData serverData3 = this.server;
        if ((this.server.securityMode & 8) != 8) {
            z3 = false;
        }
        serverData3.signaturesRequired = z3;
        this.server.maxMpxCount = readInt2(buffer, bufferIndex3);
        int bufferIndex4 = bufferIndex3 + 2;
        this.server.maxNumberVcs = readInt2(buffer, bufferIndex4);
        int bufferIndex5 = bufferIndex4 + 2;
        this.server.maxBufferSize = readInt4(buffer, bufferIndex5);
        int bufferIndex6 = bufferIndex5 + 4;
        this.server.maxRawSize = readInt4(buffer, bufferIndex6);
        int bufferIndex7 = bufferIndex6 + 4;
        this.server.sessionKey = readInt4(buffer, bufferIndex7);
        int bufferIndex8 = bufferIndex7 + 4;
        this.server.capabilities = readInt4(buffer, bufferIndex8);
        int bufferIndex9 = bufferIndex8 + 4;
        this.server.serverTime = readTime(buffer, bufferIndex9);
        int bufferIndex10 = bufferIndex9 + 8;
        this.server.serverTimeZone = readInt2(buffer, bufferIndex10);
        int bufferIndex11 = bufferIndex10 + 2;
        this.server.encryptionKeyLength = buffer[bufferIndex11] & 255;
        return (bufferIndex11 + 1) - bufferIndex;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        if ((this.server.capabilities & Integer.MIN_VALUE) == 0) {
            this.server.encryptionKey = new byte[this.server.encryptionKeyLength];
            System.arraycopy(buffer, bufferIndex, this.server.encryptionKey, 0, this.server.encryptionKeyLength);
            bufferIndex += this.server.encryptionKeyLength;
            if (this.byteCount > this.server.encryptionKeyLength) {
                int len = 0;
                try {
                    if ((this.flags2 & 32768) == 32768) {
                        do {
                            if (buffer[bufferIndex + len] == 0 && buffer[bufferIndex + len + 1] == 0) {
                                this.server.oemDomainName = new String(buffer, bufferIndex, len, SmbConstants.UNI_ENCODING);
                            } else {
                                len += 2;
                            }
                        } while (len <= 256);
                        throw new RuntimeException("zero termination not found");
                    }
                    while (buffer[bufferIndex + len] != 0) {
                        len++;
                        if (len > 256) {
                            throw new RuntimeException("zero termination not found");
                        }
                    }
                    this.server.oemDomainName = new String(buffer, bufferIndex, len, ServerMessageBlock.OEM_ENCODING);
                } catch (UnsupportedEncodingException uee) {
                    LogStream logStream = log;
                    if (LogStream.level > 1) {
                        uee.printStackTrace(log);
                    }
                }
                bufferIndex += len;
            } else {
                this.server.oemDomainName = new String();
            }
        } else {
            this.server.guid = new byte[16];
            System.arraycopy(buffer, bufferIndex, this.server.guid, 0, 16);
            this.server.oemDomainName = new String();
        }
        return bufferIndex - bufferIndex;
    }

    @Override // jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComNegotiateResponse[" + super.toString() + ",wordCount=" + this.wordCount + ",dialectIndex=" + this.dialectIndex + ",securityMode=0x" + Hexdump.toHexString(this.server.securityMode, 1) + ",security=" + (this.server.security == 0 ? "share" : "user") + ",encryptedPasswords=" + this.server.encryptedPasswords + ",maxMpxCount=" + this.server.maxMpxCount + ",maxNumberVcs=" + this.server.maxNumberVcs + ",maxBufferSize=" + this.server.maxBufferSize + ",maxRawSize=" + this.server.maxRawSize + ",sessionKey=0x" + Hexdump.toHexString(this.server.sessionKey, 8) + ",capabilities=0x" + Hexdump.toHexString(this.server.capabilities, 8) + ",serverTime=" + new Date(this.server.serverTime) + ",serverTimeZone=" + this.server.serverTimeZone + ",encryptionKeyLength=" + this.server.encryptionKeyLength + ",byteCount=" + this.byteCount + ",oemDomainName=" + this.server.oemDomainName + "]");
    }
}
