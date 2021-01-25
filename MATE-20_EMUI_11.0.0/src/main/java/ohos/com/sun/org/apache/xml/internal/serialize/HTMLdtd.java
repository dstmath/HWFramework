package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ohos.agp.styles.attributes.AbsButtonAttrsConstants;
import ohos.ai.engine.resultcode.HwHiAIResultCode;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.devtools.JLogConstants;
import ohos.global.icu.lang.UProperty;

public final class HTMLdtd {
    private static final int ALLOWED_HEAD = 32;
    private static final int CLOSE_DD_DT = 128;
    private static final int CLOSE_P = 64;
    private static final int CLOSE_SELF = 256;
    private static final int CLOSE_TABLE = 512;
    private static final int CLOSE_TH_TD = 16384;
    private static final int ELEM_CONTENT = 2;
    private static final int EMPTY = 17;
    private static final String ENTITIES_RESOURCE = "HTMLEntities.res";
    public static final String HTMLPublicId = "-//W3C//DTD HTML 4.01//EN";
    public static final String HTMLSystemId = "http://www.w3.org/TR/html4/strict.dtd";
    private static final int ONLY_OPENING = 1;
    private static final int OPT_CLOSING = 8;
    private static final int PRESERVE = 4;
    public static final String XHTMLPublicId = "-//W3C//DTD XHTML 1.0 Strict//EN";
    public static final String XHTMLSystemId = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
    private static final Map<String, String[]> _boolAttrs = new HashMap();
    private static Map<Integer, String> _byChar;
    private static Map<String, Integer> _byName;
    private static final Map<String, Integer> _elemDefs = new HashMap();

    public static boolean isEmptyTag(String str) {
        return isElement(str, 17);
    }

    public static boolean isElementContent(String str) {
        return isElement(str, 2);
    }

    public static boolean isPreserveSpace(String str) {
        return isElement(str, 4);
    }

    public static boolean isOptionalClosing(String str) {
        return isElement(str, 8);
    }

    public static boolean isOnlyOpening(String str) {
        return isElement(str, 1);
    }

    public static boolean isClosing(String str, String str2) {
        if (str2.equalsIgnoreCase("HEAD")) {
            return !isElement(str, 32);
        }
        if (str2.equalsIgnoreCase("P")) {
            return isElement(str, 64);
        }
        if (str2.equalsIgnoreCase("DT") || str2.equalsIgnoreCase("DD")) {
            return isElement(str, 128);
        }
        if (str2.equalsIgnoreCase("LI") || str2.equalsIgnoreCase("OPTION")) {
            return isElement(str, 256);
        }
        if (str2.equalsIgnoreCase("THEAD") || str2.equalsIgnoreCase("TFOOT") || str2.equalsIgnoreCase("TBODY") || str2.equalsIgnoreCase("TR") || str2.equalsIgnoreCase("COLGROUP")) {
            return isElement(str, 512);
        }
        if (str2.equalsIgnoreCase("TH") || str2.equalsIgnoreCase("TD")) {
            return isElement(str, 16384);
        }
        return false;
    }

    public static boolean isURI(String str, String str2) {
        return str2.equalsIgnoreCase(Constants.ATTRNAME_HREF) || str2.equalsIgnoreCase("src");
    }

    public static boolean isBoolean(String str, String str2) {
        String[] strArr = _boolAttrs.get(str.toUpperCase(Locale.ENGLISH));
        if (strArr == null) {
            return false;
        }
        for (String str3 : strArr) {
            if (str3.equalsIgnoreCase(str2)) {
                return true;
            }
        }
        return false;
    }

    public static int charFromName(String str) {
        initialize();
        Integer num = _byName.get(str);
        if (num == null || !(num instanceof Integer)) {
            return -1;
        }
        return num.intValue();
    }

    public static String fromChar(int i) {
        if (i > 65535) {
            return null;
        }
        initialize();
        return _byChar.get(Integer.valueOf(i));
    }

