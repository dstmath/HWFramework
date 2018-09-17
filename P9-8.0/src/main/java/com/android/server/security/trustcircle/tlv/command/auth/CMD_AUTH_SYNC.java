package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class CMD_AUTH_SYNC extends TLVRootTree {
    public static final int ID = 6;
    public static final short TAG_AUTH_VERSION = (short) 3584;
    public static final short TAG_CMD_AUTH_SYNC = (short) 14;
    public TLVChildTreeInvoker authInfo;
    public TLVIntegerInvoker authVersion;

    public CMD_AUTH_SYNC() {
        this.authVersion = new TLVIntegerInvoker((short) 3584);
        this.authInfo = new TLVChildTreeInvoker(3585);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authVersion);
        this.mNodeList.add(this.authInfo);
    }

    public int getCmdID() {
        return 6;
    }

    public short getTreeTag() {
        return (short) 14;
    }
}
