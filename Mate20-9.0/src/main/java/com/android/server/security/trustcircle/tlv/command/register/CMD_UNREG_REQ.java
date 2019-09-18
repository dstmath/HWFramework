package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_UNREG_REQ extends TLVTree.TLVRootTree {
    public static final int ID = 15;
    public static final short TAG_CMD_UNREG_REQ = 8;
    public static final short TAG_USER_ID = 2048;
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_UNREG_REQ() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 15;
    }

    public short getTreeTag() {
        return 8;
    }
}
