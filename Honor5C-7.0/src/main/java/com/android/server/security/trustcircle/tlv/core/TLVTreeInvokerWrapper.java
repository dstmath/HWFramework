package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVRootTreeInvoker;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public abstract class TLVTreeInvokerWrapper<T extends TLVTreeInvoker<TLVRootTree>> implements ICommand {
    private static HashMap<Integer, TLVRootTreeInvoker> CMD_MAP;
    private int mRetResult;
    T wrappedInvoker;

    public enum TCIS_Result {
        ;
        
        private int value;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.TCIS_Result.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.TCIS_Result.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.TCIS_Result.<clinit>():void");
        }

        private TCIS_Result(int value) {
            this.value = -1;
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public static class TLVRootTreeInvokerWrapper<TLVRootTreeInvoker> extends TLVTreeInvokerWrapper<TLVTreeInvoker<TLVRootTree>> {
        private static final int CMD_SIZE = 4;
        private static final int TLV_LENGT_SIZE = 4;
        private static final int TLV_RET_RESULT_LENGTH = 4;
        private Byte[] zeroLength;

        public TLVRootTreeInvokerWrapper(TLVRootTreeInvoker wrappedInvoker) {
            super((TLVTreeInvoker) wrappedInvoker);
            Byte[] bArr = new Byte[TLV_RET_RESULT_LENGTH];
            bArr[0] = Byte.valueOf((byte) 0);
            bArr[1] = Byte.valueOf((byte) 0);
            bArr[2] = Byte.valueOf((byte) 0);
            bArr[3] = Byte.valueOf((byte) 0);
            this.zeroLength = bArr;
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
            if (this.wrappedInvoker == null || !(this.wrappedInvoker instanceof TLVInvoker)) {
                LogHelper.e(ICommand.TAG, "error_tlv:encapsulated wrappedInvoker invalid");
                return null;
            } else if (this.wrappedInvoker.getTLVStruct() instanceof TLVRootTree) {
                int cmdID = ((TLVTree) this.wrappedInvoker.getTLVStruct()).getCmdID();
                LogHelper.d(ICommand.TAG, "encapsulated cmd id: 0x" + ByteUtil.int2StrictHexString(cmdID) + ", TLV:" + TLVTreeInvokerWrapper.getInvokerName(cmdID));
                if (getRootTreeInvokerByCmdID(cmdID) == null) {
                    return null;
                }
                Byte[] tlvLengthBytes;
                Byte[] cmdIDBytes = ByteUtil.int2ByteArray(cmdID);
                Byte[] tlvBytes = this.wrappedInvoker.encapsulate();
                if (tlvBytes == null || tlvBytes.length == 0) {
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
                return null;
            }
        }

        private TLVTreeInvoker getLegalTLVRootTreeCmd(ByteBuffer buffer) {
            if (buffer == null || buffer.remaining() < 8) {
                LogHelper.e(ICommand.TAG, "error_tlv:return cmd length invalid:" + buffer.remaining());
                return null;
            }
            int cmdID = buffer.getInt();
            LogHelper.d(ICommand.TAG, "parsed cmd id: 0x" + ByteUtil.int2StrictHexString(cmdID) + ", TLV:" + TLVTreeInvokerWrapper.getInvokerName(cmdID));
            if (isCmdLegal(buffer, cmdID)) {
                return getRootTreeInvokerByCmdID(cmdID);
            }
            return null;
        }

        private boolean isCmdLegal(ByteBuffer buffer, int cmdID) {
            if (buffer == null || buffer.remaining() < TLV_RET_RESULT_LENGTH) {
                LogHelper.e(ICommand.TAG, "error_tlv:cmd result description field length illegal:" + (buffer == null ? "ByteBuffer is null" : Integer.valueOf(buffer.remaining())));
                return false;
            }
            setRetResult(buffer.getInt());
            if (buffer.remaining() >= TLV_RET_RESULT_LENGTH) {
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

        public boolean isSuitableTreeInvoker() {
            if (this.wrappedInvoker != null && (this.wrappedInvoker instanceof TLVInvoker)) {
                return true;
            }
            LogHelper.e(ICommand.TAG, this.wrappedInvoker == null ? "error_tlv: wrapped TLV cmd is null" : "error_tlv:unkown TLV cmd type" + this.wrappedInvoker.getClass().getSimpleName());
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.<clinit>():void");
    }

    public abstract boolean isSuitableTreeInvoker();

    public TLVTreeInvokerWrapper(T wrappedInvoker) {
        this.mRetResult = TCIS_Result.UNKNOWN_CMD.value();
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
