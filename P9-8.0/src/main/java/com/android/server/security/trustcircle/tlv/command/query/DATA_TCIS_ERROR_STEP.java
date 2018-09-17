package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVShortInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import com.android.server.security.trustcircle.tlv.tree.StatesInfo;
import java.util.Vector;

public class DATA_TCIS_ERROR_STEP extends TLVRootTree {
    public static final int ID = 1073741825;
    public static final short TAG_DATA_TCIS_ERROR_STEP = (short) 4080;
    public static final short TAG_INFO = (short) 255;
    public static final short TAG_NUMBER = (short) 4095;
    public TLVChildTreeInvoker info;
    public TLVShortInvoker numbers;

    public DATA_TCIS_ERROR_STEP() {
        this.numbers = new TLVShortInvoker(TAG_NUMBER);
        this.info = new TLVChildTreeInvoker(255);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.numbers);
        this.mNodeList.add(this.info);
    }

    public DATA_TCIS_ERROR_STEP(short oriNumber, StatesInfo oriInfo) {
        this.numbers = new TLVShortInvoker(TAG_NUMBER);
        this.info = new TLVChildTreeInvoker(255);
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
