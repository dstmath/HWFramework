package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class RET_INIT_REQ extends TLVTree.TLVRootTree {
    public static final int ID = -2147483627;
    public static final short TAG_LOGIN_STATE = 7681;
    public static final short TAG_RET_INIT_RESULT = 30;
    public static final short TAG_USER_ID = 7680;
    public TLVByteArrayInvoker loginState = new TLVByteArrayInvoker(TAG_LOGIN_STATE);
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public RET_INIT_REQ() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.loginState);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 30;
    }
}
