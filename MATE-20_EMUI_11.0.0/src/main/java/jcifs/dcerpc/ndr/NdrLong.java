package jcifs.dcerpc.ndr;

public class NdrLong extends NdrObject {
    public int value;

    public NdrLong(int value2) {
        this.value = value2;
    }

    @Override // jcifs.dcerpc.ndr.NdrObject
    public void encode(NdrBuffer dst) throws NdrException {
        dst.enc_ndr_long(this.value);
    }

    @Override // jcifs.dcerpc.ndr.NdrObject
    public void decode(NdrBuffer src) throws NdrException {
        this.value = src.dec_ndr_long();
    }
}
