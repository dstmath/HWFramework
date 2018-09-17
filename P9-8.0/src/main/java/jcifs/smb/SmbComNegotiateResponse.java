package jcifs.smb;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import jcifs.util.Hexdump;
import jcifs.util.LogStream;

class SmbComNegotiateResponse extends ServerMessageBlock {
    int dialectIndex;
    ServerData server;

    SmbComNegotiateResponse(ServerData server) {
        this.server = server;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        boolean z = true;
        int start = bufferIndex;
        this.dialectIndex = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        if (this.dialectIndex > 10) {
            return bufferIndex - start;
        }
        boolean z2;
        int bufferIndex2 = bufferIndex + 1;
        this.server.securityMode = buffer[bufferIndex] & 255;
        this.server.security = this.server.securityMode & 1;
        ServerData serverData = this.server;
        if ((this.server.securityMode & 2) == 2) {
            z2 = true;
        } else {
            z2 = false;
        }
        serverData.encryptedPasswords = z2;
        serverData = this.server;
        if ((this.server.securityMode & 4) == 4) {
            z2 = true;
        } else {
            z2 = false;
        }
        serverData.signaturesEnabled = z2;
        ServerData serverData2 = this.server;
        if ((this.server.securityMode & 8) != 8) {
            z = false;
        }
        serverData2.signaturesRequired = z;
        this.server.maxMpxCount = ServerMessageBlock.readInt2(buffer, bufferIndex2);
        bufferIndex = bufferIndex2 + 2;
        this.server.maxNumberVcs = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        this.server.maxBufferSize = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.server.maxRawSize = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.server.sessionKey = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.server.capabilities = ServerMessageBlock.readInt4(buffer, bufferIndex);
        bufferIndex += 4;
        this.server.serverTime = ServerMessageBlock.readTime(buffer, bufferIndex);
        bufferIndex += 8;
        this.server.serverTimeZone = ServerMessageBlock.readInt2(buffer, bufferIndex);
        bufferIndex += 2;
        bufferIndex2 = bufferIndex + 1;
        this.server.encryptionKeyLength = buffer[bufferIndex] & 255;
        bufferIndex = bufferIndex2;
        return bufferIndex2 - start;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        int start = bufferIndex;
        if ((this.server.capabilities & Integer.MIN_VALUE) == 0) {
            this.server.encryptionKey = new byte[this.server.encryptionKeyLength];
            System.arraycopy(buffer, bufferIndex, this.server.encryptionKey, 0, this.server.encryptionKeyLength);
            bufferIndex += this.server.encryptionKeyLength;
            if (this.byteCount > this.server.encryptionKeyLength) {
                int len = 0;
                try {
                    if ((this.flags2 & 32768) == 32768) {
                        do {
                            if (buffer[bufferIndex + len] == (byte) 0 && buffer[(bufferIndex + len) + 1] == (byte) 0) {
                                this.server.oemDomainName = new String(buffer, bufferIndex, len, SmbConstants.UNI_ENCODING);
                            } else {
                                len += 2;
                            }
                        } while (len <= 256);
                        throw new RuntimeException("zero termination not found");
                    }
                    while (buffer[bufferIndex + len] != (byte) 0) {
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
        return bufferIndex - start;
    }

    public String toString() {
        return new String("SmbComNegotiateResponse[" + super.toString() + ",wordCount=" + this.wordCount + ",dialectIndex=" + this.dialectIndex + ",securityMode=0x" + Hexdump.toHexString(this.server.securityMode, 1) + ",security=" + (this.server.security == 0 ? "share" : "user") + ",encryptedPasswords=" + this.server.encryptedPasswords + ",maxMpxCount=" + this.server.maxMpxCount + ",maxNumberVcs=" + this.server.maxNumberVcs + ",maxBufferSize=" + this.server.maxBufferSize + ",maxRawSize=" + this.server.maxRawSize + ",sessionKey=0x" + Hexdump.toHexString(this.server.sessionKey, 8) + ",capabilities=0x" + Hexdump.toHexString(this.server.capabilities, 8) + ",serverTime=" + new Date(this.server.serverTime) + ",serverTimeZone=" + this.server.serverTimeZone + ",encryptionKeyLength=" + this.server.encryptionKeyLength + ",byteCount=" + this.byteCount + ",oemDomainName=" + this.server.oemDomainName + "]");
    }
}
