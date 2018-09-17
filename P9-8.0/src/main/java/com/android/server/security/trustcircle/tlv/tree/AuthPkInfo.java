package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import java.util.Vector;

public class AuthPkInfo extends TLVChildTree {
    public static final short TAG_AUTH_PK = (short) 775;
    public static final short TAG_AUTH_PK_INFO = (short) 768;
    public static final short TAG_INDEX_VERSION = (short) 772;
    public static final short TAG_TCIS_ID = (short) 774;
    public static final short TAG_USER_ID = (short) 773;
    public TLVByteArrayInvoker authPK;
    public TLVIntegerInvoker indexVersion;
    public TLVByteArrayInvoker tcisID;
    public TLVLongInvoker userID;

    public AuthPkInfo() {
        this.indexVersion = new TLVIntegerInvoker(TAG_INDEX_VERSION);
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
        this.authPK = new TLVByteArrayInvoker(TAG_AUTH_PK);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.authPK);
    }

    public short getTreeTag() {
        return (short) 768;
    }
}
