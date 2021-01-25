package com.huawei.android.net.netlink;

import android.net.netlink.StructNdMsg;
import java.nio.ByteBuffer;

public class StructNdMsgEx {
    public static final int STRUCT_SIZE = 12;
    private final StructNdMsg mStructNdMsg = new StructNdMsg();

    public void pack(ByteBuffer byteBuffer) {
        this.mStructNdMsg.pack(byteBuffer);
    }
}
