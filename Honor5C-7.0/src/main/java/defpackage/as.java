package defpackage;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.model.channel.ChannelMgr;

/* renamed from: as */
public class as extends o {
    private static String LOG_TAG;
    private static boolean bJ;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: as.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: as.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: as.<clinit>():void");
    }

    public as(Context context) {
    }

    private static void a(Context context, boolean z, String str) {
        Intent intent = new Intent();
        aw.d(LOG_TAG, "sendStateBroadcast the current push state is: " + z);
        intent.setAction("com.huawei.intent.action.PUSH_STATE").putExtra("push_state", z).setFlags(32);
        if (intent != null) {
            intent.setPackage(str);
        }
        context.sendBroadcast(intent);
    }

    private static void k(boolean z) {
        bJ = z;
    }

    public void onReceive(Context context, Intent intent) {
        aw.d(LOG_TAG, "enter ChannelRecorder:onReceive(intent:" + intent + " context:" + context);
        String action = intent.getAction();
        boolean hasConnection = ChannelMgr.aW().hasConnection();
        aw.d(LOG_TAG, "PushState get action :" + action);
        if ("com.huawei.android.push.intent.GET_PUSH_STATE".equals(action)) {
            action = intent.getStringExtra("pkg_name");
            aw.d(LOG_TAG, "responseClinetGetPushState: get the client packageName: " + action);
            try {
                aw.d(LOG_TAG, "current program pkgName is: " + context.getPackageName());
                aw.d(LOG_TAG, "the current push curIsConnect:" + hasConnection);
                as.a(context, hasConnection, action);
            } catch (Exception e) {
                aw.d(LOG_TAG, "e:" + e.toString());
            }
        }
        if (bJ != hasConnection) {
            as.k(hasConnection);
        }
    }
}
