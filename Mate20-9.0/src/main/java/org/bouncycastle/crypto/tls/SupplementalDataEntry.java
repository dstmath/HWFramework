package org.bouncycastle.crypto.tls;

public class SupplementalDataEntry {
    protected byte[] data;
    protected int dataType;

    public SupplementalDataEntry(int i, byte[] bArr) {
        this.dataType = i;
        this.data = bArr;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getDataType() {
        return this.dataType;
    }
}
