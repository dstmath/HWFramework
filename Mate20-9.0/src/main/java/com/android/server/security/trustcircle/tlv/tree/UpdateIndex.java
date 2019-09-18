package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class UpdateIndex extends TLVTree.TLVChildTree {
    public static final short TAG_IDS = 780;
    public static final short TAG_UPDATE_INDEX = 776;
    public static final short TAG_USER_ID = 779;
    public TLVByteArrayInvoker ids = new TLVByteArrayInvoker(TAG_IDS);
    public TLVNumberInvoker.TLVLongInvoker userID = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public UpdateIndex() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.ids);
    }

    public short getTreeTag() {
        return TAG_UPDATE_INDEX;
    }
}
