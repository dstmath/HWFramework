package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_CHECK_REG_STATUS extends TLVRootTree {
    public static final int ID = 1;
    public static final short TAG_CMD_CHECK_REG_STATUS = (short) 13;
    public static final short TAG_USER_ID = (short) 3328;
    public TLVLongInvoker userID;

    public CMD_CHECK_REG_STATUS() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 1;
    }

    public short getTreeTag() {
        return (short) 13;
    }
}
