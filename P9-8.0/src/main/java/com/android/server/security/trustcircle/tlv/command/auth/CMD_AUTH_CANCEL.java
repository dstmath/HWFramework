package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_AUTH_CANCEL extends TLVRootTree {
    public static final int ID = 18;
    public static final short TAG_AUTH_ID = (short) 6912;
    public static final short TAG_CMD_AUTH_CANCEL = (short) 27;
    public TLVLongInvoker authID;

    public CMD_AUTH_CANCEL() {
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
    }

    public int getCmdID() {
        return 18;
    }

    public short getTreeTag() {
        return (short) 27;
    }
}
