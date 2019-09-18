package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_REG_CANCEL extends TLVTree.TLVRootTree {
    public static final int ID = 16;
    public static final short TAG_CMD_REG_CANCEL = 10;
    public static final short TAG_USER_ID = 2560;
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_REG_CANCEL() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 16;
    }

    public short getTreeTag() {
        return 10;
    }
}
