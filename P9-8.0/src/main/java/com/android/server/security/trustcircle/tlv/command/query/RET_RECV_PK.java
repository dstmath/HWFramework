package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;

public class RET_RECV_PK extends TLVRootTree {
    public static final int ID = -2147483636;

    public int getCmdID() {
        return -2147483636;
    }

    public short getTreeTag() {
        return (short) 0;
    }
}
