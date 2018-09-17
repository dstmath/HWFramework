package com.android.internal.telephony.cat;

import com.android.internal.telephony.CallFailCause;
import com.google.android.mms.pdu.PduPart;
import java.io.ByteArrayOutputStream;

abstract class ResponseData {
    public abstract void format(ByteArrayOutputStream byteArrayOutputStream);

    ResponseData() {
    }

    public static void writeLength(ByteArrayOutputStream buf, int length) {
        if (length > CallFailCause.INTERWORKING_UNSPECIFIED) {
            buf.write(PduPart.P_DISPOSITION_ATTACHMENT);
        }
        buf.write(length);
    }
}
