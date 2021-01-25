package ohos.com.sun.org.apache.xml.internal.utils.res;

import java.util.ListResourceBundle;

public class XResourceBundle extends ListResourceBundle {
    public static final String ERROR_RESOURCES = "ohos.com.sun.org.apache.xalan.internal.res.XSLTErrorResources";
    public static final String LANG_ADDITIVE = "additive";
    public static final String LANG_ALPHABET = "alphabet";
    public static final String LANG_BUNDLE_NAME = "com.sun.org.apache.xml.internal.utils.res.XResources";
    public static final String LANG_LEFTTORIGHT = "leftToRight";
    public static final String LANG_MULTIPLIER = "multiplier";
    public static final String LANG_MULTIPLIER_CHAR = "multiplierChar";
    public static final String LANG_MULT_ADD = "multiplicative-additive";
    public static final String LANG_NUMBERGROUPS = "numberGroups";
    public static final String LANG_NUMBERING = "numbering";
    public static final String LANG_NUM_TABLES = "tables";
    public static final String LANG_ORIENTATION = "orientation";
    public static final String LANG_RIGHTTOLEFT = "rightToLeft";
    public static final String LANG_TRAD_ALPHABET = "tradAlphabet";
    public static final String MULT_FOLLOWS = "follows";
    public static final String MULT_ORDER = "multiplierOrder";
    public static final String MULT_PRECEDES = "precedes";
    public static final String XSLT_RESOURCE = "ohos.com.sun.org.apache.xml.internal.utils.res.XResourceBundle";

    @Override // java.util.ListResourceBundle
    public Object[][] getContents() {
        return new Object[][]{new Object[]{"ui_language", "en"}, new Object[]{"help_language", "en"}, new Object[]{"language", "en"}, new Object[]{LANG_ALPHABET, new CharArrayWrapper(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'})}, new Object[]{LANG_TRAD_ALPHABET, new CharArrayWrapper(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'})}, new Object[]{"orientation", "LeftToRight"}, new Object[]{LANG_NUMBERING, LANG_ADDITIVE}};
    }
}
