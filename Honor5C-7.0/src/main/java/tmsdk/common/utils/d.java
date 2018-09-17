package tmsdk.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/* compiled from: Unknown */
public final class d {
    private static boolean KX;
    private static a KY;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.utils.d.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.utils.d.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.utils.d.<clinit>():void");
    }

    public static void P(boolean z) {
        KX = z;
        KY = !KX ? new g() : new e();
    }

    public static void a(String str, Object obj, Throwable th) {
        KY.b(str, e(obj), th);
    }

    public static void b(String str, Object obj, Throwable th) {
        KY.a(str, e(obj), th);
    }

    public static void c(String str, Object obj) {
        KY.r(str, e(obj));
    }

    public static void d(String str, Object obj) {
        KY.s(str, e(obj));
    }

    private static String e(Object obj) {
        return obj != null ? !(obj instanceof String) ? !(obj instanceof Throwable) ? obj.toString() : getStackTraceString((Throwable) obj) : (String) obj : null;
    }

    public static void e(String str, Object obj) {
        KY.d(str, e(obj));
    }

    public static void f(String str, Object obj) {
        KY.x(str, e(obj));
    }

    public static void g(String str, Object obj) {
        KY.w(str, e(obj));
    }

    public static String getStackTraceString(Throwable th) {
        if (th == null) {
            return "(Null stack trace)";
        }
        Writer stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        th.printStackTrace(printWriter);
        printWriter.flush();
        String stringWriter2 = stringWriter.toString();
        printWriter.close();
        return stringWriter2;
    }

    public static void h(String str, Object obj) {
        KY.h(str, e(obj));
    }

    public static boolean isEnable() {
        return KX;
    }
}
