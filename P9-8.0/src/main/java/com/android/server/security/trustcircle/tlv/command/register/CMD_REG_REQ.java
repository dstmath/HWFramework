package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_REG_REQ extends TLVRootTree {
    public static final int ID = 2;
    public static final short TAG_CMD_REG_REQ = (short) 1;
    public static final short TAG_SESSION_ID = (short) 257;
    public static final short TAG_USER_ID = (short) 256;
    public TLVByteArrayInvoker sessionID;
    public TLVLongInvoker userID;

    public CMD_REG_REQ() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.sessionID = new TLVByteArrayInvoker(TAG_SESSION_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.sessionID);
    }

    public int getCmdID() {
        return 2;
    }

    public short getTreeTag() {
        return (short) 1;
    }
}
