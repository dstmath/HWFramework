package com.android.server.security.trustcircle.tlv.command.register;

import com.android.server.security.trustcircle.tlv.core.TLVByteArrayInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVIntegerInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVNumberInvoker.TLVShortInvoker;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvoker.TLVChildTreeInvoker;
import java.util.Vector;

public class RET_REG_REQ extends TLVRootTree {
    public static int ID = 0;
    public static final short TAG_AUTH_KEY_ALGO_ENCODE = (short) 513;
    public static final short TAG_CLIENT_CHALLENGE = (short) 516;
    public static final short TAG_GLOBALKEY_ID = (short) 512;
    public static final short TAG_KEY_DATA_SIGNATURE = (short) 515;
    public static final short TAG_REG_AUTH_KEY_DATA = (short) 514;
    public static final short TAG_RET_REG_REQ = (short) 2;
    public int SIZE;
    public TLVShortInvoker authKeyAlgoEncode;
    public TLVByteArrayInvoker clientChallenge;
    public TLVIntegerInvoker glogbalKeyID;
    public TLVChildTreeInvoker regAuthKeyData;
    public TLVByteArrayInvoker regAuthKeyDataSign;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.tlv.command.register.RET_REG_REQ.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.tlv.command.register.RET_REG_REQ.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.tlv.command.register.RET_REG_REQ.<clinit>():void");
    }

    public RET_REG_REQ() {
        this.SIZE = 216;
        this.glogbalKeyID = new TLVIntegerInvoker(TAG_GLOBALKEY_ID);
        this.authKeyAlgoEncode = new TLVShortInvoker(TAG_AUTH_KEY_ALGO_ENCODE);
        this.regAuthKeyData = new TLVChildTreeInvoker(514);
        this.regAuthKeyDataSign = new TLVByteArrayInvoker(TAG_KEY_DATA_SIGNATURE);
        this.clientChallenge = new TLVByteArrayInvoker(TAG_CLIENT_CHALLENGE);
        this.mNodeList = new Vector();
        this.mNodeList.add(this.glogbalKeyID);
        this.mNodeList.add(this.authKeyAlgoEncode);
        this.mNodeList.add(this.regAuthKeyData);
        this.mNodeList.add(this.regAuthKeyDataSign);
        this.mNodeList.add(this.clientChallenge);
    }

    public int getCmdID() {
        return ID;
    }

    public short getTreeTag() {
        return TAG_RET_REG_REQ;
    }
}
