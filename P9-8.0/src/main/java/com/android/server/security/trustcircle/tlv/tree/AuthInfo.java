package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import java.util.Vector;

public class AuthInfo extends TLVChildTree {
    public static final short TAG_AUTH_ID = (short) 3587;
    public static final short TAG_AUTH_INFO = (short) 3585;
    public static final short TAG_AUTH_TYPE = (short) 3586;
    public static final short TAG_EE_SESSION_KEY = (short) 3590;
    public static final short TAG_POLICY = (short) 3588;
    public static final short TAG_USER_ID = (short) 3589;
    public TLVLongInvoker authID;
    public TLVIntegerInvoker authType;
    public TLVByteArrayInvoker encryptedAESKey;
    public TLVIntegerInvoker policy;
    public TLVLongInvoker userID;

    public AuthInfo() {
        this.authType = new TLVIntegerInvoker(TAG_AUTH_TYPE);
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
        this.policy = new TLVIntegerInvoker(TAG_POLICY);
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.encryptedAESKey = new TLVByteArrayInvoker(TAG_EE_SESSION_KEY);
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
