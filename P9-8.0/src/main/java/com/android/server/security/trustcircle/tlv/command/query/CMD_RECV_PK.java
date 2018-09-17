package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class CMD_RECV_PK extends TLVRootTree {
    public static final int ID = 12;
    public static final short TAG_AUTH_ID = (short) 6656;
    public static final short TAG_CMD_RECV_PK = (short) 26;
    public TLVLongInvoker authID;
    public TLVChildTreeInvoker otherCert;

    public CMD_RECV_PK() {
        this.authID = new TLVLongInvoker((short) 6656);
        this.otherCert = new TLVChildTreeInvoker(3842);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.otherCert);
    }

    public int getCmdID() {
        return 12;
    }

    public short getTreeTag() {
        return (short) 26;
    }
}
