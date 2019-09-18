package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status;

public class TLVEngine {
    public static final String TAG = TLVEngine.class.getSimpleName();

    public static class TLVResult<T extends TLVTree.TLVRootTree> {
        private int resultCode;
        private T tlv;

        public TLVResult(int resultCode2, T tlv2) {
            this.resultCode = resultCode2;
            this.tlv = tlv2;
        }

        public int getResultCode() {
            return this.resultCode;
        }

        public <T extends TLVTree.TLVRootTree> T getResultTLV() {
            return this.tlv;
        }
    }

    public static byte[] encode2CmdTLV(TLVTree tree) {
        if (tree == null) {
            LogHelper.e(TAG, "error_tlv: tlv is null");
            return new byte[0];
        } else if (tree instanceof TLVTree.TLVRootTree) {
            return ByteUtil.unboxByteArray(new TLVTreeInvokerWrapper.TLVRootTreeInvokerWrapper<>(new TLVTreeInvoker.TLVRootTreeInvoker((TLVTree.TLVRootTree) tree)).encapsulate());
        } else {
            if (tree instanceof TLVTree.TLVChildTree) {
                LogHelper.e(TAG, "error_tlv: child tree can't be encapsulated to cmd");
                return new byte[0];
            }
            String str = TAG;
            LogHelper.e(str, "error_tlv: unknown tlv type - " + tree.getClass().getSimpleName());
            return new byte[0];
        }
    }

    /* JADX WARNING: Multi-variable type inference failed */
    public static <T extends TLVTree.TLVRootTree> TLVResult<T> decodeCmdTLV(byte[] tlv) {
        TLVTreeInvokerWrapper.TLVRootTreeInvokerWrapper<TLVTree.TLVRootTree> wrapper = new TLVTreeInvokerWrapper.TLVRootTreeInvokerWrapper<>(null);
        T targetTree = null;
        int result = Status.TCIS_Result.UNKNOWN.value();
        if (wrapper.parse(tlv)) {
            targetTree = (TLVTree.TLVRootTree) wrapper.getTLVStruct();
            result = wrapper.getRetResult();
        } else {
            LogHelper.e(TAG, "error_tlv: parse failed");
        }
        return new TLVResult<>(result, targetTree);
    }

    public static byte[] encode2TLV(TLVTree tree) {
        TLVInvoker<?> invoker;
        if (tree == null) {
            LogHelper.e(TAG, "error_tlv: tlv is null");
            return new byte[0];
        }
        if (tree instanceof TLVTree.TLVRootTree) {
            invoker = new TLVTreeInvoker.TLVRootTreeInvoker((TLVTree.TLVRootTree) tree);
        } else if (tree instanceof TLVTree.TLVChildTree) {
            invoker = new TLVTreeInvoker.TLVChildTreeInvoker((TLVTree.TLVChildTree) tree);
        } else {
            String str = TAG;
            LogHelper.e(str, "error_tlv: unknown tlv type - " + tree.getClass().getSimpleName());
            return new byte[0];
        }
        return ByteUtil.unboxByteArray(invoker.encapsulate());
    }

    public static <T extends TLVTree> T decodeTLV(byte[] tlv) {
        TLVTreeInvoker tLVTreeInvoker = new TLVTreeInvoker();
        if (tLVTreeInvoker.parse(tlv)) {
            return (TLVTree) tLVTreeInvoker.getTLVStruct();
        }
        return null;
    }

    public static <T extends TLVTree> T decodeTLV(Byte[] tlv) {
        return decodeTLV(ByteUtil.unboxByteArray(tlv));
    }
}
