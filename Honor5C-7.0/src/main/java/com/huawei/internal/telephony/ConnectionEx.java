package com.huawei.internal.telephony;

import com.android.internal.telephony.Connection;
import com.huawei.android.util.NoExtAPIException;

public class ConnectionEx {

    public enum DisconnectCause {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.internal.telephony.ConnectionEx.DisconnectCause.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.internal.telephony.ConnectionEx.DisconnectCause.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.internal.telephony.ConnectionEx.DisconnectCause.<clinit>():void");
        }
    }

    public ConnectionEx() {
    }

    public static final String getErrorInfo(Connection obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final CallModifyEx getCallModify(Connection obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static final CallDetailsEx getCallDetails(Connection obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static DisconnectCause getDisconnectCause(Connection obj) {
        throw new NoExtAPIException("method not supported.");
    }
}
