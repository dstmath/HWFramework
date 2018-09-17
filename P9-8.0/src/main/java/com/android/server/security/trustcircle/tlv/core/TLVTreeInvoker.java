package com.android.server.security.trustcircle.tlv.core;

import com.android.server.emcom.daemon.CommandsInterface;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_CANCEL;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_MASTER_RECV_KEY;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_SLAVE_RECV_KEY;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_SYNC;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_SYNC_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_SYNC_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_CANCEL;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_MASTER_RECV_KEY;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SLAVE_RECV_KEY;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC_RECV;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_CANCEL;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_REQ;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_RESULT_UPDATE;
import com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_CANCEL;
import com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_REQ;
import com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_RESULT_UPDATE;
import com.android.server.security.trustcircle.tlv.command.logout.CMD_LOGOUT_REQ;
import com.android.server.security.trustcircle.tlv.command.logout.RET_LOGOUT_REQ;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_LOGIN_STATUS;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_PK;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_TCIS_ID;
import com.android.server.security.trustcircle.tlv.command.query.CMD_INIT_REQ;
import com.android.server.security.trustcircle.tlv.command.query.CMD_QUERY_PK;
import com.android.server.security.trustcircle.tlv.command.query.CMD_RECV_PK;
import com.android.server.security.trustcircle.tlv.command.query.DATA_TCIS_ERROR_STEP;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_LOGIN_STATUS;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_PK;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_TCIS_ID;
import com.android.server.security.trustcircle.tlv.command.query.RET_INIT_REQ;
import com.android.server.security.trustcircle.tlv.command.query.RET_QUERY_PK;
import com.android.server.security.trustcircle.tlv.command.register.CMD_CHECK_REG_STATUS;
import com.android.server.security.trustcircle.tlv.command.register.CMD_REG_CANCEL;
import com.android.server.security.trustcircle.tlv.command.register.CMD_REG_REQ;
import com.android.server.security.trustcircle.tlv.command.register.CMD_REG_RESULT;
import com.android.server.security.trustcircle.tlv.command.register.CMD_UNREG_REQ;
import com.android.server.security.trustcircle.tlv.command.register.RET_CHECK_REG_STATUS;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_CANCEL;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_REQ;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_RESULT;
import com.android.server.security.trustcircle.tlv.command.register.RET_UNREG_REQ;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.tree.AuthData;
import com.android.server.security.trustcircle.tlv.tree.AuthInfo;
import com.android.server.security.trustcircle.tlv.tree.AuthPkInfo;
import com.android.server.security.trustcircle.tlv.tree.AuthSyncData;
import com.android.server.security.trustcircle.tlv.tree.Cert;
import com.android.server.security.trustcircle.tlv.tree.RegAuthKeyData;
import com.android.server.security.trustcircle.tlv.tree.StatesInfo;
import com.android.server.security.trustcircle.tlv.tree.UpdateIndex;
import com.android.server.security.trustcircle.tlv.tree.UpdateIndexInfo;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map.Entry;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsALModeID;

public class TLVTreeInvoker<T extends TLVTree> extends TLVInvoker<TLVTree> {
    static HashMap<Integer, TLVTree> TYPE_MAP = new HashMap();

    public static class TLVChildTreeInvoker extends TLVTreeInvoker<TLVChildTree> {
        public TLVChildTreeInvoker(int id) {
            super(id);
        }

        public TLVChildTreeInvoker(TLVChildTree t) {
            super((TLVTree) t);
        }
    }

    public static class TLVRootTreeInvoker extends TLVTreeInvoker<TLVRootTree> {
        public TLVRootTreeInvoker(int id) {
            super(id);
        }

        public TLVRootTreeInvoker(TLVRootTree t) {
            super((TLVTree) t);
        }
    }

