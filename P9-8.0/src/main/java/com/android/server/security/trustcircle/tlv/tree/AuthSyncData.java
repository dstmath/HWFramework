package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import java.util.Vector;

public class AuthSyncData extends TLVChildTree {
    public static final short TAG_AUTH_SYNC_DATA = (short) 3843;
    public static final short TAG_INDEX_VERSION = (short) 3846;
    public static final short TAG_NONCE = (short) 3847;
    public static final short TAG_TCIS_ID = (short) 3845;
    public TLVIntegerInvoker indexVersion;
    public TLVByteArrayInvoker nonce;
    public TLVByteArrayInvoker tcisID;

    public AuthSyncData() {
        this.tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
        this.indexVersion = new TLVIntegerInvoker(TAG_INDEX_VERSION);
        this.nonce = new TLVByteArrayInvoker(TAG_NONCE);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.nonce);
    }

    public short getTreeTag() {
        return TAG_AUTH_SYNC_DATA;
    }
}
