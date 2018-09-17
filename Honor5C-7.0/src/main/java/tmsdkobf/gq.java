package tmsdkobf;

import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public class gq {
    public static long pm;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.gq.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.gq.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.gq.<clinit>():void");
    }

    public static String a(String str, List<String> list) {
        if (str == null || list == null || list.isEmpty()) {
            return str;
        }
        for (String str2 : list) {
            if (str.contains(str2)) {
                return str.replaceFirst(str2, "");
            }
        }
        return str;
    }

    public static String aN(String str) {
        if (str == null) {
            return str;
        }
        int lastIndexOf = str.lastIndexOf("/");
        return (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) ? str.substring(lastIndexOf + 1, str.length()) : str;
    }

    public static String aO(String str) {
        String aN = aN(str);
        if (aN == null) {
            return aN;
        }
        int lastIndexOf = aN.lastIndexOf(".");
        return (lastIndexOf > 0 && lastIndexOf < aN.length() - 1) ? aN.substring(0, lastIndexOf) : aN;
    }

    public static List<String> aT() {
        List<String> p = ms.p(TMSDKContext.getApplicaionContext());
        if (p.size() > 0) {
            return p;
        }
        p = e(lv.dF());
        if (p == null) {
            p = new ArrayList();
            p.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        return p;
    }

    public static List<File> aU() {
        List<String> aT = aT();
        List arrayList = new ArrayList();
        if (aT != null) {
            for (String file : aT) {
                File[] listFiles = new File(file).listFiles();
                if (listFiles != null) {
                    arrayList.addAll(Arrays.asList(listFiles));
                }
            }
        }
        return arrayList;
    }

    public static String c(String str, String str2) {
        Object obj;
        int i;
        int i2 = 0;
        String str3 = str;
        while (str3.startsWith("/")) {
            str3 = str3.substring(1);
        }
        String str4 = str2;
        while (str4.startsWith("/")) {
            str4 = str4.substring(1);
        }
        Object split = str3.split("/");
        Object split2 = str4.split("/");
        if (split.length >= split2.length) {
            obj = split;
            i = 0;
        } else {
            obj = split2;
            String[] strArr = (String[]) split.clone();
            i = 1;
        }
        int i3 = 0;
        int i4 = 0;
        while (i3 < obj.length) {
            if (obj[i3].equals(strArr[0])) {
                int i5 = i4 + 1;
                i4 = 1;
                while (i4 < strArr.length) {
                    if (i3 + i4 >= obj.length || !obj[i3 + i4].equals(strArr[i4])) {
                        i5 = 0;
                        break;
                    }
                    i5++;
                    i4++;
                }
                if (i5 == strArr.length) {
                    i2 = 1;
                    break;
                }
                i4 = i5;
            }
            i3++;
        }
        return i2 == 0 ? null : i == 0 ? str : str2;
    }

    private static List<String> e(List<String> list) {
        int i = 0;
        Object obj = null;
        while (i < list.size()) {
            Object obj2;
            for (int i2 = i + 1; i2 < list.size(); i2++) {
                String c = c((String) list.get(i), (String) list.get(i2));
                if (c != null) {
                    list.remove(c);
                    obj2 = 1;
                    break;
                }
            }
            obj2 = obj;
            if (obj2 != null) {
                return e(list);
            }
            i++;
            obj = obj2;
        }
        return list;
    }
}
