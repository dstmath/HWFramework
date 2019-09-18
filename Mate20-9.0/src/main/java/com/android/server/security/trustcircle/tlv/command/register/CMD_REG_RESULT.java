package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import java.util.Vector;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsALModeID;

public class CMD_REG_RESULT extends TLVTree.TLVRootTree {
    public static final int ID = 3;
    public static final short TAG_AUTH_PK_INFO_SIGNATURE = 769;
    public static final short TAG_CMD_REG_RESULT = 3;
    public static final short TAG_UPDATE_INDEX_INFO_SIGNATURE = 771;
    public TLVByteArrayInvoker authPKInfoSign = new TLVByteArrayInvoker(769);
    public TLVTreeInvoker.TLVChildTreeInvoker authPkInfo = new TLVTreeInvoker.TLVChildTreeInvoker((int) HighBitsALModeID.MODE_SRE_DISABLE);
    public TLVTreeInvoker.TLVChildTreeInvoker updateIndexInfo = new TLVTreeInvoker.TLVChildTreeInvoker(770);
    public TLVByteArrayInvoker updateIndexInfoSign = new TLVByteArrayInvoker(771);

    public CMD_REG_RESULT() {
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authPkInfo);
        this.mNodeList.add(this.authPKInfoSign);
        this.mNodeList.add(this.updateIndexInfo);
        this.mNodeList.add(this.updateIndexInfoSign);
    }

    public int getCmdID() {
        return 3;
    }

    public short getTreeTag() {
        return 3;
    }
}
