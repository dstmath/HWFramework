package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class RET_GET_PK extends TLVTree.TLVRootTree {
    public static final int ID = -2147483637;
    public static final short TAG_RET_GET_PK = 25;
    public TLVTreeInvoker.TLVChildTreeInvoker cert = new TLVTreeInvoker.TLVChildTreeInvoker(3842);

    public RET_GET_PK() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.cert);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 25;
    }
}
