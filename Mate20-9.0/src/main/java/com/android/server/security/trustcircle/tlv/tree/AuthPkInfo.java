package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class AuthPkInfo extends TLVTree.TLVChildTree {
    public static final short TAG_AUTH_PK = 775;
    public static final short TAG_AUTH_PK_INFO = 768;
    public static final short TAG_INDEX_VERSION = 772;
    public static final short TAG_TCIS_ID = 774;
    public static final short TAG_USER_ID = 773;
    public TLVByteArrayInvoker authPK = new TLVByteArrayInvoker(TAG_AUTH_PK);
    public TLVNumberInvoker.TLVIntegerInvoker indexVersion = new TLVNumberInvoker.TLVIntegerInvoker(TAG_INDEX_VERSION);
    public TLVByteArrayInvoker tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public AuthPkInfo() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.authPK);
    }

    public short getTreeTag() {
        return 768;
    }
}
