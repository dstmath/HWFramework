package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class CMD_AUTH_CANCEL extends TLVTree.TLVRootTree {
    public static final int ID = 18;
    public static final short TAG_AUTH_ID = 6912;
    public static final short TAG_CMD_AUTH_CANCEL = 27;
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(TAG_AUTH_ID);

    public CMD_AUTH_CANCEL() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
    }

    public int getCmdID() {
        return 18;
    }

    public short getTreeTag() {
        return 27;
    }
}
