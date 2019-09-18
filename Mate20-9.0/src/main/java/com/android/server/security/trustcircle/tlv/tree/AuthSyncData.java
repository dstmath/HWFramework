package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class AuthSyncData extends TLVTree.TLVChildTree {
    public static final short TAG_AUTH_SYNC_DATA = 3843;
    public static final short TAG_INDEX_VERSION = 3846;
    public static final short TAG_NONCE = 3847;
    public static final short TAG_TCIS_ID = 3845;
    public TLVNumberInvoker.TLVIntegerInvoker indexVersion = new TLVNumberInvoker.TLVIntegerInvoker(TAG_INDEX_VERSION);
    public TLVByteArrayInvoker nonce = new TLVByteArrayInvoker(TAG_NONCE);
    public TLVByteArrayInvoker tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);

    public AuthSyncData() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.nonce);
    }

    public short getTreeTag() {
        return TAG_AUTH_SYNC_DATA;
    }
}
