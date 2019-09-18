package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class UpdateIndexInfo extends TLVTree.TLVChildTree {
    public static final short TAG_CLIENT_CHALLENGE = 778;
    public static final short TAG_INDEX_VERSION = 777;
    public static final short TAG_UPDATE_INDEX_INFO = 770;
    public TLVByteArrayInvoker clientChallenge = new TLVByteArrayInvoker(TAG_CLIENT_CHALLENGE);
    public TLVNumberInvoker.TLVIntegerInvoker indexVersion = new TLVNumberInvoker.TLVIntegerInvoker(TAG_INDEX_VERSION);
    public TLVTreeInvoker.TLVChildTreeInvoker updateIndex = new TLVTreeInvoker.TLVChildTreeInvoker(776);

    public UpdateIndexInfo() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.updateIndex);
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.clientChallenge);
    }

    public short getTreeTag() {
        return TAG_UPDATE_INDEX_INFO;
    }
}
