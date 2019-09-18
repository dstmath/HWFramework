package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVTree;

public class RET_QUERY_PK extends TLVTree.TLVRootTree {
    public static final int ID = -2147483638;

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 0;
    }
}
