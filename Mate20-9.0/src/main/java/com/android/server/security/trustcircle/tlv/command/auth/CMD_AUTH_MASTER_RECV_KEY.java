package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class CMD_AUTH_MASTER_RECV_KEY extends TLVTree.TLVRootTree {
    public static final int ID = 19;
    public static final short TAG_AUTH_ID = 7168;
    public static final short TAG_CMD_AUTH_MASTER_RECV_KEY = 28;
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(TAG_AUTH_ID);
    public TLVTreeInvoker.TLVChildTreeInvoker cert = new TLVTreeInvoker.TLVChildTreeInvoker(3842);

    public CMD_AUTH_MASTER_RECV_KEY() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.cert);
    }

    public int getCmdID() {
        return 19;
    }

    public short getTreeTag() {
        return 28;
    }
}
