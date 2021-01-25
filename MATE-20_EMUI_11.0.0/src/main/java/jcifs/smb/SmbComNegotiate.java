package jcifs.smb;

import java.io.UnsupportedEncodingException;

/* access modifiers changed from: package-private */
public class SmbComNegotiate extends ServerMessageBlock {
    private static final String DIALECTS = "\u0002NT LM 0.12\u0000";

    SmbComNegotiate() {
        this.command = 114;
        this.flags2 = DEFAULT_FLAGS2;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.smb.ServerMessageBlock
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        try {
            byte[] dialects = DIALECTS.getBytes("ASCII");
            System.arraycopy(dialects, 0, dst, dstIndex, dialects.length);
            return dialects.length;
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

    @Override // jcifs.smb.ServerMessageBlock
    public String toString() {
        return new String("SmbComNegotiate[" + super.toString() + ",wordCount=" + this.wordCount + ",dialects=NT LM 0.12]");
    }
}
