package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVTree;

public class RET_CHECK_REG_STATUS extends TLVTree.TLVRootTree {
    public static final int ID = -2147483647;

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 0;
    }
}
