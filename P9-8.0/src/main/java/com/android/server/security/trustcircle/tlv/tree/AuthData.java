package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class AuthData extends TLVChildTree {
    public static final short TAG_AUTH_DATA = (short) 3841;
    public TLVChildTreeInvoker authSyncData;
    public TLVChildTreeInvoker cert;

    public AuthData() {
        this.cert = new TLVChildTreeInvoker(3842);
        this.authSyncData = new TLVChildTreeInvoker(3843);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.cert);
        this.mNodeList.add(this.authSyncData);
    }

    public short getTreeTag() {
        return TAG_AUTH_DATA;
    }
}
