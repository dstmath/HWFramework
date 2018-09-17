package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import java.util.Vector;

public class RegAuthKeyData extends TLVChildTree {
    public static final short TAG_AUTH_PK = (short) 519;
    public static final short TAG_REG_AUTH_KEY_DATA = (short) 514;
    public static final short TAG_SESSION_ID = (short) 520;
    public static final short TAG_TCIS_ID = (short) 518;
    public static final short TAG_USER_ID = (short) 517;
    public TLVByteArrayInvoker authPK;
    public TLVByteArrayInvoker sessionID;
    public TLVByteArrayInvoker tcisID;
    public TLVLongInvoker userID;

    public RegAuthKeyData() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
        this.authPK = new TLVByteArrayInvoker(TAG_AUTH_PK);
        this.sessionID = new TLVByteArrayInvoker(TAG_SESSION_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.authPK);
        this.mNodeList.add(this.sessionID);
    }

    public short getTreeTag() {
        return (short) 514;
    }
}
