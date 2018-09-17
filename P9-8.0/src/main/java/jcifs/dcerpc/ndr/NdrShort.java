package jcifs.dcerpc.ndr;

public class NdrShort extends NdrObject {
    public int value;

    public NdrShort(int value) {
        this.value = value & 255;
    }

    public void encode(NdrBuffer dst) throws NdrException {
        dst.enc_ndr_short(this.value);
    }

    public void decode(NdrBuffer src) throws NdrException {
        this.value = src.dec_ndr_short();
    }
}
