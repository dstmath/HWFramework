package com.android.server.security.trustcircle.tlv.command.logout;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_LOGOUT_REQ extends TLVTree.TLVRootTree {
    public static final int ID = 14;
    public static final short TAG_CMD_LOGOUT_REQ = 9;
    public static final short TAG_USER_ID = 2304;
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_LOGOUT_REQ() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 14;
    }

    public short getTreeTag() {
        return 9;
    }
}
