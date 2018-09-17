package com.android.server.security.trustcircle.tlv.command.logout;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_LOGOUT_REQ extends TLVRootTree {
    public static final int ID = 14;
    public static final short TAG_CMD_LOGOUT_REQ = (short) 9;
    public static final short TAG_USER_ID = (short) 2304;
    public TLVLongInvoker userID;

    public CMD_LOGOUT_REQ() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 14;
    }

    public short getTreeTag() {
        return (short) 9;
    }
}
