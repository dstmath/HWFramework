package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import tmsdk.common.module.urlcheck.UrlCheckType;

/* compiled from: Unknown */
public class jy {
    private static final String[] uM = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.jy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.jy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.jy.<clinit>():void");
    }

    public static int a(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra("state");
        if (stringExtra != null) {
            if (stringExtra.equals("IDLE")) {
                return 0;
            }
            if (stringExtra.equals("RINGING")) {
                return 1;
            }
            if (stringExtra.equals("OFFHOOK")) {
                return 2;
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        return telephonyManager == null ? 0 : telephonyManager.getCallState();
    }

    public static void a(Context context, BroadcastReceiver broadcastReceiver) {
        String[] strArr;
        int i = 0;
        qz qzVar = jq.uh;
        if (qzVar != null) {
            String str = qzVar.if();
            strArr = (str == null || str.equalsIgnoreCase("android.intent.action.PHONE_STATE")) ? uM : new String[]{"android.intent.action.PHONE_STATE", str};
        } else {
            strArr = uM;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(UrlCheckType.UNKNOWN);
        intentFilter.addCategory("android.intent.category.DEFAULT");
        int length = strArr.length;
        while (i < length) {
            intentFilter.addAction(strArr[i]);
            i++;
        }
        context.registerReceiver(broadcastReceiver, intentFilter);
    }
}
