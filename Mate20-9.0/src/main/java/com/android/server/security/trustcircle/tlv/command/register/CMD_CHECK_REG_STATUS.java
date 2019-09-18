package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_CHECK_REG_STATUS extends TLVTree.TLVRootTree {
    public static final int ID = 1;
    public static final short TAG_CMD_CHECK_REG_STATUS = 13;
    public static final short TAG_USER_ID = 3328;
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_CHECK_REG_STATUS() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 1;
    }

    public short getTreeTag() {
        return 13;
    }
}
