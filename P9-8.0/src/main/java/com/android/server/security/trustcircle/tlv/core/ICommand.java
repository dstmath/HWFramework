package com.android.server.security.trustcircle.tlv.core;

import java.nio.ByteBuffer;

public interface ICommand {
    public static final int NO_ID = 0;
    public static final short NO_TLV = (short) 0;
    public static final String TAG = "TLV";

    Byte[] encapsulate();

    boolean parse(ByteBuffer byteBuffer);

    boolean parse(byte[] bArr);
}
