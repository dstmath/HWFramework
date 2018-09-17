package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVShortInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsALModeID;

public class Cert extends TLVChildTree {
    public static final short TAG_AUTH_KEY_ALGO_ENCODE = (short) 3844;
    public static final short TAG_AUTH_PK_INFO = (short) 768;
    public static final short TAG_AUTH_PK_INFO_SIGNATURE = (short) 769;
    public static final short TAG_CERT = (short) 3842;
    public TLVShortInvoker authKeyAlgoEncode;
    public TLVByteArrayInvoker authPKInfoSign;
    public TLVChildTreeInvoker authPkInfo;

    public Cert() {
        this.authKeyAlgoEncode = new TLVShortInvoker(TAG_AUTH_KEY_ALGO_ENCODE);
        this.authPkInfo = new TLVChildTreeInvoker((int) HighBitsALModeID.MODE_SRE_DISABLE);
        this.authPKInfoSign = new TLVByteArrayInvoker((short) 769);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authKeyAlgoEncode);
        this.mNodeList.add(this.authPkInfo);
        this.mNodeList.add(this.authPKInfoSign);
    }

    public short getTreeTag() {
        return TAG_CERT;
    }
}
