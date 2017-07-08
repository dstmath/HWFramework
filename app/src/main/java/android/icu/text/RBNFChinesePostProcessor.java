package android.icu.text;

import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

final class RBNFChinesePostProcessor implements RBNFPostProcessor {
    private static final String[] rulesetNames = null;
    private int format;
    private boolean longForm;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.RBNFChinesePostProcessor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.RBNFChinesePostProcessor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.RBNFChinesePostProcessor.<clinit>():void");
    }

    RBNFChinesePostProcessor() {
    }

    public void init(RuleBasedNumberFormat formatter, String rules) {
    }

    public void process(StringBuffer buf, NFRuleSet ruleSet) {
        int n;
        int x;
        String ling;
        int m;
        int nn;
        int ns;
        String name = ruleSet.getName();
        int i = 0;
        while (i < rulesetNames.length) {
            String[] m2;
            int s;
            char c;
            if (rulesetNames[i].equals(name)) {
                this.format = i;
                boolean z = i == 1 || i == 3;
                this.longForm = z;
                if (this.longForm) {
                    String DIAN = "\u9ede";
                    markers = new String[3][];
                    markers[0] = new String[]{"\u842c", "\u5104", "\u5146", "\u3007"};
                    markers[1] = new String[]{"\u4e07", "\u4ebf", "\u5146", "\u3007"};
                    markers[2] = new String[]{"\u842c", "\u5104", "\u5146", "\u96f6"};
                    m2 = markers[this.format];
                    for (i = 0; i < m2.length - 1; i++) {
                        n = buf.indexOf(m2[i]);
                        if (n != -1) {
                            buf.insert(m2[i].length() + n, '|');
                        }
                    }
                    x = buf.indexOf("\u9ede");
                    if (x == -1) {
                        x = buf.length();
                    }
                    s = 0;
                    n = -1;
                    ling = markers[this.format][3];
                    while (x >= 0) {
                        m = buf.lastIndexOf("|", x);
                        nn = buf.lastIndexOf(ling, x);
                        ns = 0;
                        if (nn > m) {
                            if (nn > 0) {
                                if (buf.charAt(nn - 1) != '*') {
                                    ns = 2;
                                }
                            }
                            ns = 1;
                        }
                        x = m - 1;
                        switch ((s * 3) + ns) {
                            case XmlPullParser.START_DOCUMENT /*0*/:
                                s = ns;
                                n = -1;
                                break;
                            case NodeFilter.SHOW_ELEMENT /*1*/:
                                s = ns;
                                n = nn;
                                break;
                            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                                s = ns;
                                n = -1;
                                break;
                            case XmlPullParser.END_TAG /*3*/:
                                s = ns;
                                n = -1;
                                break;
                            case NodeFilter.SHOW_TEXT /*4*/:
                                buf.delete(nn - 1, ling.length() + nn);
                                s = 0;
                                n = -1;
                                break;
                            case XmlPullParser.CDSECT /*5*/:
                                buf.delete(n - 1, ling.length() + n);
                                s = ns;
                                n = -1;
                                break;
                            case XmlPullParser.ENTITY_REF /*6*/:
                                s = ns;
                                n = -1;
                                break;
                            case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                                buf.delete(nn - 1, ling.length() + nn);
                                s = 0;
                                n = -1;
                                break;
                            case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                                s = ns;
                                n = -1;
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                    }
                    i = buf.length();
                    while (true) {
                        i--;
                        if (i >= 0) {
                            c = buf.charAt(i);
                            if (c != '*' || c == '|') {
                                buf.delete(i, i + 1);
                            }
                        } else {
                            return;
                        }
                    }
                }
                i = buf.indexOf("*");
                while (i != -1) {
                    buf.delete(i, i + 1);
                    i = buf.indexOf("*", i);
                }
            }
            i++;
        }
        if (this.longForm) {
            String DIAN2 = "\u9ede";
            markers = new String[3][];
            markers[0] = new String[]{"\u842c", "\u5104", "\u5146", "\u3007"};
            markers[1] = new String[]{"\u4e07", "\u4ebf", "\u5146", "\u3007"};
            markers[2] = new String[]{"\u842c", "\u5104", "\u5146", "\u96f6"};
            m2 = markers[this.format];
            for (i = 0; i < m2.length - 1; i++) {
                n = buf.indexOf(m2[i]);
                if (n != -1) {
                    buf.insert(m2[i].length() + n, '|');
                }
            }
            x = buf.indexOf("\u9ede");
            if (x == -1) {
                x = buf.length();
            }
            s = 0;
            n = -1;
            ling = markers[this.format][3];
            while (x >= 0) {
                m = buf.lastIndexOf("|", x);
                nn = buf.lastIndexOf(ling, x);
                ns = 0;
                if (nn > m) {
                    if (nn > 0) {
                        if (buf.charAt(nn - 1) != '*') {
                            ns = 2;
                        }
                    }
                    ns = 1;
                }
                x = m - 1;
                switch ((s * 3) + ns) {
                    case XmlPullParser.START_DOCUMENT /*0*/:
                        s = ns;
                        n = -1;
                        break;
                    case NodeFilter.SHOW_ELEMENT /*1*/:
                        s = ns;
                        n = nn;
                        break;
                    case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                        s = ns;
                        n = -1;
                        break;
                    case XmlPullParser.END_TAG /*3*/:
                        s = ns;
                        n = -1;
                        break;
                    case NodeFilter.SHOW_TEXT /*4*/:
                        buf.delete(nn - 1, ling.length() + nn);
                        s = 0;
                        n = -1;
                        break;
                    case XmlPullParser.CDSECT /*5*/:
                        buf.delete(n - 1, ling.length() + n);
                        s = ns;
                        n = -1;
                        break;
                    case XmlPullParser.ENTITY_REF /*6*/:
                        s = ns;
                        n = -1;
                        break;
                    case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                        buf.delete(nn - 1, ling.length() + nn);
                        s = 0;
                        n = -1;
                        break;
                    case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                        s = ns;
                        n = -1;
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
            i = buf.length();
            while (true) {
                i--;
                if (i >= 0) {
                    c = buf.charAt(i);
                    if (c != '*') {
                    }
                    buf.delete(i, i + 1);
                } else {
                    return;
                }
            }
        }
        i = buf.indexOf("*");
        while (i != -1) {
            buf.delete(i, i + 1);
            i = buf.indexOf("*", i);
        }
    }
}
