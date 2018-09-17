package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class UpdateIndexInfo extends TLVChildTree {
    public static final short TAG_CLIENT_CHALLENGE = (short) 778;
    public static final short TAG_INDEX_VERSION = (short) 777;
    public static final short TAG_UPDATE_INDEX_INFO = (short) 770;
    public TLVByteArrayInvoker clientChallenge;
    public TLVIntegerInvoker indexVersion;
    public TLVChildTreeInvoker updateIndex;

    public UpdateIndexInfo() {
        this.updateIndex = new TLVChildTreeInvoker(776);
        this.indexVersion = new TLVIntegerInvoker(TAG_INDEX_VERSION);
        this.clientChallenge = new TLVByteArrayInvoker(TAG_CLIENT_CHALLENGE);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.updateIndex);
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.clientChallenge);
    }

    public short getTreeTag() {
        return TAG_UPDATE_INDEX_INFO;
    }
}
