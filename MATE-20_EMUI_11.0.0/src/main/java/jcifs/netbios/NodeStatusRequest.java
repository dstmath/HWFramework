package jcifs.netbios;

/* access modifiers changed from: package-private */
public class NodeStatusRequest extends NameServicePacket {
    NodeStatusRequest(Name name) {
        this.questionName = name;
        this.questionType = 33;
        this.isRecurDesired = false;
        this.isBroadcast = false;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int writeBodyWireFormat(byte[] dst, int dstIndex) {
        int tmp = this.questionName.hexCode;
        this.questionName.hexCode = 0;
        int result = writeQuestionSectionWireFormat(dst, dstIndex);
        this.questionName.hexCode = tmp;
        return result;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int readBodyWireFormat(byte[] src, int srcIndex) {
        return 0;
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
        return new String("NodeStatusRequest[" + super.toString() + "]");
    }
}
