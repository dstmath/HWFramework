package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class StatesInfo extends TLVTree.TLVChildTree {
    public static final short TAG_PAIRS = 30583;
    public static final short TAG_STATES_INFO = 255;
    public TLVByteArrayInvoker infos = new TLVByteArrayInvoker(TAG_PAIRS);

    public StatesInfo() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.infos);
    }

    public StatesInfo(Byte[] info) {
        this.infos.setTLVStruct(info);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.infos);
    }

    public short getTreeTag() {
        return 255;
    }
}
