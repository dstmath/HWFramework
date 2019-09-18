package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_QUERY_PK extends TLVTree.TLVRootTree {
    public static final int ID = 10;
    public static final short TAG_AUTH_ID = 5635;
    public static final short TAG_CMD_QUERY_PK = 22;
    public static final short TAG_INDEX_VERSION = 5634;
    public static final short TAG_TCIS_ID = 5633;
    public static final short TAG_USER_ID = 5632;
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(TAG_AUTH_ID);
    public TLVNumberInvoker.TLVIntegerInvoker indexVersion = new TLVNumberInvoker.TLVIntegerInvoker(TAG_INDEX_VERSION);
    public TLVByteArrayInvoker tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_QUERY_PK() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.authID);
    }

    public int getCmdID() {
        return 10;
    }

    public short getTreeTag() {
        return 22;
    }
}
