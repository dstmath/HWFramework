package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_GET_PK extends TLVTree.TLVRootTree {
    public static final int ID = 11;
    public static final short TAG_AUTH_ID = 6145;
    public static final short TAG_CMD_GET_PK = 24;
    public static final short TAG_USER_ID = 6144;
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(TAG_AUTH_ID);
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_GET_PK() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.authID);
    }

    public int getCmdID() {
        return 11;
    }

    public short getTreeTag() {
        return 24;
    }
}
