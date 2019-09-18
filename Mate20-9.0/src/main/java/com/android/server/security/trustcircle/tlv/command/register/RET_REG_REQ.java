package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class RET_REG_REQ extends TLVTree.TLVRootTree {
    public static final int ID = -2147483646;
    public static final short TAG_AUTH_KEY_ALGO_ENCODE = 513;
    public static final short TAG_CLIENT_CHALLENGE = 516;
    public static final short TAG_GLOBALKEY_ID = 512;
    public static final short TAG_KEY_DATA_SIGNATURE = 515;
    public static final short TAG_REG_AUTH_KEY_DATA = 514;
    public static final short TAG_RET_REG_REQ = 2;
    public TLVNumberInvoker.TLVShortInvoker authKeyAlgoEncode = new TLVNumberInvoker.TLVShortInvoker(TAG_AUTH_KEY_ALGO_ENCODE);
    public TLVByteArrayInvoker clientChallenge = new TLVByteArrayInvoker(TAG_CLIENT_CHALLENGE);
    public TLVNumberInvoker.TLVIntegerInvoker glogbalKeyID = new TLVNumberInvoker.TLVIntegerInvoker(TAG_GLOBALKEY_ID);
    public TLVTreeInvoker.TLVChildTreeInvoker regAuthKeyData = new TLVTreeInvoker.TLVChildTreeInvoker(514);
    public TLVByteArrayInvoker regAuthKeyDataSign = new TLVByteArrayInvoker(TAG_KEY_DATA_SIGNATURE);

    public RET_REG_REQ() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.glogbalKeyID);
        this.mNodeList.add(this.authKeyAlgoEncode);
        this.mNodeList.add(this.regAuthKeyData);
        this.mNodeList.add(this.regAuthKeyDataSign);
        this.mNodeList.add(this.clientChallenge);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 2;
    }
}
