package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class CMD_AUTH_SYNC_RECV extends TLVTree.TLVRootTree {
    public static final int ID = 7;
    public static final short TAG_AUTH_VERSION = 3584;
    public static final short TAG_CMD_AUTH_SYNC_RECV = 16;
    public static final short TAG_TA_VERSION = 1793;
    public TLVNumberInvoker.TLVShortInvoker TAVersion = new TLVNumberInvoker.TLVShortInvoker(1793);
    public TLVTreeInvoker.TLVChildTreeInvoker authData = new TLVTreeInvoker.TLVChildTreeInvoker(3841);
    public TLVTreeInvoker.TLVChildTreeInvoker authInfo = new TLVTreeInvoker.TLVChildTreeInvoker(3585);
    public TLVNumberInvoker.TLVIntegerInvoker authVersion = new TLVNumberInvoker.TLVIntegerInvoker(3584);

    public CMD_AUTH_SYNC_RECV() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authVersion);
        this.mNodeList.add(this.authInfo);
        this.mNodeList.add(this.authData);
        this.mNodeList.add(this.TAVersion);
    }

    public int getCmdID() {
        return 7;
    }

    public short getTreeTag() {
        return 16;
    }
}
