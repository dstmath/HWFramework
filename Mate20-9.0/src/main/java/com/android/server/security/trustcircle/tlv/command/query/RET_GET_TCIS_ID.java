package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class RET_GET_TCIS_ID extends TLVTree.TLVRootTree {
    public static final int ID = -2147483635;
    public static final short TAG_RET_GET_TCIS_ID = 7;
    public static final short TAG_TA_VERSION = 1793;
    public static final short TAG_TCIS_ID = 1792;
    public TLVNumberInvoker.TLVShortInvoker TAVersion = new TLVNumberInvoker.TLVShortInvoker(1793);
    public TLVByteArrayInvoker tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);

    public RET_GET_TCIS_ID() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.TAVersion);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 7;
    }
}
