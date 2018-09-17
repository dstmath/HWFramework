package jcifs.smb;

import java.io.UnsupportedEncodingException;

class SmbComNegotiate extends ServerMessageBlock {
    private static final String DIALECTS = "\u0002NT LM 0.12\u0000";

    SmbComNegotiate() {
        this.command = (byte) 114;
        this.flags2 = DEFAULT_FLAGS2;
    }

    int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int writeBytesWireFormat(byte[] dst, int dstIndex) {
        try {
            byte[] dialects = DIALECTS.getBytes("ASCII");
            System.arraycopy(dialects, 0, dst, dstIndex, dialects.length);
            return dialects.length;
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
        return new String("SmbComNegotiate[" + super.toString() + ",wordCount=" + this.wordCount + ",dialects=NT LM 0.12]");
    }
}
