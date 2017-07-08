package com.android.server.security.trustcircle.tlv.command.login;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Vector;

public class RET_LOGIN_REQ extends TLVRootTree {
    public static int ID = 0;
    public static final short TAG_CLIENT_CHALLENGE = (short) 1281;
    public static final short TAG_INDEX_VERSION = (short) 1280;
    public static final short TAG_RET_LOGIN_REQ = (short) 5;
    public int SIZE;
    public TLVByteArrayInvoker clientChallenge;
    public TLVIntegerInvoker indexVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_REQ.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_REQ.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_REQ.<clinit>():void");
    }

    public RET_LOGIN_REQ() {
        this.SIZE = 24;
        this.indexVersion = new TLVIntegerInvoker(TAG_INDEX_VERSION);
        this.clientChallenge = new TLVByteArrayInvoker(TAG_CLIENT_CHALLENGE);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.indexVersion);
        this.mNodeList.add(this.clientChallenge);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return TAG_RET_LOGIN_REQ;
    }
}
