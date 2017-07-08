package tmsdkobf;

import java.io.File;
import java.util.HashMap;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class go {
    private static qa pi;
    private static HashMap<String, py> pj;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.go.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.go.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.go.<clinit>():void");
    }

    public static void a(fy fyVar) {
        if (fyVar.getPackageName() == null || fyVar.getPackageName().equals("")) {
            fyVar.an(6);
            return;
        }
        int c = TMServiceFactory.getSystemInfoService().c(fyVar.getPackageName(), fyVar.hB());
        if (c == 0) {
            fyVar.an(2);
        } else if (c == 2) {
            fyVar.an(9);
        } else if (c == -1) {
            fyVar.an(1);
        } else if (c == 1) {
            fyVar.an(11);
        }
    }

    public static fy aJ(String str) {
        py i;
        py pyVar = pj == null ? null : (py) pj.get(str);
        if (pyVar == null) {
            try {
                i = pi.i(str, 73);
            } catch (Exception e) {
                d.f("ApkUtil", e.getMessage());
            }
            if (i == null) {
                i = aK(str);
            }
            if (i == null) {
                return null;
            }
            fy a = lr.a(i);
            a(a);
            return a;
        }
        i = pyVar;
        if (i == null) {
            i = aK(str);
        }
        if (i == null) {
            return null;
        }
        fy a2 = lr.a(i);
        a(a2);
        return a2;
    }

    public static py aK(String str) {
        py pyVar = new py();
        File file = new File(str);
        if (file != null) {
            String name = file.getName();
            if (!(name == null || name.equals(""))) {
                name = name.substring(0, name.length() - 4);
            }
            pyVar.cS(null);
            pyVar.aS(str);
            pyVar.setAppName(name);
            pyVar.setSize(file.length());
            pyVar.O(true);
        }
        return pyVar;
    }
}
