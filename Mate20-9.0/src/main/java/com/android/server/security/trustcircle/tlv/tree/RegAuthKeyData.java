package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class RegAuthKeyData extends TLVTree.TLVChildTree {
    public static final short TAG_AUTH_PK = 519;
    public static final short TAG_REG_AUTH_KEY_DATA = 514;
    public static final short TAG_SESSION_ID = 520;
    public static final short TAG_TCIS_ID = 518;
    public static final short TAG_USER_ID = 517;
    public TLVByteArrayInvoker authPK = new TLVByteArrayInvoker(TAG_AUTH_PK);
    public TLVByteArrayInvoker sessionID = new TLVByteArrayInvoker(TAG_SESSION_ID);
    public TLVByteArrayInvoker tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public RegAuthKeyData() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.authPK);
        this.mNodeList.add(this.sessionID);
    }

    public short getTreeTag() {
        return 514;
    }
}
