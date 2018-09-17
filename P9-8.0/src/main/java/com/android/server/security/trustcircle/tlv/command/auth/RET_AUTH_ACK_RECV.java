package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class RET_AUTH_ACK_RECV extends TLVRootTree {
    public static final int ID = -2147483639;
    public static final short TAG_IV = (short) 5377;
    public static final short TAG_RET_AUTH_ACK_RECV = (short) 21;
    public static final short TAG_SESSION_KEY = (short) 5376;
    public TLVByteArrayInvoker iv;
    public TLVByteArrayInvoker sessionKey;

    public RET_AUTH_ACK_RECV() {
        this.sessionKey = new TLVByteArrayInvoker(TAG_SESSION_KEY);
        this.iv = new TLVByteArrayInvoker(TAG_IV);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.sessionKey);
        this.mNodeList.add(this.iv);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return (short) 21;
    }
}
