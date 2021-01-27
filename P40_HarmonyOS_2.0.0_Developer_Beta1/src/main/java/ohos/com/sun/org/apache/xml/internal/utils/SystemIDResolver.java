package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.File;
import ohos.com.sun.org.apache.xml.internal.utils.URI;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.javax.xml.transform.TransformerException;

public class SystemIDResolver {
    public static String getAbsoluteURIFromRelative(String str) {
        String str2;
        String str3;
        if (str == null || str.length() == 0) {
            return "";
        }
        if (!isAbsolutePath(str)) {
            try {
                str2 = getAbsolutePathFromRelativePath(str);
            } catch (SecurityException unused) {
                return "file:" + str;
            }
        } else {
            str2 = str;
        }
        if (str2 == null) {
            str3 = "file:" + str;
        } else if (str2.startsWith(File.separator)) {
            str3 = "file://" + str2;
        } else {
            str3 = "file:///" + str2;
        }
        return replaceChars(str3);
    }

    private static String getAbsolutePathFromRelativePath(String str) {
        return new File(str).getAbsolutePath();
    }

    public static boolean isAbsoluteURI(String str) {
        if (isWindowsAbsolutePath(str)) {
            return false;
        }
        int indexOf = str.indexOf(35);
        int indexOf2 = str.indexOf(63);
        int indexOf3 = str.indexOf(47);
        int indexOf4 = str.indexOf(58);
        int length = str.length() - 1;
        if (indexOf > 0) {
            length = indexOf;
        }
        if (indexOf2 > 0 && indexOf2 < length) {
            length = indexOf2;
        }
        if (indexOf3 > 0 && indexOf3 < length) {
            length = indexOf3;
        }
        if (indexOf4 <= 0 || indexOf4 >= length) {
            return false;
        }
        return true;
    }

    public static boolean isAbsolutePath(String str) {
        if (str == null) {
            return false;
        }
        return new File(str).isAbsolute();
    }

    private static boolean isWindowsAbsolutePath(String str) {
        if (isAbsolutePath(str) && str.length() > 2 && str.charAt(1) == ':' && Character.isLetter(str.charAt(0)) && (str.charAt(2) == '\\' || str.charAt(2) == '/')) {
            return true;
        }
        return false;
    }

    private static String replaceChars(String str) {
        StringBuffer stringBuffer = new StringBuffer(str);
        int length = stringBuffer.length();
        int i = 0;
        while (i < length) {
            char charAt = stringBuffer.charAt(i);
            if (charAt == ' ') {
                stringBuffer.setCharAt(i, '%');
                stringBuffer.insert(i + 1, "20");
                length += 2;
                i += 2;
            } else if (charAt == '\\') {
                stringBuffer.setCharAt(i, '/');
            }
            i++;
        }
        return stringBuffer.toString();
    }

    public static String getAbsoluteURI(String str) {
        int indexOf;
        if (!isAbsoluteURI(str)) {
            return getAbsoluteURIFromRelative(str);
        }
        if (!str.startsWith("file:")) {
            return str;
        }
        String substring = str.substring(5);
        if (substring == null || !substring.startsWith(PsuedoNames.PSEUDONAME_ROOT)) {
            return getAbsoluteURIFromRelative(str.substring(5));
        }
        if ((substring.startsWith("///") || !substring.startsWith("//")) && (indexOf = str.indexOf(58, 5)) > 0) {
            int i = indexOf - 1;
            String substring2 = str.substring(i);
            try {
                if (!isAbsolutePath(substring2)) {
                    str = str.substring(0, i) + getAbsolutePathFromRelativePath(substring2);
                }
            } catch (SecurityException unused) {
                return str;
            }
        }
        return replaceChars(str);
    }

    public static String getAbsoluteURI(String str, String str2) throws TransformerException {
        if (str2 == null) {
            return getAbsoluteURI(str);
        }
        try {
            return replaceChars(new URI(new URI(getAbsoluteURI(str2)), str).toString());
        } catch (URI.MalformedURIException e) {
            throw new TransformerException(e);
        }
    }
}
