package defpackage;

import com.huawei.android.pushagent.datatype.pollingmessage.basic.PollingMessage;
import java.io.InputStream;
import java.util.HashMap;

/* renamed from: l */
public class l {
    private static HashMap G;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: l.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: l.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: l.<clinit>():void");
    }

    public static PollingMessage a(Byte b, InputStream inputStream) {
        if (G.containsKey(b)) {
            PollingMessage pollingMessage = (PollingMessage) ((Class) G.get(b)).newInstance();
            PollingMessage a = pollingMessage.a(inputStream);
            if (a != null) {
                aw.d("PushLog2828", "after decode msg:" + a);
            } else {
                aw.e("PushLog2828", "call " + pollingMessage.getClass().getSimpleName() + " decode failed!");
            }
            return a;
        }
        aw.e("PushLog2828", "cmdId:" + b + " is not exist, all:" + G.keySet());
        throw new InstantiationException("cmdId:" + b + " is not register");
    }
}
