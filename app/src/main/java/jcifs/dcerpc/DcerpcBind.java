package jcifs.dcerpc;

import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.util.Hexdump;

public class DcerpcBind extends DcerpcMessage {
    static final String[] result_message = null;
    DcerpcBinding binding;
    int max_recv;
    int max_xmit;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.dcerpc.DcerpcBind.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.dcerpc.DcerpcBind.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.dcerpc.DcerpcBind.<clinit>():void");
    }

    static String getResultMessage(int result) {
        return result < 4 ? result_message[result] : "0x" + Hexdump.toHexString(result, 4);
    }

    public DcerpcException getResult() {
        if (this.result != 0) {
            return new DcerpcException(getResultMessage(this.result));
        }
        return null;
    }

    DcerpcBind(DcerpcBinding binding, DcerpcHandle handle) {
        this.binding = binding;
        this.max_xmit = handle.max_xmit;
        this.max_recv = handle.max_recv;
        this.ptype = 11;
        this.flags = 3;
    }

    public int getOpnum() {
        return 0;
    }

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
