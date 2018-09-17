package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import java.util.Vector;

public class StatesInfo extends TLVChildTree {
    public static final short TAG_PAIRS = (short) 30583;
    public static final short TAG_STATES_INFO = (short) 255;
    public TLVByteArrayInvoker infos;

    public StatesInfo() {
        this.infos = new TLVByteArrayInvoker(TAG_PAIRS);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.infos);
    }

    public StatesInfo(Byte[] info) {
        this.infos = new TLVByteArrayInvoker(TAG_PAIRS);
        this.infos.setTLVStruct(info);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.infos);
    }

    public short getTreeTag() {
        return (short) 255;
    }
}
