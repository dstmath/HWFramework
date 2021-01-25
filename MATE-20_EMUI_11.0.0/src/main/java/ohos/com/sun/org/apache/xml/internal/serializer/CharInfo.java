package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.Utils;
import ohos.com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException;
import ohos.javax.xml.transform.TransformerException;

/* access modifiers changed from: package-private */
public final class CharInfo {
    private static final int ASCII_MAX = 128;
    public static final String HTML_ENTITIES_RESOURCE = "com.sun.org.apache.xml.internal.serializer.HTMLEntities";
    private static final int LOW_ORDER_BITMASK = 31;
    private static final int SHIFT_PER_WORD = 5;
    public static final char S_CARRIAGERETURN = '\r';
    public static final char S_HORIZONAL_TAB = '\t';
    public static final char S_LINEFEED = '\n';
    public static final String XML_ENTITIES_RESOURCE = "com.sun.org.apache.xml.internal.serializer.XMLEntities";
    private static HashMap m_getCharInfoCache = new HashMap();
    private int[] array_of_bits;
    private int firstWordNotUsed;
    private boolean[] isCleanTextASCII;
    private boolean[] isSpecialAttrASCII;
    private boolean[] isSpecialTextASCII;
    private HashMap m_charToString;
    final boolean onlyQuotAmpLtGt;

    private static int arrayIndex(int i) {
        return i >> 5;
    }

    private static int bit(int i) {
        return 1 << (i & 31);
    }

    private boolean extraEntity(int i) {
        return (i >= 128 || i == 34 || i == 38 || i == 60 || i == 62) ? false : true;
    }

    private CharInfo(String str, String str2) {
        this(str, str2, false);
    }

