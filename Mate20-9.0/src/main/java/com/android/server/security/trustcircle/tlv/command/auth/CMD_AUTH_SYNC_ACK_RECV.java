package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class CMD_AUTH_SYNC_ACK_RECV extends TLVTree.TLVRootTree {
    public static final int ID = 8;
    public static final short TAG_AUTH_ID = 4352;
    public static final short TAG_CMD_AUTH_SYNC_ACK_RECV = 18;
    public static final short TAG_MAC = 4353;
    public TLVTreeInvoker.TLVChildTreeInvoker authData = new TLVTreeInvoker.TLVChildTreeInvoker(3841);
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(4352);
    public TLVByteArrayInvoker mac = new TLVByteArrayInvoker(4353);

    public CMD_AUTH_SYNC_ACK_RECV() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.mac);
        this.mNodeList.add(this.authData);
    }

    public int getCmdID() {
        return 8;
    }

    public short getTreeTag() {
        return 18;
    }
}
