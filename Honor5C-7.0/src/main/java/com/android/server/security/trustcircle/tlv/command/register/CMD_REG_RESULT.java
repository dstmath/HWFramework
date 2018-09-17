package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class CMD_REG_RESULT extends TLVRootTree {
    public static int ID = 0;
    public static final short TAG_AUTH_PK_INFO_SIGNATURE = (short) 769;
    public static final short TAG_CMD_REG_RESULT = (short) 3;
    public static final short TAG_UPDATE_INDEX_INFO_SIGNATURE = (short) 771;
    public int SIZE;
    public TLVByteArrayInvoker authPKInfoSign;
    public TLVChildTreeInvoker authPkInfo;
    public TLVChildTreeInvoker updateIndexInfo;
    public TLVByteArrayInvoker updateIndexInfoSign;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.command.register.CMD_REG_RESULT.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.command.register.CMD_REG_RESULT.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.command.register.CMD_REG_RESULT.<clinit>():void");
    }

    public CMD_REG_RESULT() {
        this.SIZE = -1;
        this.authPkInfo = new TLVChildTreeInvoker(768);
        this.authPKInfoSign = new TLVByteArrayInvoker(TAG_AUTH_PK_INFO_SIGNATURE);
        this.updateIndexInfo = new TLVChildTreeInvoker(770);
        this.updateIndexInfoSign = new TLVByteArrayInvoker(TAG_UPDATE_INDEX_INFO_SIGNATURE);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authPkInfo);
        this.mNodeList.add(this.authPKInfoSign);
        this.mNodeList.add(this.updateIndexInfo);
        this.mNodeList.add(this.updateIndexInfoSign);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return TAG_CMD_REG_RESULT;
    }
}
