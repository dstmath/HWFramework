package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_QUERY_PK extends TLVRootTree {
    public static final int ID = 10;
    public static final short TAG_AUTH_ID = (short) 5635;
    public static final short TAG_CMD_QUERY_PK = (short) 22;
    public static final short TAG_INDEX_VERSION = (short) 5634;
    public static final short TAG_TCIS_ID = (short) 5633;
    public static final short TAG_USER_ID = (short) 5632;
    public TLVLongInvoker authID;
    public TLVIntegerInvoker indexVersion;
    public TLVByteArrayInvoker tcisID;
    public TLVLongInvoker userID;

    public CMD_QUERY_PK() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
        this.indexVersion = new TLVIntegerInvoker(TAG_INDEX_VERSION);
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
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
        return (short) 22;
    }
}
