package jcifs.smb;

import java.io.UnsupportedEncodingException;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Encdec;
import jcifs.util.Hexdump;
import jcifs.util.LogStream;

public class NtlmContext {
    NtlmPasswordAuthentication auth;
    boolean isEstablished = false;
    LogStream log;
    String netbiosName = null;
    int ntlmsspFlags;
    byte[] serverChallenge = null;
    byte[] signingKey = null;
    int state = 1;
    String workstation;

    public NtlmContext(NtlmPasswordAuthentication auth2, boolean doSigning) {
        this.auth = auth2;
        this.ntlmsspFlags = this.ntlmsspFlags | 4 | 524288 | 536870912;
        if (doSigning) {
            this.ntlmsspFlags |= 1073774608;
        }
        this.workstation = Type1Message.getDefaultWorkstation();
        this.log = LogStream.getInstance();
    }

    public String toString() {
        String ret;
        String ret2;
        String ret3 = "NtlmContext[auth=" + this.auth + ",ntlmsspFlags=0x" + Hexdump.toHexString(this.ntlmsspFlags, 8) + ",workstation=" + this.workstation + ",isEstablished=" + this.isEstablished + ",state=" + this.state + ",serverChallenge=";
        if (this.serverChallenge == null) {
            ret = ret3 + "null";
        } else {
            ret = ret3 + Hexdump.toHexString(this.serverChallenge, 0, this.serverChallenge.length * 2);
        }
        String ret4 = ret + ",signingKey=";
        if (this.signingKey == null) {
            ret2 = ret4 + "null";
        } else {
            ret2 = ret4 + Hexdump.toHexString(this.signingKey, 0, this.signingKey.length * 2);
        }
        return ret2 + "]";
    }

    public boolean isEstablished() {
        return this.isEstablished;
    }

    public byte[] getServerChallenge() {
        return this.serverChallenge;
    }

    public byte[] getSigningKey() {
        return this.signingKey;
    }

    public String getNetbiosName() {
        return this.netbiosName;
    }

    private String getNtlmsspListItem(byte[] type2token, int id0) {
        int ri = 58;
        while (true) {
            int id = Encdec.dec_uint16le(type2token, ri);
            int len = Encdec.dec_uint16le(type2token, ri + 2);
            int ri2 = ri + 4;
            if (id == 0 || ri2 + len > type2token.length) {
                break;
            } else if (id == id0) {
                try {
                    return new String(type2token, ri2, len, SmbConstants.UNI_ENCODING);
                } catch (UnsupportedEncodingException e) {
                }
            } else {
                ri = ri2 + len;
            }
        }
        return null;
    }

    public byte[] initSecContext(byte[] token, int offset, int len) throws SmbException {
        byte[] token2;
        switch (this.state) {
            case 1:
                Type1Message msg1 = new Type1Message(this.ntlmsspFlags, this.auth.getDomain(), this.workstation);
                token2 = msg1.toByteArray();
                LogStream logStream = this.log;
                if (LogStream.level >= 4) {
                    this.log.println(msg1);
                    LogStream logStream2 = this.log;
                    if (LogStream.level >= 6) {
                        Hexdump.hexdump(this.log, token2, 0, token2.length);
                    }
                }
                this.state++;
                break;
            case 2:
                try {
                    Type2Message msg2 = new Type2Message(token);
                    LogStream logStream3 = this.log;
                    if (LogStream.level >= 4) {
                        this.log.println(msg2);
                        LogStream logStream4 = this.log;
                        if (LogStream.level >= 6) {
                            Hexdump.hexdump(this.log, token, 0, token.length);
                        }
                    }
                    this.serverChallenge = msg2.getChallenge();
                    this.ntlmsspFlags &= msg2.getFlags();
                    Type3Message msg3 = new Type3Message(msg2, this.auth.getPassword(), this.auth.getDomain(), this.auth.getUsername(), this.workstation, this.ntlmsspFlags);
                    token2 = msg3.toByteArray();
                    LogStream logStream5 = this.log;
                    if (LogStream.level >= 4) {
                        this.log.println(msg3);
                        LogStream logStream6 = this.log;
                        if (LogStream.level >= 6) {
                            Hexdump.hexdump(this.log, token2, 0, token2.length);
                        }
                    }
                    if ((this.ntlmsspFlags & 16) != 0) {
                        this.signingKey = msg3.getMasterKey();
                    }
                    this.isEstablished = true;
                    this.state++;
                    break;
                } catch (Exception e) {
                    throw new SmbException(e.getMessage(), e);
                }
            default:
                throw new SmbException("Invalid state");
        }
        return token2;
    }
}
