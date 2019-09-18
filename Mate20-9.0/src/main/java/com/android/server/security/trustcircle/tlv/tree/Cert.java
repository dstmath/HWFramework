package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsALModeID;

public class Cert extends TLVTree.TLVChildTree {
    public static final short TAG_AUTH_KEY_ALGO_ENCODE = 3844;
    public static final short TAG_AUTH_PK_INFO = 768;
    public static final short TAG_AUTH_PK_INFO_SIGNATURE = 769;
    public static final short TAG_CERT = 3842;
    public TLVNumberInvoker.TLVShortInvoker authKeyAlgoEncode = new TLVNumberInvoker.TLVShortInvoker(TAG_AUTH_KEY_ALGO_ENCODE);
    public TLVByteArrayInvoker authPKInfoSign = new TLVByteArrayInvoker(769);
    public TLVTreeInvoker.TLVChildTreeInvoker authPkInfo = new TLVTreeInvoker.TLVChildTreeInvoker((int) HighBitsALModeID.MODE_SRE_DISABLE);

    public Cert() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authKeyAlgoEncode);
        this.mNodeList.add(this.authPkInfo);
        this.mNodeList.add(this.authPKInfoSign);
    }

    public short getTreeTag() {
        return TAG_CERT;
    }
}
