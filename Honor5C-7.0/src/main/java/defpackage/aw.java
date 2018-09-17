package defpackage;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/* renamed from: aw */
public class aw {
    private static String bL;
    private static aw bM;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: aw.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: aw.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: aw.<clinit>():void");
    }

    private aw() {
    }

    private synchronized void a(int i, String str, String str2, Throwable th, int i2) {
        try {
            if (aw.isLoggable(i)) {
                String str3 = "[" + Thread.currentThread().getName() + "-" + Thread.currentThread().getId() + "]" + str2;
                StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                str3 = stackTrace.length > i2 ? str3 + "(" + bL + "/" + stackTrace[i2].getFileName() + ":" + stackTrace[i2].getLineNumber() + ")" : str3 + "(" + bL + "/unknown source)";
                if (th != null) {
                    str3 = str3 + '\n' + aw.getStackTraceString(th);
                }
                Log.println(i, str, str3);
            }
        } catch (Throwable e) {
            aw.d("PushLog2828", "call writeLog cause:" + e.toString(), e);
        }
    }

    public static void a(String str, String str2, Throwable th) {
        aw.bL().a(3, str, str2, th, 2);
    }

    public static void b(String str, String str2, Throwable th) {
        aw.bL().a(4, str, str2, th, 2);
    }

    private static synchronized aw bL() {
        aw awVar;
        synchronized (aw.class) {
            if (bM == null) {
                bM = new aw();
            }
            awVar = bM;
        }
        return awVar;
    }

    public static void c(String str, String str2, Throwable th) {
        aw.bL().a(5, str, str2, th, 2);
    }

    public static void d(String str, String str2) {
        aw.bL().a(3, str, str2, null, 2);
    }

    public static void d(String str, String str2, Throwable th) {
        aw.bL().a(6, str, str2, th, 2);
    }

    public static void e(String str, String str2) {
        aw.bL().a(6, str, str2, null, 2);
    }

    public static String getStackTraceString(Throwable th) {
        return Log.getStackTraceString(th);
    }

    public static void i(String str, String str2) {
        aw.bL().a(4, str, str2, null, 2);
    }

    public static void init(Context context) {
        if (bM == null) {
            aw.bL();
        }
        if (TextUtils.isEmpty(bL)) {
            if (bM != null) {
                ag.n(context);
            }
            String packageName = context.getPackageName();
            if (packageName != null) {
                String[] split = packageName.split("\\.");
                if (split != null && split.length > 0) {
                    bL = split[split.length - 1];
                }
            }
        }
    }

    private static boolean isLoggable(int i) {
        try {
            return Log.isLoggable("hwpush", i);
        } catch (Exception e) {
            return false;
        }
    }

    public static void v(String str, String str2) {
        aw.bL().a(2, str, str2, null, 2);
    }

    public static void w(String str, String str2) {
        aw.bL().a(5, str, str2, null, 2);
    }
}
