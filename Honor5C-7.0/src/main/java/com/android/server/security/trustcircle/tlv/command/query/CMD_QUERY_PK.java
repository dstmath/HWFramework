package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class CMD_QUERY_PK extends TLVRootTree {
    public static int ID = 0;
    public static final short TAG_AUTH_ID = (short) 5635;
    public static final short TAG_CMD_QUERY_PK = (short) 22;
    public static final short TAG_INDEX_VERSION = (short) 5634;
    public static final short TAG_TCIS_ID = (short) 5633;
    public static final short TAG_USER_ID = (short) 5632;
    public int SIZE;
    public TLVLongInvoker authID;
    public TLVIntegerInvoker indexVersion;
    public TLVByteArrayInvoker tcisID;
    public TLVLongInvoker userID;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.command.query.CMD_QUERY_PK.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.command.query.CMD_QUERY_PK.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.command.query.CMD_QUERY_PK.<clinit>():void");
    }

    public CMD_QUERY_PK() {
        this.SIZE = 38;
        this.userID = new TLVLongInvoker(TAG_USER_ID);
        this.tcisID = new TLVByteArrayInvoker(TAG_TCIS_ID);
        this.indexVersion = new TLVIntegerInvoker(TAG_INDEX_VERSION);
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.userID);
        this.mNodeList.add(this.tcisID);
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.authID);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return TAG_CMD_QUERY_PK;
    }
}
