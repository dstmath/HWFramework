package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVShortInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class RET_GET_TCIS_ID extends TLVRootTree {
    public static final int ID = -2147483635;
    public static final short TAG_RET_GET_TCIS_ID = (short) 7;
    public static final short TAG_TA_VERSION = (short) 1793;
    public static final short TAG_TCIS_ID = (short) 1792;
    public TLVShortInvoker TAVersion;
    public TLVByteArrayInvoker tcisID;

    public RET_GET_TCIS_ID() {
        this.tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
        this.TAVersion = new TLVShortInvoker((short) 1793);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.TAVersion);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return (short) 7;
    }
}
