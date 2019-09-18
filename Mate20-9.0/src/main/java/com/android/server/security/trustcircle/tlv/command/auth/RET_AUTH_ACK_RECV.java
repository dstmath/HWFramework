package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class RET_AUTH_ACK_RECV extends TLVTree.TLVRootTree {
    public static final int ID = -2147483639;
    public static final short TAG_IV = 5377;
    public static final short TAG_RET_AUTH_ACK_RECV = 21;
    public static final short TAG_SESSION_KEY = 5376;
    public TLVByteArrayInvoker iv = new TLVByteArrayInvoker(TAG_IV);
    public TLVByteArrayInvoker sessionKey = new TLVByteArrayInvoker(TAG_SESSION_KEY);

    public RET_AUTH_ACK_RECV() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.sessionKey);
        this.mNodeList.add(this.iv);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 21;
    }
}
