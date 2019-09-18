package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class RET_GET_LOGIN_STATUS extends TLVTree.TLVRootTree {
    public static final int ID = -2147483628;
    public static final short TAG_RET_GET_LOGIN_STATUS = 12;
    public static final short TAG_USER_ID = 3072;
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public RET_GET_LOGIN_STATUS() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 12;
    }
}
