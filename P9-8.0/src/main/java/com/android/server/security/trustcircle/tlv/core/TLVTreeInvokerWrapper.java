package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_CANCEL;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_MASTER_RECV_KEY;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC_RECV;
import com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_CANCEL;
import com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_REQ;
import com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_RESULT_UPDATE;
import com.android.server.security.trustcircle.tlv.command.logout.RET_LOGOUT_REQ;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_LOGIN_STATUS;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_PK;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_TCIS_ID;
import com.android.server.security.trustcircle.tlv.command.query.RET_INIT_REQ;
import com.android.server.security.trustcircle.tlv.command.query.RET_QUERY_PK;
import com.android.server.security.trustcircle.tlv.command.register.RET_CHECK_REG_STATUS;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_CANCEL;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_REQ;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_RESULT;
import com.android.server.security.trustcircle.tlv.command.register.RET_UNREG_REQ;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVRootTreeInvoker;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status.TCIS_Result;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public abstract class TLVTreeInvokerWrapper<T extends TLVTreeInvoker<TLVRootTree>> implements ICommand {
    private static HashMap<Integer, TLVRootTreeInvoker> CMD_MAP = new HashMap();
    private int mRetResult = TCIS_Result.UNKNOWN_CMD.value();
    T wrappedInvoker;

    public static class TLVRootTreeInvokerWrapper<TLVRootTreeInvoker> extends TLVTreeInvokerWrapper<TLVTreeInvoker<TLVRootTree>> {
        private static final int CMD_SIZE = 4;
        private static final int TLV_LENGT_SIZE = 4;
        private static final int TLV_RET_RESULT_LENGTH = 4;
        private Byte[] zeroLength = new Byte[]{Byte.valueOf((byte) 0), Byte.valueOf((byte) 0), Byte.valueOf((byte) 0), Byte.valueOf((byte) 0)};

        public TLVRootTreeInvokerWrapper(TLVRootTreeInvoker wrappedInvoker) {
            super((TLVTreeInvoker) wrappedInvoker);
        }

        public boolean parse(ByteBuffer buffer) {
            LogHelper.d(ICommand.TAG, ">>>>parse cmd<<<<");
            this.wrappedInvoker = getLegalTLVRootTreeCmd(buffer);
            if (this.wrappedInvoker == null || this.wrappedInvoker.getTLVStruct() == null) {
                return false;
            }
            if (this.wrappedInvoker.getTag() != (short) 0) {
                return this.wrappedInvoker.parse(buffer);
            }
            LogHelper.d(ICommand.TAG, "the cmd has no tlv");
            return true;
        }

        public Byte[] encapsulate() {
            LogHelper.d(ICommand.TAG, ">>>>encapsulate to cmd<<<<");
            if (this.wrappedInvoker == null) {
                LogHelper.e(ICommand.TAG, "error_tlv:encapsulated wrappedInvoker invalid");
                return new Byte[0];
            } else if (this.wrappedInvoker.getTLVStruct() instanceof TLVRootTree) {
                int cmdID = ((TLVTree) this.wrappedInvoker.getTLVStruct()).getCmdID();
                LogHelper.d(ICommand.TAG, "encapsulated cmd id: 0x" + ByteUtil.int2StrictHexString(cmdID) + ", TLV:" + TLVTreeInvokerWrapper.getInvokerName(cmdID));
                if (getRootTreeInvokerByCmdID(cmdID) == null) {
                    return new Byte[0];
                }
                Byte[] tlvLengthBytes;
                Byte[] cmdIDBytes = ByteUtil.int2ByteArray(cmdID);
                Byte[] tlvBytes = this.wrappedInvoker.encapsulate();
                if (tlvBytes.length == 0) {
                    tlvLengthBytes = this.zeroLength;
                    tlvBytes = new Byte[0];
                } else {
                    tlvLengthBytes = ByteUtil.int2ByteArray((short) tlvBytes.length);
                }
                Byte[] targetCmd = new Byte[((cmdIDBytes.length + tlvLengthBytes.length) + tlvBytes.length)];
                System.arraycopy(cmdIDBytes, 0, targetCmd, 0, cmdIDBytes.length);
                System.arraycopy(tlvLengthBytes, 0, targetCmd, cmdIDBytes.length, tlvLengthBytes.length);
                System.arraycopy(tlvBytes, 0, targetCmd, cmdIDBytes.length + tlvLengthBytes.length, tlvBytes.length);
                return targetCmd;
            } else {
                LogHelper.e(ICommand.TAG, "error_tlv:encapsulated tlv root tree invalid");
                return new Byte[0];
            }
        }

        private TLVTreeInvoker getLegalTLVRootTreeCmd(ByteBuffer buffer) {
            if (buffer == null || buffer.remaining() < 8) {
                LogHelper.e(ICommand.TAG, "error_tlv:return cmd length invalid: " + (buffer == null ? "buffer is null" : Integer.valueOf(buffer.remaining())));
                return null;
            }
            int cmdID = buffer.getInt();
            LogHelper.i(ICommand.TAG, "parsed cmd id: 0x" + ByteUtil.int2StrictHexString(cmdID) + ", TLV:" + TLVTreeInvokerWrapper.getInvokerName(cmdID));
            if (isCmdLegal(buffer, cmdID)) {
                return getRootTreeInvokerByCmdID(cmdID);
            }
            return null;
        }

        private boolean isCmdLegal(ByteBuffer buffer, int cmdID) {
            if (buffer == null || buffer.remaining() < 4) {
                LogHelper.e(ICommand.TAG, "error_tlv:cmd result description field length illegal:" + (buffer == null ? "ByteBuffer is null" : Integer.valueOf(buffer.remaining())));
                return false;
            }
            setRetResult(buffer.getInt());
            if (buffer.remaining() >= 4) {
                int claimedTLVLength = buffer.getInt();
                int realTLVLength = buffer.remaining();
                if (isInvokerExisted(cmdID) && claimedTLVLength == realTLVLength) {
                    return true;
                }
                LogHelper.e(ICommand.TAG, "error_tlv: cmd tlv length claimed: " + claimedTLVLength + ", actual:" + realTLVLength);
                return false;
            }
            LogHelper.e(ICommand.TAG, "error_tlv: cmd tlv length description field length illegal:" + buffer.remaining());
            return false;
        }

        public boolean isInvokerExisted(int cmdID) {
            if (TLVTreeInvokerWrapper.getInvoker(cmdID) != null) {
                return true;
            }
            LogHelper.e(ICommand.TAG, "error_tlv: unknown TLV cmd id:" + ByteUtil.int2StrictHexString(cmdID));
            return false;
        }

        private <T extends TLVTreeInvoker> T getRootTreeInvokerByCmdID(int cmdID) {
            TLVRootTreeInvoker invoker = TLVTreeInvokerWrapper.getInvoker(cmdID);
            if (invoker != null) {
                return invoker;
            }
            LogHelper.e(ICommand.TAG, "error_tlv: unknown TLV invoker");
            return null;
        }
    }

    public TLVTreeInvokerWrapper(T wrappedInvoker) {
        this.wrappedInvoker = wrappedInvoker;
    }

    public boolean parse(Byte[] bytes) {
        return parse(ByteUtil.unboxByteArray(bytes));
    }

    public boolean parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return parse(buffer);
    }

    public <T extends TLVTreeInvoker<TLVRootTree>> T getWrappedInvoker() {
        return this.wrappedInvoker;
    }

    public TLVTree getTLVStruct() {
        return this.wrappedInvoker != null ? (TLVTree) this.wrappedInvoker.getTLVStruct() : null;
    }

    static {
        CMD_MAP.put(Integer.valueOf(1), new TLVRootTreeInvoker(1));
        CMD_MAP.put(Integer.valueOf(2), new TLVRootTreeInvoker(2));
        CMD_MAP.put(Integer.valueOf(3), new TLVRootTreeInvoker(3));
        CMD_MAP.put(Integer.valueOf(4), new TLVRootTreeInvoker(4));
        CMD_MAP.put(Integer.valueOf(5), new TLVRootTreeInvoker(5));
        CMD_MAP.put(Integer.valueOf(6), new TLVRootTreeInvoker(6));
        CMD_MAP.put(Integer.valueOf(7), new TLVRootTreeInvoker(7));
        CMD_MAP.put(Integer.valueOf(8), new TLVRootTreeInvoker(8));
        CMD_MAP.put(Integer.valueOf(9), new TLVRootTreeInvoker(9));
        CMD_MAP.put(Integer.valueOf(10), new TLVRootTreeInvoker(10));
        CMD_MAP.put(Integer.valueOf(11), new TLVRootTreeInvoker(11));
        CMD_MAP.put(Integer.valueOf(12), new TLVRootTreeInvoker(12));
        CMD_MAP.put(Integer.valueOf(13), new TLVRootTreeInvoker(13));
        CMD_MAP.put(Integer.valueOf(14), new TLVRootTreeInvoker(14));
        CMD_MAP.put(Integer.valueOf(15), new TLVRootTreeInvoker(15));
        CMD_MAP.put(Integer.valueOf(16), new TLVRootTreeInvoker(16));
        CMD_MAP.put(Integer.valueOf(17), new TLVRootTreeInvoker(17));
        CMD_MAP.put(Integer.valueOf(18), new TLVRootTreeInvoker(18));
        CMD_MAP.put(Integer.valueOf(19), new TLVRootTreeInvoker(19));
        CMD_MAP.put(Integer.valueOf(12), new TLVRootTreeInvoker(12));
        CMD_MAP.put(Integer.valueOf(20), new TLVRootTreeInvoker(20));
        CMD_MAP.put(Integer.valueOf(RET_CHECK_REG_STATUS.ID), new TLVRootTreeInvoker((int) RET_CHECK_REG_STATUS.ID));
        CMD_MAP.put(Integer.valueOf(21), new TLVRootTreeInvoker(21));
        CMD_MAP.put(Integer.valueOf(RET_REG_REQ.ID), new TLVRootTreeInvoker((int) RET_REG_REQ.ID));
        CMD_MAP.put(Integer.valueOf(RET_REG_RESULT.ID), new TLVRootTreeInvoker((int) RET_REG_RESULT.ID));
        CMD_MAP.put(Integer.valueOf(RET_LOGIN_REQ.ID), new TLVRootTreeInvoker((int) RET_LOGIN_REQ.ID));
        CMD_MAP.put(Integer.valueOf(RET_LOGIN_RESULT_UPDATE.ID), new TLVRootTreeInvoker((int) RET_LOGIN_RESULT_UPDATE.ID));
        CMD_MAP.put(Integer.valueOf(RET_AUTH_SYNC.ID), new TLVRootTreeInvoker((int) RET_AUTH_SYNC.ID));
        CMD_MAP.put(Integer.valueOf(RET_AUTH_SYNC_RECV.ID), new TLVRootTreeInvoker((int) RET_AUTH_SYNC_RECV.ID));
        CMD_MAP.put(Integer.valueOf(RET_AUTH_SYNC_ACK_RECV.ID), new TLVRootTreeInvoker((int) RET_AUTH_SYNC_ACK_RECV.ID));
        CMD_MAP.put(Integer.valueOf(RET_AUTH_ACK_RECV.ID), new TLVRootTreeInvoker((int) RET_AUTH_ACK_RECV.ID));
        CMD_MAP.put(Integer.valueOf(RET_QUERY_PK.ID), new TLVRootTreeInvoker((int) RET_QUERY_PK.ID));
        CMD_MAP.put(Integer.valueOf(RET_GET_PK.ID), new TLVRootTreeInvoker((int) RET_GET_PK.ID));
        CMD_MAP.put(Integer.valueOf(-2147483636), new TLVRootTreeInvoker(-2147483636));
        CMD_MAP.put(Integer.valueOf(RET_AUTH_MASTER_RECV_KEY.ID), new TLVRootTreeInvoker((int) RET_AUTH_MASTER_RECV_KEY.ID));
        CMD_MAP.put(Integer.valueOf(RET_GET_TCIS_ID.ID), new TLVRootTreeInvoker((int) RET_GET_TCIS_ID.ID));
        CMD_MAP.put(Integer.valueOf(RET_LOGOUT_REQ.ID), new TLVRootTreeInvoker((int) RET_LOGOUT_REQ.ID));
        CMD_MAP.put(Integer.valueOf(RET_UNREG_REQ.ID), new TLVRootTreeInvoker((int) RET_UNREG_REQ.ID));
        CMD_MAP.put(Integer.valueOf(RET_REG_CANCEL.ID), new TLVRootTreeInvoker((int) RET_REG_CANCEL.ID));
        CMD_MAP.put(Integer.valueOf(RET_LOGIN_CANCEL.ID), new TLVRootTreeInvoker((int) RET_LOGIN_CANCEL.ID));
        CMD_MAP.put(Integer.valueOf(RET_AUTH_CANCEL.ID), new TLVRootTreeInvoker((int) RET_AUTH_CANCEL.ID));
        CMD_MAP.put(Integer.valueOf(RET_GET_LOGIN_STATUS.ID), new TLVRootTreeInvoker((int) RET_GET_LOGIN_STATUS.ID));
        CMD_MAP.put(Integer.valueOf(RET_INIT_REQ.ID), new TLVRootTreeInvoker((int) RET_INIT_REQ.ID));
    }

    public int getRetResult() {
        return this.mRetResult;
    }

    protected void setRetResult(int mRetResult) {
        this.mRetResult = mRetResult;
    }

    private static TLVRootTreeInvoker getInvoker(int id) {
        return (TLVRootTreeInvoker) CMD_MAP.get(Integer.valueOf(id));
    }

    private static String getInvokerName(int id) {
        TLVRootTreeInvoker invoker = getInvoker(id);
        if (invoker != null) {
            TLVTree tlv = invoker.getInvokeredTLVById(invoker.mID);
            if (tlv != null) {
                return tlv.getClass().getSimpleName();
            }
        }
        return "unknown";
    }
}
