package com.android.server.security.trustcircle.tlv.tree;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Vector;

public class KaInfo extends TLVTree.TLVChildTree {
    public static final short TAG_AAD = 8198;
    public static final short TAG_CT = 8196;
    public static final short TAG_KA_INFO = 8195;
    public static final short TAG_NONCE = 8199;
    public static final short TAG_TAG = 8197;
    public static final short TAG_TMP_PK = 8200;
    public TLVByteArrayInvoker aad = new TLVByteArrayInvoker(TAG_AAD);
    public TLVByteArrayInvoker ct = new TLVByteArrayInvoker(TAG_CT);
    public TLVByteArrayInvoker nonce = new TLVByteArrayInvoker(TAG_NONCE);
    public TLVByteArrayInvoker tag = new TLVByteArrayInvoker(TAG_TAG);
    public TLVByteArrayInvoker tmpPk = new TLVByteArrayInvoker(TAG_TMP_PK);

    public KaInfo() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.ct);
        this.mNodeList.add(this.tag);
        this.mNodeList.add(this.aad);
        this.mNodeList.add(this.nonce);
        this.mNodeList.add(this.tmpPk);
    }

    public short getTreeTag() {
        return TAG_KA_INFO;
    }
}
