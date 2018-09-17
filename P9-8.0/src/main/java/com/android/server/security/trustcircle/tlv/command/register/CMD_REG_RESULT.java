package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsALModeID;

public class CMD_REG_RESULT extends TLVRootTree {
    public static final int ID = 3;
    public static final short TAG_AUTH_PK_INFO_SIGNATURE = (short) 769;
    public static final short TAG_CMD_REG_RESULT = (short) 3;
    public static final short TAG_UPDATE_INDEX_INFO_SIGNATURE = (short) 771;
    public TLVByteArrayInvoker authPKInfoSign;
    public TLVChildTreeInvoker authPkInfo;
    public TLVChildTreeInvoker updateIndexInfo;
    public TLVByteArrayInvoker updateIndexInfoSign;

    public CMD_REG_RESULT() {
        this.authPkInfo = new TLVChildTreeInvoker((int) HighBitsALModeID.MODE_SRE_DISABLE);
        this.authPKInfoSign = new TLVByteArrayInvoker((short) 769);
        this.updateIndexInfo = new TLVChildTreeInvoker(770);
        this.updateIndexInfoSign = new TLVByteArrayInvoker((short) 771);
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
        return (short) 3;
    }
}
