package com.android.server.security.trustcircle.tlv.command.ka;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class CMD_KA extends TLVTree.TLVRootTree {
    public static final int ID = -2147483616;
    public static final int TAG_CMD_KA = 32;
    public static final short TAG_EE_AES_TMP_KEY = 8194;
    public static final short TAG_KA_VERSION = 8192;
    public static final short TAG_USER_ID = 8193;
    public TLVByteArrayInvoker eeAesTmpKey = new TLVByteArrayInvoker(TAG_EE_AES_TMP_KEY);
    public TLVTreeInvoker.TLVChildTreeInvoker kaInfo = new TLVTreeInvoker.TLVChildTreeInvoker(8195);
    public TLVNumberInvoker.TLVIntegerInvoker kaVersion = new TLVNumberInvoker.TLVIntegerInvoker(TAG_KA_VERSION);
    public TLVNumberInvoker.TLVLongInvoker userId = new TLVNumberInvoker.TLVLongInvoker(TAG_USER_ID);

    public CMD_KA() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.kaVersion);
        this.mNodeList.add(this.userId);
        this.mNodeList.add(this.eeAesTmpKey);
        this.mNodeList.add(this.kaInfo);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return 32;
    }
}
