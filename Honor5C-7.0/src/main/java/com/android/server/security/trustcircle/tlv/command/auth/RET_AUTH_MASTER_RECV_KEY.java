package com.android.server.security.trustcircle.tlv.command.auth;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVLongInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class RET_AUTH_MASTER_RECV_KEY extends TLVRootTree {
    public static int ID = 0;
    public static final short TAG_AUTH_ID = (short) 4864;
    public static final short TAG_IV = (short) 4867;
    public static final short TAG_MAC = (short) 4865;
    public static final short TAG_RET_AUTH_MASTER_RECV_KEY = (short) 19;
    public static final short TAG_SESSION_KEY = (short) 4866;
    public int SIZE_LONG;
    public int SIZE_SHORT;
    public TLVLongInvoker authID;
    public TLVByteArrayInvoker iv;
    public TLVByteArrayInvoker mac;
    public TLVByteArrayInvoker sessionKey;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_MASTER_RECV_KEY.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_MASTER_RECV_KEY.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_MASTER_RECV_KEY.<clinit>():void");
    }

    public RET_AUTH_MASTER_RECV_KEY() {
        this.SIZE_SHORT = 68;
        this.SIZE_LONG = 92;
        this.authID = new TLVLongInvoker(TAG_AUTH_ID);
        this.mac = new TLVByteArrayInvoker(TAG_MAC);
        this.sessionKey = new TLVByteArrayInvoker(TAG_SESSION_KEY);
        this.iv = new TLVByteArrayInvoker(TAG_IV);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.authID);
        this.mNodeList.add(this.mac);
        this.mNodeList.add(this.sessionKey);
        this.mNodeList.add(this.iv);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return TAG_RET_AUTH_MASTER_RECV_KEY;
    }
}