    static {
        TYPE_MAP.put(Integer.valueOf(1), new CMD_CHECK_REG_STATUS());
        TYPE_MAP.put(Integer.valueOf(2), new CMD_REG_REQ());
        TYPE_MAP.put(Integer.valueOf(3), new CMD_REG_RESULT());
        TYPE_MAP.put(Integer.valueOf(4), new CMD_LOGIN_REQ());
        TYPE_MAP.put(Integer.valueOf(5), new CMD_LOGIN_RESULT_UPDATE());
        TYPE_MAP.put(Integer.valueOf(6), new CMD_AUTH_SYNC());
        TYPE_MAP.put(Integer.valueOf(7), new CMD_AUTH_SYNC_RECV());
        TYPE_MAP.put(Integer.valueOf(8), new CMD_AUTH_SYNC_ACK_RECV());
        TYPE_MAP.put(Integer.valueOf(9), new CMD_AUTH_ACK_RECV());
        TYPE_MAP.put(Integer.valueOf(10), new CMD_QUERY_PK());
        TYPE_MAP.put(Integer.valueOf(11), new CMD_GET_PK());
        TYPE_MAP.put(Integer.valueOf(12), new CMD_RECV_PK());
        TYPE_MAP.put(Integer.valueOf(13), new CMD_GET_TCIS_ID());
        TYPE_MAP.put(Integer.valueOf(14), new CMD_LOGOUT_REQ());
        TYPE_MAP.put(Integer.valueOf(15), new CMD_UNREG_REQ());
        TYPE_MAP.put(Integer.valueOf(16), new CMD_REG_CANCEL());
        TYPE_MAP.put(Integer.valueOf(17), new CMD_LOGIN_CANCEL());
        TYPE_MAP.put(Integer.valueOf(18), new CMD_AUTH_CANCEL());
        TYPE_MAP.put(Integer.valueOf(19), new CMD_AUTH_MASTER_RECV_KEY());
        TYPE_MAP.put(Integer.valueOf(12), new CMD_AUTH_SLAVE_RECV_KEY());
        TYPE_MAP.put(Integer.valueOf(20), new CMD_GET_LOGIN_STATUS());
        TYPE_MAP.put(Integer.valueOf(21), new CMD_INIT_REQ());
        TYPE_MAP.put(Integer.valueOf(RET_CHECK_REG_STATUS.ID), new RET_CHECK_REG_STATUS());
        TYPE_MAP.put(Integer.valueOf(RET_REG_REQ.ID), new RET_REG_REQ());
        TYPE_MAP.put(Integer.valueOf(RET_REG_RESULT.ID), new RET_REG_RESULT());
        TYPE_MAP.put(Integer.valueOf(RET_LOGIN_REQ.ID), new RET_LOGIN_REQ());
        TYPE_MAP.put(Integer.valueOf(RET_LOGIN_RESULT_UPDATE.ID), new RET_LOGIN_RESULT_UPDATE());
        TYPE_MAP.put(Integer.valueOf(RET_AUTH_SYNC.ID), new RET_AUTH_SYNC());
        TYPE_MAP.put(Integer.valueOf(RET_AUTH_SYNC_RECV.ID), new RET_AUTH_SYNC_RECV());
        TYPE_MAP.put(Integer.valueOf(RET_AUTH_SYNC_ACK_RECV.ID), new RET_AUTH_SYNC_ACK_RECV());
        TYPE_MAP.put(Integer.valueOf(RET_AUTH_ACK_RECV.ID), new RET_AUTH_ACK_RECV());
        TYPE_MAP.put(Integer.valueOf(RET_QUERY_PK.ID), new RET_QUERY_PK());
        TYPE_MAP.put(Integer.valueOf(RET_GET_PK.ID), new RET_GET_PK());
        TYPE_MAP.put(Integer.valueOf(-2147483636), new RET_AUTH_SLAVE_RECV_KEY());
        TYPE_MAP.put(Integer.valueOf(RET_AUTH_MASTER_RECV_KEY.ID), new RET_AUTH_MASTER_RECV_KEY());
        TYPE_MAP.put(Integer.valueOf(RET_GET_TCIS_ID.ID), new RET_GET_TCIS_ID());
        TYPE_MAP.put(Integer.valueOf(RET_LOGOUT_REQ.ID), new RET_LOGOUT_REQ());
        TYPE_MAP.put(Integer.valueOf(RET_UNREG_REQ.ID), new RET_UNREG_REQ());
        TYPE_MAP.put(Integer.valueOf(RET_REG_CANCEL.ID), new RET_REG_CANCEL());
        TYPE_MAP.put(Integer.valueOf(RET_LOGIN_CANCEL.ID), new RET_LOGIN_CANCEL());
        TYPE_MAP.put(Integer.valueOf(RET_AUTH_CANCEL.ID), new RET_AUTH_CANCEL());
        TYPE_MAP.put(Integer.valueOf(RET_GET_LOGIN_STATUS.ID), new RET_GET_LOGIN_STATUS());
        TYPE_MAP.put(Integer.valueOf(RET_INIT_REQ.ID), new RET_INIT_REQ());
        TYPE_MAP.put(Integer.valueOf(DATA_TCIS_ERROR_STEP.ID), new DATA_TCIS_ERROR_STEP());
        TYPE_MAP.put(Integer.valueOf(3841), new AuthData());
        TYPE_MAP.put(Integer.valueOf(3585), new AuthInfo());
        TYPE_MAP.put(Integer.valueOf(HighBitsALModeID.MODE_SRE_DISABLE), new AuthPkInfo());
        TYPE_MAP.put(Integer.valueOf(3843), new AuthSyncData());
        TYPE_MAP.put(Integer.valueOf(3842), new Cert());
        TYPE_MAP.put(Integer.valueOf(CommandsInterface.EMCOM_DS_HTTP_INFO), new RegAuthKeyData());
        TYPE_MAP.put(Integer.valueOf(770), new UpdateIndexInfo());
        TYPE_MAP.put(Integer.valueOf(776), new UpdateIndex());
        TYPE_MAP.put(Integer.valueOf(255), new StatesInfo());
    }

