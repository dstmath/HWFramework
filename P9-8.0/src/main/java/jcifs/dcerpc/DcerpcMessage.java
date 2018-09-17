package jcifs.dcerpc;

import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;

public abstract class DcerpcMessage extends NdrObject implements DcerpcConstants {
    protected int alloc_hint = 0;
    protected int call_id = 0;
    protected int flags = 0;
    protected int length = 0;
    protected int ptype = -1;
    protected int result = 0;

    public abstract void decode_out(NdrBuffer ndrBuffer) throws NdrException;

    public abstract void encode_in(NdrBuffer ndrBuffer) throws NdrException;

    public abstract int getOpnum();

    public boolean isFlagSet(int flag) {
        return (this.flags & flag) == flag;
    }

    public void unsetFlag(int flag) {
        this.flags &= flag ^ -1;
    }

    public void setFlag(int flag) {
        this.flags |= flag;
    }

    public DcerpcException getResult() {
        if (this.result != 0) {
            return new DcerpcException(this.result);
        }
        return null;
    }

    void encode_header(NdrBuffer buf) {
        buf.enc_ndr_small(5);
        buf.enc_ndr_small(0);
        buf.enc_ndr_small(this.ptype);
        buf.enc_ndr_small(this.flags);
        buf.enc_ndr_long(16);
        buf.enc_ndr_short(this.length);
        buf.enc_ndr_short(0);
        buf.enc_ndr_long(this.call_id);
    }

    void decode_header(NdrBuffer buf) throws NdrException {
        if (buf.dec_ndr_small() == 5 && buf.dec_ndr_small() == 0) {
            this.ptype = buf.dec_ndr_small();
            this.flags = buf.dec_ndr_small();
            if (buf.dec_ndr_long() != 16) {
                throw new NdrException("Data representation not supported");
            }
            this.length = buf.dec_ndr_short();
            if (buf.dec_ndr_short() != 0) {
                throw new NdrException("DCERPC authentication not supported");
            }
            this.call_id = buf.dec_ndr_long();
            return;
        }
        throw new NdrException("DCERPC version not supported");
    }

    public void encode(NdrBuffer buf) throws NdrException {
        int start = buf.getIndex();
        int alloc_hint_index = 0;
        buf.advance(16);
        if (this.ptype == 0) {
            alloc_hint_index = buf.getIndex();
            buf.enc_ndr_long(0);
            buf.enc_ndr_short(0);
            buf.enc_ndr_short(getOpnum());
        }
        encode_in(buf);
        this.length = buf.getIndex() - start;
        if (this.ptype == 0) {
            buf.setIndex(alloc_hint_index);
            this.alloc_hint = this.length - alloc_hint_index;
            buf.enc_ndr_long(this.alloc_hint);
        }
        buf.setIndex(start);
        encode_header(buf);
        buf.setIndex(this.length + start);
    }

    public void decode(NdrBuffer buf) throws NdrException {
        decode_header(buf);
        if (this.ptype == 12 || this.ptype == 2 || this.ptype == 3 || this.ptype == 13) {
            if (this.ptype == 2 || this.ptype == 3) {
                this.alloc_hint = buf.dec_ndr_long();
                buf.dec_ndr_short();
                buf.dec_ndr_short();
            }
            if (this.ptype == 3 || this.ptype == 13) {
                this.result = buf.dec_ndr_long();
                return;
            } else {
                decode_out(buf);
                return;
            }
        }
        throw new NdrException("Unexpected ptype: " + this.ptype);
    }
}
