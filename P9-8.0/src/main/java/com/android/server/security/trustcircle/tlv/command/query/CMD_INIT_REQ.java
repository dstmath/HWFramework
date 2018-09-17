package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_INIT_REQ extends TLVRootTree {
    public static final int ID = 21;
    public static final short TAG_CMD_INIT_REQ = (short) 29;
    public static final short TAG_USER_HANDLE = (short) 7424;
    public TLVIntegerInvoker userHandle;

    public CMD_INIT_REQ() {
        this.userHandle = new TLVIntegerInvoker(TAG_USER_HANDLE);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userHandle);
    }

    public int getCmdID() {
        return 21;
    }

    public short getTreeTag() {
        return (short) 29;
    }
}
