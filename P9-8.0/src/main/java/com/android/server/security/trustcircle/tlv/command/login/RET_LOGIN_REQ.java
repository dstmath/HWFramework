package com.android.server.security.trustcircle.tlv.command.login;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class RET_LOGIN_REQ extends TLVRootTree {
    public static final int ID = -2147483644;
    public static final short TAG_CLIENT_CHALLENGE = (short) 1281;
    public static final short TAG_INDEX_VERSION = (short) 1280;
    public static final short TAG_RET_LOGIN_REQ = (short) 5;
    public TLVByteArrayInvoker clientChallenge;
    public TLVIntegerInvoker indexVersion;

    public RET_LOGIN_REQ() {
        this.indexVersion = new TLVIntegerInvoker(TAG_INDEX_VERSION);
        this.clientChallenge = new TLVByteArrayInvoker(TAG_CLIENT_CHALLENGE);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.clientChallenge);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return (short) 5;
    }
}
