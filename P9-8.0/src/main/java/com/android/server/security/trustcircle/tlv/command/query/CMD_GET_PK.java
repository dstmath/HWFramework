package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_GET_PK extends TLVRootTree {
    public static final int ID = 11;
    public static final short TAG_AUTH_ID = (short) 6145;
    public static final short TAG_CMD_GET_PK = (short) 24;
    public static final short TAG_USER_ID = (short) 6144;
    public TLVLongInvoker authID;
    public TLVLongInvoker userID;

    public CMD_GET_PK() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.authID);
    }

    public int getCmdID() {
        return 11;
    }

    public short getTreeTag() {
        return (short) 24;
    }
}
