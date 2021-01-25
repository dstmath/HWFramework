package jcifs.dcerpc;

import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.util.Hexdump;

public class DcerpcBind extends DcerpcMessage {
    static final String[] result_message = {"0", "DCERPC_BIND_ERR_ABSTRACT_SYNTAX_NOT_SUPPORTED", "DCERPC_BIND_ERR_PROPOSED_TRANSFER_SYNTAXES_NOT_SUPPORTED", "DCERPC_BIND_ERR_LOCAL_LIMIT_EXCEEDED"};
    DcerpcBinding binding;
    int max_recv;
    int max_xmit;

    static String getResultMessage(int result) {
        return result < 4 ? result_message[result] : "0x" + Hexdump.toHexString(result, 4);
    }

    @Override // jcifs.dcerpc.DcerpcMessage
    public DcerpcException getResult() {
        if (this.result != 0) {
            return new DcerpcException(getResultMessage(this.result));
        }
        return null;
    }

    public DcerpcBind() {
    }

    DcerpcBind(DcerpcBinding binding2, DcerpcHandle handle) {
        this.binding = binding2;
        this.max_xmit = handle.max_xmit;
        this.max_recv = handle.max_recv;
        this.ptype = 11;
        this.flags = 3;
    }

    @Override // jcifs.dcerpc.DcerpcMessage
    public int getOpnum() {
        return 0;
    }

    @Override // jcifs.dcerpc.DcerpcMessage
    public void encode_in(NdrBuffer buf) throws NdrException {
        buf.enc_ndr_short(this.max_xmit);
        buf.enc_ndr_short(this.max_recv);
        buf.enc_ndr_long(0);
        buf.enc_ndr_small(1);
        buf.enc_ndr_small(0);
        buf.enc_ndr_short(0);
        buf.enc_ndr_short(0);
        buf.enc_ndr_small(1);
        buf.enc_ndr_small(0);
        this.binding.uuid.encode(buf);
        buf.enc_ndr_short(this.binding.major);
        buf.enc_ndr_short(this.binding.minor);
        DCERPC_UUID_SYNTAX_NDR.encode(buf);
        buf.enc_ndr_long(2);
    }

    @Override // jcifs.dcerpc.DcerpcMessage
    public void decode_out(NdrBuffer buf) throws NdrException {
        buf.dec_ndr_short();
        buf.dec_ndr_short();
        buf.dec_ndr_long();
        buf.advance(buf.dec_ndr_short());
        buf.align(4);
        buf.dec_ndr_small();
        buf.align(4);
        this.result = buf.dec_ndr_short();
        buf.dec_ndr_short();
        buf.advance(20);
    }
}
