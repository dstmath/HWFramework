package jcifs.netbios;

class NameQueryResponse extends NameServicePacket {
    NameQueryResponse() {
        this.recordName = new Name();
    }

    /* access modifiers changed from: package-private */
    public int writeBodyWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readBodyWireFormat(byte[] src, int srcIndex) {
        return readResourceRecordWireFormat(src, srcIndex);
    }

    /* access modifiers changed from: package-private */
    public int writeRDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readRDataWireFormat(byte[] src, int srcIndex) {
        boolean groupName = false;
        if (this.resultCode != 0 || this.opCode != 0) {
            return 0;
        }
        if ((src[srcIndex] & 128) == 128) {
            groupName = true;
        }
        int nodeType = (src[srcIndex] & 96) >> 5;
        int address = readInt4(src, srcIndex + 2);
        if (address != 0) {
            this.addrEntry[this.addrIndex] = new NbtAddress(this.recordName, address, groupName, nodeType);
        } else {
            this.addrEntry[this.addrIndex] = null;
        }
        return 6;
    }

    public String toString() {
        return new String("NameQueryResponse[" + super.toString() + ",addrEntry=" + this.addrEntry + "]");
    }
}
