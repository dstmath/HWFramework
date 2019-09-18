package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVTree;

public class RET_AUTH_CANCEL extends TLVTree.TLVRootTree {
    public static final int ID = -2147483630;

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 0;
    }
}
