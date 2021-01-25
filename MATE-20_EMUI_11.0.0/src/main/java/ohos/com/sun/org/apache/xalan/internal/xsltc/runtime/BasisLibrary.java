package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.AbsoluteIterator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.ArrayNodeListIterator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.MultiDOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SingletonIterator;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.StepIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.xml.sax.SAXException;

public final class BasisLibrary {
    public static final String AXIS_SUPPORT_ERR = "AXIS_SUPPORT_ERR";
    public static final String CANT_RESOLVE_RELATIVE_URI_ERR = "CANT_RESOLVE_RELATIVE_URI_ERR";
    public static final String DATA_CONVERSION_ERR = "DATA_CONVERSION_ERR";
    public static final String DOM_ADAPTER_INIT_ERR = "DOM_ADAPTER_INIT_ERR";
    private static final int DOUBLE_FRACTION_DIGITS = 340;
    private static final String EMPTYSTRING = "";
    public static final String EQUALITY_EXPR_ERR = "EQUALITY_EXPR_ERR";
    public static final String ERROR_MESSAGES_KEY = "error-messages";
    public static final String EXTERNAL_FUNC_ERR = "EXTERNAL_FUNC_ERR";
    public static final String FORMAT_NUMBER_ERR = "FORMAT_NUMBER_ERR";
    public static final String INVALID_ARGUMENT_ERR = "INVALID_ARGUMENT_ERR";
    public static final String INVALID_NCNAME_ERR = "INVALID_NCNAME_ERR";
    public static final String INVALID_QNAME_ERR = "INVALID_QNAME_ERR";
    public static final String ITERATOR_CLONE_ERR = "ITERATOR_CLONE_ERR";
    public static final String NAMESPACES_SUPPORT_ERR = "NAMESPACES_SUPPORT_ERR";
    public static final String NAMESPACE_PREFIX_ERR = "NAMESPACE_PREFIX_ERR";
    public static final String PARSER_DTD_SUPPORT_ERR = "PARSER_DTD_SUPPORT_ERR";
    public static final String RUN_TIME_COPY_ERR = "RUN_TIME_COPY_ERR";
    public static final String RUN_TIME_INTERNAL_ERR = "RUN_TIME_INTERNAL_ERR";
    public static final String STRAY_ATTRIBUTE_ERR = "STRAY_ATTRIBUTE_ERR";
    public static final String STRAY_NAMESPACE_ERR = "STRAY_NAMESPACE_ERR";
    public static final String TYPED_AXIS_SUPPORT_ERR = "TYPED_AXIS_SUPPORT_ERR";
    public static final String UNALLOWED_EXTENSION_ELEMENT_ERR = "UNALLOWED_EXTENSION_ELEMENT_ERR";
    public static final String UNALLOWED_EXTENSION_FUNCTION_ERR = "UNALLOWED_EXTENSION_FUNCTION_ERR";
    public static final String UNKNOWN_TRANSLET_VERSION_ERR = "UNKNOWN_TRANSLET_VERSION_ERR";
    public static final String UNSUPPORTED_EXT_ERR = "UNSUPPORTED_EXT_ERR";
    public static final String UNSUPPORTED_XSL_ERR = "UNSUPPORTED_XSL_ERR";
    private static char[] _characterArray = new char[32];
    private static FieldPosition _fieldPosition = new FieldPosition(0);
    private static DecimalFormat defaultFormatter = null;
    private static String defaultPattern = "";
    private static final double lowerBounds = 0.001d;
    private static ResourceBundle m_bundle = SecuritySupport.getResourceBundle("ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.ErrorMessages");
    private static final ThreadLocal<AtomicInteger> threadLocalPrefixIndex = new ThreadLocal<AtomicInteger>() {
        /* class ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.AnonymousClass4 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };
    private static final ThreadLocal<StringBuffer> threadLocalStringBuffer = new ThreadLocal<StringBuffer>() {
        /* class ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.AnonymousClass2 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public StringBuffer initialValue() {
            return new StringBuffer();
        }
    };
    private static final ThreadLocal<StringBuilder> threadLocalStringBuilder = new ThreadLocal<StringBuilder>() {
        /* class ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public StringBuilder initialValue() {
            return new StringBuilder();
        }
    };
    private static final double upperBounds = 1.0E7d;
    private static DecimalFormat xpathFormatter = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));

    private static boolean isWhiteSpace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    public static int realToInt(double d) {
        return (int) d;
    }

    static {
        NumberFormat instance = NumberFormat.getInstance(Locale.getDefault());
        defaultFormatter = instance instanceof DecimalFormat ? (DecimalFormat) instance : new DecimalFormat();
        defaultFormatter.setMaximumFractionDigits(340);
        defaultFormatter.setMinimumFractionDigits(0);
        defaultFormatter.setMinimumIntegerDigits(1);
        defaultFormatter.setGroupingUsed(false);
        xpathFormatter.setMaximumFractionDigits(340);
        xpathFormatter.setMinimumFractionDigits(0);
        xpathFormatter.setMinimumIntegerDigits(1);
        xpathFormatter.setGroupingUsed(false);
    }

    public static int countF(DTMAxisIterator dTMAxisIterator) {
        return dTMAxisIterator.getLast();
    }

    public static int positionF(DTMAxisIterator dTMAxisIterator) {
        if (dTMAxisIterator.isReverse()) {
            return (dTMAxisIterator.getLast() - dTMAxisIterator.getPosition()) + 1;
        }
        return dTMAxisIterator.getPosition();
    }

    public static double sumF(DTMAxisIterator dTMAxisIterator, DOM dom) {
        double d = XPath.MATCH_SCORE_QNAME;
        while (true) {
            try {
                int next = dTMAxisIterator.next();
                if (next == -1) {
                    return d;
                }
                d += Double.parseDouble(dom.getStringValueX(next));
            } catch (NumberFormatException unused) {
                return Double.NaN;
            }
        }
    }

    public static String stringF(int i, DOM dom) {
        return dom.getStringValueX(i);
    }

    public static String stringF(Object obj, DOM dom) {
        if (obj instanceof DTMAxisIterator) {
            return dom.getStringValueX(((DTMAxisIterator) obj).reset().next());
        }
        if (obj instanceof Node) {
            return dom.getStringValueX(((Node) obj).node);
        }
        if (obj instanceof DOM) {
            return ((DOM) obj).getStringValue();
        }
        return obj.toString();
    }

    public static String stringF(Object obj, int i, DOM dom) {
        if (obj instanceof DTMAxisIterator) {
            return dom.getStringValueX(((DTMAxisIterator) obj).reset().next());
        }
        if (obj instanceof Node) {
            return dom.getStringValueX(((Node) obj).node);
        }
        if (obj instanceof DOM) {
            return ((DOM) obj).getStringValue();
        }
        if (!(obj instanceof Double)) {
            return obj != null ? obj.toString() : "";
        }
        String d = ((Double) obj).toString();
        int length = d.length();
        int i2 = length - 2;
        return (d.charAt(i2) == '.' && d.charAt(length + -1) == '0') ? d.substring(0, i2) : d;
    }

    public static double numberF(int i, DOM dom) {
        return stringToReal(dom.getStringValueX(i));
    }

    public static double numberF(Object obj, DOM dom) {
        if (obj instanceof Double) {
            return ((Double) obj).doubleValue();
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        }
        if (obj instanceof Boolean) {
            if (((Boolean) obj).booleanValue()) {
                return 1.0d;
            }
            return XPath.MATCH_SCORE_QNAME;
        } else if (obj instanceof String) {
            return stringToReal((String) obj);
        } else {
            if (obj instanceof DTMAxisIterator) {
                return stringToReal(dom.getStringValueX(((DTMAxisIterator) obj).reset().next()));
            }
            if (obj instanceof Node) {
                return stringToReal(dom.getStringValueX(((Node) obj).node));
            }
            if (obj instanceof DOM) {
                return stringToReal(((DOM) obj).getStringValue());
            }
            runTimeError(INVALID_ARGUMENT_ERR, obj.getClass().getName(), "number()");
            return XPath.MATCH_SCORE_QNAME;
        }
    }

    public static double roundF(double d) {
        int i;
        if (d < -0.5d || d > XPath.MATCH_SCORE_QNAME) {
            return Math.floor(d + 0.5d);
        }
        if (i == 0) {
            return d;
        }
        return Double.isNaN(d) ? Double.NaN : -0.0d;
    }

    public static boolean booleanF(Object obj) {
        if (obj instanceof Double) {
            double doubleValue = ((Double) obj).doubleValue();
            return doubleValue != XPath.MATCH_SCORE_QNAME && !Double.isNaN(doubleValue);
        } else if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue() != XPath.MATCH_SCORE_QNAME;
        } else {
            if (obj instanceof Boolean) {
                return ((Boolean) obj).booleanValue();
            }
            if (obj instanceof String) {
                return !((String) obj).equals("");
            }
            if (obj instanceof DTMAxisIterator) {
                return ((DTMAxisIterator) obj).reset().next() != -1;
            }
            if (obj instanceof Node) {
                return true;
            }
            if (obj instanceof DOM) {
                return !((DOM) obj).getStringValue().equals("");
            }
            runTimeError(INVALID_ARGUMENT_ERR, obj.getClass().getName(), "boolean()");
            return false;
        }
    }

    public static String substringF(String str, double d) {
        int round;
        if (Double.isNaN(d) || (round = ((int) Math.round(d)) - 1) > getStringLength(str)) {
            return "";
        }
        if (round < 1) {
            round = 0;
        }
        try {
            return str.substring(str.offsetByCodePoints(0, round));
        } catch (IndexOutOfBoundsException unused) {
            runTimeError(RUN_TIME_INTERNAL_ERR, "substring()");
            return null;
        }
    }

    public static String substringF(String str, double d, double d2) {
        if (!Double.isInfinite(d) && !Double.isNaN(d) && !Double.isNaN(d2) && d2 >= XPath.MATCH_SCORE_QNAME) {
            int round = ((int) Math.round(d)) - 1;
            int round2 = (int) Math.round(d2);
            int i = Double.isInfinite(d2) ? Integer.MAX_VALUE : round + round2;
            int stringLength = getStringLength(str);
            if (i >= 0 && round <= stringLength) {
                if (round < 0) {
                    round2 += round;
                    round = 0;
                }
                try {
                    int offsetByCodePoints = str.offsetByCodePoints(0, round);
                    if (i > stringLength) {
                        return str.substring(offsetByCodePoints);
                    }
                    return str.substring(offsetByCodePoints, str.offsetByCodePoints(offsetByCodePoints, round2));
                } catch (IndexOutOfBoundsException unused) {
                    runTimeError(RUN_TIME_INTERNAL_ERR, "substring()");
                    return null;
                }
            }
        }
        return "";
    }

    public static String substring_afterF(String str, String str2) {
        int indexOf = str.indexOf(str2);
        return indexOf >= 0 ? str.substring(indexOf + str2.length()) : "";
    }

    public static String substring_beforeF(String str, String str2) {
        int indexOf = str.indexOf(str2);
        return indexOf >= 0 ? str.substring(0, indexOf) : "";
    }

    public static String translateF(String str, String str2, String str3) {
        int length = str3.length();
        int length2 = str2.length();
        int length3 = str.length();
        StringBuilder sb = threadLocalStringBuilder.get();
        sb.setLength(0);
        for (int i = 0; i < length3; i++) {
            char charAt = str.charAt(i);
            int i2 = 0;
            while (true) {
                if (i2 >= length2) {
                    break;
                } else if (charAt != str2.charAt(i2)) {
                    i2++;
                } else if (i2 < length) {
                    sb.append(str3.charAt(i2));
                }
            }
            if (i2 == length2) {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }

    public static String normalize_spaceF(int i, DOM dom) {
        return normalize_spaceF(dom.getStringValueX(i));
    }

    public static String normalize_spaceF(String str) {
        int length = str.length();
        StringBuilder sb = threadLocalStringBuilder.get();
        int i = 0;
        sb.setLength(0);
        while (i < length && isWhiteSpace(str.charAt(i))) {
            i++;
        }
        while (true) {
            if (i < length && !isWhiteSpace(str.charAt(i))) {
                sb.append(str.charAt(i));
                i++;
            } else if (i == length) {
                return sb.toString();
            } else {
                while (i < length && isWhiteSpace(str.charAt(i))) {
                    i++;
                }
                if (i < length) {
                    sb.append(' ');
                }
            }
        }
    }

    public static String generate_idF(int i) {
        if (i <= 0) {
            return "";
        }
        return "N" + i;
    }

    public static String getLocalName(String str) {
        int lastIndexOf = str.lastIndexOf(58);
        if (lastIndexOf >= 0) {
            str = str.substring(lastIndexOf + 1);
        }
        int lastIndexOf2 = str.lastIndexOf(64);
        return lastIndexOf2 >= 0 ? str.substring(lastIndexOf2 + 1) : str;
    }

    public static void unresolved_externalF(String str) {
        runTimeError(EXTERNAL_FUNC_ERR, str);
    }

    public static void unallowed_extension_functionF(String str) {
        runTimeError(UNALLOWED_EXTENSION_FUNCTION_ERR, str);
    }

    public static void unallowed_extension_elementF(String str) {
        runTimeError(UNALLOWED_EXTENSION_ELEMENT_ERR, str);
    }

    public static void unsupported_ElementF(String str, boolean z) {
        if (z) {
            runTimeError("UNSUPPORTED_EXT_ERR", str);
        } else {
            runTimeError("UNSUPPORTED_XSL_ERR", str);
        }
    }

    public static String namespace_uriF(DTMAxisIterator dTMAxisIterator, DOM dom) {
        return namespace_uriF(dTMAxisIterator.next(), dom);
    }

    public static String system_propertyF(String str) {
        if (str.equals("xsl:version")) {
            return "1.0";
        }
        if (str.equals("xsl:vendor")) {
            return "Apache Software Foundation (Xalan XSLTC)";
        }
        if (str.equals("xsl:vendor-url")) {
            return "http://xml.apache.org/xalan-j";
        }
        runTimeError(INVALID_ARGUMENT_ERR, str, "system-property()");
        return "";
    }

    public static String namespace_uriF(int i, DOM dom) {
        String nodeName = dom.getNodeName(i);
        int lastIndexOf = nodeName.lastIndexOf(58);
        return lastIndexOf >= 0 ? nodeName.substring(0, lastIndexOf) : "";
    }

    public static String objectTypeF(Object obj) {
        if (obj instanceof String) {
            return "string";
        }
        if (obj instanceof Boolean) {
            return "boolean";
        }
        if (obj instanceof Number) {
            return "number";
        }
        if (obj instanceof DOM) {
            return "RTF";
        }
        return obj instanceof DTMAxisIterator ? "node-set" : "unknown";
    }

    public static DTMAxisIterator nodesetF(Object obj) {
        if (obj instanceof DOM) {
            return new SingletonIterator(((DOM) obj).getDocument(), true);
        }
        if (obj instanceof DTMAxisIterator) {
            return (DTMAxisIterator) obj;
        }
        runTimeError("DATA_CONVERSION_ERR", "node-set", obj.getClass().getName());
        return null;
    }

    private static boolean compareStrings(String str, String str2, int i, DOM dom) {
        if (i == 0) {
            return str.equals(str2);
        }
        if (i == 1) {
            return !str.equals(str2);
        }
        if (i == 2) {
            return numberF(str, dom) > numberF(str2, dom);
        }
        if (i == 3) {
            return numberF(str, dom) < numberF(str2, dom);
        }
        if (i == 4) {
            return numberF(str, dom) >= numberF(str2, dom);
        }
        if (i == 5) {
            return numberF(str, dom) <= numberF(str2, dom);
        }
        runTimeError(RUN_TIME_INTERNAL_ERR, "compare()");
        return false;
    }

    public static boolean compare(DTMAxisIterator dTMAxisIterator, DTMAxisIterator dTMAxisIterator2, int i, DOM dom) {
        dTMAxisIterator.reset();
        while (true) {
            int next = dTMAxisIterator.next();
            if (next == -1) {
                return false;
            }
            String stringValueX = dom.getStringValueX(next);
            dTMAxisIterator2.reset();
            while (true) {
                int next2 = dTMAxisIterator2.next();
                if (next2 != -1) {
                    if (next == next2) {
                        if (i == 0) {
                            return true;
                        }
                        if (i == 1) {
                            continue;
                        }
                    }
                    if (compareStrings(stringValueX, dom.getStringValueX(next2), i, dom)) {
                        return true;
                    }
                }
            }
        }
    }

    public static boolean compare(int i, DTMAxisIterator dTMAxisIterator, int i2, DOM dom) {
        int next;
        int next2;
        if (i2 == 0) {
            int next3 = dTMAxisIterator.next();
            if (next3 == -1) {
                return false;
            }
            String stringValueX = dom.getStringValueX(i);
            while (i != next3 && !stringValueX.equals(dom.getStringValueX(next3))) {
                next3 = dTMAxisIterator.next();
                if (next3 == -1) {
                    return false;
                }
            }
            return true;
        } else if (i2 == 1) {
            int next4 = dTMAxisIterator.next();
            if (next4 == -1) {
                return false;
            }
            String stringValueX2 = dom.getStringValueX(i);
            do {
                if (i != next4 && !stringValueX2.equals(dom.getStringValueX(next4))) {
                    return true;
                }
                next4 = dTMAxisIterator.next();
            } while (next4 != -1);
            return false;
        } else if (i2 == 2) {
            do {
                next = dTMAxisIterator.next();
                if (next == -1) {
                    return false;
                }
            } while (next >= i);
            return true;
        } else if (i2 != 3) {
            return false;
        } else {
            do {
                next2 = dTMAxisIterator.next();
                if (next2 == -1) {
                    return false;
                }
            } while (next2 <= i);
            return true;
        }
    }

    public static boolean compare(DTMAxisIterator dTMAxisIterator, double d, int i, DOM dom) {
        int next;
        int next2;
        int next3;
        int next4;
        int next5;
        int next6;
        if (i == 0) {
            do {
                next = dTMAxisIterator.next();
                if (next == -1) {
                    return false;
                }
            } while (numberF(dom.getStringValueX(next), dom) != d);
            return true;
        } else if (i == 1) {
            do {
                next2 = dTMAxisIterator.next();
                if (next2 == -1) {
                    return false;
                }
            } while (numberF(dom.getStringValueX(next2), dom) == d);
            return true;
        } else if (i == 2) {
            do {
                next3 = dTMAxisIterator.next();
                if (next3 == -1) {
                    return false;
                }
            } while (numberF(dom.getStringValueX(next3), dom) <= d);
            return true;
        } else if (i == 3) {
            do {
                next4 = dTMAxisIterator.next();
                if (next4 == -1) {
                    return false;
                }
            } while (numberF(dom.getStringValueX(next4), dom) >= d);
            return true;
        } else if (i == 4) {
            do {
                next5 = dTMAxisIterator.next();
                if (next5 == -1) {
                    return false;
                }
            } while (numberF(dom.getStringValueX(next5), dom) < d);
            return true;
        } else if (i != 5) {
            runTimeError(RUN_TIME_INTERNAL_ERR, "compare()");
            return false;
        } else {
            do {
                next6 = dTMAxisIterator.next();
                if (next6 == -1) {
                    return false;
                }
            } while (numberF(dom.getStringValueX(next6), dom) > d);
            return true;
        }
    }

    public static boolean compare(DTMAxisIterator dTMAxisIterator, String str, int i, DOM dom) {
        int next;
        do {
            next = dTMAxisIterator.next();
            if (next == -1) {
                return false;
            }
        } while (!compareStrings(dom.getStringValueX(next), str, i, dom));
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00bc, code lost:
        if (numberF(r6, r9) == numberF(r7, r9)) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c0, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00ca, code lost:
        if (booleanF(r6) == booleanF(r7)) goto L_0x00be;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0156  */
    /* JADX WARNING: Removed duplicated region for block: B:137:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00d4  */
    public static boolean compare(Object obj, Object obj2, int i, DOM dom) {
        boolean z;
        boolean z2;
        boolean z3 = hasSimpleType(obj) && hasSimpleType(obj2);
        if (!(i == 0 || i == 1)) {
            if ((obj instanceof Node) || (obj2 instanceof Node)) {
                if (obj instanceof Boolean) {
                    obj2 = new Boolean(booleanF(obj2));
                    z3 = true;
                }
                if (obj2 instanceof Boolean) {
                    obj = new Boolean(booleanF(obj));
                    z3 = true;
                }
            }
            if (z3) {
                if (i == 2) {
                    return numberF(obj, dom) > numberF(obj2, dom);
                }
                if (i == 3) {
                    return numberF(obj, dom) < numberF(obj2, dom);
                }
                if (i == 4) {
                    return numberF(obj, dom) >= numberF(obj2, dom);
                }
                if (i == 5) {
                    return numberF(obj, dom) <= numberF(obj2, dom);
                }
                runTimeError(RUN_TIME_INTERNAL_ERR, "compare()");
            }
        }
        if (z3) {
            if (!(obj instanceof Boolean) && !(obj2 instanceof Boolean)) {
                if (!(obj instanceof Double) && !(obj2 instanceof Double) && !(obj instanceof Integer) && !(obj2 instanceof Integer)) {
                    z2 = stringF(obj, dom).equals(stringF(obj2, dom));
                    if (i == 1) {
                        return z2;
                    }
                    if (z2) {
                        return false;
                    }
                }
            }
            z2 = true;
            if (i == 1) {
            }
        } else {
            if (obj instanceof Node) {
                obj = new SingletonIterator(((Node) obj).node);
            }
            if (obj2 instanceof Node) {
                obj2 = new SingletonIterator(((Node) obj2).node);
            }
            if (hasSimpleType(obj) || ((obj instanceof DOM) && (obj2 instanceof DTMAxisIterator))) {
                i = Operators.swapOp(i);
                obj2 = obj;
                obj = obj2;
            }
            if (!(obj instanceof DOM)) {
                DTMAxisIterator reset = ((DTMAxisIterator) obj).reset();
                if (obj2 instanceof DTMAxisIterator) {
                    return compare(reset, (DTMAxisIterator) obj2, i, dom);
                }
                if (obj2 instanceof String) {
                    return compare(reset, (String) obj2, i, dom);
                }
                if (obj2 instanceof Number) {
                    return compare(reset, ((Number) obj2).doubleValue(), i, dom);
                }
                if (obj2 instanceof Boolean) {
                    if ((reset.reset().next() != -1) != ((Boolean) obj2).booleanValue()) {
                        return false;
                    }
                } else if (obj2 instanceof DOM) {
                    return compare(reset, ((DOM) obj2).getStringValue(), i, dom);
                } else {
                    if (obj2 == null) {
                        return false;
                    }
                    runTimeError(INVALID_ARGUMENT_ERR, obj2.getClass().getName(), "compare()");
                    return false;
                }
            } else if (obj2 instanceof Boolean) {
                return ((Boolean) obj2).booleanValue() == (i == 0);
            } else {
                String stringValue = ((DOM) obj).getStringValue();
                if (!(obj2 instanceof Number)) {
                    if (obj2 instanceof String) {
                        z = stringValue.equals((String) obj2);
                    } else if (obj2 instanceof DOM) {
                        z = stringValue.equals(((DOM) obj2).getStringValue());
                    }
                    if (i == 1) {
                    }
                } else if (((Number) obj2).doubleValue() == stringToReal(stringValue)) {
                    z = true;
                    if (i == 1) {
                        return !z;
                    }
                    return z;
                }
                z = false;
                if (i == 1) {
                }
            }
        }
        return true;
    }

    public static boolean testLanguage(String str, DOM dom, int i) {
        String language = dom.getLanguage(i);
        if (language == null) {
            return false;
        }
        String lowerCase = language.toLowerCase();
        String lowerCase2 = str.toLowerCase();
        if (lowerCase2.length() == 2) {
            return lowerCase.startsWith(lowerCase2);
        }
        return lowerCase.equals(lowerCase2);
    }

    private static boolean hasSimpleType(Object obj) {
        return (obj instanceof Boolean) || (obj instanceof Double) || (obj instanceof Integer) || (obj instanceof String) || (obj instanceof Node) || (obj instanceof DOM);
    }

    public static double stringToReal(String str) {
        try {
            return Double.valueOf(str).doubleValue();
        } catch (NumberFormatException unused) {
            return Double.NaN;
        }
    }

    public static int stringToInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException unused) {
            return -1;
        }
    }

    public static String realToString(double d) {
        double abs = Math.abs(d);
        if (abs >= lowerBounds && abs < upperBounds) {
            String d2 = Double.toString(d);
            int length = d2.length();
            int i = length - 2;
            return (d2.charAt(i) == '.' && d2.charAt(length + -1) == '0') ? d2.substring(0, i) : d2;
        } else if (Double.isNaN(d) || Double.isInfinite(d)) {
            return Double.toString(d);
        } else {
            double d3 = d + XPath.MATCH_SCORE_QNAME;
            StringBuffer stringBuffer = threadLocalStringBuffer.get();
            stringBuffer.setLength(0);
            xpathFormatter.format(d3, stringBuffer, _fieldPosition);
            return stringBuffer.toString();
        }
    }

    public static String formatNumber(double d, String str, DecimalFormat decimalFormat) {
        if (decimalFormat == null) {
            decimalFormat = defaultFormatter;
        }
        try {
            StringBuffer stringBuffer = threadLocalStringBuffer.get();
            stringBuffer.setLength(0);
            if (str != defaultPattern) {
                decimalFormat.applyLocalizedPattern(str);
            }
            decimalFormat.format(d, stringBuffer, _fieldPosition);
            return stringBuffer.toString();
        } catch (IllegalArgumentException unused) {
            runTimeError(FORMAT_NUMBER_ERR, Double.toString(d), str);
            return "";
        }
    }

    public static DTMAxisIterator referenceToNodeSet(Object obj) {
        if (obj instanceof Node) {
            return new SingletonIterator(((Node) obj).node);
        }
        if (obj instanceof DTMAxisIterator) {
            return ((DTMAxisIterator) obj).cloneIterator().reset();
        }
        runTimeError("DATA_CONVERSION_ERR", obj.getClass().getName(), "node-set");
        return null;
    }

    public static NodeList referenceToNodeList(Object obj, DOM dom) {
        if ((obj instanceof Node) || (obj instanceof DTMAxisIterator)) {
            return dom.makeNodeList(referenceToNodeSet(obj));
        }
        if (obj instanceof DOM) {
            return ((DOM) obj).makeNodeList(0);
        }
        runTimeError("DATA_CONVERSION_ERR", obj.getClass().getName(), "ohos.org.w3c.dom.NodeList");
        return null;
    }

    public static Node referenceToNode(Object obj, DOM dom) {
        if ((obj instanceof Node) || (obj instanceof DTMAxisIterator)) {
            return dom.makeNode(referenceToNodeSet(obj));
        }
        if (obj instanceof DOM) {
            DOM dom2 = (DOM) obj;
            return dom2.makeNode(dom2.getChildren(0));
        }
        runTimeError("DATA_CONVERSION_ERR", obj.getClass().getName(), "ohos.org.w3c.dom.Node");
        return null;
    }

    public static long referenceToLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        runTimeError("DATA_CONVERSION_ERR", obj.getClass().getName(), Long.TYPE);
        return 0;
    }

    public static double referenceToDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        runTimeError("DATA_CONVERSION_ERR", obj.getClass().getName(), Double.TYPE);
        return XPath.MATCH_SCORE_QNAME;
    }

    public static boolean referenceToBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        }
        runTimeError("DATA_CONVERSION_ERR", obj.getClass().getName(), Boolean.TYPE);
        return false;
    }

    public static String referenceToString(Object obj, DOM dom) {
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof DTMAxisIterator) {
            return dom.getStringValueX(((DTMAxisIterator) obj).reset().next());
        }
        if (obj instanceof Node) {
            return dom.getStringValueX(((Node) obj).node);
        }
        if (obj instanceof DOM) {
            return ((DOM) obj).getStringValue();
        }
        runTimeError("DATA_CONVERSION_ERR", obj.getClass().getName(), String.class);
        return null;
    }

    public static DTMAxisIterator node2Iterator(final Node node, Translet translet, DOM dom) {
        return nodeList2Iterator(new NodeList() {
            /* class ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary.AnonymousClass3 */

            public int getLength() {
                return 1;
            }

            public Node item(int i) {
                if (i == 0) {
                    return node;
                }
                return null;
            }
        }, translet, dom);
    }

    private static DTMAxisIterator nodeList2IteratorUsingHandleFromNode(NodeList nodeList, Translet translet, DOM dom) {
        int i;
        int length = nodeList.getLength();
        int[] iArr = new int[length];
        DTMManager dTMManager = dom instanceof MultiDOM ? ((MultiDOM) dom).getDTMManager() : null;
        for (int i2 = 0; i2 < length; i2++) {
            DTMNodeProxy item = nodeList.item(i2);
            if (dTMManager != null) {
                i = dTMManager.getDTMHandleFromNode(item);
            } else {
                if (item instanceof DTMNodeProxy) {
                    DTMNodeProxy dTMNodeProxy = item;
                    if (dTMNodeProxy.getDTM() == dom) {
                        i = dTMNodeProxy.getDTMNodeNumber();
                    }
                }
                runTimeError(RUN_TIME_INTERNAL_ERR, "need MultiDOM");
                return null;
            }
            iArr[i2] = i;
            PrintStream printStream = System.out;
            printStream.println("Node " + i2 + " has handle 0x" + Integer.toString(i, 16));
        }
        return new ArrayNodeListIterator(iArr);
    }

    public static DTMAxisIterator nodeList2Iterator(NodeList nodeList, Translet translet, DOM dom) {
        AbsoluteIterator absoluteIterator;
        DTMAxisIterator dTMAxisIterator;
        DTMAxisIterator dTMAxisIterator2;
        int[] iArr = new int[nodeList.getLength()];
        boolean z = dom instanceof MultiDOM;
        DTMAxisIterator dTMAxisIterator3 = null;
        DTMManager dTMManager = z ? ((MultiDOM) dom).getDTMManager() : null;
        Document document = null;
        int i = 0;
        for (int i2 = 0; i2 < nodeList.getLength(); i2++) {
            DTMNodeProxy item = nodeList.item(i2);
            if (item instanceof DTMNodeProxy) {
                DTMNodeProxy dTMNodeProxy = item;
                DTM dtm = dTMNodeProxy.getDTM();
                int dTMNodeNumber = dTMNodeProxy.getDTMNodeNumber();
                boolean z2 = dtm == dom;
                if (!z2 && dTMManager != null) {
                    try {
                        z2 = dtm == dTMManager.getDTM(dTMNodeNumber);
                    } catch (ArrayIndexOutOfBoundsException unused) {
                    }
                }
                if (z2) {
                    iArr[i2] = dTMNodeNumber;
                    i++;
                }
            }
            iArr[i2] = -1;
            short nodeType = item.getNodeType();
            if (document == null) {
                if (!z) {
                    runTimeError(RUN_TIME_INTERNAL_ERR, "need MultiDOM");
                    return null;
                }
                try {
                    document = ((AbstractTranslet) translet).newDocument("", "__top__");
                } catch (ParserConfigurationException e) {
                    runTimeError(RUN_TIME_INTERNAL_ERR, e.getMessage());
                    return null;
                }
            }
            switch (nodeType) {
                case 1:
                case 3:
                case 4:
                case 5:
                case 7:
                case 8:
                    Element createElementNS = document.createElementNS((String) null, "__dummy__");
                    createElementNS.appendChild(document.importNode(item, true));
                    document.getDocumentElement().appendChild(createElementNS);
                    break;
                case 2:
                    Element createElementNS2 = document.createElementNS((String) null, "__dummy__");
                    createElementNS2.setAttributeNodeNS(document.importNode(item, true));
                    document.getDocumentElement().appendChild(createElementNS2);
                    break;
                case 6:
                default:
                    runTimeError(RUN_TIME_INTERNAL_ERR, "Don't know how to convert node type " + ((int) nodeType));
                    continue;
            }
            i++;
        }
        if (document != null) {
            DOM dom2 = (DOM) dTMManager.getDTM(new DOMSource(document), false, null, true, false);
            ((MultiDOM) dom).addDOMAdapter(new DOMAdapter(dom2, translet.getNamesArray(), translet.getUrisArray(), translet.getTypesArray(), translet.getNamespaceArray()));
            absoluteIterator = new AbsoluteIterator(new StepIterator(dom2.getAxisIterator(3), dom2.getAxisIterator(3)));
            absoluteIterator.setStartNode(0);
            dTMAxisIterator3 = dom2.getAxisIterator(3);
            dTMAxisIterator = dom2.getAxisIterator(2);
        } else {
            dTMAxisIterator = null;
            absoluteIterator = null;
        }
        int[] iArr2 = new int[i];
        int i3 = 0;
        for (int i4 = 0; i4 < nodeList.getLength(); i4++) {
            if (iArr[i4] != -1) {
                iArr2[i3] = iArr[i4];
                i3++;
            } else {
                switch (nodeList.item(i4).getNodeType()) {
                    case 1:
                    case 3:
                    case 4:
                    case 5:
                    case 7:
                    case 8:
                        dTMAxisIterator2 = dTMAxisIterator3;
                        break;
                    case 2:
                        dTMAxisIterator2 = dTMAxisIterator;
                        break;
                    case 6:
                    default:
                        throw new InternalRuntimeError("Mismatched cases");
                }
                if (dTMAxisIterator2 != null) {
                    dTMAxisIterator2.setStartNode(absoluteIterator.next());
                    iArr2[i3] = dTMAxisIterator2.next();
                    if (iArr2[i3] == -1) {
                        throw new InternalRuntimeError("Expected element missing at " + i4);
                    } else if (dTMAxisIterator2.next() == -1) {
                        i3++;
                    } else {
                        throw new InternalRuntimeError("Too many elements at " + i4);
                    }
                } else {
                    continue;
                }
            }
        }
        if (i3 == iArr2.length) {
            return new ArrayNodeListIterator(iArr2);
        }
        throw new InternalRuntimeError("Nodes lost in second pass");
    }

    public static DOM referenceToResultTree(Object obj) {
        try {
            return (DOM) obj;
        } catch (IllegalArgumentException unused) {
            runTimeError("DATA_CONVERSION_ERR", "reference", obj.getClass().getName());
            return null;
        }
    }

    public static DTMAxisIterator getSingleNode(DTMAxisIterator dTMAxisIterator) {
        return new SingletonIterator(dTMAxisIterator.next());
    }

    public static void copy(Object obj, SerializationHandler serializationHandler, int i, DOM dom) {
        try {
            if (obj instanceof DTMAxisIterator) {
                dom.copy(((DTMAxisIterator) obj).reset(), serializationHandler);
            } else if (obj instanceof Node) {
                dom.copy(((Node) obj).node, serializationHandler);
            } else if (obj instanceof DOM) {
                DOM dom2 = (DOM) obj;
                dom2.copy(dom2.getDocument(), serializationHandler);
            } else {
                String obj2 = obj.toString();
                int length = obj2.length();
                if (length > _characterArray.length) {
                    _characterArray = new char[length];
                }
                obj2.getChars(0, length, _characterArray, 0);
                serializationHandler.characters(_characterArray, 0, length);
            }
        } catch (SAXException unused) {
            runTimeError(RUN_TIME_COPY_ERR);
        }
    }

    public static void checkAttribQName(String str) {
        int indexOf = str.indexOf(58);
        int lastIndexOf = str.lastIndexOf(58);
        String substring = str.substring(lastIndexOf + 1);
        if (indexOf > 0) {
            String substring2 = str.substring(0, indexOf);
            if (indexOf != lastIndexOf) {
                String substring3 = str.substring(indexOf + 1, lastIndexOf);
                if (!XML11Char.isXML11ValidNCName(substring3)) {
                    runTimeError("INVALID_QNAME_ERR", substring3 + ":" + substring);
                }
            }
            if (!XML11Char.isXML11ValidNCName(substring2)) {
                runTimeError("INVALID_QNAME_ERR", substring2 + ":" + substring);
            }
        }
        if (!XML11Char.isXML11ValidNCName(substring) || substring.equals("xmlns")) {
            runTimeError("INVALID_QNAME_ERR", substring);
        }
    }

    public static void checkNCName(String str) {
        if (!XML11Char.isXML11ValidNCName(str)) {
            runTimeError("INVALID_NCNAME_ERR", str);
        }
    }

    public static void checkQName(String str) {
        if (!XML11Char.isXML11ValidQName(str)) {
            runTimeError("INVALID_QNAME_ERR", str);
        }
    }

    public static String startXslElement(String str, String str2, SerializationHandler serializationHandler, DOM dom, int i) {
        try {
            int indexOf = str.indexOf(58);
            if (indexOf > 0) {
                String substring = str.substring(0, indexOf);
                if (str2 == null || str2.length() == 0) {
                    try {
                        str2 = dom.lookupNamespace(i, substring);
                    } catch (RuntimeException unused) {
                        serializationHandler.flushPending();
                        str2 = serializationHandler.getNamespaceMappings().lookupNamespace(substring);
                        if (str2 == null) {
                            runTimeError(NAMESPACE_PREFIX_ERR, substring);
                        }
                    }
                }
                serializationHandler.startElement(str2, str.substring(indexOf + 1), str);
                serializationHandler.namespaceAfterStartElement(substring, str2);
                return str;
            } else if (str2 == null || str2.length() <= 0) {
                serializationHandler.startElement(null, null, str);
                return str;
            } else {
                String generatePrefix = generatePrefix();
                String str3 = generatePrefix + ':' + str;
                serializationHandler.startElement(str2, str3, str3);
                serializationHandler.namespaceAfterStartElement(generatePrefix, str2);
                return str3;
            }
        } catch (SAXException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getPrefix(String str) {
        int indexOf = str.indexOf(58);
        if (indexOf > 0) {
            return str.substring(0, indexOf);
        }
        return null;
    }

    public static String generatePrefix() {
        return Constants.ATTRNAME_NS + threadLocalPrefixIndex.get().getAndIncrement();
    }

    public static void resetPrefixIndex() {
        threadLocalPrefixIndex.get().set(0);
    }

    public static void runTimeError(String str) {
        throw new RuntimeException(m_bundle.getString(str));
    }

    public static void runTimeError(String str, Object[] objArr) {
        throw new RuntimeException(MessageFormat.format(m_bundle.getString(str), objArr));
    }

    public static void runTimeError(String str, Object obj) {
        runTimeError(str, new Object[]{obj});
    }

    public static void runTimeError(String str, Object obj, Object obj2) {
        runTimeError(str, new Object[]{obj, obj2});
    }

    public static void consoleOutput(String str) {
        System.out.println(str);
    }

    public static String replace(String str, char c, String str2) {
        return str.indexOf(c) < 0 ? str : replace(str, String.valueOf(c), new String[]{str2});
    }

    public static String replace(String str, String str2, String[] strArr) {
        int length = str.length();
        StringBuilder sb = threadLocalStringBuilder.get();
        sb.setLength(0);
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            int indexOf = str2.indexOf(charAt);
            if (indexOf >= 0) {
                sb.append(strArr[indexOf]);
            } else {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }

    public static String mapQNameToJavaName(String str) {
        return replace(str, ".-:/{}?#%*", new String[]{"$dot$", "$dash$", "$colon$", "$slash$", "", "$colon$", "$ques$", "$hash$", "$per$", "$aster$"});
    }

    public static int getStringLength(String str) {
        return str.codePointCount(0, str.length());
    }
}
