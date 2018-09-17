package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class RET_INIT_REQ extends TLVRootTree {
    public static final int ID = -2147483627;
    public static final short TAG_LOGIN_STATE = (short) 7681;
    public static final short TAG_RET_INIT_RESULT = (short) 30;
    public static final short TAG_USER_ID = (short) 7680;
    public TLVByteArrayInvoker loginState;
    public TLVLongInvoker userID;

    public RET_INIT_REQ() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.loginState = new TLVByteArrayInvoker(TAG_LOGIN_STATE);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.loginState);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return (short) 30;
    }
}
