package com.android.server.security.trustcircle.tlv.command.ka;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class RET_KA extends TLVTree.TLVRootTree {
    public static final int ID = -2147483615;
    public static final short TAG_IV = 8449;
    public static final short TAG_PAYLOAD = 8448;
    public static final short TAG_RET_KA = 33;
    public TLVByteArrayInvoker iv = new TLVByteArrayInvoker(TAG_IV);
    public TLVByteArrayInvoker payload = new TLVByteArrayInvoker(TAG_PAYLOAD);

    public RET_KA() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.payload);
        this.mNodeList.add(this.iv);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 33;
    }
}
