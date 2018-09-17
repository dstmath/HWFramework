package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class RET_GET_PK extends TLVRootTree {
    public static final int ID = -2147483637;
    public static final short TAG_RET_GET_PK = (short) 25;
    public TLVChildTreeInvoker cert;

    public RET_GET_PK() {
        this.cert = new TLVChildTreeInvoker(3842);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.cert);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return (short) 25;
    }
}
