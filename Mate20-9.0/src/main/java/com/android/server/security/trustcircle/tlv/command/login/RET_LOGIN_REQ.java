package com.android.server.security.trustcircle.tlv.command.login;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class RET_LOGIN_REQ extends TLVTree.TLVRootTree {
    public static final int ID = -2147483644;
    public static final short TAG_CLIENT_CHALLENGE = 1281;
    public static final short TAG_INDEX_VERSION = 1280;
    public static final short TAG_RET_LOGIN_REQ = 5;
    public TLVByteArrayInvoker clientChallenge = new TLVByteArrayInvoker(TAG_CLIENT_CHALLENGE);
    public TLVNumberInvoker.TLVIntegerInvoker indexVersion = new TLVNumberInvoker.TLVIntegerInvoker(TAG_INDEX_VERSION);

    public RET_LOGIN_REQ() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.clientChallenge);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 5;
    }
}
