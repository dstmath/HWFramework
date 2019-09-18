package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class AuthInfo extends TLVTree.TLVChildTree {
    public static final short TAG_AUTH_ID = 3587;
    public static final short TAG_AUTH_INFO = 3585;
    public static final short TAG_AUTH_TYPE = 3586;
    public static final short TAG_EE_SESSION_KEY = 3590;
    public static final short TAG_POLICY = 3588;
    public static final short TAG_USER_ID = 3589;
    public TLVNumberInvoker.TLVLongInvoker authID = new TLVNumberInvoker.TLVLongInvoker(TAG_AUTH_ID);
    public TLVNumberInvoker.TLVIntegerInvoker authType = new TLVNumberInvoker.TLVIntegerInvoker(TAG_AUTH_TYPE);
    public TLVByteArrayInvoker encryptedAESKey = new TLVByteArrayInvoker(TAG_EE_SESSION_KEY);
    public TLVNumberInvoker.TLVIntegerInvoker policy = new TLVNumberInvoker.TLVIntegerInvoker(TAG_POLICY);
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public AuthInfo() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authType);
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.policy);
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.encryptedAESKey);
    }

    public short getTreeTag() {
        return TAG_AUTH_INFO;
    }
}
