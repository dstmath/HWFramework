package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_GET_LOGIN_STATUS extends TLVRootTree {
    public static final int ID = 20;

    public CMD_GET_LOGIN_STATUS() {
        this.mNodeList = new Vector();
    }

    public int getCmdID() {
        return 20;
    }

    public short getTreeTag() {
        return (short) 0;
    }
}
