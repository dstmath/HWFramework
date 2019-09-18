package org.apache.xml.serializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.serializer.utils.SystemIDResolver;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;

final class CharInfo {
    static final int ASCII_MAX = 128;
    public static final String HTML_ENTITIES_RESOURCE = (SerializerBase.PKG_NAME + ".HTMLEntities");
    private static final int LOW_ORDER_BITMASK = 31;
    private static final int SHIFT_PER_WORD = 5;
    static final char S_CARRIAGERETURN = '\r';
    static final char S_GT = '>';
    static final char S_HORIZONAL_TAB = '\t';
    static final char S_LINEFEED = '\n';
    static final char S_LINE_SEPARATOR = ' ';
    static final char S_LT = '<';
    static final char S_NEL = '';
    static final char S_QUOTE = '\"';
    static final char S_SPACE = ' ';
    public static final String XML_ENTITIES_RESOURCE = (SerializerBase.PKG_NAME + ".XMLEntities");
    private static Hashtable m_getCharInfoCache = new Hashtable();
    private final int[] array_of_bits;
    private int firstWordNotUsed;
    private final CharKey m_charKey;
    private HashMap m_charToString;
    boolean onlyQuotAmpLtGt;
    private final boolean[] shouldMapAttrChar_ASCII;
    private final boolean[] shouldMapTextChar_ASCII;

    private static class CharKey {
        private char m_char;

        public CharKey(char key) {
            this.m_char = key;
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

    private CharInfo() {
        this.array_of_bits = createEmptySetOfIntegers(DTMManager.IDENT_NODE_DEFAULT);
        this.firstWordNotUsed = 0;
        this.shouldMapAttrChar_ASCII = new boolean[128];
        this.shouldMapTextChar_ASCII = new boolean[128];
        this.m_charKey = new CharKey();
        this.onlyQuotAmpLtGt = true;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0079 A[SYNTHETIC, Splitter:B:35:0x0079] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0114 A[SYNTHETIC, Splitter:B:72:0x0114] */
    private CharInfo(String entitiesResource, String method, boolean internal) {
        this();
        BufferedReader reader;
        InputStream is;
        InputStream openStream;
        String str = entitiesResource;
        String str2 = method;
        this.m_charToString = new HashMap();
        ResourceBundle entities = null;
        boolean noExtraEntities = true;
        if (internal) {
            try {
                entities = PropertyResourceBundle.getBundle(entitiesResource);
            } catch (Exception e) {
            }
        }
        if (entities != null) {
            Enumeration keys = entities.getKeys();
            while (keys.hasMoreElements()) {
                String name = keys.nextElement();
                if (defineEntity(name, (char) Integer.parseInt(entities.getString(name)))) {
                    noExtraEntities = false;
                }
            }
        } else {
            InputStream is2 = null;
            int i = 1;
            if (internal) {
                try {
                    openStream = CharInfo.class.getResourceAsStream(str);
                } catch (Exception e2) {
                    throw new RuntimeException(Utils.messages.createMessage("ER_RESOURCE_COULD_NOT_LOAD", new Object[]{str, e2.toString(), str, e2.toString()}));
                } catch (Throwable th) {
                    Throwable th2 = th;
                    if (is2 != null) {
                        try {
                            is2.close();
                        } catch (Exception e3) {
                        }
                    }
                    throw th2;
                }
            } else {
                ClassLoader cl = ObjectFactory.findClassLoader();
                if (cl == null) {
                    is = ClassLoader.getSystemResourceAsStream(entitiesResource);
                } else {
                    is = cl.getResourceAsStream(str);
                }
                is2 = is;
                if (is2 == null) {
                    try {
                        openStream = new URL(str).openStream();
                    } catch (Exception e4) {
                    }
                }
                if (is2 == null) {
                    try {
                        reader = new BufferedReader(new InputStreamReader(is2, "UTF-8"));
                    } catch (UnsupportedEncodingException e5) {
                        reader = new BufferedReader(new InputStreamReader(is2));
                    }
                    BufferedReader reader2 = reader;
                    String line = reader2.readLine();
                    while (line != null) {
                        if (line.length() != 0) {
                            if (line.charAt(0) != '#') {
                                int index = line.indexOf(32);
                                if (index > i) {
                                    String name2 = line.substring(0, index);
                                    int index2 = index + 1;
                                    if (index2 < line.length()) {
                                        String value = line.substring(index2);
                                        int index3 = value.indexOf(32);
                                        if (defineEntity(name2, (char) Integer.parseInt(index3 > 0 ? value.substring(0, index3) : value))) {
                                            noExtraEntities = false;
                                        }
                                    }
                                }
                                line = reader2.readLine();
                                i = 1;
                            }
                        }
                        line = reader2.readLine();
                        i = 1;
                    }
                    is2.close();
                    if (is2 != null) {
                        try {
                            is2.close();
                        } catch (Exception e6) {
                        }
                    }
                } else {
                    throw new RuntimeException(Utils.messages.createMessage("ER_RESOURCE_COULD_NOT_FIND", new Object[]{str, str}));
                }
            }
            is2 = openStream;
            if (is2 == null) {
            }
        }
        this.onlyQuotAmpLtGt = noExtraEntities;
        if ("xml".equals(str2)) {
            this.shouldMapTextChar_ASCII[34] = false;
        }
        if ("html".equals(str2)) {
            this.shouldMapAttrChar_ASCII[60] = false;
            this.shouldMapTextChar_ASCII[34] = false;
        }
    }

    private boolean defineEntity(String name, char value) {
        StringBuffer sb = new StringBuffer("&");
        sb.append(name);
        sb.append(';');
        return defineChar2StringMapping(sb.toString(), value);
    }

    /* access modifiers changed from: package-private */
    public String getOutputStringForChar(char value) {
        this.m_charKey.setChar(value);
        return (String) this.m_charToString.get(this.m_charKey);
    }

    /* access modifiers changed from: package-private */
    public final boolean shouldMapAttrChar(int value) {
        if (value < 128) {
            return this.shouldMapAttrChar_ASCII[value];
        }
        return get(value);
    }

    /* access modifiers changed from: package-private */
    public final boolean shouldMapTextChar(int value) {
        if (value < 128) {
            return this.shouldMapTextChar_ASCII[value];
        }
        return get(value);
    }

    private static CharInfo getCharInfoBasedOnPrivilege(final String entitiesFileName, final String method, final boolean internal) {
        return (CharInfo) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new CharInfo(entitiesFileName, method, internal);
            }
        });
    }

