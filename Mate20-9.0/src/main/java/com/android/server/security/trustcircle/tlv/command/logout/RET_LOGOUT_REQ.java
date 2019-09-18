package com.android.server.security.trustcircle.tlv.command.logout;

import com.android.server.security.trustcircle.tlv.core.TLVTree;

public class RET_LOGOUT_REQ extends TLVTree.TLVRootTree {
    public static final int ID = -2147483634;

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 0;
    }
}