    private static void initialize() {
        if (_byName == null) {
            InputStream inputStream = null;
            try {
                _byName = new HashMap();
                _byChar = new HashMap();
                InputStream resourceAsStream = HTMLdtd.class.getResourceAsStream(ENTITIES_RESOURCE);
                if (resourceAsStream != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, "ASCII"));
                    String readLine = bufferedReader.readLine();
                    while (readLine != null) {
                        if (readLine.length() != 0) {
                            if (readLine.charAt(0) != '#') {
                                int indexOf = readLine.indexOf(32);
                                if (indexOf > 1) {
                                    String substring = readLine.substring(0, indexOf);
                                    int i = indexOf + 1;
                                    if (i < readLine.length()) {
                                        String substring2 = readLine.substring(i);
                                        int indexOf2 = substring2.indexOf(32);
                                        if (indexOf2 > 0) {
                                            substring2 = substring2.substring(0, indexOf2);
                                        }
                                        defineEntity(substring, (char) Integer.parseInt(substring2));
                                    }
                                }
                                readLine = bufferedReader.readLine();
                            }
                        }
                        readLine = bufferedReader.readLine();
                    }
                    resourceAsStream.close();
                    try {
                        resourceAsStream.close();
                    } catch (Exception unused) {
                    }
                } else {
                    throw new RuntimeException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "ResourceNotFound", new Object[]{ENTITIES_RESOURCE}));
                }
            } catch (Exception e) {
                throw new RuntimeException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "ResourceNotLoaded", new Object[]{ENTITIES_RESOURCE, e.toString()}));
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (Exception unused2) {
                    }
                }
                throw th;
            }
        }
    }

    private static void defineEntity(String str, char c) {
        if (_byName.get(str) == null) {
            _byName.put(str, new Integer(c));
            _byChar.put(new Integer(c), str);
        }
    }

    private static void defineElement(String str, int i) {
        _elemDefs.put(str, Integer.valueOf(i));
    }

    private static void defineBoolean(String str, String str2) {
        defineBoolean(str, new String[]{str2});
    }

    private static void defineBoolean(String str, String[] strArr) {
        _boolAttrs.put(str, strArr);
    }

    private static boolean isElement(String str, int i) {
        Integer num = _elemDefs.get(str.toUpperCase(Locale.ENGLISH));
        if (num != null && (num.intValue() & i) == i) {
            return true;
        }
        return false;
    }

    static {
        defineElement("ADDRESS", 64);
        defineElement("AREA", 17);
        defineElement("BASE", 49);
        defineElement("BASEFONT", 17);
        defineElement("BLOCKQUOTE", 64);
        defineElement("BODY", 8);
        defineElement("BR", 17);
        defineElement("COL", 17);
        defineElement("COLGROUP", HwHiAIResultCode.AIRESULT_SERVICE_BIND_CONNECTED);
        defineElement("DD", 137);
        defineElement("DIV", 64);
        defineElement("DL", 66);
        defineElement("DT", 137);
        defineElement("FIELDSET", 64);
        defineElement("FORM", 64);
        defineElement("FRAME", 25);
        defineElement("H1", 64);
        defineElement("H2", 64);
        defineElement("H3", 64);
        defineElement("H4", 64);
        defineElement("H5", 64);
        defineElement("H6", 64);
        defineElement("HEAD", 10);
        defineElement("HR", 81);
        defineElement("HTML", 10);
        defineElement("IMG", 17);
        defineElement("INPUT", 17);
        defineElement("ISINDEX", 49);
        defineElement("LI", 265);
        defineElement("LINK", 49);
        defineElement("MAP", 32);
        defineElement("META", 49);
        defineElement("OL", 66);
        defineElement("OPTGROUP", 2);
        defineElement("OPTION", 265);
        defineElement("P", JLogConstants.JLID_CAMERA3_HAL_CAF_BEGIN);
        defineElement("PARAM", 17);
        defineElement("PRE", 68);
        defineElement("SCRIPT", 36);
        defineElement("NOSCRIPT", 36);
        defineElement("SELECT", 2);
        defineElement("STYLE", 36);
        defineElement("TABLE", 66);
        defineElement("TBODY", HwHiAIResultCode.AIRESULT_SERVICE_BIND_CONNECTED);
        defineElement("TD", UProperty.SIMPLE_TITLECASE_MAPPING);
        defineElement("TEXTAREA", 4);
        defineElement("TFOOT", HwHiAIResultCode.AIRESULT_SERVICE_BIND_CONNECTED);
        defineElement("TH", UProperty.SIMPLE_TITLECASE_MAPPING);
        defineElement("THEAD", HwHiAIResultCode.AIRESULT_SERVICE_BIND_CONNECTED);
        defineElement("TITLE", 32);
        defineElement("TR", HwHiAIResultCode.AIRESULT_SERVICE_BIND_CONNECTED);
        defineElement("UL", 66);
        defineBoolean("AREA", Constants.ATTRNAME_HREF);
        defineBoolean("BUTTON", "disabled");
        defineBoolean("DIR", "compact");
        defineBoolean("DL", "compact");
        defineBoolean("FRAME", "noresize");
        defineBoolean("HR", "noshade");
        defineBoolean("IMAGE", "ismap");
        defineBoolean("INPUT", new String[]{"defaultchecked", AbsButtonAttrsConstants.CHECKED, "readonly", "disabled"});
        defineBoolean("LINK", "link");
        defineBoolean("MENU", "compact");
        defineBoolean("OBJECT", "declare");
        defineBoolean("OL", "compact");
        defineBoolean("OPTGROUP", "disabled");
        defineBoolean("OPTION", new String[]{"default-selected", "selected", "disabled"});
        defineBoolean("SCRIPT", "defer");
        defineBoolean("SELECT", new String[]{Constants.ATTRVAL_MULTI, "disabled"});
        defineBoolean("STYLE", "disabled");
        defineBoolean("TD", "nowrap");
        defineBoolean("TH", "nowrap");
        defineBoolean("TEXTAREA", new String[]{"disabled", "readonly"});
        defineBoolean("UL", "compact");
        initialize();
    }
}
