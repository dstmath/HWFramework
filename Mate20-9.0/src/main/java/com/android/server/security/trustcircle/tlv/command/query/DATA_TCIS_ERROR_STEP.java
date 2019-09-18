package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import com.android.server.security.trustcircle.tlv.tree.StatesInfo;
import java.util.Vector;

public class DATA_TCIS_ERROR_STEP extends TLVTree.TLVRootTree {
    public static final int ID = 1073741825;
    public static final short TAG_DATA_TCIS_ERROR_STEP = 4080;
    public static final short TAG_INFO = 255;
    public static final short TAG_NUMBER = 4095;
    public TLVTreeInvoker.TLVChildTreeInvoker info = new TLVTreeInvoker.TLVChildTreeInvoker(255);
    public TLVNumberInvoker.TLVShortInvoker numbers = new TLVNumberInvoker.TLVShortInvoker(TAG_NUMBER);

    public DATA_TCIS_ERROR_STEP() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.numbers);
        this.mNodeList.add(this.info);
    }

    public DATA_TCIS_ERROR_STEP(short oriNumber, StatesInfo oriInfo) {
        this.numbers.setTLVStruct(Short.valueOf(oriNumber));
        this.info.setTLVStruct(oriInfo);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.numbers);
        this.mNodeList.add(this.info);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return TAG_DATA_TCIS_ERROR_STEP;
    }
}
