package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_INIT_REQ extends TLVTree.TLVRootTree {
    public static final int ID = 21;
    public static final short TAG_CMD_INIT_REQ = 29;
    public static final short TAG_USER_HANDLE = 7424;
    public TLVNumberInvoker.TLVIntegerInvoker userHandle = new TLVNumberInvoker.TLVIntegerInvoker(TAG_USER_HANDLE);

    public CMD_INIT_REQ() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userHandle);
    }

    public int getCmdID() {
        return 21;
    }

    public short getTreeTag() {
        return 29;
    }
}
