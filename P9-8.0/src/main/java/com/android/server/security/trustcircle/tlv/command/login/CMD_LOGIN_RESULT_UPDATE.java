package com.android.server.security.trustcircle.tlv.command.login;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class CMD_LOGIN_RESULT_UPDATE extends TLVRootTree {
    public static final int ID = 5;
    public static final short TAG_CMD_LOGIN_RESULT_UPDATE = (short) 6;
    public static final short TAG_UPDATE_INDEX_INFO_SIGNATURE = (short) 771;
    public TLVChildTreeInvoker updateIndexInfo;
    public TLVByteArrayInvoker updateIndexInfoSign;

    public CMD_LOGIN_RESULT_UPDATE() {
        this.updateIndexInfo = new TLVChildTreeInvoker(770);
        this.updateIndexInfoSign = new TLVByteArrayInvoker((short) 771);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.updateIndexInfo);
        this.mNodeList.add(this.updateIndexInfoSign);
    }

    public int getCmdID() {
        return 5;
    }

    public short getTreeTag() {
        return (short) 6;
    }
}
