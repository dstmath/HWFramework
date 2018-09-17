package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_REG_CANCEL extends TLVRootTree {
    public static final int ID = 16;
    public static final short TAG_CMD_REG_CANCEL = (short) 10;
    public static final short TAG_USER_ID = (short) 2560;
    public TLVLongInvoker userID;

    public CMD_REG_CANCEL() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 16;
    }

    public short getTreeTag() {
        return (short) 10;
    }
}
