package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class RET_AUTH_MASTER_RECV_KEY extends TLVRootTree {
    public static final int ID = -2147483629;
    public static final short TAG_AUTH_ID = (short) 4864;
    public static final short TAG_IV = (short) 4867;
    public static final short TAG_MAC = (short) 4865;
    public static final short TAG_RET_AUTH_MASTER_RECV_KEY = (short) 19;
    public static final short TAG_SESSION_KEY = (short) 4866;
    public TLVLongInvoker authID;
    public TLVByteArrayInvoker iv;
    public TLVByteArrayInvoker mac;
    public TLVByteArrayInvoker sessionKey;

    public RET_AUTH_MASTER_RECV_KEY() {
        this.authID = new TLVLongInvoker((short) 4864);
        this.mac = new TLVByteArrayInvoker((short) 4865);
        this.sessionKey = new TLVByteArrayInvoker((short) 4866);
        this.iv = new TLVByteArrayInvoker((short) 4867);
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
        return (short) 19;
    }
}
