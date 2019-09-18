package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_GET_TCIS_ID extends TLVTree.TLVRootTree {
    public static final int ID = 13;

    public CMD_GET_TCIS_ID() {
        this.mNodeList = new Vector();
    }

    public int getCmdID() {
        return 13;
    }

    public short getTreeTag() {
        return 0;
    }
}