    private CharInfo(String str, String str2, boolean z) {
        ResourceBundle resourceBundle;
        boolean z2;
        String str3;
        BufferedReader bufferedReader;
        this.m_charToString = new HashMap();
        this.isSpecialAttrASCII = new boolean[128];
        this.isSpecialTextASCII = new boolean[128];
        this.isCleanTextASCII = new boolean[128];
        this.array_of_bits = createEmptySetOfIntegers(65535);
        InputStream inputStream = null;
        if (z) {
            try {
                resourceBundle = PropertyResourceBundle.getBundle(str);
            } catch (Exception unused) {
            }
        } else {
            ClassLoader contextClassLoader = SecuritySupport.getContextClassLoader();
            if (contextClassLoader != null) {
                resourceBundle = PropertyResourceBundle.getBundle(str, Locale.getDefault(), contextClassLoader);
            }
            resourceBundle = null;
        }
        if (resourceBundle != null) {
            Enumeration<String> keys = resourceBundle.getKeys();
            boolean z3 = true;
            while (keys.hasMoreElements()) {
                String nextElement = keys.nextElement();
                int parseInt = Integer.parseInt(resourceBundle.getString(nextElement));
                defineEntity(nextElement, (char) parseInt);
                if (extraEntity(parseInt)) {
                    z3 = false;
                }
            }
            set(10);
            set(13);
            z2 = z3;
        } else {
            if (z) {
                try {
                    inputStream = CharInfo.class.getResourceAsStream(str);
                    str3 = null;
                } catch (Exception e) {
                    throw new RuntimeException(Utils.messages.createMessage("ER_RESOURCE_COULD_NOT_LOAD", new Object[]{str, e.toString(), str, e.toString()}));
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            inputStream.close();
                        } catch (Exception unused2) {
                        }
                    }
                    throw th;
                }
            } else {
                ClassLoader contextClassLoader2 = SecuritySupport.getContextClassLoader();
                if (contextClassLoader2 != null) {
                    try {
                        inputStream = contextClassLoader2.getResourceAsStream(str);
                        str3 = null;
                    } catch (Exception e2) {
                        str3 = e2.getMessage();
                    }
                } else {
                    str3 = null;
                }
                if (inputStream == null) {
                    try {
                        inputStream = new URL(str).openStream();
                    } catch (Exception e3) {
                        str3 = e3.getMessage();
                    }
                }
            }
            if (inputStream != null) {
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                } catch (UnsupportedEncodingException unused3) {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                }
                String readLine = bufferedReader.readLine();
                z2 = true;
                while (readLine != null) {
                    if (readLine.length() == 0 || readLine.charAt(0) == '#') {
                        readLine = bufferedReader.readLine();
                    } else {
                        int indexOf = readLine.indexOf(32);
                        if (indexOf > 1) {
                            String substring = readLine.substring(0, indexOf);
                            int i = indexOf + 1;
                            if (i < readLine.length()) {
                                String substring2 = readLine.substring(i);
                                int indexOf2 = substring2.indexOf(32);
                                int parseInt2 = Integer.parseInt(indexOf2 > 0 ? substring2.substring(0, indexOf2) : substring2);
                                defineEntity(substring, (char) parseInt2);
                                if (extraEntity(parseInt2)) {
                                    z2 = false;
                                }
                            }
                        }
                        readLine = bufferedReader.readLine();
                    }
                }
                inputStream.close();
                set(10);
                set(13);
                try {
                    inputStream.close();
                } catch (Exception unused4) {
                }
            } else {
                throw new RuntimeException(Utils.messages.createMessage("ER_RESOURCE_COULD_NOT_FIND", new Object[]{str, str3}));
            }
        }
        for (int i2 = 0; i2 < 128; i2++) {
            if (((32 <= i2 || 10 == i2 || 13 == i2 || 9 == i2) && !get(i2)) || 34 == i2) {
                this.isCleanTextASCII[i2] = true;
                this.isSpecialTextASCII[i2] = false;
            } else {
                this.isCleanTextASCII[i2] = false;
                this.isSpecialTextASCII[i2] = true;
            }
        }
        this.onlyQuotAmpLtGt = z2;
        for (int i3 = 0; i3 < 128; i3++) {
            this.isSpecialAttrASCII[i3] = get(i3);
        }
        if ("xml".equals(str2)) {
            this.isSpecialAttrASCII[9] = true;
        }
    }

    private void defineEntity(String str, char c) {
        defineChar2StringMapping("&" + str + ';', c);
    }

    /* access modifiers changed from: package-private */
    public String getOutputStringForChar(char c) {
        CharKey charKey = new CharKey();
        charKey.setChar(c);
        return (String) this.m_charToString.get(charKey);
    }

    /* access modifiers changed from: package-private */
    public final boolean isSpecialAttrChar(int i) {
        if (i < 128) {
            return this.isSpecialAttrASCII[i];
        }
        return get(i);
    }

    /* access modifiers changed from: package-private */
    public final boolean isSpecialTextChar(int i) {
        if (i < 128) {
            return this.isSpecialTextASCII[i];
        }
        return get(i);
    }

    /* access modifiers changed from: package-private */
    public final boolean isTextASCIIClean(int i) {
        return this.isCleanTextASCII[i];
    }

    static CharInfo getCharInfoInternal(String str, String str2) {
        CharInfo charInfo = (CharInfo) m_getCharInfoCache.get(str);
        if (charInfo != null) {
            return charInfo;
        }
        CharInfo charInfo2 = new CharInfo(str, str2, true);
        m_getCharInfoCache.put(str, charInfo2);
        return charInfo2;
    }

    static CharInfo getCharInfo(String str, String str2) {
        String str3;
        try {
            return new CharInfo(str, str2, false);
        } catch (Exception unused) {
            if (str.indexOf(58) < 0) {
                str3 = SystemIDResolver.getAbsoluteURIFromRelative(str);
            } else {
                try {
                    str3 = SystemIDResolver.getAbsoluteURI(str, null);
                } catch (TransformerException e) {
                    throw new WrappedRuntimeException(e);
                }
            }
            return new CharInfo(str3, str2, false);
        }
    }

    private int[] createEmptySetOfIntegers(int i) {
        this.firstWordNotUsed = 0;
        return new int[(arrayIndex(i - 1) + 1)];
    }

    private final void set(int i) {
        setASCIIdirty(i);
        int i2 = i >> 5;
        int i3 = i2 + 1;
        if (this.firstWordNotUsed < i3) {
            this.firstWordNotUsed = i3;
        }
        int[] iArr = this.array_of_bits;
        iArr[i2] = (1 << (i & 31)) | iArr[i2];
    }

    private final boolean get(int i) {
        int i2 = i >> 5;
        if (i2 >= this.firstWordNotUsed || (this.array_of_bits[i2] & (1 << (i & 31))) == 0) {
            return false;
        }
        return true;
    }

    private void setASCIIdirty(int i) {
        if (i >= 0 && i < 128) {
            this.isCleanTextASCII[i] = false;
            this.isSpecialTextASCII[i] = true;
        }
    }

    private void setASCIIclean(int i) {
        if (i >= 0 && i < 128) {
            this.isCleanTextASCII[i] = true;
            this.isSpecialTextASCII[i] = false;
        }
    }

    private void defineChar2StringMapping(String str, char c) {
        this.m_charToString.put(new CharKey(c), str);
        set(c);
    }

    /* access modifiers changed from: private */
    public static class CharKey {
        private char m_char;

        public CharKey(char c) {
            this.m_char = c;
        }

        public CharKey() {
        }

        public final void setChar(char c) {
            this.m_char = c;
        }

        public final int hashCode() {
            return this.m_char;
        }

        public final boolean equals(Object obj) {
            return ((CharKey) obj).m_char == this.m_char;
        }
    }
}
