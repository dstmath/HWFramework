package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import java.util.Vector;

public class UpdateIndex extends TLVChildTree {
    public static final short TAG_IDS = (short) 780;
    public static final short TAG_UPDATE_INDEX = (short) 776;
    public static final short TAG_USER_ID = (short) 779;
    public TLVByteArrayInvoker ids;
    public TLVLongInvoker userID;

    public UpdateIndex() {
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.ids = new TLVByteArrayInvoker(TAG_IDS);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.ids);
    }

    public short getTreeTag() {
        return TAG_UPDATE_INDEX;
    }
}
