package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class RET_GET_LOGIN_STATUS extends TLVRootTree {
    public static final int ID = -2147483628;
    public static final short TAG_RET_GET_LOGIN_STATUS = (short) 12;
    public static final short TAG_USER_ID = (short) 3072;
    public TLVLongInvoker userID;

    public RET_GET_LOGIN_STATUS() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return (short) 12;
    }
}
