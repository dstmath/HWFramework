package com.android.server.security.trustcircle.tlv.command.login;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_LOGIN_CANCEL extends TLVTree.TLVRootTree {
    public static final int ID = 17;
    public static final short TAG_CMD_LOGIN_CANCEL = 11;
    public static final short TAG_USER_ID = 2816;
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_LOGIN_CANCEL() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
    }

    public int getCmdID() {
        return 17;
    }

    public short getTreeTag() {
        return 11;
    }
}
