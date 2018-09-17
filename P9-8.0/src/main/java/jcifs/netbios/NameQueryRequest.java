package jcifs.netbios;

class NameQueryRequest extends NameServicePacket {
    NameQueryRequest(Name name) {
        this.questionName = name;
        this.questionType = 32;
    }

    int writeBodyWireFormat(byte[] dst, int dstIndex) {
        return writeQuestionSectionWireFormat(dst, dstIndex);
    }

    int readBodyWireFormat(byte[] src, int srcIndex) {
        return readQuestionSectionWireFormat(src, srcIndex);
    }

    int writeRDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readRDataWireFormat(byte[] src, int srcIndex) {
        return 0;
    }

    public String toString() {
        return new String("NameQueryRequest[" + super.toString() + "]");
    }
}