    static CharInfo getCharInfo(String entitiesFileName, String method) {
        CharInfo charInfo = (CharInfo) m_getCharInfoCache.get(entitiesFileName);
        if (charInfo != null) {
            return mutableCopyOf(charInfo);
        }
        try {
            CharInfo charInfo2 = getCharInfoBasedOnPrivilege(entitiesFileName, method, true);
            m_getCharInfoCache.put(entitiesFileName, charInfo2);
            return mutableCopyOf(charInfo2);
        } catch (Exception e) {
            try {
                return getCharInfoBasedOnPrivilege(entitiesFileName, method, false);
            } catch (Exception e2) {
                if (entitiesFileName.indexOf(58) < 0) {
                    String absoluteURIFromRelative = SystemIDResolver.getAbsoluteURIFromRelative(entitiesFileName);
                } else {
                    try {
                        String absoluteURI = SystemIDResolver.getAbsoluteURI(entitiesFileName, null);
                    } catch (TransformerException te) {
                        throw new WrappedRuntimeException(te);
                    }
                }
                return getCharInfoBasedOnPrivilege(entitiesFileName, method, false);
            }
        }
    }

    private static CharInfo mutableCopyOf(CharInfo charInfo) {
        CharInfo copy = new CharInfo();
        System.arraycopy(charInfo.array_of_bits, 0, copy.array_of_bits, 0, charInfo.array_of_bits.length);
        copy.firstWordNotUsed = charInfo.firstWordNotUsed;
        System.arraycopy(charInfo.shouldMapAttrChar_ASCII, 0, copy.shouldMapAttrChar_ASCII, 0, charInfo.shouldMapAttrChar_ASCII.length);
        System.arraycopy(charInfo.shouldMapTextChar_ASCII, 0, copy.shouldMapTextChar_ASCII, 0, charInfo.shouldMapTextChar_ASCII.length);
        copy.m_charToString = (HashMap) charInfo.m_charToString.clone();
        copy.onlyQuotAmpLtGt = charInfo.onlyQuotAmpLtGt;
        return copy;
    }

    private static int arrayIndex(int i) {
        return i >> 5;
    }

    private static int bit(int i) {
        return 1 << (i & 31);
    }

    private int[] createEmptySetOfIntegers(int max) {
        this.firstWordNotUsed = 0;
        return new int[(arrayIndex(max - 1) + 1)];
    }

    private final void set(int i) {
        setASCIItextDirty(i);
        setASCIIattrDirty(i);
        int j = i >> 5;
        int k = j + 1;
        if (this.firstWordNotUsed < k) {
            this.firstWordNotUsed = k;
        }
        int[] iArr = this.array_of_bits;
        iArr[j] = iArr[j] | (1 << (i & 31));
    }

    private final boolean get(int i) {
        int j = i >> 5;
        if (j >= this.firstWordNotUsed) {
            return false;
        }
        boolean in_the_set = true;
        if ((this.array_of_bits[j] & (1 << (i & 31))) == 0) {
            in_the_set = false;
        }
        return in_the_set;
    }

    private boolean extraEntity(String outputString, int charToMap) {
        if (charToMap >= 128) {
            return false;
        }
        if (charToMap != 34) {
            if (charToMap != 38) {
                if (charToMap != 60) {
                    if (charToMap != 62) {
                        return true;
                    }
                    if (!outputString.equals(SerializerConstants.ENTITY_GT)) {
                        return true;
                    }
                    return false;
                } else if (!outputString.equals(SerializerConstants.ENTITY_LT)) {
                    return true;
                } else {
                    return false;
                }
            } else if (!outputString.equals(SerializerConstants.ENTITY_AMP)) {
                return true;
            } else {
                return false;
            }
        } else if (!outputString.equals(SerializerConstants.ENTITY_QUOT)) {
            return true;
        } else {
            return false;
        }
    }

    private void setASCIItextDirty(int j) {
        if (j >= 0 && j < 128) {
            this.shouldMapTextChar_ASCII[j] = true;
        }
    }

    private void setASCIIattrDirty(int j) {
        if (j >= 0 && j < 128) {
            this.shouldMapAttrChar_ASCII[j] = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean defineChar2StringMapping(String outputString, char inputChar) {
        this.m_charToString.put(new CharKey(inputChar), outputString);
        set(inputChar);
        return extraEntity(outputString, inputChar);
    }
}
