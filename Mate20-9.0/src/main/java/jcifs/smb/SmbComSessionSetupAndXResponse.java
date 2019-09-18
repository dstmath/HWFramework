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

    /* access modifiers changed from: package-private */
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        boolean z = true;
        int start = bufferIndex;
        if ((buffer[bufferIndex] & 1) != 1) {
            z = false;
        }
        this.isLoggedInAsGuest = z;
        int bufferIndex2 = bufferIndex + 2;
        if (this.extendedSecurity) {
            int blobLength = readInt2(buffer, bufferIndex2);
            bufferIndex2 += 2;
            this.blob = new byte[blobLength];
        }
        return bufferIndex2 - start;
    }

    /* access modifiers changed from: package-private */
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        int start = bufferIndex;
        if (this.extendedSecurity) {
            System.arraycopy(buffer, bufferIndex, this.blob, 0, this.blob.length);
            bufferIndex += this.blob.length;
        }
        this.nativeOs = readString(buffer, bufferIndex);
        int bufferIndex2 = bufferIndex + stringWireLength(this.nativeOs, bufferIndex);
        this.nativeLanMan = readString(buffer, bufferIndex2, start + this.byteCount, 255, this.useUnicode);
        int bufferIndex3 = bufferIndex2 + stringWireLength(this.nativeLanMan, bufferIndex2);
        if (!this.extendedSecurity) {
            this.primaryDomain = readString(buffer, bufferIndex3, start + this.byteCount, 255, this.useUnicode);
            bufferIndex3 += stringWireLength(this.primaryDomain, bufferIndex3);
        }
        return bufferIndex3 - start;
    }

    public String toString() {
        return new String("SmbComSessionSetupAndXResponse[" + super.toString() + ",isLoggedInAsGuest=" + this.isLoggedInAsGuest + ",nativeOs=" + this.nativeOs + ",nativeLanMan=" + this.nativeLanMan + ",primaryDomain=" + this.primaryDomain + "]");
    }
}
