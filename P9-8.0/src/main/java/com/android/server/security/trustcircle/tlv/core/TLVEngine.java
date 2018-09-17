package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVRootTreeInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.TLVRootTreeInvokerWrapper;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status.TCIS_Result;

public class TLVEngine {
    public static final String TAG = TLVEngine.class.getSimpleName();

    public static class TLVResult<T extends TLVRootTree> {
        private int resultCode;
        private T tlv;

        public TLVResult(int resultCode, T tlv) {
            this.resultCode = resultCode;
            this.tlv = tlv;
        }

        public int getResultCode() {
            return this.resultCode;
        }

        public <T extends TLVRootTree> T getResultTLV() {
            return this.tlv;
        }
    }

    public static byte[] encode2CmdTLV(TLVTree tree) {
        if (tree == null) {
            LogHelper.e(TAG, "error_tlv: tlv is null");
            return new byte[0];
        } else if (tree instanceof TLVRootTree) {
            return ByteUtil.unboxByteArray(new TLVRootTreeInvokerWrapper(new TLVRootTreeInvoker((TLVRootTree) tree)).encapsulate());
        } else {
            if (tree instanceof TLVChildTree) {
                LogHelper.e(TAG, "error_tlv: child tree can't be encapsulated to cmd");
                return new byte[0];
            }
            LogHelper.e(TAG, "error_tlv: unknown tlv type - " + tree.getClass().getSimpleName());
            return new byte[0];
        }
    }

    public static <T extends TLVRootTree> TLVResult<T> decodeCmdTLV(byte[] tlv) {
        TLVRootTreeInvokerWrapper<TLVRootTree> wrapper = new TLVRootTreeInvokerWrapper(null);
        T targetTree = null;
        int result = TCIS_Result.UNKNOWN.value();
        if (wrapper.parse(tlv)) {
            targetTree = (TLVRootTree) wrapper.getTLVStruct();
            result = wrapper.getRetResult();
        } else {
            LogHelper.e(TAG, "error_tlv: parse failed");
        }
        return new TLVResult(result, targetTree);
    }

    public static byte[] encode2TLV(TLVTree tree) {
        if (tree == null) {
            LogHelper.e(TAG, "error_tlv: tlv is null");
            return new byte[0];
        }
        TLVInvoker<?> invoker;
        if (tree instanceof TLVRootTree) {
            invoker = new TLVRootTreeInvoker((TLVRootTree) tree);
        } else if (tree instanceof TLVChildTree) {
            invoker = new TLVChildTreeInvoker((TLVChildTree) tree);
        } else {
            LogHelper.e(TAG, "error_tlv: unknown tlv type - " + tree.getClass().getSimpleName());
            return new byte[0];
        }
        return ByteUtil.unboxByteArray(invoker.encapsulate());
    }

    public static <T extends TLVTree> T decodeTLV(byte[] tlv) {
        TLVInvoker<TLVTree> invoker = new TLVTreeInvoker();
        if (invoker.parse(tlv)) {
            return (TLVTree) invoker.getTLVStruct();
        }
        return null;
    }

    public static <T extends TLVTree> T decodeTLV(Byte[] tlv) {
        return decodeTLV(ByteUtil.unboxByteArray(tlv));
    }
}
