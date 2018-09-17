package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_AUTH_ACK_RECV extends TLVRootTree {
    public static final int ID = 9;
    public static final short TAG_AUTH_ID = (short) 5120;
    public static final short TAG_CMD_AUTH_ACK_RECV = (short) 20;
    public static final short TAG_MAC = (short) 5121;
    public TLVLongInvoker authID;
    public TLVByteArrayInvoker mac;

    public CMD_AUTH_ACK_RECV() {
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
        this.mac = new TLVByteArrayInvoker(TAG_MAC);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.mac);
    }

    public int getCmdID() {
        return 9;
    }

    public short getTreeTag() {
        return (short) 20;
    }
}
