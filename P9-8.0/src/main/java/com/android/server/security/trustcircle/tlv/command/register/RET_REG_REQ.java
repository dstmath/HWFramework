package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.emcom.daemon.CommandsInterface;
import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVShortInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class RET_REG_REQ extends TLVRootTree {
    public static final int ID = -2147483646;
    public static final short TAG_AUTH_KEY_ALGO_ENCODE = (short) 513;
    public static final short TAG_CLIENT_CHALLENGE = (short) 516;
    public static final short TAG_GLOBALKEY_ID = (short) 512;
    public static final short TAG_KEY_DATA_SIGNATURE = (short) 515;
    public static final short TAG_REG_AUTH_KEY_DATA = (short) 514;
    public static final short TAG_RET_REG_REQ = (short) 2;
    public TLVShortInvoker authKeyAlgoEncode;
    public TLVByteArrayInvoker clientChallenge;
    public TLVIntegerInvoker glogbalKeyID;
    public TLVChildTreeInvoker regAuthKeyData;
    public TLVByteArrayInvoker regAuthKeyDataSign;

    public RET_REG_REQ() {
        this.glogbalKeyID = new TLVIntegerInvoker(TAG_GLOBALKEY_ID);
        this.authKeyAlgoEncode = new TLVShortInvoker(TAG_AUTH_KEY_ALGO_ENCODE);
        this.regAuthKeyData = new TLVChildTreeInvoker((int) CommandsInterface.EMCOM_DS_HTTP_INFO);
        this.regAuthKeyDataSign = new TLVByteArrayInvoker(TAG_KEY_DATA_SIGNATURE);
        this.clientChallenge = new TLVByteArrayInvoker(TAG_CLIENT_CHALLENGE);
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
        return (short) 2;
    }
}
