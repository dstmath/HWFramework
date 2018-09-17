package com.android.server.security.trustcircle.tlv.command.query;

import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.Vector;

public class CMD_RECV_PK extends TLVRootTree {
    public static int ID = 0;
    public static final short TAG_AUTH_ID = (short) 6656;
    public static final short TAG_CMD_RECV_PK = (short) 26;
    public int SIZE;
    public TLVLongInvoker authID;
    public TLVChildTreeInvoker otherCert;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.command.query.CMD_RECV_PK.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.command.query.CMD_RECV_PK.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.command.query.CMD_RECV_PK.<clinit>():void");
    }

    public CMD_RECV_PK() {
        this.SIZE = WifiProCommonUtils.HTTP_REACHALBE_GOOLE;
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
        this.otherCert = new TLVChildTreeInvoker(3842);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.otherCert);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return TAG_CMD_RECV_PK;
    }
}
