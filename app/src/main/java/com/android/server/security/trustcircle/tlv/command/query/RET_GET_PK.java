package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class RET_GET_PK extends TLVRootTree {
    public static int ID = 0;
    public static final short TAG_RET_GET_PK = (short) 25;
    public int SIZE;
    public TLVChildTreeInvoker cert;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.command.query.RET_GET_PK.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.command.query.RET_GET_PK.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.command.query.RET_GET_PK.<clinit>():void");
    }

    public RET_GET_PK() {
        this.SIZE = 192;
        this.cert = new TLVChildTreeInvoker(3842);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.cert);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return TAG_RET_GET_PK;
    }
}
