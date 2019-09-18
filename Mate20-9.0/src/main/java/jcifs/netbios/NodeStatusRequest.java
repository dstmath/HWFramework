package jcifs.netbios;

class NodeStatusRequest extends NameServicePacket {
    NodeStatusRequest(Name name) {
        this.questionName = name;
        this.questionType = 33;
        this.isRecurDesired = false;
        this.isBroadcast = false;
    }

    /* access modifiers changed from: package-private */
    public int writeBodyWireFormat(byte[] dst, int dstIndex) {
        int tmp = this.questionName.hexCode;
        this.questionName.hexCode = 0;
        int result = writeQuestionSectionWireFormat(dst, dstIndex);
        this.questionName.hexCode = tmp;
        return result;
    }

    /* access modifiers changed from: package-private */
    public int readBodyWireFormat(byte[] src, int srcIndex) {
        return 0;
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
        return new String("NodeStatusRequest[" + super.toString() + "]");
    }
}
