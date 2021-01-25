package ohos.com.sun.org.apache.xalan.internal.xsltc.compiler.util;

import java.util.StringTokenizer;
import ohos.com.sun.org.apache.bcel.internal.generic.Type;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.utils.XML11Char;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public final class Util {
    private static char filesep = SecuritySupport.getSystemProperty("file.separator", PsuedoNames.PSEUDONAME_ROOT).charAt(0);

    public static String noExtName(String str) {
        int lastIndexOf = str.lastIndexOf(46);
        if (lastIndexOf < 0) {
            lastIndexOf = str.length();
        }
        return str.substring(0, lastIndexOf);
    }

    public static String baseName(String str) {
        int lastIndexOf = str.lastIndexOf(92);
        if (lastIndexOf < 0) {
            lastIndexOf = str.lastIndexOf(47);
        }
        if (lastIndexOf >= 0) {
            return str.substring(lastIndexOf + 1);
        }
        int lastIndexOf2 = str.lastIndexOf(58);
        return lastIndexOf2 > 0 ? str.substring(lastIndexOf2 + 1) : str;
    }

    public static String pathName(String str) {
        int lastIndexOf = str.lastIndexOf(47);
        if (lastIndexOf < 0) {
            lastIndexOf = str.lastIndexOf(92);
        }
        return str.substring(0, lastIndexOf + 1);
    }

    public static String toJavaName(String str) {
        if (str.length() <= 0) {
            return str;
        }
        StringBuffer stringBuffer = new StringBuffer();
        char charAt = str.charAt(0);
        if (!Character.isJavaIdentifierStart(charAt)) {
            charAt = '_';
        }
        stringBuffer.append(charAt);
        int length = str.length();
        for (int i = 1; i < length; i++) {
            char charAt2 = str.charAt(i);
            if (!Character.isJavaIdentifierPart(charAt2)) {
                charAt2 = '_';
            }
            stringBuffer.append(charAt2);
        }
        return stringBuffer.toString();
    }

    public static Type getJCRefType(String str) {
        return Type.getType(str);
    }

    public static String internalName(String str) {
        return str.replace('.', filesep);
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public static void println(char c) {
        System.out.println(c);
    }

    public static void TRACE1() {
        System.out.println("TRACE1");
    }

    public static void TRACE2() {
        System.out.println("TRACE2");
    }

    public static void TRACE3() {
        System.out.println("TRACE3");
    }

    public static String replace(String str, char c, String str2) {
        return str.indexOf(c) < 0 ? str : replace(str, String.valueOf(c), new String[]{str2});
    }

    public static String replace(String str, String str2, String[] strArr) {
        int length = str.length();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            int indexOf = str2.indexOf(charAt);
            if (indexOf >= 0) {
                stringBuffer.append(strArr[indexOf]);
            } else {
                stringBuffer.append(charAt);
            }
        }
        return stringBuffer.toString();
    }

    public static String escape(String str) {
        return replace(str, ".-/:", new String[]{"$dot$", "$dash$", "$slash$", "$colon$"});
    }

    public static String getLocalName(String str) {
        int lastIndexOf = str.lastIndexOf(":");
        return lastIndexOf > 0 ? str.substring(lastIndexOf + 1) : str;
    }

    public static String getPrefix(String str) {
        int lastIndexOf = str.lastIndexOf(":");
        return lastIndexOf > 0 ? str.substring(0, lastIndexOf) : "";
    }

    public static boolean isLiteral(String str) {
        int length = str.length();
        for (int i = 0; i < length - 1; i++) {
            if (str.charAt(i) == '{' && str.charAt(i + 1) != '{') {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidQNames(String str) {
        if (str == null || str.equals("")) {
            return true;
        }
        StringTokenizer stringTokenizer = new StringTokenizer(str);
        while (stringTokenizer.hasMoreTokens()) {
            if (!XML11Char.isXML11ValidQName(stringTokenizer.nextToken())) {
                return false;
            }
        }
        return true;
    }
}
