package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class CMD_AUTH_SYNC_ACK_RECV extends TLVRootTree {
    public static final int ID = 8;
    public static final short TAG_AUTH_ID = (short) 4352;
    public static final short TAG_CMD_AUTH_SYNC_ACK_RECV = (short) 18;
    public static final short TAG_MAC = (short) 4353;
    public TLVChildTreeInvoker authData;
    public TLVLongInvoker authID;
    public TLVByteArrayInvoker mac;

    public CMD_AUTH_SYNC_ACK_RECV() {
        this.authID = new TLVLongInvoker((short) 4352);
        this.mac = new TLVByteArrayInvoker((short) 4353);
        this.authData = new TLVChildTreeInvoker(3841);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.mac);
        this.mNodeList.add(this.authData);
    }

    public int getCmdID() {
        return 8;
    }

    public short getTreeTag() {
        return (short) 18;
    }
}
