package jcifs.netbios;

/* access modifiers changed from: package-private */
public class NameQueryRequest extends NameServicePacket {
    NameQueryRequest(Name name) {
        this.questionName = name;
        this.questionType = 32;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int writeBodyWireFormat(byte[] dst, int dstIndex) {
        return writeQuestionSectionWireFormat(dst, dstIndex);
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int readBodyWireFormat(byte[] src, int srcIndex) {
        return readQuestionSectionWireFormat(src, srcIndex);
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int writeRDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int readRDataWireFormat(byte[] src, int srcIndex) {
        return 0;
    }

    @Override // jcifs.netbios.NameServicePacket
    public String toString() {
        return new String("NameQueryRequest[" + super.toString() + "]");
    }
}
