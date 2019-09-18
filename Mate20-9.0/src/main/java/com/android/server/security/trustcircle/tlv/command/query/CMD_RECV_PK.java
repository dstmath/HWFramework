package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class CMD_RECV_PK extends TLVTree.TLVRootTree {
    public static final int ID = 12;
    public static final short TAG_AUTH_ID = 6656;
    public static final short TAG_CMD_RECV_PK = 26;
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(6656);
    public TLVTreeInvoker.TLVChildTreeInvoker otherCert = new TLVTreeInvoker.TLVChildTreeInvoker(3842);

    public CMD_RECV_PK() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.otherCert);
    }

    public int getCmdID() {
        return 12;
    }

    public short getTreeTag() {
        return 26;
    }
}
