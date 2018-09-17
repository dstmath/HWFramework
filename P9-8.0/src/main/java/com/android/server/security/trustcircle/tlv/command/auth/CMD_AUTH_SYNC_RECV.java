package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVShortInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class CMD_AUTH_SYNC_RECV extends TLVRootTree {
    public static final int ID = 7;
    public static final short TAG_AUTH_VERSION = (short) 3584;
    public static final short TAG_CMD_AUTH_SYNC_RECV = (short) 16;
    public static final short TAG_TA_VERSION = (short) 1793;
    public TLVShortInvoker TAVersion;
    public TLVChildTreeInvoker authData;
    public TLVChildTreeInvoker authInfo;
    public TLVIntegerInvoker authVersion;

    public CMD_AUTH_SYNC_RECV() {
        this.authVersion = new TLVIntegerInvoker((short) 3584);
        this.authInfo = new TLVChildTreeInvoker(3585);
        this.authData = new TLVChildTreeInvoker(3841);
        this.TAVersion = new TLVShortInvoker((short) 1793);
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
        return (short) 16;
    }
}
