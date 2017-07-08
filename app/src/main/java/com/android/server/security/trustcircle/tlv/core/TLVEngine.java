package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVRootTreeInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.TCIS_Result;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.TLVRootTreeInvokerWrapper;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;

public class TLVEngine {
    public static final String TAG = null;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.core.TLVEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.core.TLVEngine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.core.TLVEngine.<clinit>():void");
    }

    public static byte[] encode2CmdTLV(TLVTree tree) {
        if (tree == null) {
            LogHelper.e(TAG, "error_tlv: tlv is null");
            return null;
        } else if (tree instanceof TLVRootTree) {
            return ByteUtil.unboxByteArray(new TLVRootTreeInvokerWrapper(new TLVRootTreeInvoker((TLVRootTree) tree)).encapsulate());
        } else {
            if (tree instanceof TLVChildTree) {
                LogHelper.e(TAG, "error_tlv: child tree can't be encapsulated to cmd");
                return null;
            }
            LogHelper.e(TAG, "error_tlv: unknown tlv type - " + tree.getClass().getSimpleName());
            return null;
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
            return null;
        }
        TLVInvoker<?> invoker;
        if (tree instanceof TLVRootTree) {
            invoker = new TLVRootTreeInvoker((TLVRootTree) tree);
        } else if (tree instanceof TLVChildTree) {
            invoker = new TLVChildTreeInvoker((TLVChildTree) tree);
        } else {
            LogHelper.e(TAG, "error_tlv: unknown tlv type - " + tree.getClass().getSimpleName());
            return null;
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
