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

    /* synthetic */ CharInfo(String entitiesResource, String method, boolean internal, CharInfo -this3) {
        this(entitiesResource, method, internal);
    }

    private CharInfo() {
        this.array_of_bits = createEmptySetOfIntegers(DTMManager.IDENT_NODE_DEFAULT);
        this.firstWordNotUsed = 0;
        this.shouldMapAttrChar_ASCII = new boolean[128];
        this.shouldMapTextChar_ASCII = new boolean[128];
        this.m_charKey = new CharKey();
        this.onlyQuotAmpLtGt = true;
    }

    private CharInfo(String entitiesResource, String method, boolean internal) {
        this();
        this.m_charToString = new HashMap();
        ResourceBundle entities = null;
        boolean noExtraEntities = true;
        if (internal) {
            try {
                entities = PropertyResourceBundle.getBundle(entitiesResource);
            } catch (Exception e) {
            }
        }
        String name;
        if (entities != null) {
            Enumeration keys = entities.getKeys();
            while (keys.hasMoreElements()) {
                name = (String) keys.nextElement();
                if (defineEntity(name, (char) Integer.parseInt(entities.getString(name)))) {
                    noExtraEntities = false;
                }
            }
        } else {
            InputStream is = null;
            if (internal) {
                try {
                    is = CharInfo.class.getResourceAsStream(entitiesResource);
                } catch (Exception e2) {
                    throw new RuntimeException(Utils.messages.createMessage("ER_RESOURCE_COULD_NOT_LOAD", new Object[]{entitiesResource, e2.toString(), entitiesResource, e2.toString()}));
                } catch (Throwable th) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e3) {
                        }
                    }
                }
            } else {
                ClassLoader cl = ObjectFactory.findClassLoader();
                if (cl == null) {
                    is = ClassLoader.getSystemResourceAsStream(entitiesResource);
                } else {
                    is = cl.getResourceAsStream(entitiesResource);
                }
                if (is == null) {
                    try {
                        is = new URL(entitiesResource).openStream();
                    } catch (Exception e4) {
                    }
                }
            }
            if (is == null) {
                throw new RuntimeException(Utils.messages.createMessage("ER_RESOURCE_COULD_NOT_FIND", new Object[]{entitiesResource, entitiesResource}));
            }
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            } catch (UnsupportedEncodingException e5) {
                reader = new BufferedReader(new InputStreamReader(is));
            }
            String line = reader.readLine();
            while (line != null) {
                if (line.length() == 0 || line.charAt(0) == '#') {
                    line = reader.readLine();
                } else {
                    int index = line.indexOf(32);
                    if (index > 1) {
                        name = line.substring(0, index);
                        index++;
                        if (index < line.length()) {
                            String value = line.substring(index);
                            index = value.indexOf(32);
                            if (index > 0) {
                                value = value.substring(0, index);
                            }
                            if (defineEntity(name, (char) Integer.parseInt(value))) {
                                noExtraEntities = false;
                            }
                        }
                    }
                    line = reader.readLine();
                }
            }
            is.close();
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e6) {
                }
            }
        }
        this.onlyQuotAmpLtGt = noExtraEntities;
        if ("xml".equals(method)) {
            this.shouldMapTextChar_ASCII[34] = false;
        }
        if ("html".equals(method)) {
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

    String getOutputStringForChar(char value) {
        this.m_charKey.setChar(value);
        return (String) this.m_charToString.get(this.m_charKey);
    }

    final boolean shouldMapAttrChar(int value) {
        if (value < 128) {
            return this.shouldMapAttrChar_ASCII[value];
        }
        return get(value);
    }

    final boolean shouldMapTextChar(int value) {
        if (value < 128) {
            return this.shouldMapTextChar_ASCII[value];
        }
        return get(value);
    }

    private static CharInfo getCharInfoBasedOnPrivilege(final String entitiesFileName, final String method, final boolean internal) {
        return (CharInfo) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new CharInfo(entitiesFileName, method, internal, null);
            }
        });
    }

    static CharInfo getCharInfo(String entitiesFileName, String method) {
        CharInfo charInfo = (CharInfo) m_getCharInfoCache.get(entitiesFileName);
        if (charInfo != null) {
            return mutableCopyOf(charInfo);
        }
        try {
            charInfo = getCharInfoBasedOnPrivilege(entitiesFileName, method, true);
            m_getCharInfoCache.put(entitiesFileName, charInfo);
            return mutableCopyOf(charInfo);
        } catch (Exception e) {
            try {
                return getCharInfoBasedOnPrivilege(entitiesFileName, method, false);
            } catch (Exception e2) {
                String absoluteEntitiesFileName;
                if (entitiesFileName.indexOf(58) < 0) {
                    absoluteEntitiesFileName = SystemIDResolver.getAbsoluteURIFromRelative(entitiesFileName);
                } else {
                    try {
                        absoluteEntitiesFileName = SystemIDResolver.getAbsoluteURI(entitiesFileName, null);
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
        if (j < this.firstWordNotUsed) {
            return (this.array_of_bits[j] & (1 << (i & 31))) != 0;
        } else {
            return false;
        }
    }

    private boolean extraEntity(String outputString, int charToMap) {
        if (charToMap >= 128) {
            return false;
        }
        switch (charToMap) {
            case 34:
                if (outputString.equals(SerializerConstants.ENTITY_QUOT)) {
                    return false;
                }
                return true;
            case 38:
                if (outputString.equals(SerializerConstants.ENTITY_AMP)) {
                    return false;
                }
                return true;
            case 60:
                if (outputString.equals(SerializerConstants.ENTITY_LT)) {
                    return false;
                }
                return true;
            case 62:
                if (outputString.equals(SerializerConstants.ENTITY_GT)) {
                    return false;
                }
                return true;
            default:
                return true;
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

    boolean defineChar2StringMapping(String outputString, char inputChar) {
        this.m_charToString.put(new CharKey(inputChar), outputString);
        set(inputChar);
        return extraEntity(outputString, inputChar);
    }
}
