package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class RET_AUTH_SYNC_ACK_RECV extends TLVTree.TLVRootTree {
    public static final int ID = -2147483640;
    public static final short TAG_AUTH_ID = 4864;
    public static final short TAG_IV = 4867;
    public static final short TAG_MAC = 4865;
    public static final short TAG_RET_AUTH_SYNC_ACK_RECV = 19;
    public static final short TAG_SESSION_KEY = 4866;
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(4864);
    public TLVByteArrayInvoker iv = new TLVByteArrayInvoker(4867);
    public TLVByteArrayInvoker mac = new TLVByteArrayInvoker(4865);
    public TLVByteArrayInvoker sessionKey = new TLVByteArrayInvoker(4866);

    public RET_AUTH_SYNC_ACK_RECV() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.mac);
        this.mNodeList.add(this.sessionKey);
        this.mNodeList.add(this.iv);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 19;
    }
}
