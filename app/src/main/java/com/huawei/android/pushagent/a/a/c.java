package com.huawei.android.pushagent.a.a;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/* compiled from: Unknown */
public class c {
    private static String a;
    private static String b;
    private static String c;
    private static c d;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.a.a.c.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.a.a.c.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.a.a.c.<clinit>():void");
    }

    private c() {
    }

    public static synchronized c a() {
        c cVar;
        synchronized (c.class) {
            if (d == null) {
                d = new c();
            }
            cVar = d;
        }
        return cVar;
    }

    public static String a(Throwable th) {
        return Log.getStackTraceString(th);
    }

    private synchronized void a(int i, String str, String str2, Throwable th, int i2) {
        try {
            if (a(i)) {
                String str3 = "[" + Thread.currentThread().getName() + "-" + Thread.currentThread().getId() + "]" + str2;
                StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                str3 = stackTrace.length <= i2 ? str3 + "(" + a + "/unknown source)" : str3 + "(" + a + "/" + stackTrace[i2].getFileName() + ":" + stackTrace[i2].getLineNumber() + ")";
                if (th != null) {
                    str3 = str3 + '\n' + a(th);
                }
                Log.println(i, c, str3);
            }
        } catch (Throwable e) {
            Log.e("PushLogSC2606", "call writeLog cause:" + e.toString(), e);
        }
    }

    public static synchronized void a(Context context) {
        synchronized (c.class) {
            if (d == null) {
                a();
            }
            if (TextUtils.isEmpty(a)) {
                String packageName = context.getPackageName();
                if (packageName != null) {
                    String[] split = packageName.split("\\.");
                    if (split != null && split.length > 0) {
                        a = split[split.length - 1];
                    }
                }
                c = b(context);
                return;
            }
        }
    }

    public static void a(String str, String str2) {
        a().a(3, str, str2, null, 2);
    }

    public static void a(String str, String str2, Throwable th) {
        a().a(3, str, str2, th, 2);
    }

    public static void a(String str, String str2, Object... objArr) {
        try {
            a().a(3, str, String.format(str2, objArr), null, 2);
        } catch (Throwable e) {
            Log.e("PushLogSC2606", "call writeLog cause:" + e.toString(), e);
        }
    }

    private static boolean a(int i) {
        return Log.isLoggable(b, i);
    }

    public static String b(Context context) {
        String str = "PushLogSC2606";
        if (context == null) {
            return str;
        }
        if ("com.huawei.android.pushagent".equals(context.getPackageName())) {
            str = str.replace("SC", "AC");
        } else if ("android".equals(context.getPackageName())) {
            str = str.replace("SC", "");
        }
        return str;
    }

    public static void b(String str, String str2) {
        a().a(4, str, str2, null, 2);
    }

    public static void b(String str, String str2, Throwable th) {
        a().a(4, str, str2, th, 2);
    }

    public static void b(String str, String str2, Object... objArr) {
        try {
            a().a(2, str, String.format(str2, objArr), null, 2);
        } catch (Throwable e) {
            Log.e("PushLogSC2606", "call writeLog cause:" + e.toString(), e);
        }
    }

    public static void c(String str, String str2) {
        a().a(5, str, str2, null, 2);
    }

    public static void c(String str, String str2, Throwable th) {
        a().a(6, str, str2, th, 2);
    }

    public static void d(String str, String str2) {
        a().a(6, str, str2, null, 2);
    }

    public static void d(String str, String str2, Throwable th) {
        a().a(2, str, str2, th, 2);
    }

    public static void e(String str, String str2) {
        a().a(2, str, str2, null, 2);
    }
}
