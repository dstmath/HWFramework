package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class AuthData extends TLVTree.TLVChildTree {
    public static final short TAG_AUTH_DATA = 3841;
    public TLVTreeInvoker.TLVChildTreeInvoker authSyncData = new TLVTreeInvoker.TLVChildTreeInvoker(3843);
    public TLVTreeInvoker.TLVChildTreeInvoker cert = new TLVTreeInvoker.TLVChildTreeInvoker(3842);

    public AuthData() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.cert);
        this.mNodeList.add(this.authSyncData);
    }

    public short getTreeTag() {
        return TAG_AUTH_DATA;
    }
}
