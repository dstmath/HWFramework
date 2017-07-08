package sun.net;

import java.util.Map;
import java.util.Set;

public class RegisteredDomain {
    private static Set<String> arSet;
    private static Set<String> jp2Set;
    private static Set<String> jpSet;
    private static Set<String> omSet;
    private static Set<String> top1Set;
    private static Set<String> top2Set;
    private static Map<String, Set> top3Map;
    private static Set<String> top3Set;
    private static Set<String> top4Set;
    private static Set<String> top5Set;
    private static Map<String, Set> topMap;
    private static Set<String> ukSet;
    private static Set<String> usStateSet;
    private static Set<String> usSubStateSet;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.net.RegisteredDomain.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.net.RegisteredDomain.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.net.RegisteredDomain.<clinit>():void");
    }

    public static String getRegisteredDomain(String cname) {
        int dot = cname.lastIndexOf(46);
        if (dot == -1) {
            return cname;
        }
        if (dot == 0) {
            return "";
        }
        if (dot == cname.length() - 1) {
            cname = cname.substring(0, cname.length() - 1);
            dot = cname.lastIndexOf(46);
            if (dot == -1) {
                return cname;
            }
            if (dot == 0) {
                return "";
            }
        }
        if (dot == cname.length() - 1) {
            return "";
        }
        int second = cname.lastIndexOf(46, dot - 1);
        if (second == -1) {
            return cname;
        }
        if (second == 0) {
            return "";
        }
        int third = cname.lastIndexOf(46, second - 1);
        int fourth = -1;
        if (third > 0) {
            fourth = cname.lastIndexOf(46, third - 1);
        }
        int fifth = -1;
        if (fourth > 0) {
            fifth = cname.lastIndexOf(46, fourth - 1);
        }
        String s = cname.substring(dot + 1);
        String s2 = cname.substring(second + 1, dot);
        if (fourth != -1 && s.equals("us") && usStateSet.contains(s2)) {
            String s3 = cname.substring(third + 1, second);
            String s4 = cname.substring(fourth + 1, third);
            if (s3.equals("k12")) {
                if (s2.equals("ma") && (s4.equals("chtr") || s4.equals("paroch"))) {
                    return cname.substring(fifth + 1);
                }
                if (s4.equals("pvt")) {
                    return cname.substring(fifth + 1);
                }
            }
        }
        String str = cname.substring(third + 1);
        if (third != -1) {
            Set set = (Set) top3Map.get(s);
            if (set != null) {
                if (set.contains(str)) {
                    return cname.substring(fourth + 1);
                }
            } else if (s.equals("us") && usStateSet.contains(s2)) {
                if (!usSubStateSet.contains(cname.substring(third + 1, second))) {
                    return cname.substring(third + 1);
                }
                if (fourth != -1) {
                    cname = cname.substring(fourth + 1);
                }
                return cname;
            } else if (s.equals("uk")) {
                if (s2.equals("sch")) {
                    return cname.substring(fourth + 1);
                }
            } else if (s.equals("jp") && jpSet.contains(s2)) {
                if (jp2Set.contains(str)) {
                    return cname.substring(third + 1);
                }
                return cname.substring(fourth + 1);
            }
        }
        if (jp2Set.contains(str)) {
            return cname.substring(third + 1);
        }
        Set topSet = (Set) topMap.get(s);
        if (topSet != null) {
            if (topSet.contains(s2)) {
                return cname.substring(third + 1);
            }
            boolean contains = (s.equals("us") && usStateSet.contains(s2)) ? true : s.equals("jp") ? jpSet.contains(s2) : false;
            if (!contains) {
                return cname.substring(second + 1);
            }
        } else if (top2Set.contains(s)) {
            if (s2.equals("gov")) {
                return cname.substring(third + 1);
            }
            return cname.substring(second + 1);
        } else if (top3Set.contains(s)) {
            if ((s.equals("ad") && s2.equals("nom")) || ((s.equals("aw") && s2.equals("com")) || ((s.equals("be") && s2.equals("ac")) || ((s.equals("cl") && s2.equals("gov")) || ((s.equals("cl") && s2.equals("gob")) || ((s.equals("fi") && s2.equals("aland")) || ((s.equals("int") && s2.equals("eu")) || ((s.equals("io") && s2.equals("com")) || ((s.equals("mc") && s2.equals("tm")) || ((s.equals("mc") && s2.equals("asso")) || (s.equals("vc") && s2.equals("com")))))))))))) {
                return cname.substring(third + 1);
            }
            return cname.substring(second + 1);
        } else if (top4Set.contains(s)) {
            if (s2.equals("com") || s2.equals("edu") || s2.equals("gov") || s2.equals("net") || s2.equals("org")) {
                return cname.substring(third + 1);
            }
            return cname.substring(second + 1);
        } else if (top5Set.contains(s)) {
            return cname.substring(third + 1);
        }
        if (s.equals("tr")) {
            if (s2.equals("nic") || s2.equals("tsk")) {
                return cname.substring(second + 1);
            }
            return cname.substring(third + 1);
        } else if (s.equals("uk")) {
            if (ukSet.contains(s2)) {
                return cname.substring(second + 1);
            }
            return cname.substring(third + 1);
        } else if (s.equals("ar")) {
            if (arSet.contains(s2)) {
                return cname.substring(second + 1);
            }
            return cname.substring(third + 1);
        } else if (s.equals("om")) {
            if (omSet.contains(s2)) {
                return cname.substring(second + 1);
            }
            return cname.substring(third + 1);
        } else if (top1Set.contains(s)) {
            return cname.substring(second + 1);
        } else {
            return cname;
        }
    }
}
