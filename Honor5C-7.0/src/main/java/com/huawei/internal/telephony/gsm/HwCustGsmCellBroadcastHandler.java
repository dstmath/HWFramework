package com.huawei.internal.telephony.gsm;

import com.google.android.mms.pdu.PduHeaders;
import java.util.Arrays;

public class HwCustGsmCellBroadcastHandler {
    private static final String FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG = "2B";
    private static final boolean IS_CBSPDU_HANDLER_NULL_MSG = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.internal.telephony.gsm.HwCustGsmCellBroadcastHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.internal.telephony.gsm.HwCustGsmCellBroadcastHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.internal.telephony.gsm.HwCustGsmCellBroadcastHandler.<clinit>():void");
    }

    public byte[] cbsPduAfterDiscardNullBlock(byte[] receivedPdu) {
        if (IS_CBSPDU_HANDLER_NULL_MSG) {
            int cbsPduLength = receivedPdu.length;
            if (cbsPduLength > 0) {
                StringBuilder sb = new StringBuilder();
                for (int j = cbsPduLength - 1; j > 0; j--) {
                    int b = receivedPdu[j] & PduHeaders.STORE_STATUS_ERROR_END;
                    if (b < 16) {
                        sb.append('0');
                    }
                    sb.append(Integer.toHexString(b));
                    if (!sb.toString().equalsIgnoreCase(FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG)) {
                        break;
                    }
                    cbsPduLength--;
                    sb.delete(0, sb.length());
                }
                return Arrays.copyOf(receivedPdu, cbsPduLength);
            }
        }
        return receivedPdu;
    }
}
