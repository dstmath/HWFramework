package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVShortInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class RET_AUTH_SYNC extends TLVRootTree {
    public static final int ID = -2147483642;
    public static final short TAG_AUTH_ID = (short) 3840;
    public static final short TAG_RET_AUTH_SYNC = (short) 15;
    public static final short TAG_TA_VERSION = (short) 1793;
    public TLVShortInvoker TAVersion;
    public TLVChildTreeInvoker authData;
    public TLVLongInvoker authID;

    public RET_AUTH_SYNC() {
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
        this.authData = new TLVChildTreeInvoker(3841);
        this.TAVersion = new TLVShortInvoker((short) 1793);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.authData);
        this.mNodeList.add(this.TAVersion);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return (short) 15;
    }
}
