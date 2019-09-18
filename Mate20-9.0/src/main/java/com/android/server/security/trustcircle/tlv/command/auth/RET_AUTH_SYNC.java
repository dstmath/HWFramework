package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class RET_AUTH_SYNC extends TLVTree.TLVRootTree {
    public static final int ID = -2147483642;
    public static final short TAG_AUTH_ID = 3840;
    public static final short TAG_RET_AUTH_SYNC = 15;
    public static final short TAG_TA_VERSION = 1793;
    public TLVNumberInvoker.TLVShortInvoker TAVersion = new TLVNumberInvoker.TLVShortInvoker(1793);
    public TLVTreeInvoker.TLVChildTreeInvoker authData = new TLVTreeInvoker.TLVChildTreeInvoker(3841);
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(TAG_AUTH_ID);

    public RET_AUTH_SYNC() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.authData);
        this.mNodeList.add(this.TAVersion);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 15;
    }
}
