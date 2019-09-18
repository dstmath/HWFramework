package jcifs.netbios;

class NameQueryRequest extends NameServicePacket {
    NameQueryRequest(Name name) {
        this.questionName = name;
        this.questionType = 32;
    }

    /* access modifiers changed from: package-private */
    public int writeBodyWireFormat(byte[] dst, int dstIndex) {
        return writeQuestionSectionWireFormat(dst, dstIndex);
    }

    /* access modifiers changed from: package-private */
    public int readBodyWireFormat(byte[] src, int srcIndex) {
        return readQuestionSectionWireFormat(src, srcIndex);
    }

    /* access modifiers changed from: package-private */
    public int writeRDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readRDataWireFormat(byte[] src, int srcIndex) {
        return 0;
    }

    public String toString() {
        return new String("NameQueryRequest[" + super.toString() + "]");
    }
}
