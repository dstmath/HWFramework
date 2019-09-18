package com.android.internal.telephony.cat;

import java.io.ByteArrayOutputStream;

abstract class ResponseData {
    public abstract void format(ByteArrayOutputStream byteArrayOutputStream);

    ResponseData() {
    }

    public static void writeLength(ByteArrayOutputStream buf, int length) {
        if (length > 127) {
            buf.write(129);
        }
        buf.write(length);
    }
}
