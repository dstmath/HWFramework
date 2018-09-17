package com.android.server.security.trustcircle.tlv.command.login;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_LOGIN_CANCEL extends TLVRootTree {
    public static final int ID = 17;
    public static final short TAG_CMD_LOGIN_CANCEL = (short) 11;
    public static final short TAG_USER_ID = (short) 2816;
    public TLVLongInvoker userID;

    public CMD_LOGIN_CANCEL() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 17;
    }

    public short getTreeTag() {
        return (short) 11;
    }
}
