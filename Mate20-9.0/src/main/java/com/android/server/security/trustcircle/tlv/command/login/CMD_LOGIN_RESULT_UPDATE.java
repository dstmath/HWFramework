package com.android.server.security.trustcircle.tlv.command.login;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;

public class CMD_LOGIN_RESULT_UPDATE extends TLVTree.TLVRootTree {
    public static final int ID = 5;
    public static final short TAG_CMD_LOGIN_RESULT_UPDATE = 6;
    public static final short TAG_UPDATE_INDEX_INFO_SIGNATURE = 771;
    public TLVTreeInvoker.TLVChildTreeInvoker updateIndexInfo = new TLVTreeInvoker.TLVChildTreeInvoker(770);
    public TLVByteArrayInvoker updateIndexInfoSign = new TLVByteArrayInvoker(771);

    public CMD_LOGIN_RESULT_UPDATE() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.updateIndexInfo);
        this.mNodeList.add(this.updateIndexInfoSign);
    }

    public int getCmdID() {
        return 5;
    }

    public short getTreeTag() {
        return 6;
    }
}
