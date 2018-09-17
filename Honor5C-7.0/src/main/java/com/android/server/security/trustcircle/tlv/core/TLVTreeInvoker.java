package com.android.server.security.trustcircle.tlv.core;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map.Entry;

public class TLVTreeInvoker<T extends TLVTree> extends TLVInvoker<TLVTree> {
    static HashMap<Integer, TLVTree> TYPE_MAP;

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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.<clinit>():void");
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
        if (TYPE_MAP == null || TYPE_MAP.isEmpty()) {
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
        if (unboxBytes == null) {
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
        if (t == null || !(t instanceof TLVTree)) {
            return null;
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
