package jcifs.smb;

class SmbComSessionSetupAndXResponse extends AndXServerMessageBlock {
    byte[] blob = null;
    boolean isLoggedInAsGuest;
    private String nativeLanMan = "";
    private String nativeOs = "";
    private String primaryDomain = "";

    SmbComSessionSetupAndXResponse(ServerMessageBlock andx) {
        super(andx);
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
        if ((buffer[bufferIndex] & 1) != 1) {
            z = false;
        }
        this.isLoggedInAsGuest = z;
        bufferIndex += 2;
        if (this.extendedSecurity) {
            int blobLength = ServerMessageBlock.readInt2(buffer, bufferIndex);
            bufferIndex += 2;
            this.blob = new byte[blobLength];
        }
        return bufferIndex - start;
    }

    int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        int start = bufferIndex;
        if (this.extendedSecurity) {
            System.arraycopy(buffer, bufferIndex, this.blob, 0, this.blob.length);
            bufferIndex += this.blob.length;
        }
        this.nativeOs = readString(buffer, bufferIndex);
        bufferIndex += stringWireLength(this.nativeOs, bufferIndex);
        this.nativeLanMan = readString(buffer, bufferIndex, start + this.byteCount, 255, this.useUnicode);
        bufferIndex += stringWireLength(this.nativeLanMan, bufferIndex);
        if (!this.extendedSecurity) {
            this.primaryDomain = readString(buffer, bufferIndex, start + this.byteCount, 255, this.useUnicode);
            bufferIndex += stringWireLength(this.primaryDomain, bufferIndex);
        }
        return bufferIndex - start;
    }

    public String toString() {
        return new String("SmbComSessionSetupAndXResponse[" + super.toString() + ",isLoggedInAsGuest=" + this.isLoggedInAsGuest + ",nativeOs=" + this.nativeOs + ",nativeLanMan=" + this.nativeLanMan + ",primaryDomain=" + this.primaryDomain + "]");
    }
}
