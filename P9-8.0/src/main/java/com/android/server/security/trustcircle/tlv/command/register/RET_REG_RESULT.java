package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;

public class RET_REG_RESULT extends TLVRootTree {
    public static final int ID = -2147483645;

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return (short) 0;
    }
}
