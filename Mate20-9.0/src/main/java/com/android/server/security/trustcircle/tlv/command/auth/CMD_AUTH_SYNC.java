package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class CMD_AUTH_SYNC extends TLVTree.TLVRootTree {
    public static final int ID = 6;
    public static final short TAG_AUTH_VERSION = 3584;
    public static final short TAG_CMD_AUTH_SYNC = 14;
    public TLVTreeInvoker.TLVChildTreeInvoker authInfo = new TLVTreeInvoker.TLVChildTreeInvoker(3585);
    public TLVNumberInvoker.TLVIntegerInvoker authVersion = new TLVNumberInvoker.TLVIntegerInvoker(3584);

    public CMD_AUTH_SYNC() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authVersion);
        this.mNodeList.add(this.authInfo);
    }

    public int getCmdID() {
        return 6;
    }

    public short getTreeTag() {
        return 14;
    }
}
