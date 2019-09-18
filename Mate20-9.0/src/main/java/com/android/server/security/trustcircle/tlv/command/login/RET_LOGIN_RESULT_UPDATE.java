package com.android.server.security.trustcircle.tlv.command.login;

import com.android.server.security.trustcircle.tlv.core.TLVTree;

public class RET_LOGIN_RESULT_UPDATE extends TLVTree.TLVRootTree {
    public static final int ID = -2147483643;

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 0;
    }
}