    public TLVTreeInvoker() {
        this.mID = 0;
        this.mTag = (short) 0;
        this.mType = null;
    }

    public TLVTreeInvoker(int id) {
        super(id);
        this.mType = getInvokeredTLVById(id);
        if (this.mType != null) {
            this.mTag = ((TLVTree) this.mType).getTreeTag();
        } else {
            this.mTag = (short) 0;
        }
    }

    public TLVTreeInvoker(T t) {
        super((Object) t);
        this.mID = t.getCmdID();
        this.mTag = t.getTreeTag();
    }

    protected short getTagByType(TLVTree type) {
        if (this.mType != null) {
            this.mTag = ((TLVTree) this.mType).getTreeTag();
        }
        return this.mTag;
    }

    <T extends TLVTree> T getInvokeredTLVById(int id) {
        if (TYPE_MAP == null || (TYPE_MAP.isEmpty() ^ 1) == 0) {
            return null;
        }
        return (TLVTree) TYPE_MAP.get(Integer.valueOf(id));
    }

    public int getID() {
        return this.mType != null ? ((TLVTree) this.mType).getCmdID() : 0;
    }

    public String byteArray2ServerHexString() {
        if (this.mType != null) {
            return ByteUtil.byteArray2ServerHexString(((TLVTree) this.mType).mOriginalByteArray);
        }
        return null;
    }

    public <T> T byteArray2Type(Byte[] raw) {
        if (raw == null) {
            LogHelper.e(ICommand.TAG, "error_tlv in TLVTreeInvoker.byteArray2Type:input byte array is null");
            return null;
        }
        byte[] unboxBytes = ByteUtil.unboxByteArray(raw);
        if (unboxBytes.length == 0) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(unboxBytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        if (this.mType == null) {
            this.mType = (TLVTree) TYPE_MAP.get(Integer.valueOf(this.mID));
        }
        if (this.mType != null) {
            ((TLVTree) this.mType).parse(buffer);
            ((TLVTree) this.mType).setOriginalTLVBytes(raw);
        }
        return this.mType;
    }

    public <T> Byte[] type2ByteArray(T t) {
        if (t == null || ((t instanceof TLVTree) ^ 1) != 0) {
            return new Byte[0];
        }
        return ((TLVTree) t).encapsulate();
    }

    public boolean isTypeExists(int id) {
        return TYPE_MAP.get(Integer.valueOf(id)) != null;
    }

    public boolean isTypeExists(short tag) {
        return findTypeByTag(tag) != null;
    }

    public <T> T findTypeByTag(short tag) {
        for (Entry<Integer, TLVTree> entry : TYPE_MAP.entrySet()) {
            TLVTree tree = (TLVTree) entry.getValue();
            if (tree != null && tree.getTreeTag() != (short) 0 && tree.getTreeTag() == tag) {
                return tree;
            }
        }
        return null;
    }
}
