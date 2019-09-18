package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_AUTH_ACK_RECV extends TLVTree.TLVRootTree {
    public static final int ID = 9;
    public static final short TAG_AUTH_ID = 5120;
    public static final short TAG_CMD_AUTH_ACK_RECV = 20;
    public static final short TAG_MAC = 5121;
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(TAG_AUTH_ID);
    public TLVByteArrayInvoker mac = new TLVByteArrayInvoker(TAG_MAC);

    public CMD_AUTH_ACK_RECV() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.mac);
    }

    public int getCmdID() {
        return 9;
    }

    public short getTreeTag() {
        return 20;
    }
}
