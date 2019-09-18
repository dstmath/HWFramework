package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_REG_REQ extends TLVTree.TLVRootTree {
    public static final int ID = 2;
    public static final short TAG_CMD_REG_REQ = 1;
    public static final short TAG_SESSION_ID = 257;
    public static final short TAG_USER_ID = 256;
    public TLVByteArrayInvoker sessionID = new TLVByteArrayInvoker(TAG_SESSION_ID);
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_REG_REQ() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.sessionID);
    }

    public int getCmdID() {
        return 2;
    }

    public short getTreeTag() {
        return 1;
    }
}
