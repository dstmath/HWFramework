package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_UNREG_REQ extends TLVRootTree {
    public static final int ID = 15;
    public static final short TAG_CMD_UNREG_REQ = (short) 8;
    public static final short TAG_USER_ID = (short) 2048;
    public TLVLongInvoker userID;

    public CMD_UNREG_REQ() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 15;
    }

    public short getTreeTag() {
        return (short) 8;
    }
}
